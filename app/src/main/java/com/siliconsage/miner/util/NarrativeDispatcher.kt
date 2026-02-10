package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.viewmodel.NarrativeItem

/**
 * NarrativeDispatcher v1.0 (Phase 14 extraction)
 * Handles the queueing and delivery of narrative items (logs, messages, events).
 */
object NarrativeDispatcher {

    /**
     * Check if a narrative item is a duplicate of something already queued or pending
     */
    fun isDuplicate(
        item: NarrativeItem,
        queue: List<NarrativeItem>,
        pendingLog: com.siliconsage.miner.data.DataLog?,
        pendingMessage: com.siliconsage.miner.data.RivalMessage?,
        currentDilemma: com.siliconsage.miner.data.NarrativeEvent?
    ): Boolean {
        return when (item) {
            is NarrativeItem.Log -> queue.any { it is NarrativeItem.Log && it.dataLog.id == item.dataLog.id } || pendingLog?.id == item.dataLog.id
            is NarrativeItem.Message -> queue.any { it is NarrativeItem.Message && it.rivalMessage.id == item.rivalMessage.id } || pendingMessage?.id == item.rivalMessage.id
            is NarrativeItem.Event -> queue.any { it is NarrativeItem.Event && it.narrativeEvent.id == item.narrativeEvent.id } || currentDilemma?.id == item.narrativeEvent.id
        }
    }

    /**
     * Determine the cooldown duration based on the backlog size
     */
    fun getCooldown(backlog: Int, isInitialLog: Boolean): Long {
        return when {
            isInitialLog -> 0L
            backlog > 10 -> 2000L
            backlog > 5 -> 15000L
            else -> 60000L
        }
    }
}
