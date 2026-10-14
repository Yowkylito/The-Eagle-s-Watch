package ph.eagleswatch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Principle
import ph.eagleswatch.content.plannedSources
import ph.eagleswatch.content.principles
import ph.eagleswatch.ui.Chip
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalLayout

/** How the project works: what it promises, and the public sources it plans to connect. */
@Composable
fun HowItWorksScreen(modifier: Modifier = Modifier) {
    val layout = LocalLayout.current
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = layout.gutter, vertical = if (layout.wide) 40.dp else 28.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        if (layout.wide) {
            Row(Modifier.widthIn(max = 1120.dp), horizontalArrangement = Arrangement.spacedBy(64.dp)) {
                Column(Modifier.weight(0.8f)) {
                    Intro()
                    Spacer(Modifier.height(32.dp))
                    Sources()
                }
                Column(Modifier.weight(1.2f)) { Promises() }
            }
        } else {
            Intro()
            Spacer(Modifier.height(24.dp))
            Promises()
            Spacer(Modifier.height(28.dp))
            Sources()
        }
    }
}

@Composable
private fun Intro() {
    val wide = LocalLayout.current.wide
    EText(
        text = "Records, not accusations.",
        display = true,
        size = if (wide) 64.sp else 42.sp,
        lineHeight = if (wide) 62.sp else 42.sp,
    )
    Spacer(Modifier.height(14.dp))
    EText(
        text = "The Eagle's Watch is non-partisan. We publish what official documents say, " +
            "show where they come from, and let citizens judge for themselves.",
        modifier = Modifier.widthIn(max = 520.dp),
        size = 17.sp,
        lineHeight = 27.sp,
        color = EagleColors.Body,
    )
}

@Composable
private fun Promises() {
    Column {
        principles.forEachIndexed { index, principle ->
            if (index > 0) Spacer(Modifier.height(20.dp))
            Promise(principle)
        }
    }
}

/** A promise is a statement, not a step, so it gets a rule instead of a number or a card. */
@Composable
private fun Promise(principle: Principle) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        Box(Modifier.width(4.dp).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(EagleColors.Blue))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.padding(vertical = 2.dp)) {
            EText(principle.title, display = true, size = 28.sp, lineHeight = 30.sp)
            Spacer(Modifier.height(6.dp))
            EText(principle.body, modifier = Modifier.widthIn(max = 560.dp), size = 16.sp, lineHeight = 25.sp, color = EagleColors.Body)
        }
    }
}

@Composable
private fun Sources() {
    Column {
        EText("Public sources we plan to connect", size = 18.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        // Manual wrapping keeps this off the experimental FlowRow API.
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            plannedSources.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { Chip(it) }
                }
            }
        }
    }
}
