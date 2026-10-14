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
import ph.eagleswatch.resources.jetbrains_mono_medium
import ph.eagleswatch.resources.space_grotesk_bold
import ph.eagleswatch.resources.space_grotesk_medium
import ph.eagleswatch.resources.space_grotesk_regular

/** Palette taken from the app icon: deep navy ink, flag gold, flag red and blue as sparing accents. */
object EagleColors {
    val Ink = Color(0xFF050A18)
    val Raised = Color(0xFF0A1226)
    val Line = Color(0x1AFFFFFF)
    val LineStrong = Color(0x33FFFFFF)
    val Text = Color(0xFFE9EDF7)
    val Body = Color(0xFFB4BDD3)
    val Muted = Color(0xFF8590AB)
    val Gold = Color(0xFFF5B921)
    val GoldBright = Color(0xFFFFCC4D)
    val GoldSoft = Color(0x1FF5B921)
    val Red = Color(0xFFE23D57)
    val Blue = Color(0xFF3D6BFF)
}

@Immutable
class EagleFonts(val display: FontFamily, val mono: FontFamily)

val LocalFonts = staticCompositionLocalOf<EagleFonts> { error("EagleFonts not provided") }

@Composable
fun rememberEagleFonts(): EagleFonts {
    val regular = Font(Res.font.space_grotesk_regular, FontWeight.Normal)
    val medium = Font(Res.font.space_grotesk_medium, FontWeight.Medium)
    val bold = Font(Res.font.space_grotesk_bold, FontWeight.Bold)
    val mono = Font(Res.font.jetbrains_mono_medium, FontWeight.Medium)
    return remember(regular, medium, bold, mono) {
        EagleFonts(display = FontFamily(regular, medium, bold), mono = FontFamily(mono))
    }
}

@Composable
fun EText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    size: TextUnit = 16.sp,
    color: Color = EagleColors.Text,
    weight: FontWeight = FontWeight.Normal,
    mono: Boolean = false,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    align: TextAlign = TextAlign.Start,
) {
    val fonts = LocalFonts.current
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = size,
            fontWeight = weight,
            fontFamily = if (mono) fonts.mono else fonts.display,
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
    color: Color = EagleColors.Text,
    weight: FontWeight = FontWeight.Normal,
    mono: Boolean = false,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    align: TextAlign = TextAlign.Start,
) {
    EText(AnnotatedString(text), modifier, size, color, weight, mono, lineHeight, letterSpacing, align)
}
