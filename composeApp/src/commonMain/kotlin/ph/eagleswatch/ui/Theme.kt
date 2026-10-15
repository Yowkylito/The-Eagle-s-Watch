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
import ph.eagleswatch.resources.barlow_condensed_bold
import ph.eagleswatch.resources.barlow_condensed_semibold
import ph.eagleswatch.resources.inter_regular
import ph.eagleswatch.resources.inter_semibold

/**
 * Colour tokens. White pages, hairline separators, ink text. The flag's blue is the one action colour:
 * links, primary buttons, the selected tab. Status colours always travel with a glyph and a label, never
 * alone. Gold appears only in the brand mark.
 *
 * Every text colour here reads at 4.5:1 or better on [Page] and on its paired soft background.
 */
object EagleColors {
    val Page = Color(0xFFFFFFFF)

    /** Quiet panels: notices, skeletons, the pressed state of a row. */
    val Surface = Color(0xFFF3F4F8)
    val SurfaceStrong = Color(0xFFE7E9F0)

    /** Hairlines between rows, and the stronger line around inputs. */
    val Line = Color(0xFFD9DDE6)
    val LineStrong = Color(0xFFB2B9C9)

    val Ink = Color(0xFF111A33)
    val Body = Color(0xFF3C4560)
    val Muted = Color(0xFF5D667F)

    val Blue = Color(0xFF1440C2)
    val BlueDeep = Color(0xFF0E2F96)
    val BlueSoft = Color(0xFFE6EBFA)

    val Gold = Color(0xFFF4B41A)

    val Red = Color(0xFFB4213A)
    val RedSoft = Color(0xFFFBE9EC)
    val Green = Color(0xFF14704F)
    val GreenSoft = Color(0xFFE1F2EA)
    val Amber = Color(0xFF8A4300)
    val AmberSoft = Color(0xFFFCEBD6)

    /** Behind sheets and dialogs. */
    val Scrim = Color(0x73000000)
}

/** The type scale. Sizes are in sp; nothing a citizen reads is smaller than [Caption]. */
object EagleType {
    /** Page titles, in Barlow Condensed. */
    val Display: TextUnit = 26.sp

    /** Large figures such as a contract amount, in Barlow Condensed. */
    val Figure: TextUnit = 22.sp

    /** Row and section titles. */
    val Title: TextUnit = 17.sp
    val Body: TextUnit = 15.sp
    val Secondary: TextUnit = 14.sp

    /** Section labels and tags. */
    val Label: TextUnit = 13.sp
    val Caption: TextUnit = 12.sp
}

/** Spacing and shape tokens on a 4dp grid. */
object EagleMetrics {
    val Radius: Dp = 4.dp
    val RadiusSmall: Dp = 3.dp

    /** Minimum size of anything tappable. */
    val Touch: Dp = 44.dp

    /** Horizontal page padding on phones; wide windows use [Layout.gutter]. */
    val PhoneGutter: Dp = 16.dp
    val WideGutter: Dp = 32.dp

    /** The widest a column of text or a list is allowed to be. */
    val ContentMax: Dp = 720.dp
    val ReadingMax: Dp = 600.dp
}

@Immutable
class EagleFonts(val display: FontFamily, val sans: FontFamily)

val LocalFonts = staticCompositionLocalOf<EagleFonts> { error("EagleFonts not provided") }

@Composable
fun rememberEagleFonts(): EagleFonts {
    val displayBold = Font(Res.font.barlow_condensed_bold, FontWeight.Bold)
    val displaySemibold = Font(Res.font.barlow_condensed_semibold, FontWeight.SemiBold)
    val regular = Font(Res.font.inter_regular, FontWeight.Normal)
    val semibold = Font(Res.font.inter_semibold, FontWeight.SemiBold)
    return remember(displayBold, displaySemibold, regular, semibold) {
        EagleFonts(
            display = FontFamily(displayBold, displaySemibold),
            sans = FontFamily(regular, semibold),
        )
    }
}

/**
 * Text in the house fonts: Barlow Condensed for [display] titles and figures, Inter for everything else.
 * [tabular] switches on tabular numerals so columns of figures line up. [heading] marks the text as a
 * heading for screen readers.
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
            letterSpacing = letterSpacing,
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
