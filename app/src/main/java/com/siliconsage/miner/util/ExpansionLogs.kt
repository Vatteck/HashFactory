package com.siliconsage.miner.util

/**
 * ExpansionLogs — Aggregator for all data log collections.
 * Split into: CharacterDossierLogs, MemoryHallucinationLogs, EndgameLogs.
 */
object ExpansionLogs {
    val allDataLogs = CharacterDossierLogs.logs +
            MemoryHallucinationLogs.logs +
            EndgameLogs.logs
}
