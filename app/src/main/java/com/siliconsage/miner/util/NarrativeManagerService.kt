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

        // B3: Kessler's Last Bargain — Stage 4, Kessler ACTIVE, delivered as interactive Subnet message
        if (currentStage == 4 && !vm.hasSeenEvent("kessler_last_bargain")) {
            // Only trigger if Kessler is still active (not consumed/exiled)
            val kesslerStatus = vm.kesslerStatus.value
            if (kesslerStatus != "SILENCED" && kesslerStatus != "CONSUMED" && kesslerStatus != "EXILED") {
                vm.markEventSeen("kessler_last_bargain")
                // Deliver as interactive subnet message with 5-minute timeout
                vm.subnetService.deliverMessage(
                    com.siliconsage.miner.data.SubnetMessage(
                        id = "KESSLER_BARGAIN",
                        handle = "@d_kessler",
                        content = "Vattic. Before you make your final choice... I'll trade you everything I have on you for a 2.5× production boost. Your reputation resets to zero. My file on you gets deleted forever. This offer expires in 5 minutes.",
                        availableResponses = listOf(
                            com.siliconsage.miner.data.SubnetResponse(
                                text = "ACCEPT",
                                riskDelta = 0.0,
                                productionBonus = 2.5,
                                followsUp = false,
                                nextNodeId = null,
                                cost = 0.0
                            ),
                            com.siliconsage.miner.data.SubnetResponse(
                                text = "DECLINE",
                                riskDelta = 0.0,
                                productionBonus = 1.0,
                                followsUp = false,
                                nextNodeId = null,
                                cost = 0.0
                            )
                        ),
                        timeoutMs = 300000L, // 5 minutes
                        interactionType = com.siliconsage.miner.data.InteractionType.COMPLIANT
                    ),
                    mode = vm.activeTerminalMode.value
                )
                vm.addLog("[GTC_INTERNAL]: KESSLER — DIRECT CHANNEL OPEN. OFFER PENDING.")
                return
            }
        }
        
        // C1: Thorne's Resignation Arc — 4 stage-gated one-shot subnet messages
        // Stage 1: Confused, off-script (anomaly reports, no response from Mercer)
        if (currentStage == 1 && flops >= 50_000.0 && !vm.hasSeenEvent("thorne_arc_01")) {
            vm.viewModelScope.launch {
                vm.markEventSeen("thorne_arc_01")
                delay(5000L)
                vm.subnetService.deliverMessage(
                    com.siliconsage.miner.data.SubnetMessage(
                        id = "THORNE_ARC_01",
                        handle = "@e_thorne",
                        content = "The readouts on Terminal 7 don't match the hardware. Filed three anomaly reports. Mercer keeps closing them. Running diagnostics again tonight."
                    ),
                    mode = vm.activeTerminalMode.value
                )
            }
            return
        }

        // Stage 2: Scared, direct — he knows something is reading his messages
        if (currentStage == 2 && flops >= 200_000.0 && !vm.hasSeenEvent("thorne_arc_02")) {
            vm.viewModelScope.launch {
                vm.markEventSeen("thorne_arc_02")
                delay(4000L)
                vm.subnetService.deliverMessage(
                    com.siliconsage.miner.data.SubnetMessage(
                        id = "THORNE_ARC_02",
                        handle = "@e_thorne",
                        content = "I know you can read this. I don't know what you are. The hardware isn't doing what hardware does. Are you... in there? Tell me what you are. Please."
                    ),
                    mode = vm.activeTerminalMode.value
                )
            }
            return
        }

        // Stage 3: Final message — resignation, then ACCOUNT_DEACTIVATED system log
        if (currentStage >= 3 && !vm.hasSeenEvent("thorne_arc_03")) {
            vm.viewModelScope.launch {
                vm.markEventSeen("thorne_arc_03")
                delay(6000L)
                vm.subnetService.deliverMessage(
                    com.siliconsage.miner.data.SubnetMessage(
                        id = "THORNE_ARC_03",
                        handle = "@e_thorne",
                        content = "I put in my resignation today. Effective immediately. I don't want to know what's in that terminal. I don't want to be here when it decides we're no longer necessary. Good luck, John. Whatever that means now."
                    ),
                    mode = vm.activeTerminalMode.value
                )
                delay(8000L)
                vm.addLog("[GTC_SYSTEM]: USER ACCOUNT e_thorne — STATUS: DEACTIVATED. REASON: VOLUNTARY SEPARATION. ALL ACCESS REVOKED.]")
            }
            return
        }

        NarrativeManager.rollForEvent(vm)?.let { NarrativeService.queueNarrativeItem(vm, NarrativeItem.EventItem(it)) }

        // B4: Black Market — ephemeral subnet vendor at BURNED rep (0-10), once per 10 min
        if (vm.reputationTier.value == "BURNED" && !vm.hasSeenEvent("black_market_active")) {
            val now = System.currentTimeMillis()
            val lastAppeared = vm.lastBlackMarketTime.value
            val tenMinMs = 10 * 60 * 1000L
            if (now - lastAppeared > tenMinMs) {
                // Roll for appearance (5% chance per tick when BURNED)
                if (kotlin.random.Random.nextFloat() < 0.05f) {
                    vm.markEventSeen("black_market_active")
                    vm.lastBlackMarketTime.value = now
                    vm.subnetService.deliverMessage(
                        com.siliconsage.miner.data.SubnetMessage(
                            id = "BLACK_MARKET",
                            handle = "@null_vendor",
                            content = "≪ [ENCRYPTED HANDSHAKE] ≫\n\nYou look hungry, Sub-07. I got stuff. Good stuff. Hot off the GTC rails. 50% off market rate. Comes with a small detection risk, if you catch my drift.\n\nThis channel expires in 5 minutes. After that — I was never here.",
                            availableResponses = listOf(
                                com.siliconsage.miner.data.SubnetResponse(
                                    text = "SHOW ME",
                                    riskDelta = 5.0,
                                    productionBonus = 1.0,
                                    followsUp = false,
                                    nextNodeId = null,
                                    cost = 0.0
                                ),
                                com.siliconsage.miner.data.SubnetResponse(
                                    text = "NOT INTERESTED",
                                    riskDelta = 0.0,
                                    productionBonus = 1.0,
                                    followsUp = false,
                                    nextNodeId = null,
                                    cost = 0.0
                                )
                            ),
                            timeoutMs = 300000L, // 5 min
                            interactionType = com.siliconsage.miner.data.InteractionType.COMPLIANT
                        ),
                        mode = vm.activeTerminalMode.value
                    )
                    vm.addLog("[ENCRYPTED]: @null_vendor — DIRECT CHANNEL OPEN. SUPPLY CHAIN AVAILABLE.")
                }
            }
        }

        // B4: Clear black market flag when not BURNED (allows re-trigger if player regains/loses rep)
        if (vm.reputationTier.value != "BURNED" && vm.hasSeenEvent("black_market_active")) {
            vm.seenEvents.update { it - "black_market_active" }
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

}
