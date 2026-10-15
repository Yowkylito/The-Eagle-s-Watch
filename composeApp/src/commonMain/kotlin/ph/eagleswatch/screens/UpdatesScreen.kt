package ph.eagleswatch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import ph.eagleswatch.ui.Card
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleMetrics
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.EmptyState
import ph.eagleswatch.ui.ErrorState
import ph.eagleswatch.ui.FilterChip
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.IconTile
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.SkeletonRows
import ph.eagleswatch.ui.Tag
import ph.eagleswatch.ui.VerificationTag
import ph.eagleswatch.ui.glyph

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

    Column(modifier.fillMaxSize().background(EagleColors.Canvas)) {
        TabTitle("Updates", subtitle = "Dated entries from every record, with who reported each one")
        ContentColumn {
            Row(Modifier.padding(horizontal = layout.gutter), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip("All projects", selected = !watchingOnly, onClick = { watchingOnly = false }, role = Role.RadioButton)
                FilterChip("Watching", selected = watchingOnly, onClick = { watchingOnly = true }, role = Role.RadioButton, count = citizen.watched.size)
            }
        }
        Spacer(Modifier.height(4.dp))
        when (state) {
            LoadState.Loading -> ContentColumn { SkeletonRows(3, Modifier.padding(horizontal = layout.gutter)) }
            is LoadState.Failed -> ContentColumn {
                ErrorState("The records could not be loaded", state.message, onRetry = store::retry, modifier = Modifier.padding(horizontal = layout.gutter))
            }
            is LoadState.Ready -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(EagleMetrics.CardGap),
            ) {
                if (feed.isEmpty()) {
                    item(key = "empty") {
                        ContentColumn {
                            Card(Modifier.padding(horizontal = layout.gutter)) {
                                EmptyState(
                                    glyph = Glyph.Bell,
                                    title = if (watchingOnly) "No updates from watched projects" else "No updates yet",
                                    body = if (watchingOnly) "Tap the eye on a project to follow it. Its updates will show here." else "Updates appear as records gain dated entries.",
                                )
                            }
                        }
                    }
                }
                items(feed, key = { (project, update) -> "${project.id}:${update.date.epochDay}:${update.title}" }) { (project, update) ->
                    ContentColumn { UpdateCard(project, update, onOpen = { onOpen(project) }, modifier = Modifier.padding(horizontal = layout.gutter)) }
                }
            }
        }
    }
}

@Composable
private fun UpdateCard(project: Project, update: Update, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier, onClick = onOpen, description = "Open ${project.title}") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EText(update.date.format(), modifier = Modifier.weight(1f), size = EagleType.Label, weight = FontWeight.SemiBold, color = EagleColors.Muted, tabular = true)
            if (update.percent != null) {
                Tag("${update.percent}% work", color = EagleColors.Blue, background = EagleColors.BlueSoft)
                Spacer(Modifier.width(6.dp))
            }
            VerificationTag(update.attribution.verification)
        }
        Spacer(Modifier.height(8.dp))
        EText(update.title, display = true, size = EagleType.Title, lineHeight = 22.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        EText(update.body, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body, maxLines = 3, ellipsis = true)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(project.category.glyph, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                EText(project.title, size = EagleType.Label, weight = FontWeight.Medium, color = EagleColors.Ink, maxLines = 1, ellipsis = true)
                EText(project.place.short, size = EagleType.Caption, color = EagleColors.Muted, maxLines = 1, ellipsis = true)
            }
        }
    }
}
