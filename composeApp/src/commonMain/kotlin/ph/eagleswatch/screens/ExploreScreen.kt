package ph.eagleswatch.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ph.eagleswatch.content.Category
import ph.eagleswatch.content.Level
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.Status
import ph.eagleswatch.content.projects
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.eagle_logo
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.ui.ActionButton
import ph.eagleswatch.ui.ButtonStyle
import ph.eagleswatch.ui.ChoiceChip
import ph.eagleswatch.ui.Divider
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.LocalFonts
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.SampleBadge
import ph.eagleswatch.ui.TextAction
import ph.eagleswatch.ui.Wrap
import ph.eagleswatch.ui.compactPesos
import ph.eagleswatch.ui.focusRing
import ph.eagleswatch.ui.glyph

/** Which projects the citizen has narrowed the feed to. Empty sets mean "any". */
private data class Filters(
    val query: String = "",
    val category: Category? = null,
    val levels: Set<Level> = emptySet(),
    val statuses: Set<Status> = emptySet(),
) {
    val extra: Int get() = levels.size + statuses.size

    fun matches(project: Project): Boolean {
        val q = query.trim().lowercase()
        val text = q.isEmpty() || listOf(project.title, project.place, project.proposedBy, project.implementedBy)
            .any { it.lowercase().contains(q) }
        return text &&
            (category == null || project.category == category) &&
            (levels.isEmpty() || project.level in levels) &&
            (statuses.isEmpty() || project.status in statuses)
    }
}

/** Every posted project, searchable and grouped by what it builds. */
@Composable
fun ExploreScreen(citizen: CitizenState, onOpen: (Project) -> Unit, modifier: Modifier = Modifier) {
    val layout = LocalLayout.current
    var filters by remember { mutableStateOf(Filters()) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val shown = projects.filter(filters::matches)

    Box(modifier.fillMaxSize().background(EagleColors.Page)) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(EagleColors.Page)
                    .then(if (layout.wide) Modifier else Modifier.windowInsetsPadding(WindowInsets.statusBars)),
            ) {
                SearchRow(
                    query = filters.query,
                    onQuery = { filters = filters.copy(query = it) },
                    extraFilters = filters.extra,
                    onFilters = { showFilters = true },
                )
                CategoryTabs(selected = filters.category, onSelect = { filters = filters.copy(category = it) })
                Divider(Modifier.fillMaxWidth())
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = layout.gutter, end = layout.gutter, top = 16.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) { Summary(shown) }
                if (shown.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { NoResults(onClear = { filters = Filters() }) }
                }
                items(shown.take(3), key = { it.id }) { ProjectCard(it, citizen, onOpen = { onOpen(it) }) }
                if (shown.size >= 3 && filters == Filters()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { FactCard(Modifier.widthIn(max = 640.dp)) }
                }
                items(shown.drop(3), key = { it.id }) { ProjectCard(it, citizen, onOpen = { onOpen(it) }) }
            }
        }

        AnimatedVisibility(
            visible = showFilters,
            enter = fadeIn() + slideInVertically { it / 3 },
            exit = fadeOut() + slideOutVertically { it / 3 },
        ) {
            FilterSheet(
                filters = filters,
                onChange = { filters = it },
                onClose = { showFilters = false },
            )
        }
    }
}

@Composable
private fun SearchRow(query: String, onQuery: (String) -> Unit, extraFilters: Int, onFilters: () -> Unit) {
    val layout = LocalLayout.current
    val fonts = LocalFonts.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = layout.gutter).padding(top = 12.dp, bottom = 8.dp),
        horizontalArrangement = if (layout.wide) Arrangement.Center else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!layout.wide) {
            Image(
                painter = painterResource(Res.drawable.eagle_logo),
                contentDescription = "The Eagle's Watch",
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
            )
            Spacer(Modifier.width(10.dp))
        }
        Row(
            Modifier
                .weight(1f, fill = !layout.wide)
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .height(54.dp)
                .shadow(6.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(EagleColors.Page)
                .padding(start = 18.dp, end = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Glyph.Search, tint = EagleColors.Ink, size = 20.dp)
            Spacer(Modifier.width(12.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    EText("Search projects, places or agencies", size = 15.sp, color = EagleColors.Muted, maxLines = 1, ellipsis = true)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQuery,
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = fonts.sans, fontSize = 15.sp, color = EagleColors.Ink),
                    cursorBrush = SolidColor(EagleColors.Blue),
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Search projects" },
                )
            }
            if (query.isNotEmpty()) {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).clickable(role = Role.Button) { onQuery("") }
                        .semantics { contentDescription = "Clear search" },
                    contentAlignment = Alignment.Center,
                ) { Icon(Glyph.Close, tint = EagleColors.Muted, size = 16.dp) }
            }
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .size(40.dp)
                    .focusRing(CircleShape)
                    .clip(CircleShape)
                    .border(1.dp, if (extraFilters > 0) EagleColors.Ink else EagleColors.Line, CircleShape)
                    .clickable(role = Role.Button, onClick = onFilters)
                    .semantics { contentDescription = "Filters" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Glyph.Filter, tint = EagleColors.Ink, size = 18.dp)
                if (extraFilters > 0) {
                    Box(
                        Modifier.align(Alignment.TopEnd).size(16.dp).background(EagleColors.Ink, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { EText("$extraFilters", size = 10.sp, weight = FontWeight.SemiBold, color = EagleColors.Page) }
                }
            }
        }
    }
}

/** Icon tabs across the top, the way a travel app groups homes: one per kind of project. */
@Composable
private fun CategoryTabs(selected: Category?, onSelect: (Category?) -> Unit) {
    val layout = LocalLayout.current
    val tabs: List<Category?> = listOf(null) + Category.entries
    LazyRow(
        contentPadding = PaddingValues(horizontal = layout.gutter - 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(tabs, key = { it?.name ?: "all" }) { category ->
            val isSelected = category == selected
            val color = if (isSelected) EagleColors.Ink else EagleColors.Muted
            val shape = RoundedCornerShape(10.dp)
            Column(
                Modifier
                    .focusRing(shape)
                    .clip(shape)
                    .selectable(
                        selected = isSelected,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab,
                        onClick = { onSelect(category) },
                    )
                    .padding(horizontal = 6.dp)
                    .padding(top = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(category?.glyph ?: Glyph.All, tint = color, size = 24.dp)
                Spacer(Modifier.height(6.dp))
                EText(category?.label ?: "All", size = 12.sp, weight = FontWeight.SemiBold, color = color, maxLines = 1)
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .height(2.dp)
                        .width(if (isSelected) 36.dp else 0.dp)
                        .background(EagleColors.Ink),
                )
            }
        }
    }
}

@Composable
private fun Summary(shown: List<Project>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        EText(
            text = "${shown.size} projects · ${compactPesos(shown.sumOf { it.budget })} in budgets",
            modifier = Modifier.weight(1f),
            size = 14.sp,
            weight = FontWeight.SemiBold,
            color = EagleColors.Body,
        )
        SampleBadge()
    }
}

@Composable
private fun NoResults(onClear: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Glyph.Search, tint = EagleColors.Muted, size = 40.dp)
        Spacer(Modifier.height(14.dp))
        EText("No projects match", size = 18.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        EText("Try another place or agency, or clear the filters.", size = 15.sp, color = EagleColors.Body)
        Spacer(Modifier.height(18.dp))
        ActionButton("Clear filters", onClick = onClear, style = ButtonStyle.Outline)
    }
}

@Composable
private fun FilterSheet(filters: Filters, onChange: (Filters) -> Unit, onClose: () -> Unit) {
    val count = projects.count(filters::matches)
    Column(Modifier.fillMaxSize().background(EagleColors.Page)) {
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onClose)
                    .semantics { contentDescription = "Close filters" },
                contentAlignment = Alignment.Center,
            ) { Icon(Glyph.Close, size = 20.dp) }
            EText("Filters", modifier = Modifier.weight(1f), size = 17.sp, weight = FontWeight.SemiBold, align = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.width(40.dp))
        }
        Divider(Modifier.fillMaxWidth())
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
        ) {
            EText("Level of government", size = 20.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            Wrap(spacing = 10.dp) {
                Level.entries.forEach { level ->
                    ChoiceChip(
                        label = level.label,
                        selected = level in filters.levels,
                        onClick = {
                            onChange(filters.copy(levels = if (level in filters.levels) filters.levels - level else filters.levels + level))
                        },
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
            Divider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(28.dp))
            EText("Status", size = 20.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            Wrap(spacing = 10.dp) {
                Status.entries.forEach { status ->
                    ChoiceChip(
                        label = status.label,
                        selected = status in filters.statuses,
                        onClick = {
                            onChange(filters.copy(statuses = if (status in filters.statuses) filters.statuses - status else filters.statuses + status))
                        },
                    )
                }
            }
        }
        Divider(Modifier.fillMaxWidth())
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextAction("Clear all", onClick = { onChange(filters.copy(levels = emptySet(), statuses = emptySet())) }, color = EagleColors.Ink)
            Spacer(Modifier.weight(1f))
            ActionButton(if (count == 1) "Show 1 project" else "Show $count projects", onClick = onClose)
        }
    }
}
