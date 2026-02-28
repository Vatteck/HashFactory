package com.siliconsage.miner.domain.engine

import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.util.DatasetManager
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlin.random.Random

/**
 * AutoClickerEngine v2.0 (Phase 2 Automation)
 *
 * Driven by SOFTWARE upgrades (AUTO_HARVEST_SPEED / AUTO_HARVEST_ACCURACY).
 * Bottlenecked by SystemLoadEngine — if system load is locked (>=100%), hard shutdown.
 * Throttled proportionally when load exceeds 80%.
 *
 * The Pressure Loop: more software → more system load → need more hardware →
 * more heat + power → need more cooling + generators → repeat.
 */
object AutoClickerEngine {

    // Accumulator for fractional taps (100ms tick = 0.1s)
    private var tapAccumulator = 0.0

    /**
     * Called every 100ms from the game loop.
     */
    fun tick(vm: GameViewModel) {
        val speedLevel = vm.upgrades.value[UpgradeType.AUTO_HARVEST_SPEED] ?: 0
        if (speedLevel <= 0) return

        // Power Shutoff — unpaid utility bills hard-stop all automation
        if (vm.powerBill.value > 0.0 && vm.missedBillingPeriods >= 1) return

        // SystemLoadEngine bottleneck — overloaded system = hard shutdown
        val snapshot = vm.systemLoadSnapshot.value
        if (snapshot.isLocked) return

        val dataset = vm.activeDataset.value ?: return
        val nodes = vm.activeDatasetNodes.value
        if (nodes.isEmpty()) return

        // 0.5 taps/sec per speed level, throttled by system load
        val tapsPerSecond = speedLevel * 0.5
        tapAccumulator += tapsPerSecond * 0.1 * snapshot.throttleMultiplier

        while (tapAccumulator >= 1.0) {
            tapAccumulator -= 1.0

            val available = nodes.filter { !it.isHarvested && !it.isCorruptTapped }
            if (available.isEmpty()) break

            val accuracyLevel = vm.upgrades.value[UpgradeType.AUTO_HARVEST_ACCURACY] ?: 0
            // Base 50% accuracy + 5% per level, max 99%
            val accuracy = (0.50 + (accuracyLevel * 0.05)).coerceIn(0.50, 0.99)

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
