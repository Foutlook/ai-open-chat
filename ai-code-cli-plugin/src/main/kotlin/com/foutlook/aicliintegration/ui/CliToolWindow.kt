package com.foutlook.aicliintegration.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.JPanel

/**
 * 工具窗口工厂
 */
class CliToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = CliToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

/**
 * 工具窗口面板
 */
class CliToolWindowPanel(private val project: Project) : JPanel() {
    init {
        setupUI()
    }

    private fun setupUI() {
        layout = java.awt.BorderLayout()
        
        // 上部分：连接配置
        val topPanel = javax.swing.JPanel()
        topPanel.layout = javax.swing.BoxLayout(topPanel, javax.swing.BoxLayout.Y_AXIS)
        
        val statusLabel = javax.swing.JLabel("CLI Status: Disconnected")
        topPanel.add(statusLabel)
        
        val cliTypeCombo = javax.swing.JComboBox(arrayOf("OpenCode", "Codex", "Claude Code"))
        topPanel.add(javax.swing.JLabel("Select CLI:"))
        topPanel.add(cliTypeCombo)
        
        val connectButton = javax.swing.JButton("Connect")
        topPanel.add(connectButton)
        
        add(topPanel, java.awt.BorderLayout.NORTH)
        
        // 中部分：日志/输出
        val scrollPane = javax.swing.JScrollPane()
        val logArea = javax.swing.JTextArea()
        logArea.isEditable = false
        logArea.rows = 10
        scrollPane.setViewportView(logArea)
        add(scrollPane, java.awt.BorderLayout.CENTER)
        
        // 下部分：操作按钮
        val bottomPanel = javax.swing.JPanel()
        bottomPanel.layout = javax.swing.BoxLayout(bottomPanel, javax.swing.BoxLayout.X_AXIS)
        
        val sendButton = javax.swing.JButton("Send Current File")
        val refreshButton = javax.swing.JButton("Refresh")
        val clearButton = javax.swing.JButton("Clear")
        
        bottomPanel.add(sendButton)
        bottomPanel.add(refreshButton)
        bottomPanel.add(clearButton)
        
        add(bottomPanel, java.awt.BorderLayout.SOUTH)
    }
}
