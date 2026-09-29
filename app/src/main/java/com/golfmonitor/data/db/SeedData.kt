package com.golfmonitor.data.db

import android.content.Context
import com.golfmonitor.data.db.entity.CourseEntity
import com.golfmonitor.data.db.entity.DealEntity

object SeedData {
    suspend fun seedIfEmpty(context: Context) {
        val db = DealDatabaseProvider.getDatabase(context)
        val existing = db.courseDao().getAll()
        if (existing.isEmpty()) {
            val courses = listOf(
                CourseEntity(id="c1", name="Royal Mid-Surrey", county="Surrey", latitude=51.322, longitude=-0.198, postcode="KT5", greenFeeBaseline=85.0),
                CourseEntity(id="c2", name="Wentworth West", county="Surrey", latitude=51.398, longitude=-0.548, postcode="SL4", greenFeeBaseline=90.0),
                CourseEntity(id="c3", name="Chart Hills", county="Kent", latitude=51.185, longitude=0.456, postcode="TN12", greenFeeBaseline=80.0),
                CourseEntity(id="c4", name="Crowborough Beacon", county="East Sussex", latitude=51.114, longitude=0.172, postcode="TN6", greenFeeBaseline=65.0)
            )
            db.courseDao().insertAll(courses)
            val deals = listOf(
                DealEntity(id="c1-2026-10-03-08:30", courseId="c1", courseName="Royal Mid-Surrey", date="2026-10-03", time="08:30", priceGbp=70.0, players=4, source="GolfNow", bookingUrl=null),
                DealEntity(id="c2-2026-10-04-09:00", courseId="c2", courseName="Wentworth West", date="2026-10-04", time="09:00", priceGbp=75.0, players=4, source="GolfNow", bookingUrl=null),
                DealEntity(id="c3-2026-10-03-07:45", courseId="c3", courseName="Chart Hills", date="2026-10-03", time="07:45", priceGbp=55.0, players=4, source="GolfNow", bookingUrl=null),
                DealEntity(id="c4-2026-10-04-10:00", courseId="c4", courseName="Crowborough Beacon", date="2026-10-04", time="10:00", priceGbp=65.0, players=4, source="GolfNow", bookingUrl=null)
            )
            db.dealDao().insertAll(deals)
        }
    }
}
