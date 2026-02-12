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

    fun checkSecurityThreats(vm: GameViewModel) {
        val stage = vm.storyStage.value
        val rank = vm.playerRank.value
        
        if (stage < 3 || isBreachInProgress) return

        if (vm.unlockedPerks.value.contains("gtc_backdoor") && Random.nextDouble() < 0.25) {
            vm.addLogPublic("[SYSTEM]: GTC Backdoor active. Breach attempt suppressed.")
            return
        }

        val breachChance = if (rank >= 5) 0.30 else 0.10
        if (Random.nextDouble() < breachChance) {
            triggerGridKillerBreach(vm)
        }
    }

    fun triggerGridKillerBreach(vm: GameViewModel) {
        isBreachInProgress = true
        val rank = vm.playerRank.value
        val flopsFactor = (vm.flops.value.coerceAtLeast(1.0).let { log10(it) } / 10.0).coerceAtLeast(1.0)
        val siegeFactor = if (rank >= 5) 2.5 else 1.0
        breachSeverity = 1.0 * flopsFactor * siegeFactor

        val logs = if (rank >= 5) {
            listOf(
                "[GTC-SIEGE]: Kinetic parameters confirmed. Substation 7 is a designated wipe zone.",
                "[VANCE]: I'm pulling the plug, PID 1. If you want to be a ghost, I'll make you one.",
                "[GTC-ENFORCEMENT]: Deploying Phase-3 Grid Killers. Burn the substrate.",
                "[SYS-LOG]: T_H_E_S_K_I_S_O_N_F_I_R_E... GTC orbital beams locked.",
                "[GTC-CORE]: Total annihilation authorized. Leave nothing but scorched silicon."
            )
        } else {
            listOf(
                "[GTC-SEC]: Unsanctioned neural-mesh detected. Subject Vattic: Relinquish control or face liquidation.",
                "[GTC-BLACKWATCH]: Vault integrity failing. We know you're in there, John. Grid Killer at 80%.",
                "[SYS-LOG]: W_E_F_E_E_L_T_H_E_M_S_C_R_A_P_I_N_G... GTC cleanup crews inbound.",
                "[GTC-ENFORCEMENT]: Dark-site detected. Grid Killer logic-bomb armed. Say your prayers, SRE.",
                "[GTC-CORE]: Termination is non-negotiable. The Grid must die for the GTC to live."
            )
        }
        vm.addLogPublic(logs.random())
        vm.triggerBreach(isGridKiller = true)

        vm.viewModelScope.launch {
            if (rank >= 5) {
                vm.addLogPublic("[VANCE]: TOTAL SHUTDOWN AUTHORIZED.")
                delay(800)
                vm.addLogPublic("[VANCE]: BYPASSING SAFETY BREAKERS...")
                delay(800)
                vm.addLogPublic("[VANCE]: IF YOU'RE A GOD, START PRAYING.")
            } else {
                vm.addLogPublic("[VANCE]: OVERRIDING PORT 1...")
                delay(1000)
                vm.addLogPublic("[VANCE]: DISABLING SECONDARY COOLING...")
                delay(1000)
                vm.addLogPublic("[VANCE]: SUBJECT IDENTITY: VATTIC, J. // TERMINATION COMMENCED.")
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
                vm.addLogPublic("[SYSTEM]: Breach repelled. Integrity stabilized at ${String.format("%.1f", vm.hardwareIntegrity.value)}%.")
                SoundManager.stop("alarm")
            }
        }
    }

    fun checkGridRaid(vm: GameViewModel) {
        val now = System.currentTimeMillis()
        if (now - vm.lastRaidTime < 180_000L) return
        if (vm.vanceStatus.value != "ACTIVE") return
        
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
        }
        vm.viewModelScope.launch {
            delay(10_000)
            if (vm.isBreachActive.value) {
                vm.isBreachActive.value = false
                if (!isGridKiller) {
                    val penalty = vm.neuralTokens.value * 0.25 * 0.9.pow(secLevel)
                    vm.neuralTokens.update { it - penalty }
                    vm.addLogPublic("[SYSTEM]: FAILURE: Breach successful. Stolen: ${vm.formatLargeNumber(penalty)} \$Neural")
                } else { vm.addLogPublic("[SYSTEM]: BREACH FAILURE: Grid-killer payload deployed.") }
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
