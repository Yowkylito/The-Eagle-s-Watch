package ph.eagleswatch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleMetrics
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.IconButton
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.hairlineBelow

/** The title at the top of a tab. Phones pad for the status bar; wide windows sit under the top bar. */
@Composable
fun TabTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val layout = LocalLayout.current
    Column(
        modifier
            .fillMaxWidth()
            .then(if (layout.wide) Modifier else Modifier.windowInsetsPadding(WindowInsets.statusBars))
            .padding(horizontal = layout.gutter)
            .padding(top = if (layout.wide) 24.dp else 12.dp, bottom = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = EagleMetrics.Touch)) {
            EText(title, modifier = Modifier.weight(1f), display = true, size = EagleType.Display, lineHeight = 30.sp, heading = true)
            if (trailing != null) trailing()
        }
        if (subtitle != null) {
            EText(subtitle, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body)
        }
    }
}

/** The bar on a page opened over the tabs: back, an optional title, and actions on the right. */
@Composable
fun PageBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    backGlyph: Glyph = Glyph.Back,
    backDescription: String = "Back",
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(EagleColors.Page)
            .hairlineBelow()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(backGlyph, backDescription, onClick = onBack)
        Spacer(Modifier.width(4.dp))
        if (title != null) {
            EText(
                title,
                modifier = Modifier.weight(1f),
                size = EagleType.Body,
                weight = FontWeight.SemiBold,
                maxLines = 1,
                ellipsis = true,
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
        if (actions != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) { actions() }
        }
    }
}

/** Keeps content at a readable width on wide windows, centred. */
@Composable
fun ContentColumn(modifier: Modifier = Modifier, max: androidx.compose.ui.unit.Dp = EagleMetrics.ContentMax, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = max).fillMaxWidth(), content = content)
    }
}

/** Vertical rhythm between sections. */
@Composable
fun SectionGap() = Spacer(Modifier.height(20.dp))
