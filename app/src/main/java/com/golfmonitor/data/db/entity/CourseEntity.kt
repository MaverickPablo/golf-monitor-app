package com.golfmonitor.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val county: String,
    val latitude: Double,
    val longitude: Double,
    val postcode: String,
    val greenFeeBaseline: Double?,
    // Sunday Caddie course knowledge (seeded from assets/courses.json)
    val driveMinutes: Int? = null,
    val bookingSystem: String? = null,
    val bookingUrl: String? = null,
    val websiteUrl: String? = null,
    val imageUrl: String? = null,
    val lastPlayed: String? = null // "2026-06-28" or "2024-05"
)
