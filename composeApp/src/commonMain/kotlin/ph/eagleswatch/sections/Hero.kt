package ph.eagleswatch.sections

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ph.eagleswatch.content.trail
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.eagle_logo
import ph.eagleswatch.ui.Chip
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.FlagMark
import ph.eagleswatch.ui.LocalEnv
import ph.eagleswatch.ui.Page
import ph.eagleswatch.ui.Pill
import ph.eagleswatch.ui.Reveal
import ph.eagleswatch.ui.SoftCard
import ph.eagleswatch.ui.anchored
import ph.eagleswatch.ui.softShadow

@Composable
fun Hero() {
    val env = LocalEnv.current
    Page(Modifier.anchored("top")) {
        Spacer(Modifier.height(if (env.wide) 128.dp else 100.dp))
        if (env.wide) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeroCopy(Modifier.weight(1.1f))
                Spacer(Modifier.width(56.dp))
                HeroArt(Modifier.weight(0.9f))
            }
        } else {
            HeroCopy(Modifier.fillMaxWidth())
            Spacer(Modifier.height(48.dp))
            HeroArt(Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(if (env.wide) 104.dp else 72.dp))
    }
}

@Composable
private fun HeroCopy(modifier: Modifier = Modifier) {
    val env = LocalEnv.current
    Reveal(modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlagMark()
                Spacer(Modifier.width(12.dp))
                EText(
                    "Philippine public funds, in the open",
                    size = 14.sp,
                    weight = FontWeight.SemiBold,
                    color = EagleColors.Blue,
                )
            }
            Spacer(Modifier.height(22.dp))
            EText(
                text = buildAnnotatedString {
                    append("Follow every ")
                    withStyle(SpanStyle(color = EagleColors.Blue)) { append("peso.") }
                },
                serif = true,
                size = if (env.wide) 76.sp else 50.sp,
                lineHeight = if (env.wide) 80.sp else 54.sp,
                letterSpacing = (-1.5).sp,
            )
            Spacer(Modifier.height(22.dp))
            EText(
                text = "From your barangay to the highest office, see where citizens' taxes go. " +
                    "Every figure tied to an official public record.",
                modifier = Modifier.widthIn(max = 520.dp),
                size = if (env.wide) 19.sp else 17.sp,
                lineHeight = if (env.wide) 30.sp else 27.sp,
                color = EagleColors.Body,
            )
            Spacer(Modifier.height(10.dp))
            EText("Sundan ang bawat piso.", size = 15.sp, color = EagleColors.Muted)
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Pill("Start tracing", onClick = { env.navigate("explorer") }, primary = true)
                Pill("How it works", onClick = { env.navigate("principles") })
            }
            Spacer(Modifier.height(28.dp))
            Chip(
                text = "Design preview · sample data only",
                background = EagleColors.GoldSoft,
                color = Color(0xFF8A5A00),
            )
        }
    }
}

/** The app icon floating above a card that previews the trail from citizen to president. */
@Composable
private fun HeroArt(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "hero-float")
    val float = transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float",
    )
    Reveal(modifier) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            SoftCard(Modifier.padding(top = 96.dp).widthIn(max = 420.dp).fillMaxWidth(), padding = 28.dp, radius = 28.dp) {
                Spacer(Modifier.height(84.dp))
                EText("Your peso's path", size = 13.sp, weight = FontWeight.SemiBold, color = EagleColors.Muted)
                Spacer(Modifier.height(14.dp))
                MiniTrail()
            }
            Image(
                painter = painterResource(Res.drawable.eagle_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(168.dp)
                    .graphicsLayer { translationY = float.value * 6.dp.toPx() }
                    .softShadow(RoundedCornerShape(38.dp), elevation = 24.dp),
            )
        }
    }
}

@Composable
private fun MiniTrail() {
    val rowHeight = 40.dp
    Column(
        Modifier.drawBehind {
            // connector through the dots
            val x = 6.dp.toPx()
            val half = rowHeight.toPx() / 2f
            drawLine(EagleColors.Line, Offset(x, half), Offset(x, size.height - half), strokeWidth = 2.dp.toPx())
        },
    ) {
        trail.forEach { tier ->
            val dot = when (tier.index) {
                "00" -> EagleColors.Gold
                "05" -> EagleColors.Red
                else -> EagleColors.Blue
            }
            Row(Modifier.height(rowHeight).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(dot))
                Spacer(Modifier.width(14.dp))
                EText(tier.name, size = 16.sp, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                EText(tier.index, size = 13.sp, color = EagleColors.Muted)
            }
        }
    }
}
