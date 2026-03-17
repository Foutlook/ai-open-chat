# 快速开始指南

## 5 分钟快速开始

### 第 1 步：克隆或获取项目

```bash
cd /workspaces/ai-open-chat
cd ai-code-cli-plugin
```

### 第 2 步：构建项目

```bash
# 使用 Gradle wrapper（推荐）
./gradlew build

# 或者如果系统已安装 Gradle
gradle build
```

### 第 3 步：运行 IDE 进行测试

```bash
./gradlew runIde
```

这会启动一个配置了本插件的 IntelliJ IDEA 实例。

### 第 4 步：配置 CLI 工具

1. 在打开的 IDEA 中，进入 **File → Settings → Tools → AI CLI Integration**
2. 配置你已安装的 CLI 工具的路径：
   - **OpenCode CLI**: `/path/to/opencode`
   - **Codex CLI**: `/path/to/codex`
   - **Claude Code CLI**: `/path/to/claude-code`
3. 点击 **Apply** 保存配置

### 第 5 步：连接并使用

1. 打开或创建一个 Java/Kotlin 文件
2. 菜单栏选择 **AI CLI Integration → Connect to CLI**
3. 选择 CLI 可执行文件
4. 编辑代码，插件会实时监听并可以发送到 CLI
5. 在底部 **AI CLI Integration** 工具窗口查看结果

## 常用命令

### 开发构建

```bash
# 清理构建
./gradlew clean

# 构建项目
./gradlew build

# 测试
./gradlew test

# 生成可分发的插件包
./gradlew buildPlugin

# 在 IDE 中运行和调试
./gradlew runIde
```

### 查看项目信息

```bash
# 查看依赖
./gradlew dependencies

# 查看任务列表
./gradlew tasks

# 查看项目属性
./gradlew properties
```

## 项目结构速览

```
ai-code-cli-plugin/
├── src/main/kotlin/                           # Kotlin 源代码
│   └── com/foutlook/aicliintegration/         # 包名
│       ├── action/                            # 菜单操作 ✅
│       ├── listener/                          # 事件监听 ✅
│       ├── model/                             # 数据模型 ✅
│       ├── service/                           # 核心服务 ✅
│       ├── settings/                          # 配置管理 ✅
│       ├── ui/                                # UI 组件 ✅
│       └── util/                              # 工具类 ✅
├── src/main/resources/META-INF/
│   └── plugin.xml                             # 插件配置文件 ✅
├── build.gradle.kts                           # Gradle 配置 ✅
└── settings.gradle.kts                        # 项目设置 ✅
```

## 功能验证清单

### ✅ 已完成

- [x] 项目结构搭建
- [x] Gradle 构建系统配置
- [x] 核心服务实现
  - [x] CliConnectionService（CLI 连接）
  - [x] CodeSyncService（代码同步）
  - [x] CliSettings（设置管理）
- [x] UI 组件
  - [x] Tool Window（工具窗口）
  - [x] 设置页面（Settings）
  - [x] 菜单项（Actions）
- [x] 事件系统
  - [x] DocumentChangeListener（文档监听）
  - [x] ConnectionListener（连接监听）
  - [x] ResponseListener（响应监听）
- [x] 工具类
  - [x] CliProtocolParser（协议解析）
  - [x] DiffViewerUtil（差异展示）
- [x] 文档
  - [x] README（使用文档）
  - [x] DEVELOPMENT.md（开发指南）
  - [x] ARCHITECTURE.md（架构总结）

### 📋 待实现（后续版本）

- [ ] 单元测试
- [ ] 集成测试
- [ ] 更复杂的 Diff 展示
- [ ] 代码建议预览面板
- [ ] 编排和流程自动化
- [ ] 支持更多 CLI 工具
- [ ] 性能优化
- [ ] 国际化支持

## 测试 CLI 集成

### 创建测试 CLI 脚本

创建一个简单的测试 CLI 脚本来验证集成是否工作：

**test-cli.sh**:
```bash
#!/bin/bash
# 简单的测试 CLI - 读取输入并返回 JSON 响应

while IFS= read -r line; do
    echo "{\"success\": true, \"message\": \"Processed: $line\"}"
done
```

### 使用测试 CLI

1. 保存脚本为 `/tmp/test-cli.sh`
2. 添加执行权限：`chmod +x /tmp/test-cli.sh`
3. 在插件配置中，设置 OpenCode CLI 路径为 `/tmp/test-cli.sh`
4. 连接并测试

## 常见问题

### Q: 如何在 IDE 中调试插件？

A: 使用以下命令：
```bash
./gradlew runIde --debug
```

然后在 IDE 中附加调试器。

### Q: 如何查看插件日志？

A: 
1. Help → Show Log in Explorer
2. 查看 idea.log 文件
3. 或在 IDE 控制台查看输出

### Q: 如何生成可安装的插件？

A: 
```bash
./gradlew buildPlugin
```

输出文件位于 `build/distributions/` 目录，可以在 IDEA 中安装它。

### Q: 如何添加新的 CLI 支持？

A: 
1. 在 `Models.kt` 中的 `CliType` 枚举添加新类型
2. 创建相应的适配器类
3. 在 `CliConnectionService` 中集成适配器

### Q: 支持哪些 IDEA 版本？

A: 
- 最低版本：IntelliJ IDEA 2023.2
- 推荐版本：2024.1 或更高

## 获取帮助

- 📖 查看 README.md：使用文档
- 🔨 查看 DEVELOPMENT.md：开发指南
- 🏗️ 查看 ARCHITECTURE.md：架构详解
- 💬 GitHub Issues：报告问题
- 📧 邮件：support@example.com

## 下一步

1. **学习代码**：阅读核心服务的实现代码
2. **扩展功能**：按照开发指南添加新的 CLI 支持
3. **测试集成**：使用测试 CLI 脚本验证集成
4. **优化性能**：进行性能测试和优化
5. **提交贡献**：向项目贡献改进

---

**现在就开始开发吧！🚀**
