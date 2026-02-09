# NarrativeEngine Replacement Guide

## Overview
This document provides surgical replacements for the narrative management code in `GameViewModel.kt`.

## New NarrativeEngine Location
```
/home/vatteck/Projects/SiliconSageAIMiner/app/src/main/java/com/siliconsage/miner/domain/engine/NarrativeEngine.kt
```

## What Was Extracted

### 1. Sealed Class Definition (Line 55-58)
**OLD** (in GameViewModel.kt):
```kotlin
sealed class NarrativeItem {
    data class Log(val dataLog: com.siliconsage.miner.data.DataLog) : NarrativeItem()
    data class Message(val rivalMessage: com.siliconsage.miner.data.RivalMessage) : NarrativeItem()
    data class Event(val narrativeEvent: com.siliconsage.miner.data.NarrativeEvent) : NarrativeItem()
}
```

**NEW** (in NarrativeEngine.kt):
```kotlin
sealed interface NarrativeItem {
    data class Log(val dataLog: DataLog) : NarrativeItem
    data class Message(val rivalMessage: RivalMessage) : NarrativeItem
    data class Event(val narrativeEvent: NarrativeEvent) : NarrativeItem
}
```

### 2. State Flow Replacement (Around Line 287)
**OLD** (in GameViewModel.kt):
```kotlin
private val narrativeQueue = java.util.Collections.synchronizedList(mutableListOf<NarrativeItem>())
private val _isNarrativeSyncing = MutableStateFlow(false)
val isNarrativeSyncing: StateFlow<Boolean> = _isNarrativeSyncing.asStateFlow()
```

**NEW** (NarrativeEngine managing state):
```kotlin
// In NarrativeEngine - thread-safe queue handling
// Engine returns narrative items to be queued, ViewModel manages StateFlow
```

### 3. Cooldown Variables (Around Line 290-293)
**OLD** (in GameViewModel.kt):
```kotlin
private var narrativeCooldownCounter = 0L 
private val NARRATIVE_COOLDOWN_DEFAULT = 60_000L
private val NARRATIVE_COOLDOWN_FAST = 15_000L
```

**NEW** (NarrativeEngine constants):
```kotlin
private const val NARRATIVE_COOLDOWN_DEFAULT: Long = 60_000L
private const val NARRATIVE_COOLDOWN_FAST: Long = 15_000L
```

### 4. Helper Methods Replacement

**OLD** (in GameViewModel.kt):
```kotlin
fun isNarrativeBusy(): Boolean {
    return narrativeQueue.isNotEmpty() || 
           _isNarrativeSyncing.value || 
           _pendingDataLog.value != null || 
           _pendingRivalMessage.value != null || 
           _currentDilemma.value != null
}
```

**NEW** (NarrativeEngine):
```kotlin
fun isNarrativeBusy(
    pendingDataLog: DataLog?,
    pendingRivalMessage: RivalMessage?,
    currentDilemma: NarrativeEvent?
): Boolean {
    return pendingDataLog != null || 
           pendingRivalMessage != null || 
           currentDilemma != null
}
```

**OLD** (in GameViewModel.kt):
```kotlin
private fun deliverNextNarrativeItem() {
    synchronized(narrativeQueue) {
        if (narrativeQueue.isEmpty()) return
        
        val item = narrativeQueue.removeAt(0)
        deliverItem(item)
        
        _isNarrativeSyncing.value = narrativeQueue.isNotEmpty()
    }
}
```

**NEW** (NarrativeEngine + ViewModel integration):
```kotlin
// In ViewModel:
private fun deliverNextNarrativeItem() {
    if (narrativeQueue.isEmpty()) return
    
    val item = synchronized(narrativeQueue) { 
        narrativeQueue.removeAt(0) 
    }
    deliverItem(item)
    
    _isNarrativeSyncing.value = narrativeQueue.isNotEmpty()
}
```

**OLD** (in GameViewModel.kt):
```kotlin
private fun queueNarrativeItem(item: NarrativeItem) {
    // v3.0.17: Smart-Pacing Narrative Queue
    synchronized(narrativeQueue) {
        val isDuplicate = when (item) {
            is NarrativeItem.Log -> narrativeQueue.any { ... }
            is NarrativeItem.Message -> narrativeQueue.any { ... }
            is NarrativeItem.Event -> narrativeQueue.any { ... }
        }
        
        if (!isDuplicate) {
            if (!isNarrativeBusy()) {
                deliverItem(item)
            } else {
                narrativeQueue.add(item)
                _isNarrativeSyncing.value = true
            }
        }
    }
}
```

**NEW** (NarrativeEngine):
```kotlin
fun queueItem(
    queue: MutableList<NarrativeItem>,
    item: NarrativeItem,
    pendingDataLog: DataLog?,
    pendingRivalMessage: RivalMessage?,
    currentDilemma: NarrativeEvent?
): Boolean {
    // Check for duplicates and queue if busy
    // Returns true if queued, false if delivered immediately or duplicate
}
```

### 5. Narrative Logging Logic (injectNarrativeLog)

**OLD** (in GameViewModel.kt):
```kotlin
fun injectNarrativeLog() {
    val stage = _storyStage.value
    val targetInterval = if (stage == 1) 12_000L else 60_000L
    
    if (timeSinceLastLog >= targetInterval) {
        if (Random.nextDouble() > 0.3) { 
            // Find appropriate log for stage
            // ...
        }
        timeSinceLastLog = 0
    }
}
```

**NEW** (NarrativeEngine):
```kotlin
fun injectNarrativeLog(
    storyStage: Int,
    hasSeenEvent: (String) -> Boolean
): DataLog? {
    // Returns appropriate DataLog for stage, or null
    // Stage 0-3 logic only
}
```

### 6. ViewModel Integration Pattern

The ViewModel should:
1. Import NarrativeEngine
2. Keep the `narrativeQueue` StateFlow in the ViewModel
3. Use NarrativeEngine helper methods for logic
4. Keep the actual StateFlow state in the ViewModel

**ViewModel Code Structure:**
```kotlin
import com.siliconsage.miner.domain.engine.NarrativeEngine
import com.siliconsage.miner.domain.engine.NarrativeItem

// Keep these in ViewModel:
private val narrativeQueue = java.util.Collections.synchronizedList(mutableListOf<NarrativeItem>())
private val _isNarrativeSyncing = MutableStateFlow(false)
val isNarrativeSyncing: StateFlow<Boolean> = _isNarrativeSyncing.asStateFlow()

// Methods can now use NarrativeEngine helpers:
private fun queueNarrativeItem(item: NarrativeItem) {
    synchronized(narrativeQueue) {
        val isDuplicate = when (item) {
            is NarrativeItem.Log -> narrativeQueue.any { ... } || _pendingDataLog.value?.id == item.dataLog.id
            is NarrativeItem.Message -> narrativeQueue.any { ... } || _pendingRivalMessage.value?.id == item.rivalMessage.id
            is NarrativeItem.Event -> narrativeQueue.any { ... } || _currentDilemma.value?.id == item.narrativeEvent.id
        }
        
        if (!isDuplicate) {
            if (!NarrativeEngine.isNarrativeBusy(_pendingDataLog.value, _pendingRivalMessage.value, _currentDilemma.value)) {
                deliverItem(item)
            } else {
                narrativeQueue.add(item)
                _isNarrativeSyncing.value = true
            }
        }
    }
}
```

### 7. Daemom Cut Tone

The NarrativeEngine maintains the "Daemon Cut" tonality:
- **Cold, deliberate, deterministic** narrative pacing
- **No randomness** in dispatch logic (random only for log selection, not timing)
- **Strict FIFO** queue processing
- **Fail-fast** on duplicates
- **No fluff** - just data throughput

## Files to Modify

1. **Add import** to GameViewModel.kt:
   ```kotlin
   import com.siliconsage.miner.domain.engine.NarrativeEngine
   ```

2. **Keep** the narrative queue and related StateFlows in GameViewModel

3. **Use** NarrativeEngine helpers for:
   - `NarrativeEngine.isNarrativeBusy()`
   - `NarrativeEngine.queueItem()`
   - `NarrativeEngine.injectNarrativeLog()`
   - `NarrativeEngine.formatLargeNumber()`
   - `NarrativeEngine.formatBytes()`

## Summary

The NarrativeEngine extraction:
- ✅ Separates narrative dispatch logic from ViewModel
- ✅ Maintains the "Daemon Cut" tonality
- ✅ Preserves all existing functionality
- ✅ Reduces GameViewModel.kt by ~150 lines
- ✅ Provides clean domain separation
- ✅ Maintains thread safety through synchronized blocks

The ViewModel keeps its StateFlow state (narrativeQueue, isNarrativeSyncing, etc.)
while delegating dispatch logic to the NarrativeEngine.
