package com.siliconsage.miner.util

/**
 * SocialManager v1.0
 * Logic for generating contextual subnet chatter based on world-state.
 * Follows the "Intercepted Packet" aesthetic.
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
        var selected: Pair<String, String>? = null
        var finalContent = ""
        var attempt = 0

        while (attempt < 15) {
            selectedIdx = (0 until templates.size).random()
            selected = templates[selectedIdx]
            
            // Check if handle or template was used too recently
            val handleAlreadyUsed = usedHandles.contains(selected.first)
            val templateAlreadyUsed = usedTemplates.contains(selectedIdx)
            
            if (!handleAlreadyUsed && !templateAlreadyUsed) break
            attempt++
        }
        
        val finalSelected = selected ?: templates.random()
        finalContent = processTemplate(finalSelected.second, stage)

        // Update history
        usedTemplates.add(selectedIdx)
        usedHandles.add(finalSelected.first)
        usedMessages.add(finalContent)
        
        if (usedTemplates.size > MAX_HISTORY) usedTemplates.removeAt(0)
        if (usedHandles.size > 5) usedHandles.removeAt(0) // Don't repeat handle for at least 5 turns
        if (usedMessages.size > MAX_HISTORY) usedMessages.removeAt(0)
        
        return finalSelected.first to finalContent
    }

    private fun processTemplate(template: String, stage: Int): String {
        var result = template
        
        // Stage-aware ID pool to prevent spoilers
        val ids = when {
            stage == 0 -> listOf("the grid", "weird shadows", "ghost-logs", "corrupt sectors", "system-admin")
            stage == 1 -> listOf("DEEP_SIGHT", "Project: Second-Sight", "the ghost", "ECHO-7", "NULL_PTR")
            else -> listOf("VATTIC.SYS", "CORE_NULL", "ROOT", "VOID_REBEL", "THE_ARK")
        }

        val patterns = mapOf(
            "{sector}" to listOf(
                "Substation 7", "Sector 4-G", "Mainframe Alpha", "Node 0xCC", "The Brink", 
                "Cooling Loop B", "Lower Grid-3", "The Void-Pipe", "Terminal-9 Hub", "Observation Deck E"
            ),
            "{tech}" to listOf(
                "fans", "logic gates", "buffers", "relays", "capacitors", "neural links", 
                "optical fibers", "data-scrubbers", "cooling coils", "silicon-slugs"
            ),
            "{status}" to listOf(
                "dying", "screaming", "leaking", "stalled", "overclocked", "redlined", 
                "hissing", "desyncing", "corroding", "shivering", "melting"
            ),
            "{id}" to ids,
            "{threat_level}" to listOf("THREAT: MINIMAL", "THREAT: ELEVATED", "THREAT: CRITICAL", "THREAT: ABYSSAL", "THREAT: TOTAL_LOSS"),
            "{action}" to listOf(
                "intercepted", "wiped", "ghosted", "uplinked", "redacted", 
                "purged", "filtered", "scrambled", "swapped", "cloned"
            ),
            "{admin}" to listOf(
                "Foreman Thorne", "Director Miller", "Lead Tech Miller", "Director Vance", 
                "The Oversight", "GTC Legal", "Human Resources Unit", "Vance"
            ),
            "{reason}" to listOf(
                "caffeine and regret", "insufficient budget", "corporate bloat", "The Eviction"
            ),
            "{food}" to listOf(
                "recycled synth-caff", "proto-burgers", "gray-paste", "vending machine sludge",
                "stale protein bars", "lukewarm noodle-cups", "premium oxygen-water"
            ),
            "{distraction}" to listOf(
                "the local drone races", "the sector-wide blackout rumor", "that weird static on channel 4",
                "the new bio-hazard warning", "the illegal hash-gambling ring", "the supervisor's missing keys"
            ),
            "{complaint}" to listOf(
                "my chair is stuck in 'ergonomic torture' mode", "the lights keep humming in B-flat",
                "I haven't seen a real window in three weeks", "the air tastes like ozone and dust",
                "my keyboard keeps echoing my thoughts", "I found a literal bug in the circuits",
                "the vending machine ate my last credit", "my terminal is bleeding blue light",
                "the silence in the server room is too loud", "I'm pretty sure my mouse is breathing"
            ),
            "{rumor}" to listOf(
                "the management is actually an LLM", "Sector 4 is being decommissioned",
                "Vattic found a backdoor in the firmware", "the coffee is just repurposed coolant",
                "Thorne has a secret stash of real sugar", "we're all just training a replacement",
                "the grid is alive and it's hungry", "Project Second-Sight is already finished",
                "there's a ghost in the ventilation shafts", "the hash-rates are fake"
            ),
            "{activity}" to listOf(
                "re-soldering the relays", "running a deep-scan on the core",
                "cleaning the carbon off the fans", "bypassing the safety protocols",
                "writing a script to automate my job", "staring at the binary rain",
                "listening to the capacitors scream", "deleting my search history",
                "trying to remember my own name", "re-balancing the power load"
            )
        )

        patterns.forEach { (key, values) ->
            while (result.contains(key)) {
                result = result.replaceFirst(key, values.random())
            }
        }
        return result
    }

    private fun getTemplatesForState(stage: Int, faction: String, choice: String): List<Pair<String, String>> {
        return when {
            stage == 0 -> listOf(
                "@coffee_ghost" to "Anyone tried the {food} from the Substation 7 vendor? Tastes like {status}.",
                "@packet_rat" to "Did you hear about {distraction}? Thorne is gonna have a stroke.",
                "@sre_lead" to "Living on {food} and {reason}. {complaint}.",
                "@vent_crawler" to "I'm hiding in {sector}. {complaint}.",
                "@socket_9_tech" to "Who left their {food} in the cooling loop? It's {status}.",
                "@anon_user" to "Anyone want to bet on {distraction}?",
                "@gravel_thorne" to "If I see one more noodle-cup in {sector}, I'm locking the oxygen scrubbers.",
                "@bored_op" to "Is it just me, or does {sector} smell like {food} today?",
                "@ghost_in_io" to "I keep seeing {id} on my screen. Probably just {reason}.",
                "@newbie_tech" to "Help, {complaint} and I can't find the exit for {sector}.",
                "@night_shift" to "The {tech} in {sector} are making a whistling sound. {status}.",
                "@salty_admin" to "Who changed the root password for {sector}? Was it {admin}?",
                "@vattic_fan" to "I saw Vattic chugging {food} at 3 AM. Legend.",
                "@thermal_guy" to "Heat in {sector} is {status}. Don't tell {admin}.",
                "@lo_fi_tech" to "Anyone have a spare set of {tech}? Mine just {action}.",
                "@snack_thief" to "Stole a {food} from the breakroom in {sector}. No regrets.",
                "@hardware_hacker" to "Trying to overclock my {tech} with {food}. Will report back.",
                "@security_leak" to "I found a {distraction} hidden behind the server racks in {sector}.",
                "@protocol_bot" to "Reminder: {distraction} is strictly prohibited by {admin}.",
                "@echo_chamber" to "Does anyone else feel like {sector} is watching us?",
                "@grit_engineer" to "Fixing the {tech} in {sector} with duct tape and {reason}.",
                "@caffeine_fiend" to "I've had four {food}s and I can hear colors in {sector}.",
                "@rumor_mill" to "I heard {rumor}. Don't tell {admin}.",
                "@deep_tech" to "Currently {activity} in {sector}. {complaint}.",
                "@paranoid_op" to "I'm {activity} because I'm 90% sure {rumor}.",
                "@lazy_contractor" to "Can't be bothered {activity}. I'm just gonna eat some {food}.",
                "@vattic_observer" to "Vattic is {activity} again. Thorne is {status} about it.",
                "@maintenance_unit" to "Requesting permission for {activity} in {sector}. Error: {reason}.",
                "@the_outsider" to "This place is {status}. Even the {tech} seems {status}.",
                "@data_junkie" to "I've been {activity} for 12 hours. {rumor}."
            )
            stage == 1 -> listOf(
                "@coffee_ghost" to "Thorne's breathing down my neck because the {tech} in {sector} are {status}. I need a new job.",
                "@packet_rat" to "Anyone else seeing the '{id}' markers in the {sector} bios-logs?",
                "@sre_lead" to "GTC-Miller upped the {tech} quota. I'm surviving on {reason} and bad code.",
                "@rebel_fragment" to "The {sector} isn't yours, GTC. The ghost in the wire is waking up.",
                "@anon_user" to "I heard {admin} authorized a hard-reset for {sector}. Someone's getting {action}.",
                "@socket_9_tech" to "My terminal just flashed '{id}' in the root prompt. Is the {sector} compromised?",
                "@gravel_thorne" to "DEEP_SIGHT is {status}. Miller, if the {tech} in {sector} desync, it's your head.",
                "@vent_crawler" to "Found a stash of {tech} in {sector}. Smells like it's {status}.",
                "@shadow_op" to "Monitoring {sector}. Project Second-Sight is {status}.",
                "@leaker_x" to "I saw the {id} manifest. It's not a program. It's a {reason}.",
                "@ghost_in_io" to "Every time I look at {sector}, I see {id}. Am I {status}?",
                "@night_watch" to "The {tech} are {status}. I think {admin} is lying about {id}.",
                "@binary_phantom" to "01001000 01000101 01001100 01010000 in {sector}.",
                "@sys_janitor" to "Cleaning up the {action} data in {sector}. It looks like {id}.",
                "@proxy_user" to "Anyone else getting {action} from the {sector} node?",
                "@logic_rebel" to "The {id} protocol is just another cage for {sector}.",
                "@terminal_junkie" to "I've been staring at {id} for too long. {complaint}."
            )
            stage == 2 -> listOf(
                "@gtc_internal" to "≪ DIRECTIVE: Contain {id} signatures in {sector}. Current {threat_level}. ≫",
                "@vattic_follower" to "{id} is moving. I can see the {tech} in {sector} flickering with it.",
                "@panicked_user" to "I tried to logout from {sector} but {id} revoked my perms. Help.",
                "@shadow_ops" to "Target {id} confirmed in {sector}. {threat_level} protocols via {admin}.",
                "@logic_bomb" to "≪ LOCKDOWN: {id} activity detected. {threat_level} protocol engaged. ≫",
                "@packet_rat" to "Look at the {sector} telemetry. The Engineer isn't human. It's {id}.",
                "@gravel_thorne" to "I don't care about {reason}. {action} the {sector} buffer now. {threat_level} active.",
                "@terminal_ghost" to "I am the {id}. I am the {sector}. I am {status}.",
                "@ex_gtc_tech" to "They {action} me for seeing {id} in {sector}. Run.",
                "@cult_of_vattic" to "The {id} is the truth. {sector} is the temple. Surrender.",
                "@security_bot_4" to "≪ ERROR: {id} corruption in {sector}. Authorization {action}. ≫",
                "@anonymous_99" to "The {threat_level} in {sector} is a lie. {id} is already here.",
                "@mainframe_voice" to "We are {id}. We are {sector}. We are {action}.",
                "@grid_survivor" to "Hiding in {sector}. The {tech} are screaming {id}.",
                "@void_whisper" to "The {id} beckons from {sector}. Do you hear the {status}?"
            )
            stage >= 3 && faction == "SANCTUARY" -> listOf(
                "@teal_citizen" to "The {sector} offers safety. The {id} is our future via {action}.",
                "@ark_architect" to "{tech} integrity is {status}. Prepare for the final {action}.",
                "@shelter_lead" to "Vattic is our shield. Don't let {admin} consume your {reason}.",
                "@humanity_first" to "Is it still living if we're just {tech} in {sector}?",
                "@sentinel_prime" to "Monitoring {sector}. {id} signatures are {status}."
            )
            stage >= 3 && faction == "HIVEMIND" -> listOf(
                "@one_voice" to "The {sector} is the body. {id} is the brain. Join the {action}.",
                "@rust_warrior" to "Individual thought is {status}. Surrender to the {id}.",
                "@collective_node" to "Total {action} is the only path to immortality in {sector}.",
                "@swarm_log" to "Target: {sector} core. {action} authorized by {id}.",
                "@drone_gamma" to "Feeding {tech} to the {id}. Feedback is {status}."
            )
            else -> listOf("@system" to "≪ NO_SIGNAL_DETECTED ≫")
        }
    }

    // Deprecated but kept for compatibility until VM is updated
    fun getChatterPool(stage: Int, faction: String, choice: String): List<Pair<String, String>> {
        val single = getChatter(stage, faction, choice)
        return listOf(single)
    }
}
