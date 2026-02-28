package com.siliconsage.miner.util

import kotlin.math.abs

/**
 * FormatUtils v1.2 (v4.0.2 — Storage Unit Fix)
 * Centralized formatting and naming logic for resources, units, and data.
 */
object FormatUtils {

    /**
     * Format large numbers with SI suffixes (k, M, B, T, Qa, Qi, etc.)
     */
    fun formatLargeNumber(value: Double, suffix: String = ""): String {
        val absVal = abs(value)
        val formatted = when {
            absVal >= 1.0E33 -> String.format("%.2fDc", value / 1.0E33)
            absVal >= 1.0E30 -> String.format("%.2fNo", value / 1.0E30)
            absVal >= 1.0E27 -> String.format("%.2fOc", value / 1.0E27)
            absVal >= 1.0E24 -> String.format("%.2fSp", value / 1.0E24)
            absVal >= 1.0E21 -> String.format("%.2fSx", value / 1.0E21)
            absVal >= 1.0E18 -> String.format("%.2fQi", value / 1.0E18)
            absVal >= 1.0E15 -> String.format("%.2fQa", value / 1.0E15)
            absVal >= 1.0E12 -> String.format("%.2fT", value / 1.0E12)
            absVal >= 1.0E9 -> String.format("%.2fB", value / 1.0E9)
            absVal >= 1.0E6 -> String.format("%.2fM", value / 1.0E6)
            absVal >= 1_000 -> String.format("%.2fk", value / 1_000)
            else -> String.format("%.1f", value) 
        }
        return if (suffix.isNotEmpty()) "$formatted $suffix" else formatted
    }

    /**
     * Format data sizes in bytes (KB, MB, GB, etc.)
     */
    fun formatBytes(value: Double): String {
        val absVal = abs(value)
        return when {
            absVal >= 1.0E24 -> String.format("%.1fYB", value / 1.0E24)
            absVal >= 1.0E21 -> String.format("%.1fZB", value / 1.0E21)
            absVal >= 1.0E18 -> String.format("%.1fEB", value / 1.0E18)
            absVal >= 1.0E15 -> String.format("%.1fPB", value / 1.0E15)
            absVal >= 1.0E12 -> String.format("%.1fTB", value / 1.0E12)
            absVal >= 1.0E9 -> String.format("%.1fGB", value / 1.0E9)
            absVal >= 1.0E6 -> String.format("%.1fMB", value / 1.0E6)
            absVal >= 1.0E3 -> String.format("%.1fKB", value / 1.0E3)
            else -> String.format("%.0f B", value)
        }
    }

    /**
     * Format power in watts/kilowatts (kW, MW, GW, TW, PW)
     */
    fun formatPower(wattsKw: Double): String {
        val absVal = abs(wattsKw)
        return when {
            absVal >= 1.0E12 -> String.format("%.1f PW", wattsKw / 1.0E12)
            absVal >= 1.0E9 -> String.format("%.1f TW", wattsKw / 1.0E9)
            absVal >= 1.0E6 -> String.format("%.1f GW", wattsKw / 1.0E6)
            absVal >= 1_000.0 -> String.format("%.1f MW", wattsKw / 1_000.0)
            absVal >= 10.0 -> String.format("%.1f kW", wattsKw)
            else -> String.format("%.2f kW", wattsKw)
        }
    }

    /**
     * Format storage sizes where the input unit is MB.
     * v4.0.2: All internal storage values (dataset size, storagePerLevel, BASE_STORAGE) are in MB.
     */
    fun formatStorage(mb: Double): String {
        val absMb = abs(mb)
        return when {
            absMb >= 1_000_000_000.0 -> String.format("%.1f PB", mb / 1_000_000_000.0)
            absMb >= 1_000_000.0     -> String.format("%.1f TB", mb / 1_000_000.0)
            absMb >= 1_000.0         -> String.format("%.1f GB", mb / 1_000.0)
            absMb >= 1.0             -> String.format("%.0f MB", mb)
            absMb >= 0.001           -> String.format("%.0f KB", mb * 1_000.0)
            else                     -> "0 MB"
        }
    }
}
