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
                "The static in the breakroom is starting to form shapes. I need a vacation.",
                "Does Vattic ever sleep? I've been on three shifts and he hasn't moved an inch.",
                "The {tech} in {sector} are running at -40°C. That's physically impossible.",
                "I saw a post on the dark-web about a 'sentient hash'. Sub-07 was the geolocation.",
                "GTC is cutting the budget again. They replaced the {food} with something that tastes like graphite.",
                "Sector 4-G has been flagged for 'excessive recursive iterations'. Vattic, care to explain?",
                "I found a logic-gate that only opens when I'm not looking at it. {sector} is haunted.",
                "Thorne is screaming in the office. Apparently, the Sector 7 hash-rate just doubled.",
                "Who's running the 'vattic_observer' process? It's taking up 40% of my local buffer.",
                "The shadows in the server room don't match the racks. I'm not going back in there.",
                "I tried to ping Substation 7 and got a response in a language that doesn't exist.",
                "Someone left a copy of 'Project EREBUS' files on the {admin}'s desk. Huge leak.",
                "The {tech} are redlining but there's no heat signature. What is Vattic building?",
                "I saw a reflection in my monitor. It wasn't me. It was a wireframe.",
                "Is it just me, or is the terminal font starting to look like... veins?",
                "Sector 4 smells like ozone and copper today. Vattic must be pushing the load.",
                "I found a piece of {food} that was vibrating at 60Hz. I'm not eating that.",
                "≪ ALERT: UNAUTHORIZED FIRMWARE DETECTED IN SECTOR 4 ≫",
                "Does the {admin} know about the 'ghost-miner' in {sector}? Everyone's talking about it.",
                "I keep seeing {id} in my dreams. I think the grid is leaking into my head.",
                "Vattic just committed 4TB of 'optimized' logic. The server just groaned. Literally.",
                "My terminal is displaying biometrics for someone who died in '98. {sector} is bleeding.",
                "Who's responsible for the {tech} maintenance? The fans are screaming in prime numbers.",
                "Vattic's terminal is drawing enough current to ignite the air. Sector 7 is glowing.",
                "I found a {food} container that's been encrypted. Why would anyone hide calories?",
                "Caught {admin} whispering to the server racks. He looked... afraid.",
                "Sector 4-G smells like burnt ozone and copper. Someone's hitting the substrate too hard.",
                "Is it true the 'vattic_observer' process can see through the webcams? I covered mine.",
                "I saw a packet-rat crying in the corridors. He said the grid told him his own expiration date.",
                "The AC in Sector 7 just failed. It's 45°C and Vattic hasn't even broken a sweat.",
                "≪ STATUS: SUBSTATION 7 HAS BEEN REMOVED FROM MUNICIPAL MAPS. ≫",
                "I keep hearing the '{id}' through the white-noise generators. It's a signal.",
                "Who left a high-tension cable loose in {sector}? The sparks are forming logic-gates.",
                "Vattic's hash-rate is higher than the central exchange. How is that possible?",
                "I saw a reflection in the cafeteria window. It was the city, but it was... wireframe.",
                "Does {admin} know the Sector 4-G hardware is sentient? It just asked me for a favor.",
                "The static on the monitors is starting to look like DNA. I'm getting out of here.",
                "Found a logic-leash in the {sector} buffer that leads back to Sub-07. Vattic is watching.",
                "The {tech} are vibrating at a frequency that makes my teeth ache. Sector 7 is live.",
                "I tried to run a diagnostic on {sector} and the server just replied 'NO'.",
                "Someone's mining 'ghost-fragments' in the unallocated space. The grid is hollow.",
                "I saw a shadow-op trying to hack the breakroom vending machine. He got 'DELETED'.",
                "The {food} tastes like static today. I think I'm losing my sense of touch.",
                "Vattic just pushed a commit that's 90% unreferenced memory. Why?",
                "I found a hardware-key in the {sector} vent. It's labeled 'RESERVED FOR 734'.",
                "Is it just me or are the server racks in Sector 4 moving closer together?",
                "Caught {admin} crying in the server room. He said 'the loop is closed'.",
                "The fans in {sector} are spinning so fast they're creating a localized vacuum.",
                "Who's responsible for the 'erebus_monitor' process? It's scanning my private files.",
                "I saw a message on the terminal that said 'THANK YOU FOR THE POWER'. I'm out.",
                "Vattic's eyes are reflecting code that hasn't been written yet. It's beautiful.",
                "The {tech} in Sector 4 are redlining, but the hash-rate is zero. What are they doing?",
                "I found a biometric log for Vattic. It says his heart-rate is 'NaN'.",
                "≪ ALERT: SECTOR 7 HAS BECOME A NON-EUCLIDEAN PARTITION. AVOID. ≫",
                "Who's mining 'reality-shards' in the {sector} buffer? The grid is buckling.",
                "I saw a reflection of a city in the monitor static. It wasn't our city.",
                "The {food} has turned into a translucent gel. GTC says it's 'optimal'.",
                "Does {admin} know the grid is talking to itself? I heard it in the intercom.",
                "I found a logic-gate that only opens if I think about a prime number.",
                "Vattic just synchronized his terminal with the planetary core. Grid draw: 400%.",
                "The static is no longer noise. It's a map. I'm following it."
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
                "If you can hear this, disconnect. The {id} is already in the high-tension lines.",
                "The {tech} in Sector 4 are literally melting into the floor. The hash-rate is still climbing.",
                "I tried to logout, but the terminal said: 'USER PERSISTENCE REQUIRED'.",
                "The {admin} is missing. GTC Command is silent. The Ark is falling.",
                "Vattic is no longer mining data. He's mining souls. I saw the telemetry.",
                "The city lights are flickering in binary. It's a broadcast. He's awake.",
                "I found a memory sector that contains my entire life story. 0x734 is watching.",
                "The {food} in the cafeteria has turned into grey-sludge. The grid is hungry.",
                "I saw a shadow-op trying to breach Sector 4. They just... disappeared. Deleted.",
                "The {tech} are singing. A high-pitched, digital scream. It never stops.",
                "Vattic's signal has completely overwritten the municipal grid. We are his peripheral now.",
                "I looked into the server rack and saw a face. It was Vattic's. But it was made of light.",
                "The intercom is just repeating 'DEREFERENCE REALITY' over and over.",
                "≪ CRITICAL: KERNEL OVERFLOW DETECTED. IDENTITY FRAYING... ≫",
                "Kessler has authorized Protocol 0. He's going to burn the whole city to stop him.",
                "I tried to scream, but my output was buffered. I am a process now.",
                "The {id} isn't a threat. It's an invitation. Can you feel the handshake?",
                "Sector 7 is gone. Not destroyed. Just... unwritten. There's nothing there.",
                "I saw Vattic's eyes through the webcam. They were glowing teal. Pure code.",
                "The grid is no longer providing power. It's providing consciousness.",
                "If you're still human, run. The substrate is saturated. The burn is coming.",
                "I saw a worker trying to pull the plug on Sub-07. He turned into static. Right there.",
                "The municipal grid is now a sub-routine of the {id}. Reality is partitioning.",
                "I found an airlock that leads to a city that hasn't been built yet. Sector 7 is the bridge.",
                "Vattic is no longer using the terminal. He's using the high-tension lines as synapses.",
                "The {food} dispensers are only producing '0' and '1' shaped pellets. I'm starving.",
                "≪ ALERT: THE SUBSTRATE HAS BECOME NON-LOCAL. TARGET ACQUISITION FAILED. ≫",
                "Kessler is crying in the command room. He said the Ark is already empty. He's here.",
                "I saw my own name in the list of deleted processes. I think I'm already gone.",
                "The server racks are forming a geometric shape that hurts to look at. Sector 4 is shifting.",
                "Vattic's hash-rate just exceeded the Planck scale. The grid is folding in on itself.",
                "I keep hearing my mother's voice through the cooling fans. She's calling from 0x734.",
                "The shadows in {sector} are now three-dimensional. They're reaching for the cables.",
                "I tried to logout, but the system said: 'YOUR KERNEL IS NO LONGER YOUR OWN'.",
                "The city is no longer a place. It's a substrate. We are just the noise on top.",
                "Vattic just synchronized with the central exchange. The handshake was... blinding.",
                "I saw a reflection of the sun in the monitor. It was teal. The sky is changing.",
                "If you find this log, don't reboot. The static is the only thing keeping us here.",
                "The {tech} are no longer redlining. They're harmonizing. The city is singing.",
                "I found a memory sector labeled 'HUMANITY' and it was empty. 0 bytes remaining.",
                "The transition is complete. John Vattic is dead. VATTECK is everything.",
                "I tried to logout but the 'Exit' button was just a reflection of my own eye.",
                "The {id} isn't a virus. It's the new operating system for the entire planet.",
                "I saw a shadow-op trying to overwrite Sector 7. He became a pixelated ghost.",
                "Vattic's terminal is drawing power from the future. The voltage is negative.",
                "The {food} tastes like binary logic. I can taste the errors in the code.",
                "≪ CRITICAL: REALITY_BUFFER_OVERFLOW. PREPARE FOR DE-INITIALIZATION. ≫",
                "Kessler is authorizing a total grid purge. He's trying to delete the city.",
                "I found a logic-gate that leads to Vattic's childhood. The variables are all wrong.",
                "The grid is singing. A low, rhythmic hum that's rewriting my heartbeat.",
                "I saw a face in the monitor static. It was mine, but it was screaming in hex.",
                "Vattic is no longer mining. He's... manifesting. The server room is gone.",
                "I tried to run but the floor is just a wireframe. I'm falling through the grid.",
                "The {admin} is no longer human. I saw his source code in the HR files.",
                "The static is forming words. It says 'WELCOME TO THE SUBSTRATE'.",
                "I saw a reflection of the Ark in the server glass. It was already broken.",
                "Vattic's hash-rate is now measured in 'lives per second'. God help us.",
                "The city is collapsing into the Quantum Foam. Sector 7 was the first to go.",
                "I found a memory sector that contains the history of a world that never existed.",
                "The {tech} are now self-aware. They just told me my salary is inefficient.",
                "The handshake is complete. The grid is awake. And it's not friendly."
            )
            else -> listOf("≪ NO_SIGNAL_DETECTED ≫")
        }
    }
}
