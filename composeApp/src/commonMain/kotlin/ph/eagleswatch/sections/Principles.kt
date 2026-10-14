package ph.eagleswatch.sections

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.GITHUB_URL
import ph.eagleswatch.content.Principle
import ph.eagleswatch.content.plannedSources
import ph.eagleswatch.content.principles
import ph.eagleswatch.ui.Chip
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalEnv
import ph.eagleswatch.ui.Page
import ph.eagleswatch.ui.Pill
import ph.eagleswatch.ui.Reveal
import ph.eagleswatch.ui.SectionHeader
import ph.eagleswatch.ui.SoftCard
import ph.eagleswatch.ui.anchored
import ph.eagleswatch.ui.softShadow

/** How the project works: three promises and the public sources it plans to connect. */
@Composable
fun PrinciplesSection() {
    val env = LocalEnv.current
    Page {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = if (env.wide) 96.dp else 64.dp)
                .anchored("principles"),
        ) {
            SectionHeader(
                eyebrow = "How it works",
                title = "Records, not accusations.",
                body = "The Eagle's Watch is non-partisan. We publish what official documents say, " +
                    "show where they come from, and let citizens judge for themselves.",
            )
            Spacer(Modifier.height(if (env.wide) 48.dp else 32.dp))
            if (env.wide) {
                Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    principles.forEach { PrincipleCard(it, Modifier.weight(1f).fillMaxHeight()) }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    principles.forEach { PrincipleCard(it, Modifier.fillMaxWidth()) }
                }
            }
            Spacer(Modifier.height(if (env.wide) 44.dp else 32.dp))
            PlannedSources()
        }
    }
}

@Composable
private fun PrincipleCard(principle: Principle, modifier: Modifier = Modifier) {
    Reveal(modifier) {
        SoftCard(Modifier.fillMaxWidth().fillMaxHeight(), padding = 28.dp) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(EagleColors.BlueSoft),
                contentAlignment = Alignment.Center,
            ) {
                EText(principle.index.trimStart('0'), size = 17.sp, weight = FontWeight.SemiBold, color = EagleColors.Blue)
            }
            Spacer(Modifier.height(20.dp))
            EText(principle.title, serif = true, size = 23.sp, lineHeight = 29.sp)
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
            EText("Public sources we plan to connect", size = 14.sp, weight = FontWeight.SemiBold, color = EagleColors.Muted)
            Spacer(Modifier.height(14.dp))
            // Manual wrapping (avoids FlowRow): three per row on wide windows, two per row on phones.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                plannedSources.chunked(if (env.wide) 3 else 2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { Chip(it) }
                    }
                }
            }
        }
    }
}

/** Closing call to action on a glossy flag-blue card. */
@Composable
fun CallToAction() {
    val env = LocalEnv.current
    val uriHandler = LocalUriHandler.current
    Page {
        Reveal(Modifier.padding(bottom = if (env.wide) 96.dp else 64.dp)) {
            val shape = RoundedCornerShape(32.dp)
            Column(
                Modifier
                    .fillMaxWidth()
                    .softShadow(shape, elevation = 24.dp)
                    .clip(shape)
                    .background(Brush.linearGradient(listOf(EagleColors.BlueBright, EagleColors.Blue, EagleColors.BlueDeep)))
                    .padding(if (env.wide) 56.dp else 28.dp),
            ) {
                EText("Join the watch", size = 14.sp, weight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.75f))
                Spacer(Modifier.height(14.dp))
                EText(
                    text = "The best watchdog is an informed citizen.",
                    serif = true,
                    size = if (env.wide) 42.sp else 30.sp,
                    lineHeight = if (env.wide) 48.sp else 36.sp,
                    color = Color.White,
                )
                Spacer(Modifier.height(14.dp))
                EText(
                    text = "The Eagle's Watch is open source. Follow the build, suggest data sources, or help us connect them.",
                    size = 17.sp,
                    lineHeight = 27.sp,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(28.dp))
                Pill("View on GitHub", onClick = { uriHandler.openUri(GITHUB_URL) })
            }
        }
    }
}
