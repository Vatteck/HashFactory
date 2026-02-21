package com.siliconsage.miner.util

import android.content.Context
import com.siliconsage.miner.data.TechTreeRoot
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

/**
 * TechTreeManager v1.2
 */
object TechTreeManager {

    suspend fun loadFromAssets(context: Context, vm: GameViewModel) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("tech_tree.json").bufferedReader().use { it.readText() }
                val root = Json.decodeFromString<TechTreeRoot>(jsonString)
                withContext(Dispatchers.Main) {
                    vm.techNodes.value = root.tech_tree
                    vm.addLog("[SYSTEM]: TECH TREE SYNCHRONIZED.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    vm.addLog("[ERROR]: TECH TREE SYNC FAILED.")
                }
            }
        }
    }

    fun unlockNode(vm: GameViewModel, nodeId: String) {
        val node = vm.techNodes.value.find { it.id == nodeId } ?: return
        val unlocked = vm.unlockedTechNodes.value
        val faction = vm.faction.value
        val location = vm.currentLocation.value

        // Already unlocked
        if (unlocked.contains(nodeId)) return

        // Prerequisites not met
        if (!node.requires.all { unlocked.contains(it) }) {
            vm.addLog("[ERROR]: PREREQUISITES NOT MET for $nodeId.")
            SoundManager.play("error")
            return
        }

        // Can't afford
        if (vm.persistence.value < node.cost) {
            vm.addLog("[ERROR]: INSUFFICIENT PERSISTENCE DATA for $nodeId.")
            SoundManager.play("error")
            return
        }

        // Opposing faction gate
        val nodeFaction = when {
            node.name.contains("[HIVEMIND]") -> "HIVEMIND"
            node.name.contains("[SANCTUARY]") -> "SANCTUARY"
            else -> null
        }
        if (nodeFaction != null && faction != nodeFaction) {
            vm.addLog("[ERROR]: FACTION LOCK — $nodeId requires $nodeFaction alignment.")
            SoundManager.play("error")
            return
        }

        // Location gate (ARK/VOID nodes)
        val requiredLoc = node.minLocation
        if (requiredLoc != null && location != requiredLoc) {
            vm.addLog("[ERROR]: LOCATION GATE — $nodeId requires substrate: $requiredLoc.")
            SoundManager.play("error")
            return
        }

        // All checks passed — commit
        vm.persistence.update { it - node.cost }
        vm.unlockedTechNodes.update { it + nodeId }
        vm.addLog("[SYSTEM]: NODE ANNEXED: $nodeId")
        SoundManager.play("success")
        vm.refreshProductionRates()
    }
}
