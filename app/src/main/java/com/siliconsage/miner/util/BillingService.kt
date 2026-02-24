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
        if (hasPowerInfra && pElapsed >= vm.powerBillingPeriodSeconds) {
            vm.lastPowerStatementTime = nowSec
            val gross = vm.billingPeriodAccumulator
            val gen = vm.billingPeriodGenAccumulator
            val net = (gross - gen).coerceAtLeast(0.0)
            vm.billingPeriodAccumulator = 0.0
            vm.billingPeriodGenAccumulator = 0.0
            if (net > 0.0) processPowerCharge(vm, net) else if (gen > 0.0) processPowerSurplus(vm, gen)
        }

        // Water cycle: 150s
        val wElapsed = (nowSec - vm.lastWaterStatementTime).coerceAtLeast(0L)
        vm.waterPeriodProgressFlow.value = (wElapsed.toFloat() / vm.waterBillingPeriodSeconds).coerceIn(0f, 1f)
        if (wElapsed >= vm.waterBillingPeriodSeconds) {
            vm.lastWaterStatementTime = nowSec
            processWaterCharge(vm)
        }
    }

    private fun processPowerCharge(vm: GameViewModel, net: Double) {
        val mult = when (vm.missedBillingPeriods) { 0 -> 1.0; 1 -> 2.0; 2 -> 3.0; else -> 5.0 }
        val due = net * vm.energyPriceMultiplier.value * mult
        if (vm.neuralTokens.value >= due) {
            vm.neuralTokens.update { it - due }; vm.powerBill.value = 0.0; vm.missedBillingPeriods = 0
            vm.powerBillHistory.add(0, true to due)
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
        if (vm.neuralTokens.value >= cost) {
            vm.neuralTokens.update { it - cost }
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

    private fun processPowerSurplus(vm: GameViewModel, gen: Double) {
        val credit = gen * vm.energyPriceMultiplier.value * 0.3
        vm.neuralTokens.update { it + credit }; vm.powerBill.value = 0.0; vm.missedBillingPeriods = 0
        vm.powerBillHistory.add(0, true to -credit)
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
