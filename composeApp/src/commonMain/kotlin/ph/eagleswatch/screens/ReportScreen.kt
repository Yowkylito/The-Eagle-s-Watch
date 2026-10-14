package ph.eagleswatch.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Day
import ph.eagleswatch.content.NOT_SURE
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.Report
import ph.eagleswatch.content.Subject
import ph.eagleswatch.content.issues
import ph.eagleswatch.content.lguLevels
import ph.eagleswatch.content.newReference
import ph.eagleswatch.content.projectById
import ph.eagleswatch.content.projects
import ph.eagleswatch.content.publicServantRoles
import ph.eagleswatch.content.reportFacts
import ph.eagleswatch.content.reportPlaces
import ph.eagleswatch.content.reportTimes
import ph.eagleswatch.ui.ActionButton
import ph.eagleswatch.ui.Avatar
import ph.eagleswatch.ui.ChoiceChip
import ph.eagleswatch.ui.CircleButton
import ph.eagleswatch.ui.Divider
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.ProjectArt
import ph.eagleswatch.ui.TextAction
import ph.eagleswatch.ui.TextInput
import ph.eagleswatch.ui.Wrap
import ph.eagleswatch.ui.focusRing

private enum class Step { Subject, Issue, Who, PlaceTime, Details, Review }

private const val OTHER = "Other"

/** Everything the reporter has chosen so far. Most answers are taps; text is only for "Other" and details. */
@Stable
private class Draft(val fromProject: Project?) {
    var subject by mutableStateOf(if (fromProject != null) Subject.ThisProject else null)
    var pickedProject by mutableStateOf<Project?>(null)
    var unlistedProject by mutableStateOf(false)

    /** The person, office, role or LGU level picked from a list. */
    var pick by mutableStateOf<String?>(null)

    /** Typed name: of an LGU, a person, an unlisted project, or an "Other" role. */
    var name by mutableStateOf("")
    var issues by mutableStateOf(setOf<String>())
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
            Subject.ThisProject, Subject.ProjectPerson, Subject.ProjectAgency -> fromProject
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
            Subject.Other, null -> "Something else"
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
 * Report a problem with a project, an LGU or a public servant, one question at a time.
 * Every question offers ready answers to tap, with "Other" or "Not sure" for the rest.
 */
@Composable
fun ReportScreen(projectId: String?, onSubmit: (Report) -> Unit, onClose: () -> Unit) {
    val draft = remember(projectId) { Draft(projectId?.let(::projectById)) }
    var index by remember { mutableIntStateOf(0) }
    var filed by remember { mutableStateOf<Report?>(null) }
    val layout = LocalLayout.current

    val done = filed
    if (done != null) {
        Box(Modifier.fillMaxSize().background(EagleColors.Page)) { Filed(done, onClose) }
        return
    }
    Column(Modifier.fillMaxSize().background(EagleColors.Page).imePadding()) {
        val steps = draft.steps
        val step = steps[index.coerceAtMost(steps.lastIndex)]

        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleButton(Glyph.Close, "Close", onClose)
            Spacer(Modifier.weight(1f))
            Icon(Glyph.FlagFilled, tint = EagleColors.Red, size = 18.dp)
            Spacer(Modifier.width(6.dp))
            EText("Report", size = 15.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            EText("${steps.indexOf(step) + 1} of ${steps.size}", size = 14.sp, color = EagleColors.Muted, modifier = Modifier.width(64.dp), align = TextAlign.End, maxLines = 1)
        }

        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val forward = steps.indexOf(targetState) > steps.indexOf(initialState)
                (slideInHorizontally(tween(220)) { if (forward) it / 4 else -it / 4 } + fadeIn(tween(220))) togetherWith fadeOut(tween(120))
            },
            label = "step",
        ) { shown ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = layout.gutter, vertical = 12.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Column(Modifier.widthIn(max = 640.dp)) {
                    when (shown) {
                        Step.Subject -> SubjectStep(draft)
                        Step.Issue -> IssueStep(draft)
                        Step.Who -> WhoStep(draft)
                        Step.PlaceTime -> PlaceTimeStep(draft)
                        Step.Details -> DetailsStep(draft)
                        Step.Review -> ReviewStep(draft, onEdit = { index = steps.indexOf(it) })
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        // Progress in segments, then Back and Next, as in a step-by-step listing flow.
        Row(Modifier.fillMaxWidth().padding(horizontal = 0.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            steps.forEachIndexed { i, _ ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(if (i <= steps.indexOf(step)) EagleColors.Ink else EagleColors.Line),
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = layout.gutter, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (index > 0) {
                TextAction("Back", onClick = { index -= 1 }, color = EagleColors.Ink)
            }
            Spacer(Modifier.weight(1f))
            if (step == Step.Review) {
                ActionButton("Submit report", onClick = {
                    val report = draft.toReport()
                    onSubmit(report)
                    filed = report
                }, glyph = Glyph.FlagFilled)
            } else {
                ActionButton("Next", onClick = { index = (index + 1).coerceAtMost(steps.lastIndex) }, enabled = draft.isDone(step))
            }
        }
    }
}

@Composable
private fun Question(title: String, hint: String? = null) {
    EText(title, size = 26.sp, lineHeight = 32.sp, weight = FontWeight.SemiBold)
    if (hint != null) {
        Spacer(Modifier.height(6.dp))
        EText(hint, size = 15.sp, lineHeight = 22.sp, color = EagleColors.Body)
    }
    Spacer(Modifier.height(22.dp))
}

@Composable
private fun Label(text: String) {
    EText(text, size = 16.sp, weight = FontWeight.SemiBold)
    Spacer(Modifier.height(10.dp))
}

/** A large tappable card, in a grid, for the first and most important choice. */
@Composable
private fun OptionCard(glyph: Glyph, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier
            .focusRing(shape)
            .clip(shape)
            .background(if (selected) EagleColors.Surface else EagleColors.Page)
            .border(if (selected) 2.dp else 1.dp, if (selected) EagleColors.Ink else EagleColors.Line, shape)
            .selectable(
                selected = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(16.dp),
    ) {
        Icon(glyph, tint = EagleColors.Ink, size = 30.dp)
        Spacer(Modifier.height(14.dp))
        EText(title, size = 16.sp, lineHeight = 21.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(3.dp))
        EText(subtitle, size = 13.sp, lineHeight = 18.sp, color = EagleColors.Body)
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
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .focusRing(shape)
            .clip(shape)
            .background(if (selected) EagleColors.Surface else EagleColors.Page)
            .border(if (selected) 2.dp else 1.dp, if (selected) EagleColors.Ink else EagleColors.Line, shape)
            .selectable(
                selected = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            EText(title, size = 16.sp, lineHeight = 21.sp, weight = FontWeight.SemiBold)
            if (subtitle != null) EText(subtitle, size = 14.sp, lineHeight = 19.sp, color = EagleColors.Body)
        }
        if (selected) {
            Spacer(Modifier.width(10.dp))
            Icon(Glyph.Verified, tint = EagleColors.Ink, size = 22.dp)
        }
    }
}

@Composable
private fun GlyphBadge(glyph: Glyph) {
    Box(Modifier.size(44.dp).clip(CircleShape).background(EagleColors.Surface), contentAlignment = Alignment.Center) {
        Icon(glyph, tint = EagleColors.Ink, size = 22.dp)
    }
}

private val Subject.glyph: Glyph
    get() = when (this) {
        Subject.ThisProject, Subject.Project -> Glyph.HardHat
        Subject.ProjectPerson, Subject.PublicServant -> Glyph.Person
        Subject.ProjectAgency, Subject.Lgu -> Glyph.Agency
        Subject.Other -> Glyph.Chat
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
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
    Wrap(spacing = 10.dp) {
        (subject.issues() + OTHER).forEach { issue ->
            ChoiceChip(issue, onClick = { draft.toggleIssue(issue) }, selected = issue in draft.issues)
        }
    }
    if (OTHER in draft.issues) {
        Spacer(Modifier.height(16.dp))
        TextInput(draft.otherIssue, { draft.otherIssue = it }, "Describe it in a few words", singleLine = true, maxLength = 120)
    }
}

@Composable
private fun WhoStep(draft: Draft) {
    val project = draft.fromProject
    when (draft.subject) {
        Subject.ThisProject -> {
            Question("Who do you think is responsible?", "Pick one. It's fine to choose Not sure.")
            project?.people?.forEachIndexed { i, person ->
                val value = "${person.name}, ${person.role}"
                OptionRow(person.name, "${person.role} · ${person.office}", draft.pick == value, { draft.pick = value }) {
                    Avatar(person.initials, size = 44.dp, color = listOf(EagleColors.Blue, EagleColors.Green, EagleColors.GoldDeep)[i % 3])
                }
            }
            project?.contractor?.let { contractor ->
                OptionRow(contractor, "Contractor", draft.pick == contractor, { draft.pick = contractor }) { GlyphBadge(Glyph.HardHat) }
            }
            project?.proposedBy?.let { agency ->
                OptionRow(agency, "Proposing agency", draft.pick == agency, { draft.pick = agency }) { GlyphBadge(Glyph.Agency) }
            }
            OptionRow(NOT_SURE, null, draft.pick == NOT_SURE, { draft.pick = NOT_SURE }) { GlyphBadge(Glyph.Info) }
        }
        Subject.ProjectPerson -> {
            Question("Who is it about?", "The people named on this project.")
            project?.people?.forEachIndexed { i, person ->
                val value = "${person.name}, ${person.role}"
                OptionRow(person.name, "${person.role} · ${person.office}", draft.pick == value, { draft.pick = value }) {
                    Avatar(person.initials, size = 44.dp, color = listOf(EagleColors.Blue, EagleColors.Green, EagleColors.GoldDeep)[i % 3])
                }
            }
            OptionRow("Someone not listed", "A worker, guard or other official", draft.pick == OTHER, { draft.pick = OTHER }) { GlyphBadge(Glyph.Plus) }
            if (draft.pick == OTHER) {
                TextInput(draft.name, { draft.name = it }, "Their name or role, e.g. site guard", singleLine = true, maxLength = 120)
            }
        }
        Subject.ProjectAgency -> {
            Question("Which office?")
            listOfNotNull(project?.proposedBy, project?.implementedBy).distinct().forEach { office ->
                val role = if (office == project?.proposedBy) "Proposed the project" else "Carries out the project"
                OptionRow(office, role, draft.pick == office, { draft.pick = office }) { GlyphBadge(Glyph.Agency) }
            }
        }
        Subject.Project -> {
            Question("Which project?", "Pick it from the list, or tell us about one that's not here.")
            projects.forEach { p ->
                OptionRow(
                    p.title,
                    p.place,
                    draft.pickedProject == p,
                    { draft.pickedProject = p; draft.unlistedProject = false },
                ) {
                    ProjectArt(p.category, p.lastUpdate?.stage ?: 0f, p.palette, Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)))
                }
            }
            OptionRow("It's not in the app", "Name the project and where it is", draft.unlistedProject, {
                draft.unlistedProject = true
                draft.pickedProject = null
            }) { GlyphBadge(Glyph.Plus) }
            if (draft.unlistedProject) {
                TextInput(draft.name, { draft.name = it }, "e.g. Covered court, Brgy. Poblacion, Tanauan", singleLine = true, maxLength = 160)
            }
        }
        Subject.Lgu -> {
            Question("Which LGU?", "Pick the level, then tap a name or type it.")
            Wrap(spacing = 10.dp) {
                lguLevels.forEach { level -> ChoiceChip(level, onClick = { draft.pick = level }, selected = draft.pick == level) }
            }
            val level = draft.pick
            if (level != null) {
                val suggestions = lguSuggestions(level)
                Spacer(Modifier.height(22.dp))
                Label("Name (optional)")
                if (suggestions.isNotEmpty()) {
                    Wrap(spacing = 8.dp) {
                        suggestions.forEach { name -> ChoiceChip(name, onClick = { draft.name = name }, selected = draft.name == name) }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                TextInput(draft.name, { draft.name = it }, "Or type the name", singleLine = true, maxLength = 120)
            }
        }
        Subject.PublicServant -> {
            Question("Who is it about?", "Pick their role. Their name is optional.")
            Wrap(spacing = 10.dp) {
                (publicServantRoles + OTHER).forEach { role -> ChoiceChip(role, onClick = { draft.pick = role }, selected = draft.pick == role) }
            }
            Spacer(Modifier.height(22.dp))
            Label(if (draft.pick == OTHER) "Their role" else "Name (optional)")
            TextInput(
                draft.name,
                { draft.name = it },
                if (draft.pick == OTHER) "e.g. Building inspector" else "Name, if you know it",
                singleLine = true,
                maxLength = 120,
            )
        }
        Subject.Other, null -> Unit
    }
}

/** LGU names from the places of posted projects, so common answers are a tap away. */
private fun lguSuggestions(level: String): List<String> {
    val parts = projects.map { it.place.split(", ") }
    return when (level) {
        "Barangay" -> parts.mapNotNull { p -> p.firstOrNull()?.takeIf { p.size == 3 && it.startsWith("Brgy.") }?.replace("Brgy.", "Barangay") }
        "City" -> parts.mapNotNull { p -> p.getOrNull(p.size - 2)?.takeIf { it.contains("City") } }
        "Municipality" -> parts.mapNotNull { p -> p.getOrNull(p.size - 2)?.takeIf { p.size == 3 && !it.contains("City") } }
        else -> parts.mapNotNull { it.lastOrNull() }
    }.distinct().sorted()
}

@Composable
private fun PlaceTimeStep(draft: Draft) {
    Question("Where and when did it happen?")
    Label("Where")
    val site = draft.project?.let { "At the project site" }
    Wrap(spacing = 10.dp) {
        (listOfNotNull(site) + reportPlaces + NOT_SURE).forEach { place ->
            ChoiceChip(place, onClick = { draft.place = place }, selected = draft.place == place)
        }
    }
    Spacer(Modifier.height(12.dp))
    TextInput(
        draft.placeDetail,
        { draft.placeDetail = it },
        draft.project?.let { "Landmark, e.g. near the ${it.place.substringBefore(",")} approach" } ?: "Place or landmark (optional)",
        singleLine = true,
        maxLength = 160,
    )
    Spacer(Modifier.height(28.dp))
    Label("When")
    Wrap(spacing = 10.dp) {
        (reportTimes + NOT_SURE).forEach { time ->
            ChoiceChip(time, onClick = { draft.time = time }, selected = draft.time == time)
        }
    }
    Spacer(Modifier.height(12.dp))
    TextInput(draft.timeDetail, { draft.timeDetail = it }, "Date or time, if you know it (optional)", singleLine = true, maxLength = 80)
}

@Composable
private fun DetailsStep(draft: Draft) {
    Question("Anything else?", "Optional. Tick what applies, and add a few words if you like.")
    Wrap(spacing = 10.dp) {
        reportFacts.forEach { fact ->
            ChoiceChip(fact, onClick = { draft.facts = if (fact in draft.facts) draft.facts - fact else draft.facts + fact }, selected = fact in draft.facts)
        }
    }
    Spacer(Modifier.height(16.dp))
    TextInput(draft.details, { draft.details = it }, "What should the office know? Stick to what you saw or heard.", minHeight = 120.dp)
    Spacer(Modifier.height(28.dp))
    Label("Who can the office contact?")
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OptionCard(Glyph.Eye, "Stay anonymous", "Recommended. Your name is not sent.", draft.anonymous, { draft.anonymous = true }, Modifier.weight(1f).fillMaxHeight())
        OptionCard(Glyph.Person, "Share my contact", "So the office can ask you more.", !draft.anonymous, { draft.anonymous = false }, Modifier.weight(1f).fillMaxHeight())
    }
    if (!draft.anonymous) {
        Spacer(Modifier.height(12.dp))
        TextInput(draft.contact, { draft.contact = it }, "Your name and phone or email", singleLine = true, maxLength = 160)
    }
}

@Composable
private fun ReviewStep(draft: Draft, onEdit: (Step) -> Unit) {
    Question("Check your report", "Tap Edit to change an answer.")
    val subject = draft.subject ?: return
    ReviewRow("What", subject.label, Step.Subject, onEdit)
    if (subject != Subject.Other) {
        ReviewRow("About", listOf(draft.target, draft.involved).filter { it.isNotEmpty() }.joinToString("\n"), Step.Who, onEdit)
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
    Spacer(Modifier.height(8.dp))
    EText(
        text = "Report only what you saw or know. False reports can harm people and may be against the law.",
        size = 13.sp,
        lineHeight = 19.sp,
        color = EagleColors.Muted,
    )
}

@Composable
private fun ReviewRow(label: String, value: String, step: Step, onEdit: (Step) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            EText(label, size = 14.sp, weight = FontWeight.SemiBold, color = EagleColors.Body)
            Spacer(Modifier.height(2.dp))
            EText(value.ifEmpty { "—" }, size = 16.sp, lineHeight = 23.sp)
        }
        Spacer(Modifier.width(12.dp))
        TextAction("Edit", onClick = { onEdit(step) }, color = EagleColors.Ink)
    }
    Divider(Modifier.fillMaxWidth())
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
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(EagleColors.GreenSoft), contentAlignment = Alignment.Center) {
            Icon(Glyph.Check, tint = EagleColors.Green, size = 36.dp, weight = 2.6f)
        }
        Spacer(Modifier.height(20.dp))
        EText("Report saved", size = 26.sp, weight = FontWeight.SemiBold, align = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        EText("Your reference number", size = 14.sp, color = EagleColors.Body)
        EText(report.reference, display = true, size = 36.sp, lineHeight = 40.sp, color = EagleColors.Blue)
        Spacer(Modifier.height(20.dp))
        Column(
            Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(EagleColors.Surface)
                .padding(18.dp),
        ) {
            EText("What happens next", size = 16.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            EText(
                text = "In this preview, reports stay on your device and are not sent to anyone. When the service is live, " +
                    "your report will go to the office concerned, and you can follow its status under About.",
                size = 15.sp,
                lineHeight = 22.sp,
                color = EagleColors.Body,
            )
            Spacer(Modifier.height(14.dp))
            EText("Need help now?", size = 16.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            EText(
                text = "For emergencies, call 911. To file a formal complaint about a public official, you can call the " +
                    "8888 Citizens' Complaint Hotline or go to the Office of the Ombudsman.",
                size = 15.sp,
                lineHeight = 22.sp,
                color = EagleColors.Body,
            )
        }
        Spacer(Modifier.height(28.dp))
        ActionButton("Done", onClick = onDone, modifier = Modifier.widthIn(min = 200.dp))
    }
}

/** A report in the citizen's list under About. */
@Composable
fun ReportCard(report: Report, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .border(1.dp, EagleColors.Line, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Glyph.FlagFilled, tint = EagleColors.Red, size = 18.dp)
            Spacer(Modifier.width(8.dp))
            EText(report.reference, size = 15.sp, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            EText(report.filed.format(), size = 13.sp, color = EagleColors.Muted)
        }
        Spacer(Modifier.height(8.dp))
        EText(report.target, size = 16.sp, lineHeight = 22.sp, weight = FontWeight.SemiBold, maxLines = 2, ellipsis = true)
        EText(report.issues.joinToString(" · "), size = 14.sp, lineHeight = 20.sp, color = EagleColors.Body, maxLines = 2, ellipsis = true)
        Spacer(Modifier.height(10.dp))
        EText(
            text = "Saved on this device",
            modifier = Modifier.clip(RoundedCornerShape(50)).background(EagleColors.Surface).padding(horizontal = 10.dp, vertical = 4.dp),
            size = 12.sp,
            weight = FontWeight.SemiBold,
            color = EagleColors.Body,
        )
    }
}
