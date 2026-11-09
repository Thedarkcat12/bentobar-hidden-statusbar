package com.meridian11.books.bentobar.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A progress track in Material 3's wavy style: the part done is a travelling wave with round ends, the rest a
 * straight line a small gap after it. With [wavy] off, or while nothing [playing] (the wave only runs while the music
 * does), it is the plain track (both parts straight). The wave stands still with "No animations" or the system's Remove
 * animations. With [thumb] a small upright bar marks the end of the part done, with a gap on each side, as the pop-ups'
 * sliders have. The track's height is the caller's: [thickness] plus twice [amplitude] fits.
 */
@Composable
fun WavyTrack(
    fraction: Float, active: Color, inactive: Color, modifier: Modifier = Modifier,
    wavy: Boolean = true, thickness: Dp = 4.dp, amplitude: Dp = 2.dp, wavelength: Dp = 16.dp,
    thumb: Boolean = false, thumbColor: Color = active, thumbHeight: Dp = 16.dp, playing: Boolean = true,
) {
    val wave = wavy && playing
    val moving = wave && !Motion.off
    // Standing still, nothing runs: no frame is asked for.
    val phase: State<Float> = if (moving) rememberInfiniteTransition(label = "wavy").animateFloat(
        0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing)), label = "phase") else remember { mutableStateOf(0f) }
    Canvas(modifier) {
        val t = thickness.toPx()
        val mid = size.height / 2
        val f = fraction.coerceIn(0f, 1f)
        val tw = 4.dp.toPx()
        // With a thumb, it stands on the end of what is done, and neither the done part nor the rest is drawn within 2 dp of it.
        val x = if (thumb) (size.width * f).coerceIn(tw / 2, size.width - tw / 2) else (size.width * f).coerceAtLeast(t / 2)
        val near = if (thumb) tw / 2 + 2.dp.toPx() else t
        val doneTo = if (thumb) x - near else x
        // What is left of the track: a straight line with a round end, starting a small gap after the done part.
        val restFrom = x + near
        if (restFrom < size.width - t / 2) drawLine(inactive, Offset(restFrom, mid), Offset(size.width - t / 2, mid), strokeWidth = t, cap = StrokeCap.Round)
        if (thumb) drawRoundRect(thumbColor, Offset(x - tw / 2, mid - thumbHeight.toPx() / 2), androidx.compose.ui.geometry.Size(tw, thumbHeight.toPx()),
            androidx.compose.ui.geometry.CornerRadius(tw / 2))
        if (doneTo < t / 2) return@Canvas
        if (!wave || f <= 0f) {
            if (f > 0f) drawLine(active, Offset(t / 2, mid), Offset(doneTo, mid), strokeWidth = t, cap = StrokeCap.Round)
            else if (!thumb) drawCircle(active, t / 2, Offset(t / 2, mid))
            return@Canvas
        }
        val a = amplitude.toPx()
        val length = wavelength.toPx()
        val shift = phase.value * 2f * Math.PI.toFloat()
        val path = Path()
        val step = 2.dp.toPx()
        var px = t / 2
        var first = true
        while (px <= doneTo) {
            val y = mid + a * kotlin.math.sin(2f * Math.PI.toFloat() * (px - t / 2) / length - shift)
            if (first) { path.moveTo(px, y); first = false } else path.lineTo(px, y)
            px += step
        }
        drawPath(path, active, style = Stroke(width = t, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
