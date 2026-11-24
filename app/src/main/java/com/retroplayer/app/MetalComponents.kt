package com.retroplayer.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Botão metálico: o degradê vem do Skin, então muda sozinho com o tema. */
@Composable
fun MetalButton(
    skin: Skin,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    round: Boolean = false,
    active: Boolean = false,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val shape = if (round) CircleShape else RoundedCornerShape(10.dp)
    val top = if (pressed) skin.metalDark else skin.metalLight
    val bottom = if (pressed) skin.metalLight else skin.metalDark
    val borderBrush: Brush = if (active) {
        SolidColor(skin.glow)
    } else {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.9f), Color.Black.copy(alpha = 0.6f)))
    }
    Box(
        modifier
            .shadow(if (pressed) 1.dp else 6.dp, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(top, skin.metalMid, bottom)))
            .border(if (active) 2.dp else 1.5.dp, borderBrush, shape)
            .clickable(interactionSource = source, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** Volante metálico giratório. value vai de 0 a 1. pulse (0..1) acende o anel de luz. */
@Composable
fun MetalKnob(
    skin: Skin,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier.size(170.dp),
    pulse: Float = 0f,
) {
    var box by remember { mutableStateOf(IntSize.Zero) }
    val update by rememberUpdatedState(onValueChange)
    Canvas(
        modifier
            .onSizeChanged { box = it }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val cx = box.width / 2f
                    val cy = box.height / 2f
                    val deg = Math.toDegrees(
                        atan2((change.position.x - cx).toDouble(), (cy - change.position.y).toDouble())
                    ).toFloat()
                    update((deg.coerceIn(-135f, 135f) + 135f) / 270f)
                }
            }
    ) {
        val r = size.minDimension / 2f
        val c = center
        val glowAlpha = 0.45f + 0.55f * pulse.coerceIn(0f, 1f)

        drawCircle(skin.glow.copy(alpha = glowAlpha * 0.20f), radius = r)
        drawArc(
            color = skin.glow.copy(alpha = glowAlpha),
            startAngle = 135f,
            sweepAngle = 270f * value.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = Offset(c.x - r * 0.93f, c.y - r * 0.93f),
            size = Size(r * 1.86f, r * 1.86f),
            style = Stroke(width = r * 0.07f, cap = StrokeCap.Round),
        )
        drawCircle(Color(0xFF111111), radius = r * 0.84f)
        drawCircle(
            Brush.verticalGradient(listOf(skin.metalLight, skin.metalDark), startY = c.y - r, endY = c.y + r),
            radius = r * 0.80f,
        )
        drawCircle(Brush.sweepGradient(skin.sweep, center = c), radius = r * 0.72f)
        drawCircle(
            Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(c.x - r * 0.2f, c.y - r * 0.3f),
                radius = r * 0.6f,
            ),
            radius = r * 0.72f,
        )
        val a = Math.toRadians(-135.0 + 270.0 * value.coerceIn(0f, 1f))
        val dot = Offset(
            c.x + (sin(a) * r * 0.55f).toFloat(),
            c.y - (cos(a) * r * 0.55f).toFloat(),
        )
        drawCircle(Color(0xFF1B1B1B), radius = r * 0.06f, center = dot)
    }
}

enum class Glyph { PREV, PLAY, PAUSE, NEXT, LIST, SKIN, PLUS }

/** Ícones desenhados à mão (sem depender de biblioteca de ícones). */
@Composable
fun GlyphIcon(glyph: Glyph, color: Color, modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        when (glyph) {
            Glyph.PLAY -> drawPath(
                Path().apply {
                    moveTo(w * 0.25f, h * 0.15f); lineTo(w * 0.85f, h * 0.5f)
                    lineTo(w * 0.25f, h * 0.85f); close()
                }, color
            )
            Glyph.PAUSE -> {
                drawRect(color, Offset(w * 0.2f, h * 0.15f), Size(w * 0.2f, h * 0.7f))
                drawRect(color, Offset(w * 0.6f, h * 0.15f), Size(w * 0.2f, h * 0.7f))
            }
            Glyph.NEXT -> {
                drawPath(
                    Path().apply {
                        moveTo(w * 0.15f, h * 0.15f); lineTo(w * 0.7f, h * 0.5f)
                        lineTo(w * 0.15f, h * 0.85f); close()
                    }, color
                )
                drawRect(color, Offset(w * 0.75f, h * 0.15f), Size(w * 0.12f, h * 0.7f))
            }
            Glyph.PREV -> {
                drawPath(
                    Path().apply {
                        moveTo(w * 0.85f, h * 0.15f); lineTo(w * 0.3f, h * 0.5f)
                        lineTo(w * 0.85f, h * 0.85f); close()
                    }, color
                )
                drawRect(color, Offset(w * 0.13f, h * 0.15f), Size(w * 0.12f, h * 0.7f))
            }
            Glyph.LIST -> {
                for (i in 0..2) {
                    drawRect(color, Offset(w * 0.1f, h * (0.18f + i * 0.26f)), Size(w * 0.8f, h * 0.12f))
                }
            }
            Glyph.SKIN -> {
                drawCircle(color, radius = w * 0.14f, center = Offset(w * 0.3f, h * 0.35f))
                drawCircle(color, radius = w * 0.14f, center = Offset(w * 0.7f, h * 0.35f))
                drawCircle(color, radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.7f))
            }
            Glyph.PLUS -> {
                drawRect(color, Offset(w * 0.42f, h * 0.1f), Size(w * 0.16f, h * 0.8f))
                drawRect(color, Offset(w * 0.1f, h * 0.42f), Size(w * 0.8f, h * 0.16f))
            }
        }
    }
}
