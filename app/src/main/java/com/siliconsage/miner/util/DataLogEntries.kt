package com.siliconsage.miner.util

/**
 * DataLogEntries — All data log definitions (lore collectibles).
 * Split into smaller files v3.11.2 for better context management.
 * 
 * Delegates to: CoreLogs, ExpansionLogs
 */
object DataLogEntries {
    val allDataLogs = CoreLogs.allDataLogs + ExpansionLogs.allDataLogs
}
