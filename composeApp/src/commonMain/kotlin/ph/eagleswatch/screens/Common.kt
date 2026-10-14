package ph.eagleswatch.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Day
import ph.eagleswatch.content.facts
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.Pill
import ph.eagleswatch.ui.PillTone
import ph.eagleswatch.ui.Sun

/** The large title at the top of a tab, as on Watchlist and Updates. Wide windows show it under the top bar. */
@Composable
fun ScreenTitle(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    val layout = LocalLayout.current
    Column(
        modifier
            .fillMaxWidth()
            .then(if (layout.wide) Modifier else Modifier.windowInsetsPadding(WindowInsets.statusBars))
            .padding(horizontal = layout.gutter)
            .padding(top = if (layout.wide) 32.dp else 20.dp, bottom = 12.dp),
    ) {
        EText(title, size = 30.sp, lineHeight = 36.sp, weight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            EText(subtitle, size = 15.sp, lineHeight = 22.sp, color = EagleColors.Body)
        }
    }
}

/** A real rule of Philippine public finance, a different one each day. */
@Composable
fun FactCard(modifier: Modifier = Modifier) {
    var index by remember { mutableIntStateOf((Day.today().epochDay % facts.size).toInt()) }
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(EagleColors.Blue),
    ) {
        Sun(
            Modifier.align(Alignment.TopEnd).offset(x = 70.dp, y = (-70).dp).size(200.dp),
            color = Color.White.copy(alpha = 0.08f),
        )
        Column(Modifier.padding(22.dp).animateContentSize()) {
            EText("Alam mo ba?", display = true, size = 30.sp, lineHeight = 32.sp, color = EagleColors.Gold)
            EText("Did you know?", size = 13.sp, color = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(14.dp))
            AnimatedContent(
                targetState = index,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                label = "fact",
            ) { shown ->
                val fact = facts[shown % facts.size]
                Column {
                    EText(fact.text, size = 18.sp, lineHeight = 27.sp, color = Color.White)
                    Spacer(Modifier.height(10.dp))
                    EText(fact.source, size = 13.sp, lineHeight = 19.sp, color = Color.White.copy(alpha = 0.75f))
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill("Another fact", onClick = { index = (index + 1) % facts.size }, tone = PillTone.Ghost)
                Spacer(Modifier.width(14.dp))
                EText("${index + 1} of ${facts.size}", size = 13.sp, color = Color.White.copy(alpha = 0.75f))
            }
        }
    }
}
