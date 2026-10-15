package ph.eagleswatch.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput

/** TEMPORARY: logs where presses land, to diagnose misrouted taps. Remove after diagnosis. */
fun Modifier.debugTaps(tag: String): Modifier = pointerInput(tag) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            if (event.type == PointerEventType.Press) {
                val p = event.changes.firstOrNull()?.position
                println("EW-DEBUG press [$tag] at ${p?.x}, ${p?.y} size=$size")
            }
        }
    }
}
