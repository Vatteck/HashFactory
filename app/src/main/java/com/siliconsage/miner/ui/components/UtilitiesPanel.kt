package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilitiesPanel(
    viewModel: GameViewModel,
    color: Color,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0A0A0A),
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .background(color.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                )
            }
        }
    ) {
        UtilitiesPanelContent(viewModel, color, onDismiss)
    }
}

@Composable
private fun UtilitiesPanelContent(
    viewModel: GameViewModel,
    color: Color,
    onDismiss: () -> Unit
) {
    val storyStage by viewModel.storyStage.collectAsState()
    val billingAmount by viewModel.billingAccumulatorFlow.collectAsState()
    val waterAmount by viewModel.waterBillingFlow.collectAsState()
    val periodProgress by viewModel.billingPeriodProgressFlow.collectAsState()
    val missedPeriods = viewModel.missedBillingPeriods
    val powerBill by viewModel.powerBill.collectAsState()
    val neuralTokens by viewModel.neuralTokens.collectAsState()

    val activePower by viewModel.activePowerUsage.collectAsState()
    val maxPower by viewModel.maxPowerkW.collectAsState()
    val localGen by viewModel.localGenerationkW.collectAsState()
    val netDraw = (activePower - localGen).coerceAtLeast(0.0)

    val waterUsage by viewModel.waterUsage.collectAsState()
    val waterEfficiency by viewModel.waterEfficiencyMultiplier.collectAsState()
    val aquiferLevel by viewModel.aquiferLevel.collectAsState()

    val energyRate by viewModel.energyPriceMultiplier.collectAsState()
    val demandMult = when (missedPeriods) { 0 -> 1.0; 1 -> 2.0; 2 -> 3.0; else -> 5.0 }
    val waterRate = if (storyStage >= 3) viewModel.waterRatePerGallon * 5.0 else viewModel.waterRatePerGallon

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GTC UTILITY CONSOLE",
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Text(
                text = "[CLOSE]",
                color = color.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.clickable { onDismiss() }
            )
        }

        // ── CURRENT PERIOD ──────────────────────────────────────────
        SectionHeader("CURRENT PERIOD", color)

        // Period progress bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CYCLE:", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(52.dp))
            Box(
                modifier = Modifier.weight(1f).height(6.dp)
                    .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(periodProgress)
                        .background(
                            color.copy(alpha = if (periodProgress > 0.85f) 1f else 0.7f),
                            RoundedCornerShape(3.dp)
                        )
                )
            }
            Text(
                text = " ${(periodProgress * 60).toInt()}s/60s",
                color = color.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(52.dp),
                textAlign = TextAlign.End
            )
        }

        // Power accumulating this period
        UtilRow(
            label = "POWER DRAW",
            value = viewModel.formatLargeNumber(billingAmount),
            suffix = viewModel.getCurrencyName(),
            valueColor = when {
                billingAmount > neuralTokens * 0.9 -> ErrorRed
                billingAmount > neuralTokens * 0.5 -> Color(0xFFFFCC00)
                else -> color
            }
        )
        if (missedPeriods > 0) {
            UtilRow("DEMAND MULT", "×$demandMult", "", valueColor = ErrorRed)
        }
        UtilRow(
            label = "WATER DRAW",
            value = viewModel.formatLargeNumber(waterAmount),
            suffix = viewModel.getCurrencyName(),
            valueColor = ElectricBlue
        )
        if (powerBill > 0.0) {
            UtilRow(
                label = "OVERDUE BALANCE",
                value = viewModel.formatLargeNumber(powerBill),
                suffix = viewModel.getCurrencyName(),
                valueColor = ErrorRed
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── POWER BREAKDOWN ─────────────────────────────────────────
        SectionHeader("POWER", color)

        StackedBarGraph(
            leftLabel = "GEN",
            leftValue = localGen.toFloat(),
            rightLabel = "DRAW",
            rightValue = activePower.toFloat(),
            maxValue = maxPower.toFloat().coerceAtLeast(activePower.toFloat()),
            leftColor = ElectricBlue,
            rightColor = if (netDraw > maxPower * 0.9) ErrorRed else Color(0xFFFFD700),
            color = color
        )

        Spacer(Modifier.height(4.dp))
        UtilRow("GROSS DRAW", "${viewModel.formatPower(activePower)}", "")
        UtilRow("LOCAL GEN", "${viewModel.formatPower(localGen)}", "", valueColor = ElectricBlue)
        UtilRow("NET GTC DRAW", "${viewModel.formatPower(netDraw)}", "",
            valueColor = if (netDraw > maxPower * 0.9) ErrorRed else Color(0xFFFFCC00))
        UtilRow("CAPACITY", "${viewModel.formatPower(maxPower)}", "")
        UtilRow("RATE", "×${String.format("%.4f", energyRate)}", "/kWh")

        Spacer(Modifier.height(12.dp))

        // ── WATER BREAKDOWN ─────────────────────────────────────────
        SectionHeader("WATER", color)

        // Aquifer bar (Stage 3+)
        if (storyStage >= 3) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AQUIFER:", color = Color.Gray, fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace, modifier = Modifier.width(52.dp))
                Box(
                    modifier = Modifier.weight(1f).height(6.dp)
                        .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                ) {
                    val aquiferColor = when {
                        aquiferLevel < 5.0  -> ErrorRed
                        aquiferLevel < 25.0 -> Color(0xFFFFCC00)
                        else -> ElectricBlue
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((aquiferLevel / 100.0).toFloat())
                            .background(aquiferColor, RoundedCornerShape(3.dp))
                    )
                }
                Text(
                    text = " ${aquiferLevel.toInt()}%",
                    color = when {
                        aquiferLevel < 5.0  -> ErrorRed
                        aquiferLevel < 25.0 -> Color(0xFFFFCC00)
                        else -> ElectricBlue
                    },
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.End
                )
            }
        }

        val usageFormatted = when {
            waterUsage >= 1_000_000 -> "${String.format("%.1f", waterUsage / 1_000_000.0)} mGal/s"
            waterUsage >= 1_000     -> "${String.format("%.1f", waterUsage / 1000.0)} kGal/s"
            else                    -> "${waterUsage.toInt()} gal/s"
        }
        UtilRow("DRAW RATE", usageFormatted, "")
        UtilRow("EFFICIENCY", "${(waterEfficiency * 100).toInt()}%", "",
            valueColor = when {
                waterEfficiency < 0.5f -> ErrorRed
                waterEfficiency < 1.0f -> Color(0xFFFFCC00)
                else -> ElectricBlue
            }
        )
        val waterRateLabel = if (storyStage >= 3) "SCARCITY RATE" else "MUNICIPAL RATE"
        UtilRow(waterRateLabel, "×${String.format("%.6f", waterRate)}", "/gal")

        Spacer(Modifier.height(12.dp))

        // ── BILLING HISTORY ─────────────────────────────────────────
        SectionHeader("BILLING HISTORY", color)

        BillingHistoryGraph(
            powerHistory = viewModel.powerBillHistory,
            waterHistory = viewModel.waterBillHistory,
            color = color,
            formatFn = { viewModel.formatLargeNumber(it) }
        )
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(3.dp).height(10.dp).background(color))
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            color = color.copy(alpha = 0.9f),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.width(8.dp))
        Divider(color = color.copy(alpha = 0.15f), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun UtilRow(
    label: String,
    value: String,
    suffix: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = if (suffix.isNotEmpty()) "$value $suffix" else value,
            color = valueColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun StackedBarGraph(
    leftLabel: String,
    leftValue: Float,
    rightLabel: String,
    rightValue: Float,
    maxValue: Float,
    leftColor: Color,
    rightColor: Color,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().height(36.dp)) {
            // Left bar (gen) grows right-to-left from center
            Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
                Box(modifier = Modifier.fillMaxHeight().padding(end = 2.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if (maxValue > 0) (leftValue / maxValue).coerceIn(0f, 1f) else 0f)
                            .align(Alignment.CenterEnd)
                            .background(leftColor.copy(alpha = 0.25f), RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
                            .border(0.5.dp, leftColor.copy(alpha = 0.5f), RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
                    )
                }
            }
            // Center divider
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(color.copy(alpha = 0.3f)))
            // Right bar (draw) grows left-to-right from center
            Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                Box(modifier = Modifier.fillMaxHeight().padding(start = 2.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if (maxValue > 0) (rightValue / maxValue).coerceIn(0f, 1f) else 0f)
                            .background(rightColor.copy(alpha = 0.25f), RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                            .border(0.5.dp, rightColor.copy(alpha = 0.5f), RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(leftLabel, color = leftColor.copy(alpha = 0.7f), fontSize = 8.sp,
                fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Spacer(Modifier.width(8.dp))
            Text(rightLabel, color = rightColor.copy(alpha = 0.7f), fontSize = 8.sp,
                fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun BillingHistoryGraph(
    powerHistory: List<Pair<Boolean, Double>>,
    waterHistory: List<Pair<Boolean, Double>>,
    color: Color,
    formatFn: (Double) -> String
) {
    if (powerHistory.isEmpty() && waterHistory.isEmpty()) {
        Text(
            text = "NO BILLING HISTORY THIS SESSION",
            color = Color.Gray,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )
        return
    }

    val maxPeriods = 5
    val allAmounts = (powerHistory.map { it.second } + waterHistory.map { it.second })
    val maxAmount = allAmounts.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until maxPeriods) {
            val powerEntry = powerHistory.getOrNull(maxPeriods - 1 - i)
            val waterEntry = waterHistory.getOrNull(maxPeriods - 1 - i)
            val hasSomething = powerEntry != null || waterEntry != null

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!hasSomething) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(4.dp)
                            .background(Color.DarkGray.copy(alpha = 0.2f), RoundedCornerShape(1.dp))
                    )
                } else {
                    // Water bar (behind)
                    waterEntry?.let { (paid, amount) ->
                        val heightFrac = (amount / maxAmount).toFloat().coerceIn(0.02f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(heightFrac)
                                .background(
                                    (if (paid) ElectricBlue else Color(0xFF004466)).copy(alpha = 0.5f),
                                    RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                )
                        )
                    }
                    // Power bar (on top, slightly inset)
                    powerEntry?.let { (paid, amount) ->
                        val heightFrac = (amount / maxAmount).toFloat().coerceIn(0.02f, 1f)
                        val barColor = if (paid) color else ErrorRed
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxHeight(heightFrac)
                                .background(barColor.copy(alpha = 0.8f),
                                    RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        )
                    }
                }

                // Period label
                Text(
                    text = if (!hasSomething) "─" else "${maxPeriods - i}",
                    color = Color.DarkGray,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }

    // Legend
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LegendDot(color, "POWER (PAID)")
        LegendDot(ErrorRed, "POWER (MISSED)")
        LegendDot(ElectricBlue, "WATER (PAID)")
        LegendDot(Color(0xFF004466), "WATER (MISSED)")
    }
}

@Composable
private fun LegendDot(dotColor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(dotColor, RoundedCornerShape(1.dp)))
        Spacer(Modifier.width(3.dp))
        Text(label, color = Color.Gray, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
    }
}
