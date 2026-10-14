package ph.eagleswatch.ui

import kotlin.math.roundToLong

/** "12,500" for 12500. */
fun groupDigits(amount: Long): String =
    amount.toString().reversed().chunked(3).joinToString(",").reversed()

/** "₱12,500" for 12500. */
fun formatPesos(amount: Long): String = "₱" + groupDigits(amount)

/** "₱48.5M", "₱2.15M", "₱850K", "₱1.2B": short enough for a card, precise to three figures. */
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
    var factor = 1L
    repeat(decimals) { factor *= 10 }
    val rounded = (scaled * factor).roundToLong()
    val whole = rounded / factor
    val fraction = (rounded % factor).toString().padStart(decimals, '0').trimEnd('0')
    return if (fraction.isEmpty()) "$whole$suffix" else "$whole.$fraction$suffix"
}

/** "78%" */
fun percent(fraction: Float): String = "${(fraction * 100).roundToLong()}%"
