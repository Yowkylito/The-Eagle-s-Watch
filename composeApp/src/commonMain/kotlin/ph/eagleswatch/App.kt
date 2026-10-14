package ph.eagleswatch

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ph.eagleswatch.content.trail
import ph.eagleswatch.nav.BottomNav
import ph.eagleswatch.nav.Destination
import ph.eagleswatch.nav.TopBar
import ph.eagleswatch.screens.HowItWorksScreen
import ph.eagleswatch.screens.JoinScreen
import ph.eagleswatch.screens.TraceScreen
import ph.eagleswatch.state.TraceProgress
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.Layout
import ph.eagleswatch.ui.LocalFonts
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.rememberEagleFonts

/**
 * Three screens, one tap apart: Trace (the product), How it works, Join in.
 * Phones navigate from a bottom bar; wide windows use tabs in the top bar.
 */
@Composable
fun App() {
    val fonts = rememberEagleFonts()
    CompositionLocalProvider(LocalFonts provides fonts) {
        BoxWithConstraints(Modifier.fillMaxSize().background(EagleColors.Sheet)) {
            val wide = maxWidth >= Layout.WideBreakpoint
            val layout = remember(wide) { Layout(wide = wide, gutter = if (wide) 40.dp else 20.dp) }

            var destination by remember { mutableStateOf(Destination.Trace) }
            // Hoisted so the chosen level and the citizen's progress survive switching screens.
            val pager = rememberPagerState(pageCount = { trail.size })
            val progress = remember { TraceProgress() }

            CompositionLocalProvider(LocalLayout provides layout) {
                Column(Modifier.fillMaxSize()) {
                    TopBar(current = destination, onSelect = { destination = it })
                    Crossfade(
                        targetState = destination,
                        modifier = Modifier
                            .weight(1f)
                            .then(if (wide) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier),
                        animationSpec = tween(180),
                        label = "screen",
                    ) { shown ->
                        when (shown) {
                            Destination.Trace -> TraceScreen(pager, progress)
                            Destination.HowItWorks -> HowItWorksScreen()
                            Destination.Join -> JoinScreen()
                        }
                    }
                    if (!wide) BottomNav(current = destination, onSelect = { destination = it })
                }
            }
        }
    }
}
