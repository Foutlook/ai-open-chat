# 多 CLI 架构升级指南

## 🎯 快速开始 - 新架构使用

### 第 1 步：打开 CLI 实例面板

- 菜单栏：**AI CLI Integration → Open CLI Instances**
- 或快捷键：**Ctrl+Shift+Alt+O**
- 工具窗口底部会显示：**AI CLI Instances**

### 第 2 步：启动第一个 CLI

1. 点击工具窗口中的 **[Start]** 按钮
2. 或菜单：**AI CLI Integration → Start CLI Instance**
3. 或快捷键：**Ctrl+Shift+Alt+S**

4. 选择 CLI 类型：
   - OpenCode
   - Codex
   - Claude Code

5. 选择可执行文件（通过文件浏览器）

6. 输入工作目录（默认为项目目录）

7. 点击 **OK** - CLI 启动完成！

### 第 3 步：启动第二个 CLI（可选）

重复第 2 步，选择另一个 CLI 工具。现在你有 2 个 CLI 实例运行！

### 第 4 步：向 CLI 发送代码

1. 打开/编辑一个代码文件
2. 菜单：**AI CLI Integration → Send File to CLI**
3. 或快捷键：**Ctrl+Shift+Alt+F**
4. 选择目标 CLI 实例
5. 点击 **OK**
6. 代码发送给 CLI，结果显示在工具窗口

### 第 5 步：查看输出和管理实例

在 **AI CLI Instances** 工具窗口中：
- **Console** 标签：查看所有 CLI 的实时输出
- **Instances** 标签：查看所有运行的实例、状态和快速控制

## 📊 新架构主要区别

| 项目 | 旧架构 | 新架构 |
|-----|--------|--------|
| **配置方式** | 预先配置 CLI 路径 | 动态选择和启动 |
| **同时运行** | 仅 1 个 CLI | 多个 CLI |
| **启动方式** | 通过设置页面 | 通过菜单/操作 |
| **管理方式** | 单一服务 | 实例管理器 |
| **UI** | 单一工具窗口 | 多实例面板 + 标签页 |
| **事件系统** | 基础回调 | 完整事件总线 |

## 🔄 从旧架构迁移

### 保留旧功能
旧架构仍然可用，如需继续使用：
- 设置页面：**File → Settings → Tools → AI CLI Integration**
- 旧工具窗口：**AI CLI Integration**（为辅助工具窗口）

### 完全迁移到新架构

#### 步骤 1：删除旧的配置
- 不需要手动删除，新架构独立运行

#### 步骤 2：使用新的 API（如需集成代码）

**旧方式**：
```kotlin
val cliService = project.getService(CliConnectionService::class.java)
cliService.connect(config)
```

**新方式**：
```kotlin
val managerService = project.getService(CliInstanceManagerService::class.java)
managerService.startCliInstance(instance)
```

#### 步骤 3：使用新的事件系统

**旧方式**：
```kotlin
cliService.addResponseListener(listener)
```

**新方式**：
```kotlin
managerService.addListener(object : CliInstanceListener {
    override fun onInstanceEvent(event: CliInstanceEvent) {
        when (event) {
            is CliInstanceEvent.Created -> { /* ... */ }
            is CliInstanceEvent.Output -> { /* ... */ }
            is CliInstanceEvent.Error -> { /* ... */ }
            else -> {}
        }
    }
})
```

## 🎓 常见问题

### Q: 我的旧配置会丢失吗？
A: 不会。旧配置仍保存在 `ai-cli-integration.xml`，可继续使用。

### Q: 可以同时使用旧和新架构吗？
A: 可以，它们独立运行，不会互相影响。

### Q: 最多可以启动多少个 CLI 实例？
A: 理论上无限制，但考虑系统资源，建议 ≤ 10 个。

### Q: CLI 进程在 IDE 关闭时会发生什么？
A: 会被正确终止。插件在卸载时清理所有资源。

### Q: 支持哪些命令行参数？
A: 启动时指定工作目录。CLI 工具的参数需要在启动脚本中配置。

### Q: 如何持久化 CLI 配置？
A: 目前每次启动都需要选择。后续版本会支持保存最近使用的 CLI 配置。

## 📈 工作流示例

### 场景：同时使用三个 AI 工具对比代码建议

1. **启动 3 个 CLI 实例**
   ```
   ☑ OpenCode-1234
   ☑ Claude Code-5678
   ☑ Codex-9012
   ```

2. **编写代码**
   ```java
   public class Main {
       public static void main(String[] args) {
           // 编写你的代码
       }
   }
   ```

3. **发送到 OpenCode**
   - 快捷键：Ctrl+Shift+Alt+F
   - 选择：OpenCode-1234
   - 查看：OpenCode 的分析结果

4. **发送到 Claude Code**
   - 快捷键：Ctrl+Shift+Alt+F
   - 选择：Claude Code-5678
   - 查看：Claude 的改进建议

5. **发送到 Codex**
   - 快捷键：Ctrl+Shift+Alt+F
   - 选择：Codex-9012
   - 查看：Codex 的补全建议

6. **对比三个建议**</p>
   - 在工具窗口的 Console 标签中查看所有输出
   - 每个 CLI 的输出都有标记
   - 选择最好的建议应用到代码中

## 🚀 高级用法

### 自定义 CLI 启动脚本

创建 `my-cli-wrapper.sh`：
```bash
#!/bin/bash
# 包装脚本可以设置环境变量、传递参数等
export PYTHONPATH=/path/to/python/libs
exec /path/to/my-cli "$@"
```

然后在启动 CLI 时选择这个脚本作为可执行文件。

### 编程方式启动 CLI

```kotlin
val instance = CliInstance(
    name = "MyCustomCLI",
    cliType = CliType.OPENCODE,
    executablePath = "/path/to/cli",
    workingDirectory = project.basePath!!
)

val managerService = project.getService(CliInstanceManagerService::class.java)
managerService.startCliInstance(instance)
```

### 监听特定实例的输出

```kotlin
managerService.addListener(object : CliInstanceListener {
    override fun onInstanceEvent(event: CliInstanceEvent) {
        when (event) {
            is CliInstanceEvent.Output -> {
                if (event.instanceId == targetInstanceId) {
                    handleOutput(event.output)
                }
            }
            else -> {}
        }
    }
})
```

## 📚 相关文档

- [MULTI_CLI_ARCHITECTURE.md](./MULTI_CLI_ARCHITECTURE.md) - 详细架构设计
- [README.md](./README.md) - 插件基本说明
- [DEVELOPMENT.md](./DEVELOPMENT.md) - 开发指南

## 🆘 故障排除

### CLI 无法启动
1. 检查可执行文件路径是否正确
2. 确保文件有执行权限：`chmod +x /path/to/cli`
3. 检查工作目录是否存在和可写
4. 查看日志：Help → Show Log in Explorer

### 连接后无输出
1. 确认 CLI 工具已正确启动
2. 检查是否有错误输出（Instances 标签）
3. 查看 CLI 工具的日志文件

### UI 不响应
1. 重启 IDE
2. 检查是否有过多的 CLI 实例运行
3. 清理并停止所有实例

## 📞 获取帮助

- GitHub Issues: https://github.com/Foutlook/ai-open-chat/issues
- 查看日志：IDE 中 Help → Show Log in Explorer → idea.log

---

**现在你已准备好使用新的多 CLI 架构了！🎉**
