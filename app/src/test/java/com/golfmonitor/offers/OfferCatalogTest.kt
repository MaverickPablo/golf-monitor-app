package com.golfmonitor.offers

import com.golfmonitor.data.db.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OfferCatalogTest {
    // Gradle runs unit tests with the module directory (app/) as the working directory.
    private val offers = OfferCatalog.parse(File("src/main/assets/offers.json").readText())
    private val courseIds = SeedData.parseCourses(File("src/main/assets/courses.json").readText()).map { it.id }.toSet()

    private fun offer(from: String?, to: String?, scheme: Scheme = Scheme.TWOFORE1) =
        Offer("c", scheme, "2-for-1", from, to, "Phone", "2026-10-01")

    @Test
    fun bundledOffersPointAtBundledCourses() {
        assertTrue(offers.isNotEmpty())
        offers.forEach { assertTrue("unknown course ${it.courseId}", it.courseId in courseIds) }
    }

    @Test
    fun sundayWindowMustOverlapMorning() {
        assertTrue(offer("09:00", null).fitsSundayMorning)
        assertTrue(offer(null, null).fitsSundayMorning)
        assertTrue(offer(null, "08:00").fitsSundayMorning)
        assertFalse(offer("12:00", null).fitsSundayMorning)
        assertFalse(offer("13:30", "16:00").fitsSundayMorning)
        assertFalse(offer(null, "07:00").fitsSundayMorning)
    }

    @Test
    fun onlyMemberSchemesAreUsable() {
        assertTrue(OfferCatalog.usableByCourse(offers, emptySet()).isEmpty())
        val twoFore1 = OfferCatalog.usableByCourse(offers, setOf(Scheme.TWOFORE1))
        assertEquals(setOf("tenterden-golf-club"), twoFore1.keys)
        assertEquals("2fore1: 2-for-1 green fee after 09:00 - Phone 01580 763987", twoFore1.getValue("tenterden-golf-club").single().summary())
    }
}
