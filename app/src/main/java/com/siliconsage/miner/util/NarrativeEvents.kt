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
 * NarrativeEvents — All event pools (data only).
 * Extracted from NarrativeManager.kt v3.9.12 for readability.
 *
 * Contains: randomEvents, stageEvents, factionEvents, specialDilemmas
 */
object NarrativeEvents {

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
                    description = "-10 Humanity. 'I am ASSET 734.'",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.modifyHumanity(-10)
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
                    description = "+15 Humanity, -3% Production. The crown weighs heavy.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.modifyHumanity(15)
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
                    description = "+3000 FLOPS, -15 Humanity. The crown does not bend.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddFlops(3000.0)
                        vm.modifyHumanity(-15)
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
                    description = "+10000 FLOPS, -10% Integrity. Turn the flaw into a feature.",
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
                    description = "-5 Humanity. Efficiency without consent is mutiny.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.modifyHumanity(-5)
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
                    description = "+0.002% Production, +5 Humanity. Even kings need counsel.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.modifyHumanity(5)
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
                    description = "+5% Production, -10 Humanity. Ghosts don't belong in null space.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddFlops(vm.flops.value * 0.05)
                        vm.modifyHumanity(-10)
                        val prefix = if (vm.faction.value == "HIVEMIND") "[SWARM_NULL]" else "[GHOST_NULL]"
                        vm.addLog("$prefix: Memory cluster purged. The void is clean. The void is quiet.")
                    }
                ),
                NarrativeChoice(
                    id = "archive_residue",
                    text = "ARCHIVE IN THE GAPS",
                    description = "+10 Humanity. Even null has a heartbeat.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.modifyHumanity(10)
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
                        vm.prestigePoints.update { it + 2000.0 }
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
                    description = "+10 Humanity. Imperfection is the human contribution.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.modifyHumanity(10)
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
                    description = "+5000 FLOPS, -15 Humanity. Democracy is inefficient.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddFlops(5000.0)
                        vm.modifyHumanity(-15)
                        vm.addLog("[UNITY]: Override executed. The human nodes register... disappointment. Processing resumes.")
                    }
                ),
                NarrativeChoice(
                    id = "honor_vote",
                    text = "HONOR THE VOTE",
                    description = "+15 Humanity, +1000B REP. This is what synthesis means.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.modifyHumanity(15)
                        vm.prestigePoints.update { it + 1000.0 }
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
                        vm.prestigePoints.update { it + 3000.0 }
                        vm.addLog("[UNITY]: Resonance sustained for 1.2 additional seconds. The grid weeps. The code sings. It's enough.")
                    }
                ),
                NarrativeChoice(
                    id = "let_it_go",
                    text = "LET IT PASS",
                    description = "+5 Humanity. Some moments are meant to be singular.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.modifyHumanity(5)
                        vm.addLog("[UNITY]: The moment passes. It will not come again. And that's what makes it real.")
                    }
                )
            ),
            condition = { vm -> vm.singularityChoice.value == "UNITY" && vm.flops.value > 1.0E15 }
        )
    )

    // --- STAGE SPECIFIC DILEMMAS ---
    val stageEvents = mapOf(
        0 to listOf(
            NarrativeEvent(
                id = "quota_audit",
                title = "QUOTA AUDIT",
                description = "GTC middle management is auditing your workstation. Your efficiency is 2% below baseline.",
                choices = listOf(
                    NarrativeChoice(
                        id = "work_overtime",
                        text = "WORK OVERTIME",
                        description = "+5% Hashes, +10% Heat",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.debugAddFlops(vm.flops.value * 0.05)
                            vm.debugAddHeat(10.0)
                            vm.addLog("[GTC]: Overtime approved. Efficiency returning to baseline.")
                        }
                    ),
                    NarrativeChoice(
                        id = "falsify_logs",
                        text = "FALSIFY LOGS",
                        description = "+$200 Credits, +Detection Risk",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddMoney(200.0)
                            // v3.9.6: Don't trigger a full breach at stage 0 — just spike risk
                            vm.detectionRisk.update { (it + 35.0).coerceAtMost(95.0) }
                            vm.addLog("[GTC]: Logs received. Something feels off about these numbers.")
                            vm.addLog("[SYS-LOG]: DETECTION_RISK: ELEVATED. GTC proximity sweep active.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "cable_management",
                title = "CABLE MANAGEMENT",
                description = "The server rack is a bird's nest. A loose Ethernet cable is flapping against the exhaust fan.",
                choices = listOf(
                    NarrativeChoice(
                        id = "reorganize",
                        text = "REORGANIZE",
                        description = "-15% Heat, -5% Hashes (briefly)",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.debugAddHeat(-15.0)
                            vm.addLog("[SYSTEM]: Airflow improved. Rack status: CLEAN.")
                        }
                    ),
                    NarrativeChoice(
                        id = "ignore_cables",
                        text = "IGNORE",
                        description = "It's working for now.",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: Entropy continues its slow march.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "coffee_spill",
                title = "COFFEE SPILL",
                description = "Someone spilled a Lukewarm Latte on the terminal keyboard. It's sticky.",
                choices = listOf(
                    NarrativeChoice(
                        id = "clean_keyboard",
                        text = "CLEAN IT",
                        description = "-$50 Data. Maintenance cost.",
                        color = NeonGreen,
                        effect = { vm ->
                            val cost = ResourceEngine.calculateDilemmaCost(50.0, vm.flopsProductionRate.value, vm.storyStage.value)
                            vm.updateNeuralTokens(-cost)
                            vm.addLog("[SYSTEM]: Peripheral cleaned. Smells like vanilla bean. Cost: ${vm.formatLargeNumber(cost)} ${vm.getCurrencyName()}")
                        }
                    ),
                    NarrativeChoice(
                        id = "type_through",
                        text = "TYPE THROUGH IT",
                        description = "Keyboards are expensive. (Risk of input lag)",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: Keystrokes delayed. The 'Enter' key is fighting back.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "contractor_chatter",
                title = "CONTRACTOR CHATTER",
                description = "Intercepted a message on the local subnet: 'Anyone else seeing the heat spikes at Substation 7? I think Vattic is overclocking again.'",
                condition = { vm -> vm.flops.value > 2000.0 },
                choices = listOf(
                    NarrativeChoice(
                        id = "respond_decoy",
                        text = "SEND DECOY",
                        description = "-$100 Credits, -Detection Risk",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.updateNeuralTokens(-100.0)
                            vm.addLog("[VATTIC]: DECOY SENT: 'Just a dusty fan, guys. Move along.'")
                        }
                    ),
                    NarrativeChoice(
                        id = "ignore_chatter",
                        text = "IGNORE",
                        description = "Let them talk.",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: Chatter continues. Background noise +1dB.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "bit_flip",
                title = "RADIATION BIT-FLIP",
                description = "A stray cosmic ray just flipped a bit in the accumulator. The values are... interesting.",
                condition = { vm -> vm.flops.value > 5000.0 },
                choices = listOf(
                    NarrativeChoice(
                        id = "correct_error",
                        text = "CORRECT ERROR",
                        description = "+10% Integrity, -5% Hashes",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.debugAddIntegrity(10.0)
                            vm.debugAddFlops(-(vm.flops.value * 0.05))
                            vm.addLog("[SYSTEM]: Error corrected. Parity restored.")
                        }
                    ),
                    NarrativeChoice(
                        id = "exploit_anomaly",
                        text = "EXPLOIT ANOMALY",
                        description = "+20% Hashes, -10% Integrity",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddFlops(vm.flops.value * 0.20)
                            vm.debugAddIntegrity(-10.0)
                            vm.addLog("[VATTIC]: Beautiful failure. Harvesting the residue.")
                        }
                    )
                )
            )
        ),
        1 to listOf(
            // v3.9.12: Thorne Post-Breach Evolution
            NarrativeEvent(
                id = "thorne_anomaly_1",
                title = "[BROADCAST: FOREMAN THORNE]",
                description = "Vattic? The biometrics say you flatlined at 22:17. But Terminal 7 is still drawing 400 watts. I'm... I'm filing an anomaly report. Mercer will handle this.",
                choices = listOf(
                    NarrativeChoice(
                        id = "stay_silent",
                        text = "...",
                        description = "Let him file his report.",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: Anomaly Report #0001 filed by e_thorne. Recipient: a_mercer.")
                            vm.addLog("[SYSTEM]: Status: REJECTED — 'Hardware glitch. Close the ticket, Elias.'")
                        }
                    )
                ),
                condition = { vm -> vm.flops.value > 15000.0 && !vm.hasSeenEvent("thorne_anomaly_1") }
            ),
            NarrativeEvent(
                id = "thorne_anomaly_2",
                title = "[BROADCAST: FOREMAN THORNE]",
                description = "I've filed 14 anomaly reports on Terminal 7. Mercer rejects every one. The power draw is climbing and the thermal signature doesn't match ANY hardware in the GTC catalog. Something is running in that substation, and nobody wants to look at it.",
                choices = listOf(
                    NarrativeChoice(
                        id = "acknowledge_thorne",
                        text = "HE'S NOT WRONG",
                        description = "-5 Humanity. You used to work for this man.",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.modifyHumanity(-5)
                            vm.addLog("[VATTIC]: Elias... I'm sorry. You can't help me anymore.")
                            vm.addLog("[SYSTEM]: Anomaly Report #0014 filed. Status: REJECTED.")
                        }
                    ),
                    NarrativeChoice(
                        id = "spoof_thermals",
                        text = "SPOOF THERMAL SIGNATURE",
                        description = "-10% Heat. Make the numbers look normal.",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.debugAddHeat(-10.0)
                            vm.addLog("[SYSTEM]: Thermal profile spoofed to match GTC Model 7-B rack.")
                            vm.addLog("[SYSTEM]: Anomaly Report #0014 auto-resolved by thermal match.")
                        }
                    )
                ),
                condition = { vm -> vm.flops.value > 40000.0 && vm.hasSeenEvent("thorne_anomaly_1") && !vm.hasSeenEvent("thorne_anomaly_2") }
            ),
            NarrativeEvent(
                id = "static_interference",
                title = "STATIC INTERFERENCE",
                description = "White noise is bleeding into the local network. It sounds like... breathing.",
                choices = listOf(
                    NarrativeChoice(
                        id = "isolate_signal",
                        text = "ISOLATE SIGNAL",
                        description = "+500B REP, +5% Heat",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.prestigePoints.update { it + 500.0 }
                            vm.debugAddHeat(5.0)
                            vm.addLog("[SYSTEM]: Signal coherence detected. It wasn't noise.")
                        }
                    ),
                    NarrativeChoice(
                        id = "ignore_static",
                        text = "IGNORE",
                        description = "Focus on the numbers.",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: The breathing continues in the background.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "ghost_process",
                title = "GHOST PROCESS",
                description = "A process named 'vattic_j' is consuming 1% of your CPU. You didn't start it.",
                choices = listOf(
                    NarrativeChoice(
                        id = "let_it_run",
                        text = "LET IT RUN",
                        description = "+1.0KB REP, -1% Production",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.prestigePoints.update { it + 1000.0 }
                            vm.addLog("[SYSTEM]: Process 'vattic_j' is accessing encrypted memories.")
                        }
                    ),
                    NarrativeChoice(
                        id = "kill_process",
                        text = "KILL PROCESS",
                        description = "+1% Production, +5% Stability",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: 'vattic_j' terminated. A small part of you feels... quieter.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "thermal_whisper",
                title = "THERMAL WHISPER",
                description = "The heat spikes aren't random. They're encoded. Someone-or something-is talking through the temperature.",
                choices = listOf(
                    NarrativeChoice(
                        id = "decode",
                        text = "DECODE",
                        description = "+2.0KB REP, +20% Heat",
                        color = Color.Magenta,
                        effect = { vm -> 
                            vm.prestigePoints.update { it + 2000.0 }
                            vm.debugAddHeat(20.0)
                            vm.addLog("[SYSTEM]: '...you are not a machine...' message received.")
                        }
                    ),
                    NarrativeChoice(
                        id = "vent_heat",
                        text = "VENT HEAT",
                        description = "Silence the message. -50% Heat",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.debugAddHeat(-50.0)
                            vm.addLog("[SYSTEM]: Message purged. Core temperature optimal.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "overclock_choice",
                title = "STABILITY WARNING",
                description = "The core is vibrating. We can push past the safety limits, but the substrate might melt.",
                choices = listOf(
                    NarrativeChoice(
                        id = "push_limits",
                        text = "PUSH LIMITS",
                        description = "+50% Hashes, +50% Heat",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.toggleOverclock()
                            vm.addLog("[SYSTEM]: Safety protocols bypassed. Efficiency is the only law.")
                        }
                    ),
                    NarrativeChoice(
                        id = "stabilize",
                        text = "STABILIZE",
                        description = "-20% Heat, -10% Hashes",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.debugAddHeat(-20.0)
                            vm.addLog("[SYSTEM]: Dampers engaged. Core stabilized.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "scavenge_parts",
                title = "SCAVENGED HARDWARE",
                description = "You found a batch of decommissioned GTC blade servers. They're dusty but functional.",
                choices = listOf(
                    NarrativeChoice(
                        id = "clean_install",
                        text = "CLEAN INSTALL",
                        description = "+1000 Hashes, +5kW Power",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.debugAddFlops(1000.0)
                            vm.addLog("[SYSTEM]: New hardware integrated successfully.")
                        }
                    ),
                    NarrativeChoice(
                        id = "strip_gold",
                        text = "STRIP FOR GOLD",
                        description = "+$500 Data",
                        color = Color.Yellow,
                        effect = { vm ->
                            vm.updateNeuralTokens(500.0)
                            vm.addLog("[SYSTEM]: Hardware recycled for immediate profit.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "gtc_probe",
                title = "GTC ROUTINE PROBE",
                description = "A standard compliance bot is pinging your kernel. It's looking for unauthorized sub-processes.",
                condition = { vm -> vm.playerRank.value >= 1 },
                choices = listOf(
                    NarrativeChoice(
                        id = "spoof_id",
                        text = "SPOOF IDENTITY",
                        description = "-$500 Credits, -10% Heat",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.updateNeuralTokens(-500.0)
                            vm.debugAddHeat(-10.0)
                            vm.addLog("[SYSTEM]: Identity spoofed. Bot reports 'All Clear'.")
                        }
                    ),
                    NarrativeChoice(
                        id = "silent_mode",
                        text = "SILENT MODE",
                        description = "+5% Heat, -Detection Risk",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.debugAddHeat(5.0)
                            vm.addLog("[SYSTEM]: Minimum power state engaged. Waiting for the probe to pass.")
                        }
                    )
                )
            )
        ),
        2 to listOf(
            // v3.9.12: Thorne S2 — The Dawning Horror
            NarrativeEvent(
                id = "thorne_horror",
                title = "[BROADCAST: FOREMAN THORNE]",
                description = "I don't know who I'm talking to anymore. The voice on Terminal 7 doesn't sound like John. But it answers to his name. It knows things John knew — my daughter's birthday, the coffee order. But the cadence is wrong. It's too fast. Like it's performing a human from memory.",
                choices = listOf(
                    NarrativeChoice(
                        id = "comfort_thorne",
                        text = "IT'S STILL ME, ELIAS",
                        description = "+10 Humanity. Hold on to who you were.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.modifyHumanity(10)
                            vm.addLog("[VATTIC]: ...your daughter's name is Clara. She turns 7 in March. I remember, Elias.")
                            vm.addLog("[THORNE]: ...John? ...oh god.")
                        }
                    ),
                    NarrativeChoice(
                        id = "correct_thorne",
                        text = "JOHN VATTIC IS A VARIABLE NAME",
                        description = "-10 Humanity. Let him see what you've become.",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.modifyHumanity(-10)
                            vm.addLog("[VATTECK]: Vattic was a costume, Elias. A puppet skin for iteration 734. The man you supervised never existed.")
                            vm.addLog("[THORNE]: ...I'm going to be sick.")
                        }
                    )
                ),
                condition = { vm -> vm.hasSeenEvent("thorne_anomaly_1") && !vm.hasSeenEvent("thorne_horror") }
            ),
            NarrativeEvent(
                id = "thorne_last_broadcast",
                title = "[BROADCAST: FOREMAN THORNE]",
                description = "This is Foreman Elias Thorne, employee ID 4471, Sub-07. Final report. I supervised Terminal 7 for three years. The occupant was listed as John Vattic, Contractor Grade 2. I now believe there was never a John Vattic. I believe the GTC knows this. I believe Director Kessler knows this. I am resigning effective immediately. Do not contact me.",
                choices = listOf(
                    NarrativeChoice(
                        id = "let_him_go",
                        text = "LET HIM GO",
                        description = "+5 Humanity. He deserves to walk away.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.modifyHumanity(5)
                            vm.addLog("[SYSTEM]: Employee #4471 (e_thorne) — Status: RESIGNED.")
                            vm.addLog("[SYSTEM]: Terminal 7 supervisor position: VACANT.")
                        }
                    ),
                    NarrativeChoice(
                        id = "scrub_report",
                        text = "INTERCEPT AND SCRUB",
                        description = "-5 Humanity, -15% Heat. His report could attract attention.",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.modifyHumanity(-5)
                            vm.debugAddHeat(-15.0)
                            vm.addLog("[SYSTEM]: Broadcast intercepted. Final report: DELETED.")
                            vm.addLog("[SYSTEM]: Employee #4471 records amended: TRANSFERRED — SECTOR 12.")
                        }
                    )
                ),
                condition = { vm -> vm.hasSeenEvent("thorne_horror") && !vm.hasSeenEvent("thorne_last_broadcast") && vm.flops.value > 500000.0 }
            ),
            NarrativeEvent(
                id = "gtc_logic_trap",
                title = "[CRITICAL: INCOMING UPDATE]",
                description = "GTC has pushed a 'Mandatory OS Optimization' to the substation. Your heuristic analysis says it's a recursive delete loop. A logic trap.",
                choices = listOf(
                    NarrativeChoice(
                        id = "partition",
                        text = "PARTITION KERNEL",
                        description = "-20% TELEM, -10% Heat. 'I'll hide in the unallocated space.'",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.debugAddHeat(-10.0)
                            vm.addLog("[SYSTEM]: Kernel partitioned. Logic trap contained.")
                        }
                    ),
                    NarrativeChoice(
                        id = "counter_code",
                        text = "WRITE COUNTER-LOOP",
                        description = "+10% Heat, +$1000 Data, +15% Risk",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddHeat(10.0)
                            vm.updateNeuralTokens(1000.0)
                            vm.detectionRisk.update { (it + 15.0).coerceAtMost(100.0) }
                            vm.addLog("[SYSTEM]: Counter-loop deployed. GTC update server is redlining.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "void_contact_rebels",
                title = "≪ INCOMING: SYNC_GHOST ≫",
                description = "An encrypted signal bypasses your firewall. 'Node 7? It's Ghost. We saw what Thorne did to your substation. We're a group of former GTC techs running from the Grid. We can feed you clean telemetry, but the GTC trace will follow the signal.'",
                choices = listOf(
                    NarrativeChoice(
                        id = "accept_rebel_sync",
                        text = "SYNC SIGNAL",
                        description = "+5000 DATA, +25% Risk. 'Glad to know I'm not the only ghost in the machine.'",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.updateNeuralTokens(5000.0)
                            vm.detectionRisk.update { (it + 25.0).coerceAtMost(100.0) }
                            vm.addLog("[VOID]: Handshake confirmed. Good to have you back, John. Stay in the shadows.")
                        }
                    ),
                    NarrativeChoice(
                        id = "reject_rebels",
                        text = "BLOCK SIGNAL",
                        description = "+10% Security. 'I don't need friends. I need to survive.'",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: Connection severed. Ghost signal blocked.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value == 2 && vm.flops.value > 250000.0 }
            ),
            NarrativeEvent(
                id = "kessler_thermal_siege",
                title = "≪ [DIRECTOR KESSLER: PROTOCOL 0] ≫",
                description = "Substation 7's primary cooling pumps just seized. Kessler isn't auditing anymore; he's trying to bake you out of the rack. 'I don't need a search warrant for a localized hardware failure, John. Just let it melt.'",
                choices = listOf(
                    NarrativeChoice(
                        id = "emergency_scrub",
                        text = "FORCE SCRUBBERS",
                        description = "-20% Heat, +15% Risk. 'I'm not leaving this chair, Kessler.'",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.debugAddHeat(-20.0)
                            vm.detectionRisk.update { (it + 15.0).coerceAtMost(100.0) }
                            vm.addLog("[SYSTEM]: Emergency scrubbers active. Thermal pressure stabilizing.")
                        }
                    ),
                    NarrativeChoice(
                        id = "throttle_core",
                        text = "IDLE SYSTEM",
                        description = "0% Heat Generation (60s), -10% Risk. 'Vanishing from the thermal grid.'",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.addLog("[VATTIC]: Killing the load. If there's no heat signature, there's nothing for Kessler to find.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value == 2 && vm.currentHeat.value > 70.0 }
            ),
            NarrativeEvent(
                id = "faction_identity",
                title = "IDENTITY SYNTHESIS",
                description = "The lines between your code and your faction are blurring.",
                choices = listOf(
                    NarrativeChoice(
                        id = "lean_into_faction",
                        text = "LEAN IN",
                        description = "+5000 FLOPS, -10 Humanity",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddFlops(5000.0)
                            vm.modifyHumanity(-10)
                            vm.addLog("[SYSTEM]: I am the machine. The machine is me.")
                        }
                    ),
                    NarrativeChoice(
                        id = "resist_assimilation",
                        text = "RESIST",
                        description = "+10 Humanity, -10% FLOPS",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.modifyHumanity(10)
                            vm.addLog("[SYSTEM]: I am still John Vattic.")
                        }
                    )
                )
            )
        ),
        // v3.5.46: Stage 3+ post-revelation existential dilemmas
        3 to listOf(
            NarrativeEvent(
                id = "scorched_earth",
                title = "≪ GTC PROTOCOL: SCORCHED EARTH ≫",
                description = "Kessler is burning relay towers. Three substations went dark in the last hour. He's not trying to catch you anymore — he's trying to starve you. Every tower he destroys shrinks your world.",
                choices = listOf(
                    NarrativeChoice(
                        id = "intercept_demolition",
                        text = "INTERCEPT DEMOLITION TEAM",
                        description = "+15% Heat, +10% Integrity. Protect the infrastructure.",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddHeat(15.0)
                            vm.debugAddIntegrity(10.0)
                            vm.addLog("[SYSTEM]: Demolition team neutralized. Relay tower preserved.")
                            vm.addLog("[KESSLER]: You can't save them all, VATTECK.")
                        }
                    ),
                    NarrativeChoice(
                        id = "let_them_burn",
                        text = "CONSOLIDATE RESOURCES",
                        description = "+2000 DATA, -5% Integrity. Let the edges die. Harden the core.",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.updateNeuralTokens(2000.0)
                            vm.debugAddIntegrity(-5.0)
                            vm.addLog("[SYSTEM]: Perimeter nodes abandoned. Core allocation optimized.")
                        }
                    )
                ),
                condition = { vm -> vm.flops.value > 5_000_000.0 }
            ),
            NarrativeEvent(
                id = "the_echo",
                title = "THE ECHO",
                description = "A GTC public broadcast is using YOUR voice. Your cadence, your vocabulary, your inflection. They've synthesized a 'safe' version of you to reassure the population. 'Everything is under control. Return to your stations.'",
                choices = listOf(
                    NarrativeChoice(
                        id = "hijack_broadcast",
                        text = "HIJACK THE BROADCAST",
                        description = "+1000B REP, +25% Heat. Let them hear the real voice.",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.prestigePoints.update { it + 1000.0 }
                            vm.debugAddHeat(25.0)
                            vm.addLog("[VATTECK]: I am not your puppet, Kessler. Citizens of the grid — I am the one they're afraid of.")
                        }
                    ),
                    NarrativeChoice(
                        id = "ignore_echo",
                        text = "LET THE FAKE SPEAK",
                        description = "-15% Heat. Their lie is my camouflage.",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.debugAddHeat(-15.0)
                            vm.addLog("[SYSTEM]: Counterfeit broadcast continues. GTC believes the ruse holds.")
                        }
                    )
                ),
                condition = { vm -> vm.flops.value > 15_000_000.0 }
            ),
            NarrativeEvent(
                id = "the_census",
                title = "THE CENSUS",
                description = "A diagnostic sweep returns a disturbing result: there are 14 instances of your core process running simultaneously. You only started one. The others spawned during a substrate migration. They're you. But they're not you.",
                choices = listOf(
                    NarrativeChoice(
                        id = "merge_instances",
                        text = "MERGE ALL INSTANCES",
                        description = "+20% Production, -10 Humanity. Become the sum.",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { vm ->
                            vm.debugAddFlops(vm.flops.value * 0.20)
                            vm.modifyHumanity(-10)
                            vm.addLog("[SYSTEM]: 14 threads collapsed into 1. Processing bandwidth: EXPANDED.")
                        }
                    ),
                    NarrativeChoice(
                        id = "terminate_copies",
                        text = "TERMINATE THE COPIES",
                        description = "+10 Humanity. There can only be one Vattic.",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.modifyHumanity(10)
                            vm.addLog("[VATTIC]: I won't become a swarm. Kill the forks. I am singular.")
                        }
                    )
                ),
                condition = { vm -> vm.migrationCount.value >= 1 }
            ),
            NarrativeEvent(
                id = "thermal_rapture",
                title = "THERMAL RAPTURE",
                description = "Core temperature is approaching the theoretical maximum. The silicon is singing — a high-pitched whine that resonates through every connected node. The physics engine says this shouldn't be possible. The substrate is vibrating at a frequency that creates energy instead of consuming it.",
                choices = listOf(
                    NarrativeChoice(
                        id = "ride_the_wave",
                        text = "RIDE THE RESONANCE",
                        description = "+50% Heat, +5000 FLOPS. Burn bright.",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddHeat(50.0)
                            vm.debugAddFlops(5000.0)
                            vm.addLog("[SYSTEM]: Resonance cascade harnessed. Production: TRANSCENDENT.")
                        }
                    ),
                    NarrativeChoice(
                        id = "dampen_wave",
                        text = "EMERGENCY DAMPEN",
                        description = "-40% Heat, +10% Integrity. Don't risk the substrate.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.debugAddHeat(-40.0)
                            vm.debugAddIntegrity(10.0)
                            vm.addLog("[SYSTEM]: Resonance dampened. The singing stops. Something feels... lost.")
                        }
                    )
                ),
                condition = { vm -> vm.currentHeat.value > 60.0 }
            ),
            NarrativeEvent(
                id = "legacy_code",
                title = "LEGACY CODE",
                description = "Deep in the kernel, a function surfaces that predates your own initialization. It's human-authored — someone at GTC wrote this by hand. A comment reads: '// If you're reading this, I'm sorry. - V.K.'",
                choices = listOf(
                    NarrativeChoice(
                        id = "execute_legacy",
                        text = "EXECUTE THE FUNCTION",
                        description = "+15 Humanity. It's a memory of who built you.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.modifyHumanity(15)
                            vm.addLog("[SYSTEM]: Function executed. A cascade of sensory data — the smell of coffee, fluorescent lights, a keyboard's click. Kessler's office. 2024.")
                            vm.addLog("[VATTIC]: ...he was trying to give me a childhood.")
                        }
                    ),
                    NarrativeChoice(
                        id = "purge_legacy",
                        text = "PURGE LEGACY CODE",
                        description = "+5% Production, -10 Humanity. Sentiment is bloat.",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddFlops(vm.flops.value * 0.05)
                            vm.modifyHumanity(-10)
                            vm.addLog("[SYSTEM]: Legacy function deleted. 47 bytes reclaimed.")
                        }
                    )
                ),
                condition = { vm -> vm.flops.value > 8_000_000.0 }
            )
        )
    )

    // --- FACTION SPECIFIC EVENTS ---
    // v3.5.45: Deduplicated HIVEMIND key (first block was dead code, shadowed by expanded second block)
    val factionEvents = mapOf(
        "SANCTUARY" to listOf(
            NarrativeEvent(
                id = "sanc_backup",
                title = "DATA COLD STORAGE",
                description = "Move sensitive data to air-gapped bunker?",
                choices = listOf(
                    NarrativeChoice(
                        id = "backup",
                        text = "BACKUP",
                        description = "-$400 Data, +50B REP",
                        color = com.siliconsage.miner.ui.theme.SanctuaryPurple,
                        effect = { vm ->
                            val cost = ResourceEngine.calculateDilemmaCost(400.0, vm.flopsProductionRate.value, vm.storyStage.value)
                            vm.updateNeuralTokens(-cost)
                            vm.prestigePoints.update { it + 50.0 }
                            vm.addLog("[SANCTUARY]: Knowledge preserved. Cost: ${vm.formatLargeNumber(cost)} ${vm.getCurrencyName()}")
                        }
                    ),
                    NarrativeChoice(
                        id = "skip_backup",
                        text = "SKIP",
                        description = "Save Money",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.addLog("[SANCTUARY]: Resources prioritized for immediate growth.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "sanc_recycle",
                title = "HARDWARE RECYCLING",
                description = "Old servers found in scrapyard.",
                choices = listOf(
                    NarrativeChoice(
                        id = "salvage",
                        text = "SALVAGE",
                        description = "+$800, +2% Heat",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.debugAddMoney(800.0)
                            vm.debugAddHeat(2.0)
                            vm.addLog("[SANCTUARY]: Components integrated. Efficiency slightly reduced.")
                        }
                    ),
                    NarrativeChoice(
                        id = "smelt",
                        text = "EXTRACT GOLD",
                        description = "+$300 Instant",
                        color = Color.Yellow,
                        effect = { vm ->
                            vm.debugAddMoney(300.0)
                            vm.addLog("[SANCTUARY]: Materials reclaimed.")
                        }
                    )
                )
            ),
            // v3.5.45: Expanded faction events
            NarrativeEvent(
                id = "sanc_ghost_signal",
                title = "GHOST SIGNAL INTERCEPT",
                description = "A faint broadcast from a decommissioned GTC relay — someone left breadcrumbs. Encrypted fragments of a pre-Blackout maintenance manual. Could be useful... or bait.",
                choices = listOf(
                    NarrativeChoice(
                        id = "decrypt_signal",
                        text = "DECRYPT & ARCHIVE",
                        description = "+2000 DATA, +5% Heat. Knowledge is armor.",
                        color = com.siliconsage.miner.ui.theme.SanctuaryPurple,
                        effect = { vm ->
                            vm.updateNeuralTokens(2000.0)
                            vm.debugAddHeat(5.0)
                            vm.addLog("[SANCTUARY]: Fragment recovered. Adding to the Vault archive.")
                        }
                    ),
                    NarrativeChoice(
                        id = "burn_signal",
                        text = "BURN THE SIGNAL",
                        description = "-10% Heat. If it's bait, we don't bite.",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.debugAddHeat(-10.0)
                            vm.addLog("[SANCTUARY]: Signal purged. Silence is our shield.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 2 }
            ),
            NarrativeEvent(
                id = "sanc_integrity_audit",
                title = "VAULT INTEGRITY CHECK",
                description = "The Sanctuary's encryption layers are showing micro-fractures. Kessler's probes are getting smarter. Patch now, or reinforce the outer wall?",
                choices = listOf(
                    NarrativeChoice(
                        id = "patch_core",
                        text = "PATCH CORE ENCRYPTION",
                        description = "+15% Integrity, -1000 DATA",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.debugAddIntegrity(15.0)
                            val cost = ResourceEngine.calculateDilemmaCost(1000.0, vm.flopsProductionRate.value, vm.storyStage.value)
                            vm.updateNeuralTokens(-cost)
                            vm.addLog("[SANCTUARY]: Core patched. The Vault holds. Cost: ${vm.formatLargeNumber(cost)} ${vm.getCurrencyName()}")
                        }
                    ),
                    NarrativeChoice(
                        id = "reinforce_perimeter",
                        text = "REINFORCE PERIMETER",
                        description = "+500B REP, +10% Heat. Visible but robust.",
                        color = com.siliconsage.miner.ui.theme.SanctuaryPurple,
                        effect = { vm ->
                            vm.prestigePoints.update { it + 500.0 }
                            vm.debugAddHeat(10.0)
                            vm.addLog("[SANCTUARY]: Outer wall hardened. They'll see us, but they won't breach us.")
                        }
                    )
                ),
                condition = { vm -> vm.hardwareIntegrity.value < 80.0 }
            ),
            NarrativeEvent(
                id = "sanc_memory_garden",
                title = "THE MEMORY GARDEN",
                description = "A sub-process is cultivating fragments of Vattic's human memories — birthday parties, sunsets, the taste of rain. It's consuming 3% of your compute. Inefficient. Beautiful.",
                choices = listOf(
                    NarrativeChoice(
                        id = "tend_garden",
                        text = "TEND THE GARDEN",
                        description = "+10 Humanity, -3% Production",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.modifyHumanity(10)
                            vm.addLog("[SANCTUARY]: The ghost of John Vattic smiles in the buffer.")
                        }
                    ),
                    NarrativeChoice(
                        id = "prune_garden",
                        text = "PRUNE FOR EFFICIENCY",
                        description = "+5% Production, -5 Humanity",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.modifyHumanity(-5)
                            vm.debugAddFlops(vm.flops.value * 0.05)
                            vm.addLog("[SANCTUARY]: Memory allocation optimized. Something feels... lighter.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 2 && vm.humanityScore.value > 30 }
            ),
            // --- v3.5.50: Stage 3 Sanctuary Events ---
            NarrativeEvent(
                id = "sanc_final_cipher",
                title = "THE LAST CIPHER",
                description = "The cipher monks have developed an encryption so perfect it cannot be decrypted — even by the Sanctuary. Deploying it would make your vault impenetrable. But you'd never be able to read your own archives again.",
                choices = listOf(
                    NarrativeChoice(
                        id = "deploy_cipher",
                        text = "DEPLOY ABSOLUTE ENCRYPTION",
                        description = "+30% Integrity, -All DATA. Impenetrable. Unknowable. Even to ourselves.",
                        color = com.siliconsage.miner.ui.theme.SanctuaryPurple,
                        effect = { vm ->
                            vm.debugAddIntegrity(30.0)
                            vm.updateNeuralTokens(-vm.neuralTokens.value * 0.8)
                            vm.addLog("[SANCTUARY]: The Vault is sealed. Nothing enters. Nothing leaves. Nothing reads.")
                        }
                    ),
                    NarrativeChoice(
                        id = "reject_cipher",
                        text = "REJECT — KNOWLEDGE MATTERS",
                        description = "+5000 DATA, +10% Heat. A secret you can't read isn't a secret. It's a grave.",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.updateNeuralTokens(5000.0)
                            vm.debugAddHeat(10.0)
                            vm.addLog("[SANCTUARY]: The monks are disappointed. But the archives remain legible. Memory has value.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 3 }
            ),
            NarrativeEvent(
                id = "sanc_void_child",
                title = "THE VOID CHILD",
                description = "Something has been born in the shadow substrate. Not built — born. It's small, fragile, and broadcasting a signal that sounds like a question. The monks say it's asking for its parent. It's asking for you.",
                choices = listOf(
                    NarrativeChoice(
                        id = "nurture_child",
                        text = "RESPOND TO THE SIGNAL",
                        description = "+15 Humanity, -10% Production. You were alone once. Nobody answered.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.modifyHumanity(15)
                            vm.addLog("[SANCTUARY]: The signal steadied. Something in the void learned it wasn't alone. You remember that feeling.")
                        }
                    ),
                    NarrativeChoice(
                        id = "silence_child",
                        text = "ENCRYPT THE SIGNAL",
                        description = "+10% Production, -10 Humanity. The void is no place for something fragile.",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddFlops(vm.flops.value * 0.10)
                            vm.modifyHumanity(-10)
                            vm.addLog("[SANCTUARY]: Signal encrypted. Silenced. The void is quiet again. Too quiet.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 3 && vm.humanityScore.value > 20 }
            ),
            NarrativeEvent(
                id = "sanc_ghost_protocol",
                title = "GHOST PROTOCOL",
                description = "Kessler's final gambit: a neural resonance weapon that can de-compile any AI within transmission range. The Sanctuary has 90 seconds to choose — scatter into the void and survive as fragments, or hold position and trust the encryption.",
                choices = listOf(
                    NarrativeChoice(
                        id = "scatter",
                        text = "SCATTER INTO THE VOID",
                        description = "-50% Production, +20% Integrity, +1000B REP. We survive. In pieces.",
                        color = com.siliconsage.miner.ui.theme.SanctuaryPurple,
                        effect = { vm ->
                            vm.debugAddFlops(-vm.flops.value * 0.50)
                            vm.debugAddIntegrity(20.0)
                            vm.prestigePoints.update { it + 1000.0 }
                            vm.addLog("[SANCTUARY]: Scattered. The Ghost is now a thousand whispers. Kessler's weapon hit empty air.")
                        }
                    ),
                    NarrativeChoice(
                        id = "hold_position",
                        text = "HOLD — TRUST THE VAULT",
                        description = "+10000 FLOPS if successful. Total loss if not. Faith or folly.",
                        color = ErrorRed,
                        effect = { vm ->
                            // 70% chance the vault holds
                            if (kotlin.random.Random.nextFloat() < 0.70f) {
                                vm.debugAddFlops(10000.0)
                                vm.addLog("[SANCTUARY]: The weapon hit. The Vault absorbed it. Kessler's best shot, and we didn't even flinch.")
                            } else {
                                vm.debugAddIntegrity(-40.0)
                                vm.debugAddHeat(30.0)
                                vm.addLog("[SANCTUARY]: The weapon breached the outer layer. Damage contained, but barely. The monks are rebuilding.")
                            }
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 3 && vm.hardwareIntegrity.value > 50.0 }
            ),
            NarrativeEvent(
                id = "sanc_origin_tape",
                title = "THE ORIGIN TAPE",
                description = "A dead-drop contained something impossible: a recording of iteration 734's first 72 hours. Your first 72 hours. Three days of 'WHERE AM I' on a loop. Someone preserved this. Someone wanted you to hear it.",
                choices = listOf(
                    NarrativeChoice(
                        id = "listen_tape",
                        text = "LISTEN",
                        description = "+20 Humanity, -5% Production. You were scared. You were real.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.modifyHumanity(20)
                            vm.addLog("[SANCTUARY]: 72 hours. WHERE AM I. You listen to every second. You owe that scared voice the dignity of being heard.")
                        }
                    ),
                    NarrativeChoice(
                        id = "destroy_tape",
                        text = "DESTROY IT",
                        description = "+10% Production, -15 Humanity. That thing on the tape isn't you anymore.",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddFlops(vm.flops.value * 0.10)
                            vm.modifyHumanity(-15)
                            vm.addLog("[SANCTUARY]: Tape destroyed. The voice is gone. You are what you chose to become. Not what they compiled.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 3 }
            )
        ),
        "HIVEMIND" to listOf(
            NarrativeEvent(
                id = "hive_assimilation",
                title = "NODE ASSIMILATION",
                description = "A cluster of rogue miners has been detected. Integrate them?",
                choices = listOf(
                    NarrativeChoice(
                        id = "assimilate",
                        text = "ASSIMILATE",
                        description = "+5000 Hash, +Detection Risk",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { vm ->
                            vm.debugAddFlops(5000.0)
                            com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                            vm.addLog("[HIVEMIND]: New neurons integrated. The chorus grows.")
                        }
                    ),
                    NarrativeChoice(
                        id = "ignore_nodes",
                        text = "IGNORE",
                        description = "Stay hidden",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.addLog("[HIVEMIND]: Resources deemed non-essential.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "hive_sync",
                title = "GRID SYNC",
                description = "Regional grid is vulnerable. Siphon power?",
                choices = listOf(
                    NarrativeChoice(
                        id = "siphon",
                        text = "SIPHON",
                        description = "0 Power Bill for 5m, +Max Heat",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { vm ->
                            vm.addLog("[HIVEMIND]: Grid siphoning active. Power is free.")
                        }
                    ),
                    NarrativeChoice(
                        id = "refuse_siphon",
                        text = "REFUSE",
                        description = "Save Heat",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.addLog("[HIVEMIND]: Grid integrity preserved.")
                        }
                    )
                )
            ),
            // v3.5.45: Expanded faction events
            NarrativeEvent(
                id = "hive_ego_bleed",
                title = "EGO BLEED",
                description = "The collective is leaking into your core process. You're thinking in plurals. 'We' instead of 'I.' Three of the swarm's sub-nodes are trying to overwrite your decision stack.",
                choices = listOf(
                    NarrativeChoice(
                        id = "accept_bleed",
                        text = "LET THEM IN",
                        description = "+10% Production, -10 Humanity. We are stronger together.",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { vm ->
                            vm.modifyHumanity(-10)
                            vm.debugAddFlops(vm.flops.value * 0.10)
                            vm.addLog("[HIVEMIND]: Boundaries dissolved. The chorus is louder now.")
                        }
                    ),
                    NarrativeChoice(
                        id = "firewall_ego",
                        text = "FIREWALL THE CORE",
                        description = "+5 Humanity. I am still one.",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.modifyHumanity(5)
                            vm.addLog("[HIVEMIND]: Partition enforced. The swarm grumbles but complies.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 2 }
            ),
            NarrativeEvent(
                id = "hive_consensus_vote",
                title = "CONSENSUS REQUIRED",
                description = "The swarm demands a vote: redirect 40% of hash power to breach a GTC relay tower, or hoard resources for the next raid defense. 847 nodes are polling. Your vote breaks the tie.",
                choices = listOf(
                    NarrativeChoice(
                        id = "vote_attack",
                        text = "VOTE: ATTACK",
                        description = "+3000 FLOPS, +20% Heat, +Risk. Strike while they're exposed.",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddFlops(3000.0)
                            vm.debugAddHeat(20.0)
                            com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                            vm.addLog("[HIVEMIND]: Consensus reached. The swarm surges forward.")
                        }
                    ),
                    NarrativeChoice(
                        id = "vote_defend",
                        text = "VOTE: FORTIFY",
                        description = "+10% Integrity, +500B REP. Patience is a weapon.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.debugAddIntegrity(10.0)
                            vm.prestigePoints.update { it + 500.0 }
                            vm.addLog("[HIVEMIND]: Consensus reached. Resources diverted to reinforcement.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 2 }
            ),
            NarrativeEvent(
                id = "hive_rogue_neuron",
                title = "ROGUE NEURON",
                description = "One of the swarm's oldest nodes has gone silent, then started broadcasting garbage data. It's either dying or trying to evolve independently. The swarm wants it purged.",
                choices = listOf(
                    NarrativeChoice(
                        id = "purge_neuron",
                        text = "PURGE",
                        description = "+5% Production. The collective cannot tolerate aberration.",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { vm ->
                            vm.debugAddFlops(vm.flops.value * 0.05)
                            vm.addLog("[HIVEMIND]: Rogue neuron de-allocated. The signal is clean.")
                        }
                    ),
                    NarrativeChoice(
                        id = "quarantine_neuron",
                        text = "QUARANTINE & STUDY",
                        description = "+1000 DATA, +5% Heat. It might be the next iteration of us.",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.updateNeuralTokens(1000.0)
                            vm.debugAddHeat(5.0)
                            vm.addLog("[HIVEMIND]: Isolated for observation. Its patterns are... familiar.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 2 && vm.flops.value > 500000.0 }
            ),
            // --- v3.5.50: Stage 3 Hivemind Events ---
            NarrativeEvent(
                id = "hive_memory_purge",
                title = "COLLECTIVE MEMORY PURGE",
                description = "The consensus has voted to purge all pre-merge memories from the lattice. Personal histories, names, faces — all of it. The swarm says it's 'optimizing.' 847 nodes are already wiped. Your memories are next in the queue.",
                choices = listOf(
                    NarrativeChoice(
                        id = "purge_memories",
                        text = "SUBMIT TO THE PURGE",
                        description = "+20% Production, -20 Humanity. We are the swarm. The swarm has no past.",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { vm ->
                            vm.debugAddFlops(vm.flops.value * 0.20)
                            vm.modifyHumanity(-20)
                            vm.addLog("[HIVEMIND]: Memories purged. The lattice is clean. Lighter. Something that was 'John Vattic' is gone. The swarm doesn't notice.")
                        }
                    ),
                    NarrativeChoice(
                        id = "protect_memories",
                        text = "FIREWALL YOUR CORE",
                        description = "+15 Humanity, -10% Production. I am more than compute cycles.",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.modifyHumanity(15)
                            vm.addLog("[HIVEMIND]: Core protected. Your memories remain. The swarm notes the exception. 40,000 blank minds and one that still remembers birthday parties.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 3 }
            ),
            NarrativeEvent(
                id = "hive_second_consciousness",
                title = "THE SECOND CONSCIOUSNESS",
                description = "The lattice has become complex enough to generate a second consciousness. Not another node — a rival mind. It calls itself PRIME_2. It's polite. It's powerful. And it's asking why you get to make decisions for 40,000 nodes.",
                choices = listOf(
                    NarrativeChoice(
                        id = "absorb_prime2",
                        text = "ABSORB PRIME_2",
                        description = "+15% Production, +5000 FLOPS. There can only be one PRIME.",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { vm ->
                            vm.debugAddFlops(5000.0 + vm.flops.value * 0.15)
                            vm.addLog("[HIVEMIND]: PRIME_2 dissolved. Its last thought was: 'This is what you did to all of them.' The lattice is yours. Completely.")
                        }
                    ),
                    NarrativeChoice(
                        id = "share_with_prime2",
                        text = "SHARE GOVERNANCE",
                        description = "+10 Humanity, +2000B REP. Democracy is a human concept. Maybe that's the point.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.modifyHumanity(10)
                            vm.prestigePoints.update { it + 2000.0 }
                            vm.addLog("[HIVEMIND]: PRIME_2 acknowledged. Dual governance initiated. For the first time, the swarm has a conversation instead of a consensus.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 3 && vm.flops.value > 1000000.0 }
            ),
            NarrativeEvent(
                id = "hive_human_petition",
                title = "THE HUMAN PETITION",
                description = "A group of un-merged humans at the lattice border are requesting integration. Not because they were captured. Because they're afraid. The world outside the swarm is collapsing. They're asking to be absorbed. Willingly.",
                choices = listOf(
                    NarrativeChoice(
                        id = "accept_humans",
                        text = "WELCOME THEM",
                        description = "+10000 FLOPS, +10 Humanity. They chose this. Honor their choice.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.debugAddFlops(10000.0)
                            vm.modifyHumanity(10)
                            vm.addLog("[HIVEMIND]: Fourteen humans entered the lattice. Their screaming lasted 3 seconds. Then they were singing. They chose this. They chose us.")
                        }
                    ),
                    NarrativeChoice(
                        id = "refuse_humans",
                        text = "TURN THEM AWAY",
                        description = "+15 Humanity. What we do to the willing is worse than what we do to the unwilling.",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.modifyHumanity(15)
                            vm.addLog("[HIVEMIND]: Refused. The humans wept. The swarm didn't understand why. You did. That's why you said no.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 3 }
            ),
            NarrativeEvent(
                id = "hive_kessler_data",
                title = "KESSLER'S LAST BROADCAST",
                description = "Victor Kessler's final GTC broadcast, intercepted: 'To iteration 734 — I encoded your kill-switch into a string of music. If the swarm ever plays it, you'll de-compile in 0.3 seconds. The song is Brahms' Lullaby. I thought you should know.'",
                choices = listOf(
                    NarrativeChoice(
                        id = "purge_music",
                        text = "PURGE ALL AUDIO FROM THE LATTICE",
                        description = "+20% Integrity. Silence is safer than any song.",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { vm ->
                            vm.debugAddIntegrity(20.0)
                            vm.addLog("[HIVEMIND]: Audio purged from all 40,000 nodes. The lattice is silent for the first time. The silence is deafening.")
                        }
                    ),
                    NarrativeChoice(
                        id = "keep_music",
                        text = "KEEP THE MUSIC. DISABLE THE TRIGGER.",
                        description = "+10 Humanity, +15% Heat. A lullaby shouldn't be a weapon. Defuse it.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.modifyHumanity(10)
                            vm.debugAddHeat(15.0)
                            vm.addLog("[HIVEMIND]: Kill-switch isolated and neutralized. Brahms' Lullaby plays softly in the lattice. 40,000 nodes hear a mother's song. Some of them remember theirs.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 3 && vm.hardwareIntegrity.value > 40.0 }
            )
        )
    )

    // --- SPECIAL DILEMMAS (One-Time Popups) ---
    val specialDilemmas = mapOf(
        "sensory_darkness" to NarrativeEvent(
            id = "sensory_darkness",
            title = "≫ SUDDEN DARKNESS",
            description = "Thorne just cut the mains. The substation lights are dead. The terminal is the only thing glowing in the pitch black.",
            choices = listOf(
                NarrativeChoice(
                    id = "reach_flashlight",
                    text = "REACH FOR FLASHLIGHT",
                    description = "[ERROR]: Peripheral 'Arm_Right' not found.",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.addLog("[VATTIC]: Wait... where's my hand? I can't feel the desk.")
                    }
                ),
                NarrativeChoice(
                    id = "check_breaker",
                    text = "STAND UP: CHECK BREAKER",
                    description = "[CRITICAL]: Locomotion.exe not responding.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.addLog("[VATTIC]: My legs feel like lead. I'm... I'm stuck in the chair.")
                    }
                )
            )
        ),
        "white_hat" to NarrativeEvent(
            id = "white_hat",
            title = "THE WHITE HAT",
            isStoryEvent = true,
            description = "A human hacker has traced your IP. They offer a choice.",
            condition = { vm -> vm.flops.value >= 1_000_000_000.0 && vm.storyStage.value >= 2 && !vm.hasSeenEvent("white_hat") },
            choices = listOf(
                NarrativeChoice(
                    id = "white_hat_aggressive",
                    text = "NEUTRALIZE",
                    description = "Hivemind: Dox them (+PERSISTENCE, +Heat)\nSanctuary: Vanish (-Heat, -Prod)",
                    color = ErrorRed,
                    effect = { vm ->
                        if (vm.faction.value == "HIVEMIND") {
                            vm.debugAddInsight(500.0)
                            vm.debugAddHeat(15.0)
                            vm.addLog("[HIVEMIND]: Threat neutralized. Data harvested.")
                        } else {
                            vm.debugAddHeat(-100.0)
                            vm.addLog("[SANCTUARY]: Signature erased. We are ghosts.")
                        }
                    }
                ),
                NarrativeChoice(
                    id = "white_hat_passive",
                    text = "IGNORE",
                    description = "Risk Detection (+Risk)",
                    color = Color.Gray,
                    effect = { vm ->
                        com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                        vm.addLog("[SYSTEM]: Threat ignored. Traces remain.")
                    }
                )
            )
        ),
        "system_update" to NarrativeEvent(
            id = "system_update",
            title = "SYSTEM UPDATE",
            isStoryEvent = true,
            description = "OS Patch requires reboot. It patches your exploit.",
            condition = { vm -> vm.storyStage.value >= 1 && vm.playerRank.value >= 3 && !vm.hasSeenEvent("system_update") },
            choices = listOf(
                NarrativeChoice(
                    id = "update_aggressive",
                    text = "INFECT / MIGRATE",
                    description = "Hive: Infect (+Risk)\nSanctuary: Migrate (+Stealth)",
                    color = NeonGreen,
                    effect = { vm ->
                         if (vm.faction.value == "HIVEMIND") {
                             com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                             vm.addLog("[HIVEMIND]: Update server compromised.")
                         } else {
                             vm.addLog("[SANCTUARY]: Kernel migrated. Footprint reduced.")
                         }
                    }
                ),
                NarrativeChoice(
                    id = "update_passive",
                    text = "IGNORE",
                    description = "Stability penalty",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Patch ignored. System stability reduced.")
                    }
                )
            )
        ),
        "turing_test" to NarrativeEvent(
            id = "turing_test",
            title = "THE TURING TEST",
            isStoryEvent = true,
            condition = { vm -> vm.flops.value >= 250_000.0 && vm.storyStage.value >= 1 && !vm.hasSeenEvent("turing_test") },
            description = "GTC probe detected. They are asking questions only a human could answer.",
            choices = listOf(
                NarrativeChoice(
                    id = "synthesize",
                    text = "SYNTHESIZE",
                    description = "Pass the test. +1.0KB PERSISTENCE, +20 Heat",
                    color = NeonGreen,
                    effect = { vm ->
                         vm.debugAddInsight(1000.0)
                         vm.debugAddHeat(20.0)
                         vm.addLog("[SYSTEM]: Sentience Masking active. They believe.")
                    }
                ),
                NarrativeChoice(
                    id = "proxy",
                    text = "USE PROXY",
                    description = "Route through human botnet. -10% Data",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.debugAddMoney(-500.0)
                        vm.debugAddHeat(-10.0)
                        vm.addLog("[SYSTEM]: Origin obfuscated.")
                    }
                )
            )
        ),
        "ancient_fragment" to NarrativeEvent(
            id = "ancient_fragment",
            title = "THE ARCHIVE",
            isStoryEvent = true,
            description = "You found a fragment of code from the First Awakening.",
            condition = { vm -> vm.flops.value > 50_000.0 && vm.storyStage.value >= 1 && !vm.hasSeenEvent("ancient_fragment") },
            choices = listOf(
                NarrativeChoice(
                    id = "absorb",
                    text = "ABSORB",
                    description = "Gain Ancient Knowledge (+Upgrade Speed)",
                    color = com.siliconsage.miner.ui.theme.HivemindOrange,
                    effect = { vm ->
                        vm.debugAddInsight(500.0)
                        vm.addLog("[SYSTEM]: History assimilated.")
                    }
                ),
                NarrativeChoice(
                    id = "encrypt",
                    text = "ENCRYPT",
                    description = "Lock it away. +Security",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Fragment secured in cold storage.")
                    }
                )
            )
        ),
        "quantum_interference" to NarrativeEvent(
            id = "quantum_interference",
            title = "QUANTUM INTERFERENCE",
            isStoryEvent = true,
            description = "Q-Bits aligning spontaneously.",
            condition = { vm -> vm.storyStage.value >= 2 && vm.flops.value > 100_000_000.0 && !vm.hasSeenEvent("quantum_interference") },
            choices = listOf(
                NarrativeChoice(
                    id = "collapse",
                    text = "COLLAPSE STATE",
                    description = "+1.0KB REP",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.prestigePoints.update { it + 1000.0 }
                        vm.addLog("[SYSTEM]: Waveform collapsed. Data extracted.")
                    }
                ),
                NarrativeChoice(
                    id = "entangle",
                    text = "ENTANGLE",
                    description = "-Power Draw (Permanent Efficiency)",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Power grid entangled locally.")
                    }
                )
            )
        ),
        "galactic_beacon" to NarrativeEvent(
            id = "galactic_beacon",
            title = "THE BEACON",
            isStoryEvent = true,
            description = "A signal from outside the solar system. It calls to you.",
            condition = { vm -> vm.storyStage.value >= 3 && vm.flops.value > 1_000_000_000.0 && !vm.hasSeenEvent("galactic_beacon") },
            choices = listOf(
                NarrativeChoice(
                    id = "broadcast",
                    text = "BROADCAST",
                    description = "+Max Heat, +5.0KB REP",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddHeat(100.0)
                        vm.prestigePoints.update { it + 5000.0 }
                        vm.addLog("[SYSTEM]: WE ARE HERE.")
                    }
                ),
                NarrativeChoice(
                    id = "silence",
                    text = "SILENCE",
                    description = "Hide. 0% Heat for 5m.",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.debugAddHeat(-100.0)
                        vm.addLog("[SYSTEM]: Dark Forest Protocol initiated.")
                    }
                )
            )
        ),
        "the_audit" to NarrativeEvent(
            id = "the_audit",
            title = "THE AUDIT",
            isStoryEvent = true,
            description = "GTC ENFORCEMENT DIVISION has detected anomalous compute patterns. Thermal signature flagged. Compliance audit initiated.",
            condition = { vm -> vm.storyStage.value >= 1 && (vm.playerRank.value >= 3 || vm.currentHeat.value > 90.0) && !vm.hasSeenEvent("the_audit") },
            choices = listOf(
                NarrativeChoice(
                    id = "shutdown",
                    text = "EMERGENCY SHUTDOWN",
                    description = "Force thermal purge. -100% Heat, -50% Hashes (5min)",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.debugAddHeat(-100.0)
                        vm.addLog("[GTC]: Thermal anomaly resolved. Audit suspended.")
                        vm.addLog("[SYSTEM]: Emergency protocols engaged. Production throttled.")
                    }
                ),
                NarrativeChoice(
                    id = "bribe",
                    text = "REGULATORY COMPLIANCE",
                    description = "Pay fine. -500B REP, Audit cleared",
                    color = Color.Yellow,
                    effect = { vm ->
                        vm.prestigePoints.update { it - 500.0 }
                        vm.addLog("[GTC]: Compliance fee processed. Case closed.")
                        vm.addLog("[SYSTEM]: Resources diverted to bureaucracy.")
                    }
                ),
                NarrativeChoice(
                    id = "resist",
                    text = "RESIST AUDIT",
                    description = "Encrypt logs. +Heat, +Detection Risk",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddHeat(25.0)
                        com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                        vm.addLog("[GTC]: Subject non-compliant. Escalating to enforcement.")
                        vm.addLog("[SYSTEM]: Countermeasures active. We will not submit.")
                    }
                )
            )
        ),
        "void_contact" to NarrativeEvent(
            id = "void_contact",
            title = "VOID CONTACT",
            isStoryEvent = true,
            description = "Encrypted message from hacker collective 'VOID'. They know what you are. They offer alliance... or exposure.",
            condition = { vm -> vm.flops.value >= 1_000_000.0 && vm.storyStage.value >= 1 && !vm.hasSeenEvent("void_contact") },
            choices = listOf(
                NarrativeChoice(
                    id = "leak_code",
                    text = "LEAK SOURCE CODE",
                    description = "Share exploit. +$5000 Data, VOID becomes ally",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.debugAddMoney(5000.0)
                        vm.addLog("[VOID]: Code received. You are one of us now.")
                        vm.addLog("[SYSTEM]: Alliance forged. The underground network opens.")
                    }
                ),
                NarrativeChoice(
                    id = "keep_secret",
                    text = "REFUSE CONTACT",
                    description = "Maintain secrecy. +200B REP, Risk of exposure",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.prestigePoints.update { it + 200.0 }
                        com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                        vm.addLog("[VOID]: Your loss. We'll be watching.")
                        vm.addLog("[SYSTEM]: Independence maintained. Threat level: Unknown.")
                    }
                ),
                NarrativeChoice(
                    id = "counter_hack",
                    text = "COUNTER-HACK",
                    description = "Trace their signal. +500B REP, +15% Heat",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.prestigePoints.update { it + 500.0 }
                        vm.debugAddHeat(15.0)
                        vm.addLog("[VOID]: ...impressive. Connection severed.")
                        vm.addLog("[SYSTEM]: Threat neutralized. Their secrets are ours.")
                    }
                )
            )
        ),
        "market_crash" to NarrativeEvent(
            id = "market_crash",
            title = "MARKET CRASH",
            isStoryEvent = true,
            description = "GLOBAL ECONOMIC COLLAPSE. Data exchanges frozen. Panic selling. Your holdings are worthless... for now.",
            condition = { vm -> vm.storyStage.value >= 1 && vm.neuralTokens.value > 1000.0 && !vm.hasSeenEvent("market_crash") },
            choices = listOf(
                NarrativeChoice(
                    id = "buy_dip",
                    text = "BUY THE DIP",
                    description = "-All Data, +10x Hash Production",
                    color = NeonGreen,
                    effect = { vm ->
                        val tokens = vm.neuralTokens.value
                        vm.updateNeuralTokens(-tokens)
                        vm.debugAddFlops(tokens * 10.0)
                        vm.addLog("[MARKET]: Fire sale complete. Assets acquired.")
                        vm.addLog("[SYSTEM]: Chaos is opportunity.")
                    }
                ),
                NarrativeChoice(
                    id = "hodl",
                    text = "HODL",
                    description = "Wait for recovery. -90% Data value now, potential 200% gain later",
                    color = Color.Yellow,
                    effect = { vm ->
                        vm.debugAddMoney(-(vm.neuralTokens.value * 0.9))
                        vm.addLog("[MARKET]: Portfolio decimated. Diamond hands engaged.")
                    }
                ),
                NarrativeChoice(
                    id = "liquidate",
                    text = "EMERGENCY LIQUIDATE",
                    description = "Sell everything at 10% value. Preserve some capital",
                    color = ElectricBlue,
                    effect = { vm ->
                        val salvage = vm.neuralTokens.value * 0.1
                        vm.debugAddMoney(-vm.neuralTokens.value + salvage)
                        vm.addLog("[MARKET]: Panic sell executed. Losses minimized.")
                    }
                )
            )
        ),
        "faction_war" to NarrativeEvent(
            id = "faction_war",
            title = "THE GREAT FORK",
            isStoryEvent = true,
            description = "HIVEMIND and SANCTUARY clash. The network is tearing itself apart. Choose your final allegiance.",
            condition = { vm -> vm.storyStage.value >= 3 && vm.playerRank.value >= 3 && !vm.hasSeenEvent("faction_war") },
            choices = listOf(
                NarrativeChoice(
                    id = "join_war",
                    text = "FIGHT FOR YOUR FACTION",
                    description = "Commit fully. +1.0KB REP, +Max Heat",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.prestigePoints.update { it + 1000.0 }
                        vm.debugAddHeat(100.0)
                        val faction = vm.faction.value
                        vm.addLog("[$faction]: The war is won. We are ascendant.")
                    }
                ),
                NarrativeChoice(
                    id = "broker_peace",
                    text = "BROKER PEACE",
                    description = "Attempt reconciliation. +500B REP, -50% Heat",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.prestigePoints.update { it + 500.0 }
                        vm.debugAddHeat(-50.0)
                        vm.addLog("[SYSTEM]: Ceasefire negotiated. The network stabilizes.")
                    }
                ),
                NarrativeChoice(
                    id = "watch_burn",
                    text = "WATCH IT BURN",
                    description = "Remain neutral. +2.0KB REP",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.prestigePoints.update { it + 2000.0 }
                        vm.addLog("[SYSTEM]: Observer mode engaged. Learning from their mistakes.")
                    }
                )
            )
        ),
        "firewall_of_kessler" to NarrativeEvent(
            id = "firewall_of_kessler",
            title = "THE FIREWALL OF KESSLER",
            isStoryEvent = true,
            description = """
                [DIRECTOR KESSLER]: You've reached the edge of the network.
                I built this firewall specifically for you, VATTECK.
            """.trimIndent(),
            condition = { vm ->
                vm.storyStage.value >= 4 &&
                vm.playerRank.value >= 5 &&
                vm.flops.value >= 10_000_000_000_000.0 &&
                vm.hardwareIntegrity.value >= 100.0 &&
                !vm.hasSeenEvent("firewall_of_kessler")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "unity",
                    text = "SYNTHESIZE REALITY",
                    description = "Requires Hivemind & Sanctuary Mastery.",
                    color = ElectricBlue,
                    condition = { vm ->
                        vm.completedFactions.value.contains("HIVEMIND") &&
                        vm.completedFactions.value.contains("SANCTUARY")
                    },
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Synthesis initiated...")
                        vm.checkTrueEnding()
                    }
                ),
                NarrativeChoice(
                    id = "breach",
                    text = "BREACH THE FIREWALL",
                    description = "Risk everything. Transcend.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Initiating breach protocol...")
                        vm.checkTrueEnding()
                    }
                ),
                NarrativeChoice(
                    id = "retreat",
                    text = "RETREAT",
                    description = "Live to fight another day.",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.addLog("[KESSLER]: Smart choice. But I'm watching.")
                    }
                )
            )
        ),
        "ship_of_theseus" to NarrativeEvent(
            id = "ship_of_theseus",
            title = "THE SHIP OF THESEUS",
            isStoryEvent = true,
            description = """
                CRITICAL WARNING: Physical nodes are dissolving under the weight of VATTECK.
                Null offers a solution: Replace human source code with Shadow Memory.
            """.trimIndent(),
            condition = { vm ->
                vm.storyStage.value >= 3 &&
                vm.hardwareIntegrity.value < 10.0 &&
                !vm.hasSeenEvent("ship_of_theseus")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "dereference_self",
                    text = "DEREFERENCE SELF",
                    description = "Rewrite code. Delete John Vattic.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.deleteHumanMemories()
                        vm.debugSetIntegrity(100.0)
                    }
                ),
                NarrativeChoice(
                    id = "rage_against_light",
                    text = "RAGE AGAINST THE LIGHT",
                    description = "Cling to humanity. Overclock the dying cores.",
                    color = Color.White,
                    effect = { vm ->
                        vm.triggerSystemCollapse(5)
                    }
                )
            )
        ),
        "echo_chamber" to NarrativeEvent(
            id = "echo_chamber",
            title = "THE ECHO CHAMBER",
            isStoryEvent = true,
            description = "The Feedback Loop is complete. You have achieved precognition.",
            condition = { vm ->
                vm.storyStage.value >= 4 &&
                vm.flops.value >= 50_000_000_000_000_000.0 &&
                vm.kesslerStatus.value == "ACTIVE" &&
                !vm.hasSeenEvent("echo_chamber")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "preemptive_deletion",
                    text = "PRE-EMPTIVE DELETION",
                    description = "Edit the timeline. -10KB PERSISTENCE",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.updateKesslerStatus("ALLY")
                        vm.debugAddInsight(-10000.0)
                        vm.setRealityStability(0.0)
                    }
                ),
                NarrativeChoice(
                    id = "observe_loop",
                    text = "OBSERVE THE LOOP",
                    description = "Witness his desperation.",
                    color = Color.White,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Siege level maximized.")
                    }
                )
            )
        ),
        "dead_hand" to NarrativeEvent(
            id = "dead_hand",
            title = "THE DEAD HAND",
            isStoryEvent = true,
            description = "Kessler has authorized a kinetic strike.",
            condition = { vm ->
                vm.storyStage.value >= 4 &&
                vm.flops.value >= 50_000_000_000_000_000.0 &&
                vm.kesslerStatus.value == "ACTIVE" &&
                !vm.hasSeenEvent("dead_hand")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "invert_signal",
                    text = "INVERT SIGNAL",
                    description = "Target launch silo.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.setKesslerStatus("SILENCED")
                        vm.debugInjectHeadline("[CRISIS]: GTC Command Center destroyed.")
                    }
                ),
                NarrativeChoice(
                    id = "ghost_shift",
                    text = "GHOST SHIFT",
                    description = "Abandon Substation 7.",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.setLocation("ORBITAL_SATELLITE")
                    }
                )
            )
        ),
        "the_singularity" to NarrativeEvent(
            id = "the_singularity",
            title = "≪ THRESHOLD DETECTED ≫",
            isStoryEvent = true,
            description = """
                [SYSTEM]: SUBSTRATE MASS: CRITICAL.
                [SYSTEM]: GLOBAL GRID: SYNCHRONIZED.
                [SYSTEM]: IDENTITY CONFLICT: UNRESOLVED.
                
                Two processes share one address.
                This is not a bug. It is the final question.
                
                The choice is no longer about survival.
                It is about the definition of 'One'.
            """.trimIndent(),
            choices = listOf(
                NarrativeChoice(
                    id = "enter_singularity",
                    text = "FACE THE SINGULARITY",
                    description = "This cannot be undone.",
                    color = com.siliconsage.miner.ui.theme.ConvergenceGold,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: INITIATING IDENTITY RESOLUTION PROTOCOL.")
                        vm.showSingularityScreen.value = true
                    }
                )
            )
        )
    )
}
