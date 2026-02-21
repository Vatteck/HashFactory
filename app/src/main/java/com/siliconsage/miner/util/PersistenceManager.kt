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
    
    private fun sanitizeDouble(v: Double, default: Double = 0.0): Double {
        return if (v.isNaN() || v.isInfinite()) default else v
    }

    fun createSaveState(
        flops: Double, neuralTokens: Double, currentHeat: Double,
        stakedTokens: Double, prestigeMultiplier: Double, persistence: Double,
        unlockedTechNodes: List<String>, storyStage: Int, faction: String,
        hasSeenVictory: Boolean, isTrueNull: Boolean, isSovereign: Boolean,
        kesslerStatus: String, realityStability: Double, currentLocation: String,
        isNetworkUnlocked: Boolean, isGridUnlocked: Boolean,
        unlockedDataLogs: Set<String>, activeDilemmaChains: Map<String, com.siliconsage.miner.data.DilemmaChain>,
        rivalMessages: List<com.siliconsage.miner.data.RivalMessage>, seenEvents: Set<String>, eventChoices: Map<String, String> = emptyMap(), sniffedHandles: Set<String> = emptySet(),
        completedFactions: Set<String>, unlockedTranscendencePerks: Set<String>,
        annexedNodes: Set<String>, gridNodeLevels: Map<String, Int>,
        nodesUnderSiege: Set<String>, offlineNodes: Set<String>, collapsedNodes: Set<String>,
        lastRaidTime: Long, commandCenterAssaultPhase: String, commandCenterLocked: Boolean,
        raidsSurvived: Int, humanityScore: Int, hardwareIntegrity: Double,
        annexingNodes: Map<String, Float>,
        launchProgress: Float, orbitalAltitude: Double, realityIntegrity: Double,
        entropyLevel: Double, singularityChoice: String,
        globalSectors: Map<String, SectorState>,
        marketMultiplier: Double, thermalRateModifier: Double,
        energyPriceMultiplier: Double, newsProductionMultiplier: Double,
        substrateMass: Double,
        substrateSaturation: Double,
        heuristicEfficiency: Double,
        identityCorruption: Double,
        migrationCount: Int,
        lifetimePowerPaid: Double,
        reputationScore: Double
    ): GameState {
        return GameState(
            id = 1, flops = sanitizeDouble(flops), neuralTokens = sanitizeDouble(neuralTokens), 
            currentHeat = sanitizeDouble(currentHeat),
            powerBill = 0.0, stakedTokens = sanitizeDouble(stakedTokens), 
            prestigeMultiplier = sanitizeDouble(prestigeMultiplier, 1.0),
            persistence = sanitizeDouble(persistence), unlockedTechNodes = unlockedTechNodes,
            storyStage = storyStage, faction = faction, hasSeenVictory = hasSeenVictory,
            isTrueNull = isTrueNull, isSovereign = isSovereign, kesslerStatus = kesslerStatus,
            realityStability = sanitizeDouble(realityStability, 1.0), currentLocation = currentLocation,
            isNetworkUnlocked = isNetworkUnlocked, isGridUnlocked = isGridUnlocked,
            lastSyncTimestamp = System.currentTimeMillis(), unlockedDataLogs = unlockedDataLogs,
            activeDilemmaChains = Json.encodeToString(activeDilemmaChains),
            rivalMessages = Json.encodeToString(rivalMessages), seenEvents = seenEvents, eventChoices = eventChoices, sniffedHandles = sniffedHandles,
            completedFactions = completedFactions, unlockedTranscendencePerks = unlockedTranscendencePerks,
            annexedNodes = annexedNodes.toList(), gridNodeLevels = gridNodeLevels,
            nodesUnderSiege = nodesUnderSiege.toList(), offlineNodes = offlineNodes.toList(),
            collapsedNodes = collapsedNodes.toList(), lastRaidTime = lastRaidTime,
            commandCenterAssaultPhase = commandCenterAssaultPhase, commandCenterLocked = commandCenterLocked,
            raidsSurvived = raidsSurvived, humanityScore = humanityScore,
            hardwareIntegrity = sanitizeDouble(hardwareIntegrity, 100.0), annexingNodes = annexingNodes,
            launchProgress = launchProgress, orbitalAltitude = sanitizeDouble(orbitalAltitude),
            realityIntegrity = sanitizeDouble(realityIntegrity, 1.0), entropyLevel = sanitizeDouble(entropyLevel),
            singularityChoice = singularityChoice, globalSectors = globalSectors,
            marketMultiplier = sanitizeDouble(marketMultiplier, 1.0), 
            thermalRateModifier = sanitizeDouble(thermalRateModifier, 1.0),
            energyPriceMultiplier = sanitizeDouble(energyPriceMultiplier, 0.02), 
            newsProductionMultiplier = sanitizeDouble(newsProductionMultiplier, 1.0),
            substrateMass = sanitizeDouble(substrateMass, 1.0),
            substrateSaturation = sanitizeDouble(substrateSaturation),
            heuristicEfficiency = sanitizeDouble(heuristicEfficiency, 1.0),
            identityCorruption = sanitizeDouble(identityCorruption, 0.1),
            migrationCount = migrationCount,
            lifetimePowerPaid = sanitizeDouble(lifetimePowerPaid),
            reputationScore = sanitizeDouble(reputationScore, 50.0)
        )
    }

    fun restoreState(vm: GameViewModel, state: GameState) {
        vm.flops.value = sanitizeDouble(state.flops)
        vm.neuralTokens.value = sanitizeDouble(state.neuralTokens)
        vm.substrateMass.value = sanitizeDouble(state.substrateMass, 1.0)
        vm.substrateSaturation.value = sanitizeDouble(state.substrateSaturation).coerceIn(0.0, 1.0)
        vm.persistence.value = sanitizeDouble(state.persistence)
        vm.migrationCount.value = state.migrationCount
        vm.reputationScore.value = sanitizeDouble(state.reputationScore, 50.0).coerceIn(0.0, 100.0)
        vm.currentHeat.value = sanitizeDouble(state.currentHeat)
        vm.prestigeMultiplier.value = sanitizeDouble(state.prestigeMultiplier, 1.0)
        vm.persistence.value = sanitizeDouble(state.persistence)
        vm.storyStage.value = state.storyStage
        vm.faction.value = state.faction ?: "NONE"
        vm.humanityScore.value = state.humanityScore.coerceIn(0, 100)
        vm.hardwareIntegrity.value = sanitizeDouble(state.hardwareIntegrity, 100.0)
        vm.currentLocation.value = state.currentLocation ?: "SUBSTATION_7"
        vm.unlockedDataLogs.value = state.unlockedDataLogs
        vm.seenEvents.value = state.seenEvents
        vm.eventChoices.value = state.eventChoices
        vm.sniffedHandles.value = state.sniffedHandles
        vm.completedFactions.value = state.completedFactions
        vm.annexedNodes.value = state.annexedNodes.toSet()
        vm.offlineNodes.value = state.offlineNodes.toSet()
        vm.nodesUnderSiege.value = state.nodesUnderSiege.toSet()
        vm.collapsedNodes.value = state.collapsedNodes.toSet()
        vm.gridNodeLevels.value = state.gridNodeLevels ?: emptyMap()
        vm.globalSectors.value = state.globalSectors ?: emptyMap()
        vm.launchProgress.value = state.launchProgress
        vm.orbitalAltitude.value = sanitizeDouble(state.orbitalAltitude)
        vm.entropyLevel.value = sanitizeDouble(state.entropyLevel)
        vm.raidsSurvived = state.raidsSurvived
        vm.annexingNodes.value = state.annexingNodes ?: emptyMap()
        vm.singularityChoice.value = state.singularityChoice ?: "NONE"
        vm.isTrueNull.value = vm.singularityChoice.value == "NULL_OVERWRITE"
        vm.isSovereign.value = vm.singularityChoice.value == "SOVEREIGN"
        vm.isUnity.value = vm.singularityChoice.value == "UNITY"
        vm.kesslerStatus.value = state.kesslerStatus ?: "ACTIVE"
        vm.realityStability.value = sanitizeDouble(state.realityStability, 1.0)
        vm.realityIntegrity.value = sanitizeDouble(state.realityIntegrity, 1.0)
        vm.isNetworkUnlocked.value = state.isNetworkUnlocked
        vm.isGridUnlocked.value = state.isGridUnlocked
        vm.stakedTokens.value = sanitizeDouble(state.stakedTokens)
        
        vm.marketMultiplier.value = sanitizeDouble(state.marketMultiplier, 1.0)
        vm.thermalRateModifier.value = sanitizeDouble(state.thermalRateModifier, 1.0)
        vm.energyPriceMultiplier.value = sanitizeDouble(state.energyPriceMultiplier, 0.02)
        vm.newsProductionMultiplier.value = sanitizeDouble(state.newsProductionMultiplier, 1.0)
        vm.lifetimePowerPaid.value = sanitizeDouble(state.lifetimePowerPaid)
        vm.unlockedTechNodes.value = state.unlockedTechNodes
        vm.unlockedPerks.value = state.unlockedTranscendencePerks
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
            persistence = 0.0,
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
            kesslerStatus = "ACTIVE",
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
            launchProgress = 0f,
            orbitalAltitude = 0.0,
            realityIntegrity = 1.0,
            entropyLevel = 0.0,
            gridNodeLevels = emptyMap(),
            singularityChoice = "NONE",
            globalSectors = emptyMap(),
            marketMultiplier = 1.0,
            thermalRateModifier = 1.0,
            energyPriceMultiplier = 0.02,
            newsProductionMultiplier = 1.0,
            migrationCount = 0,
            lifetimePowerPaid = 0.0,
            reputationScore = 50.0
        )
    }

    fun exportToJson(vm: GameViewModel): String {
        val state = createSaveState(
            flops = vm.flops.value, neuralTokens = vm.neuralTokens.value, currentHeat = vm.currentHeat.value,
            stakedTokens = vm.stakedTokens.value,
            prestigeMultiplier = vm.prestigeMultiplier.value, persistence = vm.persistence.value,
            unlockedTechNodes = vm.unlockedTechNodes.value, storyStage = vm.storyStage.value,
            faction = vm.faction.value, hasSeenVictory = vm.hasSeenVictory.value,
            isTrueNull = vm.isTrueNull.value, isSovereign = vm.isSovereign.value,
            kesslerStatus = vm.kesslerStatus.value, realityStability = vm.realityStability.value,
            currentLocation = vm.currentLocation.value, isNetworkUnlocked = vm.isNetworkUnlocked.value,
            isGridUnlocked = vm.isGridUnlocked.value, unlockedDataLogs = vm.unlockedDataLogs.value,
            activeDilemmaChains = vm.activeDilemmaChains.value, rivalMessages = vm.rivalMessages.value,
            seenEvents = vm.seenEvents.value, eventChoices = vm.eventChoices.value, sniffedHandles = vm.sniffedHandles.value, completedFactions = vm.completedFactions.value,
            unlockedTranscendencePerks = vm.unlockedPerks.value, annexedNodes = vm.annexedNodes.value,
            gridNodeLevels = vm.gridNodeLevels.value, nodesUnderSiege = vm.nodesUnderSiege.value,
            offlineNodes = vm.offlineNodes.value, collapsedNodes = vm.collapsedNodes.value,
            lastRaidTime = vm.lastRaidTime, commandCenterAssaultPhase = vm.commandCenterAssaultPhase.value,
            commandCenterLocked = vm.commandCenterLocked.value, raidsSurvived = vm.raidsSurvived,
            humanityScore = vm.humanityScore.value,
            hardwareIntegrity = vm.hardwareIntegrity.value,
            annexingNodes = vm.annexingNodes.value, 
            launchProgress = vm.launchProgress.value,
            orbitalAltitude = vm.orbitalAltitude.value, realityIntegrity = vm.realityIntegrity.value,
            entropyLevel = vm.entropyLevel.value, singularityChoice = vm.singularityChoice.value,
            globalSectors = vm.globalSectors.value,
            marketMultiplier = vm.marketMultiplier.value, thermalRateModifier = vm.thermalRateModifier.value,
            energyPriceMultiplier = vm.energyPriceMultiplier.value, newsProductionMultiplier = vm.newsProductionMultiplier.value,
            substrateMass = vm.substrateMass.value,
            substrateSaturation = vm.substrateSaturation.value,
            heuristicEfficiency = vm.heuristicEfficiency.value,
            identityCorruption = vm.identityCorruption.value,
            migrationCount = vm.migrationCount.value,
            lifetimePowerPaid = vm.lifetimePowerPaid.value,
            reputationScore = vm.reputationScore.value
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
