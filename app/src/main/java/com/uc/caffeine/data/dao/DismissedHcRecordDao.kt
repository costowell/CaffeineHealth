package com.uc.caffeine.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uc.caffeine.data.model.DismissedHealthConnectRecord

@Dao
interface DismissedHcRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<DismissedHealthConnectRecord>)

    @Query("SELECT recordId FROM hc_dismissed_records")
    suspend fun getAllIds(): List<String>

    // Tombstones only matter while their record can still appear in the import window
    @Query("DELETE FROM hc_dismissed_records WHERE dismissedAtMillis < :beforeMillis")
    suspend fun pruneOlderThan(beforeMillis: Long)
}
