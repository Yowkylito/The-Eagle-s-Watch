package ph.eagleswatch.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Day
import ph.eagleswatch.content.Feedback
import ph.eagleswatch.content.Person
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.Status
import ph.eagleswatch.content.Update
import ph.eagleswatch.content.pictures
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.state.Note
import ph.eagleswatch.state.Reaction
import ph.eagleswatch.state.shareText
import ph.eagleswatch.ui.ActionButton
import ph.eagleswatch.ui.Avatar
import ph.eagleswatch.ui.ButtonStyle
import ph.eagleswatch.ui.CircleButton
import ph.eagleswatch.ui.Divider
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.LocalFonts
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.ProgressBar
import ph.eagleswatch.ui.ProjectArt
import ph.eagleswatch.ui.StatusBadge
import ph.eagleswatch.ui.TextAction
import ph.eagleswatch.ui.compactCount
import ph.eagleswatch.ui.compactPesos
import ph.eagleswatch.ui.focusRing
import ph.eagleswatch.ui.formatPesos
import ph.eagleswatch.ui.glyph
import ph.eagleswatch.ui.groupDigits
import ph.eagleswatch.ui.percent

private val AvatarColors = listOf(EagleColors.Blue, EagleColors.Green, EagleColors.GoldDeep, EagleColors.Ink)

/** Everything posted about one project: pictures, money, dates, people, updates and what citizens say. */
@Composable
fun ProjectScreen(
    project: Project,
    citizen: CitizenState,
    onBack: () -> Unit,
    onArticle: () -> Unit,
    onReport: () -> Unit,
) {
    val layout = LocalLayout.current
    var writing by remember { mutableStateOf(false) }
    val share = { shareText(project.title, "${project.title} · ${project.place}\n${compactPesos(project.budget)} budget, ${project.status.label.lowercase()}. Seen on The Eagle's Watch.") }

    Box(Modifier.fillMaxSize().background(EagleColors.Page)) {
        if (layout.wide) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = layout.gutter, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircleButton(Glyph.Back, "Back", onBack)
                    Spacer(Modifier.weight(1f))
                    CircleButton(Glyph.Flag, "Report this project", onReport)
                    Spacer(Modifier.width(10.dp))
                    CircleButton(Glyph.Share, "Share", share)
                    Spacer(Modifier.width(10.dp))
                    WatchButton(project, citizen)
                }
                Column(Modifier.align(Alignment.CenterHorizontally).widthIn(max = 1120.dp).padding(horizontal = layout.gutter)) {
                    Gallery(project, height = 460.dp, rounded = true)
                    Spacer(Modifier.height(28.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(56.dp)) {
                        Column(Modifier.weight(1f)) { Details(project, citizen, onArticle, onFeedback = { writing = true }, onReport = onReport) }
                        Column(Modifier.width(360.dp)) { SideCard(project, citizen, onFeedback = { writing = true }) }
                    }
                    Spacer(Modifier.height(48.dp))
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Box {
                        Gallery(project, height = 340.dp, rounded = false)
                        Row(
                            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircleButton(Glyph.Back, "Back", onBack)
                            Spacer(Modifier.weight(1f))
                            CircleButton(Glyph.Flag, "Report this project", onReport)
                            Spacer(Modifier.width(10.dp))
                            CircleButton(Glyph.Share, "Share", share)
                            Spacer(Modifier.width(10.dp))
                            WatchButton(project, citizen)
                        }
                    }
                    Column(Modifier.padding(horizontal = layout.gutter, vertical = 22.dp)) {
                        Details(project, citizen, onArticle, onFeedback = { writing = true }, onReport = onReport)
                    }
                }
                BottomBar(project, citizen, onFeedback = { writing = true })
            }
        }

        AnimatedVisibility(visible = writing, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { writing = false },
            ))
        }
        AnimatedVisibility(
            visible = writing,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
            FeedbackSheet(project, onPost = { citizen.addNote(project, it); writing = false }, onCancel = { writing = false })
        }
    }
}

@Composable
private fun Gallery(project: Project, height: Dp, rounded: Boolean) {
    val pictures = project.pictures
    val pager = rememberPagerState(pageCount = { pictures.size })
    val shape = RoundedCornerShape(if (rounded) 20.dp else 0.dp)
    Box(Modifier.fillMaxWidth().height(height).clip(shape)) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
            ProjectArt(project.category, pictures[page].stage, project.palette, Modifier.fillMaxSize())
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(90.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)))),
        )
        EText(
            text = pictures[pager.currentPage].caption,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, end = 84.dp, bottom = 14.dp),
            size = 13.sp,
            lineHeight = 18.sp,
            weight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 2,
            ellipsis = true,
        )
        EText(
            text = "${pager.currentPage + 1} / ${pictures.size}",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 9.dp, vertical = 4.dp),
            size = 12.sp,
            weight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

@Composable
private fun Section(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Divider(Modifier.fillMaxWidth().padding(vertical = 26.dp))
    if (title != null) {
        EText(title, size = 21.sp, lineHeight = 26.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))
    }
    Column(content = content)
}

@Composable
private fun Details(project: Project, citizen: CitizenState, onArticle: () -> Unit, onFeedback: () -> Unit, onReport: () -> Unit) {
    val today = remember { Day.today() }
    EText(
        text = "Sample project for the design preview. It is not a real government record.",
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(EagleColors.RedSoft).padding(horizontal = 12.dp, vertical = 9.dp),
        size = 13.sp,
        lineHeight = 18.sp,
        color = EagleColors.Red,
    )
    Spacer(Modifier.height(18.dp))
    StatusBadge(project.status, detail = project.statusDetail())
    Spacer(Modifier.height(10.dp))
    EText(project.title, size = 26.sp, lineHeight = 32.sp, weight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    IconLine(Glyph.Pin, project.place)
    IconLine(project.category.glyph, "${project.category.label} · ${project.level.label} project")
    Spacer(Modifier.height(20.dp))
    Scorecard(project, citizen)

    Section {
        PersonRow(project.lead, "Led by ${project.lead.name}", AvatarColors[0], verified = true)
    }

    Section {
        Highlight(Glyph.Peso, "${formatPesos(project.budget)} budget", "Funded by ${project.fundedBy}")
        Highlight(Glyph.Calendar, "${project.start.format()} to ${project.end.format()}", timing(project, today))
        Highlight(
            Glyph.Agency,
            "Proposed by ${project.proposedBy}",
            if (project.implementedBy != project.proposedBy) "Carried out by ${project.implementedBy}" else "Also the implementing office",
        )
        if (project.contractor != null) Highlight(Glyph.HardHat, "Built by ${project.contractor}", "Contractor")
    }

    Section("Progress") { ProgressBlock(project, today) }

    Section("About this project") {
        EText(project.purpose, size = 16.sp, lineHeight = 25.sp, color = EagleColors.Ink)
        Spacer(Modifier.height(10.dp))
        EText(project.article.first(), size = 16.sp, lineHeight = 25.sp, color = EagleColors.Body, maxLines = 3, ellipsis = true)
        Spacer(Modifier.height(16.dp))
        ActionButton("Read the full article", onClick = onArticle, style = ButtonStyle.Outline)
    }

    Section("Updates · ${project.updates.size}") { UpdatesBlock(project) }

    Section("Who's responsible") {
        project.people.forEachIndexed { index, person ->
            if (index > 0) Spacer(Modifier.height(16.dp))
            PersonRow(person, person.name, AvatarColors[index % AvatarColors.size], verified = false)
        }
    }

    Section { FeedbackBlock(project, citizen, onFeedback) }

    Section("Records") {
        project.records.forEach { record ->
            Row(Modifier.padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Glyph.Document, tint = EagleColors.Ink, size = 22.dp)
                Spacer(Modifier.width(14.dp))
                EText(record, modifier = Modifier.weight(1f), size = 16.sp)
            }
        }
        EText(
            text = "Each record will link to the official document once live data is connected.",
            size = 13.sp,
            lineHeight = 19.sp,
            color = EagleColors.Muted,
        )
    }

    Section {
        // As on a listing page: the report link sits at the very end, easy to find but out of the way.
        Row(
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(role = Role.Button, onClick = onReport)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Glyph.FlagFilled, tint = EagleColors.Ink, size = 20.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                EText("Report this project", size = 16.sp, weight = FontWeight.SemiBold, decoration = TextDecoration.Underline)
                EText("A problem with the works, the money or the people in charge", size = 14.sp, lineHeight = 20.sp, color = EagleColors.Body)
            }
        }
    }
}

@Composable
private fun IconLine(glyph: Glyph, text: String) {
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(glyph, tint = EagleColors.Body, size = 17.dp)
        Spacer(Modifier.width(8.dp))
        EText(text, size = 15.sp, lineHeight = 21.sp, color = EagleColors.Body)
    }
}

/** Approval, reactions and money at a glance, in one bordered card like a listing's rating strip. */
@Composable
private fun Scorecard(project: Project, citizen: CitizenState) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .border(1.dp, EagleColors.Line, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Score(percent(citizen.approval(project)), "approve", Modifier.weight(1f))
        Box(Modifier.width(1.dp).fillMaxHeight().background(EagleColors.Line))
        Score(compactCount(citizen.likes(project) + citizen.dislikes(project)), "reactions", Modifier.weight(1f))
        Box(Modifier.width(1.dp).fillMaxHeight().background(EagleColors.Line))
        Score(compactPesos(project.budget), "budget", Modifier.weight(1f))
    }
}

@Composable
private fun Score(value: String, label: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        EText(value, display = true, size = 26.sp, lineHeight = 28.sp)
        EText(label, size = 13.sp, color = EagleColors.Body)
    }
}

@Composable
private fun PersonRow(person: Person, headline: String, color: Color, verified: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            Avatar(person.initials, size = 52.dp, color = color)
            if (verified) {
                Box(Modifier.align(Alignment.BottomEnd).size(20.dp).background(EagleColors.Page, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Glyph.Verified, tint = EagleColors.Blue, size = 18.dp)
                }
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            EText(headline, size = 16.sp, lineHeight = 22.sp, weight = FontWeight.SemiBold)
            EText("${person.role} · ${person.office}", size = 14.sp, lineHeight = 20.sp, color = EagleColors.Body)
        }
    }
}

@Composable
private fun Highlight(glyph: Glyph, title: String, body: String) {
    Row(Modifier.padding(bottom = 20.dp)) {
        Icon(glyph, tint = EagleColors.Ink, size = 26.dp)
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            EText(title, size = 16.sp, lineHeight = 22.sp, weight = FontWeight.SemiBold)
            EText(body, size = 14.sp, lineHeight = 20.sp, color = EagleColors.Body)
        }
    }
}

private fun timing(project: Project, today: Day): String {
    val days = project.durationDays
    return when {
        today < project.start -> "$days days planned · starts in ${project.start.epochDay - today.epochDay} days"
        project.status == Status.Completed -> "$days days planned · finished"
        today > project.end -> "$days days planned · ${today.epochDay - project.end.epochDay} days past the target date"
        else -> "$days days planned · ${project.end.epochDay - today.epochDay} days left"
    }
}

/** Work done against time used: the gap between the two bars is the honest measure of a delay. */
@Composable
private fun ProgressBlock(project: Project, today: Day) {
    val work = project.progress / 100f
    val time = project.elapsed(today)
    Bar("Work done", percent(work), work, EagleColors.Blue)
    Spacer(Modifier.height(16.dp))
    Bar("Time used", if (project.status == Status.Completed) "Done" else percent(time), if (project.status == Status.Completed) 1f else time, EagleColors.Ink.copy(alpha = 0.55f))
    Spacer(Modifier.height(16.dp))
    val released = project.released.toFloat() / project.budget
    Bar("Funds released", "${compactPesos(project.released)} of ${compactPesos(project.budget)}", released, EagleColors.Gold)
    val gap = ((time - work) * 100).toInt()
    val note = when {
        project.status == Status.Completed -> "Finished and turned over."
        project.status == Status.Planned -> "Works have not started."
        gap > 10 -> "Work is $gap points behind the time used."
        else -> "Work is keeping pace with the schedule."
    }
    Spacer(Modifier.height(14.dp))
    EText(note, size = 14.sp, lineHeight = 20.sp, color = if (gap > 10 && project.status != Status.Completed) EagleColors.Amber else EagleColors.Body)
}

@Composable
private fun Bar(label: String, value: String, fraction: Float, color: Color) {
    Row {
        EText(label, modifier = Modifier.weight(1f), size = 15.sp, color = EagleColors.Body)
        EText(value, size = 15.sp, weight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(7.dp))
    ProgressBar(fraction, color = color)
}

@Composable
private fun UpdatesBlock(project: Project) {
    var showAll by remember { mutableStateOf(false) }
    val updates = project.updates.sortedByDescending { it.date }
    val shown = if (showAll) updates else updates.take(3)
    shown.forEachIndexed { index, update ->
        UpdateRow(project, update, last = index == shown.lastIndex)
    }
    if (updates.size > 3) {
        Spacer(Modifier.height(4.dp))
        ActionButton(
            if (showAll) "Show fewer updates" else "Show all ${updates.size} updates",
            onClick = { showAll = !showAll },
            style = ButtonStyle.Outline,
        )
    }
}

@Composable
private fun UpdateRow(project: Project, update: Update, last: Boolean) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        // A dotted timeline down the left edge.
        Column(Modifier.width(14.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(6.dp))
            Box(Modifier.size(10.dp).background(EagleColors.Blue, CircleShape))
            if (!last) Box(Modifier.width(2.dp).weight(1f).background(EagleColors.Line))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f).padding(bottom = 22.dp)) {
            EText(update.date.format(), size = 13.sp, color = EagleColors.Muted)
            EText(update.title, size = 16.sp, lineHeight = 22.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            EText(update.body, size = 15.sp, lineHeight = 22.sp, color = EagleColors.Body)
            Spacer(Modifier.height(10.dp))
            ProjectArt(
                project.category,
                update.stage,
                project.palette,
                Modifier.fillMaxWidth().widthIn(max = 420.dp).height(150.dp).clip(RoundedCornerShape(12.dp)),
            )
        }
    }
}

@Composable
private fun FeedbackBlock(project: Project, citizen: CitizenState, onFeedback: () -> Unit) {
    val likes = citizen.likes(project)
    val dislikes = citizen.dislikes(project)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Glyph.ThumbUpFilled, tint = EagleColors.Ink, size = 26.dp)
        Spacer(Modifier.width(10.dp))
        EText("${percent(citizen.approval(project))} approve", size = 21.sp, weight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(4.dp))
    EText("${groupDigits(likes.toLong())} likes and ${groupDigits(dislikes.toLong())} dislikes from citizens", size = 14.sp, color = EagleColors.Body)
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))) {
        Box(Modifier.weight(likes.toFloat().coerceAtLeast(1f)).fillMaxHeight().background(EagleColors.Blue))
        Spacer(Modifier.width(3.dp))
        Box(Modifier.weight(dislikes.toFloat().coerceAtLeast(1f)).fillMaxHeight().background(EagleColors.Line))
    }
    Spacer(Modifier.height(20.dp))
    val mine = citizen.notes[project.id].orEmpty()
    val layout = LocalLayout.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(mine) { MyNoteCard(it) }
        items(project.feedback) { FeedbackCard(it) }
    }
    Spacer(Modifier.height(18.dp))
    if (!layout.wide) ActionButton("Give feedback", onClick = onFeedback, style = ButtonStyle.Outline, glyph = Glyph.Chat)
}

@Composable
private fun FeedbackCardFrame(name: String, sub: String, date: Day, text: String, color: Color, tag: Glyph?) {
    Column(
        Modifier
            .width(280.dp)
            .heightIn(min = 170.dp)
            .border(1.dp, EagleColors.Line, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(name.take(1).uppercase(), size = 38.dp, color = color)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                EText(name, size = 15.sp, weight = FontWeight.SemiBold, maxLines = 1, ellipsis = true)
                EText("$sub · ${date.monthYear()}", size = 13.sp, color = EagleColors.Muted, maxLines = 1, ellipsis = true)
            }
            if (tag != null) Icon(tag, tint = EagleColors.Body, size = 18.dp)
        }
        Spacer(Modifier.height(12.dp))
        EText(text, size = 15.sp, lineHeight = 22.sp, color = EagleColors.Ink, maxLines = 5, ellipsis = true)
    }
}

@Composable
private fun FeedbackCard(feedback: Feedback) = FeedbackCardFrame(
    name = feedback.name,
    sub = feedback.place,
    date = feedback.date,
    text = feedback.text,
    color = if (feedback.liked) EagleColors.Green else EagleColors.Muted,
    tag = if (feedback.liked) Glyph.ThumbUp else Glyph.ThumbDown,
)

@Composable
private fun MyNoteCard(note: Note) = FeedbackCardFrame(
    name = "You",
    sub = "On this device",
    date = note.day,
    text = note.text,
    color = EagleColors.Blue,
    tag = null,
)

/** Like and dislike, the two reactions every citizen can give. */
@Composable
private fun ReactionButton(selected: Boolean, glyph: Glyph, selectedGlyph: Glyph, count: Int, description: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .focusRing(shape)
            .clip(shape)
            .background(if (selected) EagleColors.BlueSoft else EagleColors.Page)
            .border(1.dp, if (selected) EagleColors.Blue else EagleColors.Line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(if (selected) selectedGlyph else glyph, tint = if (selected) EagleColors.Blue else EagleColors.Ink, size = 20.dp)
        Spacer(Modifier.width(6.dp))
        EText(compactCount(count), size = 15.sp, weight = FontWeight.SemiBold, color = if (selected) EagleColors.Blue else EagleColors.Ink)
    }
}

@Composable
private fun Reactions(project: Project, citizen: CitizenState) {
    val reaction = citizen.reactions[project.id]
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ReactionButton(reaction == Reaction.Like, Glyph.ThumbUp, Glyph.ThumbUpFilled, citizen.likes(project), "Like this project") {
            citizen.react(project, Reaction.Like)
        }
        ReactionButton(reaction == Reaction.Dislike, Glyph.ThumbDown, Glyph.ThumbDownFilled, citizen.dislikes(project), "Dislike this project") {
            citizen.react(project, Reaction.Dislike)
        }
    }
}

/** Phones: reactions and the feedback button stay in reach at the bottom, like a booking bar. */
@Composable
private fun BottomBar(project: Project, citizen: CitizenState, onFeedback: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(EagleColors.Page)) {
        Divider(Modifier.fillMaxWidth())
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Reactions(project, citizen)
            Spacer(Modifier.weight(1f))
            ActionButton("Feedback", onClick = onFeedback, glyph = Glyph.Chat)
        }
    }
}

/** Wide windows: the same actions in a card beside the details. */
@Composable
private fun SideCard(project: Project, citizen: CitizenState, onFeedback: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(EagleColors.Page)
            .border(1.dp, EagleColors.Line, RoundedCornerShape(18.dp))
            .padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            EText(compactPesos(project.budget), display = true, size = 36.sp, lineHeight = 38.sp)
            Spacer(Modifier.width(6.dp))
            EText("budget", modifier = Modifier.padding(bottom = 4.dp), size = 16.sp, color = EagleColors.Body)
        }
        EText("Funded by ${project.fundedBy}", size = 14.sp, lineHeight = 20.sp, color = EagleColors.Body)
        Spacer(Modifier.height(18.dp))
        EText("What do you think of this project?", size = 15.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Reactions(project, citizen)
        Spacer(Modifier.height(16.dp))
        ActionButton("Give feedback", onClick = onFeedback, glyph = Glyph.Chat, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        EText(
            text = "Agencies will see likes, dislikes and feedback once accounts are connected.",
            size = 13.sp,
            lineHeight = 19.sp,
            color = EagleColors.Muted,
        )
    }
}

@Composable
private fun FeedbackSheet(project: Project, onPost: (String) -> Unit, onCancel: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val fonts = LocalFonts.current
    Column(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(EagleColors.Page)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .imePadding()
            .padding(24.dp),
    ) {
        EText("Give feedback", size = 21.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        EText(project.title, size = 15.sp, color = EagleColors.Body, maxLines = 1, ellipsis = true)
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 130.dp)
                .border(1.dp, EagleColors.Line, RoundedCornerShape(14.dp))
                .padding(14.dp),
        ) {
            if (text.isEmpty()) {
                EText("What should the agency know? Be specific and respectful.", size = 16.sp, lineHeight = 23.sp, color = EagleColors.Muted)
            }
            BasicTextField(
                value = text,
                onValueChange = { if (it.length <= 1000) text = it },
                textStyle = TextStyle(fontFamily = fonts.sans, fontSize = 16.sp, lineHeight = 23.sp, color = EagleColors.Ink),
                cursorBrush = SolidColor(EagleColors.Blue),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Your feedback" },
            )
        }
        Spacer(Modifier.height(8.dp))
        EText("In this preview, feedback is saved on your device only.", size = 13.sp, color = EagleColors.Muted)
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextAction("Cancel", onClick = onCancel, color = EagleColors.Ink)
            Spacer(Modifier.weight(1f))
            ActionButton("Post feedback", onClick = { if (text.isNotBlank()) onPost(text) })
        }
    }
}
