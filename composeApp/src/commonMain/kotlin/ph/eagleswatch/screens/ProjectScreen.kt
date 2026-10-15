package ph.eagleswatch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import ph.eagleswatch.ui.GlassStyles
import ph.eagleswatch.ui.aurora
import ph.eagleswatch.ui.glass
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import ph.eagleswatch.ui.Card
import ph.eagleswatch.ui.CardHeader
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleMetrics
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Hairline
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.IconButton
import ph.eagleswatch.ui.IconTile
import ph.eagleswatch.ui.Initials
import ph.eagleswatch.ui.KeyValueRow
import ph.eagleswatch.ui.LinkRow
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.Locator
import ph.eagleswatch.ui.Meter
import ph.eagleswatch.ui.SampleTag
import ph.eagleswatch.ui.SourceBadge
import ph.eagleswatch.ui.Stat
import ph.eagleswatch.ui.StatusTag
import ph.eagleswatch.ui.Tag
import ph.eagleswatch.ui.WorkTrack
import ph.eagleswatch.ui.compactPesos
import ph.eagleswatch.ui.consumeTouches
import ph.eagleswatch.ui.days
import ph.eagleswatch.ui.fill
import ph.eagleswatch.ui.fixed
import ph.eagleswatch.ui.formatCoordinates
import ph.eagleswatch.ui.formatPesos
import ph.eagleswatch.ui.glyph
import ph.eagleswatch.ui.percent

/** A public map page for the coordinates; opens in the browser or a maps app on every platform. */
private fun mapUrl(c: Coordinates): String {
    val lat = fixed(c.lat, 5)
    val lon = fixed(c.lon, 5)
    return "https://www.openstreetmap.org/?mlat=$lat&mlon=$lon#map=15/$lat/$lon"
}

private fun shareSummary(project: Project): String {
    val reported = project.progress.reported
    return listOf(
        "${project.title} · ${project.place.full}",
        project.status.label + (reported?.let { " · ${it.percent}% physical progress reported ${it.reportedOn.format()}" } ?: ""),
        listOfNotNull(project.contract.amount?.let { "${formatPesos(it)} approved" }, project.implementingAgency?.name).joinToString(" · "),
        if (project.sample) "Sample record on The Eagle's Watch, not a real project." else "Seen on The Eagle's Watch.",
    ).filter { it.isNotBlank() }.joinToString("\n")
}

/**
 * Everything on file about one project, in the order a sceptical reader asks for it: what and where, how far
 * along, the contract, the location, the dated timeline, the evidence, who is responsible, where the data
 * came from, and how to correct it.
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

    // The page scrolls under a frosted night jade bar, so cards blur behind it as they pass.
    val pageHaze = rememberHazeState()
    Box(Modifier.fillMaxSize().background(EagleColors.Canvas).consumeTouches()) {
        Column(Modifier.fillMaxSize().hazeSource(pageHaze).verticalScroll(rememberScrollState()).windowInsetsPadding(WindowInsets.navigationBars)) {
            Hero(project, today)
            ContentColumn(max = if (layout.wide) 1120.dp else EagleMetrics.ContentMax) {
                val gap = Arrangement.spacedBy(EagleMetrics.CardGap)
                if (layout.wide) {
                    Row(Modifier.padding(horizontal = layout.gutter, vertical = 16.dp), horizontalArrangement = gap) {
                        Column(Modifier.weight(1.4f), verticalArrangement = gap) {
                            ProgressCard(project, today, onReport)
                            TimelineCard(project, today)
                            EvidenceCard(project)
                            PartiesCard(project)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = gap) {
                            ContractCard(project)
                            LocationCard(project)
                            ProvenanceCard(project)
                            CorrectionsCard(onReport)
                        }
                    }
                } else {
                    Column(Modifier.padding(horizontal = layout.gutter, vertical = 16.dp), verticalArrangement = gap) {
                        ProgressCard(project, today, onReport)
                        ContractCard(project)
                        LocationCard(project)
                        TimelineCard(project, today)
                        EvidenceCard(project)
                        PartiesCard(project)
                        ProvenanceCard(project)
                        CorrectionsCard(onReport)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        PageBar(
            onBack = onBack,
            transparent = true,
            modifier = Modifier.hazeEffect(pageHaze, GlassStyles.Night),
            actions = {
                IconButton(Glyph.Share, "Share this record", onClick = { shareText(project.title, shareSummary(project)) }, tint = EagleColors.OnNight, iconSize = 20.dp)
                IconButton(Glyph.Flag, "Report a problem with this project", onClick = { onReport(null) }, tint = EagleColors.OnNight, iconSize = 20.dp)
                IconButton(
                    glyph = if (watching) Glyph.EyeFilled else Glyph.Eye,
                    description = if (watching) "Stop watching this project" else "Watch this project",
                    onClick = { citizen.toggleWatch(project.id) },
                    tint = if (watching) EagleColors.Night else EagleColors.OnNight,
                    iconSize = 20.dp,
                    toggled = watching,
                    label = if (watching) "Watching" else "Watch",
                    filled = if (watching) EagleColors.AccentBright else Color.White.copy(alpha = 0.12f),
                )
                Spacer(Modifier.width(4.dp))
            },
        )
    }
}

/** The dark band: status, title, place, and the three figures that matter most. */
@Composable
private fun Hero(project: Project, today: Day) {
    val layout = LocalLayout.current
    val reported = project.progress.reported
    val work = reported?.let { it.percent / 100f }
    val elapsed = project.timeElapsed(today)
    val signal = scheduleSignal(project, today)
    val s = project.schedule
    val (thirdValue, thirdLabel) = when {
        project.status == Status.Completed -> (s.actualEnd?.monthYear() ?: "—") to "Completed"
        project.status == Status.Planned -> (s.start?.monthYear() ?: "—") to "Planned start"
        else -> (elapsed?.let { percent(it) } ?: "—") to "Time used"
    }
    Box(Modifier.fillMaxWidth().aurora()) {
        ContentColumn(max = if (layout.wide) 1120.dp else EagleMetrics.ContentMax) {
            Column(Modifier.padding(horizontal = layout.gutter).padding(top = 4.dp, bottom = 22.dp)) {
                // Room for the frosted page bar that floats over the top of the band.
                Spacer(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars))
                Spacer(Modifier.height(64.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusTag(project.status)
                    if (project.sample) SampleTag(onDark = true)
                }
                Spacer(Modifier.height(12.dp))
                EText(project.title, display = true, size = 26.sp, lineHeight = 31.sp, color = EagleColors.OnNight, heading = true)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Glyph.Pin, tint = EagleColors.OnNightMuted, size = 15.dp, weight = 2f)
                    Spacer(Modifier.width(6.dp))
                    EText(project.place.full, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.OnNightMuted)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(project.category.glyph, tint = EagleColors.OnNightMuted, size = 15.dp, weight = 2f)
                    Spacer(Modifier.width(6.dp))
                    EText("${project.category.label} · ${project.level.label} project", size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.OnNightMuted)
                }
                Spacer(Modifier.height(18.dp))
                Column(
                    Modifier.fillMaxWidth().glass(RoundedCornerShape(EagleMetrics.RadiusCard)).padding(16.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Stat(
                            project.contract.amount?.let { compactPesos(it) } ?: "—",
                            if (project.status == Status.Planned) "Proposed" else "Contract",
                            Modifier.weight(1.1f),
                            valueColor = EagleColors.OnNight,
                            labelColor = EagleColors.OnNightMuted,
                            size = 22.sp,
                        )
                        Stat(work?.let { percent(it) } ?: "—", "Work done", Modifier.weight(1f), valueColor = EagleColors.OnNight, labelColor = EagleColors.OnNightMuted, size = 22.sp)
                        Stat(thirdValue, thirdLabel, Modifier.weight(1f), valueColor = EagleColors.OnNight, labelColor = EagleColors.OnNightMuted, size = 22.sp)
                    }
                    if (project.status != Status.Planned) {
                        Spacer(Modifier.height(12.dp))
                        WorkTrack(
                            work = work,
                            color = project.status.fill,
                            time = if (project.status.active) elapsed else null,
                            track = Color.White.copy(alpha = 0.14f),
                            marker = EagleColors.OnNight,
                        )
                    }
                    if (signal != null) {
                        Spacer(Modifier.height(12.dp))
                        Tag(signal.text, color = signal.tone.color, background = signal.tone.soft, glyph = signal.glyph)
                    }
                }
            }
        }
    }
}

/** A labelled figure inside a card, with its source badge on the right. */
@Composable
private fun Figure(label: String, value: String, attribution: Attribution, muted: Boolean = false, content: @Composable () -> Unit = {}) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                EText(label, size = EagleType.Label, lineHeight = 18.sp, color = EagleColors.Muted)
                EText(value, display = true, size = 22.sp, lineHeight = 28.sp, weight = FontWeight.SemiBold, color = if (muted) EagleColors.Muted else EagleColors.Ink, tabular = true)
            }
            SourceBadge(label, attribution)
        }
        content()
    }
}

@Composable
private fun ProgressCard(project: Project, today: Day, onReport: (ReportPreset?) -> Unit) {
    val reported = project.progress.reported
    val independent = project.progress.independent
    val financial = project.progress.financial
    val amount = project.contract.amount
    val elapsed = project.timeElapsed(today)
    val active = project.status.active
    Card {
        CardHeader("Progress", subtitle = "Status, physical work and money are reported separately")
        Spacer(Modifier.height(14.dp))
        if (reported != null) {
            Figure("Reported progress", "${reported.percent}%", reported.attribution) {
                WorkTrack(reported.percent / 100f, color = project.status.fill, time = if (active) elapsed else null)
                if (active && elapsed != null) {
                    EText("The tick marks ${percent(elapsed)} of contract time used, as of ${today.format()}.", size = EagleType.Caption, lineHeight = 17.sp, color = EagleColors.Muted)
                }
            }
        } else {
            Figure("Reported progress", "Not on file", Attribution(Verification.Unavailable, "No progress report on file"), muted = true)
        }
        Spacer(Modifier.height(14.dp))
        Hairline()
        Spacer(Modifier.height(14.dp))
        if (independent != null) {
            Figure("Independent check", "${independent.percent}%", independent.attribution) {
                WorkTrack(independent.percent / 100f, color = EagleColors.FillGreen)
            }
        } else {
            Figure("Independent check", "None yet", Attribution(Verification.Unavailable, "No site visit, audit finding or citizen report on file"), muted = true) {
                Spacer(Modifier.height(4.dp))
                EText("Nobody has checked the reported figure against the site.", size = EagleType.Label, lineHeight = 19.sp, color = EagleColors.Body)
                Spacer(Modifier.height(10.dp))
                Button("Report what you see at the site", onClick = { onReport(null) }, glyph = Glyph.Flag, compact = true)
            }
        }
        Spacer(Modifier.height(14.dp))
        Hairline()
        Spacer(Modifier.height(14.dp))
        when {
            financial != null && amount != null && amount > 0 -> {
                val ratio = financial.disbursed.toFloat() / amount
                Figure("Funds disbursed", "${compactPesos(financial.disbursed)} · ${percent(ratio)}", financial.attribution) {
                    Meter(ratio, color = EagleColors.Ink)
                    EText("of the ${formatPesos(amount)} contract, as of ${financial.asOf.format()}", size = EagleType.Caption, lineHeight = 17.sp, color = EagleColors.Muted)
                }
            }
            financial != null -> Figure("Funds disbursed", formatPesos(financial.disbursed), financial.attribution)
            else -> Figure("Funds disbursed", "Not on file", Attribution(Verification.Unavailable, "No disbursement report on file"), muted = true)
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(EagleMetrics.Radius)).background(EagleColors.Surface).padding(12.dp)) {
            EText(progressReading(project, today), size = EagleType.Label, lineHeight = 19.sp, color = EagleColors.Body)
        }
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
        Status.Planned -> "Works have not started. The amount is a proposal until an award is on file."
        Status.Suspended -> "Works are suspended, so the reported figure will not move until they resume."
        else -> {
            val points = scheduleGap(project, today)?.let { (it * 100).toInt() }
            when {
                points == null -> "Reported progress cannot be compared with the schedule because dates are missing."
                points > 10 -> "Reported work is $points points behind the share of contract time used. This compares the agency's own figure with the calendar; it is not an independent finding."
                points < -10 -> "Reported work is ${-points} points ahead of the share of contract time used, by the agency's own figure."
                else -> "Reported work is keeping pace with the contract schedule, by the agency's own figure."
            }
        }
    }
}

@Composable
private fun ContractCard(project: Project) {
    val c = project.contract
    val s = project.schedule
    Card {
        CardHeader("Contract", trailing = { SourceBadge("Contract", c.attribution) })
        Spacer(Modifier.height(4.dp))
        KeyValueRow("Approved amount", c.amount?.let { formatPesos(it) }, note = c.reference?.let { "Ref. $it" })
        Hairline()
        KeyValueRow("Funding source", c.fundingSource)
        Hairline()
        KeyValueRow("Implementing agency", project.implementingAgency?.name)
        Hairline()
        KeyValueRow("Contractor", project.contractor?.name, note = project.contractor?.detail)
        Hairline()
        KeyValueRow("Start", s.start?.format())
        Hairline()
        KeyValueRow("Target completion", s.targetEnd?.format(), note = s.revisedEnd?.let { "Revised to ${it.format()}" })
        Hairline()
        KeyValueRow(
            "Completed",
            s.actualEnd?.format() ?: if (project.status == Status.Completed) null else "Not yet",
            muted = s.actualEnd == null,
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            EText("Schedule source", modifier = Modifier.weight(1f), size = EagleType.Label, color = EagleColors.Muted)
            SourceBadge("Schedule", s.attribution)
        }
    }
}

@Composable
private fun LocationCard(project: Project) {
    val uriHandler = LocalUriHandler.current
    val place = project.place
    val c = place.coordinates
    Card {
        CardHeader("Location")
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.clip(RoundedCornerShape(EagleMetrics.Radius)).background(EagleColors.Surface).padding(10.dp)) {
                Locator(c, Modifier.width(76.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                EText(place.full, size = EagleType.Body, lineHeight = 21.sp, weight = FontWeight.SemiBold)
                EText(place.region, size = EagleType.Label, lineHeight = 18.sp, color = EagleColors.Muted)
                Spacer(Modifier.height(8.dp))
                if (c != null) {
                    EText(formatCoordinates(c), size = EagleType.Label, lineHeight = 18.sp, color = EagleColors.Body, tabular = true)
                    EText(place.precision.label.replaceFirstChar { it.uppercase() }, size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted)
                    Spacer(Modifier.height(10.dp))
                    Button("Open in maps", onClick = { uriHandler.openUri(mapUrl(c)) }, glyph = Glyph.Map, compact = true)
                } else {
                    Tag("No coordinates on file", glyph = Glyph.CircleDashed, color = EagleColors.Muted)
                }
            }
        }
    }
}

private class TimelineEntry(
    val date: Day,
    val title: String,
    val body: String?,
    val percent: Int?,
    val attribution: Attribution,
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
private fun TimelineCard(project: Project, today: Day) {
    val entries = remember(project, today) { timelineEntries(project, today) }
    var showAll by remember { mutableStateOf(false) }
    val shown = if (showAll || entries.size <= 5) entries else entries.take(5)
    Card {
        CardHeader("Timeline", subtitle = "${entries.size} dated entries, newest first. Tap a source to see it.")
        Spacer(Modifier.height(14.dp))
        shown.forEachIndexed { index, entry -> TimelineRow(entry, last = index == shown.lastIndex) }
        if (entries.size > 5) {
            Spacer(Modifier.height(4.dp))
            Button(if (showAll) "Show fewer" else "Show all ${entries.size} entries", onClick = { showAll = !showAll }, compact = true, fillWidth = true)
        }
    }
}

@Composable
private fun TimelineRow(entry: TimelineEntry, last: Boolean) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(Modifier.width(20.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(4.dp))
            val dot = Modifier.size(12.dp).clip(CircleShape)
            Box(
                if (entry.upcoming) dot.border(2.dp, EagleColors.LineStrong, CircleShape)
                else dot.background(if (entry.body == null) EagleColors.Ink else EagleColors.Blue),
            )
            if (!last) Box(Modifier.width(2.dp).weight(1f).background(EagleColors.Line))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(bottom = if (last) 4.dp else 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EText(entry.date.format(), size = EagleType.Caption, weight = FontWeight.SemiBold, color = EagleColors.Muted, tabular = true)
                if (entry.upcoming) {
                    Spacer(Modifier.width(6.dp))
                    Tag("Upcoming")
                }
                if (entry.percent != null) {
                    Spacer(Modifier.width(6.dp))
                    Tag("${entry.percent}% work", color = EagleColors.Blue, background = EagleColors.BlueSoft)
                }
            }
            Spacer(Modifier.height(2.dp))
            EText(entry.title, size = EagleType.Body, lineHeight = 21.sp, weight = FontWeight.SemiBold, color = if (entry.upcoming) EagleColors.Muted else EagleColors.Ink)
            if (entry.body != null) EText(entry.body, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body)
            SourceBadge(entry.title, entry.attribution, quiet = entry.attribution.verification == Verification.AgencyReported)
        }
    }
}

@Composable
private fun EvidenceCard(project: Project) {
    val uriHandler = LocalUriHandler.current
    Card {
        CardHeader("Evidence", subtitle = "Site photos and official documents")
        Spacer(Modifier.height(12.dp))
        if (project.photos.isEmpty()) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(EagleMetrics.Radius)).background(EagleColors.Surface).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(Glyph.Photo, tint = EagleColors.Muted, background = EagleColors.Page)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    EText("No site photos on file", size = EagleType.Secondary, weight = FontWeight.SemiBold)
                    EText("Dated, captioned photos appear here once an agency or a checked citizen report supplies them. Nothing is shown in their place.", size = EagleType.Caption, lineHeight = 17.sp, color = EagleColors.Muted)
                }
            }
        } else {
            project.photos.forEach { photo ->
                LinkRow(
                    title = photo.caption,
                    detail = listOfNotNull(photo.takenOn?.format() ?: "Date not on file", photo.attribution.verification.label, photo.attribution.source).joinToString(" · "),
                    glyph = Glyph.Photo,
                    onClick = { uriHandler.openUri(photo.url) },
                    external = true,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        if (project.documents.isEmpty()) {
            EText("No documents on file.", size = EagleType.Secondary, color = EagleColors.Muted)
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
    }
}

@Composable
private fun PartiesCard(project: Project) {
    val attribution = project.parties.firstOrNull()?.attribution
    Card {
        CardHeader("Responsible parties", trailing = if (attribution != null) ({ SourceBadge("Responsible parties", attribution) }) else null)
        Spacer(Modifier.height(8.dp))
        listOf(PartyRole.Implementing, PartyRole.Proposing, PartyRole.Contractor).forEach { role ->
            val party = project.parties.firstOrNull { it.role == role }
            if (party != null || role != PartyRole.Proposing) {
                PartyRow(
                    leading = { IconTile(if (role == PartyRole.Contractor) Glyph.HardHat else Glyph.Agency) },
                    title = party?.name ?: "Not on file",
                    subtitle = listOfNotNull(role.label, party?.detail).joinToString(" · "),
                    missing = party == null,
                )
            }
        }
        project.officials.forEach { official ->
            PartyRow(leading = { Initials(official.name) }, title = official.name, subtitle = official.detail ?: "Official")
        }
        Spacer(Modifier.height(6.dp))
        EText("Listed as the source names them. Being named is not a finding about any problem with the project.", size = EagleType.Caption, lineHeight = 17.sp, color = EagleColors.Muted)
    }
}

@Composable
private fun PartyRow(leading: @Composable () -> Unit, title: String, subtitle: String, missing: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        leading()
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            EText(title, size = EagleType.Body, lineHeight = 21.sp, weight = FontWeight.SemiBold, color = if (missing) EagleColors.Muted else EagleColors.Ink)
            EText(subtitle, size = EagleType.Label, lineHeight = 18.sp, color = EagleColors.Muted)
        }
    }
}

@Composable
private fun ProvenanceCard(project: Project) {
    val uriHandler = LocalUriHandler.current
    Card {
        CardHeader("Sources & history")
        Spacer(Modifier.height(4.dp))
        KeyValueRow("Last checked", project.lastChecked?.format() ?: "Not yet checked", muted = project.lastChecked == null)
        Hairline()
        KeyValueRow("Record updated", project.recordUpdated?.format())
        Hairline()
        KeyValueRow("Record type", if (project.sample) "Sample fixture" else "Live record", muted = project.sample)
        Hairline()
        KeyValueRow("Record ID", project.id)
        Spacer(Modifier.height(8.dp))
        project.sources.forEach { source ->
            val url = source.url
            LinkRow(title = source.name, glyph = Glyph.Document, onClick = if (url != null) ({ uriHandler.openUri(url) }) else null, external = true, unavailable = "No link on file")
        }
        Spacer(Modifier.height(10.dp))
        EText("Revision history", size = EagleType.Secondary, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        project.revisions.sortedByDescending { it.date.epochDay }.forEach { revision ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                EText(revision.date.format(), modifier = Modifier.width(96.dp), size = EagleType.Caption, lineHeight = 18.sp, weight = FontWeight.SemiBold, color = EagleColors.Muted, tabular = true)
                Column(Modifier.weight(1f)) {
                    EText(revision.summary, size = EagleType.Label, lineHeight = 18.sp, color = EagleColors.Ink)
                    EText(revision.by, size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted)
                }
            }
        }
    }
}

@Composable
private fun CorrectionsCard(onReport: (ReportPreset?) -> Unit) {
    Card {
        CardHeader("Something wrong?")
        Spacer(Modifier.height(6.dp))
        LinkRow(
            title = "Correct this record",
            detail = "A wrong figure, a missing document, a status that has changed",
            glyph = Glyph.Alert,
            onClick = { onReport(ReportPreset.IncorrectInformation) },
        )
        LinkRow(
            title = "Report a problem with this project",
            detail = "Work stopped, poor quality, a safety hazard, or a concern about the people in charge",
            glyph = Glyph.Flag,
            onClick = { onReport(null) },
        )
        Spacer(Modifier.height(4.dp))
        EText("Anonymous unless you share contact details. In this preview, reports stay on your device.", size = EagleType.Caption, lineHeight = 17.sp, color = EagleColors.Muted)
    }
}
