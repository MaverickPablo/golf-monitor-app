package com.golfmonitor.data.provider

import com.golfmonitor.model.TeeTimeDeal

interface AvailabilityProvider {
    suspend fun fetchWeekendDeals(courses: List<com.golfmonitor.model.Course>): List<TeeTimeDeal>
}
