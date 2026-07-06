package com.uc.caffeine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tombstone for a Health Connect record the user deleted locally.
// Without it, deleting an imported entry removes its id from the dedup set
// and the next import resurrects it.
@Entity(tableName = "hc_dismissed_records")
data class DismissedHealthConnectRecord(
    @PrimaryKey val recordId: String,
    val dismissedAtMillis: Long,
)
