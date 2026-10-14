package ph.eagleswatch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.projects
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.ui.ActionButton
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.ProjectArt
import ph.eagleswatch.ui.StatusBadge
import ph.eagleswatch.ui.focusRing

/** Projects the citizen keeps an eye on, two to a row. */
@Composable
fun WatchlistScreen(citizen: CitizenState, onOpen: (Project) -> Unit, onExplore: () -> Unit, modifier: Modifier = Modifier) {
    val layout = LocalLayout.current
    val watched = projects.filter { citizen.isWatching(it) }
    Column(modifier.fillMaxSize().background(EagleColors.Page)) {
        ScreenTitle(
            "Watchlist",
            subtitle = when (watched.size) {
                0 -> null
                1 -> "1 project you follow"
                else -> "${watched.size} projects you follow"
            },
        )
        if (watched.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = layout.gutter, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.clip(RoundedCornerShape(50)).background(EagleColors.BlueSoft).padding(18.dp)) {
                    Icon(Glyph.Eye, tint = EagleColors.Blue, size = 36.dp)
                }
                Spacer(Modifier.height(18.dp))
                EText("Watch a project to follow it", size = 20.sp, weight = FontWeight.SemiBold, align = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                EText(
                    text = "Tap the eye on any project. It will be kept here, and its updates show first under Updates.",
                    modifier = Modifier.widthIn(max = 360.dp),
                    size = 15.sp,
                    lineHeight = 22.sp,
                    color = EagleColors.Body,
                    align = TextAlign.Center,
                )
                Spacer(Modifier.height(22.dp))
                ActionButton("Explore projects", onClick = onExplore)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(start = layout.gutter, end = layout.gutter, top = 8.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                items(watched, key = { it.id }) { project -> WatchedTile(project, onOpen = { onOpen(project) }) }
            }
        }
    }
}

@Composable
private fun WatchedTile(project: Project, onOpen: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(Modifier.focusRing(shape).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = onOpen,
        )) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(shape)) {
            ProjectArt(project.category, project.lastUpdate?.stage ?: 0f, project.palette, Modifier.fillMaxSize())
            StatusBadge(project.status, Modifier.align(Alignment.TopStart).padding(8.dp), onPicture = true)
        }
        Spacer(Modifier.height(8.dp))
        EText(project.title, size = 15.sp, lineHeight = 20.sp, weight = FontWeight.SemiBold, maxLines = 2, ellipsis = true)
        EText(
            text = project.lastUpdate?.let { "Updated ${it.date.format()}" } ?: project.place,
            size = 13.sp,
            color = EagleColors.Muted,
            maxLines = 1,
            ellipsis = true,
        )
    }
}
