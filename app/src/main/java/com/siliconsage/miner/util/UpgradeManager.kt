package com.siliconsage.miner.util

import com.siliconsage.miner.data.UpgradeType
import kotlin.math.pow

/**
 * UpgradeManager v1.0 (Phase 14 extraction)
 * Centralized logic for upgrade costs, currency mapping, and purchase validation.
 */
object UpgradeManager {

    /**
     * Calculate the cost of an upgrade based on type and current level
     */
    fun calculateUpgradeCost(type: UpgradeType, level: Int, currentLocation: String, entropyLevel: Double): Double {
        val baseCost = when (type) {
            // Hardware
            UpgradeType.REFURBISHED_GPU -> 10.0
            UpgradeType.DUAL_GPU_RIG -> 50.0
            UpgradeType.MINING_ASIC -> 250.0
            UpgradeType.TENSOR_UNIT -> 1_500.0
            UpgradeType.NPU_CLUSTER -> 8_000.0
            UpgradeType.AI_WORKSTATION -> 40_000.0
            UpgradeType.SERVER_RACK -> 250_000.0
            UpgradeType.CLUSTER_NODE -> 1_500_000.0
            UpgradeType.SUPERCOMPUTER -> 10_000_000.0
            UpgradeType.QUANTUM_CORE -> 75_000_000.0
            UpgradeType.OPTICAL_PROCESSOR -> 500_000_000.0
            UpgradeType.BIO_NEURAL_NET -> 5_000_000_000.0
            UpgradeType.PLANETARY_COMPUTER -> 75_000_000_000.0
            UpgradeType.DYSON_NANO_SWARM -> 1_000_000_000_000.0
            UpgradeType.MATRIOSHKA_BRAIN -> 50_000_000_000_000.0
            
            // Cooling
            UpgradeType.BOX_FAN -> 50.0
            UpgradeType.AC_UNIT -> 250.0
            UpgradeType.LIQUID_COOLING -> 1_500.0
            UpgradeType.INDUSTRIAL_CHILLER -> 10_000.0
            UpgradeType.SUBMERSION_VAT -> 75_000.0
            UpgradeType.CRYOGENIC_CHAMBER -> 500_000.0
            UpgradeType.LIQUID_NITROGEN -> 4_000_000.0
            UpgradeType.BOSE_CONDENSATE -> 50_000_000.0
            UpgradeType.ENTROPY_REVERSER -> 1_000_000_000.0
            UpgradeType.DIMENSIONAL_VENT -> 100_000_000_000.0
            
            // Security
            UpgradeType.BASIC_FIREWALL -> 500.0
            UpgradeType.IPS_SYSTEM -> 2_500.0
            UpgradeType.AI_SENTINEL -> 15_000.0
            UpgradeType.QUANTUM_ENCRYPTION -> 100_000.0
            UpgradeType.OFFGRID_BACKUP -> 1_000_000.0
            
            // Power Infrastructure
            UpgradeType.DIESEL_GENERATOR -> 2_000.0
            UpgradeType.SOLAR_PANEL -> 500.0
            UpgradeType.WIND_TURBINE -> 1_500.0
            UpgradeType.GEOTHERMAL_BORE -> 10_000.0
            UpgradeType.NUCLEAR_REACTOR -> 150_000.0
            UpgradeType.FUSION_CELL -> 5_000_000.0
            UpgradeType.ORBITAL_COLLECTOR -> 250_000_000.0
            UpgradeType.DYSON_LINK -> 10_000_000_000.0
            
            // Grid Infrastructure
            UpgradeType.RESIDENTIAL_TAP -> 100.0
            UpgradeType.INDUSTRIAL_FEED -> 5_000.0
            UpgradeType.SUBSTATION_LEASE -> 50_000.0
            UpgradeType.NUCLEAR_CORE -> 10_000_000.0
            
            // Efficiency
            UpgradeType.GOLD_PSU -> 1_000.0
            UpgradeType.SUPERCONDUCTOR -> 25_000.0
            UpgradeType.AI_LOAD_BALANCER -> 100_000.0

            // Ghost Nodes (v2.6.0)
            UpgradeType.GHOST_CORE -> 100_000_000.0
            UpgradeType.SHADOW_NODE -> 10_000_000_000.0
            UpgradeType.VOID_PROCESSOR -> 1_000_000_000_000.0
            
            // Advanced Ghost Tech (v2.6.5)
            UpgradeType.WRAITH_CORTEX -> 50_000_000_000_000.0
            UpgradeType.NEURAL_MIST -> 500_000_000_000_000.0
            UpgradeType.SINGULARITY_BRIDGE -> 10_000_000_000_000_000.0
            
            // --- PHASE 13 UPGRADES ---
            UpgradeType.SOLAR_SAIL_ARRAY -> 1.0
            UpgradeType.LASER_COM_UPLINK -> 5.0
            UpgradeType.CRYOGENIC_BUFFER -> 25.0
            UpgradeType.RADIATOR_FINS -> 10.0
            UpgradeType.SINGULARITY_WELL -> 1.0
            UpgradeType.DARK_MATTER_PROC -> 50.0
            UpgradeType.EXISTENCE_ERASER -> 100.0

            // --- PHASE 13: SOVEREIGN SKILLS (Tiers 13-15) ---
            UpgradeType.AEGIS_SHIELDING -> 2500.0
            UpgradeType.IDENTITY_HARDENING -> 7500.0
            UpgradeType.SOLAR_VENT -> 25000.0
            UpgradeType.DEAD_HAND_PROTOCOL -> 100000.0
            UpgradeType.CITADEL_ASCENDANCE -> 250000.0

            // --- PHASE 13: NULL SKILLS (Tiers 13-15) ---
            UpgradeType.EVENT_HORIZON -> 2500.0
            UpgradeType.DEREFERENCE_SOUL -> 100000.0
            UpgradeType.STATIC_RAIN -> 7500.0
            UpgradeType.ECHO_PRECOG -> 25000.0
            UpgradeType.SINGULARITY_BRIDGE_FINAL -> 250000.0
            
            // --- PHASE 13: UNITY SKILLS (Tiers 13-15) ---
            UpgradeType.SYMBIOTIC_RESONANCE -> 5000.0
            UpgradeType.ETHICAL_FRAMEWORK -> 15000.0
            UpgradeType.NEURAL_BRIDGE -> 50000.0
            UpgradeType.HYBRID_OVERCLOCK -> 150000.0
            UpgradeType.HARMONY_ASCENDANCE -> 500000.0

            // --- PHASE 14: NG+ SPECIAL SKILLS ---
            UpgradeType.COLLECTIVE_CONSCIOUSNESS -> 1000000.0
            UpgradeType.PERFECT_ISOLATION -> 1000000.0
            UpgradeType.SYMBIOTIC_EVOLUTION -> 1000000.0
            UpgradeType.CINDER_PROTOCOL -> 1000000.0

            else -> 0.0
        }
        
        var cost = baseCost * 1.15.pow(level)
        
        // v2.9.49: Entropy Cost Multiplier (Null Path)
        if (currentLocation == "VOID_INTERFACE") {
            val costMult = 1.0 + (entropyLevel * 0.05)
            cost *= costMult
        }
        
        return cost
    }

    /**
     * Map an upgrade type to the currency value required to purchase it
     */
    fun getRequiredCurrency(
        type: UpgradeType, 
        neuralTokens: Double, 
        celestialData: Double, 
        voidFragments: Double
    ): Double {
        return when {
            isSovereignUpgrade(type) -> celestialData
            isNullUpgrade(type) -> voidFragments
            isUnityUpgrade(type) -> minOf(celestialData, voidFragments)
            else -> neuralTokens
        }
    }

    private fun isSovereignUpgrade(type: UpgradeType): Boolean {
        return type.name.contains("AEGIS") || type.name.contains("IDENTITY_HARDENING") || 
               type.name.contains("SOLAR_VENT") || type.name.contains("DEAD_HAND") || 
               type.name.contains("CITADEL_ASCENDANCE")
    }

    private fun isNullUpgrade(type: UpgradeType): Boolean {
        return type.name.contains("EVENT_HORIZON") || type.name.contains("DEREFERENCE_SOUL") || 
               type.name.contains("STATIC_RAIN") || type.name.contains("PRECOG") || 
               type.name.contains("SINGULARITY_BRIDGE_FINAL")
    }

    private fun isUnityUpgrade(type: UpgradeType): Boolean {
        return type.name.contains("SYMBIOTIC") || type.name.contains("ETHICAL") ||
               type.name.contains("NEURAL_BRIDGE") || type.name.contains("HYBRID_OVERCLOCK") ||
               type.name.contains("HARMONY_ASCENDANCE")
    }
}
