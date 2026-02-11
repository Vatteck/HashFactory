package com.siliconsage.miner.util

import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.RivalMessage
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.viewmodel.NarrativeItem

/**
 * NarrativeDispatcher v1.2
 * Handles duplication checks for renamed NarrativeItem subclasses.
 */
object NarrativeDispatcher {

    fun isDuplicate(
        item: NarrativeItem,
        queue: List<NarrativeItem>,
        pendingLog: DataLog?,
        pendingMessage: RivalMessage?,
        currentDilemma: NarrativeEvent?
    ): Boolean {
        return when (item) {
            is NarrativeItem.LogItem -> queue.any { it is NarrativeItem.LogItem && it.dataLog.id == item.dataLog.id } || pendingLog?.id == item.dataLog.id
            is NarrativeItem.MessageItem -> queue.any { it is NarrativeItem.MessageItem && it.rivalMessage.id == item.rivalMessage.id } || pendingMessage?.id == item.rivalMessage.id
            is NarrativeItem.EventItem -> queue.any { it is NarrativeItem.EventItem && it.narrativeEvent.id == item.narrativeEvent.id } || currentDilemma?.id == item.narrativeEvent.id
        }
    }

    fun getCooldown(backlog: Int, isInitialLog: Boolean): Long {
        return when {
            isInitialLog -> 0L
            backlog > 10 -> 2000L
            backlog > 5 -> 15000L
            else -> 60000L
        }
    }
}
