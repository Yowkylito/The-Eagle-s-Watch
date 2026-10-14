package ph.eagleswatch.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

private const val Columns = 10

/** How long each coin takes to flip, as a share of the whole reveal. */
private const val FlipShare = 0.22f

/**
 * One hundred peso coins, one for every percent. Coins are grouped by share in reading order.
 *
 * @param counts coins per share, adding up to 100, largest first.
 * @param colors coin face for each share.
 * @param reveal 0 to 1, read while drawing. Coins flip from blank to their colour one after another as it rises.
 * @param highlight a share to pick out; the others fade back.
 */
@Composable
fun CoinGrid(
    counts: List<Int>,
    colors: List<Color>,
    reveal: () -> Float,
    highlight: Int?,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.aspectRatio(1f)) {
        val cell = size.width / Columns
        val radius = cell * 0.43f
        val stagger = (1f - FlipShare) / 100f
        val progress = reveal()
        var coin = 0
        counts.forEachIndexed { share, count ->
            repeat(count) {
                val center = Offset(cell * (coin % Columns + 0.5f), cell * (coin / Columns + 0.5f))
                val t = ((progress - coin * stagger) / FlipShare).coerceIn(0f, 1f)
                // A flip: the coin narrows to its edge, then widens showing the other face.
                val squeeze = abs(cos(PI * t)).toFloat().coerceAtLeast(0.06f)
                val faceUp = t >= 0.5f
                val dimmed = faceUp && highlight != null && highlight != share
                val face = if (faceUp) colors[share] else EagleColors.CoinBlank
                val alpha = if (dimmed) 0.22f else 1f
                val w = radius * squeeze
                drawOval(face.copy(alpha = alpha), Offset(center.x - w, center.y - radius), Size(w * 2, radius * 2))
                val rim = radius * 0.72f
                drawOval(
                    color = Color.White.copy(alpha = (if (faceUp) 0.4f else 0.7f) * alpha),
                    topLeft = Offset(center.x - rim * squeeze, center.y - rim),
                    size = Size(rim * squeeze * 2, rim * 2),
                    style = Stroke(width = radius * 0.1f),
                )
                coin++
            }
        }
    }
}
