package ph.eagleswatch.nav

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.eagle_logo
import ph.eagleswatch.ui.ActionButton
import ph.eagleswatch.ui.ButtonStyle
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.SampleBadge
import ph.eagleswatch.ui.focusRing

/**
 * The four tabs. Project, article and report pages open on top of whichever tab is showing.
 * [glyph] is null for About, which shows the app's own icon.
 */
enum class Destination(val label: String, val glyph: Glyph?, val selectedGlyph: Glyph? = glyph) {
    Explore("Explore", Glyph.Search),
    Watchlist("Watchlist", Glyph.Eye, Glyph.EyeFilled),
    Updates("Updates", Glyph.Bell, Glyph.BellFilled),
    About("About", null),
}

/** Wide windows only: brand on the left, tabs on the right, and the sample-data notice. */
@Composable
fun TopBar(current: Destination, onSelect: (Destination) -> Unit, onReport: () -> Unit, modifier: Modifier = Modifier) {
    val layout = LocalLayout.current
    Box(
        modifier
            .fillMaxWidth()
            .background(EagleColors.Page)
            .drawBehind { drawRect(EagleColors.Line, Offset(0f, size.height - 1.dp.toPx()), Size(size.width, 1.dp.toPx())) }
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            Modifier.fillMaxWidth().height(72.dp).padding(horizontal = layout.gutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.eagle_logo),
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(9.dp)),
            )
            Spacer(Modifier.width(10.dp))
            EText("The Eagle's Watch", display = true, size = 26.sp, color = EagleColors.Blue)
            Spacer(Modifier.weight(1f))
            Destination.entries.forEach { destination ->
                TopTab(destination, selected = destination == current) { onSelect(destination) }
                Spacer(Modifier.width(4.dp))
            }
            Spacer(Modifier.width(12.dp))
            ActionButton("Report", onClick = onReport, style = ButtonStyle.Outline, glyph = Glyph.Flag)
            Spacer(Modifier.width(16.dp))
            SampleBadge()
        }
    }
}

@Composable
private fun TopTab(destination: Destination, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .focusRing(shape)
            .clip(shape)
            .background(if (selected) EagleColors.Surface else EagleColors.Page)
            .selectable(
                selected = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EText(
            text = destination.label,
            size = 15.sp,
            weight = FontWeight.SemiBold,
            color = if (selected) EagleColors.Ink else EagleColors.Muted,
        )
    }
}

/**
 * Phone navigation: four tabs inside thumb reach, with Report raised in the middle so it is always one
 * tap away. Selected tabs switch to a filled or heavier icon.
 */
@Composable
fun BottomNav(current: Destination, onSelect: (Destination) -> Unit, onReport: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .background(EagleColors.Page)
            .drawBehind { drawRect(EagleColors.Line, size = Size(size.width, 1.dp.toPx())) }
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Row(Modifier.fillMaxWidth().height(66.dp), verticalAlignment = Alignment.CenterVertically) {
            val tabs = Destination.entries
            tabs.take(2).forEach { NavTab(it, it == current, Modifier.weight(1f)) { onSelect(it) } }
            ReportTab(onReport, Modifier.weight(1f))
            tabs.drop(2).forEach { NavTab(it, it == current, Modifier.weight(1f)) { onSelect(it) } }
        }
    }
}

@Composable
private fun NavTab(destination: Destination, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
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
        verticalArrangement = Arrangement.Center,
    ) {
        val glyph = if (selected) destination.selectedGlyph else destination.glyph
        if (glyph != null) {
            Icon(glyph, tint = color, size = 26.dp, weight = if (selected) 2.5f else 1.8f)
        } else {
            // About wears the app icon, greyed out until selected.
            Image(
                painter = painterResource(Res.drawable.eagle_logo),
                contentDescription = null,
                colorFilter = if (selected) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                alpha = if (selected) 1f else 0.7f,
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .then(if (selected) Modifier.border(1.5.dp, EagleColors.Blue, RoundedCornerShape(7.dp)) else Modifier),
            )
        }
        Spacer(Modifier.height(4.dp))
        EText(destination.label, size = 12.sp, weight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun ReportTab(onClick: () -> Unit, modifier: Modifier) {
    Column(
        modifier
            .fillMaxHeight()
            .focusRing(RoundedCornerShape(8.dp), EagleColors.Blue)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = "Report a problem" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(width = 52.dp, height = 36.dp)
                .shadow(4.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(EagleColors.Blue),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Glyph.FlagFilled, tint = Color.White, size = 20.dp, weight = 2f)
        }
        Spacer(Modifier.height(4.dp))
        EText("Report", size = 12.sp, weight = FontWeight.SemiBold, color = EagleColors.Blue)
    }
}
