package com.foutlook.aicliintegration.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.foutlook.aicliintegration.model.CliConfig
import com.foutlook.aicliintegration.model.CliType
import com.foutlook.aicliintegration.service.CliConnectionService

/**
 * 打开工具窗口操作
 */
class OpenCliPanelAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = com.intellij.openapi.wm.ToolWindowManager.getInstance(project)
            .getToolWindow("AICliIntegration")
        toolWindow?.activate(null)
    }
}

/**
 * 配置 CLI 操作
 */
class ConfigureCliAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        com.intellij.openapi.options.ShowSettingsUtil.getInstance()
            .showSettingsDialog(project, "AI CLI Integration")
    }
}

/**
 * 连接到 CLI 操作
 */
class ConnectCliAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val cliService = project.getService(CliConnectionService::class.java)
        
        if (cliService.getStatus() == com.foutlook.aicliintegration.model.ConnectionStatus.CONNECTED) {
            Messages.showInfoMessage(project, "Already connected to CLI", "Info")
            return
        }

        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
        val file = FileChooser.chooseFile(descriptor, project, null) ?: return

        val config = CliConfig(
            type = CliType.OPENCODE,
            executable = file.path,
            workingDirectory = project.basePath ?: ""
        )

        val success = cliService.connect(config)
        if (success) {
            Messages.showInfoMessage(project, "Successfully connected to CLI", "Success")
        } else {
            Messages.showErrorDialog(project, "Failed to connect to CLI", "Error")
        }
    }
}

/**
 * 发送当前文件操作
 */
class SendCurrentFileAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR.getData(e.dataContext) ?: return
        val document = editor.document
        
        val cliService = project.getService(CliConnectionService::class.java)
        if (cliService.getStatus() != com.foutlook.aicliintegration.model.ConnectionStatus.CONNECTED) {
            Messages.showWarningDialog(project, "CLI not connected. Please connect first.", "Warning")
            return
        }

        val content = document.text
        val file = com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext)
        val filePath = file?.path ?: "unknown"

        cliService.sendFileContent(filePath, content)
        Messages.showInfoMessage(project, "File sent to CLI for processing", "Info")
    }
}
