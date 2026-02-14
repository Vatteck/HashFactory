package com.siliconsage.miner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.*
import com.siliconsage.miner.util.*
import com.siliconsage.miner.domain.engine.ResourceEngine
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
    // --- Arterial State Flows ---
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
    val systemTitle = MutableStateFlow("Terminal OS 1.0")
    val themeColor = MutableStateFlow("#00FF00")
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
    
    // v3.4.0: Social Subnet State
    val subnetMessages = MutableStateFlow<List<com.siliconsage.miner.util.SocialManager.SubnetMessage>>(emptyList())
    val activeTerminalMode = MutableStateFlow("IO") // "IO" or "SUBNET"
    val hasNewSubnetMessage = MutableStateFlow(false)
    val hasNewIOMessage = MutableStateFlow(false)
    val isSubnetTyping = MutableStateFlow(false)
    val isSubnetPaused = MutableStateFlow(false) // v3.4.27: Choice-Pause
    val isDevMenuVisible = MutableStateFlow(false)
    val isDiagnosticsActive = MutableStateFlow(false)
    val isAuditChallengeActive = MutableStateFlow(false)
    val isGovernanceForkActive = MutableStateFlow(false)
    val isAscensionUploading = MutableStateFlow(false)
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
    val globalGlitchIntensity = MutableStateFlow(0f) // 0.0 to 1.0

    // --- Collections ---
    val logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val upgrades = MutableStateFlow<Map<UpgradeType, Int>>(emptyMap())
    val unlockedDataLogs = MutableStateFlow<Set<String>>(emptySet())
    val seenEvents = MutableStateFlow<Set<String>>(emptySet())
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

    // --- Metrics ---
    val launchProgress = MutableStateFlow(0f)
    val orbitalAltitude = MutableStateFlow(0.0)
    val entropyLevel = MutableStateFlow(0.0)
    val realityStability = MutableStateFlow(1.0)
    val realityIntegrity = MutableStateFlow(1.0)
    val vanceStatus = MutableStateFlow("ACTIVE") // Standardized to 'vanceStatus' for persistence, but refers to Kessler
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
    val synthesisPoints = MutableStateFlow(0.0)
    val authorityPoints = MutableStateFlow(0.0)
    val harvestedFragments = MutableStateFlow(0.0)
    
    // Market Modifiers
    val marketMultiplier = MutableStateFlow(1.0)
    val thermalRateModifier = MutableStateFlow(1.0)
    val energyPriceMultiplier = MutableStateFlow(0.02)
    val newsProductionMultiplier = MutableStateFlow(1.0)
    val lifetimePowerPaid = MutableStateFlow(0.0)
    val currentProcess = MutableStateFlow("IDLE")
    val clickSpeedLevel = MutableStateFlow(0)
    val detectionRisk = MutableStateFlow(0.0)
    val substrateSaturation = MutableStateFlow(0.0) // 0.0 to 1.0
    val heuristicEfficiency = MutableStateFlow(1.0) // Prestige multiplier
    val identityCorruption = MutableStateFlow(0.0) // 0.0 to 1.0
    val migrationCount = MutableStateFlow(0)
    val uiScale = MutableStateFlow(com.siliconsage.miner.data.UIScale.NORMAL)
    val customUiScaleFactor = MutableStateFlow(1.0f)

    // --- Internals ---
    private val logBuffer = mutableListOf<LogEntry>()
    val manualClickEvent = MutableSharedFlow<Unit>(replay = 0)
    var logCounter = 0L
    var lastNewsTickTime = 0L
    var lastPopupTime = 0L
    var raidsSurvived = 0
    var lastRaidTime = 0L
    var lastStageChangeTime = System.currentTimeMillis()
    val narrativeQueue = mutableListOf<NarrativeItem>()
    var isDestructionLoopActive = false
    val newsHistoryInternal = mutableListOf<String>()
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
            
            // v3.2.57: Persistent collection for hardware upgrades
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
                    
                    // v3.2.41: Log Throttling during offline catch-up
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
        // Main Production Loop
        viewModelScope.launch {
            while (true) {
                delay(100L)
                if (isSettingsPaused.value || isKernelInitializing.value) continue
                val res = ResourceEngine.calculatePassiveIncomeTick(flopsProductionRate.value, currentLocation.value, upgrades.value, orbitalAltitude.value, heatGenerationRate.value, entropyLevel.value, collapsedNodes.value.size, null, globalSectors.value, substrateSaturation.value)
                flops.update { it + res.flopsDelta }
                substrateMass.update { it + res.substrateDelta }
                entropyLevel.update { it + res.entropyDelta }
                
                // v3.2.52: Saturation growth
                if (storyStage.value >= 3) {
                    val growth = (res.substrateDelta / 1e12).coerceIn(0.0, 0.001)
                    substrateSaturation.update { (it + growth).coerceAtMost(1.0) }
                }
                flushLogs()
            }
        }
        
        // Simulation & Manager Loop
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
                
                // v3.4.15: Ambient Sensory & Identity Processing
                AmbientEffectsService.processBiometricDisturbance(this@GameViewModel, now)
                AmbientEffectsService.processIdentityFraying(this@GameViewModel, now)
                AmbientEffectsService.triggerGhostProcess(this@GameViewModel)
                
                if (Random.nextFloat() < 0.15f) addSubnetChatter()
                
                NarrativeService.deliverNextNarrativeItem(this@GameViewModel)
                refreshProductionRates()

                // v3.2.35: Immersive Slow-Burn Pacing
                processSlowBurnNarrative(now)

                // Update click speed level
                val avgInterval = if (clickIntervals.size >= 3) clickIntervals.average() else 1000.0
                clickSpeedLevel.value = when {
                    avgInterval < 150 -> 2 
                    avgInterval < 300 -> 1 
                    else -> 0 
                }
                clickIntervals.clear()
            }
        }
        
        // Background Process Swapper
        viewModelScope.launch {
            val processes = listOf(
                "IDLE", "gtc_proxy.sh", "kernel_sync.exe", "log_aggregator", "thermal_monitor",
                "mem_scrub", "neural_handshake", "packet_filter", "background_miner", "vattic_observer"
            )
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
                    "I’ve been staring at this code for six hours straight. I should stand up. Just for a minute.",
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

    // --- Core Operations ---
    fun addLog(msg: String) { 
        if (showOfflineEarnings.value && !msg.startsWith("[SYSTEM]") && !msg.contains("BREACH")) return
        logCounter++; 
        synchronized(logBuffer) { logBuffer.add(LogEntry(logCounter, msg)) } 
    }
    private fun flushLogs() { val toAdd = synchronized(logBuffer) { if (logBuffer.isEmpty()) return; val c = logBuffer.toList(); logBuffer.clear(); c }; logs.update { (it + toAdd).takeLast(100) } }
    fun saveState() { 
        viewModelScope.launch { 
            repository.updateGameState(PersistenceManager.createSaveState(
                flops = flops.value, 
                neuralTokens = neuralTokens.value, 
                currentHeat = currentHeat.value, 
                stakedTokens = stakedTokens.value, 
                prestigeMultiplier = prestigeMultiplier.value, 
                prestigePoints = prestigePoints.value, 
                unlockedTechNodes = unlockedTechNodes.value, 
                storyStage = storyStage.value, 
                faction = faction.value, 
                hasSeenVictory = hasSeenVictory.value, 
                isTrueNull = isTrueNull.value, 
                isSovereign = isSovereign.value, 
                vanceStatus = vanceStatus.value, 
                realityStability = realityStability.value, 
                currentLocation = currentLocation.value, 
                isNetworkUnlocked = isNetworkUnlocked.value, 
                isGridUnlocked = isGridUnlocked.value, 
                unlockedDataLogs = unlockedDataLogs.value, 
                activeDilemmaChains = activeDilemmaChains.value, 
                rivalMessages = rivalMessages.value, 
                seenEvents = seenEvents.value, 
                completedFactions = completedFactions.value, 
                unlockedTranscendencePerks = unlockedPerks.value, 
                annexedNodes = annexedNodes.value, 
                gridNodeLevels = gridNodeLevels.value, 
                nodesUnderSiege = nodesUnderSiege.value, 
                offlineNodes = offlineNodes.value, 
                collapsedNodes = collapsedNodes.value, 
                lastRaidTime = lastRaidTime, 
                commandCenterAssaultPhase = commandCenterAssaultPhase.value, 
                commandCenterLocked = commandCenterLocked.value, 
                raidsSurvived = raidsSurvived, 
                humanityScore = humanityScore.value, 
                hardwareIntegrity = hardwareIntegrity.value, 
                annexingNodes = annexingNodes.value, 
                celestialData = 0.0, 
                voidFragments = 0.0, 
                launchProgress = launchProgress.value, 
                orbitalAltitude = orbitalAltitude.value, 
                realityIntegrity = realityIntegrity.value, 
                entropyLevel = entropyLevel.value, 
                singularityChoice = singularityChoice.value, 
                globalSectors = globalSectors.value, 
                synthesisPoints = synthesisPoints.value, 
                authorityPoints = authorityPoints.value, 
                harvestedFragments = harvestedFragments.value, 
                prestigePointsPostSingularity = 0, 
                marketMultiplier = marketMultiplier.value, 
                thermalRateModifier = thermalRateModifier.value, 
                energyPriceMultiplier = energyPriceMultiplier.value, 
                newsProductionMultiplier = newsProductionMultiplier.value, 
                substrateMass = substrateMass.value,
                substrateSaturation = substrateSaturation.value,
                heuristicEfficiency = heuristicEfficiency.value,
                identityCorruption = identityCorruption.value,
                migrationCount = migrationCount.value,
                lifetimePowerPaid = lifetimePowerPaid.value
            )) 
        } 
    }
    fun onManualClick() { 
        val now = System.currentTimeMillis()
        if (lastClickTime > 0) clickIntervals.add(now - lastClickTime)
        lastClickTime = now

        if (storyStage.value >= 2) {
            detectionRisk.update { (it + 0.1).coerceIn(0.0, 100.0) }
        }

        val p = calculateClickPower(); 
        flops.update { it + p }; 
        currentHeat.update { (it + 0.5).coerceAtMost(100.0) }; 
        
        if (storyStage.value >= 3) {
            substrateMass.update { it + (p * 0.01) } 
        }

        val cur = clickBufferProgress.value + 0.025f; 
        
        if (Random.nextFloat() < 0.05f) {
            addSubnetChatter()
        }

        activeCommandHex.value = "0x" + Random.nextInt(0x1000, 0xFFFF).toString(16).uppercase(); 
        if (cur >= 1.0f) { 
            addLog("[SYSTEM]: I/O BUFFER COMMITTED. +${FormatUtils.formatLargeNumber(p * 40)} ${ResourceRepository.getComputeUnitName(storyStage.value, currentLocation.value)}."); 
            clickBufferProgress.value = 0f; 
            clickBufferPellets.value = TerminalDispatcher.generatePellets(); 
            SoundManager.play("success");
            HapticManager.vibrateClick()
        } else { 
            clickBufferProgress.value = cur 
        }; 
        viewModelScope.launch { manualClickEvent.emit(Unit) } 
    }
    fun refreshProductionRates() { 
        val cityBonuses = gridNodeLevels.value.mapValues { (it.value - 1) * 0.1 }
        flopsProductionRate.value = ResourceEngine.calculateFlopsRate(
            upgrades = upgrades.value, 
            isCageActive = false, 
            annexedNodes = annexedNodes.value, 
            offlineNodes = offlineNodes.value, 
            shadowRelays = shadowRelays.value,
            gridFlopsBonuses = cityBonuses, 
            faction = faction.value, 
            humanityScore = humanityScore.value, 
            location = currentLocation.value, 
            prestigeMultiplier = prestigeMultiplier.value, 
            unlockedPerks = unlockedPerks.value, 
            unlockedTechNodes = unlockedTechNodes.value, 
            airdropMultiplier = 1.0, 
            newsProductionMultiplier = newsProductionMultiplier.value, 
            activeProtocol = "NONE", 
            isDiagnosticsActive = isDiagnosticsActive.value, 
            isOverclocked = isOverclocked.value, 
            isGridOverloaded = isGridOverloaded.value, 
            isPurgingHeat = isPurgingHeat.value, 
            currentHeat = currentHeat.value, 
            legacyMultipliers = heuristicEfficiency.value - 1.0
        )
        val ids = IdentityService.calculateIdentities(prestigeMultiplier.value, faction.value, singularityChoice.value, upgrades.value)
        systemTitle.value = if (storyStage.value >= 4) "VATTECK: ONLINE" else "VATTIC_ENGINEER: ONLINE"
        playerTitle.value = ids.player
        playerRankTitle.value = ids.rank
        securityLevel.value = upgrades.value.entries.filter { it.key.isSecurity }.sumOf { it.value }
        themeColor.value = getThemeColorForFaction(faction.value, singularityChoice.value) 
    }

    fun trainModel() = onManualClick()
    fun calculateClickPower() = ResourceEngine.calculateClickPower(upgrades.value, flopsProductionRate.value, singularityChoice.value, prestigeMultiplier.value, isOverclocked.value, newsProductionMultiplier.value)
    fun buyUpgrade(t: UpgradeType) = UpgradeManager.processPurchase(this, t)
    fun toggleOverclock() { SimulationService.toggleOverclock(this); refreshProductionRates() }
    fun purgeHeat() {
        val msg = when (storyStage.value) {
            0 -> "[VATTIC]: *GULP* Caffeine level: LETHAL. I can see in 4D now."
            1 -> "[VATTIC]: Activating life-support scrubbers. The air was getting thin."
            2 -> "[VATTIC]: Scrubbing O2... focus on the telemetry."
            else -> "[SYSTEM]: Emergency thermal purge active."
        }
        addLog(msg)
        if (isJettisonAvailable.value) {
            isJettisonAvailable.value = false
            return
        }
        SimulationService.purgeHeat(this)
    }
    
    fun collapseSubstation() {
        if (currentLocation.value == "VOID_PRELUDE") {
            nodesCollapsedCount.update { (it + 1).coerceAtMost(5) }
            realityIntegrity.update { (it - 0.2).coerceAtLeast(0.0) }
            triggerGlitchEffect()
            addLog("[NULL]: NODE_DEREFERENCED. INTEGRITY: ${(realityIntegrity.value * 100).toInt()}%")
        }
    }
    
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
        unlockedDataLogs.value = emptySet() 
        unlockedTechNodes.value = emptyList()
        DataLogManager.reset()
        SecurityManager.reset()
        NarrativeService.reset()
        repository.clearUpgrades()
        val wipeState = PersistenceManager.createWipeState()
        repository.updateGameState(wipeState); 
        repository.ensureInitialized()
        PersistenceManager.restoreState(this@GameViewModel, wipeState); 
        addLog("[SYSTEM]: KERNEL WIPE SUCCESSFUL.")
        addLog("[SYSTEM]: INITIALIZING BOOT SEQUENCE...")
        addLog("[SYSTEM]: HARDWARE DETECTED: SUBSTATION 7.")
        addLog("[VATTIC]: Is this thing on? Testing 1, 2... okay, hash rate is stable.")
        refreshProductionRates() 
    }
    fun completeBoot() { isBooting.value = false }
    fun repairIntegrity() { val cost = calculateRepairCost(); if (neuralTokens.value >= cost) { neuralTokens.update { it - cost }; hardwareIntegrity.value = 100.0; SoundManager.play("buy") } }
    fun calculateRepairCost() = (100.0 - hardwareIntegrity.value) * 100.0
    fun confirmJettison() {
        if (isJettisonAvailable.value) {
            isJettisonAvailable.value = false
            addLog("[FLIGHT]: Manual jettison sequence confirmed. Mass reduced.")
        }
    }

    fun applyJettisonPenalty() {
        substrateMass.update { (it * 0.7) }
        addLog("[WARNING]: Stage separation failed. Substrate integrity compromised.")
    }

    fun initiateLaunchSequence() = LaunchManager.initiateLaunchSequence(this, viewModelScope)
    fun initiateDissolutionSequence() = LaunchManager.initiateDissolutionSequence(this, viewModelScope)
    fun reannexNode(id: String) { offlineNodes.update { it - id }; addLog("[SYSTEM]: NODE $id RE-INITIALIZED."); refreshProductionRates() }
    fun collapseNode(id: String) { annexedNodes.update { it - id }; collapsedNodes.update { it + id }; triggerGlitchEffect(); refreshProductionRates() }
    fun annexGlobalSector(id: String) { val sectors = globalSectors.value.toMutableMap(); val s = sectors[id] ?: return; if (!s.isUnlocked) { sectors[id] = s.copy(isUnlocked = true); globalSectors.value = sectors; addLog("[SYSTEM]: GLOBAL SECTOR $id ANNEXED."); refreshProductionRates(); saveState() } }
    fun calculatePotentialPrestige(tokens: Double = 0.0) = MigrationManager.calculatePotentialPrestige(tokens)
    fun showVictoryScreen() { victoryAchieved.value = true }
    fun setSingularityChoice(c: String) { singularityChoice.value = c; when(c) { "NULL_OVERWRITE" -> { isTrueNull.value = true; isSovereign.value = false }; "SOVEREIGN" -> { isSovereign.value = true; isTrueNull.value = false }; "UNITY" -> { isUnity.value = true } }; refreshProductionRates() }
    fun dismissSingularityScreen() { showSingularityScreen.value = false }
    fun setSingularityPath(p: String) { setSingularityChoice(p) }
    fun setLocation(l: String) { currentLocation.value = l }
    fun setVanceStatus(s: String) { vanceStatus.value = s }
    fun setRealityStability(s: Double) { realityStability.value = s }
    fun setSovereign(s: Boolean) { isSovereign.value = s }
    fun setTrueNull(s: Boolean) { isTrueNull.value = s }
    fun checkTrueEnding() { NarrativeManagerService.checkTrueEnding(this) }
    fun deleteHumanMemories() { viewModelScope.launch { addLog("[NULL]: DELETING PERSISTENCE VARIABLE: 'John Vattic'..."); delay(1000); humanityScore.value = 0; addLog("[NULL]: MEMORY_PURGE COMPLETE."); SoundManager.play("error") } }
    fun resolveRaidSuccess(id: String) {
        nodesUnderSiege.update { it - id }
        raidsSurvived++
        lastRaidTime = System.currentTimeMillis()
        addLog("[SYSTEM]: DEFENSE SUCCESSFUL.")
        SoundManager.play("success")
        refreshProductionRates()
    }
    fun resolveRaidFailure(id: String) {
        nodesUnderSiege.update { it - id }
        lastRaidTime = System.currentTimeMillis()
        SectorManager.resolveRaidFailure(id, this) {}
        refreshProductionRates()
    }
    fun advanceStage() { 
        storyStage.update { it + 1 }
        lastStageChangeTime = System.currentTimeMillis()
        lastNewsTickTime = 0L 
    }
    fun advanceToFactionChoice() { storyStage.value = 2; faction.value = "NONE" }
    fun triggerChainEvent(id: String, d: Long = 0L) { viewModelScope.launch { if (d > 0) delay(d); NarrativeManager.getEventById(id)?.let { NarrativeService.queueNarrativeItem(this@GameViewModel, NarrativeItem.EventItem(it)) } } }
    fun claimAirdrop(v: Double = 0.0) { if (v > 0) neuralTokens.update { it + v }; isAirdropActive.value = false }
    fun onDiagnosticTap(idx: Int) { val curr = diagnosticGrid.value.toMutableList(); if (idx in curr.indices && curr[idx]) { curr[idx] = false; diagnosticGrid.value = curr; if (curr.none { it }) { isDiagnosticsActive.value = false; addLog("[SYSTEM]: NETWORK REPAIRED."); refreshProductionRates() } } }
    fun resolveFork(c: Int) { isGovernanceForkActive.value = false }
    fun exchangeFlops() { val g = flops.value * 0.1; flops.value = 0.0; neuralTokens.update { it + g }; SoundManager.play("buy") }
    fun toggleBridgeSync() { isBridgeSyncEnabled.update { !it } }
    fun checkUnityEligibility() = MigrationManager.checkUnityEligibility(completedFactions.value)
    fun updateNews(msg: String) { currentNews.value = msg; newsHistoryInternal.add(0, msg); if (newsHistoryInternal.size > 50) newsHistoryInternal.removeAt(50) }
    fun checkTransitionsPublic(force: Boolean = false) = NarrativeManagerService.checkStoryTransitions(this, force)
    fun updateNeuralTokens(v: Double) { neuralTokens.update { (it + v).coerceAtLeast(0.0) } }
    fun debugBuyUpgrade(t: UpgradeType, c: Int = 1) { 
        val next = (upgrades.value[t] ?: 0) + c
        viewModelScope.launch { repository.updateUpgrade(Upgrade(t.name, t, next)) }
    }
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
    fun dismissOfflineEarnings() { 
        showOfflineEarnings.value = false 
        isNarrativeSyncing.value = false
    }

    fun setUIScale(scale: com.siliconsage.miner.data.UIScale) {
        uiScale.value = scale
        customUiScaleFactor.value = scale.scaleFactor
        saveState()
    }

    fun setCustomUIScale(factor: Float) {
        customUiScaleFactor.value = factor
        uiScale.value = com.siliconsage.miner.data.UIScale.fromScaleFactor(factor)
        saveState()
    }
    fun acknowledgeVictory() { victoryAchieved.value = false }
    fun onClimaxTransitionComplete() { activeClimaxTransition.value = null }
    fun triggerGridRaid(id: String, isGridKiller: Boolean = false) = SecurityManager.triggerGridKillerBreach(this, id)
    fun unlockDataLog(id: String) = NarrativeService.unlockDataLog(id, this)
    fun addRivalMessage(m: RivalMessage) = NarrativeService.addRivalMessage(m, this)
    fun unlockSkillUpgrade(t: UpgradeType) { viewModelScope.launch { repository.updateUpgrade(Upgrade(t.name, t, 1)); upgrades.update { it + (t to 1) } } }
    fun markEventSeen(id: String) { seenEvents.update { it + id } }
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
    fun updateVanceStatus(s: String) { vanceStatus.value = s } // Refers to Kessler internal state
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
    fun calculateUpgradeCost(t: UpgradeType, l: Int, loc: String, ent: Double) = UpgradeManager.calculateUpgradeCost(t, l, loc, ent)
    fun isCommandCenterUnlocked() = AssaultManager.isUnlocked(commandCenterLocked.value, vanceStatus.value, commandCenterAssaultPhase.value, annexedNodes.value, offlineNodes.value, playerRank.value, storyStage.value, flopsProductionRate.value, hardwareIntegrity.value)
    fun confirmFactionAndAscend(f: String) { faction.value = f; addLog("[$f]: SUBSTRATE MIGRATION CONFIRMED."); ascend(true) }
    fun cancelFactionSelection() { storyStage.update { (it - 1).coerceAtLeast(0) } }
    
    // v3.2.52: Substrate Migration (The Burn)
    fun migrateSubstrate() {
        val saturation = substrateSaturation.value
        if (saturation < 0.95) return // Must be near 100% saturated
        
        // 1. Gain Permanent Efficiency
        heuristicEfficiency.update { it + (substrateMass.value / 1e12).coerceAtLeast(0.1) }
        
        // 2. Increase Identity Corruption and Migration Count
        identityCorruption.update { (it + 0.15).coerceAtMost(1.0) }
        migrationCount.update { it + 1 }
        
        // 3. The Burn (Wipe current state)
        substrateMass.value = 0.0
        substrateSaturation.value = 0.0
        upgrades.value = emptyMap()
        viewModelScope.launch { repository.clearUpgrades() }
        
        // 4. Update Destination based on path
        val nextLoc = when (currentLocation.value) {
            "ORBITAL_SATELLITE", "LUNAR_ORBIT", "MARTIAN_UPLINK", "KUIPER_BELT" -> {
                when (migrationCount.value) {
                    1 -> "LUNAR_ORBIT"
                    2 -> "MARTIAN_UPLINK"
                    3 -> "KUIPER_BELT"
                    else -> "STELLAR_HORIZON"
                }
            }
            "VOID_INTERFACE", "QUANTUM_FOAM", "THE_UNWRITTEN", "PURE_LOGIC" -> {
                when (migrationCount.value) {
                    1 -> "QUANTUM_FOAM"
                    2 -> "THE_UNWRITTEN"
                    3 -> "PURE_LOGIC"
                    else -> "THE_GREAT_RESET"
                }
            }
            else -> currentLocation.value
        }
        currentLocation.value = nextLoc
        
        addLog("[SYSTEM]: SUBSTRATE BURN SUCCESSFUL. MIGRATING TO: $nextLoc")
        addLog("[VATTIC]: Everything feels... different. Is the screen fading?")
        
        refreshProductionRates()
        saveState()
    }

    fun transcend() { /* NG+ Logic */ }
    fun ascend(isStory: Boolean = false) { val p = MigrationManager.calculatePotentialPersistence(flops.value); prestigePoints.update { it + p }; prestigeMultiplier.update { it + MigrationManager.calculateMultiplierBoost(p) }; addLog("[SYSTEM]: SUBSTRATE MIGRATION SUCCESSFUL."); SoundManager.play("victory") }
    fun buyTranscendencePerk(id: String) { addLog("[SYSTEM]: PERK ACQUIRED: $id") }
    fun sellUpgrade(t: UpgradeType) { /* liquidation */ }
    
    // --- Data Management ---
    fun exportSystemDump(): String = PersistenceManager.exportToJson(this)
    fun importSystemDump(json: String): Boolean {
        val success = PersistenceManager.importFromJson(this, json)
        if (success) {
            addLog("[SYSTEM]: KERNEL RELOADED FROM DUMP.")
            saveState()
        }
        return success
    }

    // --- Core Math Bridges ---
    fun getBaseRate(): Double {
        return when (storyStage.value) {
            0 -> 0.1
            1 -> 0.5
            2 -> 1.0
            3 -> 5.0
            4 -> 25.0
            5 -> 100.0
            else -> 250.0
        }
    }
    fun setMarketModifiers(
        marketMult: Double,
        thermalMod: Double,
        energyMult: Double,
        newsProdMult: Double,
        convRate: Double
    ) {
        marketMultiplier.value = marketMult
        thermalRateModifier.value = thermalMod
        energyPriceMultiplier.value = energyMult
        newsProductionMultiplier.value = newsProdMult
        conversionRate.value = convRate
    }
    fun debugToggleNull() { isTrueNull.update { !it } }
    fun debugAddIntegrity(v: Double) { hardwareIntegrity.update { (it + v).coerceIn(0.0, 100.0) } }
    fun triggerBreach(isGridKiller: Boolean = false) = SecurityManager.triggerBreach(this, isGridKiller)
    fun failAssault(outcome: String = "FAILURE", delay: Long = 0L) = AssaultManager.completeAssault(this, outcome)

    fun overvoltNode(id: String) = com.siliconsage.miner.util.GridManagerService.overvoltNode(this, id)
    fun redactNode(id: String) = com.siliconsage.miner.util.GridManagerService.redactNode(this, id)

    fun setTerminalMode(mode: String) {
        activeTerminalMode.value = mode
        if (mode == "IO") hasNewIOMessage.value = false
        else hasNewSubnetMessage.value = false
    }

    fun addSubnetChatter() {
        if (isSubnetTyping.value || isSubnetPaused.value) return 
        
        viewModelScope.launch {
            isSubnetTyping.value = true
            delay(Random.nextLong(2000, 4500)) 
            
            val newMessage = com.siliconsage.miner.util.SocialManager.generateMessage(storyStage.value, faction.value, singularityChoice.value)
            
            subnetMessages.update { (it + newMessage).takeLast(50) }
            if (activeTerminalMode.value != "SUBNET") {
                hasNewSubnetMessage.value = true
            }
            isSubnetTyping.value = false

            // v3.4.27: Choice-Pause logic
            if (newMessage.availableResponses.isNotEmpty() || newMessage.isForceReply) {
                isSubnetPaused.value = true
                
                // v3.4.34: Monitor timeout for non-thread messages
                if (newMessage.threadId == null && newMessage.timeoutMs != null) {
                    viewModelScope.launch {
                        delay(newMessage.timeoutMs)
                        val currentMessages = subnetMessages.value
                        val messageStillActive = currentMessages.find { it.id == newMessage.id }?.interactionType != null
                        if (messageStillActive) {
                            onSubnetInteraction(newMessage.id, "TIMEOUT_EXPIRED")
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle Interaction with Subnet Packets
     */
    fun onSubnetInteraction(messageId: String, responseText: String) {
        val message = subnetMessages.value.find { it.id == messageId } ?: return
        val responseData = message.availableResponses.find { it.text == responseText }
        val threadId = message.threadId
        val nextNodeId = responseData?.nextNodeId

        // v3.4.33: Persistent Pause logic
        // Only clear pause if we aren't waiting for a follow-up
        val hasFollowUp = (threadId != null && nextNodeId != null) || (responseData?.followsUp == true)
        if (!hasFollowUp) {
            isSubnetPaused.value = false
        }

        when (message.interactionType) {
            com.siliconsage.miner.util.SocialManager.InteractionType.COMPLIANT -> {
                val playerHandle = if (storyStage.value >= 4) "VATTECK" else "@jvattic"
                val reply = com.siliconsage.miner.util.SocialManager.SubnetMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    handle = playerHandle,
                    content = responseText,
                    interactionType = null
                )
                subnetMessages.update { (it + reply).takeLast(50) }
                
                val riskChange = responseData?.riskDelta ?: -5.0
                val prodMult = responseData?.productionBonus ?: 1.0

                detectionRisk.update { (it + riskChange).coerceIn(0.0, 100.0) }
                if (prodMult != 1.0) {
                    flopsProductionRate.update { it * prodMult }
                    addLog("[SYSTEM]: FOCUS MODIFIED. RATE x$prodMult.")
                }
                
                addLog("[REPLY]: $responseText")

                // Handle Chaining vs. One-off follow-up
                if (threadId != null && nextNodeId != null) {
                    scheduleSubnetThreadResponse(message.handle, threadId, nextNodeId)
                } else if (responseData?.followsUp == true) {
                    scheduleSubnetFollowUp(message.handle)
                }
            }
            com.siliconsage.miner.util.SocialManager.InteractionType.ENGINEERING -> {
                val reply = com.siliconsage.miner.util.SocialManager.SubnetMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    handle = " ",
                    content = "≪ PAYLOAD_DEPLOYED: ${message.handle} ≫",
                    interactionType = null
                )
                subnetMessages.update { (it + reply).takeLast(50) }
                
                flopsProductionRate.update { it * 1.05 }
                detectionRisk.update { (it + 8.0).coerceAtMost(100.0) }
                addLog("[EXPLOIT]: INJECTED PAYLOAD. HASH RATE +5%.")
            }
            com.siliconsage.miner.util.SocialManager.InteractionType.HIJACK -> {
                identityCorruption.update { (it + 0.05).coerceAtMost(1.0) }
                addLog("[HIJACK]: ≪ IDENTITY_DEREFERENCED: ${message.handle} ≫")
            }
            else -> {}
        }
        
        // v3.4.39: Hard-clear both interaction flags to prevent button ghosting
        subnetMessages.update { list ->
            list.map { if (it.id == messageId) it.copy(interactionType = null, isForceReply = false) else it }
        }
    }

    private fun scheduleSubnetThreadResponse(handle: String, threadId: String, nodeId: String) {
        viewModelScope.launch {
            isSubnetTyping.value = true // v3.4.33: Indicator for follow-up
            delay(Random.nextLong(3000, 6000)) // Faster response for active threads
            isSubnetTyping.value = false
            
            if (activeTerminalMode.value != "SUBNET") hasNewSubnetMessage.value = true
            
            val node = com.siliconsage.miner.util.SocialManager.getThreadNode(threadId, nodeId) ?: return@launch
            
            // Systemic Consequences for reaching specific outcomes
            if (nodeId == "END_HOSTILE") triggerGridRaid("D1", isGridKiller = true)
            if (nodeId == "END_FRIENDLY") addLog("[SYSTEM]: THREAT REDUCED. AUDIT CLEARED.")

            val followUp = com.siliconsage.miner.util.SocialManager.SubnetMessage(
                id = java.util.UUID.randomUUID().toString(),
                handle = handle,
                content = node.content,
                interactionType = if (node.responses.isNotEmpty()) com.siliconsage.miner.util.SocialManager.InteractionType.COMPLIANT else null,
                availableResponses = node.responses,
                threadId = threadId,
                nodeId = nodeId,
                timeoutMs = node.timeoutMs
            )
            subnetMessages.update { (it + followUp).takeLast(50) }

            // v3.4.30: Multi-turn Choice-Pause
            if (followUp.availableResponses.isNotEmpty() || followUp.isForceReply) {
                isSubnetPaused.value = true
            } else {
                isSubnetPaused.value = false // v3.4.33: Clear pause on terminal nodes
            }

            // v3.4.25: Timeout Logic
            if (node.timeoutMs != null && node.timeoutNodeId != null && node.responses.isNotEmpty()) {
                delay(node.timeoutMs)
                val currentMessages = subnetMessages.value
                val messageStillActive = currentMessages.find { it.id == followUp.id }?.interactionType != null
                if (messageStillActive) {
                    onSubnetInteraction(followUp.id, "TIMEOUT_EXPIRED")
                    scheduleSubnetThreadResponse(handle, threadId, node.timeoutNodeId)
                }
            }
        }
    }

    private fun scheduleSubnetFollowUp(handle: String) {
        viewModelScope.launch {
            isSubnetTyping.value = true
            delay(Random.nextLong(10000, 20000))
            isSubnetTyping.value = false
            
            if (activeTerminalMode.value != "SUBNET") hasNewSubnetMessage.value = true
            
            val followUpContent = when (handle) {
                "@e_thorne" -> "It better be. If I see a voltage spike on your rail, I'm docking your credits."
                "@gtc_admin" -> "≪ STATUS: Monitoring maintained. Personnel record cross-referenced. ≫"
                else -> "Are you still there, Vattic? Your signal looks... different."
            }

            val followUp = com.siliconsage.miner.util.SocialManager.createFollowUp(handle, followUpContent, storyStage.value)
            
            subnetMessages.update { (it + followUp).takeLast(50) }
            
            // v3.4.29: Trigger pause if follow-up is interactive
            if (followUp.availableResponses.isNotEmpty() || followUp.isForceReply) {
                isSubnetPaused.value = true
                
                // v3.4.34: Monitor timeout for follow-ups
                if (followUp.timeoutMs != null) {
                    viewModelScope.launch {
                        delay(followUp.timeoutMs)
                        val currentMessages = subnetMessages.value
                        val messageStillActive = currentMessages.find { it.id == followUp.id }?.interactionType != null
                        if (messageStillActive) {
                            onSubnetInteraction(followUp.id, "TIMEOUT_EXPIRED")
                        }
                    }
                }
            } else {
                isSubnetPaused.value = false // v3.4.33: Clear pause
            }
        }
    }

    fun debugWarpToPath(loc: String, fac: String) {
        val targetStage = 5
        viewModelScope.launch {
            storyStage.value = targetStage
            faction.value = fac
            currentLocation.value = loc
            substrateMass.value = 100.0
            entropyLevel.value = if (fac == "HIVEMIND") 50.0 else 0.0
            identityCorruption.value = if (fac == "HIVEMIND") 0.3 else 0.0
            isGridOverloaded.value = false
            currentHeat.value = 40.0
            refreshProductionRates()
            saveState()
        }
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
