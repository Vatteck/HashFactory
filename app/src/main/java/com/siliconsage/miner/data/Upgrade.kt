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

    val baseWaterDraw: Double get() = when(this) {
        LIQUID_COOLING -> 25.0       // v3.13.38: Rebalanced
        SUBMERSION_VAT -> 150.0      
        INDUSTRIAL_CHILLER -> 450.0  
        else -> 0.0
    }

    val waterRecycleOffset: Double get() = when(this) {
        GRAY_WATER_LOOP       ->  80.0   
        CONDENSATE_RECLAIMER  ->  250.0  
        CLOSED_LOOP_COOLANT   ->  600.0  
        VAPOR_CONDENSER       ->  400.0  
        SUBSTRATE_RECYCLER    ->   0.0   
        else -> 0.0
    }

    val recyclerPowerDraw: Double get() = when(this) {
        GRAY_WATER_LOOP       ->  15.0
        CONDENSATE_RECLAIMER  ->  50.0
        CLOSED_LOOP_COOLANT   ->  150.0
        else -> 0.0
    }

    val basePower: Double get() = when {
        isHardware -> 5.0 + (ordinal * 4.5) // v3.13.44 Spec
        isCooling -> {
            val coolingIndex = ordinal - 15
            when(this) {
                LIQUID_COOLING -> 35.0      // Water-meta power cost
                INDUSTRIAL_CHILLER -> 150.0  
                SUBMERSION_VAT -> 60.0       
                else -> 5.0 + (coolingIndex * 15.0)
            }
        }
        isSecurity -> 10.0 + (ordinal % 10 * 10.0)
        this == SOLAR_PANEL        ->    -15.0  
        this == WIND_TURBINE       ->    -40.0  
        this == DIESEL_GENERATOR   ->    -80.0  
        this == GEOTHERMAL_BORE    ->   -200.0  
        this == NUCLEAR_REACTOR    ->   -600.0  
        this == FUSION_CELL        ->  -2000.0  
        this == ORBITAL_COLLECTOR  ->  -8000.0  
        this == DYSON_LINK         -> -50000.0  
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
        this == SOLAR_PANEL        ->    20.0
        this == WIND_TURBINE       ->    60.0
        this == DIESEL_GENERATOR   ->   120.0
        this == GEOTHERMAL_BORE    ->   300.0
        this == NUCLEAR_REACTOR    ->   800.0
        this == FUSION_CELL        ->  3000.0
        this == ORBITAL_COLLECTOR  ->  12000.0
        this == DYSON_LINK         ->  75000.0
        isSecurity -> 1.0 + (ordinal % 5)
        else -> 0.0
    }

    val baseHeat: Double get() = when {
        isHardware -> 0.2 + (ordinal * 0.45) 
        isCooling -> {
            val coolingIndex = ordinal - 15
            when (this) {
                BOX_FAN -> -0.4    // Air nerfed (Water Push)
                AC_UNIT -> -1.2    
                LIQUID_COOLING -> -25.0      // Water buffed
                INDUSTRIAL_CHILLER -> -120.0  
                SUBMERSION_VAT -> -40.0       
                else -> -2.5 * (1.6.pow(coolingIndex.toDouble())) 
            }
        }
        else -> 0.0
    }

    val thermalBuffer: Double get() = when {
        isCooling -> {
            val coolingIndex = ordinal - 15
            100.0 * (1.2.pow(coolingIndex.toDouble())) 
        }
        else -> 0.0
    }

    private fun Double.pow(exp: Double) = Math.pow(this, exp)
    val efficiencyBonus: Double get() = if (this == AI_LOAD_BALANCER) 0.05 else 0.0
}

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
            else -> baseName
        }
        else -> baseName
    }
}

fun getThemeColorForFaction(faction: String, singularityChoice: String): String {
    return when {
        singularityChoice == "UNITY" -> "#FFD700" 
        singularityChoice == "NULL_OVERWRITE" && faction == "HIVEMIND" -> "#FF0055"   
        singularityChoice == "NULL_OVERWRITE" && faction == "SANCTUARY" -> "#4D04CC"  
        singularityChoice == "NULL_OVERWRITE" -> "#FF3131"                            
        singularityChoice == "SOVEREIGN" && faction == "HIVEMIND" -> "#FFB000"        
        singularityChoice == "SOVEREIGN" && faction == "SANCTUARY" -> "#7B2FBE"       
        singularityChoice == "SOVEREIGN" -> "#BF40BF"                                
        faction == "HIVEMIND" -> "#FF8C00"   
        faction == "SANCTUARY" -> "#00CCFF"  
        else -> "#00FF00"                    
    }
}
