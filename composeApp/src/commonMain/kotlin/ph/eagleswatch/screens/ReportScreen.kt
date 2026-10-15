package ph.eagleswatch.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Day
import ph.eagleswatch.content.NOT_SURE
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.Report
import ph.eagleswatch.content.ReportPreset
import ph.eagleswatch.content.Subject
import ph.eagleswatch.content.WRONG_INFORMATION
import ph.eagleswatch.content.issues
import ph.eagleswatch.content.lguLevels
import ph.eagleswatch.content.newReference
import ph.eagleswatch.content.publicServantRoles
import ph.eagleswatch.content.reportFacts
import ph.eagleswatch.content.reportPlaces
import ph.eagleswatch.content.reportTimes
import ph.eagleswatch.ui.Button
import ph.eagleswatch.ui.ButtonStyle
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleMetrics
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.FilterChip
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Hairline
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.Notice
import ph.eagleswatch.ui.NoticeTone
import ph.eagleswatch.ui.Tag
import ph.eagleswatch.ui.TextInput
import ph.eagleswatch.ui.Wrap
import ph.eagleswatch.ui.consumeTouches
import ph.eagleswatch.ui.glyph
import ph.eagleswatch.ui.tappable

private enum class Step { Subject, Issue, Who, PlaceTime, Details, Review }

private const val OTHER = "Other"

/** Everything the reporter has chosen so far. Most answers are taps; text is only for "Other" and details. */
@Stable
private class Draft(val fromProject: Project?, preset: ReportPreset?) {
    var subject by mutableStateOf(
        when {
            preset == ReportPreset.IncorrectInformation -> Subject.Other
            fromProject != null -> Subject.ThisProject
            else -> null
        },
    )
    var pickedProject by mutableStateOf<Project?>(null)
    var unlistedProject by mutableStateOf(false)

    /** The person, office, role or LGU level picked from a list. */
    var pick by mutableStateOf<String?>(null)

    /** Typed name: of an LGU, a person, an unlisted project, or an "Other" role. */
    var name by mutableStateOf("")
    var issues by mutableStateOf(if (preset == ReportPreset.IncorrectInformation) setOf(WRONG_INFORMATION) else emptySet())
    var otherIssue by mutableStateOf("")
    var place by mutableStateOf<String?>(null)
    var placeDetail by mutableStateOf("")
    var time by mutableStateOf<String?>(null)
    var timeDetail by mutableStateOf("")
    var facts by mutableStateOf(setOf<String>())
    var details by mutableStateOf("")
    var anonymous by mutableStateOf(true)
    var contact by mutableStateOf("")

    /** The project the report concerns, if any. */
    val project: Project?
        get() = when (subject) {
            Subject.ThisProject, Subject.ProjectPerson, Subject.ProjectAgency, Subject.Other -> fromProject
            Subject.Project -> pickedProject
            else -> null
        }

    val steps: List<Step>
        get() = if (subject == Subject.Other) Step.entries - Step.Who else Step.entries.toList()

    fun changeSubject(value: Subject) {
        if (value == subject) return
        subject = value
        // Answers to later questions depend on what is being reported, so start them over.
        pick = null
        name = ""
        issues = emptySet()
        otherIssue = ""
        pickedProject = null
        unlistedProject = false
    }

    fun toggleIssue(issue: String) {
        issues = if (issue in issues) issues - issue else issues + issue
    }

    val issueList: List<String>
        get() = issues.filter { it != OTHER } + listOfNotNull(otherIssue.trim().takeIf { OTHER in issues && it.isNotEmpty() })

    fun isDone(step: Step): Boolean = when (step) {
        Step.Subject -> subject != null
        Step.Issue -> issueList.isNotEmpty()
        Step.Who -> when (subject) {
            Subject.Project -> pickedProject != null || (unlistedProject && name.isNotBlank())
            Subject.PublicServant -> pick != null && (pick != OTHER || name.isNotBlank())
            Subject.ProjectPerson -> pick != null && (pick != OTHER || name.isNotBlank())
            else -> pick != null
        }
        Step.PlaceTime -> place != null && time != null
        Step.Details -> anonymous || contact.isNotBlank()
        Step.Review -> true
    }

    /** What the report is about, in plain words. */
    val target: String
        get() = when (subject) {
            Subject.ThisProject -> fromProject?.title.orEmpty()
            Subject.ProjectPerson -> if (pick == OTHER) name.trim() else pick.orEmpty()
            Subject.ProjectAgency -> pick.orEmpty()
            Subject.Project -> pickedProject?.title ?: name.trim()
            Subject.Lgu -> name.trim().ifEmpty { "A ${pick.orEmpty().lowercase()}, name not given" }
            Subject.PublicServant -> {
                val role = if (pick == OTHER) name.trim() else pick.orEmpty()
                if (pick != OTHER && name.isNotBlank()) "${name.trim()}, $role" else role
            }
            Subject.Other -> fromProject?.title ?: "Something else"
            null -> "Something else"
        }

    val involved: String
        get() = when (subject) {
            Subject.ThisProject -> pick?.let { "Responsible: $it" }.orEmpty()
            Subject.ProjectPerson, Subject.ProjectAgency -> fromProject?.title.orEmpty()
            Subject.Lgu -> pick.orEmpty()
            else -> ""
        }

    private fun withDetail(choice: String?, detail: String) =
        listOfNotNull(choice, detail.trim().takeIf { it.isNotEmpty() }).joinToString(" · ")

    fun toReport(): Report {
        val today = Day.today()
        return Report(
            reference = newReference(today),
            filed = today,
            subject = subject ?: Subject.Other,
            projectId = project?.id,
            target = target,
            involved = involved,
            issues = issueList,
            place = withDetail(place, placeDetail),
            time = withDetail(time, timeDetail),
            facts = facts.toList(),
            details = details.trim(),
            contact = if (anonymous) null else contact.trim(),
        )
    }
}

/**
 * Report a problem with a project, an LGU or a public servant, or correct the record, one question at a
 * time. Every question offers ready answers to tap, with "Other" or "Not sure" for the rest.
 *
 * @param project the project the report starts from, if any.
 * @param projects every record, for picking a project when the report did not start from one.
 * @param preset first answers filled in when the report starts from a specific prompt.
 */
@Composable
fun ReportScreen(
    project: Project?,
    projects: List<Project>,
    preset: ReportPreset?,
    onSubmit: (Report) -> Unit,
    onClose: () -> Unit,
) {
    val draft = remember(project, preset) { Draft(project, preset) }
    var index by remember { mutableIntStateOf(if (preset != null) draft.steps.indexOf(Step.Issue) else 0) }
    var filed by remember { mutableStateOf<Report?>(null) }
    val layout = LocalLayout.current

    val done = filed
    if (done != null) {
        Box(Modifier.fillMaxSize().background(EagleColors.Page).consumeTouches()) { Filed(done, onClose) }
        return
    }
    Column(Modifier.fillMaxSize().background(EagleColors.Canvas).consumeTouches().imePadding()) {
        val steps = draft.steps
        val step = steps[index.coerceIn(0, steps.lastIndex)]

        PageBar(
            onBack = onClose,
            title = if (preset == ReportPreset.IncorrectInformation) "Correct this record" else "Report a problem",
            backGlyph = Glyph.Close,
            backDescription = "Close the report",
            actions = {
                EText(
                    text = "${steps.indexOf(step) + 1} of ${steps.size}",
                    modifier = Modifier.padding(end = 12.dp),
                    size = EagleType.Secondary,
                    color = EagleColors.OnNightMuted,
                    tabular = true,
                )
            },
        )
        // Progress in segments, one per step.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            steps.forEachIndexed { i, _ ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(if (i <= steps.indexOf(step)) EagleColors.Accent else EagleColors.SurfaceStrong),
                )
            }
        }

        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val forward = steps.indexOf(targetState) > steps.indexOf(initialState)
                (slideInHorizontally(tween(200)) { if (forward) it / 5 else -it / 5 } + fadeIn(tween(200))) togetherWith fadeOut(tween(100))
            },
            label = "step",
        ) { shown ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = layout.gutter, vertical = 16.dp),
            ) {
                Column(Modifier.widthIn(max = EagleMetrics.ContentMax)) {
                    when (shown) {
                        Step.Subject -> SubjectStep(draft)
                        Step.Issue -> IssueStep(draft)
                        Step.Who -> WhoStep(draft, projects)
                        Step.PlaceTime -> PlaceTimeStep(draft)
                        Step.Details -> DetailsStep(draft)
                        Step.Review -> ReviewStep(draft, onEdit = { index = steps.indexOf(it) })
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        Hairline()
        Row(
            Modifier
                .fillMaxWidth()
                .background(EagleColors.Page)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = layout.gutter, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (index > 0) {
                Button("Back", onClick = { index -= 1 })
            }
            Spacer(Modifier.weight(1f))
            if (step == Step.Review) {
                Button(
                    label = "Submit report",
                    onClick = {
                        val report = draft.toReport()
                        onSubmit(report)
                        filed = report
                    },
                    style = ButtonStyle.Primary,
                    glyph = Glyph.FlagFilled,
                )
            } else {
                Button(
                    label = "Next",
                    onClick = { index = (index + 1).coerceAtMost(steps.lastIndex) },
                    style = ButtonStyle.Primary,
                    enabled = draft.isDone(step),
                )
            }
        }
    }
}

@Composable
private fun Question(title: String, hint: String? = null) {
    EText(title, size = 20.sp, lineHeight = 26.sp, weight = FontWeight.SemiBold, heading = true)
    if (hint != null) {
        Spacer(Modifier.height(4.dp))
        EText(hint, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body)
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun Label(text: String) {
    EText(text, size = EagleType.Secondary, weight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
}

/** A tappable card, in a grid, for the first and most important choice. */
@Composable
private fun OptionCard(glyph: Glyph, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(EagleMetrics.Radius)
    Column(
        modifier
            .border(if (selected) 2.dp else 1.dp, if (selected) EagleColors.Accent else EagleColors.Line, shape)
            .clip(shape)
            .background(if (selected) EagleColors.AccentSoft else EagleColors.Page)
            .tappable(onClick = onClick, shape = shape, role = Role.RadioButton, selected = selected, tint = EagleColors.SurfaceStrong)
            .padding(14.dp),
    ) {
        Icon(glyph, tint = if (selected) EagleColors.Accent else EagleColors.Ink, size = 24.dp)
        Spacer(Modifier.height(10.dp))
        EText(title, size = EagleType.Body, lineHeight = 20.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        EText(subtitle, size = EagleType.Label, lineHeight = 17.sp, color = EagleColors.Body)
    }
}

/** A full-width choice in a list, for people, offices and projects. */
@Composable
private fun OptionRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(EagleMetrics.Radius)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .border(if (selected) 2.dp else 1.dp, if (selected) EagleColors.Accent else EagleColors.Line, shape)
            .clip(shape)
            .background(if (selected) EagleColors.AccentSoft else EagleColors.Page)
            .tappable(onClick = onClick, shape = shape, role = Role.RadioButton, selected = selected, tint = EagleColors.SurfaceStrong)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            EText(title, size = EagleType.Body, lineHeight = 20.sp, weight = FontWeight.SemiBold)
            if (subtitle != null) EText(subtitle, size = EagleType.Label, lineHeight = 17.sp, color = EagleColors.Body)
        }
        if (selected) {
            Spacer(Modifier.width(8.dp))
            Icon(Glyph.Check, tint = EagleColors.Accent, size = 20.dp, weight = 2.4f)
        }
    }
}

@Composable
private fun GlyphTile(glyph: Glyph) {
    Box(Modifier.size(36.dp).clip(RoundedCornerShape(EagleMetrics.Radius)).background(EagleColors.Surface), contentAlignment = Alignment.Center) {
        Icon(glyph, tint = EagleColors.Ink, size = 20.dp)
    }
}

private val Subject.glyph: Glyph
    get() = when (this) {
        Subject.ThisProject, Subject.Project -> Glyph.HardHat
        Subject.ProjectPerson, Subject.PublicServant -> Glyph.Person
        Subject.ProjectAgency, Subject.Lgu -> Glyph.Agency
        Subject.Other -> Glyph.Alert
    }

@Composable
private fun SubjectStep(draft: Draft) {
    val project = draft.fromProject
    Question(
        "What do you want to report?",
        if (project != null) "About ${project.title}." else "Reports go to the office concerned once the service is live.",
    )
    val options = if (project != null) Subject.onProject else Subject.general
    options.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            pair.forEach { subject ->
                OptionCard(
                    glyph = subject.glyph,
                    title = subject.label,
                    subtitle = subject.hint,
                    selected = draft.subject == subject,
                    onClick = { draft.changeSubject(subject) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
            if (pair.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun IssueStep(draft: Draft) {
    val subject = draft.subject ?: return
    Question("What happened?", "Choose all that apply.")
    Wrap(spacing = 8.dp) {
        (subject.issues() + OTHER).forEach { issue ->
            FilterChip(issue, selected = issue in draft.issues, onClick = { draft.toggleIssue(issue) })
        }
    }
    if (OTHER in draft.issues) {
        Spacer(Modifier.height(14.dp))
        TextInput(draft.otherIssue, { draft.otherIssue = it }, "A few words", label = "Describe it", singleLine = true, maxLength = 120)
    }
}

@Composable
private fun WhoStep(draft: Draft, projects: List<Project>) {
    val project = draft.fromProject
    when (draft.subject) {
        Subject.ThisProject -> {
            Question("Who do you think is responsible?", "Pick one. It's fine to choose Not sure.")
            project?.officials?.forEach { person ->
                val value = "${person.name}, ${person.detail.orEmpty()}"
                OptionRow(person.name, person.detail, draft.pick == value, { draft.pick = value }) { GlyphTile(Glyph.Person) }
            }
            project?.contractor?.let { contractor ->
                OptionRow(contractor.name, "Contractor", draft.pick == contractor.name, { draft.pick = contractor.name }) { GlyphTile(Glyph.HardHat) }
            }
            project?.implementingAgency?.let { agency ->
                OptionRow(agency.name, "Implementing agency", draft.pick == agency.name, { draft.pick = agency.name }) { GlyphTile(Glyph.Agency) }
            }
            OptionRow(NOT_SURE, null, draft.pick == NOT_SURE, { draft.pick = NOT_SURE }) { GlyphTile(Glyph.Info) }
        }
        Subject.ProjectPerson -> {
            Question("Who is it about?", "The people named on this project.")
            project?.officials?.forEach { person ->
                val value = "${person.name}, ${person.detail.orEmpty()}"
                OptionRow(person.name, person.detail, draft.pick == value, { draft.pick = value }) { GlyphTile(Glyph.Person) }
            }
            OptionRow("Someone not listed", "A worker, guard or other official", draft.pick == OTHER, { draft.pick = OTHER }) { GlyphTile(Glyph.Plus) }
            if (draft.pick == OTHER) {
                TextInput(draft.name, { draft.name = it }, "e.g. site guard", label = "Their name or role", singleLine = true, maxLength = 120)
            }
        }
        Subject.ProjectAgency -> {
            Question("Which office?")
            listOfNotNull(project?.proposingOffice, project?.implementingAgency).distinctBy { it.name }.forEach { office ->
                OptionRow(office.name, office.role.label, draft.pick == office.name, { draft.pick = office.name }) { GlyphTile(Glyph.Agency) }
            }
        }
        Subject.Project -> {
            Question("Which project?", "Pick it from the list, or tell us about one that's not here.")
            projects.forEach { p ->
                OptionRow(
                    p.title,
                    p.place.full,
                    draft.pickedProject == p,
                    { draft.pickedProject = p; draft.unlistedProject = false },
                ) { GlyphTile(p.category.glyph) }
            }
            OptionRow("It's not in the app", "Name the project and where it is", draft.unlistedProject, {
                draft.unlistedProject = true
                draft.pickedProject = null
            }) { GlyphTile(Glyph.Plus) }
            if (draft.unlistedProject) {
                TextInput(draft.name, { draft.name = it }, "e.g. Covered court, Brgy. Poblacion, Tanauan", label = "Project and place", singleLine = true, maxLength = 160)
            }
        }
        Subject.Lgu -> {
            Question("Which LGU?", "Pick the level, then tap a name or type it.")
            Wrap(spacing = 8.dp) {
                lguLevels.forEach { level -> FilterChip(level, selected = draft.pick == level, onClick = { draft.pick = level }, role = Role.RadioButton) }
            }
            val level = draft.pick
            if (level != null) {
                val suggestions = lguSuggestions(level, projects)
                Spacer(Modifier.height(20.dp))
                Label("Name (optional)")
                if (suggestions.isNotEmpty()) {
                    Wrap(spacing = 8.dp) {
                        suggestions.forEach { name -> FilterChip(name, selected = draft.name == name, onClick = { draft.name = name }, role = Role.RadioButton) }
                    }
                    Spacer(Modifier.height(10.dp))
                }
                TextInput(draft.name, { draft.name = it }, "Or type the name", singleLine = true, maxLength = 120)
            }
        }
        Subject.PublicServant -> {
            Question("Who is it about?", "Pick their role. Their name is optional.")
            Wrap(spacing = 8.dp) {
                (publicServantRoles + OTHER).forEach { role -> FilterChip(role, selected = draft.pick == role, onClick = { draft.pick = role }, role = Role.RadioButton) }
            }
            Spacer(Modifier.height(20.dp))
            TextInput(
                draft.name,
                { draft.name = it },
                if (draft.pick == OTHER) "e.g. Building inspector" else "Name, if you know it",
                label = if (draft.pick == OTHER) "Their role" else "Name (optional)",
                singleLine = true,
                maxLength = 120,
            )
        }
        Subject.Other, null -> Unit
    }
}

/** LGU names from the places of listed projects, so common answers are a tap away. */
private fun lguSuggestions(level: String, projects: List<Project>): List<String> = when (level) {
    "Barangay" -> projects.mapNotNull { p -> p.place.barangay?.let { "Barangay $it" } }
    "City" -> projects.map { it.place.municipality }.filter { it.contains("City") }
    "Municipality" -> projects.map { it.place.municipality }.filter { !it.contains("City") }
    else -> projects.map { it.place.province }
}.distinct().sorted()

@Composable
private fun PlaceTimeStep(draft: Draft) {
    Question("Where and when did it happen?")
    Label("Where")
    val site = draft.project?.let { "At the project site" }
    Wrap(spacing = 8.dp) {
        (listOfNotNull(site) + reportPlaces + NOT_SURE).forEach { place ->
            FilterChip(place, selected = draft.place == place, onClick = { draft.place = place }, role = Role.RadioButton)
        }
    }
    Spacer(Modifier.height(10.dp))
    TextInput(
        draft.placeDetail,
        { draft.placeDetail = it },
        draft.project?.let { "Landmark, e.g. near the ${it.place.municipality} approach" } ?: "Place or landmark",
        label = "Details (optional)",
        singleLine = true,
        maxLength = 160,
    )
    Spacer(Modifier.height(24.dp))
    Label("When")
    Wrap(spacing = 8.dp) {
        (reportTimes + NOT_SURE).forEach { time ->
            FilterChip(time, selected = draft.time == time, onClick = { draft.time = time }, role = Role.RadioButton)
        }
    }
    Spacer(Modifier.height(10.dp))
    TextInput(draft.timeDetail, { draft.timeDetail = it }, "Date or time, if you know it", label = "Details (optional)", singleLine = true, maxLength = 80)
}

@Composable
private fun DetailsStep(draft: Draft) {
    Question("Anything else?", "Optional. Tick what applies, and add a few words if you like.")
    Wrap(spacing = 8.dp) {
        reportFacts.forEach { fact ->
            FilterChip(fact, selected = fact in draft.facts, onClick = { draft.facts = if (fact in draft.facts) draft.facts - fact else draft.facts + fact })
        }
    }
    Spacer(Modifier.height(14.dp))
    TextInput(draft.details, { draft.details = it }, "Stick to what you saw or heard.", label = "What should the office know?", minHeight = 120.dp)
    Spacer(Modifier.height(24.dp))
    Label("Who can the office contact?")
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OptionCard(Glyph.Eye, "Stay anonymous", "Recommended. Your name is not sent.", draft.anonymous, { draft.anonymous = true }, Modifier.weight(1f).fillMaxHeight())
        OptionCard(Glyph.Person, "Share my contact", "So the office can ask you more.", !draft.anonymous, { draft.anonymous = false }, Modifier.weight(1f).fillMaxHeight())
    }
    if (!draft.anonymous) {
        Spacer(Modifier.height(10.dp))
        TextInput(draft.contact, { draft.contact = it }, "Name and phone or email", label = "Your contact details", singleLine = true, maxLength = 160)
    }
}

@Composable
private fun ReviewStep(draft: Draft, onEdit: (Step) -> Unit) {
    Question("Check your report", "Tap Edit to change an answer.")
    val subject = draft.subject ?: return
    ReviewRow("What", subject.label, Step.Subject, onEdit)
    if (subject != Subject.Other) {
        ReviewRow("About", listOf(draft.target, draft.involved).filter { it.isNotEmpty() }.joinToString("\n"), Step.Who, onEdit)
    } else if (draft.fromProject != null) {
        ReviewRow("About", draft.fromProject.title, Step.Subject, onEdit)
    }
    ReviewRow("What happened", draft.issueList.joinToString("\n"), Step.Issue, onEdit)
    ReviewRow("Where", listOfNotNull(draft.place, draft.placeDetail.trim().takeIf { it.isNotEmpty() }).joinToString(" · "), Step.PlaceTime, onEdit)
    ReviewRow("When", listOfNotNull(draft.time, draft.timeDetail.trim().takeIf { it.isNotEmpty() }).joinToString(" · "), Step.PlaceTime, onEdit)
    ReviewRow(
        "Details",
        (draft.facts.toList() + listOfNotNull(draft.details.trim().takeIf { it.isNotEmpty() })).joinToString("\n").ifEmpty { "None added" },
        Step.Details,
        onEdit,
    )
    ReviewRow("Contact", if (draft.anonymous) "Anonymous" else draft.contact.trim(), Step.Details, onEdit)
    Spacer(Modifier.height(12.dp))
    EText(
        text = "Report only what you saw or know. False reports can harm people and may be against the law.",
        size = EagleType.Caption,
        lineHeight = 16.sp,
        color = EagleColors.Muted,
    )
}

@Composable
private fun ReviewRow(label: String, value: String, step: Step, onEdit: (Step) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            EText(label, size = EagleType.Caption, color = EagleColors.Muted)
            Spacer(Modifier.height(2.dp))
            EText(value.ifEmpty { "—" }, size = EagleType.Body, lineHeight = 21.sp)
        }
        Spacer(Modifier.width(12.dp))
        Button("Edit", onClick = { onEdit(step) }, style = ButtonStyle.Quiet, compact = true)
    }
    Hairline()
}

/** Confirmation with the reference number, what happens next, and official channels for urgent cases. */
@Composable
private fun Filed(report: Report, onDone: () -> Unit) {
    val layout = LocalLayout.current
    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = layout.gutter, vertical = 32.dp),
    ) {
        Column(Modifier.widthIn(max = EagleMetrics.ReadingMax)) {
            Icon(Glyph.CheckCircle, tint = EagleColors.Green, size = 36.dp, weight = 2.2f)
            Spacer(Modifier.height(14.dp))
            EText("Report saved", display = true, size = EagleType.Display, lineHeight = 30.sp, heading = true)
            Spacer(Modifier.height(10.dp))
            EText("Reference number", size = EagleType.Caption, color = EagleColors.Muted)
            EText(report.reference, display = true, size = 30.sp, lineHeight = 34.sp, color = EagleColors.Accent, tabular = true)
            Spacer(Modifier.height(16.dp))
            Notice(
                title = "What happens next",
                text = "In this preview, reports stay on your device and are not sent to anyone. When the service is live, " +
                    "your report will go to the office concerned, and you can follow its status under About.",
                tone = NoticeTone.Info,
            )
            Spacer(Modifier.height(10.dp))
            Notice(
                title = "Need help now?",
                text = "For emergencies, call 911. To file a formal complaint about a public official, call the 8888 " +
                    "Citizens' Complaint Hotline or go to the Office of the Ombudsman.",
                tone = NoticeTone.Info,
            )
            Spacer(Modifier.height(24.dp))
            Button("Done", onClick = onDone, style = ButtonStyle.Primary)
        }
    }
}

/** A report in the citizen's list under About. */
@Composable
fun ReportCard(report: Report, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .border(1.dp, EagleColors.Line, RoundedCornerShape(EagleMetrics.Radius))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Glyph.FlagFilled, tint = EagleColors.Ink, size = 16.dp)
            Spacer(Modifier.width(6.dp))
            EText(report.reference, size = EagleType.Secondary, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f), tabular = true)
            EText(report.filed.format(), size = EagleType.Caption, color = EagleColors.Muted, tabular = true)
        }
        Spacer(Modifier.height(6.dp))
        EText(report.target, size = EagleType.Body, lineHeight = 20.sp, weight = FontWeight.SemiBold, maxLines = 2, ellipsis = true)
        EText(report.issues.joinToString(" · "), size = EagleType.Secondary, lineHeight = 19.sp, color = EagleColors.Body, maxLines = 2, ellipsis = true)
        Spacer(Modifier.height(8.dp))
        Tag("Saved on this device")
    }
}
