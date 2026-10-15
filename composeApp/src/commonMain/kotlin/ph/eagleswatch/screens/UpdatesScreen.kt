package ph.eagleswatch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.Update
import ph.eagleswatch.data.LoadState
import ph.eagleswatch.data.ProjectStore
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.EmptyState
import ph.eagleswatch.ui.ErrorState
import ph.eagleswatch.ui.FilterChip
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Hairline
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.SkeletonRows
import ph.eagleswatch.ui.VerificationTag
import ph.eagleswatch.ui.glyph
import ph.eagleswatch.ui.tappable

/** Every dated entry on every record, newest first, each with who reported it. */
@Composable
fun UpdatesScreen(store: ProjectStore, citizen: CitizenState, onOpen: (Project) -> Unit, modifier: Modifier = Modifier) {
    val layout = LocalLayout.current
    var watchingOnly by rememberSaveable { mutableStateOf(false) }
    val state = store.state
    val feed = store.projects
        .filter { !watchingOnly || citizen.isWatching(it.id) }
        .flatMap { project -> project.updates.map { project to it } }
        .sortedByDescending { (_, update) -> update.date.epochDay }

    Column(modifier.fillMaxSize().background(EagleColors.Page)) {
        TabTitle("Updates", subtitle = "Dated entries from every record, newest first, with who reported each one")
        Row(Modifier.padding(horizontal = layout.gutter), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip("All projects", selected = !watchingOnly, onClick = { watchingOnly = false }, role = Role.RadioButton, count = store.projects.size)
            FilterChip("Watching", selected = watchingOnly, onClick = { watchingOnly = true }, role = Role.RadioButton, count = citizen.watched.size)
        }
        Spacer(Modifier.height(4.dp))
        when (state) {
            LoadState.Loading -> ContentColumn { SkeletonRows(3, Modifier.padding(horizontal = layout.gutter)) }
            is LoadState.Failed -> ContentColumn {
                ErrorState("The records could not be loaded", state.message, onRetry = store::retry, modifier = Modifier.padding(horizontal = layout.gutter))
            }
            is LoadState.Ready -> ContentColumn(Modifier.weight(1f)) {
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    if (feed.isEmpty()) {
                        item(key = "empty") {
                            EmptyState(
                                glyph = Glyph.Bell,
                                title = if (watchingOnly) "No updates from watched projects" else "No updates yet",
                                body = if (watchingOnly) "Tap the eye on a project to watch it. Its updates will show here." else "Updates appear as records gain dated entries.",
                                modifier = Modifier.padding(horizontal = layout.gutter),
                            )
                        }
                    }
                    items(feed, key = { (project, update) -> "${project.id}:${update.date.epochDay}:${update.title}" }) { (project, update) ->
                        UpdateItem(project, update, onOpen = { onOpen(project) })
                        Hairline()
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateItem(project: Project, update: Update, onOpen: () -> Unit) {
    val layout = LocalLayout.current
    Column(
        Modifier
            .fillMaxWidth()
            .tappable(onClick = onOpen, description = "Open ${project.title}")
            .padding(horizontal = layout.gutter, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EText(update.date.format(), size = EagleType.Label, weight = FontWeight.SemiBold, tabular = true)
            Spacer(Modifier.width(8.dp))
            VerificationTag(update.attribution.verification)
            Spacer(Modifier.weight(1f))
            if (update.percent != null) {
                EText("${update.percent}% work", size = EagleType.Caption, color = EagleColors.Muted, tabular = true)
            }
        }
        Spacer(Modifier.height(6.dp))
        EText(update.title, size = EagleType.Body, lineHeight = 20.sp, weight = FontWeight.SemiBold)
        EText(update.body, size = EagleType.Secondary, lineHeight = 19.sp, color = EagleColors.Body, maxLines = 3, ellipsis = true)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(project.category.glyph, tint = EagleColors.Muted, size = 13.dp, weight = 2f)
            Spacer(Modifier.width(5.dp))
            EText(
                text = "${project.title} · ${project.place.short}",
                size = EagleType.Caption,
                lineHeight = 16.sp,
                color = EagleColors.Blue,
                maxLines = 1,
                ellipsis = true,
            )
        }
    }
}
