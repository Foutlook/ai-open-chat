package com.foutlook.aicliintegration.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.application.ApplicationManager
import com.foutlook.aicliintegration.model.CodeChangeEvent
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 代码同步服务 - 处理 IDE 和 CLI 之间的代码同步
 */
@Service(Service.Level.PROJECT)
class CodeSyncService(private val project: Project) {
    private val syncListeners = CopyOnWriteArrayList<SyncListener>()
    private var pendingChanges = mutableMapOf<String, CodeChangeEvent>()
    
    fun addSyncListener(listener: SyncListener) {
        syncListeners.add(listener)
    }

    fun removeSyncListener(listener: SyncListener) {
        syncListeners.remove(listener)
    }

    /**
     * 应用 CLI 返回的代码变更
     */
    fun applyCodeChanges(filePath: String, newContent: String) {
        ApplicationManager.getApplication().invokeLater {
            try {
                val file = findVirtualFile(filePath)
                if (file != null) {
                    file.getDocument()?.apply {
                        setText(newContent)
                    }
                    notifyChangesApplied(filePath, newContent)
                }
            } catch (e: Exception) {
                notifyError("Failed to apply changes: ${e.message}")
            }
        }
    }

    /**
     * 记录待处理的代码变更
     */
    fun recordChange(event: CodeChangeEvent) {
        pendingChanges[event.filePath] = event
        notifyChangeRecorded(event)
    }

    /**
     * 获取待处理的变更
     */
    fun getPendingChanges(): Map<String, CodeChangeEvent> = pendingChanges.toMap()

    /**
     * 清除待处理的变更
     */
    fun clearPendingChanges() {
        pendingChanges.clear()
    }

    private fun findVirtualFile(filePath: String): VirtualFile? {
        val baseDir = project.basePath ?: return null
        val file = java.io.File(filePath)
        return com.intellij.openapi.vfs.LocalFileSystem.getInstance()
            .findFileByIoFile(file)
    }

    private fun notifyChangesApplied(filePath: String, content: String) {
        syncListeners.forEach { it.onChangesApplied(filePath, content) }
    }

    private fun notifyChangeRecorded(event: CodeChangeEvent) {
        syncListeners.forEach { it.onChangeRecorded(event) }
    }

    private fun notifyError(message: String) {
        syncListeners.forEach { it.onSyncError(message) }
    }
}

interface SyncListener {
    fun onChangesApplied(filePath: String, content: String)
    fun onChangeRecorded(event: CodeChangeEvent)
    fun onSyncError(message: String)
}

// 为 VirtualFile 添加扩展函数
private fun VirtualFile.getDocument(): com.intellij.openapi.editor.Document? {
    return com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(this)
}
