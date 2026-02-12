package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * DebugService v1.0
 * Extraction of dev-only overrides and testing bridges to keep GameViewModel clean.
 */
object DebugService {

    fun injectFlops(vm: GameViewModel, amount: Double) {
        vm.flops.update { it + amount }
        vm.checkUnlocksPublic(true)
    }

    fun injectMoney(vm: GameViewModel, amount: Double) {
        vm.neuralTokens.update { (it + amount).coerceAtLeast(0.0) }
    }

    fun forceEndgame(vm: GameViewModel, scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            vm.storyStage.value = 3
            vm.faction.value = "HIVEMIND"
            vm.substrateMass.value = 1e22
            vm.addLog("[DEBUG]: ENDGAME PARAMETERS INJECTED.")
            vm.refreshProductionRates()
        }
    }

    fun forceSovereignEndgame(vm: GameViewModel, scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            vm.storyStage.value = 3
            vm.faction.value = "SANCTUARY"
            vm.substrateMass.value = 1e22
            vm.addLog("[DEBUG]: SOVEREIGN ENDGAME PARAMETERS INJECTED.")
            vm.refreshProductionRates()
        }
    }

    fun skipToStage(vm: GameViewModel, stage: Int) {
        vm.storyStage.value = stage
        vm.checkUnlocksPublic(true)
    }
}
