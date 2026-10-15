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
 * A soft grey canvas with white cards, near-black ink, and one confident blue for actions. Headers that carry
 * the big numbers sit on a deep navy taken from the flag. Status colours always travel with a shape and a
 * label, never alone. Every text colour reads at 4.5:1 or better on the surface it is used on.
 */
object EagleColors {
    /** Behind everything. Cards sit on it. */
    val Canvas = Color(0xFFF2F4F7)

    /** Cards, sheets, the bottom bar. */
    val Page = Color(0xFFFFFFFF)

    /** Fills inside a card: tracks, idle chips, icon tiles, skeletons. */
    val Surface = Color(0xFFF2F4F7)
    val SurfaceStrong = Color(0xFFE4E7EC)

    val Line = Color(0xFFE4E7EC)
    val LineStrong = Color(0xFFCDD3DD)

    val Ink = Color(0xFF0B1220)
    val Body = Color(0xFF344054)
    val Muted = Color(0xFF5B6577)

    val Blue = Color(0xFF1D4ED8)
    val BlueDeep = Color(0xFF1E40AF)
    val BlueSoft = Color(0xFFE8EEFF)

    /** Header bands that hold the headline figures. */
    val Navy = Color(0xFF0A1A3A)
    val NavyRaised = Color(0xFF16284D)
    val OnNavy = Color(0xFFFFFFFF)
    val OnNavyMuted = Color(0xFFB4BFD4)

    /** The eagle's gold, for the brand mark only. */
    val Gold = Color(0xFFF5B700)

    val Green = Color(0xFF067647)
    val GreenSoft = Color(0xFFE5F6EC)
    val Amber = Color(0xFFB54708)
    val AmberSoft = Color(0xFFFEF0E1)
    val Red = Color(0xFFB42318)
    val RedSoft = Color(0xFFFDECEA)

    /** Bar and dot fills for each status: stronger than the text colours, readable on navy and on white. */
    val FillBlue = Color(0xFF3B6EF5)
    val FillAmber = Color(0xFFF59E0B)
    val FillRed = Color(0xFFEF4444)
    val FillGreen = Color(0xFF16A34A)
    val FillGrey = Color(0xFF98A2B3)

    val Scrim = Color(0x8C0B1220)
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
