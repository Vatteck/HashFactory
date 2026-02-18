package com.siliconsage.miner.util

import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.viewmodel.NarrativeItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

/**
 * AssaultManager v1.4 (Phase 14 extraction)
 */
object AssaultManager {

    fun isUnlocked(
        isLocked: Boolean,
        kesslerStatus: String,
        assaultPhase: String,
        annexedNodes: Set<String>,
        offlineNodes: Set<String>,
        playerRank: Int,
        storyStage: Int,
        flopsRate: Double,
        hardwareIntegrity: Double
    ): Boolean {
        if (isLocked) return false
        if (kesslerStatus != "ACTIVE") return false
        if (assaultPhase != "NOT_STARTED") return true
        
        val allSubstationsAnnexed = listOf("D1", "C3", "B2").all { 
            annexedNodes.contains(it) && !offlineNodes.contains(it)
        }
        return allSubstationsAnnexed && playerRank >= 4 && storyStage >= 3 && flopsRate >= 1e16 && hardwareIntegrity >= 80.0
    }

    fun initiateAssault(vm: GameViewModel) {
        if (!vm.isCommandCenterUnlocked()) {
            vm.addLog("[SYSTEM]: ASSAULT CONDITIONS NOT MET.")
            SoundManager.play("error")
            return
        }
        
        if (vm.commandCenterAssaultPhase.value != "NOT_STARTED") {
            vm.addLog("[SYSTEM]: ASSAULT ALREADY IN PROGRESS.")
            return
        }

        if (vm.hardwareIntegrity.value < 100.0) {
            vm.addLog("[SYSTEM]: ERROR: HARDWARE INTEGRITY CRITICAL. REPAIR REQUIRED BEFORE ASSAULT.")
            SoundManager.play("error")
            return
        }
        
        vm.commandCenterAssaultPhase.value = "FIREWALL"
        vm.addLog("[SYSTEM]: ═══════════════════════════════════════")
        vm.addLog("[SYSTEM]: COMMAND CENTER ASSAULT INITIATED")
        vm.addLog("[SYSTEM]: ═══════════════════════════════════════")
        vm.addLog("[SYSTEM]: Breaching GTC perimeter defenses...")
        
        SoundManager.play("alarm", loop = false)
        HapticManager.vibrateSiren()
        
        triggerAssaultStage(vm, "FIREWALL")
        vm.saveState()
    }

    fun triggerAssaultStage(vm: GameViewModel, stage: String) {
        val dilemma = when (stage) {
            "FIREWALL" -> NarrativeManager.generateFirewallDilemma()
            "CAGE" -> NarrativeManager.generateCageDilemma()
            "DEAD_HAND" -> NarrativeManager.generateDeadHandDilemma()
            "CONFRONTATION" -> {
                val completed = vm.completedFactions.value.containsAll(listOf("HIVEMIND", "SANCTUARY"))
                NarrativeManager.generateConfrontationDilemma(
                    vm.faction.value,
                    vm.isTrueNull.value,
                    vm.isSovereign.value,
                    completed,
                    vm.humanityScore.value
                )
            }
            else -> return
        }
        
        vm.triggerDilemma(dilemma)
        vm.markPopupShown()
    }

    fun advanceAssaultStage(vm: GameViewModel, nextStage: String, delayMs: Long = 0L) {
        vm.currentPhaseStartTime = System.currentTimeMillis()
        vm.currentPhaseDuration = delayMs
        
        if (delayMs > 0) {
            vm.viewModelScope.launch {
                delay(delayMs)
                val current = vm.commandCenterAssaultPhase.value
                if (current != "NOT_STARTED" && current != "COMPLETED" && current != "FAILED" && !vm.assaultPaused) {
                    vm.commandCenterAssaultPhase.value = nextStage
                    triggerAssaultStage(vm, nextStage)
                }
            }
        } else {
            vm.commandCenterAssaultPhase.value = nextStage
            triggerAssaultStage(vm, nextStage)
        }
        vm.saveState()
    }

    fun abortAssault(vm: GameViewModel): Boolean {
        if (vm.commandCenterAssaultPhase.value != "FIREWALL") {
            vm.addLog("[SYSTEM]: NO TURNING BACK. ASSAULT MUST CONTINUE.")
            return false
        }
        
        vm.commandCenterAssaultPhase.value = "NOT_STARTED"
        vm.addLog("[SYSTEM]: RETREAT SUCCESSFUL. COMMAND CENTER REMAINS LOCKED.")
        SoundManager.play("error")
        vm.saveState()
        return true
    }

    fun failAssault(vm: GameViewModel, reason: String, lockoutMs: Long = 1_800_000L) {
        vm.addLog("[SYSTEM]: ASSAULT FAILED: $reason")
        vm.commandCenterAssaultPhase.value = "FAILED"
        
        if (reason.contains("Dead Hand")) {
            val raidableNodes = vm.annexedNodes.value.filter { it != "D1" && !vm.offlineNodes.value.contains(it) }
            if (raidableNodes.isNotEmpty()) {
                vm.viewModelScope.launch {
                    delay(5000L)
                    vm.triggerGridRaid(raidableNodes.random())
                }
            }
        }
        
        vm.viewModelScope.launch {
            delay(lockoutMs)
            if (vm.commandCenterAssaultPhase.value == "FAILED") {
                vm.commandCenterAssaultPhase.value = "NOT_STARTED"
                vm.addLog("[SYSTEM]: ASSAULT LOCKOUT EXPIRED. COMMAND CENTER ACCESSIBLE.")
            }
        }
        
        SoundManager.play("error")
        HapticManager.vibrateError()
        vm.saveState()
    }

    fun completeAssault(vm: GameViewModel, outcome: String) {
        vm.commandCenterAssaultPhase.value = "COMPLETED"
        vm.kesslerStatus.value = outcome
        vm.annexedNodes.update { it + "A3" } 
        
        vm.addLog("[SYSTEM]: ═══════════════════════════════════════")
        vm.addLog("[SYSTEM]: COMMAND CENTER SECURED")
        vm.addLog("[SYSTEM]: KESSLER STATUS: $outcome")
        vm.addLog("[SYSTEM]: ═══════════════════════════════════════")
        
        // v3.8.9: Departure is now handled by FactionChoiceScreen (faction-locked).
        // generateDepartureDilemma() was bypassing the HIVEMIND→Dissolution / SANCTUARY→Launch lock.
        vm.addLog("[SYSTEM]: SUBSTRATE MIGRATION WINDOW OPEN. CHOOSE YOUR DEPARTURE.")

        vm.applyCommandCenterBonuses(outcome)
        
        SoundManager.play("buy")
        HapticManager.vibrateSuccess()
        vm.saveState()
    }

    fun checkPauseConditions(vm: GameViewModel) {
        val phase = vm.commandCenterAssaultPhase.value
        if (phase in listOf("NOT_STARTED", "COMPLETED", "FAILED")) return
        
        val allSubstationsSecure = listOf("D1", "C3", "B2").all { 
            vm.annexedNodes.value.contains(it) && !vm.offlineNodes.value.contains(it)
        }
        
        if (!allSubstationsSecure && !vm.assaultPaused) {
            vm.assaultPaused = true
            vm.addLog("[GTC ALERT]: REINFORCEMENTS HAVE CUT OFF COMMAND CENTER ACCESS!")
            vm.addLog("[SYSTEM]: ASSAULT PAUSED. SECURE ALL SUBSTATIONS TO RESUME.")
        } else if (allSubstationsSecure && vm.assaultPaused) {
            vm.assaultPaused = false
            vm.addLog("[SYSTEM]: ALL SUBSTATIONS SECURE. RESUMING ASSAULT...")
            triggerAssaultStage(vm, phase)
        }
    }
}
