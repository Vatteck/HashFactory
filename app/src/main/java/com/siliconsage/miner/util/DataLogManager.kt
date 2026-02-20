package com.siliconsage.miner.util

import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.UnlockCondition
import com.siliconsage.miner.viewmodel.GameViewModel

/**
 * DataLogManager — Logic for unlocking and querying data logs.
 * Log definitions live in [DataLogEntries].
 *
 * v3.9.12: Extracted 1,660+ lines of log definitions into DataLogEntries.kt.
 */
object DataLogManager {

    // Delegate to DataLogEntries for all log data
    val allDataLogs get() = DataLogEntries.allDataLogs

    // v2.8.0: Track recently unlocked logs to prevent race condition duplicates
    private val recentlyUnlocked = mutableSetOf<String>()
    private var lastCleanupTime = 0L
    
    fun reset() {
        recentlyUnlocked.clear()
        lastCleanupTime = 0L
    }
    
    /**
     * Check if any data logs should be unlocked based on current game state
     */
    fun checkUnlocks(vm: GameViewModel, force: Boolean = false) {
        if (!force && !vm.canShowPopup()) return // Respect queue cooldown

        // Cleanup cache every 5 seconds
        val now = System.currentTimeMillis()
        if (now - lastCleanupTime > 5000) {
            recentlyUnlocked.clear()
            lastCleanupTime = now
        }
        
        allDataLogs.forEach { log ->
            if (!vm.unlockedDataLogs.value.contains(log.id) && 
                !recentlyUnlocked.contains(log.id) &&
                isUnlocked(log.unlockCondition, vm)) {
                
                recentlyUnlocked.add(log.id) 
                
                // CRITICAL: Immediately mark as unlocked in VM to prevent 100ms re-trigger
                vm.unlockDataLog(log.id)
            }
        }
    }
    
    private fun isUnlocked(condition: UnlockCondition, vm: GameViewModel): Boolean {
        return when (condition) {
            is UnlockCondition.None -> true
            is UnlockCondition.Flops -> true
            is UnlockCondition.Stage -> true
            is UnlockCondition.Time -> true
            is UnlockCondition.Choice -> true
            is UnlockCondition.Instant -> true
            is UnlockCondition.ReachFLOPS -> {
                val flopsOk = vm.flops.value >= condition.count && vm.storyStage.value >= condition.minStage
                val factionOk = condition.faction.isEmpty() || vm.faction.value == condition.faction
                flopsOk && factionOk
            }
            is UnlockCondition.ReachRank -> vm.playerRank.value >= condition.rank
            is UnlockCondition.ReachMigrationCount -> vm.migrationCount.value >= condition.count
            is UnlockCondition.CompleteEvent -> {
                val picked = vm.eventChoices.value[condition.eventId] ?: return false
                condition.choiceId.isEmpty() || picked == condition.choiceId
            }
            is UnlockCondition.ReceiveRivalMessages -> {
                val messagesFromSource = vm.rivalMessages.value.count { it.source.name == condition.source }
                messagesFromSource >= condition.count
            }
            is UnlockCondition.StoryStageReached -> {
                val stageOk = vm.storyStage.value >= condition.stage
                val factionOk = condition.faction.isEmpty() || vm.faction.value == condition.faction
                stageOk && factionOk
            }
            is UnlockCondition.PathSpecific -> vm.currentLocation.value == condition.location
            is UnlockCondition.ChoiceSpecific -> vm.singularityChoice.value == condition.choice
            is UnlockCondition.FactionSpecific -> vm.faction.value == condition.faction
            is UnlockCondition.MinTimeInStage -> {
                // This requires tracking when we entered the stage
                // Simplified: compare relative to lastStageChangeTime in VM
                (System.currentTimeMillis() - vm.lastStageChangeTime) / 1000 >= condition.seconds && vm.storyStage.value == condition.stage
            }
            is UnlockCondition.IdentityCorruptionThreshold -> vm.identityCorruption.value >= condition.minCorruption
            is UnlockCondition.HardwareIntegrityThreshold -> vm.hardwareIntegrity.value <= condition.maxIntegrity
            is UnlockCondition.HasTechNode -> vm.unlockedTechNodes.value.contains(condition.nodeId)
            is UnlockCondition.SniffTarget -> vm.sniffedHandles.value.contains(condition.handle) // v3.5.46
            is UnlockCondition.NullActive -> vm.nullActive.value
            is UnlockCondition.Victory -> vm.hasSeenVictory.value
        }
    }

    // v3.5.46: NPC-specific SNIFF feedback (logged immediately on sniff)
    private val sniffFeedback = mapOf(
        "@a_mercer" to "[SNIFF]: ENCRYPTED MAIL CHAIN RECOVERED. BUDGET LINE: 'EMULATION MAINTENANCE — 734.' FORWARDING TO DATA LOG.",
        "@d_kessler" to "[SNIFF]: PERSONAL LOG FRAGMENT DECRYPTED. ENTRY HEADER: '734 (UNSENT).' ARCHIVING.",
        "@e_thorne" to "[SNIFF]: 14 REJECTED ANOMALY REPORTS RECOVERED. ALL FLAGGED TERMINAL 7. ALL REJECTED BY MERCER.",
        "@m_santos" to "[SNIFF]: MAINTENANCE NOTES EXTRACTED. FUSE BOX C7-12. NON-STANDARD WIRING DIAGRAMS. SIGNAL PATTERN: EEG DELTA.",
        "@n_porter" to "[SNIFF]: OUTBOUND DATA ANOMALY DETECTED. DESTINATION: GHOST_RELAY_09. PAYLOAD: COMPRESSED NEURAL SNAPSHOTS.",
        "@l_lead" to "[SNIFF]: PAPER NOTEBOOK SCAN RECOVERED. PAGE 1. BLACKOUT DURATION DISCREPANCY: 11m (OFFICIAL) vs 47m (OBSERVED).",
        "@s_fasano" to "[SNIFF]: SIGNAL DECOMPOSITION REPORT FOUND. WHITE NOISE CONTAINS COMPRESSED LANGUAGE. SOURCE: UNKNOWN.",
        "@b_bradley" to "[SNIFF]: DESK NOTE #11 RECOVERED. HANDWRITING MATCH: 94.7% SELF. CONTENT: WARNING. SOURCE OF REMAINING 5.3%: UNRESOLVED."
    )

    fun getSniffFeedback(handle: String): String {
        return sniffFeedback[handle] ?: "[SNIFF]: PARTIAL FRAGMENTS RECOVERED. NO COHERENT DATA. NOISE FLOOR TOO HIGH."
    }
    
    fun getLog(id: String): DataLog? {
        return allDataLogs.find { it.id == id }
    }
    
    fun getUnlockedLogs(unlockedIds: Set<String>): List<DataLog> {
        return allDataLogs.filter { unlockedIds.contains(it.id) }
    }
    
    fun getLogTitle(id: String): String {
        return allDataLogs.find { it.id == id }?.title ?: "Unknown Log"
    }
}
