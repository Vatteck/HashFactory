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
        baseFlops += (currentUpgrades[UpgradeType.REFURBISHED_GPU] ?: 0) * 1.0
        baseFlops += (currentUpgrades[UpgradeType.DUAL_GPU_RIG] ?: 0) * 5.0
        baseFlops += (currentUpgrades[UpgradeType.MINING_ASIC] ?: 0) * 25.0
        baseFlops += (currentUpgrades[UpgradeType.TENSOR_UNIT] ?: 0) * 150.0
        baseFlops += (currentUpgrades[UpgradeType.NPU_CLUSTER] ?: 0) * 800.0
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

    fun calculateCelestialDataRate(
        flopsPerSec: Double,
        activePowerUsage: Double,
        orbitalAltitude: Double,
        solarSailLevel: Int,
        globalSectors: Map<String, SectorState>,
        resonanceBonus: Double,
        heatGenerationRate: Double,
        hasSymbioticResonance: Boolean
    ): Double {
        val altitudeMult = 1.0 + (orbitalAltitude / 500.0)
        val solarMult = 1.0 + (solarSailLevel * 0.15)
        
        var cdRate = (flopsPerSec * altitudeMult * solarMult) * resonanceBonus
        
        // Global Sectors
        globalSectors.values.forEach { state ->
            if (state.isUnlocked) {
                cdRate += when(state.id) {
                    "NA_NODE" -> 5e17; "EURASIA" -> 4e17; "PACIFIC" -> 6e17
                    "AFRICA" -> 3e17; "ARCTIC" -> 2e17; "ANTARCTIC" -> 1e17
                    "ORBITAL_PRIME" -> 1e18; else -> 0.0
                }
            }
        }

        if (hasSymbioticResonance) {
            cdRate += (heatGenerationRate.coerceAtLeast(0.0) * 1000.0)
        }

        return cdRate
    }

    fun calculateVoidFragmentRate(
        flopsPerSec: Double,
        entropyLevel: Double,
        hasEventHorizon: Boolean,
        hasSingularityWell: Boolean,
        heatGenerationRate: Double,
        wellLevel: Int,
        collapsedNodesCount: Int,
        hasDarkMatterProc: Boolean,
        dmLevel: Int,
        globalSectors: Map<String, SectorState>,
        resonanceBonus: Double
    ): Double {
        val entropyMult = 1.0 + (log2(entropyLevel + 1.0) * 2.0)
        var baseVfRate = sqrt(flopsPerSec.coerceAtLeast(1.0)) * entropyMult
        
        if (hasEventHorizon && entropyLevel > 90.0) baseVfRate *= 5.0
        
        val wellConversion = if (hasSingularityWell) (heatGenerationRate.coerceAtLeast(0.0) * wellLevel * 0.1) else 0.0
        val collapseBonus = 1.0 + (collapsedNodesCount * 0.2 * dmLevel)
        
        var vfRate = (baseVfRate + wellConversion) * collapseBonus * resonanceBonus
        
        globalSectors.values.forEach { state ->
            if (state.isUnlocked) {
                vfRate += when(state.id) {
                    "NA_NODE" -> 3e17; "EURASIA" -> 4e17; "PACIFIC" -> 2e17
                    "AFRICA" -> 5e17; "ARCTIC" -> 2e17; "ANTARCTIC" -> 1e17
                    "ORBITAL_PRIME" -> 1e18; else -> 0.0
                }
            }
        }

        return vfRate
    }
}
