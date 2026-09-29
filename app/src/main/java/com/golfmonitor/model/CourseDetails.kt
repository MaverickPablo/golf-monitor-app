package com.golfmonitor.model

data class CourseDetails(
    val courseId: String,
    val par: Int,
    val slope: Int,
    val rating: Double,
    val imageUrl: String?
)
