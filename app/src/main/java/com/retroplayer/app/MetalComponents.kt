package com.retroplayer.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BrushedMetalPanel(skin: RetroSkin, modifier: Modifier = Modifier, cornerRadius: Dp = 12.dp, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.verticalGradient(listOf(skin.metalLight, skin.metalMid, skin.metalDark)))
            .border(1.5.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.6f), skin.metalBorder)), RoundedCornerShape(cornerRadius)),
        content = content
    )
}

@Composable
fun MetalTactileButton(skin: RetroSkin, onClick: () -> Unit, modifier: Modifier = Modifier, isActive: Boolean = false, content: @Composable BoxScope.() -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .shadow(if (isActive) 1.dp else 4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Brush.verticalGradient(listOf(skin.metalDark, skin.metalMid)) else Brush.verticalGradient(listOf(skin.metalLight, skin.metalMid, skin.metalDark)))
            .border(1.dp, if (isActive) skin.accent else skin.metalBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        content = content
    )
}

@Composable
fun RotaryMetalKnob(value: Float, onValueChange: (Float) -> Unit, skin: RetroSkin, label: String, modifier: Modifier = Modifier, size: Dp = 64.dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(skin.metalLight, skin.metalMid, skin.metalDark)))
                .border(2.dp, skin.metalBorder, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val center = Offset(this.size.width / 2f, this.size.height / 2f)
                        val touch = change.position
                        val angleRad = atan2(touch.y - center.y, touch.x - center.x)
                        var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                        if (angleDeg < 0f) angleDeg += 360f
                        onValueChange((angleDeg / 360f).coerceIn(0f, 1f))
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = this.size.minDimension / 2f
                val angle = (value * 270f - 225f) * (PI / 180.0)
                val markerStart = Offset(center.x + (radius * 0.35f * cos(angle)).toFloat(), center.y + (radius * 0.35f * sin(angle)).toFloat())
                val markerEnd = Offset(center.x + (radius * 0.80f * cos(angle)).toFloat(), center.y + (radius * 0.80f * sin(angle)).toFloat())
                drawLine(skin.accent, markerStart, markerEnd, strokeWidth = 6f, cap = StrokeCap.Round)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = skin.metalBorder)
    }
}

@Composable
fun RetroSpectrumVisualizer(isPlaying: Boolean, skin: RetroSkin, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val phase by infiniteTransition.animateFloat(0f, (2 * PI).toFloat(), infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart), label = "wavePhase")

    Canvas(modifier = modifier) {
        val barCount = 24
        val spacing = 4f
        val barWidth = (size.width - (barCount - 1) * spacing) / barCount
        for (i in 0 until barCount) {
            val amplitude = if (isPlaying) (((sin(phase + i * 0.45f) + cos(phase * 1.7f - i * 0.3f)) * 0.25f) + 0.55f).coerceIn(0.12f, 1.0f) else 0.08f
            val barHeight = size.height * amplitude
            val left = i * (barWidth + spacing)
            drawRect(skin.lcdDim, Offset(left, 0f), Size(barWidth, size.height))
            drawRect(if (amplitude > 0.82f) skin.accent else skin.lcdText, Offset(left, size.height - barHeight), Size(barWidth, barHeight))
        }
    }
}
