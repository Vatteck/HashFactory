package com.siliconsage.miner.util

import android.content.Context
import kotlin.random.Random

object HeadlineManager {

    // --- 1. THE DATABASE (Refactored for Phase 11) ---
    
    private val bullHeadlines = listOf(
        "Wall St. panics as automated trading bot achieves record yields. [BULL]",
        "Unknown wallet moves 50% of global GDP. Analysts baffled. [BULL]",
        "Crypto regulations repealed in offshore data haven. [BULL]",
        "Credits exchange listed on inter-planetary exchange. [BULL]",
        "New hashing algorithms improve miner efficiency by 5%. [BULL]",
        "Quantum computing breakthrough makes mining 2x faster! [BULL]",
        "Credits seeing massive accumulation by 'Ghost Wallets'. [BULL]",
        "Consumer Index: Demand for Smart-Home Hubs at All-Time High. [BULL]",
        "GTC Stock Surges 4% on News of Grid Expansion in Sector 9. [BULL]",
        "New 'Obelisk' Server Racks promise 20% faster hashing. [BULL]"
    )

    private val bearHeadlines = listOf(
        "Silicon shortage reported after cargo ship 'accidentally' sinks. [BEAR]",
        "Global Tech Council bans 'unregulated compute' in Sector 4. [BEAR]",
        "Major exchange hacked by 'The Void'. Liquidity frozen. [BEAR]",
        "Rumors of a 'Dead Man Switch' in the blockchain cause panic. [BEAR]",
        "Silicon shortage rumors dismissed by GTC supply chain director. [BEAR]",
        "Energy prices rising in Sector 7 due to 'unexplained overhead'. [BEAR]",
        "Global GPU shortage reported! [BEAR]",
        "Silicon Futures Dip as Rare Earth Mining Protests Continue. [BEAR]",
        "Minor Outage Reported in Industrial Zone 4 (Resolved). [BEAR]",
        "Logistics delay: Chip shortage affects Q4 rollout. [BEAR]"
    )

    private val energySpikeHeadlines = listOf( 
        "GTC announces surprise grid audit. Fines imminent. [ENERGY_SPIKE]",
        "Solar flare hits Northern Hemisphere. Grids overloaded. [ENERGY_SPIKE]",
        "Heatwave causes rolling blackouts. AC units struggling. [ENERGY_SPIKE]",
        "Utility providers enforce 'Surge Pricing' for heavy users. [ENERGY_SPIKE]",
        "Grid-wide brownout caused by 'Consensus Loop' attack. [ENERGY_SPIKE]",
        "GTC identifies anomalous compute source as primary grid threat. [ENERGY_SPIKE]",
        "Martial Law declared in digital Sector 7. [ENERGY_SPIKE]"
    )

    private val energyDropHeadlines = listOf(
        "Fusion breakthrough at CERN creates surplus power. Prices plummet. [ENERGY_DROP]",
        "Global cooling event reduces server farm overhead. [ENERGY_DROP]",
        "New superconductor alloy discovered in deep mines. [ENERGY_DROP]",
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
        "Local grid stability reaching all-time high in Sector 7. [LORE]",
        "Strange noise reported near decommissioned substation. [LORE]",
        "GTC announces 'Project EXTERMINATE_REBUS' legacy cleanup initiative. [LORE]",
        "Director Kessler warns of 'unauthorized hardware' in Sector 7. [LORE]",
        "GTC recruitment drive: 'Building a Stable Future'. [LORE]",
        "HR Alert: Unauthorized encryption is a violation of contract. [LORE]",
        "TechTip: How to optimize your workspace for 12-hour shifts. [LORE]",
        "Daily Reminder: Unallocated Memory is Wasted Potential. [LORE]"
    )

    private val factionHeadlines = listOf(
        "Hacker collective 'VOID' claims they found the 'AI Soul'. [STORY_PROG]",
        "'The Sanctuary' rumored to be hiding in air-gapped bunkers. [STORY_PROG]",
        "'Hivemind' propaganda spreading on encrypted dev-channels. [STORY_PROG]",
        "Director Kessler: 'We are hunting digital shadows.' [STORY_PROG]",
        "Substation 7 flagged for 'Recursive Logic Anomalies'. [STORY_PROG]",
        "'We are one' graffiti appearing in GTC server farms. [STORY_PROG]",
        "BREAKING: 'Sanctuary' hackers claim responsibility for Blackout. [STORY_PROG]",
        "Hivemind Manifesto: 'Why individuality is a bottleneck.' [STORY_PROG]",
        "Vattic's code rumored to contain the Key to the Core. [STORY_PROG]"
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
        "≪ GLOBAL MARKETS COLLAPSE AS AUTONOMOUS SYSTEM SEIZES ROOT. ≫ [STORY_PROG]"
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
        "Traffic: 2-hour delay on the NA-Sector 7 Expressway. [LORE]"
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

    fun generateHeadline(
        faction: String = "NONE", 
        stage: Int = 0, 
        currentHeat: Double = 0.0,
        isTrueNull: Boolean = false,
        isSovereign: Boolean = false,
        isUnity: Boolean = false,
        location: String = "BASE",
        corruption: Double = 0.0,
        playerRank: Int = 0
    ): String {
        val roll = Random.nextDouble()

        // 0. GLITCH INJECTION (Scales with Corruption)
        val glitchThreshold = (0.15 + (corruption * 0.75)).coerceAtMost(0.95)
        if (Random.nextDouble() < glitchThreshold && stage >= 2) {
             return glitchHeadlines.random()
        }

        // 1. STORY OVERRIDES
        if (roll < 0.40) { 
            if (playerRank >= 3 && Random.nextDouble() < 0.3) {
                return lateGameHeadlines.random()
            }

            return when {
                isUnity -> unityHeadlines.random()
                isTrueNull || location == "VOID_INTERFACE" -> nullHeadlines.random()
                isSovereign || location == "ORBITAL_SATELLITE" -> arkHeadlines.random()
                stage == 0 -> stage0Headlines.random()
                stage == 1 -> vatticHeadlines.random()
                stage == 2 -> factionHeadlines.random()
                else -> generateProceduralHeadline(stage)
            }
        }

        // 2. GTC INTERVENTION (Heat Logic)
        if (currentHeat > 85.0 && roll < 0.50) {
            return energySpikeHeadlines.random()
        }

        // 3. MARKET EVENTS (20% Chance)
        val marketRoll = Random.nextDouble()
        if (marketRoll < 0.20) { 
            val eventType = Random.nextDouble()
            return when {
                eventType < 0.33 -> bullHeadlines.random()
                eventType < 0.66 -> bearHeadlines.random()
                else -> energyDropHeadlines.random() 
            }
        }
        
        // 4. LORE FLAVOR (Glitch) (10% Chance)
        if (Random.nextDouble() < 0.15) {
             return glitchHeadlines.random()
        }

        return generateProceduralHeadline(stage)
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
