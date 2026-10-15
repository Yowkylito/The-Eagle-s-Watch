package ph.eagleswatch.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Attribution
import ph.eagleswatch.content.Status
import ph.eagleswatch.content.Verification

/* ---------------------------------------------------------------- Interaction */

/**
 * Draws a visible ring while the element has keyboard focus.
 * Chain it directly before `clickable` / `selectable` so it observes that element's focus.
 */
@Composable
fun Modifier.focusRing(shape: Shape, color: Color = EagleColors.Blue): Modifier {
    var focused by remember { mutableStateOf(false) }
    // Only for keyboard navigation: a tap or click also focuses on some platforms, and needs no ring.
    val keyboard = LocalInputModeManager.current.inputMode == InputMode.Keyboard
    return this
        .onFocusChanged { focused = it.isFocused }
        .then(if (focused && keyboard) Modifier.border(2.dp, color, shape) else Modifier)
}

/**
 * Makes an element tappable with pressed and hover feedback that never shifts layout: a tint behind the
 * content. Pass [selected] for tabs and options, [toggled] for on/off controls such as Watch.
 */
@Composable
fun Modifier.tappable(
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(EagleMetrics.Radius),
    role: Role = Role.Button,
    enabled: Boolean = true,
    description: String? = null,
    selected: Boolean? = null,
    toggled: Boolean? = null,
    tint: Color = EagleColors.SurfaceStrong,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val hovered by source.collectIsHoveredAsState()
    val overlay = when {
        !enabled -> Color.Transparent
        pressed -> tint
        hovered -> tint.copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    val interaction = when {
        toggled != null -> Modifier.toggleable(
            value = toggled,
            interactionSource = source,
            indication = null,
            enabled = enabled,
            role = role,
            onValueChange = { onClick() },
        )
        selected != null -> Modifier.selectable(
            selected = selected,
            interactionSource = source,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = onClick,
        )
        else -> Modifier.clickable(
            interactionSource = source,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = onClick,
        )
    }
    val labelled = if (description != null) Modifier.semantics { contentDescription = description } else Modifier
    return this
        .focusRing(shape)
        .clip(shape)
        .background(overlay)
        .hoverable(source, enabled)
        .then(interaction)
        .then(labelled)
}

/* ---------------------------------------------------------------- Buttons */

enum class ButtonStyle { Primary, Secondary, Quiet }

/**
 * A rectangular button. [ButtonStyle.Primary] is solid blue for the one main action on a screen,
 * [ButtonStyle.Secondary] is outlined, [ButtonStyle.Quiet] is a plain blue label for minor actions.
 */
@Composable
fun Button(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Secondary,
    glyph: Glyph? = null,
    enabled: Boolean = true,
    compact: Boolean = false,
    fillWidth: Boolean = false,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val hovered by source.collectIsHoveredAsState()
    val shape = RoundedCornerShape(EagleMetrics.Radius)
    val background = when (style) {
        ButtonStyle.Primary -> when {
            !enabled -> EagleColors.SurfaceStrong
            pressed || hovered -> EagleColors.BlueDeep
            else -> EagleColors.Blue
        }
        ButtonStyle.Secondary -> when {
            pressed -> EagleColors.SurfaceStrong
            hovered -> EagleColors.Surface
            else -> EagleColors.Page
        }
        ButtonStyle.Quiet -> when {
            pressed -> EagleColors.BlueSoft
            hovered -> EagleColors.Surface
            else -> Color.Transparent
        }
    }
    val content = when {
        !enabled -> EagleColors.Muted
        style == ButtonStyle.Primary -> Color.White
        style == ButtonStyle.Quiet -> EagleColors.Blue
        else -> EagleColors.Ink
    }
    val border = if (style == ButtonStyle.Secondary) {
        Modifier.border(1.dp, if (enabled) EagleColors.LineStrong else EagleColors.Line, shape)
    } else {
        Modifier
    }
    Row(
        modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = if (compact) 36.dp else EagleMetrics.Touch)
            .focusRing(shape)
            .clip(shape)
            .background(background)
            .then(border)
            .hoverable(source, enabled)
            .clickable(interactionSource = source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (compact) 12.dp else 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (glyph != null) {
            Icon(glyph, tint = content, size = 18.dp)
            Spacer(Modifier.width(8.dp))
        }
        EText(
            text = label,
            size = if (compact) EagleType.Secondary else EagleType.Body,
            weight = FontWeight.SemiBold,
            color = content,
            maxLines = 1,
        )
    }
}

/**
 * A square tap target around a glyph, with an accessible [description]. [toggled] makes it an on/off
 * control (Watch); [label] shows a text label beside the glyph where there is room.
 */
@Composable
fun IconButton(
    glyph: Glyph,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = EagleColors.Ink,
    iconSize: Dp = 22.dp,
    toggled: Boolean? = null,
    label: String? = null,
    bordered: Boolean = false,
) {
    val shape = RoundedCornerShape(EagleMetrics.Radius)
    Row(
        modifier
            .heightIn(min = EagleMetrics.Touch)
            .widthIn(min = EagleMetrics.Touch)
            .then(if (bordered) Modifier.border(1.dp, EagleColors.LineStrong, shape) else Modifier)
            .tappable(
                onClick = onClick,
                shape = shape,
                role = if (toggled != null) Role.Checkbox else Role.Button,
                description = description,
                toggled = toggled,
            )
            .padding(horizontal = if (label != null) 10.dp else 0.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(glyph, tint = tint, size = iconSize)
        if (label != null) {
            Spacer(Modifier.width(6.dp))
            EText(label, size = EagleType.Secondary, weight = FontWeight.SemiBold, color = tint, maxLines = 1)
        }
    }
}

/* ---------------------------------------------------------------- Tags and status */

/** A colour and the soft background it reads on. */
data class Tone(val color: Color, val soft: Color)

val Status.tone: Tone
    get() = when (this) {
        Status.Planned -> Tone(EagleColors.Muted, EagleColors.Surface)
        Status.Ongoing -> Tone(EagleColors.Blue, EagleColors.BlueSoft)
        Status.Delayed -> Tone(EagleColors.Amber, EagleColors.AmberSoft)
        Status.Suspended -> Tone(EagleColors.Red, EagleColors.RedSoft)
        Status.Completed -> Tone(EagleColors.Green, EagleColors.GreenSoft)
    }

val Verification.tone: Tone
    get() = when (this) {
        Verification.Verified -> Tone(EagleColors.Green, EagleColors.GreenSoft)
        Verification.AgencyReported -> Tone(EagleColors.Body, EagleColors.Surface)
        Verification.CommunityReported -> Tone(EagleColors.Body, EagleColors.Surface)
        Verification.Unavailable -> Tone(EagleColors.Muted, EagleColors.Surface)
    }

/**
 * A distinct shape for each status, so status never depends on colour alone:
 * an outline for Planned, a half disc for Ongoing, a triangle for Delayed, a square for Suspended,
 * a ticked disc for Completed.
 */
@Composable
fun StatusMark(status: Status, modifier: Modifier = Modifier, size: Dp = 12.dp, color: Color = status.tone.color) {
    Canvas(modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val c = Offset(r, r)
        val line = (this.size.minDimension / 6f).coerceAtLeast(1.5f)
        when (status) {
            Status.Planned -> drawCircle(color, r - line / 2, c, style = Stroke(line))
            Status.Ongoing -> {
                drawCircle(color, r - line / 2, c, style = Stroke(line))
                drawArc(color, -90f, 180f, true, Offset.Zero, Size(this.size.width, this.size.height))
            }
            Status.Delayed -> {
                val path = Path().apply {
                    moveTo(r, 0f)
                    lineTo(this@Canvas.size.width, this@Canvas.size.height)
                    lineTo(0f, this@Canvas.size.height)
                    close()
                }
                drawPath(path, color)
            }
            Status.Suspended -> drawRect(color, Offset(line * 0.5f, line * 0.5f), Size(this.size.width - line, this.size.height - line))
            Status.Completed -> {
                drawCircle(color, r, c)
                val check = Path().apply {
                    moveTo(r * 0.55f, r * 1.05f)
                    lineTo(r * 0.9f, r * 1.4f)
                    lineTo(r * 1.5f, r * 0.7f)
                }
                drawPath(check, Color.White, style = Stroke(line, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
            }
        }
    }
}

/** A small rectangular label. Always carries text; colour is secondary. */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = EagleColors.Body,
    background: Color = EagleColors.Surface,
    glyph: Glyph? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier
            .heightIn(min = 22.dp)
            .clip(RoundedCornerShape(EagleMetrics.RadiusSmall))
            .background(background)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(5.dp))
        } else if (glyph != null) {
            Icon(glyph, tint = color, size = 13.dp, weight = 2.2f)
            Spacer(Modifier.width(4.dp))
        }
        EText(text, size = EagleType.Caption, weight = FontWeight.SemiBold, color = color, maxLines = 1, letterSpacing = 0.2.sp)
    }
}

/** Project status with its mark and label; [detail] adds e.g. "78%" after the label. */
@Composable
fun StatusTag(status: Status, modifier: Modifier = Modifier, detail: String? = null) {
    val tone = status.tone
    Tag(
        text = if (detail != null) "${status.label} · $detail" else status.label,
        modifier = modifier,
        color = tone.color,
        background = tone.soft,
        leading = { StatusMark(status, size = 10.dp) },
    )
}

@Composable
fun VerificationTag(verification: Verification, modifier: Modifier = Modifier) {
    val tone = verification.tone
    Tag(verification.label, modifier, color = tone.color, background = tone.soft, glyph = verification.glyph)
}

/** Marks a development fixture. */
@Composable
fun SampleTag(modifier: Modifier = Modifier) {
    Tag("Sample data", modifier, color = EagleColors.Amber, background = EagleColors.AmberSoft, glyph = Glyph.Alert)
}

/**
 * One line under a figure saying how it was established: "Agency-reported · DPWH progress update · Oct 2, 2026".
 */
@Composable
fun ProvenanceLine(attribution: Attribution, modifier: Modifier = Modifier, prefix: String? = null) {
    val tone = attribution.verification.tone
    Row(modifier, verticalAlignment = Alignment.Top) {
        Icon(attribution.verification.glyph, modifier = Modifier.padding(top = 1.dp), tint = tone.color, size = 14.dp, weight = 2.1f)
        Spacer(Modifier.width(5.dp))
        val parts = listOfNotNull(prefix, attribution.verification.label, attribution.source, attribution.asOf?.format())
        EText(parts.joinToString(" · "), size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted)
    }
}

/* ---------------------------------------------------------------- Structure */

/** One-pixel rule. */
@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = EagleColors.Line) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

/** Draws a hairline along the bottom edge without taking layout space. */
fun Modifier.hairlineBelow(color: Color = EagleColors.Line): Modifier = drawBehind {
    val h = 1.dp.toPx()
    drawRect(color, Offset(0f, size.height - h), Size(size.width, h))
}

/** Uppercase section label, announced as a heading. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: (@Composable RowScope.() -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        EText(
            text = text.uppercase(),
            modifier = Modifier.weight(1f),
            size = EagleType.Caption,
            weight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = EagleColors.Muted,
            heading = true,
        )
        if (trailing != null) trailing()
    }
}

/**
 * A labelled value in a fact grid. A null [value] is shown as "Not on file", never hidden; [muted] greys a
 * value that is a state rather than a fact, such as "Not yet completed".
 */
data class FactItem(val label: String, val value: String?, val note: String? = null, val muted: Boolean = false)

@Composable
fun FactCell(item: FactItem, modifier: Modifier = Modifier) {
    Column(modifier) {
        EText(item.label, size = EagleType.Caption, color = EagleColors.Muted, lineHeight = 16.sp)
        Spacer(Modifier.height(2.dp))
        if (item.value != null) {
            EText(item.value, size = EagleType.Body, lineHeight = 20.sp, color = if (item.muted) EagleColors.Muted else EagleColors.Ink, tabular = true)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Glyph.CircleDashed, tint = EagleColors.Muted, size = 13.dp, weight = 2.2f)
                Spacer(Modifier.width(4.dp))
                EText("Not on file", size = EagleType.Body, lineHeight = 20.sp, color = EagleColors.Muted)
            }
        }
        if (item.note != null) {
            EText(item.note, size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted)
        }
    }
}

/** Facts in [columns] columns, filling row by row. */
@Composable
fun FactGrid(items: List<FactItem>, modifier: Modifier = Modifier, columns: Int = 2) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { FactCell(it, Modifier.weight(1f)) }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/* ---------------------------------------------------------------- Meters */

/**
 * A thin bar. [fraction] fills it; a null fraction draws the empty track for "not available".
 * [marker] draws a tick at a second value, e.g. the share of contract time used, so a reader can compare
 * work done against time spent on one line.
 */
@Composable
fun Meter(
    fraction: Float?,
    modifier: Modifier = Modifier,
    color: Color = EagleColors.Blue,
    marker: Float? = null,
    height: Dp = 6.dp,
) {
    Canvas(modifier.fillMaxWidth().height(height + 6.dp)) {
        val barTop = 3.dp.toPx()
        val barHeight = height.toPx()
        drawRect(EagleColors.SurfaceStrong, Offset(0f, barTop), Size(size.width, barHeight))
        if (fraction != null) {
            drawRect(color, Offset(0f, barTop), Size(size.width * fraction.coerceIn(0f, 1f), barHeight))
        }
        if (marker != null) {
            val x = (size.width * marker.coerceIn(0f, 1f)).coerceIn(1.dp.toPx(), size.width - 1.dp.toPx())
            drawRect(EagleColors.Ink, Offset(x - 1.dp.toPx(), 0f), Size(2.dp.toPx(), size.height))
        }
    }
}

/** A meter with its label, value and the line that says where the value came from. */
@Composable
fun MeterRow(
    label: String,
    value: String,
    fraction: Float?,
    modifier: Modifier = Modifier,
    color: Color = EagleColors.Blue,
    marker: Float? = null,
    attribution: Attribution? = null,
    note: String? = null,
) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom) {
            EText(label, modifier = Modifier.weight(1f), size = EagleType.Secondary, color = EagleColors.Body)
            EText(value, size = EagleType.Secondary, weight = FontWeight.SemiBold, tabular = true)
        }
        Spacer(Modifier.height(4.dp))
        Meter(fraction, color = color, marker = marker)
        if (attribution != null) {
            Spacer(Modifier.height(2.dp))
            ProvenanceLine(attribution)
        }
        if (note != null) {
            Spacer(Modifier.height(2.dp))
            EText(note, size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted)
        }
    }
}

/* ---------------------------------------------------------------- Notices and states */

enum class NoticeTone(val color: Color, val soft: Color) {
    Sample(EagleColors.Amber, EagleColors.AmberSoft),
    Info(EagleColors.Blue, EagleColors.BlueSoft),
    Warning(EagleColors.Amber, EagleColors.AmberSoft),
    Error(EagleColors.Red, EagleColors.RedSoft),
}

/** A full-width band with a coloured rule on its left edge. Used for sample-data, offline and error notes. */
@Composable
fun Notice(
    text: String,
    modifier: Modifier = Modifier,
    tone: NoticeTone = NoticeTone.Info,
    title: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min).background(tone.soft)) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(tone.color))
        Column(Modifier.weight(1f).padding(start = 10.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)) {
            if (title != null) {
                EText(title, size = EagleType.Label, weight = FontWeight.SemiBold, color = tone.color, lineHeight = 18.sp)
            }
            EText(text, size = EagleType.Label, lineHeight = 18.sp, color = EagleColors.Ink)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(2.dp))
                Button(actionLabel, onClick = onAction, style = ButtonStyle.Quiet, compact = true, modifier = Modifier.padding(start = 0.dp))
            }
        }
    }
}

/** A left-aligned empty state: what is missing, why, and the one thing to do about it. */
@Composable
fun EmptyState(
    glyph: Glyph,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    primary: Boolean = false,
) {
    Column(modifier.fillMaxWidth().padding(vertical = 24.dp)) {
        Icon(glyph, tint = EagleColors.Muted, size = 28.dp)
        Spacer(Modifier.height(12.dp))
        EText(title, size = EagleType.Title, weight = FontWeight.SemiBold, lineHeight = 22.sp, heading = true)
        Spacer(Modifier.height(4.dp))
        EText(body, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body, modifier = Modifier.widthIn(max = 480.dp))
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(actionLabel, onClick = onAction, style = if (primary) ButtonStyle.Primary else ButtonStyle.Secondary)
        }
    }
}

@Composable
fun ErrorState(title: String, body: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    EmptyState(Glyph.Alert, title, body, modifier, actionLabel = "Try again", onAction = onRetry, primary = true)
}

/** Grey bars standing in for rows while records load. Reserves the space so content does not jump. */
@Composable
fun SkeletonRows(count: Int = 4, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().semantics { contentDescription = "Loading" }) {
        repeat(count) {
            Column(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                SkeletonBar(0.3f, 12.dp)
                Spacer(Modifier.height(8.dp))
                SkeletonBar(0.85f, 16.dp)
                Spacer(Modifier.height(8.dp))
                SkeletonBar(0.6f, 12.dp)
                Spacer(Modifier.height(10.dp))
                SkeletonBar(1f, 6.dp)
            }
            Hairline()
        }
    }
}

@Composable
private fun SkeletonBar(widthFraction: Float, height: Dp) {
    Box(
        Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(EagleMetrics.RadiusSmall))
            .background(EagleColors.SurfaceStrong),
    )
}

/* ---------------------------------------------------------------- Inputs and choices */

/** A search box: glyph, field, and a clear button once there is text. */
@Composable
fun SearchField(
    query: String,
    onQuery: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    description: String = "Search",
) {
    val fonts = LocalFonts.current
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(EagleMetrics.Radius)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = EagleMetrics.Touch)
            .border(if (focused) 2.dp else 1.dp, if (focused) EagleColors.Blue else EagleColors.LineStrong, shape)
            .padding(start = 10.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Glyph.Search, tint = EagleColors.Muted, size = 18.dp)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                EText(placeholder, size = EagleType.Body, color = EagleColors.Muted, maxLines = 1, ellipsis = true)
            }
            BasicTextField(
                value = query,
                onValueChange = { onQuery(it.replace("\n", " ")) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                textStyle = TextStyle(fontFamily = fonts.sans, fontSize = EagleType.Body, color = EagleColors.Ink),
                cursorBrush = SolidColor(EagleColors.Blue),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused }
                    .semantics { contentDescription = description },
            )
        }
        if (query.isNotEmpty()) {
            IconButton(Glyph.Close, "Clear search", onClick = { onQuery("") }, tint = EagleColors.Muted, iconSize = 18.dp)
        }
    }
}

/** A bordered text box with a visible [label] and a placeholder. Multi-line unless [singleLine]. */
@Composable
fun TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    singleLine: Boolean = false,
    minHeight: Dp = if (singleLine) EagleMetrics.Touch else 110.dp,
    maxLength: Int = 1000,
) {
    val fonts = LocalFonts.current
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(EagleMetrics.Radius)
    Column(modifier.fillMaxWidth()) {
        if (label != null) {
            EText(label, size = EagleType.Secondary, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
        }
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .border(if (focused) 2.dp else 1.dp, if (focused) EagleColors.Blue else EagleColors.LineStrong, shape)
                .padding(horizontal = 12.dp, vertical = 11.dp),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
        ) {
            if (value.isEmpty()) {
                EText(
                    placeholder,
                    size = EagleType.Body,
                    lineHeight = 22.sp,
                    color = EagleColors.Muted,
                    maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                    ellipsis = singleLine,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = { if (it.length <= maxLength) onValueChange(if (singleLine) it.replace("\n", " ") else it) },
                singleLine = singleLine,
                textStyle = TextStyle(fontFamily = fonts.sans, fontSize = EagleType.Body, lineHeight = 22.sp, color = EagleColors.Ink),
                cursorBrush = SolidColor(EagleColors.Blue),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused }
                    .semantics { contentDescription = label ?: placeholder },
            )
        }
    }
}

/**
 * A rectangular choice. Multi-select by default ([Role.Checkbox]); pass [Role.RadioButton] for one-of-many.
 * The visible box is 36dp; the tap target around it is 44dp.
 */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
    enabled: Boolean = true,
    role: Role = Role.Checkbox,
    leading: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(EagleMetrics.Radius)
    val outer = RoundedCornerShape(EagleMetrics.Radius + 4.dp)
    Box(
        modifier
            .tappable(
                onClick = onClick,
                shape = outer,
                role = role,
                enabled = enabled,
                toggled = if (role == Role.Checkbox) selected else null,
                selected = if (role != Role.Checkbox) selected else null,
                tint = Color.Transparent,
            )
            .padding(vertical = 4.dp),
    ) {
        Row(
            Modifier
                .height(36.dp)
                .clip(shape)
                .background(if (selected) EagleColors.BlueSoft else EagleColors.Page)
                .border(1.dp, if (selected) EagleColors.Blue else EagleColors.Line, shape)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(6.dp))
            } else if (selected) {
                Icon(Glyph.Check, tint = EagleColors.Blue, size = 14.dp, weight = 2.6f)
                Spacer(Modifier.width(6.dp))
            }
            EText(
                label,
                size = EagleType.Secondary,
                weight = FontWeight.SemiBold,
                color = if (!enabled) EagleColors.Muted else if (selected) EagleColors.Blue else EagleColors.Ink,
                maxLines = 1,
            )
            if (count != null) {
                Spacer(Modifier.width(5.dp))
                EText(count.toString(), size = EagleType.Label, color = if (selected) EagleColors.Blue else EagleColors.Muted, tabular = true)
            }
        }
    }
}

/** Lays children out left to right, starting a new row when the current one is full. */
@Composable
fun Wrap(modifier: Modifier = Modifier, spacing: Dp = 8.dp, rowSpacing: Dp = 0.dp, content: @Composable () -> Unit) {
    Layout(content, modifier) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val rowGap = rowSpacing.roundToPx()
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
        val height = rows.sumOf { row -> row.maxOf { it.height } } + rowGap * (rows.size - 1).coerceAtLeast(0)
        layout(width, height) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + gap
                }
                y += row.maxOf { it.height } + rowGap
            }
        }
    }
}

/**
 * A row that opens something: a document, a source, a page. With no [onClick] it is plain text, and
 * says so with [unavailable] instead of pretending to be a link.
 */
@Composable
fun LinkRow(
    title: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
    glyph: Glyph? = null,
    onClick: (() -> Unit)? = null,
    external: Boolean = false,
    unavailable: String? = null,
) {
    val base = Modifier.fillMaxWidth().heightIn(min = 48.dp)
    val row = if (onClick != null) base.tappable(onClick = onClick) else base
    Row(modifier.then(row).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (glyph != null) {
            Icon(glyph, tint = EagleColors.Body, size = 20.dp)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            EText(
                title,
                size = EagleType.Body,
                lineHeight = 20.sp,
                weight = FontWeight.SemiBold,
                color = if (onClick != null) EagleColors.Blue else EagleColors.Ink,
            )
            if (detail != null) {
                EText(detail, size = EagleType.Label, lineHeight = 17.sp, color = EagleColors.Muted)
            }
            if (onClick == null && unavailable != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Glyph.CircleDashed, tint = EagleColors.Muted, size = 12.dp, weight = 2.2f)
                    Spacer(Modifier.width(4.dp))
                    EText(unavailable, size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted)
                }
            }
        }
        if (onClick != null) {
            Spacer(Modifier.width(8.dp))
            Icon(if (external) Glyph.External else Glyph.ChevronRight, tint = EagleColors.Muted, size = 18.dp)
        }
    }
}

/** Three short bars in the flag's colours: the brand mark on the About page. */
@Composable
fun FlagMark(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        listOf(EagleColors.Blue, EagleColors.Red, EagleColors.Gold).forEach { color ->
            Box(Modifier.size(width = 14.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(color))
        }
    }
}
