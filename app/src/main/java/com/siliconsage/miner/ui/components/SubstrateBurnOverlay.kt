package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.siliconsage.miner.ui.theme.ElectricBlue
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
fun SubstrateBurnOverlay(primaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "burn_shake")
    val shake by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(40, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    val timeMillis = System.currentTimeMillis()
    
    // v3.9.70: Phase 17 Migration VFX
    val fadeOut = animateFloatAsState(targetValue = 1f, animationSpec = tween(2000), label = "fade").value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.2f * fadeOut)) // Initial blinding flash
            .graphicsLayer {
                translationX = shake * fadeOut
                translationY = (Random.nextFloat() - 0.5f) * 10f * fadeOut
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val particleCount = 200
            
            val speed = (timeMillis % 1000000L).toFloat() / 10f
            
            for (i in 0 until particleCount) {
                val seed = i * 137.5f // Golden angle distribution
                val px = (seed * 11f + speed * (1f + (i % 5))) % w
                val py = (seed * 7f + speed * 3f) % h
                
                val pAlpha = (Random.nextFloat() * 0.5f + 0.5f) * fadeOut
                val pColor = if (i % 3 == 0) Color.White else primaryColor
                
                drawLine(
                    color = pColor.copy(alpha = pAlpha),
                    start = Offset(px, py),
                    end = Offset(px, py + Random.nextFloat() * 100f),
                    strokeWidth = (Random.nextFloat() * 4f + 1f)
                )
            }
        }
        
        // Center Burst
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val burstRadius = (timeMillis % 2500L).toFloat() / 2500f * size.width * 1.5f
            val burstAlpha = (1f - (timeMillis % 2500L).toFloat() / 2500f) * fadeOut
            
            drawCircle(
                color = ElectricBlue.copy(alpha = burstAlpha * 0.3f),
                radius = burstRadius,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = burstAlpha * 0.8f),
                radius = burstRadius * 0.8f,
                center = center
            )
        }
    }
}
