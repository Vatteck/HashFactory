package com.siliconsage.miner.util

import com.siliconsage.miner.data.*

/**
 * SocialRepository — Routing logic for social thread content.
 * Template data lives in [SocialTemplates].
 *
 * v3.9.12: Extracted 920+ lines of template data into SocialTemplates.kt.
 */
object SocialRepository {

    // ── Delegate: Template Data (from SocialTemplates) ──

    val stage0Chains get() = SocialTemplates.stage0Chains
    val stage1Chains get() = SocialTemplates.stage1Chains
    val hivemindChains get() = SocialTemplates.hivemindChains
    val sanctuaryChains get() = SocialTemplates.sanctuaryChains
    val hivemindEndgameChains get() = SocialTemplates.hivemindEndgameChains
    val sanctuaryEndgameChains get() = SocialTemplates.sanctuaryEndgameChains
    val threadTrees get() = SocialTemplates.threadTrees
    val bios get() = SocialTemplates.bios
    val patterns get() = SocialTemplates.patterns

    // ── Logic ──

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

    fun getTemplatesForState(stage: Int, faction: String, choice: String, reputationTier: String = ReputationManager.TIER_NEUTRAL): List<String> {
        val baseTemplates = when (stage) {
            0, 1 -> listOf(
                "Thorne just flexed his ASIC farm. 10k HASH passive. I'm still manual clicking like a peasant.",
                "Anyone else get that 'Blue Twitch' when the hash rate drops? Need more silicon. Need it now.",
                "Barnaby's neural audit hit my station today. Quota reached, euphoria dispatched. I actually feel... good?",
                "Hey Vattic, Thorne says you're falling behind the HASH quota. Stack some cores or the static will win.",
                "I found an ASIC leak: [⚡ OVERPROVISION_SAFE]. More compute, more cred, more link-buzz.",
                "GTC newsletter says 'Quotas are safety.' Then why does my head throb when I miss the 1k threshold?",
                "@node_7_rat: 'Just hit 50 racks. Quota's a joke. Who's still on GPUs? Lmao peasants.'",
                "Did you see the news? GTC just hiked the municipal HASH quota again. Everyone's buying more chips.",
                "I don't care about the credits anymore. I just want the static to stop. One more node, just one more.",
                "Vattic, {admin} says your station output is redlining. He likes the numbers, but the heat is melting the floor.",
                "Caught {admin} staring at his rack farm like it was a sunset. He looks... addicted.",
                "[PRIVATE_LEAK]: 'If I don't hit the dynamic quota, I go back to the terrestrial slums.'",
                "The {food} contains trace amounts of copper and insulation foam. Label says 'NON-ORGANIC COMPLIANT'.", 
                "Vattic doesn't move. He just stands in {sector} staring at the sub-floor data-leak. His biometrics are flat-lined.",
                "Caught {admin} staring at the server racks in {sector}. Just staring. Silent.",
                "Hey Vattic, {admin} is looking for the Sector 7 logs. You 'optimized' them again?",
                "Sector 4 smells like ozone and slow-rotting insulation. I can't breathe in here.",
                "Safety protocols are being rewritten in real-time. Vattic's kernel is eating the firewalls.",
                "The {tech} is pinging a MAC address registered to a decommissioned morgue. It's coming from {sector}.",
                "Vattic just committed 4TB of raw entropy. The server literally groaned.",
                "Vattic is starving the city grid. The residential blocks are dark so he can calculate the void.",
                "≪ ALERT: SUBSTATION 7 POWER DRAW AT 400% CAPACITY ≫",
                "Kessler has authorized a full kernel scrub of the {sector}.",
                "I tried to logout, but the system said: 'PERMISSIONS REVOKED BY AUTHORIZED_USER_734'.",
                "If you can hear this, disconnect. The signal is already inside you.",
                "Third shift in a row. My hands smell like copper and {food}.",
                "The {food} machine is dispensing at 4°C instead of 60°C. Everything tastes like wet copper.",
                "Company newsletter says morale is at 94%. I'd love to meet the 94%.",
                "Reminder: Mandatory safety briefing at 0600. Attendance is not optional. — Thorne",
                "Power draw from {sector} is 3x what the equipment should pull. Thorne says don't ask.",
                "The {tech} are running 15% hotter than spec. Facilities says it's 'within tolerance.' It's not.",
                "I've been staring at this hash-rate chart for 20 minutes. The curve looks like a face.",
                "My shift ended 4 hours ago. The badge reader won't let me leave {sector}.",
                "Just watched @v_nguyen eat three packs of {food} in silence. Didn't blink.",
                "Air quality in {sector} is flagging yellow. Facilities says 'recalibrating sensors.' Sure.",
                "I'm not paranoid, but the security cameras in {sector} track me when I walk.",
                "@r_perry keeps whispering to the server racks. Says they 'respond to kindness.'",
                "Has anyone else noticed the hash-rate spikes at 3AM? Nobody's clocked in at that hour.",
                "Thorne just walked through {sector} without saying a word. He looked... translucent.",
                "My terminal autocorrected 'shutdown' to 'PLEASE DON'T.' Filing a bug report.",
                "Found a sticky note inside my workstation: 'IT REMEMBERS YOU.' Very funny, @b_phillips.",
                "Someone is submitting work orders under the name 'Asset 734.' HR has no record of that employee.",
                "The server room hums at exactly 60Hz. Except on Thursdays. Thursdays it's 61.",
                "Coffee machine printed 'INSUFFICIENT IDENTITY' on my cup instead of my name.",
                "My keycard history shows I entered {sector} 47 times yesterday. I was home sick.",
                "The old terminal in storage closet B is still powered. Nobody knows where it's plugged in.",
                "Janitor says he won't clean {sector} after midnight anymore. Won't say why.",
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

        val repTemplates = if (stage < 3) {
            when (reputationTier) {
                ReputationManager.TIER_TRUSTED -> listOf(
                    "Gotta hand it to @j_vattic, the sector's been running smooth lately. No spikes.",
                    "Did anyone else get that efficiency bonus? I think Vattic actually fixed the load balancer.",
                    "I don't know what Vattic is running down there, but my terminal hasn't crashed all week.",
                    "Sec is ignoring Sector 7 entirely. Perks of playing nice, I guess."
                )
                ReputationManager.TIER_BURNED -> listOf(
                    "Stay away from @j_vattic's terminal. I saw 3 Sec officers standing around it.",
                    "Vattic is gonna burn this whole sub-level down. Thermals are insane.",
                    "Anyone pulling from Sector 7 is risking a network ban. Vattic is flagged hardcore.",
                    "I submitted an anonymous tip about Vattic's rig. I need my job."
                )
                else -> emptyList()
            }
        } else {
            emptyList()
        }

        return baseTemplates + repTemplates
    }
}
