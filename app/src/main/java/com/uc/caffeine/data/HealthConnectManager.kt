package com.uc.caffeine.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.metadata.Metadata as HCMetadata
import androidx.health.connect.client.units.Mass
import com.uc.caffeine.data.model.ConsumptionEntry
import java.time.Instant
import java.time.ZoneId

class HealthConnectManager(private val context: Context) {

    val permissions = setOf(HealthPermission.getWritePermission(NutritionRecord::class))

    fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private val client: HealthConnectClient?
        get() = if (isAvailable()) HealthConnectClient.getOrCreate(context) else null

    suspend fun hasPermission(): Boolean {
        val c = client ?: return false
        return c.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    suspend fun writeEntry(entry: ConsumptionEntry, zoneId: ZoneId = ZoneId.systemDefault()) {
        val c = client ?: return
        val start = Instant.ofEpochMilli(entry.startedAtMillis)
        val end = start.plusSeconds(entry.durationMinutes * 60L)
        val zoneRules = zoneId.rules
        c.insertRecords(
            listOf(
                NutritionRecord(
                    startTime = start,
                    endTime = end,
                    caffeine = Mass.milligrams(entry.caffeineMg.toDouble()),
                    startZoneOffset = zoneRules.getOffset(start),
                    endZoneOffset = zoneRules.getOffset(end),
                    metadata = HCMetadata.unknownRecordingMethod(),
                )
            )
        )
    }

    suspend fun syncAll(entries: List<ConsumptionEntry>, zoneId: ZoneId = ZoneId.systemDefault()) {
        entries.forEach { writeEntry(it, zoneId) }
    }
}
