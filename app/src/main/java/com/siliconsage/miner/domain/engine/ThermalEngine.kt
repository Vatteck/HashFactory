package com.siliconsage.miner.domain.engine

import com.siliconsage.miner.data.Upgrade
import com.siliconsage.miner.data.UpgradeType
import kotlin.math.abs

/**
 * ThermalEngine v1.0 (Phase 14 Refactor)
 * Decoupled thermal physics engine for high-frontier and void environments.
 */
object ThermalEngine {

    data class ThermalResults(
        val netChangeUnits: Double,
        val totalThermalBuffer: Double,
        val percentChange: Double
    )

    fun calculateThermalMetrics(
        currentUpgrades: List<Upgrade>,
        location: String,
        isOverclocked: Boolean,
        isPurging: Boolean,
        isCageActive: Boolean,
        unlockedPerks: Set<String>,
        unlockedTechNodes: Set<String>,
        playerRank: Int,
        storyStage: Int
    ): ThermalResults {
        // 1. Calculate Thermal Buffer
        var totalThermalBuffer = 100.0
        currentUpgrades.forEach { upgrade ->
            if (upgrade.count > 0) {
                totalThermalBuffer += upgrade.type.thermalBuffer * upgrade.count
            }
        }

        // 2. Base Heat Generation & Cooling
        var netChangeUnits = 0.0
        val isVacuum = location == "ORBITAL_SATELLITE"
        val isVoid = location == "VOID_INTERFACE"

        currentUpgrades.forEach { upgrade ->
            if (upgrade.count > 0) {
                val isExternal = isExternalComponent(upgrade.type)
                val isConvectionCooling = isConvectionCooling(upgrade.type)

                if (!(isCageActive && isExternal)) {
                    if ((isVacuum || isVoid) && isConvectionCooling) {
                        // High-Frontier Penalty: Fans don't work in a vacuum or void.
                        // They actually generate minor heat from friction without cooling.
                        netChangeUnits += 0.5 * upgrade.count
                    } else {
                        var heat = upgrade.type.baseHeat
                        
                        // Radiator Bonus in Space
                        if (isVacuum && (upgrade.type == UpgradeType.RADIATOR_FINS || upgrade.type == UpgradeType.ORBITAL_RADIATORS)) {
                            heat *= if (upgrade.type == UpgradeType.ORBITAL_RADIATORS) 5.0 else 2.5
                        }
                        
                        // Singularity Well in Void
                        if (isVoid && upgrade.type == UpgradeType.SINGULARITY_WELL) {
                            heat *= 1.5 // Wells consume heat to make VF
                        }

                        if (isOverclocked && heat > 0) heat *= 2.0
                        netChangeUnits += heat * upgrade.count
                    }
                }
            }
        }

        // 3. Base Dissipation (Disabled in Vacuum/Void)
        if (!isVacuum && !isVoid) {
            netChangeUnits -= 1.0
        }

        // 4. Emergency Heat Purge
        if (isPurging) {
            if (isVoid) {
                // Void Venting: Heat is gone, but it becomes Entropy
                netChangeUnits -= (totalThermalBuffer * 0.15)
            } else if (location == "ORBITAL_SATELLITE") {
                // Orbital Purge: Gas/coolant ejection
                netChangeUnits -= (totalThermalBuffer * 0.10)
            } else {
                // Standard/Terrestrial Purge (Fans high/Air intake)
                netChangeUnits -= (totalThermalBuffer * 0.05)
            }
        }

        // 5. Protection & Caps
        if (isCageActive && netChangeUnits > 10.0) netChangeUnits = 10.0
        if (unlockedPerks.contains("thermal_void") && netChangeUnits > 0) netChangeUnits *= 0.8
        if (unlockedTechNodes.contains("perfect_isolation") && netChangeUnits > 0) netChangeUnits = 0.0

        // 5. Rank-based stability
        if (netChangeUnits > 0) {
            if (storyStage == 0) netChangeUnits *= 0.60
            val rankMult = (1.0 - (playerRank * 0.05)).coerceAtLeast(0.1)
            netChangeUnits *= rankMult
        }

        val percentChange = (netChangeUnits / totalThermalBuffer) * 100.0
        return ThermalResults(netChangeUnits, totalThermalBuffer, percentChange)
    }

    private fun isExternalComponent(type: UpgradeType): Boolean {
        return type == UpgradeType.PLANETARY_COMPUTER || 
               type == UpgradeType.DYSON_NANO_SWARM || 
               type == UpgradeType.MATRIOSHKA_BRAIN ||
               type == UpgradeType.SHADOW_NODE ||
               type == UpgradeType.VOID_PROCESSOR ||
               type == UpgradeType.WRAITH_CORTEX ||
               type == UpgradeType.NEURAL_MIST ||
               type == UpgradeType.SINGULARITY_BRIDGE ||
               type == UpgradeType.ENTROPY_REVERSER ||
               type == UpgradeType.DIMENSIONAL_VENT
    }

    private fun isConvectionCooling(type: UpgradeType): Boolean {
        return type == UpgradeType.BOX_FAN || 
               type == UpgradeType.AC_UNIT || 
               type == UpgradeType.INDUSTRIAL_CHILLER
    }
}
