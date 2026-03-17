package com.foutlook.aicliintegration.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.foutlook.aicliintegration.model.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 多 CLI 实例管理服务
 * 支持同时运行多个 CLI 工具实例
 */
@Service(Service.Level.PROJECT)
class CliInstanceManagerService(private val project: Project) {
    
    // 存储所有活跃的 CLI 实例
    private val instances = ConcurrentHashMap<String, ActiveCliInstance>()
    
    // 事件监听器
    private val listeners = CopyOnWriteArrayList<CliInstanceListener>()

    /**
     * 启动新的 CLI 实例
     */
    fun startCliInstance(instance: CliInstance): Boolean {
        return try {
            if (instances.containsKey(instance.id)) {
                notifyError(instance.id, "Instance already exists")
                return false
            }

            // 创建进程
            val processBuilder = ProcessBuilder(instance.executablePath)
            processBuilder.directory(java.io.File(instance.workingDirectory))
            processBuilder.redirectErrorStream(false)
            
            val process = processBuilder.start()
            
            // 创建活跃实例包装
            val activeInstance = ActiveCliInstance(
                instance.copy(status = ConnectionStatus.CONNECTED),
                process,
                BufferedWriter(process.outputStream.bufferedWriter()),
                BufferedReader(InputStreamReader(process.inputStream)),
                BufferedReader(InputStreamReader(process.errorStream))
            )
            
            instances[instance.id] = activeInstance
            
            // 启动输出读取线程
            startOutputReader(instance.id, activeInstance)
            
            // 触发事件
            notifyEvent(CliInstanceEvent.Connected(instance.id))
            notifyEvent(CliInstanceEvent.Created(instance.id, activeInstance.config))
            
            true
        } catch (e: Exception) {
            notifyError(instance.id, "Failed to start CLI: ${e.message}")
            false
        }
    }

    /**
     * 停止 CLI 实例
     */
    fun stopCliInstance(instanceId: String) {
        val active = instances.remove(instanceId) ?: return
        try {
            active.process?.destroy()
            active.writer?.close()
            active.reader?.close()
            active.errorReader?.close()
        } catch (e: Exception) {
            // 忽略关闭错误
        }
        notifyEvent(CliInstanceEvent.Disconnected(instanceId))
        notifyEvent(CliInstanceEvent.Removed(instanceId))
    }

    /**
     * 向 CLI 实例发送命令
     */
    fun sendCommandToInstance(instanceId: String, command: String): Boolean {
        val active = instances[instanceId] ?: return false
        return try {
            active.writer?.write(command)
            active.writer?.newLine()
            active.writer?.flush()
            true
        } catch (e: Exception) {
            notifyError(instanceId, "Failed to send command: ${e.message}")
            false
        }
    }

    /**
     * 向 CLI 实例发送文件内容
     */
    fun sendFileToInstance(instanceId: String, filePath: String, content: String): Boolean {
        val command = """
            {
                "action": "analyze_file",
                "file": "$filePath",
                "content": ${escapeJson(content)}
            }
        """.trimIndent()
        return sendCommandToInstance(instanceId, command)
    }

    /**
     * 获取所有活跃的 CLI 实例
     */
    fun getAllInstances(): List<CliInstance> {
        return instances.values.map { it.config }
    }

    /**
     * 获取特定实例
     */
    fun getInstance(instanceId: String): CliInstance? {
        return instances[instanceId]?.config
    }

    /**
     * 获取实例数量
     */
    fun getInstanceCount(): Int = instances.size

    /**
     * 添加事件监听器
     */
    fun addListener(listener: CliInstanceListener) {
        listeners.add(listener)
    }

    /**
     * 移除事件监听器
     */
    fun removeListener(listener: CliInstanceListener) {
        listeners.remove(listener)
    }

    /**
     * 清空所有实例
     */
    fun clearAllInstances() {
        instances.keys.forEach { stopCliInstance(it) }
    }

    private fun startOutputReader(instanceId: String, active: ActiveCliInstance) {
        // 读取标准输出
        Thread {
            try {
                active.reader?.use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        notifyOutput(instanceId, OutputType.STDOUT, line!!)
                    }
                }
            } catch (e: Exception) {
                notifyError(instanceId, "STDOUT reader error: ${e.message}")
            }
        }.start()

        // 读取错误输出
        Thread {
            try {
                active.errorReader?.use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        notifyOutput(instanceId, OutputType.STDERR, line!!)
                    }
                }
            } catch (e: Exception) {
                notifyError(instanceId, "STDERR reader error: ${e.message}")
            }
        }.start()
    }

    private fun notifyEvent(event: CliInstanceEvent) {
        listeners.forEach { it.onInstanceEvent(event) }
    }

    private fun notifyOutput(instanceId: String, type: OutputType, content: String) {
        val output = CliInstanceOutput(instanceId, type, content)
        notifyEvent(CliInstanceEvent.Output(instanceId, output))
    }

    private fun notifyError(instanceId: String, message: String) {
        notifyEvent(CliInstanceEvent.Error(instanceId, message))
    }

    private fun escapeJson(text: String): String {
        return "\"${text.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")}\""
    }

    /**
     * 活跃 CLI 实例内部数据类
     */
    private data class ActiveCliInstance(
        val config: CliInstance,
        val process: Process?,
        val writer: BufferedWriter?,
        val reader: BufferedReader?,
        val errorReader: BufferedReader?
    )
}

/**
 * CLI 实例事件监听器
 */
interface CliInstanceListener {
    fun onInstanceEvent(event: CliInstanceEvent)
}
