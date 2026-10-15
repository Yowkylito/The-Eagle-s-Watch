package ph.eagleswatch.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Layout facts shared by every screen.
 *
 * @property wide true on tablet and desktop-width windows: top tabs, two-column pages and a grid of rows.
 * Phones get a bottom navigation bar and a single column.
 * @property gutter horizontal page padding.
 */
@Immutable
class Layout(val wide: Boolean, val gutter: Dp) {
    companion object {
        /** Windows at least this wide use the wide layout. */
        val WideBreakpoint = 860.dp
    }
}

val LocalLayout = staticCompositionLocalOf { Layout(wide = false, gutter = 16.dp) }
