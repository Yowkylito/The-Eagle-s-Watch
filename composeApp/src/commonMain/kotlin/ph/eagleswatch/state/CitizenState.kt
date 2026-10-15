package ph.eagleswatch.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ph.eagleswatch.content.Day
import ph.eagleswatch.content.Report
import ph.eagleswatch.content.Subject

/**
 * What this citizen has done on this device: the projects they watch and the reports they filed.
 * Saved locally so it is there on the next visit; nothing leaves the device in the preview.
 */
@Stable
class CitizenState {
    var watched: Set<String> by mutableStateOf(readPref(KEY_WATCHED).orEmpty().split(",").filter { it.isNotBlank() }.toSet())
        private set

    /** Reports filed from this device, newest first. */
    var reports: List<Report> by mutableStateOf(readReports())
        private set

    fun isWatching(projectId: String): Boolean = projectId in watched

    fun toggleWatch(projectId: String) {
        watched = if (projectId in watched) watched - projectId else watched + projectId
        writePref(KEY_WATCHED, watched.joinToString(","))
    }

    fun addReport(report: Report) {
        reports = listOf(report.cleaned()) + reports
        writePref(KEY_REPORTS, reports.joinToString(RECORD) { it.encode() })
    }

    private companion object {
        const val KEY_REPORTS = "reports"
        const val KEY_WATCHED = "watched"

        // ASCII record and unit separators: they cannot be typed, so they never clash with report text.
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
    }
}
