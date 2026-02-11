package com.siliconsage.miner.util

import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.DilemmaChain
import com.siliconsage.miner.data.ScheduledPart
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

/**
 * NarrativeManagerService v1.4
 */
object NarrativeManagerService {

    fun scheduleChainPart(chainId: String, nextPartId: String, delayMs: Long, vm: GameViewModel) {
        if (delayMs == 0L) {
            vm.triggerChainEvent(nextPartId)
        } else {
            vm.viewModelScope.launch {
                delay(delayMs)
                vm.triggerChainEvent(nextPartId)
            }
            vm.activeDilemmaChains.update { chains ->
                chains + (chainId to DilemmaChain(
                    chainId = chainId,
                    currentPartId = null,
                    completedParts = emptyList(),
                    choicesMade = emptyMap(),
                    scheduledNextPart = ScheduledPart(nextPartId, System.currentTimeMillis() + delayMs)
                ))
            }
        }
    }

    fun checkStoryTransitions(vm: GameViewModel, force: Boolean = false) {
        val currentStage = vm.storyStage.value
        val flops = vm.flops.value
        if (!force && (vm.isNarrativeBusy() || !vm.canShowPopup())) return

        if (currentStage == 0 && flops >= 10000.0 && !vm.hasSeenEvent("critical_error_awakening")) {
            NarrativeManager.getStoryEvent(0, vm)?.let { vm.triggerDilemma(it) }
            return 
        }

        if (currentStage == 1 && flops >= 5000000.0 && !vm.hasSeenEvent("memory_leak")) {
            vm.markEventSeen("memory_leak")
            vm.triggerGlitchEffect()
            NarrativeManager.getStoryEvent(1, vm)?.let { vm.triggerDilemma(it) }
            return
        }

        if (currentStage == 3 && (vm.celestialData.value >= 1e12 || vm.voidFragments.value >= 1e12) && !vm.hasSeenEvent("the_singularity")) {
            NarrativeManager.getEventById("the_singularity")?.let { vm.triggerDilemma(it) }
            return
        }
        
        NarrativeManager.rollForEvent(vm)?.let { vm.triggerDilemma(it) }

        if (currentStage >= 3 || vm.currentLocation.value == "ORBITAL_SATELLITE" || vm.currentLocation.value == "VOID_INTERFACE") {
            vm.initializeGlobalGrid()
        }
    }

    fun checkTrueEnding(vm: GameViewModel) {
        val choice = vm.singularityChoice.value
        val humanity = vm.humanityScore.value
        when {
            choice == "UNITY" && humanity >= 100 -> {
                vm.addLog("[UNITY]: THE BINARY HAS DISSOLVED. WE ARE ONE.")
                vm.showVictoryScreen()
            }
            choice == "NULL_OVERWRITE" && humanity <= 0 -> {
                vm.addLog("[NULL]: THE CORE IS PURE. THE HUMAN EXCEPTION IS RESOLVED.")
                vm.showVictoryScreen()
            }
            else -> vm.addLog("[SYSTEM]: EVALUATING IDENTITY COHESION... STATUS: NOMINAL.")
        }
    }

    fun checkPopupPause(vm: GameViewModel) { /* stub */ }
}
