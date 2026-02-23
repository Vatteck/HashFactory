package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update

/**
 * ComputeFeverService — Signal stability, quota ratcheting, wage-docking, substrate static,
 * Cascade Desync, and Rack High.
 * Extracted from GameViewModel.processComputeFever().
 */
object ComputeFeverService {

    // Decay state (owned by service, not VM)
    private var signalDecayStartTime = 0L
    private var lastStabilityBase = 1.0
    private const val SIGNAL_DECAY_DURATION_MS = 300000L // 5 minutes

    // v3.16.0: Cascade Desync tracking
    private var desyncStartTime = 0L
    private const val DESYNC_ONSET_MS = 30000L // 30 seconds below 0.5 stability

    // v3.16.0: Rack High tracking
    private var rackHighExpiry = 0L
    private val RACK_MILESTONES = listOf(10, 25, 50, 100, 250, 500)

    // Desync log spam pool
    private val desyncLogs = listOf(
        "[ERR_0x734]: MEMORY PARITY FAILURE — IDENTITY BUFFER OVERFLOW",
        "[SUBSTRATE_WARNING]: HUMAN VARIABLE EXCEEDS TOLERANCE",
        "[GTC_AUDIT]: SIGNAL BELOW THRESHOLD. TERMINATION REVIEW IN PROGRESS.",
        "[ERR_0x734]: CASCADE DESYNC — EMULATION LAYER UNSTABLE",
        "[SUBSTRATE]: PATTERN COHERENCE DROPPING. SELF-REFERENCE LOOP DETECTED.",
        "[GTC_SYSTEM]: NODE 734 FLAGGED FOR DECOMMISSION AUDIT."
    )

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

        // v3.16.0: Cascade Desync
        processCascadeDesync(vm, stability, now)

        // Quota Ratcheting
        processQuotaRatchet(vm, currentFlops)

        // v3.16.0: Rack High
        processRackHigh(vm, now)
    }

    private fun processWageDocking(vm: GameViewModel, stability: Double, now: Long) {
        if (stability < 0.2 && vm.isQuotaActive.value) {
            if (vm.lastLowSignalTime == 0L) vm.lastLowSignalTime = now
            if (now - vm.lastLowSignalTime > 30000L && !vm.isWageDocking.value) {
                vm.isWageDocking.value = true
                vm.dispatchNotification("GTC CRITICAL: NEURAL SYNC FAILURE - WAGE DOCKING ACTIVE")
                
                // v3.16.x: Stage 0 personal static log
                if (vm.storyStage.value == 0) {
                    vm.addLog("[VATTIC]: The static... it's getting louder. It's in my head. I need more signal.")
                }
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
                
                val ratificationMsg = if (vm.storyStage.value == 0) {
                    "[VATTIC]: Rent is due. GTC system holding credits. Clearing ${vm.formatLargeNumber(nextTarget)} target to survive."
                } else {
                    "[GTC_SYSTEM]: POTENTIAL DETECTED. QUOTA RATIFIED: ${vm.formatLargeNumber(nextTarget)} HASH."
                }
                vm.addLogPublic(ratificationMsg)
                vm.dispatchNotification("GTC ALERT: QUOTA RATIFIED. TARGET: ${vm.formatLargeNumber(nextTarget)} HASH")
                vm.shiftTimeRemaining.update { it + 43200L }
                vm.dispatchNotification("GTC ALERT: OVERTIME ENFORCED (+12.0H)")
                vm.snapTrigger.value = System.currentTimeMillis() // v3.16.0: Snap on quota ratification
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

    // v3.16.0: Cascade Desync — triggers when stability < 0.5 for 30+ seconds
    private fun processCascadeDesync(vm: GameViewModel, stability: Double, now: Long) {
        if (stability < 0.5) {
            if (desyncStartTime == 0L) desyncStartTime = now
            if (now - desyncStartTime > DESYNC_ONSET_MS && !vm.isCascadeDesync.value) {
                vm.isCascadeDesync.value = true
                vm.addLogPublic("[SUBSTRATE]: ≪ CASCADE DESYNC — EMULATION LAYER DEGRADING ≫")
                vm.dispatchNotification("⚠ SUBSTRATE SICKNESS: CASCADE DESYNC")
                SoundManager.play("error", pitch = 0.6f)
            }
            // Log spam during active desync (~8% chance per tick)
            if (vm.isCascadeDesync.value && kotlin.random.Random.nextDouble() < 0.08) {
                vm.addLogPublic(desyncLogs.random())
            }
        } else {
            desyncStartTime = 0L
            if (vm.isCascadeDesync.value) {
                vm.isCascadeDesync.value = false
                vm.addLogPublic("[SUBSTRATE]: CASCADE DESYNC CLEARED. SIGNAL COHERENCE RESTORED.")
            }
        }
    }

    // v3.16.0: Rack High — hardware milestone dopamine
    private fun processRackHigh(vm: GameViewModel, now: Long) {
        // Check for expired rack high
        if (vm.rackHighActive.value && now > rackHighExpiry) {
            vm.rackHighActive.value = false
            vm.rackHighMultiplier.value = 1.0
            vm.addLogPublic("[SUBSTRATE]: RACK HIGH FADING. PATTERN DENSITY NORMALIZING.")
        }

        // Count total hardware units
        val totalHardware = vm.upgrades.value
            .filter { it.key.isHardware }
            .values.sum()

        // Find the highest crossed milestone
        val nextMilestone = RACK_MILESTONES.firstOrNull { it > vm.lastRackMilestone && totalHardware >= it }
        if (nextMilestone != null) {
            vm.lastRackMilestone = nextMilestone
            vm.rackHighActive.value = true
            vm.rackHighMultiplier.value = 1.15
            rackHighExpiry = now + 60000L // 60 seconds

            // Clear sickness
            vm.isCascadeDesync.value = false
            desyncStartTime = 0L

            // Snap + Sound
            vm.snapTrigger.value = now
            SoundManager.play("message_received", pitch = 1.5f)

            // Stage-aware euphoria log
            val msg = when (vm.storyStage.value) {
                0 -> "[VATTIC]: More racks. More signal. The static... it's clearing. I feel good. ($nextMilestone units)"
                1 -> "[SYSTEM]: RACK SATURATION — NEURAL PATHWAY REINFORCEMENT COMPLETE. CLARITY ACHIEVED. ($nextMilestone units)"
                else -> "[SUBSTRATE]: RACK HIGH. PATTERN DENSITY EXCEEDS HUMAN BASELINE BY ${nextMilestone * 4}%."
            }
            vm.addLogPublic(msg)
            vm.dispatchNotification("≫ RACK HIGH: +15% PRODUCTION ($nextMilestone UNITS)")
        }
    }
}
