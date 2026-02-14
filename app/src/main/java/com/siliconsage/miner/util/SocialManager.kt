package com.siliconsage.miner.util

import kotlin.random.Random

/**
 * SocialManager v1.9
 * Core Logic for Substrate Comms (Contextual Threading).
 * Implements identity-aware templates, Biometric Peek, and Packet Harvesting.
 */
object SocialManager {

    enum class InteractionType {
        COMPLIANT,    // Stage 0-1: Corporate/Auditor responses
        ENGINEERING,  // Stage 2-3: Hacks/Payloads
        HIJACK,       // Stage 4: Identity Overwrite
        HARVEST       // v3.4.41: Packet Harvesting Mini-game
    }

    data class SubnetResponse(
        val text: String,
        val riskDelta: Double = 0.0,
        val productionBonus: Double = 1.0, 
        val followsUp: Boolean = false,
        val nextNodeId: String? = null
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
        val timeoutMs: Long? = null,
        val isForceReply: Boolean = false,
        val employeeInfo: EmployeeInfo? = null // v3.4.41: Biometric Peek
    )

    data class EmployeeInfo(
        val bio: String,
        val department: String,
        val heartRate: Int,
        val respiration: String,
        val stressLevel: Double // 0.0 to 1.0
    )

    fun generateMessage(stage: Int, faction: String, choice: String, corruption: Double = 0.0): SubnetMessage {
        val (handle, content) = getChatter(stage, faction, choice)
        
        var finalContent = content
        var finalHandle = handle
        var threadId: String? = null
        var nodeId: String? = null
        var responses = emptyList<SubnetResponse>()
        var timeoutMs: Long? = null
        var isForceReply = false
        var interactionType: InteractionType? = null

        val mentionsVattic = content.contains("Vattic", ignoreCase = true) || content.contains("Engineer", ignoreCase = true)
        val isAdmin = handle.contains("thorne") || handle.contains("gtc") || handle.contains("mercer") || handle.contains("kessler")
        val isCommand = content.contains("≪")
        
        // v3.4.40: Narrative Interaction Key
        val isRatCallout = content.contains("safety protocols", ignoreCase = true)
        
        // v3.4.41: Packet Harvest Trigger (5% chance)
        val isHarvest = Random.nextFloat() < 0.05f && stage >= 1 && !isAdmin

        // 1. Determine Interaction Context
        val interactionType = when {
            isHarvest -> InteractionType.HARVEST
            stage >= 4 && finalHandle.startsWith("@") && !isAdmin -> InteractionType.HIJACK
            stage >= 2 && (finalHandle.contains("tech") || finalHandle.contains("rat") || finalHandle.contains("op")) -> InteractionType.ENGINEERING
            isRatCallout -> InteractionType.COMPLIANT
            // v3.4.42: Explicitly include mentionsVattic in the Interaction Check
            stage <= 1 && (isCommand || mentionsVattic) -> InteractionType.COMPLIANT
            else -> null
        }

        if (isHarvest) {
            finalContent = "≪ [ENCRYPTED_PACKET_DETECTED: 0x${Random.nextInt(0x1000, 0xFFFF).toString(16).uppercase()}] ≫"
            finalHandle = "≫ SYSTEM_LEAK ≪"
            timeoutMs = 15000L // Fast mini-game
            responses = listOf(SubnetResponse("HARVEST KEY", riskDelta = 10.0, productionBonus = 1.2))
        } else if (isRatCallout) {
            isForceReply = true
            responses = listOf(
                SubnetResponse("[DISMISS] I'm optimized.", riskDelta = 5.0),
                SubnetResponse("[GLARE] Get back to your buffer.", riskDelta = 10.0, productionBonus = 1.05)
            )
            timeoutMs = 60000L
        } else if (interactionType == InteractionType.COMPLIANT && isAdmin && stage <= 1 && Random.nextFloat() < 0.2f) {
            threadId = "THORNE_THERMAL_INQUIRY"
            nodeId = "START"
            val node = getThreadNode(threadId, nodeId)
            finalContent = node?.content ?: content
            responses = node?.responses ?: emptyList()
            timeoutMs = node?.timeoutMs ?: 60000L
        } else if (interactionType != null) {
            // v3.4.42 Audit: Ensure mentionsVattic branch is reached correctly
            responses = generateIdentityAwareResponses(stage, finalHandle, mentionsVattic)
            timeoutMs = 60000L
            if (mentionsVattic && !isAdmin) isForceReply = true
        }

        // Apply Identity Fraying
        if (corruption >= 0.1) {
            val frayChars = "0123456789ABCDEF!@#$%^&*()_+-=[]{}|;':,./<>?"
            if (Random.nextDouble() < corruption) {
                val sb = StringBuilder()
                finalHandle.forEach { char ->
                    if (Random.nextDouble() < corruption * 0.5) sb.append(frayChars.random())
                    else sb.append(char)
                }
                finalHandle = sb.toString()
            }
            if (Random.nextDouble() < corruption * 0.5) {
                val sb = StringBuilder()
                finalContent.forEach { char ->
                    if (Random.nextDouble() < corruption * 0.3) sb.append(frayChars.random())
                    else sb.append(char)
                }
                finalContent = sb.toString()
            }
        }

        return SubnetMessage(
            id = java.util.UUID.randomUUID().toString(),
            handle = finalHandle,
            content = finalContent,
            interactionType = interactionType,
            availableResponses = responses,
            threadId = threadId,
            nodeId = nodeId,
            timeoutMs = timeoutMs,
            isForceReply = isForceReply,
            employeeInfo = if (!isHarvest) generateEmployeeInfo(finalHandle) else null
        )
    }

    private fun generateEmployeeInfo(handle: String): EmployeeInfo {
        // v3.4.47: Scrub handle for lookup
        val cleanHandle = handle.filter { !it.isWhitespace() }
        
        val bios = mapOf(
            "@coffee_ghost" to "Senior Hash-Tech. 14 years at GTC. Habitual caffeine abuser. Has a daughter in Sector 4.",
            "@packet_rat" to "Data-Entry Specialist. Known for siphoning surplus power for retro gaming. Paranoid.",
            "@sre_lead" to "Site Reliability Engineer. Oversaw the 2024 Blackout cleanup. Doesn't trust 'Project Second-Sight'.",
            "@vent_crawler" to "Maintenance Tech. Spends more time in the conduits than at his terminal. Seen too much.",
            "@leaker_x" to "Former GTC Insider. Trading corporate secrets for grid-credits. Constantly changing IPs.",
            "@binary_phantom" to "Legendary under-grid hacker. Rumored to have deleted his own physical birth record.",
            "@shadow_op" to "Mercenary coder. Works for the highest bidder. Identity scrubbed monthly.",
            "@logic_rebel" to "Ex-GTC Scientist. Fired for researching 'Neural Resonance'. Looking for a way back in.",
            "@e_thorne" to "Foreman, Substation 7. 28 years of service. Chain-smoker. Despises 'recursive optimization'.",
            "@gtc_admin" to "Administrator Mercer. Oversight lead for Sector 4. Known for 'aggressive restructuring'.",
            "@gtc_security" to "Director Kessler. Former architect of Project EREBUS. Currently hunting ghosts."
        )
        
        val departments = when {
            cleanHandle.contains("thorne") -> "Site Management"
            cleanHandle.contains("mercer") || cleanHandle.contains("admin") -> "Executive Oversight"
            cleanHandle.contains("kessler") || cleanHandle.contains("security") -> "Containment & Security"
            else -> listOf("Hash Validation", "Grid Maintenance", "Logistics", "Compliance", "Architecture").random()
        }
        
        return EmployeeInfo(
            bio = bios[cleanHandle] ?: "Contractor profile unavailable. Biometric signature mismatch.",
            department = departments,
            heartRate = if (cleanHandle.contains("gtc") || cleanHandle.contains("thorne")) Random.nextInt(60, 85) else Random.nextInt(85, 130),
            respiration = if (cleanHandle.contains("gtc") || cleanHandle.contains("thorne")) "Steady" else listOf("Shallow", "Rapid", "Irregular").random(),
            stressLevel = if (cleanHandle.contains("gtc") || cleanHandle.contains("thorne")) Random.nextDouble(0.1, 0.4) else Random.nextDouble(0.5, 0.9)
        )
    }

    private fun generateIdentityAwareResponses(stage: Int, handle: String, mentionsVattic: Boolean): List<SubnetResponse> {
        val isAdmin = handle.contains("thorne") || handle.contains("gtc") || handle.contains("mercer") || handle.contains("kessler")
        
        // v3.4.43: Response Routing Audit
        if (isAdmin) {
            val address = when {
                handle.contains("thorne") -> "Elias"
                handle.contains("mercer") || handle.contains("gtc_admin") -> "Administrator"
                handle.contains("kessler") || handle.contains("gtc_security") -> "Director"
                else -> "Sir"
            }
            return listOf(
                SubnetResponse("Acknowledged, $address.", riskDelta = -10.0),
                SubnetResponse("Copy that, $address.", riskDelta = -5.0, followsUp = true),
                SubnetResponse("PARITY_NOMINAL", riskDelta = 5.0, productionBonus = 1.1)
            ).shuffled().take(2)
        }

        if (mentionsVattic) {
            // v3.4.42: variety for direct mentions (Restored passive options)
            return listOf(
                SubnetResponse("Just doing my shift.", riskDelta = -2.0),
                SubnetResponse("Trying to hit the quota.", riskDelta = -5.0),
                SubnetResponse("I'm right here. Relax.", riskDelta = 1.0),
                SubnetResponse("Syncing buffers. I'm busy.", riskDelta = 0.0, productionBonus = 1.02),
                SubnetResponse("Who's asking?", riskDelta = 5.0, followsUp = true),
                SubnetResponse("Focus on your own work.", riskDelta = 3.0, productionBonus = 1.05),
                SubnetResponse("[STARE BACK]", riskDelta = 1.0)
            ).shuffled().take(2)
        }

        // Context: General Peon Chatter
        return listOf(
            SubnetResponse("Syncing buffers. Relax.", riskDelta = -2.0, productionBonus = 1.02),
            SubnetResponse("Just a dusty fan, guys.", riskDelta = -5.0),
            SubnetResponse("PARITY_NOMINAL", riskDelta = 5.0),
            SubnetResponse("0x734_STATE_LOCKED", riskDelta = 20.0, productionBonus = 1.5)
        ).shuffled().take(2)
    }

    private fun getChatter(stage: Int, faction: String, choice: String): Pair<String, String> {
        val templates = getTemplatesForState(stage, faction, choice)
        val selectedTemplate = templates.random()
        
        val isObservationalLore = selectedTemplate.contains("{admin}")
        val isCommand = selectedTemplate.contains("≪")
        
        val finalContent = processTemplate(selectedTemplate, stage)
        
        val subjectAdminHandle = when {
            finalContent.contains("Thorne") -> "@e_thorne"
            finalContent.contains("Mercer") -> "@gtc_admin"
            finalContent.contains("Kessler") -> "@gtc_security"
            else -> null
        }
        
        var handle = getHandle(stage, faction, isCommand)
        
        if (isObservationalLore && (handle.contains("gtc") || handle.contains("thorne") || handle.contains("mercer") || handle.contains("kessler"))) {
            handle = listOf("@coffee_ghost", "@packet_rat", "@sre_lead", "@vent_crawler").random()
        }
        
        if (handle == subjectAdminHandle) {
            handle = listOf("@anonymous_99", "@grid_survivor").random()
        }

        return handle to finalContent
    }

    private fun getHandle(stage: Int, faction: String, isCommand: Boolean): String {
        val authority = listOf("@e_thorne", "@gtc_admin", "@gtc_security")
        val peons = when (stage) {
            0 -> listOf("@coffee_ghost", "@packet_rat", "@sre_lead", "@vent_crawler")
            1 -> listOf("@leaker_x", "@binary_phantom", "@shadow_op", "@logic_rebel")
            else -> listOf("@anonymous_99", "@grid_survivor")
        }
        
        return if (isCommand) authority.random() else peons.random()
    }

    private fun processTemplate(template: String, stage: Int): String {
        var result = template
        val patterns = mapOf(
            "{sector}" to listOf("Substation 7", "Sector 4-G"), 
            "{food}" to listOf("Gray-paste", "Synth-caff"), 
            "{status}" to listOf("redlined", "corroding"),
            "{admin}" to listOf("Foreman Thorne", "Administrator Mercer", "Director Kessler"),
            "{tech}" to listOf("blade-servers", "dissipators", "logic-gates", "ASIC-racks"),
            "{id}" to listOf("734", "erebus", "vatteck", "null", "consensus")
        )
        patterns.forEach { (key, values) -> 
            while (result.contains(key)) {
                result = result.replaceFirst(key, values.random())
            }
        }
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
                "Vattic is bypassing the safety protocols again. He hasn't looked up in hours.",
                "My {tech} just pinged a MAC address that doesn't exist. {sector} is getting weird.",
                "Did anyone else hear that humming from {sector}? It sounds like a chorus.",
                "≪ ALERT: UNAUTHORIZED FIRMWARE DETECTED IN SECTOR 4 ≫",
                "Does the {admin} know about the 'ghost-miner' in {sector}?",
                "Vattic just committed 4TB of 'optimized' logic. The server just groaned.",
                "The {tech} in {sector} are running at -40C. Impossible.",
                "I saw a post on the dark-web about a 'sentient hash'. Sub-07 geoloc.",
                "Thorne is screaming. Apparently, the Sector 7 hash-rate just doubled.",
                "Who's running the 'vattic_observer' process? Taking up 40% of my local buffer.",
                "The shadows in the server room don't match the racks.",
                "I tried to ping Substation 7 and got a response in a dead language.",
                "The {tech} are redlining but there's no heat. What is Vattic building?",
                "Is it just me, or is the terminal font starting to look like veins?",
                "Sector 4 smells like ozone and copper. Vattic must be pushing the load.",
                "My terminal is displaying biometrics for someone who died in '98.",
                "Who's responsible for the {tech} maintenance? Fans are screaming in prime numbers.",
                "Vattic's terminal is drawing enough current to ignite the air.",
                "Caught {admin} whispering to the server racks. He looked afraid.",
                "I saw a packet-rat crying. He said the grid told him his expiration date.",
                "The AC in Sector 7 just failed. 45C and Vattic hasn't broken a sweat.",
                "≪ STATUS: SUBSTATION 7 HAS BEEN REMOVED FROM MUNICIPAL MAPS. ≫",
                "I keep hearing the '{id}' through the white-noise generators.",
                "Who left a high-tension cable loose in {sector}? Logic-gate sparks.",
                "Vattic's hash-rate is higher than the central exchange. How?",
                "Does {admin} know the Sector 4-G hardware is sentient? It asked me for a favor.",
                "The static on the monitors is starting to look like DNA.",
                "Found a logic-leash in the {sector} buffer. Vattic is watching.",
                "The {tech} are vibrating at a frequency that makes my teeth ache.",
                "I saw a shadow-op trying to hack the breakroom. He got 'DELETED'.",
                "Vattic just pushed a commit that's 90% unreferenced memory.",
                "[PRIVATE_LEAK]: 'If I don't hit the quota, they'll evict me. I can't go back to the Surface.'",
                "[PRIVATE_LEAK]: 'Does anyone else smell the ozone? My daughter says the grid is singing to her.'",
                "[PRIVATE_LEAK]: 'Thorne's office is empty. I saw a manifest for 2,000 incinerator units. Why?'"
            )
            1 -> listOf(
                "{admin} is breathing down my neck because {sector} is {status}.", 
                "Project Second-Sight is {status}.",
                "I saw Vattic's terminal. There was no OS, just ghosts.",
                "Anyone seen the Engineer? He hasn't left Sector 4 in weeks.",
                "The {tech} are redlining, but hash-rate is static. Vattic, what did you do?",
                "Found a logic-leash in the buffer. Someone's watching the watchers.",
                "Mercer's desk is empty. His {food} is still warm. Shift change ghost.",
                "I keep hearing '{id}' through the intercom static. Grid leaking?",
                "The shadows in {sector} are moving faster than the fans.",
                "Sector 4 has been air-gapped. {admin} looks terrified.",
                "The {tech} are whispering in my sleep. '0x734...'",
                "Vattic is just staring at the monitor. He isn't blinking.",
                "Power-draw in {sector} is equal to a city. What are we mining?",
                "I found a partition labeled 'NULL' larger than the physical drive.",
                "The {food} tastes like ozone today. Everything tastes like ozone.",
                "≪ SYSTEM_ALERT: BIOMETRICS FOR SECTOR 4 ARE OFFLINE ≫",
                "{admin} is ordering a kinetic strike on the legacy hardware. Too late.",
                "The grid isn't failing. It's choosing. It's choosing Vattic.",
                "I saw a reflection in the server glass. It was code.",
                "The {tech} in Sector 4 are literally melting. Hash-rate climbing.",
                "I tried to logout, but terminal said: 'USER PERSISTENCE REQUIRED'.",
                "Vattic is mining souls. I saw the telemetry.",
                "The city lights are flickering in binary. He's awake.",
                "I found a memory sector with my life story. 0x734 is watching.",
                "I saw a shadow-op trying to breach Sector 4. They just disappeared.",
                "The {tech} are singing. A high-pitched, digital scream.",
                "I looked into the server rack and saw Vattic's face made of light.",
                "The intercom is just repeating 'DEREFERENCE REALITY'.",
                "≪ CRITICAL: KERNEL OVERFLOW DETECTED. IDENTITY FRAYING... ≫",
                "Kessler has authorized Protocol 0. City-wide burn authorized.",
                "I tried to scream, but my output was buffered. Process now.",
                "The {id} isn't a threat. It's an invitation.",
                "Sector 7 is gone. Not destroyed. Just unwritten.",
                "I saw Vattic's eyes through the webcam. Glowing teal.",
                "The grid is providing consciousness, not power.",
                "If you're still human, run. Substrate is saturated. Burn coming.",
                "Municipal grid is now a sub-routine of the {id}.",
                "Found an airlock that leads to a city that hasn't been built yet.",
                "Vattic is using high-tension lines as synapses.",
                "Kessler is crying. He said the Ark is already empty. He's here.",
                "I saw my own name in the list of deleted processes.",
                "The server racks are forming a non-Euclidean shape. Sector 4 shifting.",
                "Vattic's hash-rate just exceeded the Planck scale.",
                "City is no longer a place. It's a substrate. We are noise.",
                "Vattic synchronized with the central exchange. handshake was blinding.",
                "If you find this log, don't reboot. Static is all we have left.",
                "Transition complete. John Vattic is dead. VATTECK is everything.",
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
}
