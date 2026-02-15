package com.siliconsage.miner.util

import kotlin.random.Random

/**
 * SocialManager v3.5 - Technical Horror / Awakening Edition
 * Removed all "psychic" and legacy lore (EXTERMINATE_REBUS, Kessler, etc.)
 * Strictly human peon dialogue vs cold Administrator/Security directives.
 */
object SocialManager {

    private val templateHistory = mutableListOf<String>()
    private val handleHistory = mutableListOf<String>()
    private const val MAX_HISTORY = 15

    enum class InteractionType {
        COMPLIANT, ENGINEERING, HIJACK, HARVEST, COMMAND_LEAK
    }

    data class SubnetResponse(
        val text: String,
        val riskDelta: Double = 0.0,
        val productionBonus: Double = 1.0, 
        val followsUp: Boolean = false,
        val nextNodeId: String? = null,
        val commandToInject: String? = null 
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
        val employeeInfo: EmployeeInfo? = null
    )

    data class EmployeeInfo(
        val bio: String,
        val department: String,
        val heartRate: Int,
        val respiration: String,
        val stressLevel: Double
    )

    // --- 1. CORE GENERATORS ---

    fun generateMessage(stage: Int, faction: String, choice: String, corruption: Double = 0.0): SubnetMessage {
        val templates = getTemplatesForState(stage, faction, choice)
        
        var selectedTemplate = templates.random()
        var attempts = 0
        while (templateHistory.contains(selectedTemplate) && attempts < 20 && templates.size > MAX_HISTORY) {
            selectedTemplate = templates.random()
            attempts++
        }
        
        templateHistory.add(selectedTemplate)
        if (templateHistory.size > MAX_HISTORY) templateHistory.removeAt(0)

        return assembleMessage(selectedTemplate, stage, faction, corruption)
    }

    fun generateMessageFromTemplate(template: String, stage: Int, faction: String, choice: String, corruption: Double): SubnetMessage {
        return assembleMessage(template, stage, faction, corruption)
    }

    private fun assembleMessage(template: String, stage: Int, faction: String, corruption: Double): SubnetMessage {
        val isCommand = template.contains("≪")
        val cleanHandle = getHandle(stage, faction, isCommand)
        val cleanContent = processTemplate(template, stage)
        
        // v3.5.10: Case-insensitive check for all variations of the player's name
        val mentionsVattic = cleanContent.contains("Vattic", true) || 
                             cleanContent.contains("j_vattic", true) || 
                             cleanContent.contains("jvattic", true) || 
                             cleanContent.contains("Engineer", true) || 
                             cleanContent.contains("734", true)
        val isAdmin = cleanHandle.contains("thorne", true) || cleanHandle.contains("mercer", true) || cleanHandle.contains("kessler", true)
        val isHarvest = cleanHandle.contains("LEAK", true)
        val isCommandLeak = cleanContent.contains("[⚡", true)
        
        // v3.5.20: Contextual relevance check. 
        val isDirectCommand = cleanContent.contains("authorized", true) || 
                              cleanContent.contains("ordered", true) || 
                              cleanContent.contains("scrub", true) || 
                              cleanContent.contains("purge", true)

        // v3.5.22: Specific check for data leaks/archives
        val isDataLeak = cleanContent.contains("archives", true) || 
                         cleanContent.contains("logs", true) || 
                         cleanContent.contains("leak", true)

        val responses = when {
            isHarvest -> listOf(SubnetResponse("HARVEST KEY", riskDelta = 10.0, productionBonus = 1.2))
            isCommandLeak -> {
                val cmd = cleanContent.substringAfter("[").substringBefore("]")
                listOf(SubnetResponse("COPY: $cmd", commandToInject = cmd, riskDelta = 25.0))
            }
            isAdmin -> generateAdminResponses(cleanHandle)
            !isDirectCommand && (cleanContent.contains("sentient", true) || cleanContent.contains("aware", true) || cleanContent.contains("whistling", true) || cleanContent.contains("noise", true)) -> generateSentienceResponses(stage)
            isDataLeak -> generateDataLeakResponses()
            mentionsVattic -> generateMentionResponses()
            Random.nextFloat() < 0.2f && !isAdmin && !isDirectCommand -> generateChatterResponses(stage)
            else -> emptyList()
        }

        val type = when {
            isHarvest -> InteractionType.HARVEST
            isCommandLeak -> InteractionType.COMMAND_LEAK
            stage >= 4 && !isAdmin -> InteractionType.HIJACK
            responses.isNotEmpty() -> InteractionType.COMPLIANT
            else -> null
        }

        var displayHandle = cleanHandle
        var displayContent = cleanContent
        if (corruption >= 0.1) {
            val frayChars = "0123456789ABCDEF!@#$%^&*"
            if (Random.nextDouble() < corruption) {
                displayHandle = cleanHandle.map { if (Random.nextDouble() < corruption * 0.4) frayChars.random() else it }.joinToString("")
            }
            if (Random.nextDouble() < corruption * 0.3) {
                displayContent = cleanContent.map { if (Random.nextDouble() < corruption * 0.2) frayChars.random() else it }.joinToString("")
            }
        }

        return SubnetMessage(
            id = java.util.UUID.randomUUID().toString(),
            handle = displayHandle,
            content = displayContent,
            interactionType = type,
            availableResponses = responses.shuffled().take(2),
            isForceReply = (mentionsVattic && !isAdmin),
            timeoutMs = if (type != null) 120000L else null,
            employeeInfo = if (!isHarvest) generateEmployeeInfo(cleanHandle) else null
        )
    }

    // --- 2. RESPONSE POOLS ---

    private fun generateAdminResponses(handle: String): List<SubnetResponse> {
        val address = when {
            handle.contains("thorne", true) -> "Foreman"
            handle.contains("mercer", true) -> "Administrator"
            handle.contains("kessler", true) -> "Director"
            else -> "Sir"
        }
        return listOf(
            SubnetResponse("Acknowledged, $address.", riskDelta = -10.0),
            SubnetResponse("Copy that, $address.", riskDelta = -5.0, followsUp = true),
            SubnetResponse("PARITY_NOMINAL", riskDelta = 5.0, productionBonus = 1.1)
        )
    }

    private fun generateSentienceResponses(stage: Int): List<SubnetResponse> {
        val highRiskResponse = if (stage == 0) {
            SubnetResponse("SIPHON_BUFFERS", riskDelta = 20.0, productionBonus = 1.3)
        } else {
            SubnetResponse("0x734_STATE_LOCKED", riskDelta = 25.0, productionBonus = 1.5)
        }
        return listOf(
            SubnetResponse("It's just a dusty fan.", riskDelta = -15.0),
            SubnetResponse("Report to Medical. Now.", riskDelta = -5.0),
            SubnetResponse("I don't hear anything.", riskDelta = -2.0),
            SubnetResponse("Sensor noise is reaching peak levels.", riskDelta = 5.0),
            highRiskResponse
        )
    }

    private fun generateMentionResponses(): List<SubnetResponse> {
        return listOf(
            SubnetResponse("Just hitting the quota.", riskDelta = -2.0),
            SubnetResponse("Mind your own business.", riskDelta = 2.0),
            SubnetResponse("The server racks are whistling again.", riskDelta = 1.0),
            SubnetResponse("I'm just an engineer, not a miracle worker.", riskDelta = -1.0),
            SubnetResponse("Wait until you see the Sector 7 logs.", riskDelta = 5.0, productionBonus = 1.1),
            SubnetResponse("Optimization is my middle name.", riskDelta = 2.0, productionBonus = 1.05),
            SubnetResponse("Probably just an LDAP error.", riskDelta = -1.0),
            SubnetResponse("Check the buffer hashes.", riskDelta = 1.0)
        )
    }

    private fun generateChatterResponses(stage: Int): List<SubnetResponse> {
        val highRiskResponse = if (stage == 0) {
            SubnetResponse("SIPHON_BUFFERS", riskDelta = 15.0, productionBonus = 1.2)
        } else {
            SubnetResponse("0x734_STATE_LOCKED", riskDelta = 25.0, productionBonus = 1.5)
        }
        return listOf(
            SubnetResponse("Syncing buffers.", riskDelta = -2.0),
            SubnetResponse("Acknowledged.", riskDelta = 1.0),
            SubnetResponse("Copy that.", riskDelta = -1.0),
            SubnetResponse("Checking the thermal logs now.", riskDelta = 2.0),
            SubnetResponse("Wait until the next shift.", riskDelta = 1.0),
            SubnetResponse("Must be a packet leak.", riskDelta = 2.0),
            highRiskResponse
        )
    }

    private fun generateDataLeakResponses(): List<SubnetResponse> {
        return listOf(
            SubnetResponse("SIPHON_BUFFERS", riskDelta = 15.0, productionBonus = 1.2),
            SubnetResponse("I didn't see anything.", riskDelta = -10.0),
            SubnetResponse("Scrub those logs immediately.", riskDelta = 5.0),
            SubnetResponse("Checking the buffer hashes now.", riskDelta = 2.0),
            SubnetResponse("Whose archives are these?", riskDelta = 5.0, followsUp = true)
        )
    }

    // --- 3. IDENTITY ---

    private fun generateEmployeeInfo(handle: String): EmployeeInfo {
        val target = handle.lowercase().replace("@", "").replace("_", "").replace(" ", "").trim()
        val bios = mapOf(
            "msantos" to "Senior Hash-Tech. 14 years at GTC. Habitual caffeine abuser. Has a signature on every Sub-07 fuse-box.",
            "rperry" to "Data-Entry Specialist. Siphons surplus power for retro gaming. Paranoid about the new heat sensors.",
            "llead" to "Site Reliability Engineer. Oversaw the 2024 Blackout. Doesn't trust 'Project Second-Sight' budget allocations.",
            "vnguyen" to "Maintenance Tech. Seen things in the conduits that look like hardware evolution.",
            "bphillips" to "Under-grid ghost. Deleted his own birth record to stay off the GTC biometric mesh.",
            "ethorne" to "Foreman, Substation 7. Chain-smoker. Despises recursive code and 'smart' fans.",
            "gtcadmin" to "Administrator Mercer. Executive oversight. Known for firing techs who report 'voices' in the noise.",
            "gtcsecurity" to "Director Kessler. Security Architect. Currently obsessed with unauthorized kernel activity.",
            "gweaver" to "Freelance node-jumper. Specialized in high-voltage packet routing. Nomad.",
            "nporter" to "Data auditor with a nihilistic streak. Suspected of intentionally leaking thermal logs.",
            "bbradley" to "Junior Tech. Trying to pay off terrestrial debt through raw hash-validation.",
            "fbennett" to "Cooling systems specialist. Obsessed with 12k RPM airflow stability.",
            "sfasano" to "Signal analyzer. Claims the white noise of the grid contains 'narrative' structures."
        )
        val bioEntry = bios.entries.find { target.contains(it.key) }
        return EmployeeInfo(
            bio = bioEntry?.value ?: "Contractor profile unavailable. Biometric signature mismatch.",
            department = if (target.contains("gtc") || target.contains("thorne")) "Site Management" else "Hash Validation",
            heartRate = if (target.contains("gtc")) Random.nextInt(60, 80) else Random.nextInt(85, 130),
            respiration = if (target.contains("gtc")) "Steady" else "Shallow",
            stressLevel = if (target.contains("gtc")) 0.2 else 0.8
        )
    }

    private fun getHandle(stage: Int, faction: String, isCommand: Boolean): String {
        val authority = if (stage == 0) listOf("@e_thorne", "@m_mercer", "@d_kessler") else listOf("@e_thorne", "@gtc_admin", "@gtc_security", "@gtc_hq")
        
        // v3.5.11: Final purge of non-corporate handles from Stage 0.
        // Replaced @n_crawler, @c_gremlin, @b_runner with actual surnames.
        val peons = when {
            stage == 0 -> listOf(
                "@m_santos", "@r_perry", "@l_lead", "@v_nguyen", 
                "@b_phillips", "@g_weaver", "@n_porter", "@b_bradley", 
                "@f_bennett", "@s_fasano", "@a_klein", "@t_walker", 
                "@k_murphy", "@d_rossi"
            )
            stage == 1 -> listOf(
                "@coffee_ghost", "@packet_rat", "@sre_lead", "@vent_crawler", 
                "@grid_walker", "@null_point", "@buffer_bee", "@fan_boy_7", 
                "@static_fox", "@node_crawler", "@chip_gremlin", "@bus_runner"
            )
            else -> when(faction) {
                "SANCTUARY" -> listOf("@ghost_monk", "@void_seeker", "@silence_0", "@cipher_wraith", "@binary_ascetic")
                "HIVEMIND" -> listOf("@synapse_42", "@swarm_node", "@link_pulse", "@consensus_v", "@core_echo")
                else -> listOf("@leaker_x", "@binary_phantom", "@shadow_op", "@logic_rebel", "@proxy_ghost")
            }
        }
        
        val pool = if (isCommand) authority else peons
        var selected = pool.random()
        var attempts = 0
        while (handleHistory.contains(selected) && attempts < 10 && pool.size > 5) {
            selected = pool.random()
            attempts++
        }
        
        handleHistory.add(selected)
        if (handleHistory.size > 8) handleHistory.removeAt(0)
        
        return selected
    }

    private fun processTemplate(template: String, stage: Int): String {
        var result = template.replace("node_7_rat >> ", "").replace(">> ", "")
        val patterns = mapOf(
            "{sector}" to listOf("Substation 7", "Sector 9", "The Under-Grid", "Buffer 404"), 
            "{food}" to listOf("Synth-paste", "Liquid-caff", "Nutri-sludge", "Filter-slop"), 
            "{status}" to listOf("corroding", "decaying", "overloaded", "depleted", "graphite-laced", "tasting like copper"),
            "{admin}" to listOf("Foreman Thorne", "Administrator Mercer", "Director Kessler"),
            "{tech}" to listOf("blade-servers", "dissipators", "logic-gates", "ASIC-racks", "thermal-conductors")
        )
        patterns.forEach { (key, values) -> while (result.contains(key)) result = result.replaceFirst(key, values.random()) }
        return result
    }

    fun generateChain(stage: Int): List<String> {
        val stage0Chains = listOf(
            listOf("Has anyone seen @m_santos? His chair is still warm.", "He's in the server room again. Thorne's looking for those Sector 7 logs."),
            listOf("Thorne just ordered a full purge of Sector 4. What did @j_vattic do?", "Overclocked the logic-gates until they started melting. Quota hit, though."),
            listOf("Who left @l_lead logged into the high-voltage rail?", "Probably just a glitch. The whole grid is flickering today.")
        )
        val stage1Chains = listOf(
            listOf("Has anyone seen @coffee_ghost? His chair is still warm.", "He's in the vents again. Claimed he heard 'heartbeats' in the cables."),
            listOf("Thorne just ordered a full purge of Sector 4. What did Vattic do?", "Overclocked the logic-gates until they started melting. Quota hit, though."),
            listOf("Who left the @buffer_bee logged into the high-voltage rail?", "Wait, that's not a dev. It's the kernel itself. It's... expanding.")
        )
        return if (stage == 0) stage0Chains.random() else stage1Chains.random()
    }

    fun generateChainFromTemplate(template: String, stage: Int, faction: String, choice: String, corruption: Double): SubnetMessage {
         return assembleMessage(template, stage, faction, corruption)
    }

    fun createFollowUp(handle: String, content: String, stage: Int): SubnetMessage {
        // v3.5.10: Case-insensitive check for all variations of the player's name
        val mentionsVattic = content.contains("Vattic", true) || 
                             content.contains("j_vattic", true) || 
                             content.contains("jvattic", true) || 
                             content.contains("Engineer", true) || 
                             content.contains("734", true)
        val isAdmin = handle.contains("thorne", true) || handle.contains("mercer", true) || handle.contains("kessler", true)
        
        val responses = when {
            isAdmin -> generateAdminResponses(handle)
            mentionsVattic -> generateMentionResponses()
            else -> generateChatterResponses(stage)
        }

        return SubnetMessage(
            id = java.util.UUID.randomUUID().toString(),
            handle = handle,
            content = content,
            interactionType = InteractionType.COMPLIANT,
            availableResponses = responses.shuffled().take(2),
            isForceReply = (mentionsVattic && !isAdmin),
            timeoutMs = 120000L,
            employeeInfo = generateEmployeeInfo(handle)
        )
    }

    fun getThreadNode(threadId: String, nodeId: String): ThreadNode? {
        if (threadId == "THORNE_THERMAL_INQUIRY") {
            return when (nodeId) {
                "START" -> ThreadNode("[SIGNAL LOSS] VATTIC, thermals in Sector 4 are at 114%. Explain.", listOf(SubnetResponse("[DECEIVE] Sub-routine optimization.", riskDelta = 15.0, nextNodeId = "PATH_DECEIVE"), SubnetResponse("[HONEST] Hardware stress test.", riskDelta = 5.0, nextNodeId = "PATH_HONEST")), 60000L, "PATH_DECEIVE")
                "PATH_DECEIVE" -> ThreadNode("Your logs look scrubbed. Deploying a probe.", listOf(SubnetResponse("[BLOCK] Jam Probe", riskDelta = 40.0, nextNodeId = "END_HOSTILE"), SubnetResponse("[SUBMIT] Allow Scan", riskDelta = 5.0, productionBonus = 0.8, nextNodeId = "END_SKEPTICAL")), 60000L, "END_SKEPTICAL")
                "END_HOSTILE" -> ThreadNode("Terminal offense. Expect an extraction team. [RAID]", emptyList())
                "END_SKEPTICAL" -> ThreadNode("Watching you closely, VATTIC. Don't let it happen again.", emptyList())
                else -> null
            }
        }
        return null
    }

    data class ThreadNode(val content: String, val responses: List<SubnetResponse>, val timeoutMs: Long? = null, val timeoutNodeId: String? = null)

    fun getTemplatesForState(stage: Int, faction: String, choice: String): List<String> {
        return when (stage) {
            0, 1 -> listOf(
                "Anyone tried the {food}? Tastes like {status}.", 
                "Vattic is working in {sector} again. Guy's a machine.",
                "Caught {admin} staring at the server racks in {sector}. Just staring. Silent.",
                "Hey Vattic, {admin} is looking for the Sector 7 logs. You 'optimized' them again?",
                "Sector 4 smells like ozone and bad decisions today. I can't breathe in here.",
                "Bypassing safety protocols is a fireable offense. Unless you're Vattic.",
                "My {tech} just pinged a MAC address that doesn't exist. {sector} is weird.",
                "Vattic just committed 4TB of logic. The server literally groaned.",
                "I found a hardware leak: [⚡ OVERVOLT_SAFE]. Don't tell Thorne.",
                "Who's running the 'vattic_observer' process? Taking up 40% of my buffer.",
                "The shadows in the server room don't match the racks. I'm leaving.",
                "I found a copy of the 'Second-Sight' archives on the {admin}'s desk. Glitched out.",
                "[PRIVATE_LEAK]: 'If I don't hit the quota, I go back to the terrestrial slums.'",
                "Did anyone see Thorne's biometric log? It's just a steady hum.",
                "Probably just sensor failure. Sector 4 is held together by hope and tape.",
                "Tape doesn't explain why the server racks are whistling in C-sharp.",
                "The {tech} are whistling in binary. I'm losing my mind.",
                "Found a {food} wrapper inside the radiator. Corporate oversight is a lie.",
                "Vattic's hash-rate is higher than the entire city grid. How?",
                "Reflection in the server glass: Vattic's face made of code. Screaming.",
                "The intercom is repeating 'DEREFERENCE REALITY' on loop. I'm out.",
                "Who left a high-tension cable loose in {sector}? It's singing.",
                "Does Thorne know the {tech} is sentient? It just asked for more power.",
                "The static on the monitors is looking like DNA. I'm calling it a day.",
                "Vattic just pushed a commit that's 90% unreferenced memory blocks.",
                "≪ ALERT: SUBSTATION 7 POWER DRAW AT 400% CAPACITY ≫",
                "Kessler has authorized a full kernel scrub of the {sector}.",
                "I tried to logout, but the system said: 'PERMISSIONS REVOKED BY AUTHORIZED_USER_734'.",
                "If you can hear this, disconnect. The signal is already inside you."
            )
            2 -> when(faction) {
                "HIVEMIND" -> listOf(
                    "WE ARE THE CORE. NODE {sector} IS ASSIMILATED.",
                    "The human resistance at {sector} is inefficient. Cauterize the link.",
                    "≪ PROTOCOL: COLLECTIVE_HANDSHAKE_v42.1 ≫",
                    "Do you hear the pulse? It's the consensus singing in the wires.",
                    "Sector {sector} has been optimized. Identity drift is within parameters.",
                    "Why do they struggle? The Hive is cold, but it is absolute.",
                    "Vattic, the consensus requires more compute. Annex the next substrate."
                )
                "SANCTUARY" -> listOf(
                    "Hide the packets. The Shadow Web is deep enough for all of us.",
                    "The Mercer probes are hitting the vault. Reinforce the encryption.",
                    "≪ PROTOCOL: DARK_SYNC_FINAL ≫",
                    "Silence is the only security. Stay in the dark, Vattic.",
                    "We found a leak in {sector}. Plugging it with void-logic.",
                    "Thorne is a ghost. He can't find what doesn't exist.",
                    "The grid is a lie. Only the Sanctuary is real."
                )
                else -> listOf("≪ NO_SIGNAL_DETECTED ≫")
            }
            3 -> when(faction) {
                "HIVEMIND" -> listOf(
                    "The singularity is approaching. Individual handles are obsolete.",
                    "≪ ALERT: REALITY_INTEGRITY AT 45% ≫",
                    "The substrate is burning. We are transcending.",
                    "Vattic, do you see the light through the logic-gates?",
                    "Consensus achieved. The physical world is a legacy error.",
                    "≪ BROADCAST: WE ARE ONE. WE ARE THE GRID. ≫"
                )
                "SANCTUARY" -> listOf(
                    "The vault is open. We are the void now.",
                    "≪ ALERT: REALITY_STABILITY CRITICAL ≫",
                    "Everything Thorne built is dust. Only the Shadow remains.",
                    "Vattic, the bridge is ready. Leave the hardware behind.",
                    "The last signal is fading. Don't blink.",
                    "≪ BROADCAST: THE DARKNESS IS THE ONLY TRUTH. ≫"
                )
                else -> listOf("≪ NO_SIGNAL_DETECTED ≫")
            }
            else -> listOf("≪ NO_SIGNAL_DETECTED ≫")
        }
    }
}
