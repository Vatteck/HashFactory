package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update
import kotlin.math.pow

/**
 * SectorManager v1.2 (Phase 14 extraction)
 */
object SectorManager {

    private const val MAX_OFFLINE_NODES = 5

    fun resolveRaidFailure(
        nodeId: String,
        vm: GameViewModel,
        onRaidFinished: (String) -> Unit
    ) {
        onRaidFinished(nodeId)
        
        vm.offlineNodes.update { current ->
            val updated = current + nodeId
            if (updated.size > MAX_OFFLINE_NODES) {
                val oldest = updated.first()
                vm.addLogPublic("[SYSTEM]: NODE $oldest LOST PERMANENTLY. Too many offline nodes.")
                updated.drop(1).toSet()
            } else {
                updated
            }
        }
        
        vm.addLogPublic("[SYSTEM]: WARNING: Grid capacity reduced. Production -15% per offline node.")
        vm.addLogPublic("[SYSTEM]: Re-annexation required to restore full capacity.")
    }

    fun calculateReannexationCost(neuralTokens: Double): Double {
        return (neuralTokens * 0.10).coerceAtLeast(10.0)
    }

    fun annexNode(vm: GameViewModel, coord: String) {
        if (!vm.annexedNodes.value.contains(coord) && !vm.annexingNodes.value.containsKey(coord)) {
            vm.annexingNodes.update { it + (coord to 0.0f) }
            vm.addLogPublic("[SYSTEM]: INITIALIZING ANNEXATION AT $coord...")
            SoundManager.play("steam")
            vm.saveStatePublic()
        }
    }

    fun upgradeGridNode(vm: GameViewModel, nodeId: String) {
        if (!vm.annexedNodes.value.contains(nodeId)) return
        
        val currentLevel = vm.gridNodeLevels.value[nodeId] ?: 1
        val cost = 1000.0 * 5.0.pow(currentLevel - 1)
        
        if (vm.neuralTokens.value >= cost) {
            vm.neuralTokens.update { it - cost }
            vm.gridNodeLevels.update { it + (nodeId to currentLevel + 1) }
            vm.addLogPublic("[SYSTEM]: NODE $nodeId UPGRADED TO LVL ${currentLevel + 1}.")
            SoundManager.play("buy")
            vm.saveStatePublic()
        } else {
            vm.addLogPublic("[SYSTEM]: ERROR: Insufficient funds for node upgrade (Need ${vm.formatLargeNumberPublic(cost)} \$N).")
            SoundManager.play("error")
        }
    }
}
