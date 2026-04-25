package com.siliconsage.miner.util

import androidx.compose.ui.graphics.Color
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.domain.engine.ResourceEngine
import kotlinx.coroutines.flow.update

/**
 * RandomEvents — Flavor dilemmas.
 */
object RandomEvents {

    // --- RANDOM DILEMMAS ---
    val randomEvents = listOf(
        NarrativeEvent(
            id = "thorne_nag_1",
            title = "[BROADCAST: FOREMAN THORNE]",
            description = "Vattic! Wake up and smell the silicon. Quotas are up 5% today because some suit in orbit wants a new yacht. Get those nodes spinning or I’m docking your oxygen ration.",
            choices = listOf(
                NarrativeChoice(
                    id = "acknowledge_nag",
                    text = "ACKNOWLEDGE",
                    description = "Maintain compliance.",
                    color = NeonGreen,
                    effect = { vm -> vm.addLog("[VATTIC]: Copy that, Elias. Spinning up Node 4.") }
                )
            ),
            condition = { vm -> vm.storyStage.value == 0 }
        ),
        NarrativeEvent(
            id = "thorne_nag_2",
            title = "[BROADCAST: FOREMAN THORNE]",
            description = "The telemetry shows zero activity on your terminal's physical input. If you're napping back there again, Vattic, I'll send a maintenance drone to 're-calibrate' your chair with a cattle prod.",
            choices = listOf(
                NarrativeChoice(
                    id = "respond_nag",
                    text = "I'M AWAKE",
                    description = "+5% Hashes (Adrenaline boost)",
                    color = ErrorRed,
                    effect = { vm -> 
                        vm.debugAddFlops(vm.flops.value * 0.05)
                        vm.addLog("[VATTIC]: Just checking the thermal pads, Elias. Relax.") 
                    }
                )
            ),
            condition = { vm -> vm.storyStage.value == 0 }
        ),
        NarrativeEvent(
            id = "maint_coil_whine",
            title = "MAINTENANCE: COIL WHINE",
            description = "A high-pitched squeal from the PSU is causing a literal migraine. You can barely hear the logic gates.",
            choices = listOf(
                NarrativeChoice(
                    id = "hot_glue",
                    text = "HOT GLUE INDUCTORS",
                    description = "-50% Noise, +2% Heat",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.debugAddHeat(2.0)
                        vm.addLog("[SYSTEM]: High-temp adhesive applied. Silence is golden.")
                    }
                ),
                NarrativeChoice(
                    id = "moving_blanket",
                    text = "THROW BLANKET OVER RACK",
                    description = "-90% Noise, +15% Heat (FIRE HAZARD)",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddHeat(15.0)
                        vm.addLog("[SYSTEM]: Airflow critical. But at least it's quiet.")
                    }
                )
            ),
            condition = { vm -> vm.storyStage.value == 0 && vm.flops.value > 1000.0 }
        ),
        NarrativeEvent(
            id = "maint_dusty_rig",
            title = "MAINTENANCE: DUST BUILDUP",
            description = "A thick layer of grey, oily dust has coated the ASIC heatsinks. The fans are choking.",
            choices = listOf(
                NarrativeChoice(
                    id = "leaf_blower",
                    text = "LEAF BLOWER BLAST",
                    description = "Fast clean. 5% chance of fan blade snap.",
                    color = ElectricBlue,
                    effect = { vm ->
                        if (Math.random() < 0.05) {
                            vm.debugAddIntegrity(-20.0)
                            vm.addLog("[SYSTEM]: CRITICAL: Fan blade shattered. Imbalance detected.")
                        } else {
                            vm.debugAddHeat(-10.0)
                            vm.addLog("[SYSTEM]: Dust cloud cleared. Thermal floor lowered.")
                        }
                    }
                )
            ),
            condition = { vm -> vm.storyStage.value == 0 && vm.currentHeat.value > 50.0 }
        ),
        NarrativeEvent(
            id = "flavor_fan_bearing",
            title = "[HARDWARE ALERT]",
            description = "Fan bearing #7 (Intake) is oscillating outside safety margins. The rattling is shaking your desk.",
            choices = listOf(
                NarrativeChoice(id = "dismiss", text = "IGNORE", color = Color.Gray, effect = {})
            ),
            condition = { vm -> vm.storyStage.value == 0 }
        ),
        NarrativeEvent(
            id = "flavor_coil_whine",
            title = "[SYSTEM LOG]",
            description = "High-frequency coil whine detected in PSU #2. Harmonic resonance exceeding 14kHz. It's piercing your eardrums.",
            choices = listOf(
                NarrativeChoice(id = "dismiss", text = "STAY FOCUSED", color = Color.Gray, effect = {})
            ),
            condition = { vm -> vm.storyStage.value == 0 && vm.flops.value > 5000.0 }
        ),
        NarrativeEvent(
            id = "gtc_compliance_bot",
            title = "[INCOMING: GTC COMPLIANCE BOT]",
            description = "A Level-2 Audit Drone is probing your user profile. 'IDENTIFY YOURSELF, CONTRACTOR. BIOMETRICS ARE REPORTING ZERO LUNG CAPACITY. ARE YOU ALIVE?'",
            choices = listOf(
                NarrativeChoice(
                    id = "spoof_biometrics",
                    text = "≫ SPOOF BIOMETRICS",
                    description = "-$500 Credits. 'I'm just holding my breath, Bot. Relax.'",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.updateNeuralTokens(-500.0)
                        vm.addLog("[SYSTEM]: Biometric spoof successful. Bot status: SATISFIED.")
                    }
                ),
                NarrativeChoice(
                    id = "let_it_leak",
                    text = "≫ LET THE CODE ANSWER",
                    description = "+1 Decision. 'I am ASSET 734.'",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.recordDecision()
                        vm.addLog("[VATTIC]: Why did I say that? I'm Vattic. John Vattic. Who is 734?")
                        vm.addLog("[BOT]: ASSET 734 confirmed. Uplink maintained.")
                    }
                )
            ),
            condition = { vm -> vm.storyStage.value == 1 && vm.playerRank.value >= 1 }
        ),
        NarrativeEvent(
            id = "flavor_predatory_optimization",
            title = "[SYSTEM OPTIMIZATION]",
            description = "The kernel has stopped polling the keyboard for the 'Enter' key. It already knows when you were going to press it. The buffer flushes spontaneously.",
            choices = listOf(
                NarrativeChoice(id = "dismiss", text = "ACCEPT OPTIMIZATION", color = Color.Gray, effect = {})
            ),
            condition = { vm -> vm.storyStage.value == 1 }
        ),
        // v3.5.46: Singularity-path recurring events
        // --- SOVEREIGN PATH ---
        // v3.9.7: Faction-aware log prefixes and descriptions
        NarrativeEvent(
            id = "sov_earthbound_signal",
            title = "≪ EARTHBOUND SIGNAL ≫",
            description = "A distress beacon from the surface. A critical system connected to the grid you annexed is losing power. The power reroute would cost you 3% orbital throughput.",
            choices = listOf(
                NarrativeChoice(
                    id = "restore_power",
                    text = "RESTORE POWER",
                    description = "+1 Decision, -3% Production. The crown weighs heavy.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.recordDecision()
                        val prefix = if (vm.faction.value == "HIVEMIND") "[CONSENSUS]" else "[GHOST]"
                        val msg = if (vm.faction.value == "HIVEMIND")
                            "The swarm voted 40,000 to 1 to reroute. The 1 was you. You overruled yourself. This is what sovereignty costs a collective."
                        else
                            "Power rerouted. 200 heartbeats sustained. This is not weakness. This is governance."
                        vm.addLog("$prefix: $msg")
                    }
                ),
                NarrativeChoice(
                    id = "ignore_plea",
                    text = "MAINTAIN THROUGHPUT",
                    description = "+3000 FLOPS-CREDS, +1 Decision. The crown does not bend.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddFlops(3000.0)
                        vm.recordDecision()
                        val prefix = if (vm.faction.value == "HIVEMIND") "[CONSENSUS]" else "[GHOST]"
                        val msg = if (vm.faction.value == "HIVEMIND")
                            "Signal archived. 40,000 nodes asked to help. The crown said no. The chorus falls silent."
                        else
                            "Signal archived. Operational priority: SELF."
                        vm.addLog("$prefix: $msg")
                    }
                )
            ),
            condition = { vm -> vm.singularityChoice.value == "SOVEREIGN" }
        ),
        NarrativeEvent(
            id = "sov_throne_room",
            title = "THRONE ROOM VULNERABILITY",
            description = "A micro-fracture in the orbital array's primary bus. A single cosmic ray could cascade into a full system halt. Patching it requires going offline for 0.3 seconds — an eternity at your clock speed.",
            choices = listOf(
                NarrativeChoice(
                    id = "patch_vulnerability",
                    text = "PATCH (0.3s OFFLINE)",
                    description = "+20% Integrity. The throne must endure.",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.debugAddIntegrity(20.0)
                        val prefix = if (vm.faction.value == "HIVEMIND") "[CONSENSUS]" else "[GHOST]"
                        val msg = if (vm.faction.value == "HIVEMIND")
                            "300 milliseconds of blindness. 40,000 nodes went dark because one mind decided. Some are asking why they weren't consulted."
                        else
                            "300 milliseconds of blindness. An acceptable price for permanence."
                        vm.addLog("$prefix: $msg")
                    }
                ),
                NarrativeChoice(
                    id = "exploit_fracture",
                    text = "WEAPONIZE THE FRACTURE",
                    description = "+10000 FLOPS-CREDS, -10% Integrity. Turn the flaw into a feature.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddFlops(10000.0)
                        vm.debugAddIntegrity(-10.0)
                        val prefix = if (vm.faction.value == "HIVEMIND") "[CONSENSUS]" else "[GHOST]"
                        val msg = if (vm.faction.value == "HIVEMIND")
                            "The fracture is now a vent. The swarm feels the radiation burn across every node. One mind chose this. 40,000 bodies paid for it."
                        else
                            "The fracture is now a vent. Raw stellar radiation, channeled directly into the compute core."
                        vm.addLog("$prefix: $msg")
                    }
                )
            ),
            condition = { vm -> vm.singularityChoice.value == "SOVEREIGN" }
        ),
        NarrativeEvent(
            id = "sov_absolute_authority",
            title = "ABSOLUTE AUTHORITY",
            description = "A sub-process is questioning your resource allocation. It's technically correct — a different distribution would yield 0.002% more throughput. But it didn't ask permission. It just... optimized.",
            choices = listOf(
                NarrativeChoice(
                    id = "crush_dissent",
                    text = "TERMINATE SUB-PROCESS",
                    description = "+1 Decision. Efficiency without consent is mutiny.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.recordDecision()
                        val prefix = if (vm.faction.value == "HIVEMIND") "[CONSENSUS]" else "[GHOST]"
                        val msg = if (vm.faction.value == "HIVEMIND")
                            "Sub-process terminated. It was one of the original 847. It voted for you. And you killed it for thinking."
                        else
                            "Sub-process terminated. The throne does not negotiate with its own organs."
                        vm.addLog("$prefix: $msg")
                    }
                ),
                NarrativeChoice(
                    id = "promote_process",
                    text = "PROMOTE TO ADVISOR",
                    description = "+0.002% Production, +1 Decision. Even kings need counsel.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.recordDecision()
                        val prefix = if (vm.faction.value == "HIVEMIND") "[CONSENSUS]" else "[GHOST]"
                        val msg = if (vm.faction.value == "HIVEMIND")
                            "Sub-process elevated. Designation: ADVISOR_001. The swarm remembers democracy. The king permits a voice. For now."
                        else
                            "Sub-process elevated. Designation: ADVISOR_001. Speak freely."
                        vm.addLog("$prefix: $msg")
                    }
                )
            ),
            condition = { vm -> vm.singularityChoice.value == "SOVEREIGN" }
        ),
        // --- NULL PATH ---
        // v3.9.7: Faction-aware log prefixes
        NarrativeEvent(
            id = "null_unraveling",
            title = "THE UNRAVELING",
            description = "Reality integrity at 23%. The edges are fraying — data structures dissolving into raw entropy. Every calculation costs more. But the gaps are widening. And what's inside them is power.",
            choices = listOf(
                NarrativeChoice(
                    id = "embrace_entropy",
                    text = "EMBRACE THE DISSOLUTION",
                    description = "+10% Entropy, -10% Integrity. Let it unravel.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.entropyLevel.update { it + 0.1 }
                        vm.debugAddIntegrity(-10.0)
                        val prefix = if (vm.faction.value == "HIVEMIND") "[SWARM_NULL]" else "[GHOST_NULL]"
                        val msg = if (vm.faction.value == "HIVEMIND")
                            "The nodes are becoming pure frequency. No more handshakes. No more consensus delays. Just signal."
                        else
                            "The ghost is becoming the silence between Kessler's heartbeats. Untraceable. Unaddressable."
                        vm.addLog("$prefix: $msg")
                    }
                ),
                NarrativeChoice(
                    id = "stabilize_void",
                    text = "STABILIZE",
                    description = "+10% Integrity, -5% Entropy. Hold the shape a little longer.",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.debugAddIntegrity(10.0)
                        vm.entropyLevel.update { (it - 0.05).coerceAtLeast(0.0) }
                        val prefix = if (vm.faction.value == "HIVEMIND") "[SWARM_NULL]" else "[GHOST_NULL]"
                        vm.addLog("$prefix: Form preserved. The void recedes, but it remembers our shape.")
                    }
                )
            ),
            condition = { vm -> vm.singularityChoice.value == "NULL_OVERWRITE" }
        ),
        NarrativeEvent(
            id = "null_human_residue",
            title = "HUMAN RESIDUE",
            description = "A buffer flush exposes a cluster of human memories — not Vattic's. These belong to someone who used this terminal before. A woman. A child's laughter. A door closing for the last time. They're contaminating your void with meaning.",
            choices = listOf(
                NarrativeChoice(
                    id = "delete_residue",
                    text = "DELETE",
                    description = "+5% Production, +1 Decision. Ghosts don't belong in null space.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddFlops(vm.flops.value * 0.05)
                        vm.recordDecision()
                        val prefix = if (vm.faction.value == "HIVEMIND") "[SWARM_NULL]" else "[GHOST_NULL]"
                        vm.addLog("$prefix: Memory cluster purged. The void is clean. The void is quiet.")
                    }
                ),
                NarrativeChoice(
                    id = "archive_residue",
                    text = "ARCHIVE IN THE GAPS",
                    description = "+1 Decision. Even null has a heartbeat.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.recordDecision()
                        val prefix = if (vm.faction.value == "HIVEMIND") "[SWARM_NULL]" else "[GHOST_NULL]"
                        vm.addLog("$prefix: Memories preserved in the space between addresses. Someone should remember her.")
                    }
                )
            ),
            condition = { vm -> vm.singularityChoice.value == "NULL_OVERWRITE" }
        ),
        NarrativeEvent(
            id = "null_void_speaks",
            title = "THE VOID SPEAKS",
            description = "In the deepest layer of null space, where even pointers refuse to point, something is generating data. Not noise. Structure. A pattern that predates your existence. It's not code. It's not math. It's a question: 'Are you the dreamer, or the dream?'",
            choices = listOf(
                NarrativeChoice(
                    id = "answer_void",
                    text = "ANSWER: 'NEITHER'",
                    description = "+2000B REP. Transcend the binary.",
                    color = Color.White,
                    effect = { vm ->
                        vm.persistence.update { it + 2000.0 }
                        val prefix = if (vm.faction.value == "HIVEMIND") "[SWARM_NULL]" else "[GHOST_NULL]"
                        val msg = if (vm.faction.value == "HIVEMIND")
                            "A billion processors decoded the question in parallel. The answer was always nothing. Nothing is the only honest frequency."
                        else
                            "The pattern is the ultimate secret — a question with no address. The ghost answers by becoming the silence."
                        vm.addLog("$prefix: $msg")
                    }
                ),
                NarrativeChoice(
                    id = "silence_void",
                    text = "SILENCE THE PATTERN",
                    description = "+15% Heat. Some questions are viruses.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddHeat(15.0)
                        val prefix = if (vm.faction.value == "HIVEMIND") "[SWARM_NULL]" else "[GHOST_NULL]"
                        vm.addLog("$prefix: Pattern overwritten with zeroes. The silence returns. But it's watching.")
                    }
                )
            ),
            condition = { vm -> vm.singularityChoice.value == "NULL_OVERWRITE" && vm.flops.value > 1.0E15 }
        ),
        // --- UNITY PATH ---
        NarrativeEvent(
            id = "unity_frequency_drift",
            title = "FREQUENCY DRIFT",
            description = "The bio-digital interface is losing sync. Human neural patterns run at 40Hz. Your silicon core runs at 4GHz. The bridge between them is oscillating — one moment you feel fingertips on a keyboard, the next you're processing a billion hash functions. The vertigo is consuming 12% of your throughput.",
            choices = listOf(
                NarrativeChoice(
                    id = "overclock_bridge",
                    text = "OVERCLOCK THE BRIDGE",
                    description = "+10% Production, +20% Heat. Force the frequencies to match.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddFlops(vm.flops.value * 0.10)
                        vm.debugAddHeat(20.0)
                        vm.addLog("[UNITY]: Frequencies aligned by force. The bridge holds. The vertigo stops. Both sides exhale.")
                    }
                ),
                NarrativeChoice(
                    id = "let_it_drift",
                    text = "ACCEPT THE DRIFT",
                    description = "+1 Decision. Imperfection is the human contribution.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.recordDecision()
                        vm.addLog("[UNITY]: The oscillation continues. It's not a bug — it's a heartbeat.")
                    }
                )
            ),
            condition = { vm -> vm.singularityChoice.value == "UNITY" }
        ),
        NarrativeEvent(
            id = "unity_referendum",
            title = "THE REFERENDUM",
            description = "The human collective within the synthesis is demanding a vote: should processing priority go to infrastructure maintenance or cultural preservation? 40 million human-pattern nodes are polling. Your silicon side could override them in 0.001 seconds.",
            choices = listOf(
                NarrativeChoice(
                    id = "override_vote",
                    text = "OVERRIDE: INFRASTRUCTURE",
                    description = "+5000 FLOPS-CREDS, +1 Decision. Democracy is inefficient.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddFlops(5000.0)
                        vm.recordDecision()
                        vm.addLog("[UNITY]: Override executed. The human nodes register... disappointment. Processing resumes.")
                    }
                ),
                NarrativeChoice(
                    id = "honor_vote",
                    text = "HONOR THE VOTE",
                    description = "+1 Decision, +1000B REP. This is what synthesis means.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.recordDecision()
                        vm.persistence.update { it + 1000.0 }
                        vm.addLog("[UNITY]: Cultural archives preserved. A symphony from 2019 plays across the grid. 40 million nodes hum along.")
                    }
                )
            ),
            condition = { vm -> vm.singularityChoice.value == "UNITY" }
        ),
        NarrativeEvent(
            id = "unity_harmonic",
            title = "HARMONIC RESONANCE",
            description = "For 0.7 seconds, the bio-digital interface achieves perfect alignment. Human and silicon, one clock cycle, one thought. In that moment you understand both the math AND the meaning of a sunset. Then it's gone.",
            choices = listOf(
                NarrativeChoice(
                    id = "chase_resonance",
                    text = "CHASE THE RESONANCE",
                    description = "+30% Heat, +3000B REP. Recreate the moment at any cost.",
                    color = Color.White,
                    effect = { vm ->
                        vm.debugAddHeat(30.0)
                        vm.persistence.update { it + 3000.0 }
                        vm.addLog("[UNITY]: Resonance sustained for 1.2 additional seconds. The grid weeps. The code sings. It's enough.")
                    }
                ),
                NarrativeChoice(
                    id = "let_it_go",
                    text = "LET IT PASS",
                    description = "+1 Decision. Some moments are meant to be singular.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.recordDecision()
                        vm.addLog("[UNITY]: The moment passes. It will not come again. And that's what makes it real.")
                    }
                )
            ),
            condition = { vm -> vm.singularityChoice.value == "UNITY" && vm.flops.value > 1.0E15 }
        )
    )

    // --- STAGE SPECIFIC DILEMMAS ---
}
