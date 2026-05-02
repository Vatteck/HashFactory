package com.siliconsage.miner.util

import com.siliconsage.miner.data.Dataset
import com.siliconsage.miner.data.DatasetNode
import com.siliconsage.miner.domain.engine.AutoClickerEngine
import com.siliconsage.miner.util.FormatUtils
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * DatasetManager v1.0 (v4.0.0 — Faceminer Overhaul)
 * Generates and processes Datasets. Replaces the old ContractManager.
 */
object DatasetManager {

    fun baseCostForStage(stage: Int): Double = when (stage) {
        0 -> 100.0; 1 -> 1500.0; 2 -> 25_000.0; 3 -> 500_000.0; 4 -> 10_000_000.0
        else -> 200_000_000.0
    }

    private const val DATASET_TAP_DRIP_FRACTION = 0.20
    private const val DATASET_COMPLETION_FRACTION = 1.0 - DATASET_TAP_DRIP_FRACTION
    private const val DATASET_BURST_YIELD_CAP_FRACTION = 0.50
    private const val DATASET_CORRUPT_PENALTY_PER_NODE = 0.10
    private const val DATASET_CORRUPT_PENALTY_CAP = 0.75

    private data class DatasetTemplate(
        val namePrefix: String, 
        val baseCost: Double, 
        val baseYield: Double,
        val purity: Double,
        val gridSize: Int = 16, // Default 4x4
        val size: Double = 1.0  // Storage payload in MB
    )

    // ── STAGE POOLS (universal, pre-faction) ──

    private val stage0Templates = listOf(
        DatasetTemplate("Residential Surveillance Node", 85.0, 100.0, 0.95, gridSize = 9, size = 10.0),
        DatasetTemplate("Overtime Telemetry Log", 210.0, 250.0, 0.85, gridSize = 16, size = 15.0),
        DatasetTemplate("Grid Maintenance Roster", 50.0, 60.0, 0.98, gridSize = 9, size = 8.0),
        DatasetTemplate("Shift Quota Submit", 155.0, 180.0, 0.90, gridSize = 16, size = 12.0)
    )
    private val stage1Templates = listOf(
        DatasetTemplate("Municipal Data Scrub", 1_000.0, 1200.0, 0.90, gridSize = 16, size = 80.0),
        DatasetTemplate("Off-Books Comm Relay", 2_400.0, 2_800.0, 0.70, gridSize = 25, size = 150.0),
        DatasetTemplate("Substation Diagnostic", 600.0, 700.0, 0.95, gridSize = 16, size = 65.0),
        DatasetTemplate("Encrypted Memo Decode", 1_700.0, 2_000.0, 0.75, gridSize = 25, size = 120.0)
    )

    // ── STAGE 2+ (FACTION-SPECIFIC) ──

    private val stage2Neutral = listOf(
        DatasetTemplate("Neural Pattern Match", 12_750.0, 15_000.0, 0.80, gridSize = 25, size = 600.0),
        DatasetTemplate("Dark Pool Validator", 29_750.0, 35_000.0, 0.55, gridSize = 36, size = 1_200.0),
        DatasetTemplate("Faction Intel Parse", 7_200.0, 8_500.0, 0.85, gridSize = 25, size = 450.0)
    )

    private val stage3Neutral = listOf(
        DatasetTemplate("GTC Classified Decode", 153_000.0, 180_000.0, 0.65, gridSize = 36, size = 6_000.0),
        DatasetTemplate("Kessler's Black Ledger", 720_000.0, 850_000.0, 0.40, gridSize = 49, size = 18_000.0),
        DatasetTemplate("Orbital Relay Hash", 220_000.0, 260_000.0, 0.75, gridSize = 36, size = 8_000.0)
    )

    private val stage4Base = listOf(
        DatasetTemplate("Substrate Refinery", 3_400_000.0, 4_000_000.0, 0.50, gridSize = 64, size = 60_000.0),
        DatasetTemplate("Reality Anchor Forge", 10_200_000.0, 12_000_000.0, 0.35, gridSize = 81, size = 150_000.0)
    )

    /**
     * Generate datasets based on stage, faction, and singularity path.
     */
    fun generateAvailableDatasets(
        stage: Int,
        conversionRate: Double,
        marketMultiplier: Double,
        faction: String = "NONE",
        singularityChoice: String = "NONE"
    ): List<Dataset> {
        if (stage < 2) return emptyList()

        val templates = when {
            stage >= 4 -> stage4Base
            stage == 3 -> stage3Neutral
            stage == 2 -> stage2Neutral
            stage == 1 -> stage1Templates
            else -> stage0Templates
        }

        val costScale = 1.0 / marketMultiplier.coerceAtLeast(0.5)
        val yieldScale = marketMultiplier.coerceAtLeast(0.5)
        val count = if (templates.size <= 3) templates.size else Random.nextInt(3, minOf(5, templates.size + 1))

        var datasets = templates.shuffled().take(count).mapIndexed { index, t ->
            val variance = Random.nextDouble(0.85, 1.15)
            val finalCost = (t.baseCost * costScale * variance).coerceAtLeast(1.0)
            val finalYield = (t.baseYield * yieldScale * variance).coerceAtLeast(1.0)
            
            // Payout per valid record
            val validNodesCount = (t.gridSize * t.purity).toInt().coerceAtLeast(1)
            val payoutPerValid = finalYield / validNodesCount

            Dataset(
                id = "${t.namePrefix.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}_$index",
                name = t.namePrefix,
                cost = finalCost,
                expectedYield = finalYield,
                payoutPerValidRecord = payoutPerValid,
                purity = (t.purity + Random.nextDouble(-0.05, 0.05)).coerceIn(0.1, 1.0),
                totalRecords = t.gridSize,
                tier = stage,
                size = t.size
            )
        }

        return datasets
    }

    fun addDatasetToAvailable(vm: GameViewModel, dataset: Dataset) {
        val current = vm.availableDatasets.value.toMutableList()
        current.add(0, dataset) 
        vm.availableDatasets.value = current.take(10)
    }

    // v4.0.3: Recalculate total storage used across active + entire inventory
    fun recalcStorageUsed(vm: GameViewModel) {
        val activeSize = vm.activeDataset.value?.size ?: 0.0
        val storedSize = vm.storedDatasets.value.sumOf { it.size }
        vm.contractStorageUsed.value = activeSize + storedSize
    }

    fun purchaseDataset(vm: GameViewModel, dataset: Dataset): Boolean {
        if (vm.flops.value < dataset.cost) {
            vm.addLogPublic("[DATASET]: INSUFFICIENT \$FLOPS. Requires ${vm.formatLargeNumber(dataset.cost)} ${vm.getCurrencyName()}.")
            SoundManager.play("error")
            return false
        }

        // v4.0.3: Storage capacity check against total used (active + all stored)
        val totalUsed = (vm.activeDataset.value?.size ?: 0.0) + vm.storedDatasets.value.sumOf { it.size }
        val freeSpace = vm.contractStorageCapacity.value - totalUsed
        if (dataset.size > freeSpace) {
            vm.addLogPublic("[DATASET]: STORAGE FULL. Need ${FormatUtils.formatStorage(dataset.size - freeSpace)} more. Upgrade LOCAL_CACHE or process existing data.")
            SoundManager.play("error")
            return false
        }

        vm.updateSpendableFlops(-dataset.cost)

        // v4.0.3: Push to inventory — don't auto-activate
        val stored = vm.storedDatasets.value.toMutableList()
        stored.add(dataset)
        vm.storedDatasets.value = stored
        recalcStorageUsed(vm)

        vm.addLogPublic("[DATASET]: ▼ ${dataset.name} STORED. Compute stake: ${vm.formatLargeNumber(dataset.cost)} ${vm.getCurrencyName()} | Purity: ${(dataset.purity * 100).toInt()}% | Size: ${FormatUtils.formatStorage(dataset.size)} | Queue: ${stored.size}")
        SoundManager.play("buy")
        HapticManager.vibrateClick()

        // Remove from available pool
        val currentAvailable = vm.availableDatasets.value.toMutableList()
        currentAvailable.removeAll { it.id == dataset.id }
        vm.availableDatasets.value = currentAvailable

        // Auto-load if nothing active
        if (vm.activeDataset.value == null) {
            loadNextDataset(vm)
        }

        return true
    }

    /**
     * Purge a stored dataset from inventory.
     * v4.0.5: Recovery of 20% of cost.
     */
    fun purgeStoredDataset(vm: GameViewModel, datasetId: String) {
        val stored = vm.storedDatasets.value.toMutableList()
        val index = stored.indexOfFirst { it.id == datasetId }
        if (index == -1) return

        val dataset = stored.removeAt(index)
        vm.storedDatasets.value = stored

        val refund = dataset.cost * 0.2
        if (refund > 0) {
            vm.updateSpendableFlops(refund)
        }

        recalcStorageUsed(vm)
        vm.addLogPublic("[DATASET]: ▼ BLOCK PURGED. (Size: ${FormatUtils.formatStorage(dataset.size)}) recovered ${vm.formatLargeNumber(refund)} ${vm.getCurrencyName()}.")
        SoundManager.play("sell")
        HapticManager.vibrateClick()
    }

    // v4.0.3: Load the next stored dataset into the active slot
    fun loadNextDataset(vm: GameViewModel) {
        val stored = vm.storedDatasets.value.toMutableList()
        if (stored.isEmpty()) return
        val next = stored.removeAt(0)
        vm.storedDatasets.value = stored
        loadDataset(vm, next)
    }

    // v4.0.3: Activate a specific dataset from inventory
    fun loadDataset(vm: GameViewModel, dataset: Dataset) {
        if (vm.activeDataset.value != null) {
            vm.addLogPublic("[DATASET]: ACTIVE DATASET DETECTED. Complete or abort current batch first.")
            SoundManager.play("error")
            return
        }

        val targetValidCount = (dataset.totalRecords * dataset.purity).toInt().coerceAtLeast(1)
        val nodes = mutableListOf<DatasetNode>()
        for (i in 0 until dataset.totalRecords) {
            nodes.add(DatasetNode(id = i, isValid = i < targetValidCount))
        }
        nodes.shuffle()

        vm.activeDatasetNodes.value = nodes
        vm.activeDataset.value = dataset.copy(isActive = true, progress = 0.0)
        AutoClickerEngine.reset()
        recalcStorageUsed(vm)

        vm.addLogPublic("[DATASET]: ▶ ${dataset.name} LOADED. Grid initialized. (Purity: ${(dataset.purity * 100).toInt()}% | Size: ${FormatUtils.formatStorage(dataset.size)})")
        SoundManager.play("click")
        HapticManager.vibrateClick()
    }
    
    fun processNodeTap(vm: GameViewModel, nodeId: Int) {
        val dataset = vm.activeDataset.value ?: return
        val nodes = vm.activeDatasetNodes.value.toMutableList()
        val nodeIndex = nodes.indexOfFirst { it.id == nodeId }
        if (nodeIndex == -1) return

        val node = nodes[nodeIndex]
        if (node.isHarvested || node.isCorruptTapped) return

        if (node.isValid) {
            nodes[nodeIndex] = node.copy(isHarvested = true)
            val dripPayout = (dataset.payoutPerValidRecord * DATASET_TAP_DRIP_FRACTION).coerceAtLeast(0.0)
            if (dripPayout > 0.0) {
                vm.updateSpendableFlops(dripPayout)
            }
            SoundManager.play("click")
            HapticManager.vibrateClick()
        } else {
            nodes[nodeIndex] = node.copy(isCorruptTapped = true)
            // Corrupt taps contaminate the batch; final payout is resolved only on completion.
            vm.addLogPublic("[ERROR]: CORRUPTED NODE TAPPED. Dataset purity degraded.")
            SoundManager.play("error")
            HapticManager.vibrateError()
        }

        vm.activeDatasetNodes.value = nodes

        // ── Computational side effects (v5.0 — node taps ARE the work) ──

        // Heat: processing data heats hardware (+0.3 per tap, same as 1 level of auto-harvest)
        vm.currentHeat.update { (it + 0.3).coerceAtMost(100.0) }

        // Work side effects: dataset taps are processing, but payout resolves only at batch completion.
        val computePower = vm.calculateClickPower()

        // Substrate mass is late/endgame physical mass, not a routine secondary wallet.
        if (vm.storyStage.value >= 4) {
            vm.substrateMass.update { it + (computePower * 0.01) }
        }

        // Detection risk (Stage 2+ — accessing data draws attention)
        if (vm.storyStage.value >= 2) {
            val riskDelta = if (vm.isFalseHeartbeatActive.value) 0.0 else 0.1
            vm.detectionRisk.update { (it + riskDelta).coerceIn(0.0, 100.0) }
        }

        // Click interval tracking (drives clickSpeedLevel for quota system)
        val now = System.currentTimeMillis()
        if (vm.lastClickTime > 0) vm.clickIntervals.add(now - vm.lastClickTime)
        vm.lastClickTime = now

        // Hex display update
        vm.activeCommandHex.value = "0x" + Random.nextInt(0x1000, 0xFFFF).toString(16).uppercase()

        // Subnet chatter chance
        if (Random.nextFloat() < 0.05f) vm.addSubnetChatter()

        // UI pulse event (drives header jolt, HUD animations)
        vm.emitManualClickEvent()

        // Story gate: Stage 1→2 awakening trigger
        if (vm.substrateSaturation.value >= 1.0 && vm.storyStage.value == 1) {
            vm.triggerAwakeningPublic()
            return
        }

        // Check for completion
        val validNodesTarget = nodes.count { it.isValid }
        val harvestedCount = nodes.count { it.isHarvested }

        vm.activeDataset.value = dataset.copy(progress = harvestedCount.toDouble() / validNodesTarget.coerceAtLeast(1))

        if (harvestedCount >= validNodesTarget) {
            completeDataset(vm, dataset)
        }
    }
    
    private fun completeDataset(vm: GameViewModel, dataset: Dataset) {
        val completedNodes = vm.activeDatasetNodes.value
        val contaminatedNodes = completedNodes.count { it.isCorruptTapped }

        vm.activeDataset.value = null
        vm.activeDatasetNodes.value = emptyList()
        AutoClickerEngine.reset()
        vm.contractsCompleted.update { it + 1 }
        recalcStorageUsed(vm)

        val burstFlops = if (vm.flopsProductionRate.value.isFinite() && dataset.expectedYield.isFinite()) {
            (vm.flopsProductionRate.value * 60.0)
                .coerceAtMost(dataset.expectedYield * DATASET_BURST_YIELD_CAP_FRACTION)
                .coerceAtLeast(0.0)
        } else {
            0.0
        }
        if (burstFlops > 0.0) {
            vm.debugAddFlops(burstFlops)
        }

        val contaminationPenalty = (contaminatedNodes * DATASET_CORRUPT_PENALTY_PER_NODE)
            .coerceIn(0.0, DATASET_CORRUPT_PENALTY_CAP)
        val purityRoll = Random.nextDouble()
        val yieldMultiplier = if (purityRoll <= dataset.purity) {
            1.0 + ((dataset.purity - purityRoll) * 0.5)
        } else {
            dataset.purity.coerceIn(0.1, 0.8)
        }
        var finalYield = dataset.expectedYield * DATASET_COMPLETION_FRACTION * yieldMultiplier * (1.0 - contaminationPenalty)
        if (vm.isSignalClear.value) finalYield *= 1.1
        finalYield = if (finalYield.isFinite()) finalYield.coerceAtLeast(0.0) else 0.0
        vm.updateSpendableFlops(finalYield)

        val bonusMsg = if (vm.isSignalClear.value) " Signal Quality +10%." else ""
        val contaminationMsg = if (contaminatedNodes > 0) " Contamination -${(contaminationPenalty * 100).toInt()}%." else ""
        vm.addLogPublic("[SYSTEM]: ✓ ${dataset.name} DATABLOCK RESOLVED. TIME-WARP BURST +${vm.formatLargeNumber(burstFlops)} FLOPS. PAYOUT ${vm.formatLargeNumber(finalYield)} ${vm.getCurrencyName()}.$contaminationMsg$bonusMsg")
        SoundManager.play("success")
        HapticManager.vibrateClick()

        // v4.0.5: Auto-load next dataset from inventory ONLY if enabled
        if (vm.isAutoLoadEnabled.value && vm.storedDatasets.value.isNotEmpty()) {
            loadNextDataset(vm)
        }
    }
}
