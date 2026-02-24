package com.siliconsage.miner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun TerminalControls(viewModel: GameViewModel, primaryColor: Color) {
    val conversionRate by viewModel.conversionRate.collectAsState()
    val integrity by viewModel.hardwareIntegrity.collectAsState()
    val currentStage by viewModel.storyStage.collectAsState()
    val voltage by viewModel.activePowerUsage.collectAsState()
    val corruption by viewModel.identityCorruption.collectAsState() // v3.10.1

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f)) {
            // v3.5.4: Removed coffee color leak from Sell button entirely.
            // The gaslight is reserved for Overclock/Purge only.
            ExchangeSection(
                rate = conversionRate,
                color = primaryColor,
                unitName = viewModel.getComputeUnitName(),
                currencyName = viewModel.getCurrencyName(),
                corruption = corruption,     // v3.10.1
                storyStage = currentStage,   // v3.10.1
                onExchange = {
                    viewModel.exchangeFlops()
                    SoundManager.play("buy")
                    HapticManager.vibrateClick()
                }
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            RepairSection(
                integrity = integrity,
                cost = viewModel.calculateRepairCost(),
                color = primaryColor,
                storyStage = currentStage,
                currencyName = viewModel.getCurrencyName(),
                onRepair = { viewModel.repairIntegrity(); HapticManager.vibrateClick() }
            )
        }
    }
}
