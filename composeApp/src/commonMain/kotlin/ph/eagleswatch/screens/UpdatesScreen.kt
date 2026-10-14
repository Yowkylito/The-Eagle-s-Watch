package ph.eagleswatch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.Update
import ph.eagleswatch.content.projects
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.ui.ChoiceChip
import ph.eagleswatch.ui.Divider
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.ProjectArt

/** Every post from every agency, newest first, like an inbox for public works. */
@Composable
fun UpdatesScreen(citizen: CitizenState, onOpen: (Project) -> Unit, modifier: Modifier = Modifier) {
    val layout = LocalLayout.current
    var watchingOnly by rememberSaveable { mutableStateOf(false) }
    val feed = projects
        .filter { !watchingOnly || citizen.isWatching(it) }
        .flatMap { project -> project.updates.map { project to it } }
        .sortedByDescending { (_, update) -> update.date }

    Column(modifier.fillMaxSize().background(EagleColors.Page)) {
        ScreenTitle("Updates", subtitle = "What agencies posted about their projects")
        Row(Modifier.padding(horizontal = layout.gutter), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip("All projects", onClick = { watchingOnly = false }, selected = !watchingOnly)
            ChoiceChip("Watching · ${citizen.watched.size}", onClick = { watchingOnly = true }, selected = watchingOnly)
        }
        LazyColumn(
            contentPadding = PaddingValues(start = layout.gutter, end = layout.gutter, top = 12.dp, bottom = 32.dp),
        ) {
            if (feed.isEmpty()) {
                item {
                    EText(
                        text = "No updates yet from projects you watch. Tap the eye on a project to follow it.",
                        modifier = Modifier.padding(vertical = 32.dp),
                        size = 15.sp,
                        lineHeight = 22.sp,
                        color = EagleColors.Body,
                    )
                }
            }
            items(feed, key = { (project, update) -> project.id + update.date.epochDay }) { (project, update) ->
                UpdateItem(project, update, onOpen = { onOpen(project) })
                Divider(Modifier.fillMaxWidth().widthIn(max = 760.dp))
            }
        }
    }
}

@Composable
private fun UpdateItem(project: Project, update: Update, onOpen: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ProjectArt(project.category, update.stage, project.palette, Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EText(
                    text = project.implementedBy,
                    modifier = Modifier.weight(1f),
                    size = 13.sp,
                    color = EagleColors.Muted,
                    maxLines = 1,
                    ellipsis = true,
                )
                Spacer(Modifier.width(8.dp))
                EText(update.date.format(), size = 13.sp, color = EagleColors.Muted)
            }
            EText(update.title, size = 16.sp, lineHeight = 22.sp, weight = FontWeight.SemiBold)
            EText(project.title, size = 14.sp, lineHeight = 20.sp, color = EagleColors.Blue, maxLines = 1, ellipsis = true)
            EText(update.body, size = 14.sp, lineHeight = 20.sp, color = EagleColors.Body, maxLines = 2, ellipsis = true)
        }
    }
}
