package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun MiniTerminalInlay(viewModel: GameViewModel, onClick: () -> Unit) {
    val logs by viewModel.logs.collectAsState()
    val corruption by viewModel.identityCorruption.collectAsState()
    val isRaid by viewModel.isRaidActive.collectAsState()
    val lastThree = logs.takeLast(2)
    
    // v3.3.7: Slower ticker animation
    val displayedLogs = remember { mutableStateListOf<String>() }
    
    LaunchedEffect(lastThree) {
        if (lastThree.isNotEmpty()) {
            delay(200) // Debounce rapid log updates
            displayedLogs.clear()
            lastThree.forEach { displayedLogs.add(it.message) }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Glitch")
    val jitterAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(50, easing = LinearEasing), RepeatMode.Reverse),
        label = "Alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onClick() }
            .graphicsLayer {
                if (isRaid || corruption > 0.5) {
                    translationX = (Random.nextFloat() - 0.5f) * (corruption.toFloat() * 15f)
                    translationY = (Random.nextFloat() - 0.5f) * (corruption.toFloat() * 5f)
                }
            }
    ) {
        // Scanline
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
        
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.Center
        ) {
            displayedLogs.forEach { msg ->
                Text(
                    text = msg,
                    color = if (isRaid) Color(0xFFFF0033).copy(alpha = jitterAlpha) else Color(0xFF39FF14).copy(alpha = jitterAlpha),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            if (displayedLogs.isEmpty()) {
                Text(
                    "≪ SYSTEM_IDLE // MONITORING_UPLINK ≫",
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
