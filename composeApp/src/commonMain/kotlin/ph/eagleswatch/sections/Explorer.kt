package ph.eagleswatch.sections

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Tier
import ph.eagleswatch.content.trail
import ph.eagleswatch.ui.Chip
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalEnv
import ph.eagleswatch.ui.Page
import ph.eagleswatch.ui.Reveal
import ph.eagleswatch.ui.SectionHeader
import ph.eagleswatch.ui.SoftCard
import ph.eagleswatch.ui.anchored
import ph.eagleswatch.ui.softShadow

/** Highest value in any sample allocation; bars are scaled against this so they never touch the edge. */
private const val BarScale = 40f

/** "Trace a peso": pick a level of government and see how a sample ₱100 is allocated there. */
@Composable
fun ExplorerSection() {
    val env = LocalEnv.current
    val levels = remember { trail.filter { it.sample != null } }
    var selected by remember { mutableStateOf(levels.first()) }

    Box(Modifier.fillMaxWidth().background(EagleColors.PaperDeep)) {
        Page {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (env.wide) 96.dp else 64.dp)
                    .anchored("explorer"),
            ) {
                if (env.wide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(56.dp)) {
                        Column(Modifier.weight(0.85f)) {
                            ExplorerIntro()
                            Spacer(Modifier.height(32.dp))
                            TierPicker(levels, selected, vertical = true) { selected = it }
                        }
                        Reveal(Modifier.weight(1.15f)) { AllocationCard(selected) }
                    }
                } else {
                    ExplorerIntro()
                    Spacer(Modifier.height(28.dp))
                    TierPicker(levels, selected, vertical = false) { selected = it }
                    Spacer(Modifier.height(20.dp))
                    Reveal { AllocationCard(selected) }
                }
            }
        }
    }
}

@Composable
private fun ExplorerIntro() {
    SectionHeader(
        eyebrow = "The explorer",
        title = "Trace a peso.",
        body = "Pick a level of government to see how a sample ₱100 is allocated there. " +
            "Live figures will replace these placeholders as official sources are connected.",
    )
}

@Composable
private fun TierPicker(
    levels: List<Tier>,
    selected: Tier,
    vertical: Boolean,
    onSelect: (Tier) -> Unit,
) {
    if (vertical) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            levels.forEach { TierChip(it, it == selected, onSelect) }
        }
    } else {
        // Extra padding so the chips' soft shadows aren't clipped by the scroll container.
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(Modifier.width(2.dp))
            levels.forEach { TierChip(it, it == selected, onSelect) }
            Spacer(Modifier.width(2.dp))
        }
    }
}

@Composable
private fun TierChip(tier: Tier, selected: Boolean, onSelect: (Tier) -> Unit) {
    val shape = RoundedCornerShape(50)
    val background by animateColorAsState(
        if (selected) EagleColors.Blue else EagleColors.Card,
        tween(220),
        label = "chip-bg",
    )
    Row(
        modifier = Modifier
            .softShadow(shape, elevation = if (selected) 10.dp else 4.dp)
            .clip(shape)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = { onSelect(tier) },
            )
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        EText(
            text = tier.index,
            size = 13.sp,
            weight = FontWeight.SemiBold,
            color = if (selected) Color.White.copy(alpha = 0.7f) else EagleColors.Muted,
        )
        EText(
            text = tier.name,
            size = 15.sp,
            weight = FontWeight.SemiBold,
            color = if (selected) Color.White else EagleColors.Ink,
        )
    }
}

@Composable
private fun AllocationCard(tier: Tier) {
    SoftCard(Modifier.fillMaxWidth(), padding = 28.dp, radius = 28.dp) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                EText("Where ₱100 goes", serif = true, size = 28.sp, lineHeight = 34.sp)
                Spacer(Modifier.height(4.dp))
                EText(tier.name, size = 16.sp, color = EagleColors.Body)
            }
            Spacer(Modifier.width(12.dp))
            Chip("Sample data", background = EagleColors.RedSoft, color = EagleColors.Red)
        }
        Spacer(Modifier.height(28.dp))
        tier.sample.orEmpty().forEachIndexed { index, allocation ->
            if (index > 0) Spacer(Modifier.height(20.dp))
            AllocationRow(allocation.label, allocation.pesos)
        }
        Spacer(Modifier.height(28.dp))
        EText(
            text = "Placeholder figures for design purposes. They are not real government data.",
            size = 13.sp,
            lineHeight = 19.sp,
            color = EagleColors.Muted,
        )
    }
}

@Composable
private fun AllocationRow(label: String, pesos: Int) {
    val fill by animateFloatAsState(
        targetValue = pesos / BarScale,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "allocation-bar",
    )
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EText(label, modifier = Modifier.weight(1f), size = 16.sp)
            EText("₱$pesos", size = 16.sp, weight = FontWeight.SemiBold, color = EagleColors.Blue)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(EagleColors.BlueSoft),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fill.coerceIn(0f, 1f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Brush.verticalGradient(listOf(EagleColors.BlueBright, EagleColors.Blue))),
            )
        }
    }
}
