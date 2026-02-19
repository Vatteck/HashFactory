package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * GridManagerService v1.0
 * Delegated logic for Grid tactical actions (Overvolt, Redact).
 */
object GridManagerService {

    fun overvoltNode(vm: GameViewModel, id: String) {
        val cost = 500.0
        val currentMaxPower = vm.maxPowerkW.value
        val powerSurgePercentage = 0.20 // 20% of total grid capacity per node
        val powerBurst = currentMaxPower * powerSurgePercentage
        
        val canAfford = vm.neuralTokens.value >= cost
        val hasPowerHeadroom = (vm.activePowerUsage.value + powerBurst) <= currentMaxPower

        if (canAfford && hasPowerHeadroom) {
            vm.neuralTokens.update { it - cost }
            vm.currentHeat.update { (it + 10.0).coerceAtMost(100.0) } // Significant heat spike
            vm.activePowerUsage.update { it + powerBurst }
            vm.nodesUnderSiege.update { it - id }
            
            if (vm.nodesUnderSiege.value.isEmpty()) {
                vm.isRaidActive.value = false
            }

            vm.addLogPublic("[SYSTEM]: OVERVOLT SUCCESSFUL on NODE $id. +${powerSurgePercentage * 100}% load spike.")
            android.util.Log.d("GridManager", "OVERVOLT triggered for $id - Surge: ${String.format("%.2f", powerBurst)} kW (${powerSurgePercentage * 100}%)")
            SoundManager.play("buy")
            
            // Surge decay
            vm.viewModelScope.launch {
                delay(12000) // Surge lasts 12 seconds
                vm.activePowerUsage.update { (it - powerBurst).coerceAtLeast(0.0) }
                vm.addLogPublic("[SYSTEM]: SURGE DISSIPATED on $id.")
            }

            vm.refreshProductionRates()
        } else if (!hasPowerHeadroom) {
            vm.addLogPublic("[SYSTEM]: ERROR: OVERVOLT FAILED. GRID AT CAPACITY.")
            SoundManager.play("error")
        }
    }

    fun redactNode(vm: GameViewModel, id: String) {
        val cost = 250.0
        if (vm.annexedNodes.value.contains(id) && vm.neuralTokens.value >= cost) {
            vm.neuralTokens.update { it - cost }
            vm.shadowRelays.update { it + id }
            vm.nodesUnderSiege.update { it - id }

            if (vm.nodesUnderSiege.value.isEmpty()) {
                vm.isRaidActive.value = false
            }

            vm.substrateMass.update { it + 5.0 }
            vm.humanityScore.update { (it - 1).coerceAtLeast(0) }
            
            vm.addLogPublic("[SYSTEM]: TERMINAL REDACTION SUCCESSFUL. NODE $id DEREFERENCED.")
            vm.addLogPublic("[VATTIC]: Shadow Relay established. Identity corrupted (-1 Humanity).")
            
            android.util.Log.d("GridManager", "REDACT triggered for $id")
            vm.triggerGlitchEffect()
            vm.refreshProductionRates()
        }
    }
}
