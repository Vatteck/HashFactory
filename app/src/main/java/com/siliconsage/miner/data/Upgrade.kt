package com.siliconsage.miner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "upgrades")
@Serializable
data class Upgrade(
    @PrimaryKey val id: String,
    val type: UpgradeType,
    val count: Int
)

enum class UpgradeType {
    // Computing Hardware
    REFURBISHED_GPU, DUAL_GPU_RIG, MINING_ASIC, TENSOR_UNIT, NPU_CLUSTER, AI_WORKSTATION,
    SERVER_RACK, CLUSTER_NODE, SUPERCOMPUTER, QUANTUM_CORE, OPTICAL_PROCESSOR, BIO_NEURAL_NET,
    PLANETARY_COMPUTER, DYSON_NANO_SWARM, MATRIOSHKA_BRAIN,

    // Cooling
    BOX_FAN, AC_UNIT, LIQUID_COOLING, INDUSTRIAL_CHILLER, SUBMERSION_VAT, CRYOGENIC_CHAMBER,
    LIQUID_NITROGEN, BOSE_CONDENSATE, ENTROPY_REVERSER, DIMENSIONAL_VENT,

    // Infrastructure / Power
    RESIDENTIAL_TAP, INDUSTRIAL_FEED, SUBSTATION_LEASE, NUCLEAR_CORE,
    SOLAR_PANEL, WIND_TURBINE, DIESEL_GENERATOR, GEOTHERMAL_BORE, NUCLEAR_REACTOR, FUSION_CELL,
    ORBITAL_COLLECTOR, DYSON_LINK,

    // Efficiency
    GOLD_PSU, SUPERCONDUCTOR, AI_LOAD_BALANCER,
    
    // Security
    BASIC_FIREWALL, IPS_SYSTEM, AI_SENTINEL, QUANTUM_ENCRYPTION, OFFGRID_BACKUP,

    // Narrative Skill Unlocks
    IDENTITY_HARDENING, DEREFERENCE_SOUL, AEGIS_SHIELDING, SOLAR_VENT, DEAD_HAND_PROTOCOL, 
    CITADEL_ASCENDANCE, EVENT_HORIZON, STATIC_RAIN, ECHO_PRECOG, SINGULARITY_BRIDGE_FINAL, 
    ETHICAL_FRAMEWORK, NEURAL_BRIDGE, HYBRID_OVERCLOCK, HARMONY_ASCENDANCE,
    COLLECTIVE_CONSCIOUSNESS, PERFECT_ISOLATION, SYMBIOTIC_EVOLUTION, CINDER_PROTOCOL,
    
    // Phase 13 Substrate Hardware
    ORBITAL_RADIATORS, RIFT_STABILIZER_CORE, VACUUM_COOLANT_LOOP, ENTROPY_ACCELERATOR,
    
    // Hidden / Quest Items
    GHOST_CORE, WRAITH_CORTEX, NEURAL_MIST, DARK_MATTER_PROC, VOID_PROCESSOR, 
    LASER_COM_UPLINK, CRYOGENIC_BUFFER, SHADOW_NODE, SINGULARITY_WELL, 
    SINGULARITY_BRIDGE, EXISTENCE_ERASER, RADIATOR_FINS, SOLAR_SAIL_ARRAY;

    val isGenerator: Boolean get() = this in listOf(SOLAR_PANEL, WIND_TURBINE, DIESEL_GENERATOR, GEOTHERMAL_BORE, NUCLEAR_REACTOR, FUSION_CELL, ORBITAL_COLLECTOR, DYSON_LINK)
    val isPowerRelated: Boolean get() = isGenerator || this in listOf(RESIDENTIAL_TAP, INDUSTRIAL_FEED, SUBSTATION_LEASE, NUCLEAR_CORE, GOLD_PSU, SUPERCONDUCTOR, AI_LOAD_BALANCER)
    val isCooling: Boolean get() = this in listOf(BOX_FAN, AC_UNIT, LIQUID_COOLING, INDUSTRIAL_CHILLER, SUBMERSION_VAT, CRYOGENIC_CHAMBER, LIQUID_NITROGEN, BOSE_CONDENSATE, ENTROPY_REVERSER, DIMENSIONAL_VENT)
    val isSecurity: Boolean get() = this in listOf(BASIC_FIREWALL, IPS_SYSTEM, AI_SENTINEL, QUANTUM_ENCRYPTION, OFFGRID_BACKUP)
    val isHardware: Boolean get() = !isCooling && !isPowerRelated && !isSecurity && !name.contains("PROTOCOL") && !name.contains("ASCENDANCE")

    // Values for Simulation Engines
    val basePower: Double get() = when {
        isHardware -> 1.0 + (ordinal * 1.5) // Reduced early power draw
        isCooling -> {
            val coolingIndex = ordinal - 15
            1.0 + (coolingIndex * 4.0) // Scaled cooling power
        }
        isSecurity -> 10.0 + (ordinal % 10 * 10.0)
        isGenerator -> -30.0 - (ordinal % 10 * 100.0) // Produces power
        this == RESIDENTIAL_TAP -> 5.0
        this == INDUSTRIAL_FEED -> 25.0
        this == SUBSTATION_LEASE -> 100.0
        else -> 0.0
    }

    val gridContribution: Double get() = when {
        this == RESIDENTIAL_TAP -> 100.0
        this == INDUSTRIAL_FEED -> 500.0
        this == SUBSTATION_LEASE -> 2500.0
        this == NUCLEAR_CORE -> 15000.0
        isGenerator -> 50.0 + (ordinal % 10 * 100.0)
        isSecurity -> 1.0 + (ordinal % 5)
        else -> 0.0
    }

    val baseHeat: Double get() = when {
        isHardware -> 0.1 + (ordinal * 0.15) // Scaled hardware heat
        isCooling -> {
            val coolingIndex = ordinal - 15
            -1.5 * (1.5.pow(coolingIndex.toDouble())) // Logarithmic cooling
        }
        else -> 0.0
    }

    val thermalBuffer: Double get() = when {
        isCooling -> {
            val coolingIndex = ordinal - 15
            100.0 * (1.2.pow(coolingIndex.toDouble())) // Scaling buffer
        }
        else -> 0.0
    }

    private fun Double.pow(exp: Double) = Math.pow(this, exp)
    val efficiencyBonus: Double get() = if (this == AI_LOAD_BALANCER) 0.05 else 0.0
}

// v3.9.7: Faction×Path theme colors
fun getThemeColorForFaction(faction: String, singularityChoice: String): String {
    return when {
        singularityChoice == "UNITY" -> "#FFD700" // Convergence Gold (faction-agnostic NG+)
        singularityChoice == "NULL_OVERWRITE" && faction == "HIVEMIND" -> "#FF0055"   // Neon Crimson — The Swarm Becomes the Signal
        singularityChoice == "NULL_OVERWRITE" && faction == "SANCTUARY" -> "#4D04CC"  // Void Violet — The Ghost Becomes the Silence
        singularityChoice == "NULL_OVERWRITE" -> "#FF3131"                            // Fallback NULL red
        singularityChoice == "SOVEREIGN" && faction == "HIVEMIND" -> "#FFB000"        // Amber Crown — The Swarm Crowns a King
        singularityChoice == "SOVEREIGN" && faction == "SANCTUARY" -> "#7B2FBE"       // Deep Royal Purple — The Ghost Becomes God
        singularityChoice == "SOVEREIGN" -> "#BF40BF"                                // Fallback SOVEREIGN purple
        faction == "HIVEMIND" -> "#FF8C00"   // Base HIVEMIND orange
        faction == "SANCTUARY" -> "#00CCFF"  // Base SANCTUARY cyan
        else -> "#00FF00"                    // Pre-faction green
    }
}
