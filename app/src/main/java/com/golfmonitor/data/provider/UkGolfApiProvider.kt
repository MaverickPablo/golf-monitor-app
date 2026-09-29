package com.golfmonitor.data.provider

import com.golfmonitor.BuildConfig
import com.golfmonitor.config.AppConfig
import com.golfmonitor.data.network.RetrofitClient
import com.golfmonitor.model.Course

class UkGolfApiProvider(private val apiKey: String = BuildConfig.UK_GOLF_API_KEY) {
    suspend fun fetchCoursesInSoutheast(): List<Course> {
        return try {
            val response = RetrofitClient.ukGolfApiService.getNearbyClubs(
                apiKey = apiKey,
                lat = AppConfig.CENTER_LAT,
                lng = AppConfig.CENTER_LNG,
                radiusKm = AppConfig.RADIUS_KM
            )
            response.clubs.map { dto ->
                Course(
                    id = dto.id,
                    name = dto.name,
                    county = dto.county ?: "Unknown",
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    postcode = dto.postcode ?: "",
                    greenFeeBaseline = dto.greenFeeBaseline,
                    facilities = emptyList()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
