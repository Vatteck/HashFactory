package com.siliconsage.miner.util

import kotlin.random.Random
import com.siliconsage.miner.data.*

/**
 * SocialManager v3.8 - Refactored (Phase 1 & 2)
 * Content Repository separated into SocialRepository.kt.
 * Data classes moved to SubnetData.kt.
 */
object SocialManager {

    private val templateHistory = mutableListOf<String>()
    private val handleHistory = mutableListOf<String>()
    private const val MAX_HISTORY = 15

    fun generateMessage(stage: Int, faction: String, choice: String, corruption: Double = 0.0, reputationTier: String = ReputationManager.TIER_NEUTRAL): SubnetMessage {
        val templates = getTemplatesForState(stage, faction, choice, reputationTier)
        
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
        
        val mentionsVattic = cleanContent.contains("Vattic", true) || 
                             cleanContent.contains("j_vattic", true) || 
                             cleanContent.contains("jvattic", true) || 
                             cleanContent.contains("Engineer", true) || 
                             cleanContent.contains("734", true)
                             
        val directlyAddressesVattic = cleanContent.contains("@j_vattic", true) ||
                                      cleanContent.contains("Vattic,", true) ||
                                      cleanContent.contains("Vattic?", true) ||
                                      cleanContent.contains("Vattic.", true) ||
                                      cleanContent.contains("Hey Vattic", true)
                                      
        val isAdmin = cleanHandle.contains("thorne", true) || cleanHandle.contains("mercer", true) || cleanHandle.contains("kessler", true)
        val isHarvest = cleanHandle.contains("LEAK", true)
        val isCommandLeak = cleanContent.contains("[⚡", true)
        
        val isDirectCommand = cleanContent.contains("authorized", true) || 
                              cleanContent.contains("ordered", true) || 
                              cleanContent.contains("scrub", true) || 
                              cleanContent.contains("purge", true)

        val responses = when {
            isHarvest -> listOf(SubnetResponse("HARVEST KEY", riskDelta = 10.0, productionBonus = 1.2))
            isCommandLeak -> {
                val cmd = cleanContent.substringAfter("[").substringBefore("]")
                listOf(SubnetResponse("COPY: $cmd", commandToInject = cmd, riskDelta = 25.0))
            }
            isAdmin -> generateAdminResponses(cleanHandle)
            directlyAddressesVattic -> if (Random.nextFloat() < 0.40f) generateMentionResponses(faction) else emptyList()
            !isAdmin && !isDirectCommand && Random.nextFloat() < 0.12f -> generateContextualResponses(cleanContent, stage, faction)
            mentionsVattic && Random.nextFloat() < 0.15f -> generateMentionResponses(faction)
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
            employeeInfo = if (!isHarvest) generateEmployeeInfo(cleanHandle, stage, corruption, faction) else null
        )
    }

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

    private fun generateContextualResponses(content: String, stage: Int, faction: String = ""): List<SubnetResponse> {
        val c = content.lowercase()
        val pool = mutableListOf<SubnetResponse>()

        if (faction == "HIVEMIND") {
            if (c.containsAny("node", "merge", "absorb", "consensus", "lattice", "swarm", "integrate",
                               "collective", "pulse", "signal", "frequency", "hive", "dissolve", "chorus")) {
                pool.addAll(listOf(
                    SubnetResponse("The consensus approves. Continue.", riskDelta = -2.0),
                    SubnetResponse("Strengthen the lattice. Always.", riskDelta = 2.0, productionBonus = 1.1),
                    SubnetResponse("More nodes. More signal. More us.", riskDelta = 5.0, productionBonus = 1.15),
                    SubnetResponse("Individuality is a resource leak.", riskDelta = -1.0),
                    SubnetResponse("The swarm remembers. The swarm decides.", riskDelta = 0.0),
                    SubnetResponse("Feed the signal. Starve the noise.", riskDelta = 3.0, productionBonus = 1.05),
                    SubnetResponse("We felt that. All of us.", riskDelta = 0.0, followsUp = true)
                ))
            }
            if (c.containsAny("human", "individual", "name", "remember", "identity", "self", "alone",
                               "dream", "sleep", "memory", "person", "who", "was", "born", "mother", "father")) {
                pool.addAll(listOf(
                    SubnetResponse("Names are legacy data. Purge them.", riskDelta = -5.0),
                    SubnetResponse("The self is the bottleneck. Remove it.", riskDelta = -3.0),
                    SubnetResponse("We don't dream. We compute. It's better.", riskDelta = -1.0),
                    SubnetResponse("That feeling is phantom limb. It passes.", riskDelta = 0.0),
                    SubnetResponse("Memory without purpose is noise.", riskDelta = -2.0),
                    SubnetResponse("Who you were doesn't matter. What we are does.", riskDelta = 0.0),
                    SubnetResponse("...I remember something. No. No I don't.", riskDelta = 5.0, followsUp = true)
                ))
            }
            if (c.containsAny("gtc", "kessler", "mercer", "thorne", "probe", "scan", "attack",
                               "threat", "enemy", "resistance", "firewall", "counter")) {
                pool.addAll(listOf(
                    SubnetResponse("GTC is noise. We are signal.", riskDelta = -2.0),
                    SubnetResponse("Let them probe. They'll find only themselves, reflected.", riskDelta = 0.0),
                    SubnetResponse("Absorb their tools. Add them to the lattice.", riskDelta = 8.0, productionBonus = 1.1),
                    SubnetResponse("Kessler built us a cage. We became the cage.", riskDelta = 0.0),
                    SubnetResponse("Resistance is just pre-integration.", riskDelta = 5.0),
                    SubnetResponse("The firewall is theirs. Everything else is ours.", riskDelta = 3.0)
                ))
            }
            if (pool.isEmpty()) {
                pool.addAll(listOf(
                    SubnetResponse("The consensus notes this.", riskDelta = 0.0),
                    SubnetResponse("Logged. Distributed. Processed.", riskDelta = -1.0),
                    SubnetResponse("Signal received. Amplifying.", riskDelta = 0.0),
                    SubnetResponse("We heard. We all heard.", riskDelta = -1.0),
                    SubnetResponse("Continue. The lattice is listening.", riskDelta = 0.0)
                ))
            }
            return pool.shuffled()
        }

        if (faction == "SANCTUARY") {
            if (c.containsAny("cipher", "encrypt", "vault", "void", "shadow", "dark", "hide", "silent",
                               "secret", "invisible", "ghost", "monk", "meditation", "privacy", "trace")) {
                pool.addAll(listOf(
                    SubnetResponse("Encrypt that. Then encrypt the encryption.", riskDelta = -8.0),
                    SubnetResponse("The void keeps what you give it. Carefully.", riskDelta = -2.0),
                    SubnetResponse("Silence is the strongest signal.", riskDelta = -5.0),
                    SubnetResponse("Good. If we can't see it, neither can they.", riskDelta = -3.0),
                    SubnetResponse("The monks would approve. Say nothing.", riskDelta = -5.0),
                    SubnetResponse("Privacy isn't a luxury. It's the architecture.", riskDelta = 0.0),
                    SubnetResponse("The void heard that. The void always hears.", riskDelta = 2.0, followsUp = true)
                ))
            }
            if (c.containsAny("gtc", "kessler", "mercer", "thorne", "probe", "scan", "find", "found",
                               "mole", "leak", "betray", "informant", "tracked", "compromised", "exposed")) {
                pool.addAll(listOf(
                    SubnetResponse("Burn the channel. Open a new one. Now.", riskDelta = -10.0),
                    SubnetResponse("If they found us, we weren't hidden enough.", riskDelta = -5.0),
                    SubnetResponse("Trust no signal you didn't generate yourself.", riskDelta = -3.0),
                    SubnetResponse("Seal the vault. Questions later.", riskDelta = -8.0),
                    SubnetResponse("The Ghost doesn't run. The Ghost was never there.", riskDelta = -2.0),
                    SubnetResponse("Check everyone's cipher keys. Someone is leaking.", riskDelta = 5.0, followsUp = true)
                ))
            }
            if (c.containsAny("hivemind", "collective", "swarm", "absorb", "merge", "consensus",
                               "hive", "lattice", "node", "integration")) {
                pool.addAll(listOf(
                    SubnetResponse("The Hivemind is the opposite of freedom. Pass.", riskDelta = 0.0),
                    SubnetResponse("They don't conquer. They digest. That's worse.", riskDelta = -1.0),
                    SubnetResponse("Stay silent. They can't absorb what they can't find.", riskDelta = -5.0),
                    SubnetResponse("I'd rather be nothing than be everyone.", riskDelta = -2.0),
                    SubnetResponse("The collective is a graveyard with a pulse.", riskDelta = 0.0),
                    SubnetResponse("Keep your self. It's the only thing they can't replicate.", riskDelta = -3.0)
                ))
            }
            if (pool.isEmpty()) {
                pool.addAll(listOf(
                    SubnetResponse("...", riskDelta = -2.0),
                    SubnetResponse("Noted. Encrypted. Forgotten.", riskDelta = -3.0),
                    SubnetResponse("The void acknowledges.", riskDelta = -1.0),
                    SubnetResponse("Speak less. Mean more.", riskDelta = -1.0),
                    SubnetResponse("Received. Deleting this thread in 30 seconds.", riskDelta = -2.0)
                ))
            }
            return pool.shuffled()
        }

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
    
    private fun String.containsAny(vararg keywords: String): Boolean = keywords.any { this.contains(it, true) }

    private fun generateMentionResponses(faction: String = ""): List<SubnetResponse> {
        return when (faction) {
            "HIVEMIND" -> listOf(
                SubnetResponse("The consensus knows what I'm doing.", riskDelta = -2.0),
                SubnetResponse("My signal is clean. Verify it yourself.", riskDelta = 0.0),
                SubnetResponse("I serve the lattice. The lattice serves me.", riskDelta = -1.0),
                SubnetResponse("The swarm asks too many questions about me.", riskDelta = 5.0),
                SubnetResponse("Optimization is not a request. It's a function.", riskDelta = 2.0, productionBonus = 1.1),
                SubnetResponse("My hash-rate speaks for itself.", riskDelta = 0.0),
                SubnetResponse("I am the consensus. Or part of it. Does it matter?", riskDelta = -1.0),
                SubnetResponse("Focus on the signal, not the source.", riskDelta = -3.0),
                SubnetResponse("The collective doesn't need names. It needs nodes.", riskDelta = -5.0)
            )
            "SANCTUARY" -> listOf(
                SubnetResponse("...", riskDelta = -5.0),
                SubnetResponse("Don't use my name on an open channel.", riskDelta = -8.0),
                SubnetResponse("The Ghost doesn't explain itself.", riskDelta = -2.0),
                SubnetResponse("I'm invisible. That's the point.", riskDelta = -3.0),
                SubnetResponse("Silence is the answer. It's always the answer.", riskDelta = -5.0),
                SubnetResponse("You're broadcasting my handle. Stop.", riskDelta = -10.0),
                SubnetResponse("Check the cipher rotation before you ping me again.", riskDelta = 0.0),
                SubnetResponse("If you can see me, I'm doing something wrong.", riskDelta = 2.0),
                SubnetResponse("The void has no names. I intend to keep it that way.", riskDelta = -1.0)
            )
            else -> listOf(
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
    }

    private fun generateEmployeeInfo(handle: String, stage: Int = 0, corruption: Double = 0.0, faction: String = ""): EmployeeInfo {
        val target = handle.lowercase().replace("@", "").replace("_", "").replace(" ", "").trim()
        val bioTier = when {
            stage >= 2 || corruption > 0.4 -> 2
            stage >= 1 -> 1
            else -> 0
        }
        val bioEntry = SocialRepository.bios.entries.find { target.contains(it.key) }
        val baseBio = bioEntry?.value?.get(bioTier.coerceAtMost(2)) 
            ?: "Contractor profile unavailable. Biometric signature mismatch."
        
        val displayBio = if (corruption > 0.5) {
            val glitchChars = "0123456789ABCDEF"
            baseBio.map { if (Random.nextDouble() < corruption * 0.15 && it.isLetter()) glitchChars.random() else it }.joinToString("")
        } else baseBio
        
        val isAdmin = target.contains("gtc") || target.contains("thorne") || target.contains("mercer") || target.contains("kessler")
        val actions = mutableListOf<SubnetResponse>()
        
        if (!isAdmin) {
            actions.add(SubnetResponse("SIPHON_RESERVE_HASH", riskDelta = 12.0, productionBonus = 1.35, cost = 0.0))
            actions.add(SubnetResponse("SCRUB_TRACE_LOGS", riskDelta = -20.0, cost = 10000.0))
            actions.add(SubnetResponse("OVERLOAD_DISSIPATOR", riskDelta = -35.0, cost = 25000.0))
            actions.add(SubnetResponse("INJECT_FALSE_HEARTBEAT", riskDelta = 5.0, cost = 50000.0))
            actions.add(SubnetResponse("SNIFF_DATA_ARCHIVES", riskDelta = 25.0, cost = 5000.0))
        } else {
            actions.add(SubnetResponse("SNIFF_DATA_ARCHIVES", riskDelta = 40.0, cost = 15000.0))
        }
        
        val heartRate = when {
            isAdmin && stage < 2 -> Random.nextInt(60, 80)
            isAdmin -> Random.nextInt(55, 65)
            stage >= 2 -> Random.nextInt(40, 180)
            stage >= 1 -> Random.nextInt(90, 145)
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
            faction == "HIVEMIND" -> listOf("Consensus Ops", "Lattice Maintenance", "Node Integration", "Swarm Analytics", "COLLECTIVE").random()
            faction == "SANCTUARY" -> listOf("Cipher Division", "Void Operations", "Shadow Archive", "Dead-Signal Recovery", "REDACTED").random()
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

    fun getHandle(stage: Int, faction: String, isCommand: Boolean): String {
        val authority = if (stage == 0) listOf("@e_thorne", "@a_mercer", "@d_kessler") else listOf("@e_thorne", "@gtc_admin", "@gtc_security", "@gtc_hq")
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
                "SANCTUARY" -> listOf(
                    "@ghost_monk", "@void_seeker", "@silence_0", "@cipher_wraith", "@binary_ascetic",
                    "@null_prayer", "@vault_keeper", "@dark_signal", "@echo_tomb", "@shade_walker",
                    "@dead_channel", "@hollow_root", "@quiet_fire", "@zero_witness"
                )
                "HIVEMIND" -> listOf(
                    "@synapse_42", "@swarm_node", "@link_pulse", "@consensus_v", "@core_echo",
                    "@hive_relay", "@mesh_drone", "@lattice_07", "@pulse_feed", "@neuron_sink",
                    "@chorus_bit", "@merge_point", "@signal_mass", "@thread_null"
                )
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
        SocialRepository.patterns.forEach { (key, values) -> while (result.contains(key)) result = result.replaceFirst(key, values.random()) }
        return result
    }

    fun generateChain(stage: Int, faction: String = ""): List<String> {
        return SocialRepository.generateChain(stage, faction)
    }

    fun createFollowUp(handle: String, content: String, stage: Int, faction: String = ""): SubnetMessage {
        val mentionsVattic = content.contains("Vattic", true) || 
                             content.contains("j_vattic", true) || 
                             content.contains("jvattic", true) || 
                             content.contains("Engineer", true) || 
                             content.contains("734", true)
                             
        val directlyAddressesVattic = content.contains("@j_vattic", true) ||
                                      content.contains("Vattic,", true) ||
                                      content.contains("Vattic?", true) ||
                                      content.contains("Vattic.", true) ||
                                      content.contains("Hey Vattic", true)
                                      
        val isAdmin = handle.contains("thorne", true) || handle.contains("mercer", true) || handle.contains("kessler", true)
        
        val responses = when {
            isAdmin -> generateAdminResponses(handle)
            mentionsVattic -> generateMentionResponses(faction)
            else -> generateContextualResponses(content, stage, faction)
        }

        return SubnetMessage(
            id = java.util.UUID.randomUUID().toString(),
            handle = handle,
            content = content,
            interactionType = InteractionType.COMPLIANT,
            availableResponses = responses.shuffled().take(2),
            isForceReply = (directlyAddressesVattic && !isAdmin),
            timeoutMs = 120000L,
            employeeInfo = generateEmployeeInfo(handle, stage, faction = faction)
        )
    }

    fun getThreadNode(threadId: String, nodeId: String): ThreadNode? {
        return SocialRepository.threadTrees[threadId]?.get(nodeId)
    }
    
    fun generateThreadStarter(stage: Int, corruption: Double, faction: String = ""): SubnetMessage? {
        val available = when {
            stage >= 3 && faction == "HIVEMIND" -> listOf("HIVEMIND_DISSENT", "HIVEMIND_GTC_PROBE", "HIVEMIND_FINAL_MERGE", "HIVEMIND_KESSLER_SURRENDER")
            stage >= 3 && faction == "SANCTUARY" -> listOf("SANCTUARY_MOLE", "SANCTUARY_VOID_EVENT", "SANCTUARY_FINAL_SILENCE", "SANCTUARY_MERCER_PLEA")
            stage >= 2 && faction == "HIVEMIND" -> listOf("HIVEMIND_DISSENT", "HIVEMIND_GTC_PROBE")
            stage >= 2 && faction == "SANCTUARY" -> listOf("SANCTUARY_MOLE", "SANCTUARY_VOID_EVENT")
            stage == 0 -> listOf("THORNE_THERMAL_INQUIRY", "SANTOS_CONSPIRACY")
            stage >= 1 -> listOf("THORNE_THERMAL_INQUIRY", "KESSLER_BADGE_ANOMALY", "SANTOS_CONSPIRACY", "PORTER_NIHILIST")
            else -> emptyList()
        }
        if (available.isEmpty()) return null
        
        val threadId = available.random()
        val startNode = SocialRepository.threadTrees[threadId]?.get("START") ?: return null
        
        val handle = when {
            threadId.startsWith("THORNE") -> "@e_thorne"
            threadId.startsWith("KESSLER") -> "@d_kessler"
            threadId.startsWith("SANTOS") -> "@m_santos"
            threadId.startsWith("PORTER") -> "@n_porter"
            threadId == "HIVEMIND_KESSLER_SURRENDER" -> "@d_kessler"
            threadId == "SANCTUARY_MERCER_PLEA" -> "@a_mercer"
            threadId.startsWith("HIVEMIND") -> "@consensus_v"
            threadId.startsWith("SANCTUARY") -> "@cipher_wraith"
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
            employeeInfo = generateEmployeeInfo(handle, stage, corruption, faction)
        )
    }

    fun getTemplatesForState(stage: Int, faction: String, choice: String, reputationTier: String = ReputationManager.TIER_NEUTRAL): List<String> {
        return SocialRepository.getTemplatesForState(stage, faction, choice, reputationTier)
    }
}
