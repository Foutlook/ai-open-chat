package com.foutlook.aicliintegration.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.foutlook.aicliintegration.model.CliType
import com.foutlook.aicliintegration.model.CliInstance
import com.foutlook.aicliintegration.service.CliInstanceManagerService
import java.util.*

/**
 * 启动新的 CLI 实例
 */
class StartCliInstanceAction : AnAction("Start CLI Instance") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        // 选择 CLI 类型
        val cliTypes = arrayOf("OpenCode", "Codex", "Claude Code")
        val selectedIndex = Messages.showChooseDialog(
            project,
            "Choose CLI type:",
            "Start CLI",
            cliTypes,
            cliTypes[0],
            null
        )
        if (selectedIndex < 0) return

        val cliType = when (selectedIndex) {
            0 -> CliType.OPENCODE
            1 -> CliType.CODEX
            2 -> CliType.CLAUDE_CODE
            else -> return
        }

        // 选择可执行文件
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
        val file = FileChooser.chooseFile(descriptor, project, null) ?: return

        // 获取工作目录
        val workDir = Messages.showInputDialog(
            project,
            "Enter working directory (optional):",
            "Working Directory",
            Messages.getQuestionIcon(),
            project.basePath,
            null
        ) ?: project.basePath ?: ""

        // 创建并启动 CLI 实例
        val instanceName = "${cliType.name}-${Date().time % 10000}"
        val instance = CliInstance(
            name = instanceName,
            cliType = cliType,
            executablePath = file.path,
            workingDirectory = workDir
        )

        val managerService = project.getService(CliInstanceManagerService::class.java)
        if (managerService.startCliInstance(instance)) {
            Messages.showInfoMessage(
                project,
                "CLI instance '$instanceName' started successfully",
                "Success"
            )
            
            // 打开工具窗口
            val toolWindow = com.intellij.openapi.wm.ToolWindowManager.getInstance(project)
                .getToolWindow("AICliInstances")
            toolWindow?.activate(null)
        } else {
            Messages.showErrorDialog(project, "Failed to start CLI instance", "Error")
        }
    }
}

/**
 * 停止 CLI 实例
 */
class StopCliInstanceAction : AnAction("Stop CLI Instance") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val managerService = project.getService(CliInstanceManagerService::class.java)
        
        val instances = managerService.getAllInstances()
        if (instances.isEmpty()) {
            Messages.showWarningDialog(project, "No active CLI instances", "Warning")
            return
        }

        val instanceNames = instances.map { it.name }.toTypedArray()
        val selectedIndex = Messages.showChooseDialog(
            project,
            "Select instance to stop:",
            "Stop CLI",
            instanceNames,
            instanceNames[0],
            null
        )
        if (selectedIndex >= 0) {
            val instanceId = instances[selectedIndex].id
            managerService.stopCliInstance(instanceId)
            Messages.showInfoMessage(project, "Instance stopped", "Info")
        }
    }
}

/**
 * 打开 CLI 实例面板
 */
class OpenCliInstancesPanel : AnAction("Open CLI Instances Panel") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = com.intellij.openapi.wm.ToolWindowManager.getInstance(project)
            .getToolWindow("AICliInstances")
        toolWindow?.activate(null)
    }
}

/**
 * 发送当前文件到选定的 CLI 实例
 */
class SendFileToCliInstanceAction : AnAction("Send to CLI Instance") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR.getData(e.dataContext) ?: return
        val document = editor.document
        val file = com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext)
        
        val managerService = project.getService(CliInstanceManagerService::class.java)
        val instances = managerService.getAllInstances()
        
        if (instances.isEmpty()) {
            Messages.showWarningDialog(project, "No active CLI instances", "Warning")
            return
        }

        val instanceNames = instances.map { it.name }.toTypedArray()
        val selectedIndex = Messages.showChooseDialog(
            project,
            "Select CLI instance:",
            "Send File",
            instanceNames,
            instanceNames[0],
            null
        )
        
        if (selectedIndex >= 0) {
            val instanceId = instances[selectedIndex].id
            val filePath = file?.path ?: "unknown"
            val content = document.text
            
            if (managerService.sendFileToInstance(instanceId, filePath, content)) {
                Messages.showInfoMessage(project, "File sent to ${instances[selectedIndex].name}", "Success")
            } else {
                Messages.showErrorDialog(project, "Failed to send file", "Error")
            }
        }
    }
}
