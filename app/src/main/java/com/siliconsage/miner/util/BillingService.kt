package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object BillingService {
    private var scope: CoroutineScope? = null
    fun setScope(s: CoroutineScope) { scope = s }

    fun processUtilityBilling(vm: GameViewModel) {
        val nowSec = System.currentTimeMillis() / 1000L
        if (!vm.isQuotaActive.value) return

        // Power cycle: 60s (v3.24.0: gated on first grid/gen upgrade)
        val hasPowerInfra = vm.localGenerationkW.value > 0 || vm.maxPowerkW.value > 100.0
        val pElapsed = (nowSec - vm.lastPowerStatementTime).coerceAtLeast(0L)
        vm.billingPeriodProgressFlow.value = if (hasPowerInfra) (pElapsed.toFloat() / vm.powerBillingPeriodSeconds).coerceIn(0f, 1f) else 0f
        if (pElapsed >= vm.powerBillingPeriodSeconds) {
            vm.lastPowerStatementTime = nowSec
            if (hasPowerInfra) {
                val gross = vm.billingPeriodAccumulator
                val gen = vm.billingPeriodGenAccumulator
                val net = (gross - gen).coerceAtLeast(0.0)
                val surplus = (gen - gross).coerceAtLeast(0.0)
                vm.billingPeriodAccumulator = 0.0
                vm.billingPeriodGenAccumulator = 0.0
                if (net > 0.0) processPowerCharge(vm, net) else if (surplus > 0.0) processPowerSurplus(vm, surplus)
            }
        }

        // Water cycle: 150s
        val hasWaterInfra = vm.waterUsage.value > 0.0 || vm.waterBillAccumulator > 0.0
        val wElapsed = (nowSec - vm.lastWaterStatementTime).coerceAtLeast(0L)
        vm.waterPeriodProgressFlow.value = if (hasWaterInfra) (wElapsed.toFloat() / vm.waterBillingPeriodSeconds).coerceIn(0f, 1f) else 0f
        if (wElapsed >= vm.waterBillingPeriodSeconds) {
            vm.lastWaterStatementTime = nowSec
            if (hasWaterInfra) {
                processWaterCharge(vm)
            }
        }
    }

    private fun processPowerCharge(vm: GameViewModel, net: Double) {
        val mult = when (vm.missedBillingPeriods) { 0 -> 1.0; 1 -> 2.0; 2 -> 3.0; else -> 5.0 }
        val due = (net / 3600.0) * vm.energyPriceMultiplier.value * mult
        val totalDue = (vm.powerBill.value + due).takeIf { it.isFinite() } ?: Double.MAX_VALUE
        if (vm.flops.value >= totalDue) {
            vm.updateSpendableFlops(-totalDue); vm.powerBill.value = 0.0; vm.missedBillingPeriods = 0
            vm.powerBillHistory.add(0, true to totalDue)
            if (vm.powerBillHistory.size > 15) vm.powerBillHistory.removeAt(15)
            flash(vm, true, "SETTLED")
        } else {
            val overdue = vm.powerBill.value + due; vm.powerBill.value = overdue; vm.missedBillingPeriods++
            vm.powerBillHistory.add(0, false to due)
            if (vm.powerBillHistory.size > 15) vm.powerBillHistory.removeAt(15)
            flash(vm, true, "OVERDUE")
        }
    }

    private fun processWaterCharge(vm: GameViewModel) {
        val rate = if (vm.storyStage.value >= 3) vm.waterRatePerGallon * 5.0 else vm.waterRatePerGallon
        val cost = vm.waterBillAccumulator * rate
        vm.waterBillAccumulator = 0.0
        if (cost <= 0.0) { flash(vm, false, "SETTLED"); return }
        if (vm.flops.value >= cost) {
            vm.updateSpendableFlops(-cost)
            vm.waterBillHistory.add(0, true to cost)
            if (vm.waterBillHistory.size > 15) vm.waterBillHistory.removeAt(15)
            flash(vm, false, "SETTLED")
        } else {
            vm.waterEfficiencyMultiplier.update { (it * 0.5).coerceAtLeast(0.1) }
            vm.waterBillHistory.add(0, false to cost)
            if (vm.waterBillHistory.size > 15) vm.waterBillHistory.removeAt(15)
            flash(vm, false, "OVERDUE")
        }
    }

    private fun processPowerSurplus(vm: GameViewModel, surplus: Double) {
        val credit = (surplus / 3600.0) * vm.energyPriceMultiplier.value * 0.3
        val safeCredit = if (credit.isFinite()) credit.coerceAtLeast(0.0) else 0.0
        val remainingBill = (vm.powerBill.value - safeCredit).coerceAtLeast(0.0)
        val walletCredit = (safeCredit - vm.powerBill.value).coerceAtLeast(0.0)
        vm.powerBill.value = remainingBill
        if (remainingBill <= 0.0) vm.missedBillingPeriods = 0
        if (walletCredit > 0.0) vm.updateSpendableFlops(walletCredit)
        vm.powerBillHistory.add(0, true to -safeCredit)
        if (vm.powerBillHistory.size > 15) vm.powerBillHistory.removeAt(15)
        flash(vm, true, "CREDIT")
    }

    private fun flash(vm: GameViewModel, isPower: Boolean, state: String) {
        scope?.launch {
            if (isPower) {
                vm.billingAccumulatorFlow.value = 0.0; vm.billingFlashState.value = state
                delay(1200); vm.billingFlashState.value = null
            } else {
                vm.waterBillingFlow.value = 0.0; vm.waterFlashState.value = state
                delay(1200); vm.waterFlashState.value = null
            }
        }
    }
}
