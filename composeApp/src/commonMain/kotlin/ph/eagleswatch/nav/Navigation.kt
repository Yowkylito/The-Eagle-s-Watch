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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
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
import ph.eagleswatch.ui.hairlineBelow
import ph.eagleswatch.ui.tappable

/** The four tabs. Project pages and reports open on top of whichever tab is showing. */
enum class Destination(val label: String, val glyph: Glyph, val selectedGlyph: Glyph = glyph) {
    Explore("Explore", Glyph.Search),
    Watchlist("Watchlist", Glyph.Eye, Glyph.EyeFilled),
    Updates("Updates", Glyph.Bell, Glyph.BellFilled),
    About("About", Glyph.Info),
}

/** Wide windows only: brand on the left, tabs beside it, Report and the sample-data tag on the right. */
@Composable
fun TopBar(
    current: Destination,
    onSelect: (Destination) -> Unit,
    onReport: () -> Unit,
    sample: Boolean,
    modifier: Modifier = Modifier,
) {
    val layout = LocalLayout.current
    Box(
        modifier
            .fillMaxWidth()
            .background(EagleColors.Page)
            .hairlineBelow()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = layout.gutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.eagle_logo),
                contentDescription = null,
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)),
            )
            Spacer(Modifier.width(10.dp))
            EText("The Eagle's Watch", display = true, size = 22.sp, color = EagleColors.Blue, heading = true)
            Spacer(Modifier.width(28.dp))
            Destination.entries.forEach { destination ->
                TopTab(destination, selected = destination == current) { onSelect(destination) }
                Spacer(Modifier.width(4.dp))
            }
            Spacer(Modifier.weight(1f))
            if (sample) {
                SampleTag()
                Spacer(Modifier.width(12.dp))
            }
            IconButton(Glyph.Flag, "Report a problem", onClick = onReport, label = "Report", iconSize = 18.dp, bordered = true)
        }
    }
}

@Composable
private fun TopTab(destination: Destination, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxHeight()
            .width(IntrinsicSize.Max)
            .tappable(onClick = onClick, role = Role.Tab, selected = selected)
            .padding(horizontal = 12.dp),
    ) {
        EText(
            text = destination.label,
            modifier = Modifier.align(Alignment.Center),
            size = EagleType.Body,
            weight = FontWeight.SemiBold,
            color = if (selected) EagleColors.Ink else EagleColors.Muted,
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(3.dp)
                .background(if (selected) EagleColors.Blue else Color.Transparent),
        )
    }
}

/** Phone navigation: four labelled tabs inside thumb reach. */
@Composable
fun BottomNav(current: Destination, onSelect: (Destination) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .background(EagleColors.Page)
            .drawBehind { drawRect(EagleColors.Line, size = Size(size.width, 1.dp.toPx())) }
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Row(Modifier.fillMaxWidth().height(56.dp)) {
            Destination.entries.forEach { destination ->
                NavTab(destination, selected = destination == current, modifier = Modifier.weight(1f)) { onSelect(destination) }
            }
        }
    }
}

@Composable
private fun NavTab(destination: Destination, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color = if (selected) EagleColors.Blue else EagleColors.Muted
    Column(
        modifier
            .fillMaxHeight()
            .tappable(onClick = onClick, role = Role.Tab, selected = selected),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            glyph = if (selected) destination.selectedGlyph else destination.glyph,
            tint = color,
            size = 22.dp,
            weight = if (selected) 2.4f else 1.8f,
        )
        Spacer(Modifier.height(3.dp))
        EText(destination.label, size = EagleType.Caption, weight = FontWeight.SemiBold, color = color)
    }
}
