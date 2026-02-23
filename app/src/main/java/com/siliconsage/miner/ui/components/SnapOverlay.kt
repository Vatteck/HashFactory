package com.siliconsage.miner.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ElectricBlue
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * SnapOverlay - Visual "Substrate Reboot" effect for stage transitions.
 */
@Composable
fun SnapOverlay(isActive: Boolean, onComplete: () -> Unit) {
    if (!isActive) return

    var showContent by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(100),
        label = "snapAlpha"
    )

    LaunchedEffect(isActive) {
        delay(50)
        showContent = true
        delay(1800)
        showContent = false
        delay(200)
        onComplete()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f * alphaAnim.value)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Static Burst (Visual Glitch)
            if (Random.nextFloat() > 0.3f) {
                Text(
                    text = "≪ RECALIBRATING SUBSTRATE ≫",
                    color = ElectricBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Progress Bar (Sweeping)
            Box(modifier = Modifier.width(200.dp).height(2.dp).background(Color.DarkGray)) {
                val progress = rememberInfiniteTransition().animateFloat(
                    0f, 1f, 
                    infiniteRepeatable(tween(1500, easing = LinearOutSlowInEasing)),
                    label = "sweep"
                )
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress.value).background(ElectricBlue))
            }
            
            Text(
                text = "NEURAL LINK OPTIMIZED",
                color = ElectricBlue.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
