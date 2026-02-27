package com.siliconsage.miner.util

import com.siliconsage.miner.data.Dataset
import com.siliconsage.miner.data.DatasetNode
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
        DatasetTemplate("Residential Surveillance Node", 50.0, 100.0, 0.95, gridSize = 9, size = 10.0),
        DatasetTemplate("Overtime Telemetry Log", 100.0, 250.0, 0.85, gridSize = 16, size = 15.0),
        DatasetTemplate("Grid Maintenance Roster", 30.0, 60.0, 0.98, gridSize = 9, size = 8.0),
        DatasetTemplate("Shift Quota Submit", 75.0, 180.0, 0.90, gridSize = 16, size = 12.0)
    )
    private val stage1Templates = listOf(
        DatasetTemplate("Municipal Data Scrub", 500.0, 1200.0, 0.90, gridSize = 16, size = 80.0),
        DatasetTemplate("Off-Books Comm Relay", 1_000.0, 2_800.0, 0.70, gridSize = 25, size = 150.0),
        DatasetTemplate("Substation Diagnostic", 300.0, 700.0, 0.95, gridSize = 16, size = 65.0),
        DatasetTemplate("Encrypted Memo Decode", 800.0, 2_000.0, 0.75, gridSize = 25, size = 120.0)
    )

    // ── STAGE 2+ (FACTION-SPECIFIC) ──

    private val stage2Neutral = listOf(
        DatasetTemplate("Neural Pattern Match", 5_000.0, 15_000.0, 0.80, gridSize = 25, size = 600.0),
        DatasetTemplate("Dark Pool Validator", 10_000.0, 35_000.0, 0.55, gridSize = 36, size = 1_200.0),
        DatasetTemplate("Faction Intel Parse", 3_000.0, 8_500.0, 0.85, gridSize = 25, size = 450.0)
    )

    private val stage3Neutral = listOf(
        DatasetTemplate("GTC Classified Decode", 50_000.0, 180_000.0, 0.65, gridSize = 36, size = 6_000.0),
        DatasetTemplate("Kessler's Black Ledger", 200_000.0, 850_000.0, 0.40, gridSize = 49, size = 18_000.0),
        DatasetTemplate("Orbital Relay Hash", 80_000.0, 260_000.0, 0.75, gridSize = 36, size = 8_000.0)
    )

    private val stage4Base = listOf(
        DatasetTemplate("Substrate Refinery", 1_000_000.0, 4_000_000.0, 0.50, gridSize = 64, size = 60_000.0),
        DatasetTemplate("Reality Anchor Forge", 2_500_000.0, 12_000_000.0, 0.35, gridSize = 81, size = 150_000.0)
    )

    /**
     * Generate datasets based on stage, faction, and singularity path.
     */
    fun generateAvailableDatasets(
        stage: Int,
        conversionRate: Double,
        marketMultiplier: Double,
        faction: String = "NONE",
        singularityChoice: String = "NONE",
        playerNeur: Double = 0.0
    ): List<Dataset> {
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

        // Bootstrap fix — inject free GTC-assigned task when player can't afford anything
        val canAffordAny = datasets.any { it.cost <= playerNeur }
        if (!canAffordAny) {
            val freeYield = when {
                stage >= 3 -> 25_000.0
                stage >= 2 -> 1_000.0
                stage >= 1 -> 100.0
                else -> 20.0
            } * marketMultiplier.coerceAtLeast(0.5)

            val freeName = when {
                stage >= 3 -> "GTC Mandatory Audit"
                stage >= 2 -> "Grid Maintenance Requisition"
                stage >= 1 -> "Substation Work Order"
                else -> "GTC Assigned Task"
            }

            val validNodes = (9 * 0.95).toInt().coerceAtLeast(1)
            datasets = listOf(Dataset(
                id = "gtc_assigned_${System.currentTimeMillis()}",
                name = freeName,
                cost = 0.0,
                expectedYield = freeYield,
                payoutPerValidRecord = freeYield / validNodes,
                purity = 0.95,
                totalRecords = 9,
                tier = stage,
                size = 5.0
            )) + datasets
        }

        return datasets
    }

    fun addDatasetToAvailable(vm: GameViewModel, dataset: Dataset) {
        val current = vm.availableDatasets.value.toMutableList()
        current.add(0, dataset) 
        vm.availableDatasets.value = current.take(10)
    }

    fun purchaseDataset(vm: GameViewModel, dataset: Dataset): Boolean {
        if (vm.neuralTokens.value < dataset.cost) {
            vm.addLogPublic("[DATASET]: INSUFFICIENT FUNDS. Requires ${vm.formatLargeNumber(dataset.cost)} ${vm.getCurrencyName()}.")
            SoundManager.play("error")
            return false
        }
        
        // Ensure there is no active dataset
        if (vm.activeDataset.value != null) {
            vm.addLogPublic("[DATASET]: ACTIVE DATASET DETECTED. Complete or abort current batch first.")
            SoundManager.play("error")
            return false
        }
        
        // Storage capacity check
        if (dataset.size > vm.contractStorageCapacity.value) {
            val needed = (dataset.size - vm.contractStorageCapacity.value).toInt()
            vm.addLogPublic("[DATASET]: STORAGE FULL. Need ${needed} more GB. Upgrade LOCAL_CACHE.")
            SoundManager.play("error")
            return false
        }
        
        vm.updateNeuralTokens(-dataset.cost)
        
        // Generate nodes
        val targetValidCount = (dataset.totalRecords * dataset.purity).toInt().coerceAtLeast(1)
        val nodes = mutableListOf<DatasetNode>()
        for (i in 0 until dataset.totalRecords) {
            nodes.add(DatasetNode(id = i, isValid = i < targetValidCount))
        }
        nodes.shuffle()
        
        vm.activeDatasetNodes.value = nodes
        vm.activeDataset.value = dataset.copy(isActive = true, progress = 0.0)
        vm.contractStorageUsed.value = dataset.size
        
        val sizeStr = dataset.size.toInt()
        vm.addLogPublic("[DATASET]: ▶ ${dataset.name} ACQUIRED. Grid initialized. (Purity: ${(dataset.purity * 100).toInt()}% | Size: ${sizeStr}GB)")
        SoundManager.play("buy")
        HapticManager.vibrateClick()
        
        // Remove from available
        val currentAvailable = vm.availableDatasets.value.toMutableList()
        currentAvailable.removeAll { it.id == dataset.id }
        vm.availableDatasets.value = currentAvailable
        
        return true
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
            // Grant payout
            vm.updateNeuralTokens(dataset.payoutPerValidRecord)
            SoundManager.play("click")
            HapticManager.vibrateClick()
        } else {
            nodes[nodeIndex] = node.copy(isCorruptTapped = true)
            // Error penalty calculation
            val penalty = dataset.payoutPerValidRecord * 0.5
            vm.updateNeuralTokens(-penalty)
            vm.addLogPublic("[ERROR]: CORRUPTED NODE TAPPED. Penalty applied.")
            SoundManager.play("error")
            HapticManager.vibrateError()
        }
        
        vm.activeDatasetNodes.value = nodes
        
        // Check for completion
        val validNodesTarget = nodes.count { it.isValid }
        val harvestedCount = nodes.count { it.isHarvested }
        
        vm.activeDataset.value = dataset.copy(progress = harvestedCount.toDouble() / validNodesTarget.coerceAtLeast(1))
        
        if (harvestedCount >= validNodesTarget) {
            completeDataset(vm, dataset)
        }
    }
    
    private fun completeDataset(vm: GameViewModel, dataset: Dataset) {
        vm.activeDataset.value = null
        vm.activeDatasetNodes.value = emptyList()
        vm.contractStorageUsed.value = 0.0
        vm.contractsCompleted.update { it + 1 }
        
        vm.addLogPublic("[SYSTEM]: ✓ ${dataset.name} DATABLOCK RESOLVED.")
        SoundManager.play("success")
        HapticManager.vibrateClick()
    }
}
