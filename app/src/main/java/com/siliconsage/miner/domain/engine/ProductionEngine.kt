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
        shadowRelays: Set<String> = emptySet(),
        gridFlopsBonuses: Map<String, Double>,
        faction: String,
        decisionsMade: Int,
        saturation: Double = 0.0
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
            if (!offlineNodes.contains(nodeId) && !shadowRelays.contains(nodeId) && (!isCageActive || nodeId == "A3")) {
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
            val moralBoost = 1.0 + (decisionsMade * 0.02).coerceAtMost(2.0)
            totalFlops *= moralBoost
        }
        if (currentUpgrades[UpgradeType.HYBRID_OVERCLOCK]?.let { it > 0 } == true) {
            totalFlops *= 3.0
        }
        
        // Phase 23, Step 6: Saturation Stall
        // As the local sector / dimensional layer is tapped out, production stalls.
        // This forces "The Overwrite" hard reset.
        val stallMultiplier = (1.0 - saturation).coerceIn(0.0, 1.0)
        totalFlops *= stallMultiplier

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
        // Phase 23, Step 5: Substrate Saturation acts as the overarching Stage 5 limit.
        // It fills up strictly based on your raw production rate (`flopsPerSec`).
        // The more you produce, the faster you tap out the local sector and are forced to Burn (Migrate).
        
        if (location == "ORBITAL_SATELLITE") {
            // Orbit Limit (Slower burn, scales with altitude)
            val altitudeMult = 1.0 + (orbitalAltitude / 500.0)
            return (flopsPerSec / (500_000_000_000_000.0 * altitudeMult)).coerceAtLeast(0.0)
        } else if (location == "VOID_INTERFACE") {
            // Void Limit (Massive, chaotic burn, accelerated by entropy)
            var entropyMult = 1.0 + (kotlin.math.log2(entropyLevel + 1.0) * 2.0)
            if ((upgrades[UpgradeType.ENTROPY_ACCELERATOR] ?: 0) > 0) entropyMult *= 2.0
            return (flopsPerSec * entropyMult / 5_000_000_000_000_000.0).coerceAtLeast(0.0)
        }
        
        return 0.0
    }

    // --- LEGACY DELEGATES (TO BE REMOVED) ---
    fun calculateCelestialDataRate(f: Double, a: Double, o: Double, s: Int, g: Map<String, SectorState>, h: Double) = 0.0
    fun calculateVoidFragmentRate(f: Double, e: Double, ev: Boolean, sw: Boolean, h: Double, w: Int, c: Int, dmp: Boolean, dm: Int, g: Map<String, SectorState>) = 0.0
}
