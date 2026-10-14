package ph.eagleswatch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.eagleswatch.content.Project
import ph.eagleswatch.ui.CircleButton
import ph.eagleswatch.ui.Divider
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.ProjectArt

/** The agency's full write-up of a project, set for reading. */
@Composable
fun ArticleScreen(project: Project, onBack: () -> Unit) {
    val layout = LocalLayout.current
    val posted = project.lastUpdate?.date ?: project.start
    Box(Modifier.fillMaxSize().background(EagleColors.Page)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircleButton(Glyph.Back, "Back", onBack)
            }
            Column(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 680.dp)
                    .padding(horizontal = layout.gutter)
                    .windowInsetsPadding(WindowInsets.navigationBars),
            ) {
                Spacer(Modifier.height(8.dp))
                EText(project.category.label + " · " + project.level.label, size = 14.sp, weight = FontWeight.SemiBold, color = EagleColors.Blue)
                Spacer(Modifier.height(8.dp))
                EText(project.title, size = 30.sp, lineHeight = 36.sp, weight = FontWeight.SemiBold)
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Glyph.Agency, tint = EagleColors.Body, size = 18.dp)
                    Spacer(Modifier.width(8.dp))
                    EText(
                        text = "Posted by ${project.proposedBy} · ${posted.format()}",
                        modifier = Modifier.weight(1f),
                        size = 14.sp,
                        lineHeight = 20.sp,
                        color = EagleColors.Body,
                    )
                }
                Spacer(Modifier.height(20.dp))
                ProjectArt(
                    project.category,
                    project.lastUpdate?.stage ?: 0f,
                    project.palette,
                    Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(16.dp)),
                )
                Spacer(Modifier.height(24.dp))
                EText(project.purpose, size = 19.sp, lineHeight = 29.sp, weight = FontWeight.SemiBold)
                project.article.forEach { paragraph ->
                    Spacer(Modifier.height(18.dp))
                    EText(paragraph, size = 17.sp, lineHeight = 28.sp, color = EagleColors.Ink)
                }
                Divider(Modifier.fillMaxWidth().padding(vertical = 28.dp))
                EText("Sample article for the design preview. It is not a real government record.", size = 13.sp, color = EagleColors.Muted)
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
