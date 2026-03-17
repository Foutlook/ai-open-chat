# V1 vs V2 架构对比

## 🔀 完整的架构对比

### 版本 1.0 架构（已弃用但仍可用）

```
┌──────────────────────────────────────────────┐
│         IntelliJ IDEA                        │
│  ┌──────────────────────────────────────┐   │
│  │ Settings Page                        │   │
│  │ - Configure CLI Path (预先配置)       │   │
│  │ - Set Working Dir                    │   │
│  └──────────────────────────────────────┘   │
│                  │                          │
│                  ▼                          │
│  ┌──────────────────────────────────────┐   │
│  │ CliToolWindow                        │   │
│  │ ├─ Status Label                      │   │
│  │ ├─ CLI Type Selector                 │   │
│  │ ├─ Connect Button                    │   │
│  │ └─ Output Log Area                   │   │
│  └──────────────────────────────────────┘   │
│                  │                          │
└──────────────────┼──────────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
  ┌──────────────┐    ┌──────────────┐
  │CliConnection│    │ CodeSync     │
  │Service      │    │Service       │
  └──────────────┘    └──────────────┘
        │                     │
        │  Process            │
        ▼                     ▼
  ┌──────────────────────────────────┐
  │      单一 CLI Tool Process        │
  │  (只有一个 CLI 可以运行)          │
  └──────────────────────────────────┘
```

**特点**：
- ✅ 简单同步的设计
- ✅ 易于理解
- ❌ 只支持一个 CLI
- ❌ 需要预先配置
- ❌ 不够灵活

### 版本 2.0 架构（推荐）

```
┌──────────────────────────────────────────────────────────────┐
│              IntelliJ IDEA                                   │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ AI CLI Integration Menu                                │ │
│  │ ├─ Start CLI Instance        (Ctrl+Shift+Alt+S)       │ │
│  │ ├─ Stop CLI Instance                                  │ │
│  │ ├─ Open CLI Instances Panel  (Ctrl+Shift+Alt+O)       │ │
│  │ └─ Send File to CLI          (Ctrl+Shift+Alt+F)       │ │
│  └────────────────────────────────────────────────────────┘ │
│                           │                                  │
│  ┌────────────────────────▼────────────────────────────────┐ │
│  │     MultiCliInstancePanel (新工具窗口)                  │ │
│  │  ┌──────────────────────────────────────────────────┐  │ │
│  │  │ Active Instances: [OpenCode ▼] [Start] [Stop]    │  │ │
│  │  ├──────────────────────────────────────────────────┤  │ │
│  │  │ ┌─ Console ─────┬─ Instances ────────┐            │  │ │
│  │  │ │               │                     │            │  │ │
│  │  │ │ [OpenCode]    │ ☑ OpenCode-1234   │            │  │ │
│  │  │ │ [OK] File     │ ☑ Claude-5678      │            │  │ │
│  │  │ │ analyzed      │ ☑ Codex-9012       │            │  │ │
│  │  │ │               │                     │            │  │ │
│  │  │ │ [Claude Code] │ [Stop] [Info]       │            │  │ │
│  │  │ │ [OK] Import   │                     │            │  │ │
│  │  │ │ suggestions   │                     │            │  │ │
│  │  │ │               │                     │            │  │ │
│  │  │ │ [Codex]       │                     │            │  │ │
│  │  │ │ [OK] Code     │                     │            │  │ │
│  │  │ │ completion    │                     │            │  │ │
│  │  │ └───────────────┴─────────────────────┘            │  │ │
│  │  ├──────────────────────────────────────────────────┤  │ │
│  │  │ [Send File] [Clear] [Refresh]                     │  │ │
│  │  └──────────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────────┘ │
│                           │                                  │
└───────────────────────────┼──────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
  ┌───────────────────────────────────────────────────────┐
  │    CliInstanceManagerService                         │
  │    (多实例管理)                                       │
  │                                                      │
  │  ConcurrentHashMap<String, ActiveCliInstance>       │
  │  ├─ OpenCode-1234 → [Process, Writer, Reader]      │
  │  ├─ Claude-5678   → [Process, Writer, Reader]      │
  │  ├─ Codex-9012    → [Process, Writer, Reader]      │
  │  └─ ...可添加更多                                    │
  └───────────────────────────────────────────────────────┘
        │                   │                   │
        ▼                   ▼                   ▼
   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
   │ Process #1  │   │ Process #2  │   │ Process #3  │
   └─────────────┘   └─────────────┘   └─────────────┘
        │                   │                   │
        ▼                   ▼                   ▼
   ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
   │ OpenCode CLI    │ │ Claude Code CLI │ │ Codex CLI       │
   │ (独立运行)       │ │ (独立运行)       │ │ (独立运行)       │
   └─────────────────┘ └─────────────────┘ └─────────────────┘
```

**特点**：
- ✅ 完全现代化设计
- ✅ 支持无限 CLI 实例
- ✅ 动态启动（无需预配置）
- ✅ 完整的事件系统
- ✅ 更灵活和强大
- ✅ 向后兼容


## 📊 功能对比表

| 功能 | V1.0 | V2.0 | 说明 |
|-----|------|------|------|
| CLI 实例数 | 1 | ∞ | 支持无限个 |
| 配置方式 | 预先配置 | 动态启动 | V2 更灵活 |
| UI 工具窗口 | 1 个 | 1 个（新） | V2 更强大 |
| 标签页 | 无 | 2 个 | Console + Instances |
| 菜单项 | 5 个 | 4 个（新） | 简化操作 |
| 快捷键 | 1 个 | 3 个 | 提高效率 |
| 事件系统 | 简单 | 完整 | V2 更灵活 |
| 日志管理 | 单一 | 汇总 | V2 更清晰 |
| 并发管理 | 无 | 完整 | V2 线程安全 |
| 向后兼容 | - | ✅ | V1 代码仍可用 |

## 🔄 迁移路径

### 保留 V1 的场景
```
如果你的项目仍然只需要一个 CLI：
- 可以继续使用 V1 架构
- 设置页面仍然有效
- 旧的服务依然可用
```

### 使用 V2 的场景
```
如果你想尝试新功能：
- 菜单：AI CLI Integration → Start CLI Instance
- 快捷键：Ctrl+Shift+Alt+S
- 开始使用多 CLI 功能
```

### 完全迁移
```
如果要完全迁移代码：
1. 替换 CliConnectionService → CliInstanceManagerService
2. 更新事件监听器逻辑
3. 调整 UI 交互
4. 测试所有功能
```

## 📝 代码对比

### V1：启动 CLI

```kotlin
val config = CliConfig(
    type = CliType.OPENCODE,
    executable = "/path/to/cli",
    workingDirectory = "/project"
)
val service = project.getService(CliConnectionService::class.java)
service.connect(config)
```

### V2：启动 CLI

```kotlin
val instance = CliInstance(
    name = "OpenCode-1",
    cliType = CliType.OPENCODE,
    executablePath = "/path/to/cli",
    workingDirectory = "/project"
)
val managerService = project.getService(CliInstanceManagerService::class.java)
managerService.startCliInstance(instance)
```

或直接通过 UI：Ctrl+Shift+Alt+S

## 🎯 使用建议

### 何时使用 V1
- 简单项目，只需一个 CLI
- 对新功能没有需求
- 习惯旧的配置方式

### 何时使用 V2（推荐）
- 需要多个 CLI 工具对比
- 需要更灵活的启动方式
- 需要更强大的管理功能
- ⭐ **新项目都应该使用 V2**

## 🚀 升级决策树

```
是否需要多个 CLI？
    ├─ 是 → 使用 V2 ✓
    └─ 否 ┬─ 想要更好的 UI？
         ├─ 是 → 使用 V2 ✓
         └─ 否 ┬─ 是否是新项目？
              ├─ 是 → 使用 V2 ✓
              └─ 否 → V1 也可以, 但建议升级到 V2
```

## 📞 支持

- **V1 问题**：可以查看 DEVELOPMENT.md
- **V2 问题**：可以查看 MULTI_CLI_ARCHITECTURE.md
- **升级问题**：可以查看 UPGRADE_TO_MULTI_CLI.md

---

**结论：V2 是全面升级，强烈推荐使用！** 🎉
