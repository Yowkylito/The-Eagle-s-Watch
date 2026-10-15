package ph.eagleswatch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Attribution
import ph.eagleswatch.content.Coordinates
import ph.eagleswatch.content.Day
import ph.eagleswatch.content.PartyRole
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.ReportPreset
import ph.eagleswatch.content.Status
import ph.eagleswatch.content.Verification
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.state.shareText
import ph.eagleswatch.ui.Button
import ph.eagleswatch.ui.ButtonStyle
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleMetrics
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.FactGrid
import ph.eagleswatch.ui.FactItem
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Hairline
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.IconButton
import ph.eagleswatch.ui.LinkRow
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.Locator
import ph.eagleswatch.ui.Meter
import ph.eagleswatch.ui.MeterRow
import ph.eagleswatch.ui.Notice
import ph.eagleswatch.ui.NoticeTone
import ph.eagleswatch.ui.ProvenanceLine
import ph.eagleswatch.ui.SectionLabel
import ph.eagleswatch.ui.StatusTag
import ph.eagleswatch.ui.VerificationTag
import ph.eagleswatch.ui.compactPesos
import ph.eagleswatch.ui.days
import ph.eagleswatch.ui.fixed
import ph.eagleswatch.ui.formatCoordinates
import ph.eagleswatch.ui.formatPesos
import ph.eagleswatch.ui.glyph
import ph.eagleswatch.ui.percent
import ph.eagleswatch.ui.tone

/** A public map page for the coordinates; opens in the browser or a maps app on every platform. */
private fun mapUrl(c: Coordinates): String {
    val lat = fixed(c.lat, 5)
    val lon = fixed(c.lon, 5)
    return "https://www.openstreetmap.org/?mlat=$lat&mlon=$lon#map=15/$lat/$lon"
}

private fun shareSummary(project: Project): String {
    val reported = project.progress.reported
    val lines = listOf(
        "${project.title} · ${project.place.full}",
        project.status.label + (reported?.let { " · ${it.percent}% physical progress reported ${it.reportedOn.format()}" } ?: ""),
        listOfNotNull(project.contract.amount?.let { "${formatPesos(it)} approved" }, project.implementingAgency?.name).joinToString(" · "),
        if (project.sample) "Sample record on The Eagle's Watch, not a real project." else "Seen on The Eagle's Watch.",
    )
    return lines.filter { it.isNotBlank() }.joinToString("\n")
}

/**
 * Everything on file about one project, in the order a sceptical reader asks for it: what and where,
 * the contract, the location, progress with its sources, the dated timeline, the evidence, who is
 * responsible, where the data came from, and how to correct it.
 */
@Composable
fun ProjectScreen(
    project: Project,
    citizen: CitizenState,
    onBack: () -> Unit,
    onReport: (ReportPreset?) -> Unit,
) {
    val layout = LocalLayout.current
    val today = remember { Day.today() }
    val watching = citizen.isWatching(project.id)

    Column(Modifier.fillMaxSize().background(EagleColors.Page)) {
        PageBar(
            onBack = onBack,
            actions = {
                IconButton(Glyph.Share, "Share this record", onClick = { shareText(project.title, shareSummary(project)) }, iconSize = 20.dp)
                IconButton(Glyph.Flag, "Report a problem with this project", onClick = { onReport(null) }, iconSize = 20.dp)
                IconButton(
                    glyph = if (watching) Glyph.EyeFilled else Glyph.Eye,
                    description = if (watching) "Stop watching this project" else "Watch this project",
                    onClick = { citizen.toggleWatch(project.id) },
                    tint = if (watching) EagleColors.Blue else EagleColors.Ink,
                    iconSize = 20.dp,
                    toggled = watching,
                    label = if (watching) "Watching" else "Watch",
                )
            },
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            ContentColumn(max = if (layout.wide) 1080.dp else EagleMetrics.ContentMax) {
                if (layout.wide) {
                    Row(Modifier.padding(horizontal = layout.gutter)) {
                        Column(Modifier.weight(1.45f)) {
                            Header(project)
                            ProgressSection(project, today, onReport)
                            TimelineSection(project, today)
                            EvidenceSection(project)
                            PartiesSection(project)
                            ProvenanceSection(project)
                            ReportSection(project, onReport)
                        }
                        Spacer(Modifier.width(40.dp))
                        Column(Modifier.weight(1f)) {
                            Spacer(Modifier.height(12.dp))
                            ContractSection(project)
                            LocationSection(project)
                        }
                    }
                } else {
                    Column(Modifier.padding(horizontal = layout.gutter)) {
                        Header(project)
                        ContractSection(project)
                        LocationSection(project)
                        ProgressSection(project, today, onReport)
                        TimelineSection(project, today)
                        EvidenceSection(project)
                        PartiesSection(project)
                        ProvenanceSection(project)
                        ReportSection(project, onReport)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 20.dp)) {
        Hairline()
        Spacer(Modifier.height(12.dp))
        SectionLabel(title)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun SubLabel(text: String) {
    EText(text, size = EagleType.Label, weight = FontWeight.SemiBold, color = EagleColors.Ink, modifier = Modifier.padding(bottom = 6.dp), heading = true)
}

@Composable
private fun Header(project: Project) {
    val reported = project.progress.reported
    Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        if (project.sample) {
            Notice(
                text = "Fictional project used to design and test the app. It is not a real government record.",
                tone = NoticeTone.Sample,
                title = "Sample record",
            )
            Spacer(Modifier.height(14.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusTag(project.status, detail = reported?.let { "${it.percent}%" })
            VerificationTag(recordVerification(project))
        }
        Spacer(Modifier.height(10.dp))
        EText(project.title, size = 22.sp, lineHeight = 28.sp, weight = FontWeight.SemiBold, heading = true)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(project.category.glyph, tint = EagleColors.Body, size = 16.dp)
            Spacer(Modifier.width(6.dp))
            EText("${project.category.label} · ${project.level.label} project", size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Glyph.Pin, tint = EagleColors.Body, size = 16.dp)
            Spacer(Modifier.width(6.dp))
            EText(project.place.full, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body)
        }
        Spacer(Modifier.height(10.dp))
        EText(project.description, size = EagleType.Body, lineHeight = 22.sp, color = EagleColors.Ink)
        Spacer(Modifier.height(4.dp))
        ProvenanceLine(project.descriptionAttribution)
    }
}

@Composable
private fun ContractSection(project: Project) {
    val c = project.contract
    val s = project.schedule
    Section("Contract and schedule") {
        FactGrid(
            listOf(
                FactItem("Approved contract amount", c.amount?.let { formatPesos(it) }, note = c.reference?.let { "Ref. $it" }),
                FactItem("Funding source", c.fundingSource),
                FactItem("Implementing agency", project.implementingAgency?.name),
                FactItem("Contractor", project.contractor?.name, note = project.contractor?.detail),
                FactItem("Contract start", s.start?.format()),
                FactItem(
                    label = "Target completion",
                    value = s.targetEnd?.format(),
                    note = s.revisedEnd?.let { "Revised to ${it.format()}" },
                ),
                FactItem(
                    label = "Actual completion",
                    value = s.actualEnd?.format() ?: if (project.status == Status.Completed) null else "Not yet completed",
                    muted = s.actualEnd == null,
                ),
                FactItem("Proposing office", project.proposingOffice?.name ?: project.implementingAgency?.name),
            ),
        )
        Spacer(Modifier.height(12.dp))
        ProvenanceLine(c.attribution, prefix = "Contract")
        Spacer(Modifier.height(4.dp))
        ProvenanceLine(s.attribution, prefix = "Schedule")
    }
}

@Composable
private fun LocationSection(project: Project) {
    val uriHandler = LocalUriHandler.current
    val place = project.place
    Section("Location") {
        Row(verticalAlignment = Alignment.Top) {
            Locator(place.coordinates, Modifier.width(84.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                EText(place.full, size = EagleType.Body, lineHeight = 21.sp, weight = FontWeight.SemiBold)
                EText(place.region, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body)
                Spacer(Modifier.height(6.dp))
                val c = place.coordinates
                if (c != null) {
                    EText(formatCoordinates(c), size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Ink, tabular = true)
                    EText(place.precision.label.replaceFirstChar { it.uppercase() }, size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted)
                    Spacer(Modifier.height(10.dp))
                    Button("Open in maps", onClick = { uriHandler.openUri(mapUrl(c)) }, glyph = Glyph.External, compact = true)
                } else {
                    ProvenanceLine(Attribution(Verification.Unavailable, "No coordinates on file for this record"))
                }
            }
        }
    }
}

@Composable
private fun ProgressSection(project: Project, today: Day, onReport: (ReportPreset?) -> Unit) {
    val reported = project.progress.reported
    val independent = project.progress.independent
    val financial = project.progress.financial
    val amount = project.contract.amount
    val elapsed = project.timeElapsed(today)
    val active = project.status.active
    Section("Progress") {
        if (reported != null) {
            MeterRow(
                label = "Physical progress, as reported",
                value = "${reported.percent}%",
                fraction = reported.percent / 100f,
                color = project.status.tone.color,
                marker = if (active) elapsed else null,
                attribution = reported.attribution,
                note = if (active && elapsed != null) "The tick marks ${percent(elapsed)} of contract time used as of ${today.format()}." else null,
            )
        } else {
            MeterRow(
                label = "Physical progress, as reported",
                value = "Not on file",
                fraction = null,
                attribution = Attribution(Verification.Unavailable, "No progress report on file"),
            )
        }
        Spacer(Modifier.height(16.dp))
        if (independent != null) {
            MeterRow(
                label = "Physical progress, independent check",
                value = "${independent.percent}%",
                fraction = independent.percent / 100f,
                color = independent.attribution.verification.tone.color,
                attribution = independent.attribution,
            )
        } else {
            Column(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.Bottom) {
                    EText("Physical progress, independent check", modifier = Modifier.weight(1f), size = EagleType.Secondary, color = EagleColors.Body)
                    EText("None on file", size = EagleType.Secondary, weight = FontWeight.SemiBold, color = EagleColors.Muted)
                }
                Spacer(Modifier.height(4.dp))
                Meter(null)
                Spacer(Modifier.height(2.dp))
                EText(
                    text = "No site visit, audit finding or citizen report has checked the reported figure.",
                    size = EagleType.Caption,
                    lineHeight = 16.sp,
                    color = EagleColors.Muted,
                )
                Spacer(Modifier.height(4.dp))
                Button("Report what you see at the site", onClick = { onReport(null) }, style = ButtonStyle.Quiet, compact = true, glyph = Glyph.Flag)
            }
        }
        Spacer(Modifier.height(16.dp))
        when {
            financial != null && amount != null && amount > 0 -> {
                val ratio = financial.disbursed.toFloat() / amount
                MeterRow(
                    label = "Funds disbursed",
                    value = "${compactPesos(financial.disbursed)} of ${compactPesos(amount)} · ${percent(ratio)}",
                    fraction = ratio,
                    color = EagleColors.Ink,
                    attribution = financial.attribution,
                )
            }
            financial != null -> MeterRow(
                label = "Funds disbursed",
                value = formatPesos(financial.disbursed),
                fraction = null,
                attribution = financial.attribution,
            )
            else -> MeterRow(
                label = "Funds disbursed",
                value = "Not on file",
                fraction = null,
                attribution = Attribution(Verification.Unavailable, "No disbursement report on file"),
            )
        }
        val start = project.schedule.start
        val end = project.schedule.currentEnd
        if (active && elapsed != null && start != null && end != null) {
            Spacer(Modifier.height(16.dp))
            val total = start.daysUntil(end).coerceAtLeast(1L)
            val used = start.daysUntil(today).coerceIn(0L, total)
            MeterRow(
                label = "Contract time used",
                value = percent(elapsed),
                fraction = elapsed,
                color = EagleColors.LineStrong,
                note = "${days(used)} of ${days(total)} as of ${today.format()}. Ends ${end.format()}" +
                    (if (project.schedule.revisedEnd != null) " (revised)." else "."),
            )
        }
        Spacer(Modifier.height(12.dp))
        EText(progressReading(project, today), size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body)
    }
}

/** One plain sentence on what the figures show, and what they do not. */
private fun progressReading(project: Project, today: Day): String {
    val s = project.schedule
    return when (project.status) {
        Status.Completed -> {
            val actual = s.actualEnd
            val target = s.targetEnd
            if (actual != null && target != null) {
                val diff = target.daysUntil(actual)
                when {
                    diff > 0 -> "Completed ${days(diff)} after the target date, according to the completion record."
                    diff < 0 -> "Completed ${days(-diff)} before the target date, according to the completion record."
                    else -> "Completed on the target date, according to the completion record."
                }
            } else {
                "Reported as completed."
            }
        }
        Status.Planned -> "Works have not started. The contract amount is a proposal until an award is on file."
        Status.Suspended -> "Works are suspended, so the reported figure will not move until they resume. The gap against contract time will widen while the suspension lasts."
        else -> {
            val gap = scheduleGap(project, today)
            val points = gap?.let { (it * 100).toInt() }
            when {
                points == null -> "Reported progress cannot be compared with the schedule because dates are missing."
                points > 10 -> "Reported work is $points points behind the share of contract time used. This compares the agency's own figure with the calendar; it is not an independent finding."
                points < -10 -> "Reported work is ${-points} points ahead of the share of contract time used, according to the agency's own figure."
                else -> "Reported work is keeping pace with the share of contract time used, according to the agency's own figure."
            }
        }
    }
}

private class TimelineEntry(
    val date: Day,
    val title: String,
    val body: String?,
    val percent: Int?,
    val attribution: Attribution?,
    val upcoming: Boolean,
)

private fun timelineEntries(project: Project, today: Day): List<TimelineEntry> {
    val s = project.schedule
    val milestones = listOfNotNull(
        s.start?.let { TimelineEntry(it, if (it > today) "Contract start (scheduled)" else "Contract start", null, null, s.attribution, it > today) },
        s.targetEnd?.let { TimelineEntry(it, if (s.revisedEnd != null) "Original target completion" else "Target completion", null, null, s.attribution, it > today) },
        s.revisedEnd?.let { TimelineEntry(it, "Revised completion date", null, null, s.attribution, it > today) },
        s.actualEnd?.let { TimelineEntry(it, "Completed", null, null, s.attribution, false) },
    )
    val updates = project.updates.map { TimelineEntry(it.date, it.title, it.body, it.percent, it.attribution, false) }
    return (milestones + updates).sortedWith(compareByDescending<TimelineEntry> { it.date.epochDay }.thenBy { it.body == null })
}

@Composable
private fun TimelineSection(project: Project, today: Day) {
    val entries = remember(project, today) { timelineEntries(project, today) }
    var showAll by remember { mutableStateOf(false) }
    val shown = if (showAll || entries.size <= 5) entries else entries.take(5)
    Section("Timeline") {
        shown.forEachIndexed { index, entry ->
            TimelineRow(entry)
            if (index < shown.lastIndex) Hairline()
        }
        if (entries.size > 5) {
            Spacer(Modifier.height(10.dp))
            Button(
                label = if (showAll) "Show fewer" else "Show all ${entries.size} entries",
                onClick = { showAll = !showAll },
                compact = true,
            )
        }
    }
}

@Composable
private fun TimelineRow(entry: TimelineEntry) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.Top) {
        Column(Modifier.width(92.dp)) {
            EText(
                text = entry.date.format(),
                size = EagleType.Label,
                lineHeight = 18.sp,
                weight = FontWeight.SemiBold,
                color = if (entry.upcoming) EagleColors.Muted else EagleColors.Ink,
                tabular = true,
            )
            if (entry.upcoming) EText("upcoming", size = EagleType.Caption, color = EagleColors.Muted)
            if (entry.percent != null) EText("${entry.percent}% work", size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted, tabular = true)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            EText(
                text = entry.title,
                size = EagleType.Body,
                lineHeight = 20.sp,
                weight = FontWeight.SemiBold,
                color = if (entry.upcoming) EagleColors.Muted else EagleColors.Ink,
            )
            if (entry.body != null) {
                Spacer(Modifier.height(2.dp))
                EText(entry.body, size = EagleType.Secondary, lineHeight = 19.sp, color = EagleColors.Body)
            }
            if (entry.attribution != null) {
                Spacer(Modifier.height(4.dp))
                ProvenanceLine(entry.attribution)
            }
        }
    }
}

@Composable
private fun EvidenceSection(project: Project) {
    val uriHandler = LocalUriHandler.current
    Section("Evidence") {
        SubLabel("Site photographs")
        if (project.photos.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Glyph.Photo, tint = EagleColors.Muted, size = 18.dp)
                Spacer(Modifier.width(8.dp))
                EText("No verified site photographs on file.", size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body)
            }
            Spacer(Modifier.height(4.dp))
            EText(
                text = "Photographs appear here with their date, caption and source once an agency posting or a checked citizen report supplies them. Nothing is shown in their place.",
                size = EagleType.Caption,
                lineHeight = 16.sp,
                color = EagleColors.Muted,
            )
        } else {
            project.photos.forEachIndexed { index, photo ->
                LinkRow(
                    title = photo.caption,
                    detail = listOfNotNull(photo.takenOn?.format() ?: "Date not on file", photo.attribution.verification.label, photo.attribution.source).joinToString(" · "),
                    glyph = Glyph.Photo,
                    onClick = { uriHandler.openUri(photo.url) },
                    external = true,
                )
                if (index < project.photos.lastIndex) Hairline()
            }
        }
        Spacer(Modifier.height(16.dp))
        SubLabel("Official documents")
        if (project.documents.isEmpty()) {
            EText("No documents on file.", size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body)
        } else {
            project.documents.forEachIndexed { index, doc ->
                val url = doc.url
                LinkRow(
                    title = doc.title,
                    detail = listOfNotNull(doc.kind.label, doc.date?.format(), doc.attribution.source).joinToString(" · "),
                    glyph = Glyph.Document,
                    onClick = if (url != null) ({ uriHandler.openUri(url) }) else null,
                    external = true,
                    unavailable = "Link not on file",
                )
                if (index < project.documents.lastIndex) Hairline()
            }
        }
        Spacer(Modifier.height(6.dp))
        EText(
            text = "Links open the document on the source's own site. Where no link is on file, the record names the document so it can be requested from the agency.",
            size = EagleType.Caption,
            lineHeight = 16.sp,
            color = EagleColors.Muted,
        )
    }
}

@Composable
private fun PartiesSection(project: Project) {
    Section("Responsible parties") {
        listOf(PartyRole.Implementing, PartyRole.Proposing, PartyRole.Contractor).forEach { role ->
            val party = project.parties.firstOrNull { it.role == role }
            if (party != null || role != PartyRole.Proposing) {
                PartyRow(role.label, party?.name, party?.detail)
            }
        }
        if (project.officials.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            SubLabel("Officials named")
            project.officials.forEach { PartyRow(null, it.name, it.detail) }
        }
        val attribution = project.parties.firstOrNull()?.attribution
        if (attribution != null) ProvenanceLine(attribution)
        Spacer(Modifier.height(4.dp))
        EText(
            text = "Listed as the source names them. Being named here is not a finding about any problem with the project.",
            size = EagleType.Caption,
            lineHeight = 16.sp,
            color = EagleColors.Muted,
        )
    }
}

@Composable
private fun PartyRow(label: String?, name: String?, detail: String?) {
    Column(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        if (label != null) EText(label, size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted)
        EText(
            text = name ?: "Not on file",
            size = EagleType.Body,
            lineHeight = 20.sp,
            weight = if (name != null) FontWeight.SemiBold else null,
            color = if (name != null) EagleColors.Ink else EagleColors.Muted,
        )
        if (detail != null) EText(detail, size = EagleType.Secondary, lineHeight = 19.sp, color = EagleColors.Body)
    }
}

@Composable
private fun ProvenanceSection(project: Project) {
    val uriHandler = LocalUriHandler.current
    Section("Data provenance") {
        FactGrid(
            listOf(
                FactItem("Last checked against sources", project.lastChecked?.format() ?: "Not yet checked", muted = project.lastChecked == null),
                FactItem("Record last updated", project.recordUpdated?.format()),
                FactItem("Record ID", project.id),
                FactItem("Record type", if (project.sample) "Sample fixture" else "Live record", muted = project.sample),
            ),
        )
        Spacer(Modifier.height(16.dp))
        SubLabel("Sources")
        project.sources.forEachIndexed { index, source ->
            val url = source.url
            LinkRow(
                title = source.name,
                glyph = Glyph.External,
                onClick = if (url != null) ({ uriHandler.openUri(url) }) else null,
                external = true,
                unavailable = "No link on file",
            )
            if (index < project.sources.lastIndex) Hairline()
        }
        Spacer(Modifier.height(16.dp))
        SubLabel("Revision history")
        project.revisions.sortedByDescending { it.date.epochDay }.forEach { revision ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                EText(revision.date.format(), modifier = Modifier.width(92.dp), size = EagleType.Label, lineHeight = 18.sp, weight = FontWeight.SemiBold, tabular = true)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    EText(revision.summary, size = EagleType.Secondary, lineHeight = 19.sp, color = EagleColors.Ink)
                    EText(revision.by, size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted)
                }
            }
        }
    }
}

@Composable
private fun ReportSection(project: Project, onReport: (ReportPreset?) -> Unit) {
    Section("Corrections and reports") {
        LinkRow(
            title = "Report incorrect or outdated information",
            detail = "A wrong figure, a missing document, a status that has changed",
            glyph = Glyph.Alert,
            onClick = { onReport(ReportPreset.IncorrectInformation) },
        )
        Hairline()
        LinkRow(
            title = "Report a problem with this project",
            detail = "Work stopped, poor quality, a safety hazard, or a concern about the people in charge",
            glyph = Glyph.Flag,
            onClick = { onReport(null) },
        )
        Spacer(Modifier.height(8.dp))
        EText(
            text = "Reports are anonymous unless you choose to share contact details. In this preview they stay on your device.",
            size = EagleType.Caption,
            lineHeight = 16.sp,
            color = EagleColors.Muted,
        )
    }
}
