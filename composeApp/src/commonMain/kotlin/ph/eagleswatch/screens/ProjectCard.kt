package ph.eagleswatch.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Day
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.Status
import ph.eagleswatch.content.Verification
import ph.eagleswatch.ui.Card
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.IconButton
import ph.eagleswatch.ui.Stat
import ph.eagleswatch.ui.StatusTag
import ph.eagleswatch.ui.Tag
import ph.eagleswatch.ui.Tone
import ph.eagleswatch.ui.VerificationTag
import ph.eagleswatch.ui.WorkTrack
import ph.eagleswatch.ui.compactPesos
import ph.eagleswatch.ui.days
import ph.eagleswatch.ui.fill
import ph.eagleswatch.ui.glyph
import ph.eagleswatch.ui.percent

/** Statuses for which comparing work done against time used means something. */
val Status.active: Boolean get() = this == Status.Ongoing || this == Status.Delayed || this == Status.Suspended

/** How much work is reported behind (positive) or ahead of (negative) the share of contract time used. */
fun scheduleGap(project: Project, today: Day): Float? {
    if (!project.status.active) return null
    val time = project.timeElapsed(today) ?: return null
    val work = (project.progress.reported?.percent ?: 0) / 100f
    return time - work
}

/** The one word that says how well the record is supported: the best check its progress figure has had. */
fun recordVerification(project: Project): Verification {
    val independent = project.progress.independent
    val reported = project.progress.reported
    return when {
        independent != null -> independent.attribution.verification
        reported != null -> reported.attribution.verification
        else -> project.descriptionAttribution.verification
    }
}

/** A short signal about the schedule, worth a chip, or null when there is nothing to flag. */
data class ScheduleSignal(val text: String, val tone: Tone, val glyph: Glyph)

fun scheduleSignal(project: Project, today: Day): ScheduleSignal? {
    val s = project.schedule
    val amber = Tone(EagleColors.Amber, EagleColors.AmberSoft)
    val green = Tone(EagleColors.Green, EagleColors.GreenSoft)
    if (project.status == Status.Completed) {
        val actual = s.actualEnd ?: return null
        val target = s.targetEnd ?: return null
        val late = target.daysUntil(actual)
        return if (late > 0) ScheduleSignal("Finished ${days(late)} late", amber, Glyph.Clock) else ScheduleSignal("Finished on time", green, Glyph.CheckCircle)
    }
    if (s.revisedEnd != null && s.targetEnd != null && s.revisedEnd > s.targetEnd) {
        return ScheduleSignal("Deadline moved to ${s.revisedEnd.format()}", amber, Glyph.Calendar)
    }
    val gap = scheduleGap(project, today) ?: return null
    val points = (gap * 100).toInt()
    return if (points > 10) ScheduleSignal("Work $points pts behind schedule", amber, Glyph.Clock) else null
}

/** The figure and label for the third slot of the stat strip, which depends on where the project stands. */
private fun thirdStat(project: Project, today: Day): Pair<String, String> {
    val s = project.schedule
    return when {
        project.status == Status.Completed -> (s.actualEnd?.monthYear() ?: "—") to "Completed"
        project.status == Status.Planned -> (s.start?.monthYear() ?: "—") to "Planned start"
        else -> (project.timeElapsed(today)?.let { percent(it) } ?: "—") to "Time used"
    }
}

/**
 * A project in a list. Status and type first, then the title and where it is, a stat strip (contract,
 * work done, time used), a track comparing work against time, a schedule signal, and who is responsible.
 */
@Composable
fun ProjectCard(
    project: Project,
    today: Day,
    watching: Boolean,
    onOpen: () -> Unit,
    onToggleWatch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reported = project.progress.reported
    val work = reported?.let { it.percent / 100f }
    val elapsed = project.timeElapsed(today)
    val (thirdValue, thirdLabel) = thirdStat(project, today)
    val signal = scheduleSignal(project, today)

    Card(modifier, onClick = onOpen, description = "Open ${project.title}") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusTag(project.status)
            Spacer(Modifier.width(8.dp))
            Icon(project.category.glyph, tint = EagleColors.Muted, size = 14.dp, weight = 2f)
            Spacer(Modifier.width(4.dp))
            EText(
                "${project.category.label} · ${project.level.label}",
                modifier = Modifier.weight(1f),
                size = EagleType.Caption,
                weight = FontWeight.Medium,
                color = EagleColors.Muted,
                maxLines = 1,
                ellipsis = true,
            )
            IconButton(
                glyph = if (watching) Glyph.EyeFilled else Glyph.Eye,
                description = if (watching) "Stop watching ${project.title}" else "Watch ${project.title}",
                onClick = onToggleWatch,
                modifier = Modifier.offset(x = 12.dp),
                tint = if (watching) EagleColors.Accent else EagleColors.Muted,
                iconSize = 20.dp,
                toggled = watching,
            )
        }
        EText(project.title, display = true, size = 18.sp, lineHeight = 23.sp, weight = FontWeight.SemiBold, maxLines = 2, ellipsis = true)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Glyph.Pin, tint = EagleColors.Muted, size = 14.dp, weight = 2f)
            Spacer(Modifier.width(5.dp))
            EText(project.place.full, size = EagleType.Label, lineHeight = 18.sp, color = EagleColors.Body, maxLines = 1, ellipsis = true)
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Stat(
                value = project.contract.amount?.let { compactPesos(it) } ?: "—",
                label = if (project.status == Status.Planned) "Proposed" else "Contract",
                modifier = Modifier.weight(1.1f),
            )
            Stat(
                value = work?.let { percent(it) } ?: "—",
                label = "Work done",
                modifier = Modifier.weight(1f),
            )
            Stat(value = thirdValue, label = thirdLabel, modifier = Modifier.weight(1f))
        }
        if (project.status != Status.Planned) {
            Spacer(Modifier.height(10.dp))
            WorkTrack(
                work = work,
                color = project.status.fill,
                time = if (project.status.active) elapsed else null,
                modifier = Modifier.semantics {
                    contentDescription = buildString {
                        append(work?.let { "${percent(it)} of work reported" } ?: "No progress figure on file")
                        if (project.status.active && elapsed != null) append(", ${percent(elapsed)} of contract time used")
                    }
                },
            )
        }
        if (signal != null) {
            Spacer(Modifier.height(10.dp))
            Tag(signal.text, color = signal.tone.color, background = signal.tone.soft, glyph = signal.glyph)
        }

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Glyph.Agency, tint = EagleColors.Muted, size = 14.dp, weight = 2f)
            Spacer(Modifier.width(6.dp))
            EText(
                project.implementingAgency?.name ?: "Implementing agency not on file",
                modifier = Modifier.weight(1f),
                size = EagleType.Caption,
                color = EagleColors.Muted,
                maxLines = 1,
                ellipsis = true,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            VerificationTag(recordVerification(project))
            Spacer(Modifier.width(8.dp))
            EText(
                text = listOfNotNull(
                    "${project.documents.size} docs",
                    (reported?.reportedOn ?: project.latestUpdate?.date)?.format(),
                ).joinToString(" · "),
                modifier = Modifier.weight(1f).padding(end = 2.dp),
                size = EagleType.Caption,
                color = EagleColors.Muted,
                maxLines = 1,
                ellipsis = true,
                align = androidx.compose.ui.text.style.TextAlign.End,
            )
        }
    }
}
