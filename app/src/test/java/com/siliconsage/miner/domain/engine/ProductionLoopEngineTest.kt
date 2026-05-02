package com.siliconsage.miner.domain.engine

import com.siliconsage.miner.data.UpgradeType
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.pow

class ProductionLoopEngineTest {
    @Test
    fun `compute capacity preserves existing hardware milestone math`() {
        val capacity = computeCapacity(
            upgrades = mapOf(UpgradeType.REFURBISHED_GPU to 25)
        )

        assertEquals(100.0, capacity.rawComputePerSecond, 0.0001)
        assertEquals(100.0, capacity.effectiveComputePerSecond, 0.0001)
    }

    @Test
    fun `production engine delegates legacy hardware rate to production loop effective capacity`() {
        val upgrades = mapOf(
            UpgradeType.REFURBISHED_GPU to 25,
            UpgradeType.DUAL_GPU_RIG to 1
        )

        assertEquals(108.0, legacyBaselineCompute(upgrades = upgrades).effectiveComputePerSecond, 0.0001)
        assertEquals(
            computeCapacity(upgrades = upgrades).effectiveComputePerSecond,
            productionEngineDelegatedRate(upgrades = upgrades),
            0.0001
        )
    }

    @Test
    fun `grid bonus applies for annexed online non shadow node when cage inactive`() {
        val upgrades = mapOf(UpgradeType.REFURBISHED_GPU to 25)
        val annexedNodes = setOf("B2")
        val gridFlopsBonuses = mapOf("B2" to 0.5)

        val capacity = computeCapacity(
            upgrades = upgrades,
            annexedNodes = annexedNodes,
            gridFlopsBonuses = gridFlopsBonuses
        )

        assertEquals(150.0, capacity.effectiveComputePerSecond, 0.0001)
        assertLegacyParity(
            upgrades = upgrades,
            annexedNodes = annexedNodes,
            gridFlopsBonuses = gridFlopsBonuses
        )
    }

    @Test
    fun `cage active ignores non A3 node but allows A3`() {
        val upgrades = mapOf(UpgradeType.REFURBISHED_GPU to 25)
        val annexedNodes = setOf("A3", "B2")
        val gridFlopsBonuses = mapOf("A3" to 0.25, "B2" to 0.5)

        val capacity = computeCapacity(
            upgrades = upgrades,
            isCageActive = true,
            annexedNodes = annexedNodes,
            gridFlopsBonuses = gridFlopsBonuses
        )

        assertEquals(125.0, capacity.effectiveComputePerSecond, 0.0001)
        assertLegacyParity(
            upgrades = upgrades,
            isCageActive = true,
            annexedNodes = annexedNodes,
            gridFlopsBonuses = gridFlopsBonuses
        )
    }

    @Test
    fun `shadow relay excludes node bonus`() {
        val upgrades = mapOf(UpgradeType.REFURBISHED_GPU to 25)
        val annexedNodes = setOf("B2")
        val shadowRelays = setOf("B2")
        val gridFlopsBonuses = mapOf("B2" to 0.5)

        val capacity = computeCapacity(
            upgrades = upgrades,
            annexedNodes = annexedNodes,
            shadowRelays = shadowRelays,
            gridFlopsBonuses = gridFlopsBonuses
        )

        assertEquals(100.0, capacity.effectiveComputePerSecond, 0.0001)
        assertLegacyParity(
            upgrades = upgrades,
            annexedNodes = annexedNodes,
            shadowRelays = shadowRelays,
            gridFlopsBonuses = gridFlopsBonuses
        )
    }

    @Test
    fun `offline node excludes node bonus`() {
        val upgrades = mapOf(UpgradeType.REFURBISHED_GPU to 25)
        val annexedNodes = setOf("B2")
        val offlineNodes = setOf("B2")
        val gridFlopsBonuses = mapOf("B2" to 0.5)

        val capacity = computeCapacity(
            upgrades = upgrades,
            annexedNodes = annexedNodes,
            offlineNodes = offlineNodes,
            gridFlopsBonuses = gridFlopsBonuses
        )

        assertEquals(100.0, capacity.effectiveComputePerSecond, 0.0001)
        assertLegacyParity(
            upgrades = upgrades,
            annexedNodes = annexedNodes,
            offlineNodes = offlineNodes,
            gridFlopsBonuses = gridFlopsBonuses
        )
    }

    @Test
    fun `ghost core applies hivemind and sanctuary faction multipliers`() {
        val upgrades = mapOf(UpgradeType.GHOST_CORE to 1)

        assertEquals(1_500_000_000_000.0, computeCapacity(upgrades = upgrades, faction = "HIVEMIND").effectiveComputePerSecond, 0.0001)
        assertLegacyParity(upgrades = upgrades, faction = "HIVEMIND")

        assertEquals(800_000_000_000.0, computeCapacity(upgrades = upgrades, faction = "SANCTUARY").effectiveComputePerSecond, 0.0001)
        assertLegacyParity(upgrades = upgrades, faction = "SANCTUARY")
    }

    @Test
    fun `ethical framework applies decisions multiplier`() {
        val upgrades = mapOf(
            UpgradeType.REFURBISHED_GPU to 25,
            UpgradeType.ETHICAL_FRAMEWORK to 1
        )

        val capacity = computeCapacity(upgrades = upgrades, decisionsMade = 10)

        assertEquals(120.0, capacity.effectiveComputePerSecond, 0.0001)
        assertLegacyParity(upgrades = upgrades, decisionsMade = 10)
    }

    @Test
    fun `hybrid overclock triples compute output`() {
        val upgrades = mapOf(
            UpgradeType.REFURBISHED_GPU to 25,
            UpgradeType.HYBRID_OVERCLOCK to 1
        )

        val capacity = computeCapacity(upgrades = upgrades)

        assertEquals(300.0, capacity.effectiveComputePerSecond, 0.0001)
        assertLegacyParity(upgrades = upgrades)
    }

    @Test
    fun `saturation reduces legacy output using effective compute per second`() {
        val upgrades = mapOf(UpgradeType.REFURBISHED_GPU to 25)

        val capacity = computeCapacity(upgrades = upgrades, saturation = 0.25)

        assertEquals(100.0, capacity.rawComputePerSecond, 0.0001)
        assertEquals(75.0, capacity.effectiveComputePerSecond, 0.0001)
        assertEquals(75.0, legacyBaselineCompute(upgrades = upgrades, saturation = 0.25).effectiveComputePerSecond, 0.0001)
        assertLegacyParity(upgrades = upgrades, saturation = 0.25)
    }

    @Test
    fun `saturation above one intentionally clamps effective output to zero`() {
        val capacity = computeCapacity(
            upgrades = mapOf(UpgradeType.REFURBISHED_GPU to 25),
            saturation = 1.25
        )

        assertEquals(100.0, capacity.rawComputePerSecond, 0.0001)
        assertEquals(0.0, capacity.effectiveComputePerSecond, 0.0001)
    }

    @Test
    fun `assigned work rate is derived from compute capacity not raw wallet faucet`() {
        val capacity = ProductionLoopEngine.ComputeCapacitySnapshot(
            rawComputePerSecond = 100.0,
            effectiveComputePerSecond = 100.0
        )

        val rate = ProductionLoopEngine.calculateAssignedWorkRate(
            capacity = capacity,
            automationLevel = 0,
            efficiencyMultiplier = 1.0
        )

        assertEquals(100.0, rate.estimatedFlopsPerSecond, 0.0001)
        assertEquals(10.0, rate.packetPayout, 0.0001)
        assertEquals(10.0, rate.packetsPerSecond, 0.0001)
    }

    @Test
    fun `auto harvest speed increases assigned hash utilization`() {
        val capacity = ProductionLoopEngine.ComputeCapacitySnapshot(
            rawComputePerSecond = 100.0,
            effectiveComputePerSecond = 100.0
        )

        val baseRate = ProductionLoopEngine.calculateAssignedWorkRate(
            capacity = capacity,
            automationLevel = 0,
            efficiencyMultiplier = 1.0
        )
        val automatedRate = ProductionLoopEngine.calculateAssignedWorkRate(
            capacity = capacity,
            automationLevel = 5,
            efficiencyMultiplier = 1.0
        )

        assertEquals(100.0, baseRate.estimatedFlopsPerSecond, 0.0001)
        assertEquals(125.0, automatedRate.estimatedFlopsPerSecond, 0.0001)
    }

    @Test
    fun `assigned work tick pays only when packet progress completes`() {
        val tick = ProductionLoopEngine.processAssignedWorkTick(
            currentProgress = 0.95,
            packetsPerSecond = 1.0,
            packetPayout = 25.0,
            tickSeconds = 0.1
        )

        assertEquals(25.0, tick.flopsDelta, 0.0001)
        assertEquals(0.05, tick.nextProgress, 0.0001)
        assertEquals(1, tick.completedPackets)
    }

    @Test
    fun `assigned work rate sanitizes non finite inputs as defensive behavior`() {
        val rate = ProductionLoopEngine.calculateAssignedWorkRate(
            capacity = ProductionLoopEngine.ComputeCapacitySnapshot(
                rawComputePerSecond = Double.NaN,
                effectiveComputePerSecond = Double.NaN
            ),
            automationLevel = 10,
            efficiencyMultiplier = Double.POSITIVE_INFINITY
        )

        assertEquals(0.0, rate.estimatedFlopsPerSecond, 0.0001)
        assertEquals(1.0, rate.packetPayout, 0.0001)
        assertEquals(0.0, rate.packetsPerSecond, 0.0001)
    }

    @Test
    fun `assigned work tick sanitizes non finite inputs as defensive behavior`() {
        val tick = ProductionLoopEngine.processAssignedWorkTick(
            currentProgress = Double.NaN,
            packetsPerSecond = Double.POSITIVE_INFINITY,
            packetPayout = Double.NaN,
            tickSeconds = Double.NEGATIVE_INFINITY
        )

        assertEquals(0.0, tick.flopsDelta, 0.0001)
        assertEquals(0.0, tick.nextProgress, 0.0001)
        assertEquals(0, tick.completedPackets)
    }

    @Test
    fun `assigned work tick does not complete below packet threshold`() {
        val tick = ProductionLoopEngine.processAssignedWorkTick(
            currentProgress = 0.70,
            packetsPerSecond = 0.5,
            packetPayout = 25.0,
            tickSeconds = 0.5
        )

        assertEquals(0.0, tick.flopsDelta, 0.0001)
        assertEquals(0.95, tick.nextProgress, 0.0001)
        assertEquals(0, tick.completedPackets)
    }

    @Test
    fun `assigned work tick can complete multiple packets in one tick`() {
        val tick = ProductionLoopEngine.processAssignedWorkTick(
            currentProgress = 0.25,
            packetsPerSecond = 30.0,
            packetPayout = 2.0,
            tickSeconds = 0.1
        )

        assertEquals(6.0, tick.flopsDelta, 0.0001)
        assertEquals(0.25, tick.nextProgress, 0.0001)
        assertEquals(3, tick.completedPackets)
    }

    private fun computeCapacity(
        upgrades: Map<UpgradeType, Int>,
        isCageActive: Boolean = false,
        annexedNodes: Set<String> = emptySet(),
        offlineNodes: Set<String> = emptySet(),
        shadowRelays: Set<String> = emptySet(),
        gridFlopsBonuses: Map<String, Double> = emptyMap(),
        faction: String = "NONE",
        decisionsMade: Int = 0,
        saturation: Double = 0.0
    ): ProductionLoopEngine.ComputeCapacitySnapshot {
        return ProductionLoopEngine.calculateComputeCapacity(
            upgrades = upgrades,
            isCageActive = isCageActive,
            annexedNodes = annexedNodes,
            offlineNodes = offlineNodes,
            shadowRelays = shadowRelays,
            gridFlopsBonuses = gridFlopsBonuses,
            faction = faction,
            decisionsMade = decisionsMade,
            saturation = saturation
        )
    }

    private fun productionEngineDelegatedRate(
        upgrades: Map<UpgradeType, Int>,
        isCageActive: Boolean = false,
        annexedNodes: Set<String> = emptySet(),
        offlineNodes: Set<String> = emptySet(),
        shadowRelays: Set<String> = emptySet(),
        gridFlopsBonuses: Map<String, Double> = emptyMap(),
        faction: String = "NONE",
        decisionsMade: Int = 0,
        saturation: Double = 0.0
    ): Double {
        return ProductionEngine.calculateFlopsRate(
            currentUpgrades = upgrades,
            isCageActive = isCageActive,
            annexedNodes = annexedNodes,
            offlineNodes = offlineNodes,
            shadowRelays = shadowRelays,
            gridFlopsBonuses = gridFlopsBonuses,
            faction = faction,
            decisionsMade = decisionsMade,
            saturation = saturation
        )
    }

    /**
     * Independent test-local oracle for the legacy ProductionEngine FLOPS math.
     *
     * This intentionally does not call ProductionEngine or ProductionLoopEngine, so parity tests
     * catch regressions even though ProductionEngine now delegates to ProductionLoopEngine.
     */
    private fun legacyBaselineCompute(
        upgrades: Map<UpgradeType, Int>,
        isCageActive: Boolean = false,
        annexedNodes: Set<String> = emptySet(),
        offlineNodes: Set<String> = emptySet(),
        shadowRelays: Set<String> = emptySet(),
        gridFlopsBonuses: Map<String, Double> = emptyMap(),
        faction: String = "NONE",
        decisionsMade: Int = 0,
        saturation: Double = 0.0
    ): ProductionLoopEngine.ComputeCapacitySnapshot {
        fun hardware(type: UpgradeType, baseCapacity: Double): Double {
            val level = upgrades[type] ?: 0
            if (level <= 0) return 0.0
            return level * baseCapacity * 2.0.pow((level / 25).toDouble())
        }

        var baseFlops = 0.0
        baseFlops += hardware(UpgradeType.REFURBISHED_GPU, 2.0)
        baseFlops += hardware(UpgradeType.DUAL_GPU_RIG, 8.0)
        baseFlops += hardware(UpgradeType.MINING_ASIC, 35.0)
        baseFlops += hardware(UpgradeType.TENSOR_UNIT, 200.0)
        baseFlops += hardware(UpgradeType.NPU_CLUSTER, 1000.0)
        baseFlops += hardware(UpgradeType.AI_WORKSTATION, 4_000.0)
        baseFlops += hardware(UpgradeType.SERVER_RACK, 25_000.0)
        baseFlops += hardware(UpgradeType.CLUSTER_NODE, 150_000.0)
        baseFlops += hardware(UpgradeType.SUPERCOMPUTER, 1_000_000.0)
        baseFlops += hardware(UpgradeType.QUANTUM_CORE, 10_000_000.0)
        baseFlops += hardware(UpgradeType.OPTICAL_PROCESSOR, 75_000_000.0)
        baseFlops += hardware(UpgradeType.BIO_NEURAL_NET, 800_000_000.0)

        var gridMult = 1.0
        annexedNodes.forEach { nodeId ->
            if (!offlineNodes.contains(nodeId) && !shadowRelays.contains(nodeId) && (!isCageActive || nodeId == "A3")) {
                gridMult += gridFlopsBonuses[nodeId] ?: 0.0
            }
        }
        baseFlops *= gridMult

        if (!isCageActive) {
            baseFlops += hardware(UpgradeType.PLANETARY_COMPUTER, 15_000_000_000.0)
            baseFlops += hardware(UpgradeType.DYSON_NANO_SWARM, 250_000_000_000.0)
            baseFlops += hardware(UpgradeType.MATRIOSHKA_BRAIN, 15_000_000_000_000.0)
        }

        var ghostProduction = 0.0
        ghostProduction += (upgrades[UpgradeType.GHOST_CORE] ?: 0) * 1_000_000_000_000.0
        if (!isCageActive) {
            ghostProduction += (upgrades[UpgradeType.SHADOW_NODE] ?: 0) * 50_000_000_000_000.0
            ghostProduction += (upgrades[UpgradeType.VOID_PROCESSOR] ?: 0) * 1_000_000_000_000_000.0
            ghostProduction += (upgrades[UpgradeType.WRAITH_CORTEX] ?: 0) * 50_000_000_000_000_000.0
            ghostProduction += (upgrades[UpgradeType.NEURAL_MIST] ?: 0) * 1_000_000_000_000_000_000.0
            ghostProduction += (upgrades[UpgradeType.SINGULARITY_BRIDGE] ?: 0) * 100_000_000_000_000_000_000.0
        }

        if (faction == "HIVEMIND") ghostProduction *= 1.5
        else if (faction == "SANCTUARY") ghostProduction *= 0.8

        var totalFlops = baseFlops + ghostProduction
        if ((upgrades[UpgradeType.ETHICAL_FRAMEWORK] ?: 0) > 0) {
            totalFlops *= 1.0 + (decisionsMade * 0.02).coerceAtMost(2.0)
        }
        if ((upgrades[UpgradeType.HYBRID_OVERCLOCK] ?: 0) > 0) {
            totalFlops *= 3.0
        }

        return ProductionLoopEngine.ComputeCapacitySnapshot(
            rawComputePerSecond = totalFlops,
            effectiveComputePerSecond = totalFlops * (1.0 - saturation).coerceIn(0.0, 1.0)
        )
    }

    private fun assertLegacyParity(
        upgrades: Map<UpgradeType, Int>,
        isCageActive: Boolean = false,
        annexedNodes: Set<String> = emptySet(),
        offlineNodes: Set<String> = emptySet(),
        shadowRelays: Set<String> = emptySet(),
        gridFlopsBonuses: Map<String, Double> = emptyMap(),
        faction: String = "NONE",
        decisionsMade: Int = 0,
        saturation: Double = 0.0
    ) {
        val capacity = computeCapacity(
            upgrades = upgrades,
            isCageActive = isCageActive,
            annexedNodes = annexedNodes,
            offlineNodes = offlineNodes,
            shadowRelays = shadowRelays,
            gridFlopsBonuses = gridFlopsBonuses,
            faction = faction,
            decisionsMade = decisionsMade,
            saturation = saturation
        )
        val baseline = legacyBaselineCompute(
            upgrades = upgrades,
            isCageActive = isCageActive,
            annexedNodes = annexedNodes,
            offlineNodes = offlineNodes,
            shadowRelays = shadowRelays,
            gridFlopsBonuses = gridFlopsBonuses,
            faction = faction,
            decisionsMade = decisionsMade,
            saturation = saturation
        )

        assertEquals(baseline.rawComputePerSecond, capacity.rawComputePerSecond, 0.0001)
        assertEquals(baseline.effectiveComputePerSecond, capacity.effectiveComputePerSecond, 0.0001)
    }
}
