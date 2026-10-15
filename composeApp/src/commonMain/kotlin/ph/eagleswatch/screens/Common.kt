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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleMetrics
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.IconButton
import ph.eagleswatch.ui.LocalLayout

/**
 * The navy band at the top of a tab, with its title. Every screen starts on navy, so the status bar always
 * sits on the same colour. Phones pad for the status bar; wide windows sit under the top bar.
 */
@Composable
fun TabTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val layout = LocalLayout.current
    ContentColumn(
        modifier
            .background(EagleColors.Navy)
            .then(if (layout.wide) Modifier else Modifier.windowInsetsPadding(WindowInsets.statusBars)),
        max = if (layout.wide) 1200.dp else EagleMetrics.ContentMax,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = layout.gutter)
                .padding(top = if (layout.wide) 28.dp else 12.dp, bottom = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = EagleMetrics.Touch)) {
                EText(title, modifier = Modifier.weight(1f), display = true, size = EagleType.Display, lineHeight = 34.sp, color = EagleColors.OnNavy, heading = true)
                if (trailing != null) trailing()
            }
            if (subtitle != null) {
                EText(subtitle, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.OnNavyMuted)
            }
        }
    }
}

/**
 * The navy bar on a page opened over the tabs: back, an optional title, and actions on the right. Actions
 * should use [EagleColors.OnNavy] for their glyphs.
 */
@Composable
fun PageBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    backGlyph: Glyph = Glyph.Back,
    backDescription: String = "Back",
    onDark: Boolean = true,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val content = EagleColors.OnNavy
    Row(
        modifier
            .fillMaxWidth()
            .background(EagleColors.Navy)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(backGlyph, backDescription, onClick = onBack, tint = content)
        Spacer(Modifier.width(4.dp))
        if (title != null) {
            EText(title, modifier = Modifier.weight(1f), size = EagleType.Title, weight = FontWeight.SemiBold, color = content, maxLines = 1, ellipsis = true)
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
fun ContentColumn(modifier: Modifier = Modifier, max: Dp = EagleMetrics.ContentMax, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = max).fillMaxWidth(), content = content)
    }
}

/** Vertical rhythm between cards. */
@Composable
fun SectionGap() = Spacer(Modifier.height(EagleMetrics.CardGap))

