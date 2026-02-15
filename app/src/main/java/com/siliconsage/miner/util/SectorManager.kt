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
            
            // v3.5: Action Reactions - Trigger Subnet chatter on annexation
            vm.triggerSubnetReaction("ANNEXATION", coord)

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
        sectors["METRO"] = com.siliconsage.miner.data.SectorState("METRO", isUnlocked = true)
        sectors["NA_NODE"] = com.siliconsage.miner.data.SectorState("NA_NODE", isUnlocked = false)
        sectors["EURASIA"] = com.siliconsage.miner.data.SectorState("EURASIA", isUnlocked = false)
        sectors["PACIFIC"] = com.siliconsage.miner.data.SectorState("PACIFIC", isUnlocked = false)
        sectors["AFRICA"] = com.siliconsage.miner.data.SectorState("AFRICA", isUnlocked = false)
        sectors["ARCTIC"] = com.siliconsage.miner.data.SectorState("ARCTIC", isUnlocked = false)
        sectors["ANTARCTIC"] = com.siliconsage.miner.data.SectorState("ANTARCTIC", isUnlocked = false)
        sectors["ORBITAL_PRIME"] = com.siliconsage.miner.data.SectorState("ORBITAL_PRIME", isUnlocked = false)
        return sectors
    }

    /**
     * v3.2.51: Calculate dynamic adjacency and synergy bonuses
     */
    fun calculateSectorYields(
        location: String,
        activeSectors: Map<String, com.siliconsage.miner.data.SectorState>
    ): Map<String, Double> {
        val adjacencyMap = mapOf(
            "METRO" to listOf("NA_NODE", "EURASIA", "AFRICA"),
            "NA_NODE" to listOf("METRO", "ARCTIC", "PACIFIC"),
            "EURASIA" to listOf("METRO", "PACIFIC", "ARCTIC", "AFRICA"),
            "PACIFIC" to listOf("NA_NODE", "EURASIA", "ANTARCTIC"),
            "AFRICA" to listOf("METRO", "EURASIA", "ANTARCTIC"),
            "ARCTIC" to listOf("NA_NODE", "EURASIA"),
            "ANTARCTIC" to listOf("PACIFIC", "AFRICA"),
            "ORBITAL_PRIME" to activeSectors.keys.toList() // Connects to all active ground sectors
        )

        val yields = mutableMapOf<String, Double>()
        activeSectors.forEach { (id, state) ->
            if (state.isUnlocked) {
                var base = 1.0 // Base multiplier
                
                // 1. Adjacency Bonus (+25% per active neighbor)
                val neighbors = adjacencyMap[id] ?: emptyList()
                val activeNeighbors = neighbors.count { activeSectors[it]?.isUnlocked == true }
                base += activeNeighbors * 0.25
                
                // 2. Path Synergy
                if (location == "ORBITAL_SATELLITE" && id == "NA_NODE") base *= 1.15
                if (location == "VOID_INTERFACE" && id == "EURASIA") base *= 1.15
                
                yields[id] = base
            }
        }
        return yields
    }

    /**
     * v3.2.51: Global Passive Modifiers
     */
    fun getGlobalMultipliers(activeSectors: Map<String, com.siliconsage.miner.data.SectorState>): Double {
        var mult = 1.0
        if (activeSectors["ORBITAL_PRIME"]?.isUnlocked == true) mult *= 1.50
        return mult
    }
}
