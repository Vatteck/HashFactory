package com.siliconsage.miner.data

/**
 * VerificationState v1.0 (v3.31.0 - Phase 2 Contract Minigame)
 */

data class VerificationBlock(
    val id: Int,
    val label: String,    // E.g., "0xA3F2", "NULL_PTR"
    val isValid: Boolean, // True if the block is good data
    val isTapped: Boolean = false
)

data class VerificationState(
    val blocks: List<VerificationBlock>,  // Typically 16 blocks for a 4x4 grid
    val timeRemainingMs: Long,            // Minigame countdown
    val contract: ComputeContract,        // The contract being verified
    val score: Int = 0                    // Current minigame score based on taps
)
