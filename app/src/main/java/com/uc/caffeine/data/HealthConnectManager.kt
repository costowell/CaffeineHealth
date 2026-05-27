package com.uc.caffeine.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.metadata.Metadata as HCMetadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Mass
import com.uc.caffeine.data.model.ConsumptionEntry
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class ImportedCaffeineRecord(
    val healthConnectRecordId: String,
    val caffeineMg: Int,
    val startedAtMillis: Long,
    val durationMinutes: Int,
)

class HealthConnectManager(private val context: Context) {

    private val nutritionWrite = HealthPermission.getWritePermission(NutritionRecord::class)
    private val nutritionRead  = HealthPermission.getReadPermission(NutritionRecord::class)
    private val sleepRead      = HealthPermission.getReadPermission(SleepSessionRecord::class)

    val allPermissions = setOf(nutritionWrite, nutritionRead, sleepRead)
    val sleepPermissions = setOf(sleepRead)

    fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private val client: HealthConnectClient?
        get() = if (isAvailable()) HealthConnectClient.getOrCreate(context) else null

    suspend fun hasPermission(): Boolean {
        val c = client ?: return false
        return c.permissionController.getGrantedPermissions().contains(nutritionWrite)
    }

    suspend fun hasNutritionReadPermission(): Boolean {
        val c = client ?: return false
        return c.permissionController.getGrantedPermissions().contains(nutritionRead)
    }

    suspend fun hasSleepPermission(): Boolean {
        val c = client ?: return false
        return c.permissionController.getGrantedPermissions().contains(sleepRead)
    }

    private fun clientRecordId(entryId: Int): String = "caffeine_entry_$entryId"

    suspend fun writeEntry(entry: ConsumptionEntry, zoneId: ZoneId = ZoneId.systemDefault()) {
        val c = client ?: return
        // Imported records are owned by another app — never echo them back
        if (entry.healthConnectRecordId != null) return
        if (!hasPermission()) return
        val start = Instant.ofEpochMilli(entry.startedAtMillis)
        val end = start.plusSeconds(entry.normalizedDurationMinutes * 60L)
        val zoneRules = zoneId.rules
        runCatching {
            c.insertRecords(listOf(
                NutritionRecord(
                    startTime = start,
                    endTime = end,
                    caffeine = Mass.milligrams(entry.caffeineMg.toDouble()),
                    name = entry.drinkName,
                    startZoneOffset = zoneRules.getOffset(start),
                    endZoneOffset = zoneRules.getOffset(end),
                    metadata = HCMetadata.manualEntry(
                        clientRecordId = clientRecordId(entry.id),
                        clientRecordVersion = System.currentTimeMillis(),
                    ),
                )
            ))
        }
    }

    suspend fun syncAll(entries: List<ConsumptionEntry>, zoneId: ZoneId = ZoneId.systemDefault()) {
        entries.forEach { writeEntry(it, zoneId) }
    }

    suspend fun deleteEntry(entry: ConsumptionEntry) {
        if (entry.healthConnectRecordId != null) return
        deleteOwnRecords(listOf(entry.id))
    }

    suspend fun deleteEntries(entries: List<ConsumptionEntry>) {
        val ownIds = entries.filter { it.healthConnectRecordId == null }.map { it.id }
        if (ownIds.isEmpty()) return
        deleteOwnRecords(ownIds)
    }

    private suspend fun deleteOwnRecords(entryIds: List<Int>) {
        val c = client ?: return
        if (!hasPermission()) return
        val clientIds = entryIds.map { clientRecordId(it) }
        runCatching {
            c.deleteRecords(NutritionRecord::class, emptyList(), clientIds)
        }
    }

    suspend fun readForeignCaffeineRecords(
        sinceMillis: Long,
        excludeRecordIds: Set<String>,
    ): List<ImportedCaffeineRecord> {
        val c = client ?: return emptyList()
        if (!hasNutritionReadPermission()) return emptyList()
        val results = mutableListOf<ImportedCaffeineRecord>()
        var pageToken: String? = null
        val ownPackage = context.packageName
        do {
            val response = runCatching {
                c.readRecords(
                    ReadRecordsRequest(
                        recordType = NutritionRecord::class,
                        timeRangeFilter = TimeRangeFilter.after(Instant.ofEpochMilli(sinceMillis)),
                        pageToken = pageToken,
                    )
                )
            }.getOrNull() ?: break

            for (record in response.records) {
                // Skip our own records
                if (record.metadata.dataOrigin.packageName == ownPackage) continue
                val caffeineMass = record.caffeine ?: continue
                val mg = caffeineMass.inMilligrams.toInt()
                if (mg <= 0) continue
                val id = record.metadata.id
                if (id.isBlank() || id in excludeRecordIds) continue
                val durationSec = record.endTime.epochSecond - record.startTime.epochSecond
                val durationMinutes = (durationSec / 60).toInt().coerceAtLeast(1)
                results += ImportedCaffeineRecord(
                    healthConnectRecordId = id,
                    caffeineMg = mg,
                    startedAtMillis = record.startTime.toEpochMilli(),
                    durationMinutes = durationMinutes,
                )
            }
            pageToken = response.pageToken
        } while (pageToken != null)

        return results
    }

    suspend fun readSleepBedtime(mode: HcSleepMode, zoneId: ZoneId = ZoneId.systemDefault()): LocalTime? {
        val c = client ?: return null
        val now = ZonedDateTime.now(zoneId)
        val rangeStart = when (mode) {
            HcSleepMode.PREVIOUS_DAY -> now.minusDays(2).toInstant()
            HcSleepMode.SEVEN_DAY_AVERAGE -> now.minusDays(8).toInstant()
        }
        val response = runCatching {
            c.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(rangeStart, now.toInstant()),
                )
            )
        }.getOrNull() ?: return null

        if (response.records.isEmpty()) return null

        return when (mode) {
            HcSleepMode.PREVIOUS_DAY -> {
                val latest = response.records.maxByOrNull { it.startTime } ?: return null
                val zdt = latest.startTime.atZone(zoneId)
                LocalTime.of(zdt.hour, zdt.minute)
            }
            HcSleepMode.SEVEN_DAY_AVERAGE -> {
                averageSleepStartTime(
                    sessions = response.records.sortedByDescending { it.startTime }.take(7),
                    zoneId = zoneId,
                )
            }
        }
    }

    private fun averageSleepStartTime(sessions: List<SleepSessionRecord>, zoneId: ZoneId): LocalTime? {
        if (sessions.isEmpty()) return null
        val minutesList = sessions.map { session ->
            val zdt = session.startTime.atZone(zoneId)
            val minuteOfDay = zdt.hour * 60 + zdt.minute
            if (minuteOfDay < 720) minuteOfDay + 1440 else minuteOfDay
        }
        val avgMinutes = (minutesList.sum() / minutesList.size) % 1440
        return LocalTime.of(avgMinutes / 60, avgMinutes % 60)
    }
}
