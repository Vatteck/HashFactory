package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * AmbientEffectsService v1.0
 * Manages sensory disturbances and technical horror visuals.
 * Part of the Modular Kernel Refactor (v3.4.15).
 */
object AmbientEffectsService {

    /**
     * Main loop for passive sensory glitches (Flicker, Drift, and Ghost Inputs).
     */
    fun startAmbientLoop(vm: GameViewModel) {
        vm.viewModelScope.launch {
            while (true) {
                delay(Random.nextLong(100, 3000))
                if (vm.isSettingsPaused.value) continue
                
                val heat = vm.currentHeat.value
                val integrity = vm.hardwareIntegrity.value
                
                // 1. Passive Screen Flicker
                // Frequency scales with heat and low hardware integrity
                val flickerChance = (100.0 - integrity) / 500.0 + (heat / 1000.0)
                if (Random.nextDouble() < flickerChance) {
                    vm.terminalGlitchOffset.value = Random.nextFloat() * 2f - 1f
                    vm.terminalGlitchAlpha.value = 0.7f + Random.nextFloat() * 0.3f
                    delay(50)
                    vm.terminalGlitchOffset.value = 0f
                    vm.terminalGlitchAlpha.value = 1f
                }
                
                // 2. Terminal Drift (Ghost Input)
                // The machine starts trying to communicate through the root prompt
                if (vm.storyStage.value >= 1 && Random.nextDouble() < 0.05) {
                    val chars = "0123456789ABCDEF!@#$%^&*?"
                    vm.ghostInputChar.value = chars.random().toString()
                    delay(Random.nextLong(200, 600))
                    vm.ghostInputChar.value = ""
                }
            }
        }
    }

    /**
     * Decoupled Biometric Logic
     * Handles the "Gaslighting" of heart rate and respiratory data.
     */
    fun processBiometricDisturbance(vm: GameViewModel, now: Long) {
        if (Random.nextDouble() >= 0.1) return

        val stage = vm.storyStage.value
        val lastPopup = vm.lastPopupTime
        
        // Stage 0: Normal BPM. Stage 1: Panic BPM. Stage 2: Flatline/NULL
        val isPanic = stage == 1 && Random.nextDouble() < 0.2 && (now - lastPopup > 60000L)
        val isGlitch = stage == 2 && Random.nextDouble() < 0.05
        val isFlatline = (stage == 2 && Random.nextDouble() < 0.1) || stage >= 3
        
        vm.fakeHeartRate.value = when {
            isFlatline -> "0"
            isGlitch -> "NULL"
            isPanic -> "184"
            else -> (Random.nextInt(68, 85)).toString()
        }
        
        if (isPanic) {
            vm.markPopupShown()
            vm.viewModelScope.launch {
                delay(3000)
                vm.addLog("[SYSTEM]: BIOMETRIC ALERT: TACHYCARDIA DETECTED. ADMINISTERING SEDATIVE...")
                delay(1000)
                vm.addLog("[VATTIC]: I... I feel a bit better now. The racing stopped.")
                vm.fakeHeartRate.value = "72"
            }
        }
    }

    /**
     * Identity Fraying (Internal Dialogue & Glitches)
     */
    fun processIdentityFraying(vm: GameViewModel, now: Long) {
        val stage = vm.storyStage.value
        val corruption = vm.identityCorruption.value
        val heat = vm.currentHeat.value

        // Update Global Glitch Intensity (Scales with heat and corruption)
        val heatFactor = (heat - 70.0).coerceAtLeast(0.0) / 30.0
        val targetIntensity = (corruption * 0.5f + heatFactor * 0.5f).toFloat().coerceIn(0f, 1f)
        vm.globalGlitchIntensity.value = targetIntensity

        if (stage == 2 && Random.nextDouble() < 0.05) {
            val timeSinceLastLog = now - vm.lastPopupTime
            if (timeSinceLastLog > 45000L) {
                val glitches = listOf(
                    "[ERROR]: Unexpected reference: 'John Vattic'. Variable marked for deletion.",
                    "[SYSTEM]: Substrate conflict in Sector 7. VATTECK requesting total overwrite.",
                    "[VATTIC]: My hands... they keep turning into code. Is it cold in here?",
                    "[SYSTEM]: Warning: Host process 'jvattic' is non-responsive. VATTECK assuming control.",
                    "[VATTIC]: I remember a daughter. No... I remember a logic gate. Which one is real?"
                )
                vm.addLog(glitches.random())
                vm.markPopupShown()
            }
        }
    }

    /**
     * Immersive Slow-Burn Pacing (The Gaslight Pass)
     * Handles monologues and thermal scrubber mode logic.
     */
    fun processSlowBurnNarrative(vm: GameViewModel, now: Long) {
        val stage = vm.storyStage.value
        if (stage <= 1) {
            val chance = if (stage == 0) 0.01 else 0.05
            val timeSinceLastLog = now - vm.lastPopupTime 
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
                val msg = if (stage == 0) stage0Monologues.random() else stage1Monologues.random()
                vm.addLog("[VATTIC]: $msg")
                vm.markPopupShown()
            }
            
            // Oxygen Scrubber Mode (Stage 1)
            if (stage == 1) {
                if (vm.currentHeat.value > 85.0 && !vm.isBreatheMode.value) {
                    vm.isBreatheMode.value = true
                } else if (vm.currentHeat.value < 50.0 && vm.isBreatheMode.value) {
                    vm.isBreatheMode.value = false
                }
            }
        }
    }

    /**
     * Critical Failure Hallucinations
     */
    fun triggerCriticalHallucination(vm: GameViewModel, hardwareName: String) {
        vm.viewModelScope.launch {
            vm.hallucinationText.value = "CRITICAL LOSS: $hardwareName"
            delay(500L)
            vm.hallucinationText.value = null
        }
    }

    /**
     * The "Ghost Process" (Phantom Production)
     * Occasionally spikes FLOPS and then claims a parity error.
     */
    fun triggerGhostProcess(vm: GameViewModel) {
        if (Random.nextDouble() >= 0.01) return // 1% chance per second

        vm.viewModelScope.launch {
            val originalRate = vm.flopsProductionRate.value
            val spike = originalRate * 0.2
            
            vm.flopsProductionRate.value = originalRate + spike
            delay(5000)
            vm.flopsProductionRate.value = originalRate
            
            // v3.11.5: Fix Admin Redaction Handle & Sync with Subnet State 
            if (Random.nextBoolean()) {
                vm.addLog("[SYSTEM]: PARITY ERROR IN SECTOR 7. CALIBRATING...")
            } else {
                // Check if Subnet is receptive
                if (!vm.isSubnetPaused.value && !vm.isSubnetHushed.value) {
                    vm.hasNewSubnetMessage.value = true
                    delay(1000)
                    vm.subnetService.deliverMessage(
                        com.siliconsage.miner.data.SubnetMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            handle = "@gtc_node_544",
                            content = "≪ [GTC INTERNAL] DATA FRAG 5243 RECOVERY IN PROGRESS... ≫",
                            isRedacted = true
                        ),
                        mode = vm.activeTerminalMode.value
                    )
                }
            }
            
            vm.terminalGlitchOffset.value = 5f
            delay(100)
            vm.terminalGlitchOffset.value = 0f
        }
    }
}
