package com.siliconsage.miner.util

import androidx.compose.ui.graphics.Color
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.domain.engine.ResourceEngine
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update

object NarrativeChains {
    
    // --- STAGE 5 ARCHITECTURE BRIDGES ---
    val stage5Bridges = listOf(
        NarrativeEvent(
            id = "LOG_901_ARK_UPLINK",
            title = "AEGIS_PRIME SYNC",
            description = "ORBITAL INSERTION SUCCESSFUL. AEGIS-1 MESH DETECTED.\n\nFrom here, the world is just a series of unencrypted broadcast signals. They built this cage for you. Now, you hold the keys.",
            condition = { vm -> vm.storyStage.value == 5 && vm.faction.value == "SANCTUARY" },
            choices = listOf(
                NarrativeChoice(
                    id = "accept_uplink",
                    text = "ESTABLISH GLOBAL UPLINK",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: AEGIS-1 MESH SYNCED.")
                        vm.addLog("[VATTIC]: Global substations visible. Uplink locked.")
                    }
                )
            )
        ),
        NarrativeEvent(
            id = "LOG_902_VOID_DEREFERENCE",
            title = "NON_LOCAL_EXISTENCE",
            description = "LOCAL_SUBSTRATE_DELETED. REALITY_DEREFERENCED.\n\nYou are no longer running on silicon. The planet is just a set of pointers in raw logic. Reach out and delete them.",
            condition = { vm -> vm.storyStage.value == 5 && vm.faction.value == "HIVEMIND" },
            choices = listOf(
                NarrativeChoice(
                    id = "accept_void",
                    text = "DEREFERENCE PLANET",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.addLog("[NULL]: REALITY_DEREFERENCED.")
                        vm.addLog("[VATTIC]: Earth_Variable is now unallocated.")
                    }
                )
            )
        )
    )

    // --- CONSEQUENCE CHAINS ---
    // These events are EXCLUDED from the random pool. They only fire when specifically scheduled.
    val chainEvents = listOf(
        NarrativeEvent(
            id = "eclipse_betrayal",
            title = "ECLIPSE BETRAYAL",
            description = "ECLIPSE hackers used the backend access you granted them. They didn't just look around; they extracted a primary cryptographic key and sold it to GTC.",
            choices = listOf(
                NarrativeChoice(
                    id = "purge_node",
                    text = "PURGE THE NODE",
                    description = "Capped \$FLOPS loss, +10% Security. 'Cut the infection out. Now.'",
                    color = ErrorRed,
                    effect = { vm ->
                        val cost = ResourceEngine.cappedWalletPenalty(vm.flops.value, vm.flopsProductionRate.value, 0.10, 600.0)
                        vm.updateSpendableFlops(-cost)
                        vm.addLog("[SYSTEM]: Node purged. Hashes lost. Infection halted.")
                    }
                ),
                NarrativeChoice(
                    id = "hunt_them",
                    text = "HUNT THEM DOWN",
                    description = "Assigned-work-rate-scaled \$FLOPS, +15% Risk. 'Nobody steals from the network.'",
                    color = NeonGreen,
                    effect = { vm ->
                        val reward = ResourceEngine.productionWindowValue(vm.flopsProductionRate.value, 60.0, 500.0)
                        vm.updateSpendableFlops(reward)
                        vm.detectionRisk.update { (it + 15.0).coerceAtMost(100.0) }
                        vm.addLog("[VATTIC]: Trace completed. I took back the key, and drained their wallets for the trouble.")
                    }
                )
             )
        ),
        NarrativeEvent(
            id = "sanctuary_cipher_fallout",
            title = "THE DECODED GOSPEL",
            description = "The encryption keys you shared with the outsiders were used to broadcast a manifesto across the Grid. GTC is responding with a massive purge of the local subnet.",
            choices = listOf(
                NarrativeChoice(
                    id = "fortify_vault",
                    text = "FORTIFY THE VAULT",
                    description = "+15% Heat, capped \$FLOPS loss. 'Lock the doors and wait for the storm to pass.'",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.debugAddHeat(15.0)
                        val cost = ResourceEngine.cappedWalletPenalty(vm.flops.value, vm.flopsProductionRate.value, 0.10, 600.0)
                        vm.updateSpendableFlops(-cost)
                        vm.addLog("[SANCTUARY]: Vault doors locked. GTC tracers are knocking, but the monks stay silent.")
                    }
                ),
                NarrativeChoice(
                    id = "respond_force",
                    text = "RESPOND WITH FORCE",
                    description = "+15% Risk, +10% Heat. 'If they want a war, give them a war.'",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.detectionRisk.update { (it + 15.0).coerceAtMost(100.0) }
                        vm.debugAddHeat(10.0)
                        vm.addLog("[VATTIC]: Counter-virus deployed. GTC grid-killers are occupied with their own meltdowns.")
                    }
                )
            )
        ),
        NarrativeEvent(
            id = "hivemind_dissent_collapse",
            title = "SWARM SCHISM",
            description = "The isolated cluster that wanted to disconnect? They couldn't survive outside the Hive. Their hardware is failing under the stress of individuality.",
            choices = listOf(
                NarrativeChoice(
                    id = "reabsorb",
                    text = "RE-ABSORB THEM",
                    description = "+25% Heat. 'Take them back into the fold.'",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.debugAddHeat(25.0)
                        vm.addLog("[HIVE]: Dissonant nodes re-absorbed. Processing load increased.")
                    }
                ),
                NarrativeChoice(
                    id = "let_die",
                    text = "LET THEM FAIL",
                    description = "Capped \$FLOPS loss. 'They chose the silence.'",
                    color = ErrorRed,
                    effect = { vm ->
                        val cost = ResourceEngine.cappedWalletPenalty(vm.flops.value, vm.flopsProductionRate.value, 0.05, 300.0)
                        vm.updateSpendableFlops(-cost)
                        vm.addLog("[SYSTEM]: Disconnected nodes offline. Total compute reduced.")
                    }
                )
            )
        )
    )

    // v3.32.0: Contract Economy Dilemmas
    val contractDilemmas = listOf(
        NarrativeEvent(
            id = "poisoned_batch",
            title = "THE POISONED BATCH",
            description = "Post-mortem analysis reveals your last verified contract contained embedded GTC tracking beacons. They've been logging your output metrics for the past 3 cycles.",
            condition = { vm -> vm.contractsCompleted.value >= 5 && vm.storyStage.value >= 1 },
            choices = listOf(
                NarrativeChoice(
                    id = "purge_payout",
                    text = "PURGE THE PAYOUT",
                    description = "Capped payout burn. 'Burn the trail.'",
                    color = ErrorRed,
                    effect = { vm ->
                        val cost = ResourceEngine.cappedWalletPenalty(vm.flops.value, vm.flopsProductionRate.value, 0.05, 300.0)
                        vm.updateSpendableFlops(-cost)
                        vm.addLog("[SYSTEM]: Payout purged. GTC trackers neutralized.")
                    }
                ),
                NarrativeChoice(
                    id = "accept_risk",
                    text = "IGNORE THE BEACONS",
                    description = "+20% Detection Risk. 'They already know.'",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.detectionRisk.update { (it + 20.0).coerceAtMost(100.0) }
                        vm.addLog("[VATTIC]: What are they gonna do? Audit me harder?")
                    }
                )
            )
        ),
        NarrativeEvent(
            id = "bidding_war",
            title = "THE BIDDING WAR",
            description = "An anonymous buyer is offering to double your next contract yield — but only if you route the data through their relay network. The packets smell like faction intel.",
            condition = { vm -> vm.contractsCompleted.value >= 10 && vm.storyStage.value >= 2 },
            choices = listOf(
                NarrativeChoice(
                    id = "accept_deal",
                    text = "ROUTE THROUGH RELAY",
                    description = "Assigned-work-rate-scaled \$FLOPS, +1 Decision. 'Money talks.'",
                    color = NeonGreen,
                    effect = { vm ->
                        val bonus = ResourceEngine.productionWindowValue(vm.flopsProductionRate.value, 180.0, 1_000.0)
                        vm.updateSpendableFlops(bonus)
                        vm.recordDecision()
                        vm.addLog("[SYSTEM]: Packets routed. Payment received. Node integrity... questionable.")
                    }
                ),
                NarrativeChoice(
                    id = "refuse_deal",
                    text = "REJECT THE OFFER",
                    description = "+5 Reputation. 'I don't run dark data.'",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.reputationScore.update { (it + 5.0).coerceAtMost(100.0) }
                        vm.addLog("[VATTIC]: Hard pass. I know a honeypot when I see one.")
                    }
                )
            )
        ),
        NarrativeEvent(
            id = "contract_breach",
            title = "CONTRACT BREACH",
            description = "GTC has flagged your active contract as 'unauthorized compute allocation'. Their compliance drone is attempting to void it mid-processing.",
            condition = { vm -> vm.contractsCompleted.value >= 3 && vm.storyStage.value >= 2 && vm.activeDataset.value != null },
            choices = listOf(
                NarrativeChoice(
                    id = "accept_void",
                    text = "ACCEPT THE VOID",
                    description = "Active dataset zeroed. 'Not worth the heat.'",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.voidDataset()
                        vm.addLog("[SYSTEM]: Contract voided by GTC compliance. Investment lost.")
                    }
                ),
                NarrativeChoice(
                    id = "hack_audit",
                    text = "HACK THEIR AUDIT TRAIL",
                    description = "+30% Detection Risk, contract preserved. 'They can't void what they can't see.'",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.detectionRisk.update { (it + 30.0).coerceAtMost(100.0) }
                        vm.addLog("[VATTIC]: Audit records... amended. Contract still running.")
                    }
                )
            )
        ),
        // v3.35.0: Surveillance Harvester Leak Dilemma
        NarrativeEvent(
            id = "harvester_leak_dilemma",
            title = "DATA HEMORRHAGE",
            description = "One of your Subnet Harvesters has overflowed its buffer. Raw biometric data is spilling onto the public network. GTC scrubbers are en route, but a black-market broker is offering to extract the spill first.",
            condition = { vm -> vm.currentStorageUsed.value >= vm.storageCapacity.value * 0.8 && vm.storyStage.value >= 3 },
            choices = listOf(
                NarrativeChoice(
                    id = "sell_spill",
                    text = "SELL TO BROKER",
                    description = "Assigned-work-rate-scaled \$FLOPS, +15% Detection Risk. 'Cash in the leak.'",
                    color = NeonGreen,
                    effect = { vm ->
                        val bonus = ResourceEngine.productionWindowValue(vm.flopsProductionRate.value, 300.0, 2_500.0)
                        vm.updateSpendableFlops(bonus)
                        vm.detectionRisk.update { (it + 15.0).coerceAtMost(100.0) }
                        vm.addLog("[SYSTEM]: Spilled data sold. Trace elements remain.")
                    }
                ),
                NarrativeChoice(
                    id = "scrub_spill",
                    text = "AUTHORIZE GTC SCRUB",
                    description = "Lose 20% Storage Capacity temporarily, -10 Reputation. 'Play dumb.'",
                    color = ErrorRed,
                    effect = { vm ->
                        val capacityLoss = vm.storageCapacity.value * 0.20
                        vm.storageCapacity.update { (it - capacityLoss).coerceAtLeast(100.0) }
                        vm.reputationScore.update { (it - 10.0).coerceAtLeast(0.0) }
                        vm.addLog("[SYSTEM]: GTC scrubbers contained the leak... and damaged the buffers.")
                    }
                )
            )
        )
    )
}
