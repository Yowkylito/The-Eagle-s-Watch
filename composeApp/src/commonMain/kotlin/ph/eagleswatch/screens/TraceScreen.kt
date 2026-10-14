package ph.eagleswatch.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import ph.eagleswatch.content.trail
import ph.eagleswatch.state.TraceProgress
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.drawSun
import ph.eagleswatch.ui.focusRing

private val CoinSize = 40.dp

/** How far the sun turns for each level the coin climbs. */
private const val SunTurnPerLevel = 22.5f

/**
 * The whole product in one screen: the citizen sets the pesos they paid, and a peso coin carries them up a
 * rail of six levels, from the citizen to the President. Swipe the card, tap a level or press Next, and
 * the coin follows.
 */
@Composable
fun TraceScreen(pager: PagerState, progress: TraceProgress, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val goTo: (Int) -> Unit = { page -> scope.launch { pager.animateScrollToPage(page) } }
    LaunchedEffect(pager, progress) {
        snapshotFlow { pager.settledPage }.collect { progress.visit(it) }
    }
    // The one orchestrated moment on load: the sun's rays reach out while the coin drops onto the rail.
    val rays = remember { Animatable(0f) }
    LaunchedEffect(Unit) { rays.animateTo(1f, tween(durationMillis = 1600, easing = FastOutSlowInEasing)) }

    if (LocalLayout.current.wide) {
        WideTrace(pager, progress, goTo, { rays.value }, modifier)
    } else {
        PhoneTrace(pager, progress, goTo, { rays.value }, modifier)
    }
}

/** Where the coin is, in level units. It moves continuously while a swipe is in progress. */
private val PagerState.position: Float
    get() = currentPage + currentPageOffsetFraction

@Composable
private fun PhoneTrace(
    pager: PagerState,
    progress: TraceProgress,
    goTo: (Int) -> Unit,
    rays: () -> Float,
    modifier: Modifier,
) {
    val layout = LocalLayout.current
    val collapse = remember { HeadlineCollapse() }
    Column(
        modifier
            .fillMaxSize()
            .background(EagleColors.Stage)
            .nestedScroll(collapse),
    ) {
        Box(Modifier.stageSun(pager, rays, center = DpOffset((-24).dp, 36.dp), radius = 160.dp)) {
            Column(Modifier.padding(horizontal = layout.gutter)) {
                Collapsible(collapse) {
                    Column {
                        Spacer(Modifier.height(4.dp))
                        Pitch(wide = false)
                        Spacer(Modifier.height(18.dp))
                    }
                }
                CoinRail(pager.position, pager.currentPage, progress.visited, goTo)
                Spacer(Modifier.height(4.dp))
            }
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(EagleColors.Sheet),
        ) {
            LevelPager(pager, progress, goTo, padding = layout.gutter)
        }
    }
}

@Composable
private fun WideTrace(
    pager: PagerState,
    progress: TraceProgress,
    goTo: (Int) -> Unit,
    rays: () -> Float,
    modifier: Modifier,
) {
    val layout = LocalLayout.current
    Row(
        modifier.fillMaxSize().padding(horizontal = layout.gutter, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(
            Modifier
                .weight(0.42f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(32.dp))
                .background(EagleColors.Stage)
                .stageSun(pager, rays, center = DpOffset((-36).dp, 48.dp), radius = 220.dp),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(36.dp),
            ) {
                Pitch(wide = true)
                Spacer(Modifier.height(32.dp))
                CoinLadder(pager.position, pager.currentPage, progress.visited, goTo)
            }
        }
        Box(
            Modifier
                .weight(0.58f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(32.dp))
                .background(EagleColors.Card),
        ) {
            LevelPager(pager, progress, goTo, padding = 40.dp)
        }
    }
}

/**
 * The flag's sun behind the pitch, drawn without taking up space. It turns a little with every level the
 * coin climbs. [center] is measured from the top-right corner.
 */
private fun Modifier.stageSun(pager: PagerState, rays: () -> Float, center: DpOffset, radius: Dp): Modifier =
    drawBehind {
        drawSun(
            center = Offset(size.width + center.x.toPx(), center.y.toPx()),
            radius = radius.toPx(),
            turn = pager.position * SunTurnPerLevel,
            grow = rays(),
            color = Color.White.copy(alpha = 0.08f),
        )
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
    // Phones keep the stage short so the citizen's own pesos show on first load; the rail says the rest.
    if (wide) {
        Spacer(Modifier.height(10.dp))
        EText(
            text = "See where your taxes go, from the barangay to the highest office. " +
                "Every figure will tie back to an official public record.",
            modifier = Modifier.widthIn(max = 440.dp),
            size = 17.sp,
            lineHeight = 26.sp,
            color = Color.White.copy(alpha = 0.88f),
        )
    }
}

/**
 * Lets the pitch above the rail fold away while the level card scrolls up, and unfold when the card is
 * pulled back down from its top, so phones give the coins the room.
 */
@Stable
private class HeadlineCollapse : NestedScrollConnection {
    /** Full height of the folding part, in px. */
    var range by mutableFloatStateOf(0f)
        private set

    /** How much of it is folded away, in px. */
    var folded by mutableFloatStateOf(0f)
        private set

    val fraction: Float
        get() = if (range > 0f) folded / range else 0f

    fun updateRange(height: Float) {
        range = height
        folded = folded.coerceAtMost(height)
    }

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (available.y >= 0f) return Offset.Zero
        val next = (folded - available.y).coerceAtMost(range)
        val used = next - folded
        folded = next
        return Offset(0f, -used)
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (available.y <= 0f) return Offset.Zero
        val next = (folded - available.y).coerceAtLeast(0f)
        val used = folded - next
        folded = next
        return Offset(0f, used)
    }
}

@Composable
private fun Collapsible(collapse: HeadlineCollapse, content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clipToBounds()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
                val shown = (placeable.height - collapse.folded).roundToInt().coerceIn(0, placeable.height)
                layout(placeable.width, shown) { placeable.placeRelative(0, shown - placeable.height) }
            },
    ) {
        Box(
            Modifier
                .onSizeChanged { collapse.updateRange(it.height.toFloat()) }
                .graphicsLayer { alpha = (1f - collapse.fraction * 1.5f).coerceIn(0f, 1f) },
        ) {
            content()
        }
    }
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

/**
 * A stop on the rail. Stops behind the coin are white; stops the citizen has already reached keep a gold
 * centre, so the rail doubles as a record of how far they have explored.
 */
private fun DrawScope.drawStop(center: Offset, lit: Boolean, visited: Boolean) {
    drawCircle(if (lit) Color.White else EagleColors.StageLift, 6.dp.toPx(), center)
    when {
        visited -> drawCircle(EagleColors.Gold, 3.5.dp.toPx(), center)
        !lit -> drawCircle(EagleColors.Stage, 3.dp.toPx(), center)
    }
}

/** Horizontal rail for phones: six stops, labelled, with the coin riding on top. */
@Composable
private fun CoinRail(
    position: Float,
    selected: Int,
    visited: Set<Int>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
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
                drawStop(Offset(x, trackY), lit = x <= coinX + 1f, visited = i in visited)
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

/** Vertical rail for wide windows: the six levels as a ladder, in the same order as the phone rail. */
@Composable
private fun CoinLadder(position: Float, selected: Int, visited: Set<Int>, onSelect: (Int) -> Unit) {
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
                drawStop(Offset(trackX, y), lit = y <= coinY + 1f, visited = i in visited)
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
private fun LevelPager(pager: PagerState, progress: TraceProgress, goTo: (Int) -> Unit, padding: Dp) {
    HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), key = { trail[it].name }) { page ->
        if (page == 0) {
            CitizensPage(progress = progress, goTo = goTo, padding = padding)
        } else {
            LevelPage(level = page, progress = progress, goTo = goTo, padding = padding)
        }
    }
}
