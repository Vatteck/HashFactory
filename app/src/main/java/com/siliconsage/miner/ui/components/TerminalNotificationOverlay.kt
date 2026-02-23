package com.siliconsage.miner.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import com.siliconsage.miner.ui.theme.ElectricBlue

@Composable
fun TerminalNotificationOverlay(viewModel: GameViewModel) {
    val notification = remember { mutableStateOf<String?>(null) }
    var isGlitchingOut by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.terminalNotification.collect {
            isGlitchingOut = false
            notification.value = it
            delay(2800) // Show for 2.8 seconds
            isGlitchingOut = true // v3.9.70: Trigger Interactive Glitch out
            com.siliconsage.miner.util.SoundManager.play("error")
            delay(200) // Glitch duration
            notification.value = null
            isGlitchingOut = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = notification.value != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Black.copy(alpha = 0.9f))
                    .border(1.dp, ElectricBlue.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                    .graphicsLayer {
                        if (isGlitchingOut) {
                            translationX = (kotlin.random.Random.nextFloat() - 0.5f) * 30f
                            translationY = (kotlin.random.Random.nextFloat() - 0.5f) * 10f
                            alpha = 0.6f
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val textToRender = if (isGlitchingOut && notification.value != null) notification.value!!.map { if (kotlin.random.Random.nextFloat() > 0.6f) listOf('$', '%', '#', '@', '0', '1', '&', '*').random() else it }.joinToString("") else (notification.value ?: "")
                Text(
                    text = textToRender,
                    color = if (isGlitchingOut) com.siliconsage.miner.ui.theme.ConvergenceGold else ElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
