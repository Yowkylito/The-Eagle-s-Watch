package ph.eagleswatch.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import ph.eagleswatch.content.Allocation
import ph.eagleswatch.content.Tier
import ph.eagleswatch.content.trail
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.Pill
import ph.eagleswatch.ui.SampleBadge
import ph.eagleswatch.ui.focusRing

private val CoinSize = 40.dp

/** Highest value in any sample allocation; bars are scaled against this so they never touch the edge. */
private const val BarScale = 40f

/**
 * The whole product in one screen: a peso coin travels a rail of six levels, from the citizen up to the
 * President. Swipe the card, tap a level or press Next, and the coin follows.
 */
@Composable
fun TraceScreen(pager: PagerState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val goTo: (Int) -> Unit = { page -> scope.launch { pager.animateScrollToPage(page) } }
    if (LocalLayout.current.wide) {
        WideTrace(pager, goTo, modifier)
    } else {
        PhoneTrace(pager, goTo, modifier)
    }
}

/** Where the coin is, in level units. It moves continuously while a swipe is in progress. */
private val PagerState.position: Float
    get() = currentPage + currentPageOffsetFraction

@Composable
private fun PhoneTrace(pager: PagerState, goTo: (Int) -> Unit, modifier: Modifier) {
    val layout = LocalLayout.current
    Column(modifier.fillMaxSize().background(EagleColors.Stage)) {
        Column(Modifier.padding(horizontal = layout.gutter)) {
            Spacer(Modifier.height(4.dp))
            Pitch(wide = false)
            Spacer(Modifier.height(18.dp))
            CoinRail(pager.position, pager.currentPage, goTo)
            Spacer(Modifier.height(4.dp))
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(EagleColors.Sheet),
        ) {
            LevelPager(pager, goTo, padding = layout.gutter)
        }
    }
}

@Composable
private fun WideTrace(pager: PagerState, goTo: (Int) -> Unit, modifier: Modifier) {
    val layout = LocalLayout.current
    Row(
        modifier.fillMaxSize().padding(horizontal = layout.gutter, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            Modifier
                .weight(0.42f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(32.dp))
                .background(EagleColors.Stage)
                .verticalScroll(rememberScrollState())
                .padding(36.dp),
        ) {
            Pitch(wide = true)
            Spacer(Modifier.height(32.dp))
            CoinLadder(pager.position, pager.currentPage, goTo)
        }
        Box(
            Modifier
                .weight(0.58f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(32.dp))
                .background(EagleColors.Card),
        ) {
            LevelPager(pager, goTo, padding = 40.dp)
        }
    }
}

@Composable
private fun Pitch(wide: Boolean) {
    EText(
        text = "Follow every peso.",
        display = true,
        size = if (wide) 76.sp else 52.sp,
        lineHeight = if (wide) 72.sp else 50.sp,
        color = Color.White,
    )
    Spacer(Modifier.height(10.dp))
    EText("Sundan ang bawat piso.", size = 16.sp, weight = FontWeight.SemiBold, color = EagleColors.Gold)
    Spacer(Modifier.height(10.dp))
    EText(
        text = if (wide) {
            "See where citizens' taxes go, from the barangay to the highest office. " +
                "Every figure ties back to an official public record."
        } else {
            "See where citizens' taxes go, from the barangay to the highest office."
        },
        modifier = Modifier.widthIn(max = 440.dp),
        size = if (wide) 17.sp else 15.sp,
        lineHeight = if (wide) 26.sp else 22.sp,
        color = Color.White.copy(alpha = 0.88f),
    )
}

/** The gold peso that sits on the current level. */
@Composable
private fun Coin(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(CoinSize)
            .background(EagleColors.Stage, CircleShape)
            .padding(3.dp)
            .background(EagleColors.Gold, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        EText("₱", size = 19.sp, weight = FontWeight.SemiBold, color = EagleColors.Ink)
    }
}

/** Horizontal rail for phones: six stops, labelled, with the coin riding on top. */
@Composable
private fun CoinRail(position: Float, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    // The one moment of motion on load: the coin drops onto the rail.
    val drop = remember { Animatable(1f) }
    LaunchedEffect(Unit) { drop.animateTo(0f, spring(dampingRatio = 0.45f, stiffness = 140f)) }

    BoxWithConstraints(modifier.fillMaxWidth().height(82.dp)) {
        val slot = with(density) { maxWidth.toPx() } / trail.size
        val trackY = with(density) { 22.dp.toPx() }
        val coinX = slot * (position + 0.5f)
        val coinPx = with(density) { CoinSize.toPx() }
        val dropPx = with(density) { 70.dp.toPx() }

        Canvas(Modifier.matchParentSize()) {
            val line = 4.dp.toPx()
            drawLine(EagleColors.StageLift, Offset(slot / 2, trackY), Offset(size.width - slot / 2, trackY), line, StrokeCap.Round)
            drawLine(Color.White, Offset(slot / 2, trackY), Offset(coinX, trackY), line, StrokeCap.Round)
            trail.indices.forEach { i ->
                val x = slot * (i + 0.5f)
                val lit = x <= coinX + 1f
                drawCircle(if (lit) Color.White else EagleColors.StageLift, 6.dp.toPx(), Offset(x, trackY))
                if (!lit) drawCircle(EagleColors.Stage, 3.dp.toPx(), Offset(x, trackY))
            }
        }

        Row(Modifier.matchParentSize()) {
            trail.forEachIndexed { index, tier ->
                val isSelected = index == selected
                val shape = RoundedCornerShape(10.dp)
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .focusRing(shape, Color.White)
                        .selectable(
                            selected = isSelected,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelect(index) },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(48.dp))
                    EText(
                        text = tier.short,
                        display = true,
                        weight = FontWeight.SemiBold,
                        size = 15.sp,
                        maxLines = 1,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                    )
                }
            }
        }

        Coin(
            Modifier
                .offset { IntOffset((coinX - coinPx / 2).roundToInt(), (trackY - coinPx / 2 - dropPx * drop.value).roundToInt()) }
                .graphicsLayer { alpha = (1f - drop.value).coerceIn(0f, 1f) },
        )
    }
}

/** Vertical rail for wide windows: the six levels as a ladder, bottom to top order kept as listed. */
@Composable
private fun CoinLadder(position: Float, selected: Int, onSelect: (Int) -> Unit) {
    val density = LocalDensity.current
    val rowHeight = 54.dp
    val drop = remember { Animatable(1f) }
    LaunchedEffect(Unit) { drop.animateTo(0f, spring(dampingRatio = 0.45f, stiffness = 140f)) }

    val rowPx = with(density) { rowHeight.toPx() }
    val trackX = with(density) { 22.dp.toPx() }
    val coinPx = with(density) { CoinSize.toPx() }
    val coinY = rowPx * (position + 0.5f)

    Box(Modifier.fillMaxWidth().height(rowHeight * trail.size)) {
        Canvas(Modifier.matchParentSize()) {
            val line = 4.dp.toPx()
            drawLine(EagleColors.StageLift, Offset(trackX, rowPx / 2), Offset(trackX, size.height - rowPx / 2), line, StrokeCap.Round)
            drawLine(Color.White, Offset(trackX, rowPx / 2), Offset(trackX, coinY), line, StrokeCap.Round)
            trail.indices.forEach { i ->
                val y = rowPx * (i + 0.5f)
                val lit = y <= coinY + 1f
                drawCircle(if (lit) Color.White else EagleColors.StageLift, 6.dp.toPx(), Offset(trackX, y))
                if (!lit) drawCircle(EagleColors.Stage, 3.dp.toPx(), Offset(trackX, y))
            }
        }
        Column(Modifier.matchParentSize()) {
            trail.forEachIndexed { index, tier ->
                val isSelected = index == selected
                val shape = RoundedCornerShape(12.dp)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .focusRing(shape, Color.White)
                        .selectable(
                            selected = isSelected,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelect(index) },
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.width(56.dp))
                    EText(
                        text = tier.name,
                        display = true,
                        weight = FontWeight.SemiBold,
                        size = 24.sp,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                    )
                }
            }
        }
        Coin(
            Modifier
                .offset { IntOffset((trackX - coinPx / 2).roundToInt(), (coinY - coinPx / 2).roundToInt()) }
                .graphicsLayer { alpha = (1f - drop.value).coerceIn(0f, 1f) },
        )
    }
}

@Composable
private fun LevelPager(pager: PagerState, goTo: (Int) -> Unit, padding: Dp) {
    HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), key = { trail[it].name }) { page ->
        val tier = trail[page]
        val last = page == trail.lastIndex
        LevelPage(
            tier = tier,
            active = page == pager.currentPage,
            nextLabel = if (last) "Start again at ${trail.first().short}" else "Next: ${trail[page + 1].short}",
            onNext = { goTo(if (last) 0 else page + 1) },
            padding = padding,
        )
    }
}

@Composable
private fun LevelPage(
    tier: Tier,
    active: Boolean,
    nextLabel: String,
    onNext: () -> Unit,
    padding: Dp,
) {
    val wide = LocalLayout.current.wide
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = padding, vertical = if (wide) 36.dp else 24.dp),
    ) {
        EText(
            text = tier.name,
            display = true,
            size = if (wide) 52.sp else 40.sp,
            lineHeight = if (wide) 54.sp else 42.sp,
        )
        Spacer(Modifier.height(8.dp))
        EText(
            text = tier.tagline,
            modifier = Modifier.widthIn(max = 560.dp),
            size = 16.sp,
            lineHeight = 25.sp,
            color = EagleColors.Body,
        )

        tier.sample?.let { sample ->
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                EText("Where ₱100 goes", size = 18.sp, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                SampleBadge()
            }
            Spacer(Modifier.height(14.dp))
            sample.forEach { allocation ->
                AllocationRow(allocation, active)
                Spacer(Modifier.height(14.dp))
            }
            EText(
                text = "Placeholder figures for design purposes. They are not real government data.",
                size = 13.sp,
                lineHeight = 19.sp,
                color = EagleColors.Muted,
            )
        }

        Spacer(Modifier.height(24.dp))
        EText("What you'll be able to see", size = 18.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        tier.reveals.forEach { item ->
            Row(Modifier.padding(bottom = 8.dp)) {
                Box(Modifier.padding(top = 9.dp).size(6.dp).background(EagleColors.Blue, CircleShape))
                Spacer(Modifier.width(12.dp))
                EText(item, size = 15.sp, lineHeight = 23.sp, color = EagleColors.Ink)
            }
        }
        Spacer(Modifier.height(4.dp))
        EText("Sources: ${tier.sources}", size = 13.sp, lineHeight = 19.sp, color = EagleColors.Muted)

        Spacer(Modifier.height(28.dp))
        Pill(nextLabel, onClick = onNext)
    }
}

@Composable
private fun AllocationRow(allocation: Allocation, active: Boolean) {
    val fill by animateFloatAsState(
        targetValue = if (active) allocation.pesos / BarScale else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "allocation-bar",
    )
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EText(allocation.label, modifier = Modifier.weight(1f), size = 16.sp)
            EText("₱${allocation.pesos}", size = 16.sp, weight = FontWeight.SemiBold, color = EagleColors.Blue)
        }
        Spacer(Modifier.height(6.dp))
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
                    .background(EagleColors.Blue),
            )
        }
    }
}
