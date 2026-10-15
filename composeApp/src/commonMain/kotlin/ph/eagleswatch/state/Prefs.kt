package ph.eagleswatch.state

/**
 * A few small strings kept on the device between visits: the citizen's tax amount and how far they got
 * along the trail. Nothing leaves the device. Reads return null when storage is unavailable.
 */
internal expect fun readPref(key: String): String?

internal expect fun writePref(key: String, value: String)
