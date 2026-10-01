package com.golfmonitor.alerts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertIngestorTest {
    @Test
    fun recognisesGolfNowUkApp() {
        assertTrue(AlertIngestor.isGolfNowPackage("com.golfbreaks.teeofftimes.phone"))
        assertTrue(AlertIngestor.isGolfNowPackage("com.golfnow.android.teetimes"))
    }

    @Test
    fun ignoresOtherApps() {
        assertFalse(AlertIngestor.isGolfNowPackage("com.whatsapp"))
        assertFalse(AlertIngestor.isGolfNowPackage("com.google.android.gm"))
    }
}
