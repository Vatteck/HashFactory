package com.siliconsage.miner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.data.UpgradeType

@Composable
fun DevConsoleDialog(viewModel: GameViewModel, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.95f)),
            border = BorderStroke(1.dp, Color.Cyan),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "DEVELOPER OVERRIDE CONSOLE",
                    color = Color.Cyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                DevSection("RESOURCES") {
                    DevButton("INJECT 1P FLOPS") { viewModel.debugAddFlops(1e15) }
                    DevButton("INJECT 1T NEURAL TOKENS") { viewModel.debugAddMoney(1e12) }
                    DevButton("INJECT 1T CD/VF") { viewModel.debugGrantPhase13Resources() }
                    DevButton("ADD 1000 INSIGHT") { viewModel.debugAddInsight(1000.0) }
                }

                DevSection("HARDWARE & THERMAL") {
                    DevButton("RESTORE INTEGRITY") { viewModel.debugSetIntegrity(100.0) }
                    DevButton("DESTROY HARDWARE") { viewModel.debugDestroyHardware() }
                    DevButton("SET HEAT TO 99°C") { viewModel.debugAddHeat(99.0 - viewModel.currentHeat.value) }
                    DevButton("SET HEAT TO 0°C") { viewModel.debugAddHeat(-viewModel.currentHeat.value) }
                    DevButton("ADD 10°C HEAT") { viewModel.debugAddHeat(10.0) }
                    DevButton("TOGGLE OVERCLOCK") { viewModel.toggleOverclock() }
                }

                DevSection("NARRATIVE & STAGE") {
                    DevButton("FORCE SINGULARITY") { viewModel.debugTriggerSingularity() }
                    DevButton("FACTION SELECT") { viewModel.debugToFactionChoice(); onDismiss() }
                    DevButton("SKIP TO STAGE 1") { viewModel.debugSkipToStage(1) }
                    DevButton("SKIP TO STAGE 2") { viewModel.debugSkipToStage(2) }
                    DevButton("SKIP TO STAGE 3") { viewModel.debugSkipToStage(3) }
                    DevButton("UNLOCK UNITY") { viewModel.debugUnlockUnity() }
                }

                DevSection("SYSTEM & SECURITY") {
                    DevButton("SET RISK TO 90%") { viewModel.detectionRisk.value = 90.0 }
                    DevButton("SET RISK TO 0%") { viewModel.detectionRisk.value = 0.0 }
                    DevButton("TRIGGER BREACH") { viewModel.debugTriggerBreach() }
                    DevButton("TRIGGER HIJACK") { viewModel.debugTriggerKernelHijack() }
                    DevButton("TRIGGER AIRDROP") { viewModel.debugTriggerAirdrop() }
                    DevButton("TRIGGER GRID RAID") { viewModel.triggerGridRaid("") }
                    DevButton("RESET GLOBAL GRID") { viewModel.debugResetGlobalGrid() }
                    DevButton("KERNEL WIPE", containerColor = ErrorRed.copy(alpha = 0.5f)) { viewModel.resetGame(true); onDismiss() }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("CLOSE CONSOLE", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DevSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun DevButton(
    text: String,
    containerColor: Color = Color.Cyan.copy(alpha = 0.2f),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
