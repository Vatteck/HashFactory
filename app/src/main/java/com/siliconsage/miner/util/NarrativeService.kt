package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.viewmodel.NarrativeItem
import com.siliconsage.miner.data.NarrativeChoice
import kotlinx.coroutines.flow.update

/**
 * NarrativeService v1.3 (Phase 14 extraction)
 */
object NarrativeService {

    private val dataLogUnlockInProgress = mutableSetOf<String>()

    fun unlockDataLog(logId: String, vm: GameViewModel) {
        if (vm.unlockedDataLogs.value.contains(logId)) return
        if (dataLogUnlockInProgress.contains(logId)) return
        
        dataLogUnlockInProgress.add(logId)
        vm.unlockedDataLogs.update { it + logId }
        
        DataLogManager.getLog(logId)?.let { log ->
            queueNarrativeItem(vm, NarrativeItem.Log(log))
        }
    }

    fun addRivalMessage(message: com.siliconsage.miner.data.RivalMessage, vm: GameViewModel) {
        vm.rivalMessages.update { it + message }
        queueNarrativeItem(vm, NarrativeItem.Message(message))
    }

    fun deliverItem(vm: GameViewModel, item: NarrativeItem) {
        when (item) {
            is NarrativeItem.Log -> {
                vm.pendingDataLog.value = item.dataLog
                vm.addLogPublic("[DATA]: Recovering fragment: ${item.dataLog.title}")
                SoundManager.play("data_recovered")
            }
            is NarrativeItem.Message -> {
                vm.pendingRivalMessage.value = item.rivalMessage
                vm.addLogPublic("[INCOMING MESSAGE FROM: ${item.rivalMessage.source.name}]")
                SoundManager.play("message_received")
            }
            is NarrativeItem.Event -> {
                vm.currentDilemma.value = item.narrativeEvent
                SoundManager.play("alert")
                HapticManager.vibrateClick()
            }
        }
        vm.markPopupShown()
        vm.checkPopupPause()
    }

    fun deliverNextNarrativeItem(vm: GameViewModel) {
        synchronized(vm.narrativeQueue) {
            if (vm.narrativeQueue.isEmpty()) return
            val item = vm.narrativeQueue.removeAt(0)
            deliverItem(vm, item)
            vm.isNarrativeSyncing.value = vm.narrativeQueue.isNotEmpty()
        }
    }

    fun queueNarrativeItem(vm: GameViewModel, item: NarrativeItem) {
        synchronized(vm.narrativeQueue) {
            val isDuplicate = NarrativeDispatcher.isDuplicate(
                item = item,
                queue = vm.narrativeQueue,
                pendingLog = vm.pendingDataLog.value,
                pendingMessage = vm.pendingRivalMessage.value,
                currentDilemma = vm.currentDilemma.value
            )
            
            if (!isDuplicate) {
                if (!vm.isNarrativeBusy()) {
                    deliverItem(vm, item)
                } else {
                    vm.narrativeQueue.add(item)
                    vm.isNarrativeSyncing.value = true
                }
            }
        }
    }

    fun selectChoice(vm: GameViewModel, choice: NarrativeChoice) {
        val currentEvent = vm.currentDilemma.value
        choice.effect(vm)
        vm.markPopupShown()
        
        currentEvent?.let { vm.markEventSeen(it.id) }
        
        if (choice.nextPartId != null) {
            val chainId = currentEvent?.chainId ?: "unknown_chain"
            vm.scheduleChainPart(chainId, choice.nextPartId, choice.nextPartDelayMs)
        }
        
        vm.currentDilemma.value = null
        vm.checkPopupPause()
        vm.addLogPublic("[DECISION]: Selected protocol: ${choice.text}")
        SoundManager.play("click")
    }
}
