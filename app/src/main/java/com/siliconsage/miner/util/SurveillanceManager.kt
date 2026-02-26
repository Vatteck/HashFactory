package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.CoreGameState
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update

object SurveillanceManager {

    /**
     * Ticks the harvest buffers for all active harvesters based on deltaSeconds.
     */
    fun tickHarvesters(vm: GameViewModel, deltaSeconds: Double) {
        val currentHarvesters = vm.activeHarvesters.value
        if (currentHarvesters.isEmpty()) return

        val buffers = vm.harvestBuffers.value.toMutableMap()
        var currentStorage = vm.currentStorageUsed.value
        val capacity = vm.storageCapacity.value
        
        // Base harvest rate per node per second
        val baseRatePerNode = 1.0

        for ((sectorId, count) in currentHarvesters) {
            val generated = count * baseRatePerNode * deltaSeconds
            val existing = buffers[sectorId] ?: 0.0
            
            // Only add if we have storage
            if (currentStorage + generated <= capacity) {
                buffers[sectorId] = (buffers[sectorId] ?: 0.0) + generated
                currentStorage += generated
            } else {
                // Storage overflow logic - Data Leak!
                val overflow = (currentStorage + generated) - capacity
                triggerDataLeakPenalty(vm, overflow)
                // Fill up to max
                val allowed = capacity - currentStorage
                if (allowed > 0) {
                    buffers[sectorId] = (buffers[sectorId] ?: 0.0) + allowed
                    currentStorage += allowed
                }
            }
        }

        vm.harvestBuffers.value = buffers
        vm.currentStorageUsed.value = currentStorage
        
        checkBundleThresholds(vm)
    }

    private fun triggerDataLeakPenalty(vm: GameViewModel, overflowAmount: Double) {
        vm.detectionRisk.update { (it + (overflowAmount * 0.1)).coerceAtMost(100.0) }
        vm.reputationScore.update { (it - (overflowAmount * 0.05)).coerceAtLeast(0.0) }
        vm.addLog("[WARNING]: DATA STORAGE OVERFLOW INCURSION. SURVEILLANCE TRACE DETECTED.")
    }

    private fun checkBundleThresholds(vm: GameViewModel) {
        // threshold to bundle into a contract is 100.0
        val threshold = 100.0
        val buffers = vm.harvestBuffers.value.toMutableMap()
        var currentStorage = vm.currentStorageUsed.value
        
        var bundlesCreated = 0
        for ((sectorId, amount) in buffers) {
            if (amount >= threshold) {
                val bundles = (amount / threshold).toInt()
                val usedAmount = bundles * threshold
                buffers[sectorId] = amount - usedAmount
                currentStorage -= usedAmount
                bundlesCreated += bundles
            }
        }

        if (bundlesCreated > 0) {
            vm.harvestBuffers.value = buffers
            vm.currentStorageUsed.value = currentStorage
            
            // Generate the contracts
            repeat(bundlesCreated) {
                val contract = generateHarvestedContract(vm.storyStage.value)
                ContractManager.addContractToAvailable(vm, contract)
            }
            vm.addLog("[SYSTEM]: COMPLETED ${bundlesCreated}x HIGH-PURITY HARVEST BUNDLES.")
        }
    }

    private fun generateHarvestedContract(storyStage: Int): com.siliconsage.miner.data.ComputeContract {
        val tier = when {
            storyStage >= 4 -> 4
            storyStage == 3 -> 3
            storyStage == 2 -> 2
            else -> 1
        }
        val baseYield = ContractManager.baseRewardForStage(tier) * 2.5 // Premium payout
        val baseFlops = ContractManager.baseFlopsForStage(tier)

        return com.siliconsage.miner.data.ComputeContract(
            id = "HARVEST_${System.currentTimeMillis()}_${(1000..9999).random()}",
            name = "RAW BIOMETRIC BUNDLE",
            cost = 0.0,
            expectedYield = baseYield,
            purity = 1.0,
            processingTime = ContractManager.getProcessingTimeForTier(tier),
            tier = tier
        )
    }
}
