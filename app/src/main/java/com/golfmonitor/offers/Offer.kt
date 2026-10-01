package com.golfmonitor.offers

import com.golfmonitor.config.AppConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Voucher / membership schemes that give cheaper rounds. Paul says which he belongs to. */
enum class Scheme(val label: String, val yearlyCost: String) {
    TWOFORE1("2fore1", "£99/yr"),
    GREENFREE("GreenFree", "membership"),
    PLAYMOREGOLF("PlayMoreGolf", "from £320/yr")
}

/**
 * One row of assets/offers.json. Sunday times are "HH:mm"; null means the scheme does
 * not publish a window (check with the club when booking).
 */
data class Offer(
    val courseId: String,
    val scheme: Scheme,
    val saving: String,
    val sundayFrom: String?,
    val sundayTo: String?,
    val bookHow: String,
    val checkedOn: String
) {
    /** True when the Sunday window overlaps the 07:00-12:00 tee-off window. */
    val fitsSundayMorning: Boolean
        get() = (sundayFrom ?: "00:00") < AppConfig.TIME_WINDOW_END &&
            (sundayTo ?: "23:59") > AppConfig.TIME_WINDOW_START

    fun summary(): String = buildString {
        append(scheme.label).append(": ").append(saving)
        when {
            sundayFrom != null && sundayTo != null -> append(" $sundayFrom-$sundayTo")
            sundayFrom != null -> append(" after $sundayFrom")
            sundayTo != null -> append(" before $sundayTo")
        }
        append(" - ").append(bookHow)
    }
}

object OfferCatalog {
    const val ASSET = "offers.json"

    private data class Row(
        val courseId: String,
        val scheme: String,
        val saving: String,
        val sundayFrom: String?,
        val sundayTo: String?,
        val bookHow: String,
        val checkedOn: String
    )

    fun parse(json: String): List<Offer> {
        val type = object : TypeToken<List<Row>>() {}.type
        val rows: List<Row> = Gson().fromJson(json, type)
        return rows.map {
            Offer(
                courseId = it.courseId,
                scheme = Scheme.valueOf(it.scheme),
                saving = it.saving,
                sundayFrom = it.sundayFrom,
                sundayTo = it.sundayTo,
                bookHow = it.bookHow,
                checkedOn = it.checkedOn
            )
        }
    }

    /** Offers Paul can actually use on a Sunday morning, grouped by course. */
    fun usableByCourse(offers: List<Offer>, memberOf: Set<Scheme>): Map<String, List<Offer>> =
        offers.filter { it.scheme in memberOf && it.fitsSundayMorning }.groupBy { it.courseId }
}
