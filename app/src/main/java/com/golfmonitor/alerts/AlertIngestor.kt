package com.golfmonitor.alerts

import android.content.Context
import com.golfmonitor.data.db.DealDatabaseProvider
import com.golfmonitor.data.db.SeedData
import com.golfmonitor.data.db.entity.CapturedAlertEntity
import com.golfmonitor.data.db.entity.DealEntity
import java.time.LocalDateTime

object AlertIngestor {
    const val SOURCE_LABEL = "GolfNow alert"
    const val GOLFNOW_URL = "https://www.golfnow.co.uk/"

    /**
     * Only notifications from apps whose package name contains one of these are read.
     * The UK GolfNow app is com.golfbreaks.teeofftimes.phone (Play Store, Oct 2026).
     */
    private val PACKAGE_HINTS = listOf("golfnow", "teeofftimes", "golfbreaks")

    fun isGolfNowPackage(packageName: String): Boolean =
        PACKAGE_HINTS.any { packageName.contains(it, ignoreCase = true) }

    /**
     * Stores the raw alert, then adds a deal if the text could be read.
     * Returns the parsed alert, or null when it was only logged.
     */
    suspend fun ingest(
        context: Context,
        source: String,
        title: String?,
        text: String,
        receivedAt: LocalDateTime
    ): ParsedAlert? {
        val db = DealDatabaseProvider.getDatabase(context)
        val fullText = listOfNotNull(title?.takeIf { it.isNotBlank() }, text).joinToString(" - ")
        val parsed = GolfNowAlertParser.parse(fullText, receivedAt, db.courseDao().getAll())

        db.capturedAlertDao().insert(
            CapturedAlertEntity(
                id = "$source-$receivedAt-${fullText.hashCode()}",
                receivedAt = receivedAt.toString(),
                source = source,
                title = title,
                text = text,
                parsedDealId = parsed?.dealId
            )
        )

        if (parsed != null) {
            // First real deal replaces the placeholder slots.
            db.dealDao().deleteBySource(SeedData.DEMO_SOURCE)
            db.dealDao().insertAll(
                listOf(
                    DealEntity(
                        id = parsed.dealId,
                        courseId = parsed.courseId,
                        courseName = parsed.courseName,
                        date = parsed.date.toString(),
                        time = parsed.time,
                        priceGbp = parsed.priceGbp,
                        baselinePriceGbp = parsed.baselinePriceGbp,
                        players = parsed.players ?: 0,
                        source = SOURCE_LABEL,
                        bookingUrl = GOLFNOW_URL
                    )
                )
            )
        }
        return parsed
    }
}
