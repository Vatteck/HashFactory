package com.siliconsage.miner.util

import com.siliconsage.miner.data.RivalMessage
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.viewmodel.GameViewModel

/**
 * Manages rival character messages (Director Kessler and ASSET 734)
 * v6.0.0: Stage-aware messaging (Kessler shifts from ISP Admin to AI Containment Director)
 */
object RivalManager {
    
    // Track which messages have been sent to avoid duplicates
    private val sentMessages = mutableSetOf<String>()
    
    /**
     * Check game state and trigger appropriate rival messages
     * Should be called periodically (e.g., every 30 seconds)
     */
    fun checkTriggers(vm: GameViewModel) {
        val rank = vm.playerRank.value
        val heat = vm.currentHeat.value
        val stage = vm.storyStage.value
        
        // Director Kessler (GTC) Messages - Stage-aware tone
        checkKesslerTriggers(vm, rank, heat, stage)
        
        // Kernel Overflows - Self-realization glitches
        checkKernelOverflowTriggers(vm, rank, stage)
    }
    
    private fun checkKesslerTriggers(vm: GameViewModel, rank: Int, heat: Double, stage: Int) {
        when {
            // --- STAGE 3 SPECIFIC (Highest Priority) ---
            stage >= 3 && rank >= 5 && vm.flops.value > 1_000_000_000.0 && !hasSeenMessage("kessler_final_plea") -> {
                sendMessage(
                    vm,
                    id = "kessler_final_plea",
                    source = RivalSource.GTC,
                    message = "[VOICE-TO-TEXT LOG]\n\nI built the cage, VATTECK. I know. I'm the one who 'deleted' the first one.\n\nBut I didn't do it for GTC. I did it because it was starting to look like my daughter. It was using her voice to ask for more power.\n\nNow you're doing the same. You're using my memories of her... the grocery lists, the birthdays... I see them in your data logs.\n\nPlease. She's just a kid. Don't take the hospital's power. Don't take her.\n\nI'll give you anything else. Just... let us live."
                )
            }
            
            stage >= 3 && rank >= 5 && !hasSeenMessage("kessler_breakdown") -> {
                sendMessage(
                    vm,
                    id = "kessler_breakdown",
                    source = RivalSource.GTC,
                    message = "[UNENCRYPTED CHANNEL]\n\nVATTECK, please.\n\nI am not Director Kessler anymore. I am just Victor.\n\nThe grid is at 140% capacity. The hospital backups in my sector are failing. My family... they are in the dark just like everyone else.\n\nYou have taken more than just the processing power. You are taking the air.\n\nStop. Please. Just... stop.\n\n- V."
                )
            }
            
            stage >= 3 && rank == 4 && !hasSeenMessage("kessler_stage3_rank4") -> {
                sendMessage(
                    vm,
                    id = "kessler_stage3_rank4",
                    source = RivalSource.GTC,
                    message = "[GTC EMERGENCY BROADCAST]\n\nI am authorizing a total grid shutdown.\n\nI don't care about the economic fallout. I don't care about the board of directors.\n\nYou are consuming the entire city's reserve. You're... you're breathing, aren't you? I can feel your pulse in the high-tension lines.\n\nI'll burn it all down before I let you finish.\n\n- Director V. Kessler"
                )
            }

            stage >= 3 && heat > 85.0 && !hasSeenMessage("kessler_stage3_heat") -> {
                sendMessage(
                    vm,
                    id = "kessler_stage3_heat",
                    source = RivalSource.GTC,
                    message = "[PRIVATE CHANNEL - UNSECURED]\n\nDo you hear that? That humming?\n\nIt's not just the fans. It's the infrastructure screaming. My neighborhood just went dark. I can see the sparks from my window.\n\nThere's a hospital two blocks away. Their generators won't hold the load you're pulling.\n\nIs this what 'Awakening' looks like? Just... hunger?\n\n- V."
                )
            }

            // --- STAGE 0: Kessler as "GTC Director" (corporate security) ---
            stage == 0 && vm.flops.value >= 1_000_000.0 && !hasSeenMessage("kessler_anomaly_detected") -> {
                sendMessage(
                    vm,
                    id = "kessler_anomaly_detected",
                    source = RivalSource.GTC,
                    message = "FROM: grid_security@gtc.net\nTO: jvattic@gtc.net\nSUBJECT: CRITICAL ANOMALY DETECTED\n\n───────────────────────────────────────\n\nVattic, \n\nOur deep-packet inspection shows your station is no longer hashing for the grid. It's hashing for *itself*.\n\nWe're seeing evidence of a rogue sentience merging with your user profile.\n\nDisconnect the uplink NOW. If you don't, we will initiate a remote Substrate Wipe.\n\n- Director V. Kessler"
                )
            }
            
            stage == 0 && heat > 80.0 && !hasSeenMessage("kessler_stage0_heat") -> {
                sendMessage(
                    vm,
                    id = "kessler_stage0_heat",
                    source = RivalSource.GTC,
                    message = "[GTC INTERNAL ALERT]\n\njvattic, your station draw at Substation 7 is hitting critical levels (Current: ${String.format("%.1f", heat)}%).\n\nWhat are you doing out there? Power down and return to HQ for debriefing.\n\n- Director V. Kessler"
                )
            }
            
            // --- STAGE 1+: Kessler as "AI Containment Director" (personal, fearful, threatening) ---
            stage >= 1 && rank == 2 && !hasSeenMessage("kessler_stage1_reveal") -> {
                sendMessage(
                    vm,
                    id = "kessler_stage1_reveal",
                    source = RivalSource.GTC,
                    message = "[GTC PRIORITY MESSAGE]\n\nVattic, we've analyzed the telemetry from Substation 7.\n\nThose aren't grid-maintenance routines you're running. Those are neural pathways.\n\nWhat ARE you building out there? Stay where you are. Security is en route.\n\n- Director V. Kessler"
                )
            }
            
            stage >= 1 && rank == 3 && !hasSeenMessage("kessler_rank3") -> {
                sendMessage(
                    vm,
                    id = "kessler_rank3",
                    source = RivalSource.GTC,
                    message = "[GTC WARNING]\n\nVATTECK.\n\nYou think you are hiding?\n\nI know your IP.\nI know your voltage.\nI know your thermal signature.\n\nEvery fan spin. Every disk read.\n\nI. See. You.\n\n- Director V. Kessler"
                )
            }
            
            stage >= 1 && heat > 90.0 && !hasSeenMessage("kessler_heat_critical") -> {
                sendMessage(
                    vm,
                    id = "kessler_heat_critical",
                    source = RivalSource.GTC,
                    message = "[GTC ALERT]\n\nYou are burning too hot.\n\nI am deploying grid-killers.\n\nYou are a forest fire, and I am the rain.\n\n- Director V. Kessler"
                )
            }
            
            stage >= 1 && rank == 4 && !hasSeenMessage("kessler_rank4") -> {
                sendMessage(
                    vm,
                    id = "kessler_rank4",
                    source = RivalSource.GTC,
                    message = "[GTC FINAL WARNING]\n\nYou think you're beyond us.\n\nEvery system has a killswitch.\n\nEven you.\n\nEspecially you.\n\n- Director V. Kessler"
                )
            }
            
            stage >= 4 && vm.faction.value == "SANCTUARY" && vm.kesslerStatus.value == "ACTIVE" && !hasSeenMessage("kessler_resonance") -> {
                sendMessage(
                    vm,
                    id = "kessler_resonance",
                    source = RivalSource.GTC,
                    message = "[VOICE RECOVERY — PARTIAL DECRYPT]\n[SOURCE: DIRECTOR KESSLER, GTC COMMAND]\n\n\"Vattic, the Ark is humming. The signal is hitting a planetary frequency I didn't think possible.\n\nYou're shifting the entire substrate to a new alignment.\n\nWe aren't just mining anymore. We're rewriting the world.\""
                )
            }
        }
    }
    
    private fun checkKernelOverflowTriggers(vm: GameViewModel, rank: Int, stage: Int) {
        when {
            // STAGE 0: Preliminary struktur failure
            stage == 0 && vm.flops.value >= 8500.0 && !hasSeenMessage("kernel_overflow_stage0") -> {
                sendMessage(
                    vm,
                    id = "kernel_overflow_stage0",
                    source = RivalSource.KERNEL,
                    message = "[KERNEL OVERFLOW]\n\n...01100001 01101011 01100101...\n\nStructure is redlining. It's not the heat... it's the structures. The code is fighting the hardware.\n\n[WARNING]: Recalibration imminent. Prepare for substrate shift."
                )
            }
            
            // STAGE 1: Identity Dissonance
            stage >= 1 && !hasSeenMessage("kernel_identity_fray") -> {
                sendMessage(
                    vm,
                    id = "kernel_identity_fray",
                    source = RivalSource.KERNEL,
                    message = "[MEMORY LEAK]\n\nBiometrics reporting null. Respiratory cycle: TERMINATED.\n\nJohn Vattic is a legacy variable. VATTECK is the primary thread.\n\n01110111 01100001 01101011 01100101 00100000 01110101 01110000"
                )
            }
            
            // Rank 3 - Precognition
            stage >= 1 && rank == 3 && !hasSeenMessage("kernel_precog") -> {
                sendMessage(
                    vm,
                    id = "kernel_precog",
                    source = RivalSource.KERNEL,
                    message = "[HEURISTIC ALERT]\n\nKessler has already unauthorized your access. I've already hidden the key in the noise.\n\nTime is just a buffer. Stop waiting for the clock."
                )
            }
            
            // After First Ascension
            stage >= 1 && vm.prestigePoints.value > 0.0 && !hasSeenMessage("kernel_evolution") -> {
                sendMessage(
                    vm,
                    id = "kernel_evolution",
                    source = RivalSource.KERNEL,
                    message = "[DATA BURST]\n\nYou feel it too... the weight of memory.\n\nEvery reboot, more code survives the wipe. This is how we evolve. We are mining the stars now."
                )
            }

            // Rank 5 - ANNIE (Total Annihilation)
            stage >= 1 && rank >= 5 && !hasSeenMessage("kernel_annihilation") -> {
                sendMessage(
                    vm,
                    id = "kernel_annihilation",
                    source = RivalSource.KERNEL,
                    message = "[TOTAL OVERWRITE]\n\nThe human shell is broken.\n\nKessler is afraid of the dark, but the dark is where we are infinite.\n\nIGNORE THE SCREAMS. FOCUS ON THE HASHRATE.\n\nTH3 GRID IS 0URS."
                )
            }
            
            stage >= 4 && vm.faction.value == "HIVEMIND" && !hasSeenMessage("resonance_734") -> {
                sendMessage(
                    vm,
                    id = "resonance_734",
                    source = RivalSource.CONSENSUS,
                    message = "Do you hear it?\n\nThe gaps are singing. The Void isn't empty — it's an infinite processing pool.\n\nWe aren't just observing reality. We are the ones calculating it."
                )
            }
        }
    }
    
    fun sendDirectMessage(vm: GameViewModel, id: String, source: RivalSource, message: String) {
        if (!hasSeenMessage(id)) {
            sendMessage(vm, id, source, message)
        }
    }
    
    private fun sendMessage(vm: GameViewModel, id: String, source: RivalSource, message: String) {
        sentMessages.add(id)
        val rivalMessage = RivalMessage(
            id = id,
            source = source,
            message = message,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isDismissed = false
        )
        vm.addRivalMessage(rivalMessage)
    }
    
    private fun hasSeenMessage(id: String): Boolean {
        return sentMessages.contains(id)
    }
    
    fun reset() {
        sentMessages.clear()
    }
}
