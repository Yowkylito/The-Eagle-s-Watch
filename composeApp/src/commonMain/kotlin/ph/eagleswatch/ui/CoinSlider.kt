package ph.eagleswatch.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private val ThumbSize = 44.dp

/**
 * Picks a peso amount on a log scale, so ₱5,000 and ₱500,000 are both easy to reach.
 * The thumb is a peso coin. Drag it, tap the track, or use the arrow keys.
 */
@Composable
fun CoinSlider(
    value: Long,
    onValueChange: (Long) -> Unit,
    min: Long,
    max: Long,
    label: String,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val latestValue by rememberUpdatedState(value)
    val change by rememberUpdatedState { next: Long ->
        if (next != latestValue) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onValueChange(next)
        }
    }
    val fraction = fractionOf(value, min, max)
    val shown by animateFloatAsState(fraction, spring(stiffness = Spring.StiffnessMedium), label = "thumb")

    val density = LocalDensity.current
    val thumbPx = with(density) { ThumbSize.toPx() }
    var widthPx by remember { mutableFloatStateOf(0f) }
    fun valueAtX(x: Float): Long {
        val track = (widthPx - thumbPx).coerceAtLeast(1f)
        return valueAt(((x - thumbPx / 2) / track).coerceIn(0f, 1f), min, max)
    }

    Box(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .onSizeChanged { widthPx = it.width.toFloat() }
            .semantics {
                contentDescription = label
                stateDescription = formatPesos(value)
                progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                setProgress { target ->
                    change(valueAt(target.coerceIn(0f, 1f), min, max))
                    true
                }
            }
            .focusRing(RoundedCornerShape(26.dp))
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val step = when (event.key) {
                    Key.DirectionRight, Key.DirectionUp -> 1
                    Key.DirectionLeft, Key.DirectionDown -> -1
                    else -> return@onKeyEvent false
                }
                change(stepFrom(latestValue, step, min, max))
                true
            }
            .focusable()
            .pointerInput(min, max) { detectTapGestures { change(valueAtX(it.x)) } }
            .pointerInput(min, max) {
                detectHorizontalDragGestures(onDragStart = { change(valueAtX(it.x)) }) { pointer, _ ->
                    pointer.consume()
                    change(valueAtX(pointer.position.x))
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val y = size.height / 2
            val start = thumbPx / 2
            val end = size.width - thumbPx / 2
            val stroke = 8.dp.toPx()
            drawLine(EagleColors.BlueSoft, Offset(start, y), Offset(end, y), stroke, StrokeCap.Round)
            drawLine(EagleColors.Gold, Offset(start, y), Offset(start + (end - start) * shown, y), stroke, StrokeCap.Round)
        }
        Box(
            Modifier
                .offset { IntOffset(((widthPx - thumbPx) * shown).roundToInt(), 0) }
                .size(ThumbSize)
                .shadow(6.dp, CircleShape)
                .background(EagleColors.GoldDeep, CircleShape)
                .padding(3.dp)
                .background(EagleColors.Gold, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            EText("₱", size = 20.sp, weight = FontWeight.SemiBold, color = EagleColors.Ink)
        }
    }
}

private fun fractionOf(value: Long, min: Long, max: Long): Float =
    (log10(value.toDouble() / min) / log10(max.toDouble() / min)).toFloat().coerceIn(0f, 1f)

private fun valueAt(fraction: Float, min: Long, max: Long): Long {
    val raw = min * (max.toDouble() / min).pow(fraction.toDouble())
    return roundNicely(raw).coerceIn(min, max)
}

/** Rounds to steps a person would type: hundreds below ₱10,000, thousands below ₱100,000, then fives of thousands. */
private fun roundNicely(raw: Double): Long {
    val step = when {
        raw < 10_000 -> 100.0
        raw < 100_000 -> 1_000.0
        else -> 5_000.0
    }
    return ((raw / step).roundToLong() * step).toLong()
}

private fun stepFrom(value: Long, direction: Int, min: Long, max: Long): Long {
    var fraction = fractionOf(value, min, max)
    // Nudge until the rounded value actually moves.
    repeat(20) {
        fraction = (fraction + direction * 0.01f).coerceIn(0f, 1f)
        val next = valueAt(fraction, min, max)
        if (next != value) return next
    }
    return value
}
