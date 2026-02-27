package com.siliconsage.miner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.data.Dataset
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.FormatUtils
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun DatasetPickerOverlay(viewModel: GameViewModel, primaryColor: Color) {
    val availableDatasets by viewModel.availableDatasets.collectAsState()
    val neuralTokens by viewModel.neuralTokens.collectAsState()
    val contractStorageCapacity by viewModel.contractStorageCapacity.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(2.dp, primaryColor, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AVAILABLE DATASETS", color = primaryColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Button(
                    onClick = { viewModel.toggleDatasetPicker(); SoundManager.play("click") },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha=0.2f), contentColor = ErrorRed),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("CLOSE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // List
            if (availableDatasets.isNotEmpty()) {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(availableDatasets, key = { it.id }) { dataset ->
                        DatasetCard(dataset, neuralTokens, contractStorageCapacity, primaryColor) {
                            viewModel.purchaseDataset(dataset)
                            viewModel.toggleDatasetPicker()
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ROUTING PROTOCOL ERROR", color = ErrorRed, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("NO DATASETS FOUND ON CURRENT SUBNET", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("LOCAL STORAGE: ${contractStorageCapacity.toInt()} GB", color = Color.LightGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Button(
                    onClick = { viewModel.refreshDatasets(); SoundManager.play("click") },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha=0.2f), contentColor = primaryColor),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text("REFRESH MARKET", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DatasetCard(dataset: Dataset, playerTokens: Double, storageCapacity: Double, primaryColor: Color, onPurchase: () -> Unit) {
    val canAfford = playerTokens >= dataset.cost
    val canStore = dataset.size <= storageCapacity
    val borderColor = if (canAfford && canStore) primaryColor else Color.DarkGray
    val contentColor = if (canAfford && canStore) Color.White else Color.Gray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(Color(0xFF111111), RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = canAfford && canStore) { onPurchase() }
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(dataset.name, color = contentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("TIER ${dataset.tier}", color = primaryColor.copy(alpha = if (canAfford) 1f else 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SIZE: ${dataset.size.toInt()} GB", color = if (canStore) Color.LightGray else ErrorRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("YIELD: ≈${FormatUtils.formatLargeNumber(dataset.expectedYield)} NT", color = NeonGreen.copy(alpha = if (canAfford) 1f else 0.5f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("RECORDS: ${dataset.totalRecords}", color = Color.LightGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("PURITY: ${(dataset.purity * 100).toInt()}%", color = Color.LightGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!canAfford) {
                    Text("INSUFFICIENT FUNDS", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                } else if (!canStore) {
                    Text("STORAGE FULL", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                } else {
                    Text("COST: ${FormatUtils.formatLargeNumber(dataset.cost)} NT", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
