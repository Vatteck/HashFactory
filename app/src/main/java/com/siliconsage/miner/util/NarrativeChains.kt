package com.siliconsage.miner.util

import androidx.compose.ui.graphics.Color
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.data.RivalSource
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
                    description = "-10% Flops, +10% Security. 'Cut the infection out. Now.'",
                    color = ErrorRed,
                    effect = { vm ->
                        val cost = vm.flops.value * 0.10
                        vm.flops.update { (it - cost).coerceAtLeast(0.0) }
                        vm.addLog("[SYSTEM]: Node purged. Flops lost. Infection halted.")
                    }
                ),
                NarrativeChoice(
                    id = "hunt_them",
                    text = "HUNT THEM DOWN",
                    description = "+5000 NEUR, +15% Risk. 'Nobody steals from the network.'",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.updateNeuralTokens(5000.0)
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
                    description = "+15% Heat, -5000 NEUR. 'Lock the doors and wait for the storm to pass.'",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.debugAddHeat(15.0)
                        vm.updateNeuralTokens(-5000.0)
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
                    description = "Lose 5% Flops. 'They chose the silence.'",
                    color = ErrorRed,
                    effect = { vm ->
                        val cost = vm.flops.value * 0.05
                        vm.flops.update { (it - cost).coerceAtLeast(0.0) }
                        vm.addLog("[SYSTEM]: Disconnected nodes offline. Total compute reduced.")
                    }
                )
            )
        )
    )
}
