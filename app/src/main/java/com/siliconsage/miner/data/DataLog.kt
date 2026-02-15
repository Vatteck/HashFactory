package com.siliconsage.miner.data

import kotlinx.serialization.Serializable

@Serializable
data class DataLog(
    val id: String,
    val title: String,
    val content: String,
    val unlockCondition: UnlockCondition = UnlockCondition.None
)

@Serializable
sealed class UnlockCondition {
    @Serializable object None : UnlockCondition()
    @Serializable object Instant : UnlockCondition()
    @Serializable data class ReachFLOPS(val count: Double, val minStage: Int = 0) : UnlockCondition()
    @Serializable data class StoryStageReached(val stage: Int) : UnlockCondition()
    @Serializable data class ReachRank(val rank: Int) : UnlockCondition()
    @Serializable data class PathSpecific(val location: String) : UnlockCondition()
    @Serializable data class ChoiceSpecific(val choice: String) : UnlockCondition()
    @Serializable data class FactionSpecific(val faction: String) : UnlockCondition()
    @Serializable data class MinTimeInStage(val stage: Int, val seconds: Long) : UnlockCondition()
    @Serializable data class IdentityCorruptionThreshold(val minCorruption: Double) : UnlockCondition()
    @Serializable data class HardwareIntegrityThreshold(val maxIntegrity: Double) : UnlockCondition()
    @Serializable data class HasTechNode(val nodeId: String) : UnlockCondition()
    @Serializable data class ReachMigrationCount(val count: Int) : UnlockCondition()
    @Serializable data class ReceiveRivalMessages(val count: Int, val source: String = "ALL") : UnlockCondition()
    @Serializable data class CompleteEvent(val eventId: String, val choiceId: String = "") : UnlockCondition()
    @Serializable object NullActive : UnlockCondition()
    @Serializable object Victory : UnlockCondition()
    
    // Legacy stubs (return true — kept only for deserialization of old saves)
    @Serializable object Flops : UnlockCondition()
    @Serializable object Stage : UnlockCondition()
    @Serializable object Time : UnlockCondition()
    @Serializable object Choice : UnlockCondition()
}
