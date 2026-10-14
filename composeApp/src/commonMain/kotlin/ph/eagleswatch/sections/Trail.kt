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
import ph.eagleswatch.ui.SoftCard
import ph.eagleswatch.ui.anchored

private val SpineX = 15.dp
private val SpineColumn = 44.dp

/** The tax trail: a line that fills with flag blue as the reader scrolls from citizens up to the presidency. */
@Composable
fun TrailSection() {
    val env = LocalEnv.current
    Page {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = if (env.wide) 96.dp else 64.dp)
                .anchored("trail"),
        ) {
            SectionHeader(
                eyebrow = "The trail",
                title = "Every peso leaves a trail.",
                body = "From the moment taxes are paid to the office that spends them, public money passes " +
                    "through a chain of hands. Here is that chain, level by level.",
            )
            Spacer(Modifier.height(if (env.wide) 56.dp else 40.dp))
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
                val stroke = 3.dp.toPx()
                drawLine(EagleColors.Line, Offset(x, 0f), Offset(x, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
                // The blue line follows the reader: it reaches 70% of the way down the viewport.
                val reach = (env.scroll.value + env.viewportPx * 0.7f - top).coerceIn(0f, size.height)
                if (reach > 0f) {
                    drawLine(EagleColors.Blue, Offset(x, 0f), Offset(x, reach), strokeWidth = stroke, cap = StrokeCap.Round)
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
            .padding(bottom = if (isLast) 0.dp else 20.dp)
            .onGloballyPositioned { contentY = env.contentY(it) },
    ) {
        Box(Modifier.width(SpineColumn), contentAlignment = Alignment.TopStart) {
            Node(progress, Modifier.padding(start = SpineX - 11.dp, top = 30.dp))
        }
        SoftCard(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    alpha = 0.55f + 0.45f * progress
                    translationY = (1f - progress) * 14.dp.toPx()
                },
            padding = if (env.wide) 32.dp else 22.dp,
        ) {
            if (env.wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                    Column(Modifier.weight(0.9f)) { StepTitle(tier) }
                    Column(Modifier.weight(1.1f).padding(top = 4.dp)) { StepDetail(tier) }
                }
            } else {
                StepTitle(tier)
                Spacer(Modifier.height(18.dp))
                StepDetail(tier)
            }
        }
    }
}

/** Trail node: an empty ring that fills with flag blue once the reader reaches it. */
@Composable
private fun Node(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(22.dp)
            .drawBehind {
                val r = size.minDimension / 2f
                drawCircle(EagleColors.Blue.copy(alpha = 0.14f * progress), radius = r + 7.dp.toPx() * progress)
                drawCircle(lerp(EagleColors.Card, EagleColors.Blue, progress), radius = r)
                drawCircle(
                    color = lerp(EagleColors.Line, EagleColors.Blue, progress),
                    radius = r - 1.5.dp.toPx(),
                    style = Stroke(width = 3.dp.toPx()),
                )
                drawCircle(EagleColors.Card.copy(alpha = progress), radius = 4.dp.toPx())
            },
    )
}

@Composable
private fun StepTitle(tier: Tier) {
    val env = LocalEnv.current
    EText("Level ${tier.index}", size = 13.sp, weight = FontWeight.SemiBold, color = EagleColors.Blue)
    Spacer(Modifier.height(8.dp))
    EText(
        text = tier.name,
        serif = true,
        size = if (env.wide) 32.sp else 26.sp,
        lineHeight = if (env.wide) 38.sp else 32.sp,
    )
    Spacer(Modifier.height(10.dp))
    EText(tier.tagline, size = 16.sp, lineHeight = 25.sp, color = EagleColors.Body)
}

@Composable
private fun StepDetail(tier: Tier) {
    EText("You will be able to see", size = 13.sp, weight = FontWeight.SemiBold, color = EagleColors.Muted)
    Spacer(Modifier.height(12.dp))
    tier.reveals.forEach { item ->
        Row(Modifier.padding(bottom = 10.dp)) {
            Box(Modifier.padding(top = 8.dp).size(6.dp).clip(CircleShape).background(EagleColors.Blue))
            Spacer(Modifier.width(12.dp))
            EText(item, size = 15.sp, lineHeight = 23.sp, color = EagleColors.Ink)
        }
    }
    Spacer(Modifier.height(4.dp))
    EText("Sources: ${tier.sources}", size = 13.sp, lineHeight = 19.sp, color = EagleColors.Muted)
}
