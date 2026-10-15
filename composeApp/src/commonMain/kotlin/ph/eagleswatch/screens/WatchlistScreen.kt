package ph.eagleswatch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ph.eagleswatch.content.Day
import ph.eagleswatch.content.Project
import ph.eagleswatch.data.LoadState
import ph.eagleswatch.data.ProjectStore
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.ui.Card
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleMetrics
import ph.eagleswatch.ui.EmptyState
import ph.eagleswatch.ui.ErrorState
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.SkeletonRows

/** Projects the citizen keeps an eye on, as the same cards as Explore. */
@Composable
fun WatchlistScreen(
    store: ProjectStore,
    citizen: CitizenState,
    onOpen: (Project) -> Unit,
    onExplore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = LocalLayout.current
    val today = remember { Day.today() }
    val state = store.state
    val watched = store.projects.filter { citizen.isWatching(it.id) }
    val columns = if (layout.wide) 2 else 1
    val max = if (layout.wide) 1200.dp else EagleMetrics.ContentMax
    Column(modifier.fillMaxSize().background(EagleColors.Canvas)) {
        TabTitle(
            "Watchlist",
            subtitle = when (watched.size) {
                0 -> "Projects you follow, with their latest reported figures"
                1 -> "1 project you follow"
                else -> "${watched.size} projects you follow"
            },
        )
        when (state) {
            LoadState.Loading -> ContentColumn { SkeletonRows(2, Modifier.padding(horizontal = layout.gutter).padding(top = 16.dp)) }
            is LoadState.Failed -> ContentColumn {
                ErrorState("The records could not be loaded", state.message, onRetry = store::retry, modifier = Modifier.padding(horizontal = layout.gutter))
            }
            is LoadState.Ready -> if (watched.isEmpty()) {
                ContentColumn {
                    Card(Modifier.padding(horizontal = layout.gutter).padding(top = 16.dp)) {
                        EmptyState(
                            glyph = Glyph.Eye,
                            title = "Nothing watched yet",
                            body = "Tap the eye on any project to follow it. It will be kept here, and its updates show under Updates.",
                            actionLabel = "Explore projects",
                            onAction = onExplore,
                            primary = true,
                        )
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)) {
                    items(watched.chunked(columns), key = { row -> row.joinToString("|") { it.id } }) { row ->
                        ContentColumn(max = max) {
                            Row(
                                Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal = layout.gutter).padding(bottom = EagleMetrics.CardGap),
                                horizontalArrangement = Arrangement.spacedBy(EagleMetrics.CardGap),
                            ) {
                                row.forEach { project ->
                                    ProjectCard(
                                        project = project,
                                        today = today,
                                        watching = true,
                                        onOpen = { onOpen(project) },
                                        onToggleWatch = { citizen.toggleWatch(project.id) },
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                    )
                                }
                                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
