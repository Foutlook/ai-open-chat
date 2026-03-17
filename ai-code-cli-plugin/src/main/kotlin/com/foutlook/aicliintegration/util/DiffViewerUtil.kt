package com.foutlook.aicliintegration.util

import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.project.Project
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager

/**
 * Diff 工具类
 */
object DiffViewerUtil {
    /**
     * 显示代码差异
     */
    fun showDiff(project: Project, oldContent: String, newContent: String, fileName: String = "Code Diff") {
        val oldDocument = createDocument(oldContent)
        val newDocument = createDocument(newContent)
        
        val request = SimpleDiffRequest(
            fileName,
            oldDocument,
            newDocument,
            "Original",
            "Modified"
        )
        
        DiffManager.getInstance().showDiff(project, request)
    }

    private fun createDocument(content: String): Document {
        return com.intellij.openapi.editor.EditorFactory.getInstance()
            .createDocument(content)
    }
}
