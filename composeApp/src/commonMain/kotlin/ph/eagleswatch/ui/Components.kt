package ph.eagleswatch.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Draws a visible ring while the element has keyboard focus.
 * Chain it directly before `clickable` / `selectable` so it observes that element's focus.
 */
@Composable
fun Modifier.focusRing(shape: Shape, color: Color = EagleColors.Gold): Modifier {
    var focused by remember { mutableStateOf(false) }
    return this
        .onFocusChanged { focused = it.isFocused }
        .then(if (focused) Modifier.border(3.dp, color, shape) else Modifier)
}

enum class PillTone { Blue, Gold, Ghost }

/** Flat, fully rounded button. Blue on light surfaces, gold or ghost on the blue stage. */
@Composable
fun Pill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: PillTone = PillTone.Blue,
) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val background by animateColorAsState(
        targetValue = when (tone) {
            PillTone.Blue -> if (hovered) Color(0xFF0E2F96) else EagleColors.Blue
            PillTone.Gold -> if (hovered) Color(0xFFFFC94D) else EagleColors.Gold
            PillTone.Ghost -> if (hovered) Color.White.copy(alpha = 0.16f) else Color.Transparent
        },
        animationSpec = tween(140),
        label = "pill-bg",
    )
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .focusRing(shape, if (tone == PillTone.Blue) EagleColors.Gold else Color.White)
            .clip(shape)
            .background(background)
            .then(if (tone == PillTone.Ghost) Modifier.border(2.dp, Color.White.copy(alpha = 0.7f), shape) else Modifier)
            .hoverable(source)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        EText(
            text = label,
            size = 16.sp,
            weight = FontWeight.SemiBold,
            color = when (tone) {
                PillTone.Blue -> Color.White
                PillTone.Gold -> EagleColors.Ink
                PillTone.Ghost -> Color.White
            },
        )
    }
}

/** Outlined rounded button for picking one of a few options. Filled while [selected]. */
@Composable
fun ChoiceChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val background by animateColorAsState(
        targetValue = when {
            selected -> EagleColors.Blue
            hovered -> EagleColors.BlueSoft
            else -> EagleColors.Card
        },
        animationSpec = tween(140),
        label = "choice-bg",
    )
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .focusRing(shape)
            .clip(shape)
            .background(background)
            .border(1.5.dp, if (selected) EagleColors.Blue else EagleColors.Blue.copy(alpha = 0.3f), shape)
            .hoverable(source)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        EText(label, size = 15.sp, weight = FontWeight.SemiBold, color = if (selected) Color.White else EagleColors.Blue)
    }
}

/** A quiet, underlined action for secondary choices next to a [Pill]. */
@Composable
fun TextAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = EagleColors.Blue,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier
            .focusRing(shape, color)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        EText(
            text = label,
            size = 15.sp,
            weight = FontWeight.SemiBold,
            color = color,
            decoration = TextDecoration.Underline,
        )
    }
}

/** Lays children out left to right, starting a new row when the current one is full. */
@Composable
fun Wrap(modifier: Modifier = Modifier, spacing: Dp = 8.dp, content: @Composable () -> Unit) {
    Layout(content, modifier) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val rows = mutableListOf<MutableList<Placeable>>()
        var rowWidth = 0
        placeables.forEach { placeable ->
            if (rows.isEmpty() || rowWidth + gap + placeable.width > constraints.maxWidth) {
                rows += mutableListOf(placeable)
                rowWidth = placeable.width
            } else {
                rows.last() += placeable
                rowWidth += gap + placeable.width
            }
        }
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else rows.maxOfOrNull { row -> row.sumOf { it.width } + gap * (row.size - 1) } ?: 0
        val height = rows.sumOf { row -> row.maxOf { it.height } } + gap * (rows.size - 1).coerceAtLeast(0)
        layout(width, height) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + gap
                }
                y += row.maxOf { it.height } + gap
            }
        }
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

/** Marks every placeholder figure. Used wherever numbers appear. */
@Composable
fun SampleBadge(modifier: Modifier = Modifier) {
    Chip("Sample data", modifier, background = EagleColors.RedSoft, color = EagleColors.Red)
}

/** Three short bars in the flag's colours. */
@Composable
fun FlagMark(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        listOf(EagleColors.Blue, EagleColors.Red, EagleColors.Gold).forEach { color ->
            Box(Modifier.size(width = 14.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(color))
        }
    }
}

/** Thin rule between rows. */
@Composable
fun Divider(modifier: Modifier = Modifier, color: Color = EagleColors.Line) {
    Spacer(modifier.height(1.dp).background(color))
}
