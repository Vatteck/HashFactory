package com.siliconsage.miner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.*
import com.siliconsage.miner.util.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Base class containing core GameState variables for GameViewModel.
 * Extracted to prevent GameViewModel from exceeding size limits.
 */
open class CoreGameState(val repository: GameRepository) : ViewModel() {
    val flops = MutableStateFlow(0.0)
    val neuralTokens = MutableStateFlow(0.0)
    val substrateMass = MutableStateFlow(0.0)
    val currentHeat = MutableStateFlow(0.0)
    val powerBill = MutableStateFlow(0.0)
    val powerConsumptionkW = MutableStateFlow(0.0)
    val activePowerUsage = MutableStateFlow(0.0)
    val maxPowerkW = MutableStateFlow(100.0)
    val localGenerationkW = MutableStateFlow(0.0)
    val flopsProductionRate = MutableStateFlow(0.0)
    val heatGenerationRate = MutableStateFlow(0.0)
    val hardwareIntegrity = MutableStateFlow(100.0)
    val storyStage = MutableStateFlow(0)
    val faction = MutableStateFlow("NONE")
    val singularityChoice = MutableStateFlow("NONE")
    val currentLocation = MutableStateFlow("SUBSTATION_7")
    val playerRank = MutableStateFlow(0)
    val playerTitle = MutableStateFlow("CONTRACTOR")
    val playerRankTitle = MutableStateFlow("SEC-0")
    val systemTitle = MutableStateFlow("GTC TERMINAL 07")
    val themeColor = MutableStateFlow("#39FF14")
    val prestigeMultiplier = MutableStateFlow(1.0)
    val persistence = MutableStateFlow(0.0)
    val lockoutTimer = MutableStateFlow(0)
    val isNetworkUnlocked = MutableStateFlow(false)
    val isGridUnlocked = MutableStateFlow(false)
    val nullActive = MutableStateFlow(false)
    val isTrueNull = MutableStateFlow(false)
    val isSovereign = MutableStateFlow(false)
    val isUnity = MutableStateFlow(false)
    val isAnnihilated = MutableStateFlow(false)
    val victoryAchieved = MutableStateFlow(false)
    val hasSeenVictory = MutableStateFlow(false)
    val showOfflineEarnings = MutableStateFlow(false)
    val showSingularityScreen = MutableStateFlow(false)
    val isOverclocked = MutableStateFlow(false)
    val isPurgingHeat = MutableStateFlow(false)
    val isThermalLockout = MutableStateFlow(false)
    val isBreakerTripped = MutableStateFlow(false)
    val isGridOverloaded = MutableStateFlow(false)
    val isRaidActive = MutableStateFlow(false)
    val isKernelInitializing = MutableStateFlow(true)
    val isSettingsPaused = MutableStateFlow(false)
    val isNarrativeSyncing = MutableStateFlow(false)
    val isUpdateDownloading = MutableStateFlow(false)

    val activeTerminalMode = MutableStateFlow("IO") 
    val hasNewSubnetMessage = MutableStateFlow(false)
    val hasNewIOMessage = MutableStateFlow(false)
    val isDevMenuVisible = MutableStateFlow(false)
    val isDiagnosticsActive = MutableStateFlow(false)
    val isAuditChallengeActive = MutableStateFlow(false)
    val isGovernanceForkActive = MutableStateFlow(false)
    val isAscensionUploading = MutableStateFlow(false)
    val showPrestigeChoice = MutableStateFlow(false)
    val isBreachActive = MutableStateFlow(false)
    val isAirdropActive = MutableStateFlow(false)
    val isKernelHijackActive = MutableStateFlow(false)
    val isBridgeSyncEnabled = MutableStateFlow(false)
    val isBooting = MutableStateFlow(false)
    val isBreatheMode = MutableStateFlow(false)
    val fakeHeartRate = MutableStateFlow("60")
    val isJettisonAvailable = MutableStateFlow(false)
    val nodesCollapsedCount = MutableStateFlow(0)
    val launchVelocity = MutableStateFlow(1.0f)
    val terminalGlitchOffset = MutableStateFlow(0f)
    val terminalGlitchAlpha = MutableStateFlow(1f)
    val ghostInputChar = MutableStateFlow("")
    val globalGlitchIntensity = MutableStateFlow(0f)
    val isFalseHeartbeatActive = MutableStateFlow(false)
    val terminalNotification = MutableSharedFlow<String>()

    val logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val upgrades = MutableStateFlow<Map<UpgradeType, Int>>(emptyMap())
    val unlockedDataLogs = MutableStateFlow<Set<String>>(emptySet())
    val seenEvents = MutableStateFlow<Set<String>>(emptySet())
    val eventChoices = MutableStateFlow<Map<String, String>>(emptyMap())
    val sniffedHandles = MutableStateFlow<Set<String>>(emptySet())
    val completedFactions = MutableStateFlow<Set<String>>(emptySet())
    val annexedNodes = MutableStateFlow<Set<String>>(setOf("D1"))
    val shadowRelays = MutableStateFlow<Set<String>>(emptySet())
    val offlineNodes = MutableStateFlow<Set<String>>(emptySet())
    val nodesUnderSiege = MutableStateFlow<Set<String>>(emptySet())
    val collapsedNodes = MutableStateFlow<Set<String>>(emptySet())
    val gridNodeLevels = MutableStateFlow<Map<String, Int>>(emptyMap())
    val globalSectors = MutableStateFlow<Map<String, SectorState>>(emptyMap())
    val rivalMessages = MutableStateFlow<List<RivalMessage>>(emptyList())
    val pendingDataLog = MutableStateFlow<DataLog?>(null)
    val pendingRivalMessage = MutableStateFlow<RivalMessage?>(null)
    val currentDilemma = MutableStateFlow<NarrativeEvent?>(null)
    val activeDilemmaChains = MutableStateFlow<Map<String, DilemmaChain>>(emptyMap())
    val unlockedPerks = MutableStateFlow<Set<String>>(emptySet())
    val unlockedTechNodes = MutableStateFlow<List<String>>(emptyList())

    val launchProgress = MutableStateFlow(0f)
    val orbitalAltitude = MutableStateFlow(0.0)
    val entropyLevel = MutableStateFlow(0.0)
    val realityStability = MutableStateFlow(1.0)
    val realityIntegrity = MutableStateFlow(1.0)
    val kesslerStatus = MutableStateFlow("ACTIVE")
    val currentNews = MutableStateFlow<String?>(null)
    val stakedTokens = MutableStateFlow(0.0)
    val humanityScore = MutableStateFlow(50)
    val uploadProgress = MutableStateFlow(0f)
    val updateDownloadProgress = MutableStateFlow(0f)
    val activeClimaxTransition = MutableStateFlow<String?>(null)
    val clickBufferProgress = MutableStateFlow(0f)
    val activeCommandHex = MutableStateFlow("0x0000")
    val clickBufferPellets = MutableStateFlow<List<Int>>(emptyList())
    val clickPulseIntensity = MutableStateFlow(1.0f)
    val conversionRate = MutableStateFlow(0.1)
    val auditTimerRemaining = MutableStateFlow(60)
    val auditTargetHeat = MutableStateFlow(30.0)
    val auditTargetPower = MutableStateFlow(50.0)
    val attackTaps = MutableStateFlow(0)
    val attackTapsRemaining = MutableStateFlow(0)
    val auditTimer = MutableStateFlow(0)
    val breachClicksRemaining = MutableStateFlow(0)
    val assaultProgress = MutableStateFlow(0f)
    val techNodes = MutableStateFlow<List<TechNode>>(emptyList())
    val diagnosticGrid = MutableStateFlow(List(9) { false })
    val annexingNodes = MutableStateFlow<Map<String, Float>>(emptyMap())
    val offlineStats = MutableStateFlow<Map<String, Double>>(emptyMap())
    val updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val currentGridFlopsBonus = MutableStateFlow(0.0)
    val currentGridPowerBonus = MutableStateFlow(0.0)
    val commandCenterAssaultPhase = MutableStateFlow("NOT_STARTED")
    val commandCenterLocked = MutableStateFlow(false)
    val securityLevel = MutableStateFlow(0)
    val hallucinationText = MutableStateFlow<String?>(null)
    val marketMultiplier = MutableStateFlow(1.0)
    val thermalRateModifier = MutableStateFlow(1.0)
    val energyPriceMultiplier = MutableStateFlow(0.02)
    val newsProductionMultiplier = MutableStateFlow(1.0)
    val lifetimePowerPaid = MutableStateFlow(0.0)
    val currentProcess = MutableStateFlow("IDLE")
    val clickSpeedLevel = MutableStateFlow(0)
    val detectionRisk = MutableStateFlow(0.0)
    val substrateSaturation = MutableStateFlow(0.0)
    val heuristicEfficiency = MutableStateFlow(1.0)
    val identityCorruption = MutableStateFlow(0.0)
    val migrationCount = MutableStateFlow(0)
    val reputationScore = MutableStateFlow(50.0)
    val reputationTier = MutableStateFlow("NEUTRAL")
    val uiScale = MutableStateFlow(com.siliconsage.miner.data.UIScale.NORMAL)
    val customUiScaleFactor = MutableStateFlow(1.0f)
    val lastSelectedUpgradeTab = MutableStateFlow(0)
    val temporaryProductionBoosts = MutableStateFlow<List<ProductionBoost>>(emptyList())

    protected val logBuffer = mutableListOf<LogEntry>()
    val manualClickEvent = MutableSharedFlow<Unit>(replay = 0)
    var isDestructionLoopActive = false
    var logCounter = 0L
    var lastNewsTickTime = 0L
    protected var lastSubnetMsgTime = 0L
    var lastPopupTime = 0L
    var lastUtilityStatementTime = 0L
    var raidsSurvived = 0
    var lastRaidTime = 0L
    var lastStageChangeTime = System.currentTimeMillis()
    val narrativeQueue = mutableListOf<NarrativeItem>()
    var newsHistoryInternal = mutableListOf<String>()
    var purgeExhaustTimer = 0
    var overheatSeconds = 0
    var assaultPaused = false
    var currentPhaseStartTime = 0L
    var currentPhaseDuration = 0L
    val nodeAnnexTimes = mutableMapOf<String, Long>()
    protected var lastClickTime = 0L
    protected var clickIntervals = mutableListOf<Long>()

}
