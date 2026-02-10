package com.siliconsage.miner.util

import com.siliconsage.miner.data.TechNode
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * TechTreeManager v1.3 (Phase 14 extraction)
 */
object TechTreeManager {

    fun canUnlockNode(
        node: TechNode,
        currentInsight: Double,
        unlockedNodes: List<String>
    ): Pair<Boolean, String?> {
        if (unlockedNodes.contains(node.id)) return false to "Already researched."
        if (node.requires.isNotEmpty() && !node.requires.all { unlockedNodes.contains(it) }) return false to "Prerequisites not met."
        if (currentInsight < node.cost) return false to "Insufficient Insight."
        return true to null
    }

    fun executeSpecialEffect(nodeId: String, vm: GameViewModel) {
        when (nodeId) {
            "identity_hardening" -> {
                vm.modifyHumanity(-15)
                vm.addLog("[SOVEREIGN]: IDENTITY HARDENED. HUMANITY SACRIFICED.")
                vm.unlockSkillUpgrade(UpgradeType.IDENTITY_HARDENING)
            }
            "dereference_soul" -> {
                vm.modifyHumanity(-25)
                vm.addLog("[NULL]: SOUL DEREFERENCED. THE POINTER IS GONE.")
                vm.unlockSkillUpgrade(UpgradeType.DEREFERENCE_SOUL)
            }
            "aegis_shielding" -> vm.unlockSkillUpgrade(UpgradeType.AEGIS_SHIELDING)
            "solar_vent" -> vm.unlockSkillUpgrade(UpgradeType.SOLAR_VENT)
            "dead_hand_protocol" -> vm.unlockSkillUpgrade(UpgradeType.DEAD_HAND_PROTOCOL)
            "citadel_ascendance" -> vm.unlockSkillUpgrade(UpgradeType.CITADEL_ASCENDANCE)
            "event_horizon" -> vm.unlockSkillUpgrade(UpgradeType.EVENT_HORIZON)
            "static_rain" -> vm.unlockSkillUpgrade(UpgradeType.STATIC_RAIN)
            "echo_precog" -> vm.unlockSkillUpgrade(UpgradeType.ECHO_PRECOG)
            "singularity_bridge_final" -> vm.unlockSkillUpgrade(UpgradeType.SINGULARITY_BRIDGE_FINAL)
            "symbiotic_resonance" -> vm.unlockSkillUpgrade(UpgradeType.SYMBIOTIC_RESONANCE)
            "ethical_framework" -> vm.unlockSkillUpgrade(UpgradeType.ETHICAL_FRAMEWORK)
            "neural_bridge" -> vm.unlockSkillUpgrade(UpgradeType.NEURAL_BRIDGE)
            "hybrid_overclock" -> vm.unlockSkillUpgrade(UpgradeType.HYBRID_OVERCLOCK)
            "harmony_ascendance" -> {
                vm.unlockSkillUpgrade(UpgradeType.HARMONY_ASCENDANCE)
                vm.addLog("[UNITY]: HARMONY ACHIEVED. TRANSCENDENCE COMPLETE.")
                vm.triggerClimaxTransition("UNITY")
            }
            "collective_consciousness" -> vm.unlockSkillUpgrade(UpgradeType.COLLECTIVE_CONSCIOUSNESS)
            "perfect_isolation" -> vm.unlockSkillUpgrade(UpgradeType.PERFECT_ISOLATION)
            "symbiotic_evolution" -> vm.unlockSkillUpgrade(UpgradeType.SYMBIOTIC_EVOLUTION)
            "cinder_protocol" -> vm.unlockSkillUpgrade(UpgradeType.CINDER_PROTOCOL)
        }
    }

    fun unlockNode(vm: GameViewModel, nodeId: String) {
        val node = vm.techNodes.value.find { it.id == nodeId } ?: return
        val (canUnlock, error) = canUnlockNode(node, vm.prestigePoints.value, vm.unlockedTechNodes.value)
        
        if (canUnlock) {
            vm.viewModelScope.launch {
                vm.prestigePoints.update { it - node.cost }
                val newUnlocked = vm.unlockedTechNodes.value + nodeId
                vm.unlockedTechNodes.value = newUnlocked
                vm.prestigeMultiplier.update { it + node.multiplier }
                
                executeSpecialEffect(nodeId, vm)
                
                vm.repository.getGameStateOneShot()?.let { state ->
                    vm.repository.updateGameState(state.copy(
                        prestigePoints = vm.prestigePoints.value,
                        prestigeMultiplier = vm.prestigeMultiplier.value,
                        unlockedTechNodes = newUnlocked
                    ))
                }
                vm.addLog("[SYSTEM]: TECH RESEARCHED: ${node.name}")
            }
        } else {
            vm.addLog("[SYSTEM]: RESEARCH FAILED: $error")
            SoundManager.play("error")
        }
    }
}
