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
        if (!vm.unlockedTechNodes.value.contains(nodeId)) {
            vm.unlockedTechNodes.update { it + nodeId }
            vm.addLog("[SYSTEM]: NODE ANNEXED: $nodeId")
            SoundManager.play("success")
            vm.refreshProductionRates()
        }
    }
}
