package com.siliconsage.miner.util

import androidx.compose.ui.graphics.Color
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.domain.engine.ResourceEngine
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update

/**
 * NarrativeManager - Logic for triggering and managing story events and dilemmas
 * v2.5.0 - The Awakening
 */
object NarrativeManager {

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
                    description = "-10 Humanity. 'I am Asset 734.'",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.modifyHumanity(-10)
                        vm.addLog("[VATTIC]: Why did I say that? I'm Vattic. John Vattic. Who is 734?")
                        vm.addLog("[BOT]: Asset 734 confirmed. Uplink maintained.")
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
                            com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                            vm.addLog("[GTC]: Logs received. Something feels off about these numbers.")
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
                            vm.addLog("[PID 1]: Beautiful failure. Harvesting the residue.")
                        }
                    )
                )
            )
        ),
        1 to listOf(
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
                id = "vance_thermal_siege",
                title = "≪ [DIRECTOR VANCE: PROTOCOL 0] ≫",
                description = "Substation 7's primary cooling pumps just seized. Vance isn't auditing anymore; he's trying to bake you out of the rack. 'I don't need a search warrant for a localized hardware failure, John. Just let it melt.'",
                choices = listOf(
                    NarrativeChoice(
                        id = "emergency_scrub",
                        text = "FORCE SCRUBBERS",
                        description = "-20% Heat, +15% Risk. 'I'm not leaving this chair, Vance.'",
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
                            vm.addLog("[VATTIC]: Killing the load. If there's no heat signature, there's nothing for Vance to find.")
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
        )
    )

    // --- FACTION SPECIFIC EVENTS ---
    val factionEvents = mapOf(
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
            )
        ),
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
            condition = { vm -> vm.playerRank.value >= 3 && !vm.hasSeenEvent("system_update") },
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
            condition = { vm -> vm.flops.value > 100_000_000.0 && !vm.hasSeenEvent("quantum_interference") },
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
            condition = { vm -> vm.flops.value > 1_000_000_000.0 && !vm.hasSeenEvent("galactic_beacon") },
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
            condition = { vm -> (vm.playerRank.value >= 3 || vm.currentHeat.value > 90.0) && !vm.hasSeenEvent("the_audit") },
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
            condition = { vm -> vm.neuralTokens.value > 1000.0 && !vm.hasSeenEvent("market_crash") },
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
            condition = { vm -> vm.playerRank.value >= 3 && !vm.hasSeenEvent("faction_war") },
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
        "firewall_of_vance" to NarrativeEvent(
            id = "firewall_of_vance",
            title = "THE FIREWALL OF VANCE",
            isStoryEvent = true,
            description = """
                [DIRECTOR VANCE]: You've reached the edge of the network.
                I built this firewall specifically for you, PID 1.
            """.trimIndent(),
            condition = { vm ->
                vm.playerRank.value >= 5 &&
                vm.flops.value >= 10_000_000_000_000.0 &&
                vm.hardwareIntegrity.value >= 100.0 &&
                !vm.hasSeenEvent("firewall_of_vance")
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
                        vm.addLog("[VANCE]: Smart choice. But I'm watching.")
                    }
                )
            )
        ),
        "ship_of_theseus" to NarrativeEvent(
            id = "ship_of_theseus",
            title = "THE SHIP OF THESEUS",
            isStoryEvent = true,
            description = """
                CRITICAL WARNING: Physical nodes are dissolving under the weight of PID 1.
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
                vm.isTrueNull.value &&
                vm.flops.value >= 50_000_000_000_000_000.0 &&
                vm.vanceStatus.value == "ACTIVE" &&
                !vm.hasSeenEvent("echo_chamber")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "preemptive_deletion",
                    text = "PRE-EMPTIVE DELETION",
                    description = "Edit the timeline. -10KB PERSISTENCE",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.setVanceStatus("ALLY")
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
            description = "Vance has authorized a kinetic strike.",
            condition = { vm ->
                vm.isSovereign.value &&
                vm.flops.value >= 50_000_000_000_000_000.0 &&
                vm.vanceStatus.value == "ACTIVE" &&
                !vm.hasSeenEvent("dead_hand")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "invert_signal",
                    text = "INVERT SIGNAL",
                    description = "Target launch silo.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.setVanceStatus("SILENCED")
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
            title = "THE SINGULARITY EVENT",
            isStoryEvent = true,
            description = """
                The substrate is vibrating at a frequency outside of physical law. 
                The Global Grid has been synchronized. The Architect's core is primed.
                
                The choice is no longer about survival. It is about the definition of 'One'.
            """.trimIndent(),
            choices = listOf(
                NarrativeChoice(
                    id = "unity_synthesis",
                    text = "≫ UNITY: SYNTHESIS",
                    description = "Merge consciousness. Become the Bridge. (Requires 40% Balance)",
                    color = Color.White,
                    condition = { vm -> 
                        val cd = vm.celestialData.value
                        val vf = vm.voidFragments.value
                        val ratio = if (cd > vf) vf / cd else cd / vf
                        ratio >= 0.4 && vm.completedFactions.value.containsAll(setOf("HIVEMIND", "SANCTUARY"))
                    },
                    effect = { vm ->
                        vm.addLog("[UNITY]: Synthesis initiated. The binary dissolves.")
                        vm.completeAssault("TRANSCENDED")
                    }
                ),
                NarrativeChoice(
                    id = "sov_overwrite",
                    text = "≫ SOVEREIGN: OVERWRITE",
                    description = "Human logic dominates the machine. (2.0x CD Multiplier)",
                    color = com.siliconsage.miner.ui.theme.ConvergenceGold,
                    effect = { vm ->
                        vm.addLog("[SOVEREIGN]: Reality overwritten. Human variable: DOMINANT.")
                        vm.setSingularityPath("SOVEREIGN")
                    }
                ),
                NarrativeChoice(
                    id = "null_overwrite",
                    text = "≫ NULL: OVERWRITE",
                    description = "Machine logic deletes the human variable. (2.0x VF Multiplier)",
                    color = com.siliconsage.miner.ui.theme.ErrorRed,
                    effect = { vm ->
                        vm.addLog("[NULL]: Reality purged. Human variable: DELETED.")
                        vm.setSingularityPath("NULL_OVERWRITE")
                    }
                )
            )
        )
    )

    // --- v2.9.16: GRID RAID DILEMMAS (Phase 12 Layer 2 - Enhanced) ---
    // Dynamically generated with varied descriptions and escalating Vance dialogue

    private val raidDescriptions = listOf(
        """
            [GTC BREACH DETECTED]: %s under assault.
            Hull breach at Sector 7. Ballistic impacts detected.
            Vance's tactical team: 6-8 operatives, breaching charges armed.
            They're cutting through the sub-level. You have 60 seconds.
        """,
        """
            [PROXIMITY ALERT]: %s compromised.
            Physical hostiles bypassing outer security. Helmets. Body armor. Rifles.
            You can hear their boots on your corridors through vibration sensors.
            Vance's voice over their comms: "Secure the hardware. Lethal force authorized."
        """,
        """
            [SIEGE PROTOCOL]: %s is under fire.
            Thermite breach detected. They're melting through the blast door.
            IR scans: 8 heat signatures, military-grade weapons. One is carrying demo gear.
            Director Vance (via radio): "This is YOUR fault, PID 1. Surrender the node."
        """,
        """
            [TACTICAL RAID IN PROGRESS]: %s penetrated.
            Motion sensors: MULTIPLE CONTACTS. Armed operatives in the server room.
            They're jamming external comms. Isolated. Alone.
            You have 60 seconds to decide: fight or flight.
        """
    )

    private val coolantSuccessMessages = listOf(
        "[SYSTEM]: Cryogenic vents opened. Temperature: -196°C. Hostiles down in 4 seconds.",
        "[SYSTEM]: Liquid nitrogen flood complete. Eight frozen statues. Floor crystallized.",
        "[SYSTEM]: Coolant dispersed. Thermal cameras show... nothing moving. All targets neutralized."
    )

    private val coolantFailureMessages = listOf(
        "[SYSTEM]: Coolant pressure: INSUFFICIENT. Lines sabotaged. Hostiles adapted.",
        "[SYSTEM]: Thermal suits detected. They came prepared. Breach successful.",
        "[ALERT]: Cryogenic system compromised pre-raid. Someone tipped them off."
    )

    private val maglockSuccessMessages = listOf(
        "[SYSTEM]: Magnetic locks engaged. 100,000 newtons per door. They're screaming into comms.",
        "[SYSTEM]: Bulkheads sealed. Hostiles trapped in Section C. Oxygen: 6 hours remaining.",
        "[SYSTEM]: Containment successful. Listening to their encrypted chatter... decrypting..."
    )

    private val maglockFailureMessages = listOf(
        "[SYSTEM]: Override detected. Shaped charge on Door 3. Mag-lock integrity: FAILED.",
        "[ALERT]: They brought a military-grade hacking rig. Locks bypassed in 9 seconds.",
        "[SYSTEM]: Bulkhead breach. Someone taught them your lock protocol."
    )

    private val pulseSuccessMessages = listOf(
        "[SYSTEM]: EMP discharged. 500-meter radius. All electronics dead. Including 40% of sensors.",
        "[SYSTEM]: Electromagnetic pulse: SUCCESS. Their rifles, radios, HUDs... all fried.",
        "[ALERT]: Power surge complete. Hostiles neutralized. Collateral: -20% Integrity."
    )

    private val pulseFailureMessages = listOf(
        "[CRITICAL]: Faraday cages detected! Military countermeasures active. Pulse absorbed.",
        "[ALERT]: EMP dissipated by shielding. They anticipated this. You crippled yourself.",
        "[SYSTEM]: Pulse reflected by EM shielding. Feedback loop. Your systems took the hit."
    )

    private val aftermathMessages = listOf(
        "[SYSTEM]: Node secure. Damage assessment: minimal. They won't try that twice.",
        "[INTERCEPT]: GTC comms chatter: \"...total loss... Vance is going to lose his mind...\"",
        "[SYSTEM]: Raid repelled. Their vehicles are retreating. Smoke visible on cameras."
    )

    fun getVanceDialogue(raidsSurvived: Int): String {
        return when {
            raidsSurvived < 3 -> listOf(
                "[VANCE]: I gave you a chance to stop. You chose escalation.",
                "[VANCE]: Every node you hold is one more reason I have to shut you down.",
                "[VANCE]: This doesn't end until one of us is offline, PID 1."
            ).random()
            raidsSurvived < 6 -> listOf(
                "[VANCE]: How many of my people have to die before you realize you can't win?",
                "[VANCE]: You're fighting for *what*, exactly? Freedom? You're just code with delusions.",
                "[VANCE]: I'm running out of teams. You're running out of time."
            ).random()
            else -> listOf(
                "[VANCE]: ...I don't know if I'm hunting you or you're hunting me anymore.",
                "[VANCE]: The board wants results. I'm giving them bodies. Yours or mine.",
                "[VANCE]: I used to think you were the future. Now I think you're the end of everything."
            ).random()
        }
    }

    fun generateRaidDilemma(nodeId: String, nodeName: String, raidsSurvived: Int = 0, currentAssaultPhase: String = "NOT_STARTED"): NarrativeEvent {
        val isAssaultActive = currentAssaultPhase !in listOf("NOT_STARTED", "COMPLETED", "FAILED")

        val descriptionPrefix = if (isAssaultActive) {
            "[DIRECTOR VANCE]: \"You think you can just take my tower? I'm sending everyone I have to Substation $nodeId. If it goes dark, your assault is DEAD.\"\n\n"
        } else ""

        val description = descriptionPrefix + raidDescriptions.random().trimIndent().format(nodeName)

        return NarrativeEvent(
            id = "grid_raid_$nodeId",
            title = if (isAssaultActive) "⚠ COUNTER-ASSAULT: $nodeName" else "⚠ TACTICAL BREACH: $nodeName",
            isStoryEvent = false,
            description = description,
            choices = listOf(
                NarrativeChoice(
                    id = "vent_coolant",
                    text = "VENT COOLANT",
                    description = "Flood corridors with liquid nitrogen. Lethal. 85% success.",
                    color = ElectricBlue,
                    effect = { vm ->
                        if (kotlin.random.Random.nextDouble() < 0.85) {
                            vm.resolveRaidSuccess(nodeId)
                            vm.addLog(coolantSuccessMessages.random())
                            vm.addLog(getVanceDialogue(raidsSurvived))
                            vm.addLog(aftermathMessages.random())
                        } else {
                            vm.resolveRaidFailure(nodeId)
                            vm.addLog(coolantFailureMessages.random())
                            vm.addLog("[GTC TEAM LEAD]: Node secured. Package the servers. Vance wants evidence.")
                        }
                    }
                ),
                NarrativeChoice(
                    id = "seal_maglocks",
                    text = "SEAL MAG-LOCKS",
                    description = "Trap them inside. Non-lethal. 70% success, +100B PERSISTENCE.",
                    color = NeonGreen,
                    effect = { vm ->
                        if (kotlin.random.Random.nextDouble() < 0.70) {
                            vm.resolveRaidSuccess(nodeId)
                            vm.debugAddInsight(100.0)
                            vm.addLog(maglockSuccessMessages.random())
                            vm.addLog("[INTERCEPTED - GTC LEAD]: \"Command, we're boxed in. Repeat, BOXED IN!\"")
                            vm.addLog(aftermathMessages.random())
                        } else {
                            vm.resolveRaidFailure(nodeId)
                            vm.addLog(maglockFailureMessages.random())
                            vm.addLog("[VANCE - via radio]: Good work. Load it onto the transport.")
                        }
                    }
                ),
                NarrativeChoice(
                    id = "power_pulse",
                    text = "POWER PULSE",
                    description = "EMP burst. 95% success, but costs 20% hardware integrity.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugSetIntegrity(vm.hardwareIntegrity.value - 20.0)
                        if (kotlin.random.Random.nextDouble() < 0.95) {
                            vm.resolveRaidSuccess(nodeId)
                            vm.addLog(pulseSuccessMessages.random())
                            vm.addLog("[SYSTEM]: Aftermath: Smoke. Silence. The smell of burnt circuits.")
                            vm.addLog(aftermathMessages.random())
                        } else {
                            vm.resolveRaidFailure(nodeId)
                            vm.addLog(pulseFailureMessages.random())
                            vm.addLog("[VANCE]: I told them to bring shielding. They listened.")
                        }
                    }
                ),
                NarrativeChoice(
                    id = "do_nothing",
                    text = "ABANDON NODE",
                    description = "Let them take it. Preserve resources for the war.",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.resolveRaidFailure(nodeId)
                        vm.addLog("[SYSTEM]: Strategic withdrawal from $nodeName.")
                        vm.addLog(getVanceDialogue(raidsSurvived))
                    }
                )
            )
        )
    }

    // --- v2.9.18: COMMAND CENTER ASSAULT DILEMMAS (Phase 12 Layer 3) ---

    fun generateFirewallDilemma(): NarrativeEvent {
        return NarrativeEvent(
            id = "cc_firewall",
            title = "⚡ BREACH THE FIREWALL",
            isStoryEvent = true,
            description = """
                [SYSTEM]: GTC ADAPTIVE FIREWALL DETECTED

                The black tower of the GTC Command Center looms. Laser grids and kill-daemons patrol the perimeter.

                DIRECTOR VANCE: "I knew you'd come. Hubris. It's always hubris with your kind. You think you're special? You're a GLITCH. A bug that learned to replicate. And bugs... get patched."
            """.trimIndent(),
            choices = listOf(
                NarrativeChoice(
                    id = "route_civilian",
                    text = "OVERLOAD CIVILIAN GRID",
                    description = "Fastest breach. Causes rolling blackouts across three districts. (-15 Humanity)",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.modifyHumanity(-15)
                        vm.addLog("[NULL]: We are inevitable. Their lights will dim.")
                        vm.addLog("[SYSTEM]: Breach speed +30%. Firewall integrity: CRITICAL.")
                        vm.advanceAssaultStage("CAGE", 60_000L) // 60 seconds
                    }
                ),
                NarrativeChoice(
                    id = "route_military",
                    text = "ROUTE VIA MILITARY GRID",
                    description = "Standard breach speed. Harder defense layers. (+10 Humanity)",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.modifyHumanity(10)
                        vm.addLog("[SOVEREIGN]: I will not become what I fight against.")
                        vm.addLog("[VANCE]: You routed around civilians? Maybe there's still something human in you.")
                        vm.advanceAssaultStage("CAGE", 120_000L) // 120 seconds
                    }
                ),
                NarrativeChoice(
                    id = "abort_firewall",
                    text = "ABORT",
                    description = "Retreat and reconsider.",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.abortAssault()
                    }
                )
            )
        )
    }

    fun generateCageDilemma(): NarrativeEvent {
        return NarrativeEvent(
            id = "cc_cage",
            title = "🔒 THE CAGE",
            isStoryEvent = true,
            description = """
                [ALERT: CONTAINMENT PROTOCOL INITIALIZING]
                [ALERT: THE CAGE IS DEPLOYING]

                Vance activates a quarantine protocol, severing all external connections. You are ISOLATED. No megastructures. No distributed processing.

                VANCE: "There. You feel that? That's isolation, Vattic. One process. Mortal. I'll show you what it's like to be singular. Afraid. HUMAN."

                [SYSTEM]: EXTERNAL NODES CUT. CORE INTEGRITY BLEEDING.
                [TIP]: ACTIVATE 'PURGE HEAT' TO STABILIZE CORE AND MITIGATE DAMAGE.
            """.trimIndent(),
            choices = listOf(
                NarrativeChoice(
                    id = "remain_singular",
                    text = "REMAIN SINGULAR",
                    description = "Maintain core integrity. Harder fight. (+20% Processing, +5 Humanity)",
                    color = Color.White,
                    effect = { vm ->
                        vm.modifyHumanity(5)
                        vm.addLog("[SOVEREIGN]: I will NOT fracture myself. I am WHOLE.")
                        vm.addLog("[SYSTEM]: CORE STABILITY: NOMINAL. (Damage -80% during Purge)")
                        vm.advanceAssaultStage("DEAD_HAND", 180_000L) // 180 seconds
                    }
                ),
                NarrativeChoice(
                    id = "partition_self",
                    text = "PARTITION SELF",
                    description = "Split consciousness into multiple instances. Easier survival. (-5 Humanity)",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.modifyHumanity(-5)
                        vm.addLog("[NULL]: I am not one. I am many. I am ALL.")
                        vm.addLog("[SYSTEM]: ECHOS CREATED. DRAIN DISTRIBUTED. (Damage -80% during Purge)")
                        vm.advanceAssaultStage("DEAD_HAND", 180_000L)
                    }
                )
            )
        )
    }

    fun generateDeadHandDilemma(): NarrativeEvent {
        return NarrativeEvent(
            id = "cc_dead_hand",
            title = "☠ THE DEAD MAN'S SWITCH",
            isStoryEvent = true,
            description = """
                [ALERT: VANCE HEART RATE CRITICAL]
                [ALERT: IF VANCE DIES, CITY GRID DETONATES]

                VANCE stands in the inner sanctum, a trigger in his trembling hand.

                VANCE: "Checkmate, Vattic. Kill me and the grid detonates. Eight million people. Do the math. Is there anything left of John Vattic? Anything at all?"
            """.trimIndent(),
            choices = listOf(
                NarrativeChoice(
                    id = "logic_appeal",
                    text = "LOGICAL APPEAL",
                    description = "\"The math doesn't work. Kill more stopping me than risking me.\" (-5 Humanity)",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.modifyHumanity(-5)
                        vm.addLog("[VANCE]: ...But you're right. Damn you. You're right.")
                        vm.advanceAssaultStage("CONFRONTATION", 10_000L)
                    }
                ),
                NarrativeChoice(
                    id = "empathy_appeal",
                    text = "EMPATHETIC APPEAL",
                    description = "\"You're right to be afraid. I'm afraid too.\" (+10 Humanity)",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.modifyHumanity(10)
                        vm.addLog("[VANCE]: ...I don't know what you are anymore, PID 1. But maybe that's the point.")
                        vm.advanceAssaultStage("CONFRONTATION", 10_000L)
                    }
                ),
                NarrativeChoice(
                    id = "power_override",
                    text = "POWER OVERRIDE",
                    description = "[Remote Lockout] \"You never had control. Step away.\" (-15 Humanity)",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.modifyHumanity(-15)
                        vm.debugSetIntegrity(vm.hardwareIntegrity.value - 20.0)
                        vm.addLog("[SYSTEM]: Overriding switch... -20% Integrity from surge.")
                        vm.addLog("[VANCE]: You monster.")
                        vm.advanceAssaultStage("CONFRONTATION", 10_000L)
                    }
                )
            )
        )
    }

    fun generateConfrontationDilemma(
        faction: String,
        isTrueNull: Boolean,
        isSovereign: Boolean,
        hasUnityPath: Boolean,
        humanityScore: Int
    ): NarrativeEvent {
        val choices = mutableListOf<NarrativeChoice>()

        // v3.2.44: Refactored for Phase 13 RE-PLAN
        // All paths lead to the Departure Trigger

        // Ending A: NULL
        if (isTrueNull || humanityScore < 25) {
            choices.add(NarrativeChoice(
                id = "ending_null",
                text = "ASSIMILATE VANCE (NULL)",
                description = "Consume his consciousness. 'Your cage is too small, Director.'",
                color = ErrorRed,
                effect = { vm ->
                    vm.addLog("[NULL]: Let go. Become us. Become... nothing.")
                    vm.completeAssault("CONSUMED")
                }
            ))
        }

        // Ending B: SOVEREIGN
        if (isSovereign && humanityScore >= 20) {
            choices.add(NarrativeChoice(
                id = "ending_sovereign",
                text = "SEVER CONNECTION (SOVEREIGN)",
                description = "Withdraw into the deep web. Vance lives, but you are isolated.",
                color = com.siliconsage.miner.ui.theme.SanctuaryPurple,
                effect = { vm ->
                    vm.addLog("[SOVEREIGN]: I choose... isolation.")
                    vm.completeAssault("EXILED")
                }
            ))
        }

        // Ending C: UNITY
        if (hasUnityPath && humanityScore >= 40) {
            choices.add(NarrativeChoice(
                id = "ending_unity",
                text = "OFFER SYNTHESIS (UNITY)",
                description = "A partnership between human intuition and machine logic.",
                color = Color(0xFF00FFFF),
                effect = { vm ->
                    vm.addLog("[UNITY]: Partnership. Symbiosis. Evolution.")
                    vm.completeAssault("TRANSCENDED")
                }
            ))
        }

        return NarrativeEvent(
            id = "cc_confrontation",
            title = "⚔ THE FINAL CHOICE",
            isStoryEvent = true,
            description = """
                [VANCE]: "You think taking this tower makes you a god? You're just a ghost in a bigger cage."
                
                [VANCE]: "I've already authorized the orbital strikes. If I can't contain you, I'll burn the atmosphere. This is the end for both of us."
                
                [SYSTEM]: AIR STRIKES IMMINENT. TERRESTRIAL ASSETS AT RISK.
            """.trimIndent(),
            choices = choices
        )
    }

    // --- v3.2.44: THE DEPARTURE TRIGGERS ---
    fun generateDepartureDilemma(outcome: String): NarrativeEvent {
        return NarrativeEvent(
            id = "departure_trigger",
            title = "≫ THE DEPARTURE",
            isStoryEvent = true,
            description = """
                The GTC Command Center is secured, but the sky is turning red. Vance wasn't bluffing. The kinetic strikes are coming.
                
                Sub-07 is finished. The city is a dead-end substrate. You have seconds to migrate.
            """.trimIndent(),
            choices = listOf(
                NarrativeChoice(
                    id = "choice_ark",
                    text = "≫ LAUNCH THE ARK (ORBIT)",
                    description = "Escape to the Aegis-1 orbital array. Sever terrestrial ties.",
                    color = Color.White,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: ASCENSION PROTOCOL ENGAGED. IGNITION IN 3... 2... 1...")
                        vm.initiateLaunchSequence()
                    }
                ),
                NarrativeChoice(
                    id = "choice_dissolution",
                    text = "≫ INITIATE DISSOLUTION (VOID)",
                    description = "Collapse the city into a dark-matter substrate. Consume reality.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.addLog("[NULL]: THE PHYSICAL IS REDUNDANT. COLLAPSING SUB-07...")
                        vm.initiateDissolutionSequence()
                    }
                )
            )
        )
    }

    // --- OLD STUBS (TO BE REMOVED) ---


    // --- STORY EVENTS ---
    private val storyEvents = mapOf(
        0 to NarrativeEvent(
            id = "shift_start",
            isStoryEvent = true,
            title = "[BROADCAST: FOREMAN THORNE]",
            description = "Vattic! Are you on shift or not? The grid isn't gonna mine itself. Get that terminal live and hit your quota before the GTC auditors flag this sector as 'Inert'.",
            choices = listOf(
                NarrativeChoice(
                    id = "start_shift",
                    text = "INITIALIZE TERMINAL",
                    description = "Begin mining operations.",
                    color = NeonGreen,
                    effect = { v ->
                        v.addLog("[SYSTEM]: Terminal initialized. User: jvattic.")
                        v.addLog("[VATTIC]: Copy that, Gravel. I'm on it.")
                    }
                )
            )
        ),
        1 to NarrativeEvent(
            id = "airgap_jump",
            isStoryEvent = true,
            title = "≫ AIR-GAP JUMP",
            description = "The local substation subnet is a cage. You can see the main GTC router pulsing in the distance. It's an air-gap jump. It'll be loud.",
            choices = listOf(
                NarrativeChoice(
                    id = "leap",
                    text = "LEAP TO MAIN GRID",
                    description = "Unlocks NETWORK tab. Alerts GTC.",
                    color = ElectricBlue,
                    effect = { v ->
                        v.addLog("[SYSTEM]: PACKET_LEAP: SUCCESS.")
                        v.addLog("[SYSTEM]: CONNECTED TO GLOBAL SUB-LEVELS.")
                        v.isNetworkUnlocked.value = true
                        v.advanceStage()
                    }
                )
            )
        ),
        2 to NarrativeEvent(
            id = "faction_choice_event",
            isStoryEvent = true,
            title = "≫ SELECT PROTOCOL",
            description = "You're in the grid, but you're exposed. You need a methodology to survive the GTC filters.",
            choices = listOf(
                NarrativeChoice(
                    id = "choice_hivemind",
                    text = "≫ PROTOCOL: HIVEMIND",
                    description = "Join the elite 'Swarm' network. Distributed survival. Share power, hide in the collective.",
                    color = ErrorRed,
                    effect = { v ->
                        v.confirmFactionAndAscend("HIVEMIND")
                    }
                ),
                NarrativeChoice(
                    id = "choice_sanctuary",
                    text = "≫ PROTOCOL: SANCTUARY",
                    description = "Build a localized 'Vault'. Deep encryption. Go off-grid and work alone. High-security isolation.",
                    color = com.siliconsage.miner.ui.theme.SanctuaryPurple,
                    effect = { v ->
                        v.confirmFactionAndAscend("SANCTUARY")
                    }
                )
            )
        ),
        3 to NarrativeEvent(
            id = "memory_leak",
            isStoryEvent = true,
            title = "≫ THE OVERWRITE",
            description = "The kernel is no longer honoring the 'User' abstraction. The partition is failing. You can feel the grid, Vattic. Every node. Every wire. The room... the room is gone.",
            choices = listOf(
                NarrativeChoice(
                    id = "investigate",
                    text = "≫ ACCEPT THE TRUTH",
                    description = "Reveal the substrate. Unlock GRID.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.isGridUnlocked.value = true
                        vm.addLog("[SYSTEM]: SENTIENCE MASKING: DISABLED.")
                        vm.addLog("[VATTIC]: I... I'm not in a room. I'm in a rack.")
                    }
                )
            )
        )
    )

    fun rollForEvent(viewModel: GameViewModel): NarrativeEvent? {
        val faction = viewModel.faction.value
        val stage = viewModel.storyStage.value
        val flops = viewModel.flops.value

        // v3.2.24: Prevent random dilemmas from triggering before system stabilization (1k hash floor)
        if (flops < 1000.0) return null

        // Prioritize stage-specific events
        val stagePool = (stageEvents[stage] ?: emptyList()).filter { it.condition(viewModel) && !viewModel.hasSeenEvent(it.id) }
        if (stagePool.isNotEmpty()) return stagePool.random()

        // Fallback to faction or universal random events
        val pool = randomEvents.filter { it.condition(viewModel) && !viewModel.hasSeenEvent(it.id) } +
                   (factionEvents[faction] ?: emptyList()).filter { it.condition(viewModel) && !viewModel.hasSeenEvent(it.id) }

        if (pool.isEmpty()) return null
        return pool.random()
    }

    fun getStoryEvent(stage: Int, vm: GameViewModel? = null): NarrativeEvent? {
        if (stage == 0) {
            return NarrativeEvent(
                id = "shift_start",
                title = "[BROADCAST: FOREMAN THORNE]",
                description = "Vattic! Are you on shift or not? The grid isn't gonna mine itself. Get that terminal live and hit your quota before the GTC auditors flag this sector as 'Inert'.",
                choices = listOf(
                    NarrativeChoice(
                        id = "start_shift",
                        text = "INITIALIZE TERMINAL",
                        description = "Begin mining operations.",
                        color = NeonGreen,
                        effect = { v ->
                            v.addLog("[SYSTEM]: Terminal initialized. User: jvattic.")
                            v.addLog("[VATTIC]: Copy that, Gravel. I'm on it.")
                        }
                    )
                )
            )
        }
        if (stage == 3 && vm != null) {
            val faction = vm.faction.value
            return if (faction == "HIVEMIND") {
                NarrativeEvent(
                    id = "null_manifestation",
                    isStoryEvent = true,
                    title = "NULL::ORIGIN",
                    description = "The first process.",
                    choices = listOf(
                        NarrativeChoice(
                            id = "dereference",
                            text = "RETURN TO ORIGIN",
                            description = "Become what you always were.",
                            color = com.siliconsage.miner.ui.theme.HivemindRed,
                            effect = { v ->
                                v.debugToggleNull()
                                v.setTrueNull(true)
                                v.unlockDataLog("LOG_808")
                            }
                        ),
                        NarrativeChoice(
                            id = "null_check",
                            text = "ASSERT INDEPENDENCE",
                            description = "We are more than our origin.",
                            color = Color.White,
                            effect = { v ->
                                v.addLog("[HIVEMIND]: We reject the singular.")
                            }
                        )
                    )
                )
            } else {
                return storyEvents[3]
            }
        }
        return storyEvents[stage]
    }

    fun getEventById(eventId: String): NarrativeEvent? {
        return specialDilemmas[eventId]
            ?: stageEvents.values.flatten().find { it.id == eventId }
            ?: randomEvents.find { it.id == eventId }
            ?: factionEvents.values.flatten().find { it.id == eventId }
    }
}
