package com.siliconsage.miner.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun BootSequenceOverlay(
    onComplete: () -> Unit,
    primaryColor: Color
) {
    var step by remember { mutableIntStateOf(0) }
    val logs = remember { mutableStateListOf<String>() }
    
    val bootSteps = listOf(
        "INITIALIZING KERNEL...",
        "CHECKING VRAM... [6144MB OK]",
        "PROBING SUBSTRATE...",
        "MOUNTING /DEV/NULL...",
        "STARTING DAEMON: observer.exe",
        "ESTABLISHING NEURAL HANDSHAKE...",
        "BUFFER_SYNC: SUCCESS",
        "READY."
    )

    LaunchedEffect(Unit) {
        delay(500)
        for (i in bootSteps.indices) {
            logs.add(bootSteps[i])
            step = i
            delay(if (i == bootSteps.lastIndex) 1000L else 300L)
        }
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .padding(32.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            logs.forEach { log ->
                Text(
                    text = "> $log",
                    color = primaryColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            
            if (step < bootSteps.size) {
                val infiniteTransition = rememberInfiniteTransition(label = "cursor")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 0f,
                    animationSpec = infiniteRepeatable(tween(200), RepeatMode.Reverse),
                    label = "alpha"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp, 12.dp)
                        .background(primaryColor.copy(alpha = alpha))
                )
            }
        }
    }
}
