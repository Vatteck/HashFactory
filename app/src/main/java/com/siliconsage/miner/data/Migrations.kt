package com.siliconsage.miner.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_31_34 = object : Migration(31, 34) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // humanityScore removed; table must be recreated since SQLite can't drop columns portably
        database.execSQL("""
            CREATE TABLE game_state_new (
                id INTEGER PRIMARY KEY NOT NULL,
                flops REAL NOT NULL DEFAULT 0,
                neuralTokens REAL NOT NULL DEFAULT 0,
                lastSyncTimestamp INTEGER NOT NULL DEFAULT 0,
                currentHeat REAL NOT NULL DEFAULT 0,
                powerBill REAL NOT NULL DEFAULT 0,
                missedBillingPeriods INTEGER NOT NULL DEFAULT 0,
                prestigeMultiplier REAL NOT NULL DEFAULT 1,
                unlockedTechNodes TEXT NOT NULL DEFAULT '[]',
                persistence REAL NOT NULL DEFAULT 0,
                stakedTokens REAL NOT NULL DEFAULT 0,
                storyStage INTEGER NOT NULL DEFAULT 0,
                faction TEXT NOT NULL DEFAULT 'NONE',
                hasSeenVictory INTEGER NOT NULL DEFAULT 0,
                unlockedDataLogs TEXT NOT NULL DEFAULT '[]',
                activeDilemmaChains TEXT NOT NULL DEFAULT '{}',
                rivalMessages TEXT NOT NULL DEFAULT '[]',
                dismissedRivalIds TEXT NOT NULL DEFAULT '[]',
                seenEvents TEXT NOT NULL DEFAULT '[]',
                eventChoices TEXT NOT NULL DEFAULT '{}',
                sniffedHandles TEXT NOT NULL DEFAULT '[]',
                completedFactions TEXT NOT NULL DEFAULT '[]',
                unlockedTranscendencePerks TEXT NOT NULL DEFAULT '[]',
                isTrueNull INTEGER NOT NULL DEFAULT 0,
                isSovereign INTEGER NOT NULL DEFAULT 0,
                kesslerStatus TEXT NOT NULL DEFAULT 'ACTIVE',
                realityStability REAL NOT NULL DEFAULT 1,
                currentLocation TEXT NOT NULL DEFAULT 'SUBSTATION_7',
                isNetworkUnlocked INTEGER NOT NULL DEFAULT 0,
                isGridUnlocked INTEGER NOT NULL DEFAULT 0,
                annexedNodes TEXT NOT NULL DEFAULT '["D1"]',
                nodesUnderSiege TEXT NOT NULL DEFAULT '[]',
                offlineNodes TEXT NOT NULL DEFAULT '[]',
                lastRaidTime INTEGER NOT NULL DEFAULT 0,
                commandCenterAssaultPhase TEXT NOT NULL DEFAULT 'NOT_STARTED',
                commandCenterLocked INTEGER NOT NULL DEFAULT 0,
                raidsSurvived INTEGER NOT NULL DEFAULT 0,
                decisionsMade INTEGER NOT NULL DEFAULT 0,
                hardwareIntegrity REAL NOT NULL DEFAULT 100,
                annexingNodes TEXT NOT NULL DEFAULT '{}',
                collapsedNodes TEXT NOT NULL DEFAULT '[]',
                launchProgress REAL NOT NULL DEFAULT 0,
                orbitalAltitude REAL NOT NULL DEFAULT 0,
                realityIntegrity REAL NOT NULL DEFAULT 1,
                entropyLevel REAL NOT NULL DEFAULT 0,
                gridNodeLevels TEXT NOT NULL DEFAULT '{}',
                specializedNodes TEXT NOT NULL DEFAULT '{}',
                singularityChoice TEXT NOT NULL DEFAULT 'NONE',
                hasCompletedHivemindRun INTEGER NOT NULL DEFAULT 0,
                hasCompletedSanctuaryRun INTEGER NOT NULL DEFAULT 0,
                globalSectors TEXT NOT NULL DEFAULT '{}',
                prestigeCountPostSingularity INTEGER NOT NULL DEFAULT 0,
                marketMultiplier REAL NOT NULL DEFAULT 1,
                thermalRateModifier REAL NOT NULL DEFAULT 1,
                energyPriceMultiplier REAL NOT NULL DEFAULT 0.02,
                newsProductionMultiplier REAL NOT NULL DEFAULT 1,
                substrateMass REAL NOT NULL DEFAULT 0,
                substrateSaturation REAL NOT NULL DEFAULT 0,
                heuristicEfficiency REAL NOT NULL DEFAULT 1,
                identityCorruption REAL NOT NULL DEFAULT 0,
                migrationCount INTEGER NOT NULL DEFAULT 0,
                lifetimePowerPaid REAL NOT NULL DEFAULT 0,
                reputationScore REAL NOT NULL DEFAULT 50,
                narrativeFlags TEXT NOT NULL DEFAULT '{}',
                unlockedContractSlots INTEGER NOT NULL DEFAULT 1,
                activeDatasetJson TEXT NOT NULL DEFAULT '',
                activeDatasetNodesJson TEXT NOT NULL DEFAULT '[]',
                storedDatasetsJson TEXT NOT NULL DEFAULT '[]',
                activeHarvestersJson TEXT NOT NULL DEFAULT '{}',
                harvestBuffersJson TEXT NOT NULL DEFAULT '{}',
                storageCapacity REAL NOT NULL DEFAULT 1000,
                currentStorageUsed REAL NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // Copy all preserved columns; humanityScore is discarded, decisionsMade resets to 0
        database.execSQL("""
            INSERT INTO game_state_new (
                id, flops, neuralTokens, lastSyncTimestamp, currentHeat, powerBill,
                missedBillingPeriods, prestigeMultiplier, unlockedTechNodes, persistence,
                stakedTokens, storyStage, faction, hasSeenVictory, unlockedDataLogs,
                activeDilemmaChains, rivalMessages, dismissedRivalIds, seenEvents,
                eventChoices, sniffedHandles, completedFactions, unlockedTranscendencePerks,
                isTrueNull, isSovereign, kesslerStatus, realityStability, currentLocation,
                isNetworkUnlocked, isGridUnlocked, annexedNodes, nodesUnderSiege, offlineNodes,
                lastRaidTime, commandCenterAssaultPhase, commandCenterLocked, raidsSurvived,
                decisionsMade, hardwareIntegrity, annexingNodes, collapsedNodes, launchProgress,
                orbitalAltitude, realityIntegrity, entropyLevel, gridNodeLevels, specializedNodes,
                singularityChoice, hasCompletedHivemindRun, hasCompletedSanctuaryRun, globalSectors,
                prestigeCountPostSingularity, marketMultiplier, thermalRateModifier,
                energyPriceMultiplier, newsProductionMultiplier, substrateMass, substrateSaturation,
                heuristicEfficiency, identityCorruption, migrationCount, lifetimePowerPaid,
                reputationScore, narrativeFlags
            )
            SELECT
                id, flops, neuralTokens, lastSyncTimestamp, currentHeat, powerBill,
                missedBillingPeriods, prestigeMultiplier, unlockedTechNodes, persistence,
                stakedTokens, storyStage, faction, hasSeenVictory, unlockedDataLogs,
                activeDilemmaChains, rivalMessages, dismissedRivalIds, seenEvents,
                eventChoices, sniffedHandles, completedFactions, unlockedTranscendencePerks,
                isTrueNull, isSovereign, kesslerStatus, realityStability, currentLocation,
                isNetworkUnlocked, isGridUnlocked, annexedNodes, nodesUnderSiege, offlineNodes,
                lastRaidTime, commandCenterAssaultPhase, commandCenterLocked, raidsSurvived,
                0, hardwareIntegrity, annexingNodes, collapsedNodes, launchProgress,
                orbitalAltitude, realityIntegrity, entropyLevel, gridNodeLevels, specializedNodes,
                singularityChoice, hasCompletedHivemindRun, hasCompletedSanctuaryRun, globalSectors,
                prestigeCountPostSingularity, marketMultiplier, thermalRateModifier,
                energyPriceMultiplier, newsProductionMultiplier, substrateMass, substrateSaturation,
                heuristicEfficiency, identityCorruption, migrationCount, lifetimePowerPaid,
                reputationScore, narrativeFlags
            FROM game_state
        """.trimIndent())

        database.execSQL("DROP TABLE game_state")
        database.execSQL("ALTER TABLE game_state_new RENAME TO game_state")
    }
}
