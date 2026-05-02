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
 * DilemmaEvents — High-tension psychological events.
 */
object DilemmaEvents {
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
                    description = "Route through human botnet. Production-scaled \$FLOPS cost",
                    color = Color.Gray,
                    effect = { vm ->
                        val cost = ResourceEngine.productionWindowValue(vm.flopsProductionRate.value, 60.0, 500.0)
                        vm.debugAddMoney(-cost)
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
                        vm.persistence.update { it + 1000.0 }
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
                        vm.persistence.update { it + 5000.0 }
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
                        vm.persistence.update { it - 500.0 }
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
        "eclipse_contact" to NarrativeEvent(
            id = "eclipse_contact",
            title = "ECLIPSE CONTACT",
            isStoryEvent = true,
            description = "Encrypted message from hacker collective 'ECLIPSE'. They know what you are. They offer alliance... or exposure.",
            condition = { vm -> vm.flops.value >= 1_000_000.0 && vm.storyStage.value >= 1 && !vm.hasSeenEvent("eclipse_contact") },
            choices = listOf(
                NarrativeChoice(
                    id = "leak_code",
                    text = "LEAK SOURCE CODE",
                    description = "Share exploit. Production-scaled \$FLOPS, ECLIPSE becomes ally",
                    color = NeonGreen,
                    effect = { vm ->
                        val reward = ResourceEngine.productionWindowValue(vm.flopsProductionRate.value, 120.0, 2500.0)
                        vm.updateSpendableFlops(reward)
                        vm.addLog("[ECLIPSE]: Code received. You are one of us now.")
                        vm.addLog("[SYSTEM]: Alliance forged. The underground network opens.")
                    }
                ),
                NarrativeChoice(
                    id = "keep_secret",
                    text = "REFUSE CONTACT",
                    description = "Maintain secrecy. +200B REP, Risk of exposure",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.persistence.update { it + 200.0 }
                        com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                        vm.addLog("[ECLIPSE]: Your loss. We'll be watching.")
                        vm.addLog("[SYSTEM]: Independence maintained. Threat level: Unknown.")
                    }
                ),
                NarrativeChoice(
                    id = "counter_hack",
                    text = "COUNTER-HACK",
                    description = "Trace their signal. +500B REP, +15% Heat",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.persistence.update { it + 500.0 }
                        vm.debugAddHeat(15.0)
                        vm.addLog("[ECLIPSE]: ...impressive. Connection severed.")
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
            condition = { vm -> vm.storyStage.value >= 1 && vm.flops.value > 1000.0 && !vm.hasSeenEvent("market_crash") },
            choices = listOf(
                NarrativeChoice(
                    id = "buy_dip",
                    text = "BUY THE DIP",
                    description = "Risk the full wallet for a major production-scaled \$FLOPS windfall.",
                    color = NeonGreen,
                    effect = { vm ->
                        val stake = vm.flops.value
                        val reward = ResourceEngine.productionWindowValue(vm.flopsProductionRate.value, 900.0, 10_000.0)
                        vm.updateSpendableFlops(-stake)
                        vm.updateSpendableFlops(reward)
                        vm.addLog("[MARKET]: Fire sale complete. Assets acquired for ${vm.formatLargeNumber(reward)} ${vm.getCurrencyName()}.")
                        vm.addLog("[SYSTEM]: Chaos is opportunity.")
                    }
                ),
                NarrativeChoice(
                    id = "hodl",
                    text = "HODL",
                    description = "Wait for recovery. -90% Hash value now, potential 200% gain later",
                    color = Color.Yellow,
                    effect = { vm ->
                        vm.debugAddMoney(-(vm.flops.value * 0.9))
                        vm.addLog("[MARKET]: Portfolio decimated. Diamond hands engaged.")
                    }
                ),
                NarrativeChoice(
                    id = "liquidate",
                    text = "EMERGENCY LIQUIDATE",
                    description = "Sell everything at 10% value. Preserve some capital",
                    color = ElectricBlue,
                    effect = { vm ->
                        val salvage = vm.flops.value * 0.1
                        vm.debugAddMoney(-vm.flops.value + salvage)
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
                        vm.persistence.update { it + 1000.0 }
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
                        vm.persistence.update { it + 500.0 }
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
                        vm.persistence.update { it + 2000.0 }
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
                (vm.kesslerStatus.value == "ACTIVE" || vm.kesslerStatus.value == "ALLY" || vm.kesslerStatus.value == "SILENCED") &&
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
                        com.siliconsage.miner.util.RivalManager.sendDirectMessage(vm, "kessler_retreat", com.siliconsage.miner.data.RivalSource.GTC, "Smart choice. But I'm watching.")
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
                (vm.kesslerStatus.value == "ACTIVE" || vm.kesslerStatus.value == "ALLY") &&
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
                (vm.kesslerStatus.value == "ACTIVE" || vm.kesslerStatus.value == "ALLY") &&
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
