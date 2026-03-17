package com.foutlook.aicliintegration.model

import java.util.UUID

/**
 * 单个 CLI 实例的状态
 */
data class CliInstance(
    val id: String = UUID.randomUUID().toString(),
    val name: String,                           // 显示名称，如 "OpenCode-1"
    val cliType: CliType,
    val executablePath: String,
    val workingDirectory: String,
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val createdAt: Long = System.currentTimeMillis(),
    var lastActivity: Long = System.currentTimeMillis()
)

/**
 * CLI 实例输出
 */
data class CliInstanceOutput(
    val instanceId: String,
    val type: OutputType,                       // STDOUT, STDERR, LOG
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 输出类型
 */
enum class OutputType {
    STDOUT, STDERR, LOG, ERROR, SUCCESS
}

/**
 * CLI 实例事件
 */
sealed class CliInstanceEvent {
    abstract val instanceId: String
    data class Created(override val instanceId: String, val instance: CliInstance) : CliInstanceEvent()
    data class Connected(override val instanceId: String) : CliInstanceEvent()
    data class Disconnected(override val instanceId: String) : CliInstanceEvent()
    data class Output(override val instanceId: String, val output: CliInstanceOutput) : CliInstanceEvent()
    data class Error(override val instanceId: String, val error: String) : CliInstanceEvent()
    data class Removed(override val instanceId: String) : CliInstanceEvent()
}
