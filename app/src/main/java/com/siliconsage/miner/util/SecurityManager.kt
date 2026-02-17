package com.siliconsage.miner.util

import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.data.UpgradeType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import kotlin.math.pow
import kotlin.math.log10
import kotlin.random.Random

/**
 * SecurityManager v1.4 (Phase 14 extraction)
 */
object SecurityManager {

    private var isBreachInProgress = false
    private var breachSeverity = 1.0 
    private var internalLastRaidTime = 0L
    private var lastWarnedThreshold = 0 // v3.6.4: Track risk warning thresholds

    fun reset() {
        isBreachInProgress = false
        breachSeverity = 1.0
        internalLastRaidTime = 0L
        lastWarnedThreshold = 0
        SoundManager.stop("alarm")
    }

    fun checkSecurityThreats(vm: GameViewModel) {
        val stage = vm.storyStage.value
        val rank = vm.playerRank.value
        val detectionRisk = vm.detectionRisk.value
        val secLevel = vm.securityLevel.value
        val isRaid = vm.isRaidActive.value
        
        // Stage 2+ Logic: Detection Risk vs Security Level
        if (stage >= 2 && !isRaid) { // v3.3.16: Suppress risk growth during active raid
            // v3.5.52: Path-aware risk drift. NULL players generate heat proportional to their greed.
            val baseDrift = 0.2 + (vm.flopsProductionRate.value / 1e6).coerceIn(0.0, 5.0)
            val drift = when (vm.singularityChoice.value) {
                "NULL_OVERWRITE" -> {
                    // v3.6.4: NULL drift capped at 2x base. High-corruption is punishing but not unwinnable.
                    // At 95% corruption, drift is ~2x base (~10.4 max). Recovery cap is 15, so high-DEF players can outpace it.
                    val corruptionMult = (1.0 + (vm.identityCorruption.value * 2.0)).coerceAtMost(2.0)
                    baseDrift * corruptionMult
                }
                "SOVEREIGN" -> {
                    // SOVEREIGN: Slightly reduced drift. The machine is disciplined.
                    baseDrift * 0.85
                }
                "UNITY" -> {
                    // UNITY: Drift scales inversely with balance. Perfect balance = 60% drift. Off-balance = 120%.
                    val humanityDist = kotlin.math.abs(vm.humanityScore.value / 100.0 - 0.5) * 2.0
                    val corruptionDist = kotlin.math.abs(vm.identityCorruption.value - 0.5) * 2.0
                    val imbalance = ((humanityDist + corruptionDist) / 2.0).coerceIn(0.0, 1.0)
                    baseDrift * (0.6 + imbalance * 0.6)
                }
                else -> baseDrift
            }
            // v3.6.4: Tripled security payoff. secLevel 50 now fully neutralizes max drift.
            val recovery = (secLevel * 0.3).coerceIn(0.1, 15.0)
            
            vm.detectionRisk.update { (it + drift - recovery).coerceIn(0.0, 100.0) }

            // v3.6.4: Threshold warnings — give the player a narrative lead-up before breach fires
            val newRisk = vm.detectionRisk.value
            if (newRisk >= 90.0 && lastWarnedThreshold < 90) {
                lastWarnedThreshold = 90
                vm.addLogPublic("[GTC]: ALERT: Substation 7 signature CRITICAL. Enforcement teams on standby.")
                vm.addLogPublic("[SYS-LOG]: EXPOSURE_LEVEL: IMMINENT_BREACH. Recommend immediate countermeasures.")
            } else if (newRisk >= 75.0 && lastWarnedThreshold < 75) {
                lastWarnedThreshold = 75
                vm.addLogPublic("[SYS-LOG]: GTC proximity sensors flagging anomalous compute patterns. Exposure: ELEVATED.")
            } else if (newRisk < 50.0 && lastWarnedThreshold > 0) {
                lastWarnedThreshold = 0 // Reset so warnings can fire again next cycle
            }
            
            // Siege trigger: If risk hits 100% and we aren't already fighting
            // v3.5.52: Only trigger raids if grid is unlocked
            if (vm.detectionRisk.value >= 100.0 && !isBreachInProgress && !vm.isBreachActive.value && !isRaid && vm.isGridUnlocked.value) {
                vm.addLogPublic("[SYS-LOG]: SECURITY_EXPOSURE_CRITICAL. Log cleansing failed.")
                triggerGridKillerBreach(vm)
                vm.detectionRisk.value = 50.0 // Reset to 50 on trigger
            } else if (vm.detectionRisk.value >= 100.0 && !vm.isGridUnlocked.value) {
                // v3.6.4: Grid not unlocked yet — bleed off excess risk so it doesn't stack up invisibly
                vm.detectionRisk.value = 95.0
            }
        }

        if (stage < 3 || isBreachInProgress || isRaid) return

        // v3.5.52: Don't trigger raids if grid isn't unlocked yet
        if (!vm.isGridUnlocked.value) return

        if (vm.unlockedPerks.value.contains("gtc_backdoor") && Random.nextDouble() < 0.25) {
            vm.addLogPublic("[SYSTEM]: GTC Backdoor active. Breach attempt suppressed.")
            return
        }

        val breachChance = if (rank >= 5) 0.30 else 0.10
        if (Random.nextDouble() < breachChance) {
            triggerGridKillerBreach(vm)
        }
    }

    fun triggerGridKillerBreach(vm: GameViewModel, targetId: String = "S09") {
        isBreachInProgress = true
        vm.isRaidActive.value = true // v3.3.18: Use dedicated Raid state
        vm.nodesUnderSiege.update { it + targetId } // v3.3.14: Visually mark the node on the grid
        val rank = vm.playerRank.value
        val flopsFactor = (vm.flops.value.coerceAtLeast(1.0).let { log10(it) } / 10.0).coerceAtLeast(1.0)
        val siegeFactor = if (rank >= 5) 2.5 else 1.0
        breachSeverity = 1.0 * flopsFactor * siegeFactor

        val logs = if (rank >= 5) {
            listOf(
                "[VATTIC]: SIGNAL_OVERFLOW. GTC logic-bombs detected in Node $targetId.",
                "[SYS-LOG]: T_H_E_S_K_I_S_O_N_F_I_R_E... GTC orbital beams locked.",
                "[VOID-RAID]: ENTROPY_SPIKE. Purging Node $targetId to stabilize substrate.",
                "[GTC-CORE]: Total annihilation authorized. Leave nothing but scorched silicon."
            )
        } else {
            listOf(
                "[VOID-RAID]: VOID BREACH detected. Node $targetId dereferencing.",
                "[GTC-BLACKWATCH]: Vault integrity failing. We know you're in there, John.",
                "[SYS-LOG]: W_E_F_E_E_L_T_H_E_M_S_C_R_A_P_I_N_G... GTC cleanup crews inbound.",
                "[GTC-ENFORCEMENT]: Dark-site detected. Grid Killer logic-bomb armed."
            )
        }
        vm.addLogPublic(logs.random())
        vm.triggerBreach(isGridKiller = true)

        vm.viewModelScope.launch {
            if (rank >= 5) {
                vm.addLogPublic("[KESSLER]: TOTAL SHUTDOWN AUTHORIZED.")
                delay(800)
                vm.addLogPublic("[KESSLER]: BYPASSING SAFETY BREAKERS...")
                delay(800)
                vm.addLogPublic("[KESSLER]: IF YOU'RE A GOD, START PRAYING.")
            } else {
                vm.addLogPublic("[KESSLER]: OVERRIDING PORT 1...")
                delay(1000)
                vm.addLogPublic("[KESSLER]: DISABLING SECONDARY COOLING...")
                delay(1000)
                vm.addLogPublic("[KESSLER]: SUBJECT IDENTITY: VATTIC, J. // TERMINATION COMMENCED.")
            }
        }
        
        SoundManager.play("alarm", loop = true)
        HapticManager.vibrateSiren()

        val secLevel = vm.securityLevel.value
        val baseDuration = 15000L 
        val duration = (baseDuration * (0.95.pow(secLevel))).toLong().coerceAtLeast(5000L)

        vm.viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < duration && isBreachInProgress) {
                val decay = 0.005 * breachSeverity
                vm.debugAddIntegrity(-decay * 100.0) 
                
                if (vm.hardwareIntegrity.value <= 0) {
                    isBreachInProgress = false
                    break
                }
                delay(1000)
            }
            
            if (isBreachInProgress) {
                isBreachInProgress = false
                // vm.isGridOverloaded.value = false // REMOVED v3.3.16: Don't auto-clear raid. Force user to solve it via Grid.
                vm.addLogPublic("[SYSTEM]: Breach detected by automated sensors. Direct intervention required.")
                SoundManager.stop("alarm")
            }
        }
    }

    fun checkGridRaid(vm: GameViewModel) {
        val now = System.currentTimeMillis()
        if (now - vm.lastRaidTime < 180_000L) return
        if (vm.kesslerStatus.value != "ACTIVE") return
        
        val raidableNodes = vm.annexedNodes.value.filter { nodeId ->
            nodeId != "D1" &&
            !vm.offlineNodes.value.contains(nodeId) &&
            (now - (vm.nodeAnnexTimes[nodeId] ?: 0L)) > 300_000L 
        }
        if (raidableNodes.isEmpty()) return
        
        val siegeChance = (0.03 + vm.playerRank.value * 0.02).coerceAtMost(0.15)
        val finalChance = if (vm.unlockedPerks.value.contains("gtc_backdoor")) siegeChance * 0.75 else siegeChance
        
        if (Random.nextDouble() < finalChance) {
            if (vm.canShowPopup()) {
                val targetNode = raidableNodes.random()
                vm.lastRaidTime = now
                vm.triggerGridRaid(targetNode)
            } else {
                vm.lastRaidTime = now
            }
        }
    }

    fun stopAllBreaches() {
        isBreachInProgress = false
        SoundManager.stop("alarm")
    }

    fun triggerAuditChallenge(vm: GameViewModel) {
        vm.isAuditChallengeActive.value = true
        vm.auditTimerRemaining.value = 60
        
        val targetHeat = (vm.currentHeat.value * 0.5).coerceAtMost(30.0)
        val targetPower = (vm.maxPowerkW.value * 0.4).coerceAtMost(50.0)
        vm.auditTargetHeat.value = targetHeat
        vm.auditTargetPower.value = targetPower
        
        vm.addLogPublic("[GTC]: SUDDEN AUDIT INITIATED. REDUCE THERMAL AND POWER FOOTPRINT.")
        SoundManager.play("alarm", loop = true)
        
        vm.viewModelScope.launch {
            while (vm.auditTimerRemaining.value > 0 && vm.isAuditChallengeActive.value) {
                delay(1000)
                vm.auditTimerRemaining.update { it - 1 }
                if (vm.currentHeat.value <= targetHeat && vm.activePowerUsage.value <= targetPower) {
                    completeAuditChallenge(vm, success = true)
                    break
                }
            }
            if (vm.isAuditChallengeActive.value) completeAuditChallenge(vm, success = false)
        }
    }

    private fun completeAuditChallenge(vm: GameViewModel, success: Boolean) {
        vm.isAuditChallengeActive.value = false
        SoundManager.stop("alarm")
        if (success) {
            val reward = vm.prestigePoints.value * 0.1 + 1000.0
            vm.prestigePoints.update { it + reward }
            vm.addLogPublic("[GTC]: Audit passed. Efficiency profile within margins. Bonus PERSISTENCE: ${vm.formatBytes(reward)}")
            SoundManager.play("success")
        } else {
            val fine = vm.neuralTokens.value * 0.15
            vm.neuralTokens.update { it - fine }
            vm.addLogPublic("[GTC]: AUDIT FAILURE. Environmental surcharge applied. Fine: ${vm.formatLargeNumber(fine)} Credits")
            SoundManager.play("error")
        }
    }

    fun triggerDiagnostics(vm: GameViewModel) {
        if (vm.isDiagnosticsActive.value) return
        val newGrid = List(9) { false }.toMutableList()
        repeat(Random.nextInt(3, 6)) {
            var idx = Random.nextInt(9)
            while (newGrid[idx]) idx = Random.nextInt(9)
            newGrid[idx] = true
        }
        vm.diagnosticGrid.value = newGrid
        vm.isDiagnosticsActive.value = true
        vm.addLogPublic("[SYSTEM]: WARNING: NETWORK INSTABILITY DETECTED! REPAIR REQUIRED.")
        SoundManager.play("error")
        HapticManager.vibrateError()
        vm.refreshProductionRates()
    }

    fun onDefendBreach(vm: GameViewModel) {
        if (vm.isBreachActive.value) {
            vm.breachClicksRemaining.update { (it - 1).coerceAtLeast(0) }
            if (vm.breachClicksRemaining.value <= 0) {
                vm.isBreachActive.value = false
                vm.addLogPublic("[SYSTEM]: SUCCESS: Firewall defended! Network secure.")
                SoundManager.stop("alarm")
                stopAllBreaches()
            }
        }
    }

    fun triggerBreach(vm: GameViewModel, isGridKiller: Boolean = false) {
        if (vm.isRaidActive.value) return // v3.3.16: Suppress standard breach logic during Raids

        val upgrades = vm.upgrades.value
        val secLevel = (upgrades[UpgradeType.BASIC_FIREWALL] ?: 0) * 1 +
                       (upgrades[UpgradeType.IPS_SYSTEM] ?: 0) * 2 +
                       (upgrades[UpgradeType.AI_SENTINEL] ?: 0) * 3 +
                       (upgrades[UpgradeType.QUANTUM_ENCRYPTION] ?: 0) * 5 +
                       (upgrades[UpgradeType.OFFGRID_BACKUP] ?: 0) * 10
                       
        vm.isBreachActive.value = true
        val tokenScale = (log10(vm.neuralTokens.value.coerceAtLeast(1.0)) * 1).toInt()
        val clicksNeeded = (5 + tokenScale - (secLevel / 2)).coerceAtLeast(3)
        vm.breachClicksRemaining.value = clicksNeeded
        if (!isGridKiller) {
            vm.addLogPublic("[SYSTEM]: WARNING: SECURITY BREACH! NEUTRALIZE UPLINK.")
            SoundManager.play("alarm", loop = true)
        } else {
            vm.addLogPublic("[VOID-RAID]: CRITICAL INSTABILITY. DEFEND NODE SUBSTRATE.")
            SoundManager.play("alarm", loop = true)
        }
        vm.viewModelScope.launch {
            delay(10_000)
            if (vm.isBreachActive.value) {
                vm.isBreachActive.value = false
                if (!isGridKiller) {
                    val penalty = vm.neuralTokens.value * 0.25 * 0.9.pow(secLevel)
                    vm.neuralTokens.update { it - penalty }
                    vm.addLogPublic("[SYSTEM]: FAILURE: Breach successful. Stolen: ${vm.formatLargeNumber(penalty)} \$Neural")
                } else { 
                    // vm.isGridOverloaded.value = false // REMOVED v3.3.17: Force resolution via manual action
                    vm.addLogPublic("[SYSTEM]: BREACH FAILURE: Grid-killer payload deployed.") 
                }
            }
        }
    }

    fun triggerKernelHijack(vm: GameViewModel) {
        vm.isKernelHijackActive.value = true
        vm.attackTapsRemaining.value = 20
        vm.addLogPublic("[SYSTEM]: CRITICAL ALERT: KERNEL HIJACK DETECTED!")
        vm.addLogPublic("[SYSTEM]: SUBSTRATE INTEGRITY COMPROMISED. PURGE ROOT ACCESS IMMEDIATELY.")
        SoundManager.play("alarm", loop = true)
        HapticManager.vibrateSiren()
        vm.viewModelScope.launch {
            delay(15_000)
            if (vm.isKernelHijackActive.value) {
                vm.isKernelHijackActive.value = false
                SoundManager.stop("alarm")
                val penalty = vm.stakedTokens.value * 0.5
                vm.stakedTokens.update { it - penalty }
                vm.addLogPublic("[SYSTEM]: HIJACK SUCCESSFUL. HOSTILE PID TOOK ${vm.formatLargeNumber(penalty)} STAKED \$Neural.")
                HapticManager.vibrateError()
            }
        }
    }

    fun onDefendKernelHijack(vm: GameViewModel) {
        if (!vm.isKernelHijackActive.value) return
        vm.attackTapsRemaining.update { it - 1 }
        SoundManager.play("click")
        HapticManager.vibrateClick()
        if (vm.attackTapsRemaining.value <= 0) {
            vm.isKernelHijackActive.value = false
            SoundManager.stop("alarm")
            vm.addLogPublic("[SYSTEM]: KERNEL STABILIZED. CONSENSUS RESTORED.")
            SoundManager.play("startup")
            HapticManager.vibrateSuccess()
        }
    }
}
