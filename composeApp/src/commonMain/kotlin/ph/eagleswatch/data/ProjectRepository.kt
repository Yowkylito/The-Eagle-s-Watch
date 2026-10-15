package ph.eagleswatch.data

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ph.eagleswatch.content.Day
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.SAMPLE_AS_OF
import ph.eagleswatch.content.sampleProjects

/** A set of project records and where they stand. */
data class Dataset(
    val name: String,
    val projects: List<Project>,
    /** When the dataset was last refreshed from its sources. */
    val asOf: Day,
    /** True when every record is a development fixture. */
    val sample: Boolean,
    val note: String,
) {
    fun byId(id: String): Project? = projects.firstOrNull { it.id == id }
}

sealed interface LoadState {
    data object Loading : LoadState
    data class Ready(val dataset: Dataset) : LoadState
    data class Failed(val message: String) : LoadState
}

/** Where project records come from. A live implementation will fetch and cache them; the preview uses the fixture. */
interface ProjectRepository {
    suspend fun load(): Dataset
}

/** Development fixture. Every record is fictional and the UI labels it as such. */
object SampleProjectRepository : ProjectRepository {
    override suspend fun load(): Dataset = Dataset(
        name = "Sample dataset",
        projects = sampleProjects,
        asOf = SAMPLE_AS_OF,
        sample = true,
        note = "Ten fictional records for design and testing. They do not describe real projects, contracts or people.",
    )
}

/** Holds the loaded records for the whole app, with retry. */
@Stable
class ProjectStore(private val repository: ProjectRepository) {
    var state: LoadState by mutableStateOf(LoadState.Loading)
        private set

    /** Bumped on every retry so the loader runs again. */
    var attempt: Int by mutableIntStateOf(0)
        private set

    val projects: List<Project>
        get() = (state as? LoadState.Ready)?.dataset?.projects.orEmpty()

    fun byId(id: String): Project? = (state as? LoadState.Ready)?.dataset?.byId(id)

    suspend fun load() {
        state = LoadState.Loading
        state = try {
            LoadState.Ready(repository.load())
        } catch (e: Exception) {
            LoadState.Failed(e.message ?: "The project records could not be loaded.")
        }
    }

    fun retry() {
        attempt += 1
    }
}
