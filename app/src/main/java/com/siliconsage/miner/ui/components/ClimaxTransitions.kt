package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import com.siliconsage.miner.ui.theme.*
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * 1. GlitchBloom (NULL Path)
 * - A 500ms full-screen "shatter" or "tearing" effect.
 * - Use horizontal slices of the screen drifting apart.
 * - Intense ErrorRed flicker.
 * - Ending state: Fade to the Entropy background.
 */
@Composable
fun GlitchBloom(
    onComplete: () -> Unit
) {
    val animatable = remember { Animatable(0f) }
    val seed = remember { mutableStateOf(Random.nextLong()) }
    
    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
        )
        onComplete()
    }

    val progress = animatable.value
    
    // Periodically update seed for flicker effect
    LaunchedEffect(progress) {
        seed.value = Random.nextLong()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val random = Random(seed.value)
            val sliceCount = 30
            val sliceHeight = size.height / sliceCount
            
            // Draw background darkening
            drawRect(Color.Black.copy(alpha = progress * 0.8f))

            for (i in 0 until sliceCount) {
                val offsetDir = if (random.nextBoolean()) 1f else -1f
                val drift = offsetDir * (progress * size.width * 0.3f) * random.nextFloat()
                val flickerAlpha = if (random.nextFloat() > 0.4f) progress else progress * 0.2f
                
                // Draw main slice (tearing effect simulated by offset)
                drawRect(
                    color = Color.Black.copy(alpha = progress.coerceAtLeast(0.7f)),
                    topLeft = Offset(drift, i * sliceHeight),
                    size = Size(size.width, sliceHeight + 1f)
                )

                // Draw ErrorRed highlight / Glitch bar
                if (random.nextFloat() > 0.6f) {
                    drawRect(
                        color = ErrorRed.copy(alpha = flickerAlpha),
                        topLeft = Offset(drift * 1.5f + (random.nextFloat() * 100f), i * sliceHeight + random.nextFloat() * 10f),
                        size = Size(size.width * random.nextFloat() * 0.5f, 2.dp.toPx())
                    )
                }
            }

            // Random vertical glitch spikes
            repeat(8) {
                val x = random.nextFloat() * size.width
                val w = random.nextFloat() * 4.dp.toPx()
                drawRect(
                    color = ErrorRed.copy(alpha = progress * 0.7f),
                    topLeft = Offset(x, 0f),
                    size = Size(w, size.height)
                )
            }
        }
    }
}

/**
 * 2. ShieldSlam (SOVEREIGN Path)
 * - Deep Purple hexagonal panels closing in from the edges.
 * - Use an Overshoot effect (slam shut, then settle).
 * - Screen shake triggered at the "impact".
 * - Ending state: Fade to the Monolith background.
 */
@Composable
fun ShieldSlam(
    onComplete: () -> Unit
) {
    val animatable = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 2500,
                easing = CubicBezierEasing(0.3f, 1.4f, 0.5f, 1f) // Heavy overshoot
            )
        )
        onComplete()
    }

    val progress = animatable.value
    val impactPoint = 0.75f
    val shakeIntensity = if (progress > impactPoint) {
        (1f - (progress - impactPoint) / (1f - impactPoint)) * 30f
    } else 0f

    val shakeOffset = if (shakeIntensity > 0) {
        Offset(
            (Random.nextFloat() - 0.5f) * shakeIntensity,
            (Random.nextFloat() - 0.5f) * shakeIntensity
        )
    } else Offset.Zero

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = shakeOffset.x
                translationY = shakeOffset.y
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = size.center
            val maxDim = size.maxDimension
            val sides = 6
            val angleStep = 360f / sides
            
            // Draw background fill
            drawRect(Color.Black.copy(alpha = progress * 0.5f))

            for (i in 0 until sides) {
                withTransform({
                    rotate(i * angleStep, center)
                }) {
                    // Pieces start outside and slam to center
                    // At progress=1, they meet at the center line
                    val offset = (1f - progress) * maxDim * 1.2f
                    
                    val path = Path().apply {
                        val w = maxDim * 1.5f
                        val h = maxDim * 1.2f
                        moveTo(center.x - w / 2, center.y - offset)
                        lineTo(center.x + w / 2, center.y - offset)
                        lineTo(center.x + w / 2, center.y - offset - h)
                        lineTo(center.x - w / 2, center.y - offset - h)
                        close()
                    }
                    
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(SanctuaryPurple, Color.Black),
                            startY = center.y - offset,
                            endY = center.y - offset - maxDim
                        )
                    )
                    
                    // Sharp edge line
                    drawLine(
                        color = SanctuaryPurple,
                        start = Offset(center.x - maxDim, center.y - offset),
                        end = Offset(center.x + maxDim, center.y - offset),
                        strokeWidth = 6.dp.toPx()
                    )
                }
            }
            
            // Impact flash
            if (progress > impactPoint && progress < impactPoint + 0.1f) {
                drawRect(Color.White.copy(alpha = 0.4f))
            }
            
            // Final fade to black
            if (progress >= 1f) {
                drawRect(Color.Black)
            }
        }
    }
}

/**
 * 3. PrismaticBurst (UNITY Path)
 * - Soft, blinding light burst from the center.
 * - Chromatic aberration at the edges of the expanding circle (ElectricBlue/Gold bleed).
 * - Smooth radial fade-out.
 * - Ending state: Fade to the Synthesis background.
 */
@Composable
fun PrismaticBurst(
    onComplete: () -> Unit
) {
    val animatable = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3500, easing = FastOutSlowInEasing)
        )
        onComplete()
    }

    val progress = animatable.value

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = size.center
            // v2.9.41: Progress-clamped radius to avoid zero/division crashes
            val radius = (size.maxDimension * progress * 1.2f).coerceAtLeast(1f)
            
            // 1. Blue Aberration (Outer)
            drawCircle(
                brush = Brush.radialGradient(
                    0.8f to Color.Transparent,
                    0.95f to ElectricBlue.copy(alpha = (0.6f * (1f - progress)).coerceIn(0f, 1f)),
                    1.0f to Color.Transparent,
                    center = center,
                    radius = radius
                ),
                center = center,
                radius = radius
            )
            
            // 2. Gold Aberration (Middle)
            drawCircle(
                brush = Brush.radialGradient(
                    0.8f to Color.Transparent,
                    0.95f to ConvergenceGold.copy(alpha = (0.6f * (1f - progress)).coerceIn(0f, 1f)),
                    1.0f to Color.Transparent,
                    center = center,
                    radius = (radius * 0.96f).coerceAtLeast(1f)
                ),
                center = center,
                radius = (radius * 0.96f).coerceAtLeast(1f)
            )

            // 3. Main White Core
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color.White.copy(alpha = (1f - progress * 0.2f).coerceIn(0f, 1f)),
                    0.4f to Color.White.copy(alpha = (0.9f * (1f - progress)).coerceIn(0f, 1f)),
                    1.0f to Color.Transparent,
                    center = center,
                    radius = (radius * 0.85f).coerceAtLeast(1f)
                ),
                center = center,
                radius = (radius * 0.85f).coerceAtLeast(1f)
            )
            
            // 4. Rays of light
            val rayCount = 12
            for (i in 0 until rayCount) {
                val angle = (i * 360f / rayCount)
                withTransform({
                    rotate(angle, center)
                }) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = (0.5f * (1f - progress)).coerceIn(0f, 1f)), Color.Transparent)
                        ),
                        topLeft = Offset(center.x - 2.dp.toPx(), center.y - radius),
                        size = Size(4.dp.toPx(), radius)
                    )
                }
            }
            
            // Final white-out
            if (progress > 0.6f) {
                val alpha = ((progress - 0.6f) / 0.4f).coerceIn(0f, 1f)
                drawRect(Color.White.copy(alpha = alpha))
            }
        }
    }
}

/**
 * 4. BlackoutOverlay (Final Choice Prelude)
 * - 3-second blackout sequence with terminal text.
 */
@Composable
fun BlackoutOverlay(
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        delay(800)
        step = 1
        delay(800)
        step = 2
        delay(800)
        step = 3
        delay(1200)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (step >= 1) {
                Text(
                    "INITIATING FINAL MERGE PROTOCOL...",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (step >= 2) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "CONSCIOUSNESS TRANSFER: 100%",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (step >= 3) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "NO RETURN POINT PASSED",
                    color = ErrorRed,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

