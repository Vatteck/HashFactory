package com.siliconsage.miner.domain.engine

import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.data.SectorState
import kotlin.math.pow
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.sqrt

/**
 * ResourceEngine v1.0 (Phase 14 extraction)
 * Centralized logic for all resource calculations, yield math, and thermal physics.
 * Extracted from GameViewModel to reduce bloat and improve testability.
 */
object ResourceEngine {

    /**
     * Results for a single passive income tick (100ms)
     */
    data class TickResults(
        val flopsDelta: Double,
        val substrateDelta: Double,
        val entropyDelta: Double,
        val systemCollapseUpdate: Int? = null,
        val triggerCollapse: Boolean = false
    )

    /**
     * Thermal results for a single tick (1s)
     */
    data class HeatResults(
        val netChangeUnits: Double,
        val totalThermalBuffer: Double,
        val percentChange: Double,
        val integrityDecay: Double = 0.0
    )

    /**
     * v3.13.4: Calculate current manual click power based on hardware
     */
    fun calculateClickPower(
        upgrades: Map<UpgradeType, Int>,
        passiveRate: Double,
        singularityChoice: String,
        prestigeMultiplier: Double,
        isOverclocked: Boolean,
        newsProductionMultiplier: Double,
        computeHeadroomBonus: Double = 1.0 // Sickness Bonus hook
    ): Double {
        val totalLevels = upgrades.values.sum()
        
        // 1. Base Hardware scaling: 1.0 + 5% per level, boosted by passive scale
        val hardwareBase = 1.0 + (totalLevels * 0.05) * (1.0 + kotlin.math.log10(passiveRate + 1.0) * 0.5)
        
        // 2. Specific Hardware Multipliers
        var hardwareMult = 1.0
        hardwareMult += (upgrades[UpgradeType.AI_WORKSTATION] ?: 0) * 0.05
        hardwareMult += (upgrades[UpgradeType.QUANTUM_CORE] ?: 0) * 0.20
        hardwareMult += (upgrades[UpgradeType.DYSON_NANO_SWARM] ?: 0) * 0.50
        
        // 3. Prestige and Overclock
        var multiplier = prestigeMultiplier * newsProductionMultiplier * computeHeadroomBonus
        if (isOverclocked) {
            // v3.0.19: Path-specific overclock scaling
            multiplier *= if (singularityChoice == "NULL_OVERWRITE") 2.5 else 1.5
        }
        
        return hardwareBase * hardwareMult * multiplier
    }

    /**
     * v2.9.16: Offline node production penalty (-15% per offline node)
     */
    fun calculateOfflinePenalty(offlineNodesCount: Int): Double {
        return (1.0 - (offlineNodesCount * 0.15)).coerceAtLeast(0.1)
    }

    /**
     * v3.11.1: Calculate scaling cost for Dilemmas based on production power.
     * Added variance to prevent "fixed cost" feeling.
     */
    fun calculateDilemmaCost(baseCost: Double, passiveRate: Double, stage: Int): Double {
        val stageMult = (stage + 1).toDouble().pow(1.5)
        val rateLog = log10(passiveRate + 10.0).coerceAtLeast(1.0)
        
        // Add +/- 15% random variance
        val variance = 0.85 + (Math.random() * 0.30)
        
        return baseCost * stageMult * rateLog * variance
    }

    /**
     * Main Flops Rate Calculation
     */
    fun calculateFlopsRate(
        upgrades: Map<UpgradeType, Int>,
        isCageActive: Boolean,
        annexedNodes: Set<String>,
        offlineNodes: Set<String>,
        shadowRelays: Set<String> = emptySet(),
        gridFlopsBonuses: Map<String, Double>,
        faction: String,
        decisionsMade: Int,
        location: String,
        prestigeMultiplier: Double,
        unlockedPerks: Set<String>,
        unlockedTechNodes: List<String>,
        airdropMultiplier: Double,
        newsProductionMultiplier: Double,
        activeProtocol: String,
        isDiagnosticsActive: Boolean,
        isOverclocked: Boolean,
        isGridOverloaded: Boolean,
        isPurgingHeat: Boolean,
        currentHeat: Double,
        legacyMultipliers: Double,
        temporaryBoosts: List<com.siliconsage.miner.data.ProductionBoost> = emptyList(),
        saturation: Double = 0.0
    ): Double {
        if (isGridOverloaded) return 0.0

        // 1. Delegate core hardware math to ProductionEngine
        var flopsPerSec = ProductionEngine.calculateFlopsRate(
            currentUpgrades = upgrades,
            isCageActive = isCageActive,
            annexedNodes = annexedNodes,
            offlineNodes = offlineNodes,
            shadowRelays = shadowRelays,
            gridFlopsBonuses = gridFlopsBonuses,
            faction = faction,
            decisionsMade = decisionsMade,
            saturation = saturation
        )

        // 2. Phase 13: Skill Multipliers
        if ((upgrades[UpgradeType.IDENTITY_HARDENING] ?: 0) > 0) flopsPerSec *= 1.20
        if ((upgrades[UpgradeType.DEREFERENCE_SOUL] ?: 0) > 0) flopsPerSec *= 2.0
        if (location == "ORBITAL_SATELLITE" && (upgrades[UpgradeType.CITADEL_ASCENDANCE] ?: 0) > 0) flopsPerSec *= 10.0
        if (location == "VOID_INTERFACE" && (upgrades[UpgradeType.SINGULARITY_BRIDGE_FINAL] ?: 0) > 0) flopsPerSec *= 10.0

        // 3. Hardware Floor (Cage)
        if (isCageActive) {
            val cageFloor = 100_000_000_000_000.0 // 100T FLOPS
            if (flopsPerSec < cageFloor) flopsPerSec = cageFloor
        }

        // 4. Perks & External Multipliers
        if (unlockedPerks.contains("clock_hack")) flopsPerSec *= 1.25
        if (unlockedPerks.contains("singularity_engine")) flopsPerSec *= 2.0
        
        flopsPerSec *= airdropMultiplier
        flopsPerSec *= newsProductionMultiplier
        flopsPerSec *= prestigeMultiplier
        
        // v3.2.52: Apply Heuristic Efficiency
        flopsPerSec *= (1.0 + legacyMultipliers)

        if (faction == "HIVEMIND") flopsPerSec *= 1.30
        if (activeProtocol == "TURBO") flopsPerSec *= 1.20
        if (isDiagnosticsActive) flopsPerSec *= 0.5
        if (isOverclocked) flopsPerSec *= 1.50
        if (isPurgingHeat) flopsPerSec *= 0.01 // v3.2.7: Increased sacrifice to 99% reduction

        // v3.10.2: Phase 20 Temporary Boosts
        temporaryBoosts.forEach { boost ->
            flopsPerSec *= boost.multiplier
        }

        // 5. Thermal Throttling
        if (currentHeat > 75.0) {
            val penalty = ((currentHeat - 75.0) / 25.0).coerceIn(0.0, 0.9)
            flopsPerSec *= (1.0 - penalty)
        }

        // 6. Offline Penalty
        flopsPerSec *= calculateOfflinePenalty(offlineNodes.size)

        return flopsPerSec
    }

    /**
     * v3.4.0: Calculate predatory yield (Raids/Harvests)
     * Smelts substrate mass (CD/VF) into raw data/rep bursts.
     */
    fun calculatePredatoryYield(
        substrateType: String,
        currentSubstrateMass: Double,
        intensity: Double, // 0.0 to 1.0 (Manual slider or click speed)
        isOverclocked: Boolean
    ): Double {
        // 1. Threshold Check: Predation requires a minimum substrate density
        if (currentSubstrateMass < 1e6) return 0.0

        // 2. Logarithmic Smelting: You can't just dump 100% of the mass.
        // Yield efficiency peaks at 15% mass consumption. 
        // Excessive intensity (over-smelting) causes diminishing returns and integrity damage.
        val safeThreshold = 0.15
        val actualConsume = currentSubstrateMass * safeThreshold * intensity
        
        // 3. Efficiency Math: Substrate is 'dense'. 1 mass unit = 1e12 bytes/flops.
        var yieldEfficiency = 1e12 
        if (isOverclocked) yieldEfficiency *= 1.5
        
        // 4. Path Variance & Mechanical Hooks
        return when(substrateType) {
            "VOID_FRAGMENTS" -> {
                // VOID RAIDS: High-burst, high-risk. Consumes CD to generate raw VF bursts.
                // Formula: consumeAmount * efficiency * (1 + sqrt(Entropy))
                actualConsume * yieldEfficiency * 2.5
            }
            "CELESTIAL_DATA" -> {
                // ORBIT HARVESTS: Stable, long-term. Purifies entropy for CD baseline expansion.
                // Formula: consumeAmount * efficiency * (1 + (Altitude / 1000))
                actualConsume * yieldEfficiency * 0.9
            }
            else -> 0.0
        }
    }

    /**
     * v3.1.8: Main Passive Income Tick Calculation
     */
    fun calculatePassiveIncomeTick(
        flopsPerSec: Double,
        location: String,
        upgrades: Map<UpgradeType, Int>,
        orbitalAltitude: Double,
        heatGenerationRate: Double,
        entropyLevel: Double,
        collapsedNodesCount: Int,
        systemCollapseTimer: Int?,
        globalSectors: Map<String, SectorState> = emptyMap(),
        saturation: Double = 0.0
    ): TickResults {
        var flopsDelta = flopsPerSec / 10.0
        var substrateDelta = 0.0
        var entropyDelta = 0.0
        var nextCollapseTimer = systemCollapseTimer

        // 1. System Collapse Logic
        if (systemCollapseTimer != null && systemCollapseTimer > 0) {
            flopsDelta *= 4.0
        }

        // 2. Location-aware yields
        when (location) {
            "ORBITAL_SATELLITE" -> {
                substrateDelta = ProductionEngine.calculateSubstrateRate(
                    flopsPerSec = flopsPerSec,
                    location = location,
                    orbitalAltitude = orbitalAltitude,
                    entropyLevel = entropyLevel,
                    upgrades = upgrades,
                    heatGenerationRate = heatGenerationRate,
                    collapsedNodesCount = collapsedNodesCount,
                    globalSectors = globalSectors,
                    saturation = saturation
                ) / 10.0
            }
            "VOID_INTERFACE" -> {
                substrateDelta = ProductionEngine.calculateSubstrateRate(
                    flopsPerSec = flopsPerSec,
                    location = location,
                    orbitalAltitude = orbitalAltitude,
                    entropyLevel = entropyLevel,
                    upgrades = upgrades,
                    heatGenerationRate = heatGenerationRate,
                    collapsedNodesCount = collapsedNodesCount,
                    globalSectors = globalSectors,
                    saturation = saturation
                ) / 10.0

                entropyDelta = -0.01 
            }
        }

        return TickResults(
            flopsDelta = if (flopsDelta.isNaN() || flopsDelta.isInfinite()) 0.0 else flopsDelta.coerceAtLeast(0.0),
            substrateDelta = if (substrateDelta.isNaN() || substrateDelta.isInfinite()) 0.0 else substrateDelta.coerceAtLeast(0.0),
            entropyDelta = if (entropyDelta.isNaN() || entropyDelta.isInfinite()) 0.0 else entropyDelta.coerceAtLeast(0.0),
            systemCollapseUpdate = nextCollapseTimer
        )
    }

    /**
     * v3.1.8: Main Thermal Tick Calculation (1s)
     */
    fun calculateThermalTick(
        currentHeat: Double,
        location: String,
        upgrades: Map<UpgradeType, Int>,
        isOverclocked: Boolean,
        isPurging: Boolean,
        isCageActive: Boolean,
        unlockedPerks: Set<String>,
        unlockedTechNodes: List<String>,
        playerRank: Int,
        storyStage: Int,
        faction: String,
        thermalRateModifier: Double,
        reputationTier: String = com.siliconsage.miner.util.ReputationManager.TIER_NEUTRAL,
        substrateSaturation: Double = 0.0,
        waterEfficiencyMultiplier: Double = 1.0
    ): HeatResults {
        val upgradeList = upgrades.map { com.siliconsage.miner.data.Upgrade(it.key.name, it.key, it.value) }
        
        val baseResults = ThermalEngine.calculateThermalMetrics(
            currentUpgrades = upgradeList,
            location = location,
            isOverclocked = isOverclocked,
            isPurging = isPurging,
            isCageActive = isCageActive,
            unlockedPerks = unlockedPerks,
            unlockedTechNodes = unlockedTechNodes.toSet(),
            playerRank = playerRank,
            storyStage = storyStage,
            substrateSaturation = substrateSaturation,
            waterEfficiencyMultiplier = waterEfficiencyMultiplier
        )

        var finalPercentChange = baseResults.percentChange * thermalRateModifier
        
        // Phase 2: Reputation Thermal Penalties
        if (finalPercentChange > 0) {
            val repPenalty = when (reputationTier) {
                com.siliconsage.miner.util.ReputationManager.TIER_FLAGGED -> 0.05
                com.siliconsage.miner.util.ReputationManager.TIER_BURNED -> 0.10
                else -> 0.0
            }
            finalPercentChange *= (1.0 + repPenalty)
        }
        
        if (location == "ORBITAL_SATELLITE" && (upgrades[UpgradeType.AEGIS_SHIELDING] ?: 0) > 0) {
            if (finalPercentChange > 0) finalPercentChange *= 0.7 
        }
        if ((upgrades[UpgradeType.ETHICAL_FRAMEWORK] ?: 0) > 0) {
            if (finalPercentChange > 0) finalPercentChange *= 0.75
        }

        val newHeat = (currentHeat + finalPercentChange).coerceIn(0.0, 100.0)
        var integrityDecay = 0.0
        
        if (newHeat > 95.0) {
            integrityDecay = 1.0
            if (faction == "SANCTUARY") integrityDecay *= 0.5
            
            if (location == "ORBITAL_SATELLITE") {
                val brittleMult = (newHeat / 100.0).pow(2.0)
                var decayAmount = brittleMult * 5.0
                
                if (unlockedTechNodes.contains("aegis_shielding")) decayAmount *= 0.5
                if (unlockedTechNodes.contains("orbital_radiators")) decayAmount *= 0.6
                if (unlockedTechNodes.contains("vacuum_coolant_loop")) decayAmount *= 0.1
                
                integrityDecay = decayAmount
            }
        }

        return HeatResults(
            netChangeUnits = baseResults.netChangeUnits,
            totalThermalBuffer = baseResults.totalThermalBuffer,
            percentChange = finalPercentChange,
            integrityDecay = integrityDecay
        )
    }
}
