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
            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.width(40.dp).height(3.dp).background(color.copy(0.3f), RoundedCornerShape(2.dp)))
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
    val storyStageValue by viewModel.storyStage.collectAsState()
    val billingAmountValue by viewModel.billingAccumulatorFlow.collectAsState()
    val waterAmountValue by viewModel.waterBillingFlow.collectAsState()
    val pPValue by viewModel.billingPeriodProgressFlow.collectAsState()
    val wPValue by viewModel.waterPeriodProgressFlow.collectAsState()
    val missedPeriodsValue = viewModel.missedBillingPeriods
    val powerBillValue by viewModel.powerBill.collectAsState()
    val neuralTokensValue by viewModel.neuralTokens.collectAsState()

    val activePowerValue by viewModel.activePowerUsage.collectAsState()
    val maxPowerValue by viewModel.maxPowerkW.collectAsState()
    val localGenValue by viewModel.localGenerationkW.collectAsState()
    val netDrawValue = (activePowerValue - localGenValue).coerceAtLeast(0.0)

    val waterUsageValue by viewModel.waterUsage.collectAsState()
    val waterEfficiencyValue by viewModel.waterEfficiencyMultiplier.collectAsState()
    val aquiferLevelValue by viewModel.aquiferLevel.collectAsState()

    val totalPeriodCost = (billingAmountValue + waterAmountValue)
    val flopsPeriodRevenue = viewModel.flopsProductionRate.collectAsState().value * 60.0 * viewModel.conversionRate.collectAsState().value
    val profitMargin = if (flopsPeriodRevenue > 0) ((flopsPeriodRevenue - totalPeriodCost) / flopsPeriodRevenue * 100.0) else 0.0

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp).padding(bottom = 32.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "GTC UTILITY CONSOLE", color = color, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
            Text(text = "[CLOSE]", color = color.copy(0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.clickable { onDismiss() })
        }

        SectionHeader("ACCOUNT STATUS", color)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = "PROFIT MARGIN", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Text(text = "${String.format("%.1f", profitMargin)}%", color = if (profitMargin > 0) NeonGreen else ErrorRed, fontSize = 18.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            }
            if (powerBillValue > 0.0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "TOTAL OVERDUE", color = ErrorRed.copy(0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "$${viewModel.formatLargeNumber(powerBillValue)}", color = ErrorRed, fontSize = 18.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionHeader("CURRENT PERIOD", color)
        UtilPeriodBar("POWER CYCLE", pPValue, color)
        UtilRow("EST. POWER BILL", viewModel.formatLargeNumber(billingAmountValue), "NEUR", if (billingAmountValue > neuralTokensValue * 0.9) ErrorRed else if (billingAmountValue > neuralTokensValue * 0.5) Color(0xFFFFCC00) else color)
        if (missedPeriodsValue > 0) UtilRow("DEMAND MULT", "×${missedPeriodsValue + 1}", "", ErrorRed)
        
        Spacer(Modifier.height(8.dp))
        UtilPeriodBar("WATER CYCLE", wPValue, ElectricBlue)
        UtilRow("EST. WATER BILL", viewModel.formatLargeNumber(waterAmountValue), "NEUR", ElectricBlue)

        Spacer(Modifier.height(16.dp))
        SectionHeader("POWER BREAKDOWN", color)
        StackedBarGraph("GEN", localGenValue.toFloat(), "DRAW", activePowerValue.toFloat(), maxOf(maxPowerValue, activePowerValue).toFloat(), ElectricBlue, if (netDrawValue > maxPowerValue * 0.9) ErrorRed else Color(0xFFFFD700), color)
        UtilRow("GROSS DRAW", viewModel.formatPower(activePowerValue), "")
        UtilRow("LOCAL GEN", viewModel.formatPower(localGenValue), "", ElectricBlue)
        UtilRow("CAPACITY", viewModel.formatPower(maxPowerValue), "")

        Spacer(Modifier.height(16.dp))
        SectionHeader("WATER BREAKDOWN", color)
        if (storyStageValue >= 3) {
            UtilRow("AQUIFER LEVEL", "${aquiferLevelValue.toInt()}%", "", if (aquiferLevelValue < 25) ErrorRed else ElectricBlue)
        }
        UtilRow("DRAW RATE", "${waterUsageValue.toInt()} gal/s", "")
        UtilRow("COOLING EFF", "${(waterEfficiencyValue * 100).toInt()}%", "", if (waterEfficiencyValue < 1.0) Color(0xFFFFCC00) else ElectricBlue)

        Spacer(Modifier.height(16.dp))
        SectionHeader("BILLING HISTORY (L15)", color)
        BillingHistoryGraph(viewModel.powerBillHistory, viewModel.waterBillHistory, color) { viewModel.formatLargeNumber(it) }
    }
}

@Composable
private fun UtilPeriodBar(label: String, progress: Float, color: Color) {
    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(80.dp))
        Box(Modifier.weight(1f).height(6.dp).background(Color.DarkGray.copy(0.3f), RoundedCornerShape(3.dp))) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(progress).background(color.copy(if (progress > 0.85f) 1f else 0.7f), RoundedCornerShape(3.dp)))
        }
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(3.dp).height(10.dp).background(color))
        Spacer(Modifier.width(6.dp))
        Text(text = title, color = color.copy(0.9f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
        Spacer(Modifier.width(8.dp))
        Divider(color = color.copy(0.15f), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun UtilRow(label: String, value: String, suffix: String, valueColor: Color = Color.White) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(text = if (suffix.isNotEmpty()) "$value $suffix" else value, color = valueColor, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun StackedBarGraph(lLabel: String, lVal: Float, rLabel: String, rVal: Float, maxV: Float, lCol: Color, rCol: Color, color: Color) {
    Column {
        Row(Modifier.fillMaxWidth().height(32.dp)) {
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) { Box(Modifier.fillMaxHeight().padding(end = 2.dp).fillMaxWidth(if (maxV > 0) (lVal/maxV).coerceIn(0f, 1f) else 0f).background(lCol.copy(0.25f), RoundedCornerShape(topStart=3.dp, bottomStart=3.dp)).border(0.5.dp, lCol.copy(0.5f), RoundedCornerShape(topStart=3.dp, bottomStart=3.dp))) }
            Box(Modifier.width(1.dp).fillMaxHeight().background(color.copy(0.3f)))
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterStart) { Box(Modifier.fillMaxHeight().padding(start = 2.dp).fillMaxWidth(if (maxV > 0) (rVal/maxV).coerceIn(0f, 1f) else 0f).background(rCol.copy(0.25f), RoundedCornerShape(topEnd=3.dp, bottomEnd=3.dp)).border(0.5.dp, rCol.copy(0.5f), RoundedCornerShape(topEnd=3.dp, bottomEnd=3.dp))) }
        }
        Row(Modifier.fillMaxWidth()) { Text(text = lLabel, color = lCol.copy(0.7f), fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f), textAlign = TextAlign.End); Spacer(Modifier.width(8.dp)); Text(text = rLabel, color = rCol.copy(0.7f), fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun LegendDot(dotC: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).background(dotC, RoundedCornerShape(1.dp)))
        Spacer(Modifier.width(3.dp))
        Text(text = label, color = Color.Gray, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun BillingHistoryGraph(pHist: List<Pair<Boolean, Double>>, wHist: List<Pair<Boolean, Double>>, color: Color, formatFn: (Double) -> String) {
    if (pHist.isEmpty() && wHist.isEmpty()) { Text(text = "NO HISTORY", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.fillMaxWidth().padding(8.dp), textAlign = TextAlign.Center); return }
    val pChron = pHist.take(15).reversed(); val wChron = wHist.take(15).reversed()
    val maxA = (pChron.map { it.second } + wChron.map { it.second }).maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    val pCnt = maxOf(pChron.size, wChron.size, 2)
    Canvas(Modifier.fillMaxWidth().height(100.dp).padding(vertical=4.dp).border(0.5.dp, Color.DarkGray.copy(0.2f), RoundedCornerShape(4.dp))) {
        val wValue = size.width; val hValue = size.height; val pL = 8.dp.toPx(); val pR = 8.dp.toPx(); val pT = 16.dp.toPx(); val pB = 16.dp.toPx(); val cW = wValue - pL - pR; val cH = hValue - pT - pB
        for (i in 0..4) { val yValue = pT + cH * (i.toFloat()/4); drawLine(Color.DarkGray.copy(0.15f), Offset(pL, yValue), Offset(pL+cW, yValue), 0.5.dp.toPx()) }
        fun xPValue(i: Int) = pL + (i.toFloat() / (pCnt - 1)) * cW
        fun yPValue(aValue: Double) = pT + cH * (1f - (aValue/maxA).toFloat().coerceIn(0f, 1f))
        if (wChron.size >= 2) { val pathVl = androidx.compose.ui.graphics.Path(); wChron.forEachIndexed { i, (pd, a) -> val xValue = xPValue(i); val yValue = yPValue(a); if (i == 0) pathVl.moveTo(xValue, yValue) else pathVl.lineTo(xValue, yValue) }; drawPath(pathVl, ElectricBlue.copy(0.4f), style = Stroke(1.2.dp.toPx())) }
        if (pChron.size >= 2) { val pathVl = androidx.compose.ui.graphics.Path(); pChron.forEachIndexed { i, (pd, a) -> val xValue = xPValue(i); val yValue = yPValue(a); if (i == 0) pathVl.moveTo(xValue, yValue) else pathVl.lineTo(xValue, yValue) }; drawPath(pathVl, color.copy(0.6f), style = Stroke(1.5.dp.toPx())) }
        pChron.forEachIndexed { i, (pd, a) -> val xValue = xPValue(i); val yValue = yPValue(a); drawCircle(if (pd) color else ErrorRed, 3.dp.toPx(), Offset(xValue, yValue)); drawCircle(Color.Black, 1.dp.toPx(), Offset(xValue, yValue)) }
        wChron.forEachIndexed { i, (pd, a) -> val xValue = xPValue(i); val yValue = yPValue(a); drawCircle(if (pd) ElectricBlue else Color(0xFF004466), 2.dp.toPx(), Offset(xValue, yValue)); drawCircle(Color.Black, 0.5.dp.toPx(), Offset(xValue, yValue)) }
    }
    Row(Modifier.fillMaxWidth().padding(top=4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { LegendDot(color, "PWR"); LegendDot(ElectricBlue, "H2O") }
        val lPValue = pHist.firstOrNull(); val lWValue = wHist.firstOrNull()
        Text(text = "LATEST: P ${lPValue?.let { formatFn(it.second) } ?: "0"} | W ${lWValue?.let { formatFn(it.second) } ?: "0"}", color = Color.Gray, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
    }
}
