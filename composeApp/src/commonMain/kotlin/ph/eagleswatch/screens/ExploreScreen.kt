package ph.eagleswatch.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import ph.eagleswatch.content.Category
import ph.eagleswatch.content.Day
import ph.eagleswatch.content.Level
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.Status
import ph.eagleswatch.data.Dataset
import ph.eagleswatch.data.LoadState
import ph.eagleswatch.data.ProjectStore
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.eagle_logo
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.state.PlatformBackHandler
import ph.eagleswatch.ui.Button
import ph.eagleswatch.ui.ButtonStyle
import ph.eagleswatch.ui.Card
import ph.eagleswatch.ui.ChipSurface
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleMetrics
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.EmptyState
import ph.eagleswatch.ui.ErrorState
import ph.eagleswatch.ui.FilterChip
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.IconButton
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.Notice
import ph.eagleswatch.ui.NoticeTone
import ph.eagleswatch.ui.SampleTag
import ph.eagleswatch.ui.SearchField
import ph.eagleswatch.ui.SectionLabel
import ph.eagleswatch.ui.SkeletonRows
import ph.eagleswatch.ui.StatusMark
import ph.eagleswatch.ui.Wrap
import ph.eagleswatch.ui.compactPesos
import ph.eagleswatch.ui.consumeTouches
import ph.eagleswatch.ui.fill
import ph.eagleswatch.ui.LocalBottomSpace
import ph.eagleswatch.ui.LocalChrome
import ph.eagleswatch.ui.aurora
import ph.eagleswatch.ui.glass

/** Approved contract amount bands for the filter. */
enum class AmountBand(val label: String, private val min: Long, private val max: Long?) {
    Under5M("Under ₱5M", 0, 5_000_000),
    From5To25M("₱5M to ₱25M", 5_000_000, 25_000_000),
    From25To100M("₱25M to ₱100M", 25_000_000, 100_000_000),
    Over100M("₱100M and above", 100_000_000, null),
    ;

    fun contains(amount: Long?): Boolean = amount != null && amount >= min && (max == null || amount < max)
}

enum class Sort(val label: String) {
    LatestUpdate("Latest update"),
    LargestContract("Largest contract"),
    BehindSchedule("Most behind schedule"),
    Title("Title, A to Z"),
}

/** Which projects the citizen has narrowed the list to. Empty sets mean "any". */
@Immutable
data class Filters(
    val query: String = "",
    val statuses: Set<Status> = emptySet(),
    val categories: Set<Category> = emptySet(),
    val levels: Set<Level> = emptySet(),
    val provinces: Set<String> = emptySet(),
    val agencies: Set<String> = emptySet(),
    val bands: Set<AmountBand> = emptySet(),
) {
    /** Filters set in the sheet. Status has its own strip, so it is not counted here. */
    val sheetCount: Int get() = categories.size + levels.size + provinces.size + agencies.size + bands.size

    fun matches(p: Project): Boolean =
        p.matches(query) &&
            (statuses.isEmpty() || p.status in statuses) &&
            (categories.isEmpty() || p.category in categories) &&
            (levels.isEmpty() || p.level in levels) &&
            (provinces.isEmpty() || p.place.province in provinces) &&
            (agencies.isEmpty() || p.implementingAgency?.name in agencies) &&
            (bands.isEmpty() || bands.any { it.contains(p.contract.amount) })

    fun clearSheet(): Filters = copy(categories = emptySet(), levels = emptySet(), provinces = emptySet(), agencies = emptySet(), bands = emptySet())
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value

/** One applied sheet filter, shown as a removable chip above the list. */
private class Applied(val label: String, val remove: (Filters) -> Filters)

private fun Filters.applied(): List<Applied> =
    categories.map { c -> Applied(c.label) { it.copy(categories = it.categories - c) } } +
        levels.map { l -> Applied(l.label) { it.copy(levels = it.levels - l) } } +
        provinces.map { p -> Applied(p) { it.copy(provinces = it.provinces - p) } } +
        agencies.map { a -> Applied(a) { it.copy(agencies = it.agencies - a) } } +
        bands.map { b -> Applied(b.label) { it.copy(bands = it.bands - b) } }

fun sortProjects(list: List<Project>, sort: Sort, today: Day): List<Project> = when (sort) {
    Sort.LatestUpdate -> list.sortedByDescending { it.latestUpdate?.date?.epochDay ?: Long.MIN_VALUE }
    Sort.LargestContract -> list.sortedByDescending { it.contract.amount ?: -1L }
    Sort.BehindSchedule -> list.sortedByDescending { scheduleGap(it, today) ?: Float.NEGATIVE_INFINITY }
    Sort.Title -> list.sortedBy { it.title.lowercase() }
}

/**
 * Every project record, searchable and filterable. The header answers how much public money the listed
 * projects carry and how they stand; the cards below answer what, where, who, how far along, and how sure.
 */
@Composable
fun ExploreScreen(
    store: ProjectStore,
    citizen: CitizenState,
    onOpen: (Project) -> Unit,
    onReport: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = LocalLayout.current
    val today = remember { Day.today() }
    var filters by remember { mutableStateOf(Filters()) }
    var sort by remember { mutableStateOf(Sort.LatestUpdate) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var showSort by rememberSaveable { mutableStateOf(false) }
    val state = store.state
    val all = store.projects
    val shown = remember(all, filters, sort) { sortProjects(all.filter(filters::matches), sort, today) }
    val dataset = (state as? LoadState.Ready)?.dataset
    val columns = if (layout.wide) 2 else 1

    PlatformBackHandler(enabled = showFilters) { showFilters = false }

    // The filter sheet has its own bottom button, so the floating tab bar steps aside while it is open.
    val chrome = LocalChrome.current
    LaunchedEffect(showFilters) { chrome.tabBarHidden = showFilters }
    DisposableEffect(Unit) { onDispose { chrome.tabBarHidden = false } }

    Box(modifier.fillMaxSize().background(EagleColors.Canvas)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = LocalBottomSpace.current + 8.dp)) {
            item(key = "hero") {
                Hero(
                    all = all,
                    shown = shown,
                    dataset = dataset,
                    filters = filters,
                    onFilters = { filters = it },
                    onOpenFilters = { showFilters = true },
                    onReport = onReport,
                )
            }
            when (state) {
                LoadState.Loading -> item(key = "loading") { LoadingCards() }
                is LoadState.Failed -> item(key = "failed") {
                    ContentColumn {
                        ErrorState("The records could not be loaded", state.message, onRetry = store::retry, modifier = Modifier.padding(horizontal = layout.gutter))
                    }
                }
                is LoadState.Ready -> {
                    if (state.dataset.sample) {
                        item(key = "sample") {
                            ContentColumn(max = if (layout.wide) 1200.dp else EagleMetrics.ContentMax) {
                                Notice(
                                    text = state.dataset.note,
                                    tone = NoticeTone.Sample,
                                    actionLabel = "How records are labelled",
                                    onAction = onAbout,
                                    modifier = Modifier.padding(horizontal = layout.gutter).padding(top = 16.dp),
                                )
                            }
                        }
                    }
                    item(key = "listhead") {
                        ContentColumn(max = if (layout.wide) 1200.dp else EagleMetrics.ContentMax) {
                            ListHead(shown.size, all.size, filters, sort, showSort, onToggleSort = { showSort = !showSort }, onSort = { sort = it; showSort = false }, onFilters = { filters = it })
                        }
                    }
                    if (shown.isEmpty()) {
                        item(key = "empty") {
                            ContentColumn {
                                EmptyState(
                                    glyph = Glyph.Search,
                                    title = "No projects match",
                                    body = if (filters.query.isNotBlank()) {
                                        "Nothing matches \"${filters.query.trim()}\" with these filters. Try a municipality, a province or an agency."
                                    } else {
                                        "No records match these filters. Clear some of them to see more."
                                    },
                                    modifier = Modifier.padding(horizontal = layout.gutter),
                                    actionLabel = "Clear search and filters",
                                    onAction = { filters = Filters() },
                                )
                            }
                        }
                    }
                    items(shown.chunked(columns), key = { row -> row.joinToString("|") { it.id } }) { row ->
                        ContentColumn(max = if (layout.wide) 1200.dp else EagleMetrics.ContentMax) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .padding(horizontal = layout.gutter)
                                    .padding(bottom = EagleMetrics.CardGap),
                                horizontalArrangement = Arrangement.spacedBy(EagleMetrics.CardGap),
                            ) {
                                row.forEach { project ->
                                    ProjectCard(
                                        project = project,
                                        today = today,
                                        watching = citizen.isWatching(project.id),
                                        onOpen = { onOpen(project) },
                                        onToggleWatch = { citizen.toggleWatch(project.id) },
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                    )
                                }
                                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                    item(key = "end") {
                        EText(
                            text = "${state.dataset.name} · ${state.dataset.projects.size} records · as of ${state.dataset.asOf.format()}",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = layout.gutter, vertical = 8.dp),
                            size = EagleType.Caption,
                            color = EagleColors.Muted,
                            align = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showFilters,
            enter = fadeIn() + slideInVertically { it / 5 },
            exit = fadeOut() + slideOutVertically { it / 5 },
        ) {
            FilterSheet(all = all, filters = filters, onChange = { filters = it }, onClose = { showFilters = false })
        }
    }
}

/** The dark band: brand, the headline figure, the status distribution as a filter, and search. */
@Composable
private fun Hero(
    all: List<Project>,
    shown: List<Project>,
    dataset: Dataset?,
    filters: Filters,
    onFilters: (Filters) -> Unit,
    onOpenFilters: () -> Unit,
    onReport: () -> Unit,
) {
    val layout = LocalLayout.current
    val amounts = shown.mapNotNull { it.contract.amount }
    val missing = shown.size - amounts.size
    val provinces = shown.map { it.place.province }.distinct().size
    Column(
        Modifier
            .fillMaxWidth()
            .aurora()
            .then(if (layout.wide) Modifier else Modifier.windowInsetsPadding(WindowInsets.statusBars))
            .padding(bottom = 20.dp),
    ) {
        ContentColumn(max = if (layout.wide) 1200.dp else EagleMetrics.ContentMax) {
            Column {
                Column(Modifier.padding(horizontal = layout.gutter)) {
                    if (!layout.wide) {
                        Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                            Image(painterResource(Res.drawable.eagle_logo), contentDescription = null, modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)))
                            Spacer(Modifier.width(10.dp))
                            EText("The Eagle's Watch", modifier = Modifier.weight(1f), display = true, size = 18.sp, weight = FontWeight.SemiBold, color = EagleColors.OnNight, heading = true)
                            IconButton(Glyph.Flag, "Report a problem", onClick = onReport, label = "Report", iconSize = 18.dp, tint = EagleColors.OnNight, filled = Color.White.copy(alpha = 0.12f))
                        }
                    }
                    Spacer(Modifier.height(if (layout.wide) 28.dp else 12.dp))
                    // The headline figure in a glass panel, like a widget on a home screen.
                    Column(Modifier.fillMaxWidth().glass(RoundedCornerShape(24.dp)).padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EText("Approved contract value", size = EagleType.Label, weight = FontWeight.Medium, color = EagleColors.OnNightMuted)
                        if (dataset?.sample == true) {
                            Spacer(Modifier.width(8.dp))
                            SampleTag(onDark = true)
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    EText(
                        text = if (dataset == null) "—" else if (amounts.isEmpty()) "₱0" else compactPesos(amounts.sum()),
                        display = true,
                        size = EagleType.Hero,
                        lineHeight = 46.sp,
                        color = EagleColors.OnNight,
                        tabular = true,
                    )
                    EText(
                        text = if (dataset == null) "Loading records…" else buildString {
                            append(if (shown.size == all.size) "${shown.size} projects" else "${shown.size} of ${all.size} projects")
                            append(" · $provinces ${if (provinces == 1) "province" else "provinces"}")
                            if (missing > 0) append(" · $missing without an amount")
                        },
                        size = EagleType.Secondary,
                        lineHeight = 20.sp,
                        color = EagleColors.OnNightMuted,
                        tabular = true,
                    )
                    if (dataset != null) {
                        EText("Data as of ${dataset.asOf.format()}", size = EagleType.Caption, lineHeight = 17.sp, color = EagleColors.OnNightMuted)
                    }
                    if (all.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        StatusDistribution(all)
                    }
                    }
                }
                Spacer(Modifier.height(8.dp))
                StatusStrip(all, filters, onFilters)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.padding(horizontal = layout.gutter), verticalAlignment = Alignment.CenterVertically) {
                    SearchField(
                        query = filters.query,
                        onQuery = { onFilters(filters.copy(query = it)) },
                        modifier = Modifier.weight(1f).widthIn(max = 640.dp),
                        placeholder = "Search projects, places, agencies",
                        description = "Search projects",
                        onDark = true,
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        glyph = Glyph.Filter,
                        description = if (filters.sheetCount > 0) "More filters, ${filters.sheetCount} applied" else "More filters",
                        onClick = onOpenFilters,
                        label = if (filters.sheetCount > 0) "${filters.sheetCount}" else null,
                        tint = if (filters.sheetCount > 0) EagleColors.Night else EagleColors.OnNight,
                        iconSize = 20.dp,
                        filled = if (filters.sheetCount > 0) EagleColors.AccentBright else Color.White.copy(alpha = 0.12f),
                    )
                }
            }
        }
    }
}

/** How the listed projects split by status, as one segmented bar. Counts are in the chips below it. */
@Composable
private fun StatusDistribution(all: List<Project>) {
    val counts = Status.entries.map { s -> s to all.count { it.status == s } }.filter { it.second > 0 }
    val description = "Status of all projects: " + counts.joinToString(", ") { (s, n) -> "$n ${s.label.lowercase()}" }
    Row(
        Modifier.fillMaxWidth().height(10.dp).semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        counts.forEach { (status, n) ->
            Box(Modifier.weight(n.toFloat()).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(status.fill))
        }
    }
}

/** Status is the first thing a citizen narrows by, so it lives on the header as a row of counted chips. */
@Composable
private fun StatusStrip(all: List<Project>, filters: Filters, onFilters: (Filters) -> Unit) {
    val layout = LocalLayout.current
    LazyRow(contentPadding = PaddingValues(horizontal = layout.gutter), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item(key = "all") {
            FilterChip(
                label = "All",
                selected = filters.statuses.isEmpty(),
                onClick = { onFilters(filters.copy(statuses = emptySet())) },
                count = all.size,
                role = Role.RadioButton,
                surface = ChipSurface.Dark,
            )
        }
        items(Status.entries, key = { it.name }) { status ->
            val n = all.count { it.status == status }
            FilterChip(
                label = status.label,
                selected = status in filters.statuses,
                onClick = { onFilters(filters.copy(statuses = filters.statuses.toggle(status))) },
                count = n,
                enabled = n > 0 || status in filters.statuses,
                surface = ChipSurface.Dark,
                leading = { StatusMark(status, size = 10.dp, color = status.fill) },
            )
        }
    }
}

@Composable
private fun ListHead(
    shownCount: Int,
    total: Int,
    filters: Filters,
    sort: Sort,
    showSort: Boolean,
    onToggleSort: () -> Unit,
    onSort: (Sort) -> Unit,
    onFilters: (Filters) -> Unit,
) {
    val layout = LocalLayout.current
    Column(Modifier.fillMaxWidth().padding(horizontal = layout.gutter).padding(top = 16.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EText(
                if (shownCount == total) "All projects" else "$shownCount matching",
                modifier = Modifier.weight(1f),
                display = true,
                size = 20.sp,
                weight = FontWeight.SemiBold,
                heading = true,
            )
            Button(sort.label, onClick = onToggleSort, style = ButtonStyle.Quiet, compact = true, glyph = Glyph.Sort, trailingGlyph = Glyph.ChevronDown)
        }
        AnimatedVisibility(visible = showSort) {
            Wrap(spacing = 6.dp) {
                Sort.entries.forEach { option ->
                    FilterChip(option.label, selected = option == sort, onClick = { onSort(option) }, role = Role.RadioButton)
                }
            }
        }
        val applied = filters.applied()
        if (applied.isNotEmpty()) {
            Wrap(spacing = 6.dp) {
                applied.forEach { chip ->
                    FilterChip(
                        label = chip.label,
                        selected = true,
                        onClick = { onFilters(chip.remove(filters)) },
                        role = Role.Button,
                        leading = { Icon(Glyph.Close, tint = EagleColors.Page, size = 12.dp, weight = 2.6f) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingCards() {
    val layout = LocalLayout.current
    // Near-instant loads show nothing; the skeleton appears only when the wait is long enough to notice.
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        show = true
    }
    if (show) ContentColumn { SkeletonRows(3, Modifier.padding(horizontal = layout.gutter).padding(top = 16.dp)) }
}

@Composable
private fun FilterSheet(all: List<Project>, filters: Filters, onChange: (Filters) -> Unit, onClose: () -> Unit) {
    val layout = LocalLayout.current
    val count = all.count(filters::matches)
    Column(Modifier.fillMaxSize().background(EagleColors.Canvas).consumeTouches()) {
        PageBar(
            onBack = onClose,
            title = "Filters",
            backGlyph = Glyph.Close,
            backDescription = "Close filters",
            actions = {
                if (filters.sheetCount > 0) Button("Clear", onClick = { onChange(filters.clearSheet()) }, style = ButtonStyle.OnDark, compact = true, modifier = Modifier.padding(end = 8.dp))
            },
        )
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            ContentColumn {
                Column(Modifier.padding(horizontal = layout.gutter, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(EagleMetrics.CardGap)) {
                    Facet("Type of project", Category.entries, { it.label }, filters.categories, { c -> all.count { filters.copy(categories = setOf(c)).matches(it) } }) {
                        onChange(filters.copy(categories = filters.categories.toggle(it)))
                    }
                    Facet("Level of government", Level.entries, { it.label }, filters.levels, { l -> all.count { filters.copy(levels = setOf(l)).matches(it) } }) {
                        onChange(filters.copy(levels = filters.levels.toggle(it)))
                    }
                    Facet("Province", all.map { it.place.province }.distinct().sorted(), { it }, filters.provinces, { p -> all.count { filters.copy(provinces = setOf(p)).matches(it) } }) {
                        onChange(filters.copy(provinces = filters.provinces.toggle(it)))
                    }
                    Facet("Implementing agency", all.mapNotNull { it.implementingAgency?.name }.distinct().sorted(), { it }, filters.agencies, { a -> all.count { filters.copy(agencies = setOf(a)).matches(it) } }) {
                        onChange(filters.copy(agencies = filters.agencies.toggle(it)))
                    }
                    Facet("Approved contract amount", AmountBand.entries, { it.label }, filters.bands, { b -> all.count { filters.copy(bands = setOf(b)).matches(it) } }) {
                        onChange(filters.copy(bands = filters.bands.toggle(it)))
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().background(EagleColors.Page).windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = layout.gutter, vertical = 12.dp), contentAlignment = Alignment.Center) {
            Button(
                label = when (count) {
                    0 -> "No projects match"
                    1 -> "Show 1 project"
                    else -> "Show $count projects"
                },
                onClick = onClose,
                style = ButtonStyle.Primary,
                fillWidth = true,
                modifier = Modifier.widthIn(max = EagleMetrics.ContentMax),
            )
        }
    }
}

/** One facet in its own card: a label and a wrap of counted chips. Options with no matches stay visible but disabled. */
@Composable
private fun <T> Facet(
    title: String,
    options: List<T>,
    label: (T) -> String,
    selected: Set<T>,
    count: (T) -> Int,
    onToggle: (T) -> Unit,
) {
    Card {
        SectionLabel(title)
        Spacer(Modifier.height(4.dp))
        Wrap(spacing = 6.dp) {
            options.forEach { option ->
                val n = count(option)
                FilterChip(label(option), selected = option in selected, onClick = { onToggle(option) }, count = n, enabled = n > 0 || option in selected)
            }
        }
    }
}
