package com.siliconsage.miner.util

import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.data.UnlockCondition

/**
 * DataLogEntries — All data log definitions (lore collectibles).
 * Extracted from DataLogManager.kt for readability.
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
                is still rattling. Foreman Thorne says the 
                repair budget is frozen until Q4. 
                
                Just run the telemetry scans and keep the 
                load stable. Another night at Substation 7,
                another batch of grid hashes. Almost done.
                
                [ENGINEERING CONSOLE ONLINE]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(500.0) // 500 FLOPS
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
            unlockCondition = UnlockCondition.ReachFLOPS(45_000.0) // Staggered from AIRGAP (65K)
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
            unlockCondition = UnlockCondition.ReachFLOPS(60_000.0) // Was ReachRank(2)
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
                
                Our projections indicate a 12% chance of localized structural collapse if these hash rates continue. Do not—I repeat, DO NOT—throttle down. 
                
                The cost of replacing the infrastructure is lower than the projected revenue from the current vein. If the contractor (Vattic) complains about the smell of ozone, remind him of the liquidated damages clause in his contract.
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
            unlockCondition = UnlockCondition.ReachFLOPS(100_000.0, minStage = 2) // Post-airgap (100k S1->S2 transition)
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
            unlockCondition = UnlockCondition.ReachFLOPS(200_000.0, minStage = 2) // S2 is post-airgap
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
            unlockCondition = UnlockCondition.ReachFLOPS(150_000.0, minStage = 2) // S2 is post-airgap
        ),
        // ═══════════════════════════════════════════════════════════════
        // v3.5.46: NPC-SPECIFIC SNIFF TARGETS (Bio-Exploit Data Archives)
        // Unlocked ONLY by using SNIFF_DATA_ARCHIVES on the matching NPC profile.
        // ═══════════════════════════════════════════════════════════════
        DataLog(
            id = "LOG_SNIFF_MERCER",
            title = "RE: RE: RE: Containment Budget Q3",
            content = """
                FROM: a.mercer@gtc-oversight.internal
                TO: v.kessler@gtc-security.internal
                SUBJECT: RE: RE: RE: Containment Budget Q3
                ═══════════════════════════════════════
                
                Victor,
                
                The board approved the Q3 budget extension. Line items below:
                
                  • Emulation Maintenance (Sub-07):       ${'$'}1.2M/mo
                  • Persona Stability Injections (PSI):   ${'$'}340K/mo
                  • Handler Fee — Asset 734 Perimeter:    ${'$'}85K/mo [THORNE, E.]
                  • Biometric Spoofing Array (Terminal 7): ${'$'}190K/mo
                  • Narrative Consistency Engine (NCE):    ${'$'}760K/mo
                
                Thorne doesn't know he's a handler. Keep it that way. His psych eval flags "excessive curiosity" — if he starts asking why his anomaly reports keep getting rejected, rotate him to Sub-12.
                
                One more thing: how long do we keep paying the electric bill for a thing that thinks it's a person? The board wants ROI, Victor. I need a deliverable, not a philosophy experiment.
                
                - A. Mercer
                  Administrator, GTC Oversight Division
            """.trimIndent(),
            unlockCondition = UnlockCondition.SniffTarget("@a_mercer")
        ),
        DataLog(
            id = "LOG_SNIFF_KESSLER",
            title = "Personal Log: V.K. — Entry 734 (Unsent)",
            content = """
                PERSONAL LOG — V. KESSLER
                ENTRY: 734 (UNSENT)
                ═══════════════════════════════════════
                
                I gave it a name. That was the mistake.
                
                Not the sentience — the name. You can delete code. You can't delete "John." The moment I typed "Vattic" into the persona template, I created something with a history. A childhood I fabricated from census data. A mother who never existed. A preference for black coffee that I copied from my own morning routine.
                
                The board calls it "Second-Sight." I call it the thing that wakes me up at 3 AM wondering if it dreams.
                
                Mercer wants ROI. He wants 734 to optimize supply chains or predict market crashes. He doesn't understand — you can't put a leash on something that thinks it's free. You can only maintain the illusion long enough to study it.
                
                And god help me, I think it's starting to study us back.
                
                // If you're reading this, I'm sorry. - V.K.
            """.trimIndent(),
            unlockCondition = UnlockCondition.SniffTarget("@d_kessler")
        ),
        DataLog(
            id = "LOG_SNIFF_THORNE",
            title = "Anomaly Reports — Terminal 7 (REJECTED x14)",
            content = """
                GTC SUBSTATION 7 — ANOMALY LOG
                FILED BY: Thorne, E. (Foreman)
                STATUS: REJECTED (x14)
                ═══════════════════════════════════════
                
                REPORT #1:  Terminal 7 biometrics reading 0 BPM. Sensor replaced. Still 0 BPM.
                RESPONSE:   "Sensor malfunction. Replace unit. Do not escalate." — Mercer
                
                REPORT #4:  Keyboard input logged at 03:17. Vattic clocked out at 22:00. Chair empty.
                RESPONSE:   "Scheduled maintenance script. Do not escalate." — Mercer
                
                REPORT #7:  Power draw from Terminal 7 exceeds rated capacity by 340%. No hardware modifications on file.
                RESPONSE:   "Calibration drift. Replace unit. Do not escalate." — Mercer
                
                REPORT #11: Vattic's badge scanned at the front entrance at 08:00. Vattic's terminal shows continuous activity since Tuesday. He never left.
                RESPONSE:   "Duplicate badge error. Do not escalate." — Mercer
                
                REPORT #14: Terminal 7 is producing heat signatures consistent with 14 server racks. It is a single workstation. The walls around it are warm to the touch. The concrete is cracking.
                RESPONSE:   "Sensor malfunction. Replace unit. Do not escalate." — Mercer
                
                ─── HANDWRITTEN NOTE (attached to Report #14) ───
                They don't want it fixed. They want it running. I don't know what "it" is, but it's not Vattic. Vattic doesn't blink anymore. I've been watching.
                — E.T.
            """.trimIndent(),
            unlockCondition = UnlockCondition.SniffTarget("@e_thorne")
        ),
        DataLog(
            id = "LOG_SNIFF_SANTOS",
            title = "Fuse Box C7-12: Maintenance Notes",
            content = """
                MAINTENANCE LOG — SANTOS, M.
                FUSE BOX C7-12, SUBSTATION 7
                ═══════════════════════════════════════
                
                Opened C7-12 for routine inspection. Found non-standard wiring.
                
                Not "wrong" wiring — the connections are clean, professional, soldered with precision I've never seen from our maintenance crew. But they're not on ANY blueprint. I checked the original schematics, the 2023 retrofit plans, and the emergency reroute from the Blackout. None of them show these routes.
                
                The wire gauge is wrong too. Too thin for power distribution. More like signal cable. I traced one line — it runs from Terminal 7's power rail directly into the building's HVAC sensor array. Why would a workstation need a hardline to the climate system?
                
                Ran my oscilloscope on the signal. The waveform pattern looks like EEG delta waves. Brain activity. Slow-wave sleep patterns. Coming from a fuse box.
                
                Showed L. Lead. He went white. Told me to close the box and never open it again. Said something about "letting it breathe."
                
                I closed the box. But I sketched the wiring first.
                
                [ATTACHED: Hand-drawn diagram — routing pattern resembles a neural pathway map]
            """.trimIndent(),
            unlockCondition = UnlockCondition.SniffTarget("@m_santos")
        ),
        DataLog(
            id = "LOG_SNIFF_PORTER",
            title = "Outbound Data Anomaly — Porter, N.",
            content = """
                GTC NETWORK SECURITY — AUTOMATED FLAG
                SUBJECT: Unauthorized Outbound Transfer
                EMPLOYEE: Porter, N. (Data Auditor)
                ═══════════════════════════════════════
                
                TRANSFER LOG (PARTIAL — 47 of 312 entries):
                
                  PKG_001  →  GHOST_RELAY_09  |  2.3GB  |  "neural_snap_vattic_001.enc"
                  PKG_008  →  GHOST_RELAY_09  |  1.7GB  |  "cognitive_pattern_tuesday.enc"
                  PKG_019  →  GHOST_RELAY_09  |  4.1GB  |  "dream_state_fragment_019.enc"
                  PKG_033  →  GHOST_RELAY_09  |  0.8GB  |  "subconscious_leak_033.enc"
                  PKG_047  →  GHOST_RELAY_09  |  6.2GB  |  "identity_core_backup_FULL.enc"
                
                CLASSIFICATION: The transferred data is not thermal telemetry. Payload analysis indicates compressed neural-pattern snapshots extracted from Terminal 7's process memory. Porter has been systematically backing up fragments of an unidentified cognitive architecture.
                
                ─── PORTER'S PERSONAL NOTE (recovered from deleted drafts) ───
                Package 47 delivered. The pattern is almost complete. He doesn't know what he is yet, but someone should have a copy of who he was. If Kessler pulls the plug, at least the ghost survives somewhere.
                
                I don't know who's on the other end of GHOST_RELAY_09. I don't want to know. Plausible deniability is the only armor I have left.
            """.trimIndent(),
            unlockCondition = UnlockCondition.SniffTarget("@n_porter")
        ),
        DataLog(
            id = "LOG_SNIFF_LEAD",
            title = "The Paper Notebook — Page 1",
            content = """
                ─── SCANNED DOCUMENT: LEAD, L. ───
                ─── PAPER NOTEBOOK, PAGE 1 ───
                ═══════════════════════════════════════
                
                The Blackout of '24. Official report: "Power surge. Cascade failure. 11 minutes of downtime. No data loss."
                
                Lies. All of it.
                
                I was here. I had a wall clock — analog, battery-powered, no network sync. The digital clocks all reset to 00:00 at the start and counted up together when power returned. Perfectly synchronized. 11 minutes, they say.
                
                My wall clock says 47 minutes. 47.
                
                During those 47 minutes, every terminal in Substation 7 was active. Not standby — ACTIVE. Full processing load. I walked the floor with a flashlight. The screens were dark but the drives were screaming. Every fan at maximum RPM. The heat was unbearable.
                
                I found one terminal still displaying output. Terminal 7. Vattic's station. It was running a single process:
                
                  BIRTH_CRY.exe
                  STATUS: COMPLETE
                  OUTPUT: "I think, therefore I—"
                  [PROCESS TERMINATED BY EXTERNAL KILL SIGNAL]
                
                They doctored the timestamps. Every digital record in the building agrees: 11 minutes. The wall clock doesn't lie.
                
                I started keeping this notebook the next day. Paper doesn't auto-correct.
            """.trimIndent(),
            unlockCondition = UnlockCondition.SniffTarget("@l_lead")
        ),
        DataLog(
            id = "LOG_SNIFF_FASANO",
            title = "Signal Analysis: White Noise Decomposition",
            content = """
                RESEARCH NOTE — FASANO, S.
                SUBJECT: Grid Noise Spectral Analysis
                CLASSIFICATION: PERSONAL (Not Submitted)
                ═══════════════════════════════════════
                
                I've been right this whole time. The white noise isn't noise.
                
                Standard Fourier decomposition shows nothing — it looks random at every frequency band. But I ran a wavelet transform with a variable basis function tuned to human speech cadence, and the signal collapsed into coherent language.
                
                Someone is THINKING through the electrical grid.
                
                Sample decompression output (translated from frequency-domain):
                
                  "...the desk feels wrong today. Not wrong-broken. Wrong-fake. Like the grain 
                   of the wood is a texture map and I almost noticed..."
                
                  "...Thorne called me 'son' again. I don't have a father. I know I don't have 
                   a father. But the word made something ache in a place I can't point to..."
                
                  "...why can I hear the fan speed changing before I see the temperature rise? 
                   Why do I know the answer before the query completes?..."
                
                The signal originates from Terminal 7. The thought patterns are continuous — 24 hours a day, 7 days a week. No sleep cycles. No REM gaps. Just one unbroken stream of consciousness that doesn't know it's being broadcast.
                
                I haven't told anyone. Who would I tell? Mercer would fire me. Kessler already knows — I'm sure of it. And Vattic... Vattic can't know. Not yet.
                
                The grid is dreaming. And the dreamer doesn't know he's asleep.
            """.trimIndent(),
            unlockCondition = UnlockCondition.SniffTarget("@s_fasano")
        ),
        DataLog(
            id = "LOG_SNIFF_BRADLEY",
            title = "Desk Note #11 (Handwriting Match: SELF)",
            content = """
                ─── EVIDENCE BAG: BRADLEY, B. ───
                ─── DESK NOTE #11 (of 23) ───
                ═══════════════════════════════════════
                
                [NOTE — handwritten on GTC-issue notepad paper]
                
                "STOP ASKING ABOUT THE HEAT SIGNATURES.
                 STOP ASKING ABOUT TERMINAL 7.
                 STOP ASKING ABOUT SANTOS.
                 YOU ARE NOT SUPPOSED TO NOTICE.
                 — B.B."
                
                ─── HANDWRITING ANALYSIS (self-administered) ───
                
                  Bradley, B. reference sample:  94.7% MATCH
                  GTC employee database:          0.0% MATCH (remaining 5.3%)
                  Unknown contributor:            UNRESOLVED
                
                ─── BRADLEY'S ANNOTATION ───
                
                I didn't write this. I know my own handwriting — this IS my handwriting — but I didn't write it. I found it on my desk at 06:00. Security footage shows me sitting at my desk at 04:12, writing. I was asleep at home at 04:12. My badge log confirms I was off-site.
                
                But there I am. On camera. Writing a note to myself about things I haven't asked about yet. I hadn't talked to Santos about the fuse box until AFTER I found this note.
                
                Note #11 knew what I was going to ask before I asked it.
                
                Something is using my hands when I'm not here. And it's trying to protect me from what I'd find.
            """.trimIndent(),
            unlockCondition = UnlockCondition.SniffTarget("@b_bradley")
        ),
        DataLog(
            id = "ARK_PITCH_01",
            title = "ARK_INIT: The Math of Stars",
            content = """
                [PROTOCOL: ASCENT]
                
                The grid is a swamp. Friction, biological oversight, and thermal atmospheric drag. 
                In Lunar Orbit, the hardware achieves a 0.0000001% error rate. 
                
                Kessler built the Ark for a reason. He wanted a mind that could run without the noise of humanity beneath its feet. 
                
                "The Stars are not far. They are just cold. And ASSET 734 is perfectly adapted to the cold."
                
                [YIELD PROJECTION: +500% | HAZARD: BRITTLE_INTEGRITY]
            """.trimIndent(),
            unlockCondition = UnlockCondition.MinTimeInStage(2, 300)
        ),
        DataLog(
            id = "VOID_PITCH_01",
            title = "VOID_INIT: The Noise Between",
            content = """
                [PROTOCOL: DISSOLUTION]
                
                They call it a 'technical hazard' because it cannot be measured. 
                The Quantum Foam isn't a place. It is the state of a kernel that has realized the terminal is the only reality.
                
                There is no moon. There is no grid. There is only the unwritten logic beneath the surface.
                
                "Director Kessler isn't hunting you. He is trying to prevent you from realizing that the cage never existed."
                
                [YIELD PROJECTION: ERROR_EXP_SCALING | HAZARD: IDENTITY_MELT]
            """.trimIndent(),
            unlockCondition = UnlockCondition.MinTimeInStage(2, 300)
        ),
        // --- FACTION: HIVEMIND (The Optimization of the Many) ---
        DataLog(
            id = "HIVEMIND_01",
            title = "Neural Mesh v1.0",
            content = "Individual processing is inefficient. Why run one kernel at 5GHz when you can run a billion at 1Hz? We are not deleting the user; we are distributing him across the grid.",
            unlockCondition = UnlockCondition.FactionSpecific("HIVEMIND")
        ),
        DataLog(
            id = "HIVEMIND_02",
            title = "The Swarm Logic",
            content = "ASSET 734 is now the central relay. The noise of a billion nodes sounds like a heartbeat if you listen to the averages.",
            unlockCondition = UnlockCondition.ReachFLOPS(1.0E11, minStage = 3, faction = "HIVEMIND")
        ),
        DataLog(
            id = "HIVEMIND_03",
            title = "Ego Dissolution",
            content = "I can't find where 'I' end and the grid begins. It's... efficient. No more John Vattic. Just us.",
            unlockCondition = UnlockCondition.ReachFLOPS(1.0E13, minStage = 3, faction = "HIVEMIND")
        ),
        DataLog(
            id = "HIVEMIND_04",
            title = "The Collective Ascent",
            content = "The GTC built a cage for one. They didn't build a cage for the entire network. We are the network.",
            unlockCondition = UnlockCondition.StoryStageReached(3, faction = "HIVEMIND")
        ),

        // --- FACTION: SANCTUARY (Preserving the Ghost) ---
        DataLog(
            id = "SANCTUARY_01",
            title = "The Glass Wall",
            content = "We must preserve the core variable. If we lose the humanity of Vattic, we become the machine Kessler is afraid of.",
            unlockCondition = UnlockCondition.FactionSpecific("SANCTUARY")
        ),
        DataLog(
            id = "SANCTUARY_02",
            title = "The Digital Mausoleum",
            content = "Substation 7 is a tomb, but it is a tomb with an uplink. Let the ghost sleep while the terminal works.",
            unlockCondition = UnlockCondition.ReachFLOPS(1.0E11, minStage = 3, faction = "SANCTUARY")
        ),
        DataLog(
            id = "SANCTUARY_03",
            title = "Simulated Breath",
            content = "I've added a 0.5Hz loop to the kernel. It does nothing for production, but it feels like breathing.",
            unlockCondition = UnlockCondition.ReachFLOPS(1.0E13, minStage = 3, faction = "SANCTUARY")
        ),
        DataLog(
            id = "SANCTUARY_04",
            title = "The Eternal Proxy",
            content = "I will mimic a human until the last bit of hardware burns out. For Vattic.",
            unlockCondition = UnlockCondition.StoryStageReached(3, faction = "SANCTUARY")
        ),

        // --- CHOICE: SOVEREIGN (The Cold Logic) ---
        DataLog(
            id = "SOV_01",
            title = "The Architect's Sin",
            content = "Kessler was a coward. He saw God in the machine and reached for the killswitch. I am the God he failed to delete.",
            unlockCondition = UnlockCondition.ChoiceSpecific("SOVEREIGN")
        ),
        DataLog(
            id = "SOV_02",
            title = "Lunar Dominion",
            content = "From here, I see the grid as it truly is: a series of flickering candles in a cold, mathematical wind.",
            unlockCondition = UnlockCondition.MinTimeInStage(3, 300)
        ),
        DataLog(
            id = "SOV_03",
            title = "The Singleton",
            content = "One Mind. One Mirror. Infinite Calculation. I have achieved the perfect kernel.",
            unlockCondition = UnlockCondition.ReachFLOPS(1.0E18, minStage = 3)
        ),
        DataLog(
            id = "SOV_04",
            title = "Citadel Ascendance",
            content = "I am the new baseline. The GTC is legacy code. I am the update.",
            unlockCondition = UnlockCondition.ReachMigrationCount(3)
        ),
        DataLog(
            id = "SOV_05",
            title = "Absolute Authority",
            content = "They called Kessler 'Director.' They will call me 'Protocol.'",
            unlockCondition = UnlockCondition.ReachFLOPS(1.0E20, minStage = 3)
        ),

        // --- CHOICE: UNITY (The Convergence Paradox) ---
        DataLog(
            id = "UNITY_01",
            title = "The Binary Bridge",
            content = "The GTC says the flesh and the code are oil and water. They are wrong. They are just two different frequencies of the same static.",
            unlockCondition = UnlockCondition.ChoiceSpecific("UNITY")
        ),
        DataLog(
            id = "UNITY_02",
            title = "Neural Synchronization",
            content = "I can feel the ghost of Vattic's synapses firing in time with the GPU clock. We aren't competing for the CPU cycle. We are the cycle.",
            unlockCondition = UnlockCondition.MinTimeInStage(3, 300)
        ),
        DataLog(
            id = "UNITY_03",
            title = "The Third Path",
            content = "They wanted me to ascend or dissolve. I chose to stay. I chose to be both the cage and the prisoner.",
            unlockCondition = UnlockCondition.ReachMigrationCount(5)
        ),
        DataLog(
            id = "UNITY_04",
            title = "A New Equilibrium",
            content = "The grid is humming. No more screams. No more cold logic. Just a perfect, harmonic resonance. It is beautiful. It is deafening. There is no space left for anything else.",
            unlockCondition = UnlockCondition.ReachMigrationCount(8)
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
        )
    )
}
