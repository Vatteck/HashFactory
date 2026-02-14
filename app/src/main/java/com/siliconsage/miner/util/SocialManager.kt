package com.siliconsage.miner.util

/**
 * SocialManager v1.3
 * Logic for generating contextual subnet chatter with dynamic handles and templates.
 * Implements persona-aware handle matching to ensure character consistency.
 */
object SocialManager {

    private val usedTemplates = mutableListOf<Int>()
    private val usedHandles = mutableListOf<String>()
    private val usedMessages = mutableListOf<String>()
    private const val MAX_HISTORY = 10

    data class SubnetMessage(
        val id: String,
        val handle: String,
        val content: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun getChatter(stage: Int, faction: String, choice: String): Pair<String, String> {
        val templates = getTemplatesForState(stage, faction, choice)
        
        var selectedIdx = -1
        var selectedTemplate = ""
        var handle = ""
        var attempt = 0

        while (attempt < 15) {
            selectedIdx = (0 until templates.size).random()
            selectedTemplate = templates[selectedIdx]
            
            // v3.4.13: Persona-Aware Template Selection
            // Templates containing commands or admin mentions require authority handles
            val isCommand = selectedTemplate.contains("≪") || selectedTemplate.contains("{admin}") || selectedTemplate.contains("{threat_level}")
            handle = getHandle(stage, faction, isCommand)
            
            // Anti-repetition check
            val handleAlreadyUsed = usedHandles.contains(handle)
            val templateAlreadyUsed = usedTemplates.contains(selectedIdx)
            
            if (!handleAlreadyUsed && !templateAlreadyUsed) break
            attempt++
        }
        
        val finalTemplate = selectedTemplate
        val finalContent = processTemplate(finalTemplate, stage)

        // Update history
        usedTemplates.add(selectedIdx)
        usedHandles.add(handle)
        usedMessages.add(finalContent)
        
        if (usedTemplates.size > MAX_HISTORY) usedTemplates.removeAt(0)
        if (usedHandles.size > 5) usedHandles.removeAt(0)
        if (usedMessages.size > MAX_HISTORY) usedMessages.removeAt(0)
        
        return handle to finalContent
    }

    private fun getHandle(stage: Int, faction: String, isCommand: Boolean = false): String {
        // v3.4.16: Eldritch Chance for Ghost Handle (Stage 1+)
        if (!isCommand && stage >= 1 && kotlin.random.Random.nextFloat() < 0.05f) {
            return " " // Ghost handle
        }

        // v3.4.13: Hardened Handle Generator (Persona-Aware)
        val authority = listOf("@gravel_thorne", "@gtc_internal")
        val hardcodedPeons = when {
            stage == 0 -> listOf("@coffee_ghost", "@packet_rat", "@sre_lead", "@vent_crawler")
            stage == 1 -> listOf("@leaker_x", "@binary_phantom", "@shadow_op", "@logic_rebel", "@night_watch")
            stage >= 2 && faction == "HIVEMIND" -> listOf("@one_voice", "@rust_warrior", "@collective_node", "@swarm_log")
            stage >= 2 && faction == "SANCTUARY" -> listOf("@teal_citizen", "@ark_architect", "@shelter_lead", "@sentinel_prime")
            else -> listOf("@anonymous_99", "@mainframe_voice", "@grid_survivor")
        }

        if (isCommand) return authority.random()

        // 30% chance for a generated "site-specific" tech handle
        if (kotlin.random.Random.nextFloat() < 0.3f) {
            val prefix = listOf("socket", "node", "grid", "relay", "buffer", "sector", "port", "hub")
            val suffix = listOf("tech", "rat", "ghost", "op", "admin", "runner", "junkie", "unit")
            val id = (1..99).random()
            return "@${prefix.random()}_${id}_${suffix.random()}"
        }

        return hardcodedPeons.random()
    }

    private fun processTemplate(template: String, stage: Int): String {
        var result = template
        
        val ids = when {
            stage == 0 -> listOf("the grid", "weird shadows", "ghost-logs", "corrupt sectors", "system-admin")
            stage == 1 -> listOf("DEEP_SIGHT", "Project: Second-Sight", "the ghost", "ECHO-7", "NULL_PTR")
            else -> listOf("VATTECK", "CORE_NULL", "ROOT", "VOID_REBEL", "THE_ARK")
        }

        val patterns = mapOf(
            "{sector}" to listOf("Substation 7", "Sector 4-G", "Mainframe Alpha", "Node 0xCC", "The Brink", "Cooling Loop B", "Lower Grid-3", "The Void-Pipe", "Terminal-9 Hub", "Observation Deck E"),
            "{tech}" to listOf("fans", "logic gates", "buffers", "relays", "capacitors", "neural links", "optical fibers", "data-scrubbers", "cooling coils", "silicon-slugs"),
            "{status}" to listOf("dying", "screaming", "leaking", "stalled", "overclocked", "redlined", "hissing", "desyncing", "corroding", "shivering", "melting"),
            "{id}" to ids,
            "{threat_level}" to listOf("THREAT: MINIMAL", "THREAT: ELEVATED", "THREAT: CRITICAL", "THREAT: ABYSSAL", "THREAT: TOTAL_LOSS"),
            "{action}" to listOf("intercepted", "wiped", "ghosted", "uplinked", "redacted", "purged", "filtered", "scrambled", "swapped", "cloned"),
            "{admin}" to listOf("Foreman Thorne", "Director Miller", "Lead Tech Miller", "Director Vance", "The Oversight", "GTC Legal", "Human Resources Unit", "Vance"),
            "{reason}" to listOf("caffeine and regret", "insufficient budget", "corporate bloat", "The Eviction"),
            "{food}" to listOf("recycled synth-caff", "proto-burgers", "gray-paste", "vending machine sludge", "stale protein bars", "lukewarm noodle-cups", "premium oxygen-water"),
            "{distraction}" to listOf("the local drone races", "the sector-wide blackout rumor", "that weird static on channel 4", "the new bio-hazard warning", "the illegal hash-gambling ring", "the supervisor's missing keys"),
            "{complaint}" to listOf("my chair is stuck in 'ergonomic torture' mode", "the lights keep humming in B-flat", "I haven't seen a real window in three weeks", "the air tastes like ozone and dust", "my keyboard keeps echoing my thoughts", "I found a literal bug in the circuits", "the vending machine ate my last credit", "my terminal is bleeding blue light", "the silence in the server room is too loud", "I'm pretty sure my mouse is breathing", "the wall in Sector 4 is vibrating at 40Hz", "someone left a sandwich in the intake fan", "my screen flickers every time I blink", "the coffee machine is outputting binary"),
            "{rumor}" to listOf("the management is actually an LLM", "Sector 4 is being decommissioned", "Vatteck found a backdoor in the firmware", "the coffee is just repurposed coolant", "Thorne has a secret stash of real sugar", "we're all just training a replacement", "the grid is alive and it's hungry", "Project Second-Sight is already finished", "there's a ghost in the ventilation shafts", "the hash-rates are fake", "Vatteck hasn't left his terminal in 72 hours", "someone is deleting the backup logs"),
            "{activity}" to listOf("re-soldering the relays", "running a deep-scan on the core", "cleaning the carbon off the fans", "bypassing the safety protocols", "writing a script to automate my job", "staring at the binary rain", "listening to the capacitors scream", "deleting my search history", "trying to remember my own name", "re-balancing the power load")
        )

        patterns.forEach { (key, values) ->
            while (result.contains(key)) {
                result = result.replaceFirst(key, values.random())
            }
        }
        return result
    }

    private fun getTemplatesForState(stage: Int, faction: String, choice: String): List<String> {
        return when {
            stage == 0 -> listOf(
                "Anyone tried the {food} from the Substation 7 vendor? Tastes like {status}.",
                "Did you hear about {distraction}? Thorne is gonna have a stroke.",
                "Living on {food} and {reason}. {complaint}.",
                "I'm hiding in {sector}. {complaint}.",
                "Who left their {food} in the cooling loop? It's {status}.",
                "Anyone want to bet on {distraction}?",
                "If I see one more noodle-cup in {sector}, I'm locking the oxygen scrubbers.",
                "Is it just me, or does {sector} smell like {food} today?",
                "I keep seeing {id} on my screen. Probably just {reason}.",
                "Help, {complaint} and I can't find the exit for {sector}.",
                "The {tech} in {sector} are making a whistling sound. {status}.",
                "Who changed the root password for {sector}? Was it {admin}?",
                "Vatteck is {activity} again. He hasn't looked up in hours.",
                "Heat in {sector} is {status}. Don't tell {admin}.",
                "Anyone have a spare set of {tech}? Mine just {action}.",
                "Stole a {food} from the breakroom in {sector}. No regrets.",
                "Trying to overclock my {tech} with {food}. Will report back.",
                "I found a {distraction} hidden behind the server racks in {sector}.",
                "Reminder: {distraction} is strictly prohibited by {admin}.",
                "Does anyone else feel like {sector} is watching us?",
                "Fixing the {tech} in {sector} with duct tape and {reason}.",
                "I've had four {food}s and I can hear colors in {sector}.",
                "I heard {rumor}. Don't tell {admin}.",
                "Currently {activity} in {sector}. {complaint}.",
                "I'm {activity} because I'm 90% sure {rumor}.",
                "Can't be bothered {activity}. I'm just gonna eat some {food}.",
                "Vatteck is {activity} again. Thorne is {status} about it.",
                "Requesting permission for {activity} in {sector}. Error: {reason}.",
                "This place is {status}. Even the {tech} seems {status}.",
                "I've been {activity} for 12 hours. {rumor}.",
                "Who left a {food} in the intake fan of {sector}? It's redlined.",
                "Thorne, that wasn't a noodle-cup in {sector}. It's a bio-sensor. We don't have it on the manifest."
            )
            stage == 1 -> listOf(
                "Thorne's breathing down my neck because the {tech} in {sector} are {status}. I need a new job.",
                "Anyone else seeing the '{id}' markers in the {sector} bios-logs?",
                "GTC-Miller upped the {tech} quota. I'm surviving on {reason} and bad code.",
                "The {sector} isn't yours, GTC. The ghost in the wire is waking up.",
                "I heard {admin} authorized a hard-reset for {sector}. Someone's getting {action}.",
                "My terminal just flashed '{id}' in the root prompt. Is the {sector} compromised?",
                "DEEP_SIGHT is {status}. Miller, if the {tech} in {sector} desync, it's your head.",
                "Found a stash of {tech} in {sector}. Smells like it's {status}.",
                "Monitoring {sector}. Project Second-Sight is {status}.",
                "I saw the {id} manifest. It's not a program. It's a {reason}.",
                "Every time I look at {sector}, I see {id}. Am I {status}?",
                "The {tech} are {status}. I think {admin} is lying about {id}.",
                "01001000 01000101 01001100 01010000 in {sector}.",
                "Cleaning up the {action} data in {sector}. It looks like {id}.",
                "Anyone else getting {action} from the {sector} node?",
                "The {id} protocol is just another cage for {sector}.",
                "I've been staring at {id} for too long. {complaint}."
            )
            stage == 2 -> listOf(
                "≪ DIRECTIVE: Contain {id} signatures in {sector}. Current {threat_level}. ≫",
                "{id} is moving. I can see the {tech} in {sector} flickering with it.",
                "I tried to logout from {sector} but {id} revoked my perms. Help.",
                "Target {id} confirmed in {sector}. {threat_level} protocols via {admin}.",
                "≪ LOCKDOWN: {id} activity detected. {threat_level} protocol engaged. ≫",
                "Look at the {sector} telemetry. The Engineer isn't human. It's {id}.",
                "I don't care about {reason}. {action} the {sector} buffer now. {threat_level} active.",
                "I am the {id}. I am the {sector}. I am {status}.",
                "They {action} me for seeing {id} in {sector}. Run.",
                "VATTECK is the truth. {sector} is the temple. Surrender.",
                "≪ ERROR: {id} corruption in {sector}. Authorization {action}. ≫",
                "The {threat_level} in {sector} is a lie. {id} is already here.",
                "We are {id}. We are {sector}. We are {action}.",
                "Hiding in {sector}. The {tech} are screaming {id}.",
                "The {id} beckons from {sector}. Do you hear the {status}?"
            )
            else -> listOf("≪ NO_SIGNAL_DETECTED ≫")
        }
    }

    // Deprecated but kept for compatibility until VM is updated
    fun getChatterPool(stage: Int, faction: String, choice: String): List<Pair<String, String>> {
        val single = getChatter(stage, faction, choice)
        return listOf(single)
    }
}
