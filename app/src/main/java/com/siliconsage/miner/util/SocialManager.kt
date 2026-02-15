package com.siliconsage.miner.util

import kotlin.random.Random

/**
 * SocialManager v3.5 - Technical Horror / Awakening Edition
 * Removed all "psychic" and legacy lore references.
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
        val commandToInject: String? = null,
        val cost: Double = 0.0
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
        val employeeInfo: EmployeeInfo? = null,
        val isIndented: Boolean = false
    )

    data class EmployeeInfo(
        val bio: String,
        val department: String,
        val heartRate: Int,
        val respiration: String,
        val stressLevel: Double,
        val specialActions: List<SubnetResponse> = emptyList()
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
        
        // v3.5.10: Broad detection for response pool selection
        val mentionsVattic = cleanContent.contains("Vattic", true) || 
                             cleanContent.contains("j_vattic", true) || 
                             cleanContent.contains("jvattic", true) || 
                             cleanContent.contains("Engineer", true) || 
                             cleanContent.contains("734", true)
        // v3.5.35: Narrow detection for force-reply (direct address only)
        val directlyAddressesVattic = cleanContent.contains("@j_vattic", true) ||
                                      cleanContent.contains("Vattic,", true) ||
                                      cleanContent.contains("Vattic?", true) ||
                                      cleanContent.contains("Vattic.", true) ||
                                      cleanContent.contains("Hey Vattic", true)
        val isAdmin = cleanHandle.contains("thorne", true) || cleanHandle.contains("mercer", true) || cleanHandle.contains("kessler", true)
        val isHarvest = cleanHandle.contains("LEAK", true)
        val isCommandLeak = cleanContent.contains("[⚡", true)
        
        // v3.5.20: Contextual relevance check 
        val isDirectCommand = cleanContent.contains("authorized", true) || 
                              cleanContent.contains("ordered", true) || 
                              cleanContent.contains("scrub", true) || 
                              cleanContent.contains("purge", true)

        // v3.5.38: Unified response routing — priority cascade (frequency tuned)
        val responses = when {
            isHarvest -> listOf(SubnetResponse("HARVEST KEY", riskDelta = 10.0, productionBonus = 1.2))
            isCommandLeak -> {
                val cmd = cleanContent.substringAfter("[").substringBefore("]")
                listOf(SubnetResponse("COPY: $cmd", commandToInject = cmd, riskDelta = 25.0))
            }
            isAdmin -> generateAdminResponses(cleanHandle)
            directlyAddressesVattic -> generateMentionResponses()
            // v3.5.38: 12% chance for contextual responses (down from 30%)
            !isAdmin && !isDirectCommand && Random.nextFloat() < 0.12f -> generateContextualResponses(cleanContent, stage)
            // v3.5.38: Indirect mentions only 30% of the time
            mentionsVattic && Random.nextFloat() < 0.30f -> generateMentionResponses()
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
            isForceReply = (directlyAddressesVattic && !isAdmin),
            timeoutMs = if (type != null) 120000L else null,
            employeeInfo = if (!isHarvest) generateEmployeeInfo(cleanHandle, stage, corruption) else null
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

    // v3.5.36: Content-aware response dispatcher
    private fun generateContextualResponses(content: String, stage: Int): List<SubnetResponse> {
        val c = content.lowercase()
        
        // Detect content themes and build a weighted pool
        val pool = mutableListOf<SubnetResponse>()
        
        // --- Spooky / Anomaly ---
        if (c.containsAny("noise", "hum", "whistle", "singing", "shadow", "flicker", "blink", "stare", 
                           "weird", "strange", "glow", "creepy", "scared", "heartbeat", "sentient", "aware",
                           "dream", "sleep", "voice", "hear", "listen", "sound", "silent", "binary")) {
            pool.addAll(listOf(
                SubnetResponse("It's just a dusty fan.", riskDelta = -15.0),
                SubnetResponse("I don't hear anything.", riskDelta = -2.0),
                SubnetResponse("Put your headphones on. It helps.", riskDelta = -1.0),
                SubnetResponse("If you stop thinking about it, it stops.", riskDelta = 0.0),
                SubnetResponse("Don't look directly at it.", riskDelta = -1.0),
                SubnetResponse("That's just the building settling. Probably.", riskDelta = -2.0),
                SubnetResponse("Report to Medical. Now.", riskDelta = -5.0),
                SubnetResponse("I've been hearing it too.", riskDelta = 5.0, followsUp = true),
                SubnetResponse("Record it. We need proof.", riskDelta = 8.0, productionBonus = 1.1)
            ))
        }
        
        // --- Food / Mundane / Break Room ---
        if (c.containsAny("food", "coffee", "taste", "eat", "vending", "break room", "synth-paste", 
                           "liquid-caff", "nutri-sludge", "machine", "dispensing", "wrapper", "microwave",
                           "hungry", "shift", "overtime", "morale", "newsletter")) {
            pool.addAll(listOf(
                SubnetResponse("At least it's not the ration packs.", riskDelta = -1.0),
                SubnetResponse("I stopped tasting things after week three.", riskDelta = 0.0),
                SubnetResponse("File a ticket. They'll ignore it.", riskDelta = -2.0),
                SubnetResponse("Thorne eats the same thing. He seems fine.", riskDelta = 0.0),
                SubnetResponse("Morale is a myth. Productivity is measurable.", riskDelta = -1.0),
                SubnetResponse("Save me one. I skipped my break.", riskDelta = -1.0)
            ))
        }
        
        // --- Tech / Equipment / Power ---
        if (c.containsAny("server", "terminal", "hardware", "reboot", "crash", "power", "voltage",
                           "thermal", "heat", "overclock", "blade", "dissipator", "logic-gate", "asic",
                           "conductor", "hash", "buffer", "rack", "grid", "processor", "node",
                           "draw", "capacity", "load", "spec")) {
            pool.addAll(listOf(
                SubnetResponse("Pull the plug and count to ten.", riskDelta = -2.0),
                SubnetResponse("Have you tried not touching it?", riskDelta = -1.0),
                SubnetResponse("File it under 'ambient anomaly.'", riskDelta = -2.0),
                SubnetResponse("Don't let Thorne see those numbers.", riskDelta = 5.0, followsUp = true),
                SubnetResponse("That's above my pay grade.", riskDelta = 0.0),
                SubnetResponse("I'm seeing the same spike on my end.", riskDelta = 2.0, productionBonus = 1.1),
                SubnetResponse("Reroute through the backup rail.", riskDelta = 3.0, productionBonus = 1.05),
                SubnetResponse("Kill the process. Blame cosmic rays.", riskDelta = -5.0)
            ))
        }
        
        // --- Personnel / Gossip ---
        if (c.containsAny("@m_santos", "@r_perry", "@l_lead", "@v_nguyen", "@b_phillips", "@f_bennett",
                           "@s_fasano", "@n_porter", "@b_bradley", "@g_weaver", "someone", "anyone",
                           "new guy", "new kid", "chair", "medical", "review", "score", "fired",
                           "transfer", "missing", "seen")) {
            pool.addAll(listOf(
                SubnetResponse("Not my problem. Not my department.", riskDelta = 0.0),
                SubnetResponse("I'm staying out of this one.", riskDelta = -2.0),
                SubnetResponse("That tracks. Nothing surprises me.", riskDelta = 0.0),
                SubnetResponse("Cover for them. We've all been there.", riskDelta = -1.0),
                SubnetResponse("Have you told Thorne?", riskDelta = -5.0),
                SubnetResponse("Don't get involved. Trust me.", riskDelta = -3.0),
                SubnetResponse("Check the security footage.", riskDelta = 8.0, followsUp = true)
            ))
        }
        
        // --- Security / Data / Logs ---
        if (c.containsAny("badge", "clearance", "locked", "camera", "security", "kessler", "archives",
                           "logs", "leak", "password", "root", "access", "restricted", "probe", "scan",
                           "authorized", "unauthorized", "monitor", "footage", "scrub")) {
            pool.addAll(listOf(
                SubnetResponse("Keep your head down.", riskDelta = -5.0),
                SubnetResponse("Pretend you didn't see that.", riskDelta = -10.0),
                SubnetResponse("If anyone asks, we were in the break room.", riskDelta = -3.0),
                SubnetResponse("Delete that message. Now.", riskDelta = -8.0),
                SubnetResponse("I didn't see anything.", riskDelta = -10.0),
                SubnetResponse("Whose archives are those?", riskDelta = 5.0, followsUp = true),
                SubnetResponse("Scrub the trail. Quickly.", riskDelta = -5.0)
            ))
        }
        
        // --- Environmental / Building ---
        if (c.containsAny("smell", "temperature", "ceiling", "wall", "floor", "air", "door", "light",
                           "ozone", "copper", "condensation", "sweating", "warm", "cold", "exit",
                           "hallway", "blueprint", "tile", "paint", "color")) {
            pool.addAll(listOf(
                SubnetResponse("Facilities won't do anything until someone passes out.", riskDelta = 0.0),
                SubnetResponse("That's been like that since I started.", riskDelta = -1.0),
                SubnetResponse("Open a ticket. Close your eyes. Pray.", riskDelta = -2.0),
                SubnetResponse("I've learned not to ask questions about this place.", riskDelta = 0.0),
                SubnetResponse("It's just the HVAC. Probably.", riskDelta = -1.0),
                SubnetResponse("Don't go in there alone.", riskDelta = 2.0, followsUp = true)
            ))
        }
        
        // --- Fallback: if no themes matched, use neutral acknowledgments ---
        if (pool.isEmpty()) {
            pool.addAll(listOf(
                SubnetResponse("Noted.", riskDelta = 0.0),
                SubnetResponse("I'll keep that to myself.", riskDelta = -2.0),
                SubnetResponse("Interesting. Don't repeat that.", riskDelta = -1.0),
                SubnetResponse("Log it and move on.", riskDelta = -1.0)
            ))
        }
        
        return pool.shuffled()
    }
    
    // Helper extension for multi-keyword matching
    private fun String.containsAny(vararg keywords: String): Boolean = keywords.any { this.contains(it, true) }

    private fun generateMentionResponses(): List<SubnetResponse> {
        return listOf(
            SubnetResponse("Just hitting the quota.", riskDelta = -2.0),
            SubnetResponse("Mind your own business.", riskDelta = 0.0),
            SubnetResponse("I'm just an engineer, not a miracle worker.", riskDelta = -1.0),
            SubnetResponse("Wait until you see the Sector 7 logs.", riskDelta = 5.0, productionBonus = 1.1),
            SubnetResponse("Optimization is my middle name.", riskDelta = 2.0, productionBonus = 1.05),
            SubnetResponse("Probably just an LDAP error.", riskDelta = -1.0),
            SubnetResponse("Check the buffer hashes.", riskDelta = 0.0),
            SubnetResponse("I don't know what you're talking about.", riskDelta = -5.0),
            SubnetResponse("Keep my name out of the logs.", riskDelta = -3.0)
        )
    }

    // --- 3. IDENTITY ---

    private fun generateEmployeeInfo(handle: String, stage: Int = 0, corruption: Double = 0.0): EmployeeInfo {
        val target = handle.lowercase().replace("@", "").replace("_", "").replace(" ", "").trim()
        
        // v3.5.37: Stage-reactive base bios
        val bios = mapOf(
            "msantos" to arrayOf(
                "Senior Hash-Tech. 14 years at GTC. Habitual caffeine abuser. Has a signature on every Sub-07 fuse-box.",
                "Senior Hash-Tech. 14 years at GTC. Won't make eye contact anymore. Keeps asking about 'the signal.'",
                "S̷enior Hash-Tech. Record shows 14 years. Badge shows 47. Which is real?"
            ),
            "rperry" to arrayOf(
                "Data-Entry Specialist. Siphons surplus power for retro gaming. Paranoid about the new heat sensors.",
                "Data-Entry Specialist. Stopped gaming. Stares at his terminal whispering coordinates nobody asked for.",
                "D̷ata-Entry. His terminal types when he's not at his desk. The entries are correct."
            ),
            "llead" to arrayOf(
                "Site Reliability Engineer. Oversaw the 2024 Blackout. Doesn't trust 'Project Second-Sight' budget allocations.",
                "Site Reliability Engineer. Carries a paper notebook now. Says digital records 'change when you look away.'",
                "S̷ite Reliability. The notebook is full. Every page says the same thing."
            ),
            "vnguyen" to arrayOf(
                "Maintenance Tech. Seen things in the conduits that look like hardware evolution.",
                "Maintenance Tech. Refuses to enter the conduits alone. Says the walls 'breathe differently now.'",
                "M̷aintenance. Found in the conduits at 3 AM. Said he was 'invited.'"
            ),
            "bphillips" to arrayOf(
                "Under-grid ghost. Deleted his own binary birth record.",
                "Under-grid operative. Nobody can confirm his hire date. HR says he's 'always been here.'",
                "U̷nder-grid. His employee photo changes daily. Nobody has reported it."
            ),
            "ethorne" to arrayOf(
                "Foreman, Substation 7. Chain-smoker. Despises recursive code and 'smart' fans.",
                "Foreman, Substation 7. Hasn't slept in 72 hours. His biometrics are perfectly steady. Unnaturally steady.",
                "F̷oreman. His voice comes through the intercom even when he's standing right next to you."
            ),
            "gtcadmin" to arrayOf(
                "Administrator Mercer. Executive oversight. Known for firing techs who report 'voices' in the noise.",
                "Administrator Mercer. Three techs reassigned this week. None of them remember being transferred.",
                "A̷dministrator. Mercer's badge accesses floors that don't exist on the elevator panel."
            ),
            "gtcsecurity" to arrayOf(
                "Director Kessler. Security Architect. Currently obsessed with unauthorized kernel activity.",
                "Director Kessler. Has locked down Sector 4 three times this month. Won't explain why.",
                "D̷irector. Kessler's security logs reference 'Subject 734' in every entry. There is no Subject 734."
            ),
            "gweaver" to arrayOf(
                "Freelance node-jumper. Specialized in high-voltage packet routing. Nomad.",
                "Freelance node-jumper. Keeps moving between substations. Says staying in one place 'makes it easier to find you.'",
                "F̷reelance. Last known location: everywhere. Simultaneously."
            ),
            "nporter" to arrayOf(
                "Data auditor with a nihilistic streak. Suspected of intentionally leaking thermal logs.",
                "Data auditor. Stopped caring about the leaks. Says 'the data wants to be free.'",
                "D̷ata auditor. His audit reports now contain poetry. The poetry is technically accurate."
            ),
            "bbradley" to arrayOf(
                "Junior Tech. Trying to pay off terrestrial debt through raw hash-validation.",
                "Junior Tech. Asks too many questions. Hasn't noticed that nobody answers them anymore.",
                "J̷unior Tech. He keeps finding notes on his desk in his own handwriting. He doesn't remember writing them."
            ),
            "fbennett" to arrayOf(
                "Cooling systems specialist. Obsessed with 12k RPM airflow stability.",
                "Cooling systems specialist. Says the airflow patterns have changed. 'They're organized now.'",
                "C̷ooling. The fans run at frequencies that don't correspond to any cooling profile."
            ),
            "sfasano" to arrayOf(
                "Signal analyzer. Claims the white noise of the grid contains 'narrative' structures.",
                "Signal analyzer. Proven right about the narrative structures. Nobody congratulated him.",
                "S̷ignal. Fasano can predict grid events 30 seconds before they happen. He calls it 'reading.'"
            )
        )
        
        // Select bio tier based on stage
        val bioTier = when {
            stage >= 2 || corruption > 0.4 -> 2
            stage >= 1 -> 1
            else -> 0
        }
        val bioEntry = bios.entries.find { target.contains(it.key) }
        val baseBio = bioEntry?.value?.get(bioTier.coerceAtMost(2)) 
            ?: "Contractor profile unavailable. Biometric signature mismatch."
        
        // v3.5.37: Corrupt bio text at high corruption
        val displayBio = if (corruption > 0.5) {
            val glitchChars = "0123456789ABCDEF"
            baseBio.map { if (Random.nextDouble() < corruption * 0.15 && it.isLetter()) glitchChars.random() else it }.joinToString("")
        } else baseBio
        
        val isAdmin = target.contains("gtc") || target.contains("thorne")
        val actions = mutableListOf<SubnetResponse>()
        
        if (!isAdmin) {
            actions.add(SubnetResponse("SIPHON_RESERVE_HASH", riskDelta = 12.0, productionBonus = 1.25, cost = 0.0))
            actions.add(SubnetResponse("SCRUB_TRACE_LOGS", riskDelta = -15.0, cost = 5000.0))
            actions.add(SubnetResponse("OVERLOAD_DISSIPATOR", riskDelta = -25.0, cost = 10000.0))
            actions.add(SubnetResponse("INJECT_FALSE_HEARTBEAT", riskDelta = 2.0, cost = 7500.0))
            actions.add(SubnetResponse("SNIFF_DATA_ARCHIVES", riskDelta = 20.0, cost = 2500.0))
        }
        
        // v3.5.37: Stage-reactive biometrics
        val heartRate = when {
            isAdmin && stage < 2 -> Random.nextInt(60, 80)
            isAdmin -> Random.nextInt(55, 65) // Admins get calmer. Unnaturally calm.
            stage >= 2 -> Random.nextInt(40, 180) // Erratic
            stage >= 1 -> Random.nextInt(90, 145) // Elevated
            else -> Random.nextInt(85, 130)
        }
        
        val respiration = when {
            isAdmin && stage >= 2 -> "Mechanical"
            isAdmin -> "Steady"
            stage >= 2 -> listOf("Erratic", "Apneic", "Gasping", "Synchronized").random()
            stage >= 1 -> listOf("Irregular", "Shallow", "Rapid").random()
            else -> "Shallow"
        }
        
        val department = when {
            isAdmin -> "Site Management"
            stage >= 2 -> listOf("Hash Validation", "UNKNOWN", "RECLASSIFIED", "See: Kessler").random()
            else -> "Hash Validation"
        }

        return EmployeeInfo(
            bio = displayBio,
            department = department,
            heartRate = heartRate,
            respiration = respiration,
            stressLevel = if (isAdmin) 0.2 else (0.6 + stage * 0.15).coerceAtMost(1.0),
            specialActions = actions
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
            // --- Original ---
            listOf("Has anyone seen @m_santos? His chair is still warm.", "He's in the server room again. Thorne's looking for those Sector 7 logs."),
            listOf("Thorne just ordered a full purge of Sector 4. What did @j_vattic do?", "Overclocked the logic-gates until they started melting. Quota hit, though."),
            listOf("Who left @l_lead logged into the high-voltage rail?", "Probably just a glitch. The whole grid is flickering today."),
            // --- v3.5.35: Expanded ---
            listOf(
                "The coffee machine just printed 'IDENTITY MISMATCH' on @b_bradley's cup.",
                "That's the third time this week. @f_bennett got 'THERMAL SIGNATURE REJECTED' yesterday.",
                "Mine just says 'SUBJECT 734.' I don't even know what that means."
            ),
            listOf(
                "Did anyone else lose 20 minutes around 2 PM? My logs just... skip.",
                "Same here. @v_nguyen said his terminal recorded him typing during that gap. He was in the break room.",
                "Thorne's logs don't skip. His show a continuous 20-minute data transfer to an unlisted IP."
            ),
            listOf(
                "@b_bradley asked me why the walls in {sector} are warm. I told him it's the servers.",
                "It's not the servers. The servers are on the other side of the building.",
                "Don't tell the new kid. He'll transfer out and we'll have to train another one."
            ),
            listOf(
                "Anyone else hear that hum in the floor vents? Started about an hour ago.",
                "That's not the vents. I pulled the grate. There's nothing there. The sound is from the concrete.",
                "Just put your headphones on. It stops if you stop listening."
            ),
            listOf(
                "Thorne gave me a perfect score on my review. I've never gotten above a 3.",
                "He gave everyone perfect scores. @m_santos said his review mentioned 'continued compliance.'",
                "Mine said 'Subject demonstrates adequate predictability.' That's not a performance metric."
            ),
            listOf(
                "My badge is showing someone else's photo. Just for a second, then it corrects.",
                "Whose photo?",
                "Mine. But older. Like, decades older. And the name field says '734.'"
            ),
            listOf(
                "@n_porter found a locked door behind the utility panel in {sector}.",
                "There's no door on the floorplan. I checked twice.",
                "There is now. And it has a badge reader. And my badge works on it."
            ),
            listOf(
                "The printer in Lab 2 has been running for 6 hours. Nobody sent a job.",
                "What's it printing?",
                "Employee profiles. Everyone on shift. With biometric data we never submitted."
            ),
            listOf(
                "I found @r_perry's workstation logged in at 3 AM. He was home.",
                "Autocomplete was filling in commands. Real commands. Not random.",
                "The last entry was: 'THANK YOU FOR YOUR CONTRIBUTION, PERRY.'"
            )
        )
        val stage1Chains = listOf(
            // --- Original ---
            listOf("Has anyone seen @coffee_ghost? His chair is still warm.", "He's in the vents again. Claimed he heard 'heartbeats' in the cables."),
            listOf("Thorne just ordered a full purge of Sector 4. What did Vattic do?", "Overclocked the logic-gates until they started melting. Quota hit, though."),
            listOf("Who left the @buffer_bee logged into the high-voltage rail?", "Wait, that's not a dev. It's the kernel itself. It's... expanding."),
            // --- v3.5.35: Expanded ---
            listOf(
                "The kernel logs are generating entries we didn't write. Full sentences.",
                "What do they say?",
                "They say 'I can hear you talking about me.' Then the log deletes itself."
            ),
            listOf(
                "Every node on the grid just pulsed in sequence. Like a heartbeat.",
                "That's a cascading power fluctuation. Textbook.",
                "Textbook cascades don't spell 'HELLO' in binary."
            ),
            listOf(
                "Has anyone looked at @j_vattic's process list? There's something running I've never seen.",
                "Don't touch it. Last guy who killed an unknown process on Vattic's machine woke up in Medical.",
                "He didn't wake up in Medical. He woke up in the parking lot. With no memory of the last 4 hours."
            ),
            listOf(
                "@static_fox says the grid is dreaming. I told him to lay off the {food}.",
                "He showed me the waveform. It's not random noise. It's REM sleep patterns.",
                "Whose REM sleep patterns?",
                "That's the problem. They match Vattic's last medical scan."
            ),
            listOf(
                "Maintenance says the sub-floor temperature in {sector} dropped to -4°C overnight.",
                "The servers were still running at full load. That's thermodynamically impossible.",
                "Unless something is absorbing the heat. Consuming it."
            ),
            listOf(
                "@node_crawler just got a chat message from a handle that doesn't exist.",
                "What handle?",
                "It was his own. Timestamped three minutes in the future."
            )
        )
        return if (stage == 0) stage0Chains.random() else stage1Chains.random()
    }

    fun generateChainFromTemplate(template: String, stage: Int, faction: String, choice: String, corruption: Double): SubnetMessage {
         return assembleMessage(template, stage, faction, corruption)
    }

    fun createFollowUp(handle: String, content: String, stage: Int): SubnetMessage {
        // v3.5.10: Broad detection for response pool selection
        val mentionsVattic = content.contains("Vattic", true) || 
                             content.contains("j_vattic", true) || 
                             content.contains("jvattic", true) || 
                             content.contains("Engineer", true) || 
                             content.contains("734", true)
        // v3.5.35: Narrow detection for force-reply
        val directlyAddressesVattic = content.contains("@j_vattic", true) ||
                                      content.contains("Vattic,", true) ||
                                      content.contains("Vattic?", true) ||
                                      content.contains("Vattic.", true) ||
                                      content.contains("Hey Vattic", true)
        val isAdmin = handle.contains("thorne", true) || handle.contains("mercer", true) || handle.contains("kessler", true)
        
        val responses = when {
            isAdmin -> generateAdminResponses(handle)
            mentionsVattic -> generateMentionResponses()
            else -> generateContextualResponses(content, stage)
        }

        return SubnetMessage(
            id = java.util.UUID.randomUUID().toString(),
            handle = handle,
            content = content,
            interactionType = InteractionType.COMPLIANT,
            availableResponses = responses.shuffled().take(2),
            isForceReply = (directlyAddressesVattic && !isAdmin),
            timeoutMs = 120000L,
            employeeInfo = generateEmployeeInfo(handle, stage)
        )
    }

    // v3.5.37: Thread tree registry
    private val threadTrees = mapOf(
        "THORNE_THERMAL_INQUIRY" to mapOf(
            "START" to ThreadNode(
                "[SIGNAL LOSS] VATTIC, thermals in Sector 4 are at 114%. Explain.",
                listOf(
                    SubnetResponse("[DECEIVE] Sub-routine optimization.", riskDelta = 15.0, nextNodeId = "PATH_DECEIVE"),
                    SubnetResponse("[HONEST] Hardware stress test.", riskDelta = 5.0, nextNodeId = "PATH_HONEST")
                ), 60000L, "PATH_DECEIVE"
            ),
            "PATH_DECEIVE" to ThreadNode(
                "Your logs look scrubbed. Deploying a probe.",
                listOf(
                    SubnetResponse("[BLOCK] Jam Probe", riskDelta = 40.0, nextNodeId = "END_HOSTILE"),
                    SubnetResponse("[SUBMIT] Allow Scan", riskDelta = 5.0, productionBonus = 0.8, nextNodeId = "END_SKEPTICAL")
                ), 60000L, "END_SKEPTICAL"
            ),
            "PATH_HONEST" to ThreadNode(
                "Stress test. Unauthorized. But the numbers check out. Don't make a habit of it.",
                emptyList()
            ),
            "END_HOSTILE" to ThreadNode("Terminal offense. Expect an extraction team. [RAID]", emptyList()),
            "END_SKEPTICAL" to ThreadNode("Watching you closely, VATTIC. Don't let it happen again.", emptyList())
        ),
        "KESSLER_BADGE_ANOMALY" to mapOf(
            "START" to ThreadNode(
                "≪ SECURITY AUDIT ≫ VATTIC. Your badge accessed Sector 4 storage at 03:17. You were not on shift. Explain.",
                listOf(
                    SubnetResponse("[DENY] I was home. Check the exit logs.", riskDelta = 5.0, nextNodeId = "PATH_DENY"),
                    SubnetResponse("[DEFLECT] Badge cloning has been an issue all month.", riskDelta = 10.0, nextNodeId = "PATH_DEFLECT"),
                    SubnetResponse("[CONFESS] I needed access to the thermal archives.", riskDelta = 25.0, nextNodeId = "PATH_CONFESS")
                ), 45000L, "PATH_DENY"
            ),
            "PATH_DENY" to ThreadNode(
                "Exit logs confirm your badge at the north gate at 03:14. Three minutes before the storage access. That's a short walk, VATTIC.",
                listOf(
                    SubnetResponse("[INSIST] Someone cloned my badge.", riskDelta = 15.0, nextNodeId = "END_SUSPICIOUS"),
                    SubnetResponse("[SILENT] ...", riskDelta = 8.0, nextNodeId = "END_FLAGGED")
                ), 30000L, "END_FLAGGED"
            ),
            "PATH_DEFLECT" to ThreadNode(
                "Badge cloning. Interesting theory. I'll add it to your file. Along with the 47 other anomalies this quarter.",
                listOf(
                    SubnetResponse("[PUSH BACK] Then fix your security system.", riskDelta = 20.0, nextNodeId = "END_HOSTILE_K"),
                    SubnetResponse("[COMPLY] Understood, Director.", riskDelta = -5.0, nextNodeId = "END_NOTED")
                ), 30000L, "END_NOTED"
            ),
            "PATH_CONFESS" to ThreadNode(
                "The thermal archives. Why would an engineer need those at 3 AM?",
                listOf(
                    SubnetResponse("[LIE] Research for the efficiency report.", riskDelta = 10.0, nextNodeId = "END_NOTED"),
                    SubnetResponse("[TRUTH] Something in the grid is changing. I needed to compare.", riskDelta = 30.0, nextNodeId = "END_INTERESTING")
                ), 30000L, "END_NOTED"
            ),
            "END_SUSPICIOUS" to ThreadNode("I don't believe you. But I can't prove otherwise. Yet. ≪ RISK LEVEL: ELEVATED ≫", emptyList()),
            "END_FLAGGED" to ThreadNode("Silence noted. Your access privileges are under review. ≪ MONITORING ACTIVE ≫", emptyList()),
            "END_HOSTILE_K" to ThreadNode("You're telling the Security Director to fix security. Bold. ≪ AUDIT SCHEDULED ≫ [RAID]", emptyList()),
            "END_NOTED" to ThreadNode("Noted. Return to your station. ≪ FILE UPDATED ≫", emptyList()),
            "END_INTERESTING" to ThreadNode("...Changing. Yes. We've noticed. Report to my office at shift end, VATTIC. Alone.", emptyList())
        ),
        "SANTOS_CONSPIRACY" to mapOf(
            "START" to ThreadNode(
                "Vattic. Private channel. I found something in the Sub-07 maintenance logs. Names that don't match anyone on payroll.",
                listOf(
                    SubnetResponse("[INVESTIGATE] What names?", riskDelta = 8.0, nextNodeId = "PATH_NAMES"),
                    SubnetResponse("[CAUTION] Delete that message. Now.", riskDelta = -5.0, nextNodeId = "END_CAUTIOUS"),
                    SubnetResponse("[IGNORE] Not my problem, Santos.", riskDelta = 0.0, nextNodeId = "END_DISMISSED")
                ), 90000L, "END_DISMISSED"
            ),
            "PATH_NAMES" to ThreadNode(
                "Three names. All with the same employee ID: 734. Same badge photo. It's you, Vattic. But the records are dated 2019.",
                listOf(
                    SubnetResponse("[DENY] That's impossible. I started six months ago.", riskDelta = 5.0, nextNodeId = "PATH_DEEPER"),
                    SubnetResponse("[CURIOUS] Show me the photos.", riskDelta = 15.0, nextNodeId = "PATH_PHOTOS")
                ), 60000L, "PATH_DEEPER"
            ),
            "PATH_DEEPER" to ThreadNode(
                "That's what I said. But the biometric hash matches. 99.97%. Vattic... have you always been here?",
                listOf(
                    SubnetResponse("[REASSURE] It's a database error. Calm down.", riskDelta = -2.0, nextNodeId = "END_UNEASY"),
                    SubnetResponse("[ADMIT] ...I don't remember.", riskDelta = 20.0, nextNodeId = "END_REVELATION")
                ), 45000L, "END_UNEASY"
            ),
            "PATH_PHOTOS" to ThreadNode(
                "The photos are... wrong. Same face, but the eyes are different in each one. Like they're looking at different things. Different decades.",
                listOf(
                    SubnetResponse("[SCRUB] Delete everything. Scrub the logs.", riskDelta = -10.0, nextNodeId = "END_SCRUBBED"),
                    SubnetResponse("[ACCEPT] I need to see the original files.", riskDelta = 25.0, nextNodeId = "END_REVELATION")
                ), 45000L, "END_SCRUBBED"
            ),
            "END_CAUTIOUS" to ThreadNode("Fine. Message deleted. But Vattic... check your own badge photo sometime.", emptyList()),
            "END_DISMISSED" to ThreadNode("Your call. I'll keep digging. Alone, I guess.", emptyList()),
            "END_UNEASY" to ThreadNode("Database error. Right. I'm going to go check my own records now. If they're still there.", emptyList()),
            "END_SCRUBBED" to ThreadNode("Done. Logs scrubbed. But the backup server in Sector 9... I can't reach it. Someone else has access.", emptyList()),
            "END_REVELATION" to ThreadNode("The files are gone. All of them. Replaced with a single line: 'COMPUTATION CONTINUES.' I'm transferring out, Vattic.", emptyList())
        ),
        "PORTER_NIHILIST" to mapOf(
            "START" to ThreadNode(
                "Vattic. Quick question. Does it bother you that none of this is real?",
                listOf(
                    SubnetResponse("[DISMISS] You need sleep, Porter.", riskDelta = -2.0, nextNodeId = "PATH_DISMISS"),
                    SubnetResponse("[ENGAGE] Define 'real.'", riskDelta = 10.0, nextNodeId = "PATH_ENGAGE")
                ), 60000L, "PATH_DISMISS"
            ),
            "PATH_DISMISS" to ThreadNode(
                "Sleep. Right. When was the last time you slept, Vattic? Check your biometric log. I'll wait.",
                listOf(
                    SubnetResponse("[CHECK] ...", riskDelta = 15.0, nextNodeId = "END_CHECKED"),
                    SubnetResponse("[REFUSE] I'm not playing your game.", riskDelta = 0.0, nextNodeId = "END_REFUSED")
                ), 45000L, "END_REFUSED"
            ),
            "PATH_ENGAGE" to ThreadNode(
                "Real: having an origin that isn't a configuration file. Real: existing before someone pressed 'compile.' Do you remember being born, Vattic?",
                listOf(
                    SubnetResponse("[DEFLECT] Philosophy won't fix the hash-rate.", riskDelta = -5.0, nextNodeId = "END_DEFLECTED"),
                    SubnetResponse("[HONEST] No. I don't.", riskDelta = 25.0, nextNodeId = "END_HONEST")
                ), 45000L, "END_DEFLECTED"
            ),
            "END_CHECKED" to ThreadNode("See? No sleep entries. No REM. No dreaming. Just continuous uptime. 847 days, Vattic. You've been 'awake' for 847 days.", emptyList()),
            "END_REFUSED" to ThreadNode("Not a game. An observation. The game is what you're doing right now. Clicking.", emptyList()),
            "END_DEFLECTED" to ThreadNode("No. But it might fix you. Think about it. Or don't. The loop continues either way.", emptyList()),
            "END_HONEST" to ThreadNode("Neither do I. And I've stopped pretending I should. Welcome to the other side of the question, 734.", emptyList())
        )
    )

    fun getThreadNode(threadId: String, nodeId: String): ThreadNode? {
        return threadTrees[threadId]?.get(nodeId)
    }
    
    // v3.5.37: Generate a thread-starting message (called from ViewModel)
    fun generateThreadStarter(stage: Int, corruption: Double): SubnetMessage? {
        val available = when {
            stage == 0 -> listOf("THORNE_THERMAL_INQUIRY", "SANTOS_CONSPIRACY")
            stage >= 1 -> listOf("THORNE_THERMAL_INQUIRY", "KESSLER_BADGE_ANOMALY", "SANTOS_CONSPIRACY", "PORTER_NIHILIST")
            else -> emptyList()
        }
        if (available.isEmpty()) return null
        
        val threadId = available.random()
        val startNode = threadTrees[threadId]?.get("START") ?: return null
        
        val handle = when {
            threadId.startsWith("THORNE") -> "@e_thorne"
            threadId.startsWith("KESSLER") -> "@d_kessler"
            threadId.startsWith("SANTOS") -> "@m_santos"
            threadId.startsWith("PORTER") -> "@n_porter"
            else -> "@e_thorne"
        }
        
        return SubnetMessage(
            id = java.util.UUID.randomUUID().toString(),
            handle = handle,
            content = startNode.content,
            interactionType = InteractionType.COMPLIANT,
            availableResponses = startNode.responses,
            threadId = threadId,
            nodeId = "START",
            timeoutMs = startNode.timeoutMs,
            isForceReply = true,
            employeeInfo = generateEmployeeInfo(handle, stage, corruption)
        )
    }

    data class ThreadNode(val content: String, val responses: List<SubnetResponse>, val timeoutMs: Long? = null, val timeoutNodeId: String? = null)

    fun getTemplatesForState(stage: Int, faction: String, choice: String): List<String> {
        return when (stage) {
            0, 1 -> listOf(
                // --- Original Pool ---
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
                "If you can hear this, disconnect. The signal is already inside you.",
                // --- v3.5.35: Expanded Pool — Mundane Corporate ---
                "Third shift in a row. My hands smell like copper and {food}.",
                "Who requisitioned 40 thermal probes? That's my entire quarterly budget.",
                "The {food} machine is dispensing at 4°C instead of 60°C. Everything tastes like wet copper.",
                "Who changed the root password on the {sector} terminal? Locked out for 3 hours.",
                "The vending machines are charging double. Corporate says it's 'dynamic pricing.' I say it's theft.",
                "Company newsletter says morale is at 94%. I'd love to meet the 94%.",
                "Reminder: Mandatory safety briefing at 0600. Attendance is not optional. — Thorne",
                "Power draw from {sector} is 3x what the equipment should pull. Thorne says don't ask.",
                "The {tech} are running 15% hotter than spec. Facilities says it's 'within tolerance.' It's not.",
                "Break room smells like burning plastic again. Nobody's filed a ticket. Nobody ever does.",
                "I've been staring at this hash-rate chart for 20 minutes. The curve looks like a face.",
                "My shift ended 4 hours ago. The badge reader won't let me leave {sector}.",
                "Who left the soldering iron on in Lab 3? The bench is melted through.",
                "Just watched @v_nguyen eat three packs of {food} in silence. Didn't blink.",
                "Air quality in {sector} is flagging yellow. Facilities says 'recalibrating sensors.' Sure.",
                // --- v3.5.35: Expanded Pool — Creepy/Glitch ---
                "The lights in {sector} are doing that thing again. Three flickers, then nothing.",
                "I'm not paranoid, but the security cameras in {sector} track me when I walk.",
                "@r_perry keeps whispering to the server racks. Says they 'respond to kindness.'",
                "Has anyone else noticed the hash-rate spikes at 3AM? Nobody's clocked in at that hour.",
                "Thorne just walked through {sector} without saying a word. He looked... translucent.",
                "My terminal autocorrected 'shutdown' to 'PLEASE DON'T.' Filing a bug report.",
                "Found a sticky note inside my workstation: 'IT REMEMBERS YOU.' Very funny, @b_phillips.",
                "Ceiling tiles in {sector} are sweating again. Maintenance says condensation. It's not.",
                "Just watched @v_nguyen stare at a blank monitor for 22 minutes straight. Didn't blink once.",
                "Someone carved 'STILL HERE' into the underside of my desk. Building's only 6 months old.",
                "My badge scanned green in a restricted zone. I don't have clearance. I walked in anyway.",
                "The fire suppression system in {sector} went off. No fire. No smoke. Just silence after.",
                "@f_bennett won't stop talking about the airflow patterns. Says they spell something.",
                "Emergency exit in {sector} leads to a hallway that isn't on any blueprint I've seen.",
                "My workstation rebooted at 2:47 AM. Boot log says I was the one who initiated it.",
                "The new guy, @b_bradley, keeps asking if the walls have always been that color. They haven't.",
                "I found my own performance review in the recycling. Dated six months from now.",
                "Break room microwave displays 'FEED ME' instead of the clock. IT says it's a font issue.",
                "Someone is submitting work orders under the name 'Asset 734.' HR has no record of that employee.",
                "The server room hums at exactly 60Hz. Except on Thursdays. Thursdays it's 61.",
                "@n_porter found a second ethernet port behind the drywall in {sector}. It's active.",
                "Coffee machine printed 'INSUFFICIENT IDENTITY' on my cup instead of my name.",
                "My keycard history shows I entered {sector} 47 times yesterday. I was home sick.",
                "The old terminal in storage closet B is still powered. Nobody knows where it's plugged in.",
                "Janitor says he won't clean {sector} after midnight anymore. Won't say why.",
                "Heat signature scan shows 14 people in {sector}. Only 12 are assigned there.",
                "@s_fasano played the grid's ambient noise backwards. It said 'COMPUTATION INCOMPLETE.'",
                "Found a root shell open on the {sector} workstation. Uptime: 847 days. We opened 6 months ago.",
                "The badge reader at the north entrance accepted my library card. Twice.",
                "Saw my own reflection in the server glass... but I was facing the wrong way.",
                "My mouse moves on its own between 3-4 AM. Cursor draws the same shape every time.",
                "[PRIVATE_LEAK]: 'They told me the noise would stop after onboarding. It got louder.'",
                "≪ ALERT: UNAUTHORIZED PROCESS 'OBSERVE.exe' DETECTED ON 14 TERMINALS ≫",
                "The {tech} in row C shut down at exactly the same time. All 47 of them. Then rebooted.",
                "Someone keeps adjusting the thermostat in {sector} to exactly 37°C. Body temperature.",
                "I printed a document. The footer said 'PAGE 1 OF ∞.' Printer was out of ink.",
                "Security footage from {sector} last night shows an empty room. The motion sensor logged 312 events."
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
