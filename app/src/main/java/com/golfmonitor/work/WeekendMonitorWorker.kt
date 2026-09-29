package com.golfmonitor.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.golfmonitor.config.AppConfig
import com.golfmonitor.data.db.DealDatabaseProvider
import com.golfmonitor.data.mapper.toEntity
import com.golfmonitor.data.provider.GolfNowProvider
import com.golfmonitor.data.provider.UkGolfApiProvider

class WeekendMonitorWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return try {
            val db = DealDatabaseProvider.getDatabase(applicationContext)
            val ukProvider = UkGolfApiProvider("")
            val courses = ukProvider.fetchCoursesInSoutheast()
            // Cache courses
            val courseEntities = courses.map { it.toEntity() }
            if (courseEntities.isNotEmpty()) {
                db.courseDao().insertAll(courseEntities)
            }
            val provider = GolfNowProvider("")
            val deals = provider.fetchWeekendDeals(courses)
            val filtered = deals.filter { it.priceGbp <= AppConfig.MAX_GREEN_FEE_GBP }
            val dealEntities = filtered.map { it.toEntity() }
            if (dealEntities.isNotEmpty()) {
                db.dealDao().insertAll(dealEntities)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

fun scheduleWeekendMonitor(context: Context) {
    val request = PeriodicWorkRequestBuilder<WeekendMonitorWorker>(4, TimeUnit.HOURS).build()
    WorkManager.getInstance(context).enqueue(request)
}
