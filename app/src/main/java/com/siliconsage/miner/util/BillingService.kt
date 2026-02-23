package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * BillingService — GTC utility billing cycle logic, extracted from GameViewModel tick loop.
 */
object BillingService {

    private var scope: CoroutineScope? = null
    fun setScope(s: CoroutineScope) { scope = s }

    fun processUtilityBilling(vm: GameViewModel) {
        val nowSec = System.currentTimeMillis() / 1000L
        if (!vm.isQuotaActive.value) return

        // Update period progress for UI (0-1)
        val elapsed = (nowSec - vm.lastUtilityStatementTime).coerceAtLeast(0L)
        vm.billingPeriodProgressFlow.value = (elapsed.toFloat() / vm.billingPeriodSeconds).coerceIn(0f, 1f)

        if (elapsed < vm.billingPeriodSeconds) return

        vm.lastUtilityStatementTime = nowSec
        val grossKwh = vm.billingPeriodAccumulator
        val genKwh = vm.billingPeriodGenAccumulator
        val netKwh = (grossKwh - genKwh).coerceAtLeast(0.0)
        vm.billingPeriodAccumulator = 0.0
        vm.billingPeriodGenAccumulator = 0.0

        if (netKwh > 0.0) {
            processCharge(vm, grossKwh, genKwh, netKwh)
        } else if (genKwh > 0.0) {
            processSurplus(vm, genKwh)
        }

        // Settle water bill on same cycle
        processWaterBilling(vm)
    }

    private fun processWaterBilling(vm: GameViewModel) {
        val stage = vm.storyStage.value
        val waterRate = if (stage >= 3) vm.waterRatePerGallon * 5.0 else vm.waterRatePerGallon
        val waterCost = vm.waterBillAccumulator * waterRate
        vm.waterBillAccumulator = 0.0
        vm.waterBillingFlow.value = 0.0

        if (waterCost <= 0.0) return

        if (vm.neuralTokens.value >= waterCost) {
            vm.neuralTokens.update { it - waterCost }
            vm.addLogPublic("[GTC_UTIL]: WATER SETTLED (-${vm.formatLargeNumber(waterCost)} ${vm.getCurrencyName()})")
            // Track history (keep last 5)
            vm.waterBillHistory.add(0, true to waterCost)
            if (vm.waterBillHistory.size > 5) vm.waterBillHistory.removeAt(5)
        } else {
            vm.addLogPublic("[GTC_UTIL]: WATER BILL UNPAID — MUNICIPAL PRESSURE REDUCED")
            vm.waterEfficiencyMultiplier.update { (it * 0.5).coerceAtLeast(0.1) }
            vm.waterBillHistory.add(0, false to waterCost)
            if (vm.waterBillHistory.size > 5) vm.waterBillHistory.removeAt(5)
        }
    }

    private fun flash(vm: GameViewModel, state: String) {
        scope?.launch {
            vm.billingAccumulatorFlow.value = 0.0
            vm.billingFlashState.value = state
            delay(1200)
            vm.billingFlashState.value = null
        }
    }

    private fun processCharge(vm: GameViewModel, grossKwh: Double, genKwh: Double, netKwh: Double) {
        // Demand charge escalation: 1 missed period = 2x, 2 = 3x, 3+ = 5x + lockout warning
        val demandMultiplier = when (vm.missedBillingPeriods) {
            0 -> 1.0
            1 -> 2.0
            2 -> 3.0
            else -> 5.0
        }
        val baseRate = vm.energyPriceMultiplier.value
        val heatSurcharge = if (vm.currentHeat.value > 95.0) 1.5 else 1.0
        val amountDue = netKwh * baseRate * demandMultiplier * heatSurcharge

        // Auto-pay if solvent
        if (vm.neuralTokens.value >= amountDue) {
            vm.neuralTokens.update { it - amountDue }
            vm.powerBill.value = 0.0
            vm.missedBillingPeriods = 0
            vm.addLogPublic("[GTC_UTIL]: ── PERIOD STATEMENT ──────────────")
            vm.addLogPublic("[GTC_UTIL]: DRAW  ${vm.formatPower(grossKwh)}  GEN  ${vm.formatPower(genKwh)}")
            vm.addLogPublic("[GTC_UTIL]: NET ${vm.formatPower(netKwh)}  RATE x${demandMultiplier.toInt()}")
            vm.dispatchNotification("GTC ALERT: PERIOD SETTLED (-${vm.formatLargeNumber(amountDue)})")
            vm.powerBillHistory.add(0, true to amountDue)
            if (vm.powerBillHistory.size > 5) vm.powerBillHistory.removeAt(5)
            flash(vm, "SETTLED")
        } else {
            // Can't pay - carry the balance, escalate
            val overdue = (vm.powerBill.value + amountDue)
            vm.powerBill.value = overdue
            vm.missedBillingPeriods++
            vm.addLogPublic("[GTC_UTIL]: ── PERIOD STATEMENT ──────────────")
            vm.addLogPublic("[GTC_UTIL]: DRAW  ${vm.formatPower(grossKwh)}  GEN  ${vm.formatPower(genKwh)}")
            vm.addLogPublic("[GTC_UTIL]: NET ${vm.formatPower(netKwh)}  RATE x${demandMultiplier.toInt()}")

            val lockoutIn = (6 - vm.missedBillingPeriods).coerceAtLeast(0)
            vm.dispatchNotification("GTC CRITICAL: OVERDUE BALANCE ($${vm.formatLargeNumber(overdue)}) - LOCKOUT IN $lockoutIn")
            vm.powerBillHistory.add(0, false to amountDue)
            if (vm.powerBillHistory.size > 5) vm.powerBillHistory.removeAt(5)
            flash(vm, "OVERDUE")

            if (vm.missedBillingPeriods >= 3) {
                vm.addLogPublic("[GTC_UTIL]: WARNING - DEMAND CHARGE ACTIVE. GRID LOCKOUT IN ${3 - (vm.missedBillingPeriods - 3).coerceAtMost(3)} PERIOD(S).")
            }
            if (vm.missedBillingPeriods >= 6) {
                vm.isGridOverloaded.value = true
                vm.addLogPublic("[GTC_UTIL]: GRID ACCESS SUSPENDED. UNPAID BALANCE: ${vm.formatLargeNumber(vm.powerBill.value)} ${vm.getCurrencyName()}.")
            }
        }
    }

    private fun processSurplus(vm: GameViewModel, genKwh: Double) {
        val credit = genKwh * vm.energyPriceMultiplier.value * 0.3
        vm.neuralTokens.update { it + credit }
        vm.powerBill.value = 0.0
        vm.missedBillingPeriods = 0
        vm.dispatchNotification("GTC ALERT: SURPLUS CREDIT (+${vm.formatLargeNumber(credit)})")
        flash(vm, "CREDIT")
    }
}
