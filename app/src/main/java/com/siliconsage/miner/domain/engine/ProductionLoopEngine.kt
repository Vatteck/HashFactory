package com.siliconsage.miner.domain.engine

import com.siliconsage.miner.data.UpgradeType
import kotlin.math.floor
import kotlin.math.pow

/**
 * Pure math engine for production-loop calculations.
 *
 * This mirrors the existing compute-rate math from [ProductionEngine] without
 * mutating game state, so callers can derive assigned-work rates and ticks in a
 * deterministic, testable way.
 */
object ProductionLoopEngine {
    data class ComputeCapacitySnapshot(
        val rawComputePerSecond: Double,
        val effectiveComputePerSecond: Double
    )

    data class AssignedWorkRate(
        val estimatedFlopsPerSecond: Double,
        val packetPayout: Double,
        val packetsPerSecond: Double
    )

    data class AssignedWorkTick(
        val flopsDelta: Double,
        val nextProgress: Double,
        val completedPackets: Int
    )

    fun calculateComputeCapacity(
        upgrades: Map<UpgradeType, Int>,
        isCageActive: Boolean,
        annexedNodes: Set<String>,
        offlineNodes: Set<String>,
        shadowRelays: Set<String> = emptySet(),
        gridFlopsBonuses: Map<String, Double>,
        faction: String,
        decisionsMade: Int,
        saturation: Double = 0.0
    ): ComputeCapacitySnapshot {
        var baseFlops = 0.0

        // 1. Local Hardware
        baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.REFURBISHED_GPU, 2.0)
        baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.DUAL_GPU_RIG, 8.0)
        baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.MINING_ASIC, 35.0)
        baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.TENSOR_UNIT, 200.0)
        baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.NPU_CLUSTER, 1000.0)
        baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.AI_WORKSTATION, 4_000.0)
        baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.SERVER_RACK, 25_000.0)
        baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.CLUSTER_NODE, 150_000.0)
        baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.SUPERCOMPUTER, 1_000_000.0)
        baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.QUANTUM_CORE, 10_000_000.0)
        baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.OPTICAL_PROCESSOR, 75_000_000.0)
        baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.BIO_NEURAL_NET, 800_000_000.0)

        // 2. Grid Multiplier
        var gridMult = 1.0
        annexedNodes.forEach { nodeId ->
            if (!offlineNodes.contains(nodeId) && !shadowRelays.contains(nodeId) && (!isCageActive || nodeId == "A3")) {
                gridMult += gridFlopsBonuses[nodeId] ?: 0.0
            }
        }
        baseFlops *= gridMult

        // 3. External Hardware
        if (!isCageActive) {
            baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.PLANETARY_COMPUTER, 15_000_000_000.0)
            baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.DYSON_NANO_SWARM, 250_000_000_000.0)
            baseFlops += calculateHardwareCapacity(upgrades, UpgradeType.MATRIOSHKA_BRAIN, 15_000_000_000_000.0)
        }

        // 4. Ghost Technology
        var ghostProduction = 0.0
        ghostProduction += (upgrades[UpgradeType.GHOST_CORE] ?: 0) * 1_000_000_000_000.0

        if (!isCageActive) {
            ghostProduction += (upgrades[UpgradeType.SHADOW_NODE] ?: 0) * 50_000_000_000_000.0
            ghostProduction += (upgrades[UpgradeType.VOID_PROCESSOR] ?: 0) * 1_000_000_000_000_000.0
            ghostProduction += (upgrades[UpgradeType.WRAITH_CORTEX] ?: 0) * 50_000_000_000_000_000.0
            ghostProduction += (upgrades[UpgradeType.NEURAL_MIST] ?: 0) * 1_000_000_000_000_000_000.0
            ghostProduction += (upgrades[UpgradeType.SINGULARITY_BRIDGE] ?: 0) * 100_000_000_000_000_000_000.0
        }

        // Faction Synergy
        if (faction == "HIVEMIND") ghostProduction *= 1.5
        else if (faction == "SANCTUARY") ghostProduction *= 0.8

        var totalFlops = baseFlops + ghostProduction

        // 5. Unity Skill Multipliers
        if (upgrades[UpgradeType.ETHICAL_FRAMEWORK]?.let { it > 0 } == true) {
            val moralBoost = 1.0 + (decisionsMade * 0.02).coerceAtMost(2.0)
            totalFlops *= moralBoost
        }
        if (upgrades[UpgradeType.HYBRID_OVERCLOCK]?.let { it > 0 } == true) {
            totalFlops *= 3.0
        }

        val rawComputePerSecond = sanitizeCapacity(totalFlops)

        // Phase 23, Step 6: Saturation Stall
        val stallMultiplier = (1.0 - saturation).coerceIn(0.0, 1.0)
        val effectiveComputePerSecond = sanitizeCapacity(rawComputePerSecond * stallMultiplier)

        return ComputeCapacitySnapshot(
            rawComputePerSecond = rawComputePerSecond,
            effectiveComputePerSecond = effectiveComputePerSecond
        )
    }

    fun calculateAssignedWorkRate(
        capacity: ComputeCapacitySnapshot,
        automationLevel: Int,
        efficiencyMultiplier: Double
    ): AssignedWorkRate {
        val safeEfficiency = if (efficiencyMultiplier.isFinite() && efficiencyMultiplier >= 0.0) {
            efficiencyMultiplier
        } else {
            1.0
        }
        val utilization = (1.0 + automationLevel * 0.05).coerceIn(1.0, 2.5)
        val estimated = sanitizeCapacity(capacity.effectiveComputePerSecond * safeEfficiency * utilization)
        val packetPayout = (estimated / 10.0).coerceAtLeast(1.0)
        val packetsPerSecond = if (packetPayout > 0.0) sanitizeCapacity(estimated / packetPayout) else 0.0

        return AssignedWorkRate(
            estimatedFlopsPerSecond = estimated,
            packetPayout = packetPayout,
            packetsPerSecond = packetsPerSecond
        )
    }

    fun processAssignedWorkTick(
        currentProgress: Double,
        packetsPerSecond: Double,
        packetPayout: Double,
        tickSeconds: Double
    ): AssignedWorkTick {
        val safeCurrentProgress = if (currentProgress.isFinite()) currentProgress.coerceIn(0.0, MAX_PROGRESS) else 0.0
        val safePacketsPerSecond = if (packetsPerSecond.isFinite()) packetsPerSecond.coerceAtLeast(0.0) else 0.0
        val safePacketPayout = if (packetPayout.isFinite()) packetPayout.coerceAtLeast(0.0) else 0.0
        val safeTickSeconds = if (tickSeconds.isFinite()) tickSeconds.coerceAtLeast(0.0) else 0.0

        val totalProgress = safeCurrentProgress + (safePacketsPerSecond * safeTickSeconds)
        val completedPackets = floor(totalProgress).toInt().coerceAtLeast(0)
        val nextProgress = (totalProgress - completedPackets).coerceIn(0.0, MAX_PROGRESS)
        val flopsDelta = sanitizeCapacity(safePacketPayout * completedPackets)

        return AssignedWorkTick(
            flopsDelta = flopsDelta,
            nextProgress = nextProgress,
            completedPackets = completedPackets
        )
    }

    private fun calculateHardwareCapacity(upgrades: Map<UpgradeType, Int>, type: UpgradeType, baseCapacity: Double): Double {
        val level = upgrades[type] ?: 0
        if (level <= 0) return 0.0
        val milestoneMultiplier = 2.0.pow((level / 25).toDouble())
        return level * baseCapacity * milestoneMultiplier
    }

    /**
     * Defensive guard for production-loop outputs that may feed UI and wallet progress.
     *
     * Legacy finite FLOPS math is preserved by parity tests; non-finite and negative values are
     * intentionally normalized so assigned-work and saturation paths fail closed instead of
     * propagating NaN or infinities through game state.
     */
    private fun sanitizeCapacity(value: Double): Double {
        return when {
            value.isNaN() -> 0.0
            value == Double.POSITIVE_INFINITY -> Double.MAX_VALUE
            value == Double.NEGATIVE_INFINITY -> 0.0
            value < 0.0 -> 0.0
            else -> value
        }
    }

    private const val MAX_PROGRESS = 0.999999
}
