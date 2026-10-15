package ph.eagleswatch.ui

import kotlin.math.abs
import kotlin.math.roundToLong
import ph.eagleswatch.content.Coordinates

/** "12,500" for 12500. */
fun groupDigits(amount: Long): String =
    amount.toString().reversed().chunked(3).joinToString(",").reversed()

/** "₱12,500" for 12500. */
fun formatPesos(amount: Long): String = "₱" + groupDigits(amount)

/** "₱48.5M", "₱2.15M", "₱850K", "₱1.2B": short enough for a row, precise to three figures. */
fun compactPesos(amount: Long): String = "₱" + compact(amount)

/** "1.2K" for 1184, "412" for 412. */
fun compactCount(count: Int): String = if (count < 1_000) count.toString() else compact(count.toLong())

private fun compact(value: Long): String {
    val (divisor, suffix) = when {
        value >= 1_000_000_000 -> 1_000_000_000.0 to "B"
        value >= 1_000_000 -> 1_000_000.0 to "M"
        value >= 1_000 -> 1_000.0 to "K"
        else -> return value.toString()
    }
    val scaled = value / divisor
    // Three significant figures at most, without trailing zeros: 48.5, 2.15, 120.
    val decimals = when {
        scaled >= 100 -> 0
        scaled >= 10 -> 1
        else -> 2
    }
    val text = fixed(scaled, decimals)
    return (if (decimals > 0) text.trimEnd('0').trimEnd('.') else text) + suffix
}

/** "78%" */
fun percent(fraction: Float): String = "${(fraction * 100).roundToLong()}%"

/** [value] with exactly [decimals] digits after the point, without platform formatting APIs. */
fun fixed(value: Double, decimals: Int): String {
    var factor = 1L
    repeat(decimals) { factor *= 10 }
    val rounded = (abs(value) * factor).roundToLong()
    val whole = rounded / factor
    val sign = if (value < 0) "-" else ""
    if (decimals == 0) return "$sign$whole"
    val fraction = (rounded % factor).toString().padStart(decimals, '0')
    return "$sign$whole.$fraction"
}

/** "14.967° N, 120.633° E" */
fun formatCoordinates(c: Coordinates): String {
    val ns = if (c.lat >= 0) "N" else "S"
    val ew = if (c.lon >= 0) "E" else "W"
    return "${fixed(abs(c.lat), 3)}° $ns, ${fixed(abs(c.lon), 3)}° $ew"
}

/** "1 day", "14 days". */
fun days(count: Long): String = if (count == 1L) "1 day" else "$count days"
