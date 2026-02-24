package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun TerminalHeader(viewModel: GameViewModel, color: Color) {
    val powerUsage by viewModel.activePowerUsage.collectAsState()
    val maxPower by viewModel.maxPowerkW.collectAsState()
    val currentHeat by viewModel.currentHeat.collectAsState()

    val isCritical = currentHeat > 90.0 || (powerUsage > maxPower * 0.9)
    val criticalTransition = rememberInfiniteTransition(label = "criticalVibration")
    val vibrationOffset by criticalTransition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(50, easing = LinearEasing), RepeatMode.Reverse),
        label = "hudVibration"
    )
    val vibrationState by animateFloatAsState(targetValue = if (isCritical) vibrationOffset else 0f, label = "hudVibrationBlend")

    HeaderSection(
        viewModel = viewModel,
        color = color,
        onToggleOverclock = { viewModel.toggleOverclock() },
        onPurge = { viewModel.purgeHeat() },
        onRepair = { viewModel.repairIntegrity() },
        modifier = Modifier.graphicsLayer { translationX = vibrationState }
    )
}
