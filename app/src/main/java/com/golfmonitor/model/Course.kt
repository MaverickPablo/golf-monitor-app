package com.golfmonitor.model

data class Course(
    val id: String,
    val name: String,
    val county: String,
    val latitude: Double,
    val longitude: Double,
    val postcode: String,
    val greenFeeBaseline: Double?,
    val facilities: List<String> = emptyList()
)

data class TeeTimeDeal(
    val courseId: String,
    val courseName: String,
    val date: String, // ISO 2026-10-03
    val time: String, // 08:30
    val priceGbp: Double,
    val baselinePriceGbp: Double?,
    val players: Int,
    val source: String,
    val bookingUrl: String?
) {
    val discountPercent: Double?
        get() = baselinePriceGbp?.let {
            if (it > 0) ((it - priceGbp) / it * 100) else null
        }
}
