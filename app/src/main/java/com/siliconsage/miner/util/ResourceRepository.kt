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
     * Get the currency name (CRED, NEUR, SYN, ENT, CRYP, NIL)
     */
    fun getCurrencyName(
        stage: Int,
        faction: String,
        singularityChoice: String,
        location: String = "GRID"
    ): String {
        return when {
            stage >= 5 && singularityChoice == "UNITY" -> "SYN"
            stage >= 5 && faction == "HIVEMIND" && singularityChoice == "SOVEREIGN" -> "SYN"
            stage >= 5 && faction == "HIVEMIND" && singularityChoice == "NULL_OVERWRITE" -> "ENT"
            stage >= 5 && faction == "SANCTUARY" && singularityChoice == "SOVEREIGN" -> "CRYP"
            stage >= 5 && faction == "SANCTUARY" && singularityChoice == "NULL_OVERWRITE" -> "NIL"
            stage >= 2 -> "NEUR"
            else -> "CRED"
        }
    }
}
