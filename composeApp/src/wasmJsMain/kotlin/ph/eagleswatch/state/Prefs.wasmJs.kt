@file:OptIn(ExperimentalWasmJsInterop::class)

package ph.eagleswatch.state

// localStorage throws in some private-browsing modes, so both calls swallow errors.

private fun storageGet(key: String): String? =
    js("(function (k) { try { return window.localStorage.getItem(k); } catch (e) { return null; } })(key)")

private fun storageSet(key: String, value: String): Unit =
    js("(function (k, v) { try { window.localStorage.setItem(k, v); } catch (e) {} })(key, value)")

internal actual fun readPref(key: String): String? = storageGet("eagles_watch.$key")

internal actual fun writePref(key: String, value: String) = storageSet("eagles_watch.$key", value)
