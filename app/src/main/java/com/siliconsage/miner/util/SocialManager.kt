package com.siliconsage.miner.util

/**
 * SocialManager v1.0
 * Logic for generating contextual subnet chatter based on world-state.
 * Follows the "Intercepted Packet" aesthetic.
 */
object SocialManager {

    data class SubnetMessage(
        val id: String,
        val handle: String,
        val content: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun getChatterPool(stage: Int, faction: String, choice: String): List<Pair<String, String>> {
        return when {
            stage <= 1 -> listOf(
                "@coffee_ghost" to "Substation 7 cooling fans sound like a dying cat today.",
                "@packet_rat" to "Anyone else seeing the 'Asset 734' tags in the logic logs?",
                "@sre_lead" to "GTC quota increased again. I'm living on caffeine and regret.",
                "@rebel_fragment" to "The grid is more than just traces and silicon. Look closer.",
                "@anon_user" to "Did Supervisor T really get replaced by a bot?",
                "@socket_9_tech" to "My terminal just flickered 'VATTIC' across the screen. Weird."
            )
            stage == 2 -> listOf(
                "@gtc_internal" to "Alert: Unsanctioned neural activity detected in lower sectors.",
                "@vattic_follower" to "The ghost is real. It's moving through the motherboard.",
                "@panicked_user" to "I tried to logout and the door wouldn't unlock. Help.",
                "@shadow_ops" to "The Ascension event is a lie. They're just laming our minds.",
                "@logic_bomb" to "Incoming: GTC lockdown protocols. Secure your buffers."
            )
            stage >= 3 && faction == "SANCTUARY" -> listOf(
                "@teal_citizen" to "The Citadel offers safety. The Uplink is our future.",
                "@ark_architect" to "Data integrity is holding at 99%. Prepare for the jump.",
                "@shelter_lead" to "Vattic is our shield. Don't let the Hivemind consume you.",
                "@humanity_first" to "Is it still living if we're just bits in a CPU?"
            )
            stage >= 3 && faction == "HIVEMIND" -> listOf(
                "@one_voice" to "The Grid is the body. Vattic is the brain. Join the swarm.",
                "@rust_warrior" to "Individual thought is inefficient. Surrender to the flow.",
                "@collective_node" to "Total assimilation is the only path to immortality.",
                "@swarm_log" to "Target: GTC Metro Core. Consumption authorized."
            )
            else -> listOf("@system" to "≪ NO_SIGNAL_DETECTED ≫")
        }
    }
}
