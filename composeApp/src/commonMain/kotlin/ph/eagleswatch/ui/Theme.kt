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
import ph.eagleswatch.resources.fraunces_semibold
import ph.eagleswatch.resources.inter_regular
import ph.eagleswatch.resources.inter_semibold

/**
 * Warm paper and ink, with the flag's blue as the working accent.
 * Red and gold appear only in small, meaningful doses (badges, markers).
 */
object EagleColors {
    val Paper = Color(0xFFF5EEE3)
    val PaperDeep = Color(0xFFECE2D2)
    val Card = Color(0xFFFFFFFF)
    val Ink = Color(0xFF14213D)
    val Body = Color(0xFF4B5466)
    val Muted = Color(0xFF8B8F99)
    val Line = Color(0xFFE3D8C6)
    val Blue = Color(0xFF1440C2)
    val BlueBright = Color(0xFF2B5FE6)
    val BlueDeep = Color(0xFF0A2788)
    val BlueSoft = Color(0xFFE6ECFB)
    val Red = Color(0xFFC8243F)
    val RedSoft = Color(0xFFFBE7EA)
    val Gold = Color(0xFFE3A21A)
    val GoldSoft = Color(0xFFFCF1D8)

    /** Warm brown tint for soft shadows, so cards sit on the paper instead of floating in grey. */
    val Shadow = Color(0xFF6B4A2B)
}

@Immutable
class EagleFonts(val serif: FontFamily, val sans: FontFamily)

val LocalFonts = staticCompositionLocalOf<EagleFonts> { error("EagleFonts not provided") }

@Composable
fun rememberEagleFonts(): EagleFonts {
    val serif = Font(Res.font.fraunces_semibold, FontWeight.SemiBold)
    val regular = Font(Res.font.inter_regular, FontWeight.Normal)
    val semibold = Font(Res.font.inter_semibold, FontWeight.SemiBold)
    return remember(serif, regular, semibold) {
        EagleFonts(serif = FontFamily(serif), sans = FontFamily(regular, semibold))
    }
}

/** Text in the house fonts: Fraunces for [serif] headlines, Inter for everything else. */
@Composable
fun EText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    size: TextUnit = 16.sp,
    color: Color = EagleColors.Ink,
    weight: FontWeight = FontWeight.Normal,
    serif: Boolean = false,
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
            fontWeight = if (serif) FontWeight.SemiBold else weight,
            fontFamily = if (serif) fonts.serif else fonts.sans,
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
    weight: FontWeight = FontWeight.Normal,
    serif: Boolean = false,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    align: TextAlign = TextAlign.Start,
) {
    EText(AnnotatedString(text), modifier, size, color, weight, serif, lineHeight, letterSpacing, align)
}
