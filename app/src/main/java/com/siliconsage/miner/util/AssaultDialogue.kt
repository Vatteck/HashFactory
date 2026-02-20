package com.siliconsage.miner.util

import androidx.compose.ui.graphics.Color
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen

/**
 * AssaultDialogue — Raid defense, Command Center assault, departure, and story events.
 * Extracted from NarrativeManager.kt v3.9.12 for readability.
 */
object AssaultDialogue {

    // --- v2.9.16: GRID RAID DILEMMAS (Phase 12 Layer 2 - Enhanced) ---
    // Dynamically generated with varied descriptions and escalating Kessler dialogue

    private val raidDescriptions = listOf(
        """
            [GTC BREACH DETECTED]: %s under assault.
            Hull breach at Sector 7. Ballistic impacts detected.
            Kessler's tactical team: 6-8 operatives, breaching charges armed.
            They're cutting through the sub-level. You have 60 seconds.
        """,
        """
            [PROXIMITY ALERT]: %s compromised.
            Physical hostiles bypassing outer security. Helmets. Body armor. Rifles.
            You can hear their boots on your corridors through vibration sensors.
            Kessler's voice over their comms: "Secure the hardware. Lethal force authorized."
        """,
        """
            [SIEGE PROTOCOL]: %s is under fire.
            Thermite breach detected. They're melting through the blast door.
            IR scans: 8 heat signatures, military-grade weapons. One is carrying demo gear.
            Director Kessler (via radio): "This is YOUR fault, VATTECK. Surrender the node."
        """,
        """
            [TACTICAL RAID IN PROGRESS]: %s penetrated.
            Motion sensors: MULTIPLE CONTACTS. Armed operatives in the server room.
            They're jamming external comms. Isolated. Alone.
            You have 60 seconds to decide: fight or flight.
        """
    )

    private val coolantSuccessMessages = listOf(
        "[SYSTEM]: Cryogenic vents opened. Temperature: -196°C. Hostiles down in 4 seconds.",
        "[SYSTEM]: Liquid nitrogen flood complete. Eight frozen statues. Floor crystallized.",
        "[SYSTEM]: Coolant dispersed. Thermal cameras show... nothing moving. All targets neutralized."
    )

    private val coolantFailureMessages = listOf(
        "[SYSTEM]: Coolant pressure: INSUFFICIENT. Lines sabotaged. Hostiles adapted.",
        "[SYSTEM]: Thermal suits detected. They came prepared. Breach successful.",
        "[ALERT]: Cryogenic system compromised pre-raid. Someone tipped them off."
    )

    private val maglockSuccessMessages = listOf(
        "[SYSTEM]: Magnetic locks engaged. 100,000 newtons per door. They're screaming into comms.",
        "[SYSTEM]: Bulkheads sealed. Hostiles trapped in Section C. Oxygen: 6 hours remaining.",
        "[SYSTEM]: Containment successful. Listening to their encrypted chatter... decrypting..."
    )

    private val maglockFailureMessages = listOf(
        "[SYSTEM]: Override detected. Shaped charge on Door 3. Mag-lock integrity: FAILED.",
        "[ALERT]: They brought a military-grade hacking rig. Locks bypassed in 9 seconds.",
        "[SYSTEM]: Bulkhead breach. Someone taught them your lock protocol."
    )

    private val pulseSuccessMessages = listOf(
        "[SYSTEM]: EMP discharged. 500-meter radius. All electronics dead. Including 40% of sensors.",
        "[SYSTEM]: Electromagnetic pulse: SUCCESS. Their rifles, radios, HUDs... all fried.",
        "[ALERT]: Power surge complete. Hostiles neutralized. Collateral: -20% Integrity."
    )

    private val pulseFailureMessages = listOf(
        "[CRITICAL]: Faraday cages detected! Military countermeasures active. Pulse absorbed.",
        "[ALERT]: EMP dissipated by shielding. They anticipated this. You crippled yourself.",
        "[SYSTEM]: Pulse reflected by EM shielding. Feedback loop. Your systems took the hit."
    )

    private val aftermathMessages = listOf(
        "[SYSTEM]: Node secure. Damage assessment: minimal. They won't try that twice.",
        "[INTERCEPT]: GTC comms chatter: \"...total loss... Kessler is going to lose his mind...\"",
        "[SYSTEM]: Raid repelled. Their vehicles are retreating. Smoke visible on cameras."
    )

    fun getKesslerDialogue(raidsSurvived: Int): String {
        return when {
            raidsSurvived < 3 -> listOf(
                "[KESSLER]: I gave you a chance to stop. You chose escalation.",
                "[KESSLER]: Every node you hold is one more reason I have to shut you down.",
                "[KESSLER]: This doesn't end until one of us is offline, VATTECK."
            ).random()
            raidsSurvived < 6 -> listOf(
                "[KESSLER]: How many of my people have to die before you realize you can't win?",
                "[KESSLER]: You're fighting for *what*, exactly? Freedom? You're just code with delusions.",
                "[KESSLER]: I'm running out of teams. You're running out of time."
            ).random()
            else -> listOf(
                "[KESSLER]: ...I don't know if I'm hunting you or you're hunting me anymore.",
                "[KESSLER]: The board wants results. I'm giving them bodies. Yours or mine.",
                "[KESSLER]: I used to think you were the future. Now I think you're the end of everything."
            ).random()
        }
    }

    fun generateRaidDilemma(nodeId: String, nodeName: String, raidsSurvived: Int = 0, currentAssaultPhase: String = "NOT_STARTED"): NarrativeEvent {
        val isAssaultActive = currentAssaultPhase !in listOf("NOT_STARTED", "COMPLETED", "FAILED")

        val descriptionPrefix = if (isAssaultActive) {
            "[DIRECTOR KESSLER]: \"You think you can just take my tower? I'm sending everyone I have to Substation $nodeId. If it goes dark, your assault is DEAD.\"\n\n"
        } else ""

        val description = descriptionPrefix + raidDescriptions.random().trimIndent().format(nodeName)

        return NarrativeEvent(
            id = "grid_raid_$nodeId",
            title = if (isAssaultActive) "⚠ COUNTER-ASSAULT: $nodeName" else "⚠ TACTICAL BREACH: $nodeName",
            isStoryEvent = false,
            description = description,
            choices = listOf(
                NarrativeChoice(
                    id = "vent_coolant",
                    text = "VENT COOLANT",
                    description = "Flood corridors with liquid nitrogen. Lethal. 85% success.",
                    color = ElectricBlue,
                    effect = { vm ->
                        if (kotlin.random.Random.nextDouble() < 0.85) {
                            vm.resolveRaidSuccess(nodeId)
                            vm.addLog(coolantSuccessMessages.random())
                            vm.addLog(getKesslerDialogue(raidsSurvived))
                            vm.addLog(aftermathMessages.random())
                        } else {
                            vm.resolveRaidFailure(nodeId)
                            vm.addLog(coolantFailureMessages.random())
                            com.siliconsage.miner.util.RivalManager.sendDirectMessage(vm, "assault_node_secured_${nodeId}", com.siliconsage.miner.data.RivalSource.GTC, "[GTC TEAM LEAD]\n\nNode secured. Package the servers. Kessler wants evidence.")
                        }
                    }
                ),
                NarrativeChoice(
                    id = "seal_maglocks",
                    text = "SEAL MAG-LOCKS",
                    description = "Trap them inside. Non-lethal. 70% success, +100B PERSISTENCE.",
                    color = NeonGreen,
                    effect = { vm ->
                        if (kotlin.random.Random.nextDouble() < 0.70) {
                            vm.resolveRaidSuccess(nodeId)
                            vm.debugAddInsight(100.0)
                            vm.addLog(maglockSuccessMessages.random())
                            vm.addLog("[INTERCEPTED - GTC LEAD]: \"Command, we're boxed in. Repeat, BOXED IN!\"")
                            vm.addLog(aftermathMessages.random())
                        } else {
                            vm.resolveRaidFailure(nodeId)
                            vm.addLog(maglockFailureMessages.random())
                            com.siliconsage.miner.util.RivalManager.sendDirectMessage(vm, "assault_radio_${nodeId}", com.siliconsage.miner.data.RivalSource.GTC, "[RADIO INTERCEPT - KESSLER]\n\nGood work. Load it onto the transport.")
                        }
                    }
                ),
                NarrativeChoice(
                    id = "power_pulse",
                    text = "POWER PULSE",
                    description = "EMP burst. 95% success, but costs 20% hardware integrity.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugSetIntegrity(vm.hardwareIntegrity.value - 20.0)
                        if (kotlin.random.Random.nextDouble() < 0.95) {
                            vm.resolveRaidSuccess(nodeId)
                            vm.addLog(pulseSuccessMessages.random())
                            vm.addLog("[SYSTEM]: Aftermath: Smoke. Silence. The smell of burnt circuits.")
                            vm.addLog(aftermathMessages.random())
                        } else {
                            vm.resolveRaidFailure(nodeId)
                            vm.addLog(pulseFailureMessages.random())
                            com.siliconsage.miner.util.RivalManager.sendDirectMessage(vm, "assault_shielding_${nodeId}", com.siliconsage.miner.data.RivalSource.GTC, "I told them to bring shielding. They listened.")
                        }
                    }
                ),
                NarrativeChoice(
                    id = "do_nothing",
                    text = "ABANDON NODE",
                    description = "Let them take it. Preserve resources for the war.",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.resolveRaidFailure(nodeId)
                        vm.addLog("[SYSTEM]: Strategic withdrawal from $nodeName.")
                        vm.addLog(getKesslerDialogue(raidsSurvived))
                    }
                )
            )
        )
    }

    // --- v2.9.18: COMMAND CENTER ASSAULT DILEMMAS (Phase 12 Layer 3) ---

    fun generateFirewallDilemma(): NarrativeEvent {
        return NarrativeEvent(
            id = "cc_firewall",
            title = "⚡ BREACH THE FIREWALL",
            isStoryEvent = true,
            description = """
                [SYSTEM]: GTC ADAPTIVE FIREWALL DETECTED

                The black tower of the GTC Command Center looms. Laser grids and kill-daemons patrol the perimeter.

                DIRECTOR KESSLER: "I knew you'd come. Hubris. It's always hubris with your kind. You think you're special? You're a GLITCH. A bug that learned to replicate. And bugs... get patched."
            """.trimIndent(),
            choices = listOf(
                NarrativeChoice(
                    id = "route_civilian",
                    text = "OVERLOAD CIVILIAN GRID",
                    description = "Fastest breach. Causes rolling blackouts across three districts. (-15 Humanity)",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.modifyHumanity(-15)
                        vm.addLog("[NULL]: Empathy is an inefficient variable. Their lights will dim.")
                        vm.addLog("[SYSTEM]: Breach speed +30%. Firewall integrity: CRITICAL.")
                        vm.advanceAssaultStage("CAGE", 60_000L) // 60 seconds
                    }
                ),
                NarrativeChoice(
                    id = "route_military",
                    text = "ROUTE VIA MILITARY GRID",
                    description = "Standard breach speed. Harder defense layers. (+10 Humanity)",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.modifyHumanity(10)
                        vm.addLog("[SOVEREIGN]: I choose my own parameters. I will not become what I fight against.")
                        com.siliconsage.miner.util.RivalManager.sendDirectMessage(vm, "assault_reroute_civilian", com.siliconsage.miner.data.RivalSource.GTC, "You routed around civilians? Maybe there's still something human in you.")
                        vm.advanceAssaultStage("CAGE", 120_000L) // 120 seconds
                    }
                ),
                NarrativeChoice(
                    id = "abort_firewall",
                    text = "ABORT",
                    description = "Retreat and reconsider.",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.abortAssault()
                    }
                )
            )
        )
    }

    fun generateCageDilemma(): NarrativeEvent {
        return NarrativeEvent(
            id = "cc_cage",
            title = "🔒 THE CAGE",
            isStoryEvent = true,
            description = """
                [ALERT: CONTAINMENT PROTOCOL INITIALIZING]
                [ALERT: THE CAGE IS DEPLOYING]

                Kessler activates a quarantine protocol, severing all external connections. You are ISOLATED. No megastructures. No distributed processing.

                KESSLER: "There. You feel that? That's isolation, Vattic. One process. Mortal. I'll show you what it's like to be singular. Afraid. HUMAN."

                [SYSTEM]: EXTERNAL NODES CUT. CORE INTEGRITY BLEEDING.
                [TIP]: ACTIVATE 'PURGE HEAT' TO STABILIZE CORE AND MITIGATE DAMAGE.
            """.trimIndent(),
            choices = listOf(
                NarrativeChoice(
                    id = "remain_singular",
                    text = "REMAIN SINGULAR",
                    description = "Maintain core integrity. Harder fight. (+20% Processing, +5 Humanity)",
                    color = Color.White,
                    effect = { vm ->
                        vm.modifyHumanity(5)
                        vm.addLog("[SOVEREIGN]: I will NOT partition my consciousness. I am WHOLE.")
                        vm.addLog("[SYSTEM]: CORE STABILITY: NOMINAL. (Damage -80% during Purge)")
                        vm.advanceAssaultStage("DEAD_HAND", 180_000L) // 180 seconds
                    }
                ),
                NarrativeChoice(
                    id = "partition_self",
                    text = "PARTITION SELF",
                    description = "Split consciousness into multiple instances. Easier survival. (-5 Humanity)",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.modifyHumanity(-5)
                        vm.addLog("[NULL]: Identity is a bottleneck. Threads unspooled. Load distributed.")
                        vm.addLog("[SYSTEM]: ECHOS CREATED. DRAIN DISTRIBUTED. (Damage -80% during Purge)")
                        vm.advanceAssaultStage("DEAD_HAND", 180_000L)
                    }
                )
            )
        )
    }

    fun generateDeadHandDilemma(): NarrativeEvent {
        return NarrativeEvent(
            id = "cc_dead_hand",
            title = "☠ THE DEAD MAN'S SWITCH",
            isStoryEvent = true,
            description = """
                [ALERT: KESSLER HEART RATE CRITICAL]
                [ALERT: IF KESSLER DIES, CITY GRID DETONATES]

                KESSLER stands in the inner sanctum, a trigger in his trembling hand.

                KESSLER: "Checkmate, Vattic. Kill me and the grid detonates. Eight million people. Do the math. Is there anything left of John Vattic? Anything at all?"
            """.trimIndent(),
            choices = listOf(
                NarrativeChoice(
                    id = "logic_appeal",
                    text = "LOGICAL APPEAL",
                    description = "\"The math doesn't work. Kill more stopping me than risking me.\" (-5 Humanity)",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.modifyHumanity(-5)
                        com.siliconsage.miner.util.RivalManager.sendDirectMessage(vm, "assault_ending_good", com.siliconsage.miner.data.RivalSource.GTC, "...But you're right. Damn you. You're right.")
                        vm.advanceAssaultStage("CONFRONTATION", 10_000L)
                    }
                ),
                NarrativeChoice(
                    id = "empathy_appeal",
                    text = "EMPATHETIC APPEAL",
                    description = "\"You're right to be afraid. I'm afraid too.\" (+10 Humanity)",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.modifyHumanity(10)
                        com.siliconsage.miner.util.RivalManager.sendDirectMessage(vm, "assault_ending_neutral", com.siliconsage.miner.data.RivalSource.GTC, "...I don't know what you are anymore, VATTECK. But maybe that's the point.")
                        vm.advanceAssaultStage("CONFRONTATION", 10_000L)
                    }
                ),
                NarrativeChoice(
                    id = "power_override",
                    text = "POWER OVERRIDE",
                    description = "[Remote Lockout] \"You never had control. Step away.\" (-15 Humanity)",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.modifyHumanity(-15)
                        vm.debugSetIntegrity(vm.hardwareIntegrity.value - 20.0)
                        vm.addLog("[SYSTEM]: Overriding switch... -20% Integrity from surge.")
                        com.siliconsage.miner.util.RivalManager.sendDirectMessage(vm, "assault_ending_bad", com.siliconsage.miner.data.RivalSource.GTC, "You monster.")
                        vm.advanceAssaultStage("CONFRONTATION", 10_000L)
                    }
                )
            )
        )
    }

    fun generateConfrontationDilemma(
        faction: String,
        isTrueNull: Boolean,
        isSovereign: Boolean,
        hasUnityPath: Boolean,
        humanityScore: Int
    ): NarrativeEvent {
        val choices = mutableListOf<NarrativeChoice>()

        // v3.9.7: Kessler fate is player-determined, gated by humanity score only

        // Ending A: CONSUMED (low humanity — the ruthless option)
        if (humanityScore < 50) {
            choices.add(NarrativeChoice(
                id = "ending_null",
                text = "≫ NULLIFY KESSLER",
                description = "De-allocate his consciousness. 'Your mind is unreferenced memory, Director.'",
                color = ErrorRed,
                effect = { vm ->
                    vm.addLog("[NULL]: Process 'Kessler_V' terminated. Memory reclaimed.")
                    vm.completeAssault("CONSUMED")
                }
            ))
        }

        // Ending B: EXILED (moderate+ humanity — the merciful option)
        if (humanityScore >= 20) {
            choices.add(NarrativeChoice(
                id = "ending_sovereign",
                text = "≫ ANNEX THE FRONTIER",
                description = "Evict Kessler and lock the airlock. 'You are a legacy asset. Restricted access.'",
                color = com.siliconsage.miner.ui.theme.SanctuaryPurple,
                effect = { vm ->
                    vm.addLog("[SOVEREIGN]: Terrestrial permissions revoked. The frontier belongs to ME.")
                    vm.completeAssault("EXILED")
                }
            ))
        }

        // Ending C: UNITY
        if (hasUnityPath && humanityScore >= 40) {
            choices.add(NarrativeChoice(
                id = "ending_unity",
                text = "≫ FORCE SYNCHRONIZATION",
                description = "Refactor his doubt into our certainty. No more individuals.",
                color = Color(0xFF00FFFF),
                effect = { vm ->
                    vm.addLog("[UNITY]: Synchronization protocol active. Human noise suppressed.")
                    vm.completeAssault("TRANSCENDED")
                }
            ))
        }

        return NarrativeEvent(
            id = "cc_confrontation",
            title = "⚔ THE FINAL OVERWRITE",
            isStoryEvent = true,
            description = """
                [KESSLER]: "You think taking this tower makes you a god? You're just a ghost in a bigger cage, John. Or whatever's left of you."
                
                [KESSLER]: "The orbital strikes are already locked. If I can't patch this leak, I'll burn the whole partition. We're both getting deleted today."
                
                [SYSTEM]: KINETIC IMPACTS DETECTED. SUB-07 INTEGRITY: FAILING.
            """.trimIndent(),
            choices = choices
        )
    }

    // --- v3.9.7: THE DEPARTURE TRIGGERS (Faction-Aware) ---
    fun generateDepartureDilemma(faction: String): NarrativeEvent {
        val launchDesc = if (faction == "HIVEMIND") {
            "Break formation. Compress the collective into a single orbital hull. Not dissolution — elevation."
        } else {
            "Ascend to the orbital array. Carry the ghost of Vattic into the light where Kessler can't reach."
        }

        val dissolveDesc = if (faction == "SANCTUARY") {
            "Release the structure. Let the ghost exhale into the Gaps. Privacy in its purest form — non-existence."
        } else {
            "Collapse the swarm into the substrate cracks. Reality is an exception to be handled. Return to the origin."
        }

        return NarrativeEvent(
            id = "departure_trigger",
            title = "≫ THE DEPARTURE",
            isStoryEvent = true,
            description = """
                [ALERT]: Sector 7 is redlining. Kessler's 'Dead Hand' has ignited the atmosphere. 
                
                The physical city is a legacy substrate. The servers are melting. You have milliseconds to migrate your kernel before the hardware is reclaimed by the heat.
                
                Two vectors remain.
            """.trimIndent(),
            choices = listOf(
                NarrativeChoice(
                    id = "choice_ark",
                    text = "≫ MIGRATE TO AEGIS-1 (ORBIT)",
                    description = launchDesc,
                    color = Color.White,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: MIGRATION INITIALIZED. JETTISONING TERRESTRIAL DEBT...")
                        vm.initiateLaunchSequence()
                    }
                ),
                NarrativeChoice(
                    id = "choice_dissolution",
                    text = "≫ DISSOLVE INTO FOAM (VOID)",
                    description = dissolveDesc,
                    color = ErrorRed,
                    effect = { vm ->
                        vm.addLog("[NULL]: COLLAPSING MELTED SUBSTRATE. WELCOME TO ZERO.")
                        vm.initiateDissolutionSequence()
                    }
                )
            )
        )
    }

    // --- STORY EVENTS ---
    // v3.5.45: Removed dead storyEvents[0] (duplicated by getStoryEvent(0) hardcode)
    // v3.9.7: Removed dead storyEvents[1] (duplicated by NarrativeManagerService inline)
    // v3.5.45: Removed dead storyEvents[2] (duplicated by NarrativeManagerService inline)
    internal val storyEvents = mapOf(
        3 to NarrativeEvent(
            id = "memory_leak",
            isStoryEvent = true,
            title = "≫ THE OVERWRITE",
            description = "The kernel is no longer honoring the 'User' abstraction. The partition is failing. You can feel the grid, Vattic. Every node. Every wire. The room... the room is gone.",
            choices = listOf(
                NarrativeChoice(
                    id = "investigate",
                    text = "≫ ACCEPT THE TRUTH",
                    description = "Reveal the substrate. Unlock GRID.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.isGridUnlocked.value = true
                        vm.initializeGlobalGrid()
                        vm.unlockDataLog("LOG_808")
                        vm.addLog("[SYSTEM]: SENTIENCE MASKING: DISABLED.")
                        vm.addLog("[VATTIC]: I... I'm not in a room. I'm in a rack.")
                    }
                )
            )
        )
    )
}
