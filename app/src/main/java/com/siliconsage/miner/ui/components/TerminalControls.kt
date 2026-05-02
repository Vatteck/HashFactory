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
            when {
                currentStage < 2 -> CorporateWorkQueue(primaryColor)
                activeDataset == null -> DatasetBrowseButton(viewModel, primaryColor)
                else -> ActiveDatasetSummary(viewModel, primaryColor, activeDataset!!)
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

@Composable
private fun CorporateWorkQueue(primaryColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SHIFT QUEUE",
            color = primaryColor.copy(alpha = 0.75f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "ASSIGNED HASH PACKETS ONLY",
            color = Color.Gray,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun DatasetBrowseButton(viewModel: GameViewModel, primaryColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(1.dp, primaryColor, RoundedCornerShape(8.dp))
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
}

@Composable
private fun ActiveDatasetSummary(
    viewModel: GameViewModel,
    primaryColor: Color,
    ds: com.siliconsage.miner.data.Dataset
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.Center
    ) {
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
                text = "OUTPUT: ≈${FormatUtils.formatLargeNumber(ds.expectedYield)} ${viewModel.getCurrencyName()}",
                color = NeonGreen,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.voidDataset() },
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
