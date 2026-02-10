package com.siliconsage.miner.util

import androidx.lifecycle.viewModelScope
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
        unlockedNodes: List<String>,
        playerFaction: String,
        hasSovereignHistory: Boolean = false,
        hasNullHistory: Boolean = false
    ): Pair<Boolean, String?> {
        if (unlockedNodes.contains(node.id)) return false to "Already researched."
        
        // Faction locking logic
        val isSancNode = node.description.contains("[SANCTUARY]") || node.description.contains("[NG+ SOVEREIGN]")
        val isHiveNode = node.description.contains("[HIVEMIND]") || node.description.contains("[NG+ NULL]")
        val isUnityNode = node.description.contains("[UNITY]") || node.description.contains("[NG+ UNITY]")

        if (isSancNode && playerFaction == "HIVEMIND") return false to "Incompatible with current Substrate (HIVEMIND)."
        if (isHiveNode && playerFaction == "SANCTUARY") return false to "Incompatible with current Substrate (SANCTUARY)."
        
        if (isUnityNode && !(hasSovereignHistory && hasNullHistory)) {
            return false to "UNITY requires previous MIGRATION of both paths."
        }

        if (node.requires.isNotEmpty() && !node.requires.all { unlockedNodes.contains(it) }) return false to "Prerequisites not met."
        if (currentInsight < node.cost) return false to "Insufficient PERSISTENCE DATA."
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
        
        // Pass history flags for Unity/Path unlocking
        val (canUnlock, error) = canUnlockNode(
            node = node,
            currentInsight = vm.prestigePoints.value,
            unlockedNodes = vm.unlockedTechNodes.value,
            playerFaction = vm.faction.value,
            hasSovereignHistory = vm.completedFactions.value.contains("SANCTUARY") || vm.completedFactions.value.contains("SOVEREIGN"),
            hasNullHistory = vm.completedFactions.value.contains("HIVEMIND") || vm.completedFactions.value.contains("NULL")
        )
        
        if (canUnlock) {
            vm.viewModelScope.launch {
                vm.prestigePoints.update { it - node.cost }
                val newUnlocked = vm.unlockedTechNodes.value + nodeId
                vm.unlockedTechNodes.value = newUnlocked
                vm.prestigeMultiplier.update { it + node.multiplier }
                
                executeSpecialEffect(nodeId, vm)
                
                // Note: repository access in VM is public for now to allow manager access
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
