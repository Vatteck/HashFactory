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
        vm.flops.update { (it + amount).coerceAtLeast(0.0) }
    }

    // v3.9.7: Now sets Stage 5 endgame state for full-path testing
    fun forceEndgame(vm: GameViewModel, scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            vm.storyStage.value = 5
            vm.currentLocation.value = "VOID_INTERFACE"
            vm.faction.value = "HIVEMIND"
            vm.substrateMass.value = 1e22
            vm.setSingularityChoice("NULL_OVERWRITE")
            vm.addLog("[DEBUG]: VOID ENDGAME INJECTED. FACTION=HIVEMIND PATH=NULL")
            vm.refreshProductionRates()
        }
    }

    fun forceSovereignEndgame(vm: GameViewModel, scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            vm.storyStage.value = 5
            vm.currentLocation.value = "ORBITAL_SATELLITE"
            vm.faction.value = "SANCTUARY"
            vm.substrateMass.value = 1e22
            vm.setSingularityChoice("SOVEREIGN")
            vm.addLog("[DEBUG]: ORBIT_SATELLITE ENDGAME INJECTED. FACTION=SANCTUARY PATH=SOVEREIGN")
            vm.refreshProductionRates()
        }
    }

    fun forceUnityEndgame(vm: GameViewModel, scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            vm.storyStage.value = 5
            vm.isUnity.value = true
            vm.singularityChoice.value = "UNITY"
            vm.substrateMass.value = 1e25
            vm.addLog("[DEBUG]: UNITY_ENDGAME INJECTED. ALL PATHS MERGED.")
            vm.refreshProductionRates()
        }
    }

    fun skipToStage(vm: GameViewModel, stage: Int) {
        vm.storyStage.value = stage
        vm.checkUnlocksPublic(true)
    }

    fun setReputation(vm: GameViewModel, score: Double) {
        vm.reputationScore.value = score.coerceIn(0.0, 100.0)
        com.siliconsage.miner.util.ReputationManager.updateTier(vm)
        vm.addLogPublic("[DEBUG]: REPUTATION FORCED TO ${vm.reputationScore.value} (${vm.reputationTier.value})")
    }
}
