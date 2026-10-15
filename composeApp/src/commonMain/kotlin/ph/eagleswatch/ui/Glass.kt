package ph.eagleswatch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

/*
 * Glass: frosted surfaces that let the page show through, blurred.
 *
 * Real backdrop blur (Haze) is used where content moves underneath: the floating tab bar, the page bar on a
 * project, the status-bar strip and the source sheet. On Android before 12, where the platform cannot blur,
 * Haze draws the fallback tint instead, so those surfaces stay opaque and readable.
 *
 * On the navy header bands, which do not move, glass is a translucent white fill with a light top edge. A
 * blurred gradient looks the same as the gradient, so no blur is needed there.
 */

/** Blur styles, one per kind of surface. */
object GlassStyles {
    /** The floating tab bar and other light glass over the canvas. */
    val Light = HazeStyle(
        backgroundColor = EagleColors.Canvas,
        tint = HazeTint(Color.White.copy(alpha = 0.6f)),
        blurRadius = 24.dp,
        noiseFactor = 0.04f,
        fallbackTint = HazeTint(Color.White.copy(alpha = 0.96f)),
    )

    /** Bars that sit on the navy bands: the status-bar strip and the page bar. */
    val Navy = HazeStyle(
        backgroundColor = EagleColors.Navy,
        tint = HazeTint(EagleColors.Navy.copy(alpha = 0.74f)),
        blurRadius = 24.dp,
        noiseFactor = 0.03f,
        fallbackTint = HazeTint(EagleColors.Navy.copy(alpha = 0.97f)),
    )

    /** The source sheet. */
    val Sheet = HazeStyle(
        backgroundColor = EagleColors.Page,
        tint = HazeTint(Color.White.copy(alpha = 0.8f)),
        blurRadius = 32.dp,
        noiseFactor = 0.03f,
        fallbackTint = HazeTint(Color.White.copy(alpha = 0.98f)),
    )
}

/** The app's blur source: every tab and page draws into it, and glass surfaces read from it. */
val LocalHaze = staticCompositionLocalOf<HazeState?> { null }

/** Space to leave at the bottom of scrolling content so the floating tab bar never covers it. */
val LocalBottomSpace = staticCompositionLocalOf { 0.dp }

/** Lets a screen ask for the floating tab bar to step aside, e.g. while a full-screen sheet is open. */
@Stable
class ChromeState {
    var tabBarHidden: Boolean by mutableStateOf(false)
}

val LocalChrome = staticCompositionLocalOf { ChromeState() }

/**
 * A translucent glass panel for the navy bands: a white fill at [fill] opacity and an edge that is brighter
 * at the top, the way light catches the rim of a pane.
 */
fun Modifier.glass(shape: Shape, fill: Float = 0.08f, edge: Float = 0.18f): Modifier = this
    .clip(shape)
    .background(Color.White.copy(alpha = fill))
    .border(1.dp, glassEdge(edge), shape)

/** The rim of a glass pane: bright at the top, fading towards the bottom. */
fun glassEdge(strength: Float = 0.18f): Brush =
    Brush.verticalGradient(listOf(Color.White.copy(alpha = (strength * 1.8f).coerceAtMost(1f)), Color.White.copy(alpha = strength * 0.35f)))

/**
 * The navy band's background: the flag's deep blue, lifted by a soft blue light from the upper right and a
 * faint warmth from the lower left. It gives the glass something to sit on without drawing attention.
 */
fun Modifier.aurora(): Modifier = drawBehind {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF081633), Color(0xFF0E2550))))
    val w = size.width
    val h = size.height
    val r1 = maxOf(w, h) * 0.75f
    val c1 = Offset(w * 0.92f, h * 0.05f)
    drawCircle(Brush.radialGradient(listOf(Color(0x663B6EF5), Color.Transparent), center = c1, radius = r1), radius = r1, center = c1)
    val r2 = maxOf(w, h) * 0.55f
    val c2 = Offset(w * 0.05f, h * 1.0f)
    drawCircle(Brush.radialGradient(listOf(Color(0x1FF5B700), Color.Transparent), center = c2, radius = r2), radius = r2, center = c2)
}

/** The height of the floating tab bar itself, without its margins. */
val TabBarHeight: Dp = 64.dp
