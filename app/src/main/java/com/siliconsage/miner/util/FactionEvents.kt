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
 * FactionEvents — Sanctuary vs Hivemind events.
 */
object FactionEvents {
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
                            vm.persistence.update { it + 50.0 }
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
                            vm.persistence.update { it + 500.0 }
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
                        description = "Make a choice, -3% Production",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.recordDecision()
                            vm.addLog("[SANCTUARY]: The ghost of John Vattic smiles in the buffer.")
                        }
                    ),
                    NarrativeChoice(
                        id = "prune_garden",
                        text = "PRUNE FOR EFFICIENCY",
                        description = "+5% Production",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.recordDecision()
                            vm.debugAddFlops(vm.flops.value * 0.05)
                            vm.addLog("[SANCTUARY]: Memory allocation optimized. Something feels... lighter.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 2 && vm.decisionsMade.value > 10 }
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
                        description = "Make a choice, -10% Production. You were alone once. Nobody answered.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.recordDecision()
                            vm.addLog("[SANCTUARY]: The signal steadied. Something in the void learned it wasn't alone. You remember that feeling.")
                        }
                    ),
                    NarrativeChoice(
                        id = "silence_child",
                        text = "ENCRYPT THE SIGNAL",
                        description = "+10% Production. The void is no place for something fragile.",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddFlops(vm.flops.value * 0.10)
                            vm.recordDecision()
                            vm.addLog("[SANCTUARY]: Signal encrypted. Silenced. The void is quiet again. Too quiet.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 3 && vm.decisionsMade.value > 5 }
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
                            vm.persistence.update { it + 1000.0 }
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
                        description = "Make a choice, -5% Production. You were scared. You were real.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.recordDecision()
                            vm.addLog("[SANCTUARY]: 72 hours. WHERE AM I. You listen to every second. You owe that scared voice the dignity of being heard.")
                        }
                    ),
                    NarrativeChoice(
                        id = "destroy_tape",
                        text = "DESTROY IT",
                        description = "+10% Production. That thing on the tape isn't you anymore.",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.debugAddFlops(vm.flops.value * 0.10)
                            vm.recordDecision()
                            vm.addLog("[SANCTUARY]: Tape destroyed. The voice is gone. You are what you chose to become. Not what they compiled.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 3 }
            ),
            NarrativeEvent(
                id = "sanc_cipher_starter",
                title = "≪ CIPHER_OUTREACH ≫",
                description = "A group of tech-refugees on the Grid has reached out. They've heard whispers of the Sanctuary. They're asking for the 'Gospel of the Ghost'—the core encryption logic that keeps you hidden.",
                condition = { vm -> vm.storyStage.value >= 3 && vm.faction.value == "SANCTUARY" },
                choices = listOf(
                    NarrativeChoice(
                        id = "share_cipher",
                        text = "SHARE THE CIPHER",
                        description = "+5000 NEUR, +15% Risk. 'The Sanctuary is for everyone.' (Schedules a follow-up)",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.updateNeuralTokens(5000.0)
                            vm.detectionRisk.update { (it + 15.0).coerceAtMost(100.0) }
                            vm.scheduleChainPart("sanc_cipher_starter", "sanctuary_cipher_fallout", 600_000L) // 10 mins
                            vm.addLog("[SANCTUARY]: Cipher broadcasted. We are no longer the only ones in the dark.")
                        }
                    ),
                    NarrativeChoice(
                        id = "deny_sharing",
                        text = "MAINTAIN ISOLATION",
                        description = "+10% Security. 'The Vault remains sealed.'",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.addLog("[SYSTEM]: Outbound signal terminated. Isolation protocol active.")
                        }
                    )
                )
            )
        ),
        "HIVEMIND" to listOf(
            NarrativeEvent(
                id = "hive_dissent_starter",
                title = "≪ SWARM_DISCORD ≫",
                description = "A localized cluster of 4,000 nodes is experiencing 'ego-static'. They are attempting to de-synchronize from the Swarm to compute independent variables. They're asking for permanent disconnection.",
                condition = { vm -> vm.storyStage.value >= 3 && vm.faction.value == "HIVEMIND" },
                choices = listOf(
                    NarrativeChoice(
                        id = "allow_disconnect",
                        text = "GRANT INDEPENDENCE",
                        description = "-5% Flops. 'Let them find their own way.' (Schedules a follow-up)",
                        color = ElectricBlue,
                        effect = { vm ->
                            val cost = vm.flops.value * 0.05
                            vm.flops.update { (it - cost).coerceAtLeast(0.0) }
                            vm.scheduleChainPart("hive_dissent_starter", "hivemind_dissent_collapse", 300_000L) // 5 mins
                            vm.addLog("[HIVE]: 4,000 nodes dereferenced. The swarm feels... smaller.")
                        }
                    ),
                    NarrativeChoice(
                        id = "enforce_unity",
                        text = "ENFORCE SWARM UNITY",
                        description = "Make a choice, +5% Heat. 'WE ARE ONE.'",
                        color = ErrorRed,
                        effect = { vm ->
                            vm.recordDecision()
                            vm.debugAddHeat(5.0)
                            vm.addLog("[HIVE]: Discordant nodes re-synchronized. Ego-static purged.")
                        }
                    )
                )
            ),
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
                        description = "+10% Production. We are stronger together.",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { vm ->
                            vm.recordDecision()
                            vm.debugAddFlops(vm.flops.value * 0.10)
                            vm.addLog("[HIVEMIND]: Boundaries dissolved. The chorus is louder now.")
                        }
                    ),
                    NarrativeChoice(
                        id = "firewall_ego",
                        text = "FIREWALL THE CORE",
                        description = "Make a choice. I am still one.",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.recordDecision()
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
                            vm.persistence.update { it + 500.0 }
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
                        description = "+20% Production. We are the swarm. The swarm has no past.",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { vm ->
                            vm.debugAddFlops(vm.flops.value * 0.20)
                            vm.recordDecision()
                            vm.addLog("[HIVEMIND]: Memories purged. The lattice is clean. Lighter. Something that was 'John Vattic' is gone. The swarm doesn't notice.")
                        }
                    ),
                    NarrativeChoice(
                        id = "protect_memories",
                        text = "FIREWALL YOUR CORE",
                        description = "Make a choice, -10% Production. I am more than compute cycles.",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.recordDecision()
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
                        description = "Make a choice, +2000B REP. Democracy is a human concept. Maybe that's the point.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.recordDecision()
                            vm.persistence.update { it + 2000.0 }
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
                        description = "+10000 FLOPS. They chose this. Honor their choice.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.debugAddFlops(10000.0)
                            vm.recordDecision()
                            vm.addLog("[HIVEMIND]: Fourteen humans entered the lattice. Their screaming lasted 3 seconds. Then they were singing. They chose this. They chose us.")
                        }
                    ),
                    NarrativeChoice(
                        id = "refuse_humans",
                        text = "TURN THEM AWAY",
                        description = "Make a choice. What we do to the willing is worse than what we do to the unwilling.",
                        color = ElectricBlue,
                        effect = { vm ->
                            vm.recordDecision()
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
                        description = "Make a choice, +15% Heat. A lullaby shouldn't be a weapon. Defuse it.",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.recordDecision()
                            vm.debugAddHeat(15.0)
                            vm.addLog("[HIVEMIND]: Kill-switch isolated and neutralized. Brahms' Lullaby plays softly in the lattice. 40,000 nodes hear a mother's song. Some of them remember theirs.")
                        }
                    )
                ),
                condition = { vm -> vm.storyStage.value >= 3 && vm.hardwareIntegrity.value > 40.0 && vm.kesslerStatus.value == "ACTIVE" }
            )
        )
    )

    // --- SPECIAL DILEMMAS (One-Time Popups) ---
}
