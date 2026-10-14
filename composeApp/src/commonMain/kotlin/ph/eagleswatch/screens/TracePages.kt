package ph.eagleswatch.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import ph.eagleswatch.content.Allocation
import ph.eagleswatch.content.MAX_TAX_PESOS
import ph.eagleswatch.content.MIN_TAX_PESOS
import ph.eagleswatch.content.TaxKind
import ph.eagleswatch.content.facts
import ph.eagleswatch.content.taxKinds
import ph.eagleswatch.content.taxPresets
import ph.eagleswatch.content.trail
import ph.eagleswatch.state.TraceProgress
import ph.eagleswatch.state.largestIndex
import ph.eagleswatch.ui.ChoiceChip
import ph.eagleswatch.ui.CoinGrid
import ph.eagleswatch.ui.CoinSlider
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.Pill
import ph.eagleswatch.ui.PillTone
import ph.eagleswatch.ui.RollingPesos
import ph.eagleswatch.ui.SampleBadge
import ph.eagleswatch.ui.Sun
import ph.eagleswatch.ui.TextAction
import ph.eagleswatch.ui.Wrap
import ph.eagleswatch.ui.focusRing
import ph.eagleswatch.ui.formatPesos
import ph.eagleswatch.ui.formatPesosShort

/** A scrolling page inside the level pager. */
@Composable
private fun PageColumn(padding: Dp, content: @Composable ColumnScope.() -> Unit) {
    val wide = LocalLayout.current.wide
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = padding, vertical = if (wide) 36.dp else 24.dp),
        content = content,
    )
}

@Composable
private fun PageTitle(text: String) {
    val wide = LocalLayout.current.wide
    EText(
        text = text,
        display = true,
        size = if (wide) 52.sp else 40.sp,
        lineHeight = if (wide) 54.sp else 42.sp,
    )
}

@Composable
private fun Lead(text: String) {
    EText(text, modifier = Modifier.widthIn(max = 560.dp), size = 16.sp, lineHeight = 25.sp, color = EagleColors.Body)
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    EText(text, modifier = modifier, size = 18.sp, lineHeight = 24.sp, weight = FontWeight.SemiBold)
}

@Composable
private fun FinePrint(text: String) {
    EText(text, size = 13.sp, lineHeight = 19.sp, color = EagleColors.Muted)
}

// ---------------------------------------------------------------------------------------------------------
// Citizens: the citizen sets their own pesos.
// ---------------------------------------------------------------------------------------------------------

@Composable
internal fun CitizensPage(progress: TraceProgress, goTo: (Int) -> Unit, padding: Dp) {
    val citizens = trail.first()
    PageColumn(padding) {
        if (progress.returning) {
            WelcomeBack(progress, goTo)
            Spacer(Modifier.height(24.dp))
        }
        PageTitle("Start with your own pesos.")
        Spacer(Modifier.height(8.dp))
        Lead(citizens.tagline)
        Spacer(Modifier.height(20.dp))
        AmountCard(progress)
        Spacer(Modifier.height(20.dp))
        Pill("Follow my ${formatPesos(progress.pesos)}", onClick = { goTo(1) })

        Spacer(Modifier.height(36.dp))
        SectionTitle("Where it comes from")
        Spacer(Modifier.height(14.dp))
        taxKinds.forEach { TaxKindRow(it) }
        FinePrint("Sources: ${citizens.sources}")

        Spacer(Modifier.height(32.dp))
        FactCard()
    }
}

@Composable
private fun WelcomeBack(progress: TraceProgress, goTo: (Int) -> Unit) {
    val next = (1..trail.lastIndex).firstOrNull { it !in progress.visited } ?: trail.lastIndex
    val reached = progress.visited.size
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(EagleColors.GoldSoft)
            .padding(start = 18.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            EText("Welcome back.", size = 16.sp, weight = FontWeight.SemiBold)
            EText(
                text = "You reached $reached of ${trail.size} levels.",
                size = 14.sp,
                lineHeight = 20.sp,
                color = EagleColors.Body,
            )
        }
        Spacer(Modifier.width(12.dp))
        Pill("Go to ${trail[next].short}", onClick = { goTo(next) })
    }
}

@Composable
private fun AmountCard(progress: TraceProgress) {
    val label = "My taxes this year"
    Column(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(EagleColors.Card)
            .border(1.dp, EagleColors.Line, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        EText(label, size = 15.sp, weight = FontWeight.SemiBold, color = EagleColors.Body)
        RollingPesos(progress.pesos, size = 64.sp, signLift = 13.dp)
        Spacer(Modifier.height(4.dp))
        CoinSlider(
            value = progress.pesos,
            onValueChange = progress::updatePesos,
            min = MIN_TAX_PESOS,
            max = MAX_TAX_PESOS,
            label = label,
        )
        Spacer(Modifier.height(14.dp))
        Wrap(spacing = 8.dp) {
            taxPresets.forEach { preset ->
                ChoiceChip(
                    label = formatPesosShort(preset),
                    onClick = { progress.updatePesos(preset) },
                    selected = preset == progress.pesos,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        FinePrint("Employees can find the tax withheld from their pay on BIR Form 2316.")
    }
}

@Composable
private fun TaxKindRow(kind: TaxKind) {
    Row(Modifier.padding(bottom = 14.dp)) {
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(20.dp)
                .background(EagleColors.GoldDeep, CircleShape)
                .padding(2.dp)
                .background(EagleColors.Gold, CircleShape),
        )
        Spacer(Modifier.width(14.dp))
        Column {
            EText(kind.name, size = 16.sp, lineHeight = 22.sp, weight = FontWeight.SemiBold)
            EText(kind.body, modifier = Modifier.widthIn(max = 520.dp), size = 15.sp, lineHeight = 22.sp, color = EagleColors.Body)
        }
    }
}

/** Day number in Philippine time, so the fact changes at midnight in Manila. */
@OptIn(ExperimentalTime::class)
private fun manilaDay(): Long = (Clock.System.now().epochSeconds + 8 * 3600) / 86_400

/** A real rule of public finance, a different one each day. */
@Composable
private fun FactCard() {
    var index by remember { mutableIntStateOf((manilaDay() % facts.size).toInt()) }
    val shape = RoundedCornerShape(24.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .clip(shape)
            .background(EagleColors.Stage),
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

// ---------------------------------------------------------------------------------------------------------
// Levels: make a call, then watch the coins.
// ---------------------------------------------------------------------------------------------------------

@Composable
internal fun LevelPage(level: Int, progress: TraceProgress, goTo: (Int) -> Unit, padding: Dp) {
    val tier = trail[level]
    val sample = tier.sample ?: return
    // Largest share first; sortedByDescending is stable, so ties keep the listed order.
    val order = remember(sample) { sample.indices.sortedByDescending { sample[it].pesos } }
    val revealed = level in progress.revealed
    val reveal = remember { Animatable(if (revealed) 1f else 0f) }
    LaunchedEffect(revealed) {
        if (revealed) reveal.animateTo(1f, tween(durationMillis = 1500, easing = LinearOutSlowInEasing)) else reveal.snapTo(0f)
    }
    var highlight by remember { mutableStateOf<Int?>(null) }
    val pesos = progress.pesos

    PageColumn(padding) {
        PageTitle(tier.name)
        Spacer(Modifier.height(8.dp))
        Lead(tier.tagline)

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("How ${tier.spender} would split your ${formatPesos(pesos)}", Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            SampleBadge()
        }
        Spacer(Modifier.height(14.dp))
        Box(Modifier.animateContentSize()) {
            if (revealed) {
                CallResult(sample, top = largestIndex(sample), pick = progress.calls[level])
            } else {
                CallPrompt(sample, onCall = { progress.call(level, it) }, onSkip = { progress.reveal(level) })
            }
        }

        Spacer(Modifier.height(18.dp))
        val grid = @Composable { gridModifier: Modifier ->
            CoinGrid(
                counts = order.map { sample[it].pesos },
                colors = EagleColors.Slices,
                reveal = { reveal.value },
                highlight = highlight,
                modifier = gridModifier.semantics {
                    contentDescription = if (revealed) {
                        "100 coins: " + order.joinToString { "${sample[it].label} ${sample[it].pesos}" }
                    } else {
                        "100 coins, not yet revealed"
                    }
                },
            )
        }
        val legend = @Composable {
            order.forEachIndexed { rank, index ->
                LegendRow(
                    allocation = sample[index],
                    color = EagleColors.Slices[rank],
                    pesos = pesos * sample[index].pesos / 100,
                    selected = highlight == rank,
                    onClick = { highlight = if (highlight == rank) null else rank },
                )
            }
        }
        val coinNote = "Each coin is ${formatPesos(pesos / 100)}, one hundredth of your ${formatPesos(pesos)}."
        if (LocalLayout.current.wide) {
            // Wide cards have room to read the legend beside the coins.
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                grid(Modifier.weight(1f).widthIn(max = 400.dp))
                Column(Modifier.weight(1f)) {
                    if (revealed) legend()
                    Spacer(Modifier.height(10.dp))
                    FinePrint(coinNote)
                }
            }
        } else {
            grid(Modifier.widthIn(max = 400.dp).fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            FinePrint(coinNote)
            if (revealed) {
                Spacer(Modifier.height(18.dp))
                legend()
                Spacer(Modifier.height(6.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        FinePrint("Placeholder split for design purposes. It is not real government data.")

        Spacer(Modifier.height(28.dp))
        SectionTitle("What you'll be able to see")
        Spacer(Modifier.height(10.dp))
        tier.reveals.forEach { item ->
            Row(Modifier.padding(bottom = 8.dp)) {
                Box(Modifier.padding(top = 9.dp).size(6.dp).background(EagleColors.Blue, CircleShape))
                Spacer(Modifier.width(12.dp))
                EText(item, size = 15.sp, lineHeight = 23.sp, color = EagleColors.Ink)
            }
        }
        Spacer(Modifier.height(4.dp))
        FinePrint("Sources: ${tier.sources}")

        Spacer(Modifier.height(28.dp))
        if (level == trail.lastIndex) {
            FinishCard(progress, onRestart = { goTo(0) })
        } else {
            Pill("Next: ${trail[level + 1].short}", onClick = { goTo(level + 1) })
        }
    }
}

@Composable
private fun CallPrompt(sample: List<Allocation>, onCall: (Int) -> Unit, onSkip: () -> Unit) {
    Column {
        EText("Where does the most go? Make your call.", size = 16.sp, lineHeight = 24.sp, color = EagleColors.Body)
        Spacer(Modifier.height(12.dp))
        Wrap(spacing = 8.dp) {
            sample.forEachIndexed { index, allocation ->
                ChoiceChip(allocation.label, onClick = { onCall(index) })
            }
        }
        Spacer(Modifier.height(4.dp))
        TextAction("Skip and show the split", onClick = onSkip)
    }
}

@Composable
private fun CallResult(sample: List<Allocation>, top: Int, pick: Int?) {
    val haptics = LocalHapticFeedback.current
    val leader = sample[top]
    val right = pick == top
    LaunchedEffect(Unit) { if (right) haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
    val (title, body) = when {
        pick == null -> null to "The most goes to ${leader.label}: ${leader.pesos} of the 100 coins."
        right -> "You called it." to "The most goes to ${leader.label}: ${leader.pesos} of the 100 coins."
        else -> "Not this time." to
            "The most goes to ${leader.label}: ${leader.pesos} coins. Your pick, ${sample[pick].label}, has ${sample[pick].pesos}."
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (right) EagleColors.GoldSoft else EagleColors.BlueSoft)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        if (title != null) {
            EText(title, display = true, size = 26.sp, lineHeight = 28.sp, color = if (right) EagleColors.Ink else EagleColors.Blue)
            Spacer(Modifier.height(2.dp))
        }
        EText(body, size = 15.sp, lineHeight = 22.sp, color = EagleColors.Ink)
    }
}

@Composable
private fun LegendRow(allocation: Allocation, color: Color, pesos: Long, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .focusRing(shape)
            .clip(shape)
            .background(if (selected) EagleColors.BlueSoft else Color.Transparent)
            .selectable(selected = selected, role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(22.dp)
                .background(color, CircleShape)
                .border(2.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            EText(allocation.label, size = 16.sp, lineHeight = 22.sp)
            EText("${allocation.pesos} coins", size = 13.sp, lineHeight = 18.sp, color = EagleColors.Muted)
        }
        EText(formatPesos(pesos), size = 16.sp, weight = FontWeight.SemiBold, color = EagleColors.Ink)
    }
}

/** Shown at the top of the trail: how far the citizen got and how many calls they made right. */
@Composable
private fun FinishCard(progress: TraceProgress, onRestart: () -> Unit) {
    val correct = progress.correctCalls
    val callable = progress.callable
    val verdict = when {
        progress.calls.size < callable -> "Make a call on every level to see how sharp your eye is."
        correct == callable -> "Eagle eye. You called every level right."
        else -> "Clear your calls and climb again to beat $correct."
    }
    Box(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(EagleColors.Ink),
    ) {
        Sun(
            Modifier.align(Alignment.TopEnd).offset(x = 70.dp, y = (-70).dp).size(200.dp),
            color = Color.White.copy(alpha = 0.07f),
        )
        Column(Modifier.padding(24.dp)) {
            EText(
                text = "You followed ${formatPesos(progress.pesos)} to the top.",
                display = true,
                size = 34.sp,
                lineHeight = 36.sp,
                color = Color.White,
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                Score("${progress.visited.size} of ${trail.size}", "levels reached")
                Score("$correct of $callable", "calls right")
            }
            Spacer(Modifier.height(14.dp))
            EText(verdict, size = 15.sp, lineHeight = 22.sp, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(20.dp))
            Pill("Start again at Citizens", onClick = onRestart, tone = PillTone.Gold)
            if (progress.revealed.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                TextAction("Clear my calls", onClick = progress::resetCalls, color = Color.White)
            }
        }
    }
}

@Composable
private fun Score(value: String, label: String) {
    Column {
        EText(value, display = true, size = 32.sp, lineHeight = 34.sp, color = EagleColors.Gold)
        EText(label, size = 13.sp, color = Color.White.copy(alpha = 0.75f))
    }
}
