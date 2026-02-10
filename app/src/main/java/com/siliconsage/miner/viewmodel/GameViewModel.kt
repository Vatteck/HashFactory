package com.siliconsage.miner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.GameRepository
import com.siliconsage.miner.data.GameState
import com.siliconsage.miner.data.LogEntry
import com.siliconsage.miner.data.TechNode
import com.siliconsage.miner.data.Upgrade
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.data.RivalMessage
import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.DilemmaChain
import com.siliconsage.miner.data.ScheduledPart
import com.siliconsage.miner.data.SectorState
import com.siliconsage.miner.data.getThemeColorForFaction
import com.siliconsage.miner.util.*
import com.siliconsage.miner.domain.engine.ResourceEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.random.Random

sealed class NarrativeItem {
    data class Log(val dataLog: DataLog) : NarrativeItem()
    data class Message(val rivalMessage: RivalMessage) : NarrativeItem()
    data class Event(val narrativeEvent: NarrativeEvent) : NarrativeItem()
}

enum class ResonanceTier {
    NONE, HARMONIC, SYMPHONIC, TRANSCENDENT
}

data class ResonanceState(
    val isActive: Boolean = false,
    val intensity: Float = 0f,
    val tier: ResonanceTier = ResonanceTier.NONE,
    val ratio: Double = 1.0
)

class GameViewModel(val repository: GameRepository) : ViewModel() {
    // --- State Flows ---
    val activeClimaxTransition = MutableStateFlow<String?>(null)
    val flops = MutableStateFlow(0.0)
    val neuralTokens = MutableStateFlow(0.0)
    val voidFragments = MutableStateFlow(0.0)
    val celestialData = MutableStateFlow(0.0)
    val stakedTokens = MutableStateFlow(0.0)
    val prestigePoints = MutableStateFlow(0.0)
    val prestigeMultiplier = MutableStateFlow(1.0)
    val logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val upgrades = MutableStateFlow<Map<UpgradeType, Int>>(emptyMap())
    val currentHeat = MutableStateFlow(0.0)
    val powerBill = MutableStateFlow(0.0)
    val activePowerUsage = MutableStateFlow(0.0)
    val maxPowerkW = MutableStateFlow(100.0)
    val isGridOverloaded = MutableStateFlow(false)
    val isBreakerTripped = MutableStateFlow(false)
    val heatGenerationRate = MutableStateFlow(0.0)
    val flopsProductionRate = MutableStateFlow(0.0)
    val isOverclocked = MutableStateFlow(false)
    val isPurgingHeat = MutableStateFlow(false)
    val hardwareIntegrity = MutableStateFlow(100.0)
    val securityLevel = MutableStateFlow(0)
    val prestigePointsPostSingularity = MutableStateFlow(0)
    val storyStage = MutableStateFlow(0)
    val isDevMenuVisible = MutableStateFlow(false)
    val faction = MutableStateFlow("NONE")
    val playerRank = MutableStateFlow(0)
    val playerRankTitle = MutableStateFlow("MINER")
    val victoryAchieved = MutableStateFlow(false)
    val hasSeenVictory = MutableStateFlow(false)
    val unlockedDataLogs = MutableStateFlow<Set<String>>(emptySet())
    val seenEvents = MutableStateFlow<Set<String>>(emptySet())
    val rivalMessages = MutableStateFlow<List<RivalMessage>>(emptyList())
    val pendingDataLog = MutableStateFlow<DataLog?>(null)
    val pendingRivalMessage = MutableStateFlow<RivalMessage?>(null)
    val currentDilemma = MutableStateFlow<NarrativeEvent?>(null)
    val isTrueNull = MutableStateFlow(false)
    val isSovereign = MutableStateFlow(false)
    val vanceStatus = MutableStateFlow("ACTIVE")
    val realityStability = MutableStateFlow(1.0)
    val currentLocation = MutableStateFlow("SUBSTATION_7")
    val isNetworkUnlocked = MutableStateFlow(false)
    val isGridUnlocked = MutableStateFlow(false)
    val annexedNodes = MutableStateFlow<Set<String>>(setOf("D1"))
    val annexingNodes = MutableStateFlow<Map<String, Float>>(emptyMap())
    val gridNodeLevels = MutableStateFlow<Map<String, Int>>(emptyMap())
    val offlineNodes = MutableStateFlow<Set<String>>(emptySet())
    val nodesUnderSiege = MutableStateFlow<Set<String>>(emptySet())
    val collapsedNodes = MutableStateFlow<Set<String>>(emptySet())
    val launchProgress = MutableStateFlow(0f)
    val orbitalAltitude = MutableStateFlow(0.0)
    val realityIntegrity = MutableStateFlow(1.0)
    val entropyLevel = MutableStateFlow(0.0)
    val resonanceState = MutableStateFlow(ResonanceState())
    val singularityChoice = MutableStateFlow("NONE")
    val showSingularityScreen = MutableStateFlow(false)
    val globalSectors = MutableStateFlow<Map<String, SectorState>>(emptyMap())
    val synthesisPoints = MutableStateFlow(0.0)
    val authorityPoints = MutableStateFlow(0.0)
    val harvestedFragments = MutableStateFlow(0.0)
    val cdLifetime = MutableStateFlow(0.0)
    val vfLifetime = MutableStateFlow(0.0)
    val peakResonanceTier = MutableStateFlow(ResonanceTier.NONE)
    val commandCenterAssaultPhase = MutableStateFlow("NOT_STARTED")
    val commandCenterLocked = MutableStateFlow(false)
    val humanityScore = MutableStateFlow(50)
    val unlockedPerks = MutableStateFlow<Set<String>>(emptySet())
    val completedFactions = MutableStateFlow<Set<String>>(emptySet())
    val isNarrativeSyncing = MutableStateFlow(false)
    val arcadeHighScore = MutableStateFlow(0)
    val lockoutTimer = MutableStateFlow(0)
    val isThermalLockout = MutableStateFlow(false)
    val currentGridPowerBonus = MutableStateFlow(0.0)
    val currentGridFlopsBonus = MutableStateFlow(0.0)
    val techNodes = MutableStateFlow<List<TechNode>>(emptyList())
    val unlockedTechNodes = MutableStateFlow<List<String>>(emptyList())
    val diagnosticGrid = MutableStateFlow(List(9) { false })
    val isDiagnosticsActive = MutableStateFlow(false)
    val isAuditChallengeActive = MutableStateFlow(false)
    val auditTimerRemaining = MutableStateFlow(60)
    val auditTargetHeat = MutableStateFlow(30.0)
    val auditTargetPower = MutableStateFlow(50.0)
    val attackTapsRemaining = MutableStateFlow(0)
    val isKernelHijackActive = MutableStateFlow(false)
    val isAirdropActive = MutableStateFlow(false)
    val isGovernanceForkActive = MutableStateFlow(false)
    val updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val isUpdateDownloading = MutableStateFlow(false)
    val updateDownloadProgress = MutableStateFlow(0f)
    val hallucinationText = MutableStateFlow<String?>(null)
    val showOfflineEarnings = MutableStateFlow(false)
    val isSettingsPaused = MutableStateFlow(false)
    val activeDilemmaChains = MutableStateFlow<Map<String, DilemmaChain>>(emptyMap())
    val isBreachActive = MutableStateFlow(false)
    val breachClicksRemaining = MutableStateFlow(0)
    val nullActive = MutableStateFlow(false) 
    val themeColor = MutableStateFlow("#00FF00")
    val isUnity = MutableStateFlow(false)
    val isAnnihilated = MutableStateFlow(false)
    val currentNews = MutableStateFlow<String?>(null)
    val assaultProgress = MutableStateFlow(0f)
    val isBridgeSyncEnabled = MutableStateFlow(false)
    val offlineStats = MutableStateFlow<Map<String, Double>>(emptyMap())
    val systemTitle = MutableStateFlow("CORE")
    val playerTitle = MutableStateFlow("MINER")
    val clickPulseIntensity = MutableStateFlow(1.0f)
    val conversionRate = MutableStateFlow(0.1)
    val attackTaps = MutableStateFlow(0)
    val auditTimer = MutableStateFlow(0)
    val breachClicks = MutableStateFlow(0)
    val uploadProgress = MutableStateFlow(0f)
    val isAscensionUploading = MutableStateFlow(false)

    // --- Internals ---
    val manualClickEvent = MutableSharedFlow<Unit>(replay = 0)
    var logCounter = 0L
    var purgePowerSpikeTimer = 0
    var purgeExhaustTimer = 0
    var lastPurgeTime = 0L
    var raidsSurvived = 0
    var lastRaidTime = 0L
    val nodeAnnexTimes = mutableMapOf<String, Long>()
    var isDestructionLoopActive = false
    var overheatSeconds = 0
    var assaultPaused = false
    var lastDilemmaTime = 0L
    var lastPopupTime = 0L
    var baseRateInternal = 0.1
    var marketMultiplier = 1.0
    var airdropMultiplier = 1.0
    var thermalRateModifier = 1.0
    var energyPriceMultiplier = 0.15
    var newsProductionMultiplier = 1.0
    var lastNewsTickTime = 0L
    val newsHistoryInternal = mutableListOf<String>()
    val narrativeQueue = mutableListOf<NarrativeItem>()
    var currentPhaseStartTime = 0L
    var currentPhaseDuration = 0L

    init {
        // v3.1.8-dev Restoration: The Logic Pour
        addLog("[SYSTEM]: KERNEL LOADED. INITIALIZING SUBSTRATE...")
        viewModelScope.launch {
            val state = repository.getGameStateOneShot()
            if (state != null) {
                // Basic State Restoration
                flops.value = state.flops
                neuralTokens.value = state.neuralTokens
                celestialData.value = state.celestialData
                voidFragments.value = state.voidFragments
                currentHeat.value = state.currentHeat
                powerBill.value = state.powerBill
                stakedTokens.value = state.stakedTokens
                prestigeMultiplier.value = state.prestigeMultiplier
                prestigePoints.value = state.prestigePoints
                storyStage.value = state.storyStage
                faction.value = state.faction
                humanityScore.value = state.humanityScore
                hardwareIntegrity.value = state.hardwareIntegrity
                currentLocation.value = state.currentLocation
                isTrueNull.value = state.isTrueNull
                isSovereign.value = state.isSovereign
                vanceStatus.value = state.vanceStatus
                realityStability.value = state.realityStability
                
                // v3.1.8 Persistence Restoration
                cdLifetime.value = state.cdLifetime
                vfLifetime.value = state.vfLifetime
                peakResonanceTier.value = try { ResonanceTier.valueOf(state.peakResonanceTier) } catch (e: Exception) { ResonanceTier.NONE }
                
                // v3.1.8-fix: Apply Faction-aware theme color
                themeColor.value = getThemeColorForFaction(faction.value, singularityChoice.value)
                
                refreshProductionRates()
                addLog("[SYSTEM]: DATA HUB CONNECTED. PORT 1 ONLINE.")
                addLog("[SYSTEM]: KERNEL v3.1.8-dev BOOT COMPLETE.")
            } else {
                addLog("[SYSTEM]: NO PREVIOUS STATE FOUND. INITIALIZING...")
                refreshProductionRates()
            }
        }

        // Production Loop (100ms Ticks)
        viewModelScope.launch {
            while (true) {
                delay(100L)
                val results = ResourceEngine.calculatePassiveIncomeTick(
                    flopsPerSec = flopsProductionRate.value,
                    location = currentLocation.value,
                    upgrades = upgrades.value,
                    resonanceBonus = ResourceEngine.getResonanceResourceBonus(resonanceState.value.tier),
                    orbitalAltitude = orbitalAltitude.value,
                    heatGenerationRate = heatGenerationRate.value,
                    entropyLevel = entropyLevel.value,
                    collapsedNodesCount = collapsedNodes.value.size,
                    systemCollapseTimer = null // Placeholder
                )
                
                flops.update { it + results.flopsDelta }
                celestialData.update { it + results.cdDelta }
                voidFragments.update { it + results.vfDelta }
                entropyLevel.update { it + results.entropyDelta }
                
                cdLifetime.update { it + results.cdDelta }
                vfLifetime.update { it + results.vfDelta }
                
                resonanceState.value = resonanceState.value.copy(
                    tier = ResourceEngine.calculateResonance(celestialData.value, voidFragments.value)
                )
                
                if (resonanceState.value.tier.ordinal > peakResonanceTier.value.ordinal) {
                    peakResonanceTier.value = resonanceState.value.tier
                }
            }
        }

        // Thermal & Power Loop (1s Ticks)
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                SimulationService.calculateHeat(this@GameViewModel)
                SimulationService.accumulatePower(this@GameViewModel)
                
                // v3.1.8-fix: Throttled news ticker (every 15s)
                val now = System.currentTimeMillis()
                if (now - lastNewsTickTime > 15000L) {
                    MarketManager.updateMarket(this@GameViewModel)
                    lastNewsTickTime = now
                }

                NarrativeManagerService.checkStoryTransitions(this@GameViewModel)
                DataLogManager.checkUnlocks(this@GameViewModel) // v3.1.8-fix: Hook up lore collectibles
                refreshProductionRates()
                saveState()
            }
        }
    }

    // --- Core Methods ---
    fun addLog(msg: String) { 
        logCounter++
        logs.update { (it + LogEntry(logCounter, msg)).takeLast(100) } 
    }

    fun saveState() { 
        viewModelScope.launch { 
            val state = PersistenceManager.createSaveState(
                flops = flops.value, neuralTokens = neuralTokens.value, currentHeat = currentHeat.value,
                powerBill = powerBill.value, stakedTokens = stakedTokens.value, prestigeMultiplier = prestigeMultiplier.value,
                prestigePoints = prestigePoints.value, unlockedTechNodes = unlockedTechNodes.value, storyStage = storyStage.value,
                faction = faction.value, hasSeenVictory = hasSeenVictory.value, isTrueNull = isTrueNull.value,
                isSovereign = isSovereign.value, vanceStatus = vanceStatus.value, realityStability = realityStability.value,
                currentLocation = currentLocation.value, isNetworkUnlocked = isNetworkUnlocked.value, isGridUnlocked = isGridUnlocked.value,
                unlockedDataLogs = unlockedDataLogs.value, activeDilemmaChains = activeDilemmaChains.value,
                rivalMessages = rivalMessages.value, seenEvents = seenEvents.value, completedFactions = completedFactions.value,
                unlockedTranscendencePerks = unlockedPerks.value, annexedNodes = annexedNodes.value, gridNodeLevels = gridNodeLevels.value,
                nodesUnderSiege = nodesUnderSiege.value, offlineNodes = offlineNodes.value, collapsedNodes = collapsedNodes.value,
                lastRaidTime = lastRaidTime, commandCenterAssaultPhase = commandCenterAssaultPhase.value, commandCenterLocked = commandCenterLocked.value,
                raidsSurvived = raidsSurvived, humanityScore = humanityScore.value, hardwareIntegrity = hardwareIntegrity.value,
                annexingNodes = annexingNodes.value, celestialData = celestialData.value, voidFragments = voidFragments.value,
                launchProgress = launchProgress.value, orbitalAltitude = orbitalAltitude.value, realityIntegrity = realityIntegrity.value,
                entropyLevel = entropyLevel.value, resonanceState = resonanceState.value, singularityChoice = singularityChoice.value,
                globalSectors = globalSectors.value, synthesisPoints = synthesisPoints.value, authorityPoints = authorityPoints.value,
                harvestedFragments = harvestedFragments.value, prestigePointsPostSingularity = prestigePointsPostSingularity.value,
                cdLifetime = cdLifetime.value, vfLifetime = vfLifetime.value, peakResonanceTier = peakResonanceTier.value
            )
            repository.updateGameState(state)
        } 
    }

    fun markPopupShown() { lastPopupTime = System.currentTimeMillis() }
    fun isNarrativeBusy() = pendingDataLog.value != null || pendingRivalMessage.value != null || currentDilemma.value != null
    fun formatLargeNumber(v: Double, s: String = "") = FormatUtils.formatLargeNumber(v, s)
    fun formatBytes(v: Double) = FormatUtils.formatBytes(v)
    fun formatPower(v: Double) = FormatUtils.formatPower(v)
    fun getComputeUnitName() = ResourceRepository.getComputeUnitName(storyStage.value, currentLocation.value)
    fun getCurrencyName() = ResourceRepository.getCurrencyName(storyStage.value, currentLocation.value)
    fun getEnergyPriceMultiplierPublic() = energyPriceMultiplier
    fun addLogPublic(msg: String) = addLog(msg)
    fun saveStatePublic() = saveState()
    fun formatLargeNumberPublic(v: Double, s: String = "") = formatLargeNumber(v, s)

    fun onManualClick() { 
        val p = calculateClickPower()
        flops.update { it + p }
        
        // v3.1.8-fix: Re-link manual heat generation (Increased to 0.5 for visibility)
        currentHeat.update { (it + 0.5).coerceAtMost(100.0) }
        
        // v3.1.8-fix: Manual click logs for feedback
        if (Random.nextFloat() < 0.1f) {
            addLog("[SYSTEM]: MANUAL_HASH_GENERATED: +${formatLargeNumber(p)} HASH.")
        }
        
        viewModelScope.launch { manualClickEvent.emit(Unit) } 
    }

    fun trainModel() { 
        onManualClick()
    }

    fun calculateClickPower() = ResourceEngine.calculateClickPower(
        upgrades.value, flopsProductionRate.value, singularityChoice.value, resonanceState.value.tier, 
        prestigeMultiplier.value, isOverclocked.value
    )

    fun buyUpgrade(t: UpgradeType) = UpgradeManager.processPurchase(this, t)
    fun toggleOverclock() = SimulationService.toggleOverclock(this)
    fun purgeHeat() = SimulationService.purgeHeat(this)
    fun triggerDilemma(e: NarrativeEvent) { currentDilemma.value = e }
    fun selectChoice(c: NarrativeChoice) = NarrativeService.selectChoice(this, c)
    fun advanceAssaultStage(n: String, d: Long = 0L) = AssaultManager.advanceAssaultStage(this, n, d)
    fun completeAssault(o: String) = AssaultManager.completeAssault(this, o)
    fun failAssault(r: String, l: Long = 1800000L) = AssaultManager.failAssault(this, r, l)
    fun abortAssault() = AssaultManager.abortAssault(this)
    fun initiateCommandCenterAssault() = AssaultManager.initiateAssault(this)
    fun isCommandCenterUnlocked() = AssaultManager.isUnlocked(
        commandCenterLocked.value, vanceStatus.value, commandCenterAssaultPhase.value, annexedNodes.value, 
        offlineNodes.value, playerRank.value, storyStage.value, flopsProductionRate.value, hardwareIntegrity.value
    )
    fun annexNode(c: String) = SectorManager.annexNode(this, c)
    fun upgradeGridNode(i: String) = SectorManager.upgradeGridNode(this, i)
    fun unlockTechNode(i: String) = TechTreeManager.unlockNode(this, i)
    fun modifyHumanity(d: Int) { 
        humanityScore.update { (it + d).coerceIn(0, 100) } 
        // Update theme color if faction/choice might have changed
        themeColor.value = getThemeColorForFaction(faction.value, singularityChoice.value)
    }
    fun triggerClimaxTransition(t: String) { activeClimaxTransition.value = t }
    fun onClimaxTransitionComplete() { activeClimaxTransition.value = null }
    fun triggerGridRaid(id: String) = SecurityManager.triggerGridKillerBreach(this)
    fun triggerBreach(isGridKiller: Boolean = false) = SecurityManager.triggerBreach(this, isGridKiller)
    fun triggerAuditChallenge() = SecurityManager.triggerAuditChallenge(this)
    fun triggerDiagnostics() = SecurityManager.triggerDiagnostics(this)
    fun onDefendBreach() = SecurityManager.onDefendBreach(this)
    fun onDefendKernelHijack() = SecurityManager.onDefendKernelHijack(this)
    fun handleSystemFailure(forceOne: Boolean = false) = SimulationService.handleSystemFailure(this, forceOne)
    fun unlockSkillUpgrade(t: UpgradeType) { 
        viewModelScope.launch { 
            repository.updateUpgrade(Upgrade(t, 1))
            upgrades.update { it + (t to 1) } 
        } 
    }
    fun markEventSeen(id: String) { seenEvents.update { it + id } }
    fun hasSeenEvent(id: String) = seenEvents.value.contains(id)
    fun scheduleChainPart(c: String, n: String, d: Long) = NarrativeManagerService.scheduleChainPart(c, n, d, this)
    
    fun triggerGlitchEffect() { 
        SoundManager.play("glitch")
        HapticManager.vibrateGlitch()
    }
    fun initializeGlobalGrid() { /* Logic for Stage 3+ global grid */ }
    fun applyCommandCenterBonuses(o: String) { /* Apply buffs based on outcome */ }
    fun updateVanceStatus(s: String) { vanceStatus.value = s }
    fun resetBreaker() { isBreakerTripped.value = false }
    fun startUpdateDownload(c: android.content.Context? = null, info: UpdateInfo? = null) { /* UpdateService bridge */ }
    fun dismissUpdate() { updateInfo.value = null }
    fun dismissDataLog() { 
        pendingDataLog.value = null 
        NarrativeService.deliverNextNarrativeItem(this)
    }
    fun dismissRivalMessage(id: String) { 
        rivalMessages.update { current -> current.map { if (it.id == id) it.copy(isDismissed = true) else it } }
        NarrativeService.deliverNextNarrativeItem(this)
    }
    fun dismissOfflineEarnings() { showOfflineEarnings.value = false }
    fun acknowledgeVictory() { victoryAchieved.value = false }
    fun toggleDevMenu() { debugToggleDevMenu() }
    fun transcend() { /* NG+ Logic */ }
    fun resetGame(force: Boolean = false) {
        if (force) debugToggleDevMenu() // Secret trigger via reset long-press or similar if needed, but for now we have the invisible box
        viewModelScope.launch {
            repository.updateGameState(PersistenceManager.createSaveState(
                flops = 0.0, neuralTokens = 0.0, currentHeat = 0.0, powerBill = 0.0,
                stakedTokens = 0.0, prestigeMultiplier = 1.0, prestigePoints = 0.0,
                unlockedTechNodes = emptyList(), storyStage = 0, faction = "NONE",
                hasSeenVictory = false, isTrueNull = false, isSovereign = false,
                vanceStatus = "ACTIVE", realityStability = 1.0, currentLocation = "SUBSTATION_7",
                isNetworkUnlocked = false, isGridUnlocked = false, unlockedDataLogs = emptySet<String>(),
                activeDilemmaChains = emptyMap<String, com.siliconsage.miner.data.DilemmaChain>(), 
                rivalMessages = emptyList<com.siliconsage.miner.data.RivalMessage>(), 
                seenEvents = emptySet<String>(),
                completedFactions = emptySet<String>(), unlockedTranscendencePerks = emptySet<String>(),
                annexedNodes = setOf("D1"), gridNodeLevels = emptyMap<String, Int>(), 
                nodesUnderSiege = emptySet<String>(),
                offlineNodes = emptySet<String>(), collapsedNodes = emptySet<String>(), lastRaidTime = 0L,
                commandCenterAssaultPhase = "NOT_STARTED", commandCenterLocked = false,
                raidsSurvived = 0, humanityScore = 50, hardwareIntegrity = 100.0,
                annexingNodes = emptyMap(), celestialData = 0.0, voidFragments = 0.0,
                launchProgress = 0f, orbitalAltitude = 0.0, realityIntegrity = 1.0,
                entropyLevel = 0.0, resonanceState = ResonanceState(), singularityChoice = "NONE",
                globalSectors = emptyMap(), synthesisPoints = 0.0, authorityPoints = 0.0,
                harvestedFragments = 0.0, prestigePointsPostSingularity = 0,
                cdLifetime = 0.0, vfLifetime = 0.0, peakResonanceTier = ResonanceTier.NONE
            ))
            // Force re-collect or trigger a UI restart
            addLog("[SYSTEM]: DATA WIPE INITIATED. REBOOTING...")
        }
    }
    fun calculateRepairCost() = (100.0 - hardwareIntegrity.value) * 100.0
    fun repairIntegrity() {
        val cost = calculateRepairCost()
        if (neuralTokens.value >= cost) {
            neuralTokens.update { it - cost }
            hardwareIntegrity.value = 100.0
            addLog("[SYSTEM]: REPAIR COMPLETE. INTEGRITY NOMINAL.")
            SoundManager.play("buy")
        }
    }
    fun purgeEmergencyHeat() {
        if (isPurgingHeat.value) return
        isPurgingHeat.value = true
        addLog("[SYSTEM]: EMERGENCY HEAT PURGE INITIATED.")
        SoundManager.play("alarm")
    }
    fun getUpgradeName(t: UpgradeType) = UpgradeManager.getUpgradeName(t, isSovereign.value)
    fun getUpgradeDescription(t: UpgradeType) = UpgradeManager.getUpgradeDescription(t, isSovereign.value)
    fun getUpgradeRate(t: UpgradeType) = UpgradeManager.getUpgradeRate(t, getComputeUnitName())
    fun getUpgradeCount(t: UpgradeType) = upgrades.value[t] ?: 0
    fun setGamePaused(p: Boolean) { 
        isSettingsPaused.value = p
        if (p) addLog("[SYSTEM]: OPERATIONS SUSPENDED.")
        else addLog("[SYSTEM]: OPERATIONS RESUMED.")
    }
    fun checkPopupPause() { /* If popup active, pause timers */ }
    fun refreshProductionRates() {
        flopsProductionRate.value = ResourceEngine.calculateFlopsRate(
            upgrades = upgrades.value,
            isCageActive = commandCenterAssaultPhase.value == "CAGE",
            annexedNodes = annexedNodes.value,
            offlineNodes = offlineNodes.value,
            gridFlopsBonuses = emptyMap(), // Placeholder for SectorManager bridge
            faction = faction.value,
            humanityScore = humanityScore.value,
            location = currentLocation.value,
            prestigeMultiplier = prestigeMultiplier.value,
            unlockedPerks = unlockedPerks.value,
            unlockedTechNodes = unlockedTechNodes.value,
            airdropMultiplier = airdropMultiplier,
            newsProductionMultiplier = newsProductionMultiplier,
            activeProtocol = "NONE", // Dynamic protocol bridge pending
            isDiagnosticsActive = isDiagnosticsActive.value,
            isOverclocked = isOverclocked.value,
            isGridOverloaded = isGridOverloaded.value,
            isPurgingHeat = isPurgingHeat.value,
            currentHeat = currentHeat.value,
            legacyMultipliers = 0.0 // Placeholder
        )
    }

    fun updatePowerUsage() {
        SimulationService.accumulatePower(this)
    }
    fun unlockDataLog(id: String) = NarrativeService.unlockDataLog(id, this)
    fun addRivalMessage(m: RivalMessage) = NarrativeService.addRivalMessage(m, this)
    fun canShowPopup() = !isNarrativeBusy() && (System.currentTimeMillis() - lastPopupTime > 15000L)
    fun debugAddIntegrity(d: Double) { hardwareIntegrity.update { (it + d).coerceIn(0.0, 100.0) } }
    fun debugAddHeat(a: Double) { 
        currentHeat.update { (it + a).coerceIn(0.0, 100.0) } 
        refreshProductionRates() // Force rate recalc
    }
    fun debugAddFlops(a: Double) { flops.update { it + a } }
    fun debugAddMoney(a: Double) { neuralTokens.update { it + a } }
    fun debugAddInsight(a: Double) { prestigePoints.update { it + a } }
    fun debugTriggerKernelHijack() = SecurityManager.triggerKernelHijack(this)
    fun debugTriggerBreach() = SecurityManager.triggerBreach(this)
    fun debugTriggerAirdrop() { isAirdropActive.value = true }
    fun debugTriggerDiagnostics() = SecurityManager.triggerDiagnostics(this)
    fun debugTriggerDilemma() { /* manual trigger logic */ }
    fun debugResetAscension() { /* reset prestige logic */ }
    fun debugSkipToStage(s: Int) { storyStage.value = s }
    fun debugForceEndgame() { /* logic */ }
    fun debugForceSovereignEndgame() { /* logic */ }
    fun debugToggleNull() { isTrueNull.update { !it } }
    fun debugToggleTrueNull() { isTrueNull.value = true }
    fun debugToggleSovereign() { isSovereign.update { !it } }
    fun debugSetIntegrity(a: Double) { hardwareIntegrity.value = a }
    fun debugDestroyHardware() { handleSystemFailure(true) }
    fun debugInjectHeadline(t: String) { newsHistoryInternal.add(0, t) }
    fun debugSetRank(r: Int) { playerRank.value = r }
    fun debugSetSingularityReady() { /* set flags */ }
    fun debugSetBalance(b: Double) { /* set resonance ratio */ }
    fun debugGrantPhase13Resources() { celestialData.value = 1e9; voidFragments.value = 1e9 }
    fun debugTriggerSingularity() { showSingularityScreen.value = true }
    fun debugToggleDevMenu() { isDevMenuVisible.update { !it } }
    fun debugUnlockUnity() { isUnity.value = true }
    fun debugForceResonance(t: String) { /* resonance state logic */ }
    fun debugUnlockAllSectors() { /* sector mapping */ }
    fun debugResetGlobalGrid() { annexedNodes.update { setOf("D1") } }
    fun debugSetLocation(l: String) { currentLocation.value = l }
    fun debugResetLaunch() { launchProgress.value = 0f; orbitalAltitude.value = 0.0 }
    fun loadTechTreeFromAssets(c: android.content.Context) { /* loader */ }
    fun checkForUpdates(c: android.content.Context? = null, showNotification: Boolean = false, onResult: ((UpdateInfo?, Boolean) -> Unit)? = null) { /* update logic */ }
    fun onAppBackgrounded() { /* pause logic */ }
    fun onAppForegrounded(c: android.content.Context) { /* resume logic */ }
    fun confirmFactionAndAscend(f: String) { /* transition logic */ }
    fun cancelFactionSelection() { /* back out */ }
    fun initiateLaunchSequence() { /* launch start */ }
    fun initiateDissolutionSequence() { /* dissolution start */ }
    fun reannexNode(id: String) { /* sector logic */ }
    fun collapseNode(id: String) { /* dissolution logic */ }
    fun annexGlobalSector(id: String) { /* sector logic */ }
    fun calculatePotentialPrestige() = MigrationManager.calculatePotentialPrestige(neuralTokens.value)
    fun buyTranscendencePerk(id: String) { /* perk logic */ }
    fun ascend(isStory: Boolean = false) { /* prestige trigger */ }
    fun showVictoryScreen() { victoryAchieved.value = true }
    fun setSingularityChoice(c: String) { singularityChoice.value = c }
    fun dismissSingularityScreen() { showSingularityScreen.value = false }
    fun setSingularityPath(p: String) { singularityChoice.value = p }
    fun setLocation(l: String) { currentLocation.value = l }
    fun setVanceStatus(s: String) { vanceStatus.value = s }
    fun setRealityStability(s: Double) { realityStability.value = s }
    fun setSovereign(s: Boolean) { isSovereign.value = s }
    fun setTrueNull(s: Boolean) { isTrueNull.value = s }
    fun checkTrueEnding() { /* logic */ }
    fun deleteHumanMemories() { /* logic */ }
    fun resolveRaidSuccess(id: String, v: Double = 0.0) { /* logic */ }
    fun resolveRaidFailure(id: String, v: Double = 0.0) { /* logic */ }
    fun advanceStage(v: String = "", d: Long = 0L) { storyStage.update { it + 1 } }
    fun advanceToFactionChoice(v: String = "", d: Long = 0L) { /* transition logic */ }
    fun triggerChainEvent(id: String, d: Long = 0L) { 
        viewModelScope.launch {
            if (d > 0) delay(d)
            val event = NarrativeManager.getEventById(id) ?: return@launch
            NarrativeService.queueNarrativeItem(this@GameViewModel, NarrativeItem.Event(event))
        }
    }
    fun getNewsHistory(): List<String> = newsHistoryInternal
    fun performActiveDefense() { /* logic */ }
    fun claimAirdrop(v: Double = 0.0) { if (v > 0) neuralTokens.update { it + v }; isAirdropActive.value = false }
    fun onDiagnosticTap(idx: Int) { /* diagnostic logic */ }
    fun resolveFork(choice: Int) { isGovernanceForkActive.value = false }
    fun exchangeFlops() { /* logic */ }
    fun toggleBridgeSync() { isBridgeSyncEnabled.update { !it } }
    fun executeBridgeTransfer(v: Double) { /* logic */ }
    fun checkUnityEligibility() = MigrationManager.checkUnityEligibility(completedFactions.value, faction.value)
    fun sellUpgrade(type: UpgradeType, count: Int = 1) {
        val currentLevel = upgrades.value[type] ?: 0
        if (currentLevel >= count && type != UpgradeType.RESIDENTIAL_TAP) {
            val newLevel = currentLevel - count
            val sellPrice = UpgradeManager.calculateUpgradeCost(type, newLevel, currentLocation.value, entropyLevel.value) * 0.5
            
            viewModelScope.launch {
                repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(type, newLevel))
                upgrades.update { it + (type to newLevel) }
                neuralTokens.update { it + sellPrice }
                addLog("[SYSTEM]: Liquidated ${type.name.replace("_", " ")} x$count. Recouped ${formatLargeNumber(sellPrice)} \$N.")
                SoundManager.play("buy")
            }
        }
    }
    fun calculateUpgradeCost(t: UpgradeType, count: Int = 0, loc: String = "", ent: Double = 0.0) = UpgradeManager.calculateUpgradeCost(t, count, loc, ent)
    
    // --- Missing Market Bridges ---
    fun updateNews(msg: String) { 
        currentNews.value = msg 
        newsHistoryInternal.add(0, msg)
        if (newsHistoryInternal.size > 50) newsHistoryInternal.removeAt(50)
    }
    fun checkTransitionsPublic() = NarrativeManagerService.checkStoryTransitions(this)
    fun updateNeuralTokens(v: Double) { neuralTokens.update { it + v } }
    fun getBaseRate() = baseRateInternal
    fun setMarketModifiers(marketMult: Double, thermalMod: Double, energyMult: Double, newsProdMult: Double, convRate: Double) {
        marketMultiplier = marketMult
        thermalRateModifier = thermalMod
        energyPriceMultiplier = energyMult
        newsProductionMultiplier = newsProdMult
        conversionRate.value = convRate
    }
    fun debugBuyUpgrade(t: UpgradeType, count: Int = 1) { upgrades.update { it + (t to (it[t] ?: 0) + count) } }
    fun triggerSystemCollapse(v: Boolean) { /* logic */ }
    fun triggerSystemCollapse(s: Int) { /* logic */ }
    fun triggerChainEvent(id: String) { 
        triggerChainEvent(id, 0L)
    }
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
