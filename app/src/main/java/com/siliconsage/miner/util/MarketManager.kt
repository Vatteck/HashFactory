package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlin.random.Random

/**
 * MarketManager v1.0 (Phase 14 extraction)
 * Handles market rate calculations, news parsing, and economic volatility.
 */
object MarketManager {

    data class MarketTickResult(
        val marketMultiplier: Double,
        val thermalRateModifier: Double,
        val energyPriceMultiplier: Double,
        val newsProductionMultiplier: Double,
        val triggerStoryProgression: Boolean = false,
        val triggerGlitch: Boolean = false
    )

    fun updateMarket(vm: GameViewModel) {
        val headline = HeadlineManager.generateHeadline(
            faction = vm.faction.value,
            stage = vm.storyStage.value,
            currentHeat = vm.currentHeat.value,
            isTrueNull = vm.isTrueNull.value,
            isSovereign = vm.isSovereign.value,
            isUnity = vm.isUnity.value,
            location = vm.currentLocation.value,
            corruption = vm.identityCorruption.value,
            playerRank = vm.playerRank.value,
            aquiferLevel = vm.aquiferLevel.value,
            isQuotaActive = vm.isQuotaActive.value,
            contractsCompleted = vm.contractsCompleted.value // v3.32.0
        )
        
        vm.updateNews(headline)
        
        // v3.5.12: Action Reactions - 20% chance for peons to react to the news (10% at Stage 0)
        val reactionChance = if (vm.storyStage.value == 0) 0.10 else 0.20
        if (Random.nextDouble() < reactionChance) {
            val cleanHeadline = headline.substringBefore("[").trim()
            vm.triggerSubnetReaction("NEWS", cleanHeadline)
        }
        
        val result = parseHeadline(headline)
        
        if (result.triggerGlitch) {
            HapticManager.vibrateGlitch()
            SoundManager.play("glitch")
        }
        if (result.triggerStoryProgression) {
             vm.checkTransitionsPublic()
        }
        
        var energyPrice = result.energyPriceMultiplier
        
        // Faction Power Logic
        if (vm.faction.value == "HIVEMIND") {
            energyPrice *= 0.7
            if (vm.activePowerUsage.value > vm.maxPowerkW.value * 0.5 && Random.nextDouble() < 0.05) {
                val fines = vm.neuralTokens.value * 0.05
                vm.updateNeuralTokens(-fines)
                vm.addLog("[SYSTEM]: GRID SIPHON DETECTED by Utility Co. Fined ${vm.formatLargeNumber(fines)} \$N.")
            }
        } else if (vm.faction.value == "SANCTUARY") {
             if (energyPrice > 0.15) energyPrice = 0.15
        }

        val convRate = calculateConversionRate(
            baseRate = vm.getBaseRate(),
            marketMultiplier = result.marketMultiplier,
            faction = vm.faction.value,
            unlockedNodes = vm.unlockedTechNodes.value
        )

        vm.setMarketModifiers(
            marketMult = result.marketMultiplier,
            thermalMod = result.thermalRateModifier,
            energyMult = energyPrice,
            newsProdMult = result.newsProductionMultiplier,
            convRate = convRate
        )

        // v4.0.0: Refresh available datasets each market tick
        vm.refreshDatasets()
        
        if (result.marketMultiplier > 1.0) SoundManager.play("market_up")
        else if (result.marketMultiplier < 1.0) SoundManager.play("market_down")
    }

    /**
     * Parse news headline tags and return economic modifiers
     */
    fun parseHeadline(headline: String): MarketTickResult {
        var mult = 1.0
        var heatMod = 1.0
        var energyPriceMult = 0.15 // Base
        
        if (headline.contains("[BULL]")) mult = 1.2
        if (headline.contains("[BEAR]")) mult = 0.8
        
        if (headline.contains("[HEAT_UP]")) heatMod = 1.1
        if (headline.contains("[HEAT_DOWN]")) heatMod = 0.9
        
        if (headline.contains("[ENERGY_SPIKE]")) energyPriceMult = 0.45 // 3x Cost
        else if (headline.contains("[ENERGY_DROP]")) energyPriceMult = 0.08 // Half Cost

        return MarketTickResult(
            marketMultiplier = mult,
            thermalRateModifier = heatMod,
            energyPriceMultiplier = energyPriceMult,
            newsProductionMultiplier = if (mult > 1.0) 1.1 else 0.9,
            triggerStoryProgression = headline.contains("[STORY_PROG]"),
            triggerGlitch = headline.contains("[GLITCH]")
        )
    }

    /**
     * Calculate the new conversion rate based on market factors and faction bonuses
     */
    fun calculateConversionRate(
        baseRate: Double,
        marketMultiplier: Double,
        faction: String,
        unlockedNodes: List<String>
    ): Double {
        val volatility = Random.nextDouble(0.95, 1.05)
        var newRate = baseRate * marketMultiplier * volatility
        
        if (faction == "SANCTUARY") {
            newRate *= 1.2
        }
        
        if (unlockedNodes.contains("symbiotic_evolution")) {
            newRate *= 3.0
        }

        return newRate.coerceAtLeast(0.01)
    }
}
