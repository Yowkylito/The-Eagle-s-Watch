package ph.eagleswatch.content

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val MonthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** A calendar date, enough for project timelines without a date library. */
data class Day(val year: Int, val month: Int, val day: Int) : Comparable<Day> {
    /** Days since 1970-01-01. */
    val epochDay: Long
        get() {
            val y = if (month <= 2) year - 1 else year
            val era = (if (y >= 0) y else y - 399) / 400
            val yearOfEra = y - era * 400
            val shiftedMonth = (month + 9) % 12
            val dayOfYear = (153 * shiftedMonth + 2) / 5 + day - 1
            val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
            return era * 146_097L + dayOfEra - 719_468
        }

    override fun compareTo(other: Day): Int = epochDay.compareTo(other.epochDay)

    /** Days from this date to [other]; negative when [other] is earlier. */
    fun daysUntil(other: Day): Long = other.epochDay - epochDay

    /** "Mar 3, 2025" */
    fun format(): String = "${MonthNames[month - 1]} $day, $year"

    /** "Mar 2025" */
    fun monthYear(): String = "${MonthNames[month - 1]} $year"

    /** "2025-03-03", for machine-readable places such as share text. */
    fun iso(): String = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

    companion object {
        fun ofEpochDay(epochDay: Long): Day {
            val z = epochDay + 719_468
            val era = (if (z >= 0) z else z - 146_096) / 146_097
            val dayOfEra = z - era * 146_097
            val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36_524 - dayOfEra / 146_096) / 365
            val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
            val shiftedMonth = (5 * dayOfYear + 2) / 153
            val day = (dayOfYear - (153 * shiftedMonth + 2) / 5 + 1).toInt()
            val month = (if (shiftedMonth < 10) shiftedMonth + 3 else shiftedMonth - 9).toInt()
            val year = (yearOfEra + era * 400 + if (month <= 2) 1 else 0).toInt()
            return Day(year, month, day)
        }

        /** Today in Philippine time. */
        @OptIn(ExperimentalTime::class)
        fun today(): Day = ofEpochDay((Clock.System.now().epochSeconds + 8 * 3600) / 86_400)
    }
}
