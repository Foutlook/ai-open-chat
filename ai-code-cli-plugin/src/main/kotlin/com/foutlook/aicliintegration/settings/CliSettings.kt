package com.foutlook.aicliintegration.service

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.foutlook.aicliintegration.model.CliType

/**
 * CLI 设置存储
 */
@State(
    name = "AiCliIntegrationSettings",
    storages = [Storage("ai-cli-integration.xml")]
)
class CliSettings : PersistentStateComponent<CliSettings> {
    var openCodeCliPath = ""
    var codexCliPath = ""
    var claudeCodeCliPath = ""
    
    var enableRealTimeSync = true
    var autoApplyChanges = false
    var showDiffPreview = true
    var syncDelayMs = 500L
    
    var lastUsedCliType: String? = null

    override fun getState(): CliSettings = this

    override fun loadState(state: CliSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
