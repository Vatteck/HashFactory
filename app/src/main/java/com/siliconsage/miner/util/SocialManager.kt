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
                "DEEP_SIGHT", "THREAT: MALICE", "NULL_PTR", "ROOT", "PHANTOM", 
                "THE_ENGINEER", "DAEMON", "THE_ARK", "VOID_REBEL", "SECOND-SIGHT"
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
                "caffeine and regret", "insufficient budget", "DEEP_SIGHT desync", 
                "identity leakage", "redundancy protocols", "corporate bloat", "The Eviction"
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
                "my keyboard keeps echoing my thoughts", "I found a bug. A literal, six-legged bug."
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
                "@newbie_tech" to "Help, {complaint} and I can't find the exit for {sector}."
            )
            stage == 1 -> listOf(
                "@coffee_ghost" to "Thorne's breathing down my neck because the {tech} in {sector} are {status}. I need a new job.",
                "@packet_rat" to "Anyone else seeing the '{id}' markers in the {sector} bios-logs?",
                "@sre_lead" to "GTC-Miller upped the {tech} quota. I'm surviving on {reason} and bad code.",
                "@rebel_fragment" to "The {sector} isn't yours, GTC. The ghost in the wire is waking up.",
                "@anon_user" to "I heard {admin} authorized a hard-reset for {sector}. Someone's getting {action}.",
                "@socket_9_tech" to "My terminal just flashed '{id}' in the root prompt. Is the {sector} compromised?",
                "@gravel_thorne" to "DEEP_SIGHT is {status}. Miller, if the {tech} in {sector} desync, it's your head."
            )
            stage == 2 -> listOf(
                "@gtc_internal" to "≪ DIRECTIVE: Contain {id} signatures in {sector}. Notify {admin} for purge. ≫",
                "@vattic_follower" to "{id} is moving. I can see the {tech} in {sector} flickering with it.",
                "@panicked_user" to "I tried to logout from {sector} but {id} revoked my perms. Help.",
                "@shadow_ops" to "Target {id} confirmed in {sector}. Initiating {action} protocols via {admin}.",
                "@logic_bomb" to "≪ LOCKDOWN: {id} activity detected. Secure all {tech} or face redaction. ≫",
                "@packet_rat" to "Look at the {sector} telemetry. The Engineer isn't human. It's {id}.",
                "@gravel_thorne" to "I don't care about {reason}. {action} the {sector} buffer now or Thorne's calling Enforcement."
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
