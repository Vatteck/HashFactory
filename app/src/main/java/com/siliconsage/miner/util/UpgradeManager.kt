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
            // --- Computing Hardware ---
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

            // --- Cooling (progressive tiers ~10x each step) ---
            UpgradeType.BOX_FAN -> 25.0
            UpgradeType.AC_UNIT -> 250.0
            UpgradeType.LIQUID_COOLING -> 2000.0
            UpgradeType.INDUSTRIAL_CHILLER -> 15000.0
            UpgradeType.SUBMERSION_VAT -> 75000.0
            UpgradeType.CRYOGENIC_CHAMBER -> 400000.0
            UpgradeType.LIQUID_NITROGEN -> 2000000.0
            UpgradeType.BOSE_CONDENSATE -> 12000000.0
            UpgradeType.ENTROPY_REVERSER -> 80000000.0
            UpgradeType.DIMENSIONAL_VENT -> 600000000.0

            // --- Power Grid (capacity purchases) ---
            UpgradeType.RESIDENTIAL_TAP -> 500.0
            UpgradeType.INDUSTRIAL_FEED -> 5000.0
            UpgradeType.SUBSTATION_LEASE -> 50000.0
            UpgradeType.NUCLEAR_CORE -> 500000.0

            // --- Power Generators ---
            UpgradeType.SOLAR_PANEL -> 300.0
            UpgradeType.WIND_TURBINE -> 1200.0
            UpgradeType.DIESEL_GENERATOR -> 8000.0
            UpgradeType.GEOTHERMAL_BORE -> 60000.0
            UpgradeType.NUCLEAR_REACTOR -> 500000.0
            UpgradeType.FUSION_CELL -> 5000000.0
            UpgradeType.ORBITAL_COLLECTOR -> 50000000.0
            UpgradeType.DYSON_LINK -> 1000000000.0

            // --- Efficiency ---
            UpgradeType.GOLD_PSU -> 3000.0
            UpgradeType.SUPERCONDUCTOR -> 25000.0
            UpgradeType.AI_LOAD_BALANCER -> 150000.0

            // --- Security (each tier ~5-8x previous) ---
            UpgradeType.BASIC_FIREWALL -> 500.0
            UpgradeType.IPS_SYSTEM -> 3500.0
            UpgradeType.AI_SENTINEL -> 25000.0
            UpgradeType.QUANTUM_ENCRYPTION -> 200000.0
            UpgradeType.OFFGRID_BACKUP -> 1500000.0

            else -> 1000.0
        }

        // VOID_INTERFACE entropy surcharge — applies on top of level scaling
        val entropyMultiplier = if (location == "VOID_INTERFACE") (1.0 + entropy * 0.1) else 1.0

        return base * 1.15.pow(level.toDouble()) * entropyMultiplier
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
            UpgradeType.REFURBISHED_GPU -> 1.5
            UpgradeType.DUAL_GPU_RIG -> 7.5
            UpgradeType.MINING_ASIC -> 35.0
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

    fun isNullUpgrade(t: UpgradeType) = t.name.contains("VOID") || t.name.contains("ENTROPY") || 
                                       t.name.contains("GHOST") || t.name.contains("SHADOW") || 
                                       t.name.contains("WRAITH") || t.name.contains("MIST") || 
                                       t.name.contains("DARK_MATTER") || t.name.contains("EXISTENCE") || 
                                       t.name.contains("SINGULARITY") || t.name.contains("ECHO") || 
                                       t.name.contains("SOUL") || t.name.contains("STATIC")

    fun isSovereignUpgrade(t: UpgradeType) = t.name.contains("CELESTIAL") || t.name.contains("ORBITAL") || 
                                            t.name.contains("SAIL") || t.name.contains("LASER") || 
                                            t.name.contains("CRYOGENIC") || t.name.contains("RADIATOR") || 
                                            t.name.contains("AEGIS") || t.name.contains("VENT") || 
                                            t.name.contains("IDENTITY") || t.name.contains("CITADEL") || 
                                            t.name.contains("DEAD_HAND")
    fun isUnityUpgrade(t: UpgradeType) = t.name.contains("HYBRID") || t.name.contains("HARMONY")

    fun processPurchase(vm: GameViewModel, type: UpgradeType): Boolean {
        val currentLevel = vm.upgrades.value[type] ?: 0
        val cost = vm.calculateUpgradeCost(type)
        val stage = vm.storyStage.value
        
        // v3.2.46: Handle consolidated Substrate Mass for Stage 3+
        if (stage >= 3) {
            if (vm.substrateMass.value >= cost) {
                vm.substrateMass.update { it - cost }
                completePurchase(vm, type, currentLevel)
                return true
            }
        } else {
            if (vm.neuralTokens.value >= cost) {
                vm.neuralTokens.update { it - cost }
                completePurchase(vm, type, currentLevel)
                return true
            }
        }
        return false
    }

    private fun completePurchase(vm: GameViewModel, type: UpgradeType, currentLevel: Int) {
        vm.viewModelScope.launch {
            vm.repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(type.name, type, currentLevel + 1))
            vm.upgrades.update { it + (type to currentLevel + 1) }
            vm.addLog("[SYSTEM]: PURCHASE COMPLETE: ${type.name.replace("_", " ")}")
            vm.refreshProductionRates()
            vm.updatePowerUsage()
            vm.saveState() 
        }
    }
}

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
