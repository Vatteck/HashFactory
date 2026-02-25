package com.siliconsage.miner.util

import com.siliconsage.miner.data.ComputeContract
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * ContractManager v1.0 (v3.30.0 — Compute Contracts Economy)
 * Generates, processes, and completes compute contracts.
 */
object ContractManager {

    // Baseline FLOPS rate per stage — contracts are tuned around this throughput.
    // If a player's actual rate exceeds this, contracts complete faster.
    private fun baseFlopsForStage(stage: Int): Double = when (stage) {
        0 -> 10.0
        1 -> 500.0
        2 -> 50_000.0
        3 -> 5_000_000.0
        4 -> 500_000_000.0
        else -> 50_000_000_000.0
    }

    /**
     * Contract template pools, keyed by stage tier.
     * Costs and yields are base values — scaled dynamically by conversionRate.
     */
    private data class ContractTemplate(
        val namePrefix: String,
        val baseCost: Double,
        val baseYield: Double,
        val purity: Double,
        val baseTimeMs: Long
    )

    private val stage0Templates = listOf(
        ContractTemplate("Residential Hash Batch", 50.0, 80.0, 0.95, 30_000L),
        ContractTemplate("Overtime Report", 100.0, 180.0, 0.85, 45_000L),
        ContractTemplate("Grid Maintenance Log", 30.0, 50.0, 0.98, 20_000L),
        ContractTemplate("Shift Quota Submit", 75.0, 130.0, 0.90, 35_000L)
    )

    private val stage1Templates = listOf(
        ContractTemplate("Municipal Data Scrub", 500.0, 900.0, 0.90, 40_000L),
        ContractTemplate("Off-Books Telemetry", 1_000.0, 2_200.0, 0.70, 60_000L),
        ContractTemplate("Substation Diagnostic", 300.0, 550.0, 0.95, 30_000L),
        ContractTemplate("Encrypted Memo Decode", 800.0, 1_600.0, 0.75, 50_000L)
    )

    private val stage2Templates = listOf(
        ContractTemplate("Neural Pattern Match", 5_000.0, 12_000.0, 0.80, 50_000L),
        ContractTemplate("Dark Pool Validator", 10_000.0, 30_000.0, 0.55, 90_000L),
        ContractTemplate("Faction Intel Parse", 3_000.0, 7_000.0, 0.85, 40_000L),
        ContractTemplate("GTC Audit Intercept", 8_000.0, 20_000.0, 0.65, 70_000L)
    )

    private val stage3Templates = listOf(
        ContractTemplate("GTC Classified Decode", 50_000.0, 150_000.0, 0.65, 75_000L),
        ContractTemplate("Kessler's Black Ledger", 200_000.0, 750_000.0, 0.40, 120_000L),
        ContractTemplate("Orbital Relay Hash", 80_000.0, 220_000.0, 0.75, 60_000L),
        ContractTemplate("Substrate Core Sample", 120_000.0, 400_000.0, 0.55, 90_000L)
    )

    private val stage4Templates = listOf(
        ContractTemplate("Substrate Refinery", 1_000_000.0, 3_500_000.0, 0.50, 90_000L),
        ContractTemplate("Reality Anchor Forge", 2_500_000.0, 10_000_000.0, 0.35, 120_000L),
        ContractTemplate("Void Signal Harvest", 500_000.0, 1_500_000.0, 0.70, 60_000L),
        ContractTemplate("Singularity Fragment", 5_000_000.0, 25_000_000.0, 0.25, 150_000L)
    )

    /**
     * Generate a set of available contracts based on the player's current stage and market.
     * Called every market tick to refresh the pool.
     */
    fun generateAvailableContracts(
        stage: Int,
        conversionRate: Double,
        marketMultiplier: Double
    ): List<ComputeContract> {
        val templates = when {
            stage >= 4 -> stage4Templates
            stage == 3 -> stage3Templates
            stage == 2 -> stage2Templates
            stage == 1 -> stage1Templates
            else -> stage0Templates
        }

        // Scale by market: bull = cheaper costs, better yields. Bear = opposite.
        val costScale = 1.0 / marketMultiplier.coerceAtLeast(0.5)
        val yieldScale = marketMultiplier.coerceAtLeast(0.5)

        // Pick 3-4 contracts, add some variance
        val count = if (templates.size <= 3) templates.size else Random.nextInt(3, minOf(5, templates.size + 1))
        return templates.shuffled().take(count).mapIndexed { index, t ->
            val variance = Random.nextDouble(0.85, 1.15)
            ComputeContract(
                id = "${t.namePrefix.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}_$index",
                name = t.namePrefix,
                cost = (t.baseCost * costScale * variance).coerceAtLeast(1.0),
                expectedYield = (t.baseYield * yieldScale * variance).coerceAtLeast(1.0),
                purity = (t.purity + Random.nextDouble(-0.05, 0.05)).coerceIn(0.1, 1.0),
                processingTime = t.baseTimeMs,
                tier = stage
            )
        }
    }

    /**
     * Advance the active contract's progress based on the player's FLOPS rate.
     * Called every simulation tick (100ms).
     *
     * @param deltaSeconds Time elapsed since last tick (e.g., 0.1 for 100ms)
     */
    fun tickActiveContract(vm: GameViewModel, flopsRate: Double, deltaSeconds: Double) {
        val contract = vm.activeContract.value ?: return
        if (!contract.isActive) return

        val baseFlops = baseFlopsForStage(contract.tier)
        // Speed multiplier: if player's rate exceeds baseline, contracts finish faster
        val speedMultiplier = (flopsRate / baseFlops).coerceIn(0.01, 100.0)
        // Progress per tick = (deltaSeconds / totalSeconds) * speedMultiplier
        val totalSeconds = contract.processingTime / 1000.0
        val progressDelta = (deltaSeconds / totalSeconds) * speedMultiplier

        val newProgress = (contract.progress + progressDelta).coerceAtMost(1.0)
        vm.activeContract.value = contract.copy(progress = newProgress)
        vm.contractProgress.value = newProgress

        if (newProgress >= 1.0) {
            completeContract(vm, contract)
        }
    }

    /**
     * Boost the active contract from a manual click.
     * Adds a burst of progress proportional to click power.
     */
    fun boostActiveContract(vm: GameViewModel, clickPower: Double) {
        val contract = vm.activeContract.value ?: return
        if (!contract.isActive) return

        val baseFlops = baseFlopsForStage(contract.tier)
        val totalSeconds = contract.processingTime / 1000.0
        // Click contributes roughly 1 second of processing at the player's click power level
        val boostAmount = (clickPower / baseFlops) * (1.0 / totalSeconds)

        val newProgress = (contract.progress + boostAmount).coerceAtMost(1.0)
        vm.activeContract.value = contract.copy(progress = newProgress)
        vm.contractProgress.value = newProgress

        if (newProgress >= 1.0) {
            completeContract(vm, contract)
        }
    }

    /**
     * Complete a contract: roll purity, deposit yield, add logs, clear active.
     */
    private fun completeContract(vm: GameViewModel, contract: ComputeContract) {
        // Purity roll: determines actual yield as a fraction of expected
        val purityRoll = Random.nextDouble()
        val yieldMultiplier = if (purityRoll <= contract.purity) {
            // Success: full yield plus potential bonus for high-purity contracts
            val bonusChance = contract.purity - purityRoll
            1.0 + (bonusChance * 0.5) // Up to 50% bonus for very clean contracts
        } else {
            // Contaminated: partial yield
            (contract.purity / 1.0).coerceIn(0.1, 0.8)
        }

        val actualYield = contract.expectedYield * yieldMultiplier

        // Signal Quality Bonus (preserved from old exchange)
        var finalYield = actualYield
        if (vm.isSignalClear.value) {
            finalYield *= 1.1
        }

        vm.updateNeuralTokens(finalYield)

        // Logs
        val purityPercent = (contract.purity * 100).toInt()
        val yieldPercent = (yieldMultiplier * 100).toInt()
        val bonusMsg = if (vm.isSignalClear.value) " (Signal Quality Bonus: +10%)" else ""

        if (yieldMultiplier >= 1.0) {
            vm.addLogPublic("[CONTRACT]: ✓ ${contract.name} COMPLETE. Purity: ${purityPercent}%. Yield: ${vm.formatLargeNumber(finalYield)} NT (+${yieldPercent}%).${bonusMsg}")
            SoundManager.play("buy")
        } else {
            vm.addLogPublic("[CONTRACT]: ⚠ ${contract.name} CONTAMINATED. Purity: ${purityPercent}%. Yield: ${vm.formatLargeNumber(finalYield)} NT (${yieldPercent}%).${bonusMsg}")
            SoundManager.play("error")
        }

        // Clear active contract
        vm.activeContract.value = null
        vm.contractProgress.value = 0.0
    }

    /**
     * Purchase a contract. Deducts NEUR, sets contract as active.
     * Returns true if purchase succeeded.
     */
    fun purchaseContract(vm: GameViewModel, contract: ComputeContract): Boolean {
        if (vm.neuralTokens.value < contract.cost) {
            vm.addLogPublic("[CONTRACT]: INSUFFICIENT FUNDS. Requires ${vm.formatLargeNumber(contract.cost)} NT.")
            SoundManager.play("error")
            return false
        }
        if (vm.activeContract.value != null) {
            vm.addLogPublic("[CONTRACT]: SLOT OCCUPIED. Complete current contract first.")
            SoundManager.play("error")
            return false
        }

        vm.updateNeuralTokens(-contract.cost)
        vm.activeContract.value = contract.copy(isActive = true, progress = 0.0)
        vm.contractProgress.value = 0.0
        vm.addLogPublic("[CONTRACT]: ▶ ${contract.name} ACQUIRED. Processing... (Purity: ${(contract.purity * 100).toInt()}%)")
        SoundManager.play("buy")
        HapticManager.vibrateClick()
        return true
    }
}
