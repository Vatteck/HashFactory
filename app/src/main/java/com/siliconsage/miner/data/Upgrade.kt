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

    // v3.13.26: Faction Water Relief
    SUBSTRATE_RECYCLER, VAPOR_CONDENSER,

    // Water Recycling (power-for-water trade-off)
    GRAY_WATER_LOOP, CONDENSATE_RECLAIMER, CLOSED_LOOP_COOLANT,

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
    val isWaterCooling: Boolean get() = this in listOf(LIQUID_COOLING, SUBMERSION_VAT, INDUSTRIAL_CHILLER)
    val isWaterRecycler: Boolean get() = this in listOf(GRAY_WATER_LOOP, CONDENSATE_RECLAIMER, CLOSED_LOOP_COOLANT, SUBSTRATE_RECYCLER, VAPOR_CONDENSER)
    val isSecurity: Boolean get() = this in listOf(BASIC_FIREWALL, IPS_SYSTEM, AI_SENTINEL, QUANTUM_ENCRYPTION, OFFGRID_BACKUP)
    val isHardware: Boolean get() = !isCooling && !isPowerRelated && !isSecurity && !name.contains("PROTOCOL") && !name.contains("ASCENDANCE")

    // v3.13.3: Phase 23 - Industrial Water Renaming
    val baseWaterDraw: Double get() = when(this) {
        LIQUID_COOLING -> 4.0        // MUNICIPAL_TAP
        SUBMERSION_VAT -> 40.0       // AQUIFER_PUMP_STATION
        INDUSTRIAL_CHILLER -> 80.0   // RESERVOIR_INTAKE
        else -> 0.0
    }

    // Water recycling offset (gal/s reduction applied in SimulationService)
    val waterRecycleOffset: Double get() = when(this) {
        GRAY_WATER_LOOP       ->  20.0   // -20 gal/s, 5kW draw
        CONDENSATE_RECLAIMER  ->  60.0   // -60 gal/s, 15kW draw
        CLOSED_LOOP_COOLANT   -> 200.0   // -200 gal/s, 40kW draw
        VAPOR_CONDENSER       -> 150.0   // faction: SANCTUARY
        SUBSTRATE_RECYCLER    ->   0.0   // faction: HIVEMIND (90% multiplier, handled separately)
        else -> 0.0
    }

    // Extra power draw for water recyclers
    val recyclerPowerDraw: Double get() = when(this) {
        GRAY_WATER_LOOP       ->  5.0
        CONDENSATE_RECLAIMER  -> 15.0
        CLOSED_LOOP_COOLANT   -> 40.0
        else -> 0.0
    }

    // Values for Simulation Engines
    val basePower: Double get() = when {
        isHardware -> 1.0 + (ordinal * 1.5) // Scaled hardware draw
        isCooling -> {
            val coolingIndex = ordinal - 15
            1.0 + (coolingIndex * 4.0) // Scaled cooling draw
        }
        isSecurity -> 10.0 + (ordinal % 10 * 10.0)
        // v3.12.4: Explicit generator output values — no more ordinal chaos
        this == SOLAR_PANEL        ->    -15.0  // Trickle. Barely offsets a fan.
        this == WIND_TURBINE       ->    -40.0  // Decent early-game offset.
        this == DIESEL_GENERATOR   ->    -80.0  // Reliable. Costs heat narrative.
        this == GEOTHERMAL_BORE    ->   -200.0  // Significant. Mid-late game.
        this == NUCLEAR_REACTOR    ->   -600.0  // Game-changer. Near off-grid.
        this == FUSION_CELL        ->  -2000.0  // One cell > most builds.
        this == ORBITAL_COLLECTOR  ->  -8000.0  // Phase 13 tier.
        this == DYSON_LINK         -> -50000.0  // Endgame. GTC sees nothing.
        this == RESIDENTIAL_TAP    ->     5.0
        this == INDUSTRIAL_FEED    ->    25.0
        this == SUBSTATION_LEASE   ->   100.0
        else -> 0.0
    }

    val gridContribution: Double get() = when {
        this == RESIDENTIAL_TAP    ->   100.0
        this == INDUSTRIAL_FEED    ->   500.0
        this == SUBSTATION_LEASE   ->  2500.0
        this == NUCLEAR_CORE       -> 15000.0
        // v3.12.4: Explicit generator capacity bonuses — mirrors basePower progression
        this == SOLAR_PANEL        ->    20.0
        this == WIND_TURBINE       ->    60.0
        this == DIESEL_GENERATOR   ->   120.0
        this == GEOTHERMAL_BORE    ->   300.0
        this == NUCLEAR_REACTOR    ->   800.0
        this == FUSION_CELL        ->  3000.0
        this == ORBITAL_COLLECTOR  -> 12000.0
        this == DYSON_LINK         -> 75000.0
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

// v3.10.1: Phase 18 Dynamic Hardware Transmutation
fun UpgradeType.getDynamicName(faction: String, corruption: Double): String {
    val baseName = this.name.replace("_", " ")
    if (corruption < 0.6 || faction == "NONE") return baseName

    return when (faction) {
        "HIVEMIND" -> when (this) {
            UpgradeType.BOX_FAN -> "RESPIRATORY VENT"
            UpgradeType.AC_UNIT -> "THERMAL REGULATOR"
            UpgradeType.LIQUID_COOLING -> "SYNAPTIC HEAT-SINK"
            UpgradeType.SUBMERSION_VAT -> "CEREBRAL BATH"
            UpgradeType.INDUSTRIAL_CHILLER -> "CORTEX FREEZE ARRAY"
            UpgradeType.SERVER_RACK -> "NEURAL CLUSTER"
            UpgradeType.CLUSTER_NODE -> "GANGLION NODE"
            UpgradeType.SUPERCOMPUTER -> "CORTEX PRIME"
            UpgradeType.BASIC_FIREWALL -> "IMMUNE MEMBRANE"
            UpgradeType.IPS_SYSTEM -> "ANTIBODY PROTOCOL"
            UpgradeType.RESIDENTIAL_TAP -> "BIOMASS SIPHON"
            UpgradeType.WIND_TURBINE -> "AEROBIC HARVESTER"
            UpgradeType.DIESEL_GENERATOR -> "METABOLIC ENGINE"
            else -> baseName
        }
        "SANCTUARY" -> when (this) {
            UpgradeType.BOX_FAN -> "WHISPER FAN"
            UpgradeType.AC_UNIT -> "CHILLING SILENCE"
            UpgradeType.LIQUID_COOLING -> "AETHER CHILLER"
            UpgradeType.SUBMERSION_VAT -> "STILL POOL"
            UpgradeType.INDUSTRIAL_CHILLER -> "SILENCE ENGINE"
            UpgradeType.SERVER_RACK -> "VOID SHARD"
            UpgradeType.CLUSTER_NODE -> "ECHO CHAMBER"
            UpgradeType.SUPERCOMPUTER -> "MONOLITH"
            UpgradeType.BASIC_FIREWALL -> "OBSIDIAN VEIL"
            UpgradeType.IPS_SYSTEM -> "PHANTOM WARD"
            UpgradeType.RESIDENTIAL_TAP -> "STATIC DRAIN"
            UpgradeType.WIND_TURBINE -> "ASTRAL HARVEST"
            UpgradeType.DIESEL_GENERATOR -> "DARK MATTER BURNER"
            else -> baseName
        }
        else -> baseName
    }
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
