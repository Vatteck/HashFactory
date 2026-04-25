package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.ConvergenceGold
import com.siliconsage.miner.util.FormatUtils

@Composable
fun SurveillanceVisualizer(viewModel: GameViewModel, primaryColor: Color) {
    val activeHarvesters by viewModel.activeHarvesters.collectAsState()
    val harvestBuffers by viewModel.harvestBuffers.collectAsState()
    val storageCapacity by viewModel.storageCapacity.collectAsState()
    val currentStorageUsed by viewModel.currentStorageUsed.collectAsState()

    val sectors = (1..12).toList()
    val isLeaking = currentStorageUsed >= storageCapacity * 0.9

    Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
        // Storage Bar
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .border(BorderStroke(1.dp, if (isLeaking) ErrorRed else Color.DarkGray), RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GLOBAL DATA STORAGE", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${FormatUtils.formatStorage(currentStorageUsed)} / ${FormatUtils.formatStorage(storageCapacity)}", color = if (isLeaking) ErrorRed else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                
                val fillRatio = (currentStorageUsed / storageCapacity).toFloat().coerceIn(0f, 1f)
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.DarkGray.copy(alpha=0.5f), RoundedCornerShape(2.dp))) {
                    Box(modifier = Modifier.fillMaxWidth(fillRatio).fillMaxHeight().background(if (isLeaking) ErrorRed else primaryColor, RoundedCornerShape(2.dp)))
                }
                if (isLeaking) {
                    Text("⚠️ CRITICAL STORAGE OVERFLOW EXPOSURE - PURGE DATA IMMEDIATELY ⚠️", color = ErrorRed, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sector Grid
        Text("ACTIVE HARVESTER NETWORKS", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            sectors.chunked(3).forEach { rowSectors ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowSectors.forEach { sectorId ->
                        val count = activeHarvesters[sectorId] ?: 0
                        val buffer = harvestBuffers[sectorId] ?: 0.0
                        
                        Box(
                            modifier = Modifier.weight(1f)
                                .background(if (count > 0) primaryColor.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(4.dp))
                                .border(BorderStroke(1.dp, if (count > 0) primaryColor.copy(alpha = 0.5f) else Color.DarkGray.copy(alpha = 0.5f)), RoundedCornerShape(4.dp))
                                .clickable {
                                    // Logic to buy harvester, costs Neural Tokens scaled by count
                                    val cost = 5000.0 * (count + 1)
                                    if (viewModel.neuralTokens.value >= cost) {
                                        viewModel.updateNeuralTokens(-cost)
                                        val newMap = activeHarvesters.toMutableMap()
                                        newMap[sectorId] = count + 1
                                        viewModel.activeHarvesters.value = newMap
                                        viewModel.addLogPublic("[SURVEILLANCE]: Harvester Unit deployed to Sector $sectorId.")
                                    } else {
                                        viewModel.addLogPublic("[ERROR]: INSUFFICIENT FUNDS (${FormatUtils.formatLargeNumber(cost)} NT REQUIRED).")
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("SEC $sectorId", color = if (count > 0) Color.White else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                val countStr = if (count > 0) "x$count" else "OFFLINE"
                                Text(countStr, color = if (count > 0) primaryColor else Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                val bufferPercent = ((buffer / 100.0) * 100).toInt().coerceAtMost(100)
                                Text("BUF: $bufferPercent%", color = Color.Gray, fontSize = 8.sp)
                                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.DarkGray)) {
                                    Box(modifier = Modifier.fillMaxWidth((bufferPercent / 100f)).fillMaxHeight().background(ElectricBlue))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
