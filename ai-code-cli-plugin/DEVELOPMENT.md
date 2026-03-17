# IDEA AI Code CLI 集成插件 - 开发指南

## 概览

本项目是一个功能完整的 IntelliJ IDEA 插件，用于集成本地 AI 代码生成 CLI 工具（OpenCode、Codex、Claude Code）。通过该插件，开发者可以在 IDEA 中直接使用这些 AI 工具，实现代码的实时改进和优化。

## 核心特性

### ✅ 已实现的功能

1. **多 CLI 支持框架**
   - 支持 OpenCode、Codex、Claude Code
   - 可扩展的 CLI 类型系统
   - 实现了 `CliConfig` 配置管理

2. **连接管理**
   - `CliConnectionService` - 处理 CLI 进程生命周期
   - 连接状态管理（DISCONNECTED → CONNECTING → CONNECTED）
   - 错误处理和自动重连机制

3. **代码同步**
   - `CodeSyncService` - 管理代码变更
   - `DocumentChangeListener` - 监听 IDE 编辑事件
   - 变更记录和应用机制

4. **UI 组件**
   - 工具窗口（Tool Window）- 显示 CLI 状态和日志
   - 设置页面（Settings） - 配置 CLI 路径和选项
   - 菜单项和操作（Actions）- 快速访问常用功能

5. **协议支持**
   - JSON 格式的请求/响应
   - `CliProtocolParser` - 灵活的响应解析
   - Diff 显示工具

## 项目架构

### 层级结构

```
┌─────────────────────────────────────────┐
│     UI 层                               │
│  ├─ CliToolWindow (工具窗口)            │
│  ├─ CliConfigurable (设置页面)          │
│  └─ CliActions (菜单操作)               │
├─────────────────────────────────────────┤
│     业务逻辑层                          │
│  ├─ CliConnectionService (连接管理)     │
│  ├─ CodeSyncService (代码同步)          │
│  └─ CliSettings (设置管理)              │
├─────────────────────────────────────────┤
│     数据层                              │
│  ├─ Models (数据模型)                   │
│  ├─ CliProtocolParser (协议解析)        │
│  └─ DiffViewerUtil (差异工具)           │
├─────────────────────────────────────────┤
│     事件监听层                          │
│  ├─ DocumentChangeListener (文档变更)   │
│  ├─ ConnectionListener (连接状态)       │
│  └─ ResponseListener (响应事件)         │
└─────────────────────────────────────────┘
```

## 核心服务详解

### 1. CliConnectionService

**职责**：管理与本地 CLI 工具的进程通信

```kotlin
// 关键方法
connect(config: CliConfig): Boolean          // 连接
disconnect()                                 // 断开连接
sendCommand(command: String): CliResponse    // 发送命令
sendFileContent(filePath, content)           // 发送文件
getStatus(): ConnectionStatus                // 获取状态
```

**工作流程**：
```
1. 创建进程 → ProcessBuilder
2. 启动进程 → Process.start()
3. 监听输出 → BufferedReader
4. 解析响应 → CliProtocolParser
5. 触发事件 → ResponseListener
```

### 2. CodeSyncService

**职责**：协调 IDE 和 CLI 之间的代码同步

```kotlin
// 关键方法
recordChange(event: CodeChangeEvent)        // 记录变更
applyCodeChanges(filePath, newContent)      // 应用变更
getPendingChanges(): Map<...>                // 获取待处理变更
```

**变更流程**：
```
IDE 代码编辑
    ↓
DocumentChangeListener 捕获事件
    ↓
CodeSyncService.recordChange()
    ↓
发送到 CLI（可选）
    ↓
CLI 返回建议
    ↓
CodeSyncService.applyCodeChanges()
    ↓
IDE 显示变更
```

### 3. DocumentChangeListener

**职责**：实时监听 IDE 中的代码编辑事件

```kotlin
override fun documentChanged(event: DocumentEvent)
    // 1. 获取变更
    // 2. 创建 CodeChangeEvent
    // 3. 通知 CodeSyncService
    // 4. 考虑防抖处理
```

## API 使用示例

### 连接到 CLI

```kotlin
val project = ProjectManager.getInstance().openProjects.first()
val cliService = project.getService(CliConnectionService::class.java)

val config = CliConfig(
    type = CliType.OPENCODE,
    executable = "/usr/local/bin/opencode",
    workingDirectory = project.basePath!!,
    enableRealTimeSync = true
)

if (cliService.connect(config)) {
    println("Connected successfully")
} else {
    println("Connection failed")
}
```

### 发送代码分析

```kotlin
val response = cliService.sendFileContent(
    "/path/to/file.java",
    "public class Main {}"
)

if (response.success) {
    response.changedFiles.forEach { change ->
        println("File: ${change.path}, Operation: ${change.operation}")
    }
}
```

### 监听连接状态变更

```kotlin
cliService.addConnectionListener(object : ConnectionListener {
    override fun onStatusChanged(status: ConnectionStatus) {
        when (status) {
            ConnectionStatus.CONNECTED -> println("Connected")
            ConnectionStatus.DISCONNECTED -> println("Disconnected")
            else -> {}
        }
    }
})
```

### 监听代码同步事件

```kotlin
val syncService = project.getService(CodeSyncService::class.java)
syncService.addSyncListener(object : SyncListener {
    override fun onChangesApplied(filePath: String, content: String) {
        println("Applied changes to $filePath")
    }

    override fun onChangeRecorded(event: CodeChangeEvent) {
        println("Recorded change: ${event.filePath}")
    }

    override fun onSyncError(message: String) {
        println("Sync error: $message")
    }
})
```

## 扩展开发指南

### 添加新的 CLI 类型

#### 步骤 1：定义 CLI 类型

```kotlin
// 修改 Models.kt
enum class CliType {
    OPENCODE, CODEX, CLAUDE_CODE, MY_NEW_CLI
}
```

#### 步骤 2：创建 CLI 适配器

```kotlin
// 新建 adapter/MyCliAdapter.kt
class MyCliAdapter(config: CliConfig) {
    fun parseResponse(rawOutput: String): CliResponse {
        // 实现特定的响应解析逻辑
        return CliResponse(success = true, message = "Parsed")
    }

    fun formatRequest(filePath: String, content: String): String {
        // 格式化请求格式
        return "my-cli analyze $filePath"
    }
}
```

#### 步骤 3：集成到 CliConnectionService

```kotlin
// 修改 CliConnectionService.kt
private fun getAdapter(config: CliConfig): CliAdapter? {
    return when (config.type) {
        CliType.MY_NEW_CLI -> MyCliAdapter(config)
        else -> null
    }
}
```

### 自定义 UI 组件

#### 添加新的工具栏按钮

```kotlin
// 修改 CliToolWindow.kt
private fun addCustomButton(panel: JPanel) {
    val customButton = JButton("My Custom Action")
    customButton.addActionListener {
        performCustomAction()
    }
    panel.add(customButton)
}
```

#### 添加新的设置项

```kotlin
// 修改 CliConfigurable.kt
private lateinit var myNewOption: JCheckBox

override fun createComponent(): JComponent {
    myNewOption = JCheckBox("Enable My Feature")
    mainPanel.add(createRow("My Feature:", myNewOption))
    return mainPanel
}
```

### 实现自定义同步策略

```kotlin
// 新建 CustomSyncStrategy.kt
class CustomSyncStrategy : SyncListener {
    override fun onChangesApplied(filePath: String, content: String) {
        // 自定义处理逻辑
        validateCode(content)
        runTests(filePath)
    }

    override fun onChangeRecorded(event: CodeChangeEvent) {
        // 记录分析指标
        logMetrics(event)
    }

    override fun onSyncError(message: String) {
        // 高级错误处理
        notifyUser(message)
        fallbackStrategy()
    }
}
```

## 测试指南

### 单元测试

```kotlin
// src/test/kotlin/com/foutlook/aicliintegration/service/CliConnectionServiceTest.kt
class CliConnectionServiceTest {
    @Test
    fun testConnect() {
        // Mock CliConfig
        // 验证 connect() 返回 true
    }

    @Test
    fun testSendCommand() {
        // Mock Process
        // 验证命令发送成功
    }
}
```

### 集成测试

1. 启动 IDE 测试环境
2. 加载插件
3. 测试 UI 交互
4. 验证代码同步

### 手动测试检查清单

- [ ] 连接到 CLI
- [ ] 发送文件到 CLI
- [ ] 接收并应用代码修改
- [ ] 验证代码高亮和差异显示
- [ ] 测试设置保存/加载
- [ ] 测试错误恢复

## 构建和发布

### 本地开发构建

```bash
cd ai-code-cli-plugin
./gradlew runIde                    # 运行 IDE 进行调试
./gradlew build                     # 构建项目
./gradlew test                      # 运行单元测试
```

### 生成发布包

```bash
./gradlew buildPlugin               # 生成 .zip 插件包
```

输出位置：`build/distributions/ai-code-cli-plugin-1.0.0.zip`

### 发布到 JetBrains Marketplace

1. 登录 [JetBrains Marketplace](https://plugins.jetbrains.com)
2. 上传 `ai-code-cli-plugin-1.0.0.zip`
3. 填写插件信息和版本说明
4. 提交审核

## 常见问题解决

### Q1: 如何调试插件？

```bash
./gradlew runIde --debug
# 或在 IDE 中使用 Debug 模式
```

### Q2: 连接到 CLI 失败？

检查以下项：
- CLI 路径是否正确
- CLI 是否已安装且有执行权限
- 工作目录是否存在
- 查看插件日志

### Q3: 代码变更未同步？

- 确保启用了实时同步选项
- 检查 CLI 响应格式
- 验证 `CliProtocolParser` 正确解析了响应
- 查看日志中的错误信息

### Q4: 如何自定义 JSON 协议？

修改 `CliProtocolParser.kt` 中的解析逻辑:

```kotlin
fun parseJsonResponse(json: String): CliResponse {
    // 根据实际的 CLI 响应格式自定义解析
}
```

## 依赖管理

### 主要依赖

```gradle
- intellij-gradle-plugin: IDE 开发框架
- kotlin-stdlib: Kotlin 标准库
- gson: JSON 解析
- commons-io: 文件 I/O 操作
```

### 更新依赖

```bash
./gradlew dependencies              # 查看依赖树
./gradlew dependencyUpdates         # 检查更新
```

## 性能优化

### 1. 防抖机制

```kotlin
// DocumentChangeListener 中使用防抖
private var syncTimer: Timer? = null

private fun scheduleSync(codeSyncService: CodeSyncService, filePath: String) {
    syncTimer?.cancel()
    syncTimer = timer(initialDelay = 500) {
        // 触发同步
    }
}
```

### 2. 异步处理

```kotlin
// 在 CodeSyncService 中使用异步更新
ApplicationManager.getApplication().invokeLater {
    // UI 相关操作
}
```

### 3. 内存管理

```kotlin
// 定期清理待处理变更
pendingChanges.clear()
```

## 贡献工作流

1. Fork 项目
2. 创建特性分支：`git checkout -b feature/xyz`
3. 提交更改：`git commit -am 'Add xyz'`
4. 推送分支：`git push origin feature/xyz`
5. 创建 Pull Request

## 许可证

MIT License - 详见 LICENSE 文件

## 联系方式

- 项目主页：https://github.com/Foutlook/ai-open-chat
- 问题报告：https://github.com/Foutlook/ai-open-chat/issues
- 邮箱：support@example.com
