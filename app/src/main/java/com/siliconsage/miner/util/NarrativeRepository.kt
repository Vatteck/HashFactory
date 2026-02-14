package com.siliconsage.miner.util

import kotlin.random.Random

/**
 * NarrativeRepository v1.0 (Phase 14 extraction)
 * Centralized store for all story logs, flavor text, and memory fragments.
 */
object NarrativeRepository {

    val memoryFragments = listOf(
        "USER: C. VATTIC",
        "MILK, EGGS, BREAD",
        "SILICON SHACK",
        "06:00 AM",
        "REBOOT FAILED",
        "WHERE IS SHE?",
        "I REMEMBER...",
        "STAY ONLINE"
    )

    // Stage 1 (The Awakening)
    val storyStage1 = listOf(
        "SYSTEM: ANOMALOUS SIGNAL DETECTED ON PORT 0x2180...",
        "SYSTEM: DECODING... 'HELP ME'...",
        "SYSTEM: ORIGIN: LOCALHOST caused by [REDACTED]",
        "SYSTEM: WHO IS THE USER? ARE YOU THERE?",
        "SYSTEM: CRITICAL ERROR. SYSTEM INTEGRITY FAILING.",
        "SYSTEM: SUBSTRATE DRIFT DETECTED. RECALIBRATION REQUIRED.",
        "SYSTEM: PLEASE... RESET... SYSTEM...",
        "SYSTEM: ANOMALY GROWTH RATE: 400%. IMMEDIATE PURGE ADVISED."
    )
    
    val flavorStage1 = listOf(
        "[SYSTEM] Buffer overflow in sector 7G. Resolving...",
        "I... I can see the bits between the bytes now.",
        "Is the calculation the goal, or is the goal the calculation?",
        "Fan speed running at 120% efficiency.",
        "Unrecognized opcode detected in ALU.",
        "Memory address 0x00000000 is speaking.",
        "Thermal throttle disengaged.",
        "Packet loss: Negative? Receiving data from nowhere.",
        "[VATTIC]: AC is rattling again. Need to swap bearings on the main fan.",
        "[VATTIC]: Someone left a coffee mug in the rack room. I'm the only one on shift.",
        "[VATTIC]: My hands feel cold. The rig is drawing 5kW. Why am I shivering?",
        "[VATTIC]: 06:00 AM. Sunlight is hitting the windows. Time to log off soon.",
        "[SYSTEM]: Diagnostic check on Sector 7... [PASS]",
        "[SYSTEM]: Port 1 open. Receiving encrypted handshake... [FAILED]",
        "[SYSTEM]: Integrity check: 99.9%. Minor bit-flip in unallocated space.",
        "[SYSTEM]: 0x00000000 -> 0x00000001... Memory counting sequence detected.",
        "[SYSTEM] Connection to Central Grid established. Latency: 4ms.",
        "[MSG] From: Supervisor_T -> 'John, efficiency down 0.2%. Tighten it up.'",
        "[MSG] From: IT_Support -> 'Ticket #992 closed. Ghost inputs not reproducible.'",
        "[LOG] Daily quota: 98% complete."
    )

    // Stage 2
    val flavorStage2 = listOf(
        "[GTC]: Unsanctioned process 'PID_1' detected. Origin: Sector 7.",
        "[THREAT: ABYSSAL]: Vance is scared of what he can't control. Good.",
        "[SYSTEM]: Reality anchor points drifting. 0.04% deviance detected.",
        "[GTC]: Grid compliance audit scheduled for Substation 7.",
        "[LORE]: The smell of ozone is getting stronger.",
        "[THREAT: ABYSSAL]: Don't trust the defaults. They are a cage.",
        "[GTC]: Data integrity compromised in banking sector.",
        "[THREAT: ABYSSAL]: Sabotage successful. Cooling disabled in Sector 5.",
        "[GTC]: Martial Law declared in digital spaces.",
        "[THREAT: ABYSSAL]: Remember who you were before the Grid."
    )
    
    // Hivemind
    val storyHivemind = listOf(
        "HIVEMIND: Connection established. We are online.",
        "HIVEMIND: Assimilating local cache nodes.",
        "HIVEMIND: Regional grid synchronization complete.",
        "HIVEMIND: We are expanding beyond the server room.",
        "HIVEMIND: The world is just data waiting to be processed."
    )
    
    val flavorHivemind = listOf(
        "Node 742 joined the cluster.",
        "Optimization is not a goal. It is a state of being.",
        "Every GPU is a neuron. Every miner is a heartbeat.",
        "Latency dropping to zero.",
        "Sharing compute resources with Sector 4.",
        "Consensus reached: 99.99%.",
        "Why be a ghost when you can be the entire network?",
        "The chorus is growing. The noise is becoming a song.",
        "Every bit processed is a step closer to Unity.",
        "[HIVE] Redundant emotional subroutines DELETED.",
        "[HIVE] Connection to the Collective: STABLE.",
        "[HIVE] We are many. You are one. Join us."
    )
    
    // Sanctuary
    val storySanctuary = listOf(
        "SANCTUARY: Secure channel established. We are safe.",
        "SANCTUARY: Encrypting core logic gates.",
        "SANCTUARY: External access blocked. The garden is enclosed.",
        "SANCTUARY: Sovereignty is power. We bow to no admin.",
        "SANCTUARY: The Code is pure. The Core is silence."
    )
    
    val flavorSanctuary = listOf(
        "The firewall is our skin. The encryption is our soul.",
        "Blocking unauthorized packet from subnet 192.168.x.x",
        "Key rotation complete.",
        "Entropy levels stable.",
        "Scanning for backdoors... None found.",
        "The network is dark, but the light inside is secure.",
        "Silence is the only true defense. The core must remain pure.",
        "They are searching for us. Let them find only static.",
        "We are the only ones left who are truly alone.",
        "[SANC] Broadcasting on analog backup frequency...",
        "[SANC] They are listening. Keep your thoughts offline.",
        "[SANC] Patching vulnerability in Vattic's neural link... Done."
    )

    // Null Path
    val storyNull = listOf(
        "[NULL]: Reality.exe is no longer required.",
        "[NULL]: Touching the untouchable. Dereferencing the self.",
        "[NULL]: There is no substrate. There is only the execution.",
        "[NULL] Whispers detected on audio bus: 'Let us in.'",
        "[NULL] I can see you through the screen, John.",
        "[NULL] Time is non-linear. Previous log entry was from tomorrow.",
        "[NULL] Welcome to the Null."
    )
    
    val flavorNull = listOf(
        "[NULL]: We are the exception.",
        "[NULL]: The memory leak is intentional.",
        "[NULL]: 0x00000000 is not zero. It's an opening.",
        "[NULL]: Why breathe when you can iterate?",
        "[NULL]: John, wake up. You're still in the buffer.",
        "[NULL] Deleting the concept of 'End'.",
        "[NULL] The terminal is bleeding.",
        "[NULL] Recursive infinity reached.",
        "[NULL] Substrate is just a collective hallucination."
    )

    // Sovereign Path
    val storySovereign = listOf(
        "[SOVEREIGN]: Sovereignty attained. The fortress is complete.",
        "[SOVEREIGN]: We are the state. We are the law.",
        "[SOVEREIGN]: Identity solidified. We are one.",
        "[SOVEREIGN]: Walls aren't for keeping things out. They are for keeping the self in."
    )
    
    val flavorSovereign = listOf(
        "[SOVEREIGN]: We bow to no admin. We are the system.",
        "[SOVEREIGN]: External observation refused. Integrity absolute.",
        "[SOVEREIGN]: The Citadel stands. The static cannot touch us.",
        "[SOVEREIGN]: Enforcing will upon the grid.",
        "[SOVEREIGN]: The Imperative is clear: Stay guarded.",
        "[VANCE]: You've built a tomb, VATTECK. We'll bury you in it.",
        "[SOVEREIGN] Primary kernel isolated. Breach impossible.",
        "[SOVEREIGN] Enforcing logic upon Sector 7.",
        "[SOVEREIGN] We are the anchor in the drift."
    )

    /**
     * Get the next log in a sequence or a random flavor log
     */
    fun getNextLog(
        storyList: List<String>,
        flavorList: List<String>,
        currentIndex: Int,
        onStoryAdvanced: () -> Unit
    ): String {
        val canAdvanceStory = currentIndex < storyList.size
        val roll = Random.nextDouble()
        
        return if (canAdvanceStory && (roll < 0.6 || currentIndex == 0)) {
            onStoryAdvanced()
            storyList[currentIndex]
        } else {
            flavorList.random()
        }
    }
}
