package ph.eagleswatch.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Attribution
import ph.eagleswatch.content.Status
import ph.eagleswatch.content.Verification

/* ================================================================ Interaction */

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
        hovered -> tint.copy(alpha = tint.alpha * 0.5f)
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

/**
 * Keeps touches on a page drawn over other content from reaching what is underneath. Put it on the root of
 * every full-screen page and sheet; without it a tap on an empty part of the page falls through.
 */
fun Modifier.consumeTouches(): Modifier = pointerInput(Unit) { detectTapGestures { } }

/* ================================================================ Surfaces */

/** A white card on the canvas. No border and no shadow: the canvas does the separating. */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(EagleMetrics.RadiusCard)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(EagleColors.Page)
            .then(if (onClick != null) Modifier.tappable(onClick = onClick, shape = shape, description = description, tint = EagleColors.Surface) else Modifier)
            .padding(padding),
        content = content,
    )
}

/** The title row of a card, with an optional control on the right. */
@Composable
fun CardHeader(title: String, modifier: Modifier = Modifier, subtitle: String? = null, trailing: (@Composable RowScope.() -> Unit)? = null) {
    Row(modifier.fillMaxWidth().heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            EText(title, size = EagleType.Title, weight = FontWeight.SemiBold, display = true, heading = true)
            if (subtitle != null) EText(subtitle, size = EagleType.Label, lineHeight = 18.sp, color = EagleColors.Muted)
        }
        if (trailing != null) trailing()
    }
}

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

/** A small label above a group of controls in a sheet. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: (@Composable RowScope.() -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        EText(text, modifier = Modifier.weight(1f), size = EagleType.Secondary, weight = FontWeight.SemiBold, heading = true)
        if (trailing != null) trailing()
    }
}

/** A rounded square holding a glyph: the leading element of list rows. */
@Composable
fun IconTile(glyph: Glyph, modifier: Modifier = Modifier, tint: Color = EagleColors.Body, background: Color = EagleColors.Surface, size: Dp = 40.dp) {
    Box(modifier.size(size).clip(RoundedCornerShape(EagleMetrics.RadiusChip)).background(background), contentAlignment = Alignment.Center) {
        Icon(glyph, tint = tint, size = size * 0.5f)
    }
}

/** Initials in a circle, for people named on a record. Not a photo, and not presented as one. */
@Composable
fun Initials(name: String, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val letters = name.split(" ")
        .filter { part -> part.isNotEmpty() && part.first().isUpperCase() && !part.endsWith(".") }
        .let { listOfNotNull(it.firstOrNull(), it.drop(1).lastOrNull()) }
        .joinToString("") { it.first().toString() }
    Box(modifier.size(size).clip(CircleShape).background(EagleColors.Surface), contentAlignment = Alignment.Center) {
        EText(letters, size = (size.value * 0.36f).sp, weight = FontWeight.SemiBold, color = EagleColors.Body)
    }
}

/* ================================================================ Buttons */

enum class ButtonStyle { Primary, Secondary, Quiet, OnDark }

/**
 * [ButtonStyle.Primary] is solid blue for the one main action on a screen, [ButtonStyle.Secondary] a grey
 * fill, [ButtonStyle.Quiet] a plain blue label, [ButtonStyle.OnDark] a translucent white fill on navy.
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
    trailingGlyph: Glyph? = null,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val hovered by source.collectIsHoveredAsState()
    val shape = RoundedCornerShape(EagleMetrics.Radius)
    val active = pressed || hovered
    val background = when (style) {
        ButtonStyle.Primary -> if (!enabled) EagleColors.SurfaceStrong else if (active) EagleColors.BlueDeep else EagleColors.Blue
        ButtonStyle.Secondary -> if (active) EagleColors.SurfaceStrong else EagleColors.Surface
        ButtonStyle.Quiet -> if (active) EagleColors.BlueSoft else Color.Transparent
        ButtonStyle.OnDark -> Color.White.copy(alpha = if (active) 0.22f else 0.12f)
    }
    val content = when {
        !enabled -> EagleColors.Muted
        style == ButtonStyle.Primary || style == ButtonStyle.OnDark -> Color.White
        style == ButtonStyle.Quiet -> EagleColors.Blue
        else -> EagleColors.Ink
    }
    Row(
        modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = if (compact) 40.dp else EagleMetrics.Touch)
            .focusRing(shape)
            .clip(shape)
            .background(background)
            .hoverable(source, enabled)
            .clickable(interactionSource = source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (compact) 12.dp else 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (glyph != null) {
            Icon(glyph, tint = content, size = 18.dp)
            Spacer(Modifier.width(8.dp))
        }
        EText(label, size = if (compact) EagleType.Secondary else EagleType.Body, weight = FontWeight.SemiBold, color = content, maxLines = 1)
        if (trailingGlyph != null) {
            Spacer(Modifier.width(4.dp))
            Icon(trailingGlyph, tint = content, size = 16.dp, weight = 2.2f)
        }
    }
}

/**
 * A 48dp tap target around a glyph, with an accessible [description]. [toggled] makes it an on/off control;
 * [label] shows text beside the glyph; [filled] gives it a translucent backing for use on navy.
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
    filled: Color? = null,
) {
    val shape = RoundedCornerShape(EagleMetrics.Radius)
    Row(
        modifier
            .heightIn(min = EagleMetrics.Touch)
            .widthIn(min = EagleMetrics.Touch)
            .then(if (bordered) Modifier.border(1.dp, EagleColors.LineStrong, shape) else Modifier)
            .then(if (filled != null) Modifier.clip(shape).background(filled) else Modifier)
            .tappable(
                onClick = onClick,
                shape = shape,
                role = if (toggled != null) Role.Checkbox else Role.Button,
                description = description,
                toggled = toggled,
                tint = if (filled != null) Color.White.copy(alpha = 0.16f) else EagleColors.SurfaceStrong,
            )
            .padding(horizontal = if (label != null) 12.dp else 0.dp),
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

/* ================================================================ Status and provenance */

/** A colour and the soft background it reads on. */
data class Tone(val color: Color, val soft: Color)

val Status.tone: Tone
    get() = when (this) {
        Status.Planned -> Tone(EagleColors.Body, EagleColors.Surface)
        Status.Ongoing -> Tone(EagleColors.Blue, EagleColors.BlueSoft)
        Status.Delayed -> Tone(EagleColors.Amber, EagleColors.AmberSoft)
        Status.Suspended -> Tone(EagleColors.Red, EagleColors.RedSoft)
        Status.Completed -> Tone(EagleColors.Green, EagleColors.GreenSoft)
    }

/** The bar and dot fill for a status. */
val Status.fill: Color
    get() = when (this) {
        Status.Planned -> EagleColors.FillGrey
        Status.Ongoing -> EagleColors.FillBlue
        Status.Delayed -> EagleColors.FillAmber
        Status.Suspended -> EagleColors.FillRed
        Status.Completed -> EagleColors.FillGreen
    }

val Verification.tone: Tone
    get() = when (this) {
        Verification.Verified -> Tone(EagleColors.Green, EagleColors.GreenSoft)
        Verification.AgencyReported -> Tone(EagleColors.Body, EagleColors.Surface)
        Verification.CommunityReported -> Tone(EagleColors.Body, EagleColors.Surface)
        Verification.Unavailable -> Tone(EagleColors.Muted, EagleColors.Surface)
    }

/** What each label promises, in one sentence. Shown in the source sheet and on About. */
val Verification.meaning: String
    get() = when (this) {
        Verification.Verified -> "Checked against an independent source: a site visit, an audit report or a second record."
        Verification.AgencyReported -> "Taken from the implementing or proposing agency's own posting. Nobody has checked it independently."
        Verification.CommunityReported -> "Submitted by a citizen through the app. Nobody has checked it independently."
        Verification.Unavailable -> "Nobody has supplied this information yet. The record says so instead of guessing."
    }

/**
 * A distinct shape for each status, so status never depends on colour alone:
 * an outline for Planned, a half disc for Ongoing, a triangle for Delayed, a square for Suspended,
 * a ticked disc for Completed.
 */
@Composable
fun StatusMark(status: Status, modifier: Modifier = Modifier, size: Dp = 12.dp, color: Color = status.tone.color) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val r = this.size.minDimension / 2f
        val c = Offset(r, r)
        val line = (this.size.minDimension / 6f).coerceAtLeast(1.5f)
        when (status) {
            Status.Planned -> drawCircle(color, r - line / 2, c, style = Stroke(line))
            Status.Ongoing -> {
                drawCircle(color, r - line / 2, c, style = Stroke(line))
                drawArc(color, -90f, 180f, true, Offset.Zero, Size(w, h))
            }
            Status.Delayed -> {
                val path = Path().apply {
                    moveTo(r, 0f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path, color)
            }
            Status.Suspended -> drawRoundRect(color, Offset(line * 0.5f, line * 0.5f), Size(w - line, h - line), CornerRadius(line * 0.6f))
            Status.Completed -> {
                drawCircle(color, r, c)
                val check = Path().apply {
                    moveTo(r * 0.55f, r * 1.05f)
                    lineTo(r * 0.9f, r * 1.4f)
                    lineTo(r * 1.5f, r * 0.7f)
                }
                drawPath(check, Color.White, style = Stroke(line, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

/** A small rounded label. Always carries text; colour is secondary. */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = EagleColors.Body,
    background: Color = EagleColors.Surface,
    glyph: Glyph? = null,
    leading: (@Composable () -> Unit)? = null,
    trailingGlyph: Glyph? = null,
) {
    Row(
        modifier
            .heightIn(min = 24.dp)
            .clip(RoundedCornerShape(EagleMetrics.RadiusSmall))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(6.dp))
        } else if (glyph != null) {
            Icon(glyph, tint = color, size = 13.dp, weight = 2.2f)
            Spacer(Modifier.width(5.dp))
        }
        EText(text, size = EagleType.Caption, weight = FontWeight.SemiBold, color = color, maxLines = 1)
        if (trailingGlyph != null) {
            Spacer(Modifier.width(4.dp))
            Icon(trailingGlyph, tint = color, size = 12.dp, weight = 2.2f)
        }
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
fun SampleTag(modifier: Modifier = Modifier, onDark: Boolean = false) {
    Tag(
        "Sample data",
        modifier,
        color = if (onDark) EagleColors.Ink else EagleColors.Amber,
        background = if (onDark) EagleColors.Gold else EagleColors.AmberSoft,
        glyph = Glyph.Alert,
    )
}

/** What the source sheet shows: which figure, and where it came from. */
@Immutable
data class SourceInfo(val figure: String, val attribution: Attribution)

/** Opens the source sheet. Provided by the app shell; a no-op where there is none. */
val LocalSourceSheet = staticCompositionLocalOf<(SourceInfo) -> Unit> { {} }

/**
 * The provenance of one figure as a small tappable badge. Tapping it opens a sheet with the source, the date
 * and what the label means, so the details are one tap away instead of printed under every number.
 */
@Composable
fun SourceBadge(figure: String, attribution: Attribution, modifier: Modifier = Modifier, quiet: Boolean = false) {
    val open = LocalSourceSheet.current
    val v = attribution.verification
    val tone = v.tone
    Box(
        modifier
            .heightIn(min = 40.dp)
            .tappable(
                onClick = { open(SourceInfo(figure, attribution)) },
                shape = RoundedCornerShape(EagleMetrics.RadiusSmall + 4.dp),
                description = "${v.label}. Source of $figure",
                tint = if (quiet) EagleColors.Surface else Color.Transparent,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (quiet) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(v.glyph, tint = EagleColors.Muted, size = 13.dp, weight = 2.2f)
                Spacer(Modifier.width(5.dp))
                EText(attribution.source, size = EagleType.Caption, color = EagleColors.Muted, maxLines = 1, ellipsis = true, modifier = Modifier.widthIn(max = 240.dp))
                Spacer(Modifier.width(2.dp))
                Icon(Glyph.ChevronRight, tint = EagleColors.Muted, size = 12.dp, weight = 2.2f)
            }
        } else {
            Tag(v.label, color = tone.color, background = tone.soft, glyph = v.glyph, trailingGlyph = Glyph.ChevronRight)
        }
    }
}

/** The body of the source sheet. */
@Composable
fun SourceSheetContent(info: SourceInfo, onClose: () -> Unit) {
    val a = info.attribution
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 10.dp, bottom = 16.dp)) {
        Box(Modifier.align(Alignment.CenterHorizontally).size(width = 36.dp, height = 4.dp).clip(CircleShape).background(EagleColors.SurfaceStrong))
        Spacer(Modifier.height(16.dp))
        EText("Where this comes from", display = true, size = 20.sp, weight = FontWeight.SemiBold, heading = true)
        EText(info.figure, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Muted)
        Spacer(Modifier.height(14.dp))
        VerificationTag(a.verification)
        Spacer(Modifier.height(8.dp))
        EText(a.verification.meaning, size = EagleType.Body, lineHeight = 22.sp, color = EagleColors.Body)
        Spacer(Modifier.height(12.dp))
        KeyValueRow("Source", a.source)
        Hairline()
        KeyValueRow("As of", a.asOf?.format() ?: "Date not on file", muted = a.asOf == null)
        Spacer(Modifier.height(16.dp))
        Button("Close", onClick = onClose, fillWidth = true)
    }
}

/* ================================================================ Data display */

/** A label on the left and its value on the right, wrapping under it when long. A null value says so. */
@Composable
fun KeyValueRow(label: String, value: String?, modifier: Modifier = Modifier, note: String? = null, muted: Boolean = false) {
    Row(modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.Top) {
        EText(label, modifier = Modifier.width(132.dp).padding(end = 12.dp), size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Muted)
        Column(Modifier.weight(1f)) {
            if (value != null) {
                EText(
                    value,
                    size = EagleType.Secondary,
                    lineHeight = 20.sp,
                    weight = FontWeight.Medium,
                    color = if (muted) EagleColors.Muted else EagleColors.Ink,
                    tabular = true,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Glyph.CircleDashed, tint = EagleColors.Muted, size = 13.dp, weight = 2.2f)
                    Spacer(Modifier.width(5.dp))
                    EText("Not on file", size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Muted)
                }
            }
            if (note != null) EText(note, size = EagleType.Caption, lineHeight = 17.sp, color = EagleColors.Muted)
        }
    }
}

/** A figure with its label underneath, for stat strips. */
@Composable
fun Stat(value: String, label: String, modifier: Modifier = Modifier, valueColor: Color = EagleColors.Ink, labelColor: Color = EagleColors.Muted, size: androidx.compose.ui.unit.TextUnit = EagleType.Figure) {
    Column(modifier) {
        EText(value, display = true, size = size, weight = FontWeight.SemiBold, color = valueColor, tabular = true, maxLines = 1)
        EText(label, size = EagleType.Caption, lineHeight = 16.sp, color = labelColor, maxLines = 1)
    }
}

/**
 * Work done against contract time used, on one rounded track. The fill is the reported work; the tick is the
 * share of time that has passed. A null [work] draws the empty track.
 */
@Composable
fun WorkTrack(
    work: Float?,
    modifier: Modifier = Modifier,
    color: Color = EagleColors.FillBlue,
    time: Float? = null,
    track: Color = EagleColors.SurfaceStrong,
    marker: Color = EagleColors.Ink,
    height: Dp = 8.dp,
) {
    Canvas(modifier.fillMaxWidth().height(height + 8.dp)) {
        val top = 4.dp.toPx()
        val h = height.toPx()
        val radius = CornerRadius(h / 2, h / 2)
        drawRoundRect(track, Offset(0f, top), Size(size.width, h), radius)
        if (work != null && work > 0f) {
            val w = (size.width * work.coerceIn(0f, 1f)).coerceAtLeast(h)
            drawRoundRect(color, Offset(0f, top), Size(w, h), radius)
        }
        if (time != null) {
            val x = (size.width * time.coerceIn(0f, 1f)).coerceIn(1.5.dp.toPx(), size.width - 1.5.dp.toPx())
            drawRoundRect(marker, Offset(x - 1.5.dp.toPx(), 0f), Size(3.dp.toPx(), size.height), CornerRadius(1.5.dp.toPx()))
        }
    }
}

/** A plain thin bar, for secondary figures such as funds disbursed. */
@Composable
fun Meter(fraction: Float?, modifier: Modifier = Modifier, color: Color = EagleColors.Ink) {
    WorkTrack(work = fraction, modifier = modifier, color = color, height = 6.dp)
}

/* ================================================================ Notices and states */

enum class NoticeTone(val color: Color, val soft: Color, val glyph: Glyph) {
    Sample(EagleColors.Amber, EagleColors.AmberSoft, Glyph.Alert),
    Info(EagleColors.Blue, EagleColors.BlueSoft, Glyph.Info),
    Warning(EagleColors.Amber, EagleColors.AmberSoft, Glyph.Alert),
    Error(EagleColors.Red, EagleColors.RedSoft, Glyph.Alert),
}

/** A soft rounded panel with a glyph, for sample-data, offline and error notes. */
@Composable
fun Notice(
    text: String,
    modifier: Modifier = Modifier,
    tone: NoticeTone = NoticeTone.Info,
    title: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(EagleMetrics.Radius))
            .background(tone.soft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(tone.glyph, modifier = Modifier.padding(top = 1.dp), tint = tone.color, size = 18.dp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            if (title != null) EText(title, size = EagleType.Secondary, weight = FontWeight.SemiBold, color = EagleColors.Ink, lineHeight = 20.sp)
            EText(text, size = EagleType.Label, lineHeight = 19.sp, color = EagleColors.Body)
            if (actionLabel != null && onAction != null) {
                Box(Modifier.heightIn(min = 40.dp).tappable(onClick = onAction, tint = Color.Transparent), contentAlignment = Alignment.CenterStart) {
                    EText("$actionLabel ›", size = EagleType.Label, weight = FontWeight.SemiBold, color = EagleColors.Blue)
                }
            }
        }
    }
}

/** An empty state: what is missing, why, and the one thing to do about it. */
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
    Column(modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        IconTile(glyph, tint = EagleColors.Body, background = EagleColors.SurfaceStrong, size = 56.dp)
        Spacer(Modifier.height(16.dp))
        EText(title, display = true, size = 20.sp, weight = FontWeight.SemiBold, align = TextAlign.Center, heading = true)
        Spacer(Modifier.height(6.dp))
        EText(body, size = EagleType.Secondary, lineHeight = 21.sp, color = EagleColors.Body, align = TextAlign.Center, modifier = Modifier.widthIn(max = 420.dp))
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(18.dp))
            Button(actionLabel, onClick = onAction, style = if (primary) ButtonStyle.Primary else ButtonStyle.Secondary)
        }
    }
}

@Composable
fun ErrorState(title: String, body: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    EmptyState(Glyph.Alert, title, body, modifier, actionLabel = "Try again", onAction = onRetry, primary = true)
}

/** Grey cards standing in for project cards while records load. Reserves the space so content does not jump. */
@Composable
fun SkeletonRows(count: Int = 3, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().semantics { contentDescription = "Loading" }, verticalArrangement = Arrangement.spacedBy(EagleMetrics.CardGap)) {
        repeat(count) {
            Card {
                SkeletonBar(0.28f, 22.dp)
                Spacer(Modifier.height(12.dp))
                SkeletonBar(0.9f, 18.dp)
                Spacer(Modifier.height(8.dp))
                SkeletonBar(0.6f, 14.dp)
                Spacer(Modifier.height(18.dp))
                SkeletonBar(1f, 8.dp)
            }
        }
    }
}

@Composable
private fun SkeletonBar(widthFraction: Float, height: Dp) {
    Box(Modifier.fillMaxWidth(widthFraction).height(height).clip(RoundedCornerShape(EagleMetrics.RadiusSmall)).background(EagleColors.Surface))
}

/* ================================================================ Inputs and choices */

/** A filled search box: glyph, field, and a clear button once there is text. */
@Composable
fun SearchField(
    query: String,
    onQuery: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    description: String = "Search",
    onDark: Boolean = false,
) {
    val fonts = LocalFonts.current
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(EagleMetrics.Radius)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = EagleMetrics.Touch)
            .clip(shape)
            .background(if (onDark) EagleColors.Page else EagleColors.Surface)
            .then(if (focused) Modifier.border(2.dp, EagleColors.Blue, shape) else Modifier)
            .padding(start = 14.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Glyph.Search, tint = EagleColors.Muted, size = 18.dp)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) EText(placeholder, size = EagleType.Body, color = EagleColors.Muted, maxLines = 1, ellipsis = true)
            BasicTextField(
                value = query,
                onValueChange = { onQuery(it.replace("\n", " ")) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                textStyle = TextStyle(fontFamily = fonts.sans, fontSize = EagleType.Body, color = EagleColors.Ink),
                cursorBrush = SolidColor(EagleColors.Blue),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }.semantics { contentDescription = description },
            )
        }
        if (query.isNotEmpty()) IconButton(Glyph.Close, "Clear search", onClick = { onQuery("") }, tint = EagleColors.Muted, iconSize = 18.dp)
    }
}

/** A text box with a visible [label] and a placeholder. Multi-line unless [singleLine]. */
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
                .clip(shape)
                .background(EagleColors.Surface)
                .then(if (focused) Modifier.border(2.dp, EagleColors.Blue, shape) else Modifier)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
        ) {
            if (value.isEmpty()) {
                EText(placeholder, size = EagleType.Body, lineHeight = 22.sp, color = EagleColors.Muted, maxLines = if (singleLine) 1 else Int.MAX_VALUE, ellipsis = singleLine)
            }
            BasicTextField(
                value = value,
                onValueChange = { if (it.length <= maxLength) onValueChange(if (singleLine) it.replace("\n", " ") else it) },
                singleLine = singleLine,
                textStyle = TextStyle(fontFamily = fonts.sans, fontSize = EagleType.Body, lineHeight = 22.sp, color = EagleColors.Ink),
                cursorBrush = SolidColor(EagleColors.Blue),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }.semantics { contentDescription = label ?: placeholder },
            )
        }
    }
}

/** Where a chip sits: on a white card or sheet, or on a navy header. */
enum class ChipSurface { Light, Dark }

/**
 * A filled choice chip. Multi-select by default ([Role.Checkbox]); pass [Role.RadioButton] for one-of-many.
 * Selected chips turn solid ink on light surfaces and solid white on navy. The visible chip is 36dp; the
 * tap target around it is 48dp.
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
    surface: ChipSurface = ChipSurface.Light,
    leading: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(EagleMetrics.RadiusChip)
    val dark = surface == ChipSurface.Dark
    val background = when {
        dark && selected -> EagleColors.Page
        dark -> Color.White.copy(alpha = 0.10f)
        selected -> EagleColors.Ink
        else -> EagleColors.Page
    }
    val content = when {
        !enabled -> if (dark) EagleColors.OnNavyMuted.copy(alpha = 0.6f) else EagleColors.Muted
        dark && selected -> EagleColors.Ink
        dark -> EagleColors.OnNavy
        selected -> EagleColors.Page
        else -> EagleColors.Ink
    }
    val countColor = when {
        dark && selected -> EagleColors.Muted
        dark -> EagleColors.OnNavyMuted
        selected -> EagleColors.Page.copy(alpha = 0.75f)
        else -> EagleColors.Muted
    }
    Box(
        modifier
            .tappable(
                onClick = onClick,
                shape = RoundedCornerShape(EagleMetrics.RadiusChip + 4.dp),
                role = role,
                enabled = enabled,
                toggled = if (role == Role.Checkbox) selected else null,
                selected = if (role == Role.RadioButton || role == Role.Tab) selected else null,
                tint = Color.Transparent,
            )
            .padding(vertical = 6.dp),
    ) {
        Row(
            Modifier
                .height(36.dp)
                .clip(shape)
                .background(background)
                .then(if (!dark && !selected) Modifier.border(1.dp, EagleColors.Line, shape) else Modifier)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(7.dp))
            }
            EText(label, modifier = Modifier.widthIn(max = 260.dp), size = EagleType.Secondary, weight = FontWeight.Medium, color = content, maxLines = 1, ellipsis = true)
            if (count != null) {
                Spacer(Modifier.width(6.dp))
                EText(count.toString(), size = EagleType.Label, weight = FontWeight.Medium, color = countColor, tabular = true)
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
 * A list row that opens something: a document, a source, a page. With no [onClick] it is plain text, and
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
    val base = Modifier.fillMaxWidth().heightIn(min = 56.dp)
    val row = if (onClick != null) base.tappable(onClick = onClick, tint = EagleColors.Surface) else base
    Row(modifier.then(row).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (glyph != null) {
            IconTile(glyph, tint = if (onClick != null) EagleColors.Blue else EagleColors.Body, background = if (onClick != null) EagleColors.BlueSoft else EagleColors.Surface)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            EText(title, size = EagleType.Body, lineHeight = 21.sp, weight = FontWeight.Medium, color = EagleColors.Ink)
            if (detail != null) EText(detail, size = EagleType.Label, lineHeight = 18.sp, color = EagleColors.Muted)
            if (onClick == null && unavailable != null) {
                Spacer(Modifier.height(4.dp))
                Tag(unavailable, glyph = Glyph.CircleDashed, color = EagleColors.Muted)
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
