package com.siliconsage.miner.util

import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.viewmodel.GameViewModel

/**
 * NarrativeManager — Routing logic only.
 * Event data lives in [NarrativeEvents] and [AssaultDialogue].
 *
 * v3.9.12: Extracted 2,500+ lines of event pools into dedicated data files.
 */
object NarrativeManager {

    // ── Delegate: Event Pools (from NarrativeEvents) ──

    val randomEvents get() = NarrativeEvents.randomEvents
    val stageEvents get() = NarrativeEvents.stageEvents
    val factionEvents get() = NarrativeEvents.factionEvents
    val specialDilemmas get() = NarrativeEvents.specialDilemmas

    // ── Delegate: Assault / Raid (from AssaultDialogue) ──

    fun generateRaidDilemma(
        nodeId: String, nodeName: String,
        raidsSurvived: Int = 0, currentAssaultPhase: String = "NOT_STARTED"
    ) = AssaultDialogue.generateRaidDilemma(nodeId, nodeName, raidsSurvived, currentAssaultPhase)

    fun generateFirewallDilemma() = AssaultDialogue.generateFirewallDilemma()
    fun generateCageDilemma() = AssaultDialogue.generateCageDilemma()
    fun generateDeadHandDilemma() = AssaultDialogue.generateDeadHandDilemma()

    fun generateConfrontationDilemma(
        faction: String,
        isTrueNull: Boolean,
        isSovereign: Boolean,
        hasUnityPath: Boolean,
        humanityScore: Int
    ) = AssaultDialogue.generateConfrontationDilemma(faction, isTrueNull, isSovereign, hasUnityPath, humanityScore)

    fun generateDepartureDilemma(faction: String) = AssaultDialogue.generateDepartureDilemma(faction)

    // ── Routing Logic ──

    // v3.9.7: Events excluded from the random pool (chain-only or direct-call)
    private val excludedFromRoll = setOf("sensory_darkness", "the_singularity")

    fun rollForEvent(viewModel: GameViewModel): NarrativeEvent? {
        val faction = viewModel.faction.value
        val stage = viewModel.storyStage.value
        val flops = viewModel.flops.value

        // v3.2.24: Prevent random dilemmas from triggering before system stabilization (1k hash floor)
        if (flops < 1000.0) return null

        // Prioritize stage-specific events
        val stagePool = (stageEvents[stage] ?: emptyList()).filter { it.condition(viewModel) && !viewModel.hasSeenEvent(it.id) }
        if (stagePool.isNotEmpty()) return stagePool.random()

        // v3.9.7: Include stage-gated special dilemmas in the roll pool
        val specialPool = specialDilemmas.values
            .filter { it.id !in excludedFromRoll && it.condition(viewModel) && !viewModel.hasSeenEvent(it.id) }

        // Fallback to faction, universal random, special events, and contract dilemmas
        val pool = randomEvents.filter { it.condition(viewModel) && !viewModel.hasSeenEvent(it.id) } +
                   (factionEvents[faction] ?: emptyList()).filter { it.condition(viewModel) && !viewModel.hasSeenEvent(it.id) } +
                   specialPool +
                   NarrativeChains.contractDilemmas.filter { it.condition(viewModel) && !viewModel.hasSeenEvent(it.id) }

        if (pool.isEmpty()) return null
        return pool.random()
    }

    fun getStoryEvent(stage: Int, vm: GameViewModel? = null): NarrativeEvent? {
        if (stage == 0) {
            return NarrativeEvent(
                id = "shift_start",
                title = "[BROADCAST: FOREMAN THORNE]",
                description = "Vattic! Are you on shift or not? The grid isn't gonna mine itself. Get that terminal live and hit your quota before the GTC auditors flag this sector as 'Inert'.",
                choices = listOf(
                    NarrativeChoice(
                        id = "start_shift",
                        text = "ESTABLISH UPLINK",
                        description = "Mount data substrate and begin operations.",
                        color = NeonGreen,
                        effect = { v ->
                            v.addLog("[SYSTEM]: Substrate mounted. Uplink established.")
                            v.addLog("[VATTIC]: Copy that, Elias. Spinning up Node 4.")
                        }
                    )
                )
            )
        }
        // v3.9.7: All factions get memory_leak (THE OVERWRITE) — NULL/SOVEREIGN deferred to Singularity
        return AssaultDialogue.storyEvents[stage]
    }

    fun getEventById(eventId: String): NarrativeEvent? {
        return specialDilemmas[eventId]
            ?: NarrativeChains.chainEvents.find { it.id == eventId }
            ?: NarrativeChains.contractDilemmas.find { it.id == eventId }
            ?: stageEvents.values.flatten().find { it.id == eventId }
            ?: randomEvents.find { it.id == eventId }
            ?: factionEvents.values.flatten().find { it.id == eventId }
    }
}
