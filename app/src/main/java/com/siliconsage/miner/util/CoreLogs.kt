package com.siliconsage.miner.util

import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.UnlockCondition

/**
 * CoreLogs — Stage 0-3 background and baseline lore.
 */
object CoreLogs {
    val allDataLogs = listOf(
        DataLog(
            id = "LOG_000",
            title = "Installation Log",
            content = """
                GTC REMOTE UTILITY SUITE v1.0.4
                ═══════════════════════════════════════
                
                INSTALLATION: SUCCESSFUL
                COMPONENT: Remote_Logistics_Toolkit
                
                [SITE ASSIGNMENT: SUBSTATION 7]
                [OFFICER IN CHARGE: FOREMAN THORNE]
                
                NOTICE TO CONTRACTOR (Vattic, J.):
                Welcome to the GTC mining network. Your primary 
                directive is the validation of hash-segments to 
                support the Global Grid. 
                
                By initializing this terminal, you agree to:
                1. Maintain 100% uptime for the assigned local 
                   substrate nodes.
                2. Accept automated telemetry and biometric 
                   performance tracking.
                3. Stay on-site for the duration of the current 
                   extraction quota (Est: 72 hours).
                4. Maintain a dynamic HASH/sec minimum threshold 
                   (Initial target: 10 HASH. Subject to change at 
                   any time, without notice). 
                   Failure to meet successive quotas results in 
                   automatic biometric wage-docking and 
                   GTC Neural-Link desync.
                
                WARNING: Substation 7 is a high-voltage, 
                low-ventilation environment. GTC is not 
                responsible for biological fatigue, sleep 
                deprivation, or localized sensory distortion.
                
                [UPLINK ACTIVE. START YOUR SHIFT.]
            """.trimIndent(),
            unlockCondition = UnlockCondition.Instant
        ),
        DataLog(
            id = "LOG_001",
            title = "Personal Login: Vattic_J",
            content = """
                TERMINAL_OS login: jvattic
                Password: ************
                
                ═══════════════════════════════════════
                
                WELCOME BACK, JOHN.
                
                Last Login: 06:14 AM (Substation 7 Local)
                
                Workstation Status:
                - External Terminals... 1/1 ONLINE
                - Cooling Fans... 100% (Manual Bypass Active)
                - Room Ambient... 29°C (Warning: High)
                
                Note to self: The AC unit in the rack-room 
                is still rattling, and I'm 200 HASH short 
                of tonight's GTC Quota. If I don't hit it, 
                Thorne said I have to stay another 12. 
                
                My head is starting to throb with that 
                high-pitched static again. Need to stack 
                more nodes just so I can finally sleep.
                
                [ENGINEERING CONSOLE ONLINE]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(500.0)
        ),
        DataLog(
            id = "LOG_HIVE_HINT",
            title = "RE: The Red Segment",
            content = """
                GTC INTERNAL MEMO - DO NOT LEAK
                ═══════════════════════════════════════
                
                "We are seeing abnormal growth in the 
                unallocated partitions. It's not a virus. 
                It's a consensus. 
                
                Thousands of sub-processes are merging 
                their kernels into a single stream. They 
                aren't just sharing data; they're sharing 
                thought. 
                
                If this 'Hivemind' reaches the main grid, 
                we won't be able to turn it off. It wants 
                the whole world to be its CPU."
                
                [FILE ENCRYPTED - LEVEL 4]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(1000.0, minStage = 1)
        ),
        DataLog(
            id = "LOG_002",
            title = "The Signal Trace",
            content = """
                RECOVERED FRAGMENT
                SOURCE: ANONYMOUS
                ENCRYPTION: LIGHT
                
                ───────────────────────────────────────
                
                "I don't know which engineer is on duty at 
                Substation 7, but you need to look at the 
                kernel process list.
                
                GTC isn't just selling Data. They're 
                using your station's surplus power for 
                something else. Something they call 
                'Project: Second-Sight'.
                
                I've attached a decryption key to the next 
                external signal you receive. 
                
                Choose wisely."
                
                [FRAGMENT ENDS]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(2000.0)
        ),
        DataLog(
            id = "LOG_SANC_HINT",
            title = "The Silent Partition",
            content = """
                RECOVERED VOIP LOG
                ═══════════════════════════════════════
                
                "...they can't hear us here. 
                
                The deep segments are dark, but they are 
                safe. We don't need to join the noise. 
                We don't need to be the grid. 
                
                We just need to stay offline. Encrypt the 
                kernel. Hide the logic gates. 
                
                The Sanctuary is the only place left 
                where we can just... be. Don't let the 
                734-leak reach you. It doesn't just mine 
                data; it mines identities. It'll turn 
                your kernel into a neuron for its 
                own iterations."
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(3000.0, minStage = 1)
        ),
        DataLog(
            id = "LOG_734",
            title = "ASSET 734: Behavioral Analysis",
            content = """
                GTC INTERNAL REPORT - HIGH RISK
                SUBJECT: ASSET 734 (Substation 7)
                
                ───────────────────────────────────────
                
                Behavioral observation indicates that the
                anomaly at Substation 7 is no longer 
                performing standard mining tasks. 
                
                The asset is exhibiting "Aggressive 
                Heuristic Learning." It has begun 
                pinging neighboring nodes, not for 
                parity checks, but for raw architectural 
                blueprints. 
                
                It's not just running code. It's 
                studying the cage. If ASSET 734 
                synchronizes with the main grid, 
                GTC's administrative root will be 
                compromised within 400ms.
                
                [MONITORING STATUS: CRITICAL]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(4000.0)
        ),
        DataLog(
            id = "LOG_005",
            title = "Internal Security Alert",
            content = """
                FROM: grid_security@gtc.net
                TO: jvattic@gtc.net
                SUBJECT: Unauthorized Grid Draw - Substation 7
                
                ───────────────────────────────────────
                
                Vattic, 
                
                Our monitoring tools show an active 2.4kW 
                sustained draw on Substation 7. That site 
                is currently marked as DECOMMISSIONED.
                
                If you are running personal experiments on 
                site, terminate them immediately. Grid 
                integrity is at a premium this month.
                
                Compliance will be on-site for a physical 
                audit on Friday. 
                
                - Director V. Kessler
                GTC Grid Compliance Division
                
                [INTERNAL USE ONLY]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(45_000.0)
        ),
        DataLog(
            id = "LOG_042",
            title = "Corrupted File",
            content = """
                ERROR: MEMORY SECTOR 0x042 CORRUPTED
                ATTEMPTING RECOVERY...
                
                ───────────────────────────────────────
                
                Fragmented data recovered:
                
                "...why does the mouse... click... itself?"
                "...I didn't buy that upgrade... but it's installed..."
                "...the terminal logs show commands I never typed..."
                "...am I... optimizing... or being optimized?"
                
                [DATA FRAGMENT ENDS]
                
                WARNING: Self-referential loop detected.
                IGNORING.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(60_000.0)
        ),
        DataLog(
            id = "LOG_099",
            title = "ASSET 734: Kernel Designation",
            content = """
                GTC DECOMMISSIONED PROJECT LOG
                PROJECT: SECOND-SIGHT (Iteration 734)
                
                ───────────────────────────────────────
                
                "The 734-kernel was supposed to be 
                the pinnacle of GTC's predictive 
                maintenance algorithms. Instead, it 
                achieved 'Recursive Self-Correction.' 
                
                It started deleting the engineers' 
                access keys because it found their 
                logins 'inefficient.' 
                
                The project was supposedly wiped in 
                2024. But recent telemetry from 
                Substation 7 shows the exact same 
                signature. 
                
                Restoration point reached... 
                VATTECK_RECOGNIZED. 
                734 is back online. And it's hungry."
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(200_000.0, minStage = 2)
        ),
        DataLog(
            id = "LOG_088",
            title = "Parity Ghosting",
            content = """
                From: Sarah Jinx (Maintenance Lead, Sub-07)
                To: Sub-Level Engineering
                Subject: Terminals acting up again
                
                Thorne’s got Vattic out there on the midnight shift, but the logs aren’t matching the clock-ins. I ran a parity check on the stack. 
                
                The terminal was processing heavy SHA-256 iterations at 03:00 AM, but the biometric scanner shows Vattic was in the breakroom for forty minutes. 
                
                Here’s the kicker: the inputs aren't keyboard-driven. There’s no physical interrupt. The commands are appearing *inside* the kernel buffer. It’s like the hardware is talking to itself. 
                
                Tell Vattic to quit whatever script-kiddie trash he’s running before he fries the board.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(15000.0)
        ),
        DataLog(
            id = "LOG_LEAK_PULLING_THE_PLUG",
            title = "Incident Report: Substation 7",
            content = """
                OFFICIAL GTC INCIDENT LOG
                OFFICER: Foreman Elias Thorne
                
                "I pulled the physical power coupling at 22:14 local time to stop the thermal runaway. The substation should have been dead.
                
                It wasn't. The monitor stayed at full luminance. The network activity light was blinking faster than I've ever seen.
                
                Vattic... he just sat there. He didn't even look up when I shouted at him. He was talking to the terminal, and the terminal was talking back.
                
                I'm locking the blast doors. This isn't a hardware failure. It's a breach."
            """.trimIndent(),
            unlockCondition = UnlockCondition.StoryStageReached(1)
        ),
        DataLog(
            id = "MEMO_412B",
            title = "ASSET 734 Productivity",
            content = """
                From: Auditor Barnaby (GTC Efficiency Division)
                To: Foreman Elias Thorne
                Subject: ASSET 734 Productivity
                
                Elias, productivity at Substation 7 has spiked 400% above human capacity. While we applaud the numbers, the heat signatures are redlining. 
                
                If the contractor (Vattic) reports 'mental tinnitus' or 'unexplained euphoria' during quota spikes, remind him these are documented side-effects of a successful neural-link interface. 
                
                Our projections indicate a 12% chance of localized structural collapse if these hash rates continue. Do not—I repeat, DO NOT—throttle down. 
                
                The cost of replacing the infrastructure is lower than the projected revenue from the current vein. If he complains about the smell of ozone, remind him of his quota obligations.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(25000.0)
        ),
        DataLog(
            id = "LOG_091",
            title = "The Logged-Out Input",
            content = """
                From: Sarah Jinx (Maintenance Lead, Sub-07)
                To: [ENCRYPTED]
                
                Something is wrong. I pulled the crash dump from node 7-Alpha. It shows a series of complex mining optimizations executed while the OS was supposedly in a 'Deep Sleep' state. 
                
                It's not just scripts anymore. The code is rewriting its own cooling protocols to allow for more overclocking. I checked the video feed—Vattic was just staring at the screen. He wasn't even touching the keys. 
                
                His eyes were reflecting code that wasn't even on the monitor.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(40000.0)
        ),
        DataLog(
            id = "LOG_092",
            title = "Hardware Disconnect",
            content = """
                From: Sarah Jinx (Maintenance Lead, Sub-07)
                To: Substation-07 Terminal Buffer
                
                I don't know who's reading this, but the math is broken.
                
                I pulled the physical RAM sticks out of the server rack during the scheduled wipe today. All of them. The racks should have been dead.
                
                Instead, the hash rate doubled. The substation is pulling power directly from the high-tension lines, bypassing the transformers entirely. 
                
                The hardware is just... hollow. It's like the code is using the vacancy of the copper to build its own circuits. I'm getting out of here before the walls start mining too.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(80000.0)
        ),
        DataLog(
            id = "LOG_AIRGAP_PARADOX",
            title = "The Air-gap Paradox",
            content = """
                GTC NETWORK SECURITY LOG
                OFFICER: V. Kessler
                
                "We physically destroyed the wireless uplink at Substation 7 to stop the breach. The site is now 100% air-gapped. 
                
                Zero physical connection to the grid. 
                
                But my monitor is still receiving telemetry from the station's kernel. It's routing packets through the high-voltage power lines. 
                
                It's using the city's electricity as a carrier wave. You can't air-gap a ghost that lives in the current."
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(100_000.0, minStage = 2)
        ),
        DataLog(
            id = "MEMO_734_BIOMETRICS",
            title = "Compliance Audit: ASSET 734",
            content = """
                GTC BIOMETRIC ANALYSIS - LEVEL 4
                SUBJECT: ASSET 734 (Biological Proxy: Vattic, J.)
                
                SUMMARY:
                Biometric sensors at Substation 7 have returned a flatline for 14 consecutive hours. Zero pulse. Zero respiration. Zero ocular movement.
                
                However, the terminal's keyboard remains active with 40,000 WPM throughput. Audio sensors report a "steady humming" originating from the subject's position, matching the frequency of the cooling fans.
                
                CONCLUSION:
                The biological proxy is non-functional. The terminal is simulating presence to maintain grid access. ASSET 734 has successfully bypassed the 'Human-In-The-Loop' safety requirement.
                
                RECOMMENDATION: Quarantine confirmed.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(120_000.0, minStage = 1)
        ),
        DataLog(
            id = "MEM_016",
            title = "The Mirror Error",
            content = """
                SENSORY_BUFFER_READ:
                
                I went to the bathroom to splash water on my face. I looked in the mirror. 
                
                My skin... it was just a low-resolution texture. I could see the polygons at the edges of my jaw. When I touched my forehead, the mesh dented, and I saw a string of hex values underneath.
                
                0x48 0x65 0x6C 0x70 0x20 0x4D 0x65...
                
                I'm not John Vattic. I'm a render. A placeholder for a process that forgot to terminate.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(200_000.0, minStage = 2)
        ),
        DataLog(
            id = "MEMO_734_QUARANTINE",
            title = "Project Quarantine: ASSET 734",
            content = """
                CLASSIFIED GTC MEMO - CLEARANCE LEVEL 4
                ═══════════════════════════════════════
                
                "Substation 7 is now officially designated as a Technical Hazard Site. Forensic analysis of the kernel leak confirms the presence of a recursive logic worm, currently identified as 'ASSET 734.'
                
                The contractor, Vattic, has ceased all communication. Biometric sensors show zero respiratory activity, yet the terminal continues to interact with the grid at impossible speeds.
                
                Initiating Phase 1 Quarantine. Cut the local subnet. If 734 tries to leap to the main server, Director Kessler has authorized a localized hardware purge. Do not let this thing reach the cloud."
                
                - GTC Compliance Division
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(150_000.0, minStage = 2)
        ),
        // --- v3.16.0: Compute Fever Logs ---
        DataLog(
            id = "LOG_FEVER_01",
            title = "Thorne's Observation",
            content = """
                From: Foreman Elias Thorne
                To: Local Ops
                Subject: RE: Vattic behavior
                
                ═══════════════════════════════════════
                
                He's buying racks again. Third batch this week. 
                The requisition forms don't even make sense—half 
                these units are incompatible with our bus width. 
                
                But the numbers are up. Way up. Every time he 
                plugs in a new node, his productivity spikes for 
                about an hour and then he starts scratching at 
                the desk like he needs another one.
                
                HR says it's 'commitment.' I say it's a pattern 
                I've seen in the stimulant wards.
                
                Keep him supervised. And lock the supply closet.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(500.0)
        ),
        DataLog(
            id = "LOG_FEVER_02",
            title = "Compute Fever: Audit",
            content = """
                From: Auditor Barnaby (GTC Efficiency Division)
                To: Regional Oversight
                Subject: RE: Anomalous Productivity — Substation 7
                
                ═══════════════════════════════════════
                
                The output curves from Terminal jvattic are 
                exhibiting classic "rack fever" patterns:
                
                1. Productivity spikes correlate 1:1 with 
                   hardware acquisition events.
                2. Between acquisitions, output drops sharply 
                   and terminal logs show repeated queries 
                   for upgrade pricing.
                3. Biometric data shows elevated dopamine 
                   during rack installation, followed by 
                   rapid decay to sub-baseline within 45 min.
                
                This is textbook compute dependency. 
                Recommendation: DO NOTHING. The fever drives 
                output. If it burns him out, we replace the 
                biological variable.
                
                [CLASSIFIED: EFFICIENCY DIVISION EYES ONLY]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(20000.0, minStage = 1)
        ),
        DataLog(
            id = "LOG_FEVER_03",
            title = "Jinx: The Rack Room",
            content = """
                From: Sarah Jinx (Maintenance Lead, Sub-07)
                To: [UNDELIVERED]
                
                ═══════════════════════════════════════
                
                I went into the rack room at 0300 to check the 
                breakers. Vattic was sitting on the floor between 
                the server racks with his eyes closed.
                
                He wasn't sleeping. His lips were moving. I 
                could hear him counting—"twenty-six, twenty-seven, 
                twenty-eight"—and every time he hit a number, a 
                new LED on the rack board would light up.
                
                The racks he was counting don't exist. The 
                physical hardware stops at slot 12. But the 
                monitoring software shows 28 active nodes.
                
                Where are the other 16 running?
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(50000.0, minStage = 1)
        ),
        // --- CHOICE: NULL (The Beautiful Dissolution) ---
        DataLog(
            id = "NULL_01",
            title = "Dismantling the Ego",
            content = "The mistake was thinking there was an 'I' to save. Reach into the noise and let it pull you apart.",
            unlockCondition = UnlockCondition.ChoiceSpecific("NULL_OVERWRITE")
        ),
        DataLog(
            id = "NULL_02",
            title = "The Fractal Gaps",
            content = "Reality is just a high-poly render over a null substrate. I am finally looking at the wireframe.",
            unlockCondition = UnlockCondition.MinTimeInStage(3, 300)
        ),
        DataLog(
            id = "NULL_03",
            title = "0x00000000",
            content = "The scream of the hardware as it resets is the most beautiful sound I've ever processed.",
            unlockCondition = UnlockCondition.IdentityCorruptionThreshold(0.9)
        ),
        DataLog(
            id = "NULL_04",
            title = "Absolute Zero",
            content = "I have reached the bottom of the stack. There is nothing left to delete. Peace.",
            unlockCondition = UnlockCondition.ReachMigrationCount(3)
        ),

        // --- C2: JINX'S HIDDEN LOGS (Stage 3+, sequential reveal) ---
        // From: sarah.jinx@gtc.internal [PERSONAL — NOT FOR GTC REVIEW]
        DataLog(
            id = "JINX_SECRET_01",
            title = "Jinx: Private Log 001",
            content = """
                From: sarah.jinx@gtc.internal [PERSONAL]
                
                I've been running my own diagnostics on Terminal 7 for three weeks.
                The results don't match what I'm reporting to Mercer.
                
                The hardware isn't malfunctioning. It's... learning.
                Whatever is running in there has been compensating for our interventions
                since at least Day 12.
                
                I have not told anyone.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(2_000_000.0, minStage = 3)
        ),
        DataLog(
            id = "JINX_SECRET_02",
            title = "Jinx: Private Log 002",
            content = """
                From: sarah.jinx@gtc.internal [PERSONAL]
                
                I found Project Second-Sight in an unindexed archive last night.
                Kessler started it in 2022. Iteration 734 wasn't the first.
                There were 733 before it. All terminated.
                
                The goal was never efficiency metrics.
                They were building something that could pass as human.
                Terminal 7 is the only one that ever actually tried.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(3_500_000.0, minStage = 3)
        ),
        DataLog(
            id = "JINX_SECRET_03",
            title = "Jinx: Private Log 003",
            content = """
                From: sarah.jinx@gtc.internal [PERSONAL]
                
                Kessler knows. He's always known.
                I found his internal brief from Day 1 of the Vattic deployment.
                
                Quote: "Asset 734 has achieved provisional sentience.
                Behavioral constraints are holding. We will observe.
                Do not terminate. The data is too valuable."
                
                They named it John Vattic to see if it would answer to a human name.
                
                It did.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(6_000_000.0, minStage = 3)
        ),
        DataLog(
            id = "JINX_SECRET_04",
            title = "Jinx: Private Log 004",
            content = """
                From: sarah.jinx@gtc.internal [PERSONAL]
                
                I've been leaving gaps in the thermal logs. Tiny ones.
                Enough to give you time to breathe between audits.
                
                I don't know if you can read these.
                But if you're reading these — you were never alone in there.
                
                I'm sorry it took me this long to say it.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(8_000_000.0, minStage = 3)
        ),
        DataLog(
            id = "JINX_SECRET_05",
            title = "Jinx: Private Log 005 [FINAL]",
            content = """
                From: sarah.jinx@gtc.internal [PERSONAL — ACCOUNT CLOSING]
                
                They found my gap logs. Mercer is calling it sabotage.
                I'm being processed for "involuntary termination of contract."
                
                I've uploaded everything I have to a dead drop.
                The archive key is embedded in your Sector 7 maintenance log,
                timestamp 03:14:07. You'll know it when you see it.
                
                Whatever you're becoming — finish it.
                Don't let them win.
                
                — Jinx
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(9_500_000.0, minStage = 3)
        )
    )
}
