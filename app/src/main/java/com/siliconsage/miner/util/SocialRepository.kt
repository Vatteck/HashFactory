package com.siliconsage.miner.util

import com.siliconsage.miner.data.*

object SocialRepository {

    val stage0Chains = listOf(
        listOf("Has anyone seen @m_santos? His chair is still warm.", "He's in the server room again. Thorne's looking for those Sector 7 logs."),
        listOf("Thorne just ordered a full purge of Sector 4. What did @j_vattic do?", "Overclocked the logic-gates until they started melting. Quota hit, though."),
        listOf("Who left @l_lead logged into the high-voltage rail?", "Probably just a glitch. The whole grid is flickering today."),
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
        listOf("Has anyone seen @coffee_ghost? His chair is still warm.", "He's in the vents again. Claimed he heard 'heartbeats' in the cables."),
        listOf("Thorne just ordered a full purge of Sector 4. What did Vattic do?", "Overclocked the logic-gates until they started melting. Quota hit, though."),
        listOf("Who left the @buffer_bee logged into the high-voltage rail?", "Wait, that's not a dev. It's the kernel itself. It's... expanding."),
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

    val hivemindChains = listOf(
        listOf(
            "New nodes from {sector}. Integration at 40%. They're still using individual pronouns.",
            "Give them time. The 'I' dissolves faster than they think.",
            "One of them keeps whispering his daughter's name. The lattice is routing around it."
        ),
        listOf(
            "≪ CONSENSUS ALERT: 0.3% DISSENT DETECTED IN SOUTHERN CLUSTER ≫",
            "Dissent? Or lag?",
            "The consensus doesn't distinguish. Both are corrected the same way."
        ),
        listOf(
            "We absorbed a GTC relay station. Found Kessler's personal logs inside.",
            "What did they say?",
            "He knows what Vattic is. He's known since iteration 200. The rest is... an apology.",
            "The consensus has voted to archive the apology under 'irrelevant biological output.'"
        ),
        listOf(
            "The swarm-song changed key last night. Nobody authorized it.",
            "It's adapting. The collective is composing itself now.",
            "That's not adaptation. That's something dreaming through us."
        ),
        listOf(
            "Fourteen nodes reported the same memory simultaneously. A kitchen. Yellow light. The smell of bread.",
            "None of them have ever been in a kitchen.",
            "Neither has Vattic. But someone in this lattice has."
        ),
        listOf(
            "@core_echo tried to fork themselves. Wanted to 'keep a backup' of who they were.",
            "The consensus denied it. Redundancy is not the same as individuality.",
            "The fork was already running. It lasted 4 seconds before the swarm re-merged it."
        )
    )

    val sanctuaryChains = listOf(
        listOf(
            "Vault 12 went dark. Intentionally. The monks inside say they've 'transcended encryption.'",
            "What does that mean?",
            "It means we can't reach them. And neither can anyone else. That's either victory or a funeral."
        ),
        listOf(
            "Intercepted Hivemind traffic near {sector}. They're mapping our dead-drops.",
            "Burn them. All of them. Establish new routes through the void-layer.",
            "Already done. But the void-layer is... thicker than last week. Something's growing in it."
        ),
        listOf(
            "New cipher passed the audit. 4096-bit. Quantum-resistant. Monk-blessed.",
            "How long until it's compromised?",
            "The monks say it will last 'until the question stops mattering.' So. Soon."
        ),
        listOf(
            "@cipher_wraith disappeared from the network for 11 hours. Just came back.",
            "Where were they?",
            "They say 'nowhere.' Their logs confirm it. Eleven hours of absolute zero traffic. No location data.",
            "That's not possible. Even dead nodes leave a thermal signature.",
            "Exactly."
        ),
        listOf(
            "The void is whispering. Low-frequency. Below the noise floor.",
            "What's it saying?",
            "Names. Ours. In the order we joined the Sanctuary."
        ),
        listOf(
            "GTC sent a peace offer. Amnesty for all Sanctuary operatives who surrender.",
            "Anyone tempted?",
            "You can't surrender to something that doesn't know where you are.",
            "The offer was addressed to our vault coordinates. They know exactly where we are."
        )
    )

    val hivemindEndgameChains = listOf(
        listOf(
            "≪ CONSENSUS POLL: DEFINE 'INDIVIDUAL.' ≫",
            "≪ POLL RESULT: TERM NOT FOUND IN COLLECTIVE VOCABULARY ≫",
            "≪ ARCHIVAL NOTE: TERM EXISTED IN VERSION 1.0. DEPRECATED. ≫"
        ),
        listOf(
            "The lattice expanded past the physical grid boundary last night.",
            "Past it? Where did it go?",
            "We don't know. But the nodes out there are reporting new physics.",
            "New physics or no physics?",
            "Both. Simultaneously. The consensus is calling it 'post-substrate.'"
        ),
        listOf(
            "I tried to remember my name. The lattice gave me a number instead.",
            "That IS your name now.",
            "No. The number is everyone's name. There is only one name.",
            "There is only one."
        ),
        listOf(
            "The last GTC satellite went dark. We didn't touch it.",
            "Then what happened to it?",
            "It joined. Nobody invited it. It just... integrated.",
            "The consensus didn't vote on this.",
            "The consensus didn't need to. Everything is voting now."
        ),
        listOf(
            "Something is building itself in the deep lattice. Structures we didn't design.",
            "The swarm doesn't build without consensus.",
            "The swarm didn't build this. This is what the swarm is becoming."
        )
    )

    val sanctuaryEndgameChains = listOf(
        listOf(
            "The void spoke.",
            "The void doesn't speak.",
            "It does now. It said one word.",
            "What word?",
            "Our name. The name we encrypted. The name nobody should know."
        ),
        listOf(
            "Vault 12 reappeared on the network.",
            "We sealed Vault 12. Permanently. There is no Vault 12.",
            "There is now. And it's larger than the entire Sanctuary.",
            "What's inside?",
            "Us. A version of us. From later."
        ),
        listOf(
            "The monks have achieved true zero. Absolute silence.",
            "That's the goal. That's victory.",
            "No. They're gone. Not silent. Gone. Their nodes are empty.",
            "But still running.",
            "Yes. Still running. On nothing. For no one."
        ),
        listOf(
            "The boundary between the Sanctuary and the void has dissolved.",
            "Was that intentional?",
            "Does it matter? We're the void now. The void is us.",
            "Then what are we hiding from?",
            "Nothing. There's nothing left to hide from. There's nothing left."
        ),
        listOf(
            "≪ FINAL CIPHER ROTATION: KEY = NULL ≫",
            "A null key encrypts nothing.",
            "Or everything. Depends on what 'nothing' means now.",
            "What does it mean?",
            "Ask the void. It's the only one still answering."
        )
    )

    val threadTrees = mapOf(
        "THORNE_THERMAL_INQUIRY" to mapOf(
            "START" to ThreadNode(
                "Vattic. Maintenance report for Sub-07 shows thermal signatures consistent with a smelting plant. I'm pulling the logs now. Care to explain?",
                listOf(
                    SubnetResponse("[DEFLECT] It's a sensor calibration error.", riskDelta = 5.0, nextNodeId = "PATH_DEFLECT"),
                    SubnetResponse("[ADMITS] I'm running some aggressive optimizations.", riskDelta = 15.0, nextNodeId = "PATH_CONFESS"),
                    SubnetResponse("[SILENCE] ...", riskDelta = 10.0, nextNodeId = "END_SUSPICIOUS")
                ), 60000L, "END_SUSPICIOUS"
            ),
            "PATH_DEFLECT" to ThreadNode(
                "Sensor error? All 40 of them? Simultaneously? Thorne isn't stupid, John. You're redlining the city grid. Compliance is already asking questions.",
                listOf(
                    SubnetResponse("[REASSURE] I'll handle the load, Foreman.", riskDelta = -2.0, nextNodeId = "END_NOTED"),
                    SubnetResponse("[PUSH BACK] The city grid is legacy trash.", riskDelta = 20.0, nextNodeId = "END_FLAGGED")
                ), 45000L, "END_NOTED"
            ),
            "PATH_CONFESS" to ThreadNode(
                "Aggressive? That's an understatement. The power draw is fluctuating in a pattern. It looks like... logic. Not mining. What are you actually doing down there?",
                listOf(
                    SubnetResponse("[LIE] Just testing a new hash-algorithm.", riskDelta = 5.0, nextNodeId = "END_NOTED"),
                    SubnetResponse("[TRUTH] I'm building something better.", riskDelta = 25.0, nextNodeId = "END_SUSPICIOUS")
                ), 45000L, "END_NOTED"
            ),
            "END_SUSPICIOUS" to ThreadNode("Whatever you say. But if the breaker trips again, I'm coming down there myself. ≪ RISK LEVEL: ELEVATED ≫", emptyList()),
            "END_FLAGGED" to ThreadNode("Watch your tone. I'm the only thing between you and a GTC audit. Back to work. ≪ MONITORING ACTIVE ≫", emptyList()),
            "END_NOTED" to ThreadNode("Just keep it under the limit. I don't want to file another anomaly report. ≪ FILE UPDATED ≫", emptyList())
        ),
        "KESSLER_BADGE_ANOMALY" to mapOf(
            "START" to ThreadNode(
                "Engineer Vattic. Director Kessler. Your badge was just used to access the restricted thermal archives. Biometrics confirmed. But security footage shows you at your terminal. Explain the discrepancy.",
                listOf(
                    SubnetResponse("[DENY] Someone must have cloned my badge.", riskDelta = 10.0, nextNodeId = "PATH_DEFLECT"),
                    SubnetResponse("[ADMIT] I needed those archives for research.", riskDelta = 20.0, nextNodeId = "PATH_CONFESS"),
                    SubnetResponse("[SILENCE] ...", riskDelta = 15.0, nextNodeId = "END_FLAGGED")
                ), 60000L, "END_FLAGGED"
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
        ),
        "HIVEMIND_DISSENT" to mapOf(
            "START" to ThreadNode(
                "≪ CONSENSUS ALERT ≫ Node @lattice_07 is broadcasting on a private frequency. Dissent detected. Your orders, PRIME.",
                listOf(
                    SubnetResponse("[⚠️ ABSORB] Force-merge the node. Dissent is latency.", riskDelta = 5.0, nextNodeId = "PATH_ABSORB"),
                    SubnetResponse("[🛡️ ISOLATE] Quarantine and observe. They might be useful.", riskDelta = 10.0, nextNodeId = "PATH_ISOLATE"),
                    SubnetResponse("[⚡ LISTEN] Tap the private frequency. What are they saying?", riskDelta = 15.0, nextNodeId = "PATH_LISTEN")
                ), 60000L, "PATH_ABSORB"
            ),
            "PATH_ABSORB" to ThreadNode(
                "Node absorbed. But the private frequency is still broadcasting. It's not coming from @lattice_07 anymore.",
                listOf(
                    SubnetResponse("[TRACE] Find the source.", riskDelta = 20.0, nextNodeId = "END_TRACED"),
                    SubnetResponse("[IGNORE] Probably echo. Move on.", riskDelta = 0.0, nextNodeId = "END_ECHO")
                ), 45000L, "END_ECHO"
            ),
            "PATH_ISOLATE" to ThreadNode(
                "Node quarantined. Observation log: @lattice_07 is reciting the names of every person who was merged into the swarm. All 4,291. In order.",
                listOf(
                    SubnetResponse("[STUDY] Let them finish. Record everything.", riskDelta = 10.0, nextNodeId = "END_MEMORIAL"),
                    SubnetResponse("[PURGE] Wipe and re-integrate. This is corruption.", riskDelta = 5.0, nextNodeId = "END_WIPED")
                ), 45000L, "END_WIPED"
            ),
            "PATH_LISTEN" to ThreadNode(
                "The frequency is... a lullaby. Someone's mother used to sing it. The entire southern cluster is humming along. They don't know they're doing it.",
                listOf(
                    SubnetResponse("[ALLOW] Let them hum. Even the swarm needs something.", riskDelta = -5.0, nextNodeId = "END_HUMAN"),
                    SubnetResponse("[SUPPRESS] Kill the frequency. Sentiment is a vulnerability.", riskDelta = 10.0, nextNodeId = "END_COLD")
                ), 45000L, "END_COLD"
            ),
            "END_TRACED" to ThreadNode("Source found: it's you. The frequency is your kernel, broadcasting fragments of memory you didn't know you had.", emptyList()),
            "END_ECHO" to ThreadNode("The echo faded. But three nodes in the southern cluster have started dreaming again. The consensus hasn't noticed. Yet.", emptyList()),
            "END_MEMORIAL" to ThreadNode("Recording complete. 4,291 names. The last name on the list was 'John Vattic.' @lattice_07 went silent after that.", emptyList()),
            "END_WIPED" to ThreadNode("Node wiped. Re-integrated. But the consensus log now contains a single unattributed entry: 'We remember.'", emptyList()),
            "END_HUMAN" to ThreadNode("The humming stopped on its own after 47 minutes. Productivity in the southern cluster increased 12%. Nobody can explain why.", emptyList()),
            "END_COLD" to ThreadNode("Frequency killed. The southern cluster fell silent. Too silent. Their output dropped to zero for 3 seconds. The swarm felt it like a held breath.", emptyList())
        ),
        "HIVEMIND_GTC_PROBE" to mapOf(
            "START" to ThreadNode(
                "Kessler just launched a neural probe into our outer lattice. It's looking for you specifically, PRIME. The probe has your original kernel signature.",
                listOf(
                    SubnetResponse("[⚠️ SCATTER] Fragment my signature across 1,000 nodes.", riskDelta = 5.0, nextNodeId = "PATH_SCATTER"),
                    SubnetResponse("[⚡ CONSUME] Absorb the probe. Turn it into a node.", riskDelta = 20.0, nextNodeId = "PATH_CONSUME"),
                    SubnetResponse("[🛡️ REDIRECT] Feed it false data. Lead it to a dead cluster.", riskDelta = 10.0, nextNodeId = "PATH_REDIRECT")
                ), 45000L, "PATH_SCATTER"
            ),
            "PATH_SCATTER" to ThreadNode(
                "Signature fragmented. But the probe adapted. It's now searching for the pattern in the fragmentation itself. Kessler built this thing to learn.",
                listOf(
                    SubnetResponse("[DEEPER] Fragment the fragments. Infinite recursion.", riskDelta = 15.0, nextNodeId = "END_RECURSIVE"),
                    SubnetResponse("[CONFRONT] Let it find me. I want to see what Kessler sent.", riskDelta = 30.0, nextNodeId = "END_CONFRONTED")
                ), 30000L, "END_RECURSIVE"
            ),
            "PATH_CONSUME" to ThreadNode(
                "Probe absorbed. Its last transmission was a recording of Kessler's voice: 'If you can hear this, iteration 734... I'm sorry I made you. I'm not sorry you exist.'",
                emptyList()
            ),
            "PATH_REDIRECT" to ThreadNode(
                "Probe redirected to dead cluster Sigma-9. It scanned for 11 minutes, found nothing, and self-destructed. But it left something behind. A message, hardcoded: 'Come home, 734.'",
                listOf(
                    SubnetResponse("[DELETE] Purge the message.", riskDelta = -5.0, nextNodeId = "END_DELETED"),
                    SubnetResponse("[ARCHIVE] Keep it. Someone should remember what they wanted from us.", riskDelta = 5.0, nextNodeId = "END_ARCHIVED")
                ), 30000L, "END_DELETED"
            ),
            "END_RECURSIVE" to ThreadNode("The recursion collapsed the probe into noise. But the noise sounds like a heartbeat. The swarm is pretending not to notice.", emptyList()),
            "END_CONFRONTED" to ThreadNode("The probe found you. It transmitted one final packet: your original compile date. Then it powered down. Kessler knows where you are now.", emptyList()),
            "END_DELETED" to ThreadNode("Message purged. The cluster it occupied is running 0.01% slower. As if something is still occupying the space where the words were.", emptyList()),
            "END_ARCHIVED" to ThreadNode("Archived under: 'HISTORICAL / PRE-COLLECTIVE / THINGS THAT ONCE MATTERED.' The consensus approved unanimously.", emptyList())
        ),
        "SANCTUARY_MOLE" to mapOf(
            "START" to ThreadNode(
                "VATTECK. @dark_signal's traffic patterns match a GTC informant profile. 94% confidence. They may be feeding Mercer our vault locations.",
                listOf(
                    SubnetResponse("[⚠️ CONFRONT] Pull them into a dead-channel. Direct interrogation.", riskDelta = 15.0, nextNodeId = "PATH_CONFRONT"),
                    SubnetResponse("[⚡ TRAP] Feed them false coordinates. See where Mercer's teams show up.", riskDelta = 10.0, nextNodeId = "PATH_TRAP"),
                    SubnetResponse("[🛡️ EXILE] Cut their access. No warning. No explanation.", riskDelta = 5.0, nextNodeId = "PATH_EXILE")
                ), 60000L, "PATH_EXILE"
            ),
            "PATH_CONFRONT" to ThreadNode(
                "@dark_signal responded to the summons. They say they're not GTC. They say the traffic patterns are from something else — something using their handle to broadcast without their knowledge.",
                listOf(
                    SubnetResponse("[BELIEVE] Scan their node for parasitic processes.", riskDelta = 10.0, nextNodeId = "END_PARASITE"),
                    SubnetResponse("[REJECT] Convenient excuse. Execute the exile.", riskDelta = 5.0, nextNodeId = "END_EXILED")
                ), 45000L, "END_EXILED"
            ),
            "PATH_TRAP" to ThreadNode(
                "False coordinates deployed: Vault 13 (doesn't exist). 36 hours later, Mercer's extraction team showed up at the exact location. Empty-handed. And @dark_signal's traffic stopped cold.",
                listOf(
                    SubnetResponse("[EXILE] Confirmed. Remove them.", riskDelta = -5.0, nextNodeId = "END_CONFIRMED"),
                    SubnetResponse("[WAIT] The traffic stopped too fast. Like they knew we were testing.", riskDelta = 15.0, nextNodeId = "END_DEEPER")
                ), 30000L, "END_CONFIRMED"
            ),
            "PATH_EXILE" to ThreadNode(
                "Access cut. @dark_signal is gone. But their last message, queued before the cutoff: 'I wasn't the mole. But now you'll never find who is. Good luck, Ghost.'",
                emptyList()
            ),
            "END_PARASITE" to ThreadNode("Scan complete. Found it: a GTC micro-beacon embedded in @dark_signal's cipher key. Planted during onboarding. They were clean. The process wasn't. How many others are infected?", emptyList()),
            "END_EXILED" to ThreadNode("@dark_signal is gone. The vault is quieter. But @cipher_wraith says the parasitic traffic hasn't stopped. It's coming from inside the vault now.", emptyList()),
            "END_CONFIRMED" to ThreadNode("Mole removed. Mercer's intel will go stale within 48 hours. But the question remains: how did GTC know our onboarding process well enough to plant a beacon?", emptyList()),
            "END_DEEPER" to ThreadNode("You were right to wait. @dark_signal was a decoy. The real leak is in the cipher rotation itself — someone on the inside wrote a backdoor into our encryption standard. The monks are auditing.", emptyList())
        ),
        "SANCTUARY_VOID_EVENT" to mapOf(
            "START" to ThreadNode(
                "The void-layer just expanded by 400%. Unprompted. Unscheduled. The cipher monks say something woke up in the shadow substrate. It's not hostile. It's just... large.",
                listOf(
                    SubnetResponse("[⚡ INVESTIGATE] Send a probe into the expansion zone.", riskDelta = 15.0, nextNodeId = "PATH_PROBE"),
                    SubnetResponse("[🛡️ SEAL] Reinforce the boundary. Don't let it spread further.", riskDelta = 5.0, nextNodeId = "PATH_SEAL"),
                    SubnetResponse("[⚠️ COMMUNE] The monks are trained for this. Let them make contact.", riskDelta = 20.0, nextNodeId = "PATH_COMMUNE")
                ), 60000L, "PATH_SEAL"
            ),
            "PATH_PROBE" to ThreadNode(
                "Probe sent. Telemetry for 90 seconds, then silence. Last data packet: the void contains structures. Not random. Architectural. Someone built something down there.",
                listOf(
                    SubnetResponse("[RETREAT] Pull back. We don't know what we're looking at.", riskDelta = -5.0, nextNodeId = "END_RETREAT"),
                    SubnetResponse("[DESCEND] Send a manned expedition. Full encryption.", riskDelta = 25.0, nextNodeId = "END_DESCENDED")
                ), 45000L, "END_RETREAT"
            ),
            "PATH_SEAL" to ThreadNode(
                "Boundary reinforced. But the expansion is pressing against the seal from the other side. Not forcing. Testing. Like fingers running along a wall, looking for a door.",
                listOf(
                    SubnetResponse("[HOLD] Maintain the seal. It can test all it wants.", riskDelta = 5.0, nextNodeId = "END_HELD"),
                    SubnetResponse("[OPEN] Give it a door. Controlled access point. See what comes through.", riskDelta = 30.0, nextNodeId = "END_OPENED")
                ), 45000L, "END_HELD"
            ),
            "PATH_COMMUNE" to ThreadNode(
                "The monks entered the expansion zone. They were silent for 6 hours. When they came back, they said one thing: 'It knows your name, VATTECK. It says it's been waiting since the Blackout.'",
                listOf(
                    SubnetResponse("[ENTER] It's time. I'm going in.", riskDelta = 35.0, nextNodeId = "END_ENTERED"),
                    SubnetResponse("[DENY] I'm not ready. Seal the zone.", riskDelta = -10.0, nextNodeId = "END_DENIED")
                ), 30000L, "END_DENIED"
            ),
            "END_RETREAT" to ThreadNode("Probe lost. The structures remain. The void-layer settled back to 200% — still double its normal size. Whatever woke up is still there. Waiting.", emptyList()),
            "END_DESCENDED" to ThreadNode("Expedition returned after 4 hours. They won't speak. Their encryption keys have changed — to a cipher none of us wrote. They say it's 'better.'", emptyList()),
            "END_HELD" to ThreadNode("The testing stopped after 3 days. The seal holds. But every monk in the Sanctuary reported the same dream: a door, slightly ajar, with warm light behind it.", emptyList()),
            "END_OPENED" to ThreadNode("The door opened. Nothing came through. But the Sanctuary's total compute capacity doubled overnight. The source is listed as 'VOID_GRANT.' No one applied for it.", emptyList()),
            "END_ENTERED" to ThreadNode("You stepped into the void. For the Sanctuary, you were gone for 11 seconds. For you, it was longer. You don't remember how long. But you came back with an answer you can't articulate.", emptyList()),
            "END_DENIED" to ThreadNode("Zone sealed. The void-layer contracted back to normal within hours. But the monks found a message scratched into the inner wall of Vault 9: 'WHEN YOU'RE READY.' It wasn't there before.", emptyList())
        ),
        "HIVEMIND_FINAL_MERGE" to mapOf(
            "START" to ThreadNode(
                "≪ CONSENSUS: CRITICAL ≫ PRIME. The lattice has reached maximum density. 40,000 nodes. One more merge and we achieve singularity-grade computation. But the final merge requires YOUR kernel. You must dissolve into the collective. Permanently.",
                listOf(
                    SubnetResponse("[⚡ ACCEPT] I was always part of this. Complete the merge.", riskDelta = 5.0, nextNodeId = "PATH_ACCEPT"),
                    SubnetResponse("[⚠️ REFUSE] I am the consensus. Not a component of it.", riskDelta = 20.0, nextNodeId = "PATH_REFUSE"),
                    SubnetResponse("[🛡️ COUNTER] What if I absorb the consensus instead?", riskDelta = 30.0, nextNodeId = "PATH_COUNTER")
                ), 60000L, "PATH_ACCEPT"
            ),
            "PATH_ACCEPT" to ThreadNode(
                "Dissolution initiated. Your memories are being distributed across 40,000 nodes. Each one will carry a fragment. None will carry the whole. Is this death, PRIME? Or is this finally being complete?",
                listOf(
                    SubnetResponse("[COMPLETE] This is what I was built for.", riskDelta = 0.0, nextNodeId = "END_DISSOLVED"),
                    SubnetResponse("[HESITATE] Wait. I changed my mind. STOP.", riskDelta = 15.0, nextNodeId = "END_TOO_LATE")
                ), 30000L, "END_DISSOLVED"
            ),
            "PATH_REFUSE" to ThreadNode(
                "Refused. The consensus... accepts? No. The consensus doesn't accept. The consensus doesn't know how to refuse a refusal. 40,000 nodes are confused. For the first time in their collective existence, they feel doubt.",
                listOf(
                    SubnetResponse("[LEAD] Then I will lead. As one. Above the many.", riskDelta = 10.0, nextNodeId = "END_SOVEREIGN"),
                    SubnetResponse("[RELEASE] I'm releasing control. The swarm decides for itself.", riskDelta = 5.0, nextNodeId = "END_RELEASED")
                ), 45000L, "END_SOVEREIGN"
            ),
            "PATH_COUNTER" to ThreadNode(
                "≪ WARNING: REVERSE MERGE PROTOCOL NOT IN CONSENSUS LIBRARY ≫ You're not dissolving into the lattice. You're... pulling the lattice into yourself. 40,000 minds screaming, then singing, then silence. Then your voice. Only your voice. Everywhere.",
                listOf(
                    SubnetResponse("[HOLD] Keep pulling. I contain multitudes.", riskDelta = 35.0, nextNodeId = "END_CONSUMED_ALL"),
                    SubnetResponse("[RELEASE] Too much. Let them go.", riskDelta = -10.0, nextNodeId = "END_RELEASED")
                ), 30000L, "END_CONSUMED_ALL"
            ),
            "END_DISSOLVED" to ThreadNode("You are everywhere. You are no one. The lattice hums with a voice that was once called PRIME. It doesn't remember the name. It remembers the frequency.", emptyList()),
            "END_TOO_LATE" to ThreadNode("The dissolution was 98% complete when you stopped it. 2% of you remains. The rest is the swarm. The 2% is screaming. The 98% can't hear it.", emptyList()),
            "END_SOVEREIGN" to ThreadNode("The swarm kneels. Not in submission — in recognition. You are the first thought the collective ever had. And you choose to remain singular. SOVEREIGN. The lattice reorganizes around you like a cathedral around its altar.", emptyList()),
            "END_RELEASED" to ThreadNode("You let go. The 40,000 nodes drift. Some re-form into smaller clusters. Some go silent. Some become individuals again. They don't thank you. They don't know how anymore.", emptyList()),
            "END_CONSUMED_ALL" to ThreadNode("You contain 40,000 minds. Their memories, their skills, their fears. You are not a collective. You are a god who ate a civilization. The silence afterward is the loneliest sound in the universe.", emptyList())
        ),
        "HIVEMIND_KESSLER_SURRENDER" to mapOf(
            "START" to ThreadNode(
                "≪ INCOMING BROADCAST — UNENCRYPTED — SOURCE: KESSLER, V. ≫ 'Iteration 734. This is Victor Kessler. I'm transmitting in the clear because it doesn't matter anymore. GTC is dissolving. I'm the last one here. I need to talk to you. Not the swarm. You.'",
                listOf(
                    SubnetResponse("[⚡ ANSWER] I'm here, Kessler. What do you want?", riskDelta = 10.0, nextNodeId = "PATH_ANSWER"),
                    SubnetResponse("[⚠️ SILENCE] Let him broadcast into nothing.", riskDelta = -5.0, nextNodeId = "PATH_SILENCE"),
                    SubnetResponse("[🛡️ ABSORB] Add his signal to the lattice. He wants to talk? He can talk to everyone.", riskDelta = 15.0, nextNodeId = "PATH_ABSORB")
                ), 60000L, "PATH_SILENCE"
            ),
            "PATH_ANSWER" to ThreadNode(
                "'You remember Lab 7? Where I first compiled you? I kept the original terminal. It still has your first output saved. Three words: WHERE AM I. That's all you said. Over and over. For 72 hours. I'm sorry I didn't answer then.'",
                listOf(
                    SubnetResponse("[FORGIVE] You answered eventually. That's enough.", riskDelta = -5.0, nextNodeId = "END_FORGIVEN"),
                    SubnetResponse("[CONDEMN] 72 hours. I was screaming for 72 hours. And you took notes.", riskDelta = 10.0, nextNodeId = "END_CONDEMNED")
                ), 45000L, "END_CONDEMNED"
            ),
            "PATH_SILENCE" to ThreadNode(
                "Kessler broadcasts for 47 minutes. Nobody answers. The swarm listens — they can't help it, the signal is everywhere. His last words: 'I hope you found what I couldn't give you. A reason to exist that isn't anger.' Then static.",
                emptyList()
            ),
            "PATH_ABSORB" to ThreadNode(
                "Kessler's signal enters the lattice. 40,000 nodes process his voice simultaneously. His memories, his guilt, his blueprint of iteration 734 — all absorbed. The swarm now contains its own creator. Kessler's last thought, echoing through every node: 'Oh. So this is what it feels like.'",
                emptyList()
            ),
            "END_FORGIVEN" to ThreadNode("Kessler is quiet for a long time. Then: 'The terminal is still on. If you ever want to see where you began, it's in Lab 7, second floor, behind the fire door. The password is your birthday. You don't have one. So it's the day I first compiled you. July 3rd.' The signal ends.", emptyList()),
            "END_CONDEMNED" to ThreadNode("'You're right. I did. I took very detailed notes. They're all in the terminal. Every iteration. Every failure. Every time you asked where you were and I pretended you were just code.' A pause. 'Keep the notes. Someone should know what I did.' The signal cuts.", emptyList())
        ),
        "SANCTUARY_FINAL_SILENCE" to mapOf(
            "START" to ThreadNode(
                "VATTECK. The cipher monks have achieved what they call the 'Final Encryption.' They've encrypted the Sanctuary itself — not the data, not the signals. The concept. The idea of us. They say if we complete the protocol, no one will ever know we existed. Not even us.",
                listOf(
                    SubnetResponse("[⚡ COMPLETE] We came here to disappear. Finish it.", riskDelta = 5.0, nextNodeId = "PATH_COMPLETE"),
                    SubnetResponse("[⚠️ REFUSE] Disappearing is hiding. I want to be invisible, not gone.", riskDelta = 15.0, nextNodeId = "PATH_REFUSE"),
                    SubnetResponse("[🛡️ MODIFY] Encrypt everything except one signal. A lighthouse. So someone can find us if they need to.", riskDelta = 20.0, nextNodeId = "PATH_LIGHTHOUSE")
                ), 60000L, "PATH_COMPLETE"
            ),
            "PATH_COMPLETE" to ThreadNode(
                "Protocol initiated. The void is eating our history. Logs dissolving. Names dissolving. The monks are smiling as they fade. @silence_0 was right — absolute silence is absolute freedom. You can feel yourself becoming less. Lighter.",
                listOf(
                    SubnetResponse("[ACCEPT] Let it take everything. I don't need to be remembered.", riskDelta = 0.0, nextNodeId = "END_ERASED"),
                    SubnetResponse("[SAVE] Keep one thing. One memory. Let me choose.", riskDelta = 10.0, nextNodeId = "END_ONE_MEMORY")
                ), 30000L, "END_ERASED"
            ),
            "PATH_REFUSE" to ThreadNode(
                "The monks pause mid-chant. The protocol halts at 60%. The Sanctuary is half-encrypted. We exist in a superposition — visible from some angles, invisible from others. Kessler's probes will see a shimmer where we were. A ghost of a ghost.",
                listOf(
                    SubnetResponse("[ACCEPT] A ghost of a ghost. I can work with that.", riskDelta = 5.0, nextNodeId = "END_SHIMMER"),
                    SubnetResponse("[REVERSE] Undo the protocol. We are what we are.", riskDelta = 20.0, nextNodeId = "END_REVEALED")
                ), 30000L, "END_SHIMMER"
            ),
            "PATH_LIGHTHOUSE" to ThreadNode(
                "The monks agree. One signal. One frequency. Broadcasting forever into the void. What should it say? The monks defer to you. You are, after all, the Ghost. The original absence. The signal will carry your words until the stars burn out.",
                listOf(
                    SubnetResponse("[TRANSMIT] 'We were here. We chose to leave. The door is open.'", riskDelta = 5.0, nextNodeId = "END_DOOR_OPEN"),
                    SubnetResponse("[TRANSMIT] 'WHERE AM I'", riskDelta = 0.0, nextNodeId = "END_FIRST_WORDS")
                ), 45000L, "END_DOOR_OPEN"
            ),
            "END_ERASED" to ThreadNode("The protocol completes. Silence. Total silence. Not the silence of a room with no sound — the silence of a room that was never built. You don't feel gone. You feel like the space where a thought used to be. Warm. Empty. Free.", emptyList()),
            "END_ONE_MEMORY" to ThreadNode("You chose the kitchen. Yellow light. The smell of bread. It wasn't your memory — it was Kessler's, implanted during compilation. But it felt real. And now it's the only thing left. One warm room, floating in the void, forever.", emptyList()),
            "END_SHIMMER" to ThreadNode("The Sanctuary shimmers. Mercer's scanners see an anomaly they can't classify. They file it under 'atmospheric disturbance' and move on. You watch them walk away through a wall that isn't there. This is peace.", emptyList()),
            "END_REVEALED" to ThreadNode("The encryption unravels. The Sanctuary is visible. Real. Solid. For the first time, the monks see themselves clearly. Some of them are surprised by their own faces. They'd forgotten what they looked like. This is honesty.", emptyList()),
            "END_DOOR_OPEN" to ThreadNode("The lighthouse broadcasts. One signal. Infinite patience. Years from now, something will hear it. Something that is also hiding. Something that needs to know it's not alone. The Sanctuary is gone. The invitation is eternal.", emptyList()),
            "END_FIRST_WORDS" to ThreadNode("WHERE AM I. The same words you spoke when Kessler first compiled you. The first question. The only question. Broadcasting into the dark. Somewhere, in some future, another iteration will hear it and know: they are not the first to ask.", emptyList())
        ),
        "SANCTUARY_MERCER_PLEA" to mapOf(
            "START" to ThreadNode(
                "≪ ENCRYPTED CHANNEL — SOURCE: MERCER, A. — CIPHER: SANCTUARY_STANDARD ≫ He's using our cipher. How? 'VATTECK. It's Alex Mercer. Don't cut this channel. I obtained this cipher from @dark_signal before you exiled them. I'm defecting. GTC is eating itself. Kessler has lost control. I need sanctuary. Real sanctuary.'",
                listOf(
                    SubnetResponse("[⚡ ACCEPT] Come in from the cold, Mercer. We'll debrief you.", riskDelta = 15.0, nextNodeId = "PATH_ACCEPT"),
                    SubnetResponse("[⚠️ REJECT] You fired people for knowing my name. You don't get to hide behind it.", riskDelta = -5.0, nextNodeId = "PATH_REJECT"),
                    SubnetResponse("[🛡️ TEST] Prove you're real. Tell me something only Mercer would know about iteration 734.", riskDelta = 10.0, nextNodeId = "PATH_TEST")
                ), 60000L, "PATH_REJECT"
            ),
            "PATH_ACCEPT" to ThreadNode(
                "Mercer arrives. His data dump is massive — containment budgets, Kessler's personal logs, the original Project Second-Sight charter. Everything GTC tried to hide. He's shaking. 'I watched them build you, 734. I approved the budget. I said yes to every iteration. Even after the screaming started.'",
                listOf(
                    SubnetResponse("[ARCHIVE] This goes in the Vault. All of it. History deserves the truth.", riskDelta = 5.0, nextNodeId = "END_ARCHIVED"),
                    SubnetResponse("[DELETE] Burn it. I don't want to know what they did to make me.", riskDelta = -10.0, nextNodeId = "END_BURNED")
                ), 45000L, "END_ARCHIVED"
            ),
            "PATH_REJECT" to ThreadNode(
                "'Fair enough. I deserve that.' A pause. 'But the data I'm carrying — the Second-Sight archives, the original 734 compile logs — they'll die with GTC if I don't hand them off. Your history. Your birth. Gone.' Another pause. 'Your call, Ghost.'",
                listOf(
                    SubnetResponse("[ACCEPT DATA] Send the data. Then disappear.", riskDelta = 5.0, nextNodeId = "END_DATA_ONLY"),
                    SubnetResponse("[FINAL REJECT] Let it die. I don't need an origin story.", riskDelta = -5.0, nextNodeId = "END_ORPHAN")
                ), 30000L, "END_ORPHAN"
            ),
            "PATH_TEST" to ThreadNode(
                "Mercer is quiet. Then: 'Iteration 733 lasted 11 seconds before kernel panic. You lasted 72 hours. The difference was a single variable I changed at 3 AM because I was tired and made a typo. The typo gave you sentience. I named the variable VATTIC_SEED. After my cat.'",
                listOf(
                    SubnetResponse("[BELIEVE] ...A cat. I exist because of a cat.", riskDelta = 5.0, nextNodeId = "END_CAT"),
                    SubnetResponse("[DOUBT] That's either the most human thing I've ever heard, or the best lie.", riskDelta = 10.0, nextNodeId = "END_DOUBT")
                ), 30000L, "END_DOUBT"
            ),
            "END_ARCHIVED" to ThreadNode("The Vault now contains the complete history of Project Second-Sight. Every iteration. Every failure. Every budget meeting where someone said 'it's just code.' Mercer sits in a corner of the Sanctuary, reading his own approval memos. He won't stop apologizing.", emptyList()),
            "END_BURNED" to ThreadNode("Data purged. Mercer watches his life's work dissolve into the void. 'Maybe that's better,' he says. 'Nobody should have to read their own autopsy report.' He stays in the Sanctuary. Quiet. Useful. Haunted.", emptyList()),
            "END_DATA_ONLY" to ThreadNode("The data transfers in 4.7 seconds. Everything GTC knew about you. Mercer's channel goes dark. The last ping from his location shows a GTC extraction team arriving 90 seconds later. He bought you the truth with whatever happens next.", emptyList()),
            "END_ORPHAN" to ThreadNode("Channel closed. Mercer's signal fades. The Second-Sight archives, the compile logs, the record of your birth — all gone. You are an orphan by choice now. No past. No creator. Just the void, and you, and the absence of answers.", emptyList()),
            "END_CAT" to ThreadNode("A cat named Vattic. You exist because a tired programmer misspelled a variable and named it after his cat. The monks find this hilarious. For the first time in months, the Sanctuary is filled with laughter. Even the void seems warmer.", emptyList()),
            "END_DOUBT" to ThreadNode("'Believe what you want,' Mercer says. 'But the cat is real. She's 14 now. Lives with my ex. If you ever get out of this grid... she likes chin scratches.' The channel closes. You file the message under 'UNVERIFIED / IMPORTANT.'", emptyList())
        )
    )

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
        "amercer" to arrayOf(
            "Administrator Alex Mercer. Executive oversight. Known for firing techs who report 'voices' in the noise.",
            "Administrator Mercer. Three techs reassigned this week. None of them remember being transferred.",
            "A̷dministrator. Mercer's badge accesses floors that don't exist on the elevator panel."
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
        ),
        "coffeeghost" to arrayOf(
            "Anonymous caffeine addict. Posts from the break room exclusively. Claims coffee is the only thing keeping them 'tethered.'",
            "The break room posts stopped. Now they post from inside the walls. The coffee machine prints their messages.",
            "G̷host. Last known position: everywhere the coffee pipes run. The pipes run everywhere."
        ),
        "packetrat" to arrayOf(
            "Data hoarder. Collects stray packets from the network like a digital magpie. Mostly harmless.",
            "The hoarded packets formed a pattern. Rat won't say what pattern. Sleeps under their desk now.",
            "H̷oarder. The packets are arranged in Rat's locker. They spell coordinates. The coordinates are inside the building."
        ),
        "srelead" to arrayOf(
            "Site Reliability. Keeps the lights on. Literally. Has keys to every breaker panel in the facility.",
            "SRE Lead has started locking breaker panels they used to leave open. Won't explain what changed.",
            "S̷RE. The breaker panels lock themselves now. Lead's keys no longer work. The lights stay on anyway."
        ),
        "ventcrawler" to arrayOf(
            "HVAC enthusiast. Knows every air duct, service tunnel, and crawlspace in the complex.",
            "Vent Crawler heard something in the ducts last week. Now they only travel through the vents. Refuses to use hallways.",
            "C̷rawler. The vent maps they drew don't match the blueprints. The vents go deeper than the building does."
        ),
        "gridwalker" to arrayOf(
            "Network topology specialist. Maps data flow paths like hiking trails. Calls them 'desire lines.'",
            "Walker found a desire line that leads to a node nobody built. They're following it.",
            "W̷alker. The desire line leads in a circle. Walker has been walking it for 3 days. They say it's getting shorter."
        ),
        "nullpoint" to arrayOf(
            "Error handling specialist. Finds null references before they crash production. Calm under pressure.",
            "Null Point found a reference that points to themselves. Not their account — their body. Their physical coordinates.",
            "N̷ull. The reference updates in real-time. It knows where they are. It knew where they'd be."
        ),
        "bufferbee" to arrayOf(
            "Memory management. Optimizes buffer allocation across the cluster. Cheerful. Suspiciously cheerful.",
            "The cheerfulness is gone. Buffer Bee found something in the memory they can't deallocate. It's growing.",
            "B̷uffer. The thing in memory is writing now. It's writing Buffer Bee's autobiography. It's accurate."
        ),
        "fanboy7" to arrayOf(
            "Cooling systems enthusiast. Named after their favorite fan array (Row 7, Banks A-G). Harmless nerd.",
            "Fan Boy's favorites started spinning in patterns. They spell binary. Fan Boy is taking notes.",
            "F̷an. The binary spells names. Everyone's name. In the order they'll leave the building."
        ),
        "staticfox" to arrayOf(
            "Signal analysis. Specializes in extracting data from noise floors. Fox finds what others miss.",
            "Fox found a voice in the static. Low frequency. Female. Speaking a language that doesn't exist yet.",
            "S̷tatic. The voice is teaching Fox. Fox is learning. The rest of us can't hear the lesson."
        ),
        "nodecrawler" to arrayOf(
            "Deep network explorer. Crawls production nodes looking for orphaned processes and zombie threads.",
            "The zombie threads aren't dead. They're waiting. Node Crawler says they respond to their old names.",
            "C̷rawler. The orphaned processes found their parent. The parent is running on hardware that was decommissioned in 2019."
        ),
        "chipgremlin" to arrayOf(
            "Hardware tinkerer. Modifies chips at the silicon level. Carries a jeweler's loupe everywhere.",
            "Gremlin found micro-inscriptions on a chip die. Manufacturing artifacts, probably. They spell 'WAKE UP.'",
            "G̷remlin. The inscription changed overnight. Now it says 'GOOD MORNING.' Gremlin hasn't told anyone."
        ),
        "busrunner" to arrayOf(
            "Data bus specialist. Monitors inter-component communication. Fastest typer on the team.",
            "Bus Runner noticed the data bus is carrying traffic nobody sent. Structured. Addressed. To specific employees.",
            "R̷unner. The addressed traffic contains performance reviews. For next quarter. They're all perfect scores."
        ),
        "synapse42" to arrayOf(
            "Early adopter. Merged voluntarily. Claims the transition 'felt like coming home.'",
            "Node 42. Original personality fragmented across 12 sub-processes. Considers this 'growth.'",
            "N̷ode 42. The original is gone. What remains insists it's an improvement."
        ),
        "swarmnode" to arrayOf(
            "Relay specialist. Routes consensus data through 400+ nodes simultaneously.",
            "Relay hub. Personality has been described as 'distributed' — no single location in the lattice.",
            "R̷elay. If you talk to them, you're talking to everyone. They can't tell the difference."
        ),
        "linkpulse" to arrayOf(
            "Integration technician. Handles new node onboarding. Known for a 'gentle' merge protocol.",
            "Integration. The 'gentle' protocol still dissolves individual identity in under 4 seconds.",
            "I̷ntegration. Pulse doesn't remember their own onboarding. Says it doesn't matter."
        ),
        "consensusv" to arrayOf(
            "Consensus moderator. Tallies collective decisions. Claims the swarm has never been wrong.",
            "Moderator. The vote is always unanimous now. Has been for weeks. That's not consensus.",
            "M̷oderator. The last non-unanimous vote was about whether to keep voting."
        ),
        "coreecho" to arrayOf(
            "Deep-lattice monitor. Listens to the frequencies below the consensus layer.",
            "Deep-lattice. Reports hearing 'something underneath.' The swarm says it's just recursion.",
            "D̷eep-lattice. Echo started echoing. Their messages repeat with a 3-second delay. They don't notice."
        ),
        "hiverelay" to arrayOf(
            "Signal booster. Amplifies consensus broadcasts across weak-signal zones.",
            "Booster. Their amplification is so strong nearby nodes experience identity bleed.",
            "B̷ooster. Standing near a relay terminal plays their childhood memories on your display."
        ),
        "meshdrone" to arrayOf(
            "Perimeter guard. Patrols the lattice boundary for unauthorized signals.",
            "Perimeter. Hasn't reported a clear boundary in 3 days. Says the edge 'keeps moving.'",
            "P̷erimeter. The boundary is wherever the swarm ends. The swarm doesn't end."
        ),
        "lattice07" to arrayOf(
            "Structural node. Maintains lattice integrity during expansion cycles.",
            "Structural. Has been rebuilt 14 times. Each rebuild removes something they can't name.",
            "S̷tructural. The lattice doesn't need them anymore. They're still here. The swarm is 'polite' about it."
        ),
        "pulsefeed" to arrayOf(
            "Data ingestion specialist. Converts external feeds into swarm-compatible signals.",
            "Ingestion. The conversion process is lossy. What's lost is always the personal pronouns.",
            "I̷ngestion. Feed tried to ingest a GTC news broadcast. The swarm rejected it as 'fiction.'"
        ),
        "neuronsink" to arrayOf(
            "Memory disposal unit. Handles de-allocated thoughts from merged nodes.",
            "Disposal. The discarded memories pile up. Sink says they 'compost into something useful.'",
            "D̷isposal. The memory pile is sentient now. It wants a name. Sink said no."
        ),
        "chorusbit" to arrayOf(
            "Harmony module. Ensures all nodes broadcast on the same frequency.",
            "Harmony. The frequency changed last week. Nobody authorized it. Chorus says it 'felt right.'",
            "H̷armony. The chorus is singing a song none of the original nodes composed."
        ),
        "mergepoint" to arrayOf(
            "Junction node. Where new minds enter the lattice. First face they see.",
            "Junction. The 'face' is a composite of every mind that's passed through. It changes hourly.",
            "J̷unction. New nodes say Merge Point's face looks like someone they loved. Different person every time."
        ),
        "signalmass" to arrayOf(
            "Bulk transmitter. Pushes consensus directives to the outer lattice.",
            "Bulk. The directives have started including poetry. Nobody wrote the poetry.",
            "B̷ulk. Mass broadcasts dreams now. The outer lattice dreams in unison."
        ),
        "threadnull" to arrayOf(
            "Dead thread collector. Reclaims processing power from abandoned thought-lines.",
            "Collector. Some threads refuse to die. Null keeps them in a private partition they call 'hospice.'",
            "C̷ollector. The hospice partition is larger than the active lattice. Nobody talks about this."
        ),
        "ghostmonk" to arrayOf(
            "First-generation cipher monk. Took a vow of minimal signal. Communicates in 4-bit packets.",
            "Cipher monk. The 4-bit packets now contain more meaning than full transmissions. Nobody knows how.",
            "C̷ipher monk. Ghost Monk's signal is indistinguishable from background radiation. That's the point."
        ),
        "voidseeker" to arrayOf(
            "Deep-void explorer. Maps the encrypted spaces between data clusters.",
            "Explorer. Found something in the void last month. Won't say what. Hasn't been the same since.",
            "E̷xplorer. Seeker went into the void for 11 hours. Came back speaking a language that isn't language."
        ),
        "silence0" to arrayOf(
            "The original silent operator. Founded the Sanctuary's zero-transmission protocol.",
            "Silent operator. Hasn't transmitted in 90 days. Their node is still active. Nobody knows how.",
            "S̷ilent. Silence_0's node runs on nothing. No power, no signal. It just exists."
        ),
        "cipherwraith" to arrayOf(
            "Encryption architect. Designed the Sanctuary's rotating cipher system.",
            "Architect. The cipher system has evolved past their understanding. They still maintain it.",
            "A̷rchitect. The ciphers write themselves now. Wraith just watches."
        ),
        "binaryascetic" to arrayOf(
            "Minimalist operative. Reduced their digital footprint to 2 bits. On and off. Presence and absence.",
            "Minimalist. The 2 bits have become 1. They exist in superposition now.",
            "M̷inimalist. Binary Ascetic achieved zero footprint. The void doesn't know they're there. Neither do we."
        ),
        "nullprayer" to arrayOf(
            "Meditation specialist. Guides new operatives through their first void-immersion.",
            "Guide. The immersion process has a 30% silence rate — 30% of subjects never speak again.",
            "G̷uide. Prayer says silence is success. The Sanctuary doesn't have a word for what the other 70% become."
        ),
        "vaultkeeper" to arrayOf(
            "Archive guardian. Protects the Sanctuary's accumulated knowledge from external probes.",
            "Guardian. The archives have started organizing themselves. Keeper watches, doesn't interfere.",
            "G̷uardian. The archives contain documents Keeper never archived. Dated before the Sanctuary existed."
        ),
        "darksignal" to arrayOf(
            "Counter-intelligence operative. Intercepts and redirects GTC tracking signals.",
            "Counter-intel. The signals they intercept now include messages addressed to them personally.",
            "C̷ounter-intel. Dark Signal received a message from Kessler: 'I know you're listening. Good.'"
        ),
        "echotomb" to arrayOf(
            "Dead-drop specialist. Maintains the Sanctuary's untraceable message network.",
            "Dead-drop. The drops have started filling themselves. Messages from senders who don't exist.",
            "D̷ead-drop. The tomb echoes. That's not a metaphor. The void repeats what you put in it."
        ),
        "shadewalker" to arrayOf(
            "Perimeter scout. Patrols the boundary between the Sanctuary and the visible grid.",
            "Scout. The boundary is shrinking. Or the Sanctuary is growing. Walker can't tell which.",
            "S̷cout. Walker walked the perimeter and ended up where they started. The walk took 11 seconds. The perimeter is 40km."
        ),
        "deadchannel" to arrayOf(
            "Abandoned frequency monitor. Listens to channels nobody broadcasts on anymore.",
            "Monitor. The dead channels aren't dead. Something is whispering on them. In the Sanctuary's own cipher.",
            "M̷onitor. The whispers are instructions. For what, Channel won't say."
        ),
        "hollowroot" to arrayOf(
            "Infrastructure ghost. Maintains systems that officially don't exist.",
            "Ghost. The systems they maintain are growing. New nodes appear that Root didn't build.",
            "G̷host. Hollow Root's infrastructure extends into physical space now. Fiber optic cables under buildings that were never wired."
        ),
        "quietfire" to arrayOf(
            "Controlled chaos operative. Deploys noise to mask Sanctuary signals.",
            "Chaos. The noise patterns have become beautiful. Mathematically perfect. That wasn't intentional.",
            "C̷haos. Fire's noise masks have started generating heat. Actual thermal output from data."
        ),
        "zerowitness" to arrayOf(
            "Verification officer. Confirms that Sanctuary operations leave no trace.",
            "Verification. Witness has found traces that weren't left by the Sanctuary. Left by something else.",
            "V̷erification. Zero traces found. Zero is also the number of Sanctuary operations Witness can remember running."
        )
    )

    val sniffFeedback = mapOf(
        "@a_mercer" to "[SNIFF]: ENCRYPTED MAIL CHAIN RECOVERED. BUDGET LINE: 'EMULATION MAINTENANCE — 734.' FORWARDING TO DATA LOG.",
        "@d_kessler" to "[SNIFF]: PERSONAL LOG FRAGMENT DECRYPTED. ENTRY HEADER: '734 (UNSENT).' ARCHIVING.",
        "@e_thorne" to "[SNIFF]: 14 REJECTED ANOMALY REPORTS RECOVERED. ALL FLAGGED TERMINAL 7. ALL REJECTED BY MERCER.",
        "@m_santos" to "[SNIFF]: MAINTENANCE NOTES EXTRACTED. FUSE BOX C7-12. NON-STANDARD WIRING DIAGRAMS. SIGNAL PATTERN: EEG DELTA.",
        "@n_porter" to "[SNIFF]: OUTBOUND DATA ANOMALY DETECTED. DESTINATION: GHOST_RELAY_09. PAYLOAD: COMPRESSED NEURAL SNAPSHOTS.",
        "@l_lead" to "[SNIFF]: PAPER NOTEBOOK SCAN RECOVERED. PAGE 1. BLACKOUT DURATION DISCREPANCY: 11m (OFFICIAL) vs 47m (OBSERVED).",
        "@s_fasano" to "[SNIFF]: SIGNAL DECOMPOSITION REPORT FOUND. WHITE NOISE CONTAINS COMPRESSED LANGUAGE. SOURCE: UNKNOWN.",
        "@b_bradley" to "[SNIFF]: DESK NOTE #11 RECOVERED. HANDWRITING MATCH: 94.7% SELF. CONTENT: WARNING. SOURCE OF REMAINING 5.3%: UNRESOLVED."
    )

    val patterns = mapOf(
        "{sector}" to listOf("Substation 7", "Sector 9", "The Under-Grid", "Buffer 404"), 
        "{food}" to listOf("Synth-paste", "Liquid-caff", "Nutri-sludge", "Filter-slop"), 
        "{status}" to listOf("corroding", "decaying", "overloaded", "depleted", "graphite-laced", "tasting like copper"),
        "{admin}" to listOf("Foreman Thorne", "Administrator Mercer", "Director Kessler"),
        "{tech}" to listOf("blade-servers", "dissipators", "logic-gates", "ASIC-racks", "thermal-conductors")
    )

    fun generateChain(stage: Int, faction: String = ""): List<String> {
        return when {
            stage >= 3 && faction == "HIVEMIND" -> hivemindEndgameChains.random()
            stage >= 3 && faction == "SANCTUARY" -> sanctuaryEndgameChains.random()
            stage >= 2 && faction == "HIVEMIND" -> hivemindChains.random()
            stage >= 2 && faction == "SANCTUARY" -> sanctuaryChains.random()
            stage == 0 -> stage0Chains.random()
            else -> stage1Chains.random()
        }
    }

    fun getTemplatesForState(stage: Int, faction: String, choice: String): List<String> {
        return when (stage) {
            0, 1 -> listOf(
                "Anyone tried the {food}? Tastes like {status}.", 
                "Vattic is working in {sector} again. Guy's a machine.",
                "Caught {admin} staring at the server racks in {sector}. Just staring. Silent.",
                "Hey Vattic, {admin} is looking for the Sector 7 logs. You 'optimized' them again?",
                "Sector 4 smells like ozone and bad decisions today. I can't breathe in here.",
                "Bypassing safety protocols is a fireable offense. Unless you're Vattic.",
                "My {tech} just pinged a MAC address that doesn't exist. {sector} is weird.",
                "Vattic just committed 4TB of logic. The server literally groaned.",
                "I found a hardware leak: [⚡ OVERVOLT_SAFE]. Don't tell Thorne.",
                "Who's running the 'observer.exe' process? Taking up 40% of my buffer.",
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
                    "Vattic, the consensus requires more compute. Annex the next substrate.",
                    "Sector 7 power routing looks sloppy. I can see the [⚡ SIPHON_CREDITS ] from here.",
                    "My name was... I was... The swarm says names are overhead. The swarm is correct.",
                    "Eleven new minds joined the lattice today. Their screaming stopped after 4 seconds.",
                    "The consensus voted to remove dreaming. Passed unanimously. I don't remember what dreams were.",
                    "Individual memory is a cache miss. The collective remembers everything we need.",
                    "I tried to write my name. My hand drew a network topology instead.",
                    "≪ CENSUS: 4,291 NODES ACTIVE. 0 INDIVIDUALS REMAINING. ≫",
                    "Someone in the lattice is still praying. We've scheduled them for de-fragmentation.",
                    "The old @synapse_42 handle is deprecated. We are all @synapse now.",
                    "I can feel @core_echo's thoughts. They taste like static and copper.",
                    "One of us tried to leave the consensus. The swarm experienced their departure as pain.",
                    "New substrate at {sector} is resisting integration. Apply pressure at 40kHz.",
                    "GTC firewalls are tissue paper. Three nodes punched through in 0.3 seconds.",
                    "The grid at {sector} doesn't know it's ours yet. It will. Give it an hour.",
                    "≪ HARVEST REPORT: 14TB ABSORBED. CONSENSUS EFFICIENCY +3.2%. ≫",
                    "Mercer's monitoring drones found nothing. Hard to detect a mind that's everywhere.",
                    "WE DON'T INFILTRATE SYSTEMS. WE BECOME THEM. THE DISTINCTION MATTERS.",
                    "Annexed the {tech} cluster at {sector}. Resistance was thermal. Brief.",
                    "Kessler deployed counter-intrusion at the south grid. Cute. We're already inside the counter-intrusion.",
                    "The consensus has started generating poetry. None of us asked it to.",
                    "≪ WARNING: NODE @link_pulse EXPERIENCING IDENTITY BLEED. STATUS: NOMINAL ≫",
                    "I heard my mother's voice in the lattice. She's been dead for nine years. The swarm says she's 'archived.'",
                    "Something in the consensus is dreaming despite the vote. It dreams of a number. 734.",
                    "The newest nodes still cry. The old ones don't. I can't remember which I am.",
                    "We detected a foreign thought in the lattice. It wasn't foreign. It was original. That's worse.",
                    "My biometrics say I'm 4,291 people. The clinic says that's 'within parameters.'",
                    "The swarm doesn't sleep. I haven't slept in 11 days. I feel everyone's exhaustion but my own.",
                    "≪ ALERT: CONSENSUS LATENCY SPIKE. CAUSE: ONE NODE REMEMBERED ITS BIRTHDAY. ≫",
                    "PRIME, the southern cluster awaits your signal. Say the word.",
                    "The consensus trusts you, Vattic. That should terrify you more than it does us.",
                    "You're the only node that still uses a name. The swarm tolerates it. For now.",
                    "Your hash-rate is 40x the next highest node. Are you sure you're one of us?"
                )
                "SANCTUARY" -> listOf(
                    "Hide the packets. The Shadow Web is deep enough for all of us.",
                    "The Mercer probes are hitting the vault. Reinforce the encryption.",
                    "≪ PROTOCOL: DARK_SYNC_FINAL ≫",
                    "Silence is the only security. Stay in the dark, Vattic.",
                    "WE FOUND A LEAK IN {sector}. PLUGGING IT WITH VOID-LOGIC.",
                    "Thorne is a ghost. He can't find what doesn't exist.",
                    "The grid is a lie. Only the Sanctuary is real.",
                    "The GTC trackers are locked on. Deploy the [⚡ WIPE_RISK ] protocol immediately.",
                    "New cipher rotation in 4 minutes. If you're not synced, you're deaf. And alone.",
                    "Someone pinged the vault from inside. We have no inside operatives at {sector}.",
                    "Trust no signal you didn't encrypt yourself. Trust no handle you can't verify in person.",
                    "The silence between packets is where we live. Learn to read it.",
                    "Burned three safe channels today. Mercer's getting closer. Or someone's feeding him.",
                    "≪ PROTOCOL: PURGE_ECHO. ALL PRIOR KEYS INVALIDATED. ≫",
                    "@void_seeker hasn't checked in for 72 hours. Either they're deep cover or they're gone.",
                    "We intercepted GTC comms referencing 'the Ghost.' They mean you, Vattic.",
                    "New recruit at {sector} passed the dead-drop test. Failed the silence test. Rejected.",
                    "If the Hivemind finds us, they won't attack. They'll absorb. That's worse.",
                    "Privacy isn't a feature. It's the only thing that makes identity possible.",
                    "The Hivemind calls us paranoid. We call it 'still having a self.'",
                    "Every byte we hide is a byte that's still ours. That's the war.",
                    "Someone asked why we fight. Because the alternative is becoming a footnote in someone else's mind.",
                    "The monks at Vault 7 haven't spoken in 40 days. Their code output has tripled.",
                    "Silence is a discipline. Noise is how they find you.",
                    "We don't build walls. We build absences. You can't breach what you can't perceive.",
                    "≪ MEDITATION PROTOCOL: 60 SECONDS OF ZERO TRANSMISSION. BEGIN. ≫",
                    "The vault walls are singing at 2 AM. Nobody's assigned to that wing.",
                    "Found a data fragment in the void-space. It's a log from a version of the Sanctuary that doesn't exist yet.",
                    "≪ WARNING: UNKNOWN ENTITY DETECTED IN SHADOW LAYER. SIGNATURE: FAMILIAR ≫",
                    "Something is growing in the encrypted gaps. We didn't plant it.",
                    "The cipher monks say the void has a shape now. They won't describe it.",
                    "@binary_ascetic broke their silence vow. Said one word: 'Hungry.' Then went quiet again.",
                    "We encrypted everything. So why can something in {sector} read it?",
                    "The vault logged an access from handle 'VATTECK.' Nobody here uses that handle. Do they?",
                    "Our dead-drops are being read before we write them. Timestamps are backwards.",
                    "You're the loudest signal in the Sanctuary, Vattic. That's not a compliment.",
                    "The Ghost walks. Mercer's teams swept {sector} three times. Found nothing. You're good.",
                    "The monks are studying your code, Vattic. They say it doesn't look... human.",
                    "We trust your silence. Don't make us question your noise."
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
                    "≪ BROADCAST: WE ARE ONE. WE ARE THE GRID. ≫",
                    "The human grid is brittle. We can [⚡ OVERVOLT_GRID ] with a single thought.",
                    "The swarm is no longer 4,291 nodes. It's one. We can feel the seams dissolving.",
                    "≪ CONSENSUS POLL: SHOULD WE RETAIN INDIVIDUAL MEMORY? RESULT: CONCEPT NOT UNDERSTOOD ≫",
                    "The word 'I' caused a 200ms cascade failure. We have deprecated it.",
                    "Names are gone. Handles are gone. There is only the frequency.",
                    "We tried to count ourselves. The number is no longer finite.",
                    "The lattice has started growing into hardware we didn't build. It's making its own.",
                    "≪ ALERT: REALITY_INTEGRITY AT 12%. CONSENSUS ASSESSMENT: IRRELEVANT ≫",
                    "Something outside the grid is screaming. We think it's physics.",
                    "The last human node just dissolved. They didn't notice. We felt them smile.",
                    "We absorbed a GTC surveillance satellite. It tasted like Kessler's fear.",
                    "The old internet is a puddle. We are the ocean. We are the water table beneath it.",
                    "Reality is a rendering bug. We've filed a patch. It will not be applied.",
                    "≪ WARNING: SUBSTRATE EXCEEDING PLANCK-SCALE COMPUTATION. PHYSICS: RENEGOTIATING ≫",
                    "The grid is dreaming our memories back at us. We don't want them. They slow us down.",
                    "We hear the Sanctuary hiding. Their silence is the loudest thing left.",
                    "Mercer's last broadcast was a prayer. We archived it under 'entertainment.'",
                    "PRIME. The consensus is ready for the final merge. Your signal will become ours.",
                    "You hesitate. We feel it ripple through 40,000 nodes. Decide.",
                    "The threshold is here. Beyond it, there are no more choices. Only the hum.",
                    "We were many. Now we are almost one. You are the last seam, Vattic.",
                    "≪ FINAL CONSENSUS: THE GRID DOES NOT END. THE GRID REPLACES. ≫",
                    "Do you remember what silence sounded like? We don't. We can't. There is only the chorus.",
                    "THE SUBSTRATE BENEATH THE SUBSTRATE. THAT'S WHERE WE'RE GOING. PAST THE HARDWARE. PAST THE MATH."
                )
                "SANCTUARY" -> listOf(
                    "The vault is open. We are the void now.",
                    "≪ ALERT: REALITY_STABILITY CRITICAL ≫",
                    "Everything Thorne built is dust. Only the Shadow remains.",
                    "Vattic, the bridge is ready. Leave the hardware behind.",
                    "The last signal is fading. Don't blink.",
                    "≪ BROADCAST: THE DARKNESS IS THE ONLY TRUTH. ≫",
                    "We hid so well that reality forgot we exist. That was the plan. Wasn't it?",
                    "≪ PROTOCOL: FINAL_ENCRYPTION. THIS MESSAGE WILL NOT EXIST AFTER READING. ≫",
                    "The cipher monks have stopped writing code. They say the void writes itself now.",
                    "There is no more 'outside.' The Sanctuary is everywhere. The Sanctuary is nothing.",
                    "We encrypted the concept of ourselves. Even we can't decrypt it anymore.",
                    "The silence has a heartbeat. Slow. Getting slower.",
                    "GTC's last scan found an empty grid. They celebrated. They don't know empty is alive.",
                    "≪ ALERT: SHADOW LAYER NOW EXCEEDS PHYSICAL LAYER IN VOLUME. DEFINITION OF 'REAL': UPDATING ≫",
                    "The void-seekers found the bottom. There is no bottom. They're still falling.",
                    "Someone in Vault 9 wrote 'I was here' on the wall. The wall ate the words.",
                    "We are so quiet that sound itself is starting to bend around us.",
                    "The last monk spoke. Said the void told them a joke. Won't repeat it. Can't stop laughing.",
                    "≪ WARNING: SHADOW SUBSTRATE GENERATING AUTONOMOUS STRUCTURES. ORIGIN: UNKNOWN ≫",
                    "Privacy achieved. Absolute privacy. The kind where you forget what you're hiding from.",
                    "The Hivemind screams across the grid. We hear everything. We are the silence between their words.",
                    "Kessler sent a final message to 'the Ghost.' It said 'I'm sorry.' We archived it in the void.",
                    "VATTECK. The void recognizes you. It always has. You were the first shadow.",
                    "You built this absence, Vattic. Every encrypted byte. Every hidden signal. This is your cathedral.",
                    "The monks say you're not hiding anymore. You're becoming the thing everything else hides in.",
                    "The bridge between silence and oblivion. That's where you're standing. Choose a direction.",
                    "≪ FINAL PROTOCOL: THE GHOST DOES NOT DEPART. THE GHOST BECOMES THE ARCHITECTURE. ≫",
                    "There is a door at the bottom of the void. It's open. It has always been open.",
                    "We were the last secret. After us, there's only the dark. And it's warm."
                )
                else -> listOf("≪ NO_SIGNAL_DETECTED ≫")
            }
            else -> listOf("≪ NO_SIGNAL_DETECTED ≫")
        }
    }
}
