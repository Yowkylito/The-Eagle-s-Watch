package ph.eagleswatch.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Centres content in a readable column and applies the responsive page gutter. */
@Composable
fun Page(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val env = LocalEnv.current
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = modifier
                .widthIn(max = 1160.dp)
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
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "reveal",
    )
    Box(
        modifier = modifier
            .onGloballyPositioned { contentY = env.contentY(it) }
            .graphicsLayer {
                alpha = progress
                translationY = (1f - progress) * 28.dp.toPx()
            },
        content = content,
    )
}

/** Pill-shaped button. [primary] is the solid gold call to action. */
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
    val background by animateColorAsState(
        targetValue = when {
            primary && hovered -> EagleColors.GoldBright
            primary -> EagleColors.Gold
            hovered -> EagleColors.LineStrong
            else -> Color.Transparent
        },
        animationSpec = tween(160),
        label = "pill-background",
    )
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .then(if (primary) Modifier else Modifier.border(1.dp, EagleColors.LineStrong, shape))
            .hoverable(source)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (compact) 16.dp else 24.dp, vertical = if (compact) 9.dp else 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        EText(
            text = label,
            size = if (compact) 13.sp else 15.sp,
            weight = FontWeight.Medium,
            color = if (primary) EagleColors.Ink else EagleColors.Text,
            letterSpacing = 0.2.sp,
        )
    }
}

/** Small monospaced HUD label. */
@Composable
fun Tag(text: String, modifier: Modifier = Modifier, accent: Color = EagleColors.Muted) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(EagleColors.Ink.copy(alpha = 0.82f))
            .border(1.dp, EagleColors.Line, shape)
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        EText(text = text, size = 9.5.sp, mono = true, color = accent, letterSpacing = 1.2.sp)
    }
}

/** Eyebrow + title + body block that opens each section. */
@Composable
fun SectionHeader(eyebrow: String, title: String, body: String, modifier: Modifier = Modifier) {
    val env = LocalEnv.current
    Reveal(modifier) {
        Column {
            EText(eyebrow, size = 11.sp, mono = true, color = EagleColors.Gold, letterSpacing = 2.sp)
            Spacer(Modifier.height(18.dp))
            EText(
                text = title,
                size = if (env.wide) 48.sp else 34.sp,
                lineHeight = if (env.wide) 52.sp else 38.sp,
                weight = FontWeight.Bold,
                letterSpacing = (-1).sp,
            )
            Spacer(Modifier.height(16.dp))
            EText(
                text = body,
                modifier = Modifier.widthIn(max = 560.dp),
                size = 17.sp,
                lineHeight = 27.sp,
                color = EagleColors.Body,
            )
        }
    }
}
