package com.golfmonitor.alerts

import com.golfmonitor.data.db.entity.CourseEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

data class ParsedAlert(
    val courseId: String,
    val courseName: String,
    val date: LocalDate,
    val time: String, // "08:30"
    val priceGbp: Double,
    val baselinePriceGbp: Double?,
    val players: Int?
) {
    val dealId: String get() = "$courseId-$date-$time"
}

/**
 * Best-guess reader for GolfNow deal notifications such as
 *   "Hot Deal at Bearsted Golf Club! Sun 4 Oct, 08:30 - £35 (was £55) for 3 players".
 * The real wording is not confirmed yet, so every rule is deliberately loose and
 * unparsed alerts are kept raw (see AlertIngestor) for tuning.
 */
object GolfNowAlertParser {
    private val MONTHS = mapOf(
        "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "may" to 5, "jun" to 6,
        "jul" to 7, "aug" to 8, "sep" to 9, "sept" to 9, "oct" to 10, "nov" to 11, "dec" to 12
    )
    private val WEEKDAYS = mapOf(
        "mon" to DayOfWeek.MONDAY, "tue" to DayOfWeek.TUESDAY, "wed" to DayOfWeek.WEDNESDAY,
        "thu" to DayOfWeek.THURSDAY, "fri" to DayOfWeek.FRIDAY, "sat" to DayOfWeek.SATURDAY,
        "sun" to DayOfWeek.SUNDAY
    )
    private val NUMBER_WORDS = mapOf("one" to 1, "two" to 2, "three" to 3, "four" to 4)

    // Whole words only, so "Marsh" or "Sunningdale" are not read as March / Sunday.
    private const val MONTH = "(jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|june?|july?|aug(?:ust)?" +
        "|sep(?:t|tember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)\\b\\.?"
    private const val WEEKDAY = "(mon(?:day)?|tue(?:s|sday)?|wed(?:nesday)?|thu(?:r|rs|rsday)?|fri(?:day)?" +
        "|sat(?:urday)?|sun(?:day)?)\\b\\.?"

    private val PRICE = Regex("£\\s?(\\d+(?:\\.\\d{1,2})?)")
    private val BASELINE_PRICE =
        Regex("(?:was|rrp|usually|normally|rack rate|instead of)\\s*:?\\s*£\\s?(\\d+(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE)
    private val PERCENT_OFF = Regex("(\\d{1,2})\\s?%\\s?off", RegexOption.IGNORE_CASE)

    private val TIME_24 = Regex("\\b([01]?\\d|2[0-3])[:.]([0-5]\\d)\\s*(am|pm)?\\b", RegexOption.IGNORE_CASE)
    private val TIME_AMPM = Regex("\\b(1[0-2]|0?[1-9])\\s*(am|pm)\\b", RegexOption.IGNORE_CASE)

    private val DAY_MONTH = Regex("\\b(\\d{1,2})(?:st|nd|rd|th)?\\s+(?:of\\s+)?$MONTH", RegexOption.IGNORE_CASE)
    private val MONTH_DAY = Regex("\\b$MONTH\\s+(\\d{1,2})(?:st|nd|rd|th)?\\b", RegexOption.IGNORE_CASE)
    private val NUMERIC_DATE = Regex("\\b(\\d{1,2})/(\\d{1,2})(?:/(\\d{2,4}))?\\b")
    private val WEEKDAY_ONLY = Regex("\\b$WEEKDAY", RegexOption.IGNORE_CASE)

    private val PLAYER_RANGE = Regex("(\\d)\\s*(?:-|–|to)\\s*(\\d)\\s*(?:players|golfers|people)", RegexOption.IGNORE_CASE)
    private val UP_TO_PLAYERS = Regex("up to\\s*(\\d|one|two|three|four)\\s*(?:players|golfers|people)?", RegexOption.IGNORE_CASE)
    private val PLAYERS = Regex("(\\d|one|two|three|four)[\\s-]*(?:players|golfers|people|ball)\\b", RegexOption.IGNORE_CASE)

    private val COURSE_NAME = Regex(
        "([A-Z][A-Za-z'.&-]*(?:\\s+(?:[A-Z][A-Za-z'.&-]*|&|and|on|the|of))*\\s+" +
            "(?:Golf\\s+(?:&|and)\\s+Country\\s+Club|Golf\\s+Club|Golf\\s+Course|Golf\\s+Centre|Golf\\s+Resort|Country\\s+Club|GC))\\b"
    )
    private val NAME_NOISE = Regex(
        "\\b(golf|club|course|centre|center|country|resort|hotel|the|and|gc)\\b|[^a-z0-9 ]"
    )

    fun parse(rawText: String, received: LocalDateTime, courses: List<CourseEntity>): ParsedAlert? {
        val text = rawText.replace(' ', ' ').replace('–', '-').replace('—', '-')
        val (price, baseline) = parsePrices(text) ?: return null
        val withoutPrices = PRICE.replace(text, " ")
        val time = parseTime(withoutPrices) ?: return null
        val date = parseDate(withoutPrices, received.toLocalDate()) ?: return null
        val (courseId, courseName) = matchCourse(text, courses) ?: return null
        return ParsedAlert(courseId, courseName, date, time, price, baseline, parsePlayers(text))
    }

    /** Lowest non-"was" price is the deal price; "was £X" or "N% off" gives the usual price. */
    fun parsePrices(text: String): Pair<Double, Double?>? {
        val baselines = BASELINE_PRICE.findAll(text).map { it.groupValues[1].toDouble() }.toList()
        val baselineRanges = BASELINE_PRICE.findAll(text).map { it.range }.toList()
        val prices = PRICE.findAll(text)
            .filter { m -> baselineRanges.none { r -> m.range.first in r } }
            .map { it.groupValues[1].toDouble() }
            .toList()
        val price = prices.minOrNull() ?: return null
        val baseline = baselines.maxOrNull()?.takeIf { it > price }
            ?: PERCENT_OFF.find(text)?.groupValues?.get(1)?.toDouble()
                ?.takeIf { it in 1.0..90.0 }
                ?.let { Math.round(price / (1 - it / 100) * 100) / 100.0 }
        return price to baseline
    }

    fun parseTime(text: String): String? {
        TIME_24.find(text)?.let { m ->
            var hour = m.groupValues[1].toInt()
            val ampm = m.groupValues[3].lowercase()
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            return "%02d:%s".format(hour, m.groupValues[2])
        }
        TIME_AMPM.find(text)?.let { m ->
            var hour = m.groupValues[1].toInt() % 12
            if (m.groupValues[2].equals("pm", ignoreCase = true)) hour += 12
            return "%02d:00".format(hour)
        }
        return null
    }

    fun parseDate(text: String, received: LocalDate): LocalDate? {
        DAY_MONTH.find(text)?.let { m ->
            return inferYear(m.groupValues[1].toInt(), monthOf(m.groupValues[2]), received)
        }
        MONTH_DAY.find(text)?.let { m ->
            return inferYear(m.groupValues[2].toInt(), monthOf(m.groupValues[1]), received)
        }
        NUMERIC_DATE.find(text)?.let { m ->
            val day = m.groupValues[1].toInt()
            val month = m.groupValues[2].toInt()
            val year = m.groupValues[3].takeIf { it.isNotEmpty() }?.toInt()?.let { if (it < 100) 2000 + it else it }
            return if (year != null) runCatching { LocalDate.of(year, month, day) }.getOrNull()
            else inferYear(day, month, received)
        }
        val lower = text.lowercase()
        if (Regex("\\btoday\\b").containsMatchIn(lower)) return received
        if (Regex("\\btomorrow\\b").containsMatchIn(lower)) return received.plusDays(1)
        WEEKDAY_ONLY.find(text)?.let { m ->
            val day = WEEKDAYS[m.groupValues[1].lowercase().take(3)] ?: return null
            return received.with(TemporalAdjusters.nextOrSame(day))
        }
        return null
    }

    fun parsePlayers(text: String): Int? {
        fun n(s: String) = s.toIntOrNull() ?: NUMBER_WORDS[s.lowercase()]
        PLAYER_RANGE.find(text)?.let { m ->
            val lo = m.groupValues[1].toInt()
            val hi = m.groupValues[2].toInt()
            return if (3 in lo..hi) 3 else hi
        }
        UP_TO_PLAYERS.find(text)?.let { m -> n(m.groupValues[1])?.let { return if (it >= 3) 3 else it } }
        PLAYERS.find(text)?.let { m -> return n(m.groupValues[1]) }
        return null
    }

    /** Known course whose simplified name appears in the text (longest wins), else a named but unlisted course. */
    fun matchCourse(text: String, courses: List<CourseEntity>): Pair<String, String>? {
        val haystack = " ${simplify(text)} "
        courses
            .map { it to simplify(it.name) }
            .filter { (_, key) -> key.length >= 4 && haystack.contains(" $key ") }
            .maxByOrNull { (_, key) -> key.length }
            ?.let { (course, _) -> return course.id to course.name }
        val name = COURSE_NAME.find(text)?.groupValues?.get(1)
            ?.replace(Regex("^(?:Hot Deal|Deal|New|Tee Times?|Book)\\s+(?:at\\s+)?", RegexOption.IGNORE_CASE), "")
            ?.trim() ?: return null
        return "unlisted-" + simplify(name).replace(' ', '-') to name
    }

    fun simplify(name: String): String =
        NAME_NOISE.replace(name.lowercase().replace("&", " "), " ").trim().replace(Regex("\\s+"), " ")

    private fun monthOf(token: String): Int = MONTHS.getValue(token.lowercase().take(3))

    /** Alerts are about upcoming slots: pick the year that puts the date on/after (received - 7 days). */
    private fun inferYear(day: Int, month: Int, received: LocalDate): LocalDate? {
        val thisYear = runCatching { LocalDate.of(received.year, month, day) }.getOrNull() ?: return null
        return if (thisYear.isBefore(received.minusDays(7))) thisYear.plusYears(1) else thisYear
    }
}
