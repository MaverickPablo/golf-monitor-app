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
    val greenFeeBaseline: Double?
)
