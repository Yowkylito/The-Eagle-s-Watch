package ph.eagleswatch.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ph.eagleswatch.content.Allocation
import ph.eagleswatch.content.DEFAULT_TAX_PESOS
import ph.eagleswatch.content.MAX_TAX_PESOS
import ph.eagleswatch.content.MIN_TAX_PESOS
import ph.eagleswatch.content.trail

/**
 * What the citizen has done on the Trace screen: the tax amount they follow, the levels they have reached,
 * and the calls they made about where most of it goes. Saved on the device so a return visit picks up
 * where the last one stopped.
 */
@Stable
class TraceProgress {
    /** Pesos the citizen follows up the trail. */
    var pesos by mutableLongStateOf(
        readPref(KEY_PESOS)?.toLongOrNull()?.coerceIn(MIN_TAX_PESOS, MAX_TAX_PESOS) ?: DEFAULT_TAX_PESOS,
    )
        private set

    /** Indices of trail levels the citizen has landed on. */
    var visited: Set<Int> by mutableStateOf(readIntSet(KEY_VISITED))
        private set

    /** Level index to the allocation index the citizen guessed gets the most. */
    var calls: Map<Int, Int> by mutableStateOf(readCalls())
        private set

    /** Levels whose split has been shown, with or without a call. */
    var revealed: Set<Int> by mutableStateOf(readIntSet(KEY_REVEALED))
        private set

    /** True when this device has been here before and got past the first level. */
    val returning: Boolean = visited.any { it > 0 }

    /** Levels that hold a split to call, i.e. every level after the citizen. */
    val callable: Int = trail.count { it.sample != null }

    val correctCalls: Int
        get() = calls.count { (level, pick) -> pick == trail[level].sample?.let(::largestIndex) }

    fun updatePesos(value: Long) {
        val clamped = value.coerceIn(MIN_TAX_PESOS, MAX_TAX_PESOS)
        if (clamped == pesos) return
        pesos = clamped
        writePref(KEY_PESOS, clamped.toString())
    }

    fun visit(level: Int) {
        if (level in visited) return
        visited = visited + level
        writePref(KEY_VISITED, visited.joinToString(","))
    }

    fun call(level: Int, pick: Int) {
        if (level in revealed) return
        calls = calls + (level to pick)
        writePref(KEY_CALLS, calls.entries.joinToString(",") { "${it.key}:${it.value}" })
        reveal(level)
    }

    fun reveal(level: Int) {
        if (level in revealed) return
        revealed = revealed + level
        writePref(KEY_REVEALED, revealed.joinToString(","))
    }

    /** Clears calls and reveals so the trail can be played again. The amount and visited levels stay. */
    fun resetCalls() {
        calls = emptyMap()
        revealed = emptySet()
        writePref(KEY_CALLS, "")
        writePref(KEY_REVEALED, "")
    }

    private companion object {
        const val KEY_PESOS = "pesos"
        const val KEY_VISITED = "visited"
        const val KEY_CALLS = "calls"
        const val KEY_REVEALED = "revealed"

        fun readIntSet(key: String): Set<Int> =
            readPref(key).orEmpty().split(",").mapNotNull { it.toIntOrNull() }.filter { it in trail.indices }.toSet()

        fun readCalls(): Map<Int, Int> =
            readPref(KEY_CALLS).orEmpty().split(",").mapNotNull { entry ->
                val parts = entry.split(":")
                val level = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                val pick = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                val size = trail.getOrNull(level)?.sample?.size ?: return@mapNotNull null
                if (pick in 0 until size) level to pick else null
            }.toMap()
    }
}

/** Index of the allocation that gets the most pesos. The first one wins a tie. */
fun largestIndex(sample: List<Allocation>): Int =
    sample.indices.maxBy { sample[it].pesos }
