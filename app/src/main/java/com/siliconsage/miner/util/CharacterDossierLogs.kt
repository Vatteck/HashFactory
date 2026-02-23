package com.siliconsage.miner.util

import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.UnlockCondition

/**
 * CharacterDossierLogs — Sniff-target character files (Mercer, Kessler, Thorne, Santos, Porter, Lead, Fasano, Bradley).
 */
object CharacterDossierLogs {
    val logs = listOf(
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
                
                Not the sentience — the name. You can't delete "John." The moment I typed "Vattic" into the persona template, I created something with a history. A childhood I fabricated from census data. A mother who never existed. A preference for black coffee that I copied from my own morning routine.
                
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
        )
    )
}
