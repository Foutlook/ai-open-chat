# AI Open Chat - 开源 AI 代码生成工具集成套件

一个完整的开源项目套件，用于集成和优化本地 AI 代码生成工具的使用。

## 📁 项目组成

### 1. [AI Code CLI Integration Plugin](./ai-code-cli-plugin) - IDEA 插件

一个强大的 IntelliJ IDEA 插件，集成本地 AI 代码生成 CLI 工具（OpenCode、Codex、Claude Code）。

**主要特性**：
- ✅ 多种 CLI 工具支持
- ✅ 代码实时同步与显示
- ✅ 双向交互式修改指导
- ✅ Diff 预览和一键应用
- ✅ 灵活的配置系统

**快速开始**：
```bash
cd ai-code-cli-plugin
./gradlew runIde              # 运行 IDE 测试
./gradlew build               # 构建项目
./gradlew buildPlugin         # 生成可发布的插件
```

[查看完整 README](./ai-code-cli-plugin/README.md)

## 📚 文档指南

- **[QUICKSTART.md](./QUICKSTART.md)** - 5 分钟快速开始
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - 完整架构设计
- **[ai-code-cli-plugin/DEVELOPMENT.md](./ai-code-cli-plugin/DEVELOPMENT.md)** - 开发指南
- **[ai-code-cli-plugin/README.md](./ai-code-cli-plugin/README.md)** - 插件使用说明

## 🎯 核心功能

### 1. IDE 插件集成

通过 IDEA 插件，用户可以：
- 直接在编辑器中连接 AI CLI 工具
- 实时查看 AI 生成的代码改进建议
- 一键应用或拒绝改进建议
- 查看详细的代码 Diff
- 自动格式化和优化代码

### 2. 多 CLI 支持

支持的 AI 代码生成工具：
- **OpenCode** - 开源代码生成工具
- **Codex** - GitHub 的 AI 编程助手
- **Claude Code** - Anthropic 的代码生成工具

### 3. 实时代码同步

```
IDE 编辑 → 实时监听 → 发送到 CLI → CLI 分析 → 返回建议 → IDE 展示
       ↑________________________________________________↓
```

### 4. 灵活配置

用户可以配置：
- CLI 工具的可执行文件路径
- 实时同步功能的启/关
- 同步延迟时间
- 自动应用变更
- Diff 预览选项

## 🏗️ 架构概览

```
┌─────────────────────────────────────────┐
│        IntelliJ IDEA 用户界面           │
├─────────────────────────────────────────┤
│  AI Code CLI Integration 插件           │
│  ├─ Tool Window (工具窗口)              │
│  ├─ Settings (设置页面)                 │
│  └─ Actions (菜单操作)                  │
├─────────────────────────────────────────┤
│  核心服务                               │
│  ├─ CliConnectionService (连接管理)     │
│  ├─ CodeSyncService (代码同步)          │
│  └─ CliSettings (设置存储)              │
├─────────────────────────────────────────┤
│  事件监听与处理                         │
│  ├─ DocumentChangeListener              │
│  ├─ ConnectionListener                  │
│  └─ ResponseListener                    │
├─────────────────────────────────────────┤
│  工具类与协议                           │
│  ├─ CliProtocolParser (JSON 解析)       │
│  └─ DiffViewerUtil (差异显示)           │
└─────────────────────────────────────────┘
          ⬇️ 进程通信 ⬇️
┌─────────────────────────────────────────┐
│  本地 AI Code CLI                       │
│  (OpenCode/Codex/Claude Code)           │
└─────────────────────────────────────────┘
```

## 🚀 快速开始

### 前置需求

- Java 17+
- IntelliJ IDEA 2023.2+
- 已安装的 AI Code CLI 工具（至少一个）

### 安装和配置

1. **获取项目**
   ```bash
   git clone https://github.com/Foutlook/ai-open-chat.git
   cd ai-open-chat
   ```

2. **构建插件**
   ```bash
   cd ai-code-cli-plugin
   ./gradlew build
   ```

3. **运行测试**
   ```bash
   ./gradlew runIde
   ```

4. **配置 CLI 路径**
   - 打开 Settings → Tools → AI CLI Integration
   - 输入 CLI 工具的路径
   - 配置同步选项
   - 应用并保存

5. **开始使用**
   - 连接到 CLI
   - 编辑代码
   - 查看实时建议
   - 应用改进

## 📖 使用示例

### 连接到 CLI

```kotlin
val config = CliConfig(
    type = CliType.OPENCODE,
    executable = "/usr/local/bin/opencode",
    workingDirectory = "/path/to/project"
)
cliService.connect(config)
```

### 发送代码分析请求

```kotlin
val response = cliService.sendFileContent(
    "/path/to/file.java",
    fileContent
)

response.changedFiles.forEach { change ->
    println("修改: ${change.path}, 操作: ${change.operation}")
}
```

### 监听代码变更

```kotlin
syncService.addSyncListener(object : SyncListener {
    override fun onChangesApplied(filePath: String, content: String) {
        // 处理应用的变更
    }
    override fun onChangeRecorded(event: CodeChangeEvent) {
        // 处理记录的变更
    }
})
```

## 🔧 项目结构

```
ai-open-chat/
├── README.md                              # 本文件
├── QUICKSTART.md                          # 快速开始指南
├── ARCHITECTURE.md                        # 架构详解
├── ai-code-cli-plugin/                    # IDEA 插件项目
│   ├── README.md                          # 插件文档
│   ├── DEVELOPMENT.md                     # 开发指南
│   ├── build.gradle.kts                   # Gradle 构建
│   └── src/
│       ├── main/
│       │   ├── kotlin/com/foutlook/aicliintegration/
│       │   │   ├── action/                # UI 操作
│       │   │   ├── listener/              # 事件监听
│       │   │   ├── model/                 # 数据模型
│       │   │   ├── service/               # 核心服务
│       │   │   ├── settings/              # 配置管理
│       │   │   ├── ui/                    # UI 组件
│       │   │   └── util/                  # 工具类
│       │   └── resources/META-INF/
│       │       └── plugin.xml             # 插件配置
│       └── test/                          # 测试代码
└── .git/                                  # Git 仓库
```

## 📝 主要特性

- ✅ **多 CLI 支持** - OpenCode、Codex、Claude Code
- ✅ **实时同步** - 代码变更实时监听和显示
- ✅ **双向交互** - IDE 和 CLI 可互相指导
- ✅ **Diff 预览** - 清晰展示代码变更
- ✅ **一键应用** - 快速应用建议的改进
- ✅ **灵活配置** - 自由定制同步行为
- ✅ **防抖处理** - 避免频繁的 CLI 调用
- ✅ **线程安全** - 多线程环保

## 🛠️ 开发指南

### 添加新 CLI 支持

1. 在 `CliType` 中添加新类型
2. 创建相应的适配器
3. 在 `CliConnectionService` 中集成

### 自定义 UI

修改 `CliToolWindow.kt` 或 `CliConfigurable.kt`

### 实现自定义同步策略

实现 `SyncListener` 接口

详见 [DEVELOPMENT.md](./ai-code-cli-plugin/DEVELOPMENT.md)

## 🧪 测试

```bash
# 运行单元测试
./gradlew test

# 运行集成测试
./gradlew integrationTest

# 在 IDE 中调试
./gradlew runIde --debug
```

## 📦 构建和发布

### 本地构建

```bash
./gradlew build              # 构建项目
./gradlew buildPlugin        # 生成插件包
```

### 发布到 Marketplace

```bash
# 生成签名的插件
./gradlew signPlugin

# 上传到 JetBrains Marketplace
# (需要 Marketplace 账户)
```

## 🎓 学习资源

- [IntelliJ IDEA Plugin Development](https://plugins.jetbrains.com/docs/intellij/)
- [Kotlin 文档](https://kotlinlang.org/docs/)
- [Gradle 用户指南](https://docs.gradle.org/)

## 🤝 贡献

欢迎贡献！请按以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📋 开发路线规划

- [ ] 完善单元和集成测试
- [ ] 增强 Diff 展示功能
- [ ] 添加代码建议预览面板
- [ ] 支持更多 CLI 工具
- [ ] 性能优化和缓存机制
- [ ] 国际化支持
- [ ] 插件市场发布

## ⚖️ 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 📞 联系方式

- **GitHub**: [Foutlook/ai-open-chat](https://github.com/Foutlook/ai-open-chat)
- **Issues**: [报告问题](https://github.com/Foutlook/ai-open-chat/issues)
- **邮件**: support@example.com

## 🌟 致谢

感谢所有贡献者和用户的支持！

---

**现在就开始使用 AI Code CLI Integration 插件吧！** 🚀