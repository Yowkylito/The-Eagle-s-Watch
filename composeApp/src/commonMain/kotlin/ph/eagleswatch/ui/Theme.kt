package ph.eagleswatch.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.inter_medium
import ph.eagleswatch.resources.inter_regular
import ph.eagleswatch.resources.inter_semibold
import ph.eagleswatch.resources.inter_tight_bold
import ph.eagleswatch.resources.inter_tight_semibold

/**
 * Colour tokens.
 *
 * Warm paper and warm ink, with one signature colour: jade, after the Philippine jade vine. Jade marks
 * everything you can act on (buttons, links, the selected tab, focus) and nothing else. Header bands sit on a
 * deep night jade, the darker side of the app icon's jade tile, lit by a coral sun.
 *
 * Status colours are kept clear of jade: blue for ongoing work, amber for delays, red for suspensions and a
 * leaf green for completed work. They always travel with a shape and a label, never alone. Every text colour
 * reads at 4.5:1 or better on the surface it is used on.
 */
object EagleColors {
    /** Behind everything. Cards sit on it. */
    val Canvas = Color(0xFFF4F0E8)

    /** Cards, sheets, the bottom bar. */
    val Page = Color(0xFFFFFDF9)

    /** Fills inside a card: tracks, idle chips, icon tiles, skeletons. */
    val Surface = Color(0xFFF3EEE6)
    val SurfaceStrong = Color(0xFFE6DFD3)

    val Line = Color(0xFFEAE4DA)
    val LineStrong = Color(0xFFD5CCBE)

    val Ink = Color(0xFF1C1A16)
    val Body = Color(0xFF46413A)
    val Muted = Color(0xFF6B655C)

    /** Actions: buttons, links, the selected tab, focus rings. */
    val Accent = Color(0xFF0B7368)
    val AccentDeep = Color(0xFF09625A)
    val AccentSoft = Color(0xFFDFF3EE)

    /** Selected controls on the dark bands, with [Night] content on top. */
    val AccentBright = Color(0xFF3DD3BF)

    /** Header bands that hold the headline figures. */
    val Night = Color(0xFF0C2B2B)
    val OnNight = Color(0xFFFFFFFF)
    val OnNightMuted = Color(0xFFB3CCC7)

    /** A coral sun: the warm light on the dark bands and the sample-data tag. Never a status. */
    val Sun = Color(0xFFFF7A59)

    /** The flag's gold, for the flag mark only. */
    val Gold = Color(0xFFF5B700)

    /** Ongoing work and progress figures. */
    val Blue = Color(0xFF1D4ED8)
    val BlueSoft = Color(0xFFE8EEFF)

    val Green = Color(0xFF2B7A2B)
    val GreenSoft = Color(0xFFE5F2E1)
    val Amber = Color(0xFFB54708)
    val AmberSoft = Color(0xFFFEF0E1)
    val Red = Color(0xFFB42318)
    val RedSoft = Color(0xFFFDECEA)

    /** Bar and dot fills for each status: stronger than the text colours, readable on night and on paper. */
    val FillBlue = Color(0xFF3B6EF5)
    val FillAmber = Color(0xFFF59E0B)
    val FillRed = Color(0xFFEF4444)
    val FillGreen = Color(0xFF4CAF50)
    val FillGrey = Color(0xFFA39D93)

    val Scrim = Color(0x8C1C1A16)
}

/** The type scale, in sp. Nothing a citizen reads is smaller than [Caption]. */
object EagleType {
    /** The headline figure on a header band. */
    val Hero: TextUnit = 40.sp

    /** Page titles. */
    val Display: TextUnit = 28.sp

    /** Figures in a stat strip. */
    val Figure: TextUnit = 20.sp

    /** Card titles. */
    val Title: TextUnit = 17.sp
    val Body: TextUnit = 15.sp
    val Secondary: TextUnit = 14.sp
    val Label: TextUnit = 13.sp
    val Caption: TextUnit = 12.sp
}

/** Spacing and shape tokens on a 4dp grid. */
object EagleMetrics {
    val RadiusCard: Dp = 22.dp
    val Radius: Dp = 14.dp
    val RadiusChip: Dp = 12.dp
    val RadiusSmall: Dp = 6.dp

    /** Minimum size of anything tappable. */
    val Touch: Dp = 48.dp

    val PhoneGutter: Dp = 16.dp
    val WideGutter: Dp = 32.dp

    /** Gap between cards in a list. */
    val CardGap: Dp = 12.dp

    /** The widest a column of cards is allowed to be. */
    val ContentMax: Dp = 720.dp
    val ReadingMax: Dp = 640.dp
}

@Immutable
class EagleFonts(val display: FontFamily, val sans: FontFamily)

val LocalFonts = staticCompositionLocalOf<EagleFonts> { error("EagleFonts not provided") }

@Composable
fun rememberEagleFonts(): EagleFonts {
    val tightSemibold = Font(Res.font.inter_tight_semibold, FontWeight.SemiBold)
    val tightBold = Font(Res.font.inter_tight_bold, FontWeight.Bold)
    val regular = Font(Res.font.inter_regular, FontWeight.Normal)
    val medium = Font(Res.font.inter_medium, FontWeight.Medium)
    val semibold = Font(Res.font.inter_semibold, FontWeight.SemiBold)
    return remember(tightSemibold, tightBold, regular, medium, semibold) {
        EagleFonts(
            display = FontFamily(tightSemibold, tightBold),
            sans = FontFamily(regular, medium, semibold),
        )
    }
}

/**
 * Text in the house fonts: Inter Tight for [display] titles and figures, Inter for everything else.
 * [tabular] switches on tabular numerals so figures line up. [heading] marks the text as a heading for
 * screen readers. Display text is tracked slightly tighter, the way large type should be.
 */
@Composable
fun EText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    size: TextUnit = EagleType.Body,
    color: Color = EagleColors.Ink,
    weight: FontWeight? = null,
    display: Boolean = false,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    decoration: TextDecoration? = null,
    ellipsis: Boolean = false,
    tabular: Boolean = false,
    heading: Boolean = false,
) {
    val fonts = LocalFonts.current
    val tracking = when {
        letterSpacing != TextUnit.Unspecified -> letterSpacing
        display && size.value >= 24f -> (-0.6).sp
        display -> (-0.2).sp
        else -> TextUnit.Unspecified
    }
    BasicText(
        text = text,
        modifier = if (heading) modifier.semantics { heading() } else modifier,
        maxLines = maxLines,
        overflow = if (ellipsis) TextOverflow.Ellipsis else TextOverflow.Clip,
        style = TextStyle(
            color = color,
            fontSize = size,
            fontWeight = weight ?: if (display) FontWeight.Bold else FontWeight.Normal,
            fontFamily = if (display) fonts.display else fonts.sans,
            fontFeatureSettings = if (tabular) "tnum" else null,
            lineHeight = lineHeight,
            letterSpacing = tracking,
            textAlign = align,
            textDecoration = decoration,
        ),
    )
}

@Composable
fun EText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = EagleType.Body,
    color: Color = EagleColors.Ink,
    weight: FontWeight? = null,
    display: Boolean = false,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    decoration: TextDecoration? = null,
    ellipsis: Boolean = false,
    tabular: Boolean = false,
    heading: Boolean = false,
) {
    EText(
        AnnotatedString(text), modifier, size, color, weight, display, lineHeight, letterSpacing, align, maxLines,
        decoration, ellipsis, tabular, heading,
    )
}
