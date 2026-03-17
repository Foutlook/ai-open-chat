package com.foutlook.aicliintegration.listener

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.ProjectManager
import com.foutlook.aicliintegration.model.CodeChangeEvent
import com.foutlook.aicliintegration.service.CodeSyncService
import java.util.concurrent.ConcurrentHashMap
import java.util.Timer
import kotlin.concurrent.timer

/**
 * 文档变更监听器 - 监听代码编辑事件
 */
class DocumentChangeListener : DocumentListener {
    private val pendingChanges = ConcurrentHashMap<String, String>()
    private var syncTimer: Timer? = null

    override fun documentChanged(event: DocumentEvent) {
        val document = event.document
        val fragment = event.newFragment
        val offset = event.offset
        
        // 获取所有项目并查找对应的虚拟文件
        ProjectManager.getInstance().openProjects.forEach { project ->
            val file = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance()
                .getFile(document) ?: return@forEach
            
            val filePath = file.path
            val changeEvent = CodeChangeEvent(
                filePath = filePath,
                oldContent = event.oldFragment.toString(),
                newContent = fragment.toString(),
                startLine = calculateLineNumber(document, offset),
                endLine = calculateLineNumber(document, offset + fragment.length)
            )
            
            val codeSyncService = project.getService(CodeSyncService::class.java)
            codeSyncService.recordChange(changeEvent)
            
            // 启用防抖 (debounce) 机制
            scheduleSync(codeSyncService, filePath)
        }
    }

    private fun calculateLineNumber(document: com.intellij.openapi.editor.Document, offset: Int): Int {
        return document.getLineNumber(offset.coerceAtMost(document.textLength))
    }

    private fun scheduleSync(codeSyncService: CodeSyncService, filePath: String) {
        syncTimer?.cancel()
        syncTimer = timer(initialDelay = 500, period = 0) {
            // 触发同步逻辑
            cancel()
        }
    }
}
