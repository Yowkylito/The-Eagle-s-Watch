package ph.eagleswatch.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.barlow_condensed_bold
import ph.eagleswatch.resources.barlow_condensed_semibold
import ph.eagleswatch.resources.inter_regular
import ph.eagleswatch.resources.inter_semibold

/**
 * White pages and photo-first cards, with the flag's blue as the one action colour.
 * Gold marks citizen approval and the eagle's watch; red appears only on the "Sample data" badge.
 */
object EagleColors {
    /** Flag blue: buttons, links, the selected tab. */
    val Blue = Color(0xFF1440C2)
    val BlueDeep = Color(0xFF0E2F96)
    val BlueSoft = Color(0xFFE4E9FB)

    val Page = Color(0xFFFFFFFF)

    /** Quiet panels and placeholders on the white page. */
    val Surface = Color(0xFFF4F5F9)
    val Card = Color(0xFFFFFFFF)
    val Ink = Color(0xFF0F1938)
    val Body = Color(0xFF454E6A)
    val Muted = Color(0xFF626A87)
    val Line = Color(0xFFDDE1EE)

    val Red = Color(0xFFC8243F)
    val RedSoft = Color(0xFFFBE7EA)
    val Gold = Color(0xFFF4B41A)
    val GoldDeep = Color(0xFFB77D00)
    val GoldSoft = Color(0xFFFFF3D3)

    /** Project status colours: text on its soft background. */
    val Green = Color(0xFF13795F)
    val GreenSoft = Color(0xFFE0F3EC)
    val Amber = Color(0xFF9A4B00)
    val AmberSoft = Color(0xFFFDEBD3)
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
 * Text in the house fonts: Barlow Condensed for [display] headlines and labels that need to be narrow,
 * Inter for everything else. Display text defaults to bold, body text to regular.
 */
@Composable
fun EText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    size: TextUnit = 16.sp,
    color: Color = EagleColors.Ink,
    weight: FontWeight? = null,
    display: Boolean = false,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    decoration: TextDecoration? = null,
    ellipsis: Boolean = false,
) {
    val fonts = LocalFonts.current
    BasicText(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        overflow = if (ellipsis) TextOverflow.Ellipsis else TextOverflow.Clip,
        style = TextStyle(
            color = color,
            fontSize = size,
            fontWeight = weight ?: if (display) FontWeight.Bold else FontWeight.Normal,
            fontFamily = if (display) fonts.display else fonts.sans,
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
    size: TextUnit = 16.sp,
    color: Color = EagleColors.Ink,
    weight: FontWeight? = null,
    display: Boolean = false,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    decoration: TextDecoration? = null,
    ellipsis: Boolean = false,
) {
    EText(AnnotatedString(text), modifier, size, color, weight, display, lineHeight, letterSpacing, align, maxLines, decoration, ellipsis)
}
