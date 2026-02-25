package com.siliconsage.miner.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.siliconsage.miner.ui.components.TerminalControls
import com.siliconsage.miner.ui.components.TerminalHeader
import com.siliconsage.miner.ui.components.TerminalLogs
import com.siliconsage.miner.ui.components.TerminalTab
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun TerminalScreen(viewModel: GameViewModel, primaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursorBlink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "cursor"
    )
    val showCursor = cursorAlpha > 0.5f
    val showContractPicker by viewModel.showContractPicker.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.Transparent).padding(16.dp)
        ) {
            TerminalHeader(viewModel, primaryColor)

            Spacer(modifier = Modifier.height(4.dp))

            val mode by viewModel.activeTerminalMode.collectAsState()
            val hasDecision by viewModel.hasNewSubnetDecision.collectAsState()
            val hasChatter by viewModel.hasNewSubnetChatter.collectAsState()
            val hasIO by viewModel.hasNewIOMessage.collectAsState()
            val currentHeat by viewModel.currentHeat.collectAsState()
            val corruption by viewModel.identityCorruption.collectAsState()

            Row(
                modifier = Modifier.fillMaxWidth().offset(y = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TerminalTab(
                    label = "I/O",
                    active = mode == "IO",
                    hasFlash = hasIO,
                    color = primaryColor,
                    corruption = corruption,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setTerminalMode("IO") }
                )
                TerminalTab(
                    label = "SUBNET",
                    active = mode == "SUBNET",
                    hasFlash = hasChatter || hasDecision,
                    isDecision = hasDecision,
                    color = primaryColor,
                    corruption = corruption,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setTerminalMode("SUBNET") }
                )
            }

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .border(BorderStroke(1.5.dp, if (currentHeat > 90.0) ErrorRed else primaryColor.copy(alpha = 0.85f)), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            ) {
                TerminalLogs(viewModel, primaryColor, showCursor)
            }

            Spacer(modifier = Modifier.height(16.dp))
            TerminalControls(viewModel, primaryColor)
        }

        // v3.30.0: Contract Picker Overlay
        if (showContractPicker) {
            com.siliconsage.miner.ui.components.ContractPickerOverlay(viewModel, primaryColor)
        }
    }
}

