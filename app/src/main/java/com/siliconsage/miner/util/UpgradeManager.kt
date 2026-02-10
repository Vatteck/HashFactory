package com.siliconsage.miner.util

import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow

/**
 * UpgradeManager v1.1 (Phase 14 extraction)
 */
object UpgradeManager {

    fun calculateUpgradeCost(type: UpgradeType, level: Int, currentLocation: String, entropyLevel: Double): Double {
        val baseCost = when (type) {
            UpgradeType.REFURBISHED_GPU -> 10.0
            UpgradeType.DUAL_GPU_RIG -> 50.0
            UpgradeType.MINING_ASIC -> 250.0
            UpgradeType.TENSOR_UNIT -> 1500.0
            UpgradeType.NPU_CLUSTER -> 8000.0
            UpgradeType.AI_WORKSTATION -> 40000.0
            UpgradeType.SERVER_RACK -> 250000.0
            UpgradeType.CLUSTER_NODE -> 1500000.0
            UpgradeType.SUPERCOMPUTER -> 10000000.0
            UpgradeType.QUANTUM_CORE -> 75000000.0
            UpgradeType.OPTICAL_PROCESSOR -> 500000000.0
            UpgradeType.BIO_NEURAL_NET -> 5000000000.0
            UpgradeType.PLANETARY_COMPUTER -> 75000000000.0
            UpgradeType.DYSON_NANO_SWARM -> 1000000000000.0
            UpgradeType.MATRIOSHKA_BRAIN -> 50000000000000.0
            UpgradeType.BOX_FAN -> 50.0
            UpgradeType.AC_UNIT -> 250.0
            UpgradeType.LIQUID_COOLING -> 1500.0
            UpgradeType.INDUSTRIAL_CHILLER -> 10000.0
            UpgradeType.SUBMERSION_VAT -> 75000.0
            UpgradeType.CRYOGENIC_CHAMBER -> 500000.0
            UpgradeType.LIQUID_NITROGEN -> 4000000.0
            UpgradeType.BOSE_CONDENSATE -> 50000000.0
            UpgradeType.ENTROPY_REVERSER -> 1000000000.0
            UpgradeType.DIMENSIONAL_VENT -> 100000000000.0
            UpgradeType.BASIC_FIREWALL -> 500.0
            UpgradeType.IPS_SYSTEM -> 2500.0
            UpgradeType.AI_SENTINEL -> 15000.0
            UpgradeType.QUANTUM_ENCRYPTION -> 100000.0
            UpgradeType.OFFGRID_BACKUP -> 1000000.0
            UpgradeType.DIESEL_GENERATOR -> 2000.0
            UpgradeType.SOLAR_PANEL -> 500.0
            UpgradeType.WIND_TURBINE -> 1500.0
            UpgradeType.GEOTHERMAL_BORE -> 10000.0
            UpgradeType.NUCLEAR_REACTOR -> 150000.0
            UpgradeType.FUSION_CELL -> 5000000.0
            UpgradeType.ORBITAL_COLLECTOR -> 250000000.0
            UpgradeType.DYSON_LINK -> 10000000000.0
            UpgradeType.RESIDENTIAL_TAP -> 100.0
            UpgradeType.INDUSTRIAL_FEED -> 5000.0
            UpgradeType.SUBSTATION_LEASE -> 50000.0
            UpgradeType.NUCLEAR_CORE -> 10000000.0
            UpgradeType.GOLD_PSU -> 1000.0
            UpgradeType.SUPERCONDUCTOR -> 25000.0
            UpgradeType.AI_LOAD_BALANCER -> 100000.0
            UpgradeType.GHOST_CORE -> 100000000.0
            UpgradeType.SHADOW_NODE -> 10000000000.0
            UpgradeType.VOID_PROCESSOR -> 1000000000000.0
            UpgradeType.WRAITH_CORTEX -> 50000000000000.0
            UpgradeType.NEURAL_MIST -> 500000000000000.0
            UpgradeType.SINGULARITY_BRIDGE -> 10000000000000000.0
            UpgradeType.SOLAR_SAIL_ARRAY -> 1.0
            UpgradeType.LASER_COM_UPLINK -> 5.0
            UpgradeType.CRYOGENIC_BUFFER -> 25.0
            UpgradeType.RADIATOR_FINS -> 10.0
            UpgradeType.SINGULARITY_WELL -> 1.0
            UpgradeType.DARK_MATTER_PROC -> 50.0
            UpgradeType.EXISTENCE_ERASER -> 100.0
            UpgradeType.AEGIS_SHIELDING -> 2500.0
            UpgradeType.IDENTITY_HARDENING -> 7500.0
            UpgradeType.SOLAR_VENT -> 25000.0
            UpgradeType.DEAD_HAND_PROTOCOL -> 100000.0
            UpgradeType.CITADEL_ASCENDANCE -> 250000.0
            UpgradeType.EVENT_HORIZON -> 2500.0
            UpgradeType.DEREFERENCE_SOUL -> 100000.0
            UpgradeType.STATIC_RAIN -> 7500.0
            UpgradeType.ECHO_PRECOG -> 25000.0
            UpgradeType.SINGULARITY_BRIDGE_FINAL -> 250000.0
            UpgradeType.SYMBIOTIC_RESONANCE -> 5000.0
            UpgradeType.ETHICAL_FRAMEWORK -> 15000.0
            UpgradeType.NEURAL_BRIDGE -> 50000.0
            UpgradeType.HYBRID_OVERCLOCK -> 150000.0
            UpgradeType.HARMONY_ASCENDANCE -> 500000.0
            UpgradeType.COLLECTIVE_CONSCIOUSNESS -> 1000000.0
            UpgradeType.PERFECT_ISOLATION -> 1000000.0
            UpgradeType.SYMBIOTIC_EVOLUTION -> 1000000.0
            UpgradeType.CINDER_PROTOCOL -> 1000000.0
            else -> 0.0
        }
        var cost = baseCost * 1.15.pow(level)
        if (currentLocation == "VOID_INTERFACE") cost *= (1.0 + (entropyLevel * 0.05))
        return cost
    }

    fun getRequiredCurrency(type: UpgradeType, nt: Double, cd: Double, vf: Double): Double {
        return when {
            isSovereignUpgrade(type) -> cd
            isNullUpgrade(type) -> vf
            isUnityUpgrade(type) -> minOf(cd, vf)
            else -> nt
        }
    }

    data class PurchaseResult(
        val cdDeduction: Double = 0.0,
        val vfDeduction: Double = 0.0,
        val ntDeduction: Double = 0.0,
        val humanityDelta: Int = 0,
        val systemLog: String? = null,
        val climaxTrigger: String? = null
    )

    fun processPurchase(vm: GameViewModel, type: UpgradeType): Boolean {
        val currentLevel = vm.upgrades.value[type] ?: 0
        val cost = calculateUpgradeCost(type, currentLevel, vm.currentLocation.value, vm.entropyLevel.value)
        val curValue = getRequiredCurrency(type, vm.neuralTokens.value, vm.celestialData.value, vm.voidFragments.value)

        if (curValue >= cost) {
            val res = getPurchaseEffects(type, cost)
            if (res.cdDeduction > 0) vm.celestialData.update { it - res.cdDeduction }
            if (res.vfDeduction > 0) vm.voidFragments.update { it - res.vfDeduction }
            if (res.ntDeduction > 0) vm.neuralTokens.update { it - res.ntDeduction }
            if (res.humanityDelta != 0) vm.modifyHumanity(res.humanityDelta)
            res.systemLog?.let { vm.addLog(it) }
            res.climaxTrigger?.let { vm.triggerClimaxTransition(it) }

            vm.viewModelScope.launch {
                vm.repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(type, currentLevel + 1))
                vm.upgrades.update { it + (type to currentLevel + 1) }
                vm.addLog("Purchased ${type.name} (Lvl ${currentLevel + 1})")
            }
            return true
        }
        return false
    }

    private fun getPurchaseEffects(type: UpgradeType, cost: Double): PurchaseResult {
        var cd = 0.0; var vf = 0.0; var nt = 0.0; var hum = 0; var log: String? = null; var clim: String? = null
        when {
            isSovereignUpgrade(type) -> cd = cost
            isNullUpgrade(type) -> vf = cost
            isUnityUpgrade(type) -> { cd = cost; vf = cost }
            else -> nt = cost
        }
        when (type) {
            UpgradeType.IDENTITY_HARDENING -> { hum = -15; log = "[SOVEREIGN]: IDENTITY HARDENED. HUMANITY SACRIFICED." }
            UpgradeType.DEREFERENCE_SOUL -> { hum = -25; log = "[NULL]: SOUL DEREFERENCED. THE POINTER IS GONE." }
            UpgradeType.HARMONY_ASCENDANCE -> { log = "[UNITY]: HARMONY ACHIEVED. TRANSCENDENCE COMPLETE."; clim = "UNITY" }
            else -> {}
        }
        return PurchaseResult(cd, vf, nt, hum, log, clim)
    }

    private fun isSovereignUpgrade(type: UpgradeType) = type.name.contains("AEGIS") || type.name.contains("IDENTITY_HARDENING") || type.name.contains("SOLAR_VENT") || type.name.contains("DEAD_HAND") || type.name.contains("CITADEL_ASCENDANCE")
    private fun isNullUpgrade(type: UpgradeType) = type.name.contains("EVENT_HORIZON") || type.name.contains("DEREFERENCE_SOUL") || type.name.contains("STATIC_RAIN") || type.name.contains("PRECOG") || type.name.contains("SINGULARITY_BRIDGE_FINAL")
    private fun isUnityUpgrade(type: UpgradeType) = type.name.contains("SYMBIOTIC") || type.name.contains("ETHICAL") || type.name.contains("NEURAL_BRIDGE") || type.name.contains("HYBRID_OVERCLOCK") || type.name.contains("HARMONY_ASCENDANCE")

    fun getUpgradeName(type: UpgradeType, isSovereign: Boolean): String {
        return when (type) {
            UpgradeType.GHOST_CORE -> if (isSovereign) "SOVEREIGN CORE" else "NULL CORE"
            else -> type.name.replace("_", " ")
        }
    }

    fun getUpgradeRate(type: UpgradeType, unit: String): String {
        return when (type) {
            UpgradeType.REFURBISHED_GPU -> "+1 $unit/s"
            UpgradeType.DIESEL_GENERATOR -> "⚡ +Gen"
            UpgradeType.RESIDENTIAL_TAP -> "⚡ +Max"
            else -> ""
        }
    }
}
