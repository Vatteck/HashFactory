package com.siliconsage.miner.data

import kotlinx.serialization.Serializable

/**
 * ComputeContract v1.0 (v3.30.0 — Compute Contracts Economy)
 * Represents a purchasable compute job that yields NEUR on completion.
 */
@Serializable
data class ComputeContract(
    val id: String,            // "batch_job_01"
    val name: String,          // "GTC Batch Job"
    val cost: Double,          // NEUR cost to purchase
    val expectedYield: Double, // Max NEUR payout
    val purity: Double,        // 0.0-1.0, drives yield variance
    val processingTime: Long,  // Base time in ms to complete at baseline flops
    val progress: Double = 0.0,// 0.0 to 1.0
    val isActive: Boolean = false,
    val tier: Int = 0          // Stage gate
)
