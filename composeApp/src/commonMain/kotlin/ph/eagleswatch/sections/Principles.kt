package ph.eagleswatch.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.GITHUB_URL
import ph.eagleswatch.content.Principle
import ph.eagleswatch.content.plannedSources
import ph.eagleswatch.content.principles
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalEnv
import ph.eagleswatch.ui.Page
import ph.eagleswatch.ui.Pill
import ph.eagleswatch.ui.Reveal
import ph.eagleswatch.ui.SectionHeader
import ph.eagleswatch.ui.Tag
import ph.eagleswatch.ui.anchored

/** How the project works: three promises and the public sources it plans to connect. */
@Composable
fun PrinciplesSection() {
    val env = LocalEnv.current
    Page {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = if (env.wide) 112.dp else 72.dp)
                .anchored("principles"),
        ) {
            SectionHeader(
                eyebrow = "HOW IT WORKS",
                title = "Records, not accusations.",
                body = "The Eagle's Watch is non-partisan. We publish what official documents say, " +
                    "show where they come from, and let citizens judge for themselves.",
            )
            Spacer(Modifier.height(if (env.wide) 56.dp else 40.dp))
            if (env.wide) {
                Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    principles.forEach { PrincipleCard(it, Modifier.weight(1f).fillMaxHeight()) }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    principles.forEach { PrincipleCard(it, Modifier.fillMaxWidth()) }
                }
            }
            Spacer(Modifier.height(if (env.wide) 48.dp else 36.dp))
            PlannedSources()
        }
    }
}

@Composable
private fun PrincipleCard(principle: Principle, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(18.dp)
    Reveal(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(shape)
                .background(EagleColors.Raised)
                .border(1.dp, EagleColors.Line, shape)
                .padding(24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EText(principle.index, size = 12.sp, mono = true, color = EagleColors.Gold, letterSpacing = 2.sp)
                Box(Modifier.width(24.dp).height(1.dp).background(EagleColors.LineStrong))
                EText(principle.verb, size = 11.sp, mono = true, color = EagleColors.Muted, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(22.dp))
            EText(principle.title, size = 22.sp, lineHeight = 27.sp, weight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))
            EText(principle.body, size = 15.sp, lineHeight = 24.sp, color = EagleColors.Body)
        }
    }
}

@Composable
private fun PlannedSources() {
    val env = LocalEnv.current
    Reveal {
        Column {
            EText("PLANNED PUBLIC SOURCES", size = 10.sp, mono = true, color = EagleColors.Muted, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(14.dp))
            // Manual wrapping (avoids FlowRow): one row on wide windows, two per row on phones.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                plannedSources.chunked(if (env.wide) 6 else 2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { Tag(it.uppercase()) }
                    }
                }
            }
        }
    }
}

/** Closing call to action. */
@Composable
fun CallToAction() {
    val env = LocalEnv.current
    val uriHandler = LocalUriHandler.current
    Page {
        Reveal(Modifier.padding(bottom = if (env.wide) 112.dp else 72.dp)) {
            val shape = RoundedCornerShape(28.dp)
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(EagleColors.Raised)
                    .border(1.dp, EagleColors.Line, shape)
                    .padding(if (env.wide) 56.dp else 28.dp),
            ) {
                EText("JOIN THE WATCH", size = 11.sp, mono = true, color = EagleColors.Gold, letterSpacing = 2.sp)
                Spacer(Modifier.height(18.dp))
                EText(
                    text = "The best watchdog is an informed citizen.",
                    size = if (env.wide) 44.sp else 30.sp,
                    lineHeight = if (env.wide) 48.sp else 34.sp,
                    weight = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                )
                Spacer(Modifier.height(16.dp))
                EText(
                    text = "The Eagle's Watch is open source. Follow the build, suggest data sources, or help us connect them.",
                    size = 17.sp,
                    lineHeight = 27.sp,
                    color = EagleColors.Body,
                )
                Spacer(Modifier.height(32.dp))
                Pill("View on GitHub", onClick = { uriHandler.openUri(GITHUB_URL) }, primary = true)
            }
        }
    }
}
