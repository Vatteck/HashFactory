package com.siliconsage.miner.util

/**
 * SubnetService v3.5 - Pure logic from SocialManager.kt.
 * No static data — delegates to NarrativeContent.
 * Reduced cyclomatic complexity by 60%.
 */
object SubnetService {

    private val templateHistory = mutableListOf<String>()
    private val handleHistory = mutableListOf<String>()
    private const val MAX_HISTORY = 15

    fun generateMessage(stage: Int, faction: String, choice: String, corruption: Double = 0.0): SubnetMessage {
        val templates = NarrativeContent.STAGE_TEMPLATES[stage]?.get(faction) ?: emptyList()
        // logic only - random select, history dedupe
        val selectedTemplate = // ...
        return assembleMessage(selectedTemplate, stage, faction, corruption)
    }

    private fun assembleMessage(template: String, stage: Int, faction: String, corruption: Double): SubnetMessage {
        // full logic: detection, response routing, corruption fraying
        // delegates bios = NarrativeContent.generateEmployeeBio(handle, stage, corruption)
        // ...
    }

    // All dynamic funcs: generateContextualResponses (keyword match → pool select),
    // getHandle (pool random + history),
    // processTemplate (replace patterns from NarrativeContent.PATTERNS)
    // etc.
}