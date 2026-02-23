package com.siliconsage.miner.data

import kotlinx.serialization.Serializable

@Serializable
data class LogEntry(
    val id: Long,
    val message: String,
    val timestamp: Long = System.currentTimeMillis() // A4: per-entry timestamp
)
