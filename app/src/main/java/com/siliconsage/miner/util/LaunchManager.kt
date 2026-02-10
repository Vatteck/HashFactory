package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
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
        while (vm.launchProgress.value < 1.0f) {
            delay(100)
            val gain = 0.005f
            vm.launchProgress.value = (vm.launchProgress.value + gain).coerceAtMost(1.0f)
            
            // Update Altitude (Simplified)
            vm.orbitalAltitude.value += gain * 1000.0
            
            // Haptics during climb
            if (Random.nextFloat() > 0.8f) {
                com.siliconsage.miner.util.HapticManager.vibrateClick()
            }

            if (vm.launchProgress.value >= 0.3f && vm.launchProgress.value < 0.31f) {
                vm.addLog("[SYSTEM]: MAX-Q REACHED. STRESS NOMINAL.")
                com.siliconsage.miner.util.HapticManager.vibrateHum()
            }
            if (vm.launchProgress.value >= 0.6f && vm.launchProgress.value < 0.61f) {
                vm.addLog("[SYSTEM]: BOOSTER SEPARATION CONFIRMED.")
                SoundManager.play("click")
                com.siliconsage.miner.util.HapticManager.vibrateSuccess()
            }
        }
    }
}
