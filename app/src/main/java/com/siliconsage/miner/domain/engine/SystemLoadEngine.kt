package com.siliconsage.miner.domain.engine

import com.siliconsage.miner.data.UpgradeType

/**
 * SystemLoadEngine v1.0 (v4.0.1 — FACEMINER Pressure Loop)
 *
 * Manages the CPU/RAM/Storage capacity system.
 * Software upgrades (auto-clicker, scanners) CONSUME system resources.
 * Hardware upgrades PROVIDE system resources.
 * When load exceeds capacity, the system throttles or locks out new purchases.
 *
 * The FACEMINER Rule: Every software upgrade creates hardware demand.
 * Every hardware upgrade creates thermal + power demand.
 * The only escape is the downgrade button or more income.
 */
object SystemLoadEngine {

    /**
     * System resource snapshot — consumed by UI and game logic.
     */
    data class SystemSnapshot(
        val cpuUsed: Double,
        val cpuMax: Double,
        val ramUsed: Double,
        val ramMax: Double,
        val storageUsed: Double,
        val storageMax: Double,
        val loadPercent: Double,       // Combined weighted load 0.0–1.0
        val isThrottled: Boolean,      // loadPercent > 0.8
        val isLocked: Boolean,         // loadPercent >= 1.0 (can't buy new software/datasets)
        val throttleMultiplier: Double // 1.0 at <80%, scales down to 0.15 at 100%
    )

    // ── CPU CAPACITY per hardware tier (GHz equivalent, arbitrary units) ──
    private val cpuCapacity: Map<UpgradeType, Double> = mapOf(
        UpgradeType.REFURBISHED_GPU to 2.0,
        UpgradeType.DUAL_GPU_RIG to 5.0,
        UpgradeType.MINING_ASIC to 12.0,
        UpgradeType.TENSOR_UNIT to 30.0,
        UpgradeType.NPU_CLUSTER to 75.0,
        UpgradeType.AI_WORKSTATION to 200.0,
        UpgradeType.SERVER_RACK to 500.0,
        UpgradeType.CLUSTER_NODE to 1_500.0,
        UpgradeType.SUPERCOMPUTER to 5_000.0,
        UpgradeType.QUANTUM_CORE to 20_000.0,
        UpgradeType.OPTICAL_PROCESSOR to 80_000.0,
        UpgradeType.BIO_NEURAL_NET to 300_000.0,
        UpgradeType.PLANETARY_COMPUTER to 1_500_000.0,
        UpgradeType.DYSON_NANO_SWARM to 10_000_000.0,
        UpgradeType.MATRIOSHKA_BRAIN to 100_000_000.0,
    )

    // ── RAM CAPACITY per hardware tier (GB equivalent) ──
    private val ramCapacity: Map<UpgradeType, Double> = mapOf(
        UpgradeType.REFURBISHED_GPU to 4.0,
        UpgradeType.DUAL_GPU_RIG to 8.0,
        UpgradeType.MINING_ASIC to 16.0,
        UpgradeType.TENSOR_UNIT to 32.0,
        UpgradeType.NPU_CLUSTER to 64.0,
        UpgradeType.AI_WORKSTATION to 128.0,
        UpgradeType.SERVER_RACK to 512.0,
        UpgradeType.CLUSTER_NODE to 2_048.0,
        UpgradeType.SUPERCOMPUTER to 8_192.0,
        UpgradeType.QUANTUM_CORE to 32_768.0,
        UpgradeType.OPTICAL_PROCESSOR to 131_072.0,
        UpgradeType.BIO_NEURAL_NET to 524_288.0,
        UpgradeType.PLANETARY_COMPUTER to 2_097_152.0,
        UpgradeType.DYSON_NANO_SWARM to 16_777_216.0,
        UpgradeType.MATRIOSHKA_BRAIN to 134_217_728.0,
    )

    // ── SOFTWARE CPU DEMAND (per level) ──
    // These are the "strain" costs. Automation, scanners, security, etc.
    // The FACEMINER Rule: every software level eats CPU. Stack too many → throttle → lockout.
    private val cpuDemand: Map<UpgradeType, Double> = mapOf(
        // Automation (Phase 2) — the big consumers
        UpgradeType.AUTO_HARVEST_SPEED to 8.0,
        UpgradeType.AUTO_HARVEST_ACCURACY to 5.0,
        // Efficiency upgrades have hidden CPU cost
        UpgradeType.AI_LOAD_BALANCER to 15.0,
        // Security software consumes CPU
        UpgradeType.BASIC_FIREWALL to 1.0,
        UpgradeType.IPS_SYSTEM to 5.0,
        UpgradeType.AI_SENTINEL to 25.0,
        UpgradeType.QUANTUM_ENCRYPTION to 100.0,
        UpgradeType.OFFGRID_BACKUP to 50.0,
    )

    // ── SOFTWARE RAM DEMAND (per level) ──
    private val ramDemand: Map<UpgradeType, Double> = mapOf(
        // Automation (Phase 2) — accuracy = ML models = RAM hungry
        UpgradeType.AUTO_HARVEST_SPEED to 4.0,
        UpgradeType.AUTO_HARVEST_ACCURACY to 6.0,
        UpgradeType.AI_LOAD_BALANCER to 8.0,
        UpgradeType.BASIC_FIREWALL to 2.0,
        UpgradeType.IPS_SYSTEM to 4.0,
        UpgradeType.AI_SENTINEL to 16.0,
        UpgradeType.QUANTUM_ENCRYPTION to 64.0,
        UpgradeType.OFFGRID_BACKUP to 32.0,
    )

    /**
     * Base system capacity for Stage 0 (before any hardware).
     * The player starts with a crappy terminal — just enough to run basic tasks.
     */
    private const val BASE_CPU = 1.0   // 1 GHz equivalent
    private const val BASE_RAM = 2.0   // 2 GB
    private const val BASE_STORAGE = 10.0 // 10 MB base (tiny boot disk)
    
    /**
     * Calculate the full system load snapshot.
     *
     * @param upgrades Current upgrade counts (hardware provides capacity, software demands it)
     * @param activeDatasetSize Size of currently active dataset consuming storage
     */
    fun calculateSnapshot(
        upgrades: Map<UpgradeType, Int>,
        activeDatasetSize: Double = 0.0
    ): SystemSnapshot {
        // ── CAPACITY (hardware provides) ──
        var totalCpu = BASE_CPU
        var totalRam = BASE_RAM
        var totalStorage = BASE_STORAGE

        for ((type, count) in upgrades) {
            totalCpu += (cpuCapacity[type] ?: 0.0) * count
            totalRam += (ramCapacity[type] ?: 0.0) * count
            totalStorage += type.storagePerLevel * count
        }

        // ── DEMAND (software consumes — all from demand maps, no external params) ──
        var usedCpu = 0.0
        var usedRam = 0.0

        for ((type, count) in upgrades) {
            usedCpu += (cpuDemand[type] ?: 0.0) * count
            usedRam += (ramDemand[type] ?: 0.0) * count
        }

        val usedStorage = activeDatasetSize

        // ── COMBINED LOAD (weighted average: CPU 50%, RAM 30%, Storage 20%) ──
        val cpuRatio = if (totalCpu > 0) (usedCpu / totalCpu).coerceIn(0.0, 2.0) else 0.0
        val ramRatio = if (totalRam > 0) (usedRam / totalRam).coerceIn(0.0, 2.0) else 0.0
        val storageRatio = if (totalStorage > 0) (usedStorage / totalStorage).coerceIn(0.0, 2.0) else 0.0

        val loadPercent = (cpuRatio * 0.50 + ramRatio * 0.30 + storageRatio * 0.20).coerceIn(0.0, 1.5)

        val isThrottled = loadPercent > 0.80
        val isLocked = loadPercent >= 1.0

        // Throttle multiplier: 1.0 at <=80%, linear drop to 0.15 at 100%+
        val throttleMultiplier = when {
            loadPercent <= 0.80 -> 1.0
            loadPercent >= 1.0 -> 0.15
            else -> 1.0 - ((loadPercent - 0.80) / 0.20) * 0.85 // Linear interpolation
        }

        return SystemSnapshot(
            cpuUsed = usedCpu,
            cpuMax = totalCpu,
            ramUsed = usedRam,
            ramMax = totalRam,
            storageUsed = usedStorage,
            storageMax = totalStorage,
            loadPercent = loadPercent,
            isThrottled = isThrottled,
            isLocked = isLocked,
            throttleMultiplier = throttleMultiplier.coerceIn(0.15, 1.0)
        )
    }

    /**
     * Check if a software purchase would push the system over capacity.
     * Returns a human-readable reason string, or null if the purchase is safe.
     */
    fun canInstallSoftware(
        current: SystemSnapshot,
        additionalCpu: Double,
        additionalRam: Double
    ): String? {
        val newCpuRatio = if (current.cpuMax > 0) (current.cpuUsed + additionalCpu) / current.cpuMax else 999.0
        val newRamRatio = if (current.ramMax > 0) (current.ramUsed + additionalRam) / current.ramMax else 999.0

        if (newCpuRatio > 1.0) return "CPU CAPACITY EXCEEDED. Current: ${formatPercent(current.cpuUsed / current.cpuMax)}. Need ${additionalCpu} more GHz."
        if (newRamRatio > 1.0) return "RAM CAPACITY EXCEEDED. Current: ${formatPercent(current.ramUsed / current.ramMax)}. Need ${additionalRam} more GB."
        return null
    }

    /**
     * Calculate downgrade refund (40% of base cost).
     */
    fun calculateDowngradeRefund(baseCost: Double): Double {
        return baseCost * 0.40
    }

    /** Expose CPU demand for a given upgrade type (per level). */
    fun getCpuDemand(type: UpgradeType): Double = cpuDemand[type] ?: 0.0

    /** Expose RAM demand for a given upgrade type (per level). */
    fun getRamDemand(type: UpgradeType): Double = ramDemand[type] ?: 0.0

    private fun formatPercent(ratio: Double): String {
        return "${(ratio * 100).toInt()}%"
    }
}
