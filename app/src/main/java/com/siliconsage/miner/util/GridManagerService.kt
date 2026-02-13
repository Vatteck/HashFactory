package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update

/**
 * GridManagerService v1.0
 * Delegated logic for Grid tactical actions (Overvolt, Redact).
 */
object GridManagerService {

    fun overvoltNode(vm: GameViewModel, id: String) {
        val cost = 500.0
        if (vm.neuralTokens.value >= cost) {
            vm.neuralTokens.update { it - cost }
            vm.currentHeat.update { (it + 5.0).coerceAtMost(100.0) }
            vm.nodesUnderSiege.update { it - id }
            
            if (vm.nodesUnderSiege.value.isEmpty()) {
                vm.isRaidActive.value = false
            }

            vm.addLogPublic("[SYSTEM]: OVERVOLT SUCCESSFUL on NODE $id. Repelling GTC probes.")
            SoundManager.play("buy")
            vm.refreshProductionRates()
        }
    }

    fun redactNode(vm: GameViewModel, id: String) {
        if (vm.annexedNodes.value.contains(id)) {
            vm.shadowRelays.update { it + id }
            vm.nodesUnderSiege.update { it - id }

            if (vm.nodesUnderSiege.value.isEmpty()) {
                vm.isRaidActive.value = false
            }

            vm.substrateMass.update { it + 5.0 }
            vm.addLogPublic("[SYSTEM]: TERMINAL REDACTION SUCCESSFUL. NODE $id DEREFERENCED.")
            vm.addLogPublic("[VATTIC]: Shadow Relay established. Connectivity maintained.")
            
            vm.triggerGlitchEffect()
            vm.refreshProductionRates()
        }
    }
}
