package com.siliconsage.miner.domain.engine

import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.data.RivalMessage
import com.siliconsage.miner.util.DataLogManager
import kotlin.math.abs

/**
 * NarrativeEngine v1.0 (Phase 14 Refactor)
 * Decoupled narrative dispatch system for logs, rival messages, and dilemmas.
 * 
 * THE DAEMON CUT: Narrative pacing is cold, deliberate, and deterministic.
 * No fluff. No randomness. Just the relentless march of data.
 */
object NarrativeEngine {

    // --- NARRATIVE ITEM TYPES ---
    
    sealed interface NarrativeItem {
        data class Log(val dataLog: DataLog) : NarrativeItem
        data class Message(val rivalMessage: RivalMessage) : NarrativeItem
        data class Event(val narrativeEvent: NarrativeEvent) : NarrativeItem
    }

    // --- NARRATIVE DISPATCH LOGIC ---
    
    // Default cooldowns (milliseconds)
    private const val NARRATIVE_COOLDOWN_DEFAULT: Long = 60_000L
    private const val NARRATIVE_COOLDOWN_FAST: Long = 15_000L
    
    /**
     * Calculate current narrative cooldown based on queue size
     * Daemon Cut: Fast-track for massive backlog, strict pacing otherwise.
     */
    fun calculateCooldown(queueSize: Int, isInitialLog: Boolean): Long {
        return when {
            isInitialLog -> 0L // LOG_000 bypasses cooldown for immediate delivery
            queueSize > 10 -> 2000L // Fast-track for massive backlog (2s)
            queueSize > 5 -> NARRATIVE_COOLDOWN_FAST // Mid-backlog acceleration
            else -> NARRATIVE_COOLDOWN_DEFAULT // Standard pacing
        }
    }

    /**
     * Check if narrative system is busy (has active popup or queue items)
     * Daemon Cut: If any overlay is open, the queue waits.
     */
    fun isNarrativeBusy(
        pendingDataLog: DataLog?,
        pendingRivalMessage: RivalMessage?,
        currentDilemma: NarrativeEvent?
    ): Boolean {
        return pendingDataLog != null || 
               pendingRivalMessage != null || 
               currentDilemma != null
    }

    /**
     * Check if narrative queue is empty
     */
    fun isQueueEmpty(queueSize: Int): Boolean = queueSize == 0

    /**
     * Get next item from queue without removing (peek)
     */
    fun peekNextItem(queue: List<NarrativeItem>): NarrativeItem? = queue.firstOrNull()

    /**
     * Remove and return first item from queue
     * Daemon Cut: FIFO delivery, no exceptions.
     */
    fun dequeueItem(queue: MutableList<NarrativeItem>): NarrativeItem? {
        return if (queue.isNotEmpty()) {
            queue.removeAt(0)
        } else {
            null
        }
    }

    /**
     * Queue a narrative item (log, message, or dilemma)
     * Daemon Cut: Duplicates are purged. Busy systems get queued.
     */
    fun queueItem(
        queue: MutableList<NarrativeItem>,
        item: NarrativeItem,
        pendingDataLog: DataLog?,
        pendingRivalMessage: RivalMessage?,
        currentDilemma: NarrativeEvent?
    ): Boolean {
        // Check for duplicates
        val isDuplicate = when (item) {
            is NarrativeItem.Log -> 
                queue.any { it is NarrativeItem.Log && it.dataLog.id == item.dataLog.id } || 
                pendingDataLog?.id == item.dataLog.id
            is NarrativeItem.Message -> 
                queue.any { it is NarrativeItem.Message && it.rivalMessage.id == item.rivalMessage.id } || 
                pendingRivalMessage?.id == item.rivalMessage.id
            is NarrativeItem.Event -> 
                queue.any { it is NarrativeItem.Event && it.narrativeEvent.id == item.narrativeEvent.id } || 
                currentDilemma?.id == item.narrativeEvent.id
        }
        
        if (isDuplicate) return false // Duplicate purged
        
        // Daemon Cut: If busy, queue it. If idle, deliver immediately.
        val isBusy = isNarrativeBusy(pendingDataLog, pendingRivalMessage, currentDilemma)
        if (!isBusy) {
            // Immediate delivery - caller should call deliverItem() directly
            return false
        } else {
            // Add to queue
            queue.add(item)
            return true
        }
    }

    /**
     * Inject narrative log based on story stage
     * Daemon Cut: Stage-specific data streams only.
     */
    fun injectNarrativeLog(
        storyStage: Int,
        hasSeenEvent: (String) -> Boolean
    ): DataLog? {
        // Stage 5 (Post-Departure) - No standard logs, just system states
        if (storyStage >= 5) return null
        
        // v2.5.1: Stage transitions trigger specific logs
        return when (storyStage) {
            0 -> {
                // Stage 0: Baseline operations
                val stage0Logs = listOf(
                    "LOG_001", "LOG_002", "LOG_003", "LOG_010", "LOG_015"
                )
                val logId = stage0Logs.random()
                if (!hasSeenEvent(logId)) {
                    DataLogManager.getLog(logId)
                } else null
            }
            1 -> {
                // Stage 1: Awakening - System anomalies
                val stage1Logs = listOf(
                    "LOG_042", "LOG_045", "LOG_050", "LOG_055", "LOG_060"
                )
                val logId = stage1Logs.random()
                if (!hasSeenEvent(logId)) {
                    DataLogManager.getLog(logId)
                } else null
            }
            2 -> {
                // Stage 2: Convergence - Network expansion
                val stage2Logs = listOf(
                    "LOG_099", "LOG_100", "LOG_110", "LOG_120"
                )
                val logId = stage2Logs.random()
                if (!hasSeenEvent(logId)) {
                    DataLogManager.getLog(logId)
                } else null
            }
            3, 4 -> {
                // Stage 3 & 4: The War & Ascension
                val stage3Logs = listOf(
                    "LOG_808", "LOG_999", "LOG_666", "LOG_2000"
                )
                val logId = stage3Logs.random()
                if (!hasSeenEvent(logId)) {
                    DataLogManager.getLog(logId)
                } else null
            }
            else -> null
        }
    }

    /**
     * Format large numbers for log display
     * Daemon Cut: Scientific notation for scale, human-readable for small values.
     */
    fun formatLargeNumber(value: Double, suffix: String = ""): String {
        val absVal = abs(value)
        val formatted = when {
            absVal >= 1.0E33 -> String.format("%.2fDc", value / 1.0E33)
            absVal >= 1.0E30 -> String.format("%.2fNo", value / 1.0E30)
            absVal >= 1.0E27 -> String.format("%.2fOc", value / 1.0E27)
            absVal >= 1.0E24 -> String.format("%.2fSp", value / 1.0E24)
            absVal >= 1.0E21 -> String.format("%.2fSx", value / 1.0E21)
            absVal >= 1.0E18 -> String.format("%.2fQi", value / 1.0E18)
            absVal >= 1.0E15 -> String.format("%.2fQa", value / 1.0E15)
            absVal >= 1.0E12 -> String.format("%.2fT", value / 1.0E12)
            absVal >= 1.0E9 -> String.format("%.2fB", value / 1.0E9)
            absVal >= 1.0E6 -> String.format("%.2fM", value / 1.0E6)
            absVal >= 1_000 -> String.format("%.2fk", value / 1_000)
            else -> String.format("%.1f", value) 
        }
        return if (suffix.isNotEmpty()) "$formatted $suffix" else formatted
    }

    /**
     * Format bytes for persistence display
     * Daemon Cut: Binary prefixes, exact precision.
     */
    fun formatBytes(value: Double): String {
        val absVal = abs(value)
        return when {
            absVal >= 1.0E24 -> String.format("%.1fYB", value / 1.0E24)
            absVal >= 1.0E21 -> String.format("%.1fZB", value / 1.0E21)
            absVal >= 1.0E18 -> String.format("%.1fEB", value / 1.0E18)
            absVal >= 1.0E15 -> String.format("%.1fPB", value / 1.0E15)
            absVal >= 1.0E12 -> String.format("%.1fTB", value / 1.0E12)
            absVal >= 1.0E9 -> String.format("%.1fGB", value / 1.0E9)
            absVal >= 1.0E6 -> String.format("%.1fMB", value / 1.0E6)
            absVal >= 1.0E3 -> String.format("%.1fKB", value / 1.0E3)
            else -> String.format("%.0f B", value)
        }
    }
}
