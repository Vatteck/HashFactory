package com.siliconsage.miner.util

/**
 * NarrativeEvents — All event pools (data only).
 * Split into smaller files v3.11.2 for better context management.
 * 
 * Delegates to: RandomEvents, StageEvents, FactionEvents, DilemmaEvents
 */
object NarrativeEvents {
    val randomEvents = RandomEvents.randomEvents
    val stageEvents = StageEvents.stageEvents
    val factionEvents = FactionEvents.factionEvents
    val specialDilemmas = DilemmaEvents.specialDilemmas
}
