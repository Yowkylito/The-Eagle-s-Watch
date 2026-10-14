package ph.eagleswatch.sections

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import ph.eagleswatch.content.trail
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.LocalEnv
import ph.eagleswatch.ui.Page
import ph.eagleswatch.ui.Pill
import ph.eagleswatch.ui.Reveal
import ph.eagleswatch.ui.Tag
import ph.eagleswatch.ui.anchored

@Composable
fun Hero() {
    val env = LocalEnv.current
    Box(Modifier.fillMaxWidth().anchored("top").heroBackdrop()) {
        Page {
            Spacer(Modifier.height(if (env.wide) 136.dp else 108.dp))
            if (env.wide) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HeroCopy(Modifier.weight(1.05f))
                    Spacer(Modifier.width(56.dp))
                    PesoFlow(Modifier.weight(0.95f).height(580.dp))
                }
            } else {
                HeroCopy(Modifier.fillMaxWidth())
                Spacer(Modifier.height(48.dp))
                PesoFlow(Modifier.fillMaxWidth().height(380.dp))
            }
            Spacer(Modifier.height(if (env.wide) 96.dp else 72.dp))
        }
    }
}

@Composable
private fun HeroCopy(modifier: Modifier = Modifier) {
    val env = LocalEnv.current
    Reveal(modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Dot(EagleColors.Blue)
                Dot(EagleColors.Red)
                Dot(EagleColors.Gold)
                Spacer(Modifier.width(6.dp))
                EText(
                    text = "OPEN PUBLIC FUNDS · PHILIPPINES",
                    size = 11.sp,
                    mono = true,
                    color = EagleColors.Muted,
                    letterSpacing = 1.8.sp,
                )
            }
            Spacer(Modifier.height(28.dp))
            EText(
                text = buildAnnotatedString {
                    append("Follow every ")
                    withStyle(SpanStyle(color = EagleColors.Gold)) { append("peso.") }
                },
                size = if (env.wide) 80.sp else 48.sp,
                lineHeight = if (env.wide) 82.sp else 51.sp,
                weight = FontWeight.Bold,
                letterSpacing = if (env.wide) (-2.5).sp else (-1.5).sp,
            )
            Spacer(Modifier.height(24.dp))
            EText(
                text = "From your barangay to the highest office, trace where citizens' taxes go. " +
                    "Every figure tied to an official public record.",
                modifier = Modifier.widthIn(max = 520.dp),
                size = if (env.wide) 19.sp else 17.sp,
                lineHeight = if (env.wide) 30.sp else 27.sp,
                color = EagleColors.Body,
            )
            Spacer(Modifier.height(14.dp))
            EText(
                text = "Sundan ang bawat piso. Bantayan ang bawat buwis.",
                size = 12.sp,
                mono = true,
                color = EagleColors.Gold,
                letterSpacing = 0.4.sp,
            )
            Spacer(Modifier.height(36.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Pill("Start tracing", onClick = { env.navigate("explorer") }, primary = true)
                Pill("How it works", onClick = { env.navigate("principles") })
            }
            Spacer(Modifier.height(32.dp))
            PreviewNotice()
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(Modifier.size(7.dp).clip(CircleShape).background(color))
}

/** Honest status line: this build shows structure and sample figures only. */
@Composable
private fun PreviewNotice() {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .border(1.dp, EagleColors.Line, shape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(EagleColors.Gold))
        EText(
            text = "DESIGN PREVIEW · SAMPLE DATA ONLY",
            size = 10.sp,
            mono = true,
            color = EagleColors.Muted,
            letterSpacing = 1.2.sp,
        )
    }
}

/** Dotted grid with soft blue and gold glows behind the hero. Cached; redrawn only on resize. */
private fun Modifier.heroBackdrop(): Modifier = drawWithCache {
    val step = 32.dp.toPx()
    val points = ArrayList<Offset>()
    var y = step / 2f
    while (y < size.height) {
        var x = step / 2f
        while (x < size.width) {
            points.add(Offset(x, y))
            x += step
        }
        y += step
    }
    val reach = max(size.width * 0.55f, 1f)
    val goldGlow = Brush.radialGradient(
        colors = listOf(EagleColors.Gold.copy(alpha = 0.15f), Color.Transparent),
        center = Offset(size.width * 0.78f, size.height * 0.32f),
        radius = reach,
    )
    val blueGlow = Brush.radialGradient(
        colors = listOf(EagleColors.Blue.copy(alpha = 0.16f), Color.Transparent),
        center = Offset(size.width * 0.05f, size.height * 0.95f),
        radius = reach,
    )
    val fadeOut = Brush.verticalGradient(
        colors = listOf(Color.Transparent, EagleColors.Ink),
        startY = size.height * 0.72f,
        endY = size.height,
    )
    onDrawBehind {
        drawRect(blueGlow)
        drawRect(goldGlow)
        drawPoints(
            points = points,
            pointMode = PointMode.Points,
            color = Color(0x14FFFFFF),
            strokeWidth = 2f,
            cap = StrokeCap.Round,
        )
        drawRect(fadeOut)
    }
}

// ---------------------------------------------------------------------------------------------
// Peso flow: pesos rise from the broad base (citizens) through six tiers to a single watching eye.
// ---------------------------------------------------------------------------------------------

private const val TierCount = 6
private const val StreamCount = 13
private const val ParticlesPerStream = 3

/** Vertical position (0 = top) of tier [j], where j = 0 is the Office of the President. */
private fun tierFraction(j: Int): Float = 0.26f + 0.70f * (j / (TierCount - 1f))

@Composable
private fun PesoFlow(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "peso-flow")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 18000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )
    // Top-to-bottom order matches tierFraction: President first, citizens last.
    val tags = trail.reversed()
    BoxWithConstraints(modifier) {
        Canvas(Modifier.fillMaxSize().graphicsLayer()) { drawPesoFlow(phase.value) }
        tags.forEachIndexed { j, tier ->
            Box(
                modifier = Modifier.fillMaxWidth().offset(y = maxHeight * tierFraction(j) - 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Tag("${tier.index} · ${tier.short}")
            }
        }
    }
}

private fun DrawScope.drawPesoFlow(phase: Float) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return

    val cx = w * 0.5f
    val eyeY = h * 0.09f
    val endY = h * 0.17f
    val span = h - endY
    val gold = EagleColors.Gold
    val unit = 1.dp.toPx()

    // 0 at the converging top, 1 at the broad base.
    fun spread(y: Float): Float = ((y - endY) / span).coerceIn(0f, 1f).pow(1.6f)
    fun streamX(u: Float, y: Float): Float = cx + u * w * 0.46f * spread(y)
    fun streamU(i: Int): Float = i / (StreamCount - 1f) * 2f - 1f
    fun particleT(i: Int, k: Int): Float = (phase * 3f + k / ParticlesPerStream.toFloat() + i * 0.173f) % 1f

    // Faint rails.
    val rail = Path()
    val segments = 48
    for (i in 0 until StreamCount) {
        val u = streamU(i)
        rail.reset()
        for (s in 0..segments) {
            val y = h - span * (s / segments.toFloat())
            val x = streamX(u, y)
            if (s == 0) rail.moveTo(x, y) else rail.lineTo(x, y)
        }
        drawPath(rail, Color(0x14FFFFFF), style = Stroke(width = unit))
    }

    // Tiers light up as pesos cross them.
    val glow = FloatArray(TierCount)
    for (i in 0 until StreamCount) {
        for (k in 0 until ParticlesPerStream) {
            val y = h - span * particleT(i, k)
            for (j in 0 until TierCount) {
                val d = (y - h * tierFraction(j)) / (h * 0.03f)
                glow[j] += exp(-d * d)
            }
        }
    }
    for (j in 0 until TierCount) {
        val y = h * tierFraction(j)
        val half = w * 0.46f * spread(y) + w * 0.02f
        val a = 0.10f + 0.65f * (glow[j] / 2.5f).coerceIn(0f, 1f)
        drawLine(gold.copy(alpha = a), Offset(cx - half, y), Offset(cx + half, y), strokeWidth = unit)
        drawCircle(gold.copy(alpha = a), radius = 2.dp.toPx(), center = Offset(cx - half, y))
        drawCircle(gold.copy(alpha = a), radius = 2.dp.toPx(), center = Offset(cx + half, y))
    }

    // Pesos.
    for (i in 0 until StreamCount) {
        val u = streamU(i)
        for (k in 0 until ParticlesPerStream) {
            val t = particleT(i, k)
            val y = h - span * t
            val x = streamX(u, y)
            val fade = sin(PI.toFloat() * t)
            val color = if ((i + k) % 5 == 0) Color.White else gold
            val r = (1.4f + 2.0f * (1f - t)) * unit
            drawCircle(color.copy(alpha = 0.14f * fade), radius = r * 3.2f, center = Offset(x, y))
            drawCircle(color.copy(alpha = 0.2f + 0.8f * fade), radius = r, center = Offset(x, y))
        }
    }

    // The watching eye where every trail ends.
    val eye = Offset(cx, eyeY)
    val ring = 10.dp.toPx()
    val pulse = (phase * 6f) % 1f
    drawCircle(gold.copy(alpha = 0.55f), radius = ring, center = eye, style = Stroke(width = unit))
    drawCircle(
        gold.copy(alpha = 0.45f * (1f - pulse)),
        radius = ring * (1f + 1.8f * pulse),
        center = eye,
        style = Stroke(width = unit),
    )
    drawCircle(gold, radius = 3.5.dp.toPx(), center = eye)
}
