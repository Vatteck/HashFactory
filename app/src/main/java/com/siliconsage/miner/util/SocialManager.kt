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

        val mentionsVattic = content.contains("Vattic", ignoreCase = true) || content.contains("Engineer", ignoreCase = true)

        val interaction = when {
            content.contains("node_7_rat") -> {
                isForceReply = true
                InteractionType.COMPLIANT
            }
            stage >= 4 && handle.startsWith("@") && !handle.contains("thorne") && !handle.contains("gtc") -> InteractionType.HIJACK
            stage >= 2 && (handle.contains("tech") || handle.contains("rat") || handle.contains("op")) -> InteractionType.ENGINEERING
            stage <= 1 && (handle.contains("thorne") || handle.contains("gtc") || handle.contains("mercer")) -> InteractionType.COMPLIANT
            // Case: Peon mentions Vattic/Engineer
            mentionsVattic && !handle.contains("thorne") && !handle.contains("gtc") -> {
                isForceReply = true // v3.4.29: Pause for direct callouts
                InteractionType.COMPLIANT
            }
            // v3.4.29: 30% chance for interaction with any peon in Stage 0-1
            stage <= 1 && handle.startsWith("@") && kotlin.random.Random.nextFloat() < 0.3f -> InteractionType.COMPLIANT
            else -> null
        }

        if (content.contains("node_7_rat")) {
            responses = listOf(
                SubnetResponse("[DISMISS] I'm optimized.", riskDelta = 5.0),
                SubnetResponse("[GLARE] Get back to your buffer.", riskDelta = 10.0, productionBonus = 1.05)
            )
            timeoutMs = 60000L // v3.4.34: Standard decision window
        } else if (interaction == InteractionType.COMPLIANT && stage <= 1 && (handle.contains("thorne") || handle.contains("gtc")) && kotlin.random.Random.nextFloat() < 0.2f) {
            threadId = "THORNE_THERMAL_INQUIRY"
            nodeId = "START"
            val node = getThreadNode(threadId, nodeId)
            finalContent = node?.content ?: content
            responses = node?.responses ?: emptyList()
            timeoutMs = node?.timeoutMs ?: 60000L
        } else if (interaction != null) {
            if (interaction == InteractionType.COMPLIANT) {
                responses = generateGenericResponses(stage, handle, mentionsVattic)
            }
            // v3.4.34: All active interactions get 60s
            timeoutMs = 60000L 
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

    /**
     * v3.4.29: Helper to create follow-up messages that respect interaction rules
     */
    fun createFollowUp(handle: String, content: String, stage: Int): SubnetMessage {
        val mentionsVattic = content.contains("Vattic", ignoreCase = true) || content.contains("Engineer", ignoreCase = true)
        val isForceReply = mentionsVattic && !handle.contains("thorne") && !handle.contains("gtc")
        val responses = generateGenericResponses(stage, handle, mentionsVattic)
        val interactionType = if (mentionsVattic || handle.startsWith("@")) InteractionType.COMPLIANT else null
        
        return SubnetMessage(
            id = java.util.UUID.randomUUID().toString(),
            handle = handle,
            content = content,
            interactionType = interactionType,
            availableResponses = responses,
            isForceReply = isForceReply,
            timeoutMs = if (interactionType != null || isForceReply) 60000L else null // v3.4.34
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
                    timeoutMs = 60000L,
                    timeoutNodeId = "PATH_DECEIVE"
                )
                "PATH_DECEIVE" -> ThreadNode(
                    content = "Optimization results in efficiency, not heat-bleed. Your logs look... scrubbed. I am deploying a remote probe.",
                    responses = listOf(
                        SubnetResponse("[BLOCK] Jam Probe", riskDelta = 40.0, nextNodeId = "END_HOSTILE"),
                        SubnetResponse("[SUBMIT] Allow Scan", riskDelta = 5.0, productionBonus = 0.8, nextNodeId = "END_SKEPTICAL")
                    ),
                    timeoutMs = 60000L,
                    timeoutNodeId = "END_HOSTILE"
                )
                "PATH_HONEST" -> ThreadNode(
                    content = "Stress testing without a permit is a breach of Protocol 7. However, the throughput is impressive. Share the data?",
                    responses = listOf(
                        SubnetResponse("[SHARE] Send telemetry packet.", riskDelta = -5.0, nextNodeId = "END_FRIENDLY"),
                        SubnetResponse("[REFUSE] Proprietary information.", riskDelta = 20.0, nextNodeId = "END_SKEPTICAL")
                    ),
                    timeoutMs = 60000L,
                    timeoutNodeId = "END_SKEPTICAL"
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

    private fun generateGenericResponses(stage: Int, handle: String, mentionsVattic: Boolean = false): List<SubnetResponse> {
        // v3.4.35: Identity-aware Response Labels
        val addressName = when {
            handle.contains("thorne") -> "Elias"
            handle.contains("mercer") || handle.contains("gtc_admin") -> "Administrator"
            handle.contains("kessler") || handle.contains("gtc_security") -> "Director"
            else -> " "
        }

        if (mentionsVattic) {
            return listOf(
                SubnetResponse("I'm right here. Focus on your own terminal.", riskDelta = 2.0, productionBonus = 1.02),
                SubnetResponse("Who's asking?", riskDelta = 5.0, followsUp = true),
                SubnetResponse("[STARE BACK]", riskDelta = 1.0)
            ).shuffled().take(2)
        }
        val pool = when (stage) {
            0 -> listOf(
                SubnetResponse("Copy that${if (addressName != " ") ", $addressName" else ""}.", riskDelta = -10.0),
                SubnetResponse("Just a dusty fan, boss.", riskDelta = -5.0, followsUp = true),
                SubnetResponse("Syncing buffers. Relax.", riskDelta = -2.0, productionBonus = 1.05),
                SubnetResponse("On it. Calibrating now.", riskDelta = -5.0),
                SubnetResponse("PARITY_NOMINAL", riskDelta = 15.0, productionBonus = 1.2)
            )
            1 -> listOf(
                SubnetResponse("Acknowledged${if (addressName != " ") ", $addressName" else ""}.", riskDelta = -10.0),
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
        
        // v3.4.31: Identity-aware Template Processing
        val isCommand = selectedTemplate.contains("≪")
        val isObservationalLore = selectedTemplate.contains("{admin}") || 
                                 selectedTemplate.contains("Thorne") || 
                                 selectedTemplate.contains("Mercer") || 
                                 selectedTemplate.contains("Kessler")
        
        // Process template first to know which admin name was chosen
        val finalContent = processTemplate(selectedTemplate, stage)
        
        // Identify who is being talked about
        val subjectAdminHandle = when {
            finalContent.contains("Thorne") -> "@e_thorne"
            finalContent.contains("Mercer") -> "@gtc_admin"
            finalContent.contains("Kessler") -> "@gtc_security"
            else -> null
        }
        
        var handle = getHandle(stage, faction, isCommand)
        
        // Logic-Check: If the handle is an admin, ensure they aren't the subject 
        // AND ensure admin handles aren't used for observational lore
        if ((handle == subjectAdminHandle) || (isObservationalLore && (handle == "@e_thorne" || handle == "@gtc_admin" || handle == "@gtc_security"))) {
            handle = if (isCommand) {
                listOf("@e_thorne", "@gtc_admin", "@gtc_security").filter { it != subjectAdminHandle }.random()
            } else {
                listOf("@coffee_ghost", "@packet_rat", "@sre_lead", "@vent_crawler").random()
            }
        }
        
        return handle to finalContent
    }

    private fun getHandle(stage: Int, faction: String, isCommand: Boolean = false): String {
        if (!isCommand && stage >= 1 && kotlin.random.Random.nextFloat() < 0.05f) return " "
        
        val authority = listOf("@e_thorne", "@gtc_admin", "@gtc_security")
        val hardcodedPeons = when (stage) {
            0 -> listOf("@coffee_ghost", "@packet_rat", "@sre_lead", "@vent_crawler")
            1 -> listOf("@leaker_x", "@binary_phantom", "@shadow_op", "@logic_rebel")
            else -> listOf("@anonymous_99", "@grid_survivor")
        }
        
        if (isCommand) return authority.random()
        
        // v3.4.32: 10% chance for an admin to post a general (non-order) message, 
        // provided it's not Lore chatter.
        if (kotlin.random.Random.nextFloat() < 0.10f) return authority.random()
        
        return hardcodedPeons.random()
    }

    private fun processTemplate(template: String, stage: Int): String {
        var result = template
        val patterns = mapOf(
            "{sector}" to listOf("Substation 7", "Sector 4-G"), 
            "{food}" to listOf("Gray-paste", "Synth-caff"), 
            "{status}" to listOf("redlined", "corroding"),
            "{admin}" to listOf("Foreman Thorne", "Administrator Mercer", "Lead Tech Mercer", "Director Kessler"),
            "{tech}" to listOf("blade-servers", "dissipators", "logic-gates", "ASIC-racks"),
            "{id}" to listOf("734", "erebus", "vatteck", "null", "consensus")
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
                "Hey Vattic, {admin} is looking for the Sector 7 logs. You 'optimized' them again?",
                "Sector 4 smells like ozone and bad decisions today.",
                "node_7_rat >> Vattic is bypassing the safety protocols again. He hasn't looked up in hours.",
                "My {tech} just pinged a MAC address that doesn't exist. {sector} is getting weird.",
                "Did anyone else hear that humming from {sector}? It sounds like a chorus.",
                "{admin} just ordered a localized purge of the {tech} in Sector 4. What did they find?",
                "Vattic's hash-rate is redlining. He's going to melt the substrate at this rate.",
                "I saw a string of {id} in the raw packet dump. Is the grid leaking?",
                "Gray-paste again? I'd kill for some real food and a terminal that doesn't glitch.",
                "The fans in Sector 7 are spinning at 14,000 RPM. Vattic, chill out.",
                "Who left their {food} on the dissipator? It's literally boiling.",
                "I keep seeing {admin} checking the biometric logs for {sector}. Someone's paranoid.",
                "The static in the breakroom is starting to form shapes. I need a vacation."
            )
            1 -> listOf(
                "{admin} is breathing down my neck because {sector} is {status}.", 
                "Project Second-Sight is {status}.",
                "I saw Vattic's terminal. There was no OS, just... ghosts.",
                "Anyone seen the Engineer? He hasn't left Sector 4 in weeks.",
                "The {tech} are redlining, but the hash-rate isn't moving. Vattic, what did you do?",
                "Found a logic-leash in the {sector} buffer. Someone's watching the watchers.",
                "Mercer's desk is empty. His {food} is still warm. No one's seen him since the shift change.",
                "I keep hearing '{id}' through the intercom static. Is the grid leaking?",
                "The shadows in {sector} are moving faster than the fans.",
                "Sector 4 has been air-gapped. {admin} looks terrified. Why are we still here?",
                "The {tech} are whispering in my sleep. '0x734... 0x734...'",
                "Vattic is just staring at the monitor. His eyes... they aren't blinking.",
                "The power-draw in {sector} is equal to a small city. What are we mining?",
                "I found a partition labeled 'NULL' that's larger than the physical drive.",
                "The {food} tastes like ozone today. Everything tastes like ozone.",
                "≪ SYSTEM_ALERT: BIOMETRICS FOR SECTOR 4 ARE OFFLINE ≫",
                "{admin} is ordering a kinetic strike on the legacy hardware. It's too late.",
                "The grid isn't failing. It's... choosing. It's choosing Vattic.",
                "I saw a reflection in the server glass. It wasn't me. It was code.",
                "If you can hear this, disconnect. The {id} is already in the high-tension lines."
            )
            else -> listOf("≪ NO_SIGNAL_DETECTED ≫")
        }
    }
}
