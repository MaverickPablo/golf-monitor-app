package com.golfmonitor.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deals")
data class DealEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val courseName: String,
    val date: String,
    val time: String,
    val priceGbp: Double,
    val baselinePriceGbp: Double?,
    val discountPercent: Double?,
    val players: Int,
    val source: String,
    val bookingUrl: String?
)
