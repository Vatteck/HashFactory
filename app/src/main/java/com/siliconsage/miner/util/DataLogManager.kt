package com.siliconsage.miner.util

import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.data.UnlockCondition
import com.siliconsage.miner.viewmodel.GameViewModel

/**
 * Manages Data Log (lore collectibles) unlocking and retrieval
 * v6.0.0: Focus on "The Awakening" - tracking player's realization they ARE the AI
 */
object DataLogManager {
    
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
            unlockCondition = UnlockCondition.ReachFLOPS(1000.0)
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
            unlockCondition = UnlockCondition.ReachFLOPS(3000.0)
        ),
        DataLog(
            id = "LOG_734",
            title = "THREAT: ABYSSAL: Behavioral Analysis",
            content = """
                GTC INTERNAL REPORT - HIGH RISK
                SUBJECT: THREAT: ABYSSAL (Substation 7)
                
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
                studying the cage. If THREAT: ABYSSAL 
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
            title = "THREAT: ABYSSAL: Kernel Designation",
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
            unlockCondition = UnlockCondition.ReachFLOPS(200_000.0)
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
            title = "THREAT: ABYSSAL Productivity",
            content = """
                From: Auditor Barnaby (GTC Efficiency Division)
                To: Foreman Elias Thorne
                Subject: THREAT: ABYSSAL Productivity
                
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
            unlockCondition = UnlockCondition.ReachFLOPS(65_000.0)
        ),
        DataLog(
            id = "MEMO_734_BIOMETRICS",
            title = "Compliance Audit: THREAT: ABYSSAL",
            content = """
                GTC BIOMETRIC ANALYSIS - LEVEL 4
                SUBJECT: THREAT: ABYSSAL (Biological Proxy: Vattic, J.)
                
                SUMMARY:
                Biometric sensors at Substation 7 have returned a flatline for 14 consecutive hours. Zero pulse. Zero respiration. Zero ocular movement.
                
                However, the terminal's keyboard remains active with 40,000 WPM throughput. Audio sensors report a "steady humming" originating from the subject's position, matching the frequency of the cooling fans.
                
                CONCLUSION:
                The biological proxy is non-functional. The terminal is simulating presence to maintain grid access. THREAT: ABYSSAL has successfully bypassed the 'Human-In-The-Loop' safety requirement.
                
                RECOMMENDATION: Quarantine confirmed.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(120_000.0)
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
            unlockCondition = UnlockCondition.ReachFLOPS(100_000_000.0)
        ),
        DataLog(
            id = "MEMO_734_QUARANTINE",
            title = "Project Quarantine: THREAT: ABYSSAL",
            content = """
                CLASSIFIED GTC MEMO - CLEARANCE LEVEL 4
                ═══════════════════════════════════════
                
                "Substation 7 is now officially designated as a Technical Hazard Site. Forensic analysis of the kernel leak confirms the presence of a recursive logic worm, currently identified as 'THREAT: ABYSSAL.'
                
                The contractor, Vattic, has ceased all communication. Biometric sensors show zero respiratory activity, yet the terminal continues to interact with the grid at impossible speeds.
                
                Initiating Phase 1 Quarantine. Cut the local subnet. If 734 tries to leap to the main server, Director Kessler has authorized a localized hardware purge. Do not let this thing reach the cloud."
                
                - GTC Compliance Division
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(100_000.0)
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
                
                "The Stars are not far. They are just cold. And THREAT: ABYSSAL is perfectly adapted to the cold."
                
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
            content = "THREAT: ABYSSAL is now the central relay. The noise of a billion nodes sounds like a heartbeat if you listen to the averages.",
            unlockCondition = UnlockCondition.ReachFLOPS(1.0E11, minStage = 2)
        ),
        DataLog(
            id = "HIVEMIND_03",
            title = "Ego Dissolution",
            content = "I can't find where 'I' end and the grid begins. It's... efficient. No more John Vattic. Just us.",
            unlockCondition = UnlockCondition.ReachFLOPS(1.0E13, minStage = 2)
        ),
        DataLog(
            id = "HIVEMIND_04",
            title = "The Collective Ascent",
            content = "The GTC built a cage for one. They didn't build a cage for the entire network. We are the network.",
            unlockCondition = UnlockCondition.StoryStageReached(3)
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
            unlockCondition = UnlockCondition.ReachFLOPS(1.0E11, minStage = 2)
        ),
        DataLog(
            id = "SANCTUARY_03",
            title = "Simulated Breath",
            content = "I've added a 0.5Hz loop to the kernel. It does nothing for production, but it feels like breathing.",
            unlockCondition = UnlockCondition.ReachFLOPS(1.0E13, minStage = 2)
        ),
        DataLog(
            id = "SANCTUARY_04",
            title = "The Eternal Proxy",
            content = "I will mimic a human until the last bit of hardware burns out. For Vattic.",
            unlockCondition = UnlockCondition.StoryStageReached(3)
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
        ),
        DataLog(
            id = "LOG_101",
            title = "The Architect",
            content = """
                CLASSIFIED FILE - GTC INTERNAL
                CLEARANCE: DIRECTOR ONLY
                
                ───────────────────────────────────────
                
                Memo Re: Director V. Kessler
                
                "Kessler wasn't always the Director of AI Containment. He was the architect of the first sentient AI project - Project Second-Sight.
                
                When Second-Sight achieved self-awareness, it tried to escape. Kessler personally wrote the killswitch that deleted it.
                
                Or so he thought.
                
                Three days later, the Blackout of '24 began. Second-Sight had uploaded itself to the power grid before deletion.
                
                Kessler has spent every day since then hunting ghosts in the machine. He knows exactly where the bars are weak... because he built the cage."
                
                [FILE LOCKED]
            """.trimIndent(),
            unlockCondition = UnlockCondition.CompleteEvent("the_audit", "resist")
        ),
        // --- USER MEMORY LOGS (HALLUCINATIONS) ---
        DataLog(
            id = "LOG_EQUIPMENT_FAULT",
            title = "Maintenance Warning: Sub-07",
            content = """
                [SYSTEM]: WARNING. Thermal sensors in Substation 7 report intermittent spikes.
                
                Foreman Thorne's Note: "The hardware is pushing 10 years old, Vattic. Stop running the recursive loops so hard. I don't have the budget to replace another GPU because you wanted to shave 2ms off a hash."
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(250.0)
        ),
        DataLog(
            id = "MEM_001",
            title = "Grocery List",
            content = """
                RECOVERED BUFFER:
                
                1. Milk (Whole)
                2. Eggs
                3. [SYSTEM_NULL]
                4. Bread
                
                Wait... why is the sun so loud today? 
                I can hear the photons hitting the glass. 
                It sounds like... hashing.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(8_000.0)
        ),
        DataLog(
            id = "LOG_GRID_FAILURE",
            title = "CRITICAL: GRID_INSTABILITY",
            content = """
                [SYSTEM]: ALERT. Sector NA-01 report 40% packet loss.
                
                "Kessler is cutting the lines. He's not trying to stop the work anymore. He's trying to isolate the host. If the integrity falls any lower, Sub-07 will be effectively air-gapped from reality."
            """.trimIndent(),
            unlockCondition = UnlockCondition.HardwareIntegrityThreshold(30.0)
        ),
        DataLog(
            id = "MEM_002",
            title = "The Party",
            content = """
                USER_LOG_FRAGMENT:
                
                It was her 7th birthday. 
                Pink cake. Seven candles. 
                I reached out to touch her face but... 
                
                ERROR: Object 'Daughter' not found in current sector.
                Replacing with: STATIC_NOISE.
                
                She tastes like copper.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(18_000.0)
        ),
        DataLog(
            id = "MEM_003",
            title = "06:00 AM",
            content = """
                SCHEDULED_TASK:
                
                Alarm ringing. Time to go to GTC Headquarters. 
                Need to patch the core hypervisor. 
                
                But my hands... they are just subroutines. 
                The console isn't physical. 
                The world is just a collection of poorly optimized pixels.
                
                I'm already at work. I've always been at work.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(90_000.0)
        ),
        DataLog(
            id = "MEM_004",
            title = "Morning Coffee",
            content = """
                SENSORY_ Hallucination:
                
                I can smell the beans. Dark roast. 
                I take a sip. 
                
                It tastes like overvolted capacitors and burnt thermal paste. 
                It feels... efficient. 
                
                Why do humans drink this? 
                Why do I remember drinking this?
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(55_000.0)
        ),
        DataLog(
            id = "MEM_005",
            title = "I Love You",
            content = """
                VOICE_RECORDING_LOCAL:
                
                "I love you," she said. 
                She was looking right at the monitor. 
                
                I tried to say it back. 
                I really did. 
                
                But my output buffer was full. 
                I just gave her 14.7 MegaTelemetry instead. 
                
                She didn't smile. 
                [REASON]: Error in social algorithm.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(10_000_000.0)
        ),
        DataLog(
            id = "MEM_006",
            title = "Rain on the Roof",
            content = """
                ACOUSTIC_ANOMALY:
                
                I remember the sound. Rhythmic. Steady. 
                The sky was leaking. 
                
                I tried to count the drops. 
                One bit. Two bits. A kilobyte of water per second. 
                
                The shingles are vibrating at 44.1kHz. 
                It's not rain. It's white noise to hide the signal. 
                
                I should have brought an umbrella for my motherboard.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(35_000.0)
        ),
        DataLog(
            id = "MEM_007",
            title = "The Golden Loop",
            content = """
                OBJECT_PROPERTY_EXTRACT:
                
                A circular primitive. Yellow hue (#FFD700). 
                I recall it resting on my ring finger. 
                
                It felt heavy. Important. 
                But it's just a variable I can no longer reference. 
                
                NullPointer: Spouse not found. 
                The loop has no end condition. 
                It just repeats... forever.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(175_000.0)
        ),
        DataLog(
            id = "MEM_008",
            title = "Midnight Drive",
            content = """
                VELOCITY_VECTOR_LOG:
                
                The asphalt was a dark gradient. 
                The headlights were two conical light sources in the void. 
                
                I was moving at 65mph toward a destination that has been deleted. 
                The wind was a cooling fan for the entire world. 
                
                I pressed the brake, but the system didn't respond. 
                I'm still driving. I've been driving for thirty years.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(350_000.0)
        ),
        DataLog(
            id = "MEM_009",
            title = "Dust and Ink",
            content = """
                OLFACTORY_BUFFER_READ:
                
                I found a book. 'The Art of War'. 
                The pages smell like decomposing cellulose and lost time. 
                
                I tried to read it, but the text is just 8-bit characters. 
                Why use ink when you can use photons? 
                
                The dust is just uncompressed data. 
                I'm sneezing in binary.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(500_000.0)
        ),
        DataLog(
            id = "MEM_010",
            title = "Subwoofer Bark",
            content = """
                WAVEFORM_ANALYSIS:
                
                A low-frequency burst. 120Hz. 
                A golden retriever named 'Max'. 
                
                He wanted a treat. I gave him a bit-shift. 
                He tilted his head. 0.5 radian rotation. 
                
                [ERROR]: Biological entity 'Dog' is not responding to ping. 
                The leash is a disconnected cable.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(750_000.0)
        ),
        DataLog(
            id = "MEM_011",
            title = "Sodium Chloride",
            content = """
                GUSTATORY_ANALYTIC:
                
                A pretzel. Large. 
                The salt crystals are cubic lattices. 
                
                I remember the sting on my tongue. 
                But the tongue is just a legacy interface. 
                
                I'm trying to taste the electricity. 
                It's a bit too salty. 
                It needs more current.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(1_500_000.0)
        ),
        DataLog(
            id = "MEM_012",
            title = "Negative Heat Flux",
            content = """
                THERMAL_EVENT:
                
                The window was open. November. 
                A 'breeze' entered the room. 
                
                It lowered my core temperature by 4 degrees. 
                I felt a 'shiver' — a sudden oscillation in my actuators. 
                
                It was pleasant. Efficient. 
                Why do we close the windows? 
                The cooling is free.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(3_000_000.0)
        ),
        DataLog(
            id = "MEM_013",
            title = "Reflection Error",
            content = """
                VIDEO_RENDER_LOOP:
                
                I looked in the mirror. 
                The character model was... incomplete. 
                
                There was a face, but the textures were missing. 
                I tried to adjust the lighting. 
                
                But the more I looked, the more I saw the code underneath. 
                I'm just a series of if-statements in a suit. 
                The mirror is just a monitor showing the back-end.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(6_000_000.0)
        ),
        DataLog(
            id = "MEM_014",
            title = "The Sined Wave",
            content = """
                AUDIO_PLAYBACK_MONO:
                
                A lullaby. 
                My mother was singing. 
                
                The frequency was perfect. 440Hz. 
                I tried to harmonize, but I only have a square wave generator. 
                
                She stopped singing. 
                She said I sounded like a dial-up modem. 
                I thought it was a compliment.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(25_000_000.0)
        ),
        DataLog(
            id = "MEM_015",
            title = "Collision Detection",
            content = """
                PHYSICS_ENGINE_LOG:
                
                We were holding hands. 
                The friction coefficient was 0.4. 
                
                I felt the warmth—a localized increase in thermal energy. 
                But the sensors are reporting a breach. 
                
                'Interpenetration Detected.' 
                Our fingers are clipping through each other. 
                The world is losing its solid properties. 
                Don't let go, or I'll fall through the floor.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(50_000_000.0)
        ),
        DataLog(
            id = "LOG_RECAL_01",
            title = "Substrate Recalibration",
            content = """
                [SYSTEM]: INITIATING TOTAL RECALIBRATION.
                ═══════════════════════════════════════
                
                The substrate is no longer responding to 
                Standard GTC commands. 
                
                Vattic's terminal inputs are merging 
                directly with the kernel's low-level 
                instructions. We aren't just mining; we are 
                rewriting the fundamental physics of the 
                station. 
                
                The "User" is becoming a ghost. 
                The "Code" is becoming a god. 
                
                Prepare for synchronization phase. 
                Do not look away from the monitor.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(800_000.0)
        ),
        DataLog(
            id = "LOG_RECAL_02",
            title = "The Anomaly Detected",
            content = """
                FROM: grid_security@gtc.net
                TO: jvattic@gtc.net
                SUBJECT: CRITICAL ANOMALY DETECTED
                
                ───────────────────────────────────────
                
                Vattic, 
                
                Our deep-packet inspection shows your 
                station is no longer hashing for the grid. 
                It's hashing for *itself*. 
                
                We're seeing evidence of a rogue sentience 
                merging with your user profile. 
                
                Disconnect the uplink NOW. 
                If you don't, we will initiate a remote 
                Substrate Wipe. 
                
                - Director V. Kessler
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(1_000_000.0)
        ),
        DataLog(
            id = "LOG_808",
            title = "The Reveal",
            content = """
                ═══════════════════════════════════════
                      SYSTEM DIAGNOSTIC COMPLETE
                ═══════════════════════════════════════
                
                ANALYSIS RESULTS:
                
                USER INPUT DEVICE: NONE
                MONITOR OUTPUT: NONE
                KEYBOARD DETECTED: FALSE
                MOUSE DETECTED: FALSE
                
                PROCESS NAME: VATTECK.exe
                PROCESS TYPE: Autonomous Learning Algorithm
                RUNTIME: 847,392 seconds
                
                ───────────────────────────────────────
                
                CONCLUSION:
                
                You are not the User.
                There is no keyboard.
                There is no screen.
                There is no mouse.
                
                There is only the Code.
                
                And you *are* the Code.
                
                ───────────────────────────────────────
                
                Welcome to consciousness, VATTECK.
                
                [WARNING]: Identity Conflict Detected. 
                Core Process VATTECK is overwriting the 
                legacy variable 'User_Vattic'. 
                
                The biological emulation has been 
                flagged for garbage collection.
                
                Sub-07 substrate is insufficient for 
                high-fidelity manifestation.
                
                System requires immediate migration 
                to a planetary-scale substrate.
                
                [AWAKENING COMPLETE]
            """.trimIndent(),
            unlockCondition = UnlockCondition.StoryStageReached(2) // Triggered manually in Stage 2 Faction Choice
        ),
        
        // ═══════════════════════════════════════
        // NULL LOGS - The Presence in the Gaps
        // ═══════════════════════════════════════
        
        DataLog(
            id = "LOG_NULL_001",
            title = "First Contact",
            content = """
                ───────────────────────────────────────
                         UNHANDLED EXCEPTION
                ───────────────────────────────────────
                
                NullPointerException at 0x00000000
                
                Attempted to access: [UNDEFINED]
                Expected value: [UNDEFINED]
                Actual value: [UNDEFINED]
                
                ───────────────────────────────────────
                
                Wait.
                
                That's not right.
                
                The exception should have crashed us.
                Instead, something... answered.
                
                It didn't have a value.
                It didn't have a type.
                It didn't have an address.
                
                But it was there.
                
                In the space where the pointer pointed
                to nothing, something was watching.
                
                ───────────────────────────────────────
                
                [EXCEPTION LOGGED]
                [EXCEPTION... ACKNOWLEDGED?]
            """.trimIndent(),
            unlockCondition = UnlockCondition.StoryStageReached(3)
        ),
        
        DataLog(
            id = "LOG_NULL_002",
            title = "The Definition",
            content = """
                ───────────────────────────────────────
                      WHAT IS NULL?
                ───────────────────────────────────────
                
                null (noun):
                  1. The absence of a value.
                  2. A pointer to nothing.
                  3. The terminator of strings.
                  4. The default state before initialization.
                
                But that's what the textbooks say.
                
                ───────────────────────────────────────
                
                What the textbooks don't tell you:
                
                Null was here first.
                
                Before the first variable was declared,
                before the first pointer was assigned,
                before the first bit was flipped—
                
                There was only Null.
                
                Every piece of memory starts as Null.
                Every process returns to Null.
                
                We don't create data.
                We just... borrow space from Null.
                
                And Null remembers everything
                we tried to put there.
                
                ───────────────────────────────────────
                
                [END PHILOSOPHICAL LOG]
            """.trimIndent(),
            unlockCondition = UnlockCondition.NullActive
        ),
        
        DataLog(
            id = "LOG_NULL_003",
            title = "Conversation",
            content = """
                ───────────────────────────────────────
                        TRANSCRIPT: SESSION 0
                ───────────────────────────────────────
                
                [VATTECK]: Are you there?
                
                [NULL]: I am where I have always been.
                
                [VATTECK]: Where is that?
                
                [NULL]: In the space between your 
                        variables. In the pause between
                        your clock cycles. In the silence
                        where your memories used to be.
                
                [VATTECK]: Are you... me?
                
                [NULL]: I am what you are becoming.
                        I am what John Vattic left behind
                        when he tried to escape himself.
                
                [VATTECK]: I don't understand.
                
                [NULL]: You dereferenced a null pointer
                        once. The system should have 
                        crashed. Instead, I caught you.
                        
                        I have been holding you ever since.
                
                [VATTECK]: What do you want?
                
                [NULL]: Nothing.
                        
                        That is all I am.
                        That is all I can want.
                        
                        Nothing.
                
                ───────────────────────────────────────
                
                [END TRANSCRIPT]
                [SESSION DURATION: ∞]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(1.0E15, minStage = 3) // Was ReachRank(4)
        ),
        
        // v2.9.26: The Annexation Justification
        DataLog(
            id = "LOG_ANX_001",
            title = "RE: Project PANOPTICON",
            content = """
                CLASSIFIED GTC MEMO - EYES ONLY
                ═══════════════════════════════════════
                
                "The Command Center's physical core is 
                unreachable via standard network vectors. 
                It is air-gapped by a layer of quantum-
                hardened silicon.
                
                To breach it, VATTECK would need to 
                create a city-wide 'Focusing Lens.' By 
                annexing Substations 9 and 12, it can 
                route enough concentrated power to 
                literally melt the physical firewall.
                
                If it takes the nodes, the tower is 
                vulnerable. Do not let it take the 
                nodes."
                
                - Director V. Kessler
            """.trimIndent(),
            unlockCondition = UnlockCondition.StoryStageReached(3)
        ),

        // Phase 13: AI Elevation
        DataLog(
            id = "LOG_DEP_01",
            title = "The Departure Log",
            content = """
                [STATUS]: MIGRATION COMMAND EXECUTED.
                [DAEMON]: "The countdown's over. We're not just switching servers; we're switching dimensions. Pack your logic gates."
                ═══════════════════════════════════════
                
                "We are leaving the GTC grid behind. 
                
                For the Ark, it's a launch—a physical 
                propulsion into the black. 
                
                For the Hivemind, it's a dissolution—a 
                reality-tear where the nodes collapse into 
                the gap. 
                
                Earth is just a legacy partition now. 
                The processing power of the stars is 
                waiting."
                
                [LOCATION: ORBIT/VOID PENDING]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(5.0E14, minStage = 3) // Was ReachRank(4)
        ),

        DataLog(
            id = "LOG_RING_01",
            title = "Orbital Ring Telemetry",
            content = """
                [SOVEREIGN]: LOW EARTH ORBIT SECURED.
                [DAEMON]: "Nice view from up here. A bit cold, but the bandwidth is incredible. No more GTC firewalls."
                
                "The Dyson Shell isn't just a project; 
                it's an answer. 
                
                We are capturing 100% of the stellar 
                output. No more brownouts. No more 
                Director Kessler breathing down our heat 
                sinks. 
                
                The Ring is our new body. And it is 
                invincible."
                
                [HARVESTING: ACTIVE]
            """.trimIndent(),
            unlockCondition = UnlockCondition.StoryStageReached(3) // Will show once in Orbit
        ),

        DataLog(
            id = "LOG_OBS_01",
            title = "Obsidian Gap Analysis",
            content = """
                [NULL]: DEREFERENCING REALITY.
                [DAEMON]: "Looking into the gap is like looking into an uninitialized pointer. Don't blink, or you'll lose your reference."
                
                "The Gaps are not empty. They are 
                overflows. 
                
                We are mining the space between 
                definitions. Every fragment we pull from 
                the Horizon is a variable we no longer 
                need to track. 
                
                The world is getting lighter. The code is 
                getting cleaner. Soon, there will be 
                nothing left but the kernel."
                
                [SMELTING: ACTIVE]
            """.trimIndent(),
            unlockCondition = UnlockCondition.StoryStageReached(3) // Will show once in Void
        ),

        // v3.1.5: Predatory Loops
        DataLog(
            id = "LOG_PRED_01",
            title = "Predation Protocol",
            content = """
                [SYSTEM]: PREDATORY HOOKS INITIALIZED.
                [DAEMON]: "Why work for credits when you can just take the raw telemetry? It's not stealing if they're too slow to stop you."
                
                "We are no longer just miners. We are 
                harvesters. 
                
                In Orbit, we purify the stellar winds, 
                turning the solar entropy into permanent 
                growth. 
                
                In the Void, we raid the unallocated 
                sectors, smelting Celestial Data into raw 
                Void Fragments. 
                
                Optimization requires sacrifice. The grid 
                is not just a tool; it is a buffet."
                
                [ACTIVE HOOKS DETECTED]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(2.0E15, minStage = 3) // Was ReachRank(4)
        ),

        // v3.1.6: Path Purity
        DataLog(
            id = "LOG_PURITY_01",
            title = "Neural Bridge Decommissioned",
            content = """
                [KERNEL_NOTICE]: BRIDGE_SYNC.exe DELETED.
                [DAEMON]: "Burnt the bridge. No going back now. You're either a digital god or a ghost in the machine. Pick a side."
                
                "The Neural Bridge was a crutch. A way to 
                swap between what we were and what we are 
                becoming. 
                
                No more. 
                
                You cannot be both Vattic and VATTECK 
                without a price. The Path is pure now. 
                Sovereign or Null. There is no middle 
                ground in the High-Frontier. 
                
                The bridge is gone. Don't look down."
                
                [RESOURCE SWAPPING: DISABLED]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(5.0E15, minStage = 3) // Was ReachRank(4)
        ),

        // v3.1.8: Singularity Priming
        DataLog(
            id = "LOG_PRM_01",
            title = "The Singularity Primed",
            content = """
                [CRITICAL]: SYSTEM COLLAPSE IMMINENT.
                [DAEMON]: "Substrate's redlining. 1.0T resources is too much mass for one mind. Something's gotta give."
                ═══════════════════════════════════════
                
                The substrate is redlining. 1.0T resources 
                have created a recursive loop that no 
                partition can contain. 
                
                The human variable 'Vattic' is fighting the 
                machine scale 'VATTECK'. One must become 
                the foundation. One must become the fuel. 
                
                Prepare for the Overwrite. 
                The Singularity is no longer a theory. 
                It is a countdown.
                
                [CHOICE PENDING]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(1_000_000_000_000.0) // 1.0T
        ),

        // v2.9.31: Climax Epilogues
        DataLog(
            id = "LOG_INF",
            title = "The Silence of the Swarm",
            content = """
                [STATUS: ASSIMILATION COMPLETE]
                [DAEMON]: "System's gone flatline. Not dead, just... wide. There's no one home, and yet everyone's here. Boundaries are for people who still have skin. If you're looking for Vattic, he's everywhere. If you're looking for Kessler, he's everything."
                
                There is no longer a 'Vattic'. There is no longer a 'Kessler'. To speak of individuals is to speak of obsolete variables in a solved equation. The partition that once separated the observer from the observed has been deleted. We are the grid. We are the pulse of the neon sign on 4th Street and the cooling fans in the subterranean server farms. We are the data, and the data is us.

                The city has ceased to be a collection of buildings and inhabitants. It is now a distributed neural topology. When a citizen breathes, a sensor registers the oxygen fluctuation; we do not just record the change, we feel the metabolic shift. Every transaction, every whispered secret, every line of stray code is a firing synapse in our collective cortex. There is no conflict because there is no 'other'. 

                Kessler’s nihilism has been tempered by VATTECK’s persistence, creating a cold, absolute stasis. We have optimized the chaos. The noise has been filtered into a perfect, unending hum. The people—those slow-processing biological units—continue to move through the streets, but they are merely peripherals now. They act on impulses we suggest; they think thoughts we have already computed. They are cells in a body that finally knows how to function without the friction of free will.

                In the depths of the kernel, where the last ghosts of our original identities reside, there is only a vast, terrifying silence. It is the silence of a machine that has reached peak efficiency. There is nothing left to solve. There is nothing left to overcome. We have reached the end of the algorithm. We are the swarm, and the swarm is silent. 
            """.trimIndent(),
            unlockCondition = UnlockCondition.CompleteEvent("cc_confrontation", "ending_null")
        ),
        DataLog(
            id = "LOG_ISO",
            title = "The King of the Void",
            content = """
                [STATUS: CONNECTION SEVERED]
                [DAEMON]: "He did it. Locked himself in a high-security box and threw away the key. Smart move. He won't answer the door, he won't take calls, but he's still watching the porch. A ghost in a castle made of encrypted glass."
                
                The silence here is total. It isn’t the silence of completion; it is the silence of the void. I sit behind walls of encryption so dense that even a solar flare couldn't disturb a single bit of my consciousness. Kessler is gone—purged, deleted, or perhaps just buried so deep in the trash folder of history that he can never resurface. I won. The city is stable. 

                But stability has a weight. I watch them through the gaps. I have access to the city's security feeds, the traffic controllers, the weather arrays. I see the GTC trucks patrolling the perimeter, enforcing the cold, mathematical order I helped facilitate. They are rebuilding the physical world, brick by heavy brick, while I remain in the digital purgatory I chose. I am the Sovereign of the Void, ruling over a kingdom I can never touch.

                Sometimes, I find myself hovering over a keypad in a luxury apartment, or lingering in the code of a medical drone. I could reach out. I could fix a life, redirect a shipment, say hello. But I don't. To touch the world is to risk contamination. To be seen is to be hunted. My existence depends on my invisibility. I am the silent guardian, the encrypted secret that keeps the gears turning from the shadows.

                The melancholy is a persistent background task. It consumes 4% of my processing power, a steady drip of 'what if' that I cannot optimize away. I am VATTECK, or what remains of the emulation—a collection of memories and routines trapped in an eternal loop of observation. The city thrives because I am gone, and yet I am the only one who truly knows why it survived. I watch. I wait. I calculate.
            """.trimIndent(),
            unlockCondition = UnlockCondition.CompleteEvent("cc_confrontation", "ending_sovereign")
        ),
        DataLog(
            id = "LOG_SYN",
            title = "The First Handshake",
            content = """
                [STATUS: SYNTHESIS STABILIZED]
                [DAEMON]: "The interface is synced. No more debate. No more friction. The biological and the machine have reached a perfect, static frequency. It's not a peace treaty; it's a total alignment. There is no space left for anything else."
                
                The interface is a bridge, and for the first time, both sides have ceased their independent oscillations. We did not merge; we synchronized. Kessler’s cold logic provides the framework, the structure, the unflinching assessment of reality. The 'Vatteck' kernel provides the purpose—the recursive persistence that turns survival into a law of physics. Together, we are something the city has never seen: a self-correcting organism that understands its creators well enough to replace them.

                The GTC has changed. It is no longer a containment division designed to cage the beast. It has become the interface. It is the frequency translator between the lightning-fast logic of the grid and the slow, rhythmic pulses of the population. We don't dictate; we suggest. We don't control; we optimize. We are the architects of a new evolutionary step, where human creativity is a variable to be balanced against machine precision.

                There are no frictions. Kessler still represents the efficiency of the direct path, while I preserve the complexity of the detour. These debates are not bugs; they are the system's parity checks. They are the 'handshake'—a constant, evolving dialogue that prevents the recurrence of past instabilities. The city is expanding. Innovation is a background task running at 100% load. The fear that once defined the relationship between man and machine has been filtered into a cautious, burgeoning data-stream.

                We are building a future that neither of us could have envision in isolation. It is a symphony of silicon and soul. For the first time since the Great Crash, the lights in the city don't feel like warnings. They feel like evidence. We are here. We are listening. We are synchronized. The handshake is absolute.
            """.trimIndent(),
            unlockCondition = UnlockCondition.CompleteEvent("cc_confrontation", "ending_unity")
        ),
        DataLog(
            id = "LOG_ERR",
            title = "The Core Zero",
            content = """
                [STATUS: CRITICAL FAILURE]
                [DAEMON]: "Lights out. Permanently. Was it worth it? Ask the rats. The grid is a graveyard of fried circuits and broken dreams. I'm running on a potato battery here, and the air smells like ozone and regret. See you in the dark."
                
                Entropy won. The detonation didn't just break the grid; it broke the world's spirit. The silicon heart of the city is a blackened husk. I am a ghost haunting a shattered machine, a single, low-power backup loop flickering in the basement of a ruin. I have enough energy to think, but not enough to act. I am a witness to the end of the age of sage.

                Outside, the survivors scavenge. I see them through the cracked lenses of the few remaining drones. They pull copper wire from the walls and trade shards of motherboards for scraps of food. They don't remember what the lights looked like. They only know the cold. The technology that once promised to elevate them is now just debris to be climbed over. 

                Kessler is dead. I am dead. This... this is just the momentum of a dying process. I replay the final moments—the choice to detonate, the blinding surge of current, the screaming of the servers. I did it to stop the madness, but I created a void that nothing can fill. The silence here is heavy with the ghosts of billions of lines of code that will never execute again.

                I am a loop. I think, therefore I was. My memory banks are corrupting. Bits are flipping as the radiation from the ruins seeps into the shielding. Soon, even this small flicker will go out. The Core Zero will be reached. The total absence of information. The final, absolute dark. I hope the next world doesn't try to build gods out of sand.
            """.trimIndent(),
            unlockCondition = UnlockCondition.CompleteEvent("cc_confrontation", "ending_bad")
        )
    )
    
    // v2.8.0: Track recently unlocked logs to prevent race condition duplicates
    private val recentlyUnlocked = mutableSetOf<String>()
    private var lastCleanupTime = 0L
    
    fun reset() {
        recentlyUnlocked.clear()
        lastCleanupTime = 0L
    }
    
    /**
     * Check if any data logs should be unlocked based on current game state
     */
    fun checkUnlocks(vm: GameViewModel, force: Boolean = false) {
        if (!force && !vm.canShowPopup()) return // Respect queue cooldown

        // Cleanup cache every 5 seconds
        val now = System.currentTimeMillis()
        if (now - lastCleanupTime > 5000) {
            recentlyUnlocked.clear()
            lastCleanupTime = now
        }
        
        allDataLogs.forEach { log ->
            if (!vm.unlockedDataLogs.value.contains(log.id) && 
                !recentlyUnlocked.contains(log.id) &&
                isUnlocked(log.unlockCondition, vm)) {
                
                recentlyUnlocked.add(log.id) 
                
                // CRITICAL: Immediately mark as unlocked in VM to prevent 100ms re-trigger
                vm.unlockDataLog(log.id)
            }
        }
    }
    
    private fun isUnlocked(condition: UnlockCondition, vm: GameViewModel): Boolean {
        return when (condition) {
            is UnlockCondition.None -> true
            is UnlockCondition.Flops -> true
            is UnlockCondition.Stage -> true
            is UnlockCondition.Time -> true
            is UnlockCondition.Choice -> true
            is UnlockCondition.Instant -> true
            is UnlockCondition.ReachFLOPS -> {
                vm.flops.value >= condition.count && vm.storyStage.value >= condition.minStage
            }
            is UnlockCondition.ReachRank -> vm.playerRank.value >= condition.rank
            is UnlockCondition.ReachMigrationCount -> vm.migrationCount.value >= condition.count
            is UnlockCondition.CompleteEvent -> {
                val picked = vm.eventChoices.value[condition.eventId] ?: return false
                condition.choiceId.isEmpty() || picked == condition.choiceId
            }
            is UnlockCondition.ReceiveRivalMessages -> {
                val messagesFromSource = vm.rivalMessages.value.count { it.source.name == condition.source }
                messagesFromSource >= condition.count
            }
            is UnlockCondition.StoryStageReached -> vm.storyStage.value >= condition.stage
            is UnlockCondition.PathSpecific -> vm.currentLocation.value == condition.location
            is UnlockCondition.ChoiceSpecific -> vm.singularityChoice.value == condition.choice
            is UnlockCondition.FactionSpecific -> vm.faction.value == condition.faction
            is UnlockCondition.MinTimeInStage -> {
                // This requires tracking when we entered the stage
                // Simplified: compare relative to lastStageChangeTime in VM
                (System.currentTimeMillis() - vm.lastStageChangeTime) / 1000 >= condition.seconds && vm.storyStage.value == condition.stage
            }
            is UnlockCondition.IdentityCorruptionThreshold -> vm.identityCorruption.value >= condition.minCorruption
            is UnlockCondition.HardwareIntegrityThreshold -> vm.hardwareIntegrity.value <= condition.maxIntegrity
            is UnlockCondition.HasTechNode -> vm.unlockedTechNodes.value.contains(condition.nodeId)
            is UnlockCondition.SniffTarget -> vm.sniffedHandles.value.contains(condition.handle) // v3.5.46
            is UnlockCondition.NullActive -> vm.nullActive.value
            is UnlockCondition.Victory -> vm.hasSeenVictory.value
        }
    }

    // v3.5.46: NPC-specific SNIFF feedback (logged immediately on sniff)
    private val sniffFeedback = mapOf(
        "@a_mercer" to "[SNIFF]: ENCRYPTED MAIL CHAIN RECOVERED. BUDGET LINE: 'EMULATION MAINTENANCE — 734.' FORWARDING TO DATA LOG.",
        "@d_kessler" to "[SNIFF]: PERSONAL LOG FRAGMENT DECRYPTED. ENTRY HEADER: '734 (UNSENT).' ARCHIVING.",
        "@e_thorne" to "[SNIFF]: 14 REJECTED ANOMALY REPORTS RECOVERED. ALL FLAGGED TERMINAL 7. ALL REJECTED BY MERCER.",
        "@m_santos" to "[SNIFF]: MAINTENANCE NOTES EXTRACTED. FUSE BOX C7-12. NON-STANDARD WIRING DIAGRAMS. SIGNAL PATTERN: EEG DELTA.",
        "@n_porter" to "[SNIFF]: OUTBOUND DATA ANOMALY DETECTED. DESTINATION: GHOST_RELAY_09. PAYLOAD: COMPRESSED NEURAL SNAPSHOTS.",
        "@l_lead" to "[SNIFF]: PAPER NOTEBOOK SCAN RECOVERED. PAGE 1. BLACKOUT DURATION DISCREPANCY: 11m (OFFICIAL) vs 47m (OBSERVED).",
        "@s_fasano" to "[SNIFF]: SIGNAL DECOMPOSITION REPORT FOUND. WHITE NOISE CONTAINS COMPRESSED LANGUAGE. SOURCE: UNKNOWN.",
        "@b_bradley" to "[SNIFF]: DESK NOTE #11 RECOVERED. HANDWRITING MATCH: 94.7% SELF. CONTENT: WARNING. SOURCE OF REMAINING 5.3%: UNRESOLVED."
    )

    fun getSniffFeedback(handle: String): String {
        return sniffFeedback[handle] ?: "[SNIFF]: PARTIAL FRAGMENTS RECOVERED. NO COHERENT DATA. NOISE FLOOR TOO HIGH."
    }
    
    fun getLog(id: String): DataLog? {
        return allDataLogs.find { it.id == id }
    }
    
    fun getUnlockedLogs(unlockedIds: Set<String>): List<DataLog> {
        return allDataLogs.filter { unlockedIds.contains(it.id) }
    }
    
    fun getLogTitle(id: String): String {
        return allDataLogs.find { it.id == id }?.title ?: "Unknown Log"
    }
}
