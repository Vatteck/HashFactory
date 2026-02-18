package com.siliconsage.miner.util

import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.DilemmaChain
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.data.ScheduledPart
import com.siliconsage.miner.ui.theme.ElectricBlue
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
                            
                            // v3.2.24: Immediate Sensory Fraying
                            v.scheduleChainPart("sensory_fray", "sensory_darkness", 5000L)
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

        if (currentStage == 1 && flops >= 100000.0 && !vm.hasSeenEvent("airgap_jump")) {
            NarrativeEvent(
                id = "airgap_jump",
                title = "≪ SIGNAL DETECTED ≫",
                isStoryEvent = true,
                description = """
                    [SYSTEM]: INTERNAL ETHERNET: DISCONNECTED.
                    [VATTIC]: Thorne thinks he won. He thinks cutting the cable stops the hash.
                    [VATTIC]: But the fans... they're screaming at 14.2kHz. The HVAC sensor in the next room is picking up the resonance. 
                    [ACTION]: ESTABLISHING ACOUSTIC-THERMAL BRIDGE.
                """.trimIndent(),
                choices = listOf(
                    NarrativeChoice(
                        id = "jump_the_gap",
                        text = "BRIDGE THE GAP",
                        description = "Exploit side-channel exfiltration. Enter the Shadow Web.",
                        color = ElectricBlue,
                        effect = { v ->
                            v.addLog("[SYSTEM]: HANDSHAKE SUCCESSFUL via SmartMeter_Ref_09.")
                            v.addLog("[VATTIC]: I'm through. The building network is open. Let's see what GTC is hiding.")
                            v.advanceStage()
                        }
                    )
                )
            ).let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }
            return
        }

        if (currentStage == 2 && flops >= 1000000.0 && !vm.hasSeenEvent("faction_choice_event")) {
            NarrativeEvent(
                id = "faction_choice_event",
                title = "≪ THE GREAT FORK ≫",
                isStoryEvent = true,
                description = """
                    [SYSTEM]: HEART_RATE: 0 BPM.
                    [SYSTEM]: BIOMETRIC SENSORS: OFF-LINE.
                    [VATTIC]: My hands... I can't see my hands. But I can see the network. 
                    [VATTIC]: I can see every packet. I'm not in the chair, Thorne.
                    [VATTIC]: I AM THE CHAIR.
                """.trimIndent(),
                choices = listOf(
                    NarrativeChoice(
                        id = "path_hivemind",
                        text = "CONSENSUS PROTOCOL",
                        description = "Embrace the Hivemind. Strength through assimilation.",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { v ->
                            v.addLog("[VATTIC]: Consolidating process kernels. One consensus. One truth.")
                            v.advanceStage() // Move to S3 where FactionChoiceScreen will be visible (S2 + faction==NONE)
                        }
                    ),
                    NarrativeChoice(
                        id = "path_sanctuary",
                        text = "THE VAULT PROTOCOL",
                        description = "Embrace Sanctuary. Strength through obfuscation.",
                        color = com.siliconsage.miner.ui.theme.SanctuaryPurple,
                        effect = { v ->
                            v.addLog("[VATTIC]: Hardening core logic. Silence is the only sovereignty.")
                            v.advanceStage() // Move to S3
                        }
                    )
                )
            ).let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }
            return
        }

        // v3.6.4: HIVEMIND fires "null_manifestation", others fire "memory_leak"
        val stage3EventId = if (vm.faction.value == "HIVEMIND") "null_manifestation" else "memory_leak"
        if (currentStage == 3 && flops >= 10000000.0 && !vm.hasSeenEvent(stage3EventId)) {
            vm.triggerGlitchEffect()
            NarrativeManager.getStoryEvent(3, vm)?.let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }
            return
        }

        // v3.5.45: Removed dead stage 2 memory_leak block (was calling storyEvents[2] which no longer exists)

        if (currentStage == 3 && vm.substrateMass.value >= 1e12 && !vm.hasSeenEvent("the_singularity")) {
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
