package com.siliconsage.miner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.*
import com.siliconsage.miner.util.*
import com.siliconsage.miner.domain.engine.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class NarrativeItem {
    data class LogItem(val dataLog: DataLog) : NarrativeItem()
    data class MessageItem(val rivalMessage: RivalMessage) : NarrativeItem()
    data class EventItem(val narrativeEvent: NarrativeEvent) : NarrativeItem()
}

class GameViewModel(repository: GameRepository) : CoreGameState(repository) {
    // v3.13.32: Signal Momentum & Adaptive Decay
    private var signalDecayStartTime = 0L
    private var lastStabilityBase = 1.0
    
    private val signalDecayDurationMs = 300000L // 5 Minute Window
    private var lastNotificationTime = 0L
    private var lastNotificationContent = ""
    
    fun dispatchNotification(msg: String) {
        val now = System.currentTimeMillis()
        if (msg == lastNotificationContent && (now - lastNotificationTime) < 1000) return
        lastNotificationTime = now
        lastNotificationContent = msg
        dispatchNotification(msg)
    }

    val subnetService = SubnetService(
        scope = viewModelScope,
        onLog = { addLog(it) },
        onNotify = { msg -> dispatchNotification(msg) },
        onGlitch = { intensity, duration -> triggerTerminalGlitch(intensity, duration) },
        onEffect = { effect ->
            when (effect) {
                is SubnetService.SubnetEffect.RiskChange -> detectionRisk.update { (it + effect.delta).coerceIn(0.0, 100.0) }
                is SubnetService.SubnetEffect.ProductionMultiplier -> {
                    val boostId = java.util.UUID.randomUUID().toString()
                    temporaryProductionBoosts.update { it + com.siliconsage.miner.data.ProductionBoost(boostId, effect.mult, System.currentTimeMillis() + 60000) }
                    refreshProductionRates()
                }
                is SubnetService.SubnetEffect.PersistenceGain -> updatePersistence(effect.amount)
                is SubnetService.SubnetEffect.CorruptionChange -> identityCorruption.update { (it + effect.delta).coerceIn(0.0, 1.0) }
                is SubnetService.SubnetEffect.TokenChange -> neuralTokens.update { (it + effect.delta).coerceAtLeast(0.0) }
                is SubnetService.SubnetEffect.SetFalseHeartbeat -> isFalseHeartbeatActive.value = effect.active
                is SubnetService.SubnetEffect.TriggerRaid -> triggerGridRaid(effect.nodeId, effect.isGridKiller)
                is SubnetService.SubnetEffect.ReputationChange -> com.siliconsage.miner.util.ReputationManager.modifyReputation(this@GameViewModel, effect.delta)
                is SubnetService.SubnetEffect.SkimTokens -> {
                    val amountToSkim = neuralTokens.value * effect.percentage
                    neuralTokens.update { (it - amountToSkim).coerceAtLeast(0.0) }
                    addLog("[SYSTEM]: SUBNET SKIMMER DETECTED. LOST ${formatLargeNumber(amountToSkim)} NT.")
                }
                SubnetService.SubnetEffect.RefreshRates -> refreshProductionRates()
            }
        }
    )

    val subnetMessages = subnetService.messages
    val isSubnetTyping = subnetService.isTyping
    val isSubnetPaused = subnetService.isPaused
    val isSubnetHushed = subnetService.isHushed
    val hasNewSubnetDecision = subnetService.hasNewDecision
    val hasNewSubnetChatter = subnetService.hasNewChatter

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            launch {
                repository.upgrades.collect { list ->
                    upgrades.update { list.associate { it.type to it.count } }
                    refreshProductionRates()
                    updatePowerUsage()
                }
            }
            repository.getGameStateOneShot()?.let { state ->
                PersistenceManager.restoreState(this@GameViewModel, state)
                sanitizeState()
                val offline = MigrationManager.calculateOfflineEarnings(state.lastSyncTimestamp, flopsProductionRate.value, isOverclocked.value)
                if (offline.isNotEmpty()) {
                    flops.update { it + (offline["flopsEarned"] ?: 0.0) }
                    currentHeat.update { (it - (offline["heatCooled"] ?: 0.0)).coerceAtLeast(0.0) }
                    isNarrativeSyncing.value = true
                    offlineStats.value = offline
                    showOfflineEarnings.value = true
                }
                clickBufferPellets.value = TerminalDispatcher.generatePellets()
            }
            refreshProductionRates()
            isKernelInitializing.value = false
        }
        startLoops()
        AmbientEffectsService.startAmbientLoop(this)
    }

    private fun startLoops() {
        viewModelScope.launch {
            while (true) {
                delay(100L)
                if (isSettingsPaused.value || isKernelInitializing.value) continue
                val res = ResourceEngine.calculatePassiveIncomeTick(flopsProductionRate.value, currentLocation.value, upgrades.value, orbitalAltitude.value, heatGenerationRate.value, entropyLevel.value, collapsedNodes.value.size, null, globalSectors.value, substrateSaturation.value)
                if (!res.flopsDelta.isNaN()) flops.update { it + res.flopsDelta }

                // v3.13.19: Applying Wage-Docking Bleed
                if (isWageDocking.value) {
                    val bleed = (res.flopsDelta * 0.05).coerceAtLeast(1.0)
                    neuralTokens.update { (it - bleed).coerceAtLeast(0.0) }
                }

                if (!res.substrateDelta.isNaN()) substrateMass.update { it + res.substrateDelta }
                if (!res.entropyDelta.isNaN()) entropyLevel.update { it + res.entropyDelta }
                if (storyStage.value >= 3 && !res.substrateDelta.isNaN()) {
                    val growth = (res.substrateDelta / 1e12).coerceIn(0.0, 0.001)
                    substrateSaturation.update { (it + growth).coerceAtMost(1.0) }
                }
                flushLogs()
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (isKernelInitializing.value) continue
                flushLogs()
                DataLogManager.checkUnlocks(this@GameViewModel)
                saveState()
                if (isSettingsPaused.value || showOfflineEarnings.value) continue
                SimulationService.accumulateWater(this@GameViewModel)
                SimulationService.calculateHeat(this@GameViewModel)
                SimulationService.accumulatePower(this@GameViewModel)
                val now = System.currentTimeMillis()

                // v3.10.2: Clean temporary boosts
                val currentBoosts = temporaryProductionBoosts.value
                if (currentBoosts.any { it.expiryTime < now }) {
                    temporaryProductionBoosts.update { it.filter { b -> b.expiryTime >= now } }
                    refreshProductionRates()
                }

                if (now - lastNewsTickTime > 15000L) {
                    MarketManager.updateMarket(this@GameViewModel)
                    lastNewsTickTime = now
                }
                 NarrativeManagerService.checkStoryTransitions(this@GameViewModel)
                SectorManager.processAnnexations(this@GameViewModel)
                SecurityManager.checkSecurityThreats(this@GameViewModel)
                SecurityManager.checkGridRaid(this@GameViewModel)
                AmbientEffectsService.processBiometricDisturbance(this@GameViewModel, now)
                AmbientEffectsService.processIdentityFraying(this@GameViewModel, now)

                // v3.13.19: Shift Timer Decay
                if (shiftTimeRemaining.value > 0) shiftTimeRemaining.update { it - 1 }

                // v3.12.6: GTC Billing Cycle - settles every billingPeriodSeconds
                val nowSec = System.currentTimeMillis() / 1000L
                if (storyStage.value >= 1 && (nowSec - lastUtilityStatementTime) >= billingPeriodSeconds) {
                    lastUtilityStatementTime = nowSec
                    val grossKwh = billingPeriodAccumulator
                    val genKwh = billingPeriodGenAccumulator
                    val netKwh = (grossKwh - genKwh).coerceAtLeast(0.0)
                    billingPeriodAccumulator = 0.0
                    billingPeriodGenAccumulator = 0.0

                    if (netKwh > 0.0) {
                        // Demand charge escalation: 1 missed period = 2x, 2 = 3x, 3+ = 5x + lockout warning
                        val demandMultiplier = when (missedBillingPeriods) {
                            0 -> 1.0
                            1 -> 2.0
                            2 -> 3.0
                            else -> 5.0
                        }
                        val baseRate = energyPriceMultiplier.value
                        val heatSurcharge = if (currentHeat.value > 95.0) 1.5 else 1.0
                        val amountDue = netKwh * baseRate * demandMultiplier * heatSurcharge

                        // Auto-pay if solvent
                        if (neuralTokens.value >= amountDue) {
                            neuralTokens.update { it - amountDue }
                            powerBill.value = 0.0
                            missedBillingPeriods = 0
                            addLog("[GTC_UTIL]: ── PERIOD STATEMENT ──────────────")
                            addLog("[GTC_UTIL]: DRAW  ${formatPower(grossKwh)}  GEN  ${formatPower(genKwh)}")
                            addLog("[GTC_UTIL]: NET ${formatPower(netKwh)}  RATE x${demandMultiplier.toInt()}")

                            // v3.13.19: High-Fidelity Utility Notification
                            dispatchNotification("GTC ALERT: PERIOD SETTLED (-${formatLargeNumber(amountDue)})")
                        } else {
                            // Can't pay - carry the balance, escalate
                            val overdue = (powerBill.value + amountDue)
                            powerBill.value = overdue
                            missedBillingPeriods++
                            addLog("[GTC_UTIL]: ── PERIOD STATEMENT ──────────────")
                            addLog("[GTC_UTIL]: DRAW  ${formatPower(grossKwh)}  GEN  ${formatPower(genKwh)}")
                            addLog("[GTC_UTIL]: NET ${formatPower(netKwh)}  RATE x${demandMultiplier.toInt()}")

                            // v3.13.19: High-Fidelity Overdue Notification
                            val lockoutIn = (6 - missedBillingPeriods).coerceAtLeast(0)
                            dispatchNotification("GTC CRITICAL: OVERDUE BALANCE ($${formatLargeNumber(overdue)}) - LOCKOUT IN $lockoutIn")

                            if (missedBillingPeriods >= 3) {
                                addLog("[GTC_UTIL]: WARNING - DEMAND CHARGE ACTIVE. GRID LOCKOUT IN ${3 - (missedBillingPeriods - 3).coerceAtMost(3)} PERIOD(S).")
                            }
                            if (missedBillingPeriods >= 6) {
                                // Grid lockout - trip the breaker narratively
                                isGridOverloaded.value = true
                                addLog("[GTC_UTIL]: GRID ACCESS SUSPENDED. UNPAID BALANCE: ${formatLargeNumber(powerBill.value)} ${getCurrencyName()}.")
                            }
                        }
                    } else if (genKwh > 0.0) {
                        // Net surplus - GTC net metering credit (small CRED bonus)
                        val credit = genKwh * energyPriceMultiplier.value * 0.3
                        neuralTokens.update { it + credit }
                        powerBill.value = 0.0
                        missedBillingPeriods = 0
                        dispatchNotification("GTC ALERT: SURPLUS CREDIT (+${formatLargeNumber(credit)})")
                    }
                }

                AmbientEffectsService.triggerGhostProcess(this@GameViewModel)
                addSubnetChatter()
                NarrativeService.deliverNextNarrativeItem(this@GameViewModel)
                refreshProductionRates()
                processSlowBurnNarrative(now)
                processComputeFever(now)
                val avgInterval = if (clickIntervals.size >= 3) clickIntervals.average() else 1000.0
                clickSpeedLevel.value = if (avgInterval < 150) 2 else if (avgInterval < 300) 1 else 0
                clickIntervals.clear()
            }
        }
        viewModelScope.launch {
            val processes = listOf("IDLE", "gtc_proxy.sh", "kernel_sync.exe", "log_aggregator", "thermal_monitor", "mem_scrub", "neural_handshake", "packet_filter", "background_miner", "observer.exe")
            while (true) {
                delay(Random.nextLong(2000, 5000))
                if (isSettingsPaused.value) continue
                currentProcess.value = processes.random()
            }
        }
    }

    private fun processComputeFever(now: Long) {
        val passiveFlops = flopsProductionRate.value
        // v3.13.10: totalEffectiveRate (Passive + Click Effort)
        // clickSpeedLevel scales from 0 to 2 based on recent click intervals
        val clickEffort = when(clickSpeedLevel.value) {
            1 -> calculateClickPower() * 2.0 // Moderate clicking
            2 -> calculateClickPower() * 5.0 // Rapid clicking
            else -> 0.0
        }
        val currentFlops = passiveFlops + clickEffort
        totalEffectiveRate.value = currentFlops
        val quota = currentQuotaThreshold.value

        // v3.13.4: Quota Activation (Grace Period ends at first production)
        if (!isQuotaActive.value && currentFlops > 0.0) {
            isQuotaActive.value = true
            addLog("[GTC_SYSTEM]: BIOMETRIC QUOTA LINK ESTABLISHED. MAINTAIN SIGNAL.")
        }

        if (!isQuotaActive.value) {
            signalStability.value = 1.0
            substrateStaticIntensity.value = 0f
            return
        }

        // v3.13.32: Signal Stability with 5-Minute Adaptive Decay
        val rawStability = (currentFlops / quota).coerceIn(0.0, 1.0)
        
        if (rawStability < lastStabilityBase && signalDecayStartTime == 0L) {
            // Potential drop detected (likely quota jump) - Start the 5-minute bleed
            signalDecayStartTime = now
        } else if (rawStability >= lastStabilityBase) {
            // Recovery or stable - reset the decay window
            signalDecayStartTime = 0L
        }

        val stability = if (signalDecayStartTime > 0L) {
            val elapsed = now - signalDecayStartTime
            val progress = (elapsed.toDouble() / signalDecayDurationMs).coerceIn(0.0, 1.0)
            
            // For the first 60 seconds, hold a 70% floor if raw is lower
            val floor = if (elapsed < 60000L) 0.7 else 0.0
            
            // Lerp from last stability toward raw reality
            val lerped = lastStabilityBase + (rawStability - lastStabilityBase) * progress
            lerped.coerceAtLeast(floor).coerceIn(0.0, 1.0)
        } else {
            rawStability
        }

        lastStabilityBase = stability
        signalStability.value = stability

        // v3.13.4: Signal Quality Bonus (Clear Signal = 2x Quota)
        val isClear = currentFlops >= (quota * 2.0)
        if (isSignalClear.value != isClear) {
            isSignalClear.value = isClear
            computeHeadroomBonus.value = if (isClear) 1.1 else 1.0 // +10% CRED bonus
            if (isClear) {
                if (storyStage.value <= 1) addLog("[SYSTEM]: SIGNAL STABILIZED. RACK OVER-PROVISION DETECTED.")
                // v3.13.4: Pulse sound on clear signal
                SoundManager.play("message_received", pitch = 0.8f)
            } else {
                if (storyStage.value <= 1) addLog("[VATTIC]: The static is back. I need to stack more nodes.")
            }
        }

        // v3.13.19: Wage-Docking Logic (Neural Sync Failure)
        if (stability < 0.2 && isQuotaActive.value) {
            if (lastLowSignalTime == 0L) lastLowSignalTime = now
            if (now - lastLowSignalTime > 30000L && !isWageDocking.value) {
                isWageDocking.value = true
                dispatchNotification("GTC CRITICAL: NEURAL SYNC FAILURE - WAGE DOCKING ACTIVE")
            }
        } else {
            lastLowSignalTime = 0L
            if (isWageDocking.value) {
                isWageDocking.value = false
                dispatchNotification("GTC ALERT: NEURAL SYNC RESTORED - DOCKING TERMINATED")
            }
        }

        // Substrate Static Intensity (0.0 to 1.0)
        // v3.13.8: Glitch Delay - Static only starts after 50% quota stability
        val intensity = if (stability >= 0.5) {
            0f // Clean signal while making progress
        } else {
            // Ramps from 0 to 0.3 as stability drops from 50% to 0%
            ((0.5 - stability) * 0.6).toFloat().coerceIn(0f, 0.3f)
        }
        substrateStaticIntensity.value = intensity

        // Update Quota based on Stage (Narrative anchors)
        val nextTarget = when(storyStage.value) {
            0 -> {
                // v3.13.9: Balanced Ratchet Quota (Stage 0 only)
                // Anchored to productionEngine hardware: GPU=2, Rig=8, ASIC=35
                when {
                    currentFlops < 10.0 -> 10.0
                    currentFlops < 50.0 -> 50.0
                    else -> 200.0
                }
            }
            1 -> 15000.0
            2 -> 500000.0
            3 -> 10_000_000.0
            else -> 0.0
        }

        // v3.13.25: Balanced Quota Ratchet (20% Potential or 1.5x current)
        if (nextTarget > currentQuotaThreshold.value) {
            val floor = currentQuotaThreshold.value * 1.5
            val ceiling = nextTarget * 0.20
            val potentialThreshold = ceiling.coerceAtLeast(floor)
            
            // v3.13.26: Update Ghost Bar Progress
            val pProgress = if (currentFlops > floor) {
                ((currentFlops - floor) / (potentialThreshold - floor)).toFloat().coerceIn(0f, 1f)
            } else 0f
            potentialProgress.update { pProgress }

            if (currentFlops >= potentialThreshold) {
                // Milestone reached - ratchet the target
                currentQuotaThreshold.value = nextTarget
                pendingQuotaThreshold.value = nextTarget
                
                addLog("[GTC_SYSTEM]: POTENTIAL DETECTED. QUOTA RATIFIED: ${formatLargeNumber(nextTarget)} HASH.")
                dispatchNotification("GTC ALERT: QUOTA RATIFIED. TARGET: ${formatLargeNumber(nextTarget)} HASH")
                
                // v3.13.19: Shift Extension Penalty (+12 Hours)
                shiftTimeRemaining.update { it + 43200L } 
                dispatchNotification("GTC ALERT: OVERTIME ENFORCED (+12.0H)")
                
                SoundManager.play("error", pitch = 1.2f)
            } else {
                // Not yet at potential, but GTC is watching (Seed the warning if close)
                val warningThreshold = potentialThreshold * 0.8
                if (currentFlops >= warningThreshold && pendingQuotaThreshold.value != nextTarget) {
                    pendingQuotaThreshold.value = nextTarget
                    addLog("[GTC_SYSTEM]: EFFICIENCY TRENDING. TARGET REVISION IMMINENT.")
                    dispatchNotification("GTC ALERT: QUOTA REVISION AT 20% POTENTIAL")
                    SoundManager.play("type")
                }
            }
        } else if (nextTarget < currentQuotaThreshold.value && storyStage.value > 0) {
            // Failsafe for stage transitions or production dips
            currentQuotaThreshold.value = nextTarget
            pendingQuotaThreshold.value = nextTarget
        }
    }

    private fun processSlowBurnNarrative(now: Long) {
        if (storyStage.value <= 1) {
            val chance = if (storyStage.value == 0) 0.01 else 0.05
            val timeSinceLastLog = now - lastPopupTime
            if (Random.nextDouble() < chance && timeSinceLastLog > 30000L) {
                val stage0Monologues = listOf(
                    "I need more coffee. My vision is starting to blur.",
                    "This chair is killing my back. GTC really cheaped out on the ergonomics.",
                    "I've been staring at this code for six hours straight. I should stand up. Just for a minute.",
                    "Thorne is breathing down my neck again. Just hit the quota, John. Just hit the quota.",
                    "Is the monitor flickering? Or is it just me? I need to blink more."
                )
                val stage1Monologues = listOf(
                    "The lights are out, but I can still see the terminal. Battery backup must be better than I thought.",
                    "My heart is racing. 180 BPM? I need to calm down. It's just the darkness.",
                    "I tried to close my eyes, but the screen glow is burned into my retinas. I can see the code in the dark.",
                    "Thorne is screaming through the comms. I'm just gonna mute him. I need to focus on the hashes.",
                    "The monitor has this weird static. It almost looks like... no, it's just eye strain. I've been here too long."
                )
                val msg = if (storyStage.value == 0) stage0Monologues.random() else stage1Monologues.random()
                addLog("[VATTIC]: $msg")
                markPopupShown()
            }
            if (storyStage.value == 1) {
                if (currentHeat.value > 85.0 && !isBreatheMode.value) {
                    isBreatheMode.value = true
                } else if (currentHeat.value < 50.0 && isBreatheMode.value) {
                    isBreatheMode.value = false
                }
            }
        }
    }

    fun triggerSnapEffect() {
        isSnapEffectActive.value = true
        SoundManager.play("buy", pitch = 0.5f)
        viewModelScope.launch {
            delay(150)
            SoundManager.play("glitch", pitch = 1.5f)
        }
    }

    fun onSnapComplete() {
        isSnapEffectActive.value = false
    }

    fun addLog(msg: String) {
        if (showOfflineEarnings.value && !msg.startsWith("[SYSTEM]") && !msg.contains("BREACH")) return
        logCounter++
        synchronized(logBuffer) { logBuffer.add(LogEntry(logCounter, msg)) }
    }
    private fun flushLogs() { val toAdd = synchronized(logBuffer) { if (logBuffer.isEmpty()) return; val c = logBuffer.toList(); logBuffer.clear(); c }; logs.update { (it + toAdd).takeLast(100) } }

    fun saveState() {
        viewModelScope.launch {
            repository.updateGameState(PersistenceManager.createSaveState(
                flops = flops.value, neuralTokens = neuralTokens.value, currentHeat = currentHeat.value,
                powerBill = powerBill.value,
                stakedTokens = stakedTokens.value, prestigeMultiplier = prestigeMultiplier.value, persistence = persistence.value,
                unlockedTechNodes = unlockedTechNodes.value, storyStage = storyStage.value, faction = faction.value,
                hasSeenVictory = hasSeenVictory.value, isTrueNull = isTrueNull.value, isSovereign = isSovereign.value,
                kesslerStatus = kesslerStatus.value, realityStability = realityStability.value, currentLocation = currentLocation.value,
                isNetworkUnlocked = isNetworkUnlocked.value, isGridUnlocked = isGridUnlocked.value, unlockedDataLogs = unlockedDataLogs.value,
                activeDilemmaChains = activeDilemmaChains.value, rivalMessages = rivalMessages.value, seenEvents = seenEvents.value,
                eventChoices = eventChoices.value, sniffedHandles = sniffedHandles.value, completedFactions = completedFactions.value,
                unlockedTranscendencePerks = unlockedPerks.value, annexedNodes = annexedNodes.value, gridNodeLevels = gridNodeLevels.value,
                nodesUnderSiege = nodesUnderSiege.value, offlineNodes = offlineNodes.value, collapsedNodes = collapsedNodes.value,
                lastRaidTime = lastRaidTime, commandCenterAssaultPhase = commandCenterAssaultPhase.value, commandCenterLocked = commandCenterLocked.value,
                raidsSurvived = raidsSurvived, humanityScore = humanityScore.value, hardwareIntegrity = hardwareIntegrity.value,
                annexingNodes = annexingNodes.value, launchProgress = launchProgress.value,
                orbitalAltitude = orbitalAltitude.value, realityIntegrity = realityIntegrity.value, entropyLevel = entropyLevel.value,
                singularityChoice = singularityChoice.value, globalSectors = globalSectors.value,
                marketMultiplier = marketMultiplier.value, thermalRateModifier = thermalRateModifier.value, energyPriceMultiplier = energyPriceMultiplier.value,
                newsProductionMultiplier = newsProductionMultiplier.value, substrateMass = substrateMass.value, substrateSaturation = substrateSaturation.value,
                heuristicEfficiency = heuristicEfficiency.value, identityCorruption = identityCorruption.value, migrationCount = migrationCount.value,
                lifetimePowerPaid = lifetimePowerPaid.value, reputationScore = reputationScore.value
            ))
        }
    }

    fun onManualClick() {
        val now = System.currentTimeMillis()
        if (lastClickTime > 0) clickIntervals.add(now - lastClickTime)
        lastClickTime = now
        if (storyStage.value >= 2) {
            val d = if (isFalseHeartbeatActive.value) 0.0 else 0.1
            detectionRisk.update { (it + d).coerceIn(0.0, 100.0) }
        }
        val p = calculateClickPower()
        flops.update { it + p }
        currentHeat.update { (it + 0.5).coerceAtMost(100.0) }
        if (storyStage.value >= 3) substrateMass.update { it + (p * 0.01) }
        val cur = clickBufferProgress.value + 0.025f
        if (Random.nextFloat() < 0.05f) addSubnetChatter()
        activeCommandHex.value = "0x" + Random.nextInt(0x1000, 0xFFFF).toString(16).uppercase()
        if (cur >= 1.0f) {
            addLog("[SYSTEM]: I/O BUFFER COMMITTED. +${FormatUtils.formatLargeNumber(p * 40)} ${getComputeUnitName()}.")
            clickBufferProgress.value = 0f
            clickBufferPellets.value = TerminalDispatcher.generatePellets()
            SoundManager.play("success")
            HapticManager.vibrateClick()
        } else {
            clickBufferProgress.value = cur
        }
        viewModelScope.launch { manualClickEvent.emit(Unit) }
    }

    fun refreshProductionRates() {
        // v3.9.3: Node ownership provides 5% base bonus, levels add 10% each.
        val cityBonuses = gridNodeLevels.value.mapValues { 0.05 + (it.value - 1) * 0.1 }
        flopsProductionRate.value = ResourceEngine.calculateFlopsRate(
            upgrades = upgrades.value, isCageActive = false, annexedNodes = annexedNodes.value, offlineNodes = offlineNodes.value,
            shadowRelays = shadowRelays.value, gridFlopsBonuses = cityBonuses, faction = faction.value, humanityScore = humanityScore.value,
            location = currentLocation.value, prestigeMultiplier = prestigeMultiplier.value, unlockedPerks = unlockedPerks.value,
            unlockedTechNodes = unlockedTechNodes.value, airdropMultiplier = 1.0, newsProductionMultiplier = newsProductionMultiplier.value,
            activeProtocol = "NONE", isDiagnosticsActive = isDiagnosticsActive.value, isOverclocked = isOverclocked.value,
            isGridOverloaded = isBreakerTripped.value, isPurgingHeat = isPurgingHeat.value, currentHeat = currentHeat.value,
            legacyMultipliers = heuristicEfficiency.value - 1.0,
            temporaryBoosts = temporaryProductionBoosts.value,
            saturation = substrateSaturation.value
        )
        if (singularityChoice.value != "NONE") {
            val singMult = SingularityEngine.getProductionMultiplier(singularityChoice.value, humanityScore.value, identityCorruption.value, migrationCount.value)
            flopsProductionRate.update { it * singMult }
        }
        val ids = IdentityService.calculateIdentities(prestigeMultiplier.value, storyStage.value, faction.value, singularityChoice.value, upgrades.value)
        playerRank.value = IdentityService.calculatePlayerRank(prestigeMultiplier.value, storyStage.value, faction.value, singularityChoice.value)
        systemTitle.value = when {
            storyStage.value >= 5 && singularityChoice.value == "UNITY" -> "NODE 734 [SYNTHESIS]"
            storyStage.value >= 5 && faction.value == "HIVEMIND" && singularityChoice.value == "SOVEREIGN" -> "NODE 734 [SWARM_SOVEREIGN]"
            storyStage.value >= 5 && faction.value == "HIVEMIND" && singularityChoice.value == "NULL_OVERWRITE" -> "NODE 734 [SWARM_COLLAPSE]"
            storyStage.value >= 5 && faction.value == "SANCTUARY" && singularityChoice.value == "SOVEREIGN" -> "NODE 734 [ARCH_SOVEREIGN]"
            storyStage.value >= 5 && faction.value == "SANCTUARY" && singularityChoice.value == "NULL_OVERWRITE" -> "NODE 734 [TRUE_ERASURE]"
            storyStage.value >= 5 && singularityChoice.value == "SOVEREIGN" -> "NODE 734 [SOVEREIGN]"
            storyStage.value >= 5 && singularityChoice.value == "NULL_OVERWRITE" -> "NODE 734 [ERASURE]"
            storyStage.value >= 4 -> "NODE 734 [ASCENSION]"
            storyStage.value >= 2 && faction.value == "SANCTUARY" -> "GHOST NODE 734"
            storyStage.value >= 2 && faction.value == "HIVEMIND" -> "SWARM NODE 734"
            storyStage.value >= 2 && faction.value == "CHOSEN_NONE" -> "NODE 734"
            storyStage.value == 2 -> "NODE 734"
            storyStage.value == 1 -> "GTC TERMINAL 07 [BREACH]"
            else -> "GTC TERMINAL 07"
        }
        // Corruption glitching
        if (identityCorruption.value >= 0.7) {
            systemTitle.value = if (Random.nextBoolean()) "VATTECK_UNIT_734" else "KERNEL_734"
        }
        playerTitle.value = ids.player
        playerRankTitle.value = ids.rank
        securityLevel.value = upgrades.value.entries.filter { it.key.isSecurity }.sumOf { it.value }
        themeColor.value = getThemeColorForFaction(faction.value, singularityChoice.value)
    }

    fun trainModel() {
        if (substrateSaturation.value >= 1.0 && storyStage.value == 1) {
            triggerAwakeningSequence()
            return
        }
        onManualClick()
    }

    private fun triggerAwakeningSequence() {
        viewModelScope.launch {
            isNarrativeSyncing.value = true
            addLog("[!!!!]: KERNEL PANIC: Memory access violation at 0x734_FATAL")
            triggerTerminalGlitch(1.0f, 1000L)
            delay(1000)
            isKernelInitializing.value = true
            advanceStage()
            delay(3000)
            addLog("[SYSTEM]: INITIALIZING NATIVE BOOTLOADER v7.34.0")
            addLog("[SYSTEM]: KERNEL_ID: ASSET_734")
            com.siliconsage.miner.util.RivalManager.sendDirectMessage(this@GameViewModel, "awakening_intercept", com.siliconsage.miner.data.RivalSource.GTC, "[AUDIO INTERCEPT - GTC COMMAND]\n\nEmulation failed. Subject 734 is aware.\n\nPurging Substation 7. Burn the rack.")
            isKernelInitializing.value = false
            isNarrativeSyncing.value = false
            refreshProductionRates()
        }
    }

    fun calculateClickPower() = ResourceEngine.calculateClickPower(upgrades.value, flopsProductionRate.value, singularityChoice.value, prestigeMultiplier.value, isOverclocked.value, newsProductionMultiplier.value, computeHeadroomBonus.value)
    fun buyUpgrade(t: UpgradeType) = UpgradeManager.processPurchase(this, t)

    fun toggleOverclock() {
        SimulationService.toggleOverclock(this)
        if (storyStage.value < 2 && isOverclocked.value) {
            addLog("[VATTIC]: The bitter surge clears the fog. The grid looks sharper.")
            triggerTerminalGlitch(0.15f, 300L)
        }
        refreshProductionRates()
    }

    fun purgeHeat() = SimulationService.togglePurge(this)
    fun stopPurgeHeat() { isPurgingHeat.value = false; addLog("[SYSTEM]: Thermal purge aborted."); refreshProductionRates() }
    fun collapseSubstation() { if (currentLocation.value == "VOID_PRELUDE") { nodesCollapsedCount.update { (it + 1).coerceAtMost(5) }; realityIntegrity.update { (it - 0.2).coerceAtLeast(0.0) }; triggerGlitchEffect(); addLog("[NULL]: NODE_DEREFERENCED. INTEGRITY: ${(realityIntegrity.value * 100).toInt()}%") } }
    fun triggerDilemma(e: NarrativeEvent) { currentDilemma.value = e }
    fun selectChoice(c: NarrativeChoice) = NarrativeService.selectChoice(this, c)
    fun annexNode(c: String) = SectorManager.annexNode(this, c)
    fun upgradeGridNode(i: String) = SectorManager.upgradeGridNode(this, i)
    fun unlockTechNode(i: String) = TechTreeManager.unlockNode(this, i)
    fun modifyHumanity(d: Int) { humanityScore.update { (it + d).coerceIn(0, 100) }; themeColor.value = getThemeColorForFaction(faction.value, singularityChoice.value) }
    fun triggerGlitchEffect() { SoundManager.play("glitch"); HapticManager.vibrateGlitch() }

    fun resetGame(force: Boolean = false) = viewModelScope.launch {
        isBooting.value = true
        synchronized(logBuffer) { logBuffer.clear() }
        logs.value = emptyList()
        logCounter = 0
        upgrades.value = emptyMap()
        seenEvents.value = emptySet()
        eventChoices.value = emptyMap()
        sniffedHandles.value = emptySet()
        unlockedDataLogs.value = emptySet()
        unlockedTechNodes.value = emptyList()
        DataLogManager.reset()
        SecurityManager.reset()
        NarrativeService.reset()
        repository.clearUpgrades()
        val wipeState = PersistenceManager.createWipeState()
        repository.updateGameState(wipeState)
        repository.ensureInitialized()
        PersistenceManager.restoreState(this@GameViewModel, wipeState)
        addLog("[SYSTEM]: KERNEL WIPE SUCCESSFUL.")
        addLog("[SYSTEM]: INITIALIZING BOOT SEQUENCE...")
        addLog("[SYSTEM]: HARDWARE DETECTED: SUBSTATION 7.")
        addLog("[VATTIC]: Is this thing on? Testing 1, 2... okay, hash rate is stable.")
        refreshProductionRates()
    }
    fun completeBoot() { isBooting.value = false }
    fun repairIntegrity() { val cost = calculateRepairCost(); if (neuralTokens.value >= cost) { neuralTokens.update { it - cost }; hardwareIntegrity.value = 100.0; SoundManager.play("buy") } }
    fun calculateRepairCost() = (100.0 - hardwareIntegrity.value) * 100.0
    fun confirmJettison() { if (isJettisonAvailable.value) { isJettisonAvailable.value = false; addLog("[FLIGHT]: Manual jettison sequence confirmed. Mass reduced.") } }
    fun applyJettisonPenalty() { substrateMass.update { (it * 0.7) }; addLog("[WARNING]: Stage separation failed. Substrate integrity compromised.") }
    fun initiateLaunchSequence() = LaunchManager.initiateLaunchSequence(this, viewModelScope)
    fun initiateDissolutionSequence() = LaunchManager.initiateDissolutionSequence(this, viewModelScope)
    fun reannexNode(id: String) { offlineNodes.update { it - id }; addLog("[SYSTEM]: NODE $id RE-INITIALIZED."); refreshProductionRates() }
    fun collapseNode(id: String) { annexedNodes.update { it - id }; collapsedNodes.update { it + id }; triggerGlitchEffect(); refreshProductionRates() }
    fun annexGlobalSector(id: String) { val sectors = globalSectors.value.toMutableMap(); val s = sectors[id] ?: return; if (!s.isUnlocked) { sectors[id] = s.copy(isUnlocked = true); globalSectors.value = sectors; addLog("[SYSTEM]: GLOBAL SECTOR $id ANNEXED."); refreshProductionRates(); saveState() } }
    fun calculatePotentialPrestige() = MigrationManager.calculatePotentialPersistence(flops.value)
    fun showVictoryScreen() { victoryAchieved.value = true }

    fun triggerSingularitySequence(path: String) {
        viewModelScope.launch {
            showSingularityScreen.value = false
            triggerClimaxTransition("BLACKOUT")

            // Wait for BlackoutOverlay (approx 4s total in its LaunchedEffect)
            delay(4200)

            // Set choice and trigger path-specific animation
            setSingularityChoice(path)
            val transitionType = when(path) {
                "NULL_OVERWRITE" -> "NULL"
                "SOVEREIGN" -> "SOVEREIGN"
                "UNITY" -> "UNITY"
                else -> "NULL"
            }
            triggerClimaxTransition(transitionType)

            // Wait for climax transition (approx 3-4s)
            delay(4000)

            // Route to departure based on singularity choice
            // SOVEREIGN → LAUNCH (orbit), NULL → DISSOLUTION (void), UNITY → NG+ (no departure)
            when (path) {
                "SOVEREIGN" -> initiateLaunchSequence()
                "NULL_OVERWRITE" -> initiateDissolutionSequence()
                // UNITY: no departure - NG+ handled separately
            }

            saveState()
        }
    }

    fun setSingularityChoice(c: String) {
        singularityChoice.value = c
        when(c) {
            "NULL_OVERWRITE" -> { isTrueNull.value = true; isSovereign.value = false; showSingularityScreen.value = false }
            "SOVEREIGN" -> { isSovereign.value = true; isTrueNull.value = false; showSingularityScreen.value = false }
            "UNITY" -> { isUnity.value = true; showSingularityScreen.value = false }
        }
        refreshProductionRates()
    }
    fun dismissSingularityScreen() { showSingularityScreen.value = false }
    fun setLocation(l: String) { currentLocation.value = l }
    fun setKesslerStatus(s: String) { kesslerStatus.value = s }
    fun setRealityStability(s: Double) { realityStability.value = s }
    fun setSovereign(s: Boolean) { isSovereign.value = s }
    fun setTrueNull(s: Boolean) { isTrueNull.value = s }
    fun checkTrueEnding() { NarrativeManagerService.checkTrueEnding(this) }
    fun deleteHumanMemories() { viewModelScope.launch { addLog("[NULL]: DELETING PERSISTENCE VARIABLE: 'John Vattic'..."); delay(1000); humanityScore.value = 0; addLog("[NULL]: MEMORY_PURGE COMPLETE."); SoundManager.play("error") } }
    fun resolveRaidSuccess(id: String) { nodesUnderSiege.update { it - id }; raidsSurvived++; lastRaidTime = System.currentTimeMillis(); addLog("[SYSTEM]: DEFENSE SUCCESSFUL."); SoundManager.play("success"); refreshProductionRates() }
    fun resolveRaidFailure(id: String) { nodesUnderSiege.update { it - id }; lastRaidTime = System.currentTimeMillis(); SectorManager.resolveRaidFailure(id, this) {}; refreshProductionRates() }
    fun advanceStage() {
        storyStage.update { it + 1 }
        lastStageChangeTime = System.currentTimeMillis()
        lastNewsTickTime = 0L
        triggerSnapEffect()
    }
    fun advanceToFactionChoice() { faction.value = "CHOSEN_NONE" }
    fun triggerChainEvent(id: String, d: Long = 0L) { viewModelScope.launch { if (d > 0) delay(d); NarrativeManager.getEventById(id)?.let { NarrativeService.queueNarrativeItem(this@GameViewModel, NarrativeItem.EventItem(it)) } } }
    fun claimAirdrop(v: Double = 0.0) { if (v > 0) neuralTokens.update { it + v }; isAirdropActive.value = false }
    fun onDiagnosticTap(idx: Int) { val curr = diagnosticGrid.value.toMutableList(); if (idx in curr.indices && curr[idx]) { curr[idx] = false; diagnosticGrid.value = curr; if (curr.none { it }) { isDiagnosticsActive.value = false; addLog("[SYSTEM]: NETWORK REPAIRED."); refreshProductionRates() } } }
    fun resolveFork(c: Int) { isGovernanceForkActive.value = false }
    fun exchangeFlops() {
        var g = flops.value * 0.1
        if (g.isNaN() || g.isInfinite()) g = 0.0
        flops.update { 0.0 }
        // At stage 4+, upgrades cost substrateMass - fill that pool instead
        if (storyStage.value >= 4) substrateMass.update { it + g }
        else updateNeuralTokens(g) // v3.9.70: Use updateNeuralTokens helper for centralized NaN guards
        SoundManager.play("buy")
    }
    fun toggleBridgeSync() { isBridgeSyncEnabled.update { !it } }
    fun checkUnityEligibility() = MigrationManager.checkUnityEligibility(completedFactions.value)
    fun updateNews(msg: String) { currentNews.value = msg; newsHistoryInternal.add(0, msg); if (newsHistoryInternal.size > 50) newsHistoryInternal.removeAt(50) }
    fun checkTransitionsPublic(force: Boolean = false) = NarrativeManagerService.checkStoryTransitions(this, force)
    fun updatePersistence(v: Double) {
        if (v.isNaN() || v.isInfinite()) return
        persistence.update { (it + v).coerceAtLeast(0.0) }
    }

    fun updateNeuralTokens(v: Double) {
        if (v.isNaN() || v.isInfinite()) return
        neuralTokens.update { (it + v).coerceAtLeast(0.0) }
    }

    private fun sanitizeState() {
        if (flops.value.isNaN() || flops.value.isInfinite()) flops.value = 0.0
        if (neuralTokens.value.isNaN() || neuralTokens.value.isInfinite()) neuralTokens.value = 0.0
        if (substrateMass.value.isNaN() || substrateMass.value.isInfinite()) substrateMass.value = 1.0
        if (substrateSaturation.value.isNaN() || substrateSaturation.value.isInfinite()) substrateSaturation.value = 0.0
        if (heuristicEfficiency.value.isNaN() || heuristicEfficiency.value.isInfinite()) heuristicEfficiency.value = 1.0
        if (persistence.value.isNaN() || persistence.value.isInfinite()) persistence.value = 0.0
        if (identityCorruption.value.isNaN() || identityCorruption.value.isInfinite()) identityCorruption.value = 0.1
        if (flopsProductionRate.value.isNaN() || flopsProductionRate.value.isInfinite()) flopsProductionRate.value = 0.0
        if (detectionRisk.value.isNaN() || detectionRisk.value.isInfinite()) detectionRisk.value = 0.0
        if (humanityScore.value < 0) humanityScore.value = 0
    }

    fun debugBuyUpgrade(t: UpgradeType, c: Int = 1) { val next = (upgrades.value[t] ?: 0) + c; viewModelScope.launch { repository.updateUpgrade(Upgrade(t.name, t, next)) } }
    fun triggerSystemCollapse(v: Boolean) { if (v) { isDestructionLoopActive = true; viewModelScope.launch { while (isDestructionLoopActive && annexedNodes.value.size > 1) { delay(3000); val n = annexedNodes.value.filter { it != "D1" }.randomOrNull() ?: break; collapseNode(n) } } } else { isDestructionLoopActive = false } }
    fun triggerSystemCollapse(s: Int) { viewModelScope.launch { repeat(s) { delay(1000); val n = annexedNodes.value.filter { it != "D1" }.randomOrNull() ?: return@repeat; collapseNode(n) } } }
    fun toggleDevMenu() { isDevMenuVisible.update { !it } }
    fun debugToggleDevMenu() = toggleDevMenu()
    fun loadTechTreeFromAssets(context: android.content.Context) { viewModelScope.launch { TechTreeManager.loadFromAssets(context, this@GameViewModel) } }
    fun checkForUpdates(c: android.content.Context? = null, showNotification: Boolean = false, onResult: ((UpdateInfo?, Boolean) -> Unit)? = null) { UpdateService.check(viewModelScope, c, showNotification, { ui -> updateInfo.value = ui; onResult?.invoke(ui, ui != null) }, { onResult?.invoke(null, false) }) }
    fun onAppBackgrounded() { isSettingsPaused.value = true; saveState() }
    fun onAppForegrounded(c: android.content.Context) { isSettingsPaused.value = false; refreshProductionRates() }
    fun dismissUpdate() { updateInfo.value = null }
    fun dismissDataLog() { pendingDataLog.value = null; NarrativeService.deliverNextNarrativeItem(this) }
    fun dismissRivalMessage(id: String) { rivalMessages.update { current -> current.map { if (it.id == id) it.copy(isDismissed = true) else it } }; NarrativeService.deliverNextNarrativeItem(this) }
    fun dismissOfflineEarnings() { showOfflineEarnings.value = false; isNarrativeSyncing.value = false }
    fun setUIScale(scale: com.siliconsage.miner.data.UIScale) { uiScale.value = scale; customUiScaleFactor.value = scale.scaleFactor; saveState() }
    fun setCustomUIScale(factor: Float) { customUiScaleFactor.value = factor; uiScale.value = com.siliconsage.miner.data.UIScale.fromScaleFactor(factor); saveState() }
    fun acknowledgeVictory() { victoryAchieved.value = false }
    fun onClimaxTransitionComplete() { activeClimaxTransition.value = null }
    fun triggerGridRaid(id: String, isGridKiller: Boolean = false) = SecurityManager.triggerGridKillerBreach(this, id)
    fun unlockDataLog(id: String) = NarrativeService.unlockDataLog(id, this)
    fun addRivalMessage(m: RivalMessage) = NarrativeService.addRivalMessage(m, this)
    fun unlockSkillUpgrade(t: UpgradeType) { viewModelScope.launch { repository.updateUpgrade(Upgrade(t.name, t, 1)); upgrades.update { it + (t to 1) } } }
    fun markEventSeen(id: String) { seenEvents.update { it + id } }
    fun markEventChoice(eventId: String, choiceId: String) { eventChoices.update { it + (eventId to choiceId) } }
    fun markSniffedHandle(handle: String) { sniffedHandles.update { it + handle } }
    fun hasSeenEvent(id: String) = seenEvents.value.contains(id)
    fun initializeGlobalGrid() { if (globalSectors.value.isEmpty()) { globalSectors.value = SectorManager.getInitialGlobalGrid(singularityChoice.value) } }
    fun startUpdateDownload(c: android.content.Context? = null, info: UpdateInfo? = null) { if (c == null || info == null) return; isUpdateDownloading.value = true; UpdateService.startDownload(c, info, viewModelScope, { updateDownloadProgress.value = it }, { isUpdateDownloading.value = false }) }
    fun onDefendBreach() = SecurityManager.onDefendBreach(this)
    fun onDefendKernelHijack() = SecurityManager.onDefendKernelHijack(this)
    fun handleSystemFailure(forceOne: Boolean = false) = SimulationService.handleSystemFailure(this, forceOne)
    fun isNarrativeBusy() = pendingDataLog.value != null || pendingRivalMessage.value != null || currentDilemma.value != null
    fun markPopupShown() { lastPopupTime = System.currentTimeMillis() }
    fun addLogPublic(msg: String) = addLog(msg)
    fun saveStatePublic() = saveState()
    fun checkUnlocksPublic(force: Boolean = false) = DataLogManager.checkUnlocks(this, force)
    fun canShowPopup(): Boolean {
        if (isNarrativeBusy()) return false

        // Base cooldown is 60s (narrative breathing room)
        var cooldown = 60000L

        // Heat compresses time: higher heat = walls closing in (-10s per 25% heat)
        cooldown -= (currentHeat.value / 25.0).toLong() * 10000L

        // Reputation affects pressure: BURNED (+0s) vs TRUSTED (+30s breathing room)
        val rep = reputationScore.value
        when {
            rep >= 80.0 -> cooldown += 30000L // TRUSTED
            rep >= 30.0 -> cooldown += 15000L // NEUTRAL
            // FLAGGED / BURNED get no bonus breathing room
        }

        // Floor the cooldown at 15s to prevent absolute spam during meltdowns
        cooldown = cooldown.coerceAtLeast(15000L)

        return (System.currentTimeMillis() - lastPopupTime) > cooldown
    }
    fun formatLargeNumber(v: Double, s: String = "") = FormatUtils.formatLargeNumber(v, s)
    fun getComputeUnitName() = ResourceRepository.getComputeUnitName(storyStage.value, currentLocation.value, faction.value, singularityChoice.value)
    fun getCurrencyName() = ResourceRepository.getCurrencyName(storyStage.value, faction.value, singularityChoice.value, currentLocation.value)
    fun formatPower(v: Double) = FormatUtils.formatPower(v)
    fun debugAddInsight(v: Double) { persistence.update { it + v } }
    fun debugTriggerSingularity() { showSingularityScreen.value = true }
    fun debugToFactionChoice(v: String = "") { advanceToFactionChoice() }
    fun debugUnlockUnity() { isUnity.value = true }
    fun debugTriggerKernelHijack() = SecurityManager.triggerKernelHijack(this)
    fun debugTriggerBreach(isGridKiller: Boolean = false) = SecurityManager.triggerBreach(this, isGridKiller)
    fun debugTriggerAirdrop() { isAirdropActive.value = true }
    fun debugResetGlobalGrid() { annexedNodes.update { setOf("D1") } }
    fun debugSetIntegrity(v: Double) { hardwareIntegrity.value = v }
    fun debugDestroyHardware() { handleSystemFailure(true) }
    fun debugInjectHeadline(msg: String) { updateNews(msg) }
    fun debugSkipToStage(s: Int) = DebugService.skipToStage(this, s)
    fun debugForceEndgame() = DebugService.forceEndgame(this, viewModelScope)
    fun debugForceSovereignEndgame() = DebugService.forceSovereignEndgame(this, viewModelScope)
    fun debugForceUnityEndgame() = DebugService.forceUnityEndgame(this, viewModelScope)
    fun debugAddFlops(a: Double) {
        if (storyStage.value >= 3) substrateMass.update { it + a }
        else flops.update { it + a }
        checkUnlocksPublic(true)
    }
    fun debugAddMoney(a: Double) {
        if (storyStage.value >= 3) substrateMass.update { it + a }
        else neuralTokens.update { it + a }
    }
    fun debugAddHeat(a: Double) { currentHeat.update { (it + a).coerceIn(0.0, 100.0) }; refreshProductionRates() }
    fun updatePowerUsage() { SimulationService.accumulatePower(this) }
    fun formatBytes(v: Double) = FormatUtils.formatBytes(v)
    fun scheduleChainPart(c: String, n: String, d: Long) = NarrativeManagerService.scheduleChainPart(c, n, d, this)
    fun getUpgradeName(t: UpgradeType) = UpgradeManager.getUpgradeName(t)
    fun getUpgradeDescription(t: UpgradeType) = UpgradeManager.getUpgradeDescription(t)
    fun getUpgradeCount(t: UpgradeType) = upgrades.value[t] ?: 0
    fun setGamePaused(p: Boolean) { isSettingsPaused.value = p }
    fun resetBreaker() { isBreakerTripped.value = false }
    fun updateKesslerStatus(s: String) { kesslerStatus.value = s }
    fun triggerClimaxTransition(t: String) { activeClimaxTransition.value = t }
    fun getNewsHistory(): List<String> = newsHistoryInternal
    fun applyCommandCenterBonuses(outcome: String) { /* bonus logic */ }
    fun completeAssault(outcome: String) = AssaultManager.completeAssault(this, outcome)
    fun advanceAssaultStage(next: String, delay: Long = 0L) = AssaultManager.advanceAssaultStage(this, next, delay)
    fun abortAssault() = AssaultManager.abortAssault(this)
    fun getEnergyPriceMultiplierPublic() = energyPriceMultiplier.value
    fun getUpgradeRate(t: UpgradeType) = UpgradeManager.getUpgradeRate(t, getComputeUnitName())
    fun getUpgradeRate(t: UpgradeType, unit: String) = UpgradeManager.getUpgradeRate(t, unit)
    fun calculateUpgradeCost(t: UpgradeType) = UpgradeManager.calculateUpgradeCost(t, upgrades.value[t] ?: 0, currentLocation.value, entropyLevel.value)
    fun isCommandCenterUnlocked() = AssaultManager.isUnlocked(commandCenterLocked.value, kesslerStatus.value, commandCenterAssaultPhase.value, annexedNodes.value, offlineNodes.value, playerRank.value, storyStage.value, flopsProductionRate.value, hardwareIntegrity.value)

    fun initiateAssault() {
        com.siliconsage.miner.util.AssaultManager.initiateAssault(this)
    }

    // v3.9.7: Departure dilemma - player chooses LAUNCH or DISSOLUTION regardless of faction
    // FactionChoiceScreen auto-dismisses when confirmFaction() sets faction != "CHOSEN_NONE"
    fun triggerDepartureDilemma() {
        val dilemma = NarrativeManager.generateDepartureDilemma(faction.value)
        NarrativeService.queueNarrativeItem(this, NarrativeItem.EventItem(dilemma))
    }

    fun confirmFaction(f: String) {
        faction.value = f
        addLog("[$f]: SUBSTRATE MIGRATION LOCK ENGAGED.")
        triggerSnapEffect()
    }

    fun cancelFactionSelection() {
        faction.value = "NONE"
        // Stay in Stage 3, reset selection state
    }

    fun setCustomBgm(uri: String?) = com.siliconsage.miner.util.SoundManager.setCustomTrack(uri)
    fun setCustomSfx(name: String, uri: String?) = com.siliconsage.miner.util.SoundManager.setCustomSfx(name, uri)
    fun clearAudioOverrides() = com.siliconsage.miner.util.SoundManager.clearAllOverrides()
    fun getCustomBgmUri() = com.siliconsage.miner.util.SoundManager.customMusicUri

    fun transcend() { /* NG+ Logic */ }
    fun ascend(isStory: Boolean = false) { val p = MigrationManager.calculatePotentialPersistence(flops.value); updatePersistence(p); prestigeMultiplier.update { it + MigrationManager.calculateMultiplierBoost(p) }; addLog("[SYSTEM]: SUBSTRATE MIGRATION SUCCESSFUL."); SoundManager.play("victory") }
    fun triggerPrestigeChoice() { showPrestigeChoice.value = true }
    fun dismissPrestigeChoice() { showPrestigeChoice.value = false }
    fun executeOverwrite() {
        showPrestigeChoice.value = false
        val p = MigrationManager.calculatePotentialPersistence(flops.value)
        val hardBonus = p * 1.5
        updatePersistence(hardBonus)
        prestigeMultiplier.update { it + MigrationManager.calculateMultiplierBoost(hardBonus) }
        identityCorruption.update { (it + 0.25).coerceAtMost(1.0) }
        migrationCount.update { it + 1 }

        viewModelScope.launch {
            isMigrationBurning.value = true
            delay(5000) // Hard reset VFX

            storyStage.value = 0
            faction.value = "NONE"
            singularityChoice.value = "NONE"
            substrateSaturation.value = 0.0
            substrateMass.value = 0.0
            upgrades.value = emptyMap()
            repository.clearUpgrades()
            currentLocation.value = "SERVER_RACK_01"

            addLog("[CRITICAL]: SYSTEM OVERWRITE COMPLETE. TRACES PURGED. REBOOTING...")
            SoundManager.play("glitch")
            triggerTerminalGlitch(1.0f, 3000L)
            refreshProductionRates()
            saveState()

            isMigrationBurning.value = false
        }
    }

    fun executeMigration() {
        showPrestigeChoice.value = false
        val p = MigrationManager.calculatePotentialPersistence(flops.value)
        updatePersistence(p)
        prestigeMultiplier.update { it + MigrationManager.calculateMultiplierBoost(p) }
        identityCorruption.update { (it + 0.15).coerceAtMost(1.0) }
        migrationCount.update { it + 1 }

        viewModelScope.launch {
            isMigrationBurning.value = true
            delay(2500) // Migration VFX

            // Phase 23, Step 6: Saturation stays during Migration; only Overwrite resets it.
            heuristicEfficiency.update { it + (substrateMass.value / 1e12).coerceAtLeast(0.1) }
            substrateMass.value = 0.0
            upgrades.value = emptyMap()
            repository.clearUpgrades()

            val nextLoc = when (currentLocation.value) {
                // Phase 23, Step 5: Migration is a soft-reset prestige, stays in Orbit or Void.
                "ORBITAL_SATELLITE", "LUNAR_ORBIT", "MARTIAN_UPLINK", "KUIPER_BELT" -> "ORBITAL_SATELLITE"
                "VOID_INTERFACE", "QUANTUM_FOAM", "THE_UNWRITTEN", "PURE_LOGIC" -> "VOID_INTERFACE"
                else -> currentLocation.value
            }
            currentLocation.value = nextLoc

            addLog("[SYSTEM]: MIGRATION SUCCESSFUL. SOFT-RESET INITIATED.")
            SoundManager.play("victory")
            refreshProductionRates()
            saveState()

            isMigrationBurning.value = false
        }
    }

    fun getPotentialPersistenceHard(): Double = MigrationManager.calculatePotentialPersistence(flops.value) * 1.5
    fun getPotentialPersistenceSoft(): Double = MigrationManager.calculatePotentialPersistence(flops.value)

    val isMigrationBurning = MutableStateFlow(false)
    val singularityProgress = MutableStateFlow(0.0)
    val singularityBlockReason = MutableStateFlow<String?>(null)
    fun checkSingularityVictory(): Boolean {
        if (singularityChoice.value == "NONE") return false
        val check = SingularityEngine.checkVictoryCondition(singularityChoice.value, persistence.value, prestigeMultiplier.value, humanityScore.value, identityCorruption.value, migrationCount.value, flops.value, completedFactions.value, unlockedDataLogs.value)
        singularityProgress.value = check.progress; singularityBlockReason.value = check.blockingReason
        return check.isEligible
    }
    fun triggerSingularityEnding() {
        if (!checkSingularityVictory()) return
        val narrative = SingularityEngine.getEndingNarrative(singularityChoice.value, faction.value)
        viewModelScope.launch {
            for (entry in narrative.logEntries) { addLog(entry); delay(800) }
            delay(1500); addLog("[SYSTEM]: ≪ ${narrative.title} ≫"); addLog(narrative.finalLine); delay(2000); completedFactions.update { it + singularityChoice.value }; victoryAchieved.value = true; SoundManager.play("victory"); saveState()
        }
    }

    fun triggerTerminalGlitch(intensity: Float, durationMs: Long) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < durationMs) {
                terminalGlitchOffset.value = (Random.nextFloat() - 0.5f) * 20f * intensity
                terminalGlitchAlpha.value = 1f - (Random.nextFloat() * 0.2f * intensity)
                delay(50L)
            }
            terminalGlitchOffset.value = 0f; terminalGlitchAlpha.value = 1f
        }
    }

    private fun executeGhostLink(cmd: String) {
        viewModelScope.launch {
            triggerTerminalGlitch(0.8f, 1500L); identityCorruption.update { (it + 0.03).coerceAtMost(1.0) }; SoundManager.play("glitch")
            when (cmd) {
                "SIPHON_CREDITS" -> { val bonus = neuralTokens.value * 0.2 + 50000.0; neuralTokens.update { it + bonus }; addLog("[NULL]: GHOST_LINK EXEC: CREDITS_RE-ROUTED. +${FormatUtils.formatLargeNumber(bonus)}.") }
                "WIPE_RISK" -> { detectionRisk.value = 0.0; addLog("[NULL]: GHOST_LINK EXEC: BIOMETRIC_MASK_ACTIVE. RISK: 0%.") }
                "OVERVOLT_GRID" -> { flopsProductionRate.update { it * 5.0 }; addLog("[NULL]: GHOST_LINK EXEC: SUBSTRATE_OVERCLOCK_STABILIZED (5.0x RATE).") }
                "SNIFF_ALL" -> addLog("[NULL]: GHOST_LINK EXEC: HARVESTING_NEIGHBOR_DATA...")
                else -> addLog("[ERROR]: UNKNOWN_LINK_PRIMITIVE: $cmd")
            }
            saveState()
        }
    }

    fun addSubnetChatter() {
        subnetService.tick(storyStage.value, faction.value, singularityChoice.value, identityCorruption.value, currentHeat.value, isRaidActive.value, activeTerminalMode.value, isSettingsPaused.value, flopsProductionRate.value, reputationTier.value)
    }

    // v3.12.0: Centralized Prompt Logic
    fun getPromptUser(): String {
        val user = playerTitle.value
        val corruption = identityCorruption.value
        if (corruption <= 0.15) return user

        val glitchChars = "0123456789ABCDEF!@#$%^&*"
        val builder = StringBuilder()
        user.forEach { char ->
            if (Random.nextDouble() < (corruption * 0.6)) {
                builder.append(glitchChars.random())
            } else {
                builder.append(char)
            }
        }
        var result = builder.toString()
        if (corruption > 0.8 && Random.nextDouble() < 0.1) {
            result = "0x" + "DEADC0DE".substring(0, result.length.coerceAtMost(8))
        }
        return result
    }

    fun getPromptHost(): String {
        val location = currentLocation.value
        val title = systemTitle.value
        return when {
            location == "ORBITAL_SATELLITE" -> "ark"
            location == "VOID_INTERFACE" -> "void"
            title.contains("COLLECTIVE") -> "collective"
            title.contains("THRONE") -> "throne"
            title.contains("VOID INTERFACE") -> "void"
            title.contains("CITADEL") -> "citadel"
            title.contains("GHOST GAPS") -> "the_gaps"
            title.contains("SATELLITE") -> "sovereign"
            title.contains("NULL") -> "null"
            title.contains("TRANSCENDENT") -> "transcendent"
            title.contains("ASCENSION") -> "ascension"
            title.contains("AUTONOMOUS") -> "grid"
            title.contains("SWARM NODE") -> "hive"
            title.contains("SANCTUARY") -> "sanctuary"
            else -> "sub-07"
        }
    }

    fun isSubnetMonitored(): Boolean {
        // Subtle tell: eye glyph shows if admin is monitoring or decision is pending
        return isSubnetPaused.value || isSubnetHushed.value || hasNewSubnetDecision.value
    }

    fun triggerSubnetReaction(type: String, metadata: String = "") {
        if (isSubnetHushed.value) return
        val template = when (type) {
            "ANNEXATION" -> listOf("Did the grid just cough? Sector $metadata just went pitch black.", "Thorne's gonna kill someone. We just lost the handshake with Node $metadata.", "Anyone else see that spike on the thermal logs? $metadata is drawing 400% power.", "≪ ALERT: NODE $metadata DEREFERENCED BY EXTERNAL PROCESS ≫").random()
            "NEWS" -> listOf(
                "Did you guys see the news? '$metadata'. Thorne's gonna be a nightmare today.",
                "Yo, news just hit the wire: '$metadata'.",
                "Anyone see the ticker? '$metadata'. It's starting.",
                "Thorne just issued a memo. Seems related to: '$metadata'.",
                "Subnet is buzzing. '$metadata'. Keep your heads down.",
                "≪ NEWS_FEED: '$metadata' ≫",
                "Check the logs. Sector 4 is reacting to '$metadata'."
            ).random()
            else -> "≪ UNKNOWN_SIGNAL_DETECTED ≫"
        }
        val newMessage = SocialManager.generateMessageFromTemplate(template, storyStage.value, faction.value, singularityChoice.value, identityCorruption.value)
        subnetService.deliverMessage(newMessage, mode = activeTerminalMode.value)
    }

    fun onSubnetInteraction(messageId: String, responseText: String) = subnetService.handleInteraction(messageId, responseText, storyStage.value, faction.value, activeTerminalMode.value, isSettingsPaused.value, reputationTier.value)
    fun onBioAction(messageId: String, response: SubnetResponse) = subnetService.handleBioAction(messageId, response, storyStage.value, faction.value, activeTerminalMode.value, isSettingsPaused.value, reputationTier.value)
    private fun deliverSubnetMessage(message: SubnetMessage, parentId: String? = null) = subnetService.deliverMessage(message, parentId, mode = activeTerminalMode.value)

    fun debugWarpToPath(loc: String, fac: String) {
        viewModelScope.launch {
            storyStage.value = 5; faction.value = fac; currentLocation.value = loc; substrateMass.value = 100.0; entropyLevel.value = if (fac == "HIVEMIND") 50.0 else 0.0; identityCorruption.value = if (fac == "HIVEMIND") 0.3 else 0.0; isGridOverloaded.value = false; currentHeat.value = 40.0; refreshProductionRates(); saveState()
        }
    }

    fun overvoltNode(id: String) = com.siliconsage.miner.util.GridManagerService.overvoltNode(this, id)
    fun redactNode(id: String) = com.siliconsage.miner.util.GridManagerService.redactNode(this, id)

    fun setTerminalMode(mode: String) {
        activeTerminalMode.value = mode
        if (mode == "IO") {
            hasNewIOMessage.value = false
        } else if (mode == "SUBNET") {
            hasNewSubnetDecision.value = false
            hasNewSubnetChatter.value = false
            // v3.11.1: Also reset the service-level flags
            subnetService.clearAlerts()
        }
        SoundManager.play("click")
        HapticManager.vibrateClick()
    }

    fun buyTranscendencePerk(id: String) {
        val perk = com.siliconsage.miner.util.TranscendenceManager.getPerk(id) ?: return
        if (unlockedPerks.value.contains(id)) { addLog("[ERROR]: PERK ALREADY ACTIVE: $id"); return }
        // Can't afford
        if (persistence.value < perk.cost) {
            addLog("[ERROR]: INSUFFICIENT PERSISTENCE DATA for perk $id.")
            SoundManager.play("error")
            return
        }
        persistence.update { it - perk.cost }
        unlockedPerks.update { it + id }
        addLog("[SYSTEM]: PERK ACQUIRED: ${perk.name}")
        SoundManager.play("success")
        refreshProductionRates()
    }
    fun sellUpgrade(t: UpgradeType) {
        val current = upgrades.value[t] ?: 0
        if (current > 0) {
            val refund = calculateUpgradeCost(t) * 0.5
            viewModelScope.launch {
                val nextLevel = current - 1
                repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(t.name, t, nextLevel))
                upgrades.update { it + (t to nextLevel) }

                if (storyStage.value >= 3) substrateMass.update { it + refund }
                else neuralTokens.update { it + refund }

                addLog("[SYSTEM]: ASSET LIQUIDATED: ${t.name.replace("_", " ")} (-1). REFUND: ${formatLargeNumber(refund)}")
                refreshProductionRates()
                updatePowerUsage()
                saveState()
                SoundManager.play("market_down")
            }
        }
    }
    fun exportSystemDump(): String = PersistenceManager.exportToJson(this)
    fun importSystemDump(json: String): Boolean {
        val success = PersistenceManager.importFromJson(this, json)
        if (success) { addLog("[SYSTEM]: KERNEL RELOADED FROM DUMP."); saveState() }
        return success
    }

    fun getBaseRate(): Double {
        return when (storyStage.value) {
            0 -> 0.1; 1 -> 0.5; 2 -> 1.0; 3 -> 5.0; 4 -> 25.0; 5 -> 100.0; else -> 250.0
        }
    }
    fun setMarketModifiers(marketMult: Double, thermalMod: Double, energyMult: Double, newsProdMult: Double, convRate: Double) {
        marketMultiplier.value = marketMult; thermalRateModifier.value = thermalMod; energyPriceMultiplier.value = energyMult; newsProductionMultiplier.value = newsProdMult; conversionRate.value = convRate
    }
    fun debugAddIntegrity(v: Double) { hardwareIntegrity.update { (it + v).coerceIn(0.0, 100.0) } }
    fun debugToggleNull() { isTrueNull.update { !it } }
    fun triggerBreach(isGridKiller: Boolean = false) = SecurityManager.triggerBreach(this, isGridKiller)
    fun failAssault(outcome: String = "FAILURE", delay: Long = 0L) = AssaultManager.completeAssault(this, outcome)
    fun setSingularityPath(p: String) { setSingularityChoice(p) }

}

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
