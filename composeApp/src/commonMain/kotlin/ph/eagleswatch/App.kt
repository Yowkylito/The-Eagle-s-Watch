package ph.eagleswatch

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.projectById
import ph.eagleswatch.nav.BottomNav
import ph.eagleswatch.nav.Destination
import ph.eagleswatch.nav.TopBar
import ph.eagleswatch.screens.AboutScreen
import ph.eagleswatch.screens.ArticleScreen
import ph.eagleswatch.screens.ExploreScreen
import ph.eagleswatch.screens.ProjectScreen
import ph.eagleswatch.screens.ReportScreen
import ph.eagleswatch.screens.UpdatesScreen
import ph.eagleswatch.screens.WatchlistScreen
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.state.PlatformBackHandler
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.Layout
import ph.eagleswatch.ui.LocalFonts
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.rememberEagleFonts

/** A page opened on top of the tabs. */
private sealed interface Route {
    data class ProjectPage(val id: String) : Route
    data class Article(val id: String) : Route

    /** Report a problem; about a project when [projectId] is set. */
    data class Report(val projectId: String?) : Route
}

/**
 * Four tabs (Explore, Watchlist, Updates, About), with project pages, articles and reports stacked on top.
 * Phones navigate from a bottom bar; wide windows use tabs in the top bar.
 */
@Composable
fun App() {
    val fonts = rememberEagleFonts()
    CompositionLocalProvider(LocalFonts provides fonts) {
        BoxWithConstraints(Modifier.fillMaxSize().background(EagleColors.Page)) {
            val wide = maxWidth >= Layout.WideBreakpoint
            val layout = remember(wide) { Layout(wide = wide, gutter = if (wide) 40.dp else 20.dp) }

            var destination by remember { mutableStateOf(Destination.Explore) }
            val stack = remember { mutableStateListOf<Route>() }
            val citizen = remember { CitizenState() }
            val openProject: (Project) -> Unit = { stack.add(Route.ProjectPage(it.id)) }
            val back: () -> Unit = { stack.removeLastOrNull() }
            val report: (String?) -> Unit = { stack.add(Route.Report(it)) }
            PlatformBackHandler(enabled = stack.isNotEmpty(), onBack = back)

            CompositionLocalProvider(LocalLayout provides layout) {
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        if (wide) TopBar(current = destination, onSelect = { destination = it }, onReport = { report(null) })
                        Crossfade(
                            targetState = destination,
                            modifier = Modifier.weight(1f),
                            animationSpec = tween(180),
                            label = "tab",
                        ) { shown ->
                            when (shown) {
                                Destination.Explore -> ExploreScreen(citizen, onOpen = openProject)
                                Destination.Watchlist -> WatchlistScreen(
                                    citizen,
                                    onOpen = openProject,
                                    onExplore = { destination = Destination.Explore },
                                )
                                Destination.Updates -> UpdatesScreen(citizen, onOpen = openProject)
                                Destination.About -> AboutScreen(citizen)
                            }
                        }
                        if (!wide) BottomNav(current = destination, onSelect = { destination = it }, onReport = { report(null) })
                    }

                    // Pages slide in from the right as they open and back out as they close.
                    AnimatedContent(
                        targetState = stack.size to stack.lastOrNull(),
                        transitionSpec = {
                            if (targetState.first > initialState.first) {
                                slideInHorizontally(tween(260)) { it } togetherWith fadeOut(tween(260))
                            } else {
                                fadeIn(tween(200)) togetherWith slideOutHorizontally(tween(240)) { it }
                            }
                        },
                        label = "page",
                    ) { (_, route) ->
                        when (route) {
                            null -> Box(Modifier)
                            is Route.ProjectPage -> projectById(route.id)?.let { project ->
                                ProjectScreen(
                                    project = project,
                                    citizen = citizen,
                                    onBack = back,
                                    onArticle = { stack.add(Route.Article(project.id)) },
                                    onReport = { report(project.id) },
                                )
                            }
                            is Route.Article -> projectById(route.id)?.let { ArticleScreen(it, onBack = back) }
                            is Route.Report -> ReportScreen(
                                projectId = route.projectId,
                                onSubmit = citizen::addReport,
                                onClose = back,
                            )
                        }
                    }
                }
            }
        }
    }
}
