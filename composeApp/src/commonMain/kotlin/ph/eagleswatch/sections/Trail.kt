package ph.eagleswatch.sections

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Tier
import ph.eagleswatch.content.trail
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalEnv
import ph.eagleswatch.ui.Page
import ph.eagleswatch.ui.SectionHeader
import ph.eagleswatch.ui.anchored

private val SpineX = 20.dp
private val SpineColumn = 56.dp

/** The tax trail: a vertical spine that fills with gold as the reader scrolls from citizens up to the presidency. */
@Composable
fun TrailSection() {
    val env = LocalEnv.current
    Page {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = if (env.wide) 112.dp else 72.dp)
                .anchored("trail"),
        ) {
            SectionHeader(
                eyebrow = "THE TRAIL",
                title = "Every peso leaves a trail.",
                body = "From the moment taxes are paid to the office that spends them, public money passes " +
                    "through a chain of hands. We lay that chain out level by level.",
            )
            Spacer(Modifier.height(if (env.wide) 72.dp else 48.dp))
            TrailSteps()
        }
    }
}

@Composable
private fun TrailSteps() {
    val env = LocalEnv.current
    var top by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    Column(
        Modifier
            .fillMaxWidth()
            .onGloballyPositioned { top = env.contentY(it) }
            .drawBehind {
                val x = SpineX.toPx()
                val stroke = 2.dp.toPx()
                drawLine(EagleColors.Line, Offset(x, 0f), Offset(x, size.height), strokeWidth = stroke)
                // The gold line follows the reader: it reaches 70% of the way down the viewport.
                val reach = (env.scroll.value + env.viewportPx * 0.7f - top).coerceIn(0f, size.height)
                if (reach > 0f) {
                    drawLine(EagleColors.Gold, Offset(x, 0f), Offset(x, reach), strokeWidth = stroke, cap = StrokeCap.Round)
                    drawCircle(EagleColors.Gold.copy(alpha = 0.22f), radius = 10.dp.toPx(), center = Offset(x, reach))
                }
            },
    ) {
        trail.forEachIndexed { index, tier ->
            TrailStep(tier, isLast = index == trail.lastIndex)
        }
    }
}

@Composable
private fun TrailStep(tier: Tier, isLast: Boolean) {
    val env = LocalEnv.current
    var contentY by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    val lit by remember(env) {
        derivedStateOf { env.scroll.value + env.viewportPx * 0.7f >= contentY }
    }
    val progress by animateFloatAsState(
        targetValue = if (lit) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "trail-step",
    )

    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else if (env.wide) 72.dp else 52.dp)
            .onGloballyPositioned { contentY = env.contentY(it) },
    ) {
        Box(Modifier.width(SpineColumn), contentAlignment = Alignment.TopStart) {
            Node(progress, Modifier.padding(start = SpineX - 7.dp, top = 12.dp))
        }
        val content = Modifier
            .weight(1f)
            .graphicsLayer {
                alpha = 0.3f + 0.7f * progress
                translationY = (1f - progress) * 18.dp.toPx()
            }
        if (env.wide) {
            Row(content, horizontalArrangement = Arrangement.spacedBy(48.dp)) {
                Column(Modifier.weight(0.9f)) { StepTitle(tier, progress) }
                Column(Modifier.weight(1.1f).padding(top = 6.dp)) { StepDetail(tier) }
            }
        } else {
            Column(content) {
                StepTitle(tier, progress)
                Spacer(Modifier.height(20.dp))
                StepDetail(tier)
            }
        }
    }
}

/** Spine node: a hollow ring that ignites gold once the trail reaches it. */
@Composable
private fun Node(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(14.dp)
            .drawBehind {
                val r = size.minDimension / 2f
                drawCircle(EagleColors.Gold.copy(alpha = 0.22f * progress), radius = r + 9.dp.toPx() * progress)
                drawCircle(EagleColors.Ink, radius = r)
                drawCircle(
                    color = lerp(EagleColors.LineStrong, EagleColors.Gold, progress),
                    radius = r - 1.dp.toPx(),
                    style = Stroke(width = 2.dp.toPx()),
                )
                drawCircle(EagleColors.Gold.copy(alpha = progress), radius = r - 4.dp.toPx())
            },
    )
}

@Composable
private fun StepTitle(tier: Tier, progress: Float) {
    val env = LocalEnv.current
    EText(
        text = tier.index,
        size = 12.sp,
        mono = true,
        color = lerp(EagleColors.Muted, EagleColors.Gold, progress),
        letterSpacing = 2.sp,
    )
    Spacer(Modifier.height(10.dp))
    EText(
        text = tier.name,
        size = if (env.wide) 36.sp else 28.sp,
        lineHeight = if (env.wide) 40.sp else 32.sp,
        weight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    )
    Spacer(Modifier.height(12.dp))
    EText(tier.tagline, size = 16.sp, lineHeight = 25.sp, color = EagleColors.Body)
}

@Composable
private fun StepDetail(tier: Tier) {
    EText("YOU WILL BE ABLE TO SEE", size = 10.sp, mono = true, color = EagleColors.Muted, letterSpacing = 1.8.sp)
    Spacer(Modifier.height(14.dp))
    tier.reveals.forEach { item ->
        Row(Modifier.padding(bottom = 10.dp)) {
            Box(Modifier.padding(top = 9.dp).size(4.dp).clip(CircleShape).background(EagleColors.Gold))
            Spacer(Modifier.width(12.dp))
            EText(item, size = 15.sp, lineHeight = 23.sp, color = EagleColors.Text)
        }
    }
    Spacer(Modifier.height(6.dp))
    EText("SOURCES · ${tier.sources}", size = 10.sp, mono = true, color = EagleColors.Muted, letterSpacing = 1.2.sp, lineHeight = 16.sp)
}
