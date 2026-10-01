package com.golfmonitor.data.provider

import com.golfmonitor.BuildConfig
import com.golfmonitor.config.AppConfig
import com.golfmonitor.data.network.GreenFeeDto
import com.golfmonitor.data.network.RetrofitClient
import com.golfmonitor.data.network.UkGolfClubDto

class UkGolfApiProvider(private val apiKey: String = BuildConfig.UK_GOLF_API_KEY) {
    val hasKey: Boolean get() = apiKey.isNotBlank()

    /** The 50 nearest clubs to home (the API's maximum for one call). */
    suspend fun fetchNearbyClubs(): List<UkGolfClubDto> =
        RetrofitClient.ukGolfApiService.getNearbyClubs(
            apiKey = apiKey,
            lat = AppConfig.CENTER_LAT,
            lng = AppConfig.CENTER_LNG,
            radiusMiles = AppConfig.RADIUS_MILES
        ).clubs

    suspend fun fetchUsualSundayFee(clubId: String): Double? =
        usualSundayFee(RetrofitClient.ukGolfApiService.getGreenFees(apiKey, clubId))

    companion object {
        /** Concessions, specials, multi-round, winter and afternoon rates are not a Sunday-morning visitor price. */
        private val EXCLUDED = Regex(
            "junior|child|under|student|senior|twilight|member|guest|society|county card|braid|league|" +
                "special|fourball|package|day rate|day ticket|winter|9 hole|10 hole|27|36|par 3|" +
                "evening|after|before|sunset|pm|pint",
            RegexOption.IGNORE_CASE
        )
        private val WEEKEND = Regex("weekend|sunday|sat|sun", RegexOption.IGNORE_CASE)
        private val VISITOR = Regex("visitor", RegexOption.IGNORE_CASE)

        /**
         * The usual adult 18-hole price for a Sunday round, used as the value baseline.
         * Visitor fees are preferred, then weekend-labelled ones. Many clubs list fees only
         * as "Adult" without weekday/weekend, so otherwise the highest is taken (weekend
         * rates are the top rate). Returns null rather than guess when nothing fits.
         */
        fun usualSundayFee(fees: List<GreenFeeDto>): Double? {
            val adult = fees.filter { fee ->
                val price = fee.priceGbp ?: return@filter false
                val label = listOfNotNull(fee.feeType, fee.notes).joinToString(" ")
                price in 15.0..400.0 && (fee.holes == null || fee.holes == 18) && !EXCLUDED.containsMatchIn(label)
            }
            val visitor = adult.filter { VISITOR.containsMatchIn(it.feeType.orEmpty()) }.ifEmpty { adult }
            val weekend = visitor.filter { WEEKEND.containsMatchIn(it.feeType.orEmpty()) }
            return (weekend.ifEmpty { visitor }).mapNotNull { it.priceGbp }.maxOrNull()
        }
    }
}
