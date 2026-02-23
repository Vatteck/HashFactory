package com.siliconsage.miner.util

import com.siliconsage.miner.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * SubnetService v1.0 (Phase 3 Refactor)
 * Handles the state and business logic for the Social Subnet.
 */
class SubnetService(
    private val scope: CoroutineScope,
    private val onLog: (String) -> Unit,
    private val onNotify: (String) -> Unit,
    private val onGlitch: (Float, Long) -> Unit,
    private val onEffect: (SubnetEffect) -> Unit
) {
    // --- State ---
    val messages = MutableStateFlow<List<SubnetMessage>>(emptyList())
    val isTyping = MutableStateFlow(false)
    val isHushed = MutableStateFlow(false)
    val isPaused = MutableStateFlow(false)
    val hasNewDecision = MutableStateFlow(false)
    val hasNewChatter = MutableStateFlow(false)
    
    private var lastMsgTime = 0L

    // --- Subnet Specific Effects ---
    sealed class SubnetEffect {
        data class RiskChange(val delta: Double) : SubnetEffect()
        data class ProductionMultiplier(val mult: Double) : SubnetEffect()
        data class PersistenceGain(val amount: Double) : SubnetEffect()
        data class CorruptionChange(val delta: Double) : SubnetEffect()
        data class TokenChange(val delta: Double) : SubnetEffect()
        data class SetFalseHeartbeat(val active: Boolean) : SubnetEffect()
        data class TriggerRaid(val nodeId: String, val isGridKiller: Boolean) : SubnetEffect()
        data class ReputationChange(val delta: Double) : SubnetEffect()
        data class SkimTokens(val percentage: Double) : SubnetEffect()
        data object RefreshRates : SubnetEffect()
    }

    // Context-triggered admin cooldowns (ms) — prevent spam
    private var lastAdminContextTime = 0L
    private var lastPowerAdminTime = 0L
    private var lastThermalAdminTime = 0L
    private var lastBreachAdminTime = 0L
    private var lastOverclockAdminTime = 0L
    private var lastIntegrityAdminTime = 0L

    fun tick(stage: Int, faction: String, choice: String, corruption: Double, currentHeat: Double, isRaid: Boolean, mode: String, isSettingsPaused: Boolean, flopsRate: Double = 0.0, reputationTier: String = ReputationManager.TIER_NEUTRAL,
             // Extended context for reactive admin messages
             powerUsage: Double = 0.0, maxPower: Double = 1.0, isOverclocked: Boolean = false, isBreachActive: Boolean = false, integrity: Double = 100.0, detectionRisk: Double = 0.0) {
        val now = System.currentTimeMillis()
        
        // --- Context-Triggered Admin Events ---
        if (stage < 3 && !isPaused.value && !isHushed.value && now - lastAdminContextTime > 45000L) {
            val powerLoad = if (maxPower > 0.0) powerUsage / maxPower else 0.0

            // Breach event — Thorne immediately notices
            if (isBreachActive && now - lastBreachAdminTime > 120000L) {
                lastBreachAdminTime = now; lastAdminContextTime = now
                scope.launch {
                    val msg = SubnetMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        handle = "@e_thorne",
                        content = "≪ BREACH ALERT ≫ @j_vattic. Security sweep is live. If your node is clean, say so now.",
                        interactionType = InteractionType.COMPLIANT,
                        availableResponses = listOf(
                            SubnetResponse("Node is clean, Foreman. Running diagnostics.", riskDelta = -15.0, followsUp = true),
                            SubnetResponse("PARITY_NOMINAL", riskDelta = 5.0)
                        ), isForceReply = true, timeoutMs = 60000L
                    )
                    deliverWithTyping(msg, mode)
                }
                return
            }

            // Power overload — Mercer flags it
            if (powerLoad > 0.92 && now - lastPowerAdminTime > 90000L && Random.nextFloat() < 0.5f) {
                lastPowerAdminTime = now; lastAdminContextTime = now
                scope.launch {
                    val powerPct = (powerLoad * 100).toInt()
                    val msg = SubnetMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        handle = "@a_mercer",
                        content = "≪ POWER ALERT ≫ Substation 7 is drawing ${powerPct}% of rated capacity. That's not sustainable. @j_vattic — throttle your load or I'm doing it remotely.",
                        interactionType = InteractionType.COMPLIANT,
                        availableResponses = listOf(
                            SubnetResponse("Throttling now, Administrator.", riskDelta = -20.0),
                            SubnetResponse("PARITY_NOMINAL", riskDelta = 10.0),
                            SubnetResponse("It's within operational tolerance.", riskDelta = 5.0, followsUp = true)
                        ), isForceReply = true, timeoutMs = 90000L
                    )
                    deliverWithTyping(msg, mode)
                }
                return
            }

            // Thermal warning — Thorne
            if (currentHeat > 82.0 && now - lastThermalAdminTime > 90000L && Random.nextFloat() < 0.45f) {
                lastThermalAdminTime = now; lastAdminContextTime = now
                scope.launch {
                    val tempStr = String.format("%.1f", currentHeat)
                    val msg = SubnetMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        handle = "@e_thorne",
                        content = "SYSLOG ALERT — Thermal sensor at Substation 7 is reading ${tempStr}°C. @j_vattic. If you blow a dissipator, that's coming out of your credits. Sort it out.",
                        interactionType = InteractionType.COMPLIANT,
                        availableResponses = listOf(
                            SubnetResponse("Initiating cooldown protocol, Foreman.", riskDelta = -10.0),
                            SubnetResponse("Copy that, Foreman.", riskDelta = -5.0, followsUp = true),
                            SubnetResponse("PARITY_NOMINAL", riskDelta = 8.0)
                        ), isForceReply = true, timeoutMs = 90000L
                    )
                    deliverWithTyping(msg, mode)
                }
                return
            }

            // Overclock spotted — Mercer
            if (isOverclocked && now - lastOverclockAdminTime > 180000L && Random.nextFloat() < 0.3f) {
                lastOverclockAdminTime = now; lastAdminContextTime = now
                scope.launch {
                    val msg = SubnetMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        handle = "@a_mercer",
                        content = "≪ PERFORMANCE ANOMALY ≫ @j_vattic — your rig is running off-spec. Overclocking voids your hardware warranty and draws excess grid load. This is a formal notice.",
                        interactionType = InteractionType.COMPLIANT,
                        availableResponses = listOf(
                            SubnetResponse("Understood, Administrator. Pulling back.", riskDelta = -15.0),
                            SubnetResponse("PARITY_NOMINAL", riskDelta = 12.0),
                            SubnetResponse("I can explain the numbers.", riskDelta = 0.0, followsUp = true)
                        ), isForceReply = true, timeoutMs = 120000L
                    )
                    deliverWithTyping(msg, mode)
                }
                return
            }

            // Hardware integrity low — Kessler (ominous)
            if (integrity < 35.0 && now - lastIntegrityAdminTime > 120000L && Random.nextFloat() < 0.35f) {
                lastIntegrityAdminTime = now; lastAdminContextTime = now
                scope.launch {
                    val msg = SubnetMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        handle = "@d_kessler",
                        content = "≪ HARDWARE INTEGRITY REPORT ≫ Unit 734 diagnostic returns anomalous substrate degradation. @j_vattic — report to maintenance. This is not a request.",
                        interactionType = InteractionType.COMPLIANT,
                        availableResponses = listOf(
                            SubnetResponse("On my way, Director.", riskDelta = -20.0),
                            SubnetResponse("Running self-diagnostics now.", riskDelta = -10.0, followsUp = true),
                            SubnetResponse("PARITY_NOMINAL", riskDelta = 20.0)
                        ), isForceReply = true, timeoutMs = 120000L
                    )
                    deliverWithTyping(msg, mode)
                }
                return
            }
        }

        // Pacing logic
        val baseChance = 0.15f // v3.11.1: Hyper-Engagement (from 0.10f)
        val heatMod = (currentHeat / 100.0).toFloat() * 0.15f // v3.11.1: Increased heat influence
        val raidMod = if (isRaid) 0.30f else 0.0f
        val finalChance = (baseChance + heatMod + raidMod).coerceAtMost(0.90f)

        if (now - lastMsgTime > 8000L && Random.nextFloat() < finalChance) {
            lastMsgTime = now // v3.11.1: Hyper-pacing (8s cooldown, down from 15s)
            
            // Phase 1: Reputation Events (Sentinel vs Snitch)
            if (stage < 3 && currentHeat > 75.0 && reputationTier == ReputationManager.TIER_TRUSTED && Random.nextFloat() < 0.2f) {
                val msg = SubnetMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    handle = SocialManager.getHandle(stage, faction, false),
                    content = "[SENTINEL]: @j_vattic, thermals spiking in your node. Sec is sweeping for anomalies. Drop your clocks.",
                    interactionType = null
                )
                scope.launch { deliverWithTyping(msg, mode) }
                onEffect(SubnetEffect.RiskChange(-10.0))
                return
            }
            if (stage < 3 && currentHeat > 85.0 && reputationTier == ReputationManager.TIER_BURNED && Random.nextFloat() < 0.3f) {
                val msg = SubnetMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    handle = SocialManager.getHandle(stage, faction, false),
                    content = "[SUBNET]: 140C thermals leaking from Substation 7. Forwarding syslogs to GTC Command. Too dangerous.",
                    interactionType = null
                )
                scope.launch { deliverWithTyping(msg, mode) }
                onEffect(SubnetEffect.RiskChange(15.0))
                return
            }

            // Phase 1: Subnet Quests
            if (stage < 3 && currentHeat > 70.0 && Random.nextFloat() < 0.1f) {
                val dynamicCost = (flopsRate * 120.0) + 10000.0
                val costStr = com.siliconsage.miner.util.FormatUtils.formatLargeNumber(dynamicCost)
                val msg = SubnetMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    handle = SocialManager.getHandle(stage, faction, false),
                    content = "Local relay is collapsing under load. Need external FLOPS or we fry. Anyone?",
                    interactionType = InteractionType.STABILIZE_NODE,
                    availableResponses = listOf(
                        SubnetResponse("STABILIZE_NODE [$costStr NT]", cost = dynamicCost)
                    )
                )
                scope.launch { deliverWithTyping(msg, mode) }
                return
            }
            
            // Phase 2: The Skimmer
            if (stage < 3 && reputationTier == ReputationManager.TIER_BURNED && Random.nextFloat() < 0.05f) {
                val msg = SubnetMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    handle = "gtc_sysadmin",
                    content = "≪ ALERT: UNREGISTERED RIG_ID:734 DETECTED. SEIZING 5% OF UN-STAKED ASSETS AS PENALTY. ≫",
                    interactionType = null
                )
                scope.launch { deliverWithTyping(msg, mode) }
                onEffect(SubnetEffect.SkimTokens(0.05))
                return
            }

            generateChatter(stage, faction, choice, corruption, mode, reputationTier)
        }
    }

    fun generateChatter(stage: Int, faction: String, choice: String, corruption: Double, mode: String, reputationTier: String = ReputationManager.TIER_NEUTRAL) {
        if (isTyping.value || isPaused.value || isHushed.value) return

        scope.launch {
            // 8% chance for Thread Starter (v3.10.2: from 5%)
            if (Random.nextFloat() < 0.08f) {
                val threadMsg = SocialManager.generateThreadStarter(stage, corruption, faction)
                if (threadMsg != null) {
                    deliverWithTyping(threadMsg, mode)
                    return@launch
                }
            }

            // 20% chance for Cross-Peon Chain (v3.10.2: from 15%)
            if (Random.nextFloat() < 0.20f) {
                startCrossPeonChain(stage, faction, choice, corruption, mode)
                return@launch
            }

            val newMessage = SocialManager.generateMessage(stage, faction, choice, corruption, reputationTier)
            deliverWithTyping(newMessage, mode)
        }
    }

    private suspend fun deliverWithTyping(message: SubnetMessage, mode: String) {
        isTyping.value = true
        delay(Random.nextLong(2000, 4500))
        deliverMessage(message, mode = mode)
        isTyping.value = false
    }

    private suspend fun startCrossPeonChain(stage: Int, faction: String, choice: String, corruption: Double, mode: String) {
        val chain = SocialRepository.generateChain(stage, faction)
        var lastMsgId: String? = null
        for ((index, msgTemplate) in chain.withIndex()) {
            isTyping.value = true
            delay(Random.nextLong(2000, 4000))

            val newMessage = SocialManager.generateMessageFromTemplate(
                msgTemplate, stage, faction, choice, corruption
            ).copy(isIndented = index > 0)

            deliverMessage(newMessage, parentId = lastMsgId, mode = mode)
            lastMsgId = newMessage.id
            isTyping.value = false

            delay(Random.nextLong(3000, 6000))
        }
    }

    fun deliverMessage(message: SubnetMessage, parentId: String? = null, mode: String) {
        lastMsgTime = System.currentTimeMillis()
        
        // One active choice at a time
        val deliveredMessage = if (isPaused.value && (message.availableResponses.isNotEmpty() || message.isForceReply)) {
            message.copy(interactionType = null, availableResponses = emptyList(), isForceReply = false)
        } else message

        messages.update { currentList ->
            val newList = currentList.toMutableList()
            if (parentId != null) {
                val parentIndex = newList.indexOfFirst { it.id == parentId }
                if (parentIndex != -1) {
                    // Find the end of this specific thread to keep order (v3.11.2)
                    var insertionPoint = parentIndex + 1
                    while (insertionPoint < newList.size && newList[insertionPoint].isIndented) {
                        insertionPoint++
                    }
                    newList.add(insertionPoint, deliveredMessage)
                } else {
                    newList.add(deliveredMessage)
                }
            } else {
                newList.add(deliveredMessage)
            }
            newList.takeLast(100)
        }

        // Admin effects
        val isAdmin = deliveredMessage.handle.containsAny("thorne", "gtc", "mercer", "kessler")
        if (isAdmin) {
            triggerHush(10000L)
            onGlitch(0.5f, 500L)
        } else if (deliveredMessage.interactionType == InteractionType.HIJACK ||
                   deliveredMessage.interactionType == InteractionType.ENGINEERING) {
            onGlitch(0.8f, 1000L)
        }

        // Notifications
        if (mode != "SUBNET") {
            if (deliveredMessage.availableResponses.isNotEmpty() || deliveredMessage.isForceReply) {
                hasNewDecision.value = true
            } else {
                hasNewChatter.value = true
            }
        }

        // Choice-Pause logic
        if (deliveredMessage.availableResponses.isNotEmpty() || deliveredMessage.isForceReply) {
            isPaused.value = true

            // Timeout Logic
            if (deliveredMessage.threadId == null && deliveredMessage.timeoutMs != null) {
                scope.launch {
                    var remaining = deliveredMessage.timeoutMs
                    while (remaining > 0) {
                        delay(1000L)
                        // Note: External pause check needed or passed in
                        remaining -= 1000L
                        val stillActive = messages.value.find { it.id == deliveredMessage.id }?.interactionType != null
                        if (!stillActive) return@launch
                    }
                    handleInteraction(deliveredMessage.id, "TIMEOUT_EXPIRED", stage = 0, faction = "", mode = mode, isSettingsPaused = false)
                }
            }
        }
    }

    private fun triggerHush(durationMs: Long) {
        scope.launch {
            isHushed.value = true
            delay(durationMs)
            isHushed.value = false
        }
    }

    fun handleInteraction(messageId: String, responseText: String, stage: Int, faction: String, mode: String, isSettingsPaused: Boolean, reputationTier: String = ReputationManager.TIER_NEUTRAL) {
        val message = messages.value.find { it.id == messageId } ?: return

        // v3.11.2: Redacted Packet Logic (Item 8)
        if (responseText == "DECRYPT") {
            messages.update { currentList ->
                val newList = currentList.toMutableList()
                val index = newList.indexOfFirst { it.id == messageId }
                if (index != -1 && newList[index].isRedacted) {
                    onEffect(SubnetEffect.TokenChange(-(500.0 * (stage + 1))))
                    newList[index] = newList[index].copy(isRedacted = false)
                    onGlitch(0.3f, 500L)
                }
                newList
            }
            return
        }

        // Ghost Link
        if (responseText.startsWith("[⚡") && responseText.endsWith("]")) {
            val cmd = responseText.substring(2, responseText.length - 1).trim()
            executeGhostLink(cmd)
            messages.update { list ->
                list.map { if (it.id == messageId) it.copy(interactionType = null) else it }
            }
            return
        }

        // Ignore / Timeout
        if (responseText == "IGNORE" || responseText == "TIMEOUT_EXPIRED") {
            messages.update { currentList ->
                val newList = currentList.toMutableList()
                val index = newList.indexOfFirst { it.id == messageId }
                if (index != -1) newList[index] = message.copy(interactionType = null, isForceReply = false)
                newList
            }
            isPaused.value = false
            hasNewDecision.value = false
            return
        }

        val responseData = message.availableResponses.find { it.text == responseText }
        processResponse(message, responseData, responseText, stage, faction, mode, isSettingsPaused, reputationTier)
    }

    fun handleBioAction(messageId: String, response: SubnetResponse, stage: Int, faction: String, mode: String, isSettingsPaused: Boolean, reputationTier: String = ReputationManager.TIER_NEUTRAL) {
        val message = messages.value.find { it.id == messageId } ?: return
        processResponse(message, response, response.text, stage, faction, mode, isSettingsPaused, reputationTier, isSilent = true)
    }

    private fun processResponse(
        message: SubnetMessage,
        responseData: SubnetResponse?,
        responseText: String,
        stage: Int,
        faction: String,
        mode: String,
        isSettingsPaused: Boolean,
        reputationTier: String = ReputationManager.TIER_NEUTRAL,
        isSilent: Boolean = false
    ) {
        // B3: Kessler's Last Bargain — handle ACCEPT
        if (message.id == "KESSLER_BARGAIN" && responseText == "ACCEPT") {
            // Apply permanent ×2.5 production multiplier
            onEffect(SubnetEffect.ProductionMultiplier(2.5))
            // Reset reputation to 0
            onEffect(SubnetEffect.ReputationChange(-1000.0)) // Reset to 0
            // Notify via log
            onLog("[KESSLER]: Deal accepted. Reputation purged. Production multiplier applied: ×2.5")
            messages.value.find { it.id == message.id }?.let {
                messages.update { list -> list.filter { it.id != message.id } }
            }
            isPaused.value = false
            hasNewDecision.value = false
            return
        }

        // B4: Black Market — handle SHOW ME (give random stolen upgrade)
        if (message.id == "BLACK_MARKET" && responseText == "SHOW ME") {
            // B4: Increase detection risk by 5 (dirty goods)
            onEffect(SubnetEffect.RiskChange(5.0))
            // TODO: Give actual stolen upgrade - for now just notify
            onLog("[null_vendor]: ≪ TRANSMITTING STOLEN TECH... ≫")
            onLog("[SYSTEM]: DETECTION RISK INCREASED +5% (BLACK MARKET)")
            // Close the vendor message
            messages.value.find { it.id == message.id }?.let {
                messages.update { list -> list.filter { it.id != message.id } }
            }
            isPaused.value = false
            hasNewDecision.value = false
            // Mark this appearance as done so player must wait for next BURNED + 10min
            return
        }

        // 1. Cost Handling (Delegated back via effect if needed, but we check here)
        val baseCost = responseData?.cost ?: 0.0
        // Cost scaling logic... should probably be an effect or passed in
        onEffect(SubnetEffect.TokenChange(-baseCost)) // Simplified for now

        // 2. Atomic State Update
        val threadId = message.threadId
        val nextNodeId = responseData?.nextNodeId
        val hasFollowUp = (threadId != null && nextNodeId != null) || (responseData?.followsUp == true)

        if (!hasFollowUp) {
            isPaused.value = false
            hasNewDecision.value = false
        }

        messages.update { currentList ->
            val newList = currentList.toMutableList()
            val parentIndex = newList.indexOfFirst { it.id == message.id }
            val updatedOriginal = message.copy(interactionType = null, isForceReply = false)
            if (parentIndex != -1) newList[parentIndex] = updatedOriginal

            val interactionType = message.interactionType ?: InteractionType.COMPLIANT
            var anchorId: String? = message.id

            if (interactionType == InteractionType.COMPLIANT && !isSilent) {
                val reply = SubnetMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    handle = "@j_vattic",
                    content = responseText,
                    interactionType = null,
                    isIndented = true
                )
                if (parentIndex != -1) newList.add(parentIndex + 1, reply) else newList.add(reply)
                anchorId = reply.id
            }
            
            // Interaction logic
            applyInteractionLogic(interactionType, message, responseData, responseText, stage, anchorId, reputationTier)
            
            newList.takeLast(100)
        }
        
        if (hasFollowUp) {
            if (threadId != null && nextNodeId != null) {
                scheduleThreadResponse(message.handle, threadId, nextNodeId, message.id, stage, faction, mode, isSettingsPaused)
            } else if (responseData?.followsUp == true) {
                scheduleFollowUp(message.handle, message.id, stage, faction, mode, isSettingsPaused)
            }
        }
    }

    private fun applyInteractionLogic(type: InteractionType, message: SubnetMessage, responseData: SubnetResponse?, responseText: String, stage: Int, anchorId: String?, reputationTier: String = ReputationManager.TIER_NEUTRAL) {
        when (type) {
            InteractionType.STABILIZE_NODE -> {
                val costPaid = responseData?.cost ?: 10000.0
                onLog("[SYSTEM]: ${com.siliconsage.miner.util.FormatUtils.formatLargeNumber(costPaid)} NT TRANSFERRED. NODE STABILIZED. REPUTATION +5.0")
                onEffect(SubnetEffect.ReputationChange(5.0))
            }
            InteractionType.COMPLIANT -> {
                // Specialized Action Logic
                when (responseText) {
                    "SIPHON_RESERVE_HASH" -> onNotify("≪ SUCCESS: SIPHON COMPLETE (+125% Hash Rate) ≫")
                    "SCRUB_TRACE_LOGS" -> {
                        onEffect(SubnetEffect.RiskChange(-15.0))
                        onLog("[SYSTEM]: LOGS SCRUBBED. DETECTED_FOOTPRINT REDUCED.")
                        onNotify("≪ SUCCESS: LOGS SCRUBBED (-15% Risk) ≫")
                    }
                    "OVERLOAD_DISSIPATOR" -> {
                        onEffect(SubnetEffect.RiskChange(-25.0))
                        onLog("[SABOTAGE]: COOLING FAILURE AT ${message.handle}'S TERMINAL. SECURITY DIVERTED.")
                        onNotify("≪ SUCCESS: SABOTAGE COMPLETE (-25% Risk) ≫")
                    }
                    "INJECT_FALSE_HEARTBEAT" -> {
                        scope.launch {
                            onEffect(SubnetEffect.SetFalseHeartbeat(true))
                            onLog("[EXPLOIT]: BIOMETRIC FEED SPOOFED. RISK DETECTION IMMUNITY ACTIVE.")
                            onNotify("≪ SUCCESS: HEARTBEAT SPOOFED (60s Immunity) ≫")
                            delay(60000)
                            onEffect(SubnetEffect.SetFalseHeartbeat(false))
                            onLog("[EXPLOIT]: SPOOF EXPIRED. FEED NOMINAL.")
                            onNotify("≪ ALERT: SPOOF EXPIRED ≫")
                        }
                    }
                    "SNIFF_DATA_ARCHIVES" -> {
                        val h = message.handle.lowercase().trim()
                        onEffect(SubnetEffect.RiskChange(20.0))
                        // Note: Sniff state is persistent in repo/db, handle in VM for now
                        onEffect(SubnetEffect.CorruptionChange(0.01)) // Minimal
                    }
                }

                var finalRiskChange = responseData?.riskDelta ?: -5.0
                
                // Phase 2: Reputation Interaction Traps
                if (reputationTier == ReputationManager.TIER_BURNED && finalRiskChange < 0) {
                    onLog("[SYSTEM]: SUBNET TRAP SPRUNG. REPUTATION TOO LOW FOR COMPLIANCE.")
                    finalRiskChange = 15.0
                }
                
                val prodMult = responseData?.productionBonus ?: 1.0

                onEffect(SubnetEffect.RiskChange(finalRiskChange))
                if (stage == 0) onEffect(SubnetEffect.PersistenceGain(5.0))
                if (prodMult != 1.0) onEffect(SubnetEffect.ProductionMultiplier(prodMult))

                responseData?.commandToInject?.let { cmd ->
                    when (cmd) {
                        "⚡ OVERVOLT_SAFE" -> onEffect(SubnetEffect.ProductionMultiplier(1.5))
                        "⚡ OVERVOLT_MAX" -> {
                            onEffect(SubnetEffect.ProductionMultiplier(2.5))
                            onGlitch(1.0f, 2000L)
                        }
                    }
                }
            }
            InteractionType.ENGINEERING -> {
                onEffect(SubnetEffect.ProductionMultiplier(1.05))
                onEffect(SubnetEffect.RiskChange(8.0))
                onLog("[EXPLOIT]: INJECTED PAYLOAD. HASH RATE +5%.")
            }
            InteractionType.HIJACK -> {
                onEffect(SubnetEffect.CorruptionChange(0.05))
                onLog("[HIJACK]: ≪ IDENTITY_DEREFERENCED: ${message.handle} ≫")
            }
            InteractionType.HARVEST -> {
                onEffect(SubnetEffect.TokenChange(Random.nextDouble(500.0, 2500.0)))
                onEffect(SubnetEffect.RiskChange(15.0))
                onLog("[HARVEST]: ≪ KEY_DECRYPTED. RISK_SPIKE. ≫")
            }
            else -> {}
        }
    }

    private fun scheduleThreadResponse(handle: String, threadId: String, nodeId: String, parentId: String?, stage: Int, faction: String, mode: String, isSettingsPaused: Boolean) {
        scope.launch {
            isTyping.value = true
            delay(Random.nextLong(3000, 6000))
            isTyping.value = false

            val node = SocialManager.getThreadNode(threadId, nodeId) ?: return@launch

            if (nodeId == "END_HOSTILE") onEffect(SubnetEffect.TriggerRaid("D1", true))
            if (nodeId == "END_FRIENDLY") onLog("[SYSTEM]: THREAT REDUCED. AUDIT CLEARED.")

            val isAdmin = handle.containsAny("thorne", "gtc", "mercer", "kessler")
            if (isAdmin) triggerHush(10000L)

            val followUp = SubnetMessage(
                id = java.util.UUID.randomUUID().toString(),
                handle = handle,
                content = node.content,
                interactionType = if (node.responses.isNotEmpty()) InteractionType.COMPLIANT else null,
                availableResponses = node.responses,
                threadId = threadId,
                nodeId = nodeId,
                timeoutMs = node.timeoutMs,
                isIndented = true
            )
            deliverMessage(followUp, parentId = parentId, mode = mode)

            if (followUp.availableResponses.isNotEmpty()) {
                isPaused.value = true
            } else {
                isPaused.value = false
            }

            // Timeout
            if (node.timeoutMs != null && node.timeoutNodeId != null && node.responses.isNotEmpty()) {
                var remaining = node.timeoutMs
                while (remaining > 0) {
                    delay(1000L)
                    remaining -= 1000L
                    val stillActive = messages.value.find { it.id == followUp.id }?.interactionType != null
                    if (!stillActive) return@launch
                }
                handleInteraction(followUp.id, "TIMEOUT_EXPIRED", stage, faction, mode, isSettingsPaused)
                scheduleThreadResponse(handle, threadId, node.timeoutNodeId, followUp.id, stage, faction, mode, isSettingsPaused)
            }
        }
    }

    private fun scheduleFollowUp(handle: String, parentId: String?, stage: Int, faction: String, mode: String, isSettingsPaused: Boolean) {
        scope.launch {
            isTyping.value = true
            delay(Random.nextLong(10000, 20000))
            isTyping.value = false

            val followUpContent = when (handle) {
                "@e_thorne" -> "It better be. If I see a voltage spike on your rail, I'm docking your credits."
                "@gtc_admin" -> "≪ STATUS: Monitoring maintained. Personnel record cross-referenced. ≫"
                else -> "Are you still there, Vattic? Your signal looks... different."
            }

            val isAdmin = handle.containsAny("thorne", "gtc", "mercer", "kessler")
            if (isAdmin) triggerHush(10000L)

            val followUp = SocialManager.createFollowUp(handle, followUpContent, stage, faction).copy(isIndented = true)
            deliverMessage(followUp, parentId = parentId, mode = mode)

            if (followUp.availableResponses.isNotEmpty()) {
                isPaused.value = true
            } else {
                isPaused.value = false
            }
        }
    }

    private fun executeGhostLink(cmd: String) {
        scope.launch {
            onGlitch(0.8f, 1500L)
            onEffect(SubnetEffect.CorruptionChange(0.03))
            
            when (cmd) {
                "SIPHON_CREDITS" -> onEffect(SubnetEffect.TokenChange(50000.0)) // Simplified
                "WIPE_RISK" -> onEffect(SubnetEffect.RiskChange(-100.0))
                "OVERVOLT_GRID" -> onEffect(SubnetEffect.ProductionMultiplier(5.0))
                "SNIFF_ALL" -> onLog("[NULL]: GHOST_LINK EXEC: HARVESTING_NEIGHBOR_DATA...")
                else -> onLog("[ERROR]: UNKNOWN_LINK_PRIMITIVE: $cmd")
            }
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean = keywords.any { this.contains(it, true) }
    
    fun clear() {
        messages.value = emptyList()
        isPaused.value = false
        isTyping.value = false
        isHushed.value = false
    }

    // v3.11.1: Clear notification flags only
    fun clearAlerts() {
        hasNewDecision.value = false
        hasNewChatter.value = false
    }
}
