package ph.eagleswatch.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Centres content in a readable column and applies the responsive page gutter. */
@Composable
fun Page(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val env = LocalEnv.current
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = modifier
                .widthIn(max = 1120.dp)
                .fillMaxWidth()
                .padding(horizontal = env.gutter),
            content = content,
        )
    }
}

/** Fades and lifts its content in once its top edge scrolls into view. */
@Composable
fun Reveal(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val env = LocalEnv.current
    var contentY by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    val shown by remember(env) {
        derivedStateOf { env.scroll.value + env.viewportPx * 0.92f >= contentY }
    }
    val progress by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "reveal",
    )
    Box(
        modifier = modifier
            .onGloballyPositioned { contentY = env.contentY(it) }
            .graphicsLayer {
                alpha = progress
                translationY = (1f - progress) * 24.dp.toPx()
            },
        content = content,
    )
}

/** Soft, warm drop shadow used by every raised surface. */
fun Modifier.softShadow(shape: Shape, elevation: Dp = 16.dp): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    clip = false,
    ambientColor = EagleColors.Shadow.copy(alpha = 0.18f),
    spotColor = EagleColors.Shadow.copy(alpha = 0.22f),
)

/** White card on the paper background. */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    padding: Dp = 24.dp,
    radius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .softShadow(shape)
            .clip(shape)
            .background(EagleColors.Card)
            .padding(padding),
        content = content,
    )
}

/** Inflated pill button. [primary] is flag blue with a gloss gradient; otherwise a white pill. */
@Composable
fun Pill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    compact: Boolean = false,
) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val top by animateColorAsState(
        targetValue = when {
            primary && hovered -> Color(0xFF4B7BF2)
            primary -> EagleColors.BlueBright
            hovered -> EagleColors.BlueSoft
            else -> EagleColors.Card
        },
        animationSpec = tween(160),
        label = "pill-top",
    )
    val bottom = if (primary) EagleColors.Blue else EagleColors.Card
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .softShadow(shape, elevation = if (compact) 8.dp else 12.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .hoverable(source)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (compact) 18.dp else 26.dp, vertical = if (compact) 10.dp else 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        EText(
            text = label,
            size = if (compact) 14.sp else 16.sp,
            weight = FontWeight.SemiBold,
            color = if (primary) Color.White else EagleColors.Ink,
        )
    }
}

/** Small rounded label, e.g. a data source name. */
@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = EagleColors.BlueSoft,
    color: Color = EagleColors.Blue,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        EText(text, size = 13.sp, weight = FontWeight.SemiBold, color = color)
    }
}

/** Three short bars in the flag's colours, used as a quiet section marker. */
@Composable
fun FlagMark(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        listOf(EagleColors.Blue, EagleColors.Red, EagleColors.Gold).forEach { color ->
            Box(Modifier.size(width = 14.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(color))
        }
    }
}

/** Eyebrow + serif title + body block that opens each section. */
@Composable
fun SectionHeader(eyebrow: String, title: String, body: String, modifier: Modifier = Modifier) {
    val env = LocalEnv.current
    Reveal(modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlagMark()
                Spacer(Modifier.width(12.dp))
                EText(eyebrow, size = 14.sp, weight = FontWeight.SemiBold, color = EagleColors.Blue)
            }
            Spacer(Modifier.height(16.dp))
            EText(
                text = title,
                serif = true,
                size = if (env.wide) 46.sp else 34.sp,
                lineHeight = if (env.wide) 52.sp else 40.sp,
                letterSpacing = (-0.5).sp,
            )
            Spacer(Modifier.height(14.dp))
            EText(
                text = body,
                modifier = Modifier.widthIn(max = 580.dp),
                size = 17.sp,
                lineHeight = 27.sp,
                color = EagleColors.Body,
            )
        }
    }
}
