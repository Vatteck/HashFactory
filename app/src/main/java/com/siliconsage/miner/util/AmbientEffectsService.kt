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
}
