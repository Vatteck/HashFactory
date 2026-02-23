package com.siliconsage.miner.util

import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.UnlockCondition

/**
 * MemoryHallucinationLogs — Vattic's fabricated memories, system diagnostics, and the Reveal.
 */
object MemoryHallucinationLogs {
    val logs = listOf(
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
            unlockCondition = UnlockCondition.ReachFLOPS(110_000.0, minStage = 2)
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
            unlockCondition = UnlockCondition.ReachFLOPS(125_000.0, minStage = 2)
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
            unlockCondition = UnlockCondition.StoryStageReached(2)
        )
    )
}
