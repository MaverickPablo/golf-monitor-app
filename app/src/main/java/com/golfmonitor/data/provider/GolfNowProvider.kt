package com.golfmonitor.data.provider

import com.golfmonitor.model.Course
import com.golfmonitor.model.TeeTimeDeal

class GolfNowProvider(private val apiKey: String) : AvailabilityProvider {
    override suspend fun fetchWeekendDeals(courses: List<Course>): List<TeeTimeDeal> {
        // TODO: Implement GolfNow API calls with sandbox endpoint
        // Filter by date weekend, price <= AppConfig.MAX_GREEN_FEE_GBP
        return emptyList()
    }
}
