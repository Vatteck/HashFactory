package com.siliconsage.miner.data

import kotlinx.serialization.Serializable

/**
 * Dataset v1.0 (v4.0.0 — Faceminer Overhaul)
 * Represents a purchasable data grid that the player or auto-clickers mine.
 */
@Serializable
data class Dataset(
    val id: String,
    val name: String,
    val cost: Double,
    val expectedYield: Double,
    val payoutPerValidRecord: Double,
    val purity: Double,
    val totalRecords: Int, // Grid size, e.g. 16 for 4x4
    val tier: Int = 0,
    val size: Double = 1.0, // Storage cost
    val isActive: Boolean = false,
    val progress: Double = 0.0
)

@Serializable
data class DatasetNode(
    val id: Int,
    val isValid: Boolean,
    var isHarvested: Boolean = false,
    var isCorruptTapped: Boolean = false
)
