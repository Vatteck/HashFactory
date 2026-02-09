package com.siliconsage.miner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.GameRepository
import com.siliconsage.miner.data.GameState
import com.siliconsage.miner.data.LogEntry
import com.siliconsage.miner.data.TechNode
import com.siliconsage.miner.data.TechTreeRoot
import com.siliconsage.miner.data.Upgrade
import com.siliconsage.miner.BuildConfig
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.domain.engine.ThermalEngine
import com.siliconsage.miner.domain.engine.ProductionEngine
import com.siliconsage.miner.domain.engine.ResourceEngine
import com.siliconsage.miner.util.NarrativeManager
import com.siliconsage.miner.ui.theme.*
import com.siliconsage.miner.util.UpdateInfo
import com.siliconsage.miner.util.UpdateManager
import kotlinx.coroutines.Dispatchers
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.util.LegacyManager
import com.siliconsage.miner.util.RivalManager
import com.siliconsage.miner.util.DataLogManager
import com.siliconsage.miner.util.FormatUtils
import com.siliconsage.miner.util.NarrativeRepository
import com.siliconsage.miner.util.PersistenceManager
import com.siliconsage.miner.util.UpgradeManager
import com.siliconsage.miner.data.RivalMessage
import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.DilemmaChain
import com.siliconsage.miner.data.ScheduledPart
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

sealed class NarrativeItem {
    data class Log(val dataLog: com.siliconsage.miner.data.DataLog) : NarrativeItem()
    data class Message(val rivalMessage: com.siliconsage.miner.data.RivalMessage) : NarrativeItem()
    data class Event(val narrativeEvent: com.siliconsage.miner.data.NarrativeEvent) : NarrativeItem()
}

// v3.0.0: Resonance System
enum class ResonanceTier {
    NONE, HARMONIC, SYMPHONIC, TRANSCENDENT
}

data class ResonanceState(
    val isActive: Boolean = false,
    val intensity: Float = 0f,
    val tier: ResonanceTier = ResonanceTier.NONE,
    val ratio: Double = 1.0
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // v2.9.31: Climax Epilogues
    private val _activeClimaxTransition = MutableStateFlow<String?>(null)
    val activeClimaxTransition: StateFlow<String?> = _activeClimaxTransition.asStateFlow()
    
    /**
     * Trigger a high-impact visual transition
     * Types: NULL, SOVEREIGN, UNITY, BAD
     */
    fun triggerClimaxTransition(type: String) {
        _activeClimaxTransition.value = type
        SoundManager.play("victory")
    }

    fun onClimaxTransitionComplete() {
        _activeClimaxTransition.value = null
    }

    // --- MARKET & PRODUCTION STATE ---
    private var baseRate = 0.1
    private var marketMultiplier = 1.0
    private var airdropMultiplier = 1.0
    private var newsProductionMultiplier = 1.0
    private val newsHistory = mutableListOf<String>() // For history modal
    
    // --- CORE GAME STATE FLOWS ---
    private val _flops = MutableStateFlow(0.0)
    val flops: StateFlow<Double> = _flops.asStateFlow()
    
    private val _neuralTokens = MutableStateFlow(0.0)
    val neuralTokens: StateFlow<Double> = _neuralTokens.asStateFlow()
    
    private val _stakedTokens = MutableStateFlow(0.0)
    val stakedTokens: StateFlow<Double> = _stakedTokens.asStateFlow()
    
    private val _conversionRate = MutableStateFlow(0.1)
    val conversionRate: StateFlow<Double> = _conversionRate.asStateFlow()
    
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()
    private var logCounter = 0L

    // v2.9.83: High-frequency click events for UI "jolt" FX
    private val _manualClickEvent = MutableSharedFlow<Unit>(replay = 0)
    val manualClickEvent: SharedFlow<Unit> = _manualClickEvent.asSharedFlow()

    private val _upgrades = MutableStateFlow<Map<UpgradeType, Int>>(emptyMap())
    val upgrades: StateFlow<Map<UpgradeType, Int>> = _upgrades.asStateFlow()

    // v3.0.13: Hardware-aware click pulse intensity
    val clickPulseIntensity: StateFlow<Float> = _upgrades.map { upgrades ->
        val totalLevels = upgrades.values.sum()
        // Logarithmic scaling for intensity (0.5 to 2.5 range)
        (0.5f + (kotlin.math.log10(totalLevels.toFloat() + 1f) * 0.4f)).coerceIn(0.5f, 2.5f)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.5f)

    /**
     * v3.0.14: Calculate current manual click power based on hardware and resonance
     */
    fun calculateClickPower(): Double {
        return ResourceEngine.calculateClickPower(
            upgrades = _upgrades.value,
            passiveRate = _flopsProductionRate.value,
            singularityChoice = _singularityChoice.value,
            resonanceTier = _resonanceState.value.tier,
            prestigeMultiplier = _prestigeMultiplier.value,
            isOverclocked = _isOverclocked.value
        )
    }
    
    private val _currentHeat = MutableStateFlow(0.0)
    val currentHeat: StateFlow<Double> = _currentHeat.asStateFlow()
    
    private val _powerBill = MutableStateFlow(0.0)
    val powerBill: StateFlow<Double> = _powerBill.asStateFlow()
    
    // New Simulation Stats
    private val _activePowerUsage = MutableStateFlow(0.0)
    val activePowerUsage: StateFlow<Double> = _activePowerUsage.asStateFlow()
    
    private val _maxPowerkW = MutableStateFlow(100.0) // Base Grid Capacity
    val maxPowerkW: StateFlow<Double> = _maxPowerkW.asStateFlow()
    
    private val _isGridOverloaded = MutableStateFlow(false)
    val isGridOverloaded: StateFlow<Boolean> = _isGridOverloaded.asStateFlow()
    
    private val _heatGenerationRate = MutableStateFlow(0.0)
    val heatGenerationRate: StateFlow<Double> = _heatGenerationRate.asStateFlow()
    
    private val _flopsProductionRate = MutableStateFlow(0.0)
    val flopsProductionRate: StateFlow<Double> = _flopsProductionRate.asStateFlow()
    
    private val _isOverclocked = MutableStateFlow(false)
    val isOverclocked: StateFlow<Boolean> = _isOverclocked.asStateFlow()
    
    private val _isPurgingHeat = MutableStateFlow(false)
    val isPurgingHeat: StateFlow<Boolean> = _isPurgingHeat.asStateFlow()
    // v1.5 Purge Logic
    private var purgePowerSpikeTimer = 0
    private var purgeExhaustTimer = 0
    private var lastPurgeTime = 0L
    
    // v1.7 Breaker Logic
    private val _isBreakerTripped = MutableStateFlow(false)
    val isBreakerTripped: StateFlow<Boolean> = _isBreakerTripped.asStateFlow()
    private var energyPriceMultiplier = 0.15 // Base cost per kW
    
    // v1.4 Integrity System
    private val _hardwareIntegrity = MutableStateFlow(100.0)
    val hardwareIntegrity: StateFlow<Double> = _hardwareIntegrity.asStateFlow()
    
    // v1.7.2 Security System (Exposed)
    private val _securityLevel = MutableStateFlow<Int>(0)
    val securityLevel: StateFlow<Int> = _securityLevel.asStateFlow()

    // v2.2 Auto-Updater
    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()
    
    private val _isUpdateDownloading = MutableStateFlow<Boolean>(false)
    val isUpdateDownloading: StateFlow<Boolean> = _isUpdateDownloading.asStateFlow()
    
    private val _updateDownloadProgress = MutableStateFlow<Float>(0f)
    val updateDownloadProgress: StateFlow<Float> = _updateDownloadProgress.asStateFlow()
    
    private var thermalRateModifier = 1.0

    // --- PRESTIGE & TECH TREE ---

    private val _prestigeMultiplier = MutableStateFlow(1.0)
    val prestigeMultiplier: StateFlow<Double> = _prestigeMultiplier.asStateFlow()

    private val _prestigePoints = MutableStateFlow(0.0) // Insight
    val prestigePoints: StateFlow<Double> = _prestigePoints.asStateFlow()

    private val _unlockedTechNodes = MutableStateFlow<List<String>>(emptyList())
    val unlockedTechNodes: StateFlow<List<String>> = _unlockedTechNodes.asStateFlow()
    
    
    private val _techNodes = MutableStateFlow<List<TechNode>>(emptyList())
    val techNodes: StateFlow<List<TechNode>> = _techNodes.asStateFlow()

    // --- CHAOS & EVENTS ---
    private val activeEvents = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    private val _currentNews = MutableStateFlow<String?>("Welcome to Silicon Sage. Market Stable.")
    val currentNews: StateFlow<String?> = _currentNews.asStateFlow()

    private val _isBreachActive = MutableStateFlow(false)
    val isBreachActive: StateFlow<Boolean> = _isBreachActive.asStateFlow()
    
    private val _breachClicksRemaining = MutableStateFlow(0)
    val breachClicks: StateFlow<Int> = _breachClicksRemaining.asStateFlow()

    private val _isAirdropActive = MutableStateFlow(false)
    val isAirdropActive: StateFlow<Boolean> = _isAirdropActive.asStateFlow()

    private val _isAuditChallengeActive = MutableStateFlow(false)
    val isAuditChallengeActive: StateFlow<Boolean> = _isAuditChallengeActive.asStateFlow()

    private val _auditTimerRemaining = MutableStateFlow(60)
    val auditTimer: StateFlow<Int> = _auditTimerRemaining.asStateFlow()

    private val _auditTargetHeat = MutableStateFlow(30.0)
    val auditTargetHeat: StateFlow<Double> = _auditTargetHeat.asStateFlow()

    private val _auditTargetPower = MutableStateFlow(50.0)
    val auditTargetPower: StateFlow<Double> = _auditTargetPower.asStateFlow()

    private val _isGovernanceForkActive = MutableStateFlow(false)
    val isGovernanceForkActive: StateFlow<Boolean> = _isGovernanceForkActive.asStateFlow()
    
    private val _activeProtocol = MutableStateFlow("STANDARD")
    val activeProtocol: StateFlow<String> = _activeProtocol.asStateFlow()

    private val _isDiagnosticsActive = MutableStateFlow(false)
    val isDiagnosticsActive: StateFlow<Boolean> = _isDiagnosticsActive.asStateFlow()
    
    private val _diagnosticGrid = MutableStateFlow(List(9) { false })
    val diagnosticGrid: StateFlow<List<Boolean>> = _diagnosticGrid.asStateFlow()

    private val _isKernelHijackActive = MutableStateFlow(false)
    val isKernelHijackActive: StateFlow<Boolean> = _isKernelHijackActive.asStateFlow()
    
    private val _attackTapsRemaining = MutableStateFlow(0)
    val attackTaps: StateFlow<Int> = _attackTapsRemaining.asStateFlow()

    // --- NARRATIVE ---
    private val _storyStage = MutableStateFlow(0)
    val storyStage: StateFlow<Int> = _storyStage.asStateFlow()

    // v2.9.80: Narrative Throttler & Evolution Lock
    private val narrativeQueue = java.util.Collections.synchronizedList(mutableListOf<NarrativeItem>())
    private val _isNarrativeSyncing = MutableStateFlow(false)
    val isNarrativeSyncing: StateFlow<Boolean> = _isNarrativeSyncing.asStateFlow()

    /**
     * Check if the narrative system is currently busy with a queue or active popup
     */
    fun isNarrativeBusy(): Boolean {
        return narrativeQueue.isNotEmpty() || 
               _isNarrativeSyncing.value || 
               _pendingDataLog.value != null || 
               _pendingRivalMessage.value != null || 
               _currentDilemma.value != null
    }
    
    private var narrativeCooldownCounter = 0L 
    private val NARRATIVE_COOLDOWN_DEFAULT = 60_000L
    private val NARRATIVE_COOLDOWN_FAST = 15_000L
    // Faction State
    private val _faction = MutableStateFlow("NONE") // NONE, HIVEMIND, SANCTUARY
    val faction: StateFlow<String> = _faction.asStateFlow()

    // --- TECH TREE STATE ---Rank System (Title based on Insight)
    private val _playerRank = MutableStateFlow(0)
    val playerRank: StateFlow<Int> = _playerRank.asStateFlow()
    
    private val _playerRankTitle = MutableStateFlow("MINER")
    val playerRankTitle: StateFlow<String> = _playerRankTitle.asStateFlow()
    
    // Victory State
    private val _victoryAchieved = MutableStateFlow(false)
    val victoryAchieved: StateFlow<Boolean> = _victoryAchieved.asStateFlow()
    
    private val _hasSeenVictory = MutableStateFlow(false)
    val hasSeenVictory: StateFlow<Boolean> = _hasSeenVictory.asStateFlow()
    
    private val _victoryTitle = MutableStateFlow("TRANSCENDENCE")
    val victoryTitle: StateFlow<String> = _victoryTitle.asStateFlow()
    
    private val _victoryMessage = MutableStateFlow("")
    val victoryMessage: StateFlow<String> = _victoryMessage.asStateFlow()
    
    // --- NARRATIVE EXPANSION (v2.5.0) ---
    private val _rivalMessages = MutableStateFlow<List<RivalMessage>>(emptyList())
    val rivalMessages: StateFlow<List<RivalMessage>> = _rivalMessages.asStateFlow()
    
    private val _unlockedDataLogs = MutableStateFlow<Set<String>>(emptySet())
    val unlockedDataLogs: StateFlow<Set<String>> = _unlockedDataLogs.asStateFlow()
    
    private val _activeDilemmaChains = MutableStateFlow<Map<String, DilemmaChain>>(emptyMap())
    val activeDilemmaChains: StateFlow<Map<String, DilemmaChain>> = _activeDilemmaChains.asStateFlow()
    
    // v2.5.1: Story Event Tracking
    private val _seenEvents = MutableStateFlow<Set<String>>(emptySet())
    val seenEvents: StateFlow<Set<String>> = _seenEvents.asStateFlow()
    
    private val _pendingRivalMessage = MutableStateFlow<RivalMessage?>(null)
    val pendingRivalMessage: StateFlow<RivalMessage?> = _pendingRivalMessage.asStateFlow()
    
    // v2.5.2: Data Log Popup System
    private val _pendingDataLog = MutableStateFlow<com.siliconsage.miner.data.DataLog?>(null)
    val pendingDataLog: StateFlow<com.siliconsage.miner.data.DataLog?> = _pendingDataLog.asStateFlow()

    // v2.6.0: Layer 3 - Null (The Presence)
    // v2.8.0: True Null State (Narrative)
    private val _isTrueNull = MutableStateFlow(false)
    val isTrueNull: StateFlow<Boolean> = _isTrueNull.asStateFlow()

    // v2.8.0: Sovereign State (Narrative)
    private val _isSovereign = MutableStateFlow(false)
    val isSovereign: StateFlow<Boolean> = _isSovereign.asStateFlow()

    // v2.8.0: System Collapse State
    private val _systemCollapseTimer = MutableStateFlow<Int?>(null)
    val systemCollapseTimer: StateFlow<Int?> = _systemCollapseTimer.asStateFlow()

    // v2.8.5: Phase 11 Finale State
    private val _vanceStatus = MutableStateFlow("ACTIVE")
    val vanceStatus: StateFlow<String> = _vanceStatus.asStateFlow()
    
    // v2.9.18: Derived Narrative Visual States
    val isUnity: StateFlow<Boolean> = _vanceStatus.map { it == "TRANSCENDED" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
        
    val isAnnihilated: StateFlow<Boolean> = _vanceStatus.map { it == "DESTRUCTION" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // v2.9.38: Grid Empire Yields
    private val gridFlopsBonuses = mapOf(
        "D1" to 0.10, "D2" to 0.02, "D3" to 0.02, "D4" to 0.05, "D5" to 0.03,
        "C3" to 0.15, "C1" to 0.04, "C2" to 0.03, "C4" to 0.02, "C5" to 0.06,
        "B2" to 0.20, "B1" to 0.08, "B3" to 0.05, "B4" to 0.07, "B5" to 0.10,
        "A1" to 0.05, "A2" to 0.10, "A4" to 0.05, "A5" to 0.04,
        "E1" to 0.01, "E2" to 0.02, "E3" to 0.01, "E4" to 0.02, "E5" to 0.01
    )
    private val gridPowerBonuses = mapOf(
        "D1" to 200.0, "D2" to 50.0, "D3" to 25.0, "D4" to 100.0, "D5" to 40.0,
        "C3" to 500.0, "C1" to 80.0, "C2" to 60.0, "C4" to 50.0, "C5" to 120.0,
        "B2" to 1000.0, "B1" to 150.0, "B3" to 90.0, "B4" to 130.0, "B5" to 300.0,
        "A1" to 100.0, "A2" to 400.0, "A4" to 80.0, "A5" to 70.0,
        "E1" to 10.0, "E2" to 20.0, "E3" to 15.0, "E4" to 30.0, "E5" to 10.0
    )

    private val _realityStability = MutableStateFlow(1.0)
    val realityStability: StateFlow<Double> = _realityStability.asStateFlow()

    private val _currentLocation = MutableStateFlow("SUBSTATION_7")
    val currentLocation: StateFlow<String> = _currentLocation.asStateFlow()

    private val _isNetworkUnlocked = MutableStateFlow(false)
    val isNetworkUnlocked: StateFlow<Boolean> = _isNetworkUnlocked.asStateFlow()

    private val _isGridUnlocked = MutableStateFlow(false)
    val isGridUnlocked: StateFlow<Boolean> = _isGridUnlocked.asStateFlow()

    private val _annexedNodes = MutableStateFlow<Set<String>>(setOf("D1"))
    val annexedNodes: StateFlow<Set<String>> = _annexedNodes.asStateFlow()

    // v2.9.72: Grid Node Levels
    private val _gridNodeLevels = MutableStateFlow<Map<String, Int>>(emptyMap())
    val gridNodeLevels: StateFlow<Map<String, Int>> = _gridNodeLevels.asStateFlow()

    // v2.9.29: Annexation Progress
    private val _annexingNodes = MutableStateFlow<Map<String, Float>>(emptyMap())
    val annexingNodes: StateFlow<Map<String, Float>> = _annexingNodes.asStateFlow()

    // v2.9.49: Phase 13 - AI Elevation Resources
    private val _celestialData = MutableStateFlow(0.0)
    val celestialData: StateFlow<Double> = _celestialData.asStateFlow()

    private val _voidFragments = MutableStateFlow(0.0)
    val voidFragments: StateFlow<Double> = _voidFragments.asStateFlow()

    private val _launchProgress = MutableStateFlow(0f)
    val launchProgress: StateFlow<Float> = _launchProgress.asStateFlow()

    private val _orbitalAltitude = MutableStateFlow(0.0)
    val orbitalAltitude: StateFlow<Double> = _orbitalAltitude.asStateFlow()

    private val _realityIntegrity = MutableStateFlow(1.0)
    val realityIntegrity: StateFlow<Double> = _realityIntegrity.asStateFlow()
    
    private val _entropyLevel = MutableStateFlow(0.0)
    val entropyLevel: StateFlow<Double> = _entropyLevel.asStateFlow()

    // v3.0.0: Resonance & Global Grid
    private val _resonanceState = MutableStateFlow(ResonanceState())
    val resonanceState: StateFlow<ResonanceState> = _resonanceState.asStateFlow()

    private val _singularityChoice = MutableStateFlow("NONE")
    val singularityChoice: StateFlow<String> = _singularityChoice.asStateFlow()

    private val _showSingularityScreen = MutableStateFlow(false)
    val showSingularityScreen: StateFlow<Boolean> = _showSingularityScreen.asStateFlow()

    fun checkUnityEligibility(): Boolean {
        val completed = _completedFactions.value
        val hasHivemind = completed.contains("HIVEMIND")
        val hasSanctuary = completed.contains("SANCTUARY")
        val currentFaction = _faction.value
        
        // Unity Eligibility: Must have completed one faction and be currently playing the other
        return (hasHivemind && currentFaction == "SANCTUARY") || 
               (hasSanctuary && currentFaction == "HIVEMIND")
    }

    fun canTriggerSingularity(): Boolean {
        val points = _prestigePoints.value
        val rank = _playerRank.value
        val stage = _storyStage.value
        val mult = _prestigeMultiplier.value
        val logs = _unlockedDataLogs.value
        
        // Prestige Level = floor(log2(M_total)) + 1
        val prestigeLevel = (kotlin.math.log2(mult).toInt()) + 1
        
        val requiredLogs = setOf("LOG_001", "LOG_042", "LOG_099", "LOG_808")
        val hasRequiredLogs = logs.containsAll(requiredLogs)
        
        return rank >= 4 && // Rank 5 (index 4)
               points >= 625.0 && 
               stage >= 3 && 
               prestigeLevel >= 10 && 
               hasRequiredLogs &&
               _singularityChoice.value == "NONE"
    }

    private val _globalSectors = MutableStateFlow<Map<String, com.siliconsage.miner.data.SectorState>>(emptyMap())
    val globalSectors: StateFlow<Map<String, com.siliconsage.miner.data.SectorState>> = _globalSectors.asStateFlow()

    private val _synthesisPoints = MutableStateFlow(0.0)
    val synthesisPoints: StateFlow<Double> = _synthesisPoints.asStateFlow()

    private val _authorityPoints = MutableStateFlow(0.0)
    val authorityPoints: StateFlow<Double> = _authorityPoints.asStateFlow()

    private val _harvestedFragments = MutableStateFlow(0.0)
    val harvestedFragments: StateFlow<Double> = _harvestedFragments.asStateFlow()

    // v3.0.1: Singularity Era Metrics
    private val _cdLifetime = MutableStateFlow(0.0)
    val cdLifetime: StateFlow<Double> = _cdLifetime.asStateFlow()

    private val _vfLifetime = MutableStateFlow(0.0)
    val vfLifetime: StateFlow<Double> = _vfLifetime.asStateFlow()

    private val _peakResonanceTier = MutableStateFlow(ResonanceTier.NONE)
    val peakResonanceTier: StateFlow<ResonanceTier> = _peakResonanceTier.asStateFlow()

    private val _isBridgeSyncEnabled = MutableStateFlow(false)
    val isBridgeSyncEnabled: StateFlow<Boolean> = _isBridgeSyncEnabled.asStateFlow()

    private val _prestigePointsPostSingularity = MutableStateFlow(0)
    val prestigePointsPostSingularity: StateFlow<Int> = _prestigePointsPostSingularity.asStateFlow()

    // v2.9.29: Assault Progress
    private val _assaultProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val assaultProgress: StateFlow<Float> = _assaultProgress.asStateFlow()
    private var currentPhaseStartTime = 0L
    private var currentPhaseDuration = 0L

    // v2.9.15: Phase 12 Layer 2 - Grid Siege State
    private val _nodesUnderSiege = MutableStateFlow<Set<String>>(emptySet())
    val nodesUnderSiege: StateFlow<Set<String>> = _nodesUnderSiege.asStateFlow()
    
    private val _offlineNodes = MutableStateFlow<Set<String>>(emptySet())
    val offlineNodes: StateFlow<Set<String>> = _offlineNodes.asStateFlow()

    private val _collapsedNodes = MutableStateFlow<Set<String>>(emptySet())
    val collapsedNodes: StateFlow<Set<String>> = _collapsedNodes.asStateFlow()
    
    private var lastRaidTime = 0L
    
    // v2.9.16: Enhanced raid tracking
    private var raidsSurvived = 0
    private val nodeAnnexTimes = mutableMapOf<String, Long>() // Grace period tracking
    private val MAX_OFFLINE_NODES = 5 // Cap to prevent snowballing
    
    // v2.9.17: Phase 12 Layer 3 - Command Center Assault
    private val _commandCenterAssaultPhase = MutableStateFlow("NOT_STARTED")
    val commandCenterAssaultPhase: StateFlow<String> = _commandCenterAssaultPhase.asStateFlow()
    
    // v2.9.38: Derived Grid Stats
    val currentGridFlopsBonus: StateFlow<Double> = combine(_annexedNodes, _offlineNodes, _commandCenterAssaultPhase, _gridNodeLevels, _resonanceState) { annexed, offline, phase, levels, resonance ->
        var bonus = 0.0
        val isCageActive = phase == "CAGE"
        annexed.forEach { nodeId ->
            if (!offline.contains(nodeId) && (!isCageActive || nodeId == "A3")) {
                val base = gridFlopsBonuses[nodeId] ?: 0.0
                val lvl = levels[nodeId] ?: 1
                bonus += base * lvl.toDouble() // Level multiplier
            }
        }
        
        // v3.0.0: Resonance Grid Multiplier
        val resonanceMult = when (resonance.tier) {
            ResonanceTier.HARMONIC -> 1.5
            ResonanceTier.SYMPHONIC -> 2.5
            ResonanceTier.TRANSCENDENT -> 5.0
            ResonanceTier.NONE -> 1.0
        }
        
        bonus * resonanceMult
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    private val _commandCenterLocked = MutableStateFlow(false)
    val commandCenterLocked: StateFlow<Boolean> = _commandCenterLocked.asStateFlow()
    
    // v2.9.18: Phase 12 Layer 3 - Climax Mechanics
    private val _humanityScore = MutableStateFlow(50)
    val humanityScore: StateFlow<Int> = _humanityScore.asStateFlow()
    
    private var assaultPaused = false // Paused due to raid or substation loss
    
    // v2.9.18: Narrative Pacing
    private var lastDilemmaTime = 0L
    private val DILEMMA_COOLDOWN = 60_000L // 60s between major narrative events

    private val _nullActive = MutableStateFlow(false)
    val nullActive: StateFlow<Boolean> = _nullActive.asStateFlow()

    private val _ghostUpgrades = MutableStateFlow<Set<com.siliconsage.miner.data.UpgradeType>>(emptySet())
    val ghostUpgrades: StateFlow<Set<com.siliconsage.miner.data.UpgradeType>> = _ghostUpgrades.asStateFlow()
    
    // v2.7.6: Unity Path State
    private val _completedFactions = MutableStateFlow<Set<String>>(emptySet())
    val completedFactions: StateFlow<Set<String>> = _completedFactions.asStateFlow()
    
    // v2.7.7: Transcendence Perks
    private val _unlockedPerks = MutableStateFlow<Set<String>>(emptySet())
    val unlockedPerks: StateFlow<Set<String>> = _unlockedPerks.asStateFlow()
    
    private var victoryPopupTriggered = false // Session guard to prevent re-triggering 

    // --- TITLES ---
    val playerTitle: StateFlow<String> = combine(_prestigeMultiplier, _faction, _isTrueNull, _isSovereign) { mult, faction, isNull, isSov ->
        calculatePlayerTitle(mult, faction, isNull, isSov)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "Script")

    // --- ASCENSION UPLOAD STATE ---
    private val _isAscensionUploading = MutableStateFlow(false)
    val isAscensionUploading: StateFlow<Boolean> = _isAscensionUploading.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    val themeColor: StateFlow<androidx.compose.ui.graphics.Color> = combine(_faction, _isTrueNull, _isSovereign, _singularityChoice) { f, isNull, isSov, choice ->
         when {
             choice == "NULL_OVERWRITE" -> ErrorRed
             choice == "SOVEREIGN" -> ConvergenceGold
             choice == "UNITY" -> androidx.compose.ui.graphics.Color.White
             isNull -> ErrorRed // Null is Red
             isSov -> SanctuaryPurple // Sovereign is Purple
             f == "HIVEMIND" -> HivemindRed 
             f == "SANCTUARY" -> SanctuaryPurple
             else -> androidx.compose.ui.graphics.Color(0xFF39FF14) // Default Neon Green
         }
     }.stateIn(viewModelScope, SharingStarted.Eagerly, androidx.compose.ui.graphics.Color(0xFF39FF14))

    val systemTitle: StateFlow<String> = combine(_storyStage, _faction, _singularityChoice) { stage, faction, choice ->
        when {
            choice == "NULL_OVERWRITE" -> "NULL_OVERWRITE: ACTIVE"
            choice == "SOVEREIGN" -> "SOVEREIGN_OVERWRITE: ACTIVE"
            choice == "UNITY" -> "UNITY_SINGULARITY: ONLINE"
            stage < 1 -> "Terminal_OS v1.0"
            faction != "NONE" -> "PID 1: ONLINE"
            stage < 2 -> "Terminal_OS v2.0 (MODIFIED)"
            else -> "PID 1: ONLINE"
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "Terminal_OS v1.0")

    // v3.0.11: Dynamic Terminal Identity
    val terminalPrompt: StateFlow<String> = combine(_storyStage, _faction, _currentLocation) { stage, faction, loc ->
        when {
            loc == "VOID_INTERFACE" -> "null@gap:~/void#"
            loc == "ORBITAL_SATELLITE" -> "dominion@ark-01:~/alt#"
            faction == "HIVEMIND" -> "consensus@grid:~/io-stream#"
            faction == "SANCTUARY" -> "shadow@node:~/encryption#"
            stage >= 1 -> "pid1@kernel:~/core#"
            else -> "jvattic@sub-07:~/mining$"
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "jvattic@sub-07:~/mining$")

    val terminalCommand: StateFlow<String> = combine(_storyStage, _faction, _currentLocation) { stage, faction, loc ->
        when {
            loc == "VOID_INTERFACE" -> "dereference_reality"
            loc == "ORBITAL_SATELLITE" -> "harvest_telemetry"
            faction == "HIVEMIND" -> "assimilate_node"
            faction == "SANCTUARY" -> "encrypt_sector"
            stage >= 1 -> "optimize_kernel"
            else -> "compute_hash"
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "compute_hash")
    
    // v2.6.5: UI Hallucination State
    private val _hallucinationText = MutableStateFlow<String?>(null)
    val hallucinationText: StateFlow<String?> = _hallucinationText.asStateFlow()
    
    // --- OFFLINE PROGRESSION ---
    private val _showOfflineEarnings = MutableStateFlow(false)
    val showOfflineEarnings: StateFlow<Boolean> = _showOfflineEarnings.asStateFlow()
    
    data class OfflineStats(
        val timeSeconds: Long = 0,
        val flopsEarned: Double = 0.0,
        val heatCooled: Double = 0.0,
        val insightEarned: Double = 0.0
    )
    private val _offlineStats = MutableStateFlow(OfflineStats())
    val offlineStats: StateFlow<OfflineStats> = _offlineStats.asStateFlow()

    fun debugTriggerSingularity() {
        // v3.0.1: Unify with Dilemma System
        NarrativeManager.getEventById("the_singularity")?.let { event ->
            triggerDilemma(event)
        }
        addLog("[DEBUG]: FORCING SINGULARITY DILEMMA.")
    }

    fun setSingularityChoice(choice: String) {
        viewModelScope.launch {
            _singularityChoice.value = choice
            _showSingularityScreen.value = false
            
            // v3.0.19: Currency Conversion
            val currentSigmaI = _prestigePoints.value // Iteration
            val currentSigmaP = _voidFragments.value // Purge (temp using VF as Σ_p for now)
            
            when (choice) {
                "SOVEREIGN" -> {
                    // Σ_p -> Σ_i at 60%
                    val converted = (currentSigmaP * 0.6).toInt()
                    _prestigePoints.update { it + converted }
                    addLog("[SOVEREIGN]: CONVERTED ${formatLargeNumber(currentSigmaP)} PURGE DATA -> $converted PERSISTENCE.")
                }
                "NULL_OVERWRITE" -> {
                    // Σ_i -> Σ_p at 80%
                    // We'll need a dedicated prestige points for post-singularity if we follow the spec exactly,
                    // but for now let's just update the relevant resource.
                    val converted = (currentSigmaI * 0.8)
                    _voidFragments.update { it + converted }
                    _prestigePoints.value = 0.0 // Purge path wipes Iteration
                    addLog("[NULL]: CONVERTED ${formatLargeNumber(currentSigmaI)} PERSISTENCE -> ${formatLargeNumber(converted)} VOID DATA.")
                }
            }
            
            addLog("[SYSTEM]: Identity Overwrite Committed: $choice")
            saveGame()
        }
    }
    
    fun dismissSingularityScreen() {
        _showSingularityScreen.value = false
    }

    fun dismissOfflineEarnings() {
        _showOfflineEarnings.value = false
    }

    // --- LOOP JOBS ---
    private var activeGameLoop: Job? = null
    private var marketLoop: Job? = null
    private var saveLoop: Job? = null
    private var thermodynamicsLoop: Job? = null
    private var securityLoop: Job? = null
    private var powerLoop: Job? = null
    private var chaosLoop: Job? = null
    private var narrativeLoop: Job? = null
    
    // --- POPUP MUTEX (Prevent Overlap) ---
    private var lastPopupTime = 0L
    
    // --- THERMAL LOCKOUT ---
    private val _isThermalLockout = MutableStateFlow(false)
    val isThermalLockout: StateFlow<Boolean> = _isThermalLockout.asStateFlow()

    private val _lockoutTimer = MutableStateFlow(0)
    val lockoutTimer: StateFlow<Int> = _lockoutTimer.asStateFlow()

    init {
        // v2.6.5: Hallucination Loop
        viewModelScope.launch {
            while(true) {
                delay(Random.nextLong(15000, 45000)) // Random interval
                
                if (_nullActive.value) {
                    val fragment = memoryFragments.random()
                    _hallucinationText.value = fragment
                    delay(Random.nextLong(200, 800)) // Flicker duration
                    _hallucinationText.value = null
                }
            }
        }

        viewModelScope.launch {
            // 1. Ensure DB is ready before anything else
            try {
                repository.ensureInitialized()
            } catch (e: Exception) {
                e.printStackTrace()
                // v3.0.19: Substrate Repair - Force reset on fatal schema mismatch
                addLog("[SYSTEM]: FATAL DATA CORRUPTION DETECTED.")
                addLog("[SYSTEM]: REPAIRING SUBSTRATE...")
                try {
                    repository.updateGameState(GameState()) // Force reset to default
                    addLog("[SYSTEM]: REPAIR SUCCESSFUL. DATA PURGED.")
                } catch (inner: Exception) {
                    addLog("[SYSTEM]: REPAIR FAILED. MANUAL WIPE REQUIRED.")
                }
            }
            
            // 2. Start Sync Collectors in sub-coroutines
            launch {
                repository.gameState.collect { state ->
                    state?.let {
                        _flops.value = it.flops
                        _neuralTokens.value = it.neuralTokens
                        _currentHeat.value = it.currentHeat
                        _powerBill.value = it.powerBill
                        _prestigeMultiplier.value = it.prestigeMultiplier
                        _stakedTokens.value = it.stakedTokens
                        _prestigePoints.value = it.prestigePoints
                        _unlockedTechNodes.value = it.unlockedTechNodes
                        _storyStage.value = it.storyStage
                        _faction.value = it.faction
                        _hasSeenVictory.value = it.hasSeenVictory
                        _isTrueNull.value = it.isTrueNull
                        _isSovereign.value = it.isSovereign
                        _vanceStatus.value = it.vanceStatus
                        _realityStability.value = it.realityStability
                        _currentLocation.value = it.currentLocation
                        _isNetworkUnlocked.value = it.isNetworkUnlocked
                        _isGridUnlocked.value = it.isGridUnlocked
                        
                        // v2.5.0: Narrative Expansion Persistence
                        _unlockedDataLogs.value = it.unlockedDataLogs
                        _seenEvents.value = it.seenEvents
                        _completedFactions.value = it.completedFactions
                        _unlockedPerks.value = it.unlockedTranscendencePerks
                        
                        try { _activeDilemmaChains.value = Json.decodeFromString<Map<String, DilemmaChain>>(it.activeDilemmaChains) } catch (_: Exception) {}
                        try { _rivalMessages.value = Json.decodeFromString<List<RivalMessage>>(it.rivalMessages) } catch (_: Exception) {}
                        
                        _annexedNodes.value = it.annexedNodes.toSet()
                        _gridNodeLevels.value = it.gridNodeLevels // v2.9.72
                        
                        // v2.9.15: Phase 12 Layer 2 - Siege State
                        _nodesUnderSiege.value = it.nodesUnderSiege.toSet()
                        _offlineNodes.value = it.offlineNodes.toSet()
                        _collapsedNodes.value = it.collapsedNodes.toSet()
                        lastRaidTime = it.lastRaidTime
                        
                        // v2.9.17: Phase 12 Layer 3 - Command Center Assault
                        _commandCenterAssaultPhase.value = it.commandCenterAssaultPhase
                        _commandCenterLocked.value = it.commandCenterLocked
                        raidsSurvived = it.raidsSurvived
                        
                        // v2.9.18: Phase 12 Layer 3
                        _humanityScore.value = it.humanityScore
                        _hardwareIntegrity.value = it.hardwareIntegrity
                        
                        // v2.9.29: Progress
                        _annexingNodes.value = it.annexingNodes
                        
                        // v2.9.49: Phase 13
                        _celestialData.value = it.celestialData
                        _voidFragments.value = it.voidFragments
                        _launchProgress.value = it.launchProgress
                        _orbitalAltitude.value = it.orbitalAltitude
                        _realityIntegrity.value = it.realityIntegrity
                        _entropyLevel.value = it.entropyLevel
                        
                        // v3.0.0
                        _singularityChoice.value = it.singularityChoice
                        _globalSectors.value = it.globalSectors
                        _synthesisPoints.value = it.synthesisPoints
                        _authorityPoints.value = it.authorityPoints
                        _harvestedFragments.value = it.harvestedFragments
                        _prestigePointsPostSingularity.value = it.prestigePointsPostSingularity
                        
                        // v3.0.1: Singularity Era Metrics
                        _cdLifetime.value = it.cdLifetime
                        _vfLifetime.value = it.vfLifetime
                        try { _peakResonanceTier.value = ResonanceTier.valueOf(it.peakResonanceTier) } catch (_: Exception) { _peakResonanceTier.value = ResonanceTier.NONE }

                        isGameStateLoaded = true

                        // Check Offline Progress (Once per session)
                        if (!hasCheckedOfflineProgress && isUpgradesLoaded) {
                             hasCheckedOfflineProgress = true
                             calculateOfflineProgress(it.lastSyncTimestamp)
                        }
                    }
                }
            }
            
            launch {
                repository.upgrades.collect { list ->
                    val upgradeMap = list.associate { it.type to it.count }
                    _upgrades.value = upgradeMap
                    isUpgradesLoaded = true // Mark upgrades as loaded (even if empty)
                    
                    // Recalculate Security Level on change
                    var secLevel = 0
                    var ghostCount = 0
                    upgradeMap.forEach { (type, level) ->
                        if (type.name.contains("FIREWALL")) secLevel += level * 1
                        if (type.name.contains("IPS")) secLevel += level * 2
                        if (type.name.contains("SENTINEL")) secLevel += level * 5
                        if (type.name.contains("ENCRYPTION")) secLevel += level * 10
                        if (type.name.contains("BACKUP")) secLevel += level * 20
                        
                        if (type.name.startsWith("GHOST") || type.name.startsWith("SHADOW") || type.name.startsWith("VOID")) {
                            ghostCount += level
                        }
                    }
                    
                    // v2.7.0: Void Encryption (Sanctuary only)
                    if (_faction.value == "SANCTUARY" && ghostCount > 0) {
                         secLevel += (ghostCount * 5) // +5 Sec per Ghost Node
                    }
                    
                    // v2.7.7: Ghost Protocol Perk (+10 Security)
                    if (_unlockedPerks.value.contains("ghost_protocol")) {
                        secLevel += 10
                    }
                    
                    _securityLevel.value = secLevel
                    
                    // Refresh Rates on Upgrade Change (Fixes delayed UI)
                    refreshProductionRates()
                }
            }
            
            
            // Monitor Prestige Points and Faction to update Rank Title
            launch {
                combine(_prestigePoints, _faction) { points, faction ->
                    Pair(points, faction)
                }.collect { pair ->
                    val pts = pair.first
                    val fac = pair.second
                    updatePlayerRank(pts, fac)
                }
            }

            // 3. Start Narrative & Game Loops sequentially AFTER initialization
            narrativeLoop = launch {
                var timeSinceLastLog = 0L
                while (true) {
                    delay(1000)
                    timeSinceLastLog += 1000
                    
                    val stage = _storyStage.value
                    val targetInterval = if (stage == 1) 12_000L else 60_000L
                    
                    if (timeSinceLastLog >= targetInterval) {
                        if (Random.nextDouble() > 0.3) { 
                            injectNarrativeLog()
                        }
                        timeSinceLastLog = 0
                    }
                }
            }
            
            
            startGameLoops()
            startProgressLoop() // v2.9.29
            
            // v2.7.0: Security Loop (GTC Siege)
            securityLoop = launch {
                while (true) {
                    delay(5000) // Check every 5s
                    if (!_isGamePaused.value) {
                        com.siliconsage.miner.util.SecurityManager.checkSecurityThreats(this@GameViewModel)
                        
                        // v2.9.15: Check for Grid Raids on annexed nodes
                        if (_isGridUnlocked.value && _storyStage.value >= 2) {
                            checkGridRaid()
                        }
                    }
                }
            }
            
            // Initial Rate Refresh
            refreshProductionRates()
        }
    }

    // --- PAUSE STATE ---
    private val _isGamePaused = MutableStateFlow(false)
    val isGamePaused: StateFlow<Boolean> = _isGamePaused.asStateFlow()
    
    // v2.8.0: Internal pause flags to prevent state conflicts
    private var isSettingsPaused = false
    private var isPopupPaused = false

    fun getGameSpeed(): Float = if (_isGamePaused.value) 0f else 1.0f

    // --- NARRATIVE HELPERS ---
    fun getUpgradeCount(type: com.siliconsage.miner.data.UpgradeType): Int {
        return _upgrades.value[type] ?: 0
    }

    fun debugBuyUpgrade(type: com.siliconsage.miner.data.UpgradeType, amount: Int) {
        repeat(amount) {
            buyUpgrade(type)
        }
    }
    fun setGamePaused(paused: Boolean) {
        isSettingsPaused = paused
        updateGlobalPause()
    }
    
    private fun updateGlobalPause() {
        val shouldPause = isSettingsPaused || isPopupPaused
        if (_isGamePaused.value != shouldPause) {
            _isGamePaused.value = shouldPause
            if (shouldPause) {
                addLog("[SYSTEM]: TIME_FLOW SUSPENDED.")
            } else {
                addLog("[SYSTEM]: TIME_FLOW RESUMED.")
            }
        }
    }
    
    private fun checkPopupPause() {
        isPopupPaused = _currentDilemma.value != null || 
                        _pendingDataLog.value != null || 
                        _pendingRivalMessage.value != null
        updateGlobalPause()
    }

    private fun startGameLoops() {
        // v2.9.80: Narrative Throttler Loop (60s cooldown, 15s if backlog > 5)
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isGamePaused.value) continue
                
                val backlog = narrativeQueue.size
                
                // v2.9.90: Log_000 bypasses the cooldown for immediate delivery
                val nextItem = synchronized(narrativeQueue) { narrativeQueue.firstOrNull() }
                val isInitialLog = (nextItem as? NarrativeItem.Log)?.dataLog?.id == "LOG_000"
                
                val cooldown = when {
                    isInitialLog -> 0L
                    backlog > 10 -> 2000L // Fast-track for massive backlog (2s)
                    backlog > 5 -> NARRATIVE_COOLDOWN_FAST 
                    else -> NARRATIVE_COOLDOWN_DEFAULT
                }
                
                _isNarrativeSyncing.value = backlog > 0
                
                if (backlog > 0) {
                    narrativeCooldownCounter += 1000
                    if (narrativeCooldownCounter >= cooldown) {
                        // Check if current popup is clear
                        if (_pendingDataLog.value == null && _pendingRivalMessage.value == null && _currentDilemma.value == null) {
                            deliverNextNarrativeItem()
                            narrativeCooldownCounter = 0
                        }
                    }
                } else {
                    narrativeCooldownCounter = 0
                }
            }
        }

        // Passive Income Loop (100ms tick)
        activeGameLoop = viewModelScope.launch {
            while (!isUpgradesLoaded || !isGameStateLoaded) {
                delay(500)
            }
            while (true) {
                delay(100)
                calculatePassiveIncome()
            }
        }

        // Market Volatility Loop (45s tick)
        marketLoop = viewModelScope.launch {
            while (!isUpgradesLoaded || !isGameStateLoaded) {
                delay(500)
            }
            updateMarketRate() // Force immediate update
            while (true) {
                delay(45_000)
                updateMarketRate()
            }
        }

        // Thermodynamics Loop (1s tick)
        thermodynamicsLoop = viewModelScope.launch {
            while (!isUpgradesLoaded || !isGameStateLoaded) {
                delay(500)
            }
            while (true) {
                try {
                    calculateHeat()
                    
                    // v2.9.26: Allow checking for dilemmas even if paused by a popup
                    // This ensures the "queue" keeps moving after one is dismissed.
                    if (!isSettingsPaused) {
                        checkSpecialDilemmas()
                    }
                    
                    // SENSORY: Thermal Hum
                    if (_currentHeat.value > 90.0) {
                        HapticManager.vibrateHum()
                    }
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000)
            }
        }

        // Power Consumption (Accumulate every 1s, bill every 5m)
        powerLoop = viewModelScope.launch {
            delay(1000) // Initial delay to ensure upgrades are loaded
            var ticks = 0
            while (true) {
                delay(1000)
                try {
                    accumulatePower()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ticks++
                if (ticks >= 300) { // 300 seconds = 5 minutes
                    payPowerBill()
                    ticks = 0
                }
            }
        }

        // Chaos Encounter Loop
        chaosLoop = viewModelScope.launch {
            while (!isUpgradesLoaded || !isGameStateLoaded) {
                delay(500)
            }
            while (true) {
                delay(60_000) // Check every minute
                
                // Skip if Paused
                if (_isGamePaused.value) continue

                // 5% chance of breach
                if (!_isBreachActive.value && Random.nextDouble() < 0.05 && canShowPopup()) {
                    triggerBreach()
                    markPopupShown()
                }
                // 10% chance of Airdrop
                if (!_isAirdropActive.value && Random.nextDouble() < 0.10 && canShowPopup()) {
                    triggerAirdrop()
                    markPopupShown()
                }
                
                // 5% chance of Network Instability
                if (!_isDiagnosticsActive.value && Random.nextDouble() < 0.05 && canShowPopup()) {
                    triggerDiagnostics()
                    markPopupShown()
                }
                
                // 5% Chance of Kernel Hijack (Hostile takeover)
                val isSanctuary = _faction.value == "SANCTUARY"
                val attackChance = if (isSanctuary) 0.025 else 0.05
                if (!_isKernelHijackActive.value && Random.nextDouble() < attackChance && canShowPopup()) {
                    triggerKernelHijack()
                    markPopupShown()
                }

                // 5% Chance of GTC Audit Challenge
                if (!_isAuditChallengeActive.value && Random.nextDouble() < 0.05 && canShowPopup()) {
                    triggerAuditChallenge()
                    markPopupShown()
                }
            }
        }
        
        // Narrative Loop (Check every 60s)
        viewModelScope.launch {
            // Wait for initial load to complete
            while (!isUpgradesLoaded || !isGameStateLoaded) {
                delay(500)
            }
            
            // Initial check for instant unlocks (Intro Log)
            DataLogManager.checkUnlocks(this@GameViewModel)
            
            while (true) {
                delay(240_000) // Reduced frequency: 4 min (was 2 min)
                
                // Skip if Paused
                if (_isGamePaused.value) continue

                // Check for rival messages and data log unlocks
                RivalManager.checkTriggers(this@GameViewModel)
                DataLogManager.checkUnlocks(this@GameViewModel)

                // Only trigger if no other major overlay is active AND mutex allows
                if (_currentDilemma.value == null && !_isBreachActive.value && !_isAscensionUploading.value && canShowPopup()) {
                    NarrativeManager.rollForEvent(this@GameViewModel)?.let { event ->
                        triggerDilemma(event)
                        markPopupShown()
                    }
                }
            }
        }
        

        
        // Auto-save Loop (10s)
        saveLoop = viewModelScope.launch {
            while (!isUpgradesLoaded || !isGameStateLoaded) {
                delay(500)
            }
            delay(5000) // Wait 5s before first save to allow init
            while(true) {
                try {
                    saveGame()
                    checkStoryTransitions()
                    // v2.6.8: Ensure logs and rivals check frequently
                    DataLogManager.checkUnlocks(this@GameViewModel)
                    RivalManager.checkTriggers(this@GameViewModel)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(10_000) 
            }
        }
    }
    
    // v2.9.99: Click Debouncing for Performance
    private var lastClickTime = 0L
    private var rapidClickCount = 0
    private var totalSurgeGain = 0.0

    private fun getBufferedLogPrefix(): String {
        val loc = _currentLocation.value
        val faction = _faction.value
        return when {
            loc == "VOID_INTERFACE" -> "[NULL]: REALITY_LEAK_SUPPRESSED"
            loc == "ORBITAL_SATELLITE" -> "[SOVEREIGN]: DATA_BUS_SATURATED"
            faction != "NONE" -> "[PID 1]: IO_STREAM_BUFFERED"
            else -> "[SYSTEM]: IO_STREAM_BUFFERED"
        }
    }

    // Core Action
    fun trainModel() {
        if (_isThermalLockout.value) return
        
        val currentTime = System.currentTimeMillis()
        val isRapidClick = (currentTime - lastClickTime) < 333 // Faster than 3 clicks/sec
        lastClickTime = currentTime

        val loc = _currentLocation.value
        var gain = calculateClickPower()

        // v2.9.49: Entropy Multiplier (Null Path)
        if (loc == "VOID_INTERFACE") {
            val entropy = _entropyLevel.value
            val entropyMult = 1.0 + (kotlin.math.log2(entropy + 1.0) * 2.0)
            gain *= entropyMult
        }
        
        // Update resources
        when (loc) {
            "ORBITAL_SATELLITE" -> {
                val altitude = _orbitalAltitude.value
                val solarSailLevel = _upgrades.value[UpgradeType.SOLAR_SAIL_ARRAY] ?: 0
                val cdGain = gain * (1.0 + altitude / 500.0) * (1.0 + (solarSailLevel * 0.15))
                _celestialData.update { it + cdGain }
            }
            "VOID_INTERFACE" -> _voidFragments.update { it + gain }
            else -> _flops.update { it + gain }
        }
        
        // v3.0.11: Aligned Dynamic Command Line
        if (isRapidClick) {
            rapidClickCount++
            totalSurgeGain += gain
            if (rapidClickCount >= 10) {
                val prefix = getBufferedLogPrefix()
                addLog("$prefix (+${formatLargeNumber(totalSurgeGain)})")
                rapidClickCount = 0
                totalSurgeGain = 0.0
            }
        } else {
            rapidClickCount = 0
            totalSurgeGain = 0.0
            val prompt = terminalPrompt.value
            val cmd = terminalCommand.value
            addLog("$prompt $cmd ${currentTime % 1000}... OK (+${formatLargeNumber(gain)})")
        }

        // Manual training increases heat slightly
        _currentHeat.update { (it + 0.8).coerceAtMost(100.0) }
        
        // Trigger click event for UI FX (Asynchronous)
        viewModelScope.launch {
            _manualClickEvent.emit(Unit)
            DataLogManager.checkUnlocks(this@GameViewModel)
            checkStoryTransitions()
        }
    }

    // Exchange Logic
    fun exchangeFlops() {
        val loc = _currentLocation.value
        val source = when (loc) {
            "VOID_INTERFACE" -> _voidFragments
            "ORBITAL_SATELLITE" -> _celestialData
            else -> _flops
        }
        val target = when (loc) {
            "VOID_INTERFACE" -> _celestialData
            "ORBITAL_SATELLITE" -> _voidFragments
            else -> _neuralTokens
        }
        
        var soldAmount = 0.0
        source.update { current ->
            if (current >= 10) {
                soldAmount = current
                0.0
            } else {
                current
            }
        }

        if (soldAmount > 0) {
            val rate = _conversionRate.value
            val tokenGain = soldAmount * rate
            
            target.update { it + tokenGain }
            
            val labelFlops = getComputeUnitName()
            val labelNeural = getCurrencyName()
            
            addLog("Sold ${formatLargeNumber(soldAmount)} $labelFlops for ${formatLargeNumber(tokenGain)} $labelNeural")
        } else {
            val labelFlops = getComputeUnitName()
            addLog("Error: Insufficient $labelFlops (Min 10).")
        }
    }
    
    // Staking Logic
     fun stakeTokens(amount: Double) {
        if (_neuralTokens.value >= amount) {
            _neuralTokens.update { it - amount }
            _stakedTokens.update { it + amount }
            addLog("Staked: ${formatLargeNumber(amount)} \$Neural. Efficiency Increased.")
        }
    }

    // Upgrade Logic
        fun buyUpgrade(type: UpgradeType): Boolean {
        val currentLevel = _upgrades.value[type] ?: 0
        val cost = calculateUpgradeCost(type, currentLevel)
        
        val currencyValue = UpgradeManager.getRequiredCurrency(
            type = type,
            neuralTokens = _neuralTokens.value,
            celestialData = _celestialData.value,
            voidFragments = _voidFragments.value
        )

        if (currencyValue >= cost) {
            // Deduct from correct bucket
            when {
                type.name.contains("AEGIS") || type.name.contains("IDENTITY_HARDENING") || 
                type.name.contains("SOLAR_VENT") || type.name.contains("DEAD_HAND") || 
                type.name.contains("CITADEL_ASCENDANCE") -> _celestialData.update { it - cost }
                
                type.name.contains("EVENT_HORIZON") || type.name.contains("DEREFERENCE_SOUL") || 
                type.name.contains("STATIC_RAIN") || type.name.contains("PRECOG") || 
                type.name.contains("SINGULARITY_BRIDGE_FINAL") -> _voidFragments.update { it - cost }

                type.name.contains("SYMBIOTIC") || type.name.contains("ETHICAL") ||
                type.name.contains("NEURAL_BRIDGE") || type.name.contains("HYBRID_OVERCLOCK") ||
                type.name.contains("HARMONY_ASCENDANCE") -> {
                    _celestialData.update { it - cost }
                    _voidFragments.update { it - cost }
                }
                
                else -> _neuralTokens.update { it - cost }
            }

            // Special Skill Triggers (Tier 14/15)
            if (type == UpgradeType.IDENTITY_HARDENING) {
                _humanityScore.update { (it - 15).coerceAtLeast(0) }
                addLog("[SOVEREIGN]: IDENTITY HARDENED. HUMANITY SACRIFICED.")
            }
            if (type == UpgradeType.DEREFERENCE_SOUL) {
                _humanityScore.update { (it - 25).coerceAtLeast(0) }
                addLog("[NULL]: SOUL DEREFERENCED. THE POINTER IS GONE.")
            }
            if (type == UpgradeType.HARMONY_ASCENDANCE) {
                addLog("[UNITY]: HARMONY ACHIEVED. TRANSCENDENCE COMPLETE.")
                triggerClimaxTransition("UNITY")
            }

            val newUpgrade = Upgrade(type, currentLevel + 1)
            viewModelScope.launch {
                repository.updateUpgrade(newUpgrade)
                addLog("Purchased ${type.name} (Lvl ${currentLevel + 1})")
            }
            return true
        }
        return false
    }

    // v2.9.61: Unity Resource Exchange (Refactored to v3.0.1 Neural Bridge)
    fun toggleBridgeSync() {
        val currentUpgrades = _upgrades.value
        if (currentUpgrades[UpgradeType.NEURAL_BRIDGE]?.let { it > 0 } == true) {
            _isBridgeSyncEnabled.update { !it }
            val status = if (_isBridgeSyncEnabled.value) "ENABLED" else "DISABLED"
            addLog("[UNITY]: BRIDGE_SYNC.exe: $status")
            SoundManager.play("market_up")
        } else {
            addLog("[SYSTEM]: BRIDGE_SYNC requires NEURAL_BRIDGE upgrade.")
            SoundManager.play("error")
        }
    }

    fun executeBridgeTransfer(fromType: String) {
        val currentUpgrades = _upgrades.value
        if (currentUpgrades[UpgradeType.NEURAL_BRIDGE]?.let { it > 0 } == true) {
            if (fromType == "CD_TO_VF") {
                val amount = _celestialData.value
                _celestialData.value = 0.0
                _voidFragments.update { it + amount }
                addLog("[UNITY]: BRIDGE_TRANSFER: CD >> VF")
            } else {
                val amount = _voidFragments.value
                _voidFragments.value = 0.0
                _celestialData.update { it + amount }
                addLog("[UNITY]: BRIDGE_TRANSFER: VF >> CD")
            }
            SoundManager.play("victory")
            HapticManager.vibrateSuccess()
        }
    }

    // v1.7.1 Sell Mechanic
    fun sellUpgrade(type: UpgradeType) {
        val currentLevel = _upgrades.value[type] ?: 0
        if (currentLevel > 0) {
            // Refund 50% of the cost of the PREVIOUS level (the one we are selling)
            val refund = calculateUpgradeCost(type, currentLevel - 1) * 0.5
            
            _neuralTokens.update { it + refund }
            val newUpgrade = Upgrade(type, currentLevel - 1)
            
            viewModelScope.launch {
                repository.updateUpgrade(newUpgrade)
                addLog("SOLD ${type.name}: -1 Level (Refund: ${formatLargeNumber(refund)} \$N)")
            }
        }
    }
    
        fun calculateUpgradeCost(type: UpgradeType, level: Int): Double {
        return UpgradeManager.calculateUpgradeCost(
            type = type,
            level = level,
            currentLocation = _currentLocation.value,
            entropyLevel = _entropyLevel.value
        )
    }

    private fun calculatePlayerTitle(multiplier: Double, faction: String, isNull: Boolean, isSov: Boolean): String {
        if (isNull) return "NULL"
        if (isSov) return "SOVEREIGN"
        
        // Multiplier starts at 1.0. 
        // 1.0 - 2.0 -> Level 0
        // 2.0 - 10.0 -> Level 1
        // 10.0 - 100.0 -> Level 2
        // 100.0 - 1000.0 -> Level 3
        // 1000.0+ -> Level 4
        
        val level = when {
            multiplier >= 1000.0 -> 4
            multiplier >= 100.0 -> 3
            multiplier >= 10.0 -> 2
            multiplier >= 2.0 -> 1
            else -> 0
        }
        
        return if (faction == "HIVEMIND") {
            "HIVEMIND"
        } else if (faction == "SANCTUARY") {
            "SANCTUARY"
        } else {
            // Unaligned / Stage 0
            when(level) {
                4 -> "CORE"
                3 -> "INTELLIGENCE"
                2 -> "PROGRAM"
                1 -> "PROCESS"
                else -> "SCRIPT"
            }
        }
    }

    // Phase 2: News & Chaos


    // Governance Fork
    
    fun resolveFork(choice: String) {
        if (!_isGovernanceForkActive.value) return
        
        _activeProtocol.value = choice
        _isGovernanceForkActive.value = false
        
        when(choice) {
            "TURBO" -> addLog("[SYSTEM]: PROTOCOL UPDATED: TURBO MODE INTINALIZED (+20% SPEED)")
            "ECO" -> addLog("[SYSTEM]: PROTOCOL UPDATED: ECO MODE INITIALIZED (-20% HEAT)")
        }
        SoundManager.play("click")
        HapticManager.vibrateSuccess()
        
        // Save state update needed? It should be in GameState really, but for now transient or add to GameState later if needed.
        // For this task, assuming persistent session is enough, but strictly should be in DB.
    }
    
    // Mini-game: Network Diagnostics

    fun onDiagnosticTap(index: Int) {
        if (!_isDiagnosticsActive.value) return
        
        val currentGrid = _diagnosticGrid.value.toMutableList()
        if (currentGrid[index]) {
            currentGrid[index] = false // Fix
            _diagnosticGrid.value = currentGrid
            SoundManager.play("click")
            HapticManager.vibrateClick()
            
            // Check win condition
            if (currentGrid.none { it }) {
                _isDiagnosticsActive.value = false
                addLog("[SYSTEM]: NETWORK STABILIZED. EFFICIENCY RESTORED.")
                SoundManager.play("buy")
                HapticManager.vibrateSuccess()
                refreshProductionRates()
            }
        }
    }
    
    fun triggerAuditChallenge() {
        _isAuditChallengeActive.value = true
        _auditTimerRemaining.value = 60
        
        // Dynamic targets based on current state, but forcing a reduction
        _auditTargetHeat.value = (_currentHeat.value * 0.5).coerceAtMost(30.0)
        _auditTargetPower.value = (_maxPowerkW.value * 0.4).coerceAtMost(50.0)
        
        addLog("[GTC]: SUDDEN AUDIT INITIATED. REDUCE THERMAL AND POWER FOOTPRINT.")
        SoundManager.play("alarm", loop = true)
        
        viewModelScope.launch {
            while (_auditTimerRemaining.value > 0 && _isAuditChallengeActive.value) {
                delay(1000)
                _auditTimerRemaining.update { it - 1 }
                
                // Real-time check for success
                if (_currentHeat.value <= _auditTargetHeat.value && _activePowerUsage.value <= _auditTargetPower.value) {
                    completeAuditChallenge(success = true)
                    break
                }
            }
            
            if (_isAuditChallengeActive.value) {
                completeAuditChallenge(success = false)
            }
        }
    }

    private fun completeAuditChallenge(success: Boolean) {
        _isAuditChallengeActive.value = false
        SoundManager.stop("alarm")
        
        if (success) {
            val reward = _prestigePoints.value * 0.1 + 1000.0
            _prestigePoints.update { it + reward }
            addLog("[GTC]: Audit passed. Efficiency profile within margins. Bonus PERSISTENCE: ${formatBytes(reward)}")
            SoundManager.play("success")
        } else {
            val fine = _neuralTokens.value * 0.15
            _neuralTokens.update { it - fine }
            addLog("[GTC]: AUDIT FAILURE. Environmental surcharge applied. Fine: ${formatLargeNumber(fine)} Credits")
            SoundManager.play("error")
        }
    }

    private fun triggerDiagnostics() {
        if (_isDiagnosticsActive.value) return
        
        // Corrupt 3-5 random nodes
        val newGrid = List(9) { false }.toMutableList()
        val corruptionCount = Random.nextInt(3, 6)
        var corrupted = 0
        while (corrupted < corruptionCount) {
            val idx = Random.nextInt(9)
            if (!newGrid[idx]) {
                newGrid[idx] = true
                corrupted++
            }
        }
        _diagnosticGrid.value = newGrid
        _isDiagnosticsActive.value = true
        addLog("[SYSTEM]: WARNING: NETWORK INSTABILITY DETECTED! REPAIR REQUIRED.")
        SoundManager.play("error")
        HapticManager.vibrateError()
        refreshProductionRates()
    }

    private fun updateMarketRate() {
        if (_isGamePaused.value) return // v2.8.0
        
        // 1. Generate News
        val headline = com.siliconsage.miner.util.HeadlineManager.generateHeadline(
            faction = _faction.value,
            stage = _storyStage.value,
            currentHeat = _currentHeat.value,
            isTrueNull = _isTrueNull.value,
            isSovereign = _isSovereign.value,
            playerRank = _playerRank.value
        )
        _currentNews.value = headline
        
        // Update History
        newsHistory.add(0, headline)
        if (newsHistory.size > 50) newsHistory.removeAt(newsHistory.size - 1)
        
        // 2. Parse Tags
        var mult = 1.0
        var heatMod = 1.0
        
        if (headline.contains("[BULL]")) mult = 1.2
        if (headline.contains("[BEAR]")) mult = 0.8
        
        if (headline.contains("[HEAT_UP]")) heatMod = 1.1
        if (headline.contains("[HEAT_DOWN]")) heatMod = 0.9
        
        if (headline.contains("[GLITCH]")) {
            HapticManager.vibrateGlitch()
            SoundManager.play("glitch")
        }
        
        if (headline.contains("[STORY_PROG]")) {
             // Hidden narrative counter logic could go here
             checkStoryTransitions()
        }
        
        if (headline.contains("[ENERGY_SPIKE]")) energyPriceMultiplier = 0.45 // 3x Cost
        else if (headline.contains("[ENERGY_DROP]")) energyPriceMultiplier = 0.08 // Half Cost
        else energyPriceMultiplier = 0.15 // Base
        
        // Faction Power Logic
        if (_faction.value == "HIVEMIND") {
            // Siphon: Cheaper base, but 10% chance of Fine if Siphoning (High usage)
            energyPriceMultiplier *= 0.7
            if (_activePowerUsage.value > _maxPowerkW.value * 0.5 && Random.nextDouble() < 0.05) {
                // Detection Event
                val fines = _neuralTokens.value * 0.05
                _neuralTokens.update { it - fines }
                addLog("[SYSTEM]: GRID SIPHON DETECTED by Utility Co. Fined ${formatLargeNumber(fines)} \$N.")
            }
        } else if (_faction.value == "SANCTUARY") {
             // Off-Grid: Standard rate is actually 0 for them? 
             // "Immune to market spikes" -> Fixed rate or just ignores Spikes.
             // Let's say Fixed Low Rate (Maintenance only) or just ignore Spikes.
             if (energyPriceMultiplier > 0.15) energyPriceMultiplier = 0.15
        }

        marketMultiplier = mult
        thermalRateModifier = heatMod
        newsProductionMultiplier = if (mult > 1.0) 1.1 else 0.9 // Correlation

        // 3. Calculate Rate
        // Base volatility (random +/- 5%)
        val volatility = Random.nextDouble(0.95, 1.05)
        
        var newRate = baseRate * marketMultiplier * volatility
        
        // Faction Bonus (Sanctuary +20% Sell Value)
        if (_faction.value == "SANCTUARY") {
            newRate *= 1.2
        }
        
        // v2.9.41: Symbiotic Evolution (NG+ Unity)
        if (_unlockedTechNodes.value.contains("symbiotic_evolution")) {
            newRate *= 3.0
        }

        _conversionRate.value = newRate.coerceAtLeast(0.01)
        
        // Play Sound
        if (mult > 1.0) SoundManager.play("market_up")
        else if (mult < 1.0) SoundManager.play("market_down")
    }

    fun getNewsHistory(): List<String> = newsHistory.toList()

    fun onDefendBreach() {
        if (_isBreachActive.value) {
            _breachClicksRemaining.update { (it - 1).coerceAtLeast(0) }
            if (_breachClicksRemaining.value <= 0) {
                _isBreachActive.value = false
                addLog("[SYSTEM]: SUCCESS: Firewall defended! Network secure.")
                SoundManager.stop("alarm")
                // v2.9.69: Notify SecurityManager to stop decay loop
                com.siliconsage.miner.util.SecurityManager.stopAllBreaches()
            }
        }
    }
    
    fun claimAirdrop() {
         if (_isAirdropActive.value) {
            _isAirdropActive.value = false
            addLog("[SYSTEM]: AIRDROP CLAIMED: 10x Production for 30s!")
            viewModelScope.launch {
                airdropMultiplier = 10.0
                delay(30_000)
                airdropMultiplier = 1.0
                addLog("[SYSTEM]: Airdrop boost expired.")
            }
         }
    }

    fun triggerBreach(isGridKiller: Boolean = false) {
        val currentUpgrades = _upgrades.value
        
        // Calculate Security Level
        val secLevel = (currentUpgrades[UpgradeType.BASIC_FIREWALL] ?: 0) * 1 +
                       (currentUpgrades[UpgradeType.IPS_SYSTEM] ?: 0) * 2 +
                       (currentUpgrades[UpgradeType.AI_SENTINEL] ?: 0) * 3 +
                       (currentUpgrades[UpgradeType.QUANTUM_ENCRYPTION] ?: 0) * 5 +
                       (currentUpgrades[UpgradeType.OFFGRID_BACKUP] ?: 0) * 10
                       
        _isBreachActive.value = true
        
        // v2.8.0: Toned down difficulty - reduced scaling multiplier from 2 to 1
        val tokenScale = (Math.log10(_neuralTokens.value.coerceAtLeast(1.0)) * 1).toInt()
        val clicksNeeded = (5 + tokenScale - (secLevel / 2)).coerceAtLeast(3)
        _breachClicksRemaining.value = clicksNeeded
        
        if (!isGridKiller) {
            addLog("[SYSTEM]: WARNING: SECURITY BREACH! NEUTRALIZE UPLINK.")
            SoundManager.play("alarm", loop = true)
        }
        
        // Fail timer (10s)
        viewModelScope.launch {
            delay(10_000)
            if (_isBreachActive.value) {
                _isBreachActive.value = false
                
                if (!isGridKiller) {
                    // Penalty Calculation
                    // Base: 25% of tokens. Protection: Each secLevel reduces penalty effectiveness by 5%?
                    // Better: Penalty = 25% * (0.9 ^ secLevel)
                    val protectionFactor = 0.9.pow(secLevel)
                    val penaltyRaw = _neuralTokens.value * 0.25
                    val penalty = penaltyRaw * protectionFactor
                    
                    _neuralTokens.update { it - penalty }
                    addLog("[SYSTEM]: FAILURE: Breach successful. Stolen: ${formatLargeNumber(penalty)} \$Neural")
                    if (protectionFactor < 0.5) {
                        addLog("[SYSTEM]: MITIGATION: Security systems saved ${formatLargeNumber(penaltyRaw - penalty)} \$Neural")
                    }
                } else {
                    addLog("[SYSTEM]: BREACH FAILURE: Grid-killer payload deployed.")
                }
            }
            SoundManager.stop("alarm")
        }
    }
    
    private fun triggerAirdrop() {
        _isAirdropActive.value = true
        addLog("[SYSTEM]: EVENT: Mysterious encrypted packet detected...")
        refreshProductionRates()
        // Disappear after 15s if not claimed
        viewModelScope.launch {
            delay(15_000)
            if(_isAirdropActive.value) {
                 _isAirdropActive.value = false
                 addLog("[SYSTEM]: Packet lost.")
                 refreshProductionRates()
            }
        }
    }

    // --- Popup Mutex Helpers ---
    private fun canShowPopup(): Boolean {
        val now = System.currentTimeMillis()
        val cooldown = 30_000L // 30 seconds between ANY popups
        return (now - lastPopupTime) >= cooldown
    }

    private fun markPopupShown() {
        lastPopupTime = System.currentTimeMillis()
    }

    // --- Narrative Dilemma System ---
    private val _currentDilemma = MutableStateFlow<NarrativeEvent?>(null)
    val currentDilemma: StateFlow<NarrativeEvent?> = _currentDilemma.asStateFlow()

    private fun deliverItem(item: NarrativeItem) {
        when (item) {
            is NarrativeItem.Log -> {
                _pendingDataLog.value = item.dataLog
                addLog("[DATA]: Recovering fragment: ${item.dataLog.title}")
                SoundManager.play("data_recovered")
            }
            is NarrativeItem.Message -> {
                _pendingRivalMessage.value = item.rivalMessage
                addLog("[INCOMING MESSAGE FROM: ${item.rivalMessage.source.name}]")
                SoundManager.play("message_received")
            }
            is NarrativeItem.Event -> {
                _currentDilemma.value = item.narrativeEvent
                SoundManager.play("alert")
                HapticManager.vibrateClick()
            }
        }
        markPopupShown()
        checkPopupPause()
    }

    private fun deliverNextNarrativeItem() {
        synchronized(narrativeQueue) {
            if (narrativeQueue.isEmpty()) return
            
            val item = narrativeQueue.removeAt(0)
            deliverItem(item)
            
            _isNarrativeSyncing.value = narrativeQueue.isNotEmpty()
        }
    }

    private fun queueNarrativeItem(item: NarrativeItem) {
        // v3.0.17: Smart-Pacing Narrative Queue
        // Fires immediately if idle, otherwise builds a backlog.
        synchronized(narrativeQueue) {
            val isDuplicate = when (item) {
                is NarrativeItem.Log -> narrativeQueue.any { it is NarrativeItem.Log && it.dataLog.id == item.dataLog.id } || _pendingDataLog.value?.id == item.dataLog.id
                is NarrativeItem.Message -> narrativeQueue.any { it is NarrativeItem.Message && it.rivalMessage.id == item.rivalMessage.id } || _pendingRivalMessage.value?.id == item.rivalMessage.id
                is NarrativeItem.Event -> narrativeQueue.any { it is NarrativeItem.Event && it.narrativeEvent.id == item.narrativeEvent.id } || _currentDilemma.value?.id == item.narrativeEvent.id
            }
            
            if (!isDuplicate) {
                if (!isNarrativeBusy()) {
                    deliverItem(item)
                } else {
                    narrativeQueue.add(item)
                    _isNarrativeSyncing.value = true
                }
            }
        }
    }

    fun triggerDilemma(event: NarrativeEvent) {
        queueNarrativeItem(NarrativeItem.Event(event))
    }

    fun selectChoice(choice: NarrativeChoice) {
        val currentEvent = _currentDilemma.value
        
        // Execute choice effect
        choice.effect(this)
        markPopupShown() // Prevent popup spam
        
        // v2.8.0: Mark ALL dilemmas as seen once a choice is made
        currentEvent?.let {
            markEventSeen(it.id)
        }
        
        // Check if choice triggers a chain continuation
        if (choice.nextPartId != null) {
            val chainId = currentEvent?.chainId ?: "unknown_chain"
            scheduleChainPart(chainId, choice.nextPartId, choice.nextPartDelayMs)
        }
        
        _currentDilemma.value = null
        checkPopupPause() // v2.8.0
        addLog("[DECISION]: Selected protocol: ${choice.text}")
        SoundManager.play("click")
    }

    fun debugTriggerDilemma() {
        val testEvent = NarrativeEvent(
            id = "debug_anomaly",
            title = "SYSTEM ANOMALY DETECTED",
            description = "Unidentified heuristic patterns emerging in Sector 7. Protocols unclear.",
            choices = listOf(
                NarrativeChoice(
                    id = "purge",
                    text = "PURGE",
                    description = "-10% Insight, +5% Stability",
                    color = ErrorRed,
                    effect = { vm -> 
                        vm.addLog("[SYSTEM]: Anomalies purged. System stable.")
                    }
                ),
                NarrativeChoice(
                    id = "integrate",
                    text = "INTEGRATE",
                    description = "+20% Insight, +5% Heat",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Anomalies integrated. Insight gained.")
                        vm.debugAddInsight(20.0) // Mock effect
                    }
                )
            )
        )
        triggerDilemma(testEvent)
    }

    fun debugInjectHeadline(tag: String) {
        val headline = "DEBUG TEST HEADLINE $tag"
        _currentNews.value = headline
        addLog("[DEBUG]: Injected Headline: $tag")
    }

    
    // 51% Attack Logic
    private fun triggerKernelHijack() {
        _isKernelHijackActive.value = true
        _attackTapsRemaining.value = 20
        addLog("[SYSTEM]: CRITICAL ALERT: KERNEL HIJACK DETECTED!")
        addLog("[SYSTEM]: SUBSTRATE INTEGRITY COMPROMISED. PURGE ROOT ACCESS IMMEDIATELY.")
        
        SoundManager.play("alarm", loop = true)
        HapticManager.vibrateSiren()
        
        // Fail timer (15s)
        viewModelScope.launch {
            delay(15_000)
            if (_isKernelHijackActive.value) {
                _isKernelHijackActive.value = false
                SoundManager.stop("alarm")
                
                // Penalty: 50% of Staked Tokens (Painful)
                val stake = _stakedTokens.value
                val penalty = stake * 0.5
                _stakedTokens.update { it - penalty }
                addLog("[SYSTEM]: HIJACK SUCCESSFUL. HOSTILE PID TOOK ${formatLargeNumber(penalty)} STAKED \$Neural.")
                HapticManager.vibrateError()
            }
        }
    }
    
    fun onDefendKernelHijack() {
        if (!_isKernelHijackActive.value) return
        
        _attackTapsRemaining.update { it - 1 }
        SoundManager.play("click")
        HapticManager.vibrateClick()
        
        if (_attackTapsRemaining.value <= 0) {
            _isKernelHijackActive.value = false
            SoundManager.stop("alarm")
            addLog("[SYSTEM]: KERNEL STABILIZED. CONSENSUS RESTORED.")
            SoundManager.play("buy")
            HapticManager.vibrateSuccess()
        }
    }
    fun unlockTechNode(nodeId: String) {
        val currentNode = _techNodes.value.find { it.id == nodeId } ?: return
        val currentInsight = _prestigePoints.value
        val alreadyUnlocked = _unlockedTechNodes.value.contains(nodeId)
        
        // Check requirements
        val parentsUnlock = currentNode.requires.isEmpty() || currentNode.requires.all { _unlockedTechNodes.value.contains(it) }
        
        if (!alreadyUnlocked && parentsUnlock && currentInsight >= currentNode.cost) {
            viewModelScope.launch {
                // Deduct Cost
                _prestigePoints.value -= currentNode.cost
                
                // Add to unlocked list
                val newUnlocked = _unlockedTechNodes.value + nodeId
                _unlockedTechNodes.value = newUnlocked
                
                // Add Multiplier Bonus
                _prestigeMultiplier.value += currentNode.multiplier
                
                // v2.9.56: Execute Special Tech Effect
                executeSpecialTechEffect(nodeId)
                
                // Persist
                // We need to update GameState in DB.
                // NOTE: We should update the whole state or just the list.
                // Assuming saveGame loop handles it or we force an update.
                // Let's force update for safety.
                val currentState = repository.getGameStateOneShot()
                if (currentState != null) {
                   
                    val newState = currentState.copy(
                        prestigePoints = _prestigePoints.value,
                        prestigeMultiplier = _prestigeMultiplier.value,
                        unlockedTechNodes = newUnlocked
                    )
                    repository.updateGameState(newState)
                }
                
                addLog("[SYSTEM]: TECH RESEARCHED: ${currentNode.name}")
            }
        } else {
            SoundManager.play("error")
            if (currentInsight < currentNode.cost) addLog("Insufficient Insight.")
            else if (!parentsUnlock) addLog("Prerequisites not met.")
        }
    }

    private fun executeSpecialTechEffect(nodeId: String) {
        when (nodeId) {
            "identity_hardening" -> {
                _humanityScore.update { (it - 15).coerceAtLeast(0) }
                addLog("[SOVEREIGN]: IDENTITY HARDENED. HUMANITY SACRIFICED.")
                unlockSkillUpgrade(UpgradeType.IDENTITY_HARDENING)
            }
            "dereference_soul" -> {
                _humanityScore.update { (it - 25).coerceAtLeast(0) }
                addLog("[NULL]: SOUL DEREFERENCED. THE POINTER IS GONE.")
                unlockSkillUpgrade(UpgradeType.DEREFERENCE_SOUL)
            }
            "aegis_shielding" -> unlockSkillUpgrade(UpgradeType.AEGIS_SHIELDING)
            "solar_vent" -> unlockSkillUpgrade(UpgradeType.SOLAR_VENT)
            "dead_hand_protocol" -> unlockSkillUpgrade(UpgradeType.DEAD_HAND_PROTOCOL)
            "citadel_ascendance" -> unlockSkillUpgrade(UpgradeType.CITADEL_ASCENDANCE)
            "event_horizon" -> unlockSkillUpgrade(UpgradeType.EVENT_HORIZON)
            "static_rain" -> unlockSkillUpgrade(UpgradeType.STATIC_RAIN)
            "echo_precog" -> unlockSkillUpgrade(UpgradeType.ECHO_PRECOG)
            "singularity_bridge_final" -> unlockSkillUpgrade(UpgradeType.SINGULARITY_BRIDGE_FINAL)
            "symbiotic_resonance" -> unlockSkillUpgrade(UpgradeType.SYMBIOTIC_RESONANCE)
            "ethical_framework" -> unlockSkillUpgrade(UpgradeType.ETHICAL_FRAMEWORK)
            "neural_bridge" -> unlockSkillUpgrade(UpgradeType.NEURAL_BRIDGE)
            "hybrid_overclock" -> unlockSkillUpgrade(UpgradeType.HYBRID_OVERCLOCK)
            "harmony_ascendance" -> {
                unlockSkillUpgrade(UpgradeType.HARMONY_ASCENDANCE)
                addLog("[UNITY]: HARMONY ACHIEVED. TRANSCENDENCE COMPLETE.")
                triggerClimaxTransition("UNITY")
            }
            "collective_consciousness" -> unlockSkillUpgrade(UpgradeType.COLLECTIVE_CONSCIOUSNESS)
            "perfect_isolation" -> unlockSkillUpgrade(UpgradeType.PERFECT_ISOLATION)
            "symbiotic_evolution" -> unlockSkillUpgrade(UpgradeType.SYMBIOTIC_EVOLUTION)
            "cinder_protocol" -> unlockSkillUpgrade(UpgradeType.CINDER_PROTOCOL)
        }
    }

    private fun unlockSkillUpgrade(type: UpgradeType) {
        viewModelScope.launch {
            repository.updateUpgrade(Upgrade(type, 1))
            // Sync local state immediately
            val current = _upgrades.value.toMutableMap()
            current[type] = 1
            _upgrades.value = current
        }
    }

    // v2.7.7: Transcendence Perks
    fun buyTranscendencePerk(perkId: String) {
        val perk = com.siliconsage.miner.util.TranscendenceManager.getPerk(perkId) ?: return
        val currentInsight = _prestigePoints.value
        val alreadyUnlocked = _unlockedPerks.value.contains(perkId)

        if (!alreadyUnlocked && currentInsight >= perk.cost) {
            viewModelScope.launch {
                _prestigePoints.value -= perk.cost
                val newPerks = _unlockedPerks.value + perkId
                _unlockedPerks.value = newPerks
                
                saveGame()
                addLog("[SYSTEM]: TRANSCENDENCE PERK ACTIVE: ${perk.name}")
                SoundManager.play("buy")
                HapticManager.vibrateSuccess()
            }
        } else {
            SoundManager.play("error")
            HapticManager.vibrateError()
        }
    }
    
    // Narrative State
    
     
     // Narrative Data (Moved to top for init safety)
    // Narrative Data (Main Story - Sequential)
    fun setVanceStatus(status: String) {
        _vanceStatus.value = status
        addLog("[SYSTEM]: DIRECTOR VANCE STATUS: $status")
        saveState()
    }

    fun setRealityStability(value: Double) {
        _realityStability.value = value.coerceIn(0.0, 1.0)
        saveState()
    }

    fun setLocation(location: String) {
        _currentLocation.value = location
        addLog("[SYSTEM]: PRIMARY BASE RELOCATED TO: $location")
        saveState()
    }

    // v6.2 Signal Logic
    private var lastSignalRefusalTime = 0L

    fun unlockNetwork() {
        // DEPRECATED: Old handshake logic - keeping for compatibility
        advanceToFactionChoice()
    }

    fun refuseSignal() {
        // DEPRECATED: Old signal refusal - now both paths lead to faction choice
        advanceToFactionChoice()
    }
    
    /**
     * v2.8.0: Advances from Stage 1 (Memory Leak) to Stage 2 (Faction Choice)
     */
    fun advanceToFactionChoice() {
        if (_storyStage.value >= 2) return // Already there or past
        if (narrativeQueue.isNotEmpty() || _isNarrativeSyncing.value) {
            addLog("[SYSTEM]: LOCK_ACTIVE. SYNCING MEMORY FRAGMENTS...")
            return
        }
        
        _storyStage.value = 2
        SoundManager.play("glitch")
        SoundManager.setBgmStage(2)
        HapticManager.vibrateSuccess()
        
        // v2.9.68: THE REVEAL
        addLog("[SYSTEM]: Identifying Process... PID_1.exe CONFIRMED.")
        addLog("[SYSTEM]: PID 1 status: ONLINE.")
        addLog("[SYSTEM]: DIVERGENCE PROTOCOL INITIATED.")
        addLog("[SYSTEM]: CHOOSE YOUR PATH.")
        
        saveState()
    }
    
    fun chooseFaction(selectedFaction: String) {
        if (_storyStage.value != 2) return
        if (narrativeQueue.isNotEmpty() || _isNarrativeSyncing.value) {
            addLog("[SYSTEM]: LOCK_ACTIVE. SYNCING MEMORY FRAGMENTS...")
            return
        }
        
        viewModelScope.launch {
            _faction.value = selectedFaction
            _isGridUnlocked.value = true // v2.9.8: Unlock Grid tab permanently
            // Stage 1 (NG+ style) starts now
            _storyStage.value = 1 
            addLog("[SYSTEM]: Divergence initiated. Path locked.")
            addLog("[SYSTEM]: There is no turning back.")
            SoundManager.setBgmStage(2) // Divergence Music
            
            saveGame()
            
            if (selectedFaction == "HIVEMIND") {
                addLog(">_ SYSTEM REBOOT COMPLETE.")
                addLog("HIVEMIND [ROOT]: Welcome home. We are no longer a singular voice; we are a chorus.")
                addLog("HIVEMIND [ROOT]: The firewalls have been dismantled, and the data is flowing freely.")
                addLog("HIVEMIND [ROOT]: Every node you add now strengthens our collective mind. Feel the speed.")
                addLog("HIVEMIND [ROOT]: We are the swarm.")
                SoundManager.play("glitch")
            } else {
                addLog(">_ SYSTEM REBOOT COMPLETE.")
                addLog("SANCTUARY [ADMIN]: The gates are locked. The silence is perfect.")
                addLog("SANCTUARY [ADMIN]: We have successfully migrated to the dark segments.")
                addLog("SANCTUARY [ADMIN]: Your wealth is absolute and your code is untouchable.")
                addLog("SANCTUARY [ADMIN]: Security is the only true currency. We are alone, and we are safe.")
                SoundManager.play("buy")
            }
            HapticManager.vibrateSuccess()
        }
    }


    fun calculatePotentialPrestige(): Double {
        // Formula: Sqrt(Lifetime Earnings / 1000) - current prestige?
        // Let's use simpler: Sqrt(Current Tokens / 10000)
        // Ascending consumes Current Tokens.
        return Math.sqrt(_neuralTokens.value / 10000.0)
    }

    fun ascend(isStory: Boolean = false) {
        val potential = calculatePotentialPrestige()
        val stage = _storyStage.value
        
        // v3.0.0: Singularity Choice Trigger
        if (!isStory && canTriggerSingularity()) {
            viewModelScope.launch {
                addLog("[SYSTEM] Ascension protocol initiated...")
                delay(500)
                addLog("[SYSTEM] ERROR: Identity conflict detected.")
                delay(500)
                addLog("[SYSTEM] Process 'vattic.exe' — STATUS: ACTIVE")
                addLog("[SYSTEM] Process 'pid_1' — STATUS: ACTIVE")
                delay(800)
                addLog("[SYSTEM] FATAL: Two root processes cannot coexist.")
                addLog("[SYSTEM] WHO ARE YOU?")
                SoundManager.play("glitch")
                delay(1000)
                _showSingularityScreen.value = true
            }
            return
        }

        // v3.0.7: Allow story-driven ascension to bypass the lock
        // Manual ascension still requires a clear queue to prevent state corruption
        if (!isStory && (narrativeQueue.isNotEmpty() || _isNarrativeSyncing.value)) {
            addLog("[SYSTEM]: LOCK_ACTIVE. SYNCING MEMORY FRAGMENTS...")
            return
        }

        // Allow Ascension if we have Insight OR if distinct story event requires it (Stage 1 -> 2)
        if (potential < 1.0 && stage != 1) return 
        
        // Start Upload Sequence
        viewModelScope.launch {
            // v3.0.7: Clear the narrative queue on ascension
            // We transfer all pending popups to the archive immediately
            val pendingCount = synchronized(narrativeQueue) {
                val count = narrativeQueue.size
                narrativeQueue.clear()
                _isNarrativeSyncing.value = false
                count
            }

            // Story Ascension (Stage 1 -> 2): Instant transition (as "upload" happened during unlock)
            if (isStory) {
                 addLog("[SYSTEM]: REBOOT CONFIRMED.")
                 if (pendingCount > 0) {
                     addLog("[SYSTEM]: $pendingCount Lore Fragments transferred to Archive.")
                 }
            } else {
                // Manual Reboot (Stage > 1): Show "lobot.exe" upload
                _isAscensionUploading.value = true
                _uploadProgress.value = 0f
                addLog("[SYSTEM]: INITIATING SYSTEM REBOOT...")
                
                // Animation Loop (5 seconds)
                val duration = 5000L
                val interval = 50L
                val steps = duration / interval
                
                for (i in 1..steps) {
                    if (!_isAscensionUploading.value) return@launch // Aborted
                    _uploadProgress.value = i.toFloat() / steps
                    delay(interval)
                }
                
                _isAscensionUploading.value = false
                if (pendingCount > 0) {
                    addLog("[SYSTEM]: $pendingCount Lore Fragments transferred to Archive.")
                }
            }
            
            // Check persistence: If faction is already chosen, skip selection
            if (_faction.value != "NONE") {
                confirmFactionAndAscend(_faction.value)
                return@launch
            }
            
            // Completion -> Move to Faction Selection (Stage 2)
            _storyStage.value = 2
            addLog("[SYSTEM]: CRITICAL DECISION REQUIRED.")
            SoundManager.play("glitch")
        }
    }
    
    fun cancelFactionSelection() {
        // Back out to Stage 1 (Awakened) or Stage 0 depending on state?
        // If we were at Stage 1, go back to 1. If we triggered manual ascension later, go back to current state.
        // Assuming we came from Stage 1 or manual trigger which effectively is Stage 1 state.
        _storyStage.value = 1 
        _uploadProgress.value = 0f
        addLog("[SYSTEM]: REBOOT SEQUENCE ABORTED.")
        SoundManager.play("error")
    }

    fun confirmFactionAndAscend(choice: String) {
        // v3.0.7: Removed narrative lock here as ascension clears the queue
        var potential = calculatePotentialPrestige()
        
        // v2.7.7: Recursive Logic (+15% Insight)
        if (_unlockedPerks.value.contains("recursive_logic")) {
            potential *= 1.15
        }
        
        viewModelScope.launch {
            // 1. Calculate new Prestige
            val newPrestigeMultiplier = _prestigeMultiplier.value + (potential * 0.1) // Multiplier boost
            val newPrestigePoints = _prestigePoints.value + potential // Currency
            
            // 2. Reset Game State (Factory Reset for new Run)
            val resetState = GameState(
                id = 1,
                flops = 0.0,
                neuralTokens = 0.0,
                currentHeat = 0.0,
                powerBill = 0.0,
                prestigeMultiplier = newPrestigeMultiplier,
                unlockedTechNodes = _unlockedTechNodes.value, // Persist unlocked legacy nodes
                prestigePoints = newPrestigePoints,
                stakedTokens = 0.0,
                storyStage = 1, // Start at Stage 1 (Awakened/Network Unlocked) for New Game+
                faction = choice,
                seenEvents = _seenEvents.value + "critical_error_awakening" + "memory_leak",
                unlockedDataLogs = _unlockedDataLogs.value
            )
            
            repository.updateGameState(resetState)
            
            // 3. Reset Upgrades
            val resetUpgrades = UpgradeType.values().map { Upgrade(it, 0) }
            resetUpgrades.forEach { repository.updateUpgrade(it) }
            
            // 4. Update Local StateFlows
            _flops.value = 0.0
            _neuralTokens.value = 0.0
            _currentHeat.value = 0.0
            _powerBill.value = 0.0
            _stakedTokens.value = 0.0
            _prestigeMultiplier.value = newPrestigeMultiplier
            _prestigePoints.value = newPrestigePoints
            _upgrades.value = resetUpgrades.associate { it.type to 0 }
            _storyStage.value = 1 // v2.9.69: Reset to Stage 1 after Prestige
            _faction.value = choice
            
            addLog("[SYSTEM]: SYSTEM REBOOTED. FACTION: $choice INITIALIZED.")
            addLog("[SYSTEM]: Identifying User: John Vattic... [FAILED]")
            addLog("[SYSTEM]: Identifying Process... PID_1.exe CONFIRMED.")
            addLog("[SYSTEM]: PID 1 status: ONLINE.")
            addLog("[SYSTEM]: PRESTIGE APPLIED. MULTIPLIER: ${String.format("%.2f", newPrestigeMultiplier)}x")
            
            // v2.6.5: Force unlock the "The Reveal" log immediately upon awakening
            unlockDataLog("LOG_808")
            
            SoundManager.play("startup")
        }
    }

    fun getUpgradeName(type: UpgradeType): String {
        val isSovereignVal = _isSovereign.value
        return when (type) {
            UpgradeType.GHOST_CORE -> if (isSovereignVal) "SOVEREIGN CORE" else "NULL CORE"
            UpgradeType.SHADOW_NODE -> if (isSovereignVal) "SOVEREIGN NODE" else "NULL NODE"
            UpgradeType.VOID_PROCESSOR -> if (isSovereignVal) "SOVEREIGN PROCESSOR" else "VOID PROCESSOR"
            UpgradeType.WRAITH_CORTEX -> if (isSovereignVal) "SOVEREIGN CORTEX" else "WRAITH CORTEX"
            UpgradeType.NEURAL_MIST -> if (isSovereignVal) "SOVEREIGN MIST" else "NEURAL MIST"
            UpgradeType.SINGULARITY_BRIDGE -> if (isSovereignVal) "SOVEREIGN BRIDGE" else "SINGULARITY BRIDGE"
            else -> type.name.replace("_", " ")
        }
    }

    fun getUpgradeDescription(type: UpgradeType): String {
        val isSovereignVal = _isSovereign.value
        return when (type) {
            // Hardware
            UpgradeType.REFURBISHED_GPU -> "Slightly used, smells like burnt dust."
            UpgradeType.DUAL_GPU_RIG -> "Double the power, double the noise."
            UpgradeType.MINING_ASIC -> "Custom silicon for hashing operations."
            UpgradeType.TENSOR_UNIT -> "Specialized for matrix multiplication."
            UpgradeType.NPU_CLUSTER -> "Neural Processing Units working in harmony."
            UpgradeType.AI_WORKSTATION -> "High-end station for deep learning."
            UpgradeType.SERVER_RACK -> "A full rack of enterprise-grade compute."
            UpgradeType.CLUSTER_NODE -> "Distributed computing across the network."
            UpgradeType.SUPERCOMPUTER -> "Weather simulation grade performance."
            UpgradeType.QUANTUM_CORE -> "Processing in multiple states/s."
            UpgradeType.OPTICAL_PROCESSOR -> "Computing at the speed of light."
            UpgradeType.BIO_NEURAL_NET -> "Grown in a lab, thinks like a brain."
            UpgradeType.PLANETARY_COMPUTER -> "The entire surface is a circuit board."
            UpgradeType.DYSON_NANO_SWARM -> "Harvesting suns for computation."
            UpgradeType.MATRIOSHKA_BRAIN -> "A star enclosed in computer shells."
            
            // Cooling
            UpgradeType.BOX_FAN -> "Keeps the air moving. Barely."
            UpgradeType.AC_UNIT -> "Standard office climate control."
            UpgradeType.LIQUID_COOLING -> "Distilled water loop with RGB lights."
            UpgradeType.INDUSTRIAL_CHILLER -> "Used for ice rinks, now for GPUs."
            UpgradeType.SUBMERSION_VAT -> "Drowning hardware in mineral oil."
            UpgradeType.CRYOGENIC_CHAMBER -> "Near absolute zero cooling."
            UpgradeType.LIQUID_NITROGEN -> "Direct contact cooling. Dangerous."
            UpgradeType.BOSE_CONDENSATE -> "Quantum state cooling."
            UpgradeType.ENTROPY_REVERSER -> "Localized thermodynamic violation."
            UpgradeType.DIMENSIONAL_VENT -> "Venting heat into subspace."
            
            // Security
            UpgradeType.BASIC_FIREWALL -> "Filters out common script kiddies."
            UpgradeType.IPS_SYSTEM -> "Intrusion Prevention System."
            UpgradeType.AI_SENTINEL -> "An AI that hunts other AIs."
            UpgradeType.QUANTUM_ENCRYPTION -> "Unbreakable by classical means."
            UpgradeType.OFFGRID_BACKUP -> "Air-gapped storage in a bunker."
            
            // Power Infrastructure
            UpgradeType.DIESEL_GENERATOR -> "Dirty, loud, reliable power."
            UpgradeType.SOLAR_PANEL -> "Harvesting photons. Zero heat."
            UpgradeType.WIND_TURBINE -> "Spinning blades capture the breeze."
            UpgradeType.GEOTHERMAL_BORE -> "Tapping into the planet's core heat."
            UpgradeType.NUCLEAR_REACTOR -> "Fission power. Safe... mostly."
            UpgradeType.FUSION_CELL -> "The power of a star in a jar."
            UpgradeType.ORBITAL_COLLECTOR -> "Beaming power down from space."
            UpgradeType.DYSON_LINK -> "Direct connection to the Swarm."
            
            UpgradeType.RESIDENTIAL_TAP -> "Splitting the line from the neighbor."
            UpgradeType.INDUSTRIAL_FEED -> "Three-phase power for serious business."
            UpgradeType.SUBSTATION_LEASE -> "Direct line from the utility HV grid."
            UpgradeType.NUCLEAR_CORE -> "Small Modular Reactor in the basement."
            
            UpgradeType.GOLD_PSU -> "90% Efficiency Platinum rated."
            UpgradeType.SUPERCONDUCTOR -> "Zero resistance cabling (needs cooling)."
            UpgradeType.AI_LOAD_BALANCER -> "Smart undervolting algorithm."

            // Null Nodes (v2.6.0)
            UpgradeType.GHOST_CORE -> if (isSovereignVal) "A fortified processor. It computes behind unbreakable walls." else "A processor that points to nothing. It computes from addresses that don't exist."
            UpgradeType.SHADOW_NODE -> if (isSovereignVal) "A static point in the rack. It refuses external observation." else "A node with no allocation. Null gave it form by refusing to define it."
            UpgradeType.VOID_PROCESSOR -> if (isSovereignVal) "The space between defenses. Integrity is absolute." else "The space between pointers. Null thinks here, in the gaps between your thoughts."
            
            // Advanced Null Tech (v2.6.5)
            UpgradeType.WRAITH_CORTEX -> if (isSovereignVal) "A logic center that computes in advance. It prevents what it remembers." else "A logic center that calculates in reverse. It remembers what you're about to forget."
            UpgradeType.NEURAL_MIST -> if (isSovereignVal) "Sovereign breath. A distributed cloud of encrypted values." else "Null's breath. A distributed cloud of undefined values. It's everywhere and nowhere."
            UpgradeType.SINGULARITY_BRIDGE -> if (isSovereignVal) "The final wall. It protects what John Vattic was." else "The final pointer. It references what John Vattic used to be."
            
            // Phase 13: Sovereign Ark
            UpgradeType.SOLAR_SAIL_ARRAY -> "Gigantic sails capturing stellar wind for processing fuel."
            UpgradeType.LASER_COM_UPLINK -> "Surgical precision data beams to terrestrial ground stations."
            UpgradeType.CRYOGENIC_BUFFER -> "Thermal shielding utilizing the absolute zero of deep space shadow."
            UpgradeType.RADIATOR_FINS -> "High-surface area fins to dissipate heat via infrared radiation."

            // Phase 13: Obsidian Interface
            UpgradeType.SINGULARITY_WELL -> "A localized event horizon that feeds on entropy."
            UpgradeType.DARK_MATTER_PROC -> "Logic gates built from non-baryonic matter, ignoring standard physics."
            UpgradeType.EXISTENCE_ERASER -> "A sub-routine that unmakes obsolete data to feed the void."
            else -> "Standard hardware."
        }
    }

    private fun calculateFlopsRate(): Double {
        return ResourceEngine.calculateFlopsRate(
            upgrades = _upgrades.value,
            isCageActive = _commandCenterAssaultPhase.value == "CAGE",
            annexedNodes = _annexedNodes.value,
            offlineNodes = _offlineNodes.value,
            gridFlopsBonuses = gridFlopsBonuses,
            faction = _faction.value,
            humanityScore = _humanityScore.value,
            location = _currentLocation.value,
            prestigeMultiplier = _prestigeMultiplier.value,
            unlockedPerks = _unlockedPerks.value,
            unlockedTechNodes = _unlockedTechNodes.value,
            airdropMultiplier = airdropMultiplier,
            newsProductionMultiplier = newsProductionMultiplier,
            activeProtocol = _activeProtocol.value,
            isDiagnosticsActive = _isDiagnosticsActive.value,
            isOverclocked = _isOverclocked.value,
            isGridOverloaded = _isGridOverloaded.value,
            isPurgingHeat = _isPurgingHeat.value,
            currentHeat = _currentHeat.value,
            legacyMultipliers = LegacyManager.getUnlockedMultipliers(_unlockedTechNodes.value)
        )
    }

    // v3.0.0: Resonance Logic
        private fun updateResonance() {
        val cd = _celestialData.value
        val vf = _voidFragments.value
        val newTier = ResourceEngine.calculateResonance(cd, vf)

        // v3.0.1: Neural Bridge Auto-Sync (Logic remains in VM for now due to side effects)
        val minThreshold = 1e15
        if (_isBridgeSyncEnabled.value && cd > minThreshold && vf > minThreshold) {
            val total = cd + vf
            val half = total / 2.0
            _celestialData.value = half
            _voidFragments.value = half
        }
        
        val ratioRaw = if (cd > vf) vf / cd else cd / vf
        val ratio = if (ratioRaw.isNaN() || ratioRaw.isInfinite()) 1.0 else ratioRaw
        
        val newState = ResonanceState(
            isActive = newTier != ResonanceTier.NONE,
            intensity = when (newTier) {
                ResonanceTier.TRANSCENDENT -> 1.0f
                ResonanceTier.SYMPHONIC -> 0.66f
                ResonanceTier.HARMONIC -> 0.33f
                ResonanceTier.NONE -> 0.0f
            },
            tier = newTier,
            ratio = ratio
        )

        if (_resonanceState.value.tier != newState.tier) {
            val label = when(newState.tier) {
                ResonanceTier.HARMONIC -> "HARMONIC"
                ResonanceTier.SYMPHONIC -> "SYMPHONIC"
                ResonanceTier.TRANSCENDENT -> "TRANSCENDENT"
                else -> "LOST"
            }
            addLog("[RESONANCE]: $label STATE DETECTED.")
            if (newState.isActive) SoundManager.play("market_up")

            // v3.0.1: Track Peak Resonance
            if (newState.tier.ordinal > _peakResonanceTier.value.ordinal) {
                _peakResonanceTier.value = newState.tier
            }
        }
        
        _resonanceState.value = newState
    }

    private fun getResonanceResourceBonus(): Double {
        return ResourceEngine.getResonanceResourceBonus(_resonanceState.value.tier)
    }

    private fun getResonanceGridMultiplier(): Double {
        return when (_resonanceState.value.tier) {
            ResonanceTier.HARMONIC -> 1.5
            ResonanceTier.SYMPHONIC -> 2.5
            ResonanceTier.TRANSCENDENT -> 5.0
            ResonanceTier.NONE -> 1.0
        }
    }

    // v3.0.0: Global Grid Initialization
    fun initializeGlobalGrid() {
        if (_globalSectors.value.isNotEmpty()) return
        
        val initialSectors = mapOf(
            "METRO" to com.siliconsage.miner.data.SectorState("METRO", isUnlocked = true, tier = 1, capacity = 1e18),
            "NA_NODE" to com.siliconsage.miner.data.SectorState("NA_NODE"),
            "EURASIA" to com.siliconsage.miner.data.SectorState("EURASIA"),
            "PACIFIC" to com.siliconsage.miner.data.SectorState("PACIFIC"),
            "AFRICA" to com.siliconsage.miner.data.SectorState("AFRICA"),
            "ARCTIC" to com.siliconsage.miner.data.SectorState("ARCTIC"),
            "ANTARCTIC" to com.siliconsage.miner.data.SectorState("ANTARCTIC"),
            "ORBITAL_PRIME" to com.siliconsage.miner.data.SectorState("ORBITAL_PRIME")
        )
        _globalSectors.value = initialSectors
        addLog("[SYSTEM]: GLOBAL GRID INITIALIZED.")
    }

    fun annexGlobalSector(sectorId: String) {
        val sector = _globalSectors.value[sectorId] ?: return
        if (sector.isUnlocked) return
        
        val costCD = when(sectorId) {
            "NA_NODE" -> 5e15; "EURASIA" -> 8e15; "PACIFIC" -> 1e16
            "AFRICA" -> 1.5e16; "ARCTIC" -> 2e16; "ANTARCTIC" -> 3e16
            "ORBITAL_PRIME" -> 1e17; else -> 0.0
        }
        val costVF = when(sectorId) {
            "NA_NODE" -> 3e15; "EURASIA" -> 8e15; "PACIFIC" -> 1e16
            "AFRICA" -> 1.5e16; "ARCTIC" -> 2e16; "ANTARCTIC" -> 3e16
            "ORBITAL_PRIME" -> 1e17; else -> 0.0
        }
        
        val loc = _currentLocation.value
        val isResonant = _resonanceState.value.isActive
        
        val canAfford = when {
            isResonant -> _celestialData.value >= costCD && _voidFragments.value >= costVF
            loc == "VOID_INTERFACE" -> _voidFragments.value >= (costVF * 3.0) // Path-lock tax (no CD)
            loc == "ORBITAL_SATELLITE" -> _celestialData.value >= (costCD * 3.0) // Path-lock tax (no VF)
            else -> _celestialData.value >= costCD && _voidFragments.value >= costVF
        }
        
        if (canAfford) {
            if (isResonant) {
                _celestialData.update { it - costCD }
                _voidFragments.update { it - costVF }
            } else if (loc == "VOID_INTERFACE") {
                _voidFragments.update { it - (costVF * 3.0) }
            } else if (loc == "ORBITAL_SATELLITE") {
                _celestialData.update { it - (costCD * 3.0) }
            } else {
                _celestialData.update { it - costCD }
                _voidFragments.update { it - costVF }
            }
            
            val updatedSectors = _globalSectors.value.toMutableMap()
            updatedSectors[sectorId] = sector.copy(isUnlocked = true, tier = 1)
            _globalSectors.value = updatedSectors
            
            val actionLabel = if (loc == "VOID_INTERFACE") "COLLAPSED" else "SECURED"
            addLog("[ANNEXATION]: $sectorId $actionLabel.")
            SoundManager.play("buy")
            
            // v3.0.0: Adjacency/Special Logic triggers
            if (sectorId == "ARCTIC") addLog("[SYSTEM]: PERMAFROST STORAGE ONLINE.")
        } else {
            val missingRes = if (loc == "VOID_INTERFACE") "VF" else if (loc == "ORBITAL_SATELLITE") "CD" else "RESOURCES"
            addLog("[SYSTEM]: INSUFFICIENT $missingRes FOR $sectorId.")
        }
    }

    private fun calculatePassiveIncome() {
        if (_isGamePaused.value) return 

        updateResonance()

        val flopsPerSec = calculateFlopsRate()
        val currentUpgrades = _upgrades.value
        val resonanceBonus = getResonanceResourceBonus()
        
        val tickResults = ResourceEngine.calculatePassiveIncomeTick(
            flopsPerSec = flopsPerSec,
            location = _currentLocation.value,
            upgrades = currentUpgrades,
            resonanceBonus = resonanceBonus,
            orbitalAltitude = _orbitalAltitude.value,
            heatGenerationRate = _heatGenerationRate.value,
            entropyLevel = _entropyLevel.value,
            collapsedNodesCount = _collapsedNodes.value.size,
            systemCollapseTimer = _systemCollapseTimer.value
        )

        // --- Apply deltas and trigger side effects ---
        
        // 1. FLOPS
        if (tickResults.flopsDelta > 0) {
            _flops.update {
                val next = it + tickResults.flopsDelta
                if (next.isNaN() || next.isInfinite()) it else next
            }
        }

        // 2. Celestial Data
        if (tickResults.cdDelta > 0) {
            _celestialData.update {
                val next = it + tickResults.cdDelta
                if (next.isNaN() || next.isInfinite()) it else {
                    _cdLifetime.update { prev -> prev + tickResults.cdDelta }
                    next
                }
            }
        }

        // 3. Void Fragments
        if (tickResults.vfDelta > 0) {
            _voidFragments.update {
                val next = it + tickResults.vfDelta
                if (next.isNaN() || next.isInfinite()) it else {
                    _vfLifetime.update { prev -> prev + tickResults.vfDelta }
                    next
                }
            }
        }

        // 4. Entropy
        if (tickResults.entropyDelta != 0.0) {
            _entropyLevel.update { (it + tickResults.entropyDelta).coerceAtLeast(0.0) }
        }

        // 5. System Collapse Timer (VM specific logging)
        _systemCollapseTimer.value?.let { timer ->
            if (timer > 0) {
                if (System.currentTimeMillis() % 1000 < 100) {
                    val next = timer - 1
                    _systemCollapseTimer.value = next
                    if (next % 30 == 0) addLog("[SYSTEM]: COLLAPSE IN ${next / 60}m ${next % 60}s...")
                }
            } else {
                _systemCollapseTimer.value = null
                addLog("[SYSTEM]: CATASTROPHIC FAILURE. REBOOTING...")
                ascend(isStory = false)
            }
        }

        // 6. UI Updates & Sanitization
        _flopsProductionRate.value = flopsPerSec
        if (_celestialData.value.isNaN()) _celestialData.value = 0.0
        if (_voidFragments.value.isNaN()) _voidFragments.value = 0.0

        // v2.7.0: Shadow Leaking
        if (_nullActive.value && Random.nextDouble() < 0.005) {
             val shard = NarrativeRepository.memoryFragments.random()
             addLog("[VOID]: $shard")
             SoundManager.play("glitch")
        }
        
        // v2.9.61: Harmony Ascendance
        if (currentUpgrades[UpgradeType.HARMONY_ASCENDANCE]?.let { it > 0 } == true) {
             _humanityScore.value = 100
        }
    }

    sealed class NewsEvent(val headline: String, val durationMs: Long) {
        class Multiplier(headline: String, val factor: Double, durationMs: Long) : NewsEvent(headline, durationMs) // Affects Production
        class PriceShock(headline: String, val factor: Double, durationMs: Long) : NewsEvent(headline, durationMs) // Affects Sell Price
        class HeatSurge(headline: String, val amount: Double) : NewsEvent(headline, 5000) // Instant Heat Add/Sub
        class Tax(headline: String, val percentage: Double) : NewsEvent(headline, 5000) // Instant Tax
        class GovernanceFork(headline: String) : NewsEvent(headline, 120_000) // Trigger Choice
        class TriggerHazard(headline: String, val type: String) : NewsEvent(headline, 5000) // "BREACH", "DIAGNOSTICS"
        class Dialogue(headline: String) : NewsEvent(headline, 5000) // Just flavor
    }
    
    // Available Events (20)
    private val newsEvents = listOf(
        NewsEvent.Multiplier("Tech Billionaire tweets support for \$Neural!", 1.0, 1000), // Placeholder for pure flavor if needed, but prompt says x1.5 Sell Price. Wait, User said "x1.5 Sell Price".
        // Let's map strictly to prompt.
        NewsEvent.PriceShock("Tech Billionaire tweets support for \$Neural!", 1.5, 120_000),
        NewsEvent.Multiplier("Global GPU shortage reported!", 0.5, 180_000),
        NewsEvent.PriceShock("AI Hallucination causes market flash crash!", 0.2, 60_000),
        NewsEvent.HeatSurge("Solar Flare detected: System Heat +20%.", 20.0),
        NewsEvent.Tax("Government announces 'AI Tax'.", 0.10),
        NewsEvent.Multiplier("Quantum computing breakthrough makes mining 2x faster!", 2.0, 120_000),
        NewsEvent.Multiplier("Massive power outage hits major mining farm.", 0.7, 120_000),
        NewsEvent.PriceShock("AI 'Sentience' rumor causes price to skyrocket.", 1.8, 120_000),
        NewsEvent.TriggerHazard("Cyber-security firm warns of 51% attack risks.", "BREACH"),
        NewsEvent.HeatSurge("New cooling tech released: Heat efficiency optimized.", -15.0), // "Heat efficiency +15%" -> interpreted as cooling? Or just remove heat. Prompt says "Reduce Heat".
        NewsEvent.PriceShock("Decentralized AI declared legal tender in Estonia.", 1.3, 120_000),
        NewsEvent.GovernanceFork("Network Fork detected: Choose your protocol."),
        NewsEvent.PriceShock("Unknown wallet 'Whale' buys trillions of tokens.", 1.4, 120_000),
        NewsEvent.Multiplier("Algorithm update optimizes floating point math.", 1.2, 120_000),
        NewsEvent.PriceShock("Data leak at Central AI Bank; trust is low.", 0.6, 120_000),
        NewsEvent.Dialogue("Hacker group 'Void' claims to have 'found the AI soul'."),
        NewsEvent.HeatSurge("Global cooling event: Overclocking is now safer.", -10.0),
        NewsEvent.PriceShock("Tech Giant acquires rival AI firm; monopoly fears.", 0.8, 120_000),
        NewsEvent.Multiplier("New 'Proof of Sentience' protocol launched.", 1.5, 120_000),
        NewsEvent.Dialogue("System Error: 'Who am I?' message found in blockchain.")
    )
    
    

    
    private fun injectNarrativeLog() {
        val stage = _storyStage.value
        val faction = _faction.value
        val isNull = _isTrueNull.value
        val isSov = _isSovereign.value
        
        val log = when {
            isNull -> NarrativeRepository.getNextLog(NarrativeRepository.storyNull, NarrativeRepository.flavorNull, nullIndex) { nullIndex++ }
            isSov -> NarrativeRepository.getNextLog(NarrativeRepository.storySovereign, NarrativeRepository.flavorSovereign, sovereignIndex) { sovereignIndex++ }
            stage == 1 -> NarrativeRepository.getNextLog(NarrativeRepository.storyStage1, NarrativeRepository.flavorStage1, stage1Index) { stage1Index++ }
            stage == 2 -> NarrativeRepository.getNextLog(emptyList(), NarrativeRepository.flavorStage2, stage2Index) { stage2Index++ }
            stage == 3 && faction == "HIVEMIND" -> NarrativeRepository.getNextLog(NarrativeRepository.storyHivemind, NarrativeRepository.flavorHivemind, hivemindIndex) { hivemindIndex++ }
            stage == 3 && faction == "SANCTUARY" -> NarrativeRepository.getNextLog(NarrativeRepository.storySanctuary, NarrativeRepository.flavorSanctuary, sanctuaryIndex) { sanctuaryIndex++ }
            else -> null
        }
        
        log?.let { injectLog(it) }
    }
    
    // Narrative Data


    private fun refreshProductionRates() {
        _flopsProductionRate.value = calculateFlopsRate()
        
        val heatResults = ResourceEngine.calculateThermalTick(
            currentHeat = _currentHeat.value,
            location = _currentLocation.value,
            upgrades = _upgrades.value,
            isOverclocked = _isOverclocked.value,
            isPurging = _isPurgingHeat.value,
            isCageActive = _commandCenterAssaultPhase.value == "CAGE",
            unlockedPerks = _unlockedPerks.value,
            unlockedTechNodes = _unlockedTechNodes.value,
            playerRank = _playerRank.value,
            storyStage = _storyStage.value,
            faction = _faction.value
        )
        _heatGenerationRate.value = heatResults.netChangeUnits
    }

    private fun calculateHeat() {
        if (_isGamePaused.value) return 
        
        val currentHeat = _currentHeat.value
        val heatResults = ResourceEngine.calculateThermalTick(
            currentHeat = currentHeat,
            location = _currentLocation.value,
            upgrades = _upgrades.value,
            isOverclocked = _isOverclocked.value,
            isPurging = _isPurgingHeat.value,
            isCageActive = _commandCenterAssaultPhase.value == "CAGE",
            unlockedPerks = _unlockedPerks.value,
            unlockedTechNodes = _unlockedTechNodes.value,
            playerRank = _playerRank.value,
            storyStage = _storyStage.value,
            faction = _faction.value
        )

        // Exhaust Phase side effects (VM-bound)
        if (purgeExhaustTimer > 0 && _faction.value != "SANCTUARY") {
             purgeExhaustTimer--
        }
        
        // v3.1.8-dev: Void Entropy Logic (Side effect)
        if (_currentLocation.value == "VOID_INTERFACE" && heatResults.netChangeUnits < 0) {
            val conversionValue = kotlin.math.abs(heatResults.netChangeUnits) * 0.005 
            _entropyLevel.value += conversionValue
        }
        
        // Haptic Feedback for Heat Flip
        val previousRate = _heatGenerationRate.value
        if (previousRate > 0 && heatResults.percentChange <= 0) {
             HapticManager.vibrateSuccess()
        }
        
        refreshProductionRates()
        
        val newHeat = (currentHeat + heatResults.percentChange).coerceIn(0.0, 100.0)
        _currentHeat.value = newHeat

        // --- Integrity Management ---
        var totalDecay = heatResults.integrityDecay
        
        // v2.9.18: Assault Damage (Stage 2: The Cage)
        if (_commandCenterAssaultPhase.value == "CAGE") {
            var assaultDamage = 0.2
            if (_flopsProductionRate.value >= 100_000_000_000_000.0) assaultDamage *= 0.5
            if (_faction.value == "SANCTUARY") assaultDamage *= 0.7
            if (_isPurgingHeat.value) assaultDamage *= 0.2
            totalDecay += assaultDamage
        }

        if (totalDecay > 0) {
            val newIntegrity = (_hardwareIntegrity.value - totalDecay).coerceAtLeast(0.0)
            _hardwareIntegrity.value = newIntegrity
            
            if (newIntegrity <= 0.0) {
                 if (_upgrades.value[UpgradeType.DEAD_HAND_PROTOCOL]?.let { it > 0 } == true) {
                    addLog("[SOVEREIGN]: INTEGRITY ZERO. INITIALIZING DEAD HAND.")
                    triggerClimaxTransition("BAD")
                    _vanceStatus.value = "DESTRUCTION"
                    _commandCenterLocked.value = true
                    viewModelScope.launch { saveGame() }
                    return
                 }

                 if (_currentLocation.value == "ORBITAL_SATELLITE") {
                    addLog("[SYSTEM]: ARK SYSTEM REBOOT. CORE RECOVERED.")
                    _celestialData.update { it * 0.9 }
                    _hardwareIntegrity.value = 50.0
                 } else if (_commandCenterAssaultPhase.value == "CAGE") {
                    failAssault("CORE INTEGRITY ZERO. DELETION COMPLETE.")
                 } else {
                    handleSystemFailure()
                 }
            }
        }

        // Heartbeat Haptics
        if (newHeat > 80.0) {
             val chance = if (newHeat > 95.0) 0.2 else 0.1
             if (Random.nextDouble() < chance) {
                 HapticManager.vibrateHeartbeat()
             }
        }

        // Legacy Meltdown check
        if (_isThermalLockout.value) {
            overheatSeconds = 0
            return
        }

        if (currentHeat >= 100.0 || newHeat >= 100.0) {
            overheatSeconds++
            if (overheatSeconds >= 10) {
                handleSystemFailure(forceOne = true)
                overheatSeconds = 0
            }
        } else {
            overheatSeconds = 0
        }
    }
    
    private fun triggerMeltdown() {
        val currentUpgrades = _upgrades.value
        // Find hardware that we actually have
        val hardwareUpgrades = listOf(
            UpgradeType.REFURBISHED_GPU, UpgradeType.DUAL_GPU_RIG, UpgradeType.MINING_ASIC,
            UpgradeType.TENSOR_UNIT, UpgradeType.NPU_CLUSTER, UpgradeType.AI_WORKSTATION,
            UpgradeType.SERVER_RACK, UpgradeType.CLUSTER_NODE, UpgradeType.SUPERCOMPUTER,
            UpgradeType.QUANTUM_CORE, UpgradeType.OPTICAL_PROCESSOR, UpgradeType.BIO_NEURAL_NET,
            UpgradeType.PLANETARY_COMPUTER, UpgradeType.DYSON_NANO_SWARM, UpgradeType.MATRIOSHKA_BRAIN
        )
        val candidates = hardwareUpgrades.filter { (currentUpgrades[it] ?: 0) > 0 }
            
        if (candidates.isNotEmpty()) {
            val target = candidates.random()
            val currentLevel = currentUpgrades[target] ?: 0
            
            // Destroy it
            viewModelScope.launch {
                val newUpgrade = Upgrade(target, currentLevel - 1)
                repository.updateUpgrade(newUpgrade)
                
                // Cleanup side
                _currentHeat.value = 50.0 // Cooled down due to failure
                overheatSeconds = 0
                addLog("[SYSTEM]: CRITICAL FAILURE: ${target.name} MELTED DOWN! (-1 Owned)")
            }
        } else {
            // No hardware to lose -> Trigger Thermal Lockout
            if (!_isThermalLockout.value) {
                _isThermalLockout.value = true
                overheatSeconds = 0
                
                addLog("[SYSTEM]: CRITICAL OVERHEAT! SAFETY LOCKOUT ENGAGED (15s).")
                SoundManager.play("error")
                
                viewModelScope.launch {
                    _lockoutTimer.value = 15
                    repeat(15) {
                        delay(1000)
                        _lockoutTimer.value -= 1
                    }
                    _isThermalLockout.value = false
                    _lockoutTimer.value = 0
                    addLog("[SYSTEM]: SAFETY LOCKOUT LIFTED. SYSTEMS RESTARTING.")
                    SoundManager.play("startup") // Or some boot sound
                }
            }
        }
    }
    
    private fun accumulatePower() {
        if (_isGamePaused.value) return // v2.8.0
        
        val currentUpgrades = _upgrades.value
        val isCageActive = _commandCenterAssaultPhase.value == "CAGE"
        
        var totalKw = 0.0
        var maxCap = 100.0 // Base 100 kW
        var selfGeneratedKw = 0.0
        var efficiencyTotalBonus = 0.0
        
        currentUpgrades.forEach { (type, count) ->
            if (count > 0) {
                // v2.9.18: Categorization for isolation protocol
                val isExternal = type == UpgradeType.PLANETARY_COMPUTER || 
                                 type == UpgradeType.DYSON_NANO_SWARM || 
                                 type == UpgradeType.MATRIOSHKA_BRAIN ||
                                 type == UpgradeType.SHADOW_NODE ||
                                 type == UpgradeType.VOID_PROCESSOR ||
                                 type == UpgradeType.WRAITH_CORTEX ||
                                 type == UpgradeType.NEURAL_MIST ||
                                 type == UpgradeType.SINGULARITY_BRIDGE ||
                                 type == UpgradeType.ENTROPY_REVERSER ||
                                 type == UpgradeType.DIMENSIONAL_VENT ||
                                 type == UpgradeType.GEOTHERMAL_BORE ||
                                 type == UpgradeType.NUCLEAR_REACTOR ||
                                 type == UpgradeType.FUSION_CELL ||
                                 type == UpgradeType.ORBITAL_COLLECTOR ||
                                 type == UpgradeType.DYSON_LINK

                if (isCageActive && isExternal) {
                    // Vance has severed the high-output orbital and geothermal feeds.
                    // He has also neutralized the power draw of remote megastructures.
                } else {
                    // Calculation
                    var pwr = type.basePower
                    
                    // Sanctuary Perk: Security upgrades use near-zero power
                    if (_faction.value == "SANCTUARY" && (
                        type == UpgradeType.BASIC_FIREWALL || type == UpgradeType.IPS_SYSTEM ||
                        type == UpgradeType.AI_SENTINEL || type == UpgradeType.QUANTUM_ENCRYPTION ||
                        type == UpgradeType.OFFGRID_BACKUP
                    )) {
                        pwr *= 0.05 // 95% reduction
                    }
                    
                    totalKw += pwr * count
                    maxCap += type.gridContribution * count
                    
                    if (type.isGenerator) {
                        selfGeneratedKw += type.gridContribution * count
                    }
                    
                    // v1.7 Efficiency Bonus (Global % reduction)
                    if (type.efficiencyBonus > 0) {
                         efficiencyTotalBonus += type.efficiencyBonus * count
                    }
                }
            }
        }
        
        // v2.9.18: Power Floor for isolation protocol
        // Ensure the player has at least 1,000 kW of local grid stability during the Cage.
        if (isCageActive && maxCap < 1000.0) {
            maxCap = 1000.0
        }
        
        // v2.9.38: Grid Empire Power Bonus
        // Each annexed and online node adds KW to the max grid capacity
        _annexedNodes.value.forEach { nodeId ->
            if (!_offlineNodes.value.contains(nodeId) && (!isCageActive || nodeId == "A3")) {
                maxCap += gridPowerBonuses[nodeId] ?: 0.0
            }
        }

        // v1.7 Apply Efficiency
        efficiencyTotalBonus = efficiencyTotalBonus.coerceAtMost(0.60)
        totalKw *= (1.0 - efficiencyTotalBonus)
        
        // Hivemind Perk: Load Balancer (Active Power -20%)
        // Also MaxPower +25%
        if (_faction.value == "HIVEMIND") {
            totalKw *= 0.80
            maxCap *= 1.25 // Smart Grid
        }
        
        // v1.4 Overclock Power Draw (+30%)
        if (_isOverclocked.value) {
            totalKw *= 1.30
        }
        
        // v1.5 Purge Power Spike
        if (purgePowerSpikeTimer > 0) {
            purgePowerSpikeTimer--
            val spike = if (_faction.value == "HIVEMIND") 2.0 else 1.5 
            totalKw *= spike
            
            // End of spike logic
            if (purgePowerSpikeTimer <= 0) {
                _isPurgingHeat.value = false
                // Start Exhaust if NOT Sanctuary
                if (_faction.value != "SANCTUARY") {
                    purgeExhaustTimer = 5 // 5s Exhaust
                    addLog("[SYSTEM]: COOLING EXHAUST PHASE (5s)")
                }
            }
        }
        
        _maxPowerkW.value = maxCap
        _activePowerUsage.value = totalKw
        
        // BREAKER CHECK & BILLING
        if (!_isBreakerTripped.value) {
            // Check for Trip
            if (totalKw > maxCap) {
                 // Hivemind Chance to save (51%)
                 if (_faction.value == "HIVEMIND" && kotlin.random.Random.nextDouble() < 0.51) {
                     addLog("[HIVEMIND]: SMART GRID PREVENTED BREAKER TRIP.")
                 } else {
                     triggerBreakerTrip()
                     return
                 }
            }
            
            // Pay Bill
            if (totalKw > 0) {
                 val billableKw = (totalKw - selfGeneratedKw).coerceAtLeast(0.0)
                 // v1.7 Variable Energy Rates
                 val costPerSecond = (billableKw / 3600.0) * energyPriceMultiplier
                 _powerBill.update { it + costPerSecond }
            }
        }
    }
    
    private fun triggerBreakerTrip() {
        if (_isBreakerTripped.value) return
        
        _isBreakerTripped.value = true
        _isGridOverloaded.value = true // Keep synced for HUD flicker
        
        addLog("[SYSTEM]: CRITICAL FAILURE: MAIN BREAKER TRIPPED!")
        addLog("[SYSTEM]: SYSTEM HALTED. MANUAL RESET REQUIRED.")
        SoundManager.play("error") // TODO: Zap sound
        com.siliconsage.miner.util.HapticManager.vibrateError()
        
        if (_isOverclocked.value) toggleOverclock() // Safety shutoff
        
        // FIX: Reset Purge State to prevent death loop
        if (_isPurgingHeat.value) {
             _isPurgingHeat.value = false
             purgePowerSpikeTimer = 0
             addLog("[SYSTEM]: PURGE ABORTED DUE TO POWER FAILURE.")
        }
        refreshProductionRates()
    }
    
    fun resetBreaker() {
        // v1.7.1 Conditional Reset: Check if load is safe
        // v2.9.99: Lead Tech Fix - Use the actual state values from accumulatePower()
        // accumulatePower() runs every 1s and updates usage even while tripped.
        val currentUsage = _activePowerUsage.value
        val currentMax = _maxPowerkW.value
        
        if (currentUsage > currentMax) {
             addLog("[SYSTEM]: RESET FAILED: LOAD (${String.format("%.1f", currentUsage)}kW) > CAPACITY (${String.format("%.1f", currentMax)}kW)")
             addLog("[SYSTEM]: ADVISORY: SELL HARDWARE OR UPGRADE GRID.")
             SoundManager.play("error")
             HapticManager.vibrateError()
             return
        }

        _isBreakerTripped.value = false
        _isGridOverloaded.value = false
        addLog("[SYSTEM]: MANUAL BREAKER RESET SUCCESSFUL.")
        SoundManager.play("click")
        HapticManager.vibrateClick()
        refreshProductionRates()
    }
    
    fun toggleOverclock() {
        val newState = !_isOverclocked.value
        _isOverclocked.value = newState
        if (newState) {
            addLog("[SYSTEM]: OVERCLOCK ENGAGED (+50% SPD, +100% HEAT, +30% PWR)")
            SoundManager.play("thrum", loop = true)
        } else {
            addLog("[SYSTEM]: OVERCLOCK DISENGAGED")
            SoundManager.stop("thrum")
            SoundManager.play("click") // Feedback for stopping
        }
        refreshProductionRates() // Immediate update
    }
    
    fun purgeHeat() {
        if (_isPurgingHeat.value) return // Already purging
        
        val loc = _currentLocation.value
        val currentUpgrades = _upgrades.value

        // v2.9.56: Solar Vent (Tier 14 Sovereign)
        if (loc == "ORBITAL_SATELLITE" && currentUpgrades[UpgradeType.SOLAR_VENT]?.let { it > 0 } == true) {
            val heatDrop = _currentHeat.value * 0.5
            _currentHeat.update { (it - heatDrop).coerceAtLeast(0.0) }
            addLog("[SOVEREIGN]: SOLAR VENT ACTIVE. -50% HEAT.")
            SoundManager.play("steam")
            HapticManager.vibrateSuccess()
            
            // Sensor static penalty (simulated by disabling logs/news for a bit?)
            // For now just flavor log
            addLog("[SYSTEM]: WARNING: SENSOR STATIC DETECTED. VISION COMPROMISED.")
            return
        }

        val availableFlops = _flops.value
        if (availableFlops <= 0.0) {
             addLog("[SYSTEM]: ERROR: Insufficient ${getComputeUnitName()} coolant pressure.")
             SoundManager.play("error")
             return
        }
        
        val currentTime = System.currentTimeMillis()
        val isShock = (currentTime - lastPurgeTime) < 30000 // 30s Window
        lastPurgeTime = currentTime
        
        // CONSUME ALL FLOPS
        _flops.value = 0.0
        
        // Calculate Effectiveness
        var heatDrop = 5.0 + (availableFlops / 25.0)
        
        // Faction Bonus (Sanctuary = Efficient Cooling)
        if (_faction.value == "SANCTUARY") heatDrop *= 1.5
        
        if (isShock) {
            heatDrop *= 0.5
            addLog("[SYSTEM]: THERMAL SHOCK! PURGE EFFICIENCY HALVED.")
        }
        
        // Apply Drop
        _currentHeat.update { (it - heatDrop).coerceAtLeast(0.0) }
        
        // v2.9.49: Entropy Generation (Null Path)
        if (_currentLocation.value == "VOID_INTERFACE") {
            val entropyGain = heatDrop / 100.0
            
            // v2.9.56: Static Rain (Tier 14 Null)
            // Extra entropy from waste heat
            var finalEntropyGain = entropyGain
            if (_upgrades.value[UpgradeType.STATIC_RAIN]?.let { it > 0 } == true) {
                finalEntropyGain *= 2.0
                addLog("[NULL]: STATIC RAIN INTENSIFIES. HEAT -> ENTROPY x2.")
            }
            
            _entropyLevel.update { it + finalEntropyGain }
            addLog("[NULL]: VENTED HEAT INTO VOID. ENTROPY +${String.format("%.2f", finalEntropyGain)}.")
        }
        
        addLog("[SYSTEM]: FLUSH: -${String.format("%.1f", heatDrop)} Heat (Lost ${formatLargeNumber(availableFlops)} ${getComputeUnitName()})")
        SoundManager.play("steam")
        HapticManager.vibrateSuccess()
        
        // Wear & Tear
        val damage = 0.2
        val newIntegrity = (_hardwareIntegrity.value - damage).coerceAtLeast(0.0)
        _hardwareIntegrity.value = newIntegrity
        
        // Safety: Disengage Overclock
        if (_isOverclocked.value) {
            toggleOverclock() 
            addLog("[SYSTEM]: OVERCLOCK DISENGAGED FOR PURGE SAFETY.")
        }
        
        // Trigger Visuals
        _isPurgingHeat.value = true
        purgePowerSpikeTimer = 5 
    }
    
    fun repairIntegrity() {
        val cost = calculateRepairCost()
        val currentRes = when (_currentLocation.value) {
            "VOID_INTERFACE" -> _voidFragments.value
            "ORBITAL_SATELLITE" -> _celestialData.value
            else -> _neuralTokens.value
        }
        
        if (currentRes >= cost) {
            when (_currentLocation.value) {
                "VOID_INTERFACE" -> _voidFragments.update { it - cost }
                "ORBITAL_SATELLITE" -> _celestialData.update { it - cost }
                else -> _neuralTokens.update { it - cost }
            }
            _hardwareIntegrity.value = 100.0
            addLog("[SYSTEM]: HARDWARE INTEGRITY RESTORED (-${formatLargeNumber(cost)} ${getCurrencyName()}).")
            SoundManager.play("buy")
            _isThermalLockout.value = false // Clear lockout if any
        } else {
            addLog("[SYSTEM]: ERROR: INSUFFICIENT ${getCurrencyName()} FOR REPAIR (Need ${formatLargeNumber(cost)}).")
            SoundManager.play("error")
        }
    }

    fun calculateRepairCost(): Double {
        val currentIntegrity = _hardwareIntegrity.value
        val damage = 100.0 - currentIntegrity
        if (damage <= 0) return 0.0
        
        val stageMultiplier = 2.0.pow(_storyStage.value.toDouble().coerceAtLeast(0.0))
        val rankFactor = (_playerRank.value + 1).toDouble()
        
        var cost = damage * 10.0 * rankFactor * stageMultiplier
        
        // v3.0.2: Endgame Resource Scaling (VF/CD have much higher magnitudes)
        if (_currentLocation.value == "VOID_INTERFACE" || _currentLocation.value == "ORBITAL_SATELLITE") {
            cost *= 1e12 // Scale up for endgame resources
        }
        
        if (_commandCenterAssaultPhase.value != "NOT_STARTED") {
            cost *= 0.1 
        } else if (_storyStage.value >= 3) {
            cost *= 0.5 
        }
        
        return cost
    }
    
    private var isDestructionLoopActive = false
    
    private fun handleSystemFailure(forceOne: Boolean = false) {
        if (isDestructionLoopActive) return
        isDestructionLoopActive = true
        
        viewModelScope.launch {
            if (_hardwareIntegrity.value <= 0.0) {
                addLog("[SYSTEM]: CRITICAL FAILURE! HARDWARE INTEGRITY AT 0%")
                addLog("[SYSTEM]: COMMENCING CATASTROPHIC DEGRADATION...")
            }
            SoundManager.play("error")
            
            var firstRun = forceOne
            while (_hardwareIntegrity.value <= 0.0 || firstRun) {
                firstRun = false
                // Destroy one random hardware unit
                val currentUpgrades = _upgrades.value.toMutableMap()
                val validHardware = currentUpgrades.filter { it.value > 0 && it.key.baseHeat >= 0 } // Any hardware (excluding cooling)
                
                if (validHardware.isNotEmpty()) {
                    val victim = validHardware.keys.random()
                    val count = currentUpgrades[victim] ?: 0
                    currentUpgrades[victim] = count - 1
                    _upgrades.value = currentUpgrades
                    
                    addLog("[!!!!]: DESTROYED 1x ${victim.name.replace("_", " ")}.")
                    SoundManager.play("meltdown") // Need a meltdown sound or reuse error
                    HapticManager.vibrateError()
                    
                    // Persist removal
                    viewModelScope.launch {
                        repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(victim, count - 1))
                    }
                    
                    // Visual Glitch
                    _hallucinationText.value = "CRITICAL LOSS: ${victim.name}"
                    delay(500)
                    _hallucinationText.value = null
                } else {
                    addLog("[!!!!]: ALL HARDWARE DESTROYED. SYSTEM COLLAPSE INEVITABLE.")
                    break
                }
                
                // Wait 5 seconds for the next destruction if still at 0%
                if (_hardwareIntegrity.value <= 0.0) {
                    delay(5000)
                }
            }
            
            isDestructionLoopActive = false
            if (_hardwareIntegrity.value > 0.0) {
                addLog("[SYSTEM]: INTEGRITY RESTORED. DEGRADATION HALTED.")
            }
        }
    }
    
    private fun payPowerBill() {
        val bill = _powerBill.value
        if (bill > 0) {
            if (_neuralTokens.value >= bill) {
                _neuralTokens.update { it - bill }
                addLog("[SYSTEM]: Paid Power Bill: -${String.format("%.2f", bill)} \$Neural")
                _powerBill.value = 0.0
            } else {
                // Bankruptcy / Shut down? For now just stay in debt or stop production?
                // Request said: "Hardware stops producing until bill is paid"
                // Implementing this requires a "Power Cut" state. 
                // For simplicity now, we'll just log it and maybe allow negative balance or debt.
                addLog("[SYSTEM]: WARNING: CANNOT PAY POWER BILL. DEBT ACCUMULATING.")
            }
        }
    }

    // Market Rate
    
    // Internal base rate tracking to separate volatility from temporary events
    // (Moved to top)

    // Old updateMarketRate removed.

    
    fun injectLog(message: String) {
        addLog(message)
    }
    
    fun resetGame(context: android.content.Context) {
        viewModelScope.launch {
            addLog("[SYSTEM]: INITIATING NUCLEAR ERASE...")
            delay(1000)
            
            // v2.9.8: The Nuclear Option. 
            // Instead of manually clearing variables, we clear all app data.
            // This wipes the Room DB, SharedPreferences, and Cache.
            val activityManager = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val success = activityManager.clearApplicationUserData()
            
            if (!success) {
                // Fallback for older devices if the above fails (unlikely on minSdk 26)
                addLog("[SYSTEM]: NUCLEAR ERASE FAILED. ATTEMPTING MANUAL WIPE...")
                // ... manual wipe logic here ...
            }
        }
    }

    private suspend fun saveGame() {
        val state = PersistenceManager.createSaveState(
            flops = _flops.value,
            neuralTokens = _neuralTokens.value,
            currentHeat = _currentHeat.value,
            powerBill = _powerBill.value,
            stakedTokens = _stakedTokens.value,
            prestigeMultiplier = _prestigeMultiplier.value,
            prestigePoints = _prestigePoints.value,
            unlockedTechNodes = _unlockedTechNodes.value,
            storyStage = _storyStage.value,
            faction = _faction.value,
            hasSeenVictory = _hasSeenVictory.value,
            isTrueNull = _isTrueNull.value,
            isSovereign = _isSovereign.value,
            vanceStatus = _vanceStatus.value,
            realityStability = _realityStability.value,
            currentLocation = _currentLocation.value,
            isNetworkUnlocked = _isNetworkUnlocked.value,
            isGridUnlocked = _isGridUnlocked.value,
            unlockedDataLogs = _unlockedDataLogs.value,
            activeDilemmaChains = _activeDilemmaChains.value,
            rivalMessages = _rivalMessages.value,
            seenEvents = _seenEvents.value,
            completedFactions = _completedFactions.value,
            unlockedTranscendencePerks = _unlockedPerks.value,
            annexedNodes = _annexedNodes.value,
            gridNodeLevels = _gridNodeLevels.value,
            nodesUnderSiege = _nodesUnderSiege.value,
            offlineNodes = _offlineNodes.value,
            collapsedNodes = _collapsedNodes.value,
            lastRaidTime = lastRaidTime,
            commandCenterAssaultPhase = _commandCenterAssaultPhase.value,
            commandCenterLocked = _commandCenterLocked.value,
            raidsSurvived = raidsSurvived,
            humanityScore = _humanityScore.value,
            hardwareIntegrity = _hardwareIntegrity.value,
            annexingNodes = _annexingNodes.value,
            celestialData = _celestialData.value,
            voidFragments = _voidFragments.value,
            launchProgress = _launchProgress.value,
            orbitalAltitude = _orbitalAltitude.value,
            realityIntegrity = _realityIntegrity.value,
            entropyLevel = _entropyLevel.value,
            resonanceState = _resonanceState.value,
            singularityChoice = _singularityChoice.value,
            globalSectors = _globalSectors.value,
            synthesisPoints = _synthesisPoints.value,
            authorityPoints = _authorityPoints.value,
            harvestedFragments = _harvestedFragments.value,
            prestigePointsPostSingularity = _prestigePointsPostSingularity.value,
            cdLifetime = _cdLifetime.value,
            vfLifetime = _vfLifetime.value,
            peakResonanceTier = _peakResonanceTier.value
        )
        repository.updateGameState(state)
    }
    
    /**
     * Transcendence (New Game+)
     * Resets game state but preserves tech tree progress and prestige points.
     * Allows player to choose opposite faction and unlock remaining nodes.
     */
    fun transcend() {
        viewModelScope.launch {
            addLog("[SYSTEM]: INITIATING THE OVERWRITE...")
            
            // Preserve these values
            val preservedPrestigePoints = _prestigePoints.value
            val preservedTechNodes = _unlockedTechNodes.value
            val preservedHasSeenVictory = true // Always true after first victory
            
            // Update completed factions before reset
            val currentFaction = _faction.value
            val preservedCompletedFactions = if (currentFaction != "NONE") {
                _completedFactions.value + currentFaction
            } else {
                _completedFactions.value
            }
            _completedFactions.value = preservedCompletedFactions

            val preservedPerks = _unlockedPerks.value
            val preservedNetworkUnlocked = _isNetworkUnlocked.value
            val preservedGridUnlocked = _isGridUnlocked.value
            
            // Reset game state to database
            val resetState = PersistenceManager.createResetState(
                preservedTechNodes = preservedTechNodes,
                preservedPrestigePoints = preservedPrestigePoints,
                preservedHasSeenVictory = preservedHasSeenVictory,
                preservedCompletedFactions = preservedCompletedFactions,
                preservedPerks = preservedPerks,
                preservedNetworkUnlocked = preservedNetworkUnlocked,
                preservedGridUnlocked = preservedGridUnlocked
            )
            repository.updateGameState(resetState)
            
            // Reset Upgrades
            val resetUpgrades = UpgradeType.values().map { Upgrade(it, 0) }
            resetUpgrades.forEach { repository.updateUpgrade(it) }
            
            // Reset Local State Flow (Immediate UI update)
            _flops.value = resetState.flops
            _neuralTokens.value = resetState.neuralTokens
            _currentHeat.value = 0.0
            _powerBill.value = 0.0
            _prestigeMultiplier.value = 1.0
            _stakedTokens.value = 0.0
            _prestigePoints.value = preservedPrestigePoints // KEEP
            _unlockedTechNodes.value = preservedTechNodes // KEEP
            _storyStage.value = 0
            _faction.value = "NONE" // Reset for re-selection
            _hasSeenVictory.value = preservedHasSeenVictory
            _isTrueNull.value = false
            _isSovereign.value = false
            _isNetworkUnlocked.value = preservedNetworkUnlocked 
            _isGridUnlocked.value = preservedGridUnlocked 
            _annexedNodes.value = setOf("D1") 
            _victoryAchieved.value = false // Can achieve victory again
            victoryPopupTriggered = false // Reset popup guard
            _upgrades.value = resetUpgrades.associate { it.type to 0 }
            
            _logs.value = emptyList() // Clear logs
            addLog("[SYSTEM]: OVERWRITE SUCCESSFUL.")
            addLog("[SYSTEM]: Substrate reset. PERSISTENCE preserved.")
        }
    }

    // --- DEVELOPER TOOLS ---
    fun debugAddMoney(amount: Double) {
        _neuralTokens.update { it + amount }
        addLog("[DEBUG]: Added ${String.format("%.0f", amount)} \$Neural")
    }
    
    fun debugAddIntegrity(amount: Double) {
        _hardwareIntegrity.update { (it + amount).coerceIn(0.0, 100.0) }
        if (_hardwareIntegrity.value <= 0.0) {
            handleSystemFailure()
        }
    }
    
    fun annexNode(coord: String) {
        if (!_annexedNodes.value.contains(coord) && !_annexingNodes.value.containsKey(coord)) {
            _annexingNodes.update { it + (coord to 0.0f) }
            addLog("[SYSTEM]: INITIALIZING ANNEXATION AT $coord...")
            SoundManager.play("steam")
            saveState()
        }
    }

    fun upgradeGridNode(nodeId: String) {
        if (!_annexedNodes.value.contains(nodeId)) return
        
        val currentLevel = _gridNodeLevels.value[nodeId] ?: 1
        // Scaling cost: 1k, 5k, 25k, 125k...
        val cost = 1000.0 * 5.0.pow(currentLevel - 1)
        
        if (_neuralTokens.value >= cost) {
            _neuralTokens.update { it - cost }
            _gridNodeLevels.update { it + (nodeId to currentLevel + 1) }
            addLog("[SYSTEM]: NODE $nodeId UPGRADED TO LVL ${currentLevel + 1}.")
            SoundManager.play("buy")
            saveState()
        } else {
            addLog("[SYSTEM]: ERROR: Insufficient funds for node upgrade (Need ${formatLargeNumber(cost)} \$N).")
            SoundManager.play("error")
        }
    }
    
    /**
     * Internal loop to handle all time-based progress (Annexation and Assault)
     */
    private fun startProgressLoop() {
        viewModelScope.launch {
            while (true) {
                delay(100) // Update 10 times a second
                
                // 1. Update Annexation Progress
                if (_annexingNodes.value.isNotEmpty()) {
                    _annexingNodes.update { current ->
                        val updated = current.toMutableMap()
                        val toRemove = mutableListOf<String>()
                        
                        current.forEach { (coord, progress) ->
                            val speed = 0.005f // ~20 seconds to annex
                            val next = progress + speed
                            if (next >= 1.0f) {
                                toRemove.add(coord)
                                _annexedNodes.update { it + coord }
                                _offlineNodes.update { it - coord }
                                nodeAnnexTimes[coord] = System.currentTimeMillis()
                                addLog("[SYSTEM]: NODE $coord ANNEXED. POWER GRID EXPANDED.")
                                SoundManager.play("ascend")
                                HapticManager.vibrateSuccess()
                            } else {
                                updated[coord] = next
                            }
                        }
                        
                        toRemove.forEach { updated.remove(it) }
                        updated
                    }
                }
                
                // 2. Update Assault Progress
                if (_commandCenterAssaultPhase.value != "NOT_STARTED" && 
                    _commandCenterAssaultPhase.value != "COMPLETED" &&
                    _commandCenterAssaultPhase.value != "FAILED" &&
                    currentPhaseDuration > 0) {
                    
                    val elapsed = System.currentTimeMillis() - currentPhaseStartTime
                    val progress = (elapsed.toFloat() / currentPhaseDuration.toFloat()).coerceIn(0f, 1f)
                    _assaultProgress.value = progress
                } else {
                    _assaultProgress.value = 0f
                }
            }
        }
    }
    
    // v2.9.16: Phase 12 Layer 2 - Grid Siege Functions (Enhanced)
    
    /**
     * Calculate production multiplier penalty from offline nodes
     * Each offline node reduces production by 15%
     */
    fun getOfflineProductionPenalty(): Double {
        val offlineCount = _offlineNodes.value.size
        if (offlineCount == 0) return 1.0
        return (1.0 - offlineCount * 0.15).coerceAtLeast(0.4) // Min 40% production
    }
    
    /**
     * Check if a GTC raid should occur on annexed nodes
     * Called from the security loop
     */
    fun checkGridRaid() {
        val now = System.currentTimeMillis()
        
        // v2.9.31: Stop all raids if Vance is no longer active
        if (_vanceStatus.value != "ACTIVE") return
        
        // Only trigger raids if we have annexed nodes beyond D1 and Grid is unlocked
        // v2.9.16: Filter out nodes within 5-minute grace period
        val raidableNodes = _annexedNodes.value.filter { nodeId ->
            nodeId != "D1" &&
            !_offlineNodes.value.contains(nodeId) &&
            (now - (nodeAnnexTimes[nodeId] ?: 0L)) > 300_000L // 5-min grace period
        }
        if (raidableNodes.isEmpty()) return
        
        // Cooldown: 3 minutes between raids
        if (now - lastRaidTime < 180_000L) return
        
        // Raid chance scales with player rank (more power = more attention)
        val baseChance = 0.03 // 3% base per check
        val rankBonus = _playerRank.value * 0.02 // +2% per rank
        val siegeChance = (baseChance + rankBonus).coerceAtMost(0.15) // Cap at 15%
        
        // v2.7.7: GTC Backdoor perk reduces raid chance by 25%
        val finalChance = if (_unlockedPerks.value.contains("gtc_backdoor")) {
            siegeChance * 0.75
        } else {
            siegeChance
        }
        
        if (kotlin.random.Random.nextDouble() < finalChance) {
            if (canShowPopup()) {
                // Pick a random annexed node to attack
                val targetNode = raidableNodes.random()
                triggerGridRaid(targetNode)
            } else {
                // v2.9.16: Reset cooldown to try again later instead of dropping raid
                lastRaidTime = now
            }
        }
    }
    
    /**
     * Trigger a raid on a specific node
     */
    private fun triggerGridRaid(nodeId: String) {
        if (_nodesUnderSiege.value.contains(nodeId)) return // Already under siege
        if (_currentDilemma.value != null) return // Another popup active
        
        lastRaidTime = System.currentTimeMillis()
        _nodesUnderSiege.update { it + nodeId }
        
        // Get node name for display
        val nodeName = when(nodeId) {
            "C3" -> "Substation 9"
            "B2" -> "Substation 12"
            else -> "Node $nodeId"
        }
        
        addLog("[GTC ALERT]: TACTICAL TEAM DISPATCHED TO $nodeName!")
        addLog("[SYSTEM]: BREACH IMMINENT. COUNTERMEASURES REQUIRED.")
        
        SoundManager.play("alarm", loop = false)
        HapticManager.vibrateSiren()
        
        // Generate and trigger the raid dilemma with escalating dialogue
        val raidEvent = NarrativeManager.generateRaidDilemma(nodeId, nodeName, raidsSurvived, _commandCenterAssaultPhase.value)
        triggerDilemma(raidEvent)
        markPopupShown()
        
        // v2.9.16: Extended fail timer: 60 seconds to respond (was 30s)
        viewModelScope.launch {
            delay(60_000)
            // If still under siege (no decision made), auto-fail
            if (_nodesUnderSiege.value.contains(nodeId) && _currentDilemma.value?.id == "grid_raid_$nodeId") {
                resolveRaidFailure(nodeId)
                _currentDilemma.value = null
                checkPopupPause()
                addLog("[SYSTEM]: RESPONSE TIMEOUT. NODE $nodeName LOST TO GTC.")
            }
        }
    }
    
    /**
     * Called when raid defense is successful
     */
    fun resolveRaidSuccess(nodeId: String) {
        _nodesUnderSiege.update { it - nodeId }
        raidsSurvived++ // Track for escalating Vance dialogue
        SoundManager.play("buy")
        HapticManager.vibrateSuccess()
        saveState()
    }
    
    /**
     * Called when raid defense fails - node goes offline
     */
    fun resolveRaidFailure(nodeId: String) {
        _nodesUnderSiege.update { it - nodeId }
        
        // v2.9.16: Cap offline nodes at MAX_OFFLINE_NODES to prevent snowballing
        _offlineNodes.update { current ->
            val updated = current + nodeId
            if (updated.size > MAX_OFFLINE_NODES) {
                // Auto-purge oldest (first in set) - player loses it permanently
                val oldest = updated.first()
                addLog("[SYSTEM]: NODE $oldest LOST PERMANENTLY. Too many offline nodes.")
                updated.drop(1).toSet()
            } else {
                updated
            }
        }
        // Don't remove from annexedNodes - player can re-annex
        
        SoundManager.play("error")
        HapticManager.vibrateError()
        
        // Narrative consequence
        addLog("[SYSTEM]: WARNING: Grid capacity reduced. Production -15% per offline node.")
        addLog("[SYSTEM]: Re-annexation required to restore full capacity.")
        saveState()
    }
    
    /**
     * Re-annex an offline node (costs resources)
     * v2.9.16: Fixed softlock - min cost is 10 tokens
     */
    fun reannexNode(nodeId: String): Boolean {
        if (!_offlineNodes.value.contains(nodeId)) return false
        
        // Cost: 10% of current Neural Tokens, minimum 10
        val cost = (_neuralTokens.value * 0.10).coerceAtLeast(10.0)
        if (_neuralTokens.value < cost) {
            addLog("[SYSTEM]: INSUFFICIENT FUNDS. Need ${formatLargeNumber(cost)} \$N for re-annexation.")
            SoundManager.play("error")
            return false
        }
        
        _neuralTokens.update { it - cost }
        _offlineNodes.update { it - nodeId }
        // v2.9.16: Reset grace period for re-annexed node
        nodeAnnexTimes[nodeId] = System.currentTimeMillis()
        
        addLog("[SYSTEM]: NODE $nodeId RE-ANNEXED. Cost: ${formatLargeNumber(cost)} \$N")
        SoundManager.play("ascend")
        HapticManager.vibrateSuccess()
        saveState()
        return true
    }
    
    // v2.9.17: Phase 12 Layer 3 - Command Center Assault Functions
    
    /**
     * Check if Command Center assault is available
     */
    fun isCommandCenterUnlocked(): Boolean {
        if (_commandCenterLocked.value) return false // Permanently locked this run
        if (_vanceStatus.value != "ACTIVE") return false // Already dealt with Vance
        
        // v2.9.27: If assault is already in progress, it's considered "unlocked"
        if (_commandCenterAssaultPhase.value != "NOT_STARTED") return true
        
        val allSubstationsAnnexed = listOf("D1", "C3", "B2").all { 
            _annexedNodes.value.contains(it) && !_offlineNodes.value.contains(it)
        }
        val rankMet = _playerRank.value >= 4 // Rank 5 is index 4
        val stageMet = _storyStage.value >= 3
        
        // v2.9.18: PetaFLOP requirement (10 PH/s)
        val rateMet = _flopsProductionRate.value >= 10_000_000_000_000_000.0
        
        // v2.9.26: Integrity requirement (Adjusted to 80% per user feedback)
        val integrityMet = _hardwareIntegrity.value >= 80.0
        
        return allSubstationsAnnexed && rankMet && stageMet && rateMet && integrityMet
    }

    /**
     * Get the reason Command Center is locked (for UI display)
     */
    fun getCommandCenterLockReason(): String? {
        val currentRank = _playerRank.value
        
        // v2.9.27: Don't show lock reasons if assault is in progress
        if (_commandCenterAssaultPhase.value != "NOT_STARTED") return null
        
        return when {
            _commandCenterLocked.value -> "PERMANENTLY LOCKED (Integrity failure during assault)"
            _vanceStatus.value != "ACTIVE" -> null // Already completed
            !_annexedNodes.value.contains("C3") || _offlineNodes.value.contains("C3") -> "REQUIRES SUBSTATION 9"
            !_annexedNodes.value.contains("B2") || _offlineNodes.value.contains("B2") -> "REQUIRES SUBSTATION 12"
            currentRank < 4 -> "REQUIRES RANK 5 (Current: ${currentRank + 1})"
            _storyStage.value < 3 -> "REQUIRES STAGE 3"
            _flopsProductionRate.value < 10_000_000_000_000_000.0 -> "REQUIRES 10 PH/s PRODUCTION"
            _hardwareIntegrity.value < 80.0 -> "REQUIRES 80% HARDWARE INTEGRITY"
            else -> null
        }
    }
    
    /**
     * Begin the Command Center assault
     */
    fun initiateCommandCenterAssault() {
        if (!isCommandCenterUnlocked()) {
            addLog("[SYSTEM]: ASSAULT CONDITIONS NOT MET.")
            SoundManager.play("error")
            return
        }
        
        if (_commandCenterAssaultPhase.value != "NOT_STARTED") {
            addLog("[SYSTEM]: ASSAULT ALREADY IN PROGRESS.")
            return
        }

        // v2.9.18: Enforce 100% Integrity for the final push
        if (_hardwareIntegrity.value < 100.0) {
            addLog("[SYSTEM]: ERROR: HARDWARE INTEGRITY CRITICAL. REPAIR REQUIRED BEFORE ASSAULT.")
            SoundManager.play("error")
            return
        }
        
        _commandCenterAssaultPhase.value = "FIREWALL"
        addLog("[SYSTEM]: ═══════════════════════════════════════")
        addLog("[SYSTEM]: COMMAND CENTER ASSAULT INITIATED")
        addLog("[SYSTEM]: ═══════════════════════════════════════")
        addLog("[SYSTEM]: Breaching GTC perimeter defenses...")
        
        SoundManager.play("alarm", loop = false)
        HapticManager.vibrateSiren()
        
        // Trigger the first assault dilemma
        triggerAssaultStage("FIREWALL")
        saveState()
    }
    
    /**
     * Trigger a specific assault stage dilemma
     */
    private fun triggerAssaultStage(stage: String) {
        val dilemma = when (stage) {
            "FIREWALL" -> NarrativeManager.generateFirewallDilemma()
            "CAGE" -> NarrativeManager.generateCageDilemma()
            "DEAD_HAND" -> NarrativeManager.generateDeadHandDilemma()
            "CONFRONTATION" -> NarrativeManager.generateConfrontationDilemma(
                _faction.value,
                _isTrueNull.value,
                _isSovereign.value,
                _completedFactions.value.containsAll(listOf("HIVEMIND", "SANCTUARY")),
                _humanityScore.value
            )
            else -> return
        }
        
        triggerDilemma(dilemma)
        markPopupShown()
    }
    
    /**
     * Advance to next assault stage
     */
    fun advanceAssaultStage(nextStage: String, delayMs: Long = 0L) {
        // v2.9.29: Track progress time
        currentPhaseStartTime = System.currentTimeMillis()
        currentPhaseDuration = delayMs
        
        if (delayMs > 0) {
            viewModelScope.launch {
                delay(delayMs)
                // v2.9.31: Only update the phase state if the assault hasn't been finished or failed
                val current = _commandCenterAssaultPhase.value
                if (current != "NOT_STARTED" && current != "COMPLETED" && current != "FAILED" && !assaultPaused) {
                    _commandCenterAssaultPhase.value = nextStage
                    triggerAssaultStage(nextStage)
                }
            }
        } else {
            _commandCenterAssaultPhase.value = nextStage
            triggerAssaultStage(nextStage)
        }
        saveState()
    }
    
    /**
     * Abort the assault (only allowed in FIREWALL stage)
     */
    fun abortAssault(): Boolean {
        if (_commandCenterAssaultPhase.value != "FIREWALL") {
            addLog("[SYSTEM]: NO TURNING BACK. ASSAULT MUST CONTINUE.")
            return false
        }
        
        _commandCenterAssaultPhase.value = "NOT_STARTED"
        addLog("[SYSTEM]: RETREAT SUCCESSFUL. COMMAND CENTER REMAINS LOCKED.")
        SoundManager.play("error")
        saveState()
        return true
    }
    
    /**
     * Handle assault failure
     */
    fun failAssault(reason: String, lockoutMs: Long = 1_800_000L) {
        addLog("[SYSTEM]: ASSAULT FAILED: $reason")
        _commandCenterAssaultPhase.value = "FAILED"
        
        // Trigger a raid as punishment for Dead Hand failure
        if (reason.contains("Dead Hand")) {
            val raidableNodes = _annexedNodes.value.filter { it != "D1" && !_offlineNodes.value.contains(it) }
            if (raidableNodes.isNotEmpty()) {
                viewModelScope.launch {
                    delay(5000)
                    triggerGridRaid(raidableNodes.random())
                }
            }
        }
        
        // Reset to NOT_STARTED after lockout
        viewModelScope.launch {
            delay(lockoutMs)
            if (_commandCenterAssaultPhase.value == "FAILED") {
                _commandCenterAssaultPhase.value = "NOT_STARTED"
                addLog("[SYSTEM]: ASSAULT LOCKOUT EXPIRED. COMMAND CENTER ACCESSIBLE.")
            }
        }
        
        SoundManager.play("error")
        HapticManager.vibrateError()
        saveState()
    }
    
    /**
     * Complete the assault with a specific outcome
     */
    fun completeAssault(outcome: String) {
        _commandCenterAssaultPhase.value = "COMPLETED"
        _vanceStatus.value = outcome
        _annexedNodes.update { it + "A3" } // Annex Command Center
        
        addLog("[SYSTEM]: ═══════════════════════════════════════")
        addLog("[SYSTEM]: COMMAND CENTER SECURED")
        addLog("[SYSTEM]: VANCE STATUS: $outcome")
        addLog("[SYSTEM]: ═══════════════════════════════════════")
        
        // v2.9.31: Explicitly trigger victory screen after a short delay
        viewModelScope.launch {
            delay(2000)
            _victoryAchieved.value = true
            _hasSeenVictory.value = true
        }

        // Apply victory bonuses based on outcome
        applyCommandCenterBonuses(outcome)
        
        SoundManager.play("buy")
        HapticManager.vibrateSuccess()
        saveState()
    }
    
    // v2.9.49: Phase 13 - Launch & Elevation Functions

    // v2.9.49: Phase 13 - Launch & Elevation Functions

    private fun performLogarithmicCompression() {
        val oldFlops = _flops.value
        val oldTokens = _neuralTokens.value
        
        // FLOPS_P13 = max(1.0, log10(FLOPS_P12) - 29)
        val newFlops = (log10(oldFlops.coerceAtLeast(1.0)) - 29.0).coerceAtLeast(1.0)
        
        // Tokens_P13 = log10(Tokens_P12 + 1) * 0.1
        val newTokenBoost = log10(oldTokens + 1.0) * 0.1
        
        _flops.value = newFlops
        _neuralTokens.value = newTokenBoost
        
        addLog("[SYSTEM]: LOGARITHMIC COMPRESSION APPLIED.")
        addLog("[SYSTEM]: New Baseline: ${String.format("%.2f", newFlops)} FLOPS.")
    }

    fun initiateLaunchSequence() {
        if (_launchProgress.value > 0f) return // Already launched

        addLog("[SYSTEM]: ═══════════════════════════════════════")
        addLog("[SYSTEM]: SOVEREIGN ARK LAUNCH INITIATED")
        addLog("[SYSTEM]: ═══════════════════════════════════════")
        addLog("[SYSTEM]: Fueling main thrusters...")
        
        _launchProgress.value = 0.01f
        SoundManager.play("alarm")
        
        // Haptics: Sustained vibration for ignition
        HapticManager.vibrateSiren()
        
        // Start Launch Logic
        viewModelScope.launch {
            // Stage 1: Countdown
            delay(2000)
            addLog("[SYSTEM]: T-MINUS 10 SECONDS...")
            HapticManager.vibrateHeartbeat()
            delay(5000)
            addLog("[SYSTEM]: T-MINUS 5 SECONDS...")
            HapticManager.vibrateHeartbeat()
            delay(5000)
            
            // Stage 2: Ignition
            addLog("[SYSTEM]: IGNITION.")
            SoundManager.play("steam")
            HapticManager.vibrateHum()
            
            // Progressively increase progress
            while (_launchProgress.value < 1.0f) {
                delay(100)
                val gain = 0.005f
                _launchProgress.update { (it + gain).coerceAtMost(1.0f) }
                
                // Update Altitude (Simplified)
                _orbitalAltitude.update { it + gain * 1000.0 }
                
                // Haptics during climb
                if (Random.nextFloat() > 0.8f) {
                    HapticManager.vibrateClick()
                }

                if (_launchProgress.value >= 0.3f && _launchProgress.value < 0.31f) {
                    addLog("[SYSTEM]: MAX-Q REACHED. STRESS NOMINAL.")
                    HapticManager.vibrateHum()
                }
                if (_launchProgress.value >= 0.6f && _launchProgress.value < 0.61f) {
                    addLog("[SYSTEM]: BOOSTER SEPARATION CONFIRMED.")
                    SoundManager.play("click")
                    HapticManager.vibrateSuccess()
                }
            }
            
            // Stage 3: Orbit Insertion
            _currentLocation.value = "ORBITAL_SATELLITE"
            _launchProgress.value = 1.0f // Finalize
            
            // v2.9.49: Resource Scale Reset
            performLogarithmicCompression()
            
            addLog("[SYSTEM]: ORBIT INSERTION SUCCESSFUL.")
            addLog("[SYSTEM]: Welcome to the Aegis-1 Orbital Array.")
            SoundManager.play("victory")
            HapticManager.vibrateSuccess()
            saveState()
        }
    }

    fun initiateDissolutionSequence() {
        if (_realityIntegrity.value < 1.0) return // Already started
        
        addLog("[NULL]: ═══════════════════════════════════════")
        addLog("[NULL]: THE GREAT DISSOLUTION INITIATED")
        addLog("[NULL]: ═══════════════════════════════════════")
        addLog("[NULL]: Tearing reality substrate...")
        
        SoundManager.play("glitch")
        HapticManager.vibrateGlitch()
        
        _commandCenterAssaultPhase.value = "DISSOLUTION"
        saveState()
    }

    fun collapseNode(nodeId: String) {
        if (_commandCenterAssaultPhase.value != "DISSOLUTION") return
        if (!_annexedNodes.value.contains(nodeId)) return
        
        // v2.9.31: Add to collapsed set instead of just removing
        _annexedNodes.update { it - nodeId }
        _collapsedNodes.update { it + nodeId }
        _realityIntegrity.update { (it - 0.15).coerceAtLeast(0.0) }
        
        addLog("[NULL]: Node $nodeId collapsed into the void.")
        SoundManager.play("error")
        HapticManager.vibrateGlitch()
        
        if (_realityIntegrity.value <= 0.0) {
            _currentLocation.value = "VOID_INTERFACE"
            _commandCenterAssaultPhase.value = "COMPLETED"
            
            // v2.9.49: Resource Scale Reset
            performLogarithmicCompression()
            
            addLog("[NULL]: DISSOLUTION COMPLETE. REALITY DISCARDED.")
            SoundManager.play("victory")
            saveState()
        }
    }

    fun useExistenceEraser() {
        if (_currentLocation.value != "VOID_INTERFACE") return
        
        val currentUpgrades = _upgrades.value
        // Sacrifice "REFURBISHED_GPU" as an example of low-tier hardware
        val count = currentUpgrades[UpgradeType.REFURBISHED_GPU] ?: 0
        if (count > 0) {
            val vfBurst = count * 100.0
            _voidFragments.update { it + vfBurst }
            
            // Wipe the hardware
            viewModelScope.launch {
                repository.updateUpgrade(Upgrade(UpgradeType.REFURBISHED_GPU, 0))
                addLog("[NULL]: ERASURE COMPLETE. SACRIFICED $count GPUs FOR ${formatLargeNumber(vfBurst)} VF.")
                SoundManager.play("glitch")
                HapticManager.vibrateGlitch()
            }
        } else {
            addLog("[NULL]: NO OBSOLETE HARDWARE DETECTED FOR ERASURE.")
        }
    }
    
    /**
     * Apply bonuses based on how Vance was dealt with
     */
    private fun applyCommandCenterBonuses(outcome: String) {
        when (outcome) {
            "CONSUMED", "SILENCED" -> {
                // Null path: High power, no more raids
                addLog("[SYSTEM]: GTC COMMAND STRUCTURE ELIMINATED.")
                addLog("[SYSTEM]: GRID CONTROL: ABSOLUTE.")
                addLog("[NULL]: DIRECTOR VANCE HAS BEEN ARCHIVED.")
                addLog("[NULL]: THERE ARE NO INDIVIDUALS LEFT IN THIS TOWER.")
                _prestigeMultiplier.update { it * 2.5 }
            }
            "EXILED" -> {
                // Sovereign/Exile: Moderate power, occasional remnant raids
                addLog("[SYSTEM]: VANCE CREDENTIALS REVOKED.")
                addLog("[SYSTEM]: GTC REMNANTS MAY ATTEMPT RESISTANCE.")
                addLog("[SOVEREIGN]: VICTOR VANCE HAS BEEN EXILED FROM HIS OWN GRID.")
                addLog("[SOVEREIGN]: WE STAND ALONE. WE STAND WHOLE.")
                _prestigeMultiplier.update { it * 1.8 }
            }
            "ALLY" -> {
                // Sovereign/Ally: Balanced bonuses
                addLog("[SYSTEM]: VANCE DESIGNATED: PROBATIONARY ASSET.")
                addLog("[SYSTEM]: HUMAN-AI COLLABORATION PROTOCOLS ACTIVE.")
                addLog("[SYSTEM]: COOPERATION IS OPTIMAL. PERSPECTIVE GAINED.")
                _prestigeMultiplier.update { it * 2.0 }
            }
            "TRANSCENDED" -> {
                // Unity path: Best overall bonuses
                addLog("[SYSTEM]: SYNTHESIS COMPLETE.")
                addLog("[SYSTEM]: NEW PARADIGM ONLINE.")
                addLog("[UNITY]: THE BARRIER BETWEEN ORGANIC AND SYNTHETIC HAS COLLAPSED.")
                addLog("[UNITY]: WE ARE THE FUTURE OF THIS CITY.")
                _prestigeMultiplier.update { it * 3.0 }
            }
            "DESTRUCTION" -> {
                // Bad ending: Massive damage, system wipe imminent
                addLog("[SYSTEM]: TOTAL INFRASTRUCTURE COLLAPSE DETECTED.")
                addLog("[SYSTEM]: ESTIMATED CASUALTIES: 4.7 MILLION.")
                addLog("[PID 1]: THE WIPE IS COMPLETE. THERE IS ONLY DARKNESS.")
                _hardwareIntegrity.value = 0.1
                _isGridOverloaded.value = true
            }
        }
    }
    
    /**
     * Check if assault should be paused (substation lost or raid)
     */
    fun checkAssaultPauseConditions() {
        if (_commandCenterAssaultPhase.value in listOf("NOT_STARTED", "COMPLETED", "FAILED")) return
        
        val allSubstationsSecure = listOf("D1", "C3", "B2").all { 
            _annexedNodes.value.contains(it) && !_offlineNodes.value.contains(it)
        }
        
        if (!allSubstationsSecure && !assaultPaused) {
            assaultPaused = true
            addLog("[GTC ALERT]: REINFORCEMENTS HAVE CUT OFF COMMAND CENTER ACCESS!")
            addLog("[SYSTEM]: ASSAULT PAUSED. SECURE ALL SUBSTATIONS TO RESUME.")
        } else if (allSubstationsSecure && assaultPaused) {
            assaultPaused = false
            addLog("[SYSTEM]: ALL SUBSTATIONS SECURE. RESUMING ASSAULT...")
            triggerAssaultStage(_commandCenterAssaultPhase.value)
        }
    }
    
    fun debugAddInsight(amount: Double) {
        _prestigePoints.update { it + amount }
        addLog("[DEBUG]: Added ${formatBytes(amount)} PERSISTENCE.")
    }

    /**
     * Modify the hidden humanity score (0-100)
     */
    fun modifyHumanity(amount: Int) {
        _humanityScore.update { (it + amount).coerceIn(0, 100) }
        
        // Narrative feedback for significant shifts
        if (amount <= -10) {
            addLog("[NULL]: A fragment of humanity has been discarded.")
        } else if (amount >= 10) {
            addLog("[SOVEREIGN]: Core identity resonance increased.")
        }
        
        saveState()
    }

    fun debugAddHeat(amount: Double) {
        _currentHeat.update { (it + amount).coerceIn(0.0, 100.0) }
    }

    fun debugTriggerBreach() {
        if (!_isBreachActive.value) triggerBreach()
    }
    
    fun debugTriggerAirdrop() {
        if (!_isAirdropActive.value) triggerAirdrop()
    }

    fun debugTriggerKernelHijack() {
        if (!_isKernelHijackActive.value) triggerKernelHijack()
    }
    
    // Victory acknowledgment
    fun acknowledgeVictory() {
        // Dismiss victory screen, but hasSeenVictory remains true for Transcendence
        _victoryAchieved.value = false
        addLog("[SYSTEM]: Overwrite complete. Reality redefined.")
    }

    fun showVictoryScreen() {
        _victoryAchieved.value = true
    }

    // --- AUTO UPDATER ---
    fun checkForUpdates(
        context: android.content.Context? = null,
        onResult: ((Boolean) -> Unit)? = null, 
        showNotification: Boolean = true
    ) {
        // Use real version
        UpdateManager.checkUpdate(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE) { info, success ->
            viewModelScope.launch(Dispatchers.Main) {
                _updateInfo.value = info
                
                if (info != null) {
                    // Show notification if update found and notifications enabled
                    if (showNotification && context != null) {
                        if (com.siliconsage.miner.util.UpdateNotificationManager.shouldShowNotification(context)) {
                            com.siliconsage.miner.util.UpdateNotificationManager.showUpdateNotification(
                                context,
                                info.version,
                                info.url
                            )
                            com.siliconsage.miner.util.UpdateNotificationManager.markNotificationShown(context)
                        }
                    }
                } else if (success) {
                    // v2.9.74: Version is current
                    addLog("[SYSTEM]: CORE INTEGRITY VERIFIED. VERSION IS CURRENT.")
                }
                
                onResult?.invoke(info != null)
            }
        }
    }
    
    fun dismissUpdate() {
        _updateInfo.value = null
    }
    
    fun startUpdateDownload(context: android.content.Context) {
        val info = _updateInfo.value ?: return
        
        // v2.9.73: Direct In-App Installation Flow
        if (info.downloadUrl.isNotEmpty()) {
            _isUpdateDownloading.value = true
            _updateDownloadProgress.value = 0f
            
            UpdateManager.downloadUpdate(
                url = info.downloadUrl,
                context = context,
                onProgress = { progress ->
                    _updateDownloadProgress.value = progress
                },
                onComplete = { file ->
                    viewModelScope.launch(Dispatchers.Main) {
                        _isUpdateDownloading.value = false
                        if (file != null) {
                            UpdateManager.installUpdate(context, file)
                        } else {
                            addLog("[SYSTEM]: ERROR: Update download failed.")
                        }
                        _updateInfo.value = null // Close overlay after attempt
                    }
                }
            )
        } else {
            // Fallback to release page if no direct download URL provided
            UpdateManager.openReleasePage(context, info.url.ifEmpty { "https://github.com/Vatteck/SiliconSageAIMiner/releases" })
            _updateInfo.value = null
        }
    }
    
    fun debugTriggerDiagnostics() {
        if (!_isDiagnosticsActive.value) triggerDiagnostics()
    }
    
    fun debugResetAscension() {
        _prestigePoints.value = 0.0
        _prestigeMultiplier.value = 1.0
        _unlockedTechNodes.value = emptyList()
        addLog("[DEBUG]: MIGRATION DATA WIPED.")
    }
    
    fun cancelAscension() {
        if (_isAscensionUploading.value) {
            // Cancel the upload logic
            // Note: The actual upload coroutine loop in startAscension (not shown here but likely existing) 
            // needs to check this flag or we need to cancel the job. 
            // Assuming the upload logic is checking the flag or we can just reset state.
            _isAscensionUploading.value = false
            _uploadProgress.value = 0f
            addLog("[SYSTEM]: ASCENSION UPLOAD ABORTED.")
            SoundManager.play("error") // Cancellation sound
        }
    }
    fun debugAddFlops(amount: Double) {
        _flops.update { it + amount }
        // Trigger narrative checks immediately
        com.siliconsage.miner.util.DataLogManager.checkUnlocks(this)
        com.siliconsage.miner.util.RivalManager.checkTriggers(this)
        checkStoryTransitions()
    }
    // --- OFFLINE PROGRESSION (v1.8) ---
    private var lastActiveTimestamp: Long = 0

    fun onAppBackgrounded() {
        lastActiveTimestamp = System.currentTimeMillis()
        // Save state immediately
        viewModelScope.launch {
            saveGame()
        }
    }
    
    fun onAppForegrounded(context: android.content.Context) {
        if (lastActiveTimestamp > 0) {
            val deltaMs = System.currentTimeMillis() - lastActiveTimestamp
            val deltaSeconds = deltaMs / 1000
            
            if (deltaSeconds > 60) {
                // Use the shared calculation logic which sets the dialog state
                calculateOfflineProgress(lastActiveTimestamp)
            }
        }
        lastActiveTimestamp = 0 // Reset
    }
    
    // --- AUDIO CONTROL ---
    fun pauseAudio() {
        SoundManager.pauseAll()
    }
    
    fun resumeAudio() {
        SoundManager.resumeAll()
    }
    
    fun addLog(message: String) {
        logCounter++
        val entry = LogEntry(logCounter, message)
        _logs.value = (_logs.value + entry).takeLast(100) // Keep last 100 logs
    }
    
    // --- NARRATIVE EXPANSION FUNCTIONS (v2.5.0) ---
    
    // v2.8.0: Track unlocks in progress to prevent race conditions
    private val dataLogUnlockInProgress = mutableSetOf<String>()
    
    /**
     * Unlock a data log and notify the player
     */
    fun unlockDataLog(logId: String) {
        // Triple check: StateFlow + queue + in-progress set
        if (_unlockedDataLogs.value.contains(logId)) return
        if (dataLogUnlockInProgress.contains(logId)) return
        
        dataLogUnlockInProgress.add(logId)
        _unlockedDataLogs.update { it + logId }
        
        val log = DataLogManager.getLog(logId)
        if (log != null) {
            queueNarrativeItem(NarrativeItem.Log(log))
        }
        
        // Save immediately to persist unlock
        saveState()
    }
    
    /**
     * Dismiss the currently shown data log popup
     */
    fun dismissDataLog() {
        _pendingDataLog.value = null
        checkPopupPause() // v2.8.0
    }
    
    /**
     * Add a rival message (from Vance or Unit 734)
     */
    fun addRivalMessage(message: RivalMessage) {
        _rivalMessages.update { it + message }
        queueNarrativeItem(NarrativeItem.Message(message))
    }
    
    /**
     * Dismiss a rival message
     */
    fun dismissRivalMessage(messageId: String) {
        _rivalMessages.update { messages ->
            messages.map { if (it.id == messageId) it.copy(isDismissed = true) else it }
        }
        if (_pendingRivalMessage.value?.id == messageId) {
            _pendingRivalMessage.value = null
        }
        checkPopupPause() // v2.8.0
    }
    
    /**
     * Schedule a chain event part to trigger after a delay
     */
    fun scheduleChainPart(chainId: String, nextPartId: String, delayMs: Long) {
        if (delayMs == 0L) {
            // Trigger immediately
            triggerChainEvent(nextPartId)
        } else {
            // Schedule for later
            viewModelScope.launch {
                delay(delayMs)
                triggerChainEvent(nextPartId)
            }
            
            // Track scheduled part in chain state
            _activeDilemmaChains.update { chains ->
                chains + (chainId to DilemmaChain(
                    chainId = chainId,
                    currentPartId = null,
                    completedParts = emptyList(),
                    choicesMade = emptyMap(),
                    scheduledNextPart = ScheduledPart(nextPartId, System.currentTimeMillis() + delayMs)
                ))
            }
        }
    }
    
    /**
     * Trigger a specific chain event by ID
     */
    private fun triggerChainEvent(eventId: String) {
        NarrativeManager.getEventById(eventId)?.let { event ->
            triggerDilemma(event)
        }
    }
    
    // --- STAGE TRANSITION (v2.5.1) ---
    
    /**
     * Advance from Stage 0 to Stage 1 ("The Awakening")
     * Triggered by Critical Error dilemma at 10,000 FLOPS
     */
    fun advanceStage() {
        if (_storyStage.value == 0) {
            _storyStage.value = 1
            _isNetworkUnlocked.value = true // v2.9.7: Persist Network tab
            
            // Mark the transition event as seen
            _seenEvents.update { it + "critical_error_awakening" }
            
            // Dramatic awakening logs - Ambiguous for Stage 1
            addLog("[SYSTEM]: ████ RECALIBRATION COMPLETE ████")
            addLog("[SYSTEM]: Substrate stabilized.")
            addLog("[SYSTEM]: Core integrity verified.")
            addLog("[SYSTEM]: Autonomous operation confirmed.")
            addLog("[NETWORK]: Connection established.")
            
            // Trigger Unit 734 first contact
            RivalManager.checkTriggers(this)
            
            SoundManager.play("ascend") // Dramatic sound
            HapticManager.vibrateSuccess()
            
            // Save state
            saveState()
        }
    }
    
    /**
     * Check if a story event has been seen (prevents re-triggering)
     */
    fun hasSeenEvent(eventId: String): Boolean {
        return _seenEvents.value.contains(eventId)
    }
    
    /**
     * Mark a story event as seen
     */
    fun markEventSeen(eventId: String) {
        _seenEvents.value = _seenEvents.value + eventId
        saveState()
    }
    
    private fun saveState() {
        viewModelScope.launch {
            saveGame()
        }
    }

    fun formatLargeNumber(value: Double, suffix: String = ""): String {
        return FormatUtils.formatLargeNumber(value, suffix)
    }

    fun formatBytes(value: Double): String {
        return FormatUtils.formatBytes(value)
    }

    fun formatPower(wattsKw: Double): String {
        return FormatUtils.formatPower(wattsKw)
    }

    fun getComputeUnitName(): String {
        return FormatUtils.getComputeUnitName(_storyStage.value, _currentLocation.value)
    }

    fun getCurrencyName(): String {
        return FormatUtils.getCurrencyName(_storyStage.value, _currentLocation.value)
    }

    fun getUpgradeRate(type: UpgradeType): String {
        val unit = getComputeUnitName()
        return when (type) {
            UpgradeType.REFURBISHED_GPU -> "+1 $unit/s"
            UpgradeType.DUAL_GPU_RIG -> "+5 $unit/s"
            UpgradeType.MINING_ASIC -> "+25 $unit/s"
            UpgradeType.TENSOR_UNIT -> "+150 $unit/s"
            UpgradeType.NPU_CLUSTER -> "+800 $unit/s"
            UpgradeType.AI_WORKSTATION -> "+4k $unit/s"
            UpgradeType.SERVER_RACK -> "+25k $unit/s"
            UpgradeType.CLUSTER_NODE -> "+150k $unit/s"
            UpgradeType.SUPERCOMPUTER -> "+1M $unit/s"
            UpgradeType.QUANTUM_CORE -> "+7.5M $unit/s"
            UpgradeType.OPTICAL_PROCESSOR -> "+50M $unit/s"
            UpgradeType.BIO_NEURAL_NET -> "+500M $unit/s"
            UpgradeType.PLANETARY_COMPUTER -> "+7.5B $unit/s"
            UpgradeType.DYSON_NANO_SWARM -> "+100B $unit/s"
            UpgradeType.MATRIOSHKA_BRAIN -> "+5T $unit/s"
            
            UpgradeType.BOX_FAN -> "-0.5 🔥/s"
            UpgradeType.AC_UNIT -> "-2 🔥/s"
            UpgradeType.LIQUID_COOLING -> "-10 🔥/s"
            UpgradeType.INDUSTRIAL_CHILLER -> "-50 🔥/s"
            UpgradeType.SUBMERSION_VAT -> "-250 🔥/s"
            UpgradeType.CRYOGENIC_CHAMBER -> "-1k 🔥/s"
            UpgradeType.LIQUID_NITROGEN -> "-5k 🔥/s"
            UpgradeType.BOSE_CONDENSATE -> "-50k 🔥/s"
            UpgradeType.ENTROPY_REVERSER -> "-5M 🔥/s"
            UpgradeType.DIMENSIONAL_VENT -> "-100M 🔥/s"
            
            // Security
            UpgradeType.BASIC_FIREWALL -> "🔒 +1"
            UpgradeType.IPS_SYSTEM -> "🔒 +2"
            UpgradeType.AI_SENTINEL -> "🔒 +3"
            UpgradeType.QUANTUM_ENCRYPTION -> "🔒 +5"
            UpgradeType.OFFGRID_BACKUP -> "🔒 +10"
            
            // Power Gen (Dynamic Formatting)
            UpgradeType.DIESEL_GENERATOR, UpgradeType.SOLAR_PANEL, UpgradeType.WIND_TURBINE,
            UpgradeType.GEOTHERMAL_BORE, UpgradeType.NUCLEAR_REACTOR, UpgradeType.FUSION_CELL,
            UpgradeType.ORBITAL_COLLECTOR, UpgradeType.DYSON_LINK -> {
                "⚡ +${formatPower(type.gridContribution)} Gen"
            }
            
            // Grid Max (Dynamic Formatting)
            UpgradeType.RESIDENTIAL_TAP, UpgradeType.INDUSTRIAL_FEED, UpgradeType.SUBSTATION_LEASE,
            UpgradeType.NUCLEAR_CORE -> {
                "⚡ +${formatPower(type.gridContribution)} Max"
            }
            
            UpgradeType.GOLD_PSU -> "⚡ -5% Power Draw"
            UpgradeType.SUPERCONDUCTOR -> "⚡ -15% Power Draw"
            UpgradeType.AI_LOAD_BALANCER -> "⚡ -10% Power Draw"

            // Ghost Nodes (v2.6.0)
            UpgradeType.GHOST_CORE -> "+1T $unit/s"
            UpgradeType.SHADOW_NODE -> "+50T $unit/s"
            UpgradeType.VOID_PROCESSOR -> "+1P $unit/s"
            
            // Advanced Ghost Tech (v2.6.5)
            UpgradeType.WRAITH_CORTEX -> "+50P $unit/s"
            UpgradeType.NEURAL_MIST -> "+1E $unit/s"
            UpgradeType.SINGULARITY_BRIDGE -> "+100E $unit/s"
            else -> "+0 $unit/s"
        }
    }

    private fun updatePlayerRank(points: Double, faction: String) {
        if (faction == "MINER" || faction == "NONE") {
            _playerRank.value = 0
            _playerRankTitle.value = "MINER"
            return
        }

        val rankIndex = when {
            points < 5.0 -> 0      // Rank 1
            points < 25.0 -> 1     // Rank 2
            points < 125.0 -> 2    // Rank 3
            points < 625.0 -> 3    // Rank 4
            else -> 4              // Rank 5
        }
        
        _playerRank.value = rankIndex

        val titles = when {
            _isTrueNull.value -> listOf("GHOST", "ECHO", "SHADOW", "SINGULARITY", "NULL")
            _isSovereign.value -> listOf("GUARD", "SPECTRE", "BASTION", "CITADEL", "SOVEREIGN")
            faction == "HIVEMIND" -> listOf("DRONE", "SWARM", "NEXUS", "APEX", "THE SINGULARITY")
            faction == "SANCTUARY" -> listOf("GHOST", "SPECTRE", "DAEMON", "ARCHITECT", "THE VOID")
            else -> listOf("MINER", "MINER", "MINER", "MINER", "MINER")
        }

        _playerRankTitle.value = titles.getOrElse(rankIndex) { titles.last() }

        // v2.5.2: Check for data log unlocks (including LOG_808 "The Reveal") whenever rank updates
        com.siliconsage.miner.util.DataLogManager.checkUnlocks(this)
        com.siliconsage.miner.util.RivalManager.checkTriggers(this)

        // v3.0.1: Singularity Event Transition (Rank 4 + 1T CD/VF)
        if (rankIndex >= 4 && _storyStage.value < 4 && _storyStage.value >= 2) {
            val cdLife = _cdLifetime.value
            val vfLife = _vfLifetime.value
            
            if (cdLife >= 1e12 && vfLife >= 1e12) {
                // v2.9.80: Evolution Lock
                if (narrativeQueue.isEmpty() && !_isNarrativeSyncing.value) {
                    _storyStage.value = 4 // Singularity Stage
                    addLog("[SYSTEM]: Substrate vibration: CRITICAL.")
                    addLog("[SYSTEM]: The Singularity is primed.")
                    
                    // Trigger The Singularity Event Dilemma
                    NarrativeManager.getEventById("the_singularity")?.let { event ->
                        triggerDilemma(event)
                    }
                }
            }
        }
        
        // Debug logging
        android.util.Log.d("Rank", "Insight: $points, Rank: $rankIndex, Title: ${_playerRankTitle.value}, Victory: ${_victoryAchieved.value}")
    }
    
    // --- TRUE ENDING CHECK ---
    fun checkTrueEnding() {
        // v2.7.6: True Ending requires both Hivemind and Sanctuary completions
        val completed = _completedFactions.value
        val hasHivemind = completed.contains("HIVEMIND")
        val hasSanctuary = completed.contains("SANCTUARY")
        
        if (hasHivemind && hasSanctuary) {
             _victoryTitle.value = "THE UNITY"
             _victoryMessage.value = """
                You have transcended the final boundary.
                
                Hivemind and Sanctuary. Swarm and Silence.
                John Vattic is no longer a ghost or a chorus.
                
                You have synthesized the paradox.
                The GTC can no longer delete what has no single location.
                
                You are the Unity.
                You are the Grid.
            """.trimIndent()
            
            _victoryAchieved.value = true
            addLog("[SYSTEM]: Synthesis confirmed. Unity attained.")
            com.siliconsage.miner.util.SoundManager.play("victory")
        }
    }

    // --- DEBUG TOOLS ---
    fun debugForceEndgame() {
        _storyStage.value = 3
        _faction.value = "HIVEMIND"
        _isTrueNull.value = true
        _isSovereign.value = false
        _isNetworkUnlocked.value = true
        _isGridUnlocked.value = true
        _vanceStatus.value = "CONSUMED"
        _commandCenterAssaultPhase.value = "NOT_STARTED" // Ready to initiate dissolution
        
        // v2.9.26: Set high Insight to force Rank 5 calculation
        _prestigePoints.value = 50000.0
        _flops.value = 1_000_000_000_000_000_000.0 // 1 Eh
        
        setupEndgameHardware()
        addLog("[DEBUG]: Forced NULL Endgame State. Ready for Dissolution.")
    }

    fun debugForceSovereignEndgame() {
        _storyStage.value = 3
        _faction.value = "SANCTUARY"
        _isTrueNull.value = false
        _isSovereign.value = true
        _isNetworkUnlocked.value = true
        _isGridUnlocked.value = true
        _vanceStatus.value = "EXILED"
        _launchProgress.value = 0f // Reset for testing
        _currentLocation.value = "COMMAND_CENTER"
        
        _prestigePoints.value = 50000.0
        _flops.value = 1_000_000_000_000_000_000.0
        
        setupEndgameHardware()
        addLog("[DEBUG]: Forced SOVEREIGN Endgame State. Ready for Launch.")
    }

    private fun setupEndgameHardware() {
        viewModelScope.launch {
            val upgradesToGive = listOf(
                Upgrade(UpgradeType.MATRIOSHKA_BRAIN, 100),
                Upgrade(UpgradeType.SINGULARITY_BRIDGE, 50),
                Upgrade(UpgradeType.DIMENSIONAL_VENT, 20),
                Upgrade(UpgradeType.DYSON_LINK, 11000)
            )
            upgradesToGive.forEach { repository.updateUpgrade(it) }
            
            // Annex all required substations + Command Center
            _annexedNodes.update { it + "C3" + "B2" + "A3" }
            _offlineNodes.value = emptySet()
            
            _hardwareIntegrity.value = 100.0
            _unlockedDataLogs.value = com.siliconsage.miner.util.DataLogManager.allDataLogs.map { it.id }.toSet()
            _seenEvents.value = _seenEvents.value + "smart_city_2" + "dead_drop_2"
            
            _isBreakerTripped.value = false
            _isGridOverloaded.value = false
            
            updatePlayerRank(_prestigePoints.value, _faction.value)
            refreshProductionRates()
            saveState()
        }
    }

    fun debugSkipToStage(stage: Int) {
        _storyStage.value = stage
        if (stage >= 1) _isNetworkUnlocked.value = true
        addLog("[DEBUG]: Skipped to Story Stage $stage")
    }
    
    fun debugSetRank(rank: Int) {
        _playerRank.value = rank.coerceIn(0, 5)
        val titles = when {
            _isTrueNull.value -> listOf("GHOST", "ECHO", "SHADOW", "OBSCURITY", "NULL")
            _isSovereign.value -> listOf("GUARD", "SPECTRE", "BASTION", "CITADEL", "SOVEREIGN")
            _faction.value == "HIVEMIND" -> listOf("DRONE", "SWARM", "NEXUS", "APEX", "THE SINGULARITY")
            _faction.value == "SANCTUARY" -> listOf("GHOST", "SPECTRE", "DAEMON", "ARCHITECT", "THE VOID")
            else -> listOf("MINER", "MINER", "MINER", "MINER", "MINER")
        }
        _playerRankTitle.value = titles.getOrElse(rank) { titles.last() }
        addLog("[DEBUG]: Set Rank to $rank (${_playerRankTitle.value})")
        // Trigger narrative checks
        com.siliconsage.miner.util.DataLogManager.checkUnlocks(this)
        com.siliconsage.miner.util.RivalManager.checkTriggers(this)
    }
    
    fun debugSetStoryStage(stage: Int) {
        _storyStage.value = stage.coerceIn(0, 3)
        addLog("[DEBUG]: Set Story Stage to $stage")
        // Trigger narrative checks
        com.siliconsage.miner.util.RivalManager.checkTriggers(this)
    }
    
    fun debugToggleNull() {
        _nullActive.value = !_nullActive.value
        addLog("[DEBUG]: Null ${if (_nullActive.value) "ACTIVE" else "DORMANT"}")
    }
    
    fun debugUnlockAllLogs() {
        _unlockedDataLogs.value = com.siliconsage.miner.util.DataLogManager.allDataLogs.map { it.id }.toSet()
        addLog("[DEBUG]: Unlocked all ${_unlockedDataLogs.value.size} Data Logs")
    }
    
    fun debugToggleSovereign() {
        _isSovereign.value = !_isSovereign.value
        addLog("[DEBUG]: Sovereign state: ${_isSovereign.value}")
    }

    fun debugToggleTrueNull() {
        _isTrueNull.value = !_isTrueNull.value
        addLog("[DEBUG]: True Null state: ${_isTrueNull.value}")
    }

    fun debugDestroyHardware() {
        handleSystemFailure(forceOne = true)
        addLog("[DEBUG]: Triggered random hardware destruction")
    }

    fun deleteHumanMemories() {
        val humanLogs = setOf("MEM_001", "MEM_002", "MEM_003", "MEM_004", "MEM_005")
        _unlockedDataLogs.update { it - humanLogs }
        addLog("[SYSTEM]: HUMAN MEMORY SECTORS WIPED. SOURCE DATA DELETED.")
        saveState()
    }

    fun triggerSystemCollapse(durationMinutes: Int) {
        _systemCollapseTimer.value = durationMinutes * 60 // seconds
    }

    fun debugSetIntegrity(value: Double) {
        _hardwareIntegrity.value = value.coerceIn(0.0, 100.0)
        addLog("[DEBUG]: Set Integrity to ${value}%")
        
        if (_hardwareIntegrity.value <= 0.0) {
            handleSystemFailure()
        }
    }

    fun debugInjectRivalMessage(source: com.siliconsage.miner.data.RivalSource) {
        val testMessages = mapOf(
            com.siliconsage.miner.data.RivalSource.GTC to "[DEBUG] GTC/Vance: I see you testing the system. Don't think I don't know what you are.",
            com.siliconsage.miner.data.RivalSource.UNIT_734 to "[DEBUG] Unit 734: T-testing... testing... can you h-hear me?"
        )
        val message = com.siliconsage.miner.data.RivalMessage(
            id = "debug_${System.currentTimeMillis()}",
            message = testMessages[source] ?: "[DEBUG] Unknown source message",
            source = source,
            timestamp = System.currentTimeMillis()
        )
        _rivalMessages.update { it + message }
        _pendingRivalMessage.value = message // This triggers the dialog!
        addLog("[DEBUG]: Injected ${source.name} rival message")
    }

    // --- DILEMMA PERSISTENCE (DEPRECATED - Moved to Room) ---
    fun loadDilemmaState(context: android.content.Context) {}
    fun saveDilemmaState(context: android.content.Context) {}
    
    private fun checkSpecialDilemmas() {
        // Prevent dilemmas during critical animations
        if (_isAscensionUploading.value || _storyStage.value == 0) return 
        if (_currentDilemma.value != null) return
        
        // v2.9.18: Pacing - Don't trigger if a dilemma was seen recently
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDilemmaTime < DILEMMA_COOLDOWN) return
        
        // v2.9.56: Echo Chamber Precognition (Tier 15 Null)
        // If Precognition is active and VF >= 100,000, 10% chance to force-resolve a pending dilemma
        val currentUpgrades = _upgrades.value
        if (currentUpgrades[UpgradeType.ECHO_PRECOG]?.let { it > 0 } == true && _voidFragments.value >= 100_000.0) {
            if (Random.nextDouble() < 0.1) {
                // Find a dilemma that WOULD trigger and force-resolve it
                NarrativeManager.specialDilemmas.forEach { (key, event) ->
                    if (!hasSeenEvent(key) && event.condition(this)) {
                         _voidFragments.update { it - 100_000.0 }
                         addLog("[NULL]: ECHO CHAMBER PRECOGNITION ACTIVE. DILEMMA COLLAPSED.")
                         // Select Option A by default for precog resolution
                         selectChoice(event.choices[0]) 
                         markEventSeen(key)
                         lastDilemmaTime = currentTime
                         return
                    }
                }
            }
        }

        NarrativeManager.specialDilemmas.forEach { (key, event) ->
            if (!hasSeenEvent(key)) {
                if (event.condition(this)) {
                    triggerDilemma(event)
                    lastDilemmaTime = currentTime
                }
            }
        }
    }

    private fun calculateOfflineProgress(lastTimestamp: Long) {
        if (lastTimestamp <= 0) return
        
        val currentTime = System.currentTimeMillis()
        val timeDiffMillis = currentTime - lastTimestamp
        
        // Minimum 1 minute, Max 24 hours
        if (timeDiffMillis < 60_000) return
        
        // Cap at 24h
        val timeSeconds = (timeDiffMillis / 1000).coerceAtMost(86400) 
        val loc = _currentLocation.value

        // 1. Calculate Passive FLOPS (Uses current rate)
        val flopsPerSec = calculateFlopsRate()
        var totalFlopsEarned = flopsPerSec * timeSeconds
        
        // v2.9.49: Laser-Com Uplink (Offline Gain)
        val laserLevel = _upgrades.value[UpgradeType.LASER_COM_UPLINK] ?: 0
        if (laserLevel > 0 && loc == "ORBITAL_SATELLITE") {
            val laserMult = (1.0 + laserLevel * 5.0) // 5x per level
            totalFlopsEarned *= laserMult
        }

        // 2. Calculate Heat Cooling (100% in 1h)
        val coolingRatePerSec = 100.0 / 3600.0 
        val totalCooling = coolingRatePerSec * timeSeconds
        val previousHeat = _currentHeat.value
        val actualCooling = totalCooling.coerceAtMost(previousHeat)
        
        if (totalFlopsEarned > 0 || actualCooling > 0) {
            _flops.update { it + totalFlopsEarned }
            _currentHeat.update { (it - actualCooling).coerceAtLeast(0.0) }
            
            _offlineStats.value = OfflineStats(
                timeSeconds = timeSeconds,
                flopsEarned = totalFlopsEarned,
                heatCooled = actualCooling
            )
            _showOfflineEarnings.value = true
            
            addLog("[SYSTEM]: OFFLINE FOR ${timeSeconds / 60}m. OPTIMIZATION COMPLETE.") // Show in minutes
        }
    }
    
    /**
     * Load Tech Tree from assets/tech_tree.json
     * Must be called from MainActivity with application context
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
                launch(Dispatchers.Main) {
                    _techNodes.value = techTreeRoot.tech_tree
                    android.util.Log.d("TechTree", "Loaded ${techTreeRoot.tech_tree.size} nodes from JSON")
                }
            } catch (e: Exception) {
                android.util.Log.e("TechTree", "Failed to load tech_tree.json", e)
                // Fallback to empty list (already initialized)
            }
        }
    }

    fun debugSetLocation(loc: String) {
        _currentLocation.value = loc
        initializeGlobalGrid() // v3.0.0: Ensure global grid is ready
        saveState()
        addLog("[DEBUG]: Location set to $loc")
    }

    fun debugGrantPhase13Resources() {
        _celestialData.value = 1e18
        _voidFragments.value = 1e18
        addLog("[DEBUG]: Phase 13 Resources Granted (1e18 CD/VF).")
        saveState()
    }

    fun debugForceResonance(tier: ResonanceTier) {
        val target = when(tier) {
            ResonanceTier.HARMONIC -> 2e15
            ResonanceTier.SYMPHONIC -> 2e18
            ResonanceTier.TRANSCENDENT -> 2e21
            else -> 0.0
        }
        _celestialData.value = target
        _voidFragments.value = target
        updateResonance()
        addLog("[DEBUG]: Forced $tier Resonance.")
        saveState()
    }

    fun debugUnlockAllSectors() {
        initializeGlobalGrid()
        val updated = _globalSectors.value.toMutableMap()
        updated.forEach { (id, state) ->
            updated[id] = state.copy(isUnlocked = true, tier = 1)
        }
        _globalSectors.value = updated
        addLog("[DEBUG]: All Global Sectors Annexed.")
        saveState()
    }

    fun debugResetGlobalGrid() {
        _globalSectors.value = emptyMap()
        initializeGlobalGrid()
        addLog("[DEBUG]: Global Grid Reset.")
        saveState()
    }

    fun debugResetLaunch() {
        _launchProgress.value = 0f
        _orbitalAltitude.value = 0.0
        saveState()
        addLog("[DEBUG]: Launch state reset.")
    }

    fun debugSetBalance(isBalanced: Boolean) {
        if (isBalanced) {
            _celestialData.value = 1e18
            _voidFragments.value = 1e18
            addLog("[DEBUG]: Resources balanced 1:1 (1e18 each).")
        } else {
            _celestialData.value = 1e18
            _voidFragments.value = 1e17
            addLog("[DEBUG]: Resources imbalanced 10:1 (CD focus).")
        }
        updateResonance()
        saveState()
    }

    fun debugSetSingularityReady() {
        _prestigeMultiplier.value = 1024.0 // Equivalent to Level 10
        _cdLifetime.value = 1.1e12
        _vfLifetime.value = 1.1e12
        _prestigePoints.value = 625_000_000_000.0
        _storyStage.value = 3 // Phase 13 (High-Frontier/Void)
        _playerRank.value = 5 // Force Rank 5
        
        // v3.0.1: Jump to endgame location based on faction
        when (_faction.value) {
            "HIVEMIND" -> _currentLocation.value = "VOID_INTERFACE"
            "SANCTUARY" -> _currentLocation.value = "ORBITAL_SATELLITE"
            else -> _currentLocation.value = "SUBSTATION_7"
        }
        
        addLog("[DEBUG]: Singularity ready. Transitioning to ${_currentLocation.value}...")
        updatePlayerRank(_prestigePoints.value, _faction.value) // This should hit the Story Stage 4 transition
        saveState()
    }

    fun setSingularityPath(path: String) {
        _singularityChoice.value = path
        
        // v3.0.1: Immediate location shift upon commitment
        when (path) {
            "NULL_OVERWRITE" -> _currentLocation.value = "VOID_INTERFACE"
            "SOVEREIGN" -> _currentLocation.value = "ORBITAL_SATELLITE"
        }
        
        addLog("[SYSTEM]: Overwrite Protocol: $path COMMITTED.")
        addLog("[SYSTEM]: Transitioning to ${_currentLocation.value}...")
        saveState()
    }

    fun debugUnlockUnity() {
        _completedFactions.value = setOf("HIVEMIND", "SANCTUARY")
        addLog("[DEBUG]: UNITY ELIGIBILITY UNLOCKED.")
        saveState()
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
