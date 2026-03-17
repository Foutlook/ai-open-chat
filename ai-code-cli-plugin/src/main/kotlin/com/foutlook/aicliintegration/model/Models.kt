package com.foutlook.aicliintegration.model

/**
 * 支持的 CLI 类型
 */
enum class CliType {
    OPENCODE, CODEX, CLAUDE_CODE
}

/**
 * CLI 配置
 */
data class CliConfig(
    val type: CliType,
    val executable: String,
    val workingDirectory: String,
    val enableRealTimeSync: Boolean = true,
    val maxOutputSize: Long = 10 * 1024 * 1024 // 10MB
)

/**
 * CLI 连接状态
 */
enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

/**
 * 代码变更事件
 */
data class CodeChangeEvent(
    val filePath: String,
    val oldContent: String,
    val newContent: String,
    val startLine: Int,
    val endLine: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * CLI 响应
 */
data class CliResponse(
    val success: Boolean,
    val message: String,
    val changedFiles: List<FileChange> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val error: String? = null
)

/**
 * 文件变更
 */
data class FileChange(
    val path: String,
    val operation: FileOperation,
    val content: String? = null,
    val diff: String? = null
)

/**
 * 文件操作类型
 */
enum class FileOperation {
    CREATE, MODIFY, DELETE, RENAME
}
