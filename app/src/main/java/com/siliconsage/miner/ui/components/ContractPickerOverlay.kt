package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.data.ComputeContract
import com.siliconsage.miner.ui.theme.*
import com.siliconsage.miner.util.FormatUtils
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

/**
 * ContractPickerOverlay v1.0 (v3.30.0)
 * Fullscreen overlay showing available compute contracts for purchase.
 */
@Composable
fun ContractPickerOverlay(viewModel: GameViewModel, primaryColor: Color) {
    val contracts by viewModel.availableContracts.collectAsState()
    val currentTokens by viewModel.neuralTokens.collectAsState()
    val currencyName = viewModel.getCurrencyName()

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable(enabled = false) { /* consume clicks */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f)
                .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
                .border(BorderStroke(1.dp, primaryColor.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                "≫ COMPUTE CONTRACTS",
                color = primaryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Balance: ${FormatUtils.formatLargeNumber(currentTokens)} $currencyName",
                color = Color.LightGray,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (contracts.isEmpty()) {
                // No contracts available
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "[NO CONTRACTS AVAILABLE — WAIT FOR MARKET TICK]",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            } else {
                // Contract list
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contracts) { contract ->
                        ContractCard(
                            contract = contract,
                            canAfford = currentTokens >= contract.cost,
                            primaryColor = primaryColor,
                            currencyName = currencyName,
                            onPurchase = {
                                viewModel.purchaseContract(contract)
                                viewModel.toggleContractPicker()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Close button
            Text(
                "[ CLOSE ]",
                color = primaryColor.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    viewModel.toggleContractPicker()
                    SoundManager.play("click")
                }
            )
        }
    }
}

@Composable
private fun ContractCard(
    contract: ComputeContract,
    canAfford: Boolean,
    primaryColor: Color,
    currencyName: String,
    onPurchase: () -> Unit
) {
    val borderColor = if (canAfford) primaryColor else Color.Gray
    val textColor = if (canAfford) Color.White else Color.Gray
    val purityPercent = (contract.purity * 100).toInt()
    val purityColor = when {
        contract.purity >= 0.85 -> NeonGreen
        contract.purity >= 0.60 -> Color(0xFFFFB000) // Amber
        else -> ErrorRed
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111111), RoundedCornerShape(6.dp))
            .border(BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)), RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = canAfford) {
                onPurchase()
                HapticManager.vibrateClick()
            }
            .padding(12.dp)
    ) {
        Column {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(contract.name, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    "P:${purityPercent}%",
                    color = purityColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Cost and yield row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("COST", color = Color.Gray, fontSize = 9.sp)
                    Text(
                        "${FormatUtils.formatLargeNumber(contract.cost)} $currencyName",
                        color = if (canAfford) ErrorRed else Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("YIELD", color = Color.Gray, fontSize = 9.sp)
                    Text(
                        "≈${FormatUtils.formatLargeNumber(contract.expectedYield)} $currencyName",
                        color = if (canAfford) NeonGreen else Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Processing time
            val timeSeconds = contract.processingTime / 1000
            Text(
                "Base Processing: ${timeSeconds}s",
                color = Color.Gray,
                fontSize = 9.sp
            )

            if (!canAfford) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "INSUFFICIENT FUNDS",
                    color = ErrorRed.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
