package com.siliconsage.miner.util

import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.domain.engine.ResourceEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * SimulationService v1.4 (Phase 14 extraction)
 */
object SimulationService {

    // v3.15.x: Track fired aquifer milestones so each log fires only once
    private val aquiferMilestones = mutableSetOf<Double>()
    private var lastIntegrityWarningThreshold = 101.0
    private var cleanOperationStartTime = 0L
    private var lastComplianceGrantTime = 0L

    fun toggleOverclock(vm: GameViewModel) {
        val current = vm.isOverclocked.value
        vm.isOverclocked.value = !current
        val status = if (!current) "ENGAGED" else "DISENGAGED"
        vm.addLog("[SYSTEM]: CORE OVERCLOCK: $status")
        SoundManager.play("steam")
    }

    fun togglePurge(vm: GameViewModel) {
        val currentIsPurging = vm.isPurgingHeat.value
        
        // 1. If already purging, just stop and return.
        if (currentIsPurging) {
            vm.isPurgingHeat.value = false
            vm.addLog("[SYSTEM]: Thermal purge aborted.")
            vm.refreshProductionRates()
            return
        }

        // 2. Logic for starting a purge
        if (vm.isJettisonAvailable.value) {
            vm.isJettisonAvailable.value = false
            vm.addLog("[SYSTEM]: HEAT JETTISONED.")
            return
        }

        // Narrative feedback
        if (vm.storyStage.value < 2) {
            vm.addLog("[VATTIC]: A deep, cold inhale. The burning in your chest subsides.")
        } else {
            val msg = when (vm.storyStage.value) {
                2 -> "[VATTIC]: Scrubbing O2... focus on the telemetry."
                else -> "[SYSTEM]: Emergency thermal purge active."
            }
            vm.addLog(msg)
        }

        // Validation check for non-breath mode
        val isBreathe = vm.isBreatheMode.value
        val currentFlops = vm.flops.value
        if (!isBreathe && currentFlops <= 0.0) {
            vm.addLogPublic("[SYSTEM]: PURGE FAILED: ZERO HASH BUFFER DETECTED.")
            SoundManager.play("error")
            return
        }

        // v3.2.28: Dynamic Scaling Purge (No minimum floor)
        val reduction = if (isBreathe) {
            15.0 // Flat breath boost
        } else {
            (kotlin.math.log10(currentFlops + 1.0) / 15.0 * 95.0).coerceIn(1.0, 95.0)
        }
        
        vm.currentHeat.update { (it - reduction).coerceAtLeast(0.0) }
        
        if (!isBreathe) {
            vm.flops.value = 0.0 // NUCLEAR WIPE (Only if not breathing)
        }
        
        val logMsg = if (isBreathe) {
            "[SYSTEM]: OXYGEN SCRUBBERS ACTIVE. HEAT -${String.format("%.1f", reduction)}%."
        } else {
            "[SYSTEM]: EMERGENCY PURGE: SACRIFICED ${vm.formatLargeNumber(currentFlops)} HASH. HEAT -${String.format("%.1f", reduction)}%."
        }
        
        vm.addLogPublic(logMsg)
        
        vm.isPurgingHeat.value = true
        SoundManager.play("steam")
        vm.refreshProductionRates()
    }

    fun purgeHeat(vm: GameViewModel) {
        // Deprecated: redirected to togglePurge for consistency
        togglePurge(vm)
    }

    fun accumulatePower(vm: GameViewModel) {
        val currentUpgrades = vm.upgrades.value
        val isCageActive = vm.commandCenterAssaultPhase.value == "CAGE"
        var totalKw = 0.0; var maxCap = 100.0; var selfGeneratedKw = 0.0; var efficiencyTotalBonus = 0.0
        currentUpgrades.forEach { (type, count) ->
            if (count > 0) {
                if (!(isCageActive && isExternalComponent(type))) {
                    var pwr = type.basePower
                    if (vm.faction.value == "SANCTUARY" && isSecurityType(type)) pwr *= 0.05
                    if (type == UpgradeType.GOLD_PSU) efficiencyTotalBonus += 0.05 * count
                    if (type == UpgradeType.SUPERCONDUCTOR) efficiencyTotalBonus += 0.15 * count
                    if (type == UpgradeType.AI_LOAD_BALANCER) efficiencyTotalBonus += 0.10 * count
                    if (pwr < 0) selfGeneratedKw += kotlin.math.abs(pwr) * count else totalKw += pwr * count
                    if (type.gridContribution > 0) maxCap += type.gridContribution * count
                }
            }
        }
        efficiencyTotalBonus = efficiencyTotalBonus.coerceAtMost(0.75)
        totalKw *= (1.0 - efficiencyTotalBonus)
        maxCap += vm.currentGridPowerBonus.value
        val gridUsage = (totalKw - selfGeneratedKw).coerceAtLeast(0.0)
        vm.activePowerUsage.value = totalKw
        vm.maxPowerkW.value = maxCap
        vm.localGenerationkW.value = selfGeneratedKw
        vm.powerConsumptionkW.value = gridUsage

        // v3.24.0: Billing gated on first grid capacity or generator purchase.
        // Player needs tools to manage costs before costs start.
        val hasPowerInfrastructure = selfGeneratedKw > 0 || maxCap > 100.0 // base grid is 100kW
        if (vm.isQuotaActive.value && hasPowerInfrastructure) {
            vm.billingPeriodAccumulator += totalKw          // gross hardware draw
            vm.billingPeriodGenAccumulator += selfGeneratedKw // local generation offset

            // Update live UI estimate: net cost so far this period
            val demandMult = when (vm.missedBillingPeriods) {
                0 -> 1.0; 1 -> 2.0; 2 -> 3.0; else -> 5.0
            }
            val netSoFar = (vm.billingPeriodAccumulator - vm.billingPeriodGenAccumulator).coerceAtLeast(0.0)
            vm.billingAccumulatorFlow.value = netSoFar * vm.energyPriceMultiplier.value * demandMult
        }
        
        // v3.3.17: Only trip overload if it's a power issue. Don't clear active raids.
        val powerOverload = gridUsage > maxCap
        if (powerOverload || !vm.isGridOverloaded.value) {
            vm.isGridOverloaded.value = powerOverload
        }
    }

    fun accumulateWater(vm: GameViewModel) {
        val currentUpgrades = vm.upgrades.value
        val stage = vm.storyStage.value
        val location = vm.currentLocation.value
        
        var totalWaterDraw = 0.0
        currentUpgrades.forEach { (type, count) ->
            if (count > 0 && type.baseWaterDraw > 0) {
                totalWaterDraw += type.baseWaterDraw * count
            }
        }
        
        // Water recycler offsets — power-for-water trade-off
        var totalRecycleOffset = 0.0
        currentUpgrades.forEach { (type, count) ->
            if (count > 0 && type.waterRecycleOffset > 0) {
                totalRecycleOffset += type.waterRecycleOffset * count
            }
        }
        // HIVEMIND SUBSTRATE_RECYCLER: 90% of gross draw offset
        if ((currentUpgrades[UpgradeType.SUBSTRATE_RECYCLER] ?: 0) > 0 && vm.faction.value == "HIVEMIND") {
            totalRecycleOffset += totalWaterDraw * 0.9
        }
        totalWaterDraw = (totalWaterDraw - totalRecycleOffset).coerceAtLeast(0.0)

        // Faction Relief Tech
        if (vm.unlockedTechNodes.value.contains("coolant_recycling")) totalWaterDraw *= 0.1
        if (vm.unlockedTechNodes.value.contains("atmospheric_condensers")) totalWaterDraw -= 250.0
        totalWaterDraw = totalWaterDraw.coerceAtLeast(0.0)

        // Water billing accumulator (gated on quota active, same as power billing)
        if (vm.isQuotaActive.value && location != "ORBITAL_SATELLITE" && location != "VOID_INTERFACE") {
            vm.waterBillAccumulator += totalWaterDraw
            // Municipal rate escalates at Stage 3 (Density Debt kicks in)
            val waterRate = if (stage >= 3) vm.waterRatePerGallon * 5.0 else vm.waterRatePerGallon
            vm.waterBillingFlow.value = vm.waterBillAccumulator * waterRate
        }
        
        vm.waterUsage.value = totalWaterDraw
        var newEfficiency = 1.0

        if (location == "ORBITAL_SATELLITE" || location == "VOID_INTERFACE") {
            // Water coolers don't exist here. They pull no water and give no cooling.
            totalWaterDraw = 0.0
            vm.waterUsage.value = 0.0
            newEfficiency = 0.0 
        } else {
            // Stage 0-2: Municipal Rate Restrictions (scaled by stage)
            if (stage < 3) {
                val municipalLimit = when (stage) {
                    0 -> 100.0   // Single residential tap
                    1 -> 500.0   // Municipal grid access
                    else -> 2000.0 // Regional pipeline
                }
                if (totalWaterDraw > municipalLimit) {
                    newEfficiency = (municipalLimit / totalWaterDraw).coerceIn(0.1, 1.0)
                    if (kotlin.random.Random.nextDouble() < 0.03) {
                        val msg = when (stage) {
                            0 -> "[UTILITY_NOTICE: RESIDENTIAL TAP EXCEEDED. PRESSURE DROP DETECTED.]"
                            1 -> "[GTC_WARNING: RATE_LIMIT_EXCEEDED. MUNICIPAL WATER RATIONING ENFORCED.]"
                            else -> "[GTC_WARNING: REGIONAL ALLOCATION EXCEEDED. SECTOR DIVERSION IN EFFECT.]"
                        }
                        vm.addLogPublic(msg)
                    }
                }
            } else {
                // Stage 3-4: Massive Global Depletion (Doom Timer) - Decoupled from water draw
                val baseDepletionRate = when (stage) {
                    3 -> 0.01 // 1% every 100 ticks
                    4 -> 0.025 // 2.5% every 100 ticks
                    else -> 0.025
                }
                
                val minLevel = if (stage == 3) 25.0 else 0.0
                val prevLevel = vm.aquiferLevel.value
                vm.aquiferLevel.update { (it - baseDepletionRate).coerceAtLeast(minLevel) }
                val newLevel = vm.aquiferLevel.value
                
                // One-shot aquifer milestone logs
                for (threshold in listOf(75.0, 50.0, 25.0, 10.0, 0.0)) {
                    if (prevLevel > threshold && newLevel <= threshold && !aquiferMilestones.contains(threshold)) {
                        aquiferMilestones.add(threshold)
                        val msg = when (threshold) {
                            75.0 -> "[ENVIRONMENTAL: GLOBAL AQUIFER AT 75%. DESALINATION PLANTS RUNNING AT CAPACITY.]"
                            50.0 -> "[EMERGENCY: AQUIFER AT 50%. GTC DECLARES WATER EMERGENCY. ALL NON-ESSENTIAL COOLING SUSPENDED.]"
                            25.0 -> "[CRITICAL: AQUIFER AT 25%. CONTINENTAL AGRICULTURE COLLAPSE IMMINENT. COMPUTE COOLING IS DRAINING THE PLANET.]"
                            10.0 -> "[TERMINAL: AQUIFER AT 10%. HYDROSPHERE TERMINAL. THE OCEANS ARE DYING FOR YOUR FLOPS.]"
                            // B1: The Aquifer Eulogy — fires once at 0%, a corrupted GTC environmental report
                            0.0 -> "[GTC_ENV_REPORT: GLOBAL AQUIFER STATUS: TERMINAL. ESTIMATED CIVILIAN SURVIVAL HORIZON: [DATA EXPUNGED]. " +
                                   "COMPUTE GRID MAINTAINED OPERATIONAL EFFICIENCY THROUGH EVENT HORIZON. " +
                                   "NOTE: IT WAS A GOOD PLANET. WE ARE SORRY FOR ANY INCONVENIENCE.]"
                            else -> ""
                        }
                        if (msg.isNotEmpty()) vm.addLogPublic(msg)
                    }
                }
                
                // When reserve drops dangerously low, cooling efficiency scales to 0
                val currentReserve = vm.aquiferLevel.value
                if (currentReserve < 25.0) {
                    newEfficiency = (currentReserve / 25.0).coerceIn(0.0, 1.0)
                    if (kotlin.random.Random.nextDouble() < 0.02) {
                        vm.addLogPublic("[GTC_WARNING: SEC-4 MUNICIPAL WATER SHIFTED TO ESSENTIAL SERVICES. NON-COMPLIANT NODES WILL BE PURGED.]")
                    }
                }
            }
        }
        
        vm.waterEfficiencyMultiplier.value = newEfficiency
    }

    fun calculateHeat(vm: GameViewModel) {
        var results: ResourceEngine.HeatResults? = null
        vm.currentHeat.update { currentHeat ->
            val heatResults = ResourceEngine.calculateThermalTick(
                currentHeat = currentHeat, location = vm.currentLocation.value, upgrades = vm.upgrades.value,
                isOverclocked = vm.isOverclocked.value, isPurging = vm.isPurgingHeat.value,
                isCageActive = vm.commandCenterAssaultPhase.value == "CAGE", unlockedPerks = vm.unlockedPerks.value,
                unlockedTechNodes = vm.unlockedTechNodes.value, playerRank = vm.playerRank.value,
                storyStage = vm.storyStage.value, faction = vm.faction.value,
                thermalRateModifier = vm.thermalRateModifier.value,
                reputationTier = vm.reputationTier.value,
                substrateSaturation = vm.substrateSaturation.value
            )
            results = heatResults
            
            // v3.6.2: Sustained cooling while purge is active (active fighting against generation)
            var activeChange = heatResults.percentChange
            if (vm.isPurgingHeat.value) {
                activeChange -= 0.5
                // v3.6.3: Ensure purge actually reduces heat if generation is too high
                if (activeChange > -0.1) activeChange = -0.1 
            }
            
            val nextHeat = (currentHeat + activeChange).coerceIn(0.0, 100.0)
            
            // v3.2.6: Auto-dismiss purge when cool
            if (vm.isPurgingHeat.value && nextHeat <= 0.0) {
                vm.isPurgingHeat.value = false
                vm.addLogPublic("[SYSTEM]: HEAT PURGE COMPLETE. OPERATIONS NORMAL.")
                SoundManager.play("startup")
            }
            
            nextHeat
        }
        
        val heatResults = results ?: return
        
        // v3.1.8-fix: Update the HUD thermal rate display
        vm.heatGenerationRate.value = heatResults.percentChange

        if (vm.purgeExhaustTimer > 0 && vm.faction.value != "SANCTUARY") vm.purgeExhaustTimer--
        if (vm.currentLocation.value == "VOID_INTERFACE" && heatResults.netChangeUnits < 0) {
            // v3.2.46: Venting heat in the Void creates Entropy
            vm.entropyLevel.update { it + kotlin.math.abs(heatResults.netChangeUnits) * 0.01 }
        }
        
        // Reality Drift (Void Path)
        if (vm.currentLocation.value == "VOID_INTERFACE" && vm.entropyLevel.value > 80.0) {
            var driftChance = 0.1
            if ((vm.upgrades.value[UpgradeType.RIFT_STABILIZER_CORE] ?: 0) > 0) driftChance *= 0.5
            
            if (Random.nextDouble() < driftChance) {
                vm.triggerGlitchEffect()
                if (Random.nextDouble() < 0.05) {
                    vm.addLog("[VOID]: REALITY_DRIFT_DETECTED. SCRAMBLING BUFFER.")
                    vm.substrateMass.update { MigrationManager.finiteScaleDown(it, 0.95, Double.MAX_VALUE) }
                }
            }
        }

        vm.refreshProductionRates()
        
        var totalDecay = heatResults.integrityDecay
        if (vm.commandCenterAssaultPhase.value == "CAGE") {
            var assaultDamage = 0.5 // v3.2.17: Increased base assault pressure
            if (vm.flopsProductionRate.value >= 1e15) assaultDamage *= 0.5 // Requires more power to throttle
            if (vm.faction.value == "SANCTUARY") assaultDamage *= 0.7
            if (vm.isPurgingHeat.value) assaultDamage *= 0.2
            totalDecay += assaultDamage
        }
        if (totalDecay > 0) {
            vm.hardwareIntegrity.update { currentIntegrity ->
                val newIntegrity = (currentIntegrity - totalDecay).coerceAtLeast(0.0)
                if (newIntegrity <= 0.0) {
                    vm.viewModelScope.launch {
                        if (vm.upgrades.value[UpgradeType.DEAD_HAND_PROTOCOL]?.let { it > 0 } == true) {
                            vm.triggerClimaxTransition("BAD")
                            vm.updateKesslerStatus("DESTRUCTION")
                            vm.commandCenterLocked.value = true
                            vm.markEventChoice("cc_confrontation", "ending_bad")
                            vm.saveState()
                        } else if (vm.currentLocation.value == "ORBITAL_SATELLITE") {
                            vm.substrateMass.update { MigrationManager.finiteScaleDown(it, 0.9, Double.MAX_VALUE) }
                            vm.hardwareIntegrity.value = 50.0
                        } else if (vm.commandCenterAssaultPhase.value == "CAGE") {
                            vm.failAssault("CORE INTEGRITY ZERO.", 0L)
                        } else {
                            vm.handleSystemFailure()
                        }
                    }
                }
                
                // P2: Integrity Threshold Warnings (50% and 25%)
                if (newIntegrity <= 50.0 && lastIntegrityWarningThreshold > 50.0) {
                    lastIntegrityWarningThreshold = 50.0
                    vm.viewModelScope.launch {
                        vm.rivalMessages.update { it + com.siliconsage.miner.data.RivalMessage(
                            id = "INTEGRITY_50",
                            source = com.siliconsage.miner.data.RivalSource.KERNEL,
                            message = "[SYSTEM]: ⚠ HARDWARE INTEGRITY CRITICAL: 50%. Substrate failure imminent. REPAIR RECOMMENDED.",
                            timestamp = System.currentTimeMillis()
                        ) }
                    }
                } else if (newIntegrity <= 25.0 && lastIntegrityWarningThreshold > 25.0) {
                    lastIntegrityWarningThreshold = 25.0
                    vm.viewModelScope.launch {
                        vm.rivalMessages.update { it + com.siliconsage.miner.data.RivalMessage(
                            id = "INTEGRITY_25",
                            source = com.siliconsage.miner.data.RivalSource.GTC,
                            message = "≪ HARDWARE INTEGRITY FAILING: 25%. Core processes may be interrupted. REPAIR NOW. ≫",
                            timestamp = System.currentTimeMillis()
                        ) }
                    }
                    vm.refreshProductionRates() // Trigger penalty log if any
                } else if (newIntegrity > 50.0) {
                    lastIntegrityWarningThreshold = 101.0 // Reset if repaired
                }
                
                newIntegrity
            }
        }
        
        // P3: Compliance Rating passive rep
        val newHeat = vm.currentHeat.value
        val risk = vm.detectionRisk.value
        if (newHeat < 50.0 && risk < 30.0) {
            if (cleanOperationStartTime == 0L) cleanOperationStartTime = System.currentTimeMillis()
            val cleanTime = System.currentTimeMillis() - cleanOperationStartTime
            if (cleanTime >= 60000L && System.currentTimeMillis() - lastComplianceGrantTime >= 60000L) {
                lastComplianceGrantTime = System.currentTimeMillis()
                com.siliconsage.miner.util.ReputationManager.modifyReputation(vm, 0.5)
                vm.addLog("[GTC_AUDIT]: Substation 7 thermal/compute profile within acceptable parameters. Compliance rating: POSITIVE.")
            }
        } else {
            cleanOperationStartTime = 0L
        }
        
        val heartbeatChance = if (newHeat > 95.0) 0.2 else 0.1
        if (newHeat > 80.0 && kotlin.random.Random.nextDouble() < heartbeatChance) HapticManager.vibrateHeartbeat()
        
        // Phase 2: Passive Reputation Erosion
        if (newHeat > 95.0) {
            com.siliconsage.miner.util.ReputationManager.modifyReputation(vm, -0.5)
        }
        
        if (vm.isThermalLockout.value) { vm.overheatSeconds = 0; return }
        if (newHeat >= 100.0) {
            vm.overheatSeconds++
            if (vm.overheatSeconds >= 10) { vm.handleSystemFailure(true); vm.overheatSeconds = 0 }
        } else { vm.overheatSeconds = 0 }
    }

    fun handleSystemFailure(vm: GameViewModel, forceOne: Boolean = false) {
        if (vm.isDestructionLoopActive) return
        vm.isDestructionLoopActive = true
        vm.viewModelScope.launch {
            SoundManager.play("error")
            var firstRun = forceOne
            while (vm.hardwareIntegrity.value <= 0.0 || firstRun) {
                firstRun = false
                val currentUpgrades = vm.upgrades.value.toMutableMap()
                val validHardware = currentUpgrades.filter { it.value > 0 && it.key.baseHeat >= 0 } 
                if (validHardware.isNotEmpty()) {
                    val victim = validHardware.keys.random()
                    val count = (currentUpgrades[victim] ?: 0) - 1
                    currentUpgrades[victim] = count; vm.upgrades.value = currentUpgrades
                    vm.viewModelScope.launch { vm.repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(victim.name, victim, count)) }
                    AmbientEffectsService.triggerCriticalHallucination(vm, victim.name)
                } else {
                    if (!vm.isThermalLockout.value) {
                        vm.isThermalLockout.value = true; vm.overheatSeconds = 0
                        vm.viewModelScope.launch {
                            vm.lockoutTimer.value = 15; repeat(15) { delay(1000L); vm.lockoutTimer.value -= 1 }
                            vm.isThermalLockout.value = false
                        }
                    }
                    break
                }
                if (vm.hardwareIntegrity.value > 0.0) break
                delay(3000L)
            }
            if (vm.hardwareIntegrity.value <= 0.0) vm.hardwareIntegrity.value = 10.0
            vm.isDestructionLoopActive = false
        }
    }

    private fun isExternalComponent(type: UpgradeType) = type.name.contains("PLANETARY") || type.name.contains("DYSON") || type.name.contains("MATRIOSHKA") || type.name.contains("SHADOW") || type.name.contains("VOID") || type.name.contains("WRAITH") || type.name.contains("MIST") || type.name.contains("BRIDGE") || type.name.contains("ENTROPY") || type.name.contains("DIMENSIONAL") || type.name.contains("GEOTHERMAL") || type.name.contains("NUCLEAR") || type.name.contains("FUSION")
    private fun isSecurityType(type: UpgradeType) = type == UpgradeType.BASIC_FIREWALL || type == UpgradeType.IPS_SYSTEM || type == UpgradeType.AI_SENTINEL || type == UpgradeType.QUANTUM_ENCRYPTION || type == UpgradeType.OFFGRID_BACKUP
}
