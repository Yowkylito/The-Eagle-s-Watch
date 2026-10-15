package ph.eagleswatch.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ph.eagleswatch.content.Category
import ph.eagleswatch.content.Verification

/** Line icons drawn on a 24-unit grid with one stroke weight, so the app needs no icon library. */
enum class Glyph {
    Search, Filter, Sort, Eye, EyeFilled, Share, Back, Close, ChevronRight, ChevronDown,
    Calendar, Peso, Agency, Person, Pin, Map, External, Document, Photo, Bell, BellFilled, Info, Alert,
    HardHat, Plus, Flag, FlagFilled, Check, CheckCircle, CircleDashed, Clock,
    Road, Bridge, School, Health, Water, Flood, Sprout, Building,
}

val Category.glyph: Glyph
    get() = when (this) {
        Category.Roads -> Glyph.Road
        Category.Bridges -> Glyph.Bridge
        Category.Schools -> Glyph.School
        Category.Health -> Glyph.Health
        Category.Water -> Glyph.Water
        Category.FloodControl -> Glyph.Flood
        Category.Irrigation -> Glyph.Sprout
        Category.Buildings -> Glyph.Building
    }

val Verification.glyph: Glyph
    get() = when (this) {
        Verification.Verified -> Glyph.CheckCircle
        Verification.AgencyReported -> Glyph.Agency
        Verification.CommunityReported -> Glyph.Person
        Verification.Unavailable -> Glyph.CircleDashed
    }

/** @param weight stroke width in grid units; selected tabs use a heavier line. */
@Composable
fun Icon(glyph: Glyph, modifier: Modifier = Modifier, tint: Color = EagleColors.Ink, size: Dp = 20.dp, weight: Float = 1.8f) {
    Canvas(modifier.size(size)) { Pen(this, tint, weight).draw(glyph) }
}

private class Pen(val scope: DrawScope, val color: Color, weight: Float) {
    val u = scope.size.width / 24f
    val stroke = Stroke(width = weight * u, cap = StrokeCap.Round, join = StrokeJoin.Round)

    fun o(x: Float, y: Float) = Offset(x * u, y * u)

    fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
        scope.drawLine(color, o(x1, y1), o(x2, y2), stroke.width, StrokeCap.Round)

    fun circle(cx: Float, cy: Float, r: Float, fill: Boolean = false, tint: Color = color) =
        scope.drawCircle(tint, r * u, o(cx, cy), style = if (fill) Fill else stroke)

    fun rect(x: Float, y: Float, w: Float, h: Float, r: Float, fill: Boolean = false) =
        scope.drawRoundRect(color, o(x, y), Size(w * u, h * u), CornerRadius(r * u), style = if (fill) Fill else stroke)

    fun arc(cx: Float, cy: Float, r: Float, start: Float, sweep: Float) =
        scope.drawArc(color, start, sweep, false, o(cx - r, cy - r), Size(2 * r * u, 2 * r * u), style = stroke)

    fun path(fill: Boolean = false, tint: Color = color, block: P.() -> Unit) {
        val p = P().apply(block)
        scope.drawPath(p.path, tint, style = if (fill) Fill else stroke)
    }

    inner class P {
        val path = Path()
        fun m(x: Float, y: Float) = path.moveTo(x * u, y * u)
        fun l(x: Float, y: Float) = path.lineTo(x * u, y * u)
        fun q(x1: Float, y1: Float, x2: Float, y2: Float) = path.quadraticTo(x1 * u, y1 * u, x2 * u, y2 * u)
        fun arc(cx: Float, cy: Float, r: Float, start: Float, sweep: Float) =
            path.arcTo(Rect(o(cx - r, cy - r), Size(2 * r * u, 2 * r * u)), start, sweep, forceMoveTo = false)
        fun z() = path.close()
    }

    private fun eye(filled: Boolean) {
        val shape: P.() -> Unit = { m(2f, 12f); q(12f, 2.5f, 22f, 12f); q(12f, 21.5f, 2f, 12f); z() }
        if (filled) {
            path(fill = true, block = shape)
            circle(12f, 12f, 3.6f, fill = true, tint = Color.White)
            circle(12f, 12f, 1.6f, fill = true)
        } else {
            path(block = shape)
            circle(12f, 12f, 3.2f)
        }
    }

    fun draw(glyph: Glyph) {
        when (glyph) {
            Glyph.Search -> { circle(10.8f, 10.8f, 6.6f); line(15.8f, 15.8f, 20.5f, 20.5f) }
            Glyph.Filter -> {
                listOf(7f to 9f, 12f to 15f, 17f to 7f).forEach { (y, x) ->
                    line(4f, y, 20f, y)
                    circle(x, y, 2.4f, fill = true, tint = EagleColors.Page)
                    circle(x, y, 2.4f)
                }
            }
            Glyph.Sort -> { line(4f, 7f, 20f, 7f); line(4f, 12f, 15f, 12f); line(4f, 17f, 10f, 17f) }
            Glyph.Eye -> eye(filled = false)
            Glyph.EyeFilled -> eye(filled = true)
            Glyph.Share -> {
                path { m(8.5f, 9f); l(6.5f, 9f); q(5f, 9f, 5f, 10.5f); l(5f, 19.5f); q(5f, 21f, 6.5f, 21f); l(17.5f, 21f); q(19f, 21f, 19f, 19.5f); l(19f, 10.5f); q(19f, 9f, 17.5f, 9f); l(15.5f, 9f) }
                line(12f, 3f, 12f, 14.5f)
                path { m(8.5f, 6.5f); l(12f, 3f); l(15.5f, 6.5f) }
            }
            Glyph.Back -> { line(19.5f, 12f, 4.5f, 12f); path { m(10.5f, 6f); l(4.5f, 12f); l(10.5f, 18f) } }
            Glyph.Close -> { line(6f, 6f, 18f, 18f); line(18f, 6f, 6f, 18f) }
            Glyph.ChevronRight -> path { m(9f, 5f); l(16f, 12f); l(9f, 19f) }
            Glyph.ChevronDown -> path { m(5f, 9f); l(12f, 16f); l(19f, 9f) }
            Glyph.Calendar -> { rect(3.5f, 5.5f, 17f, 15f, 2.5f); line(3.5f, 10f, 20.5f, 10f); line(8f, 3f, 8f, 7f); line(16f, 3f, 16f, 7f) }
            Glyph.Peso -> {
                circle(12f, 12f, 9f)
                line(10f, 7f, 10f, 17f)
                path { m(10f, 7f); l(13f, 7f); q(16.2f, 7f, 16.2f, 10f); q(16.2f, 13f, 13f, 13f); l(10f, 13f) }
                line(7.5f, 9.2f, 17f, 9.2f); line(7.5f, 11f, 17f, 11f)
            }
            Glyph.Agency -> {
                path { m(3f, 9f); l(12f, 4f); l(21f, 9f); z() }
                listOf(6f, 10f, 14f, 18f).forEach { line(it, 11.5f, it, 17.5f) }
                line(3f, 20f, 21f, 20f)
            }
            Glyph.Person -> { circle(12f, 8f, 3.8f); path { m(4.5f, 20.5f); q(5f, 13.5f, 12f, 13.5f); q(19f, 13.5f, 19.5f, 20.5f) } }
            Glyph.Pin -> { path { m(12f, 21f); q(5f, 14f, 5f, 9.5f); q(5f, 3f, 12f, 3f); q(19f, 3f, 19f, 9.5f); q(19f, 14f, 12f, 21f); z() }; circle(12f, 9.5f, 2.5f) }
            Glyph.Map -> {
                path { m(3f, 6f); l(9f, 4f); l(15f, 6f); l(21f, 4f); l(21f, 18f); l(15f, 20f); l(9f, 18f); l(3f, 20f); z() }
                line(9f, 4f, 9f, 18f); line(15f, 6f, 15f, 20f)
            }
            Glyph.External -> {
                path { m(13.5f, 4.5f); l(19.5f, 4.5f); l(19.5f, 10.5f) }
                line(19.5f, 4.5f, 11f, 13f)
                path { m(17f, 13.5f); l(17f, 18.5f); q(17f, 20f, 15.5f, 20f); l(5.5f, 20f); q(4f, 20f, 4f, 18.5f); l(4f, 8.5f); q(4f, 7f, 5.5f, 7f); l(10.5f, 7f) }
            }
            Glyph.Document -> {
                path { m(6f, 3f); l(14f, 3f); l(19f, 8f); l(19f, 19.5f); q(19f, 21f, 17.5f, 21f); l(6.5f, 21f); q(5f, 21f, 5f, 19.5f); l(5f, 4.5f); q(5f, 3f, 6.5f, 3f); z() }
                path { m(14f, 3f); l(14f, 8f); l(19f, 8f) }
                line(8.5f, 13f, 15.5f, 13f); line(8.5f, 16.5f, 13.5f, 16.5f)
            }
            Glyph.Photo -> {
                rect(3f, 5f, 18f, 14f, 2f)
                circle(8.5f, 9.5f, 1.6f, fill = true)
                path { m(5f, 17f); l(10f, 12.5f); l(13.5f, 15.5f); l(16f, 13f); l(19f, 16.5f) }
            }
            Glyph.Bell, Glyph.BellFilled -> {
                val shape: P.() -> Unit = { m(6f, 16.5f); l(6f, 11f); q(6f, 5f, 12f, 5f); q(18f, 5f, 18f, 11f); l(18f, 16.5f); l(19.5f, 18f); l(4.5f, 18f); z() }
                if (glyph == Glyph.BellFilled) path(fill = true, block = shape)
                path(block = shape)
                path { m(10f, 20.5f); q(12f, 22f, 14f, 20.5f) }
                line(12f, 3f, 12f, 5f)
            }
            Glyph.Info -> { circle(12f, 12f, 9f); line(12f, 11f, 12f, 16.5f); circle(12f, 7.8f, 0.6f, fill = true) }
            Glyph.Alert -> { path { m(12f, 3.5f); l(21.5f, 20f); l(2.5f, 20f); z() }; line(12f, 9.5f, 12f, 14.5f); circle(12f, 17.3f, 0.6f, fill = true) }
            Glyph.HardHat -> {
                path { m(5f, 16f); q(5f, 7.5f, 12f, 7.5f); q(19f, 7.5f, 19f, 16f) }
                line(3f, 16.5f, 21f, 16.5f); line(10f, 7.8f, 10f, 12.5f); line(14f, 7.8f, 14f, 12.5f)
            }
            Glyph.Plus -> { line(12f, 5f, 12f, 19f); line(5f, 12f, 19f, 12f) }
            Glyph.Flag, Glyph.FlagFilled -> {
                line(5f, 21f, 5f, 3.5f)
                val flag: P.() -> Unit = { m(5f, 4.5f); q(8.5f, 2.5f, 12f, 4.5f); q(15.5f, 6.5f, 19.5f, 4.5f); l(19.5f, 13.5f); q(15.5f, 15.5f, 12f, 13.5f); q(8.5f, 11.5f, 5f, 13.5f); z() }
                if (glyph == Glyph.FlagFilled) path(fill = true, block = flag)
                path(block = flag)
            }
            Glyph.Check -> path { m(4.5f, 12.5f); l(9.5f, 17.5f); l(19.5f, 7f) }
            Glyph.CheckCircle -> { circle(12f, 12f, 8.5f); path { m(7.8f, 12.4f); l(10.8f, 15.3f); l(16.4f, 9.4f) } }
            Glyph.CircleDashed -> repeat(8) { k -> arc(12f, 12f, 8.5f, k * 45f + 4f, 26f) }
            Glyph.Clock -> { circle(12f, 12f, 9f); path { m(12f, 7f); l(12f, 12f); l(15.5f, 14f) } }
            Glyph.Road -> { line(7.5f, 20.5f, 10.5f, 3.5f); line(16.5f, 20.5f, 13.5f, 3.5f); line(12f, 6f, 12f, 8f); line(12f, 11f, 12f, 13.5f); line(12f, 16.5f, 12f, 19.5f) }
            Glyph.Bridge -> { line(2.5f, 9f, 21.5f, 9f); line(6f, 9f, 6f, 17f); line(18f, 9f, 18f, 17f); path { m(6f, 15f); q(12f, 9.5f, 18f, 15f) }; path { m(2.5f, 20f); q(5f, 18.5f, 7.5f, 20f); q(10f, 21.5f, 12.5f, 20f); q(15f, 18.5f, 17.5f, 20f); q(20f, 21.5f, 21.5f, 20f) } }
            Glyph.School -> { path { m(3f, 10f); l(12f, 4.5f); l(21f, 10f) }; rect(5f, 10f, 14f, 10.5f, 1f); rect(10f, 14.5f, 4f, 6f, 0.5f) }
            Glyph.Health -> { rect(3.5f, 3.5f, 17f, 17f, 4f); line(12f, 8f, 12f, 16f); line(8f, 12f, 16f, 12f) }
            Glyph.Water -> path { m(12f, 3f); q(18.5f, 10.5f, 18.5f, 14.5f); q(18.5f, 21f, 12f, 21f); q(5.5f, 21f, 5.5f, 14.5f); q(5.5f, 10.5f, 12f, 3f); z() }
            Glyph.Flood -> listOf(7f, 12f, 17f).forEach { y -> path { m(3f, y); q(5.25f, y - 2f, 7.5f, y); q(9.75f, y + 2f, 12f, y); q(14.25f, y - 2f, 16.5f, y); q(18.75f, y + 2f, 21f, y) } }
            Glyph.Sprout -> {
                line(12f, 21f, 12f, 10.5f)
                path { m(12f, 14f); q(5.5f, 14f, 5f, 7.5f); q(11.5f, 7.5f, 12f, 14f) }
                path { m(12f, 11.5f); q(12.5f, 4.5f, 19f, 4.5f); q(19f, 11f, 12f, 11.5f) }
            }
            Glyph.Building -> { path { m(3f, 12f); q(12f, 2.5f, 21f, 12f) }; line(5f, 11f, 5f, 20f); line(19f, 11f, 19f, 20f); line(3f, 20f, 21f, 20f); line(12f, 9f, 12f, 20f) }
        }
    }
}
