package ph.eagleswatch.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import kotlin.math.PI
import kotlin.math.sin
import ph.eagleswatch.content.Category

/**
 * A flat illustration of a project at a stage of its works: 0 is a cleared site, 1 is finished.
 * These stand in for site photos in the design preview, so they are drawn, never photographic.
 */
@Composable
fun ProjectArt(category: Category, stage: Float, palette: Int, modifier: Modifier = Modifier) {
    Canvas(modifier) { Scene(this, stage.coerceIn(0f, 1f), Skies[palette.mod(Skies.size)]).draw(category) }
}

private class Sky(
    val top: Color,
    val bottom: Color,
    val sun: Color,
    val far: Color,
    val near: Color,
    val ground: Color,
)

private val Skies = listOf(
    Sky(Color(0xFF6FB1F0), Color(0xFFD6ECFF), Color(0xFFFFF6D8), Color(0xFF9CC3B0), Color(0xFF6FA585), Color(0xFF8CC26F)),
    Sky(Color(0xFF8FC9EA), Color(0xFFFFEBC9), Color(0xFFFFF1B8), Color(0xFFA9C9A4), Color(0xFF7DAE7B), Color(0xFF97C774)),
    Sky(Color(0xFFF59E5B), Color(0xFFFFDDA6), Color(0xFFFFF4D6), Color(0xFFC9A27A), Color(0xFF9C8A5E), Color(0xFFB5B36A)),
    Sky(Color(0xFF9FB3CC), Color(0xFFE3E9F1), Color(0xFFF4F6FA), Color(0xFF95ABA5), Color(0xFF6E8E83), Color(0xFF86AE78)),
)

private val Concrete = Color(0xFFDAD6CD)
private val ConcreteShade = Color(0xFFB3AEA3)
private val Steel = Color(0xFF5B6577)
private val Soil = Color(0xFFC9A06A)
private val SoilShade = Color(0xFFA67F4C)
private val River = Color(0xFF4F9FDE)
private val RiverLight = Color(0xFF8CC4EE)
private val Wall = Color(0xFFFFF5DE)
private val Glass = Color(0xFF9CC7EE)
private val Leaf = Color(0xFF3F7D54)
private val LeafLight = Color(0xFF5DA06F)
private val Machine = Color(0xFFF4B41A)

private class Scene(val s: DrawScope, val stage: Float, val sky: Sky) {
    val w = s.size.width
    val h = s.size.height
    val horizon = h * 0.5f

    /** Where buildings stand. */
    val base = horizon + (h - horizon) * 0.58f
    val done = stage >= 1f
    val building = stage in 0.12f..0.97f

    fun x(f: Float) = w * f
    fun y(f: Float) = h * f

    fun rect(color: Color, left: Float, top: Float, right: Float, bottom: Float, radius: Float = 0f) {
        if (right <= left || bottom <= top) return
        s.drawRoundRect(color, Offset(left, top), Size(right - left, bottom - top), CornerRadius(radius))
    }

    fun poly(color: Color, points: List<Offset>) {
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
            close()
        }
        s.drawPath(path, color)
    }

    fun line(color: Color, x1: Float, y1: Float, x2: Float, y2: Float, width: Float) =
        s.drawLine(color, Offset(x1, y1), Offset(x2, y2), width, StrokeCap.Round)

    fun draw(category: Category) {
        backdrop()
        when (category) {
            Category.Bridges -> bridge()
            Category.Roads -> road()
            Category.Schools -> school()
            Category.Health -> clinic()
            Category.Water -> waterTower()
            Category.FloodControl -> riverbank()
            Category.Farms -> canal()
            Category.Buildings -> court()
        }
        foreground(category)
    }

    private fun backdrop() {
        s.drawRect(Brush.verticalGradient(listOf(sky.top, sky.bottom), endY = horizon), size = Size(w, horizon + 1))
        s.drawCircle(sky.sun, h * 0.075f, Offset(x(0.8f), y(0.16f)))
        cloud(x(0.22f), y(0.14f), h * 0.05f)
        cloud(x(0.62f), y(0.27f), h * 0.035f)
        hills(sky.far, horizon - h * 0.13f, h * 0.05f, 1.3f, 0.2f)
        hills(sky.near, horizon - h * 0.06f, h * 0.035f, 2.1f, 1.4f)
        rect(sky.ground, 0f, horizon, w, h)
    }

    private fun cloud(cx: Float, cy: Float, r: Float) {
        val white = Color.White.copy(alpha = 0.85f)
        s.drawCircle(white, r, Offset(cx, cy))
        s.drawCircle(white, r * 0.75f, Offset(cx - r * 1.1f, cy + r * 0.3f))
        s.drawCircle(white, r * 0.85f, Offset(cx + r * 1.1f, cy + r * 0.2f))
        rect(white, cx - r * 1.8f, cy + r * 0.3f, cx + r * 1.9f, cy + r * 1.05f, r * 0.4f)
    }

    private fun hills(color: Color, top: Float, amplitude: Float, waves: Float, phase: Float) {
        val path = Path().apply {
            moveTo(0f, horizon + 1)
            var i = 0
            while (i <= 24) {
                val f = i / 24f
                lineTo(w * f, top + amplitude * sin((f * waves * 2 * PI + phase).toFloat()))
                i++
            }
            lineTo(w, horizon + 1)
            close()
        }
        s.drawPath(path, color)
    }

    /** Bare earth where the works are, shrinking as the site is finished. */
    private fun site(left: Float, right: Float) {
        if (done) return
        rect(Soil, left, base - h * 0.02f, right, base + h * 0.07f, h * 0.02f)
    }

    // --- Subjects -------------------------------------------------------------------------------------

    private fun bridge() {
        val riverTop = horizon + (h - horizon) * 0.42f
        rect(River, 0f, riverTop, w, h * 0.92f)
        line(RiverLight, x(0.1f), riverTop + h * 0.08f, x(0.32f), riverTop + h * 0.08f, h * 0.012f)
        line(RiverLight, x(0.55f), riverTop + h * 0.16f, x(0.85f), riverTop + h * 0.16f, h * 0.012f)
        val deckY = riverTop - h * 0.1f
        val deckH = h * 0.04f
        // Abutments on both banks.
        rect(ConcreteShade, x(0.02f), deckY, x(0.1f), riverTop + h * 0.02f)
        rect(ConcreteShade, x(0.9f), deckY, x(0.98f), riverTop + h * 0.02f)
        if (stage > 0.1f) {
            listOf(0.3f, 0.5f, 0.7f).forEach { px ->
                val grown = ((stage - 0.1f) / 0.3f).coerceIn(0f, 1f)
                val pierTop = h * 0.92f - (h * 0.92f - deckY) * grown
                rect(Concrete, x(px) - h * 0.025f, pierTop, x(px) + h * 0.025f, h * 0.92f)
                rect(ConcreteShade, x(px) - h * 0.025f, h * 0.88f, x(px) + h * 0.025f, h * 0.92f)
            }
        }
        val deckFraction = ((stage - 0.35f) / 0.5f).coerceIn(0f, 1f)
        if (deckFraction > 0f) {
            val end = x(0.06f) + (x(0.94f) - x(0.06f)) * deckFraction
            rect(Concrete, x(0.06f), deckY, end, deckY + deckH)
            rect(ConcreteShade, x(0.06f), deckY + deckH * 0.7f, end, deckY + deckH)
        }
        if (stage > 0.85f) {
            val railY = deckY - h * 0.035f
            line(Steel, x(0.06f), railY, x(0.94f), railY, h * 0.008f)
            var px = 0.08f
            while (px < 0.94f) {
                line(Steel, x(px), railY, x(px), deckY, h * 0.006f)
                px += 0.04f
            }
        }
    }

    private fun road() {
        val cut = h - (h - horizon) * stage
        val leftAt = { yy: Float -> x(0.12f) + (x(0.47f) - x(0.12f)) * ((h - yy) / (h - horizon)) }
        val rightAt = { yy: Float -> x(0.88f) - (x(0.88f) - x(0.53f)) * ((h - yy) / (h - horizon)) }
        poly(Soil, listOf(Offset(x(0.12f), h), Offset(x(0.47f), horizon), Offset(x(0.53f), horizon), Offset(x(0.88f), h)))
        if (stage > 0f) {
            poly(Concrete, listOf(Offset(x(0.12f), h), Offset(leftAt(cut), cut), Offset(rightAt(cut), cut), Offset(x(0.88f), h)))
            // Expansion joints across the slab, closer together toward the horizon.
            var f = 0.06f
            while (f < 1f) {
                val yy = h - (h - horizon) * (1 - (1 - f) * (1 - f))
                if (yy > cut) line(ConcreteShade, leftAt(yy), yy, rightAt(yy), yy, h * 0.004f)
                f += 0.09f
            }
            line(ConcreteShade, x(0.5f), h, x(0.5f), cut.coerceAtLeast(horizon), h * 0.004f)
        }
        if (!done && stage > 0f) {
            val rx = (leftAt(cut) + rightAt(cut)) / 2
            val size = h * 0.05f * (1.4f - (h - cut) / (h - horizon))
            rect(Machine, rx - size, cut - size * 1.4f, rx + size, cut - size * 0.2f, size * 0.2f)
            rect(Steel, rx - size * 1.1f, cut - size * 0.5f, rx + size * 1.1f, cut + size * 0.2f, size * 0.3f)
        }
    }

    /** A building that rises with the works: footings, then the concrete frame, then walls and windows, then the roof. */
    private fun frame(left: Float, right: Float, floors: Int, wallColor: Color, trim: Color) {
        val floorH = h * 0.17f
        val top = base - floorH * floors
        site(left - x(0.04f), right + x(0.04f))
        rect(ConcreteShade, left - x(0.01f), base - h * 0.012f, right + x(0.01f), base + h * 0.008f)
        if (stage < 0.12f) return
        val framed = ((stage - 0.12f) / 0.5f).coerceIn(0f, 1f) * floors
        val walled = ((stage - 0.4f) / 0.45f).coerceIn(0f, 1f) * floors
        val columns = 5
        for (floor in 0 until floors) {
            val grown = (framed - floor).coerceIn(0f, 1f)
            if (grown <= 0f) break
            val floorBottom = base - floorH * floor
            val floorTop = floorBottom - floorH * grown
            if (walled - floor >= 1f) {
                rect(wallColor, left, floorTop, right, floorBottom)
                repeat(4) { i ->
                    val wx = left + (right - left) * (0.1f + i * 0.22f)
                    rect(Glass, wx, floorTop + floorH * 0.25f, wx + (right - left) * 0.14f, floorTop + floorH * 0.65f, h * 0.006f)
                }
            }
            for (c in 0 until columns) {
                val cx = left + (right - left) * c / (columns - 1)
                line(ConcreteShade, cx, floorBottom, cx, floorTop, h * 0.016f)
            }
            line(ConcreteShade, left, floorTop, right, floorTop, h * 0.02f)
        }
        if (stage >= 0.9f) {
            rect(trim, left - x(0.02f), top - h * 0.03f, right + x(0.02f), top + h * 0.005f, h * 0.006f)
            rect(trim, (left + right) / 2 - x(0.035f), base - h * 0.1f, (left + right) / 2 + x(0.035f), base)
        } else if (building) {
            // Scaffolding along the side still being built.
            val scaffoldTop = base - floorH * framed
            line(Machine.copy(alpha = 0.7f), right + x(0.025f), base, right + x(0.025f), scaffoldTop, h * 0.006f)
            var yy = base - floorH * 0.33f
            while (yy > scaffoldTop) {
                line(Machine.copy(alpha = 0.7f), right - x(0.06f), yy, right + x(0.025f), yy, h * 0.006f)
                yy -= floorH * 0.33f
            }
        }
    }

    private fun school() {
        val left = x(0.24f)
        val right = x(0.76f)
        frame(left, right, floors = 2, wallColor = Wall, trim = EagleColors.Blue)
        if (stage >= 0.9f) {
            // Flagpole with a simplified flag.
            val poleX = x(0.15f)
            line(Steel, poleX, base, poleX, base - h * 0.36f, h * 0.008f)
            val fy = base - h * 0.36f
            rect(EagleColors.Blue, poleX, fy, poleX + h * 0.12f, fy + h * 0.035f)
            rect(EagleColors.Red, poleX, fy + h * 0.035f, poleX + h * 0.12f, fy + h * 0.07f)
            poly(Color.White, listOf(Offset(poleX, fy), Offset(poleX + h * 0.06f, fy + h * 0.035f), Offset(poleX, fy + h * 0.07f)))
        }
    }

    private fun clinic() {
        val left = x(0.18f)
        val right = x(0.82f)
        frame(left, right, floors = 1, wallColor = Color.White, trim = EagleColors.Green)
        if (stage >= 0.9f) {
            val cx = (left + right) / 2
            val cy = base - h * 0.17f - h * 0.09f
            val r = h * 0.05f
            rect(Color.White, cx - r, cy - r, cx + r, cy + r, r * 0.3f)
            rect(EagleColors.Green, cx - r * 0.22f, cy - r * 0.7f, cx + r * 0.22f, cy + r * 0.7f)
            rect(EagleColors.Green, cx - r * 0.7f, cy - r * 0.22f, cx + r * 0.7f, cy + r * 0.22f)
        }
    }

    private fun waterTower() {
        val cx = x(0.5f)
        val platform = base - h * 0.32f
        site(cx - x(0.18f), cx + x(0.18f))
        // Pump house.
        rect(Wall, x(0.2f), base - h * 0.09f, x(0.3f), base)
        rect(EagleColors.Blue, x(0.19f), base - h * 0.11f, x(0.31f), base - h * 0.085f)
        val legs = ((stage - 0.1f) / 0.4f).coerceIn(0f, 1f)
        if (legs > 0f) {
            val legTop = base - (base - platform) * legs
            listOf(-0.09f, 0.09f).forEach { dx -> line(Steel, cx + x(dx), base, cx + x(dx * 0.75f), legTop, h * 0.014f) }
            if (legs >= 1f) {
                line(Steel, cx - x(0.08f), base - h * 0.1f, cx + x(0.08f), base - h * 0.2f, h * 0.007f)
                line(Steel, cx + x(0.08f), base - h * 0.1f, cx - x(0.08f), base - h * 0.2f, h * 0.007f)
            }
        }
        if (stage > 0.5f) {
            val tankH = h * 0.16f
            rect(RiverLight, cx - x(0.12f), platform - tankH, cx + x(0.12f), platform, h * 0.02f)
            rect(River, cx - x(0.12f), platform - tankH * 0.35f, cx + x(0.12f), platform, h * 0.02f)
            s.drawOval(Color.White.copy(alpha = 0.6f), Offset(cx - x(0.12f), platform - tankH - h * 0.02f), Size(x(0.24f), h * 0.04f))
        }
        if (stage > 0.6f) line(Steel, x(0.3f), base - h * 0.02f, cx - x(0.09f), base - h * 0.02f, h * 0.01f)
    }

    private fun riverbank() {
        // The river runs along the bottom right; the bank slopes down to it.
        val bankTop = horizon + (h - horizon) * 0.25f
        poly(River, listOf(Offset(x(0.35f), h), Offset(x(0.7f), bankTop + h * 0.08f), Offset(w, bankTop + h * 0.06f), Offset(w, h)))
        val slope = listOf(Offset(x(0.18f), h), Offset(x(0.55f), bankTop), Offset(x(0.7f), bankTop + h * 0.08f), Offset(x(0.35f), h))
        poly(SoilShade, slope)
        val lined = stage.coerceIn(0f, 1f)
        if (lined > 0f) {
            // Stone masonry laid from the downstream end up.
            s.clipRect(top = h - (h - bankTop) * lined, bottom = h) {
                poly(ConcreteShade, slope)
                var row = 0
                var yy = h
                while (yy > bankTop) {
                    val t = (h - yy) / (h - bankTop)
                    val l = x(0.18f) + (x(0.55f) - x(0.18f)) * t
                    val r = x(0.35f) + (x(0.7f) - x(0.35f)) * t
                    var xx = l + if (row % 2 == 0) 0f else h * 0.02f
                    while (xx < r - h * 0.02f) {
                        rect(Concrete, xx, yy - h * 0.022f, xx + h * 0.036f, yy - h * 0.004f, h * 0.008f)
                        xx += h * 0.042f
                    }
                    yy -= h * 0.026f
                    row++
                }
            }
        }
        if (done) {
            line(Steel, x(0.55f), bankTop - h * 0.03f, x(0.98f), bankTop - h * 0.01f, h * 0.008f)
        }
    }

    private fun canal() {
        // Rice paddies in rows toward the horizon.
        var f = 0f
        var dark = false
        while (f < 1f) {
            val y1 = horizon + (h - horizon) * f * f
            val next = f + 0.12f
            val y2 = horizon + (h - horizon) * (next * next).coerceAtMost(1f)
            rect(if (dark) sky.near else sky.ground, 0f, y1, w, y2)
            dark = !dark
            f = next
        }
        val near = listOf(Offset(x(0.3f), h), Offset(x(0.52f), h))
        val far = listOf(Offset(x(0.66f), horizon), Offset(x(0.69f), horizon))
        poly(SoilShade, listOf(near[0], far[0], far[1], near[1]))
        val lined = stage
        if (lined > 0f) {
            val t = lined
            val farL = Offset(near[0].x + (far[0].x - near[0].x) * t, h - (h - horizon) * t)
            val farR = Offset(near[1].x + (far[1].x - near[1].x) * t, h - (h - horizon) * t)
            poly(Concrete, listOf(near[0], farL, farR, near[1]))
            val inset = 0.18f
            poly(
                River,
                listOf(
                    Offset(near[0].x + (near[1].x - near[0].x) * inset, h),
                    Offset(farL.x + (farR.x - farL.x) * inset, farL.y),
                    Offset(farR.x - (farR.x - farL.x) * inset, farR.y),
                    Offset(near[1].x - (near[1].x - near[0].x) * inset, h),
                ),
            )
        }
        // Water still in the unlined stretch.
        poly(
            River.copy(alpha = 0.7f),
            listOf(
                Offset(x(0.666f), horizon),
                Offset(x(0.684f), horizon),
                Offset(x(0.6f), horizon + (h - horizon) * 0.3f),
                Offset(x(0.57f), horizon + (h - horizon) * 0.3f),
            ),
        )
    }

    private fun court() {
        val left = x(0.16f)
        val right = x(0.84f)
        val roofTop = base - h * 0.36f
        val eave = base - h * 0.2f
        site(left - x(0.03f), right + x(0.03f))
        if (done) {
            rect(Color(0xFF3E8E7E), left + x(0.04f), base - h * 0.03f, right - x(0.04f), base + h * 0.06f, h * 0.01f)
            line(Color.White, (left + right) / 2, base - h * 0.03f, (left + right) / 2, base + h * 0.06f, h * 0.005f)
        }
        val columns = listOf(0f, 0.33f, 0.66f, 1f)
        val grown = ((stage - 0.1f) / 0.4f).coerceIn(0f, 1f)
        columns.forEach { c ->
            val cx = left + (right - left) * c
            rect(ConcreteShade, cx - h * 0.018f, base - h * 0.01f, cx + h * 0.018f, base + h * 0.01f)
            if (grown > 0f) line(Steel, cx, base, cx, base - (base - eave) * grown, h * 0.016f)
        }
        if (stage > 0.45f) {
            val arch = Path().apply {
                moveTo(left - x(0.02f), eave)
                quadraticTo((left + right) / 2, roofTop - h * 0.1f, right + x(0.02f), eave)
            }
            if (stage >= 0.85f) {
                arch.lineTo(left - x(0.02f), eave)
                arch.close()
                s.drawPath(arch, EagleColors.Blue)
            } else {
                s.drawPath(arch, Steel, style = Stroke(width = h * 0.012f))
                columns.forEach { c ->
                    val cx = left + (right - left) * c
                    line(Steel, cx, eave, (left + right) / 2, roofTop + h * 0.02f, h * 0.006f)
                }
            }
        }
    }

    // --- Foreground: machines while building, trees when done --------------------------------------------

    private fun foreground(category: Category) {
        if (done) {
            tree(x(0.06f), h * 0.94f, 1f)
            tree(x(0.93f), h * 0.97f, 0.8f)
            return
        }
        if (building && category in setOf(Category.Schools, Category.Health, Category.Water, Category.Buildings, Category.Bridges)) {
            crane(if (category == Category.Bridges) x(0.86f) else x(0.88f))
        }
        if (stage < 0.12f && category != Category.Roads) {
            excavator(x(0.7f), h * 0.92f)
        }
        barrier(x(0.04f), h * 0.93f)
    }

    private fun tree(cx: Float, ground: Float, scale: Float) {
        val size = h * 0.09f * scale
        line(SoilShade, cx, ground, cx, ground - size * 1.3f, size * 0.22f)
        s.drawCircle(Leaf, size * 0.75f, Offset(cx, ground - size * 1.6f))
        s.drawCircle(LeafLight, size * 0.5f, Offset(cx - size * 0.25f, ground - size * 1.8f))
    }

    private fun crane(mastX: Float) {
        val top = y(0.1f)
        val width = h * 0.022f
        line(Machine, mastX, base + h * 0.02f, mastX, top, width)
        var yy = base
        while (yy > top + width) {
            line(Machine.copy(alpha = 0.8f), mastX - width / 2, yy, mastX + width / 2, yy - width, h * 0.004f)
            yy -= width * 1.4f
        }
        line(Machine, mastX + x(0.06f), top, mastX - x(0.42f), top, h * 0.014f)
        rect(Steel, mastX + x(0.03f), top - h * 0.005f, mastX + x(0.07f), top + h * 0.03f)
        line(Steel, mastX - x(0.3f), top, mastX - x(0.3f), top + h * 0.16f, h * 0.004f)
        rect(Steel, mastX - x(0.3f) - h * 0.012f, top + h * 0.16f, mastX - x(0.3f) + h * 0.012f, top + h * 0.18f)
    }

    private fun excavator(cx: Float, ground: Float) {
        val u = h * 0.05f
        rect(Steel, cx - u * 1.4f, ground - u * 0.5f, cx + u * 1.4f, ground, u * 0.25f)
        rect(Machine, cx - u * 1.1f, ground - u * 1.5f, cx + u * 0.6f, ground - u * 0.5f, u * 0.15f)
        rect(Glass, cx - u * 0.2f, ground - u * 1.35f, cx + u * 0.45f, ground - u * 0.85f, u * 0.08f)
        line(Machine, cx - u * 1f, ground - u * 1.2f, cx - u * 2.3f, ground - u * 2.2f, u * 0.28f)
        line(Machine, cx - u * 2.3f, ground - u * 2.2f, cx - u * 2.8f, ground - u * 0.9f, u * 0.24f)
        rect(Steel, cx - u * 3.1f, ground - u * 0.95f, cx - u * 2.5f, ground - u * 0.5f, u * 0.1f)
    }

    private fun barrier(left: Float, ground: Float) {
        val width = x(0.22f)
        val top = ground - h * 0.08f
        line(Steel, left + width * 0.1f, ground, left + width * 0.1f, top, h * 0.01f)
        line(Steel, left + width * 0.9f, ground, left + width * 0.9f, top, h * 0.01f)
        val stripes = 6
        repeat(stripes) { i ->
            rect(
                if (i % 2 == 0) Machine else Color.White,
                left + width * i / stripes,
                top - h * 0.03f,
                left + width * (i + 1) / stripes,
                top,
            )
        }
    }
}
