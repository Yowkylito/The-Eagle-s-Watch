package ph.eagleswatch

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.border
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import ph.eagleswatch.ui.ChromeState
import ph.eagleswatch.ui.GlassStyles
import ph.eagleswatch.ui.LocalBottomSpace
import ph.eagleswatch.ui.LocalChrome
import ph.eagleswatch.ui.LocalHaze
import ph.eagleswatch.ui.TabBarHeight
import ph.eagleswatch.ui.glassEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.ReportPreset
import ph.eagleswatch.data.LoadState
import ph.eagleswatch.data.ProjectStore
import ph.eagleswatch.data.SampleProjectRepository
import ph.eagleswatch.nav.BottomNav
import ph.eagleswatch.nav.Destination
import ph.eagleswatch.nav.TopBar
import ph.eagleswatch.screens.AboutScreen
import ph.eagleswatch.screens.ExploreScreen
import ph.eagleswatch.screens.PageBar
import ph.eagleswatch.screens.ProjectScreen
import ph.eagleswatch.screens.ReportScreen
import ph.eagleswatch.screens.UpdatesScreen
import ph.eagleswatch.screens.WatchlistScreen
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.state.PlatformBackHandler
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleMetrics
import ph.eagleswatch.ui.EmptyState
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Layout
import ph.eagleswatch.ui.LocalFonts
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.LocalSourceSheet
import ph.eagleswatch.ui.SourceInfo
import ph.eagleswatch.ui.SourceSheetContent
import ph.eagleswatch.ui.consumeTouches
import ph.eagleswatch.ui.debugTaps
import ph.eagleswatch.ui.rememberEagleFonts

/** A page opened on top of the tabs. */
private sealed interface Route {
    data class ProjectPage(val id: String) : Route

    /** Report a problem; about a project when [projectId] is set, pre-filled when [preset] is set. */
    data class Report(val projectId: String?, val preset: ReportPreset?) : Route
}

/**
 * Four tabs (Explore, Watchlist, Updates, About), with project pages and reports stacked on top.
 * Phones navigate from a bottom bar; wide windows use tabs in the top bar.
 */
@Composable
fun App() {
    val fonts = rememberEagleFonts()
    CompositionLocalProvider(LocalFonts provides fonts) {
        BoxWithConstraints(Modifier.fillMaxSize().background(EagleColors.Canvas)) {
            val wide = maxWidth >= Layout.WideBreakpoint
            val layout = remember(wide) { Layout(wide = wide, gutter = if (wide) EagleMetrics.WideGutter else EagleMetrics.PhoneGutter) }

            var destination by remember { mutableStateOf(Destination.Explore) }
            val stack = remember { mutableStateListOf<Route>() }
            val citizen = remember { CitizenState() }
            val store = remember { ProjectStore(SampleProjectRepository) }
            LaunchedEffect(store.attempt) { store.load() }

            val openProject: (Project) -> Unit = { stack.add(Route.ProjectPage(it.id)) }
            val back: () -> Unit = { println("EW-DEBUG back, stack=${stack.size}"); stack.removeLastOrNull() }
            val report: (String?, ReportPreset?) -> Unit = { id, preset -> stack.add(Route.Report(id, preset)) }
            PlatformBackHandler(enabled = stack.isNotEmpty(), onBack = back)
            val sample = (store.state as? LoadState.Ready)?.dataset?.sample == true

            // The source sheet: where a figure comes from, opened from any source badge.
            var sheetOpen by remember { mutableStateOf(false) }
            var sheetInfo by remember { mutableStateOf<SourceInfo?>(null) }
            val openSource: (SourceInfo) -> Unit = remember { { info -> println("EW-DEBUG open source ${info.figure}"); sheetInfo = info; sheetOpen = true } }
            PlatformBackHandler(enabled = sheetOpen) { sheetOpen = false }

            // Glass: every tab and page draws into one blur source; the tab bar, the status-bar strip and the
            // source sheet read from it.
            val haze = rememberHazeState()
            val chrome = remember { ChromeState() }
            val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val showTabBar = !wide && stack.isEmpty() && !chrome.tabBarHidden
            val bottomSpace = if (wide) 0.dp else TabBarHeight + 24.dp + navBottom

            CompositionLocalProvider(
                LocalLayout provides layout,
                LocalSourceSheet provides openSource,
                LocalHaze provides haze,
                LocalChrome provides chrome,
                LocalBottomSpace provides bottomSpace,
            ) {
                Box(Modifier.fillMaxSize().debugTaps("root")) {
                  Box(Modifier.fillMaxSize().hazeSource(haze)) {
                    Column(Modifier.fillMaxSize()) {
                        if (wide) TopBar(current = destination, onSelect = { destination = it }, onReport = { report(null, null) }, sample = sample)
                        Crossfade(
                            targetState = destination,
                            modifier = Modifier.weight(1f),
                            animationSpec = tween(160),
                            label = "tab",
                        ) { shown ->
                            when (shown) {
                                Destination.Explore -> ExploreScreen(
                                    store = store,
                                    citizen = citizen,
                                    onOpen = openProject,
                                    onReport = { report(null, null) },
                                    onAbout = { destination = Destination.About },
                                )
                                Destination.Watchlist -> WatchlistScreen(
                                    store = store,
                                    citizen = citizen,
                                    onOpen = openProject,
                                    onExplore = { destination = Destination.Explore },
                                )
                                Destination.Updates -> UpdatesScreen(store = store, citizen = citizen, onOpen = openProject)
                                Destination.About -> AboutScreen(citizen = citizen, onReport = { report(null, null) })
                            }
                        }
                    }

                    // Pages cross-fade in place. A horizontal slide left touch targets offset from where the
                    // page was drawn when rendering under the blur layer, so pages do not move as they open.
                    AnimatedContent(
                        targetState = stack.size to stack.lastOrNull(),
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                        label = "page",
                    ) { (_, route) ->
                        when (route) {
                            null -> Box(Modifier)
                            is Route.ProjectPage -> {
                                val project = store.byId(route.id)
                                if (project != null) {
                                    ProjectScreen(
                                        project = project,
                                        citizen = citizen,
                                        onBack = back,
                                        onReport = { preset -> report(project.id, preset) },
                                    )
                                } else {
                                    MissingProject(onBack = back)
                                }
                            }
                            is Route.Report -> ReportScreen(
                                project = route.projectId?.let { store.byId(it) },
                                projects = store.projects,
                                preset = route.preset,
                                onSubmit = citizen::addReport,
                                onClose = back,
                            )
                        }
                    }
                  }

                    // Content scrolling under the status bar shows through frosted navy, so the light status
                    // icons always have a dark backing.
                    if (!wide && stack.isEmpty()) {
                        Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).hazeEffect(haze, GlassStyles.Navy))
                    }

                    AnimatedVisibility(
                        visible = showTabBar,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        enter = slideInVertically(tween(220)) { it } + fadeIn(tween(220)),
                        exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(180)),
                    ) {
                        BottomNav(current = destination, onSelect = { destination = it })
                    }

                    AnimatedVisibility(visible = sheetOpen, enter = fadeIn(tween(160)), exit = fadeOut(tween(160))) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(EagleColors.Scrim)
                                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { sheetOpen = false },
                        )
                    }
                    AnimatedVisibility(
                        visible = sheetOpen,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        enter = slideInVertically(tween(220)) { it },
                        exit = slideOutVertically(tween(180)) { it },
                    ) {
                        val info = sheetInfo
                        if (info != null) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 640.dp)
                                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                                    .hazeEffect(haze, GlassStyles.Sheet)
                                    .border(1.dp, glassEdge(0.5f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                                    .consumeTouches()
                                    .windowInsetsPadding(WindowInsets.navigationBars),
                            ) {
                                SourceSheetContent(info, onClose = { sheetOpen = false })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissingProject(onBack: () -> Unit) {
    val layout = LocalLayout.current
    Column(Modifier.fillMaxSize().background(EagleColors.Canvas).consumeTouches()) {
        PageBar(onBack = onBack, title = "Project")
        EmptyState(
            glyph = Glyph.Alert,
            title = "This record is not available",
            body = "It may have been removed from the dataset, or the records have not finished loading.",
            modifier = Modifier.padding(horizontal = layout.gutter),
            actionLabel = "Back to the list",
            onAction = onBack,
        )
    }
}
