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
    val activeContract by viewModel.activeContract.collectAsState()
    val contractProgress by viewModel.contractProgress.collectAsState()
    val integrity by viewModel.hardwareIntegrity.collectAsState()
    val currentStage by viewModel.storyStage.collectAsState()

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f)) {
            val isAutoVerify by viewModel.isAutoVerifyEnabled.collectAsState()

            ContractSection(
                activeContract = activeContract,
                contractProgress = contractProgress,
                isAutoVerify = isAutoVerify,
                onToggleAutoVerify = { 
                    viewModel.isAutoVerifyEnabled.value = !viewModel.isAutoVerifyEnabled.value
                    SoundManager.play("click") 
                },
                storyStage = currentStage,
                color = primaryColor,
                currencyName = viewModel.getCurrencyName(),
                onBrowse = {
                    viewModel.toggleContractPicker()
                    SoundManager.play("buy")
                    HapticManager.vibrateClick()
                },
                onForgeContract = {
                    viewModel.forgeContract()
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
