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
    
    LaunchedEffect(Unit) {
        viewModel.terminalNotification.collect {
            notification.value = it
            delay(3000) // Show for 3 seconds
            notification.value = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = notification.value != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Black.copy(alpha = 0.9f))
                    .border(1.dp, ElectricBlue.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = notification.value ?: "",
                    color = ElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
