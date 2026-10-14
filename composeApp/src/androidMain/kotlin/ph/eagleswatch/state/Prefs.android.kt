package ph.eagleswatch.state

import android.content.Context
import android.content.SharedPreferences

private var prefs: SharedPreferences? = null

/** Called from MainActivity before the UI starts. */
fun initPrefs(context: Context) {
    prefs = context.applicationContext.getSharedPreferences("eagles_watch", Context.MODE_PRIVATE)
}

internal actual fun readPref(key: String): String? = prefs?.getString(key, null)

internal actual fun writePref(key: String, value: String) {
    prefs?.edit()?.putString(key, value)?.apply()
}
