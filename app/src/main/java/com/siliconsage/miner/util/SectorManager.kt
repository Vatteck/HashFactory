package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update
import kotlin.math.pow

/**
 * SectorManager v1.3
 * Handles city grid annexation, global grid scaling, and node maintenance.
 */
object SectorManager {

    private const val MAX_OFFLINE_NODES = 5
    private const val ANNEXATION_SPEED = 0.05f // 5% per simulation tick

    fun annexNode(vm: GameViewModel, coord: String) {
        if (!vm.annexedNodes.value.contains(coord) && !vm.annexingNodes.value.containsKey(coord)) {
            vm.annexingNodes.update { it + (coord to 0.0f) }
            vm.addLogPublic("[SYSTEM]: INITIALIZING ANNEXATION AT $coord...")
            SoundManager.play("steam")
            vm.saveStatePublic()
        }
    }

    /**
     * Process active annexations - Called from the Simulation Clock
     */
    fun processAnnexations(vm: GameViewModel) {
        val currentAnnexing = vm.annexingNodes.value
        if (currentAnnexing.isEmpty()) return

        val updatedMap = currentAnnexing.toMutableMap()
        val completed = mutableListOf<String>()

        currentAnnexing.forEach { (coord, progress) ->
            val newProgress = progress + ANNEXATION_SPEED
            if (newProgress >= 1.0f) {
                completed.add(coord)
                updatedMap.remove(coord)
            } else {
                updatedMap[coord] = newProgress
            }
        }

        vm.annexingNodes.value = updatedMap

        completed.forEach { coord ->
            vm.annexedNodes.update { it + coord }
            vm.addLogPublic("[SYSTEM]: ANNEXATION AT $coord COMPLETE. NODE ONLINE.")
            SoundManager.play("victory")
            vm.refreshProductionRates() // Update bonuses
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
            vm.addLogPublic("[SYSTEM]: ERROR: Insufficient funds for node upgrade.")
            SoundManager.play("error")
        }
    }

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
                vm.addLogPublic("[SYSTEM]: NODE $oldest LOST PERMANENTLY. TOO MANY OFFLINE NODES.")
                updated.drop(1).toSet()
            } else {
                updated
            }
        }
    }

    fun getInitialGlobalGrid(singularity: String): Map<String, com.siliconsage.miner.data.SectorState> {
        val sectors = mutableMapOf<String, com.siliconsage.miner.data.SectorState>()
        if (singularity == "SOVEREIGN") {
            sectors["LEO"] = com.siliconsage.miner.data.SectorState("LEO", true)
            sectors["LUN"] = com.siliconsage.miner.data.SectorState("LUN", false)
            sectors["MAR"] = com.siliconsage.miner.data.SectorState("MAR", false)
            sectors["DYS"] = com.siliconsage.miner.data.SectorState("DYS", false)
        } else {
            sectors["HOR"] = com.siliconsage.miner.data.SectorState("HOR", true)
            sectors["SPI"] = com.siliconsage.miner.data.SectorState("SPI", false)
            sectors["ENT"] = com.siliconsage.miner.data.SectorState("ENT", false)
            sectors["ROT"] = com.siliconsage.miner.data.SectorState("ROT", false)
        }
        return sectors
    }
}
