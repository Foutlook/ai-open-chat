# 项目架构总结

## 工作区结构

```
/workspaces/ai-open-chat/
├── ai-code-cli-plugin/                    # IDEA 插件主项目
│   ├── build.gradle.kts                  # Gradle 构建脚本
│   ├── settings.gradle.kts               # Gradle 项目设置
│   ├── .gitignore                        # Git 忽略文件配置
│   ├── README.md                         # 项目使用文档
│   ├── DEVELOPMENT.md                    # 开发指南
│   └── src/
│       ├── main/
│       │   ├── kotlin/                   # Kotlin 源代码
│       │   │   └── com/foutlook/aicliintegration/
│       │   │       ├── action/           # UI 操作类
│       │   │       │   ├── ConfigureCliAction.kt
│       │   │       │   ├── ConnectCliAction.kt
│       │   │       │   ├── OpenCliPanelAction.kt
│       │   │       │   └── SendCurrentFileAction.kt
│       │   │       ├── listener/         # 事件监听器
│       │   │       │   └── DocumentChangeListener.kt
│       │   │       ├── model/            # 数据模型
│       │   │       │   └── Models.kt
│       │   │       │       ├── CliType
│       │   │       │       ├── CliConfig
│       │   │       │       ├── ConnectionStatus
│       │   │       │       ├── CodeChangeEvent
│       │   │       │       ├── CliResponse
│       │   │       │       ├── FileChange
│       │   │       │       └── FileOperation
│       │   │       ├── service/          # 核心服务
│       │   │       │   ├── CliConnectionService.kt
│       │   │       │   │   ├── connect()
│       │   │       │   │   ├── disconnect()
│       │   │       │   │   ├── sendCommand()
│       │   │       │   │   └── sendFileContent()
│       │   │       │   ├── CodeSyncService.kt
│       │   │       │   │   ├── recordChange()
│       │   │       │   │   ├── applyCodeChanges()
│       │   │       │   │   └── getPendingChanges()
│       │   │       │   └── CliConnectionService.kt (扩展接口)
│       │   │       │       ├── ConnectionListener
│       │   │       │       └── ResponseListener
│       │   │       ├── settings/         # 设置管理
│       │   │       │   ├── CliSettings.kt
│       │   │       │   │   ├── openCodeCliPath
│       │   │       │   │   ├── codexCliPath
│       │   │       │   │   ├── claudeCodeCliPath
│       │   │       │   │   └── enableRealTimeSync
│       │   │       │   └── CliConfigurable.kt (IDE 设置页面)
│       │   │       ├── ui/               # UI 组件
│       │   │       │   ├── CliToolWindow.kt
│       │   │       │   │   ├── CliToolWindowFactory
│       │   │       │   │   └── CliToolWindowPanel
│       │   │       │   └── CliConfigurable.kt
│       │   │       └── util/             # 工具类
│       │   │           ├── CliProtocolParser.kt
│       │   │           │   ├── parseJsonResponse()
│       │   │           │   ├── formatJsonRequest()
│       │   │           │   └── parseTextResponse()
│       │   │           └── DiffViewerUtil.kt
│       │   │               └── showDiff()
│       │   └── resources/
│       │       └── META-INF/
│       │           └── plugin.xml        # 插件配置
│       │               ├── <actions>     # 菜单项定义
│       │               ├── <extensions>  # 扩展点
│       │               └── <applicationListeners>
│       └── test/                         # 测试代码（待实现）
│
└── README.md                             # 根项目 README
```

## 关键技术栈

- **开发语言**: Kotlin, Java
- **构建工具**: Gradle
- **IDE 平台**: IntelliJ IDEA 2023.2+
- **序列化**: Gson (JSON 处理)
- **Web 服务**: 本地 CLI 进程通信

## 数据流向

### 场景 1：IDE 编辑 → CLI 分析

```
1. 用户在 IDE 中编辑代码
       ↓
2. DocumentChangeListener 捕获 DocumentEvent
       ↓
3. 创建 CodeChangeEvent
       ↓
4. CodeSyncService.recordChange() 记录变更
       ↓
5. 防抖后自动或手动发送到 CLI
       ↓
6. CliConnectionService.sendFileContent()
       ↓
7. 写入 CLI 进程的 stdin
       ↓
8. CLI 处理并返回响应
```

### 场景 2：CLI 分析结果 → IDE 应用

```
1. CLI 生成代码修改
       ↓
2. 通过 stdout 返回 JSON 响应
       ↓
3. CliConnectionService 读取 stdout
       ↓
4. CliProtocolParser 解析 JSON
       ↓
5. 触发 ResponseListener 事件
       ↓
6. CodeSyncService.applyCodeChanges()
       ↓
7. 在 ApplicationManager 线程中更新文档
       ↓
8. 触发 SyncListener.onChangesApplied()
       ↓
9. UI 显示代码变更和 Diff
```

## 插件生命周期

```
┌─────────────────────────────────────────┐
│ IDE 启动                                │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ 加载 plugin.xml                         │
│ ├─ 注册 Actions                         │
│ ├─ 注册 Extensions                      │
│ └─ 注册 Listeners                       │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ 初始化 Project Services                 │
│ ├─ CliConnectionService.getInstance()   │
│ ├─ CodeSyncService.getInstance()        │
│ └─ CliSettings.getInstance()            │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ 启用 DocumentListener                   │
│ 创建 Tool Window                        │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ 插件运行中                              │
│ 监听用户交互和代码变更                  │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ IDE 关闭                                │
│ 卸载插件                                │
│ 释放资源                                │
└─────────────────────────────────────────┘
```

## 关键实现细节

### 1. 进程通信

```kotlin
// ProcessBuilder 创建子进程
val processBuilder = ProcessBuilder(config.executable)
processBuilder.directory(java.io.File(config.workingDirectory))
val process = processBuilder.start()

// 写入命令
val writer = process.outputStream.bufferedWriter()
writer.write(command)
writer.flush()

// 读取响应
val reader = process.inputStream.bufferedReader()
val response = reader.readLine()
```

### 2. 线程安全

```kotlin
// 使用 CopyOnWriteArrayList 线程安全集合
private val listeners = CopyOnWriteArrayList<ConnectionListener>()

// 使用 ApplicationManager 在 EDT 线程中更新 UI
ApplicationManager.getApplication().invokeLater {
    file.getDocument()?.setText(newContent)
}
```

### 3. 防抖处理

```kotlin
// 延迟同步，避免频繁操作
private var syncTimer: Timer? = null

private fun scheduleSync() {
    syncTimer?.cancel()
    syncTimer = timer(initialDelay = 500) {
        performSync()
        cancel()
    }
}
```

## 扩展点（Extension Points）

### 1. 添加新的 CLI 类型

修改 `Models.kt`:
```kotlin
enum class CliType {
    OPENCODE, CODEX, CLAUDE_CODE, YOUR_CLI
}
```

### 2. 添加新的事件监听

创建实现 `ConnectionListener` 或 `ResponseListener` 接口的类

### 3. 自定义 UI

修改 `CliToolWindow.kt` 或创建新的 UI 组件

### 4. 自定义同步策略

实现 `SyncListener` 接口

## 下一步开发方向

1. **增强协议支持**
   - 支持更多 CLI 工具
   - 实现异步双向通信

2. **UI 增强**
   - 实时日志查看
   - 更好的 Diff 展示
   - 代码建议预览

3. **性能优化**
   - 增量同步
   - 缓存机制
   - 线程池管理

4. **测试完善**
   - 单元测试
   - 集成测试
   - UI 自动化测试

5. **文档完善**
   - API 文档
   - 测试用例文档
   - 故障排除指南
