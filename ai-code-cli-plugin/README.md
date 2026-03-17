# AI Code CLI Integration Plugin for IntelliJ IDEA

一个强大的 IntelliJ IDEA 插件，集成本地 AI 代码生成工具（OpenCode、Codex、Claude Code CLI），实现代码的实时同步和交互式修改。

## 功能特性

### 核心功能
1. **多 CLI 支持** - 支持三种主流 AI 代码 CLI 工具：
   - OpenCode CLI
   - Codex CLI
   - Claude Code CLI

2. **实时代码同步**
   - CLI 生成的代码变更实时显示在 IDE 中
   - IDE 代码修改可以实时发送到 CLI 进行分析
   - 双向同步机制，实现无缝协作

3. **代码变更管理**
   - Diff 预览，清晰展示代码变更
   - 一键应用 CLI 建议的代码修改
   - 变更历史记录

4. **灵活的配置**
   - 自定义 CLI 可执行文件路径
   - 同步延迟调整
   - 实时同步开启/关闭
   - 自动应用变更选项

## 项目结构

```
ai-code-cli-plugin/
├── build.gradle.kts                    # Gradle 构建配置
├── settings.gradle.kts                 # Gradle 设置
├── src/
│   ├── main/
│   │   ├── kotlin/                     # Kotlin 源代码
│   │   │   └── com/foutlook/aicliintegration/
│   │   │       ├── action/             # 插件操作类
│   │   │       │   └── CliActions.kt
│   │   │       ├── listener/           # 事件监听器
│   │   │       │   └── DocumentChangeListener.kt
│   │   │       ├── model/              # 数据模型
│   │   │       │   └── Models.kt
│   │   │       ├── service/            # 核心服务
│   │   │       │   ├── CliConnectionService.kt
│   │   │       │   └── CodeSyncService.kt
│   │   │       ├── settings/           # 设置管理
│   │   │       │   ├── CliConfigurable.kt
│   │   │       │   └── CliSettings.kt
│   │   │       └── ui/                 # UI 组件
│   │   │           └── CliToolWindow.kt
│   │   └── resources/
│   │       └── META-INF/
│   │           └── plugin.xml          # 插件配置文件
│   └── test/                           # 测试源代码
└── README.md                           # 本文件
```

## 快速开始

### 前置条件
- Java 17+
- IntelliJ IDEA 2023.2 或更高版本
- 至少一个 AI 代码 CLI 工具已安装（OpenCode、Codex 或 Claude Code）

### 构建插件

```bash
cd ai-code-cli-plugin
./gradlew build
```

### 运行测试
```bash
./gradlew test
```

### 生成可发布的插件
```bash
./gradlew buildPlugin
```

生成的插件文件位于 `build/distributions/` 目录。

## 使用指南

### 1. 配置 CLI 路径
1. 打开 IDEA：**File → Settings → Tools → AI CLI Integration**
2. 输入各 CLI 工具的可执行文件路径
3. 配置同步选项（实时同步、差异预览等）
4. 点击 **Apply** 保存设置

### 2. 连接到 CLI
1. 菜单栏选择 **AI CLI Integration → Connect to CLI**
2. 选择 CLI 可执行文件
3. 等待连接建立

### 3. 发送文件到 CLI
1. 打开要分析的代码文件
2. **AI CLI Integration → Send Current File to CLI**
3. 等待 CLI 返回分析结果

### 4. 应用 CLI 建议的修改
1. 在工具窗口中查看 Diff 预览
2. 单击对应代码块的 **Apply** 按钮应用修改
3. 或启用 **Auto Apply Changes** 自动应用所有建议

## 架构设计

### 核心模块

#### 1. CliConnectionService
负责与本地 CLI 工具的通信：
- 进程生命周期管理
- 命令发送和响应接收
- 连接状态管理
- 事件通知机制

```kotlin
// 使用示例
val service = project.getService(CliConnectionService::class.java)
val config = CliConfig(CliType.OPENCODE, "/path/to/cli", "/project/path")
service.connect(config)
service.sendFileContent(filePath, content)
```

#### 2. CodeSyncService
管理 IDE 和 CLI 之间的代码同步：
- 记录待处理的代码变更
- 应用 CLI 返回的修改
- 变更事件通知

```kotlin
// 使用示例
val syncService = project.getService(CodeSyncService::class.java)
val event = CodeChangeEvent(filePath, oldContent, newContent, 10, 20)
syncService.recordChange(event)
syncService.applyCodeChanges(filePath, newContent)
```

#### 3. DocumentChangeListener
监听 IDE 中的代码编辑事件：
- 捕获文档变更事件
- 创建代码变更记录
- 触发代码同步流程

### 数据流

```
┌─────────────────────────────────────────────────────┐
│         IntelliJ IDEA IDE                           │
│  ┌──────────────────────────────────────────────┐   │
│  │ DocumentChangeListener (监听代码变更)         │   │
│  └──────────────────────────────────────────────┘   │
│                      │                              │
│                      ▼                              │
│  ┌──────────────────────────────────────────────┐   │
│  │ CodeSyncService (同步管理)                   │   │
│  └──────────────────────────────────────────────┘   │
│                      │                              │
│                      ▼                              │
│  ┌──────────────────────────────────────────────┐   │
│  │ CliConnectionService (CLI 通信)              │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
                       ◄─────────────►
                  Process 通信管道
                       ◄─────────────►
┌─────────────────────────────────────────────────────┐
│      本地 AI Code CLI                               │
│  (OpenCode/Codex/Claude Code)                       │
└─────────────────────────────────────────────────────┘
```

## API 文档

### CliConnectionService

```kotlin
// 连接到 CLI
fun connect(config: CliConfig): Boolean

// 断开连接
fun disconnect()

// 发送命令
fun sendCommand(command: String): CliResponse

// 发送文件内容
fun sendFileContent(filePath: String, content: String): CliResponse

// 获取连接状态
fun getStatus(): ConnectionStatus

// 添加连接监听器
fun addConnectionListener(listener: ConnectionListener)

// 添加响应监听器
fun addResponseListener(listener: ResponseListener)
```

### CodeSyncService

```kotlin
// 应用代码变更
fun applyCodeChanges(filePath: String, newContent: String)

// 记录代码变更
fun recordChange(event: CodeChangeEvent)

// 获取待处理的变更
fun getPendingChanges(): Map<String, CodeChangeEvent>

// 清除待处理的变更
fun clearPendingChanges()

// 添加同步监听器
fun addSyncListener(listener: SyncListener)
```

## 扩展开发

### 添加新的 CLI 支持

1. 在 `CliType` 中添加新的 CLI 类型：
```kotlin
enum class CliType {
    OPENCODE, CODEX, CLAUDE_CODE, YOUR_CLI
}
```

2. 创建新的 CLI 适配器（可选）：
```kotlin
class YourCliAdapter(config: CliConfig) {
    fun parseResponse(output: String): CliResponse {
        // 实现特定的响应解析逻辑
    }
}
```

3. 在 `CliConnectionService` 中集成适配器

### 自定义同步策略

继承 `SyncListener` 接口实现自定义的同步逻辑：

```kotlin
class CustomSyncListener : SyncListener {
    override fun onChangesApplied(filePath: String, content: String) {
        // 自定义处理
    }

    override fun onChangeRecorded(event: CodeChangeEvent) {
        // 自定义处理
    }

    override fun onSyncError(message: String) {
        // 自定义错误处理
    }
}
```

## 调试

### 启用调试日志

在 IDE 中启用详细日志输出：
1. **Help → Diagnostic Tools → Debug Log Settings**
2. 添加调试配置：
   ```
   com.foutlook.aicliintegration
   ```

### 常见问题

**Q: 连接失败**
- 检查 CLI 可执行文件路径是否正确
- 确保 CLI 工具已安装
- 检查工作目录权限

**Q: 代码未同步**
- 确保启用了实时同步选项
- 检查 CLI 响应格式是否正确
- 查看日志输出

## 贡献指南

欢迎贡献！请按以下流程：

1. Fork 本仓库
2. 创建新分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -am 'Add new feature'`
4. 推送到分支：`git push origin feature/your-feature`
5. 提交 Pull Request

## 许可证

MIT License

## 联系方式

- GitHub Issues: [ai-open-chat/issues](https://github.com/Foutlook/ai-open-chat/issues)
- Email: support@example.com
