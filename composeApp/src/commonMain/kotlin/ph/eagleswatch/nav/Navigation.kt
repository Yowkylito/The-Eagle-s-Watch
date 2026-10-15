package ph.eagleswatch.nav

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.eagle_logo
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.IconButton
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.SampleTag
import ph.eagleswatch.ui.tappable

/** The four tabs. Project pages and reports open on top of whichever tab is showing. */
enum class Destination(val label: String, val glyph: Glyph, val selectedGlyph: Glyph = glyph) {
    Explore("Explore", Glyph.Search),
    Watchlist("Watchlist", Glyph.Eye, Glyph.EyeFilled),
    Updates("Updates", Glyph.Bell, Glyph.BellFilled),
    About("About", Glyph.Info),
}

/** Wide windows only: a navy bar with the brand, the tabs, the sample-data tag and Report. */
@Composable
fun TopBar(
    current: Destination,
    onSelect: (Destination) -> Unit,
    onReport: () -> Unit,
    sample: Boolean,
    modifier: Modifier = Modifier,
) {
    val layout = LocalLayout.current
    Box(modifier.fillMaxWidth().background(EagleColors.Navy).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(Modifier.fillMaxWidth().height(60.dp).padding(horizontal = layout.gutter), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(Res.drawable.eagle_logo), contentDescription = null, modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(10.dp))
            EText("The Eagle's Watch", display = true, size = 19.sp, weight = FontWeight.SemiBold, color = EagleColors.OnNavy, heading = true)
            Spacer(Modifier.width(32.dp))
            Destination.entries.forEach { destination ->
                TopTab(destination, selected = destination == current) { onSelect(destination) }
                Spacer(Modifier.width(4.dp))
            }
            Spacer(Modifier.weight(1f))
            if (sample) {
                SampleTag(onDark = true)
                Spacer(Modifier.width(12.dp))
            }
            IconButton(Glyph.Flag, "Report a problem", onClick = onReport, label = "Report", iconSize = 18.dp, tint = EagleColors.OnNavy, filled = Color.White.copy(alpha = 0.12f))
        }
    }
}

@Composable
private fun TopTab(destination: Destination, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .height(40.dp)
            .width(IntrinsicSize.Max)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color.White.copy(alpha = 0.14f) else Color.Transparent)
            .tappable(onClick = onClick, role = Role.Tab, selected = selected, tint = Color.White.copy(alpha = 0.10f), shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        EText(
            destination.label,
            size = EagleType.Body,
            weight = FontWeight.Medium,
            color = if (selected) EagleColors.OnNavy else EagleColors.OnNavyMuted,
        )
    }
}

/** Phone navigation: four labelled tabs, the selected one marked by a tinted indicator behind its icon. */
@Composable
fun BottomNav(current: Destination, onSelect: (Destination) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .background(EagleColors.Page)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(EagleColors.Line))
        Row(Modifier.fillMaxWidth().height(68.dp)) {
            Destination.entries.forEach { destination ->
                NavTab(destination, selected = destination == current, modifier = Modifier.weight(1f)) { onSelect(destination) }
            }
        }
    }
}

@Composable
private fun NavTab(destination: Destination, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .fillMaxHeight()
            .tappable(onClick = onClick, role = Role.Tab, selected = selected, tint = Color.Transparent, shape = RoundedCornerShape(0.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(width = 60.dp, height = 32.dp)
                .clip(CircleShape)
                .background(if (selected) EagleColors.BlueSoft else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                glyph = if (selected) destination.selectedGlyph else destination.glyph,
                tint = if (selected) EagleColors.Blue else EagleColors.Muted,
                size = 22.dp,
                weight = if (selected) 2.3f else 1.8f,
            )
        }
        Spacer(Modifier.height(4.dp))
        EText(
            destination.label,
            size = EagleType.Caption,
            weight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) EagleColors.Ink else EagleColors.Muted,
        )
    }
}

