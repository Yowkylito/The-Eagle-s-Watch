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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Status

/**
 * Draws a visible ring while the element has keyboard focus.
 * Chain it directly before `clickable` / `selectable` so it observes that element's focus.
 */
@Composable
fun Modifier.focusRing(shape: Shape, color: Color = EagleColors.Gold): Modifier {
    var focused by remember { mutableStateOf(false) }
    // Only for keyboard navigation: a tap or click also focuses on some platforms, and needs no ring.
    val keyboard = LocalInputModeManager.current.inputMode == InputMode.Keyboard
    return this
        .onFocusChanged { focused = it.isFocused }
        .then(if (focused && keyboard) Modifier.border(3.dp, color, shape) else Modifier)
}

enum class PillTone { Blue, Gold, Ghost }

/** Flat, fully rounded button. Blue on white, gold or ghost on a blue card. */
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

enum class ButtonStyle { Primary, Outline }

/** The main buttons on a page: solid blue for the one action, outlined for "show all" and the like. */
@Composable
fun ActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    glyph: Glyph? = null,
    enabled: Boolean = true,
) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val shape = RoundedCornerShape(12.dp)
    val primary = style == ButtonStyle.Primary
    val background by animateColorAsState(
        targetValue = when {
            !enabled && primary -> EagleColors.Line
            primary && hovered -> EagleColors.BlueDeep
            primary -> EagleColors.Blue
            hovered -> EagleColors.Surface
            else -> EagleColors.Page
        },
        animationSpec = tween(140),
        label = "button-bg",
    )
    val content = when {
        !enabled -> EagleColors.Muted
        primary -> Color.White
        else -> EagleColors.Ink
    }
    Row(
        modifier
            .focusRing(shape)
            .clip(shape)
            .background(background)
            .then(if (primary) Modifier else Modifier.border(1.dp, if (enabled) EagleColors.Ink else EagleColors.Line, shape))
            .hoverable(source)
            .clickable(interactionSource = source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (glyph != null) {
            Icon(glyph, tint = content, size = 20.dp)
            Spacer(Modifier.width(8.dp))
        }
        EText(label, size = 16.sp, weight = FontWeight.SemiBold, color = content, maxLines = 1)
    }
}

/** A round white button that floats over a picture, like back and share on a project page. */
@Composable
fun CircleButton(
    glyph: Glyph,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = EagleColors.Ink,
    size: Dp = 36.dp,
) {
    Box(
        modifier
            .size(size)
            .shadow(3.dp, CircleShape)
            .focusRing(CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(glyph, tint = tint, size = size * 0.5f)
    }
}

val Status.colors: Pair<Color, Color>
    get() = when (this) {
        Status.Planned -> EagleColors.Body to EagleColors.Surface
        Status.Ongoing -> EagleColors.Blue to EagleColors.BlueSoft
        Status.Delayed -> EagleColors.Amber to EagleColors.AmberSoft
        Status.Completed -> EagleColors.Green to EagleColors.GreenSoft
    }

/** Project status as a small pill. [onPicture] gives it a solid white base so it reads over an image. */
@Composable
fun StatusBadge(status: Status, modifier: Modifier = Modifier, detail: String? = null, onPicture: Boolean = false) {
    val (text, soft) = status.colors
    Row(
        modifier
            .then(if (onPicture) Modifier.shadow(2.dp, RoundedCornerShape(50)) else Modifier)
            .clip(RoundedCornerShape(50))
            .background(if (onPicture) Color.White else soft)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).background(text, CircleShape))
        Spacer(Modifier.width(6.dp))
        EText(
            text = if (detail != null) "${status.label} · $detail" else status.label,
            size = 13.sp,
            weight = FontWeight.SemiBold,
            color = if (onPicture) EagleColors.Ink else text,
        )
    }
}

/** A thin rounded progress bar. */
@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier, color: Color = EagleColors.Blue, track: Color = EagleColors.Surface) {
    Box(modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(track)) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
    }
}

/** Initials in a circle, standing in for a photo until officials upload their own. */
@Composable
fun Avatar(initials: String, modifier: Modifier = Modifier, size: Dp = 48.dp, color: Color = EagleColors.Blue) {
    Box(modifier.size(size).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        EText(initials, size = (size.value * 0.36f).sp, weight = FontWeight.SemiBold, color = Color.White)
    }
}

/** A bordered text box with a placeholder. Multi-line unless [singleLine]. */
@Composable
fun TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minHeight: Dp = if (singleLine) 0.dp else 110.dp,
    maxLength: Int = 1000,
) {
    val fonts = LocalFonts.current
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .border(if (focused) 2.dp else 1.dp, if (focused) EagleColors.Ink else EagleColors.Line, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        if (value.isEmpty()) {
            EText(placeholder, size = 16.sp, lineHeight = 23.sp, color = EagleColors.Muted, maxLines = if (singleLine) 1 else Int.MAX_VALUE, ellipsis = singleLine)
        }
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= maxLength) onValueChange(if (singleLine) it.replace("\n", " ") else it) },
            singleLine = singleLine,
            textStyle = TextStyle(fontFamily = fonts.sans, fontSize = 16.sp, lineHeight = 23.sp, color = EagleColors.Ink),
            cursorBrush = SolidColor(EagleColors.Blue),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                .semantics { contentDescription = placeholder },
        )
    }
}
