package ph.eagleswatch.nav

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.eagle_logo
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.SampleBadge
import ph.eagleswatch.ui.focusRing

/** The three top-level screens. Everything in the app is one tap from any of them. */
enum class Destination(val label: String) {
    Trace("Trace"),
    HowItWorks("How it works"),
    Join("Join in"),
}

private val BarHeight = 64.dp

/** Flag-blue bar on every screen: brand on the left, tabs on wide windows, and the sample-data notice. */
@Composable
fun TopBar(current: Destination, onSelect: (Destination) -> Unit, modifier: Modifier = Modifier) {
    val layout = LocalLayout.current
    Box(
        modifier
            .fillMaxWidth()
            .background(EagleColors.Stage)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            Modifier.fillMaxWidth().height(BarHeight).padding(horizontal = layout.gutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Brand(onClick = { onSelect(Destination.Trace) })
            Spacer(Modifier.weight(1f))
            if (layout.wide) {
                Destination.entries.forEach { destination ->
                    TopTab(destination, selected = destination == current) { onSelect(destination) }
                    Spacer(Modifier.width(8.dp))
                }
                Spacer(Modifier.width(16.dp))
            }
            SampleBadge()
        }
    }
}

@Composable
private fun Brand(onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        Modifier
            .focusRing(shape, Color.White)
            .selectable(
                selected = false,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.eagle_logo),
            contentDescription = null,
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(9.dp)),
        )
        Spacer(Modifier.width(10.dp))
        EText("The Eagle's Watch", display = true, size = 24.sp, color = Color.White)
    }
}

@Composable
private fun TopTab(destination: Destination, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Column(
        Modifier
            .fillMaxHeight()
            .focusRing(shape, Color.White)
            .selectable(
                selected = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EText(
            text = destination.label,
            size = 15.sp,
            weight = FontWeight.SemiBold,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.78f),
        )
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .height(3.dp)
                .width(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) EagleColors.Gold else Color.Transparent),
        )
    }
}

/** Phone navigation: three labelled items inside thumb reach. */
@Composable
fun BottomNav(current: Destination, onSelect: (Destination) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .background(EagleColors.Card)
            .drawBehind { drawRect(EagleColors.Line, size = Size(size.width, 1.dp.toPx())) }
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Row(Modifier.fillMaxWidth().height(BarHeight)) {
            Destination.entries.forEach { destination ->
                BottomNavItem(
                    destination = destination,
                    selected = destination == current,
                    onClick = { onSelect(destination) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    destination: Destination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (selected) EagleColors.Blue else EagleColors.Muted
    Column(
        modifier
            .fillMaxHeight()
            .focusRing(RoundedCornerShape(8.dp), EagleColors.Blue)
            .selectable(
                selected = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .width(32.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                .background(if (selected) EagleColors.Gold else Color.Transparent),
        )
        Spacer(Modifier.height(9.dp))
        DestinationIcon(destination, color)
        Spacer(Modifier.height(3.dp))
        EText(destination.label, size = 12.sp, weight = FontWeight.SemiBold, color = color)
    }
}

/** Simple line icons drawn in code so the app needs no icon library. */
@Composable
private fun DestinationIcon(destination: Destination, color: Color) {
    Canvas(Modifier.size(24.dp)) {
        val unit = size.width / 24f
        val stroke = Stroke(width = 2f * unit, cap = StrokeCap.Round)
        when (destination) {
            Destination.Trace -> drawTraceIcon(unit, color)
            Destination.HowItWorks -> drawDocumentIcon(unit, color, stroke)
            Destination.Join -> drawJoinIcon(unit, color, stroke)
        }
    }
}

/** A rising path with three stops, like the rail on the Trace screen. */
private fun DrawScope.drawTraceIcon(unit: Float, color: Color) {
    val points = listOf(Offset(5f, 19f), Offset(12f, 12f), Offset(19f, 5f)).map { it * unit }
    drawLine(color, points.first(), points.last(), strokeWidth = 2f * unit, cap = StrokeCap.Round)
    points.forEach { drawCircle(color, radius = 2.6f * unit, center = it) }
}

private fun DrawScope.drawDocumentIcon(unit: Float, color: Color, stroke: Stroke) {
    drawRoundRect(
        color = color,
        topLeft = Offset(5f * unit, 3f * unit),
        size = Size(14f * unit, 18f * unit),
        cornerRadius = CornerRadius(2.5f * unit),
        style = stroke,
    )
    listOf(9f, 13f, 17f).forEachIndexed { i, y ->
        val end = if (i == 2) 13f else 16f
        drawLine(color, Offset(8f * unit, y * unit), Offset(end * unit, y * unit), strokeWidth = 2f * unit, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawJoinIcon(unit: Float, color: Color, stroke: Stroke) {
    drawCircle(color, radius = 9f * unit, center = Offset(12f * unit, 12f * unit), style = stroke)
    drawLine(color, Offset(12f * unit, 8f * unit), Offset(12f * unit, 16f * unit), strokeWidth = 2f * unit, cap = StrokeCap.Round)
    drawLine(color, Offset(8f * unit, 12f * unit), Offset(16f * unit, 12f * unit), strokeWidth = 2f * unit, cap = StrokeCap.Round)
}
