package ph.eagleswatch.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ph.eagleswatch.content.Day
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.Report
import ph.eagleswatch.content.Subject
import ph.eagleswatch.content.projects

enum class Reaction { Like, Dislike }

/** Feedback this citizen wrote, kept on their device until the app has accounts and a server. */
data class Note(val day: Day, val text: String)

/**
 * What this citizen has done: projects they watch, their like or dislike on each, and the feedback they
 * wrote. Saved on the device so it is there on the next visit.
 */
@Stable
class CitizenState {
    var watched: Set<String> by mutableStateOf(
        readPref(KEY_WATCHED).orEmpty().split(",").filter { id -> projects.any { it.id == id } }.toSet(),
    )
        private set

    var reactions: Map<String, Reaction> by mutableStateOf(readReactions())
        private set

    var notes: Map<String, List<Note>> by mutableStateOf(projects.associate { it.id to readNotes(it.id) }.filterValues { it.isNotEmpty() })
        private set

    /** Reports filed from this device, newest first. */
    var reports: List<Report> by mutableStateOf(readReports())
        private set

    fun isWatching(project: Project): Boolean = project.id in watched

    fun toggleWatch(project: Project) {
        watched = if (project.id in watched) watched - project.id else watched + project.id
        writePref(KEY_WATCHED, watched.joinToString(","))
    }

    /** Tapping the same reaction again takes it back. */
    fun react(project: Project, reaction: Reaction) {
        reactions = if (reactions[project.id] == reaction) reactions - project.id else reactions + (project.id to reaction)
        writePref(KEY_REACTIONS, reactions.entries.joinToString(",") { "${it.key}:${it.value.name}" })
    }

    fun likes(project: Project): Int = project.likes + if (reactions[project.id] == Reaction.Like) 1 else 0

    fun dislikes(project: Project): Int = project.dislikes + if (reactions[project.id] == Reaction.Dislike) 1 else 0

    /** Share of reactions that are likes, 0 to 1. */
    fun approval(project: Project): Float {
        val likes = likes(project)
        val total = likes + dislikes(project)
        return if (total == 0) 0f else likes.toFloat() / total
    }

    fun addNote(project: Project, text: String) {
        val clean = text.filter { it >= ' ' || it == '\n' }.trim()
        if (clean.isEmpty()) return
        val list = listOf(Note(Day.today(), clean)) + notes[project.id].orEmpty()
        notes = notes + (project.id to list)
        writePref(KEY_NOTES + project.id, list.joinToString(RECORD) { "${it.day.epochDay}$FIELD${it.text}" })
    }

    fun addReport(report: Report) {
        reports = listOf(report.cleaned()) + reports
        writePref(KEY_REPORTS, reports.joinToString(RECORD) { it.encode() })
    }

    private companion object {
        const val KEY_REPORTS = "reports"
        const val KEY_WATCHED = "watched"
        const val KEY_REACTIONS = "reactions"
        const val KEY_NOTES = "notes."

        // ASCII record and unit separators: they cannot be typed, so they never clash with feedback text.
        const val RECORD = "\u001E"
        const val FIELD = "\u001F"
        const val GROUP = "\u001D"

        fun String.clean(): String = filter { it >= ' ' || it == '\n' }.trim()

        fun Report.cleaned() = copy(
            target = target.clean(),
            involved = involved.clean(),
            issues = issues.map { it.clean() }.filter { it.isNotEmpty() },
            place = place.clean(),
            time = time.clean(),
            details = details.clean(),
            contact = contact?.clean()?.takeIf { it.isNotEmpty() },
        )

        fun Report.encode(): String = listOf(
            reference,
            filed.epochDay.toString(),
            subject.name,
            projectId.orEmpty(),
            target,
            involved,
            issues.joinToString(GROUP),
            place,
            time,
            facts.joinToString(GROUP),
            details,
            contact.orEmpty(),
        ).joinToString(FIELD)

        fun readReports(): List<Report> =
            readPref(KEY_REPORTS).orEmpty().split(RECORD).mapNotNull { record ->
                val f = record.split(FIELD)
                if (f.size != 12) return@mapNotNull null
                val subject = Subject.entries.firstOrNull { it.name == f[2] } ?: return@mapNotNull null
                Report(
                    reference = f[0],
                    filed = Day.ofEpochDay(f[1].toLongOrNull() ?: return@mapNotNull null),
                    subject = subject,
                    projectId = f[3].ifEmpty { null },
                    target = f[4],
                    involved = f[5],
                    issues = f[6].split(GROUP).filter { it.isNotEmpty() },
                    place = f[7],
                    time = f[8],
                    facts = f[9].split(GROUP).filter { it.isNotEmpty() },
                    details = f[10],
                    contact = f[11].ifEmpty { null },
                )
            }

        fun readReactions(): Map<String, Reaction> =
            readPref(KEY_REACTIONS).orEmpty().split(",").mapNotNull { entry ->
                val (id, name) = entry.split(":").takeIf { it.size == 2 } ?: return@mapNotNull null
                val reaction = Reaction.entries.firstOrNull { it.name == name } ?: return@mapNotNull null
                id to reaction
            }.toMap()

        fun readNotes(id: String): List<Note> =
            readPref(KEY_NOTES + id).orEmpty().split(RECORD).mapNotNull { record ->
                val parts = record.split(FIELD, limit = 2)
                val day = parts.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
                val text = parts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                Note(Day.ofEpochDay(day), text)
            }
    }
}
