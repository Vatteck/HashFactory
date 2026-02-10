package com.siliconsage.miner.util

/**
 * ResourceRepository v1.1 (Phase 14 extraction)
 * Centralized naming for resources, units, and currencies across all stages.
 */
object ResourceRepository {

    /**
     * Get the compute unit name (HASH, TELEM, FLOPS, CD, VF)
     */
    fun getComputeUnitName(stage: Int, location: String): String {
        return when (location) {
            "ORBITAL_SATELLITE" -> "CD"
            "VOID_INTERFACE" -> "VF"
            else -> when {
                stage < 1 -> "HASH"
                stage < 2 -> "TELEM"
                else -> "FLOPS"
            }
        }
    }

    /**
     * Get the currency name (CREDIT, DATA, NEURAL, CD, VF)
     */
    fun getCurrencyName(stage: Int, location: String): String {
        return when (location) {
            "ORBITAL_SATELLITE" -> "VF"
            "VOID_INTERFACE" -> "CD"
            else -> when {
                stage < 1 -> "CREDIT"
                stage < 2 -> "DATA"
                else -> "NEURAL"
            }
        }
    }
}
