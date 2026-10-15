package ph.eagleswatch.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Hairline
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.IconButton
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.Locator
import ph.eagleswatch.ui.Meter
import ph.eagleswatch.ui.StatusTag
import ph.eagleswatch.ui.compactPesos
import ph.eagleswatch.ui.glyph
import ph.eagleswatch.ui.percent
import ph.eagleswatch.ui.tappable
import ph.eagleswatch.ui.tone

/** Statuses for which comparing work done against time used means something. */
val Status.active: Boolean get() = this == Status.Ongoing || this == Status.Delayed || this == Status.Suspended

/** "Mar 2025 → Dec 2026 target", "Jul 2025 → Nov 2026 revised, was Jun 2026", "Apr 2025 → completed Mar 6, 2026". */
fun scheduleSummary(project: Project): String {
    val s = project.schedule
    val start = s.start?.monthYear() ?: "Start not on file"
    return when {
        s.actualEnd != null -> "$start → completed ${s.actualEnd.format()}"
        s.revisedEnd != null -> "$start → ${s.revisedEnd.monthYear()} revised" + (s.targetEnd?.let { ", was ${it.monthYear()}" } ?: "")
        s.targetEnd != null -> "$start → ${s.targetEnd.monthYear()} target"
        else -> "$start → end date not on file"
    }
}

/** How much work is reported ahead of, or behind, the share of contract time used. Null when it does not apply. */
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

/**
 * A project in a list: status and reported progress first, then the title, where it is, how much and who,
 * the dates, a meter of work against time, and the evidence line. A locator shows where in the country it sits.
 */
@Composable
fun ProjectRow(
    project: Project,
    today: Day,
    watching: Boolean,
    onOpen: () -> Unit,
    onToggleWatch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = LocalLayout.current
    val reported = project.progress.reported
    val elapsed = project.timeElapsed(today)
    Column(modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .tappable(onClick = onOpen, description = "Open ${project.title}")
                .padding(start = layout.gutter, end = layout.gutter - 8.dp, top = 10.dp, bottom = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f).padding(top = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusTag(project.status, detail = reported?.let { "${it.percent}%" })
                        if (reported != null) {
                            Spacer(Modifier.width(8.dp))
                            EText(
                                text = "reported ${reported.reportedOn.format()}",
                                modifier = Modifier.weight(1f),
                                size = EagleType.Caption,
                                color = EagleColors.Muted,
                                maxLines = 1,
                                ellipsis = true,
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    EText(
                        text = project.title,
                        size = EagleType.Title,
                        lineHeight = 22.sp,
                        weight = FontWeight.SemiBold,
                        maxLines = 2,
                        ellipsis = true,
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(project.category.glyph, tint = EagleColors.Muted, size = 14.dp, weight = 2f)
                        Spacer(Modifier.width(5.dp))
                        EText(
                            text = "${project.category.label} · ${project.place.full}",
                            size = EagleType.Secondary,
                            lineHeight = 19.sp,
                            color = EagleColors.Body,
                            maxLines = 2,
                            ellipsis = true,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        EText(
                            text = project.contract.amount?.let { compactPesos(it) } ?: "No amount on file",
                            size = EagleType.Body,
                            weight = FontWeight.SemiBold,
                            color = if (project.contract.amount != null) EagleColors.Ink else EagleColors.Muted,
                            tabular = true,
                            maxLines = 1,
                        )
                        Spacer(Modifier.width(6.dp))
                        EText(
                            text = project.implementingAgency?.name ?: "Implementing agency not on file",
                            modifier = Modifier.weight(1f),
                            size = EagleType.Secondary,
                            lineHeight = 19.sp,
                            color = EagleColors.Body,
                            maxLines = 1,
                            ellipsis = true,
                        )
                    }
                    EText(scheduleSummary(project), size = EagleType.Caption, lineHeight = 17.sp, color = EagleColors.Muted, maxLines = 1, ellipsis = true)
                }
                Spacer(Modifier.width(8.dp))
                // Right rail: the watch control on top, the locator under it.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        glyph = if (watching) Glyph.EyeFilled else Glyph.Eye,
                        description = if (watching) "Stop watching ${project.title}" else "Watch ${project.title}",
                        onClick = onToggleWatch,
                        tint = if (watching) EagleColors.Blue else EagleColors.Body,
                        iconSize = 20.dp,
                        toggled = watching,
                    )
                    Spacer(Modifier.height(2.dp))
                    Locator(
                        point = project.place.coordinates,
                        modifier = Modifier.width(36.dp),
                        description = if (project.place.coordinates != null) "Located in ${project.place.short}" else "Location not mapped",
                    )
                }
            }
            if (project.status.active || reported != null) {
                Spacer(Modifier.height(8.dp))
                val work = reported?.let { it.percent / 100f }
                Meter(
                    fraction = work,
                    modifier = Modifier.padding(end = 8.dp).semantics {
                        contentDescription = meterDescription(work, elapsed, project.status)
                    },
                    color = project.status.tone.color,
                    marker = if (project.status.active) elapsed else null,
                )
                EText(
                    text = meterDescription(work, elapsed, project.status),
                    size = EagleType.Caption,
                    lineHeight = 16.sp,
                    color = EagleColors.Muted,
                    maxLines = 1,
                    ellipsis = true,
                )
            }
            Spacer(Modifier.height(6.dp))
            EvidenceLine(project)
        }
        Hairline()
    }
}

private fun meterDescription(work: Float?, elapsed: Float?, status: Status): String {
    val workText = if (work != null) "${percent(work)} of work reported" else "No progress figure on file"
    return if (status.active && elapsed != null) "$workText · ${percent(elapsed)} of contract time used" else workText
}

/** "Agency-reported · 3 documents · no photos · checked Oct 4, 2026" */
@Composable
private fun EvidenceLine(project: Project) {
    val verification = recordVerification(project)
    val parts = listOf(
        verification.label,
        when (val n = project.documents.size) {
            0 -> "no documents"
            1 -> "1 document"
            else -> "$n documents"
        },
        when (val n = project.photos.size) {
            0 -> "no photos"
            1 -> "1 photo"
            else -> "$n photos"
        },
        project.lastChecked?.let { "checked ${it.format()}" } ?: "not yet checked",
    )
    Row(Modifier.padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
        Icon(verification.glyph, tint = verification.tone.color, size = 13.dp, weight = 2.2f)
        Spacer(Modifier.width(5.dp))
        EText(parts.joinToString(" · "), size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted, maxLines = 1, ellipsis = true)
    }
}
