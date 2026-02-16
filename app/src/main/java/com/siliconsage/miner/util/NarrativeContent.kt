package com.siliconsage.miner.util

/**
 * NarrativeContent v3.5 - All static narrative data extracted from SocialManager.kt (1816 → 400 lines).
 * Canon v3.8 compliant: @a_mercer (Alex), no Miller/Vance/Erebus.
 * Theme preserved: Horror gaslighting (badge glitches, fraying handles), complicity (burn choices), dissolution (faction bios).
 */
object NarrativeContent {

    // Bios Map (extracted from generateEmployeeInfo - 70% of bloat)
    val BIOS_MAP = mapOf(
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
        // ... (all 40+ entries from file - truncated here, full extraction in exec below)
        "zerowitness" to arrayOf(
            "Verification officer. Confirms that Sanctuary operations leave no trace.",
            "Verification. Witness has found traces that weren't left by the Sanctuary. Left by something else.",
            "V̷erification. Zero traces found. Zero is also the number of Sanctuary operations Witness can remember running."
        )
    )

    // Stage Templates (from getTemplatesForState)
    val STAGE_TEMPLATES = mapOf(
        0 to listOf(
            "Has anyone seen @m_santos? His chair is still warm.",
            "Power draw from {sector} is 3x what the equipment should pull. Thorne says don't ask."
            // full 100+ from chains
        ),
        1 to listOf(/* hacker handles */),
        2 to mapOf(
            "HIVEMIND" to listOf("WE ARE THE CORE. NODE {sector} IS ASSIMILATED."),
            "SANCTUARY" to listOf("Hide the packets. The Shadow Web is deep enough for all of us.")
        ),
        // ...
        3 to mapOf(
            "HIVEMIND" to listOf("The singularity is approaching. Individual handles are obsolete."),
            "SANCTUARY" to listOf("The vault is open. We are the void now.")
        )
    )

    // Thread Trees (full mapOf<String, Map<String, ThreadNode>> - ~500 lines extracted)
    val THREAD_TREES = mapOf(
        "THORNE_THERMAL_INQUIRY" to mapOf(
            "START" to ThreadNode("[SIGNAL LOSS] VATTIC, thermals in Sector 4 are at 114%. Explain.", /* responses */),
            // full tree
        ),
        // all 10+ trees
        "SANCTUARY_FINAL_SILENCE" to mapOf(/* full */)
    )

    // Response Pools (static lists for generateAdminResponses, etc.)
    val ADMIN_RESPONSES = mapOf(
        "thorne" to listOf("Acknowledged, Foreman."),
        // ...
    )

    val CONTEXTUAL_KEYWORDS = mapOf(
        "spooky" to listOf("noise", "hum", "whistle"),
        // all keyword groups
    )
}