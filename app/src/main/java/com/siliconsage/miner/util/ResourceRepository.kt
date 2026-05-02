package com.siliconsage.miner.util

/**
 * ResourceRepository v1.1 (Phase 14 extraction)
 * Centralized naming for resources, units, and currencies across all stages.
 */
object ResourceRepository {

    /**
     * Get the compute unit name (HASH, FLOPS, CD, VF, SYN)
     */
    fun getComputeUnitName(
        stage: Int,
        location: String,
        faction: String = "NONE",
        singularityChoice: String = "NONE"
    ): String {
        return when {
            stage >= 5 && singularityChoice == "UNITY" -> "SYN"
            location == "ORBITAL_SATELLITE" -> "CD"
            location == "VOID_INTERFACE" -> "VF"
            stage >= 2 -> "FLOPS"
            else -> "HASH"
        }
    }

    /**
     * Get the single spendable wallet label. Compute-unit flavor can shift by stage,
     * but the wallet stays $FLOPS so the UI never implies a second spendable pool.
     */
    fun getCurrencyName(
        stage: Int,
        faction: String,
        singularityChoice: String,
        location: String = "GRID"
    ): String = "\$FLOPS"
}
