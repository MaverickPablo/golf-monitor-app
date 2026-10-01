package com.golfmonitor.alerts

import com.golfmonitor.data.db.entity.CourseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/** Guessed GolfNow wordings - replace with real notification text once captured. */
class GolfNowAlertParserTest {
    private fun course(id: String, name: String) = CourseEntity(
        id = id, name = name, county = "", latitude = 51.2, longitude = 0.7, postcode = "", greenFeeBaseline = null
    )

    private val courses = listOf(
        course("bearsted-golf-club", "Bearsted Golf Club"),
        course("lydd-golf-club", "Lydd Golf Club"),
        course("park-wood-golf-club", "Park Wood Golf Club"),
        course("brighton-hove-golf-club", "Brighton & Hove Golf Club")
    )
    private val thursday = LocalDateTime.of(2026, 10, 1, 9, 0)

    private fun parse(text: String, received: LocalDateTime = thursday) =
        GolfNowAlertParser.parse(text, received, courses)

    @Test
    fun hotDealWithWasPriceAndPlayers() {
        val a = parse("Hot Deal at Bearsted Golf Club! Sun 4 Oct, 08:30 - £35 (was £55) for 3 players")!!
        assertEquals("bearsted-golf-club", a.courseId)
        assertEquals(LocalDate.of(2026, 10, 4), a.date)
        assertEquals("08:30", a.time)
        assertEquals(35.0, a.priceGbp, 0.001)
        assertEquals(55.0, a.baselinePriceGbp!!, 0.001)
        assertEquals(3, a.players)
        assertEquals("bearsted-golf-club-2026-10-04-08:30", a.dealId)
    }

    @Test
    fun titleAndSentenceWithLongDateAndUpToFour() {
        val a = parse(
            "Tee time alert - Lydd Golf Club has tee times available on Sunday 11th October " +
                "from 07:50, from £29.00 per player. Up to 4 golfers."
        )!!
        assertEquals("lydd-golf-club", a.courseId)
        assertEquals(LocalDate.of(2026, 10, 11), a.date)
        assertEquals("07:50", a.time)
        assertEquals(29.0, a.priceGbp, 0.001)
        assertNull(a.baselinePriceGbp)
        assertEquals(3, a.players)
    }

    @Test
    fun weekdayOnlyAmPmAndPercentOff() {
        val a = parse("Sunday 9:20am · Park Wood Golf Club · £42 · Save 30% off")!!
        assertEquals("park-wood-golf-club", a.courseId)
        assertEquals(LocalDate.of(2026, 10, 4), a.date)
        assertEquals("09:20", a.time)
        assertEquals(60.0, a.baselinePriceGbp!!, 0.01)
        assertNull(a.players)
    }

    @Test
    fun numericDateDotTimeAndAndInName() {
        val a = parse("Brighton and Hove 04/10 8.10am £30 2-4 players")!!
        assertEquals("brighton-hove-golf-club", a.courseId)
        assertEquals(LocalDate.of(2026, 10, 4), a.date)
        assertEquals("08:10", a.time)
        assertEquals(3, a.players)
    }

    @Test
    fun unlistedCourseKeepsItsName() {
        val a = parse("New deal: Sun 4 Oct 10:10 at Imaginary Downs Golf Club £45")!!
        assertEquals("Imaginary Downs Golf Club", a.courseName)
        assertEquals("unlisted-imaginary-downs", a.courseId)
    }

    @Test
    fun dateEarlyInYearRollsForward() {
        val a = parse("Bearsted Golf Club Sun 3 Jan 08:00 £25", LocalDateTime.of(2026, 12, 28, 18, 0))!!
        assertEquals(LocalDate.of(2027, 1, 3), a.date)
    }

    @Test
    fun marketingTextIsNotADeal() {
        assertNull(parse("Don't miss out on this weekend's deals!"))
        assertNull(parse("Bearsted Golf Club - book your next round today"))
    }

    @Test
    fun placeNamesAreNotMonthsOrDays() {
        assertNull(GolfNowAlertParser.parseDate("Romney Marsh 3 players", thursday.toLocalDate()))
        assertNull(GolfNowAlertParser.parseDate("Sunningdale special", thursday.toLocalDate()))
    }
}
