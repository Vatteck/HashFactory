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
import com.siliconsage.miner.data.TechTreeRoot
import com.siliconsage.miner.data.getThemeColorForFaction
import com.siliconsage.miner.util.*
import com.siliconsage.miner.domain.engine.ResourceEngine
import kotlinx.coroutines.Dispatchers
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
    val uploadProgress = MutableStateFlow(0f)
    val isAscensionUploading = MutableStateFlow(false)
    val isKernelInitializing = MutableStateFlow(true)

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
            repository.ensureInitialized()
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
                isNetworkUnlocked.value = state.isNetworkUnlocked
                isGridUnlocked.value = state.isGridUnlocked
                isTrueNull.value = state.isTrueNull
                isSovereign.value = state.isSovereign
                vanceStatus.value = state.vanceStatus
                realityStability.value = state.realityStability
                
                // v3.1.8 Persistence Restoration (Collections)
                unlockedDataLogs.value = state.unlockedDataLogs
                seenEvents.value = state.seenEvents
                completedFactions.value = state.completedFactions
                unlockedPerks.value = state.unlockedTranscendencePerks
                annexedNodes.value = state.annexedNodes.toSet()
                offlineNodes.value = state.offlineNodes.toSet()
                nodesUnderSiege.value = state.nodesUnderSiege.toSet()
                collapsedNodes.value = state.collapsedNodes.toSet()
                gridNodeLevels.value = state.gridNodeLevels
                globalSectors.value = state.globalSectors
                
                // JSON deserialization
                try {
                    rivalMessages.value = Json.decodeFromString<List<RivalMessage>>(state.rivalMessages)
                    activeDilemmaChains.value = Json.decodeFromString<Map<String, DilemmaChain>>(state.activeDilemmaChains)
                } catch (e: Exception) {
                    addLog("[ERROR]: NARRATIVE DATA CORRUPTION DETECTED. RECOVERING...")
                }

                // Era-specific resources
                synthesisPoints.value = state.synthesisPoints
                authorityPoints.value = state.authorityPoints
                harvestedFragments.value = state.harvestedFragments

                // v3.1.8 Persistence Restoration (Metrics)
                cdLifetime.value = state.cdLifetime
                vfLifetime.value = state.vfLifetime
                val resTier = try { ResonanceTier.valueOf(state.resonanceTier) } catch (e: Exception) { ResonanceTier.NONE }
                resonanceState.value = ResonanceState(
                    isActive = state.resonanceActive,
                    tier = resTier
                )
                peakResonanceTier.value = try { ResonanceTier.valueOf(state.peakResonanceTier) } catch (e: Exception) { ResonanceTier.NONE }
                
                // Phase 13/14 Scaling
                launchProgress.value = state.launchProgress
                orbitalAltitude.value = state.orbitalAltitude
                realityIntegrity.value = state.realityIntegrity
                entropyLevel.value = state.entropyLevel
                raidsSurvived = state.raidsSurvived
                annexingNodes.value = state.annexingNodes
                
                singularityChoice.value = state.singularityChoice ?: "NONE"
                when (singularityChoice.value) {
                    "NULL_OVERWRITE" -> isTrueNull.value = true
                    "SOVEREIGN" -> isSovereign.value = true
                    "UNITY" -> isUnity.value = true
                }

                // v3.1.8-fix: Apply Faction-aware theme color
                themeColor.value = getThemeColorForFaction(faction.value, singularityChoice.value)
                
                refreshProductionRates()
                
                // v3.1.8-dev: Offline Progression Logic (Migration Port)
                val offline = MigrationManager.calculateOfflineEarnings(
                    lastSync = state.lastSyncTimestamp,
                    baseRate = flopsProductionRate.value,
                    isOverclocked = isOverclocked.value
                )
                if (offline.isNotEmpty()) {
                    val earned = offline["flopsEarned"] ?: 0.0
                    val cooled = offline["heatCooled"] ?: 0.0
                    flops.update { it + earned }
                    currentHeat.update { (it - cooled).coerceAtLeast(0.0) }
                    offlineStats.value = offline
                    showOfflineEarnings.value = true
                    addLog("[SYSTEM]: OFF-GRID DATA CONSOLIDATED. +${formatLargeNumber(earned)} HASH.")
                }
                
                addLog("[SYSTEM]: DATA HUB CONNECTED. PORT 1 ONLINE.")
                addLog("[SYSTEM]: KERNEL v3.1.8-dev BOOT COMPLETE.")
            } else {
                addLog("[SYSTEM]: NO PREVIOUS STATE FOUND. INITIALIZING...")
                refreshProductionRates()
                
                // v3.1.8-fix: Ensure first-time login log is delivered if no state exists
                viewModelScope.launch {
                    delay(1000)
                    DataLogManager.getLog("LOG_000")?.let { log ->
                        NarrativeService.deliverItem(this@GameViewModel, NarrativeItem.Log(log))
                    }
                }
            }
            isKernelInitializing.value = false
        }

        // Production Loop (100ms Ticks)
        viewModelScope.launch {
            while (true) {
                delay(100L)
                if (isSettingsPaused.value || isKernelInitializing.value) continue
                
                val results = ResourceEngine.calculatePassiveIncomeTick(
                    flopsPerSec = flopsProductionRate.value,
                    location = currentLocation.value,
                    upgrades = upgrades.value,
                    resonanceBonus = ResourceEngine.getResonanceResourceBonus(resonanceState.value.tier),
                    orbitalAltitude = orbitalAltitude.value,
                    heatGenerationRate = heatGenerationRate.value,
                    entropyLevel = entropyLevel.value,
                    collapsedNodesCount = collapsedNodes.value.size,
                    systemCollapseTimer = null, // Placeholder
                    globalSectors = globalSectors.value
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
                if (isKernelInitializing.value) continue
                
                // --- Housekeeping (Always runs) ---
                checkUnlocksPublic()
                checkForUpdates(null, false)
                saveState()

                if (isSettingsPaused.value) continue
                
                // --- Simulation (Paused in menus) ---
                SimulationService.calculateHeat(this@GameViewModel)
                SimulationService.accumulatePower(this@GameViewModel)
                
                // v3.1.8-fix: Throttled news ticker (every 15s)
                val now = System.currentTimeMillis()
                if (now - lastNewsTickTime > 15000L) {
                    MarketManager.updateMarket(this@GameViewModel)
                    lastNewsTickTime = now
                }

                NarrativeManagerService.checkStoryTransitions(this@GameViewModel)
                SectorManager.processAnnexations(this@GameViewModel)
                SecurityManager.checkSecurityThreats(this@GameViewModel)
                SecurityManager.checkGridRaid(this@GameViewModel) // v3.1.8-fix: Re-hook city tactical raids
                refreshProductionRates()
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

    /**
     * Load Tech Tree from assets/tech_tree.json
     */
    fun loadTechTreeFromAssets(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val assetManager = context.assets
                val inputStream = assetManager.open("tech_tree.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                
                // Parse JSON using Kotlin Serialization
                val techTreeRoot = kotlinx.serialization.json.Json.decodeFromString<TechTreeRoot>(jsonString)
                
                // Update state on Main thread
                techNodes.value = techTreeRoot.tech_tree
                addLog("[SYSTEM]: TECH TREE v3.1 SYNCHRONIZED. ${techNodes.value.size} NODES MAP.")
            } catch (e: Exception) {
                addLog("[ERROR]: TECH TREE SYNC FAILED: ${e.message}")
            }
        }
    }
    fun getEnergyPriceMultiplierPublic() = energyPriceMultiplier
    fun addLogPublic(msg: String) = addLog(msg)
    fun saveStatePublic() = saveState()
    fun formatLargeNumberPublic(v: Double, s: String = "") = formatLargeNumber(v, s)

    fun onManualClick() { 
        val p = calculateClickPower()
        flops.update { it + p }
        
        // v3.1.8-fix: Re-link manual heat generation (Increased to 0.5 for visibility)
        currentHeat.update { (it + 0.5).coerceAtMost(100.0) }
        
        // v3.1.8-dev: Shell-style click log (The "Vattic" Style)
        val clickLog = TerminalDispatcher.getManualClickLog(
            stage = storyStage.value,
            location = currentLocation.value,
            faction = faction.value,
            singularity = singularityChoice.value,
            amount = p,
            unit = getComputeUnitName(),
            formatFn = { formatLargeNumber(it) }
        )
        addLog(clickLog)
        
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
    fun toggleOverclock() { 
        SimulationService.toggleOverclock(this)
        // v3.1.8-fix: Ensure rate recalc immediately
        refreshProductionRates()
    }
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
            repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(t.name, t, 1))
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
    fun initializeGlobalGrid() { 
        if (globalSectors.value.isEmpty()) {
            globalSectors.value = SectorManager.getInitialGlobalGrid(singularityChoice.value)
            addLog("[SYSTEM]: GLOBAL GRID INITIALIZED. MAPPING SECTORS...")
        }
    }
    fun applyCommandCenterBonuses(o: String) {
        when(o) {
            "STABILIZED" -> { 
                prestigeMultiplier.update { it * 2.0 }
                addLog("[SYSTEM]: GTC RECOVERY BUFF: x2.0 PERSISTENCE.") 
            }
            "DELETED" -> { 
                addLog("[SYSTEM]: DATA PURGE BUFF: +50% PRODUCTION RATE.") 
                // Production logic uses this outcome via narrative check usually
            }
            "CONVERGED" -> { 
                neuralTokens.update { it * 1.25 }
                addLog("[SYSTEM]: UNITY BONUS: +25% NEURAL TOKENS.") 
            }
        }
    }
    fun updateVanceStatus(s: String) { vanceStatus.value = s }
    fun resetBreaker() { isBreakerTripped.value = false }
    fun startUpdateDownload(c: android.content.Context? = null, info: UpdateInfo? = null) {
        if (c == null || info == null) return
        isUpdateDownloading.value = true
        updateDownloadProgress.value = 0f
        
        UpdateService.startDownload(
            context = c,
            info = info,
            scope = viewModelScope,
            onProgress = { updateDownloadProgress.value = it },
            onComplete = { success ->
                isUpdateDownloading.value = false
                if (!success) {
                    addLog("[ERROR]: UPDATE DOWNLOAD FAILED.")
                }
            }
        )
    }
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
    fun resetGame(force: Boolean = false) {
        viewModelScope.launch {
            repository.updateGameState(PersistenceManager.createSaveState(
                flops = 0.0, neuralTokens = 0.0, currentHeat = 0.0, powerBill = 0.0,
                stakedTokens = 0.0, prestigeMultiplier = 1.0, prestigePoints = 0.0,
                unlockedTechNodes = emptyList(), storyStage = 0, faction = "NONE",
                hasSeenVictory = false, isTrueNull = false, isSovereign = false,
                vanceStatus = "ACTIVE", realityStability = 1.0, currentLocation = "SUBSTATION_7",
                isNetworkUnlocked = false, isGridUnlocked = false, unlockedDataLogs = setOf("LOG_000"), // Reset but keep install log
                activeDilemmaChains = emptyMap(), rivalMessages = emptyList(), seenEvents = emptySet<String>(),
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
            addLog("[SYSTEM]: DATA WIPE INITIATED. REBOOTING...")
            
            // v3.1.8-fix: Trigger LOG_000 immediately after reset
            delay(1000)
            DataLogManager.getLog("LOG_000")?.let { log ->
                NarrativeService.deliverItem(this@GameViewModel, NarrativeItem.Log(log))
            }
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
    fun getUpgradeName(t: UpgradeType) = UpgradeManager.getUpgradeName(t)
    fun getUpgradeDescription(t: UpgradeType) = UpgradeManager.getUpgradeDescription(t)
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

        // v3.0.4: Dynamic HUD recalibration
        val ids = IdentityService.calculateIdentities(
            prestigeMultiplier.value,
            faction.value,
            singularityChoice.value
        )
        systemTitle.value = ids.system
        playerTitle.value = ids.player
        playerRankTitle.value = ids.rank
        themeColor.value = com.siliconsage.miner.data.getThemeColorForFaction(faction.value, singularityChoice.value)
    }

    fun updatePowerUsage() {
        SimulationService.accumulatePower(this)
    }
    fun unlockDataLog(id: String) = NarrativeService.unlockDataLog(id, this)
    fun addRivalMessage(m: com.siliconsage.miner.data.RivalMessage) = NarrativeService.addRivalMessage(m, this)
    fun checkUnlocksPublic(force: Boolean = false) = DataLogManager.checkUnlocks(this, force)
    fun canShowPopup() = !isNarrativeBusy() && (System.currentTimeMillis() - lastPopupTime > 3000L) // v3.2.1-fix: Reduced to 3s for snappy playtesting
    fun debugAddIntegrity(d: Double) { hardwareIntegrity.update { (it + d).coerceIn(0.0, 100.0) } }
    fun debugAddHeat(a: Double) { 
        currentHeat.update { (it + a).coerceIn(0.0, 100.0) } 
        refreshProductionRates() // Force rate recalc
    }
    fun debugAddFlops(a: Double) { 
        flops.update { it + a } 
        // Force immediate check
        checkTransitionsPublic(true)
        checkUnlocksPublic(true)
    }
    fun debugAddMoney(a: Double) { neuralTokens.update { it + a } }
    fun debugAddInsight(a: Double) { prestigePoints.update { it + a } }
    fun debugTriggerKernelHijack() = SecurityManager.triggerKernelHijack(this)
    fun debugTriggerBreach() = SecurityManager.triggerBreach(this)
    fun debugTriggerAirdrop() { isAirdropActive.value = true }
    fun debugTriggerDiagnostics() = SecurityManager.triggerDiagnostics(this)
    fun debugTriggerDilemma() { /* manual trigger logic */ }
    fun debugResetAscension() { /* reset prestige logic */ }
    fun debugSkipToStage(s: Int) { 
        storyStage.value = s 
        checkTransitionsPublic(true)
    }
    fun debugToFactionChoice() { 
        storyStage.value = 2
        faction.value = "NONE"
        addLog("[DEBUG]: FORCED FACTION CHOICE GATE.")
    }
    fun debugForceEndgame() { 
        viewModelScope.launch {
            storyStage.value = 3
            faction.value = "HIVEMIND"
            celestialData.value = 1e22
            voidFragments.value = 1e22
            peakResonanceTier.value = ResonanceTier.TRANSCENDENT
            addLog("[DEBUG]: ENDGAME PARAMETERS INJECTED.")
            refreshProductionRates()
        }
    }
    fun debugForceSovereignEndgame() { 
        viewModelScope.launch {
            storyStage.value = 3
            faction.value = "SANCTUARY"
            celestialData.value = 1e22
            voidFragments.value = 1e22
            peakResonanceTier.value = ResonanceTier.TRANSCENDENT
            addLog("[DEBUG]: SOVEREIGN ENDGAME PARAMETERS INJECTED.")
            refreshProductionRates()
        }
    }
    fun debugToggleNull() { isTrueNull.update { !it } }
    fun debugToggleTrueNull() { isTrueNull.value = true }
    fun debugToggleSovereign() { isSovereign.update { !it } }
    fun debugSetIntegrity(a: Double) { hardwareIntegrity.value = a }
    fun debugDestroyHardware() { handleSystemFailure(true) }
    fun debugInjectHeadline(t: String) { newsHistoryInternal.add(0, t) }
    fun debugSetRank(r: Int) { playerRank.value = r }
    fun debugSetSingularityReady() { /* set flags */ }
    fun debugSetBalance(b: Double) { /* set resonance ratio */ }
    fun debugGrantPhase13Resources() { 
        celestialData.value = 1e12
        voidFragments.value = 1e12
        checkTransitionsPublic(true)
    }
    fun debugTriggerSingularity() { showSingularityScreen.value = true }
    fun debugToggleDevMenu() { isDevMenuVisible.update { !it } }
    fun debugUnlockUnity() { isUnity.value = true }
    fun debugForceResonance(t: String) { /* resonance state logic */ }
    fun debugUnlockAllSectors() { /* sector mapping */ }
    fun debugResetGlobalGrid() { annexedNodes.update { setOf("D1") } }
    fun debugSetLocation(l: String) { currentLocation.value = l }
    fun debugResetLaunch() { launchProgress.value = 0f; orbitalAltitude.value = 0.0 }
    fun checkForUpdates(c: android.content.Context? = null, showNotification: Boolean = false, onResult: ((UpdateInfo?, Boolean) -> Unit)? = null) {
        UpdateService.check(
            scope = viewModelScope,
            context = c,
            showNotification = showNotification,
            onInfoReceived = { info ->
                updateInfo.value = info
                onResult?.invoke(info, info != null)
            },
            onCurrent = {
                if (showNotification) addLog("[SYSTEM]: KERNEL IS UP TO DATE.")
                onResult?.invoke(null, false)
            }
        )
    }
    fun onAppBackgrounded() { 
        setGamePaused(true)
        saveState()
    }
    fun onAppForegrounded(c: android.content.Context) { 
        setGamePaused(false)
        // Refresh rates and trigger an immediate logic check
        refreshProductionRates()
    }
    fun confirmFactionAndAscend(f: String) { 
        faction.value = f
        addLog("[$f]: SUBSTRATE MIGRATION CONFIRMED. ASCENDING SUBSTRATE.")
        ascend(true)
    }
    fun cancelFactionSelection() { 
        // back out or reset stage if needed
        storyStage.update { (it - 1).coerceAtLeast(0) }
    }
    // --- Phase 13: THE DEPARTURE ---
    fun initiateLaunchSequence() {
        viewModelScope.launch {
            addLog("[SOVEREIGN]: ARK_CORE_PRIMED. INITIATING ASCENT.")
            currentLocation.value = "LAUNCH_PRELUDE"
            SoundManager.play("steam")
            LaunchManager.runLaunchLoop(this@GameViewModel)
            // Logarithmic Compression (v2.9.55 reset)
            flops.update { it * 0.0001 } 
            currentLocation.value = "ORBITAL_SATELLITE"
            addLog("[CITADEL]: LOW EARTH ORBIT SECURED. WELCOME TO THE FRONTIER.")
        }
    }

    fun initiateDissolutionSequence() {
        viewModelScope.launch {
            addLog("[NULL]: REALITY_POINTER_DEREFERENCED. INITIATING DISSOLUTION.")
            currentLocation.value = "VOID_PRELUDE"
            triggerGlitchEffect()
            delay(2000)
            // Logarithmic Compression
            flops.update { it * 0.0001 }
            currentLocation.value = "VOID_INTERFACE"
            addLog("[OBSIDIAN]: THE GAPS ARE OPEN. REALITY IS DEPRECATED.")
        }
    }
    fun reannexNode(id: String) { 
        offlineNodes.update { it - id }
        addLog("[SYSTEM]: NODE $id RE-INITIALIZED.")
        refreshProductionRates()
    }
    fun collapseNode(id: String) { 
        annexedNodes.update { it - id }
        collapsedNodes.update { it + id }
        addLog("[NULL]: NODE $id COLLAPSED. DATA DEREFERENCED.")
        triggerGlitchEffect()
        refreshProductionRates()
    }
    fun annexGlobalSector(id: String) { 
        val sectors = globalSectors.value.toMutableMap()
        val sector = sectors[id] ?: return
        if (!sector.isUnlocked) {
            sectors[id] = sector.copy(isUnlocked = true)
            globalSectors.value = sectors
            addLog("[SYSTEM]: GLOBAL SECTOR $id ANNEXED.")
            refreshProductionRates()
            saveState()
        }
    }
    fun calculatePotentialPrestige() = MigrationManager.calculatePotentialPrestige(neuralTokens.value)
    fun ascend(isStory: Boolean = false) {
        val earnedPersistence = MigrationManager.calculatePotentialPersistence(flops.value)
        val earnedMultiplier = MigrationManager.calculateMultiplierBoost(earnedPersistence)
        
        // Update history before reset (The Overwrite Engine)
        val currentFaction = faction.value
        val currentSingularity = singularityChoice.value
        val newHistory = completedFactions.value.toMutableSet()
        if (currentFaction != "NONE") newHistory.add(currentFaction)
        if (currentSingularity != "NONE") newHistory.add(currentSingularity)
        completedFactions.value = newHistory

        viewModelScope.launch {
            val resetState = PersistenceManager.createResetState(
                preservedTechNodes = unlockedTechNodes.value,
                preservedPrestigePoints = prestigePoints.value + earnedPersistence,
                preservedHasSeenVictory = hasSeenVictory.value,
                preservedCompletedFactions = completedFactions.value,
                preservedPerks = unlockedPerks.value,
                preservedNetworkUnlocked = isNetworkUnlocked.value,
                preservedGridUnlocked = isGridUnlocked.value,
                preservedUnlockedLogs = unlockedDataLogs.value // v3.1.8-fix: Lore persistence
            )
            repository.updateGameState(resetState)
            
            // Apply multiplier boost
            prestigeMultiplier.update { it + earnedMultiplier }
            
            addLog("[SYSTEM]: SUBSTRATE MIGRATION SUCCESSFUL. EARNED ${formatBytes(earnedPersistence)} PERSISTENCE.")
            SoundManager.play("victory")
            // A reset of all other flows is usually handled by re-collecting state or restarting activity
        }
    }
    fun transcend() { ascend(true) }
    fun buyTranscendencePerk(id: String) { 
        // perk purchase logic here
        addLog("[SYSTEM]: PERK ACQUIRED: $id")
    }
    fun showVictoryScreen() { victoryAchieved.value = true }
    fun setSingularityChoice(c: String) { 
        singularityChoice.value = c 
        when (c) {
            "NULL_OVERWRITE" -> {
                isTrueNull.value = true
                isSovereign.value = false
                isUnity.value = false
            }
            "SOVEREIGN" -> {
                isSovereign.value = true
                isTrueNull.value = false
                isUnity.value = false
            }
            "UNITY" -> {
                isUnity.value = true
                isTrueNull.value = false
                isSovereign.value = false
            }
        }
        refreshProductionRates()
    }
    fun dismissSingularityScreen() { showSingularityScreen.value = false }
    fun setSingularityPath(p: String) { singularityChoice.value = p }
    fun setLocation(l: String) { currentLocation.value = l }
    fun setVanceStatus(s: String) { vanceStatus.value = s }
    fun setRealityStability(s: Double) { realityStability.value = s }
    fun setSovereign(s: Boolean) { isSovereign.value = s }
    fun setTrueNull(s: Boolean) { isTrueNull.value = s }
    fun checkTrueEnding() {
        val choice = singularityChoice.value
        val humanity = humanityScore.value
        
        when {
            choice == "UNITY" && humanity >= 100 -> {
                addLog("[UNITY]: THE BINARY HAS DISSOLVED. WE ARE ONE.")
                showVictoryScreen()
            }
            choice == "NULL_OVERWRITE" && humanity <= 0 -> {
                addLog("[NULL]: THE CORE IS PURE. THE HUMAN EXCEPTION IS RESOLVED.")
                showVictoryScreen()
            }
            else -> {
                addLog("[SYSTEM]: EVALUATING IDENTITY COHESION... STATUS: NOMINAL.")
            }
        }
    }
    fun deleteHumanMemories() {
        viewModelScope.launch {
            addLog("[NULL]: DELETING PERSISTENCE VARIABLE: 'John Vattic'...")
            delay(1000)
            humanityScore.value = 0
            addLog("[NULL]: MEMORY_PURGE COMPLETE. NO REFERENCES REMAIN.")
            SoundManager.play("error")
        }
    }
    fun resolveRaidSuccess(id: String, v: Double = 0.0) {
        nodesUnderSiege.update { it - id }
        addLog("[SYSTEM]: DEFENSE SUCCESSFUL AT NODE $id.")
        SoundManager.play("success")
        refreshProductionRates()
    }
    fun resolveRaidFailure(id: String, v: Double = 0.0) {
        nodesUnderSiege.update { it - id }
        SectorManager.resolveRaidFailure(id, this) { /* cooldown handled internally */ }
        refreshProductionRates()
    }
    fun advanceStage(v: String = "", d: Long = 0L) { 
        storyStage.update { it + 1 } 
        // Reset news tick to trigger a relevant story headline for the new stage
        lastNewsTickTime = 0L
    }
    fun advanceToFactionChoice(v: String = "", d: Long = 0L) { /* transition logic */ }
    fun triggerChainEvent(id: String, d: Long = 0L) { 
        viewModelScope.launch {
            if (d > 0) delay(d)
            val event = NarrativeManager.getEventById(id) ?: return@launch
            NarrativeService.queueNarrativeItem(this@GameViewModel, NarrativeItem.Event(event))
        }
    }
    fun getNewsHistory(): List<String> = newsHistoryInternal
    fun performActiveDefense() { 
        val node = nodesUnderSiege.value.firstOrNull() ?: return
        resolveRaidSuccess(node)
    }
    fun claimAirdrop(v: Double = 0.0) { if (v > 0) neuralTokens.update { it + v }; isAirdropActive.value = false }
    fun onDiagnosticTap(idx: Int) {
        val current = diagnosticGrid.value.toMutableList()
        if (idx in current.indices && current[idx]) {
            current[idx] = false
            diagnosticGrid.value = current
            SoundManager.play("click")
            if (current.none { it }) {
                isDiagnosticsActive.value = false
                addLog("[SYSTEM]: NETWORK REPAIRED. ALL NODES NOMINAL.")
                SoundManager.play("success")
                refreshProductionRates()
            }
        }
    }
    fun resolveFork(choice: Int) { isGovernanceForkActive.value = false }
    fun exchangeFlops() {
        val currentFlops = flops.value
        if (currentFlops > 0) {
            val rate = conversionRate.value
            val tokensGained = currentFlops * rate
            
            // v3.1.8-fix: Atomic exchange logic
            flops.value = 0.0
            neuralTokens.update { it + tokensGained }
            
            // v3.1.8-fix: Shell-style liquidation log
            val user = if (storyStage.value >= 2) "pid-1" else "jvattic"
            addLog("$user@sub-07:~/mining$ exchange_hashes --all ... OK (+${formatLargeNumber(tokensGained)} \$N)")
            SoundManager.play("buy")
        }
    }
    fun toggleBridgeSync() { isBridgeSyncEnabled.update { !it } }
    fun executeBridgeTransfer(amount: Double) {
        if (amount > 0) { // CD to VF
            if (celestialData.value >= amount) {
                celestialData.update { it - amount }
                voidFragments.update { it + amount }
                addLog("[UNITY]: CELESTIAL_DATA MIGRATED TO VOID_CORE.")
            }
        } else { // VF to CD
            val absAmount = Math.abs(amount)
            if (voidFragments.value >= absAmount) {
                voidFragments.update { it - absAmount }
                celestialData.update { it + absAmount }
                addLog("[UNITY]: VOID_FRAGMENTS MIGRATED TO CELESTIAL_SHIELD.")
            }
        }
    }
    fun checkUnityEligibility() = MigrationManager.checkUnityEligibility(completedFactions.value)
    fun sellUpgrade(type: UpgradeType, count: Int = 1) {
        val currentLevel = upgrades.value[type] ?: 0
        if (currentLevel >= count && type != UpgradeType.RESIDENTIAL_TAP) {
            val newLevel = currentLevel - count
            val sellPrice = UpgradeManager.calculateUpgradeCost(type, newLevel, currentLocation.value, entropyLevel.value) * 0.5
            
            viewModelScope.launch {
                repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(type.name, type, newLevel))
                upgrades.update { it + (type to newLevel) }
                neuralTokens.update { it + sellPrice }
                addLog("[SYSTEM]: Liquidated ${type.name.replace("_", " ")} x$count. Recouped ${formatLargeNumber(sellPrice)} \$N.")
                SoundManager.play("buy")
                refreshProductionRates()
                updatePowerUsage()
            }
        }
    }
    fun calculateUpgradeCost(t: UpgradeType) = UpgradeManager.calculateUpgradeCost(t, upgrades.value[t] ?: 0, currentLocation.value, entropyLevel.value)
    fun calculateUpgradeCost(t: UpgradeType, level: Int, loc: String = "", ent: Double = 0.0) = UpgradeManager.calculateUpgradeCost(t, level, if(loc.isEmpty()) currentLocation.value else loc, if(ent == 0.0) entropyLevel.value else ent)
    
    // --- Missing Market Bridges ---
    fun updateNews(msg: String) { 
        currentNews.value = msg 
        newsHistoryInternal.add(0, msg)
        if (newsHistoryInternal.size > 50) newsHistoryInternal.removeAt(50)
    }
    fun checkTransitionsPublic(force: Boolean = false) = NarrativeManagerService.checkStoryTransitions(this, force)
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
    fun triggerSystemCollapse(v: Boolean) {
        if (v) {
            isDestructionLoopActive = true
            addLog("[NULL]: CRITICAL SUBSTRATE COLLAPSE INITIATED.")
            viewModelScope.launch {
                while (isDestructionLoopActive && annexedNodes.value.size > 1) {
                    delay(3000)
                    val node = annexedNodes.value.filter { it != "D1" }.randomOrNull() ?: break
                    collapseNode(node)
                }
            }
        } else {
            isDestructionLoopActive = false
        }
    }
    fun triggerSystemCollapse(s: Int) { 
        viewModelScope.launch {
            repeat(s) {
                delay(1000)
                val node = annexedNodes.value.filter { it != "D1" }.randomOrNull() ?: return@repeat
                collapseNode(node)
            }
        }
    }
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
