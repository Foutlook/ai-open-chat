# 多 CLI 实例管理架构 - 升级文档

## 🔄 架构变更概述

### 旧架构 vs 新架构

#### 旧架构（已弃用）
```
单一连接 → 预配置 CLI 路径 → 一次性连接 → 固定进程
```

#### 新架构（推荐）
```
动态启动 → 选择 CLI 可执行文件 → 多个实例并发运行 → 独立进程管理
```

## 核心组件

### 1. CliInstanceManagerService

**职责**：管理所有活跃的 CLI 实例

```kotlin
// 启动新实例
fun startCliInstance(instance: CliInstance): Boolean

// 停止实例
fun stopCliInstance(instanceId: String)

// 向实例发送命令
fun sendCommandToInstance(instanceId: String, command: String): Boolean

// 获取所有实例
fun getAllInstances(): List<CliInstance>
```

**特点**：
- ConcurrentHashMap 管理多个实例
- 线程池处理输出读取
- 事件驱动通知
- UUID 唯一标识每个实例

### 2. CliInstance 数据模型

```kotlin
data class CliInstance(
    val id: String,                    // 唯一标识
    val name: String,                  // 显示名称
    val cliType: CliType,              // CLI 类型
    val executablePath: String,        // 可执行文件路径
    val workingDirectory: String,      // 工作目录
    val status: ConnectionStatus,      // 连接状态
    val createdAt: Long,               // 创建时间
    var lastActivity: Long             // 最后活动时间
)
```

### 3. CliInstanceEvent 事件系统

```kotlin
sealed class CliInstanceEvent {
    data class Created(val instance: CliInstance)      // 创建
    data class Connected(val instanceId: String)       // 已连接
    data class Disconnected(val instanceId: String)    // 已断开
    data class Output(val output: CliInstanceOutput)   // 输出
    data class Error(val error: String)                // 错误
    data class Removed(val instanceId: String)         // 已移除
}
```

### 4. MultiCliInstancePanel UI 面板

显示客户端特性：
- 标签页切换显示不同实例的输出
- 实例列表展示所有活跃连接
- 快速控制面板（启动、停止、发送）
- 实时日志显示

## 工作流程

### 启动单个 CLI 实例

```
用户点击 "Start CLI Instance"
    ↓
选择 CLI 类型（OpenCode/Codex/Claude Code）
    ↓
选择可执行文件
    ↓
输入工作目录
    ↓
创建 CliInstance 对象
    ↓
CliInstanceManagerService.startCliInstance()
    ↓
创建 ProcessBuilder
    ↓
启动进程，创建输入/输出流
    ↓
启动两个读取线程（stdout 和 stderr）
    ↓
触发 CliInstanceEvent.Connected 事件
    ↓
UI 刷新，显示新实例
```

### 向 CLI 发送代码

```
用户编辑代码
    ↓
选择 "Send File to CLI"
    ↓
选择目标 CLI 实例
    ↓
获取文件路径和内容
    ↓
格式化为 JSON 命令
    ↓
CliInstanceManagerService.sendFileToInstance()
    ↓
写入实例的 process.outputStream
    ↓
CLI 处理并返回响应
    ↓
输出读取线程捕获响应
    ↓
触发 CliInstanceEvent.Output 事件
    ↓
UI 在日志面板显示输出
```

## 使用流程

### 场景 1：启动多个 CLI

1. **启动第一个 CLI**
   - 菜单：AI CLI Integration → Start CLI Instance
   - 选择：OpenCode
   - 路径：/usr/local/bin/opencode
   - 状态：Connected ✓

2. **启动第二个 CLI**
   - 菜单：AI CLI Integration → Start CLI Instance
   - 选择：Claude Code
   - 路径：/usr/local/bin/claude-code
   - 状态：Connected ✓

3. **启动第三个 CLI**
   - 菜单：AI CLI Integration → Start CLI Instance
   - 选择：Codex
   - 路径：/usr/local/bin/codex
   - 状态：Connected ✓

现在有 3 个 CLI 实例同时运行！

### 场景 2：向不同 CLI 发送代码

1. 打开 Java 文件编辑
2. 菜单：AI CLI Integration → Send File to CLI
3. 选择 CLI 实例（下拉菜单显示：OpenCode-1234、Claude Code-5678、Codex-9012）
4. 点击确认
5. 文件发送到选定的 CLI
6. 结果显示在 CLI Instances 工具窗口的日志区域

### 场景 3：管理多个实例

工具窗口显示：
```
┌─────────────────────────────────┐
│ AI CLI Instances                │
├─────────────────────────────────┤
│ Active Instances: [OpenCode ▼]  │
│ [Start] [Stop] [Refresh]        │
├─────────────────────────────────┤
│ ┌─ Console ─ Instances ─┐       │
│ │                       │       │
│ │ [OpenCode-1234]       │       │
│ │ [Claude Code-5678]    │       │
│ │ [Codex-9012]          │       │
│ │                       │       │
│ │ Console Output...     │       │
│ │ $ process file.java   │       │
│ │ > Analysis result...  │       │
│ └─────────────────────┘        │
├─────────────────────────────────┤
│ [Send File] [Clear Output]      │
└─────────────────────────────────┘
```

## 新增操作 (Actions)

| 操作 | 快捷键 | 功能 |
|-----|--------|------|
| Start CLI Instance | Ctrl+Shift+Alt+S | 启动新 CLI 实例 |
| Stop CLI Instance | - | 停止活跃实例 |
| Open CLI Instances | Ctrl+Shift+Alt+O | 打开实例面板 |
| Send File to CLI | Ctrl+Shift+Alt+F | 发送文件到 CLI |

## 与旧架构的兼容性

### 保留的功能
- ✅ CliConnectionService（仍可使用）
- ✅ CodeSyncService（仍可使用）
- ✅ CliSettings（仍可使用）
- ✅ 设置页面（仍可使用）

### 新增功能
- ✅ CliInstanceManagerService（新服务）
- ✅ MultiCliInstancePanel（新 UI）
- ✅ 新操作类（4 个新 Actions）

### 迁移建议
**逐步迁移**：
1. 当前保留旧架构，不强制删除
2. 新项目使用新架构
3. 老项目可继续使用旧架构
4. 未来版本中弃用旧架构

## 代码示例

### 启动 CLI 实例

```kotlin
val managerService = project.getService(CliInstanceManagerService::class.java)

val instance = CliInstance(
    name = "OpenCode-1",
    cliType = CliType.OPENCODE,
    executablePath = "/usr/local/bin/opencode",
    workingDirectory = "/path/to/project"
)

if (managerService.startCliInstance(instance)) {
    println("Instance started: ${instance.name}")
} else {
    println("Failed to start instance")
}
```

### 监听事件

```kotlin
managerService.addListener(object : CliInstanceListener {
    override fun onInstanceEvent(event: CliInstanceEvent) {
        when (event) {
            is CliInstanceEvent.Created -> {
                println("Created: ${event.instance.name}")
            }
            is CliInstanceEvent.Output -> {
                println("[${event.output.type}] ${event.output.content}")
            }
            is CliInstanceEvent.Error -> {
                println("Error: ${event.error}")
            }
            else -> {}
        }
    }
})
```

### 发送命令

```kotlin
val instances = managerService.getAllInstances()
if (instances.isNotEmpty()) {
    val instanceId = instances[0].id
    managerService.sendFileToInstance(
        instanceId,
        "/path/to/file.java",
        "public class Main { }"
    )
}
```

## 架构优势

1. **并发支持** - 多个 CLI 实例同时运行
2. **动态启动** - 不需要预先配置
3. **灵活管理** - 可随时启动或停止实例
4. **事件驱动** - 清晰的事件流
5. **线程安全** - ConcurrentHashMap 和线程池
6. **易于扩展** - 支持新的 CLI 类型无需改动核心逻辑
7. **更好的 UX** - 直观的多实例管理界面

## 性能考虑

### 资源占用

| 资源 | 用量 | 说明 |
|-----|------|------|
| 内存/实例 | ~50-100MB | 取决于 CLI 工具 |
| 线程/实例 | 2 个 | 一个读 stdout，一个读 stderr |
| 最大实例数 | 建议 ≤ 10 | 防止资源耗尽 |

### 优化建议

1. **输出缓冲** - 考虑限制日志大小
2. **实例清理** - 定期清理已停止的实例
3. **线程池** - 考虑使用固定大小线程池
4. **内存监控** - 定期检查内存使用情况

## 总结

这个新架构提供了一个**企业级的多 CLI 管理系统**，支持：
- 🚀 动态启动多个 CLI 实例
- 🔄 并发运行和管理
- 📊 实时监控和日志
- 🎯 灵活的文件发送
- 🔌 事件驱动设计
- 📈 易于扩展和维护

**现在你的 IDEA 插件可以像专业的 IDE 一样管理多个 AI 工具！** 🎉
