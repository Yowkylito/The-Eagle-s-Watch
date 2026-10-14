package ph.eagleswatch.state

import androidx.compose.runtime.Composable
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

internal actual fun shareText(title: String, text: String) {
    val sheet = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
    UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(sheet, animated = true, completion = null)
}

/** iOS uses the edge swipe of its own navigation, which this app does not have, so there is nothing to hook. */
@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
