package ph.eagleswatch.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ph.eagleswatch.content.GITHUB_URL
import ph.eagleswatch.content.SUGGEST_SOURCE_URL
import ph.eagleswatch.content.plannedSources
import ph.eagleswatch.content.principles
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.eagle_logo
import ph.eagleswatch.ui.Chip
import ph.eagleswatch.ui.Divider
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.FlagMark
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Icon
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.Sun
import ph.eagleswatch.ui.Wrap

/** What the project is, what it promises, and how to take part. */
@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val layout = LocalLayout.current
    val uriHandler = LocalUriHandler.current
    Column(modifier.fillMaxSize().background(EagleColors.Page).verticalScroll(rememberScrollState())) {
        ScreenTitle("About")
        Column(Modifier.padding(horizontal = layout.gutter).widthIn(max = 760.dp)) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(EagleColors.Blue)) {
                Sun(Modifier.align(Alignment.TopEnd).offset(x = 80.dp, y = (-80).dp).size(240.dp), color = Color.White.copy(alpha = 0.08f))
                Column(Modifier.padding(24.dp)) {
                    EText("Follow every peso.", display = true, size = 40.sp, lineHeight = 40.sp, color = Color.White)
                    Spacer(Modifier.height(6.dp))
                    EText("Sundan ang bawat piso.", size = 16.sp, weight = FontWeight.SemiBold, color = EagleColors.Gold)
                    Spacer(Modifier.height(12.dp))
                    EText(
                        text = "Government agencies post their projects here: what they cost, where the money comes from, " +
                            "who is responsible, and how the work is going. Citizens watch, react and give feedback.",
                        size = 16.sp,
                        lineHeight = 24.sp,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
            EText("Records, not accusations", size = 21.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            principles.forEach { principle ->
                Row(Modifier.padding(bottom = 16.dp)) {
                    Icon(Glyph.Verified, tint = EagleColors.Blue, size = 22.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        EText(principle.title, size = 16.sp, weight = FontWeight.SemiBold)
                        EText(principle.body, size = 15.sp, lineHeight = 22.sp, color = EagleColors.Body)
                    }
                }
            }

            Divider(Modifier.fillMaxWidth().padding(vertical = 18.dp))
            EText("For government agencies", size = 21.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            EText(
                text = "Each agency will get an account to post projects, upload photos and updates, and name the officials " +
                    "responsible. Every figure links to its official record.",
                size = 15.sp,
                lineHeight = 22.sp,
                color = EagleColors.Body,
            )

            Divider(Modifier.fillMaxWidth().padding(vertical = 18.dp))
            EText("Public records we plan to connect", size = 21.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            Wrap(spacing = 10.dp) { plannedSources.forEach { Chip(it) } }

            Divider(Modifier.fillMaxWidth().padding(vertical = 18.dp))
            LinkRow(Glyph.Document, "View the code on GitHub", "The Eagle's Watch is open source") { uriHandler.openUri(GITHUB_URL) }
            LinkRow(Glyph.Plus, "Suggest a data source", "Point us to a public record we should connect") { uriHandler.openUri(SUGGEST_SOURCE_URL) }

            Divider(Modifier.fillMaxWidth().padding(vertical = 18.dp))
            EText("Where the project stands", size = 21.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            EText(
                text = "A design preview. Every project, person and figure in the app is a sample, not real government data.",
                size = 15.sp,
                lineHeight = 22.sp,
                color = EagleColors.Body,
            )

            Spacer(Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.eagle_logo),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(11.dp)),
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    EText("Built by citizens, for citizens.", size = 15.sp, weight = FontWeight.SemiBold)
                    EText("Non-partisan and open source.", size = 14.sp, color = EagleColors.Muted)
                }
            }
            Spacer(Modifier.height(14.dp))
            FlagMark()
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LinkRow(glyph: Glyph, title: String, body: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(glyph, tint = EagleColors.Ink, size = 24.dp)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            EText(title, size = 16.sp, weight = FontWeight.SemiBold)
            EText(body, size = 14.sp, color = EagleColors.Body)
        }
        Icon(Glyph.ChevronRight, tint = EagleColors.Muted, size = 20.dp)
    }
}
