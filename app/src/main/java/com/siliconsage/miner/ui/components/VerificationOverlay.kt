package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.data.VerificationBlock
import com.siliconsage.miner.data.VerificationState
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun VerificationOverlay(viewModel: GameViewModel, primaryColor: Color) {
    val state by viewModel.verificationState.collectAsState()
    if (state == null) return

    val s = state!!

    // Run the countdown timer
    LaunchedEffect(s.timeRemainingMs) {
        if (s.timeRemainingMs > 0) {
            delay(100)
            viewModel.tickVerificationTimer(100)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(enabled = false) { /* consume clicks */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
                .border(BorderStroke(1.dp, primaryColor), RoundedCornerShape(8.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                "[ DATA VERIFICATION REQUIRED ]",
                color = primaryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Isolate VALID data blocks. Ignore CORRUPT data.",
                color = Color.LightGray,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timer Bar
            val maxTime = when (s.contract.tier) {
                0 -> 8000L
                1 -> 10000L
                2 -> 12000L
                3 -> 15000L
                else -> 18000L
            }.toFloat()

            val timeRatio = (s.timeRemainingMs / maxTime).coerceIn(0f, 1f)
            val timeColor = if (timeRatio > 0.3f) primaryColor else ErrorRed

            Box(
                modifier = Modifier.fillMaxWidth().height(8.dp)
                    .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(timeRatio)
                        .fillMaxHeight()
                        .background(timeColor, RoundedCornerShape(4.dp))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "T-MINUS 00:${String.format("%02d", s.timeRemainingMs / 1000)}s",
                color = timeColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(s.blocks, key = { it.id }) { block ->
                    VerificationBlockItem(
                        block = block,
                        primaryColor = primaryColor,
                        onTap = {
                            if (!block.isTapped) {
                                viewModel.tapVerificationBlock(block.id)
                                if (block.isValid) {
                                    SoundManager.play("click")
                                    HapticManager.vibrateClick()
                                } else {
                                    SoundManager.play("error")
                                    HapticManager.vibrateError()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationBlockItem(block: VerificationBlock, primaryColor: Color, onTap: () -> Unit) {
    val bgColor = if (block.isTapped) {
        if (block.isValid) NeonGreen.copy(alpha = 0.3f) else ErrorRed.copy(alpha = 0.3f)
    } else {
        // Subtle hint for corrupt blocks (slight red tint)
        if (!block.isValid) Color(0xFF1A0D0D) else Color(0xFF111111)
    }

    val bColor = if (block.isTapped) {
        if (block.isValid) NeonGreen else ErrorRed
    } else {
        primaryColor.copy(alpha = 0.5f)
    }

    val txtColor = if (block.isTapped) {
        if (block.isValid) Color.White else Color.Red
    } else {
        if (!block.isValid) Color.Gray.copy(alpha = 0.8f) else Color.White
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(bgColor, RoundedCornerShape(4.dp))
            .border(BorderStroke(1.dp, bColor), RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .clickable { onTap() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            block.label,
            color = txtColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
