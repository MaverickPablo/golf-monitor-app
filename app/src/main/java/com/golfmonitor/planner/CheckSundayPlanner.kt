package com.golfmonitor.planner

import com.golfmonitor.data.db.entity.CourseEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * Builds the manual "Check Sunday" list: which Sunday to look at and the order to
 * open club visitor-booking pages in. No club or booking site is fetched by the app.
 */
object CheckSundayPlanner {
    /** Spec: the hunt for next Sunday starts at 14:00 on Sunday. */
    private val HUNT_RESET = LocalTime.of(14, 0)
    /** Spec: rotation score is 0 if played in the last 4 Sundays. */
    private const val RECENT_DAYS = 28L
    /** "normal" tier in the course directory; 75-90 min courses are optional extras. */
    const val NEAR_DRIVE_MINUTES = 75

    fun targetSunday(now: LocalDateTime): LocalDate {
        val today = now.toLocalDate()
        return if (today.dayOfWeek == DayOfWeek.SUNDAY && now.toLocalTime().isBefore(HUNT_RESET)) {
            today
        } else {
            today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
        }
    }

    /** Accepts "2026-06-28" or "2024-05" (month only, treated as the 1st). */
    fun parseLastPlayed(value: String?): LocalDate? = value?.let {
        runCatching { LocalDate.parse(it) }.getOrNull()
            ?: runCatching { YearMonth.parse(it).atDay(1) }.getOrNull()
    }

    fun playedRecently(course: CourseEntity, sunday: LocalDate): Boolean {
        val played = parseLastPlayed(course.lastPlayed) ?: return false
        return ChronoUnit.DAYS.between(played, sunday) in 0 until RECENT_DAYS
    }

    fun checkUrl(course: CourseEntity): String? = course.bookingUrl ?: course.websiteUrl

    /**
     * Courses not played recently first, then courses with a usable scheme offer, then
     * by drive time. Courses with no link to open go last.
     */
    fun orderForChecking(
        courses: List<CourseEntity>,
        sunday: LocalDate,
        nearOnly: Boolean,
        offerCourseIds: Set<String> = emptySet()
    ): List<CourseEntity> = courses
        .filter { !nearOnly || (it.driveMinutes ?: Int.MAX_VALUE) <= NEAR_DRIVE_MINUTES }
        .sortedWith(
            compareBy<CourseEntity> { checkUrl(it) == null }
                .thenBy { playedRecently(it, sunday) }
                .thenBy { it.id !in offerCourseIds }
                .thenBy { it.driveMinutes ?: Int.MAX_VALUE }
                .thenBy { it.name }
        )
}
