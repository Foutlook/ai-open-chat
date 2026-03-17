package com.foutlook.aicliintegration.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.foutlook.aicliintegration.service.CliInstanceManagerService
import com.foutlook.aicliintegration.model.CliInstanceEvent
import com.foutlook.aicliintegration.model.CliInstanceListener
import com.foutlook.aicliintegration.model.OutputType
import javax.swing.*

/**
 * 多 CLI 实例工具窗口工厂
 */
class CliInstancesToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = MultiCliInstancePanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

/**
 * 多 CLI 实例管理面板
 */
class MultiCliInstancePanel(private val project: Project) : JPanel() {
    
    private val managerService = project.getService(CliInstanceManagerService::class.java)
    private lateinit var instancesCombo: JComboBox<String>
    private lateinit var logArea: JTextArea
    private lateinit var statusLabel: JLabel
    private lateinit var instanceListPanel: JPanel
    private val instanceTabs = mutableMapOf<String, JTextArea>()

    init {
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        layout = java.awt.BorderLayout()

        // 上部分：实例选择和控制
        val topPanel = JPanel()
        topPanel.layout = java.awt.FlowLayout(java.awt.FlowLayout.LEFT)

        topPanel.add(JLabel("Active Instances:"))
        instancesCombo = JComboBox()
        topPanel.add(instancesCombo)

        val startButton = JButton("Start")
        val stopButton = JButton("Stop")
        val refreshButton = JButton("Refresh")

        startButton.addActionListener { showStartDialog() }
        stopButton.addActionListener { stopSelectedInstance() }
        refreshButton.addActionListener { refreshInstanceList() }

        topPanel.add(startButton)
        topPanel.add(stopButton)
        topPanel.add(refreshButton)

        statusLabel = JLabel("Ready")
        topPanel.add(statusLabel)

        add(topPanel, java.awt.BorderLayout.NORTH)

        // 中部分：标签页显示每个实例的输出
        val tabbedPane = JTabbedPane()

        // 总体输出面板
        logArea = JTextArea()
        logArea.isEditable = false
        logArea.rows = 15
        logArea.lineWrap = true
        logArea.wrapStyleWord = true
        val scrollPane = JScrollPane(logArea)
        tabbedPane.addTab("Console", scrollPane)

        // 实例列表
        instanceListPanel = JPanel()
        instanceListPanel.layout = BoxLayout(instanceListPanel, BoxLayout.Y_AXIS)
        val listScrollPane = JScrollPane(instanceListPanel)
        tabbedPane.addTab("Instances", listScrollPane)

        add(tabbedPane, java.awt.BorderLayout.CENTER)

        // 下部分：操作按钮
        val bottomPanel = JPanel()
        bottomPanel.layout = java.awt.FlowLayout(java.awt.FlowLayout.RIGHT)

        val sendFileButton = JButton("Send File")
        val clearButton = JButton("Clear Output")

        sendFileButton.addActionListener { sendFileToInstance() }
        clearButton.addActionListener { logArea.text = "" }

        bottomPanel.add(sendFileButton)
        bottomPanel.add(clearButton)

        add(bottomPanel, java.awt.BorderLayout.SOUTH)

        refreshInstanceList()
    }

    private fun setupListeners() {
        managerService.addListener(object : CliInstanceListener {
            override fun onInstanceEvent(event: CliInstanceEvent) {
                SwingUtilities.invokeLater {
                    when (event) {
                        is CliInstanceEvent.Created -> {
                            refreshInstanceList()
                            statusLabel.text = "Instance created: ${event.instance.name}"
                        }
                        is CliInstanceEvent.Connected -> {
                            statusLabel.text = "Connected to ${event.instanceId}"
                        }
                        is CliInstanceEvent.Output -> {
                            val instance = managerService.getInstance(event.instanceId)
                            val prefix = when (event.output.type) {
                                OutputType.STDOUT -> "[${instance?.name}] "
                                OutputType.STDERR -> "[${instance?.name}] ERROR: "
                                OutputType.LOG -> "[LOG] "
                                OutputType.ERROR -> "[ERROR] "
                                OutputType.SUCCESS -> "[SUCCESS] "
                            }
                            logArea.append("$prefix${event.output.content}\n")
                            logArea.caretPosition = logArea.document.length
                        }
                        is CliInstanceEvent.Error -> {
                            logArea.append("[ERROR] ${event.error}\n")
                            statusLabel.text = "Error: ${event.error}"
                        }
                        is CliInstanceEvent.Disconnected -> {
                            statusLabel.text = "Disconnected: ${event.instanceId}"
                            refreshInstanceList()
                        }
                        is CliInstanceEvent.Removed -> {
                            refreshInstanceList()
                            statusLabel.text = "Instance removed"
                        }
                    }
                }
            }
        })
    }

    private fun refreshInstanceList() {
        val instances = managerService.getAllInstances()
        
        instancesCombo.removeAllItems()
        instances.forEach { instancesCombo.addItem(it.name) }
        
        // 更新实例列表
        instanceListPanel.removeAll()
        instances.forEach { instance ->
            val panel = JPanel(java.awt.BorderLayout())
            panel.border = BorderFactory.createTitledBorder(instance.name)
            
            val infoLabel = JLabel("""
                ID: ${instance.id}
                Type: ${instance.cliType}
                Status: ${instance.status}
                CLI: ${instance.executablePath}
            """.trimIndent())
            panel.add(infoLabel, java.awt.BorderLayout.WEST)
            
            val stopBtn = JButton("Stop")
            stopBtn.addActionListener {
                managerService.stopCliInstance(instance.id)
                refreshInstanceList()
            }
            panel.add(stopBtn, java.awt.BorderLayout.EAST)
            
            instanceListPanel.add(panel)
        }
        
        instanceListPanel.revalidate()
        instanceListPanel.repaint()
    }

    private fun showStartDialog() {
        // 通过操作系统的对话框，由 Action 处理
        val action = com.foutlook.aicliintegration.action.StartCliInstanceAction()
        val e = com.intellij.openapi.actionSystem.AnActionEvent.createFromAnAction(
            action,
            null,
            "",
            com.intellij.openapi.actionSystem.ActionPlaces.UNKNOWN,
            com.intellij.openapi.actionSystem.DataContext { null }
        )
        action.actionPerformed(e)
    }

    private fun stopSelectedInstance() {
        val selectedIndex = instancesCombo.selectedIndex
        if (selectedIndex >= 0) {
            val instances = managerService.getAllInstances()
            if (selectedIndex < instances.size) {
                managerService.stopCliInstance(instances[selectedIndex].id)
                refreshInstanceList()
            }
        }
    }

    private fun sendFileToInstance() {
        val action = com.foutlook.aicliintegration.action.SendFileToCliInstanceAction()
        val e = com.intellij.openapi.actionSystem.AnActionEvent.createFromAnAction(
            action,
            null,
            "",
            com.intellij.openapi.actionSystem.ActionPlaces.UNKNOWN,
            com.intellij.openapi.actionSystem.DataContext { null }
        )
        action.actionPerformed(e)
    }
}
