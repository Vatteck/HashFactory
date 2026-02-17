package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * GlitchSurface v1.0
 * Provides a high-frequency visual "screen tear" and noise overlay.
 * Uses low-level draw calls for battery efficiency.
 */
@Composable
fun GlitchSurface(
    isGlitched: Boolean,
    intensity: Float = 0.5f,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlitchLoop")
    
    // Controlled jitter for the content
    val offsetThreshold = 0.98f
    var currentOffset by remember { mutableStateOf(Offset.Zero) }
    
    if (isGlitched) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(Random.nextLong(50, 500))
                if (Random.nextFloat() > (1f - intensity * 0.1f)) {
                    repeat(Random.nextInt(2, 5)) {
                        currentOffset = Offset(
                            x = (Random.nextFloat() - 0.5f) * 20f * intensity,
                            y = (Random.nextFloat() - 0.5f) * 5f * intensity
                        )
                        delay(30)
                    }
                    currentOffset = Offset.Zero
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = currentOffset.x
                    translationY = currentOffset.y
                    // Minimal alpha flicker
                    if (isGlitched && Random.nextFloat() > 0.95f) {
                        alpha = 0.9f
                    }
                }
        ) {
            content()
        }

        if (isGlitched) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Horizontal Scan-line "Tears"
                if (Random.nextFloat() > 0.90f) {
                    val tearY = Random.nextFloat() * h
                    val tearH = Random.nextFloat() * 20.dp.toPx()
                    clipRect(top = tearY, bottom = tearY + tearH) {
                        // This technically requires drawing the content again shifted,
                        // but for a lightweight overlay, we just draw a colored bar
                        drawRect(
                            color = Color.Red.copy(alpha = 0.15f * intensity),
                            topLeft = Offset(0f, tearY),
                            size = Size(w, tearH)
                        )
                    }
                }

                // 2. High-frequency digital noise blocks
                if (Random.nextFloat() > 0.95f) {
                    repeat(3) {
                        val bW = Random.nextFloat() * 100.dp.toPx()
                        val bH = Random.nextFloat() * 10.dp.toPx()
                        drawRect(
                            color = if (Random.nextBoolean()) Color.Cyan.copy(alpha = 0.2f) else Color.Magenta.copy(alpha = 0.2f),
                            topLeft = Offset(Random.nextFloat() * w, Random.nextFloat() * h),
                            size = Size(bW, bH)
                        )
                    }
                }
                
                // 3. Static Grain (Performance heavy if too many, keep it light)
                repeat(5) {
                    val pointX = Random.nextFloat() * w
                    val pointY = Random.nextFloat() * h
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = 1f,
                        center = Offset(pointX, pointY)
                    )
                }
            }
        }
    }
}
