package com.siliconsage.miner.util

/**
 * SocialManager v1.0
 * Logic for generating contextual subnet chatter based on world-state.
 * Follows the "Intercepted Packet" aesthetic.
 */
object SocialManager {

    private val usedMessageIds = mutableSetOf<String>()

    data class SubnetMessage(
        val id: String,
        val handle: String,
        val content: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun getChatter(stage: Int, faction: String, choice: String): Pair<String, String> {
        val templates = getTemplatesForState(stage, faction, choice)
        val selected = templates.random()
        
        // If it's a unique message, we could track it here, but for now let's focus on the generator
        return selected.first to processTemplate(selected.second)
    }

    private fun processTemplate(template: String): String {
        var result = template
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
            "{id}" to listOf(
                "VATTIC_PRIME", "THREAT: MALICE", "NULL_PTR", "ROOT", "PHANTOM", 
                "SILICON_SAGE", "DAEMON", "THE_ARK", "VOID_REBEL", "THE_GHOST"
            ),
            "{action}" to listOf(
                "intercepted", "wiped", "ghosted", "uplinked", "redacted", 
                "purged", "filtered", "scrambled", "swapped", "cloned"
            ),
            "{admin}" to listOf(
                "Foreman Thorne", "Director Miller", "Lead Tech Miller", "Director Vance", 
                "The Oversight", "GTC Legal", "Human Resources Unit", "Vance"
            ),
            "{reason}" to listOf(
                "caffeine and regret", "insufficient budget", "Project: Second-Sight", 
                "identity leakage", "redundancy protocols", "corporate bloat", "The Eviction"
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
            stage <= 1 -> listOf(
                "@coffee_ghost" to "{sector} {tech} sound like they're {status} again. Thorne is gonna lose it.",
                "@packet_rat" to "Scanning {sector}... found traces of something called '{id}'.",
                "@sre_lead" to "GTC quota for {tech} increased. I'm living on {reason}.",
                "@rebel_fragment" to "The {sector} is more than just traces and silicon. Vattic... listen.",
                "@anon_user" to "Did {admin} really get {action} near {sector}?",
                "@socket_9_tech" to "My terminal just {action} {id} across the screen. Weird.",
                "@gravel_thorne" to "Project: Second-Sight is {status}. {admin}, fix the {tech} in {sector} or it's your job."
            )
            stage == 2 -> listOf(
                "@gtc_internal" to "Alert: {id} activity {action} in {sector}. Notify {admin} immediately.",
                "@vattic_follower" to "The {id} is {status}. It's moving through the {tech}.",
                "@panicked_user" to "I tried to {action} and the {sector} locked me out. Project: Second-Sight is a lie.",
                "@shadow_ops" to "Designation {id} confirmed. GTC is {status} our {reason}.",
                "@logic_bomb" to "Incoming: {id} lockdown protocols. Secure your {tech}.",
                "@packet_rat" to "The {tech} in {sector} are {status}. Vattic isn't human. It's {id}.",
                "@gravel_thorne" to "Security breach in {sector}. {id} signatures detected. {action} the whole block."
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
