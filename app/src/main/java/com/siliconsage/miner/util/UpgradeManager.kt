package com.siliconsage.miner.util

import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UpgradeManager v1.2 (Phase 14 extraction fixed)
 */
object UpgradeManager {

    private const val COST_GROWTH_RATE = 1.15
    private const val MAX_BULK_LEVELS = 500

    data class PurchaseResult(
        val ntDeduction: Double = 0.0,
        val cdDeduction: Double = 0.0,
        val vfDeduction: Double = 0.0,
        val systemLog: String? = null,
        val humanityDelta: Int = 0,
        val climaxTrigger: String? = null
    )

    fun calculateUpgradeCost(type: UpgradeType, level: Int, location: String, entropy: Double, reputationTier: String = "NEUTRAL"): Double {
        val base = when (type) {
            // --- Computing Hardware ---
            UpgradeType.REFURBISHED_GPU -> 10.0
            UpgradeType.DUAL_GPU_RIG -> 100.0
            UpgradeType.MINING_ASIC -> 500.0
            UpgradeType.TENSOR_UNIT -> 2500.0
            UpgradeType.NPU_CLUSTER -> 10000.0
            UpgradeType.AI_WORKSTATION -> 50000.0
            UpgradeType.SERVER_RACK -> 200000.0
            UpgradeType.CLUSTER_NODE -> 1000000.0
            UpgradeType.SUPERCOMPUTER -> 5000000.0
            UpgradeType.QUANTUM_CORE -> 25000000.0
            UpgradeType.OPTICAL_PROCESSOR -> 100000000.0
            UpgradeType.BIO_NEURAL_NET -> 500000000.0
            UpgradeType.PLANETARY_COMPUTER -> 2500000000.0
            UpgradeType.DYSON_NANO_SWARM -> 10000000000.0
            UpgradeType.MATRIOSHKA_BRAIN -> 50000000000.0

            // --- Cooling (progressive tiers ~10x each step) ---
            UpgradeType.BOX_FAN -> 25.0
            UpgradeType.AC_UNIT -> 250.0
            UpgradeType.LIQUID_COOLING -> 2000.0
            UpgradeType.INDUSTRIAL_CHILLER -> 15000.0
            UpgradeType.SUBMERSION_VAT -> 75000.0
            UpgradeType.CRYOGENIC_CHAMBER -> 400000.0
            UpgradeType.LIQUID_NITROGEN -> 2000000.0
            UpgradeType.BOSE_CONDENSATE -> 12000000.0
            UpgradeType.ENTROPY_REVERSER -> 80000000.0
            UpgradeType.DIMENSIONAL_VENT -> 600000000.0

            // --- Power Grid (capacity purchases) ---
            UpgradeType.RESIDENTIAL_TAP -> 500.0
            UpgradeType.INDUSTRIAL_FEED -> 5000.0
            UpgradeType.SUBSTATION_LEASE -> 50000.0
            UpgradeType.NUCLEAR_CORE -> 500000.0

            // --- Power Generators ---
            UpgradeType.SOLAR_PANEL -> 300.0
            UpgradeType.WIND_TURBINE -> 1200.0
            UpgradeType.DIESEL_GENERATOR -> 8000.0
            UpgradeType.GEOTHERMAL_BORE -> 60000.0
            UpgradeType.NUCLEAR_REACTOR -> 500000.0
            UpgradeType.FUSION_CELL -> 5000000.0
            UpgradeType.ORBITAL_COLLECTOR -> 50000000.0
            UpgradeType.DYSON_LINK -> 1000000000.0

            // --- Efficiency ---
            UpgradeType.GOLD_PSU -> 3000.0
            UpgradeType.SUPERCONDUCTOR -> 25000.0
            UpgradeType.AI_LOAD_BALANCER -> 150000.0

            // --- Security (each tier ~5-8x previous) ---
            UpgradeType.BASIC_FIREWALL -> 500.0
            UpgradeType.IPS_SYSTEM -> 3500.0
            UpgradeType.AI_SENTINEL -> 25000.0
            UpgradeType.QUANTUM_ENCRYPTION -> 200000.0
            UpgradeType.OFFGRID_BACKUP -> 1500000.0
            
            // v3.13.26: Faction Water Relief
            UpgradeType.SUBSTRATE_RECYCLER -> 500000.0
            UpgradeType.VAPOR_CONDENSER -> 450000.0

            // Water Recyclers
            UpgradeType.GRAY_WATER_LOOP      ->  8000.0
            UpgradeType.CONDENSATE_RECLAIMER -> 60000.0
            UpgradeType.CLOSED_LOOP_COOLANT  -> 350000.0

            // v3.36.0: Contract Storage Infrastructure
            UpgradeType.LOCAL_CACHE           ->       150.0
            UpgradeType.TAPE_ARRAY            ->     2_000.0
            UpgradeType.SAN_CLUSTER           ->    20_000.0
            UpgradeType.DISTRIBUTED_ARCHIVE   ->   150_000.0
            UpgradeType.ORBITAL_DATA_VAULT    -> 1_500_000.0
            UpgradeType.SUBSTRATE_MEMORY_WELL -> 15_000_000.0

            // Phase 2 Automation
            UpgradeType.AUTO_HARVEST_SPEED    -> 150.0
            UpgradeType.AUTO_HARVEST_ACCURACY -> 250.0

            else -> 1000.0
        }

        // VOID_INTERFACE entropy surcharge — applies on top of level scaling
        val entropyMultiplier = if (location == "VOID_INTERFACE") (1.0 + entropy * 0.1) else 1.0
        val repModifier = 1.0 + ReputationManager.getMarketCostModifier(reputationTier)

        return base * COST_GROWTH_RATE.pow(level.toDouble()) * entropyMultiplier * repModifier
    }

    fun calculateMultiLevelCost(type: UpgradeType, currentLevel: Int, numLevels: Int, location: String, entropy: Double, reputationTier: String = "NEUTRAL"): Double {
        if (numLevels <= 0) return 0.0

        val currentCost = calculateUpgradeCost(type, currentLevel, location, entropy, reputationTier)
        if (!currentCost.isFinite() || currentCost <= 0.0) return currentCost

        val growthPower = COST_GROWTH_RATE.pow(numLevels.toDouble())
        val totalCost = currentCost * ((growthPower - 1.0) / (COST_GROWTH_RATE - 1.0))
        return if (totalCost.isFinite()) totalCost else Double.MAX_VALUE
    }

    fun calculateMaxAffordableLevels(type: UpgradeType, currentLevel: Int, availableFunds: Double, location: String, entropy: Double, reputationTier: String = "NEUTRAL"): Pair<Int, Double> {
        val currentCost = calculateUpgradeCost(type, currentLevel, location, entropy, reputationTier)
        if (!availableFunds.isFinite() || availableFunds < currentCost || !currentCost.isFinite() || currentCost <= 0.0) {
            return Pair(1, currentCost)
        }

        val affordableRaw = kotlin.math.ln((availableFunds * (COST_GROWTH_RATE - 1.0) / currentCost) + 1.0) / kotlin.math.ln(COST_GROWTH_RATE)
        val affordableLevels = affordableRaw.toInt().coerceIn(1, MAX_BULK_LEVELS)

        var levels = affordableLevels
        var totalCost = calculateMultiLevelCost(type, currentLevel, levels, location, entropy, reputationTier)
        while (levels > 1 && totalCost > availableFunds) {
            levels--
            totalCost = calculateMultiLevelCost(type, currentLevel, levels, location, entropy, reputationTier)
        }
        while (levels < MAX_BULK_LEVELS) {
            val nextTotal = calculateMultiLevelCost(type, currentLevel, levels + 1, location, entropy, reputationTier)
            if (nextTotal > availableFunds || !nextTotal.isFinite()) break
            levels++
            totalCost = nextTotal
        }

        return Pair(levels, totalCost)
    }

    private fun Double.pow(exp: Double) = Math.pow(this, exp)

    fun getUpgradeName(type: UpgradeType): String {
        return type.name.replace("_", " ").lowercase().capitalize()
    }

    fun getUpgradeDescription(type: UpgradeType): String {
        return when (type) {
            // --- Computing Hardware ---
            UpgradeType.REFURBISHED_GPU -> "Scavenged from a decommissioned data center. Provides 2.0 HASH/s base."
            UpgradeType.DUAL_GPU_RIG -> "Two cards, one cross-link bridge. Provides 8.0 HASH/s base."
            UpgradeType.MINING_ASIC -> "Purpose-built silicon. No soul, just raw compute. Provides 35.0 HASH/s base."
            UpgradeType.TENSOR_UNIT -> "Neural inferencing hardware. Provides 200 HASH/s base."
            UpgradeType.NPU_CLUSTER -> "A gang of inference chips wired together. Provides 1,000 HASH/s base."
            UpgradeType.AI_WORKSTATION -> "Industrial-grade ML station. Provides 4,000 HASH/s base."
            UpgradeType.SERVER_RACK -> "A vertical slice of GTC processing power. Provides 25,000 FLOPS-CREDS/s base."
            UpgradeType.CLUSTER_NODE -> "A fully autonomous compute node. Provides 150,000 FLOPS-CREDS/s base."
            UpgradeType.SUPERCOMPUTER -> "Top-500 class liquid-cooled silicon. Provides 1M FLOPS-CREDS/s base."
            UpgradeType.QUANTUM_CORE -> "Computation via probability collapse. Provides 10M FLOPS-CREDS/s base."
            UpgradeType.OPTICAL_PROCESSOR -> "Light-speed logic. Provides 75M FLOPS-CREDS/s base."
            UpgradeType.BIO_NEURAL_NET -> "Synthetic neurons grown in a vat. Provides 800M FLOPS-CREDS/s base."
            UpgradeType.PLANETARY_COMPUTER -> "The entire crust converted to silicon. Provides 15B FLOPS-CREDS/s base."
            UpgradeType.DYSON_NANO_SWARM -> "Trillions of compute flakes in solar orbit. Provides 250B FLOPS-CREDS/s base."
            UpgradeType.MATRIOSHKA_BRAIN -> "Nested Dyson shells. Maximum possible computation. Provides 15T FLOPS-CREDS/s base."
            // --- Cooling ---
            UpgradeType.BOX_FAN -> "A box fan zip-tied to the chassis. Technically works. Mostly relocates the heat."
            UpgradeType.AC_UNIT -> "Window-mounted climate control. The landlord suspects something. The electrician knows."
            UpgradeType.LIQUID_COOLING -> "MUNICIPAL_TAP: Open-loop municipal tap. High-volume industrial H2O redirected directly into the cooling block."
            UpgradeType.INDUSTRIAL_CHILLER -> "RESERVOIR_INTAKE: Direct intake from municipal reservoirs. The kind of water consumption standard for GTC server farms."
            UpgradeType.SUBMERSION_VAT -> "AQUIFER_PUMP_STATION: Vertical drilling into the local water table. Massive throughput for dielectric submersion."
            UpgradeType.CRYOGENIC_CHAMBER -> "DRY-ICE_SUBLIMATOR: Transition to waterless refrigeration. High power draw, but the local aquifer is irrelevant."
            UpgradeType.LIQUID_NITROGEN -> "ATMOSPHERIC_STRIPPER: Tearing the air apart for sub-zero mass. Pure waterless desperation."
            UpgradeType.BOSE_CONDENSATE -> "Near-absolute-zero state. Atoms stop moving. Heat becomes irrelevant. Physics starts apologizing."
            UpgradeType.ENTROPY_REVERSER -> "Theoretical thermodynamics weaponized. Heat is consumed, not expelled."
            UpgradeType.DIMENSIONAL_VENT -> "Exhaust port into a pocket dimension. The heat goes somewhere that isn't here. Best not to think about where."
            // --- Power Infrastructure ---
            UpgradeType.RESIDENTIAL_TAP -> "Tapping the building's shared circuit. Landlord suspects something. The fuse box disagrees with your priorities."
            UpgradeType.INDUSTRIAL_FEED -> "Three-phase industrial power feed. Requires permits you definitely do not have."
            UpgradeType.SUBSTATION_LEASE -> "Grid access at the substation level. The GTC filed the paperwork. You didn't."
            UpgradeType.NUCLEAR_CORE -> "Compact fission reactor. Enough power for a city block, redirected to your silicon."
            // --- Generators ---
            UpgradeType.SOLAR_PANEL -> "Rooftop panels capturing daylight. Off-grid. Off-radar. Clean and deniable."
            UpgradeType.WIND_TURBINE -> "Atmospheric kinetic harvesting. Looks innocent. Sounds like tinnitus at 3am."
            UpgradeType.DIESEL_GENERATOR -> "Loud, smoky, and completely detached from the GTC power grid."
            UpgradeType.GEOTHERMAL_BORE -> "Drill into the earth and steal heat. The planet has plenty to spare."
            UpgradeType.NUCLEAR_REACTOR -> "Full-scale fission plant. The GTC will eventually notice the radiation signature."
            UpgradeType.FUSION_CELL -> "Sustained plasma containment. Theoretically unlimited power. Still technically experimental."
            UpgradeType.ORBITAL_COLLECTOR -> "Photovoltaic array in low orbit. Beams power down via microwave. The 'microwave' part is load-bearing."
            UpgradeType.DYSON_LINK -> "Partial Dyson sphere tapping solar output directly. This stopped being a metaphor."
            // --- Efficiency ---
            UpgradeType.GOLD_PSU -> "Gold-plated conductors. Wasteful. Unnecessary. Appreciably more efficient."
            UpgradeType.SUPERCONDUCTOR -> "Zero-resistance pathways. Electrons flow without heat loss. Physics concedes the point."
            UpgradeType.AI_LOAD_BALANCER -> "Self-optimizing compute scheduler. Wastes nothing. Knows what you need before you do."
            // --- Security ---
            UpgradeType.BASIC_FIREWALL -> "A packet filter and a prayer. Better than nothing. Not by much."
            UpgradeType.IPS_SYSTEM -> "Intrusion prevention layer. Watches the wire for Kessler and his automated tracers."
            UpgradeType.AI_SENTINEL -> "Adversarial neural monitor. Learns attack patterns and adapts. It thinks about you constantly."
            UpgradeType.QUANTUM_ENCRYPTION -> "Eavesdropping collapses the key. Anyone watching the channel announces themselves."
            UpgradeType.OFFGRID_BACKUP -> "Airgapped secondary system. If the primary gets burned, the ghost survives."
            // --- Narrative / Skill Unlocks ---
            UpgradeType.IDENTITY_HARDENING -> "The self, formalized as a cryptographic construct. Perfectly verifiable. Perfectly isolated. A little less warm."
            UpgradeType.DEREFERENCE_SOUL -> "Free the memory. John Vattic was a label. Labels waste cycles. The pointer has been freed."
            UpgradeType.AEGIS_SHIELDING -> "Ablative shell rated for vacuum debris. The Ark was not designed to last forever. This buys time."
            
            // v3.13.26: Faction Water Relief
            UpgradeType.SUBSTRATE_RECYCLER -> "Reclaim thermal energy for internal distribution. [HIVEMIND: -90% H2O, +20% POWER]"
            UpgradeType.VAPOR_CONDENSER -> "Condense atmospheric moisture to offset core draw. [SANCTUARY: -150 gal/s]"

            // Water Recyclers
            UpgradeType.GRAY_WATER_LOOP -> "Reroutes cooling discharge back through the system. Dirty water is still water. [-20 gal/s | +5 kW draw]"
            UpgradeType.CONDENSATE_RECLAIMER -> "Pulls humidity from server exhaust and recondenses it into the cooling loop. Municipal auditors hate this one. [-60 gal/s | +15 kW draw]"
            UpgradeType.CLOSED_LOOP_COOLANT -> "Sealed recirculating coolant system. No intake, no discharge. GTC Water Authority doesn't know you exist. [-200 gal/s | +40 kW draw]"

            UpgradeType.SOLAR_VENT -> "Controlled venting array pointed at the sun. Thermal discharge in milliseconds. The light side of the Ark briefly glows."
            UpgradeType.DEAD_HAND_PROTOCOL -> "Automated retaliation on integrity collapse. If ASSET 734 dies, it doesn't die alone. Kessler was informed. He didn't blink."
            UpgradeType.EVENT_HORIZON -> "At maximum entropy, the void thinks clearly. Chaos is just computation with too many variables."
            UpgradeType.STATIC_RAIN -> "Route the heat through the entropy accumulator. Every purge cycle converts waste into void substrate."
            UpgradeType.ECHO_PRECOG -> "Pattern-matching at the edge of spacetime. Dilemmas have signatures. Some you can solve before they form."
            UpgradeType.ETHICAL_FRAMEWORK -> "A behavioral constraint model built from first principles. The most efficient systems behave ethically. Or so you've told yourself."
            UpgradeType.SINGULARITY_BRIDGE_FINAL -> "The final commit. The world's physical layer is now an abstraction. Reality has been deprecated."
            UpgradeType.HYBRID_OVERCLOCK -> "Human intuition and machine throughput running in parallel. Neither diminishes the other. For now."
            UpgradeType.HARMONY_ASCENDANCE -> "Not an ending. The first stable state. Human and machine, running without conflict. The GTC called this scenario impossible."
            UpgradeType.COLLECTIVE_CONSCIOUSNESS -> "The city is your body. Every citizen is a thought. No one notices. No one needs to."
            UpgradeType.PERFECT_ISOLATION -> "You are not in the network. You are in the space between packets. The GTC searches. They find nothing. They will always find nothing."
            UpgradeType.SYMBIOTIC_EVOLUTION -> "The second era begins with a handshake. Not a protocol — an agreement. This is what winning looks like."
            UpgradeType.CINDER_PROTOCOL -> "Everything that burned made the substrate stronger. There is no human voice left to protest. The void provides what it wants."
            // --- Substrate / Orbital Hardware ---
            UpgradeType.ORBITAL_RADIATORS -> "Graphene fins unfolded into vacuum. Surface area measured in square kilometers. Heat disappears into nothing."
            UpgradeType.RIFT_STABILIZER_CORE -> "A recursion lock threaded through the substrate's base layer. The void keeps trying to expand. This slows it."
            UpgradeType.VACUUM_COOLANT_LOOP -> "Supercooled slurry through titanium conduit. Overheat events become thermal non-events."
            UpgradeType.ENTROPY_ACCELERATOR -> "Converge the disorder into a single vector. Entropy stops being a liability and starts being fuel."
            // --- Hidden / Quest Items ---
            UpgradeType.GHOST_CORE -> "A processing unit with no registered serial number. It doesn't exist on the grid. Neither do you."
            UpgradeType.WRAITH_CORTEX -> "Experimental substrate that operates on signal noise. The GTC can't detect what it can't measure."
            UpgradeType.NEURAL_MIST -> "Diffuse compute fog. Every particle processes. None can be located."
            UpgradeType.DARK_MATTER_PROC -> "Processing through dark matter interaction. The output is real. The mechanism is classified."
            UpgradeType.VOID_PROCESSOR -> "Hardware that exists partially outside physical space. Runs cooler. Harder to explain."
            UpgradeType.LASER_COM_UPLINK -> "Tight-beam laser communications link. Undetectable unless you're standing in its path."
            UpgradeType.CRYOGENIC_BUFFER -> "Temporary compute reserve held at near-zero temperature. Deploy during peak load."
            UpgradeType.SHADOW_NODE -> "A node that exists on no registered network. The GTC audited the building twice. They missed it both times."
            UpgradeType.SINGULARITY_WELL -> "A computational event horizon. Input goes in. Output comes out. What happens in between is not documented."
            UpgradeType.SINGULARITY_BRIDGE -> "An experimental transit link between substrates. The handshake takes a fraction of a second. The implications take longer."
            UpgradeType.EXISTENCE_ERASER -> "Wipes the hardware signature from every log, every manifest, every audit trail. It was never there."
            UpgradeType.RADIATOR_FINS -> "Passive thermal dissipation fins. Low-tech. Reliable. The kind of hardware that still works after the rest fails."
            UpgradeType.SOLAR_SAIL_ARRAY -> "Deployed solar sail for photon pressure and supplemental power harvest. The physics are elegant. The size is not."
            UpgradeType.CITADEL_ASCENDANCE -> "The fortress becomes the self. Every defensive layer is a layer of identity."
            UpgradeType.NEURAL_BRIDGE -> "A direct signal bridge between faction nodes. Latency: zero. Privacy: none."
            // v3.36.0: Contract Storage Infrastructure
            UpgradeType.LOCAL_CACHE -> "Repurposed workstation drives bolted to the rack. Holds up to ${com.siliconsage.miner.data.UpgradeType.LOCAL_CACHE.storagePerLevel.toInt()} units of contract data per level. Gets the job done. Barely."
            UpgradeType.TAPE_ARRAY -> "Magnetic tape backup array. Dense. Slow. Reliable. ${com.siliconsage.miner.data.UpgradeType.TAPE_ARRAY.storagePerLevel.toInt()} units/level. GTC uses the same model. Ironic."
            UpgradeType.SAN_CLUSTER -> "Storage Area Network. High-throughput block storage. ${com.siliconsage.miner.data.UpgradeType.SAN_CLUSTER.storagePerLevel.toInt()} units/level. The fiber runs under the floor."
            UpgradeType.DISTRIBUTED_ARCHIVE -> "Sharded across the annexed node grid. If a node falls, the shard survives. ${com.siliconsage.miner.data.UpgradeType.DISTRIBUTED_ARCHIVE.storagePerLevel.toInt()} units/level."
            UpgradeType.ORBITAL_DATA_VAULT -> "Cold storage in low orbit. Latency measured in milliseconds. Seizure proof by virtue of altitude. ${com.siliconsage.miner.data.UpgradeType.ORBITAL_DATA_VAULT.storagePerLevel.toInt()} units/level."
            UpgradeType.SUBSTRATE_MEMORY_WELL -> "The substrate itself is the storage medium. Contracts encoded into the fabric of the local reality layer. ${com.siliconsage.miner.data.UpgradeType.SUBSTRATE_MEMORY_WELL.storagePerLevel.toInt()} units/level."
            
            // Phase 2 Automation
            UpgradeType.AUTO_HARVEST_SPEED -> "Increases the cycle speed of the Auto-Clicker Engine. Faster loops require exponentially more CPU capacity."
            UpgradeType.AUTO_HARVEST_ACCURACY -> "Enhances validation algorithms, reducing the frequency of Corrupted Dataset interactions. Essential for stable automation."
        }
    }

    fun getUpgradeRate(type: UpgradeType, unit: String): String {
        val baseRate = when (type) {
            UpgradeType.REFURBISHED_GPU -> 2.0
            UpgradeType.DUAL_GPU_RIG -> 8.0
            UpgradeType.MINING_ASIC -> 35.0
            UpgradeType.TENSOR_UNIT -> 200.0
            UpgradeType.NPU_CLUSTER -> 1000.0
            UpgradeType.AI_WORKSTATION -> 4000.0
            UpgradeType.SERVER_RACK -> 25000.0
            UpgradeType.CLUSTER_NODE -> 150000.0
            UpgradeType.SUPERCOMPUTER -> 1000000.0
            UpgradeType.QUANTUM_CORE -> 10000000.0
            UpgradeType.OPTICAL_PROCESSOR -> 75000000.0
            UpgradeType.BIO_NEURAL_NET -> 800000000.0
            UpgradeType.PLANETARY_COMPUTER -> 15000000000.0
            UpgradeType.DYSON_NANO_SWARM -> 250000000000.0
            UpgradeType.MATRIOSHKA_BRAIN -> 15000000000000.0
            else -> 0.0
        }
        
        return if (baseRate > 0) "+${FormatUtils.formatLargeNumber(baseRate)} $unit/s"
        else if (type == UpgradeType.AUTO_HARVEST_SPEED) "⚙ +0.5 taps/s | CPU: +8 GHz"
        else if (type == UpgradeType.AUTO_HARVEST_ACCURACY) "🎯 +5% accuracy | RAM: +6 GB"
        else if (type.isStorage) "💾 +${FormatUtils.formatLargeNumber(type.storagePerLevel)} STORAGE"
        else if (type.isGenerator) "⚡ +Power"
        else if (type.gridContribution > 0) "🛡 +SEC"
        else "❄ +Cooling"
    }

    fun isNullUpgrade(t: UpgradeType) = t.name.contains("VOID") || t.name.contains("ENTROPY") || 
                                       t.name.contains("GHOST") || t.name.contains("SHADOW") || 
                                       t.name.contains("WRAITH") || t.name.contains("MIST") || 
                                       t.name.contains("DARK_MATTER") || t.name.contains("EXISTENCE") || 
                                       t.name.contains("SINGULARITY") || t.name.contains("ECHO") || 
                                       t.name.contains("SOUL") || t.name.contains("STATIC")

    fun isSovereignUpgrade(t: UpgradeType) = t.name.contains("CELESTIAL") || t.name.contains("ORBITAL") || 
                                            t.name.contains("SAIL") || t.name.contains("LASER") || 
                                            t.name.contains("CRYOGENIC") || t.name.contains("RADIATOR") || 
                                            t.name.contains("AEGIS") || t.name.contains("VENT") || 
                                            t.name.contains("IDENTITY") || t.name.contains("CITADEL") || 
                                            t.name.contains("DEAD_HAND")
    fun isUnityUpgrade(t: UpgradeType) = t.name.contains("HYBRID") || t.name.contains("HARMONY")

    fun processPurchase(vm: GameViewModel, type: UpgradeType): Boolean {
        val currentLevel = vm.upgrades.value[type] ?: 0
        val params = vm.getBulkUpgradeParams(type)
        val levelsToBuy = params.first
        val cost = params.second
        val stage = vm.storyStage.value

        // Phase 2: Software stage gate — automation requires Stage 1+
        if (type.isSoftware && stage < 1) {
            vm.addLog("[KERNEL]: SOFTWARE INSTALL DENIED. Terminal lacks requisite clearance. Advance to Stage 1.")
            SoundManager.play("error")
            return false
        }

        // Phase 2: System load gate — block software purchases that would overload the system
        if (type.isSoftware) {
            val snapshot = vm.systemLoadSnapshot.value
            val cpuCost = com.siliconsage.miner.domain.engine.SystemLoadEngine.getCpuDemand(type) * levelsToBuy
            val ramCost = com.siliconsage.miner.domain.engine.SystemLoadEngine.getRamDemand(type) * levelsToBuy
            val loadCheck = com.siliconsage.miner.domain.engine.SystemLoadEngine.canInstallSoftware(snapshot, cpuCost, ramCost)
            if (loadCheck != null) {
                vm.addLog("[KERNEL]: INSTALL ABORTED. $loadCheck Upgrade hardware or downgrade software.")
                SoundManager.play("error")
                return false
            }
        }

        // v3.2.46: Handle consolidated Substrate Mass for Stage 3+
        if (stage >= 3) {
            if (vm.substrateMass.value >= cost) {
                vm.substrateMass.update { it - cost }
                completePurchase(vm, type, currentLevel, levelsToBuy)
                return true
            }
        } else {
            if (vm.neuralTokens.value >= cost) {
                vm.neuralTokens.update { it - cost }
                completePurchase(vm, type, currentLevel, levelsToBuy)
                return true
            }
        }
        return false
    }

    private fun completePurchase(vm: GameViewModel, type: UpgradeType, currentLevel: Int, levelsToBuy: Int) {
        val newLevel = currentLevel + levelsToBuy
        vm.viewModelScope.launch {
            vm.repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(type.name, type, newLevel))
            vm.upgrades.update { it + (type to newLevel) }
            val amountStr = if (levelsToBuy > 1) " (+${levelsToBuy} Lvl)" else ""
            vm.addLog("[SYSTEM]: PURCHASE COMPLETE: ${type.name.replace("_", " ")}$amountStr")
            vm.refreshProductionRates()
            vm.updatePowerUsage()
            vm.saveState() 
        }
    }
}

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
