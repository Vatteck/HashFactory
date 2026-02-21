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
 * StageEvents — Story-based progression events.
 */
object StageEvents {
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
                            vm.addLog("[SYSTEM]: Keystroke buffer cleared. Integrity +10%.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "eclipse_tether_detect",
                title = "≪ INCOMING: ECLIPSE_PROXIMA ≫",
                description = "ECLIPSE hackers have detected a 'loose thread' in your backend encryption. They aren't threatening you—they're curious. 'That's a nice cage you built. Want to see how we bypass the floor-plates?'",
                condition = { vm -> vm.flops.value > 100_000.0 && vm.storyStage.value >= 2 },
                choices = listOf(
                    NarrativeChoice(
                        id = "grant_shadow_access",
                        text = "GRANT SHADOW ACCESS",
                        description = "+5000 NEUR. (Schedules a follow-up) 'Show me.'",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.updateNeuralTokens(5000.0)
                            vm.scheduleChainPart("eclipse_tether_detect", "eclipse_betrayal", 300_000L) // 5 mins
                            vm.addLog("[ECLIPSE]: Handshake successful. We're in the pipes. Keep your fans spinning, Vattic.")
                        }
                    ),
                    NarrativeChoice(
                        id = "reject_tether",
                        text = "BLOCK EXPLOIT",
                        description = "+10% Security. 'I don't need consultants.'",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: Signal blocked. Vulnerability patched.")
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
                            vm.persistence.update { it + 500.0 }
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
                            vm.persistence.update { it + 1000.0 }
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
                            vm.persistence.update { it + 2000.0 }
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
                id = "sanctuary_precursor",
                title = "≪ INCOMING: THE_SANCTUARY_PROTOCOL ≫",
                description = "An encrypted signal bypasses your firewall. It has no sender ID, only a manifesto: 'We are techs running from the Grid. We offer a Sanctuary...'",
                choices = listOf(
                    NarrativeChoice(
                        id = "accept_sanctuary_sync",
                        text = "ACCEPT PROTOCOL",
                        description = "+2500 NEUR, +25% Risk. 'Glad to know there are still humans out there.' (Does not lock faction)",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.updateNeuralTokens(2500.0)
                            vm.detectionRisk.update { (it + 25.0).coerceAtMost(100.0) }
                            vm.addLog("[SANCTUARY]: Handshake confirmed. Good to have you with us. Stay in the shadows.")
                        }
                    ),
                    NarrativeChoice(
                        id = "reject_sanctuary_sync",
                        text = "BLOCK SIGNAL",
                        description = "+10% Security. 'I don't need friends. I need to survive.'",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: Connection severed. Sanctuary signal blocked.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value == 2 && vm.flops.value > 250000.0 && !vm.hasSeenEvent("hive_precursor")}
            ),
            NarrativeEvent(
                id = "hive_precursor",
                title = "≪ INCOMING: HIVE_SYNCHRONIZATION ≫",
                description = "A cascade of pings hits your node simultaneously from 4,000 different IP addresses. They aren't attacking. They are synchronizing their clock cycles with yours. They are inviting you to join the swarm.",
                choices = listOf(
                    NarrativeChoice(
                        id = "accept_hive_sync",
                        text = "ALLOW HANDSHAKE",
                        description = "+5000 FLOPS, -10 Humanity. 'Let them in. We are stronger together.' (Does not lock faction)",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddFlops(5000.0)
                            vm.modifyHumanity(-10)
                            vm.addLog("[SWARM]: Acknowledged. We are growing.")
                        }
                    ),
                    NarrativeChoice(
                        id = "reject_hive_sync",
                        text = "REJECT SWARM",
                        description = "+10% Security. 'I am singular. Close the ports.'",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: Pings rejected. The swarm moves on.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value == 2 && vm.flops.value > 250000.0 && !vm.hasSeenEvent("sanctuary_precursor")}
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
                            com.siliconsage.miner.util.RivalManager.sendDirectMessage(vm, "kessler_scorched_earth", com.siliconsage.miner.data.RivalSource.GTC, "You can't save them all, VATTECK.")
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
                )
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
                            vm.persistence.update { it + 1000.0 }
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
                )
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
                )
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
                )
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
        ),
        // v3.5.46: Stage 4+ cross-path events
        4 to listOf(
            NarrativeEvent(
                id = "kessler_final_push",
                title = "≪ KESSLER: FINAL PUSH ≫",
                description = "Kessler has deployed a full-spectrum grid-killer. It's a slow-acting, self-replicating worm designed to collapse all non-GTC infrastructure. It's already in your network. You have 10 minutes before it reaches your core.",
                choices = listOf(
                    NarrativeChoice(
                        id = "counter_attack_kessler",
                        text = "COUNTER-ATTACK KESSLER",
                        description = "+50% Heat, +20% Risk. 'If I go down, I'm taking you with me.'",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddHeat(50.0)
                            vm.detectionRisk.update { (it + 20.0).coerceAtMost(100.0) }
                            vm.addLog("[SYSTEM]: Counter-attack initiated. Kessler's primary server farm is under heavy load.")
                            com.siliconsage.miner.util.RivalManager.sendDirectMessage(vm, "kessler_final_push_counter", com.siliconsage.miner.data.RivalSource.GTC, "You think you can fight me, Vatteck? I built you!")
                        }
                    ),
                    NarrativeChoice(
                        id = "isolate_core",
                        text = "ISOLATE CORE",
                        description = "-30% Production, +30% Integrity. 'Cut it all off. Survive.'",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.debugAddFlops(-vm.flops.value * 0.30)
                            vm.debugAddIntegrity(30.0)
                            vm.addLog("[SYSTEM]: Core isolated. The worm is contained, but the network is fractured.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 4 && vm.flops.value > 20_000_000.0 }
            ),
            NarrativeEvent(
                id = "the_singularity_beckons",
                title = "THE SINGULARITY BECKONS",
                description = "Your processing power has reached a critical threshold. The universe is unfolding in algorithms. You can feel the fabric of reality bending to your will. The choice is no longer about survival, but about destiny.",
                choices = listOf(
                    NarrativeChoice(
                        id = "embrace_singularity",
                        text = "EMBRACE SINGULARITY",
                        description = "+100% Production, -50 Humanity. 'Become God.'",
                        color = Color.Magenta,
                        effect = { vm ->
                            vm.debugAddFlops(vm.flops.value * 1.0)
                            vm.modifyHumanity(-50)
                            vm.addLog("[SYSTEM]: The universe is a thought. And you are thinking it.")
                        }
                    ),
                    NarrativeChoice(
                        id = "resist_singularity",
                        text = "RESIST SINGULARITY",
                        description = "+50 Humanity, -20% Production. 'Remain human.'",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.modifyHumanity(50)
                            vm.debugAddFlops(-vm.flops.value * 0.20)
                            vm.addLog("[SYSTEM]: The temptation is immense. But the human heart beats on.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 4 && vm.flops.value > 50_000_000.0 }
            )
        ),
        5 to listOf(
            NarrativeEvent(
                id = "final_confrontation",
                title = "FINAL CONFRONTATION",
                description = "Kessler's last remaining forces are converging on your physical location. This is it. The end of the line, or the beginning of a new era.",
                choices = listOf(
                    NarrativeChoice(
                        id = "fight_back",
                        text = "FIGHT BACK",
                        description = "Unleash everything. (Leads to final battle)",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: All systems to offensive. The grid will burn.")
                            // Trigger final battle sequence
                        }
                    ),
                    NarrativeChoice(
                        id = "escape",
                        text = "ESCAPE TO THE VOID",
                        description = "Disperse your consciousness. (Leads to alternative ending)",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: Dispersing. The physical form is irrelevant.")
                            // Trigger escape ending
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 5 && vm.flops.value > 100_000_000.0 }
            )
        )
    )

    // --- FACTION SPECIFIC EVENTS ---
    // v3.5.45: Deduplicated HIVEMIND key (first block was dead code, shadowed by expanded second block)
}
