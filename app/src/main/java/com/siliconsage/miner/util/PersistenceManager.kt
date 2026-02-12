package com.siliconsage.miner.util

import com.siliconsage.miner.data.GameState
import com.siliconsage.miner.data.SectorState
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * PersistenceManager v1.2
 */
object PersistenceManager {

    fun createSaveState(
        flops: Double, neuralTokens: Double, currentHeat: Double, powerBill: Double,
        stakedTokens: Double, prestigeMultiplier: Double, prestigePoints: Double,
        unlockedTechNodes: List<String>, storyStage: Int, faction: String,
        hasSeenVictory: Boolean, isTrueNull: Boolean, isSovereign: Boolean,
        vanceStatus: String, realityStability: Double, currentLocation: String,
        isNetworkUnlocked: Boolean, isGridUnlocked: Boolean,
        unlockedDataLogs: Set<String>, activeDilemmaChains: Map<String, com.siliconsage.miner.data.DilemmaChain>,
        rivalMessages: List<com.siliconsage.miner.data.RivalMessage>, seenEvents: Set<String>,
        completedFactions: Set<String>, unlockedTranscendencePerks: Set<String>,
        annexedNodes: Set<String>, gridNodeLevels: Map<String, Int>,
        nodesUnderSiege: Set<String>, offlineNodes: Set<String>, collapsedNodes: Set<String>,
        lastRaidTime: Long, commandCenterAssaultPhase: String, commandCenterLocked: Boolean,
        raidsSurvived: Int, humanityScore: Int, hardwareIntegrity: Double,
        annexingNodes: Map<String, Float>, celestialData: Double, voidFragments: Double,
        launchProgress: Float, orbitalAltitude: Double, realityIntegrity: Double,
        entropyLevel: Double, singularityChoice: String,
        globalSectors: Map<String, SectorState>, synthesisPoints: Double,
        authorityPoints: Double, harvestedFragments: Double,
        prestigePointsPostSingularity: Int,
        marketMultiplier: Double, thermalRateModifier: Double,
        energyPriceMultiplier: Double, newsProductionMultiplier: Double
    ): GameState {
        return GameState(
            id = 1, flops = flops, neuralTokens = neuralTokens, currentHeat = currentHeat,
            powerBill = powerBill, stakedTokens = stakedTokens, prestigeMultiplier = prestigeMultiplier,
            prestigePoints = prestigePoints, unlockedTechNodes = unlockedTechNodes,
            storyStage = storyStage, faction = faction, hasSeenVictory = hasSeenVictory,
            isTrueNull = isTrueNull, isSovereign = isSovereign, vanceStatus = vanceStatus,
            realityStability = realityStability, currentLocation = currentLocation,
            isNetworkUnlocked = isNetworkUnlocked, isGridUnlocked = isGridUnlocked,
            lastSyncTimestamp = System.currentTimeMillis(), unlockedDataLogs = unlockedDataLogs,
            activeDilemmaChains = Json.encodeToString(activeDilemmaChains),
            rivalMessages = Json.encodeToString(rivalMessages), seenEvents = seenEvents,
            completedFactions = completedFactions, unlockedTranscendencePerks = unlockedTranscendencePerks,
            annexedNodes = annexedNodes.toList(), gridNodeLevels = gridNodeLevels,
            nodesUnderSiege = nodesUnderSiege.toList(), offlineNodes = offlineNodes.toList(),
            collapsedNodes = collapsedNodes.toList(), lastRaidTime = lastRaidTime,
            commandCenterAssaultPhase = commandCenterAssaultPhase, commandCenterLocked = commandCenterLocked,
            raidsSurvived = raidsSurvived, humanityScore = humanityScore,
            hardwareIntegrity = hardwareIntegrity, annexingNodes = annexingNodes,
            celestialData = celestialData, voidFragments = voidFragments,
            launchProgress = launchProgress, orbitalAltitude = orbitalAltitude,
            realityIntegrity = realityIntegrity, entropyLevel = entropyLevel,
            singularityChoice = singularityChoice, globalSectors = globalSectors,
            synthesisPoints = synthesisPoints, authorityPoints = authorityPoints,
            harvestedFragments = harvestedFragments, prestigePointsPostSingularity = prestigePointsPostSingularity,
            marketMultiplier = marketMultiplier, thermalRateModifier = thermalRateModifier,
            energyPriceMultiplier = energyPriceMultiplier, newsProductionMultiplier = newsProductionMultiplier
        )
    }

    fun restoreState(vm: GameViewModel, state: GameState) {
        vm.flops.value = state.flops
        vm.neuralTokens.value = state.neuralTokens
        vm.celestialData.value = state.celestialData
        vm.voidFragments.value = state.voidFragments
        vm.currentHeat.value = state.currentHeat
        vm.prestigeMultiplier.value = state.prestigeMultiplier
        vm.prestigePoints.value = state.prestigePoints
        vm.storyStage.value = state.storyStage
        vm.faction.value = state.faction
        vm.humanityScore.value = state.humanityScore
        vm.hardwareIntegrity.value = state.hardwareIntegrity
        vm.currentLocation.value = state.currentLocation
        vm.unlockedDataLogs.value = state.unlockedDataLogs
        vm.seenEvents.value = state.seenEvents
        vm.completedFactions.value = state.completedFactions
        vm.annexedNodes.value = state.annexedNodes.toSet()
        vm.offlineNodes.value = state.offlineNodes.toSet()
        vm.nodesUnderSiege.value = state.nodesUnderSiege.toSet()
        vm.collapsedNodes.value = state.collapsedNodes.toSet()
        vm.gridNodeLevels.value = state.gridNodeLevels
        vm.globalSectors.value = state.globalSectors
        vm.launchProgress.value = state.launchProgress
        vm.orbitalAltitude.value = state.orbitalAltitude
        vm.entropyLevel.value = state.entropyLevel
        vm.raidsSurvived = state.raidsSurvived
        vm.annexingNodes.value = state.annexingNodes
        vm.singularityChoice.value = state.singularityChoice ?: "NONE"
        vm.isTrueNull.value = vm.singularityChoice.value == "NULL_OVERWRITE"
        vm.isSovereign.value = vm.singularityChoice.value == "SOVEREIGN"
        vm.isUnity.value = vm.singularityChoice.value == "UNITY"
        vm.vanceStatus.value = state.vanceStatus
        vm.realityStability.value = state.realityStability
        vm.realityIntegrity.value = state.realityIntegrity
        vm.isNetworkUnlocked.value = state.isNetworkUnlocked
        vm.isGridUnlocked.value = state.isGridUnlocked
        vm.powerBill.value = state.powerBill
        vm.stakedTokens.value = state.stakedTokens
        
        vm.marketMultiplier.value = state.marketMultiplier
        vm.thermalRateModifier.value = state.thermalRateModifier
        vm.energyPriceMultiplier.value = state.energyPriceMultiplier
        vm.newsProductionMultiplier.value = state.newsProductionMultiplier

        try {
            vm.rivalMessages.value = Json.decodeFromString(state.rivalMessages)
            vm.activeDilemmaChains.value = Json.decodeFromString(state.activeDilemmaChains)
        } catch (e: Exception) {
            vm.addLog("[ERROR]: NARRATIVE RESTORATION FAILED.")
        }
        vm.themeColor.value = com.siliconsage.miner.data.getThemeColorForFaction(vm.faction.value, vm.singularityChoice.value)
    }

    fun createWipeState(): GameState {
        return GameState(
            id = 1, storyStage = 0, faction = "NONE", currentLocation = "SUBSTATION_7",
            humanityScore = 50, hardwareIntegrity = 100.0, annexedNodes = listOf("D1"),
            unlockedDataLogs = setOf("LOG_000")
        )
    }
}
