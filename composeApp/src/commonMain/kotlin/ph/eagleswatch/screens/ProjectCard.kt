package ph.eagleswatch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Project
import ph.eagleswatch.content.Status
import ph.eagleswatch.content.pictures
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.ui.CircleButton
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.ProjectArt
import ph.eagleswatch.ui.StatusBadge
import ph.eagleswatch.ui.compactPesos
import ph.eagleswatch.ui.focusRing
import ph.eagleswatch.ui.percent

/** The status badge text: how far along an ongoing project is, or just its status. */
fun Project.statusDetail(): String? = when (status) {
    Status.Ongoing, Status.Delayed -> "$progress%"
    else -> null
}

/** A project in the Explore feed: swipeable pictures first, then the facts a citizen scans for. */
@Composable
fun ProjectCard(project: Project, citizen: CitizenState, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .focusRing(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onOpen,
            ),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.08f).clip(shape)) {
            val pictures = project.pictures
            val pager = rememberPagerState(pageCount = { pictures.size })
            HorizontalPager(pager, Modifier.fillMaxSize(), key = { it }) { page ->
                ProjectArt(project.category, pictures[page].stage, project.palette, Modifier.fillMaxSize())
            }
            StatusBadge(project.status, Modifier.align(Alignment.TopStart).padding(12.dp), project.statusDetail(), onPicture = true)
            WatchButton(project, citizen, Modifier.align(Alignment.TopEnd).padding(12.dp))
            if (pictures.size > 1) PageDots(pager, Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Top) {
            EText(
                text = project.title,
                modifier = Modifier.weight(1f),
                size = 16.sp,
                lineHeight = 21.sp,
                weight = FontWeight.SemiBold,
                maxLines = 2,
                ellipsis = true,
            )
            Spacer(Modifier.width(10.dp))
            Approval(citizen.approval(project))
        }
        Spacer(Modifier.height(3.dp))
        EText(project.place, size = 15.sp, lineHeight = 21.sp, color = EagleColors.Body, maxLines = 1, ellipsis = true)
        EText(project.proposedBy, size = 15.sp, lineHeight = 21.sp, color = EagleColors.Muted, maxLines = 1, ellipsis = true)
        EText(
            text = "${project.start.monthYear()} – ${project.end.monthYear()}",
            size = 15.sp,
            lineHeight = 21.sp,
            color = EagleColors.Muted,
        )
        Spacer(Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            EText(compactPesos(project.budget), size = 16.sp, weight = FontWeight.SemiBold)
            EText(" budget", size = 16.sp, color = EagleColors.Body)
            Spacer(Modifier.weight(1f))
            EText("${project.updates.size} updates", size = 14.sp, color = EagleColors.Muted)
        }
    }
}

/** Citizen approval, the way a listing shows its rating. */
@Composable
fun Approval(fraction: Float, modifier: Modifier = Modifier) {
    Row(modifier.padding(top = 1.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Glyph.ThumbUpFilled, tint = EagleColors.Ink, size = 15.dp)
        Spacer(Modifier.width(4.dp))
        EText(percent(fraction), size = 15.sp, weight = FontWeight.SemiBold)
    }
}

/** The eye in the corner: adds the project to the citizen's watchlist. */
@Composable
fun WatchButton(project: Project, citizen: CitizenState, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 36.dp) {
    val watching = citizen.isWatching(project)
    CircleButton(
        glyph = if (watching) Glyph.EyeFilled else Glyph.Eye,
        description = if (watching) "Stop watching ${project.title}" else "Watch ${project.title}",
        onClick = { citizen.toggleWatch(project) },
        modifier = modifier,
        tint = if (watching) EagleColors.Blue else EagleColors.Ink,
        size = size,
    )
}

@Composable
fun PageDots(pager: PagerState, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(pager.pageCount) { page ->
            val current = page == pager.currentPage
            Box(
                Modifier
                    .size(if (current) 7.dp else 6.dp)
                    .background(Color.White.copy(alpha = if (current) 1f else 0.6f), CircleShape),
            )
        }
    }
}
