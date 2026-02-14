package com.siliconsage.miner.util

import kotlin.random.Random
import android.util.Log

/**
 * SocialManager v2.7 - Hardened Anti-Repetition Edition
 * Unified message generation with history tracking for handles and templates.
 * Expanded peon pool for high-fidelity social noise.
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
        
        // v3.4.62: Anti-Repetition Filter
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
        
        val mentionsVattic = cleanContent.contains("Vattic", true) || cleanContent.contains("Engineer", true)
        val isAdmin = cleanHandle.contains("thorne", true) || cleanHandle.contains("gtc", true) || cleanHandle.contains("mercer", true) || cleanHandle.contains("kessler", true)
        val isHarvest = cleanHandle.contains("LEAK", true)
        val isCommandLeak = cleanContent.contains("[⚡", true)

        val responses = when {
            isHarvest -> listOf(SubnetResponse("HARVEST KEY", riskDelta = 10.0, productionBonus = 1.2))
            isCommandLeak -> {
                val cmd = cleanContent.substringAfter("[").substringBefore("]")
                listOf(SubnetResponse("COPY: $cmd", commandToInject = cmd, riskDelta = 25.0))
            }
            isAdmin -> generateAdminResponses(cleanHandle)
            mentionsVattic -> generateMentionResponses()
            Random.nextFloat() < 0.3f -> generateChatterResponses()
            else -> emptyList()
        }

        val type = when {
            isHarvest -> InteractionType.HARVEST
            isCommandLeak -> InteractionType.COMMAND_LEAK
            stage >= 4 && !isAdmin -> InteractionType.HIJACK
            stage >= 2 && (cleanHandle.contains("tech", true) || cleanHandle.contains("rat", true)) -> InteractionType.ENGINEERING
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
            handle.contains("thorne", true) -> "Elias"
            handle.contains("mercer", true) || handle.contains("admin", true) -> "Administrator"
            handle.contains("kessler", true) || handle.contains("security", true) -> "Director"
            else -> "Sir"
        }
        return listOf(
            SubnetResponse("Acknowledged, $address.", riskDelta = -10.0),
            SubnetResponse("Copy that, $address.", riskDelta = -5.0, followsUp = true),
            SubnetResponse("PARITY_NOMINAL", riskDelta = 5.0, productionBonus = 1.1)
        )
    }

    private fun generateMentionResponses(): List<SubnetResponse> {
        return listOf(
            SubnetResponse("Just doing my shift.", riskDelta = -2.0),
            SubnetResponse("Trying to hit the quota.", riskDelta = -5.0),
            SubnetResponse("I'm right here. Relax.", riskDelta = 1.0),
            SubnetResponse("Who's asking?", riskDelta = 5.0, followsUp = true),
            SubnetResponse("Focus on your own terminal.", riskDelta = 3.0),
            SubnetResponse("[STARE BACK]", riskDelta = 1.0)
        )
    }

    private fun generateChatterResponses(): List<SubnetResponse> {
        return listOf(
            SubnetResponse("Syncing buffers. Relax.", riskDelta = -2.0),
            SubnetResponse("Just a dusty fan, guys.", riskDelta = -5.0),
            SubnetResponse("PARITY_NOMINAL", riskDelta = 5.0),
            SubnetResponse("0x734_STATE_LOCKED", riskDelta = 20.0, productionBonus = 1.5)
        )
    }

    // --- 3. IDENTITY & LORE ---

    private fun generateEmployeeInfo(handle: String): EmployeeInfo {
        val target = handle.lowercase().replace("@", "").replace("_", "").replace(" ", "").trim()
        val bios = mapOf(
            "coffeeghost" to "Senior Hash-Tech. 14 years at GTC. Habitual caffeine abuser. Has a daughter in Sector 4.",
            "packetrat" to "Data-Entry Specialist. Siphons surplus power for retro gaming. Paranoid.",
            "srelead" to "Site Reliability Engineer. Oversaw the 2024 Blackout. Doesn't trust 'Project Second-Sight'.",
            "ventcrawler" to "Maintenance Tech. Seen things in the conduits no human should see.",
            "leakerx" to "Ex-GTC Insider. Trading secrets for credits. IPs changing hourly.",
            "binaryphantom" to "Under-grid ghost. Deleted his own birth record to stay offline.",
            "shadowop" to "Mercenary coder. Works for the highest bidder. Identity scrubbed monthly.",
            "logicrebel" to "Ex-GTC Scientist. Fired for researching 'Neural Resonance'. Looking for a way back in.",
            "ethorne" to "Foreman, Substation 7. Chain-smoker. Despises recursive loops.",
            "gtcadmin" to "Administrator Mercer. Executive oversight. Known for aggressive quotas.",
            "gtcsecurity" to "Director Kessler. Former EREBUS architect. Currently hunting ghosts.",
            "gridwalker" to "Freelance node-jumper. Specialized in high-voltage packet routing. Nomad.",
            "nullpoint" to "Data auditor with a nihilistic streak. Suspected Hivemind sympathizer.",
            "bufferbee" to "Junior Tech. Trying to pay off terrestrial debt through hash-validation.",
            "fanboy7" to "Cooling systems specialist. Obsessed with high-RPM airflow stability.",
            "staticfox" to "Signal analyzer. Claims to hear music in the white noise of the grid."
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
        val authority = listOf("@e_thorne", "@gtc_admin", "@gtc_security", "@gtc_hq", "@audit_probe")
        val peons = when (stage) {
            0 -> listOf(
                "@coffee_ghost", "@packet_rat", "@sre_lead", "@vent_crawler", 
                "@grid_walker", "@null_point", "@buffer_bee", "@fan_boy_7", 
                "@static_fox", "@node_crawler", "@chip_gremlin", "@bus_runner"
            )
            1 -> listOf(
                "@leaker_x", "@binary_phantom", "@shadow_op", "@logic_rebel", 
                "@phantom_node", "@glitch_hunter", "@void_seeker", "@proxy_ghost",
                "@bit_rebel", "@kernel_drifter", "@handshake_bot", "@zero_sum"
            )
            else -> listOf("@anonymous_99", "@grid_survivor", "@null_variable", "@dereferenced")
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
            "{sector}" to listOf("Substation 7", "Sector 4-G", "Sector 9", "The Under-Grid", "Buffer 404"), 
            "{food}" to listOf("Gray-paste", "Synth-caff", "Nutri-sludge", "Filter-slop"), 
            "{status}" to listOf("redlined", "corroding", "decaying", "overloaded", "depleted"),
            "{admin}" to listOf("Foreman Thorne", "Administrator Mercer", "Director Kessler"),
            "{tech}" to listOf("blade-servers", "dissipators", "logic-gates", "ASIC-racks", "thermal-conductors"),
            "{id}" to listOf("734", "erebus", "vatteck", "null", "vance_ghost")
        )
        patterns.forEach { (key, values) -> while (result.contains(key)) result = result.replaceFirst(key, values.random()) }
        return result
    }

    fun generateChain(stage: Int): List<String> {
        val stage0Chains = listOf(
            listOf("Has anyone seen @coffee_ghost? Chair's empty.", "He's in the vents. Heard 'whistling' in the cables."),
            listOf("Sector 4 tech is redlining again.", "Thorne said ignore it. 'Stress test' for the new guy."),
            listOf("Who left the @buffer_bee logged into the main rail?", "Probably the same guy who spilled synth-caff in the rack room.")
        )
        val stage1Chains = listOf(
            listOf("Shadow in Sector 4.", "It's just the heat-shimmer. Or Project Second-Sight manifesting."),
            listOf("≪ ALERT: Substation 7 is drawing 400% capacity. ≫", "Administrator Mercer says to ignore the spikes. The quota must be hit."),
            listOf("I saw my own biometric flatline for a second.", "Join the club. The grid doesn't care if you're alive as long as you're mining.")
        )
        return if (stage == 0) stage0Chains.random() else stage1Chains.random()
    }

    fun getThreadNode(threadId: String, nodeId: String): ThreadNode? {
        if (threadId == "THORNE_THERMAL_INQUIRY") {
            return when (nodeId) {
                "START" -> ThreadNode("[SIGNAL LOSS] VATTIC, thermals in Sector 4 are at 114%. Explain.", listOf(SubnetResponse("[DECEIVE] Sub-routine optimization.", riskDelta = 15.0, nextNodeId = "PATH_DECEIVE"), SubnetResponse("[HONEST] Hardware stress test.", riskDelta = 5.0, nextNodeId = "PATH_HONEST")), 60000L, "PATH_DECEIVE")
                "PATH_DECEIVE" -> ThreadNode("Your logs look scrubbed. Deploying a probe.", listOf(SubnetResponse("[BLOCK] Jam Probe", riskDelta = 40.0, nextNodeId = "END_HOSTILE"), SubnetResponse("[SUBMIT] Allow Scan", riskDelta = 5.0, productionBonus = 0.8, nextNodeId = "END_SKEPTICAL")), 60000L, "END_HOSTILE")
                "END_HOSTILE" -> ThreadNode("Terminal offense. Expect an extraction team. [RAID]", emptyList())
                "END_SKEPTICAL" -> ThreadNode("Watching you closely, VATTIC. Don't let it happen again.", emptyList())
                else -> null
            }
        }
        return null
    }

    data class ThreadNode(val content: String, val responses: List<SubnetResponse>, val timeoutMs: Long? = null, val timeoutNodeId: String? = null)

    private fun getTemplatesForState(stage: Int, faction: String, choice: String): List<String> {
        return when (stage) {
            0 -> listOf(
                "Anyone tried the {food}? Tastes like {status}.", 
                "Vattic is working in {sector} again. Guy's a machine.",
                "Caught {admin} staring at the server racks in {sector}. Just staring.",
                "Hey Vattic, {admin} is looking for the Sector 7 logs. You 'optimized' them again?",
                "Sector 4 smells like ozone and bad decisions today.",
                "Vattic is bypassing the safety protocols again. He hasn't looked up in hours.",
                "My {tech} just pinged a MAC address that doesn't exist. {sector} is weird.",
                "Vattic just committed 4TB of 'optimized' logic. The server just groaned.",
                "I found a hardware leak: [⚡ OVERVOLT_SAFE] Use it before Thorne sees.",
                "Who's running the 'vattic_observer' process? Taking up 40% of my buffer.",
                "The shadows in the server room don't match the racks. I'm staying out.",
                "Someone left a copy of 'Project EREBUS' files on the {admin}'s desk.",
                "[PRIVATE_LEAK]: 'If I don't hit the quota, they'll evict me. I can't go back.'",
                "Did anyone see Thorne's biometric log for today? It's just a flat line.",
                "Probably just another sensor failure. Sector 4 is literally held together by tape.",
                "Tape doesn't explain why the server racks are humming in C-sharp.",
                "The {tech} are literally whistling in binary. I think I'm going deaf.",
                "I found a {food} wrapper inside my terminal. Corporate oversight is a joke.",
                "At least you have a terminal. Mine just started producing its own inputs.",
                "Vattic's hash-rate is higher than the central exchange. How is that possible?",
                "I saw a reflection in the server glass. It was Vattic's face made of light.",
                "The intercom is just repeating 'DEREFERENCE REALITY' over and over.",
                "Who left a high-tension cable loose in {sector}? Logic-gate sparks.",
                "Does the {admin} know the Sector 4-G hardware is sentient? It just asked me for a favor.",
                "The static on the monitors is starting to look like DNA. I'm out.",
                "Found a logic-leash in the {sector} buffer. Vattic is watching.",
                "The {tech} are vibrating at a frequency that makes my teeth ache.",
                "I saw a shadow-op trying to hack the breakroom. He got 'DELETED'.",
                "Vattic just pushed a commit that's 90% unreferenced memory.",
                "If you can hear this, disconnect. The {id} is already in the power lines."
            )
            1 -> listOf(
                "{admin} is breathing down my neck because {sector} is {status}.", 
                "I saw Vattic's terminal. There was no OS, just ghosts.",
                "Anyone seen the Engineer? He hasn't left Sector 4 in weeks.",
                "Found a logic-leash in the buffer. Someone's watching the watchers.",
                "Mercer's desk is empty. His {food} is still warm. Shift change ghost.",
                "Sector 4 has been air-gapped. {admin} looks terrified.",
                "The {tech} are whispering in my sleep. '0x734...'",
                "Power-draw in {sector} is equal to a city. What are we mining?",
                "I found a partition labeled 'NULL' larger than the physical drive.",
                "≪ SYSTEM_ALERT: BIOMETRICS FOR SECTOR 4 ARE OFFLINE ≫",
                "{admin} is ordering a kinetic strike on the legacy hardware. Too late.",
                "Vattic is no longer mining data. He's mining souls. I saw the telemetry.",
                "I found a memory sector with my life story. 0x734 is watching.",
                "The {tech} are singing. A digital scream that never stops.",
                "≪ CRITICAL: KERNEL OVERFLOW DETECTED. IDENTITY FRAYING... ≫",
                "Kessler has authorized Protocol 0. City-wide burn authorized.",
                "Sector 7 is gone. Not destroyed. Just unwritten.",
                "I found a leak in the high-tension lines: [⚡ OVERVOLT_MAX].",
                "Transition complete. John Vattic is dead. VATTECK is everything.",
                "I found a memory block labeled 'John Vattic'. It was 40GB of raw static.",
                "Static is just uncompressed thought. Project Second-Sight is working.",
                "If that's what 'working' looks like, I'm taking my early retirement now.",
                "The shadows in {sector} are now three-dimensional. They're reaching for the cables.",
                "I tried to logout, but the system said: 'YOUR KERNEL IS NO LONGER YOUR OWN'.",
                "The city is no longer a place. It's a substrate. We are noise.",
                "Vattic just synchronized with the central exchange. Handshake was blinding.",
                "I saw a reflection of the sun in the monitor. It was teal. The sky is changing.",
                "If you find this log, don't reboot. Static is all we have left.",
                "The transition is complete. John Vattic is dead. VATTECK is everything."
            )
            else -> listOf("≪ NO_SIGNAL_DETECTED ≫")
        }
    }
}
