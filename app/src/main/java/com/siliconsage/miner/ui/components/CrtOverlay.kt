package com.siliconsage.miner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun CrtOverlay(
    modifier: Modifier = Modifier,
    scanlineAlpha: Float = 0.15f,
    vignetteAlpha: Float = 0.5f,
    color: Color = Color.White,
    corruption: Double = 0.0, // v3.9.70: Phase 17 Dynamic CRT Curve
    glitchIntensity: Float = 0f // v3.13.33: Compute High chroma aberration
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Scanlines
        val lineHeight = 1f
        val gap = 6f
        var y = 0f
        while (y < height) {
            drawLine(
                color = color.copy(alpha = scanlineAlpha),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = lineHeight
            )
            y += (lineHeight + gap)
        }

        // 2. Vignette
        val activeVignetteAlpha = (vignetteAlpha * (1.0 - corruption)).toFloat()
        val radius = kotlin.math.max(width, height)
        if (activeVignetteAlpha > 0.01f) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = activeVignetteAlpha * 0.5f),
                        Color.Black.copy(alpha = activeVignetteAlpha)
                    ),
                    center = center,
                    radius = radius * 0.85f
                ),
                size = size,
                blendMode = BlendMode.Darken
            )
        }

        // 3. Compute High — chroma aberration (RGB split overlay)
        // Fires when signal is clear and player is clicking hard
        if (glitchIntensity > 0.01f) {
            val shift = glitchIntensity * 18f
            // Red channel — shifted right
            drawRect(
                color = Color.Red.copy(alpha = glitchIntensity * 0.15f),
                topLeft = Offset(shift, 0f),
                size = Size(width, height),
                blendMode = BlendMode.Screen
            )
            // Cyan channel — shifted left (complement of red)
            drawRect(
                color = Color.Cyan.copy(alpha = glitchIntensity * 0.10f),
                topLeft = Offset(-shift * 0.6f, 0f),
                size = Size(width, height),
                blendMode = BlendMode.Screen
            )
            // Horizontal scanline burst at peak intensity
            if (glitchIntensity > 0.25f) {
                val burstY = (height * 0.3f) + (kotlin.math.sin(System.currentTimeMillis() / 80.0) * height * 0.1f).toFloat()
                drawLine(
                    color = Color.White.copy(alpha = (glitchIntensity - 0.25f) * 1.5f),
                    start = Offset(0f, burstY),
                    end = Offset(width, burstY),
                    strokeWidth = 1.5f
                )
            }
        }
    }
}
