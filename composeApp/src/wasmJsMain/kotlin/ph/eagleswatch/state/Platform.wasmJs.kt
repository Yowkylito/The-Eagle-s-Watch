@file:OptIn(ExperimentalWasmJsInterop::class)

package ph.eagleswatch.state

import androidx.compose.runtime.Composable

private fun browserShare(title: String, text: String): Unit = js(
    """(function (t, x) {
        try {
            if (navigator.share) { navigator.share({ title: t, text: x }).catch(function () {}); }
            else if (navigator.clipboard) { navigator.clipboard.writeText(x); }
        } catch (e) {}
    })(title, text)""",
)

internal actual fun shareText(title: String, text: String) = browserShare(title, text)

/** The browser's own back button leaves the page; in-app back is the on-screen button. */
@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
