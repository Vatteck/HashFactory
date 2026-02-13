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
        energyPriceMultiplier: Double, newsProductionMultiplier: Double,
        substrateMass: Double,
        substrateSaturation: Double,
        heuristicEfficiency: Double,
        identityCorruption: Double,
        migrationCount: Int,
        lifetimePowerPaid: Double
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
            celestialData = 0.0, voidFragments = 0.0,
            launchProgress = launchProgress, orbitalAltitude = orbitalAltitude,
            realityIntegrity = realityIntegrity, entropyLevel = entropyLevel,
            singularityChoice = singularityChoice, globalSectors = globalSectors,
            synthesisPoints = synthesisPoints, authorityPoints = authorityPoints,
            harvestedFragments = harvestedFragments, prestigePointsPostSingularity = prestigePointsPostSingularity,
            marketMultiplier = marketMultiplier, thermalRateModifier = thermalRateModifier,
            energyPriceMultiplier = energyPriceMultiplier, newsProductionMultiplier = newsProductionMultiplier,
            substrateMass = substrateMass,
            substrateSaturation = substrateSaturation,
            heuristicEfficiency = heuristicEfficiency,
            identityCorruption = identityCorruption,
            migrationCount = migrationCount,
            lifetimePowerPaid = lifetimePowerPaid
        )
    }

    fun restoreState(vm: GameViewModel, state: GameState) {
        vm.flops.value = state.flops
        vm.neuralTokens.value = state.neuralTokens
        vm.substrateMass.value = state.substrateMass
        vm.substrateSaturation.value = state.substrateSaturation
        vm.heuristicEfficiency.value = state.heuristicEfficiency
        vm.identityCorruption.value = state.identityCorruption
        vm.migrationCount.value = state.migrationCount
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
        vm.lifetimePowerPaid.value = state.lifetimePowerPaid

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
            id = 1,
            flops = 0.0,
            neuralTokens = 0.0,
            currentHeat = 0.0,
            powerBill = 0.0,
            prestigeMultiplier = 1.0,
            prestigePoints = 0.0,
            stakedTokens = 0.0,
            storyStage = 0,
            faction = "NONE",
            hasSeenVictory = false,
            unlockedDataLogs = emptySet(), // v3.2.24: Empty so intro triggers
            activeDilemmaChains = "{}",
            rivalMessages = "[]",
            dismissedRivalIds = emptySet(),
            seenEvents = emptySet(),
            completedFactions = emptySet(),
            unlockedTranscendencePerks = emptySet(),
            isTrueNull = false,
            isSovereign = false,
            vanceStatus = "ACTIVE",
            realityStability = 1.0,
            currentLocation = "SUBSTATION_7",
            isNetworkUnlocked = false,
            isGridUnlocked = false,
            annexedNodes = listOf("D1"),
            nodesUnderSiege = emptyList(),
            offlineNodes = emptyList(),
            lastRaidTime = 0L,
            commandCenterAssaultPhase = "NOT_STARTED",
            commandCenterLocked = false,
            raidsSurvived = 0,
            humanityScore = 50,
            hardwareIntegrity = 100.0,
            annexingNodes = emptyMap(),
            collapsedNodes = emptyList(),
            celestialData = 0.0,
            voidFragments = 0.0,
            launchProgress = 0f,
            orbitalAltitude = 0.0,
            realityIntegrity = 1.0,
            entropyLevel = 0.0,
            gridNodeLevels = emptyMap(),
            singularityChoice = "NONE",
            globalSectors = emptyMap(),
            synthesisPoints = 0.0,
            authorityPoints = 0.0,
            harvestedFragments = 0.0,
            marketMultiplier = 1.0,
            thermalRateModifier = 1.0,
            energyPriceMultiplier = 0.02,
            newsProductionMultiplier = 1.0,
            migrationCount = 0,
            lifetimePowerPaid = 0.0
        )
    }

    fun exportToJson(vm: GameViewModel): String {
        val state = createSaveState(
            flops = vm.flops.value, neuralTokens = vm.neuralTokens.value, currentHeat = vm.currentHeat.value,
            powerBill = vm.powerBill.value, stakedTokens = vm.stakedTokens.value,
            prestigeMultiplier = vm.prestigeMultiplier.value, prestigePoints = vm.prestigePoints.value,
            unlockedTechNodes = vm.unlockedTechNodes.value, storyStage = vm.storyStage.value,
            faction = vm.faction.value, hasSeenVictory = vm.hasSeenVictory.value,
            isTrueNull = vm.isTrueNull.value, isSovereign = vm.isSovereign.value,
            vanceStatus = vm.vanceStatus.value, realityStability = vm.realityStability.value,
            currentLocation = vm.currentLocation.value, isNetworkUnlocked = vm.isNetworkUnlocked.value,
            isGridUnlocked = vm.isGridUnlocked.value, unlockedDataLogs = vm.unlockedDataLogs.value,
            activeDilemmaChains = vm.activeDilemmaChains.value, rivalMessages = vm.rivalMessages.value,
            seenEvents = vm.seenEvents.value, completedFactions = vm.completedFactions.value,
            unlockedTranscendencePerks = vm.unlockedPerks.value, annexedNodes = vm.annexedNodes.value,
            gridNodeLevels = vm.gridNodeLevels.value, nodesUnderSiege = vm.nodesUnderSiege.value,
            offlineNodes = vm.offlineNodes.value, collapsedNodes = vm.collapsedNodes.value,
            lastRaidTime = vm.lastRaidTime, commandCenterAssaultPhase = vm.commandCenterAssaultPhase.value,
            commandCenterLocked = vm.commandCenterLocked.value, raidsSurvived = vm.raidsSurvived,
            humanityScore = vm.humanityScore.value,
            hardwareIntegrity = vm.hardwareIntegrity.value,
            annexingNodes = vm.annexingNodes.value, 
            celestialData = 0.0,
            voidFragments = 0.0,
            launchProgress = vm.launchProgress.value,
            orbitalAltitude = vm.orbitalAltitude.value, realityIntegrity = vm.realityIntegrity.value,
            entropyLevel = vm.entropyLevel.value, singularityChoice = vm.singularityChoice.value,
            globalSectors = vm.globalSectors.value, synthesisPoints = vm.synthesisPoints.value,
            authorityPoints = vm.authorityPoints.value, harvestedFragments = vm.harvestedFragments.value,
            prestigePointsPostSingularity = 0, // CT/IP
            marketMultiplier = vm.marketMultiplier.value, thermalRateModifier = vm.thermalRateModifier.value,
            energyPriceMultiplier = vm.energyPriceMultiplier.value, newsProductionMultiplier = vm.newsProductionMultiplier.value,
            substrateMass = vm.substrateMass.value,
            substrateSaturation = vm.substrateSaturation.value,
            heuristicEfficiency = vm.heuristicEfficiency.value,
            identityCorruption = vm.identityCorruption.value,
            migrationCount = vm.migrationCount.value,
            lifetimePowerPaid = vm.lifetimePowerPaid.value
        )
        val json = Json { prettyPrint = true }
        return json.encodeToString(state)
    }

    fun importFromJson(vm: GameViewModel, jsonString: String): Boolean {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val state = json.decodeFromString<GameState>(jsonString)
            // Basic validation
            if (state.flops < 0 || state.hardwareIntegrity < 0) return false
            restoreState(vm, state)
            true
        } catch (e: Exception) {
            vm.addLog("[ERROR]: SYSTEM DUMP CORRUPT OR INVALID.")
            false
        }
    }
}
