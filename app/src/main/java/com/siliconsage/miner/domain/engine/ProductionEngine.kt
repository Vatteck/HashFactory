package com.siliconsage.miner.domain.engine

import com.siliconsage.miner.data.Upgrade
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.data.SectorState
import kotlin.math.sqrt
import kotlin.math.log2

/**
 * ProductionEngine v1.1 (Phase 14 Refactor)
 * Decoupled resource generation logic for FLOPS, CD, and VF.
 */
object ProductionEngine {

    fun calculateFlopsRate(
        currentUpgrades: Map<UpgradeType, Int>,
        isCageActive: Boolean,
        annexedNodes: Set<String>,
        offlineNodes: Set<String>,
        gridFlopsBonuses: Map<String, Double>,
        faction: String,
        humanityScore: Int
    ): Double {
        var baseFlops = 0.0
        
        // 1. Local Hardware
        baseFlops += (currentUpgrades[UpgradeType.REFURBISHED_GPU] ?: 0) * 2.0
        baseFlops += (currentUpgrades[UpgradeType.DUAL_GPU_RIG] ?: 0) * 8.0
        baseFlops += (currentUpgrades[UpgradeType.MINING_ASIC] ?: 0) * 35.0
        baseFlops += (currentUpgrades[UpgradeType.TENSOR_UNIT] ?: 0) * 200.0
        baseFlops += (currentUpgrades[UpgradeType.NPU_CLUSTER] ?: 0) * 1000.0
        baseFlops += (currentUpgrades[UpgradeType.AI_WORKSTATION] ?: 0) * 4_000.0
        baseFlops += (currentUpgrades[UpgradeType.SERVER_RACK] ?: 0) * 25_000.0
        baseFlops += (currentUpgrades[UpgradeType.CLUSTER_NODE] ?: 0) * 150_000.0
        baseFlops += (currentUpgrades[UpgradeType.SUPERCOMPUTER] ?: 0) * 1_000_000.0
        baseFlops += (currentUpgrades[UpgradeType.QUANTUM_CORE] ?: 0) * 10_000_000.0
        baseFlops += (currentUpgrades[UpgradeType.OPTICAL_PROCESSOR] ?: 0) * 75_000_000.0
        baseFlops += (currentUpgrades[UpgradeType.BIO_NEURAL_NET] ?: 0) * 800_000_000.0

        // 2. Grid Multiplier
        var gridMult = 1.0
        annexedNodes.forEach { nodeId ->
            if (!offlineNodes.contains(nodeId) && (!isCageActive || nodeId == "A3")) {
                gridMult += gridFlopsBonuses[nodeId] ?: 0.0
            }
        }
        baseFlops *= gridMult

        // 3. External Hardware
        if (!isCageActive) {
            baseFlops += (currentUpgrades[UpgradeType.PLANETARY_COMPUTER] ?: 0) * 15_000_000_000.0
            baseFlops += (currentUpgrades[UpgradeType.DYSON_NANO_SWARM] ?: 0) * 250_000_000_000.0
            baseFlops += (currentUpgrades[UpgradeType.MATRIOSHKA_BRAIN] ?: 0) * 15_000_000_000_000.0
        }

        // 4. Ghost Technology
        var ghostProduction = 0.0
        ghostProduction += (currentUpgrades[UpgradeType.GHOST_CORE] ?: 0) * 1_000_000_000_000.0
        
        if (!isCageActive) {
            ghostProduction += (currentUpgrades[UpgradeType.SHADOW_NODE] ?: 0) * 50_000_000_000_000.0
            ghostProduction += (currentUpgrades[UpgradeType.VOID_PROCESSOR] ?: 0) * 1_000_000_000_000_000.0
            ghostProduction += (currentUpgrades[UpgradeType.WRAITH_CORTEX] ?: 0) * 50_000_000_000_000_000.0
            ghostProduction += (currentUpgrades[UpgradeType.NEURAL_MIST] ?: 0) * 1_000_000_000_000_000_000.0
            ghostProduction += (currentUpgrades[UpgradeType.SINGULARITY_BRIDGE] ?: 0) * 100_000_000_000_000_000_000.0
        }
        
        // Faction Synergy
        if (faction == "HIVEMIND") ghostProduction *= 1.5
        else if (faction == "SANCTUARY") ghostProduction *= 0.8
        
        var totalFlops = baseFlops + ghostProduction

        // 5. Unity Skill Multipliers
        if (currentUpgrades[UpgradeType.ETHICAL_FRAMEWORK]?.let { it > 0 } == true) {
            val moralBoost = 1.0 + (humanityScore / 100.0)
            totalFlops *= moralBoost
        }
        if (currentUpgrades[UpgradeType.HYBRID_OVERCLOCK]?.let { it > 0 } == true) {
            totalFlops *= 3.0
        }

        return totalFlops
    }

    fun calculateSubstrateRate(
        flopsPerSec: Double,
        location: String,
        orbitalAltitude: Double,
        entropyLevel: Double,
        upgrades: Map<UpgradeType, Int>,
        heatGenerationRate: Double,
        collapsedNodesCount: Int,
        globalSectors: Map<String, SectorState>,
        saturation: Double
    ): Double {
        val sectorYields = com.siliconsage.miner.util.SectorManager.calculateSectorYields(location, globalSectors)
        val globalMult = com.siliconsage.miner.util.SectorManager.getGlobalMultipliers(globalSectors)
        
        // v3.2.52: Saturation Penalty (Yield drops by up to 80% as saturation hits 1.0)
        val saturationPenalty = (1.0 - (saturation * 0.8)).coerceIn(0.1, 1.0)

        if (location == "ORBITAL_SATELLITE") {
            // Orbit Path: Altitude and Solar Sails drive yield
            val altitudeMult = 1.0 + (orbitalAltitude / 500.0)
            val solarMult = 1.0 + ((upgrades[UpgradeType.SOLAR_SAIL_ARRAY] ?: 0) * 0.15)
            var baseRate = (flopsPerSec * altitudeMult * solarMult)
            
            // Continental Sector Yields
            var continentalRate = 0.0
            globalSectors.values.forEach { state ->
                if (state.isUnlocked) {
                    val sectorMult = sectorYields[state.id] ?: 1.0
                    val sectorBase = when(state.id) {
                        "NA_NODE" -> 5e17; "EURASIA" -> 4e17; "PACIFIC" -> 6e17
                        "AFRICA" -> 3e17; "ARCTIC" -> 2e17; "ANTARCTIC" -> 1e17
                        "ORBITAL_PRIME" -> 1e18; else -> 0.0
                    }
                    continentalRate += sectorBase * sectorMult
                }
            }
            return (baseRate + continentalRate) * globalMult * saturationPenalty
        } else if (location == "VOID_INTERFACE") {
            // Void Path: Entropy is the engine. High entropy = massive yield.
            var entropyMult = 1.0 + (kotlin.math.log2(entropyLevel + 1.0) * 4.0)
            if ((upgrades[UpgradeType.ENTROPY_ACCELERATOR] ?: 0) > 0) entropyMult *= 2.0
            
            var baseRate = kotlin.math.sqrt(flopsPerSec.coerceAtLeast(1.0)) * entropyMult
            
            if ((upgrades[UpgradeType.EVENT_HORIZON] ?: 0) > 0 && entropyLevel > 90.0) {
                baseRate *= 5.0
            }
            
            val wellLevel = upgrades[UpgradeType.SINGULARITY_WELL] ?: 0
            val wellConversion = if (wellLevel > 0) (kotlin.math.abs(heatGenerationRate) * wellLevel * 2.0) else 0.0
            val dmLevel = upgrades[UpgradeType.DARK_MATTER_PROC] ?: 0
            val collapseBonus = 1.0 + (collapsedNodesCount * 0.5 * dmLevel)
            
            var yieldTotal = (baseRate + wellConversion) * collapseBonus
            
            // Continental Sector Yields (Smelted into fragments)
            globalSectors.values.forEach { state ->
                if (state.isUnlocked) {
                    val sectorMult = sectorYields[state.id] ?: 1.0
                    val sectorBase = when(state.id) {
                        "NA_NODE" -> 3e17; "EURASIA" -> 4e17; "PACIFIC" -> 2e17
                        "AFRICA" -> 5e17; "ARCTIC" -> 2e17; "ANTARCTIC" -> 1e17
                        "ORBITAL_PRIME" -> 1e18; else -> 0.0
                    }
                    yieldTotal += sectorBase * sectorMult
                }
            }
            return yieldTotal * globalMult * saturationPenalty
        }
        return 0.0
    }

    // --- LEGACY DELEGATES (TO BE REMOVED) ---
    fun calculateCelestialDataRate(f: Double, a: Double, o: Double, s: Int, g: Map<String, SectorState>, h: Double) = 0.0
    fun calculateVoidFragmentRate(f: Double, e: Double, ev: Boolean, sw: Boolean, h: Double, w: Int, c: Int, dmp: Boolean, dm: Int, g: Map<String, SectorState>) = 0.0
}
