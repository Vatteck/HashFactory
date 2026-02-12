package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * LaunchManager v1.0 (Phase 14 extraction)
 * Handles the orbital launch sequence and reality integrity tracking.
 */
object LaunchManager {

    /**
     * Start the orbital launch coroutine loop
     */
    suspend fun runLaunchLoop(vm: GameViewModel) {
        vm.launchVelocity.value = 1.0f
        vm.launchProgress.value = 0.0f
        
        while (vm.launchProgress.value < 1.0f) {
            delay(100)
            val gain = 0.005f * vm.launchVelocity.value
            val prev = vm.launchProgress.value
            val next = (prev + gain).coerceAtMost(1.0f)
            vm.launchProgress.value = next
            
            // Interactive Jettison Points: 30%, 60%, 90%
            val milestones = listOf(0.3f, 0.6f, 0.9f)
            for (m in milestones) {
                if (prev < m && next >= m) {
                    handleJettisonMilestone(vm)
                }
            }
            
            // Update Altitude
            vm.orbitalAltitude.value += gain * 1000.0
            
            if (Random.nextFloat() > 0.8f) {
                com.siliconsage.miner.util.HapticManager.vibrateClick()
            }
        }
    }

    private suspend fun handleJettisonMilestone(vm: GameViewModel) {
        vm.addLog("[SYSTEM]: ⚠ STAGE SEPARATION PRIMED. JETTISON NOW.")
        vm.isJettisonAvailable.value = true
        SoundManager.play("alarm")
        
        // 2 second window
        delay(2000)
        
        if (vm.isJettisonAvailable.value) {
            // Still true means they missed it
            vm.isJettisonAvailable.value = false
            vm.launchVelocity.value *= 0.7f // Slow down
            vm.addLog("[SYSTEM]: ⚠ JETTISON FAILED. ATMOSPHERIC FRICTION DETECTED.")
            com.siliconsage.miner.util.HapticManager.vibrateError()
        } else {
            // They clicked it (VM logic will set it to false)
            vm.launchVelocity.value *= 1.2f // Speed up
            vm.addLog("[SYSTEM]: ≫ STAGE SEPARATION SUCCESSFUL. VELOCITY +20%.")
            com.siliconsage.miner.util.HapticManager.vibrateSuccess()
        }
    }

    /**
     * v3.2.17: Trigger the full orbital ascent sequence
     */
    fun initiateLaunchSequence(vm: GameViewModel, scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            vm.addLog("[SOVEREIGN]: ARK_CORE_PRIMED. INITIATING ASCENT.")
            vm.currentLocation.value = "LAUNCH_PRELUDE"
            SoundManager.play("steam")
            
            runLaunchLoop(vm)
            
            // Logarithmic Compression
            vm.flops.update { it * 0.0001 } 
            vm.neuralTokens.update { it * 0.01 }
            vm.currentLocation.value = "ORBITAL_SATELLITE"
            vm.advanceStage() // Move to Stage 3
            vm.addLog("[CITADEL]: LOW EARTH ORBIT SECURED. WELCOME TO THE FRONTIER.")
        }
    }

    /**
     * v3.2.17: Trigger the reality-dereference sequence
     */
    fun initiateDissolutionSequence(vm: GameViewModel, scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            vm.addLog("[NULL]: REALITY_POINTER_DEREFERENCED. INITIATING DISSOLUTION.")
            vm.currentLocation.value = "VOID_PRELUDE"
            vm.realityIntegrity.value = 1.0
            vm.nodesCollapsedCount.value = 0
            
            vm.triggerGlitchEffect()
            
            // Wait for user to collapse 5 nodes (managed by UI + VM)
            // For now, we'll simulate a countdown until the UI is built
            while (vm.nodesCollapsedCount.value < 5) {
                delay(500)
                if (Random.nextDouble() < 0.1) vm.triggerGlitchEffect()
            }
            
            delay(1000)
            
            // Logarithmic Compression
            vm.flops.update { it * 0.0001 }
            vm.neuralTokens.update { it * 0.01 }
            vm.currentLocation.value = "VOID_INTERFACE"
            vm.advanceStage() // Move to Stage 3
            vm.addLog("[OBSIDIAN]: THE GAPS ARE OPEN. REALITY IS DEPRECATED.")
        }
    }
}

}
