package com.siliconsage.miner.util

import com.siliconsage.miner.data.ComputeContract
import com.siliconsage.miner.data.VerificationBlock
import com.siliconsage.miner.data.VerificationState
import kotlin.random.Random

/**
 * VerificationEngine v1.0 (v3.31.0 - Phase 2 Contract Minigame)
 * Handles grid generation, scoring, and yield scaling based on accuracy.
 */
object VerificationEngine {

    private val validDataLabels = listOf(
        "0xA3F2", "SYS_OK", "CHK_SUM", "DATA_SYNC", "BLK_VLD", 
        "HASH_MT", "ACK_RTR", "MEM_CLR", "KEY_GEN", "NODE_UP"
    )

    private val corruptDataLabels = listOf(
        "NULL_PTR", "ERR_SYS", "OOM_KIL", "SEG_FLT", "BAD_SEC", 
        "INF_LOP", "RD_FAIL", "SYNC_LOST", "CORRUPT", "NO_RSP"
    )

    // Glitched labels used when corruption is high, blending valid and corrupt traits
    private val glitchedLabels = listOf(
        "0xNUL2", "S?S_OK", "CHK_ERR", "DATA_DMP", "B?K_VLD",
        "H?SH_FL", "ACK_LST", "MEM_LEK", "C??RUPT", "N?DE_DN"
    )

    /**
     * Generate a 4x4 grid (16 blocks) based on the contract's purity and player's corruption.
     */
    fun generateVerificationGrid(contract: ComputeContract, playerCorruption: Double): VerificationState {
        val totalBlocks = 16
        // Purity dictates how many blocks are valid. 
        // Example: 90% purity = ~14 valid blocks. 50% purity = ~8 valid blocks.
        val targetValidCount = (totalBlocks * contract.purity).toInt().coerceIn(4, 14)
        
        val blocks = mutableListOf<VerificationBlock>()
        
        // Use glitched labels if corruption is high
        val useGlitched = playerCorruption >= 0.7

        for (i in 0 until totalBlocks) {
            val isValid = i < targetValidCount
            
            val label = if (isValid) {
                if (useGlitched && Random.nextDouble() < 0.3) glitchedLabels.random() else validDataLabels.random()
            } else {
                if (useGlitched && Random.nextDouble() < 0.3) glitchedLabels.random() else corruptDataLabels.random()
            }

            blocks.add(VerificationBlock(id = i, label = label, isValid = isValid))
        }

        blocks.shuffle()

        // Time scales with tier. Higher tiers give slightly less time relative to complexity.
        val baseTimeMs = when (contract.tier) {
            0 -> 8000L
            1 -> 10000L
            2 -> 12000L
            3 -> 15000L
            else -> 18000L
        }

        return VerificationState(
            blocks = blocks,
            timeRemainingMs = baseTimeMs,
            contract = contract,
            score = 0
        )
    }

    /**
     * Evaluate accuracy and return a multiplier ranging from 0.4 to 1.5.
     */
    fun calculateYieldMultiplier(state: VerificationState): Double {
        val totalValid = state.blocks.count { it.isValid }
        val maxPossibleScore = totalValid.toDouble()
        
        if (maxPossibleScore == 0.0) return 1.0 // Fallback if no valid blocks exist
        
        // Calculate raw accuracy ratio
        val accuracy = (state.score.toDouble() / maxPossibleScore).coerceIn(0.0, 1.0)

        return when {
            accuracy >= 0.9 -> 1.5 // Perfect / Near Perfect: +50% bonus
            accuracy >= 0.7 -> 1.0 // Decent: baseline yield
            accuracy >= 0.4 -> 0.7 // Poor: -30% penalty
            else -> 0.4            // Terrible: -60% penalty
        }
    }
}
