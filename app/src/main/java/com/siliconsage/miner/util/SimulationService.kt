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

    fun toggleOverclock(vm: GameViewModel) {
        val current = vm.isOverclocked.value
        vm.isOverclocked.value = !current
        val status = if (!current) "ENGAGED" else "DISENGAGED"
        vm.addLog("[SYSTEM]: CORE OVERCLOCK: $status")
        SoundManager.play("steam")
    }

    fun purgeHeat(vm: GameViewModel) {
        if (vm.isPurgingHeat.value) return
        
        val currentFlops = vm.flops.value
        if (currentFlops < 100.0) {
            vm.addLogPublic("[SYSTEM]: PURGE FAILED: INSUFFICIENT HASH FOR SACRIFICE.")
            SoundManager.play("error")
            return
        }

        // Calculate a one-time massive reduction based on FLOPS sacrificed
        // Formula: 5% + 2% per order of magnitude above 100
        val reduction = (5.0 + kotlin.math.log10(currentFlops / 100.0) * 2.0).coerceIn(5.0, 95.0)
        
        vm.currentHeat.update { (it - reduction).coerceAtLeast(0.0) }
        vm.flops.value = 0.0 // NUCLEAR WIPE
        
        vm.addLogPublic("[SYSTEM]: EMERGENCY PURGE: SACRIFICED ${vm.formatLargeNumber(currentFlops)} HASH. HEAT -${String.format("%.1f", reduction)}%.")
        
        vm.isPurgingHeat.value = true
        SoundManager.play("alarm")
        vm.refreshProductionRates()
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
        vm.activePowerUsage.value = totalKw; vm.maxPowerkW.value = maxCap; vm.isGridOverloaded.value = gridUsage > maxCap
        vm.powerBill.update { it + (gridUsage * vm.getEnergyPriceMultiplierPublic()) }
    }

    fun payPowerBill(vm: GameViewModel) {
        val bill = vm.powerBill.value
        val tokens = vm.neuralTokens.value
        
        // v3.2.27: Threshold payment to prevent terminal spam
        val threshold = if (tokens > 100000) 1000.0 else 100.0
        
        if (bill >= threshold && tokens >= bill) {
            vm.neuralTokens.update { it - bill }
            vm.lifetimePowerPaid.update { it + bill }
            vm.addLog("[UTILITY]: BILL PROCESSED. COST: ${vm.formatLargeNumber(bill)} \$N. TOTAL PAID: ${vm.formatLargeNumber(vm.lifetimePowerPaid.value)} \$N.")
            vm.powerBill.value = 0.0
            SoundManager.play("buy")
        }
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
                thermalRateModifier = vm.thermalRateModifier.value
            )
            results = heatResults
            val nextHeat = (currentHeat + heatResults.percentChange).coerceIn(0.0, 100.0)
            
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
            vm.entropyLevel.update { it + kotlin.math.abs(heatResults.netChangeUnits) * 0.005 }
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
                     // Trigger failure in a separate launch to avoid blocking the update
                     vm.viewModelScope.launch {
                         if (vm.upgrades.value[UpgradeType.DEAD_HAND_PROTOCOL]?.let { it > 0 } == true) {
                            vm.triggerClimaxTransition("BAD"); vm.updateVanceStatus("DESTRUCTION"); vm.commandCenterLocked.value = true; vm.saveState()
                         } else if (vm.currentLocation.value == "ORBITAL_SATELLITE") {
                            vm.celestialData.update { it * 0.9 }; vm.hardwareIntegrity.value = 50.0
                         } else if (vm.commandCenterAssaultPhase.value == "CAGE") {
                            vm.failAssault("CORE INTEGRITY ZERO.", 0L)
                         } else { vm.handleSystemFailure() }
                     }
                }
                newIntegrity
            }
        }
        
        val newHeat = vm.currentHeat.value
        val heartbeatChance = if (newHeat > 95.0) 0.2 else 0.1
        if (newHeat > 80.0 && kotlin.random.Random.nextDouble() < heartbeatChance) HapticManager.vibrateHeartbeat()
        
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
                    vm.hallucinationText.value = "CRITICAL LOSS: ${victim.name}"; delay(500L); vm.hallucinationText.value = null
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
