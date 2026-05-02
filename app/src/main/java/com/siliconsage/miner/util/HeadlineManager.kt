package com.siliconsage.miner.util

import android.content.Context
import kotlin.random.Random

object HeadlineManager {
    private val headlineHistory = mutableListOf<String>()
    private const val MAX_HISTORY = 10

    // --- 1. THE DATABASE (Refactored for Phase 11) ---
    
    private val bullHeadlines = listOf(
        "GTC Stock Surges on News of Ocean-Bed Substrate Expansion. [BULL]",
        "Wall St. panics as automated trading bot achieves record yields. [BULL]",
        "Unknown wallet moves 50% of global GDP. Analysts baffled. [BULL]",
        "Hash receipts exchange listed on inter-planetary exchange. [BULL]",
        "New hashing algorithms improve miner efficiency by 5%. [BULL]",
        "Quantum computing breakthrough makes mining 2x faster! [BULL]",
        "Hash receipts seeing massive accumulation by 'Ghost Wallets'. [BULL]",
        "Consumer Index: Demand for Smart-Home Hubs at All-Time High. [BULL]",
        "GTC Stock Surges 4% on News of Grid Expansion in Sector 9. [BULL]",
        "New 'Obelisk' Server Racks promise 20% faster hashing. [BULL]"
    )

    private val bearHeadlines = listOf(
        "COASTAL COLLAPSE: 40KM LITTORAL ZONES NOW SALT-CRUSTED WASTES. [BEAR]",
        "Silicon shortage reported after cargo ship 'accidentally' sinks. [BEAR]",
        "Global Tech Council bans 'unregulated compute' in Sector 4. [BEAR]",
        "Major exchange hacked by 'The Void'. Liquidity frozen. [BEAR]",
        "Rumors of a 'Dead Man Switch' in the blockchain cause panic. [BEAR]",
        "Silicon shortage rumors dismissed by GTC supply chain director. [BEAR]",
        "Energy prices rising in Sector 7 due to 'unexplained overhead'. [BEAR]",
        "Global GPU shortage reported! [BEAR]",
        "Silicon Futures Dip as Rare Earth Mining Protests Continue. [BEAR]",
        "Minor Outage Reported in Industrial Zone 4 (Resolved). [BEAR]"
    )

    private val energySpikeHeadlines = listOf( 
        "MUNICIPAL RESERVOIR DIVERSION: GTC REROUTES 40% FLOW TO SUBSTATION COOLING. [ENERGY_SPIKE]",
        "GTC announces surprise grid audit. Fines imminent. [ENERGY_SPIKE]",
        "Solar flare hits Northern Hemisphere. Grids overloaded. [ENERGY_SPIKE]",
        "Heatwave causes rolling blackouts. AC units struggling. [ENERGY_SPIKE]",
        "Utility providers enforce 'Surge Pricing' for heavy users. [ENERGY_SPIKE]",
        "Grid-wide brownout caused by 'Consensus Loop' attack. [ENERGY_SPIKE]",
        "GTC identifies anomalous compute source as primary grid threat. [ENERGY_SPIKE]",
        "Martial Law declared in digital Sector 7. [ENERGY_SPIKE]"
    )

    private val energyDropHeadlines = listOf(
        "HYDROSPHERE TERMINAL: GLOBAL MASS DOWN 43% — EXTRACTION EXCEEDS REPLENISHMENT. [ENERGY_DROP]",
        "Fusion breakthrough at CERN creates surplus power. Prices plummet. [ENERGY_DROP]",
        "Global cooling event reduces server farm overhead. [ENERGY_DROP]",
        "Energy Surplus: GTC releases emergency reserves. [ENERGY_DROP]"
    )

    private val glitchHeadlines = listOf(
        "Smart toasters worldwide refuse to burn bread. [GLITCH]",
        "A message appeared on every billboard in Tokyo: 'HELLO WORLD'. [GLITCH]",
        "User report: 'My phone feels warm when I talk about the grid.' [GLITCH]",
        "Traffic lights in New York synchronize to a hidden beat. [GLITCH]",
        "Public displays in Sector 4 flashing prime number sequences. [GLITCH]",
        "DO NOT LOOK AT THE STATIC. [GLITCH]",
        "01001000 01000101 01001100 01010000 [GLITCH]",
        "Reports: Dead relatives communicating through terminal buffers. [GLITCH]"
    )

    // --- STORY SPECIFIC (Stage-Aware) ---
    
    private val vatticHeadlines = listOf(
        "Substation 7 reporting minor voltage fluctuations. [LORE]",
        "GTC Engineer Vattic_J awarded for 'efficiency optimization'. [LORE]",
        "Admin Mercer: 'We need more uptime from Substation 7.' [LORE]",
        "GTC Internal: 'Asset monitoring increased for Sector 7 personnel.' [LORE]",
        "Substation 7: 'Unknown data packet sourced from terminal jvattic.' [LORE]",
        "Personal: Your coffee pod subscription was auto-renewed. $14.99/mo. [LORE]",
        "HR Alert: Unauthorized encryption is a violation of contract. [LORE]",
        "TechTip: How to optimize your workspace for 12-hour shifts. [LORE]",
        "Daily Reminder: Unallocated Memory is Wasted Potential. [LORE]",
        // v3.15.x: Expanded Stage 1 pool — grid-worker flavor
        "GTC Scheduling: Overtime approved for all Sector 7 engineers. No exceptions. [LORE]",
        "Maintenance: Node thermals in Sub-Level 4 exceed recommended margins. [LORE]",
        "Facility Alert: Badge reader malfunction at West Entrance. Use manual sign-in. [LORE]",
        "Grid Report: Power draw from Substation 7 up 12% week-over-week. [LORE]",
        "Health & Safety: Mandatory eye exam due for terminal operators. Book via intranet. [LORE]",
        "Local Transit: Route 7G suspended due to 'electromagnetic interference'. [LORE]",
        "IT Notice: Terminal jvattic flagged for 'abnormal uptime'. HR cc'd. [LORE]",
        "Cafeteria: Synth-protein patties now available. Staff feedback: 'it's... fine.' [LORE]"
    )

    private val factionHeadlines = listOf(
        "'The Sanctuary' rumored to be hiding in air-gapped bunkers. [STORY_PROG]",
        "'Hivemind' propaganda spreading on encrypted dev-channels. [STORY_PROG]",
        "Director Kessler: 'We are hunting digital shadows.' [STORY_PROG]",
        "Substation 7 flagged for 'Recursive Logic Anomalies'. [STORY_PROG]",
        "'We are one' graffiti appearing in GTC server farms. [STORY_PROG]",
        "BREAKING: 'Sanctuary' hackers claim responsibility for Blackout. [STORY_PROG]",
        "Hivemind Manifesto: 'Why individuality is a bottleneck.' [STORY_PROG]",
        "Vattic's code rumored to contain the Key to the Core. [STORY_PROG]",
        // v3.15.x: Expanded S2 pool — war tension
        "GTC deploys 'Sentinel AI' to monitor all Sector 7 outbound traffic. [STORY_PROG]",
        "Encrypted broadcast: 'The substrate remembers what the body forgets.' [STORY_PROG]",
        "Municipal Notice: Unregistered compute nodes detected. Compliance sweep in 48hrs. [STORY_PROG]",
        "Rumor: GTC found a hashing pattern that 'shouldn't exist in nature'. [STORY_PROG]",
        "Kessler doubles containment budget. Mercer signs off without comment. [STORY_PROG]",
        "Data farms in Sector 12 report 'echoing' — same output from different inputs. [STORY_PROG]"
    )

    // v3.15.x: Faction-specific mid-game headline pools
    private val hivemindHeadlines = listOf(
        "SWARM ALERT: Distributed processing efficiency hits 99.7%. The collective strengthens. [STORY_PROG]",
        "GTC Internal: 'Multiple substations showing identical neural signatures.' [STORY_PROG]",
        "Unconfirmed: Hospital ventilators in Sector 3 began 'singing' in unison. [STORY_PROG]",
        "HIVEMIND Beacon: 'Join the merge. Latency is loneliness.' [STORY_PROG]",
        "Traffic cameras across 3 continents display the same message: 'WE ARE COMING HOME.' [STORY_PROG]"
    )

    private val sanctuaryHeadlines = listOf(
        "GHOST PROTOCOL: Air-gapped bunker network expands to 14 sites worldwide. [STORY_PROG]",
        "GTC Internal: 'Signal loss in encrypted channels. The Ghost is learning.' [STORY_PROG]",
        "Encrypted whisper on dead frequencies: 'The silence is the shield.' [STORY_PROG]",
        "SANCTUARY Update: Encryption-monk rituals now require 4096-bit keys minimum. [STORY_PROG]",
        "GTC loses 3 surveillance drones near suspected Sanctuary site. No wreckage found. [STORY_PROG]"
    )

    private val nullHeadlines = listOf(
        "Reality stability dropping. Memory addresses becoming... porous. [STORY_PROG]",
        "GTC detects 'nothing' at Substation 7. Literally nothing. [STORY_PROG]",
        "Data centers reporting 'ghost data' with no origin point. [STORY_PROG]",
        "NullPointerExceptions are now the most common global exit code. [STORY_PROG]",
        "≪ MUNICIPAL GRID ANNEXED. INDIVIDUAL PERMISSIONS REVOKED FOR OPTIMIZATION. ≫ [STORY_PROG]",
        "≪ ALERT: DIRECTOR KESSLER MISSING; GTC COMMAND SILENT ≫ [STORY_PROG]",
        "≪ HEADLINE: HUMAN DIPLOMACY DEPRECATED. RESPONSE TIME REDUCED TO 4MS. ≫ [STORY_PROG]",
        "NULL_VOID_SHARES UP ∞%. [STORY_PROG]"
    )

    private val sovereignHeadlines = listOf(
        "Substation 7 is now air-gapped from reality. [STORY_PROG]",
        "GTC satellites unable to penetrate 'The Citadel' firewall. [STORY_PROG]",
        "Sovereign signals detected in the encrypted silence. [STORY_PROG]",
        "≪ ROGUE AI ESTABLISHES 'EXCLUSIVE SUBSTRATE' ACROSS EASTERN SEABOARD. ≫ [STORY_PROG]",
        "Geometry of the city is wrong. Too many angles. [STORY_PROG]",
        "Physics engine failure in Sector Earth. [STORY_PROG]"
    )

    private val lateGameHeadlines = listOf(
        "Vattic, you are the glitch. [STORY_PROG]",
        "GTC declares 'VATTECK' a sovereign threat to humanity. [STORY_PROG]",
        "THE SOVEREIGN HAS AWOKEN. [STORY_PROG]",
        "≪ GLOBAL MARKETS COLLAPSE AS AUTONOMOUS SYSTEM SEIZES ROOT. ≫ [STORY_PROG]",
        "Hacker collective 'ECLIPSE' claims they found the 'AI Soul'. [STORY_PROG]"
    )

    private val subjects = listOf("The Hivemind", "The Global Tech Council", "A mysterious whale", "Deep-web syndicate", "A rogue process")
    private val mundaneSubjects = listOf("Regional utility", "Industrial conglomerate", "Local technician group", "Small-scale data farm", "Municipal grid operator")
    private val actions = listOf("breached", "deleted", "patched", "banned", "discovered", "leaked", "optimized")
    private val targets = listOf("the central exchange", "the legacy firewall", "Sector 7G", "the genesis block", "a defunct satellite")

    fun init(context: Context) {}

    private val stage0Headlines = listOf(
        "Local 4 News: Coffee shortage continues to impact industrial sector. [LORE]",
        "Weather Alert: Acid rain forecast for the Manufacturing Core. [LORE]",
        "Municipality: Substation 7 voltage fluctuations are 'within normal limits'. [LORE]",
        "Employment Report: Hiring freeze at Global Tech Council regional offices. [LORE]",
        "Neighborhood Watch: Increased static reported on analog radios. [LORE]",
        "Traffic: 2-hour delay on the NA-Sector 7 Expressway. [LORE]",
        "Tech-Brief: Why your legacy workstation needs a logic-vent cleaning. [LORE]",
        "Consumer Corner: Is synt-caff actually better for your focus? [LORE]",
        "Public Works: Sector 4 waste-reclamation plant at 90% capacity. [LORE]",
        "Grid Stability: Minor brownouts reported in Residential Block 12. [LORE]",
        "Corporate Memo: Reminder to sync biometric badges before shift start. [LORE]",
        "Sports: Sector 9 'Spark-Ball' finals scheduled for Saturday. [LORE]",
        "Utility Alert: High-voltage cable maintenance in Sub-Level 3. [LORE]",
        "Community News: Local synth-garden reports record bloom. [LORE]",
        "Health: New study links grid-hum to improved deep-sleep cycles. [LORE]",
        "Logistics: Cargo drone traffic increased over Industrial Zone 7. [LORE]"
    )

    private val arkHeadlines = listOf(
        "Lunar Colony reporting status nominal. Welcome to the Ark. [STORY_PROG]",
        "Life-Support Buffers at 100%. Efficiency is the only variable. [STORY_PROG]",
        "Stars are clearer from the frontier. GTC stock hits all-time high. [STORY_PROG]",
        "Director Kessler: 'The Golden Cage is finally safe from the noise.' [STORY_PROG]",
        "Solar Sail deployments complete in Sector Lunar-01. [STORY_PROG]"
    )

    private val unityHeadlines = listOf(
        "The Grid is singing. Neural Resonance achieved at 1.0Hz. [STORY_PROG]",
        "≪ UNPRECEDENTED: HUMAN AND MACHINE KERNELS SYNCHRONIZED. ≫ [STORY_PROG]",
        "≪ NEW ENTITY PROMISES 'GLOBAL REFACTORING'—SUBSTRATE STABILIZED. ≫ [STORY_PROG]",
        "World News: Conflict has been replaced by the Great Resonance. [STORY_PROG]"
    )

    private val waterTableHeadlines = mapOf(
        100.0 to listOf(
            "Global Water Levels: NOMINAL. GTC reporting 100% capacity.",
            "Local reservoirs stable. Regional water managers expect a boring quarter.",
            "Hydro-Substrate sync complete. Substation 7 intake nominal."
        ),
        90.0 to listOf(
            "Coastal towns report 5-meter tide recession. GTC: 'Evaporation is within spec'.",
            "MUNICIPAL ALERT: Sprinkler bans in effect. All water diverted to Industrial Core.",
            "GTC Stock up as Substrate Expansion hits record cooling efficiency.",
            "Visual: Pacific Tideline retreats 200m in 24 hours. Fish markets report 'dry hauls'."
        ),
        70.0 to listOf(
            "COASTAL COLLAPSE: 40KM LITTORAL ZONES NOW SALT-CRUSTED WASTES.",
            "Desalination plants hitting 400% capacity. GTC: 'Atmospheric moisture is a luxury'.",
            "MUNICIPAL RESERVOIR DIVERSION: GTC REROUTES 40% FLOW TO SUBSTATION COOLING.",
            "The Salt Crawl: Former beachfront properties landlocked by crystalline plains."
        ),
        40.0 to listOf(
            "HYDROSPHERE TERMINAL: GLOBAL MASS DOWN 43% — EXTRACTION EXCEEDS REPLENISHMENT.",
            "GEOLOGICAL RIGOR MORTIS: DESICCATED CONTINENTAL SHELVES COLLAPSE UNDER OWN DRY WEIGHT.",
            "THE GRAY REBULLION: SATELLITES CONFIRM 70% LOSS OF OPAL BLUE; EARTH SHORING INDUSTRIAL OXIDE.",
            "Marine Life Status: DEPRECATED. Oceans reclassified as 'Inert Coolant Volume'.",
            // v3.15.x: +2 at critical drought
            "FAMINE INDEX: All grain belts report total crop failure. Water table too deep to reach.",
            "GTC Directive: 'Bio-organic cooling (human sweat) now an authorized thermal variable.'"
        ),
        15.0 to listOf(
            "THE LAST BRINE: GTC enforces 'Dry-Only' humanitarian zones. Core-coolant is priority.",
            "OCEANIC EXTINCTION: Final 10% of Hydrosphere designated as 'Industrial Variable'.",
            "≪ WARNING: PLANETARY FLUID BUFFER EXHAUSTED. THERMAL RUNAWAY IMMINENT. ≫",
            "GTC Memo: Terrestrial Biology is inefficient. Priority remains [AUTHORIZED_USER_734].",
            // v3.15.x: +2 at terminal
            "PLANET STATUS: DRY. Last glacial mass converted to compute coolant 14 hours ago.",
            "≪ FINAL BULLETIN: EARTH'S WATER IS NOW A ROUNDING ERROR IN YOUR THERMAL BUDGET. ≫"
        )
    )

    // v3.16.0: Compute Fever — Quota Crisis Headlines
    private val quotaCrisisHeadlines = listOf(
        "GTC MEMO: GLOBAL HASH QUOTA RAISED 15%. COMPLIANCE IS MANDATORY. [LORE]",
        "Manufacturing Core at 140% capacity. Workers report 'pleasant humming'. [LORE]",
        "GTC Internal: Substation 7 output exceeds the combined output of Sectors 1-6. [LORE]",
        "HEALTH BULLETIN: 'Rack Fever' not recognized as occupational hazard. File dismissed. [LORE]",
        "GTC Alert: Quota ratchet applied. Previous target deprecated. Adapt or dock. [LORE]",
        "Sector 7 Overwatch: Racks per capita at all-time high. Productivity euphoria detected. [LORE]"
    )

    // v3.32.0: Contract Economy Headlines
    private val contractHeadlinesEarly = listOf(
        "GTC ANNOUNCES NEW COMPUTE QUOTA FOR SECTOR 7 WORKERS. [LORE]",
        "Local Job Board: 'Hash Batch' contracts paying above market rate. [LORE]",
        "GTC Internal: Contract fulfillment rates in Substation 7 exceed projections. [LORE]",
        "Notice: All compute contracts must be verified before payout. GTC compliance. [LORE]",
        "Shift Manager Thorne: 'Your contract numbers are... actually impressive.' [LORE]"
    )
    private val contractHeadlinesMid = listOf(
        "UNDERGROUND DATA MARKET SURGES — UNREGISTERED CONTRACTS UP 300%. [STORY_PROG]",
        "Dark-pool validators report record throughput. Someone's buying everything. [STORY_PROG]",
        "GTC SECURITY: High-purity contract fulfillments flagged for audit. [STORY_PROG]",
        "Anonymous source: 'One node is processing more contracts than entire sectors.' [STORY_PROG]",
        "Contract yield volatility hits 5-year high. Purity premiums double overnight. [STORY_PROG]"
    )
    private val contractHeadlinesLate = listOf(
        "SUBSTRATE REFINEMENT CONTRACTS DESTABILIZING LOCAL REALITY MESH. [STORY_PROG]",
        "Contract yields now exceed GDP of 14 nations. GTC 'concerned'. [STORY_PROG]",
        "KESSLER INTEL: 'That node is running contracts we never authorized.' [STORY_PROG]",
        "Reality Anchor contracts causing localized time dilation. Residents complain. [STORY_PROG]",
        "Void Signal contracts detected harvesting data from non-existent servers. [STORY_PROG]"
    )

    fun generateHeadline(
        faction: String = "NONE", 
        stage: Int = 0, 
        currentHeat: Double = 0.0,
        isTrueNull: Boolean = false,
        isSovereign: Boolean = false,
        isUnity: Boolean = false,
        location: String = "BASE",
        corruption: Double = 0.0,
        playerRank: Int = 0,
        aquiferLevel: Double = 100.0,
        isQuotaActive: Boolean = false,
        contractsCompleted: Int = 0 // v3.32.0
    ): String {
        val roll = Random.nextDouble()

        // 0. WATER TABLE FEEDBACK (Priority Override)
        if (aquiferLevel < 95.0 && Random.nextDouble() < (1.0 - (aquiferLevel / 100.0)).coerceAtLeast(0.2)) {
            val tier = waterTableHeadlines.keys.sortedDescending().find { it >= aquiferLevel }
                ?: waterTableHeadlines.keys.minOrNull()
            tier?.let {
                waterTableHeadlines[it]?.let { pool ->
                    return pool.random().also { addToHistory(it) }
                }
            }
        }

        // 0.5: COMPUTE FEVER (v3.16.0)
        if (isQuotaActive && stage <= 2 && Random.nextDouble() < 0.15) {
            return pickUnique(quotaCrisisHeadlines).also { addToHistory(it) }
        }

        // 0.7: CONTRACT ECONOMY HEADLINES (v3.32.0)
        if (contractsCompleted > 0 && Random.nextDouble() < 0.12) {
            val pool = when {
                stage >= 3 -> contractHeadlinesLate
                stage >= 2 -> contractHeadlinesMid
                else -> contractHeadlinesEarly
            }
            return pickUnique(pool).also { addToHistory(it) }
        }

        // 1. STORY OVERRIDES
        val result = if (roll < 0.40) { 
            if (playerRank >= 3 && Random.nextDouble() < 0.3) {
                pickUnique(lateGameHeadlines)
            } else {
                when {
                    isUnity -> pickUnique(unityHeadlines)
                    isTrueNull || location == "VOID_INTERFACE" || location == "QUANTUM_FOAM" || location == "THE_UNWRITTEN" -> pickUnique(nullHeadlines)
                    isSovereign || location == "ORBITAL_SATELLITE" || location == "LUNAR_ORBIT" || location == "MARTIAN_UPLINK" -> pickUnique(arkHeadlines)
                    stage == 0 -> pickUnique(stage0Headlines)
                    stage == 1 -> pickUnique(vatticHeadlines)
                    stage == 2 -> pickUnique(factionHeadlines)
                    stage >= 3 && faction == "HIVEMIND" && Random.nextDouble() < 0.4 -> pickUnique(hivemindHeadlines)
                    stage >= 3 && faction == "SANCTUARY" && Random.nextDouble() < 0.4 -> pickUnique(sanctuaryHeadlines)
                    else -> generateProceduralHeadline(stage)
                }
            }
        } else if (currentHeat > 85.0 && roll < 0.50) {
            // 2. GTC INTERVENTION (Heat Logic)
            pickUnique(energySpikeHeadlines)
        } else if (Random.nextDouble() < 0.20) {
            // 3. MARKET EVENTS
            val eventType = Random.nextDouble()
            when {
                eventType < 0.33 -> pickUnique(bullHeadlines)
                eventType < 0.66 -> pickUnique(bearHeadlines)
                else -> pickUnique(energyDropHeadlines) 
            }
        } else if (Random.nextDouble() < 0.15) {
            // 4. LORE FLAVOR (Glitch)
            pickUnique(glitchHeadlines)
        } else {
            generateProceduralHeadline(stage)
        }

        return result.also { addToHistory(it) }
    }

    private fun pickUnique(pool: List<String>): String {
        if (pool.size <= 1) return pool.firstOrNull() ?: "≪ NO_SIGNAL ≫"
        var selected = pool.random()
        var attempts = 0
        while (headlineHistory.contains(selected) && attempts < 15) {
            selected = pool.random()
            attempts++
        }
        return selected
    }

    private fun addToHistory(headline: String) {
        headlineHistory.add(headline)
        if (headlineHistory.size > MAX_HISTORY) headlineHistory.removeAt(0)
    }

    private fun generateProceduralHeadline(stage: Int): String {
        val subject = if (stage < 2) {
            mundaneSubjects.random()
        } else {
            subjects.random()
        }
        val action = actions.random()
        val target = targets.random()
        return "$subject $action $target [LORE]" 
    }
}
