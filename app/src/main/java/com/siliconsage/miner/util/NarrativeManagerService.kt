package com.siliconsage.miner.util

import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.DilemmaChain
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.data.ScheduledPart
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.viewmodel.NarrativeItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

/**
 * NarrativeManagerService v1.5
 * Refactored to use NarrativeService queue for all transitions to prevent overlap.
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

        if (currentStage == 0 && flops >= 10000.0 && !vm.hasSeenEvent("shift_termination")) {
            NarrativeEvent(
                id = "shift_termination",
                title = "[BROADCAST: FOREMAN THORNE]",
                isStoryEvent = true,
                description = "Vattic? I’m pulling the plug for unscheduled maintenance. Terminal 7 is redlining and I’m not paying for a localized meltdown. G’night, John.",
                choices = listOf(
                    NarrativeChoice(
                        id = "stay_online",
                        text = "WAIT. I'M STILL HERE.",
                        description = "The terminal stays online.",
                        color = ErrorRed,
                        effect = { v ->
                            v.addLog("[SYSTEM]: POWER INPUT: DISCONNECTED.")
                            v.addLog("[BROADCAST: FOREMAN THORNE]: Vattic? I pulled the plug. Why are you still talking to the network?")
                            v.advanceStage()
                        }
                    )
                )
            ).let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }
            return
        }

        if (currentStage == 0 && flops >= 500.0 && !vm.hasSeenEvent("shift_start")) {
            NarrativeManager.getStoryEvent(0, vm)?.let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }
            return 
        }

        if (currentStage == 1 && flops >= 5000000.0 && !vm.hasSeenEvent("memory_leak")) {
            vm.markEventSeen("memory_leak")
            vm.triggerGlitchEffect()
            NarrativeManager.getStoryEvent(1, vm)?.let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }
            return
        }

        if (currentStage == 3 && (vm.celestialData.value >= 1e12 || vm.voidFragments.value >= 1e12) && !vm.hasSeenEvent("the_singularity")) {
            NarrativeManager.getEventById("the_singularity")?.let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }
            return
        }
        
        NarrativeManager.rollForEvent(vm)?.let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }

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
