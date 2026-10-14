package ph.eagleswatch.state

import androidx.compose.runtime.Composable

/** Opens the platform's share sheet, or copies the text where there is none. */
internal expect fun shareText(title: String, text: String)

/** Runs [onBack] for the system back gesture while [enabled]. Only Android has one. */
@Composable
internal expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
