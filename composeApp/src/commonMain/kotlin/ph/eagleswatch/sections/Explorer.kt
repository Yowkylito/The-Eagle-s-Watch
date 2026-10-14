package ph.eagleswatch.sections

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Tier
import ph.eagleswatch.content.trail
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalEnv
import ph.eagleswatch.ui.Page
import ph.eagleswatch.ui.Reveal
import ph.eagleswatch.ui.SectionHeader
import ph.eagleswatch.ui.anchored

/** Highest value in any sample allocation; bars are scaled against this so they never touch the edge. */
private const val BarScale = 40f

/** "Trace a peso": pick a level of government and see how a sample ₱100 is allocated there. */
@Composable
fun ExplorerSection() {
    val env = LocalEnv.current
    val levels = remember { trail.filter { it.sample != null } }
    var selected by remember { mutableStateOf(levels.first()) }

    Page {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = if (env.wide) 112.dp else 72.dp)
                .anchored("explorer"),
        ) {
            if (env.wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(64.dp)) {
                    Column(Modifier.weight(0.8f)) {
                        ExplorerIntro()
                        Spacer(Modifier.height(32.dp))
                        TierPicker(levels, selected, vertical = true) { selected = it }
                    }
                    Reveal(Modifier.weight(1.2f)) { AllocationCard(selected) }
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

@Composable
private fun ExplorerIntro() {
    SectionHeader(
        eyebrow = "THE EXPLORER",
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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            levels.forEach { TierChip(it, it == selected, onSelect) }
        }
    } else {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            levels.forEach { TierChip(it, it == selected, onSelect) }
        }
    }
}

@Composable
private fun TierChip(tier: Tier, selected: Boolean, onSelect: (Tier) -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) EagleColors.GoldSoft else EagleColors.Raised)
            .border(1.dp, if (selected) EagleColors.Gold else EagleColors.Line, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = { onSelect(tier) },
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EText(
            text = tier.index,
            size = 11.sp,
            mono = true,
            color = if (selected) EagleColors.Gold else EagleColors.Muted,
            letterSpacing = 1.4.sp,
        )
        EText(
            text = tier.short,
            size = 12.sp,
            mono = true,
            color = if (selected) EagleColors.Gold else EagleColors.Body,
            letterSpacing = 1.4.sp,
        )
    }
}

@Composable
private fun AllocationCard(tier: Tier) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(EagleColors.Raised)
            .border(1.dp, EagleColors.Line, shape)
            .padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                EText("SAMPLE ALLOCATION", size = 10.sp, mono = true, color = EagleColors.Muted, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(8.dp))
                EText(
                    text = "Where ₱100 goes at ${tier.name}",
                    size = 22.sp,
                    lineHeight = 28.sp,
                    weight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.width(12.dp))
            SampleBadge()
        }
        Spacer(Modifier.height(28.dp))
        tier.sample.orEmpty().forEachIndexed { index, allocation ->
            if (index > 0) Spacer(Modifier.height(18.dp))
            AllocationRow(allocation.label, allocation.pesos)
        }
        Spacer(Modifier.height(28.dp))
        EText(
            text = "Placeholder figures for design purposes. They are not real government data.",
            size = 12.sp,
            lineHeight = 18.sp,
            color = EagleColors.Muted,
        )
    }
}

@Composable
private fun SampleBadge() {
    val shape = RoundedCornerShape(4.dp)
    Box(
        Modifier
            .clip(shape)
            .border(1.dp, EagleColors.Red.copy(alpha = 0.7f), shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        EText("SAMPLE DATA", size = 9.5.sp, mono = true, color = EagleColors.Red, letterSpacing = 1.4.sp)
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
            EText(label, modifier = Modifier.weight(1f), size = 15.sp, color = EagleColors.Text)
            EText("₱$pesos", size = 15.sp, weight = FontWeight.Medium, color = EagleColors.Gold)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(EagleColors.Line),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fill.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.horizontalGradient(listOf(EagleColors.Gold, EagleColors.GoldBright))),
            )
        }
    }
}
