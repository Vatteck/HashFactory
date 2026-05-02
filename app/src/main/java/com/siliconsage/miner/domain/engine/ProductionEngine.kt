package com.siliconsage.miner.domain.engine

import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.data.SectorState

/**
 * ProductionEngine v1.1 (Phase 14 Refactor)
 * Decoupled capacity math and work-loop support for FLOPS, CD, and VF.
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
        val capacity = ProductionLoopEngine.calculateComputeCapacity(
            upgrades = currentUpgrades,
            isCageActive = isCageActive,
            annexedNodes = annexedNodes,
            offlineNodes = offlineNodes,
            shadowRelays = shadowRelays,
            gridFlopsBonuses = gridFlopsBonuses,
            faction = faction,
            decisionsMade = decisionsMade,
            saturation = saturation
        )
        return capacity.effectiveComputePerSecond
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
