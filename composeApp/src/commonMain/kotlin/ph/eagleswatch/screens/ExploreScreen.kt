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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.Role
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
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleMetrics
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.EmptyState
import ph.eagleswatch.ui.ErrorState
import ph.eagleswatch.ui.FilterChip
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Hairline
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.IconButton
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.Notice
import ph.eagleswatch.ui.NoticeTone
import ph.eagleswatch.ui.SampleTag
import ph.eagleswatch.ui.SearchField
import ph.eagleswatch.ui.SectionLabel
import ph.eagleswatch.ui.SkeletonRows
import ph.eagleswatch.ui.Wrap
import ph.eagleswatch.ui.compactPesos
import ph.eagleswatch.ui.hairlineBelow

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
    val facetCount: Int get() = statuses.size + categories.size + levels.size + provinces.size + agencies.size + bands.size
    val isDefault: Boolean get() = query.isBlank() && facetCount == 0

    fun matches(p: Project): Boolean =
        p.matches(query) &&
            (statuses.isEmpty() || p.status in statuses) &&
            (categories.isEmpty() || p.category in categories) &&
            (levels.isEmpty() || p.level in levels) &&
            (provinces.isEmpty() || p.place.province in provinces) &&
            (agencies.isEmpty() || p.implementingAgency?.name in agencies) &&
            (bands.isEmpty() || bands.any { it.contains(p.contract.amount) })

    fun clearFacets(): Filters = Filters(query = query)
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value

/** One applied facet value, shown as a removable chip under the search box. */
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
 * Every project record, searchable and filterable. The first screen answers: which projects, where, how
 * much public money, what status, when last updated, and how to get at the evidence.
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

    PlatformBackHandler(enabled = showFilters) { showFilters = false }

    Box(modifier.fillMaxSize().background(EagleColors.Page)) {
        Column(Modifier.fillMaxSize()) {
            ExploreHeader(
                all = all,
                filters = filters,
                onFilters = { filters = it },
                onOpenFilters = { showFilters = true },
                onReport = onReport,
            )
            when (state) {
                LoadState.Loading -> LoadingList()
                is LoadState.Failed -> ContentColumn {
                    ErrorState(
                        title = "The records could not be loaded",
                        body = state.message,
                        onRetry = store::retry,
                        modifier = Modifier.padding(horizontal = layout.gutter),
                    )
                }
                is LoadState.Ready -> Results(
                    dataset = state.dataset,
                    shown = shown,
                    filters = filters,
                    sort = sort,
                    showSort = showSort,
                    onToggleSort = { showSort = !showSort },
                    onSort = { sort = it; showSort = false },
                    onClear = { filters = Filters() },
                    today = today,
                    citizen = citizen,
                    onOpen = onOpen,
                    onAbout = onAbout,
                )
            }
        }

        AnimatedVisibility(
            visible = showFilters,
            enter = fadeIn() + slideInVertically { it / 4 },
            exit = fadeOut() + slideOutVertically { it / 4 },
        ) {
            FilterSheet(all = all, filters = filters, onChange = { filters = it }, onClose = { showFilters = false })
        }
    }
}

@Composable
private fun ExploreHeader(
    all: List<Project>,
    filters: Filters,
    onFilters: (Filters) -> Unit,
    onOpenFilters: () -> Unit,
    onReport: () -> Unit,
) {
    val layout = LocalLayout.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(EagleColors.Page)
            .hairlineBelow()
            .then(if (layout.wide) Modifier else Modifier.windowInsetsPadding(WindowInsets.statusBars)),
    ) {
        if (!layout.wide) {
            Row(
                Modifier.fillMaxWidth().height(48.dp).padding(start = layout.gutter, end = layout.gutter - 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.eagle_logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)),
                )
                Spacer(Modifier.width(8.dp))
                EText(
                    "The Eagle's Watch",
                    modifier = Modifier.weight(1f),
                    display = true,
                    size = 20.sp,
                    color = EagleColors.Blue,
                    heading = true,
                )
                IconButton(Glyph.Flag, "Report a problem", onClick = onReport, label = "Report", iconSize = 18.dp)
            }
        }
        ContentColumn {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = layout.gutter)
                        .padding(top = if (layout.wide) 16.dp else 2.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SearchField(
                        query = filters.query,
                        onQuery = { onFilters(filters.copy(query = it)) },
                        modifier = Modifier.weight(1f),
                        placeholder = "Search projects, places, agencies",
                        description = "Search projects",
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        glyph = Glyph.Filter,
                        description = if (filters.facetCount > 0) "Filters, ${filters.facetCount} applied" else "Filters",
                        onClick = onOpenFilters,
                        label = if (filters.facetCount > 0) "Filters · ${filters.facetCount}" else "Filters",
                        iconSize = 18.dp,
                        bordered = true,
                    )
                }
                StatusStrip(all, filters, onFilters)
                val applied = filters.applied()
                if (applied.isNotEmpty()) {
                    Wrap(Modifier.padding(horizontal = layout.gutter).padding(bottom = 4.dp), spacing = 6.dp) {
                        applied.forEach { chip ->
                            FilterChip(
                                label = chip.label,
                                selected = true,
                                onClick = { onFilters(chip.remove(filters)) },
                                role = Role.Button,
                                leading = { Icon(Glyph.Close, tint = EagleColors.Blue, size = 12.dp, weight = 2.6f) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Status is the first thing a citizen narrows by, so it lives in a strip under the search box. */
@Composable
private fun StatusStrip(all: List<Project>, filters: Filters, onFilters: (Filters) -> Unit) {
    val layout = LocalLayout.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = layout.gutter),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item(key = "all") {
            FilterChip(
                label = "All",
                selected = filters.statuses.isEmpty(),
                onClick = { onFilters(filters.copy(statuses = emptySet())) },
                count = all.size,
                role = Role.RadioButton,
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
            )
        }
    }
}

@Composable
private fun LoadingList() {
    val layout = LocalLayout.current
    // Near-instant loads show nothing; the skeleton appears only when the wait is long enough to notice.
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        show = true
    }
    if (show) ContentColumn { SkeletonRows(4, Modifier.padding(horizontal = layout.gutter)) }
}

@Composable
private fun Results(
    dataset: Dataset,
    shown: List<Project>,
    filters: Filters,
    sort: Sort,
    showSort: Boolean,
    onToggleSort: () -> Unit,
    onSort: (Sort) -> Unit,
    onClear: () -> Unit,
    today: Day,
    citizen: CitizenState,
    onOpen: (Project) -> Unit,
    onAbout: () -> Unit,
) {
    val layout = LocalLayout.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (layout.wide) 2 else 1),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        if (dataset.sample) {
            item(key = "sample", span = { GridItemSpan(maxLineSpan) }) {
                Notice(
                    text = dataset.note,
                    modifier = Modifier.padding(horizontal = layout.gutter).padding(top = 8.dp),
                    tone = NoticeTone.Sample,
                    title = "Sample dataset",
                    actionLabel = "About this data",
                    onAction = onAbout,
                )
            }
        }
        item(key = "summary", span = { GridItemSpan(maxLineSpan) }) {
            Summary(dataset, shown, sort, showSort, onToggleSort, onSort)
        }
        if (shown.isEmpty()) {
            item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    glyph = Glyph.Search,
                    title = "No projects match",
                    body = if (filters.query.isNotBlank()) {
                        "Nothing in the ${dataset.projects.size} records matches \"${filters.query.trim()}\" with these filters. " +
                            "Try a municipality, a province or an agency, or clear the filters."
                    } else {
                        "No records match these filters. Clear some of them to see more."
                    },
                    modifier = Modifier.padding(horizontal = layout.gutter),
                    actionLabel = "Clear search and filters",
                    onAction = onClear,
                )
            }
        }
        items(shown, key = { it.id }) { project ->
            ProjectRow(
                project = project,
                today = today,
                watching = citizen.isWatching(project.id),
                onOpen = { onOpen(project) },
                onToggleWatch = { citizen.toggleWatch(project.id) },
            )
        }
        item(key = "end", span = { GridItemSpan(maxLineSpan) }) {
            EText(
                text = "${dataset.name} · ${dataset.projects.size} records · as of ${dataset.asOf.format()}",
                modifier = Modifier.padding(horizontal = layout.gutter, vertical = 12.dp),
                size = EagleType.Caption,
                color = EagleColors.Muted,
            )
        }
    }
}

@Composable
private fun Summary(
    dataset: Dataset,
    shown: List<Project>,
    sort: Sort,
    showSort: Boolean,
    onToggleSort: () -> Unit,
    onSort: (Sort) -> Unit,
) {
    val layout = LocalLayout.current
    val amounts = shown.mapNotNull { it.contract.amount }
    val total = amounts.sum()
    val missing = shown.size - amounts.size
    Column(Modifier.fillMaxWidth().hairlineBelow()) {
        Row(
            Modifier.fillMaxWidth().padding(start = layout.gutter, end = layout.gutter - 8.dp, top = 10.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EText(
                        text = if (shown.size == dataset.projects.size) "${shown.size} projects" else "${shown.size} of ${dataset.projects.size} projects",
                        size = EagleType.Body,
                        weight = FontWeight.SemiBold,
                        tabular = true,
                    )
                    if (dataset.sample) {
                        Spacer(Modifier.width(8.dp))
                        SampleTag()
                    }
                }
                EText(
                    text = buildString {
                        append(if (amounts.isEmpty()) "No contract amounts on file" else "${compactPesos(total)} in approved contracts")
                        if (missing > 0 && amounts.isNotEmpty()) append(" · $missing without an amount")
                    },
                    size = EagleType.Caption,
                    lineHeight = 16.sp,
                    color = EagleColors.Muted,
                    tabular = true,
                )
            }
            Button(
                label = sort.label,
                onClick = onToggleSort,
                style = ButtonStyle.Quiet,
                compact = true,
                glyph = Glyph.Sort,
            )
        }
        AnimatedVisibility(visible = showSort) {
            Column(Modifier.padding(horizontal = layout.gutter).padding(bottom = 8.dp)) {
                SectionLabel("Sort by")
                Wrap(spacing = 6.dp) {
                    Sort.entries.forEach { option ->
                        FilterChip(option.label, selected = option == sort, onClick = { onSort(option) }, role = Role.RadioButton)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSheet(all: List<Project>, filters: Filters, onChange: (Filters) -> Unit, onClose: () -> Unit) {
    val layout = LocalLayout.current
    val count = all.count(filters::matches)
    Column(Modifier.fillMaxSize().background(EagleColors.Page)) {
        PageBar(
            onBack = onClose,
            title = "Filters",
            backGlyph = Glyph.Close,
            backDescription = "Close filters",
            actions = {
                Button(
                    "Clear all",
                    onClick = { onChange(filters.clearFacets()) },
                    style = ButtonStyle.Quiet,
                    compact = true,
                    enabled = filters.facetCount > 0,
                )
            },
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = layout.gutter, vertical = 16.dp),
        ) {
            Column(Modifier.widthIn(max = EagleMetrics.ContentMax)) {
                FacetSection(
                    title = "Status",
                    options = Status.entries,
                    label = { it.label },
                    selected = filters.statuses,
                    count = { s -> all.count { filters.copy(statuses = setOf(s)).matches(it) } },
                    onToggle = { onChange(filters.copy(statuses = filters.statuses.toggle(it))) },
                )
                FacetSection(
                    title = "Type of project",
                    options = Category.entries,
                    label = { it.label },
                    selected = filters.categories,
                    count = { c -> all.count { filters.copy(categories = setOf(c)).matches(it) } },
                    onToggle = { onChange(filters.copy(categories = filters.categories.toggle(it))) },
                )
                FacetSection(
                    title = "Level of government",
                    options = Level.entries,
                    label = { it.label },
                    selected = filters.levels,
                    count = { l -> all.count { filters.copy(levels = setOf(l)).matches(it) } },
                    onToggle = { onChange(filters.copy(levels = filters.levels.toggle(it))) },
                )
                FacetSection(
                    title = "Province",
                    options = all.map { it.place.province }.distinct().sorted(),
                    label = { it },
                    selected = filters.provinces,
                    count = { p -> all.count { filters.copy(provinces = setOf(p)).matches(it) } },
                    onToggle = { onChange(filters.copy(provinces = filters.provinces.toggle(it))) },
                )
                FacetSection(
                    title = "Implementing agency",
                    options = all.mapNotNull { it.implementingAgency?.name }.distinct().sorted(),
                    label = { it },
                    selected = filters.agencies,
                    count = { a -> all.count { filters.copy(agencies = setOf(a)).matches(it) } },
                    onToggle = { onChange(filters.copy(agencies = filters.agencies.toggle(it))) },
                )
                FacetSection(
                    title = "Approved contract amount",
                    options = AmountBand.entries,
                    label = { it.label },
                    selected = filters.bands,
                    count = { b -> all.count { filters.copy(bands = setOf(b)).matches(it) } },
                    onToggle = { onChange(filters.copy(bands = filters.bands.toggle(it))) },
                )
            }
        }
        Hairline()
        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = layout.gutter, vertical = 10.dp),
        ) {
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

/** One facet: a label and a wrap of counted chips. Options with no matches stay visible but disabled. */
@Composable
private fun <T> FacetSection(
    title: String,
    options: List<T>,
    label: (T) -> String,
    selected: Set<T>,
    count: (T) -> Int,
    onToggle: (T) -> Unit,
) {
    SectionLabel(title)
    Spacer(Modifier.height(4.dp))
    Wrap(spacing = 8.dp) {
        options.forEach { option ->
            val n = count(option)
            FilterChip(
                label = label(option),
                selected = option in selected,
                onClick = { onToggle(option) },
                count = n,
                enabled = n > 0 || option in selected,
            )
        }
    }
    Spacer(Modifier.height(20.dp))
}
