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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun DatasetGrid(viewModel: GameViewModel, primaryColor: Color) {
    val activeDataset by viewModel.activeDataset.collectAsState()
    val activeNodes by viewModel.activeDatasetNodes.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "gridIdle")
    val idleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "idlePulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
            .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        if (activeDataset != null && activeNodes.isNotEmpty()) {
            val ds = activeDataset!!
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[ ${ds.name} ]",
                    color = primaryColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "${(ds.progress * 100).toInt()}%",
                    color = primaryColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            
            // Determine columns based on size
            val cols = when {
                ds.totalRecords <= 9 -> 3
                ds.totalRecords <= 16 -> 4
                ds.totalRecords <= 25 -> 5
                ds.totalRecords <= 36 -> 6
                ds.totalRecords <= 49 -> 7
                ds.totalRecords <= 64 -> 8
                else -> 9
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(activeNodes, key = { it.id }) { node ->
                    val color = when {
                        node.isHarvested -> NeonGreen.copy(alpha = 0.3f)
                        node.isCorruptTapped -> ErrorRed.copy(alpha = 0.8f)
                        node.isValid -> primaryColor
                        else -> Color.Gray.copy(alpha = 0.4f)
                    }
                    val borderColor = if (node.isHarvested || node.isCorruptTapped) Color.Transparent else color
                    
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(
                                color = if (node.isHarvested || node.isCorruptTapped) color else Color.Transparent,
                                shape = RoundedCornerShape(2.dp)
                            )
                            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
                            .clickable(enabled = !node.isHarvested && !node.isCorruptTapped) {
                                viewModel.tapDatasetNode(node.id)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!node.isHarvested && !node.isCorruptTapped) {
                            Text(
                                text = "0x" + node.id.toString(16).uppercase().padStart(2, '0'),
                                color = color,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else if (node.isCorruptTapped) {
                            Text("ERR", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
        } else {
            // Empty / Idle State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO DATASET LOADED",
                        color = primaryColor.copy(alpha = idleAlpha),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clickable { viewModel.toggleDatasetPicker() }
                            .border(1.dp, primaryColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "ACQUIRE DATASET",
                            color = primaryColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
