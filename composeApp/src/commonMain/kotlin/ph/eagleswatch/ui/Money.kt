package ph.eagleswatch.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/** "12,500" for 12500. */
fun groupDigits(amount: Long): String =
    amount.toString().reversed().chunked(3).joinToString(",").reversed()

/** "₱12,500" for 12500. */
fun formatPesos(amount: Long): String = "₱" + groupDigits(amount)

/** "₱250K" for 250000, for tight spaces. Falls back to [formatPesos] when the amount is not whole thousands. */
fun formatPesosShort(amount: Long): String =
    if (amount >= 1_000 && amount % 1_000 == 0L) "₱${groupDigits(amount / 1_000)}K" else formatPesos(amount)

/**
 * A peso amount whose digits roll like a counter when it changes, up when it grows and down when it
 * shrinks. The peso sign is set in Inter because the display face has no ₱.
 */
@Composable
fun RollingPesos(
    amount: Long,
    size: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = EagleColors.Ink,
    signLift: Dp,
) {
    val digits = groupDigits(amount)
    Row(
        modifier.clearAndSetSemantics { contentDescription = formatPesos(amount) },
        verticalAlignment = Alignment.Bottom,
    ) {
        EText(
            text = "₱",
            modifier = Modifier.padding(bottom = signLift),
            size = size * 0.6f,
            weight = FontWeight.SemiBold,
            color = color,
        )
        digits.forEachIndexed { i, char ->
            // Keyed from the right so the ones column stays the ones column as the number grows.
            key(digits.length - i) {
                AnimatedContent(
                    targetState = char,
                    modifier = Modifier.clipToBounds(),
                    transitionSpec = {
                        val up = targetState > initialState
                        (slideInVertically { if (up) it else -it } + fadeIn()) togetherWith
                            (slideOutVertically { if (up) -it else it } + fadeOut())
                    },
                    contentAlignment = Alignment.Center,
                    label = "digit",
                ) { shown ->
                    EText(shown.toString(), display = true, size = size, color = color)
                }
            }
        }
    }
}
