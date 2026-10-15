package ph.eagleswatch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ph.eagleswatch.content.Day
import ph.eagleswatch.content.Project
import ph.eagleswatch.data.LoadState
import ph.eagleswatch.data.ProjectStore
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EmptyState
import ph.eagleswatch.ui.ErrorState
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.SkeletonRows

/** Projects the citizen keeps an eye on, as the same rows as Explore. */
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
    Column(modifier.fillMaxSize().background(EagleColors.Page)) {
        TabTitle(
            "Watchlist",
            subtitle = when (watched.size) {
                0 -> "Projects you watch, with their latest reported figures"
                1 -> "1 project you watch"
                else -> "${watched.size} projects you watch"
            },
        )
        when (state) {
            LoadState.Loading -> ContentColumn { SkeletonRows(2, Modifier.padding(horizontal = layout.gutter)) }
            is LoadState.Failed -> ContentColumn {
                ErrorState("The records could not be loaded", state.message, onRetry = store::retry, modifier = Modifier.padding(horizontal = layout.gutter))
            }
            is LoadState.Ready -> if (watched.isEmpty()) {
                ContentColumn {
                    EmptyState(
                        glyph = Glyph.Eye,
                        title = "No projects watched yet",
                        body = "Tap the eye on any project to keep it here. Its updates then show first under Updates.",
                        modifier = Modifier.padding(horizontal = layout.gutter),
                        actionLabel = "Explore projects",
                        onAction = onExplore,
                        primary = true,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (layout.wide) 2 else 1),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(watched, key = { it.id }) { project ->
                        ProjectRow(
                            project = project,
                            today = today,
                            watching = true,
                            onOpen = { onOpen(project) },
                            onToggleWatch = { citizen.toggleWatch(project.id) },
                        )
                    }
                }
            }
        }
    }
}
