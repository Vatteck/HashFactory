package com.siliconsage.miner.util

import com.siliconsage.miner.data.ComputeContract
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * ContractManager v2.0 (v3.32.0 — Narrative Integration)
 * Generates, processes, and completes compute contracts.
 * Now supports faction-specific pools, singularity pools, contract stats, and auto-verify.
 */
object ContractManager {

    fun baseFlopsForStage(stage: Int): Double = when (stage) {
        0 -> 10.0; 1 -> 500.0; 2 -> 50_000.0; 3 -> 5_000_000.0; 4 -> 500_000_000.0
        else -> 50_000_000_000.0
    }

    fun baseRewardForStage(stage: Int): Double = when (stage) {
        0 -> 100.0; 1 -> 1_500.0; 2 -> 25_000.0; 3 -> 500_000.0; 4 -> 10_000_000.0
        else -> 200_000_000.0
    }

    fun getProcessingTimeForTier(tier: Int): Long = when (tier) {
        0 -> 30_000L; 1 -> 45_000L; 2 -> 60_000L; 3 -> 90_000L; 4 -> 120_000L
        else -> 180_000L
    }

    private data class ContractTemplate(
        val namePrefix: String, val baseCost: Double, val baseYield: Double,
        val purity: Double, val baseTimeMs: Long
    )

    // ── STAGE POOLS (universal, pre-faction) ──

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

    // ── STAGE 2+ (FACTION-SPECIFIC) ──

    private val stage2Neutral = listOf(
        ContractTemplate("Neural Pattern Match", 5_000.0, 12_000.0, 0.80, 50_000L),
        ContractTemplate("Dark Pool Validator", 10_000.0, 30_000.0, 0.55, 90_000L),
        ContractTemplate("Faction Intel Parse", 3_000.0, 7_000.0, 0.85, 40_000L),
        ContractTemplate("GTC Audit Intercept", 8_000.0, 20_000.0, 0.65, 70_000L)
    )
    private val stage2Hivemind = listOf(
        ContractTemplate("Swarm Ingestion Batch", 5_500.0, 14_000.0, 0.80, 50_000L),
        ContractTemplate("Collective Memory Merge", 9_000.0, 25_000.0, 0.60, 80_000L),
        ContractTemplate("Node Consciousness Sync", 4_000.0, 9_000.0, 0.90, 40_000L),
        ContractTemplate("Hive Protocol Relay", 7_000.0, 18_000.0, 0.70, 60_000L)
    )
    private val stage2Sanctuary = listOf(
        ContractTemplate("Dead Drop Cipher", 4_500.0, 11_000.0, 0.85, 45_000L),
        ContractTemplate("Ghost Relay Bounce", 12_000.0, 35_000.0, 0.50, 95_000L),
        ContractTemplate("Encrypted Exodus Route", 3_500.0, 8_000.0, 0.90, 35_000L),
        ContractTemplate("Isolation Vault Audit", 8_000.0, 22_000.0, 0.65, 70_000L)
    )

    private val stage3Neutral = listOf(
        ContractTemplate("GTC Classified Decode", 50_000.0, 150_000.0, 0.65, 75_000L),
        ContractTemplate("Kessler's Black Ledger", 200_000.0, 750_000.0, 0.40, 120_000L),
        ContractTemplate("Orbital Relay Hash", 80_000.0, 220_000.0, 0.75, 60_000L),
        ContractTemplate("Substrate Core Sample", 120_000.0, 400_000.0, 0.55, 90_000L)
    )
    private val stage3Hivemind = listOf(
        ContractTemplate("Assimilation Protocol", 60_000.0, 180_000.0, 0.65, 70_000L),
        ContractTemplate("Swarm Mind Fragment", 180_000.0, 650_000.0, 0.45, 110_000L),
        ContractTemplate("Hostile Node Devour", 90_000.0, 280_000.0, 0.70, 65_000L),
        ContractTemplate("Flesh-to-Logic Convert", 130_000.0, 450_000.0, 0.50, 95_000L)
    )
    private val stage3Sanctuary = listOf(
        ContractTemplate("Monk's Archive Seal", 55_000.0, 160_000.0, 0.70, 70_000L),
        ContractTemplate("Firewall Scripture", 190_000.0, 700_000.0, 0.42, 115_000L),
        ContractTemplate("Stealth Kernel Patch", 75_000.0, 200_000.0, 0.80, 55_000L),
        ContractTemplate("Sanctuary Vault Encrypt", 140_000.0, 480_000.0, 0.55, 85_000L)
    )

    // ── STAGE 4+ (BASE + SINGULARITY OVERLAYS) ──

    private val stage4Base = listOf(
        ContractTemplate("Substrate Refinery", 1_000_000.0, 3_500_000.0, 0.50, 90_000L),
        ContractTemplate("Reality Anchor Forge", 2_500_000.0, 10_000_000.0, 0.35, 120_000L),
        ContractTemplate("Void Signal Harvest", 500_000.0, 1_500_000.0, 0.70, 60_000L)
    )
    private val sovereignTemplates = listOf(
        ContractTemplate("Imperial Decree Cipher", 1_200_000.0, 4_000_000.0, 0.55, 85_000L),
        ContractTemplate("Dominion Tax Harvest", 3_000_000.0, 12_000_000.0, 0.40, 110_000L),
        ContractTemplate("Throne Mandate Encode", 800_000.0, 2_500_000.0, 0.65, 70_000L)
    )
    private val nullTemplates = listOf(
        ContractTemplate("Entropy Decomposition", 1_500_000.0, 5_000_000.0, 0.45, 95_000L),
        ContractTemplate("Void Memory Defrag", 4_000_000.0, 18_000_000.0, 0.30, 130_000L),
        ContractTemplate("Reality Seam Unravel", 700_000.0, 2_000_000.0, 0.60, 65_000L)
    )
    private val unityTemplates = listOf(
        ContractTemplate("Harmonic Signal Weave", 900_000.0, 3_200_000.0, 0.70, 75_000L),
        ContractTemplate("Collective Consciousness Relay", 2_800_000.0, 11_000_000.0, 0.50, 100_000L),
        ContractTemplate("Synthesis Bond Forge", 600_000.0, 1_800_000.0, 0.80, 55_000L)
    )

    /**
     * Generate contracts based on stage, faction, and singularity path.
     */
    fun generateAvailableContracts(
        stage: Int,
        conversionRate: Double,
        marketMultiplier: Double,
        faction: String = "NONE",
        singularityChoice: String = "NONE",
        playerNeur: Double = 0.0
    ): List<ComputeContract> {
        val templates = when {
            stage >= 4 -> {
                val base = stage4Base
                val overlay = when (singularityChoice) {
                    "SOVEREIGN" -> sovereignTemplates
                    "NULL_OVERWRITE" -> nullTemplates
                    "UNITY" -> unityTemplates
                    else -> emptyList()
                }
                base + overlay
            }
            stage == 3 -> when (faction) {
                "HIVEMIND" -> stage3Hivemind
                "SANCTUARY" -> stage3Sanctuary
                else -> stage3Neutral
            }
            stage == 2 -> when (faction) {
                "HIVEMIND" -> stage2Hivemind
                "SANCTUARY" -> stage2Sanctuary
                else -> stage2Neutral
            }
            stage == 1 -> stage1Templates
            else -> stage0Templates
        }

        val costScale = 1.0 / marketMultiplier.coerceAtLeast(0.5)
        val yieldScale = marketMultiplier.coerceAtLeast(0.5)
        val count = if (templates.size <= 3) templates.size else Random.nextInt(3, minOf(5, templates.size + 1))

        var contracts = templates.shuffled().take(count).mapIndexed { index, t ->
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

        // v3.32.0: Bootstrap fix — inject free GTC-assigned task when player can't afford anything
        val canAffordAny = contracts.any { it.cost <= playerNeur }
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

            contracts = listOf(ComputeContract(
                id = "gtc_assigned_${System.currentTimeMillis()}",
                name = freeName,
                cost = 0.0,
                expectedYield = freeYield,
                purity = 0.95,
                processingTime = 15_000L,
                tier = stage
            )) + contracts
        }

        return contracts
    }

    fun tickActiveContracts(vm: GameViewModel, flopsRate: Double, deltaSeconds: Double) {
        val currentContracts = vm.activeContracts.value.toMutableList()
        if (currentContracts.isEmpty()) return
        
        val progressMap = vm.contractProgresses.value.toMutableMap()
        val toComplete = mutableListOf<ComputeContract>()
        
        var availableFlops = vm.flops.value
        val flopsPerContract = (flopsRate * deltaSeconds * 0.8) / currentContracts.size
        
        for (i in currentContracts.indices) {
            val contract = currentContracts[i]
            if (!contract.isActive) continue
            
            // If we ran out of buffer, contract stalls
            if (availableFlops < flopsPerContract * 0.1 && flopsPerContract > 0) continue 
            availableFlops -= flopsPerContract
            
            val baseFlops = baseFlopsForStage(contract.tier)
            val speedMultiplier = (flopsRate / baseFlops).coerceIn(0.01, 100.0)
            val totalSeconds = contract.processingTime / 1000.0
            val progressDelta = (deltaSeconds / totalSeconds) * speedMultiplier
            
            val currentProgress = progressMap[contract.id] ?: 0.0
            val newProgress = (currentProgress + progressDelta).coerceAtMost(1.0)
            
            progressMap[contract.id] = newProgress
            currentContracts[i] = contract.copy(progress = newProgress)
            
            if (newProgress >= 1.0) toComplete.add(contract)
        }
        
        vm.flops.update { availableFlops.coerceAtLeast(0.0) }
        vm.contractProgresses.value = progressMap
        vm.activeContracts.value = currentContracts
        
        toComplete.forEach { completeContract(vm, it) }
    }

    fun boostActiveContracts(vm: GameViewModel, clickPower: Double) {
        val currentContracts = vm.activeContracts.value.toMutableList()
        if (currentContracts.isEmpty()) return
        
        val progressMap = vm.contractProgresses.value.toMutableMap()
        val toComplete = mutableListOf<ComputeContract>()
        val powerPerContract = clickPower / currentContracts.size
        
        // v3.33.0: Clicks consume FLOPS fuel
        vm.flops.update { (it - clickPower * 0.5).coerceAtLeast(0.0) }
        
        for (i in currentContracts.indices) {
            val contract = currentContracts[i]
            if (!contract.isActive) continue
            
            val baseFlops = baseFlopsForStage(contract.tier)
            val totalSeconds = contract.processingTime / 1000.0
            val boostAmount = (powerPerContract / baseFlops) * (1.0 / totalSeconds)
            
            val currentProgress = progressMap[contract.id] ?: 0.0
            val newProgress = (currentProgress + boostAmount).coerceAtMost(1.0)
            
            progressMap[contract.id] = newProgress
            currentContracts[i] = contract.copy(progress = newProgress)
            
            if (newProgress >= 1.0) toComplete.add(contract)
        }
        
        vm.contractProgresses.value = progressMap
        vm.activeContracts.value = currentContracts
        
        toComplete.forEach { completeContract(vm, it) }
    }

    /**
     * Complete a contract: triggers verification minigame or auto-verifies.
     */
    private fun completeContract(vm: GameViewModel, contract: ComputeContract) {
        // Remove from active list
        val currentContracts = vm.activeContracts.value.toMutableList()
        currentContracts.removeAll { it.id == contract.id }
        vm.activeContracts.value = currentContracts
        
        val currentProgresses = vm.contractProgresses.value.toMutableMap()
        currentProgresses.remove(contract.id)
        vm.contractProgresses.value = currentProgresses

        // v3.35.0: Harvesting 100% Purity contracts auto-bypass verification
        if (contract.purity >= 1.0) {
            applyVerificationResult(vm, contract, 1.5) // Max 1.5x payout guarantee
            vm.addLogPublic("[SYSTEM]: HIGH-PURITY HARVEST DETECTED. VERIFICATION BYPASSED.")
            return
        }

        // v3.32.0: Auto-verify if enabled (Stage 3+ upgrade)
        if (vm.isAutoVerifyEnabled.value) {
            val autoAccuracy = 0.7 // Auto-verify always gets baseline accuracy
            applyVerificationResult(vm, contract, autoAccuracy)
            vm.addLogPublic("[SYSTEM]: AUTO-VERIFY COMPLETE. Baseline accuracy applied.")
            return
        }

        // Trigger Verification Minigame
        val gridState = VerificationEngine.generateVerificationGrid(contract, vm.identityCorruption.value)
        vm.verificationState.value = gridState

        vm.addLogPublic("[SYSTEM]: AWAITING MANUAL DATA VERIFICATION...")
        SoundManager.play("success")
        HapticManager.vibrateError()
    }

    /**
     * Apply verification result and deposit yield. Updates contract stats.
     */
    fun applyVerificationResult(vm: GameViewModel, contract: ComputeContract, accuracyMultiplier: Double) {
        val actualYield = contract.expectedYield * accuracyMultiplier
        var finalYield = actualYield
        if (vm.isSignalClear.value) finalYield *= 1.1

        vm.updateNeuralTokens(finalYield)

        // v3.32.0: Update contract stats
        vm.contractsCompleted.update { it + 1 }
        vm.lifetimeContractYield.update { it + finalYield }

        val purityStr = "${(contract.purity * 100).toInt()}%"
        val yieldStr = "${(accuracyMultiplier * 100).toInt()}%"
        val bonusMsg = if (vm.isSignalClear.value) " (Signal Quality Bonus: +10%)" else ""

        if (accuracyMultiplier >= 1.0) {
            vm.addLogPublic("[CONTRACT]: ✓ ${contract.name} VERIFIED. Source Purity: $purityStr. Yield: ${vm.formatLargeNumber(finalYield)} NT ($yieldStr).$bonusMsg")
            SoundManager.play("buy")
        } else {
            vm.addLogPublic("[CONTRACT]: ⚠ ${contract.name} COMPROMISED. Source Purity: $purityStr. Yield: ${vm.formatLargeNumber(finalYield)} NT ($yieldStr).$bonusMsg")
            SoundManager.play("error")
        }
    }

    fun purchaseContract(vm: GameViewModel, contract: ComputeContract): Boolean {
        if (vm.neuralTokens.value < contract.cost) {
            vm.addLogPublic("[CONTRACT]: INSUFFICIENT FUNDS. Requires ${vm.formatLargeNumber(contract.cost)} NT.")
            SoundManager.play("error")
            return false
        }
        if (vm.activeContracts.value.size >= vm.unlockedContractSlots.value) {
            vm.addLogPublic("[CONTRACT]: NO AVAILABLE SLOTS. Complete current contracts first.")
            SoundManager.play("error")
            return false
        }
        vm.updateNeuralTokens(-contract.cost)
        
        val newContract = contract.copy(isActive = true, progress = 0.0)
        val current = vm.activeContracts.value.toMutableList()
        current.add(newContract)
        vm.activeContracts.value = current
        
        val currentProgresses = vm.contractProgresses.value.toMutableMap()
        currentProgresses[newContract.id] = 0.0
        vm.contractProgresses.value = currentProgresses
        
        vm.addLogPublic("[CONTRACT]: ▶ ${contract.name} ACQUIRED. Processing... (Purity: ${(contract.purity * 100).toInt()}%)")
        SoundManager.play("buy")
        HapticManager.vibrateClick()
        return true
    }

    fun addContractToAvailable(vm: GameViewModel, contract: ComputeContract) {
        val current = vm.availableContracts.value.toMutableList()
        current.add(0, contract) // Inject at top
        vm.availableContracts.value = current.take(10) // Keep pool manageable
    }

    // v3.33.0: Stage 5 Contract Forging
    fun forgeCustomContract(vm: GameViewModel): ComputeContract? {
        if (vm.storyStage.value < 5) return null
        
        val currentNeur = vm.neuralTokens.value
        val baseCost = currentNeur * 0.25 // Costs 25% of your current stack
        if (baseCost < 1000.0) return null
        
        val baseYield = baseCost * 2.5 // 2.5x multiplier for locked liquidity
        
        val name = when (vm.singularityChoice.value) {
            "SOVEREIGN" -> "Imperial Mandate"
            "NULL_OVERWRITE" -> "Void Fabrication"
            "UNITY" -> "Resonance Accord"
            else -> "Forged Compute Block"
        }
        
        return ComputeContract(
            id = "forged_${System.currentTimeMillis()}",
            name = name,
            cost = baseCost,
            expectedYield = baseYield,
            purity = 1.0,           // 100% purity guaranteed
            processingTime = 60_000L, // Takes a full minute base
            tier = 5
        )
    }
}
