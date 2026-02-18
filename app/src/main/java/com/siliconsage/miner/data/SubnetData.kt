package com.siliconsage.miner.data

enum class InteractionType {
    COMPLIANT, ENGINEERING, HIJACK, HARVEST, COMMAND_LEAK, GHOST_LINK
}

data class SubnetResponse(
    val text: String,
    val riskDelta: Double = 0.0,
    val productionBonus: Double = 1.0, 
    val followsUp: Boolean = false,
    val nextNodeId: String? = null,
    val commandToInject: String? = null,
    val cost: Double = 0.0
)

data class SubnetMessage(
    val id: String,
    val handle: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val interactionType: InteractionType? = null,
    val availableResponses: List<SubnetResponse> = emptyList(),
    val threadId: String? = null,
    val nodeId: String? = null,
    val timeoutMs: Long? = null,
    val isForceReply: Boolean = false,
    val employeeInfo: EmployeeInfo? = null,
    val isIndented: Boolean = false
)

data class EmployeeInfo(
    val bio: String,
    val department: String,
    val heartRate: Int,
    val respiration: String,
    val stressLevel: Double,
    val specialActions: List<SubnetResponse> = emptyList()
)

data class ThreadNode(
    val content: String, 
    val responses: List<SubnetResponse>, 
    val timeoutMs: Long? = null, 
    val timeoutNodeId: String? = null
)
