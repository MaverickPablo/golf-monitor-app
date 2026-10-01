package com.golfmonitor.data.provider

import com.golfmonitor.data.network.GreenFeeDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UsualSundayFeeTest {
    private fun fee(type: String?, price: Double?, holes: Int? = null, notes: String? = null) =
        GreenFeeDto(type, holes, price, notes)

    @Test
    fun unlabelledAdultFeesUseTheTopRate() {
        // Real response for Ridge Golf Club, 1 Oct 2026.
        val fees = listOf(
            fee("10 Holes", 28.0, holes = 10),
            fee("Adult", 38.0), fee("Adult", 28.0), fee("Adult", 43.0), fee("Adult", 25.0),
            fee("Junior 18 Holes", 28.0)
        )
        assertEquals(43.0, UkGolfApiProvider.usualSundayFee(fees)!!, 0.0)
    }

    @Test
    fun weekendFeeWinsOverHigherOtherFees() {
        val fees = listOf(
            fee("Weekday", 40.0, holes = 18),
            fee("Weekend", 55.0, holes = 18),
            fee("Weekend Twilight", 30.0, holes = 18),
            fee("Day Rate - 36 holes", 90.0)
        )
        assertEquals(55.0, UkGolfApiProvider.usualSundayFee(fees)!!, 0.0)
    }

    @Test
    fun visitorFeesBeatSpecialsAndConcessions() {
        // Real response for Copthorne Golf Club, 1 Oct 2026.
        val fees = listOf(
            fee("County Card", 45.0), fee("Fourball Special", 300.0), fee("James Braid Courses", 45.0),
            fee("Members Guest", 45.0), fee("Mid Sussex League", 45.0),
            fee("Visitor", 70.0), fee("Visitor", 90.0)
        )
        assertEquals(90.0, UkGolfApiProvider.usualSundayFee(fees)!!, 0.0)
    }

    @Test
    fun morningSlotsKeptAfternoonSlotsDropped() {
        // Basildon lists fees by time band.
        val fees = listOf(
            fee("6.00 am - 11.59 am 3 & 4 balls only", 39.0, holes = 18),
            fee("12.00 pm - 2.00 pm", 32.0, holes = 18),
            fee("Before 6.00 am", 20.0, holes = 18)
        )
        assertEquals(39.0, UkGolfApiProvider.usualSundayFee(fees)!!, 0.0)
    }

    @Test
    fun noUsableFeeGivesNull() {
        assertNull(UkGolfApiProvider.usualSundayFee(emptyList()))
        assertNull(UkGolfApiProvider.usualSundayFee(listOf(fee("Junior", 15.0), fee("9 Holes", 20.0, holes = 9), fee("Adult", null))))
    }
}
