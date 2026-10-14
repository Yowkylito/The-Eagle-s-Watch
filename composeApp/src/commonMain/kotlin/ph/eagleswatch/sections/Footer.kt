package ph.eagleswatch.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.eagle_logo
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.Page

@Composable
fun Footer() {
    Box(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(EagleColors.Line))
        Page {
            Column(Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(vertical = 40.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Image(
                        painter = painterResource(Res.drawable.eagle_logo),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)),
                    )
                    Column {
                        EText("THE EAGLE'S WATCH", size = 14.sp, weight = FontWeight.Bold, letterSpacing = 2.sp)
                        EText(
                            text = "Built by citizens, for citizens.",
                            size = 13.sp,
                            color = EagleColors.Muted,
                        )
                    }
                }
                Spacer(Modifier.height(28.dp))
                EText(
                    text = "NON-PARTISAN · OPEN SOURCE · 2026",
                    size = 10.sp,
                    mono = true,
                    color = EagleColors.Muted,
                    letterSpacing = 1.8.sp,
                )
            }
        }
    }
}
