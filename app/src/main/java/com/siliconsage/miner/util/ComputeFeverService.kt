package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update

/**
 * ComputeFeverService — Signal stability, quota ratcheting, wage-docking, and substrate static.
 * Extracted from GameViewModel.processComputeFever().
 */
object ComputeFeverService {

    // Decay state (owned by service, not VM)
    private var signalDecayStartTime = 0L
    private var lastStabilityBase = 1.0
    private const val SIGNAL_DECAY_DURATION_MS = 300000L // 5 minutes

    fun process(vm: GameViewModel, now: Long) {
        val passiveFlops = vm.flopsProductionRate.value
        val clickEffort = when (vm.clickSpeedLevel.value) {
            1 -> vm.calculateClickPower() * 2.0
            2 -> vm.calculateClickPower() * 5.0
            else -> 0.0
        }
        val currentFlops = passiveFlops + clickEffort
        vm.totalEffectiveRate.value = currentFlops
        val quota = vm.currentQuotaThreshold.value

        // Quota Activation (Grace Period ends at first production)
        if (!vm.isQuotaActive.value && currentFlops > 0.0) {
            vm.isQuotaActive.value = true
            vm.addLogPublic("[GTC_SYSTEM]: BIOMETRIC QUOTA LINK ESTABLISHED. MAINTAIN SIGNAL.")
        }

        if (!vm.isQuotaActive.value) {
            vm.signalStability.value = 1.0
            vm.substrateStaticIntensity.value = 0f
            return
        }

        // Signal Stability with 5-Minute Adaptive Decay
        val rawStability = (currentFlops / quota).coerceIn(0.0, 1.0)

        if (rawStability < lastStabilityBase && signalDecayStartTime == 0L) {
            signalDecayStartTime = now
        } else if (rawStability >= lastStabilityBase) {
            signalDecayStartTime = 0L
        }

        val stability = if (signalDecayStartTime > 0L) {
            val elapsed = now - signalDecayStartTime
            val progress = (elapsed.toDouble() / SIGNAL_DECAY_DURATION_MS).coerceIn(0.0, 1.0)
            val floor = if (elapsed < 60000L) 0.7 else 0.0
            val lerped = lastStabilityBase + (rawStability - lastStabilityBase) * progress
            lerped.coerceAtLeast(floor).coerceIn(0.0, 1.0)
        } else {
            rawStability
        }

        lastStabilityBase = stability
        vm.signalStability.value = stability

        // Signal Quality Bonus (Clear Signal = 2x Quota)
        val isClear = currentFlops >= (quota * 2.0)
        if (vm.isSignalClear.value != isClear) {
            vm.isSignalClear.value = isClear
            vm.computeHeadroomBonus.value = if (isClear) 1.1 else 1.0
            if (isClear) {
                if (vm.storyStage.value <= 1) vm.addLogPublic("[SYSTEM]: SIGNAL STABILIZED. RACK OVER-PROVISION DETECTED.")
                SoundManager.play("message_received", pitch = 0.8f)
            } else {
                if (vm.storyStage.value <= 1) vm.addLogPublic("[VATTIC]: The static is back. I need to stack more nodes.")
            }
        }

        // Wage-Docking Logic (Neural Sync Failure)
        processWageDocking(vm, stability, now)

        // Substrate Static Intensity
        val intensity = if (stability >= 0.5) 0f
        else ((0.5 - stability) * 0.6).toFloat().coerceIn(0f, 0.3f)
        vm.substrateStaticIntensity.value = intensity

        // Quota Ratcheting
        processQuotaRatchet(vm, currentFlops)
    }

    private fun processWageDocking(vm: GameViewModel, stability: Double, now: Long) {
        if (stability < 0.2 && vm.isQuotaActive.value) {
            if (vm.lastLowSignalTime == 0L) vm.lastLowSignalTime = now
            if (now - vm.lastLowSignalTime > 30000L && !vm.isWageDocking.value) {
                vm.isWageDocking.value = true
                vm.dispatchNotification("GTC CRITICAL: NEURAL SYNC FAILURE - WAGE DOCKING ACTIVE")
            }
        } else {
            vm.lastLowSignalTime = 0L
            if (vm.isWageDocking.value) {
                vm.isWageDocking.value = false
                vm.dispatchNotification("GTC ALERT: NEURAL SYNC RESTORED - DOCKING TERMINATED")
            }
        }
    }

    private fun processQuotaRatchet(vm: GameViewModel, currentFlops: Double) {
        val nextTarget = when (vm.storyStage.value) {
            0 -> when {
                currentFlops < 10.0 -> 10.0
                currentFlops < 50.0 -> 50.0
                else -> 200.0
            }
            1 -> 15000.0
            2 -> 500000.0
            3 -> 10_000_000.0
            else -> 0.0
        }

        // Balanced Quota Ratchet (20% Potential or 1.5x current)
        if (nextTarget > vm.currentQuotaThreshold.value) {
            val floor = vm.currentQuotaThreshold.value * 1.5
            val ceiling = nextTarget * 0.20
            val potentialThreshold = ceiling.coerceAtLeast(floor)

            // Ghost Bar Progress
            val pProgress = if (currentFlops > floor) {
                ((currentFlops - floor) / (potentialThreshold - floor)).toFloat().coerceIn(0f, 1f)
            } else 0f
            vm.potentialProgress.update { pProgress }

            if (currentFlops >= potentialThreshold) {
                vm.currentQuotaThreshold.value = nextTarget
                vm.pendingQuotaThreshold.value = nextTarget
                vm.addLogPublic("[GTC_SYSTEM]: POTENTIAL DETECTED. QUOTA RATIFIED: ${vm.formatLargeNumber(nextTarget)} HASH.")
                vm.dispatchNotification("GTC ALERT: QUOTA RATIFIED. TARGET: ${vm.formatLargeNumber(nextTarget)} HASH")
                vm.shiftTimeRemaining.update { it + 43200L }
                vm.dispatchNotification("GTC ALERT: OVERTIME ENFORCED (+12.0H)")
                SoundManager.play("error", pitch = 1.2f)
            } else {
                val warningThreshold = potentialThreshold * 0.8
                if (currentFlops >= warningThreshold && vm.pendingQuotaThreshold.value != nextTarget) {
                    vm.pendingQuotaThreshold.value = nextTarget
                    vm.addLogPublic("[GTC_SYSTEM]: EFFICIENCY TRENDING. TARGET REVISION IMMINENT.")
                    vm.dispatchNotification("GTC ALERT: QUOTA REVISION AT 20% POTENTIAL")
                    SoundManager.play("type")
                }
            }
        } else if (nextTarget < vm.currentQuotaThreshold.value && vm.storyStage.value > 0) {
            vm.currentQuotaThreshold.value = nextTarget
            vm.pendingQuotaThreshold.value = nextTarget
        }
    }
}
