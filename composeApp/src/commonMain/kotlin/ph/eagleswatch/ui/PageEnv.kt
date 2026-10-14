package ph.eagleswatch.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp

/** Holds the coordinates of the scrolling content column, set during placement. */
class ContentRef {
    var coordinates: LayoutCoordinates? = null
}

/**
 * Layout facts and scroll state shared by every section.
 *
 * @property wide true on tablet / desktop-width windows.
 * @property viewportPx height of the visible window in pixels (drives scroll-linked reveals).
 * @property content the scrolling content column; node offsets are measured relative to it.
 * @property anchors content-space Y of each named section, used for in-page navigation.
 */
@Stable
class PageEnv(
    val wide: Boolean,
    val gutter: Dp,
    val viewportPx: Float,
    val scroll: ScrollState,
    val content: ContentRef,
    val anchors: HashMap<String, Float>,
    val navigate: (String) -> Unit,
) {
    /**
     * Y of [node]'s top edge within the scrolling content, independent of the current scroll offset.
     * Returns [Float.MAX_VALUE] until both nodes are attached, which reads as "not on screen yet".
     */
    fun contentY(node: LayoutCoordinates): Float {
        val container = content.coordinates
        return if (container != null && container.isAttached && node.isAttached) {
            container.localPositionOf(node, Offset.Zero).y
        } else {
            Float.MAX_VALUE
        }
    }
}

val LocalEnv = compositionLocalOf<PageEnv> { error("PageEnv not provided") }

/** Registers this node's content-space Y under [key] so [PageEnv.navigate] can scroll to it. */
@Composable
fun Modifier.anchored(key: String): Modifier {
    val env = LocalEnv.current
    return onGloballyPositioned { coordinates ->
        val y = env.contentY(coordinates)
        if (y != Float.MAX_VALUE) env.anchors[key] = y
    }
}
