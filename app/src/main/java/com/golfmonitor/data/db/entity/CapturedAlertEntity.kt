package com.golfmonitor.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Raw text of every GolfNow notification (or pasted test alert), kept to tune the parser. */
@Entity(tableName = "captured_alerts")
data class CapturedAlertEntity(
    @PrimaryKey val id: String,
    val receivedAt: String, // ISO local date-time
    val source: String, // notification package name, or "manual"
    val title: String?,
    val text: String,
    val parsedDealId: String? // null when the parser could not read it
)
