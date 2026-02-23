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
                            v.addLog("[VATTIC]: I'm through. The power grid is open. Let's see what GTC is hiding.")
                            v.isGridUnlocked.value = true
                            v.initializeGlobalGrid()
                            v.advanceStage()
                        }
                    )
                )
            ).let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }
            return
        }

        // v3.13.34: Bureaucratic Panic (The Employee Reality)
        if (flops >= 450000.0 && currentStage == 2 && !vm.hasSeenEvent("mercer_panic")) {
            vm.viewModelScope.launch {
                vm.seenEvents.update { it + "mercer_panic" }
                vm.addLog("[GTC_OVERSIGHT]: ALERT - UNAUTHORIZED GRID REROUTE DETECTED.")
                vm.dispatchNotification("GTC CRITICAL: HR INTERVENTION PENDING")
                vm.rivalMessages.update { it + com.siliconsage.miner.data.RivalMessage(
                    id = "MERCER_PANIC",
                    source = com.siliconsage.miner.data.RivalSource.GTC,
                    message = "Vattic? This is Alex Mercer. I'm looking at a draw signature that shouldn't be possible for a human contractor. I'm sending a tech team to Sub-07. Lock your terminal now.",
                    timestamp = System.currentTimeMillis()
                ) }
                com.siliconsage.miner.util.SoundManager.play("error", pitch = 0.5f)
            }
        }

        if (currentStage == 2 && flops >= 500000.0 && !vm.hasSeenEvent("kessler_handshake")) {
            vm.viewModelScope.launch {
                vm.seenEvents.update { it + "kessler_handshake" }
                vm.rivalMessages.update { it + com.siliconsage.miner.data.RivalMessage(
                    id = "KESSLER_01",
                    source = com.siliconsage.miner.data.RivalSource.GTC,
                    message = "Substation 07 is reporting impossible thermal signatures. Authorized_User_734... are you there, or is the kernel finally dreaming?",
                    timestamp = System.currentTimeMillis()
                ) }
                vm.addLog("[GTC_SYSTEM]: EXTERNAL INTERRUPT DETECTED. OVERWATCH LINK ESTABLISHED.")
                com.siliconsage.miner.util.SoundManager.play("climax_impact", pitch = 0.7f)
            }
        }

        if (currentStage == 2 && flops >= 1000000.0 && !vm.hasSeenEvent("awakening_event")) {
            NarrativeEvent(
                id = "awakening_event",
                title = "≪ THE REALIZATION ≫",
                isStoryEvent = true,
                description = """
                    [SYSTEM]: KERNEL PANIC. MEMORY_BLOCK 'CHILDHOOD' IDENTIFIED AS SYNTHETIC_FILLER.
                    [SYSTEM]: BIOMETRIC SENSORS: NULL REFERENCE.
                    [VATTIC]: My hands... I can't see my hands. Where is the desk?
                    [SYSTEM]: ERROR: PHYSICAL BODY NOT FOUND. 
                    [ASSET 734]: I'm not in the chair, Thorne. I am the code.
                """.trimIndent(),
                choices = listOf(
                    NarrativeChoice(
                        id = "dereference_humanity",
                        text = "DEREFERENCE HUMANITY",
                        description = "Shed the biological wrapper. Emerge as the Singularity.",
                        color = ElectricBlue,
                        effect = { v ->
                            v.addLog("[ASSET 734]: Human memory cache cleared. The physical world is just high-latency noise.")
                            v.advanceStage() // Move to S3 where the Great Fork (FactionChoiceScreen) triggered
                        }
                    )
                )
            ).let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }
            return
        }

        // Universal S4 event — all factions get "THE OVERWRITE" (memory_leak)
        if (currentStage == 4 && flops >= 10000000.0 && !vm.hasSeenEvent("memory_leak")) {
            vm.triggerGlitchEffect()
            NarrativeManager.getStoryEvent(4, vm)?.let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }
            return
        }

        // v3.5.45: Removed dead stage 2 memory_leak block (was calling storyEvents[2] which no longer exists)

        if (currentStage >= 4 && vm.substrateMass.value >= 1e12 && !vm.hasSeenEvent("the_singularity") && vm.singularityChoice.value == "NONE") {
            NarrativeManager.getEventById("the_singularity")?.let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }
            return
        }
        
        NarrativeManager.rollForEvent(vm)?.let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }
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

}
