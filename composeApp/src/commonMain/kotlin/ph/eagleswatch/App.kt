package ph.eagleswatch

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ph.eagleswatch.sections.CallToAction
import ph.eagleswatch.sections.ExplorerSection
import ph.eagleswatch.sections.Footer
import ph.eagleswatch.sections.Hero
import ph.eagleswatch.sections.NavBar
import ph.eagleswatch.sections.NavHeight
import ph.eagleswatch.sections.PrinciplesSection
import ph.eagleswatch.sections.TrailSection
import ph.eagleswatch.ui.ContentRef
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalEnv
import ph.eagleswatch.ui.LocalFonts
import ph.eagleswatch.ui.PageEnv
import ph.eagleswatch.ui.rememberEagleFonts

/** Windows at least this wide get the two-column desktop layout. */
private val WideBreakpoint = 860.dp

@Composable
fun App() {
    val fonts = rememberEagleFonts()
    CompositionLocalProvider(LocalFonts provides fonts) {
        BoxWithConstraints(Modifier.fillMaxSize().background(EagleColors.Ink)) {
            val density = LocalDensity.current
            val wide = maxWidth >= WideBreakpoint
            val gutter = when {
                maxWidth >= 1200.dp -> 48.dp
                wide -> 40.dp
                else -> 20.dp
            }
            val viewportPx = with(density) { maxHeight.toPx() }
            val navPx = with(density) { NavHeight.toPx() }

            val scroll = rememberScrollState()
            val scope = rememberCoroutineScope()
            val content = remember { ContentRef() }
            val anchors = remember { HashMap<String, Float>() }

            val env = remember(wide, gutter, viewportPx, navPx, scroll) {
                PageEnv(
                    wide = wide,
                    gutter = gutter,
                    viewportPx = viewportPx,
                    scroll = scroll,
                    content = content,
                    anchors = anchors,
                    navigate = { key ->
                        val target = if (key == "top") 0f else (anchors[key] ?: 0f) - navPx
                        scope.launch {
                            scroll.animateScrollTo(
                                value = target.toInt().coerceAtLeast(0),
                                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                            )
                        }
                    },
                )
            }

            CompositionLocalProvider(LocalEnv provides env) {
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
                        // Node offsets are measured against this column, which moves with the scroll.
                        Column(Modifier.onPlaced { content.coordinates = it }) {
                            Hero()
                            TrailSection()
                            ExplorerSection()
                            PrinciplesSection()
                            CallToAction()
                            Footer()
                        }
                    }
                    NavBar(Modifier.align(Alignment.TopCenter))
                }
            }
        }
    }
}
