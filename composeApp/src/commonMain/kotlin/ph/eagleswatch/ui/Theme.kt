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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.barlow_condensed_bold
import ph.eagleswatch.resources.barlow_condensed_semibold
import ph.eagleswatch.resources.inter_regular
import ph.eagleswatch.resources.inter_semibold

/**
 * The flag's blue is the stage; a gold peso coin is the one bright thing on it.
 * Red appears only on the "Sample data" badge and the presidency, so it keeps its meaning.
 */
object EagleColors {
    /** Flag blue: the top bar and the stage the coin travels on. */
    val Stage = Color(0xFF1440C2)

    /** Unlit rail and quiet text on the stage. */
    val StageLift = Color(0xFF5D7FE3)

    /** Cool paper behind everything that is not the stage. */
    val Sheet = Color(0xFFF6F7FB)
    val Card = Color(0xFFFFFFFF)
    val Ink = Color(0xFF0F1938)
    val Body = Color(0xFF454E6A)
    val Muted = Color(0xFF626A87)
    val Line = Color(0xFFDDE1EE)

    /** Blue for text and bars on light surfaces. */
    val Blue = Color(0xFF1440C2)
    val BlueSoft = Color(0xFFE4E9FB)
    val Red = Color(0xFFC8243F)
    val RedSoft = Color(0xFFFBE7EA)
    val Gold = Color(0xFFF4B41A)
    val GoldDeep = Color(0xFFB77D00)
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
) {
    val fonts = LocalFonts.current
    BasicText(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        style = TextStyle(
            color = color,
            fontSize = size,
            fontWeight = weight ?: if (display) FontWeight.Bold else FontWeight.Normal,
            fontFamily = if (display) fonts.display else fonts.sans,
            lineHeight = lineHeight,
            letterSpacing = letterSpacing,
            textAlign = align,
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
) {
    EText(AnnotatedString(text), modifier, size, color, weight, display, lineHeight, letterSpacing, align, maxLines)
}
