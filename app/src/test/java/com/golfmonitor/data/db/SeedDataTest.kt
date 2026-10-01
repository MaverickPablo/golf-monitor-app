package com.golfmonitor.data.db

import com.golfmonitor.config.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate

class SeedDataTest {
    // Gradle runs unit tests with the module directory (app/) as the working directory.
    private val courses = SeedData.parseCourses(File("src/main/assets/courses.json").readText())

    @Test
    fun bundledCoursesParse() {
        assertTrue("expected 100+ courses, got ${courses.size}", courses.size >= 100)
        assertEquals("course ids must be unique", courses.size, courses.map { it.id }.toSet().size)
        courses.forEach {
            assertTrue(it.name, it.latitude in 50.0..52.5 && it.longitude in -1.5..2.0)
            assertTrue(it.name, (it.driveMinutes ?: 0) in 1..90)
            assertTrue(it.name, it.imageUrl == null || it.imageUrl!!.startsWith("https://"))
            assertTrue(it.name, it.greenFeeBaseline == null || it.greenFeeBaseline!! in 15.0..400.0)
            assertTrue(it.name, it.googleRating == null || it.googleRating!! in 1.0..5.0)
        }
        val withFees = courses.count { it.greenFeeBaseline != null }
        assertTrue("expected most courses to have a usual fee, got $withFees", withFees >= 40)
    }

    @Test
    fun demoDealsAreSundayMorningThreeBalls() {
        val deals = SeedData.demoDeals(courses, LocalDate.of(2026, 10, 1))
        assertEquals(4, deals.size)
        deals.forEach {
            assertEquals(DayOfWeek.SUNDAY, LocalDate.parse(it.date).dayOfWeek)
            assertTrue(it.time >= AppConfig.TIME_WINDOW_START && it.time <= AppConfig.TIME_WINDOW_END)
            assertTrue(it.priceGbp <= AppConfig.MAX_GREEN_FEE_GBP)
            assertEquals(AppConfig.PLAYERS_REQUIRED, it.players)
            assertEquals("Demo", it.source)
        }
        assertEquals("2026-10-04", deals.first().date)
    }
}
