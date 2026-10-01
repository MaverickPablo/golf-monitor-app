package com.golfmonitor.data.db

import android.content.Context
import com.golfmonitor.config.AppConfig
import com.golfmonitor.data.db.entity.CourseEntity
import com.golfmonitor.data.db.entity.DealEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object SeedData {
    private const val COURSES_ASSET = "courses.json"
    const val DEMO_SOURCE = "Demo"

    /** One row of assets/courses.json (bookable courses within 90 min of ME17 2DD). */
    data class SeedCourse(
        val id: String,
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val driveMinutes: Int?,
        val bookingSystem: String?,
        val bookingUrl: String?,
        val websiteUrl: String?,
        val imageUrl: String?,
        val lastPlayed: String?,
        val county: String? = null,
        val postcode: String? = null,
        val greenFeeBaseline: Double? = null,
        val googleRating: Double? = null,
        val ukGolfClubId: String? = null
    )

    fun parseCourses(json: String): List<CourseEntity> {
        val type = object : TypeToken<List<SeedCourse>>() {}.type
        val rows: List<SeedCourse> = Gson().fromJson(json, type)
        return rows.map {
            CourseEntity(
                id = it.id,
                name = it.name,
                county = it.county ?: "",
                latitude = it.latitude,
                longitude = it.longitude,
                postcode = it.postcode ?: "",
                greenFeeBaseline = it.greenFeeBaseline,
                driveMinutes = it.driveMinutes,
                bookingSystem = it.bookingSystem,
                bookingUrl = it.bookingUrl,
                websiteUrl = it.websiteUrl,
                imageUrl = it.imageUrl,
                lastPlayed = it.lastPlayed,
                googleRating = it.googleRating,
                ukGolfClubId = it.ukGolfClubId
            )
        }
    }

    suspend fun seed(context: Context) {
        val db = DealDatabaseProvider.getDatabase(context)

        // Always upsert the bundled course list so app updates refresh links and images.
        val json = context.assets.open(COURSES_ASSET).bufferedReader().use { it.readText() }
        val courses = parseCourses(json)
        db.courseDao().insertAll(courses)

        if (db.dealDao().count() == 0) {
            db.dealDao().insertAll(demoDeals(courses, LocalDate.now()))
        }
    }

    /** Placeholder slots on the next two Sundays until a live availability source exists. */
    fun demoDeals(courses: List<CourseEntity>, today: LocalDate): List<DealEntity> {
        val nextSunday = today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
        val picks = courses.filter { it.imageUrl != null }.sortedBy { it.driveMinutes ?: Int.MAX_VALUE }.take(4)
        val times = listOf("07:30", "08:10", "09:20", "10:40")
        val prices = listOf(42.0 to 60.0, 55.0 to 65.0, 38.0 to 55.0, 70.0 to 78.0)
        return picks.mapIndexed { i, course ->
            val date = nextSunday.plusWeeks((i % 2).toLong()).toString()
            val (price, baseline) = prices[i]
            DealEntity(
                id = "${course.id}-$date-${times[i]}",
                courseId = course.id,
                courseName = course.name,
                date = date,
                time = times[i],
                priceGbp = price,
                baselinePriceGbp = baseline,
                players = AppConfig.PLAYERS_REQUIRED,
                source = DEMO_SOURCE,
                bookingUrl = course.bookingUrl ?: course.websiteUrl
            )
        }
    }
}
