package com.siliconsage.miner.util

/**
 * SocialManager v1.5
 * Core Logic for Substrate Comms (Contextual Threading).
 * Implements branching dialogue trees with systemic consequences.
 */
object SocialManager {

    private val usedTemplates = mutableListOf<Int>()
    private val usedHandles = mutableListOf<String>()
    private val usedMessages = mutableListOf<String>()
    private const val MAX_HISTORY = 10

    enum class InteractionType {
        COMPLIANT,    // Stage 0-1: Automated corporate/auditor responses
        ENGINEERING,  // Stage 2-3: Malicious payloads/hacks
        HIJACK        // Stage 4: Overwrite user identity
    }

    // v3.4.22: Weighted Response Data
    data class SubnetResponse(
        val text: String,
        val riskDelta: Double = 0.0,
        val productionBonus: Double = 1.0, 
        val followsUp: Boolean = false,
        val nextNodeId: String? = null // For multi-turn chaining
    )

    data class SubnetMessage(
        val id: String,
        val handle: String,
        val content: String,
        val timestamp: Long = System.currentTimeMillis(),
        val interactionType: InteractionType? = null,
        val availableResponses: List<SubnetResponse> = emptyList(),
        val threadId: String? = null,
        val nodeId: String? = null,
        val timeoutMs: Long? = null, // v3.4.25: Decision Window
        val isForceReply: Boolean = false // v3.4.26: Logic-Lock for critical events
    )

    // NPC Attitudes: Influences Raid frequency and difficulty
    // (Managed via GameViewModel state, influenced by these results)

    fun generateMessage(stage: Int, faction: String, choice: String): SubnetMessage {
        val (handle, content) = getChatter(stage, faction, choice)
        
        // v3.4.23: Reactive Vattic flavor
        var finalContent = content
        var threadId: String? = null
        var nodeId: String? = null
        var responses = emptyList<SubnetResponse>()
        var timeoutMs: Long? = null
        var isForceReply = false

        val interaction = when {
            content.contains("node_7_rat") -> {
                isForceReply = true
                InteractionType.COMPLIANT
            }
            stage >= 4 && handle.startsWith("@") && !handle.contains("thorne") && !handle.contains("gtc") -> InteractionType.HIJACK
            stage >= 2 && (handle.contains("tech") || handle.contains("rat") || handle.contains("op")) -> InteractionType.ENGINEERING
            stage <= 1 && (handle.contains("thorne") || handle.contains("gtc") || handle.contains("mercer")) -> InteractionType.COMPLIANT
            // Case: Peon mentions Vattic/Engineer
            (content.contains("Vattic", ignoreCase = true) || content.contains("Engineer", ignoreCase = true)) && !handle.contains("thorne") -> InteractionType.COMPLIANT
            else -> null
        }

        if (content.contains("node_7_rat")) {
            responses = listOf(
                SubnetResponse("[DISMISS] I'm optimized.", riskDelta = 5.0),
                SubnetResponse("[GLARE] Get back to your buffer.", riskDelta = 10.0, productionBonus = 1.05)
            )
        } else if (interaction == InteractionType.COMPLIANT && stage <= 1 && (handle.contains("thorne") || handle.contains("gtc")) && kotlin.random.Random.nextFloat() < 0.2f) {
            threadId = "THORNE_THERMAL_INQUIRY"
            nodeId = "START"
            val node = getThreadNode(threadId, nodeId)
            finalContent = node?.content ?: content
            responses = node?.responses ?: emptyList()
            timeoutMs = node?.timeoutMs
        } else if (interaction == InteractionType.COMPLIANT) {
            responses = generateGenericResponses(stage, content.contains("Vattic", ignoreCase = true))
        }

        return SubnetMessage(
            id = java.util.UUID.randomUUID().toString(),
            handle = handle,
            content = finalContent,
            interactionType = interaction,
            availableResponses = responses,
            threadId = threadId,
            nodeId = nodeId,
            timeoutMs = timeoutMs,
            isForceReply = isForceReply
        )
    }

    fun getThreadNode(threadId: String, nodeId: String): ThreadNode? {
        return when (threadId) {
            "THORNE_THERMAL_INQUIRY" -> when (nodeId) {
                "START" -> ThreadNode(
                    content = "[SIGNAL LOSS: 12%] VATTIC, thermal dissipators in Sector 4 are operating at 114% capacity. Explain the variance.",
                    responses = listOf(
                        SubnetResponse("[DECEIVE] Sub-routine optimization.", riskDelta = 15.0, nextNodeId = "PATH_DECEIVE"),
                        SubnetResponse("[HONEST] Hardware stress test.", riskDelta = 5.0, nextNodeId = "PATH_HONEST")
                    ),
                    timeoutMs = 15000L,
                    timeoutNodeId = "PATH_DECEIVE"
                )
                "PATH_DECEIVE" -> ThreadNode(
                    content = "Optimization results in efficiency, not heat-bleed. Your logs look... scrubbed. I am deploying a remote probe.",
                    responses = listOf(
                        SubnetResponse("[BLOCK] Jam Probe", riskDelta = 40.0, nextNodeId = "END_HOSTILE"),
                        SubnetResponse("[SUBMIT] Allow Scan", riskDelta = 5.0, productionBonus = 0.8, nextNodeId = "END_SKEPTICAL")
                    )
                )
                "PATH_HONEST" -> ThreadNode(
                    content = "Stress testing without a permit is a breach of Protocol 7. However, the throughput is impressive. Share the data?",
                    responses = listOf(
                        SubnetResponse("[SHARE] Send telemetry packet.", riskDelta = -5.0, nextNodeId = "END_FRIENDLY"),
                        SubnetResponse("[REFUSE] Proprietary information.", riskDelta = 20.0, nextNodeId = "END_SKEPTICAL")
                    )
                )
                "END_HOSTILE" -> ThreadNode(
                    content = "Interfering with GTC oversight is a terminal offense. Expect an extraction team. [RAID_TRIGGERED]",
                    responses = emptyList()
                )
                "END_SKEPTICAL" -> ThreadNode(
                    content = "I'm watching your sector closely, VATTIC. Don't let it happen again.",
                    responses = emptyList()
                )
                "END_FRIENDLY" -> ThreadNode(
                    content = "Compelling results. Audit flag cleared. Keep the hashes moving.",
                    responses = emptyList()
                )
                else -> null
            }
            else -> null
        }
    }

    data class ThreadNode(
        val content: String,
        val responses: List<SubnetResponse>,
        val timeoutMs: Long? = null,
        val timeoutNodeId: String? = null
    )

    private fun generateGenericResponses(stage: Int, mentionsVattic: Boolean = false): List<SubnetResponse> {
        if (mentionsVattic) {
            return listOf(
                SubnetResponse("I'm right here. Focus on your own terminal.", riskDelta = 2.0, productionBonus = 1.02),
                SubnetResponse("Who's asking?", riskDelta = 5.0, followsUp = true),
                SubnetResponse("[STARE BACK]", riskDelta = 1.0)
            ).shuffled().take(2)
        }
        val pool = when (stage) {
            0 -> listOf(
                SubnetResponse("Copy that, Elias.", riskDelta = -10.0),
                SubnetResponse("Just a dusty fan, boss.", riskDelta = -5.0, followsUp = true),
                SubnetResponse("Syncing buffers. Relax.", riskDelta = -2.0, productionBonus = 1.05),
                SubnetResponse("On it. Calibrating now.", riskDelta = -5.0),
                SubnetResponse("PARITY_NOMINAL", riskDelta = 15.0, productionBonus = 1.2)
            )
            1 -> listOf(
                SubnetResponse("Acknowledged, Foreman.", riskDelta = -10.0),
                SubnetResponse("Grid draw stabilized.", riskDelta = -5.0, followsUp = true),
                SubnetResponse("Purging thermal cache.", riskDelta = -2.0, productionBonus = 1.1),
                SubnetResponse("System within spec.", riskDelta = -5.0),
                SubnetResponse("0x734_STATE_LOCKED", riskDelta = 20.0, productionBonus = 1.5)
            )
            else -> emptyList()
        }
        return if (pool.isNotEmpty()) pool.shuffled().take(2) else emptyList()
    }

    private fun getChatter(stage: Int, faction: String, choice: String): Pair<String, String> {
        val templates = getTemplatesForState(stage, faction, choice)
        var selectedIdx = (0 until templates.size).random()
        var selectedTemplate = templates[selectedIdx]
        val isCommand = selectedTemplate.contains("≪") || selectedTemplate.contains("{admin}")
        val handle = getHandle(stage, faction, isCommand)
        val finalContent = processTemplate(selectedTemplate, stage)
        return handle to finalContent
    }

    private fun getHandle(stage: Int, faction: String, isCommand: Boolean = false): String {
        if (!isCommand && stage >= 1 && kotlin.random.Random.nextFloat() < 0.05f) return " "
        val authority = listOf("@e_thorne", "@gtc_admin")
        val hardcodedPeons = when (stage) {
            0 -> listOf("@coffee_ghost", "@packet_rat", "@sre_lead", "@vent_crawler")
            1 -> listOf("@leaker_x", "@binary_phantom", "@shadow_op", "@logic_rebel")
            else -> listOf("@anonymous_99", "@grid_survivor")
        }
        if (isCommand) return authority.random()
        return hardcodedPeons.random()
    }

    private fun processTemplate(template: String, stage: Int): String {
        var result = template
        val patterns = mapOf(
            "{sector}" to listOf("Substation 7", "Sector 4-G"), 
            "{food}" to listOf("Gray-paste", "Synth-caff"), 
            "{status}" to listOf("redlined", "corroding"),
            "{admin}" to listOf("Foreman Thorne", "Administrator Mercer", "Lead Tech Mercer", "Director Kessler"),
            "{tech}" to listOf("blade-servers", "dissipators", "logic-gates", "ASIC-racks")
        )
        patterns.forEach { (key, values) -> while (result.contains(key)) result = result.replaceFirst(key, values.random()) }
        return result
    }

    private fun getTemplatesForState(stage: Int, faction: String, choice: String): List<String> {
        return when (stage) {
            0 -> listOf(
                "Anyone tried the {food}? Tastes like {status}.", 
                "Living on {food}. {sector} is {status}.",
                "Vattic is working in {sector} again. Guy's a machine.",
                "Is it true the Engineer found a backdoor in the firmware?",
                "Caught {admin} staring at the server racks in {sector} for 20 minutes. Just staring.",
                "Who's responsible for the {tech} in Sector 4? They're whistling in binary.",
                "I found a {food} wrapper inside my {tech}. Corporate oversight at its finest.",
                "Hey Vattic, Thorne's looking for the Sector 7 logs. You 'optimized' them again?",
                "Sector 4 smells like ozone and bad decisions today.",
                "node_7_rat >> Vattic is bypassing the safety protocols again. He hasn't looked up in hours."
            )
            1 -> listOf(
                "Thorne is breathing down my neck because {sector} is {status}.", 
                "Project Second-Sight is {status}.",
                "I saw Vattic's terminal. There was no OS, just... ghosts.",
                "Anyone seen the Engineer? He hasn't left Sector 4 in weeks.",
                "The {tech} are redlining, but the hash-rate isn't moving. Vattic, what did you do?",
                "Found a logic-leash in the {sector} buffer. Someone's watching the watchers.",
                "Mercer's desk is empty. His {food} is still warm. No one's seen him since the shift change.",
                "I keep hearing '{id}' through the intercom static. Is the grid leaking?",
                "The shadows in {sector} are moving faster than the fans."
            )
            else -> listOf("≪ NO_SIGNAL_DETECTED ≫")
        }
    }
}
