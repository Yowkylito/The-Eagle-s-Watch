package ph.eagleswatch.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.eagle_logo
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalEnv
import ph.eagleswatch.ui.Page
import ph.eagleswatch.ui.Pill

/** Floating top bar. Transparent over the hero, then fades to a frosted ink bar as the page scrolls. */
@Composable
fun NavBar(modifier: Modifier = Modifier) {
    val env = LocalEnv.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val amount = (env.scroll.value / 160f).coerceIn(0f, 1f)
                drawRect(EagleColors.Ink.copy(alpha = 0.86f * amount))
                drawLine(
                    color = EagleColors.Line.copy(alpha = EagleColors.Line.alpha * amount),
                    start = Offset(0f, size.height - 0.5f),
                    end = Offset(size.width, size.height - 0.5f),
                    strokeWidth = 1f,
                )
            }
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Page {
            Row(
                modifier = Modifier.fillMaxWidth().height(NavHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Brand(
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = { env.navigate("top") },
                    ),
                )
                Spacer(Modifier.weight(1f))
                if (env.wide) {
                    NavLink("The trail") { env.navigate("trail") }
                    Spacer(Modifier.width(28.dp))
                    NavLink("Explorer") { env.navigate("explorer") }
                    Spacer(Modifier.width(28.dp))
                    NavLink("Principles") { env.navigate("principles") }
                    Spacer(Modifier.width(32.dp))
                }
                Pill(label = "Start tracing", onClick = { env.navigate("explorer") }, primary = true, compact = true)
            }
        }
    }
}

val NavHeight = 68.dp

@Composable
private fun Brand(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Image(
            painter = painterResource(Res.drawable.eagle_logo),
            contentDescription = "The Eagle's Watch logo",
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)),
        )
        Column {
            EText("THE EAGLE'S WATCH", size = 13.sp, weight = FontWeight.Bold, letterSpacing = 2.sp)
            EText("BANTAY BUWIS", size = 9.sp, mono = true, color = EagleColors.Gold, letterSpacing = 2.4.sp)
        }
    }
}

@Composable
private fun NavLink(label: String, onClick: () -> Unit) {
    EText(
        text = label,
        size = 14.sp,
        weight = FontWeight.Medium,
        color = EagleColors.Body,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = onClick,
        ),
    )
}
