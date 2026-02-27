package com.siliconsage.miner.domain.engine

import com.siliconsage.miner.util.DatasetManager
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlin.random.Random

/**
 * AutoClickerEngine v1.0 (v4.0.1 — FACEMINER Pressure Loop)
 *
 * Progression: Manual → Assisted → Automated → Passive
 *
 * Tier 0: Manual only (player taps nodes)
 * Tier 1: Assisted — slow auto-tap, 60% accuracy (hits corrupt nodes sometimes)
 * Tier 2: Automated — medium speed, 85% accuracy
 * Tier 3: Passive — fast, 95% accuracy, taps while app is backgrounded
 *
 * Each tier consumes CPU and RAM (see SystemLoadEngine).
 * Speed is in "taps per second" but we tick at 100ms, so we accumulate fractional taps.
 */
object AutoClickerEngine {

    // Taps per second by tier
    private val SPEED = doubleArrayOf(0.0, 0.5, 2.0, 8.0)
    // Probability of correctly identifying a valid node (vs hitting corrupt)
    private val ACCURACY = doubleArrayOf(0.0, 0.60, 0.85, 0.95)

    // Accumulator for fractional taps (100ms tick = 0.1s)
    private var tapAccumulator = 0.0

    /**
     * Called every 100ms from the game loop.
     * Processes automatic node taps on the active dataset.
     */
    fun tick(vm: GameViewModel) {
        val tier = vm.autoClickerTier.value
        if (tier <= 0) return
        val dataset = vm.activeDataset.value ?: return
        val nodes = vm.activeDatasetNodes.value
        if (nodes.isEmpty()) return

        // System load throttle — if system is overloaded, auto-clicker stutters
        val loadSnapshot = vm.systemLoadSnapshot.value
        val speedMult = loadSnapshot.throttleMultiplier

        // Accumulate taps (0.1s per tick * speed * throttle)
        tapAccumulator += SPEED[tier.coerceIn(0, 3)] * 0.1 * speedMult

        // Process whole taps
        while (tapAccumulator >= 1.0) {
            tapAccumulator -= 1.0

            // Find unharvested nodes
            val available = nodes.filter { !it.isHarvested && !it.isCorruptTapped }
            if (available.isEmpty()) break

            val accuracy = ACCURACY[tier.coerceIn(0, 3)]

            // AI tries to pick a valid node
            val validNodes = available.filter { it.isValid }
            val corruptNodes = available.filter { !it.isValid }

            val targetNode = if (validNodes.isNotEmpty() && Random.nextDouble() < accuracy) {
                // Correct identification — pick a valid node
                validNodes.random()
            } else if (corruptNodes.isNotEmpty()) {
                // Misidentification — accidentally tap a corrupt node
                corruptNodes.random()
            } else {
                // Only valid nodes left, tap one
                validNodes.randomOrNull() ?: break
            }

            DatasetManager.processNodeTap(vm, targetNode.id)
        }
    }

    /**
     * Reset accumulator (call when dataset changes or auto-clicker is toggled)
     */
    fun reset() {
        tapAccumulator = 0.0
    }
}
