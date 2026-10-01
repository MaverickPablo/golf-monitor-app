package com.golfmonitor.planner

import com.golfmonitor.data.db.entity.CourseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class CheckSundayPlannerTest {
    private fun course(
        id: String,
        drive: Int?,
        lastPlayed: String? = null,
        bookingUrl: String? = "https://example.test/$id"
    ) = CourseEntity(
        id = id, name = id, county = "", latitude = 51.2, longitude = 0.7, postcode = "",
        greenFeeBaseline = null, driveMinutes = drive, bookingUrl = bookingUrl, lastPlayed = lastPlayed
    )

    @Test
    fun targetSundayRollsOverAtTwoPmOnSunday() {
        // Thursday -> coming Sunday
        assertEquals(LocalDate.of(2026, 10, 4), CheckSundayPlanner.targetSunday(LocalDateTime.of(2026, 10, 1, 9, 0)))
        // Sunday morning -> today
        assertEquals(LocalDate.of(2026, 10, 4), CheckSundayPlanner.targetSunday(LocalDateTime.of(2026, 10, 4, 7, 30)))
        // Sunday 14:00 -> next Sunday
        assertEquals(LocalDate.of(2026, 10, 11), CheckSundayPlanner.targetSunday(LocalDateTime.of(2026, 10, 4, 14, 0)))
    }

    @Test
    fun lastPlayedAcceptsDayOrMonth() {
        assertEquals(LocalDate.of(2026, 6, 28), CheckSundayPlanner.parseLastPlayed("2026-06-28"))
        assertEquals(LocalDate.of(2024, 5, 1), CheckSundayPlanner.parseLastPlayed("2024-05"))
        assertEquals(null, CheckSundayPlanner.parseLastPlayed("sometime"))
    }

    @Test
    fun playedInLastFourSundaysIsRecent() {
        val sunday = LocalDate.of(2026, 10, 4)
        assertTrue(CheckSundayPlanner.playedRecently(course("a", 10, "2026-09-13"), sunday))
        assertFalse(CheckSundayPlanner.playedRecently(course("b", 10, "2026-09-06"), sunday))
        assertFalse(CheckSundayPlanner.playedRecently(course("c", 10, null), sunday))
    }

    @Test
    fun orderPutsRecentAndLinklessLastThenNearestFirst() {
        val sunday = LocalDate.of(2026, 10, 4)
        val courses = listOf(
            course("far", 60),
            course("recent", 5, lastPlayed = "2026-09-27"),
            course("near", 20),
            course("nolink", 1, bookingUrl = null),
            course("tooFar", 85)
        )
        assertEquals(
            listOf("near", "far", "recent", "nolink"),
            CheckSundayPlanner.orderForChecking(courses, sunday, nearOnly = true).map { it.id }
        )
        assertEquals(5, CheckSundayPlanner.orderForChecking(courses, sunday, nearOnly = false).size)
    }
}
