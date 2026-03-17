package com.foutlook.aicliintegration.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.foutlook.aicliintegration.model.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.CopyOnWriteArrayList

/**
 * CLI 连接管理服务
 */
@Service(Service.Level.PROJECT)
class CliConnectionService(private val project: Project) {
    private var currentProcess: Process? = null
    private var currentConfig: CliConfig? = null
    private var connectionStatus = ConnectionStatus.DISCONNECTED
    
    private val listeners = CopyOnWriteArrayList<ConnectionListener>()
    private val responseListeners = CopyOnWriteArrayList<ResponseListener>()

    fun addConnectionListener(listener: ConnectionListener) {
        listeners.add(listener)
    }

    fun removeConnectionListener(listener: ConnectionListener) {
        listeners.remove(listener)
    }

    fun addResponseListener(listener: ResponseListener) {
        responseListeners.add(listener)
    }

    fun removeResponseListener(listener: ResponseListener) {
        responseListeners.remove(listener)
    }

    /**
     * 连接到 CLI
     */
    fun connect(config: CliConfig): Boolean {
        return try {
            connectionStatus = ConnectionStatus.CONNECTING
            notifyStatusChange(ConnectionStatus.CONNECTING)
            
            disconnect()
            
            val processBuilder = ProcessBuilder(config.executable)
            processBuilder.directory(java.io.File(config.workingDirectory))
            processBuilder.redirectErrorStream(true)
            
            currentProcess = processBuilder.start()
            currentConfig = config
            connectionStatus = ConnectionStatus.CONNECTED
            notifyStatusChange(ConnectionStatus.CONNECTED)
            
            startOutputReader()
            true
        } catch (e: Exception) {
            connectionStatus = ConnectionStatus.ERROR
            notifyStatusChange(ConnectionStatus.ERROR)
            notifyError(e)
            false
        }
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        currentProcess?.destroy()
        currentProcess = null
        currentConfig = null
        connectionStatus = ConnectionStatus.DISCONNECTED
        notifyStatusChange(ConnectionStatus.DISCONNECTED)
    }

    /**
     * 发送命令到 CLI
     */
    fun sendCommand(command: String): CliResponse {
        return try {
            if (currentProcess == null) {
                return CliResponse(false, "CLI not connected", error = "No active CLI process")
            }

            val output = mutableListOf<String>()
            val writer = currentProcess!!.outputStream.bufferedWriter()
            writer.write(command)
            writer.newLine()
            writer.flush()

            // 简单的响应读取 (实际场景需要更复杂的协议)
            val response = CliResponse(true, "Command sent successfully")
            notifyResponse(response)
            response
        } catch (e: Exception) {
            val errorResponse = CliResponse(false, "Failed to send command", error = e.message)
            notifyResponse(errorResponse)
            errorResponse
        }
    }

    /**
     * 发送文件内容到 CLI
     */
    fun sendFileContent(filePath: String, content: String): CliResponse {
        val command = """
            {
                "action": "analyze_file",
                "file": "$filePath",
                "content": ${escapeJson(content)}
            }
        """.trimIndent()
        return sendCommand(command)
    }

    /**
     * 获取连接状态
     */
    fun getStatus(): ConnectionStatus = connectionStatus

    private fun startOutputReader() {
        Thread {
            try {
                BufferedReader(InputStreamReader(currentProcess!!.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        handleCliOutput(line!!)
                    }
                }
            } catch (e: Exception) {
                notifyError(e)
            }
        }.start()
    }

    private fun handleCliOutput(output: String) {
        try {
            // 解析 JSON 响应
            val response = parseResponse(output)
            notifyResponse(response)
        } catch (e: Exception) {
            // 忽略解析错误的输出
        }
    }

    private fun parseResponse(output: String): CliResponse {
        // TODO: 实现 JSON 解析逻辑
        return CliResponse(true, output)
    }

    private fun notifyStatusChange(status: ConnectionStatus) {
        listeners.forEach { it.onStatusChanged(status) }
    }

    private fun notifyResponse(response: CliResponse) {
        responseListeners.forEach { it.onResponse(response) }
    }

    private fun notifyError(error: Exception) {
        responseListeners.forEach { it.onError(error) }
    }

    private fun escapeJson(text: String): String {
        return "\"${text.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")}\""
    }
}

interface ConnectionListener {
    fun onStatusChanged(status: ConnectionStatus)
}

interface ResponseListener {
    fun onResponse(response: CliResponse)
    fun onError(error: Exception)
}
