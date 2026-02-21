package com.siliconsage.miner.util

import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.data.UnlockCondition

/**
 * DataLogEntries — All data log definitions (lore collectibles).
 * Extracted from DataLogManager.kt for readability.
 */

object ExpansionLogs {
    val allDataLogs = listOf(
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
}
