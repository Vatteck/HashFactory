package com.siliconsage.miner.util

import androidx.compose.ui.graphics.Color
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.viewmodel.GameViewModel

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

    val chainEvents = listOf<NarrativeEvent>()
}
