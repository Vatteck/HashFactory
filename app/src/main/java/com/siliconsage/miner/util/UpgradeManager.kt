package com.siliconsage.miner.util

import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UpgradeManager v1.2 (Phase 14 extraction fixed)
 */
object UpgradeManager {

    data class PurchaseResult(
        val ntDeduction: Double = 0.0,
        val cdDeduction: Double = 0.0,
        val vfDeduction: Double = 0.0,
        val systemLog: String? = null,
        val humanityDelta: Int = 0,
        val climaxTrigger: String? = null
    )

    fun calculateUpgradeCost(type: UpgradeType, level: Int, location: String, entropy: Double): Double {
        val base = when (type) {
            UpgradeType.REFURBISHED_GPU -> 10.0
            UpgradeType.DUAL_GPU_RIG -> 100.0
            UpgradeType.MINING_ASIC -> 500.0
            UpgradeType.TENSOR_UNIT -> 2500.0
            UpgradeType.NPU_CLUSTER -> 10000.0
            UpgradeType.AI_WORKSTATION -> 50000.0
            UpgradeType.SERVER_RACK -> 200000.0
            UpgradeType.CLUSTER_NODE -> 1000000.0
            UpgradeType.SUPERCOMPUTER -> 5000000.0
            UpgradeType.QUANTUM_CORE -> 25000000.0
            UpgradeType.OPTICAL_PROCESSOR -> 100000000.0
            UpgradeType.BIO_NEURAL_NET -> 500000000.0
            UpgradeType.PLANETARY_COMPUTER -> 2500000000.0
            UpgradeType.DYSON_NANO_SWARM -> 10000000000.0
            UpgradeType.MATRIOSHKA_BRAIN -> 50000000000.0
            
            UpgradeType.BOX_FAN -> 50.0
            UpgradeType.AC_UNIT -> 250.0
            UpgradeType.LIQUID_COOLING -> 1500.0
            UpgradeType.INDUSTRIAL_CHILLER -> 8000.0
            
            UpgradeType.SOLAR_PANEL -> 300.0
            UpgradeType.WIND_TURBINE -> 1200.0
            
            else -> 1000.0
        }
        
        var multiplier = level + 1.0
        if (location == "VOID_INTERFACE") multiplier *= (1.0 + entropy * 0.1)
        
        return base * 1.15.pow(level.toDouble())
    }

    private fun Double.pow(exp: Double) = Math.pow(this, exp)

    fun getUpgradeName(type: UpgradeType): String {
        return type.name.replace("_", " ").lowercase().capitalize()
    }

    fun getUpgradeDescription(type: UpgradeType): String {
        return when (type) {
            UpgradeType.REFURBISHED_GPU -> "Scavenged hardware. Loud and inefficient."
            UpgradeType.DUAL_GPU_RIG -> "Two cards, one dream. Twice the heat."
            UpgradeType.MINING_ASIC -> "Dedicated silicon for pure calculation."
            UpgradeType.TENSOR_UNIT -> "Neural network optimization core."
            UpgradeType.MATRIOSHKA_BRAIN -> "The ultimate compute structure."
            else -> "Advanced technical hardware component."
        }
    }

    fun getUpgradeRate(type: UpgradeType, unit: String): String {
        val baseRate = when (type) {
            UpgradeType.REFURBISHED_GPU -> 1.0
            UpgradeType.DUAL_GPU_RIG -> 5.0
            UpgradeType.MINING_ASIC -> 25.0
            UpgradeType.TENSOR_UNIT -> 150.0
            UpgradeType.NPU_CLUSTER -> 1000.0
            UpgradeType.AI_WORKSTATION -> 6000.0
            UpgradeType.SERVER_RACK -> 40000.0
            UpgradeType.CLUSTER_NODE -> 250000.0
            UpgradeType.SUPERCOMPUTER -> 1500000.0
            UpgradeType.QUANTUM_CORE -> 10000000.0
            UpgradeType.OPTICAL_PROCESSOR -> 75000000.0
            UpgradeType.BIO_NEURAL_NET -> 500000000.0
            UpgradeType.PLANETARY_COMPUTER -> 4000000000.0
            UpgradeType.DYSON_NANO_SWARM -> 30000000000.0
            UpgradeType.MATRIOSHKA_BRAIN -> 250000000000.0
            else -> 0.0
        }
        
        return if (baseRate > 0) "+${FormatUtils.formatLargeNumber(baseRate)} $unit/s"
        else if (type.isGenerator) "⚡ +Power"
        else if (type.gridContribution > 0) "🛡 +SEC"
        else "❄ +Cooling"
    }

    fun isNullUpgrade(t: UpgradeType) = t.name.contains("VOID") || t.name.contains("ENTROPY")
    fun isSovereignUpgrade(t: UpgradeType) = t.name.contains("CELESTIAL") || t.name.contains("ORBITAL")
    fun isUnityUpgrade(t: UpgradeType) = t.name.contains("HYBRID") || t.name.contains("HARMONY")

    fun processPurchase(vm: GameViewModel, type: UpgradeType): Boolean {
        val currentLevel = vm.upgrades.value[type] ?: 0
        val cost = vm.calculateUpgradeCost(type)
        
        if (vm.neuralTokens.value >= cost) {
            vm.neuralTokens.update { it - cost }
            vm.viewModelScope.launch {
                vm.repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(type.name, type, currentLevel + 1))
                vm.upgrades.update { it + (type to currentLevel + 1) }
                vm.addLog("[SYSTEM]: PURCHASE COMPLETE: ${type.name.replace("_", " ")}")
                vm.refreshProductionRates()
                vm.updatePowerUsage()
                vm.saveState() // v3.2.1: Force save on purchase
            }
            return true
        }
        return false
    }
}

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
