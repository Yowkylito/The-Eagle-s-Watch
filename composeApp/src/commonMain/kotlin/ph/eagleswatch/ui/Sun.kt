package ph.eagleswatch.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The flag's sun: a disc with eight rays, each a long ray between two shorter ones.
 *
 * @param turn rotation in degrees.
 * @param grow 0 to 1, how far the rays reach out from the disc.
 */
@Composable
fun Sun(modifier: Modifier = Modifier, turn: Float = 0f, grow: Float = 1f, color: Color = Color.White) {
    Canvas(modifier) { drawSun(center, size.minDimension / 2, turn, grow, color) }
}

/** Draws the sun without taking part in layout, e.g. from `drawBehind`, so it can spill past its parent. */
fun DrawScope.drawSun(center: Offset, radius: Float, turn: Float = 0f, grow: Float = 1f, color: Color = Color.White) {
    val start = radius * 0.38f
    drawCircle(color, radius * 0.3f, center)
    if (grow <= 0f) return
    val rays = Path()
    repeat(8) { k ->
        val angle = k * 45f + turn
        rays.addRay(center, angle, halfWidth = 3.6f, inner = start, outer = start + (radius - start) * grow)
        listOf(-9f, 9f).forEach { side ->
            rays.addRay(center, angle + side, halfWidth = 2.4f, inner = start, outer = start + (radius * 0.74f - start) * grow)
        }
    }
    drawPath(rays, color)
}

/** A thin wedge pointing out from [center] at [angle] degrees. */
private fun Path.addRay(center: Offset, angle: Float, halfWidth: Float, inner: Float, outer: Float) {
    moveTo(center + polar(inner, angle - halfWidth))
    lineTo(center + polar(outer, angle))
    lineTo(center + polar(inner, angle + halfWidth))
    close()
}

private fun Path.moveTo(point: Offset) = moveTo(point.x, point.y)

private fun Path.lineTo(point: Offset) = lineTo(point.x, point.y)

private fun polar(radius: Float, degrees: Float): Offset {
    val radians = degrees * PI.toFloat() / 180f
    return Offset(cos(radians) * radius, sin(radians) * radius)
}
