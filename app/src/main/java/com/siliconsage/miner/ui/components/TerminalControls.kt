package com.siliconsage.miner.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.FormatUtils
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun TerminalControls(viewModel: GameViewModel, primaryColor: Color) {
    val activeDataset by viewModel.activeDataset.collectAsState()
    val integrity by viewModel.hardwareIntegrity.collectAsState()
    val currentStage by viewModel.storyStage.collectAsState()

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                if (activeDataset == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .border(1.dp, primaryColor, RoundedCornerShape(4.dp))
                            .clickable {
                                viewModel.toggleDatasetPicker()
                                SoundManager.play("buy")
                                HapticManager.vibrateClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BROWSE DATASETS",
                            color = primaryColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    val ds = activeDataset!!
                    Text(
                        text = "ACTIVE: ${ds.name}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PURITY: ${(ds.purity * 100).toInt()}%",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "YIELD: ≈${FormatUtils.formatLargeNumber(ds.expectedYield)} NT",
                            color = NeonGreen,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.voidDataset() }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "[ VOID DATASET ]",
                            color = Color.Red.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
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
