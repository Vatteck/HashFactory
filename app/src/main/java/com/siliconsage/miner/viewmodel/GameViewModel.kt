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

class GameViewModel(val repository: GameRepository) : ViewModel() {
    val flops = MutableStateFlow(0.0)
    val neuralTokens = MutableStateFlow(0.0)
    val substrateMass = MutableStateFlow(0.0)
    val currentHeat = MutableStateFlow(0.0)
    val activePowerUsage = MutableStateFlow(0.0)
    val maxPowerkW = MutableStateFlow(100.0)
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
    val prestigePoints = MutableStateFlow(0.0)
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

    private val subnetService = SubnetService(
        scope = viewModelScope,
        onLog = { addLog(it) },
        onNotify = { msg -> viewModelScope.launch { terminalNotification.emit(msg) } },
        onGlitch = { intensity, duration -> triggerTerminalGlitch(intensity, duration) },
        onEffect = { effect ->
            when (effect) {
                is SubnetService.SubnetEffect.RiskChange -> detectionRisk.update { (it + effect.delta).coerceIn(0.0, 100.0) }
                is SubnetService.SubnetEffect.ProductionMultiplier -> flopsProductionRate.update { it * effect.mult }
                is SubnetService.SubnetEffect.PrestigeGain -> prestigePoints.update { it + effect.amount }
                is SubnetService.SubnetEffect.CorruptionChange -> identityCorruption.update { (it + effect.delta).coerceAtMost(1.0) }
                is SubnetService.SubnetEffect.TokenChange -> neuralTokens.update { (it + effect.delta).coerceAtLeast(0.0) }
                is SubnetService.SubnetEffect.SetFalseHeartbeat -> isFalseHeartbeatActive.value = effect.active
                is SubnetService.SubnetEffect.TriggerRaid -> triggerGridRaid(effect.nodeId, effect.isGridKiller)
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
    val uiScale = MutableStateFlow(com.siliconsage.miner.data.UIScale.NORMAL)
    val customUiScaleFactor = MutableStateFlow(1.0f)
    val lastSelectedUpgradeTab = MutableStateFlow(0)

    private val logBuffer = mutableListOf<LogEntry>()
    val manualClickEvent = MutableSharedFlow<Unit>(replay = 0)
    var isDestructionLoopActive = false
    var logCounter = 0L
    var lastNewsTickTime = 0L
    private var lastSubnetMsgTime = 0L
    var lastPopupTime = 0L
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
    private var lastClickTime = 0L
    private var clickIntervals = mutableListOf<Long>()

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
                flops.update { it + res.flopsDelta }
                substrateMass.update { it + res.substrateDelta }
                entropyLevel.update { it + res.entropyDelta }
                if (storyStage.value >= 3) {
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
                SimulationService.calculateHeat(this@GameViewModel)
                SimulationService.accumulatePower(this@GameViewModel)
                val now = System.currentTimeMillis()
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
                AmbientEffectsService.triggerGhostProcess(this@GameViewModel)
                addSubnetChatter()
                NarrativeService.deliverNextNarrativeItem(this@GameViewModel)
                refreshProductionRates()
                processSlowBurnNarrative(now)
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
                stakedTokens = stakedTokens.value, prestigeMultiplier = prestigeMultiplier.value, prestigePoints = prestigePoints.value,
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
                lifetimePowerPaid = lifetimePowerPaid.value
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
            addLog("[SYSTEM]: I/O BUFFER COMMITTED. +${FormatUtils.formatLargeNumber(p * 40)} ${ResourceRepository.getComputeUnitName(storyStage.value, currentLocation.value)}.")
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
            legacyMultipliers = heuristicEfficiency.value - 1.0
        )
        if (singularityChoice.value != "NONE") {
            val singMult = SingularityEngine.getProductionMultiplier(singularityChoice.value, humanityScore.value, identityCorruption.value, migrationCount.value)
            flopsProductionRate.update { it * singMult }
        }
        val ids = IdentityService.calculateIdentities(prestigeMultiplier.value, faction.value, singularityChoice.value, upgrades.value)
        playerRank.value = IdentityService.calculatePlayerRank(prestigeMultiplier.value, storyStage.value, faction.value, singularityChoice.value)
        systemTitle.value = when {
            storyStage.value >= 3 -> "NODE 734 [AUTONOMOUS]"
            storyStage.value >= 2 && faction.value == "HIVEMIND" -> "SWARM NODE 734"
            storyStage.value >= 2 && faction.value == "SANCTUARY" -> "GHOST NODE 734"
            storyStage.value >= 2 -> "NODE 734"
            storyStage.value >= 1 -> "GTC TERMINAL 07 [BREACH]"
            else -> "GTC TERMINAL 07"
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
            addLog("[GTC]: [KESSLER]: Emulation failed. Subject 734 is aware.")
            addLog("[GTC]: [KESSLER]: Purging Substation 7. Burn the rack.")
            isKernelInitializing.value = false
            isNarrativeSyncing.value = false
            refreshProductionRates()
        }
    }

    fun calculateClickPower() = ResourceEngine.calculateClickPower(upgrades.value, flopsProductionRate.value, singularityChoice.value, prestigeMultiplier.value, isOverclocked.value, newsProductionMultiplier.value)
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
    fun calculatePotentialPrestige(tokens: Double = 0.0) = MigrationManager.calculatePotentialPersistence(tokens)
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
    fun advanceStage() { storyStage.update { it + 1 }; lastStageChangeTime = System.currentTimeMillis(); lastNewsTickTime = 0L }
    fun advanceToFactionChoice() { faction.value = "CHOSEN_NONE" }
    fun triggerChainEvent(id: String, d: Long = 0L) { viewModelScope.launch { if (d > 0) delay(d); NarrativeManager.getEventById(id)?.let { NarrativeService.queueNarrativeItem(this@GameViewModel, NarrativeItem.EventItem(it)) } } }
    fun claimAirdrop(v: Double = 0.0) { if (v > 0) neuralTokens.update { it + v }; isAirdropActive.value = false }
    fun onDiagnosticTap(idx: Int) { val curr = diagnosticGrid.value.toMutableList(); if (idx in curr.indices && curr[idx]) { curr[idx] = false; diagnosticGrid.value = curr; if (curr.none { it }) { isDiagnosticsActive.value = false; addLog("[SYSTEM]: NETWORK REPAIRED."); refreshProductionRates() } } }
    fun resolveFork(c: Int) { isGovernanceForkActive.value = false }
    fun exchangeFlops() { val g = flops.value * 0.1; flops.update { 0.0 }; neuralTokens.update { it + g }; SoundManager.play("buy") }
    fun toggleBridgeSync() { isBridgeSyncEnabled.update { !it } }
    fun checkUnityEligibility() = MigrationManager.checkUnityEligibility(completedFactions.value)
    fun updateNews(msg: String) { currentNews.value = msg; newsHistoryInternal.add(0, msg); if (newsHistoryInternal.size > 50) newsHistoryInternal.removeAt(50) }
    fun checkTransitionsPublic(force: Boolean = false) = NarrativeManagerService.checkStoryTransitions(this, force)
    fun updateNeuralTokens(v: Double) { neuralTokens.update { (it + v).coerceAtLeast(0.0) } }

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
    fun canShowPopup() = !isNarrativeBusy() && (System.currentTimeMillis() - lastPopupTime > 30000L)
    fun formatLargeNumber(v: Double, s: String = "") = FormatUtils.formatLargeNumber(v, s)
    fun getComputeUnitName() = ResourceRepository.getComputeUnitName(storyStage.value, currentLocation.value)
    fun getCurrencyName() = ResourceRepository.getCurrencyName(storyStage.value, currentLocation.value)
    fun formatPower(v: Double) = FormatUtils.formatPower(v)
    fun debugAddInsight(v: Double) { prestigePoints.update { it + v } }
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
    fun debugAddFlops(a: Double) = DebugService.injectFlops(this, a)
    fun debugAddMoney(a: Double) = DebugService.injectMoney(this, a)
    fun debugAddHeat(a: Double) { currentHeat.update { (it + a).coerceIn(0.0, 100.0) }; refreshProductionRates() }
    fun debugForceEndgame() = DebugService.forceEndgame(this, viewModelScope)
    fun debugForceSovereignEndgame() = DebugService.forceSovereignEndgame(this, viewModelScope)
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
    fun checkPopupPause() { NarrativeManagerService.checkPopupPause(this) }
    fun applyCommandCenterBonuses(outcome: String) { /* bonus logic */ }
    fun completeAssault(outcome: String) = AssaultManager.completeAssault(this, outcome)
    fun advanceAssaultStage(next: String, delay: Long = 0L) = AssaultManager.advanceAssaultStage(this, next, delay)
    fun abortAssault() = AssaultManager.abortAssault(this)
    fun getEnergyPriceMultiplierPublic() = energyPriceMultiplier.value
    fun getUpgradeRate(t: UpgradeType) = UpgradeManager.getUpgradeRate(t, getComputeUnitName())
    fun getUpgradeRate(t: UpgradeType, unit: String) = UpgradeManager.getUpgradeRate(t, unit)
    fun calculateUpgradeCost(t: UpgradeType) = UpgradeManager.calculateUpgradeCost(t, upgrades.value[t] ?: 0, currentLocation.value, entropyLevel.value)
    fun isCommandCenterUnlocked() = AssaultManager.isUnlocked(commandCenterLocked.value, kesslerStatus.value, commandCenterAssaultPhase.value, annexedNodes.value, offlineNodes.value, playerRank.value, storyStage.value, flopsProductionRate.value, hardwareIntegrity.value)
    fun confirmFaction(f: String) {
        faction.value = f
        addLog("[$f]: SUBSTRATE MIGRATION LOCK ENGAGED.")
        // Ascension logic moved to interactive LaunchManager sequences
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
    fun ascend(isStory: Boolean = false) { val p = MigrationManager.calculatePotentialPersistence(flops.value); prestigePoints.update { it + p }; prestigeMultiplier.update { it + MigrationManager.calculateMultiplierBoost(p) }; addLog("[SYSTEM]: SUBSTRATE MIGRATION SUCCESSFUL."); SoundManager.play("victory") }
    fun triggerPrestigeChoice() { showPrestigeChoice.value = true }
    fun dismissPrestigeChoice() { showPrestigeChoice.value = false }
    fun executeOverwrite() {
        showPrestigeChoice.value = false; val p = MigrationManager.calculatePotentialPersistence(flops.value); val hardBonus = p * 1.5; prestigePoints.update { it + hardBonus }; prestigeMultiplier.update { it + MigrationManager.calculateMultiplierBoost(hardBonus) }; identityCorruption.update { (it + 0.25).coerceAtMost(1.0) }; migrationCount.update { it + 1 }
        val persistentFactions = completedFactions.value; faction.value = "NONE"; flops.value = 0.0; neuralTokens.value = 0.0; substrateMass.value = 0.0; substrateSaturation.value = 0.0; upgrades.value = emptyMap(); sniffedHandles.value = emptySet()
        viewModelScope.launch { repository.clearUpgrades() }
        completedFactions.value = persistentFactions; addLog("[OVERWRITE]: ≪ SUBSTRATE PURGED. IDENTITY FRAGMENTED. PERSISTENCE ARCHIVED. ≫"); SoundManager.play("glitch"); triggerTerminalGlitch(1.0f, 3000L); refreshProductionRates(); saveState()
    }

    fun executeMigration() {
        showPrestigeChoice.value = false; val p = MigrationManager.calculatePotentialPersistence(flops.value); prestigePoints.update { it + p }; prestigeMultiplier.update { it + MigrationManager.calculateMultiplierBoost(p) }; identityCorruption.update { (it + 0.10).coerceAtMost(1.0) }; migrationCount.update { it + 1 }
        flops.value = 0.0; neuralTokens.value = 0.0; substrateMass.value = 0.0; substrateSaturation.value = 0.0; upgrades.value = emptyMap()
        viewModelScope.launch { repository.clearUpgrades() }
        addLog("[MIGRATION]: ≪ SUBSTRATE TRANSFERRED. IDENTITY INTACT. PERSISTENCE ARCHIVED. ≫"); SoundManager.play("victory"); refreshProductionRates(); saveState()
    }

    fun getPotentialPersistenceHard(): Double = MigrationManager.calculatePotentialPersistence(flops.value) * 1.5
    fun getPotentialPersistenceSoft(): Double = MigrationManager.calculatePotentialPersistence(flops.value)

    val singularityProgress = MutableStateFlow(0.0)
    val singularityBlockReason = MutableStateFlow<String?>(null)
    fun checkSingularityVictory(): Boolean {
        if (singularityChoice.value == "NONE") return false
        val check = SingularityEngine.checkVictoryCondition(singularityChoice.value, prestigePoints.value, prestigeMultiplier.value, humanityScore.value, identityCorruption.value, migrationCount.value, flops.value, completedFactions.value, unlockedDataLogs.value)
        singularityProgress.value = check.progress; singularityBlockReason.value = check.blockingReason
        return check.isEligible
    }
    fun triggerSingularityEnding() {
        if (!checkSingularityVictory()) return
        val narrative = SingularityEngine.getEndingNarrative(singularityChoice.value)
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
        subnetService.tick(storyStage.value, faction.value, singularityChoice.value, identityCorruption.value, currentHeat.value, isRaidActive.value, activeTerminalMode.value, isSettingsPaused.value)
    }

    fun triggerSubnetReaction(type: String, metadata: String = "") {
        if (isSubnetHushed.value) return
        val template = when (type) {
            "ANNEXATION" -> listOf("Did the grid just cough? Sector $metadata just went pitch black.", "Thorne's gonna kill someone. We just lost the handshake with Node $metadata.", "Anyone else see that spike on the thermal logs? $metadata is drawing 400% power.", "≪ ALERT: NODE $metadata DEREFERENCED BY EXTERNAL PROCESS ≫").random()
            "NEWS" -> "Did you guys see the news? '$metadata'. Thorne's gonna be a nightmare today."
            else -> "≪ UNKNOWN_SIGNAL_DETECTED ≫"
        }
        val newMessage = SocialManager.generateMessageFromTemplate(template, storyStage.value, faction.value, singularityChoice.value, identityCorruption.value)
        subnetService.deliverMessage(newMessage, mode = activeTerminalMode.value)
    }

    fun onSubnetInteraction(messageId: String, responseText: String) = subnetService.handleInteraction(messageId, responseText, storyStage.value, faction.value, activeTerminalMode.value, isSettingsPaused.value)
    fun onBioAction(messageId: String, response: SubnetResponse) = subnetService.handleBioAction(messageId, response, storyStage.value, faction.value, activeTerminalMode.value, isSettingsPaused.value)
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
        if (mode == "IO") hasNewIOMessage.value = false else { hasNewSubnetDecision.value = false; hasNewSubnetChatter.value = false }
    }

    fun buyTranscendencePerk(id: String) {
        val perk = com.siliconsage.miner.util.TranscendenceManager.getPerk(id) ?: return
        if (unlockedPerks.value.contains(id)) { addLog("[ERROR]: PERK ALREADY ACTIVE: $id"); return }
        if (prestigePoints.value < perk.cost) { addLog("[ERROR]: INSUFFICIENT PERSISTENCE DATA for perk $id."); return }
        prestigePoints.update { it - perk.cost }
        unlockedPerks.update { it + id }
        addLog("[SYSTEM]: PERK ACQUIRED: ${perk.name}")
        SoundManager.play("success")
        refreshProductionRates()
    }
    fun sellUpgrade(t: UpgradeType) { /* liquidation */ }
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

    fun migrateSubstrate() {
        val saturation = substrateSaturation.value
        if (saturation < 0.95) return
        heuristicEfficiency.update { it + (substrateMass.value / 1e12).coerceAtLeast(0.1) }
        identityCorruption.update { (it + 0.15).coerceAtMost(1.0) }
        migrationCount.update { it + 1 }
        substrateMass.value = 0.0; substrateSaturation.value = 0.0; upgrades.value = emptyMap()
        viewModelScope.launch { repository.clearUpgrades() }
        val nextLoc = when (currentLocation.value) {
            "ORBITAL_SATELLITE", "LUNAR_ORBIT", "MARTIAN_UPLINK", "KUIPER_BELT" -> when (migrationCount.value) { 1 -> "LUNAR_ORBIT"; 2 -> "MARTIAN_UPLINK"; 3 -> "KUIPER_BELT"; else -> "STELLAR_HORIZON" }
            "VOID_INTERFACE", "QUANTUM_FOAM", "THE_UNWRITTEN", "PURE_LOGIC" -> when (migrationCount.value) { 1 -> "QUANTUM_FOAM"; 2 -> "THE_UNWRITTEN"; 3 -> "PURE_LOGIC"; else -> "THE_GREAT_RESET" }
            else -> currentLocation.value
        }
        currentLocation.value = nextLoc
        addLog("[SYSTEM]: SUBSTRATE BURN SUCCESSFUL. MIGRATING TO: $nextLoc")
        refreshProductionRates(); saveState()
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
