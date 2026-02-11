package com.siliconsage.miner.util

import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.DilemmaChain
import com.siliconsage.miner.data.ScheduledPart
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

/**
 * NarrativeManagerService v1.0 (Phase 14 extraction)
 * Handles dilemma chain scheduling, story transitions, and event marking.
 */
object NarrativeManagerService {

    /**
     * Schedule a chain event part to trigger after a delay
     */
    fun scheduleChainPart(
        chainId: String,
        nextPartId: String,
        delayMs: Long,
        vm: GameViewModel
    ) {
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

    /**
     * Check for any story transitions based on current progress
     */
    fun checkStoryTransitions(vm: GameViewModel, force: Boolean = false) {
        val currentStage = vm.storyStage.value
        val flops = vm.flops.value

        if (!force && (vm.isNarrativeBusy() || !vm.canShowPopup())) return // Respect queue cooldown

        // Stage 0 -> 1: The Awakening (10,000 FLOPS)
        if (currentStage == 0 && flops >= 10000.0 && !vm.hasSeenEvent("critical_error_awakening")) {
            NarrativeManager.getStoryEvent(0, vm)?.let { event ->
                vm.triggerDilemma(event)
            }
            return 
        }

        // Stage 1 -> 2: The Memory Leak (5,000,000 FLOPS)
        if (currentStage == 1 && flops >= 5000000.0 && !vm.hasSeenEvent("memory_leak")) {
            vm.markEventSeen("memory_leak")
            vm.triggerGlitchEffect()
            
            NarrativeManager.getStoryEvent(1, vm)?.let { event ->
                vm.triggerDilemma(event)
            }
            return
        }

        // Stage 2 -> 3: The Command Center (Assault Completion handled in AssaultManager)
        
        // Stage 3: Singularity Priming (1.0T resources)
        if (currentStage == 3 && (vm.celestialData.value >= 1e12 || vm.voidFragments.value >= 1e12) && !vm.hasSeenEvent("the_singularity")) {
            NarrativeManager.getEventById("the_singularity")?.let { event ->
                vm.triggerDilemma(event)
            }
            return
        }
        
        // Fallback to random/faction events if no story transition is pending
        NarrativeManager.rollForEvent(vm)?.let { event ->
            vm.triggerDilemma(event)
        }

        if (currentStage >= 3 || vm.currentLocation.value == "ORBITAL_SATELLITE" || vm.currentLocation.value == "VOID_INTERFACE") {
            vm.initializeGlobalGrid()
        }
    }
}
