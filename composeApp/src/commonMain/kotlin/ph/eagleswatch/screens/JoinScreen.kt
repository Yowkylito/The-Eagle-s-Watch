package ph.eagleswatch.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ph.eagleswatch.content.GITHUB_URL
import ph.eagleswatch.content.SUGGEST_SOURCE_URL
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.eagle_logo
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.FlagMark
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.Pill
import ph.eagleswatch.ui.PillTone

/** Ways to take part, and an honest account of where the project stands. */
@Composable
fun JoinScreen(modifier: Modifier = Modifier) {
    val layout = LocalLayout.current
    val uriHandler = LocalUriHandler.current
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = layout.gutter, vertical = if (layout.wide) 20.dp else 16.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(if (layout.wide) 32.dp else 28.dp))
                .background(EagleColors.Stage)
                .padding(if (layout.wide) 48.dp else 24.dp),
        ) {
            EText(
                text = "The best watchdog is an informed citizen.",
                modifier = Modifier.widthIn(max = 640.dp),
                display = true,
                size = if (layout.wide) 64.sp else 42.sp,
                lineHeight = if (layout.wide) 62.sp else 42.sp,
                color = Color.White,
            )
            Spacer(Modifier.height(14.dp))
            EText(
                text = "The Eagle's Watch is open source. Follow the build, suggest data sources, or help us connect them.",
                modifier = Modifier.widthIn(max = 520.dp),
                size = 17.sp,
                lineHeight = 27.sp,
                color = Color.White.copy(alpha = 0.88f),
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Pill("View on GitHub", onClick = { uriHandler.openUri(GITHUB_URL) }, tone = PillTone.Gold)
                Pill("Suggest a source", onClick = { uriHandler.openUri(SUGGEST_SOURCE_URL) }, tone = PillTone.Ghost)
            }
        }

        Spacer(Modifier.height(28.dp))
        EText("Where the project stands", size = 18.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Status(
            label = "Now",
            text = "A design preview. Every figure in the app is a placeholder, not real government data.",
        )
        Spacer(Modifier.height(12.dp))
        Status(
            label = "Next",
            text = "Connect the official public sources listed under How it works.",
        )

        Spacer(Modifier.height(36.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Image(
                painter = painterResource(Res.drawable.eagle_logo),
                contentDescription = null,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(11.dp)),
            )
            Column {
                EText("Built by citizens, for citizens.", size = 15.sp, weight = FontWeight.SemiBold)
                EText("Non-partisan and open source.", size = 14.sp, color = EagleColors.Muted)
            }
        }
        Spacer(Modifier.height(14.dp))
        FlagMark()
    }
}

@Composable
private fun Status(label: String, text: String) {
    Row {
        EText(
            text = label,
            modifier = Modifier.width(48.dp),
            display = true,
            size = 22.sp,
            lineHeight = 24.sp,
            color = EagleColors.Blue,
        )
        EText(text, modifier = Modifier.widthIn(max = 560.dp), size = 16.sp, lineHeight = 24.sp, color = EagleColors.Body)
    }
}
