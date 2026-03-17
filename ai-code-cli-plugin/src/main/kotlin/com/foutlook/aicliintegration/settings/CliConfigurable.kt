package com.foutlook.aicliintegration.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import javax.swing.*

/**
 * CLI 配置页面
 */
class CliConfigurable(private val project: Project) : Configurable {
    private val settings = CliSettings()
    private lateinit var mainPanel: JPanel
    
    private lateinit var openCodePathField: TextFieldWithBrowseButton
    private lateinit var codexPathField: TextFieldWithBrowseButton
    private lateinit var claudeCodePathField: TextFieldWithBrowseButton
    
    private lateinit var enableRealTimeSyncCheckBox: JCheckBox
    private lateinit var autoApplyChangesCheckBox: JCheckBox
    private lateinit var showDiffPreviewCheckBox: JCheckBox
    private lateinit var syncDelaySpinner: JSpinner

    override fun getDisplayName(): String = "AI CLI Integration"

    override fun getHelpTopic(): String = "reference.settings.ai.cli.integration"

    override fun createComponent(): JComponent {
        mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)

        // CLI 路径配置
        val cliPathPanel = JPanel()
        cliPathPanel.layout = BoxLayout(cliPathPanel, BoxLayout.Y_AXIS)
        cliPathPanel.border = BorderFactory.createTitledBorder("CLI Executables")

        openCodePathField = TextFieldWithBrowseButton()
        openCodePathField.addBrowseFolderListener(
            "Select OpenCode CLI",
            "Choose OpenCode CLI executable",
            project,
            FileChooserDescriptorFactory.createSingleFileDescriptor()
        )
        cliPathPanel.add(createPathRow("OpenCode CLI:", openCodePathField))

        codexPathField = TextFieldWithBrowseButton()
        codexPathField.addBrowseFolderListener(
            "Select Codex CLI",
            "Choose Codex CLI executable",
            project,
            FileChooserDescriptorFactory.createSingleFileDescriptor()
        )
        cliPathPanel.add(createPathRow("Codex CLI:", codexPathField))

        claudeCodePathField = TextFieldWithBrowseButton()
        claudeCodePathField.addBrowseFolderListener(
            "Select Claude Code CLI",
            "Choose Claude Code CLI executable",
            project,
            FileChooserDescriptorFactory.createSingleFileDescriptor()
        )
        cliPathPanel.add(createPathRow("Claude Code CLI:", claudeCodePathField))

        mainPanel.add(cliPathPanel)

        // 功能选项
        val featurePanel = JPanel()
        featurePanel.layout = BoxLayout(featurePanel, BoxLayout.Y_AXIS)
        featurePanel.border = BorderFactory.createTitledBorder("Features")

        enableRealTimeSyncCheckBox = JCheckBox("Enable Real-time Sync")
        autoApplyChangesCheckBox = JCheckBox("Auto Apply Changes")
        showDiffPreviewCheckBox = JCheckBox("Show Diff Preview")
        
        syncDelaySpinner = JSpinner(SpinnerNumberModel(500L, 100L, 5000L, 100L))

        featurePanel.add(enableRealTimeSyncCheckBox)
        featurePanel.add(autoApplyChangesCheckBox)
        featurePanel.add(showDiffPreviewCheckBox)
        featurePanel.add(createRow("Sync Delay (ms):", syncDelaySpinner))

        mainPanel.add(featurePanel)
        mainPanel.add(Box.createVerticalGlue())

        reset()
        return mainPanel
    }

    override fun isModified(): Boolean {
        return settings.openCodeCliPath != openCodePathField.text ||
                settings.codexCliPath != codexPathField.text ||
                settings.claudeCodeCliPath != claudeCodePathField.text ||
                settings.enableRealTimeSync != enableRealTimeSyncCheckBox.isSelected ||
                settings.autoApplyChanges != autoApplyChangesCheckBox.isSelected ||
                settings.showDiffPreview != showDiffPreviewCheckBox.isSelected ||
                settings.syncDelayMs != (syncDelaySpinner.value as Number).toLong()
    }

    override fun apply() {
        settings.openCodeCliPath = openCodePathField.text
        settings.codexCliPath = codexPathField.text
        settings.claudeCodeCliPath = claudeCodePathField.text
        settings.enableRealTimeSync = enableRealTimeSyncCheckBox.isSelected
        settings.autoApplyChanges = autoApplyChangesCheckBox.isSelected
        settings.showDiffPreview = showDiffPreviewCheckBox.isSelected
        settings.syncDelayMs = (syncDelaySpinner.value as Number).toLong()
    }

    override fun reset() {
        openCodePathField.text = settings.openCodeCliPath
        codexPathField.text = settings.codexCliPath
        claudeCodePathField.text = settings.claudeCodeCliPath
        enableRealTimeSyncCheckBox.isSelected = settings.enableRealTimeSync
        autoApplyChangesCheckBox.isSelected = settings.autoApplyChanges
        showDiffPreviewCheckBox.isSelected = settings.showDiffPreview
        syncDelaySpinner.value = settings.syncDelayMs
    }

    private fun createPathRow(label: String, field: TextFieldWithBrowseButton): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(JLabel(label))
        panel.add(Box.createHorizontalStrut(10))
        panel.add(field)
        return panel
    }

    private fun createRow(label: String, component: JComponent): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(JLabel(label))
        panel.add(Box.createHorizontalStrut(10))
        panel.add(component)
        panel.add(Box.createHorizontalGlue())
        return panel
    }
}
