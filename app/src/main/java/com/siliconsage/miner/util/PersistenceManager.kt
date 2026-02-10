package com.siliconsage.miner.util

import com.siliconsage.miner.data.GameState
import com.siliconsage.miner.data.SectorState
import com.siliconsage.miner.viewmodel.ResonanceState
import com.siliconsage.miner.viewmodel.ResonanceTier
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * PersistenceManager v1.0 (Phase 14 extraction)
 * Handles mapping between ViewModel state flows and the persistent GameState entity.
 */
object PersistenceManager {

    /**
     * Map current ViewModel state flows to a persistent GameState entity
     */
    fun createSaveState(
        flops: Double,
        neuralTokens: Double,
        currentHeat: Double,
        powerBill: Double,
        stakedTokens: Double,
        prestigeMultiplier: Double,
        prestigePoints: Double,
        unlockedTechNodes: List<String>,
        storyStage: Int,
        faction: String,
        hasSeenVictory: Boolean,
        isTrueNull: Boolean,
        isSovereign: Boolean,
        vanceStatus: String,
        realityStability: Double,
        currentLocation: String,
        isNetworkUnlocked: Boolean,
        isGridUnlocked: Boolean,
        unlockedDataLogs: Set<String>,
        activeDilemmaChains: Map<String, com.siliconsage.miner.data.DilemmaChain>,
        rivalMessages: List<com.siliconsage.miner.data.RivalMessage>,
        seenEvents: Set<String>,
        completedFactions: Set<String>,
        unlockedTranscendencePerks: Set<String>,
        annexedNodes: Set<String>,
        gridNodeLevels: Map<String, Int>,
        nodesUnderSiege: Set<String>,
        offlineNodes: Set<String>,
        collapsedNodes: Set<String>,
        lastRaidTime: Long,
        commandCenterAssaultPhase: String,
        commandCenterLocked: Boolean,
        raidsSurvived: Int,
        humanityScore: Int,
        hardwareIntegrity: Double,
        annexingNodes: Map<String, Float>,
        celestialData: Double,
        voidFragments: Double,
        launchProgress: Float,
        orbitalAltitude: Double,
        realityIntegrity: Double,
        entropyLevel: Double,
        resonanceState: ResonanceState,
        singularityChoice: String,
        globalSectors: Map<String, SectorState>,
        synthesisPoints: Double,
        authorityPoints: Double,
        harvestedFragments: Double,
        prestigePointsPostSingularity: Int,
        cdLifetime: Double,
        vfLifetime: Double,
        peakResonanceTier: ResonanceTier
    ): GameState {
        return GameState(
            id = 1,
            flops = flops,
            neuralTokens = neuralTokens,
            currentHeat = currentHeat,
            powerBill = powerBill,
            stakedTokens = stakedTokens,
            prestigeMultiplier = prestigeMultiplier,
            prestigePoints = prestigePoints,
            unlockedTechNodes = unlockedTechNodes,
            storyStage = storyStage,
            faction = faction,
            hasSeenVictory = hasSeenVictory,
            isTrueNull = isTrueNull,
            isSovereign = isSovereign,
            vanceStatus = vanceStatus,
            realityStability = realityStability,
            currentLocation = currentLocation,
            isNetworkUnlocked = isNetworkUnlocked,
            isGridUnlocked = isGridUnlocked,
            lastSyncTimestamp = System.currentTimeMillis(),
            
            unlockedDataLogs = unlockedDataLogs,
            activeDilemmaChains = Json.encodeToString(activeDilemmaChains),
            rivalMessages = Json.encodeToString(rivalMessages),
            seenEvents = seenEvents,
            completedFactions = completedFactions,
            unlockedTranscendencePerks = unlockedTranscendencePerks,
            annexedNodes = annexedNodes.toList(),
            gridNodeLevels = gridNodeLevels,
            
            nodesUnderSiege = nodesUnderSiege.toList(),
            offlineNodes = offlineNodes.toList(),
            collapsedNodes = collapsedNodes.toList(),
            lastRaidTime = lastRaidTime,
            
            commandCenterAssaultPhase = commandCenterAssaultPhase,
            commandCenterLocked = commandCenterLocked,
            raidsSurvived = raidsSurvived,
            
            humanityScore = humanityScore,
            hardwareIntegrity = hardwareIntegrity,
            
            annexingNodes = annexingNodes,
            
            celestialData = celestialData,
            voidFragments = voidFragments,
            launchProgress = launchProgress,
            orbitalAltitude = orbitalAltitude,
            realityIntegrity = realityIntegrity,
            entropyLevel = entropyLevel,

            resonanceActive = resonanceState.isActive,
            resonanceTier = resonanceState.tier.name,
            singularityChoice = singularityChoice,
            globalSectors = globalSectors,
            synthesisPoints = synthesisPoints,
            authorityPoints = authorityPoints,
            harvestedFragments = harvestedFragments,
            prestigePointsPostSingularity = prestigePointsPostSingularity,
            cdLifetime = cdLifetime,
            vfLifetime = vfLifetime,
            peakResonanceTier = peakResonanceTier.name
        )
    }

    /**
     * Create a reset state for Transcendence (NG+)
     */
    fun createResetState(
        preservedTechNodes: List<String>,
        preservedPrestigePoints: Double,
        preservedHasSeenVictory: Boolean,
        preservedCompletedFactions: Set<String>,
        preservedPerks: Set<String>,
        preservedNetworkUnlocked: Boolean,
        preservedGridUnlocked: Boolean,
        preservedUnlockedLogs: Set<String> // v3.1.8-fix: Persist lore through migration
    ): GameState {
        return GameState(
            id = 1,
            flops = if (preservedPerks.contains("neural_dividend")) 10000.0 else 0.0,
            neuralTokens = if (preservedPerks.contains("neural_dividend")) 1000.0 else 0.0,
            currentHeat = 0.0,
            powerBill = 0.0,
            prestigeMultiplier = 1.0,
            stakedTokens = 0.0,
            unlockedTechNodes = preservedTechNodes,
            prestigePoints = preservedPrestigePoints,
            storyStage = 0,
            faction = "NONE",
            hasSeenVictory = preservedHasSeenVictory,
            vanceStatus = "ACTIVE",
            realityStability = 1.0,
            currentLocation = "SUBSTATION_7",
            isNetworkUnlocked = preservedNetworkUnlocked,
            isGridUnlocked = preservedGridUnlocked,
            unlockedDataLogs = preservedUnlockedLogs,
            annexedNodes = listOf("D1"),
            completedFactions = preservedCompletedFactions,
            unlockedTranscendencePerks = preservedPerks
        )
    }
}
