# EnsoAI 核心功能与架构分析

> **项目**: EnsoAI - Multiple Agents, Parallel Flow  
> **定位**: 基于 Electron 的增强型 Git Worktree 管理器 + 多 AI Agent 集成  
> **核心理念**: 通过 Git Worktree 技术，让每个功能分支拥有独立的物理目录、编辑器状态、终端会话和 AI Agent 上下文  

---

## 1. 支持的 CLI 工具类型

### 1.1 内置支持的 Agent

| Agent | 提供商 | 特点 | 命令 |
|-------|--------|------|------|
| **Claude** | Anthropic | ✅ 会话持久化，功能最完整 | `claude` |
| **Codex** | OpenAI | ⚠️ 不支持流式输出，支持推理深度 | `codex` |
| **Gemini** | Google | ✅ 支持 Gemini 3 Pro/Flash | `gemini` |
| **Cursor** | Cursor | ⚠️ 不支持会话持久化、工具限制 | `cursor-agent` |
| **Droid** | Factory | 📦 AI 驱动的 CI/CD 助手 | `droid` |
| **Auggie** | Augment Code | 🔧 代码增强助手 | `auggie` |
| **OpenCode** | 自有 | 🆕 开源代码助手 | `opencode` |

### 1.2 自定义 Agent 支持

- ✅ 支持任何基于 CLI 的 Agent
- 通过设置页面添加自定义 Agent
- 指定启动命令、模型选项、环境变量等

### 1.3 Provider 支持

- **Claude Code Provider** - 支持多个 Claude 配置（官方 API、第三方等）
- **Provider 切换器** - 在 SessionBar 快速切换 Provider
- **Provider 临时禁用** - 允许动态禁用单个 Provider

---

## 2. 主要功能模块

### 2.1 多 Agent 矩阵 ⭐

```
Same Project, Different Branches
├── Main Branch
│   └── Claude Session 1 (Haiku)
├── Feature-A
│   └── Codex Session 2 (GPT-5.2)
└── Bugfix-B
    └── Gemini Session 3 (Gemini 3 Pro)
```

**特点**:
- 无缝切换不同 AI 助手
- 每个 Worktree 拥有独立的持久化 AI 会话
- ⚠️ 仅 Claude 支持会话持久化（记住对话历史）
- Codex/Gemini/Cursor 每次独立运行

---

### 2.2 内置 Git 管理器

**可视化版本控制面板**

| 功能 | 说明 |
|------|------|
| **变更列表** | 显示所有修改的文件，支持列表/树形视图 |
| **暂存/取消暂存** | 快速管理文件暂存状态 |
| **代码差异对比** | 基于 Monaco Editor 的 Diff Viewer |
| **提交历史** | 浏览当前分支提交记录 |
| **AI 生成提交信息** | 自动生成高质量的 Commit Message |
| **代码审查** | AI 对每次变更进行深度审查 |
| **冲突解决** | 三栏合并编辑器 |

---

### 2.3 内置代码编辑器

**基于 Monaco Editor 的轻量级编辑器**

```
✨ 特性
├── 多语言支持 - 50+ 种语言高亮
├── 多标签编辑 - 支持拖拽排序
├── 文件树操作 - 创建/重命名/删除
├── 行间注释 - 代码审查评论
├── 自动语言检测 - 根据扩展名
├── 状态持久化 - 跨会话保留
├── 主题同步 - 与 Ghostty 主题实时同步
└── 性能优化 - Local workers，无 CDN
```

---

### 2.4 AI 代码审查 ✨

**高度可定制的代码审查流程**

```
Raw Diff + Commit Log
    ↓
[Claude/Codex/Gemini/Cursor CLI]
    ↓
流式处理，实时输出
    ↓
Markdown 渲染 + 复制/最小化
    ↓
[继续对话] 与当前 Session 关联
```

**特点**:
- ✅ 流式输出（Claude/Gemini/Cursor）
- ⚠️ Codex 仅支持 JSON 输出
- 自定义提示词模板
- 支持选择编程语言
- 推理深度配置（Codex CLI）
- 历史审查记录

---

### 2.5 三栏合并工具

**专业的冲突解决编辑器**

```
┌─────────────┬─────────────┬─────────────┐
│   THEIRS    │   RESULT    │    OURS     │
│  (Source)   │ (Editing)   │  (Target)   │
│             │             │             │
│ Left Panel  │ Middle      │ Right Panel │
│ Accept Btn  │ Editor      │ Accept Btn  │
└─────────────┴─────────────┴─────────────┘
```

**功能**:
- 一键采纳变更（Accept Theirs/Accept Ours）
- 实时结果预览
- 全部采纳/回退功能
- 提交信息输入
- 冲突文件导航（当前 X / 总数 Y）

---

### 2.6 Git Worktree 管理

**毫秒级 Worktree 创建与切换**

| 操作 | 说明 |
|------|------|
| **创建 Worktree** | 从现有分支 / 新建分支 |
| **即时切换** | Cmd+1-9 快速切换 Tab |
| **删除 Worktree** | 可选同时删除分支 |
| **可视化列表** | 显示分支状态、远程跟踪状态 |
| **搜索功能** | 快速查找 Worktree |
| **Worktree 排序** | Drag & Drop 重排序 |

---

### 2.7 IDE 桥接

**EnsoAI 作为编排层与 IDE 深度协作**

```
EnsoAI (轻量级编排)
├── Worktree 管理 → Git 操作
├── AI Agent 控制 → 调用 Claude/Codex
└── 🔗 One-Click "Open In"
    ├── VS Code
    ├── Cursor
    ├── Xcode
    ├── Ghostty
    ├── iTerm2
    └── ... (多个 IDE)
```

**Quick Access**:
- `Cmd+Shift+P` - 命令调色板
- `Cmd+,` - 设置
- `Cmd+1-9` - 标签切换

---

### 2.8 其他特性

| 特性 | 说明 |
|------|------|
| **多窗口支持** | 同时打开多个 Workspace |
| **主题同步** | App 主题与终端主题同步（400+ Ghostty 主题） |
| **键盘快捷键** | 高效导航，macOS 风格优化 |
| **设置持久化** | JSON 格式，容易备份和恢复 |
| **多语言** | 中文/English 完整支持 |
| **快速终端** | 浮动悬窗式临时终端 |
| **Web Inspector** | 网页元素选取并发送到 Agent |
| **Tmux 集成** | 会话持久化和恢复 |
| **MCP 服务器** | Claude IDE Bridge 集成 |
| **Claude 插件** | 支持 Claude Plugin Marketplace |

---

## 3. UI 设计与交互方式

### 3.1 设计系统

**技术栈**:
- **框架**: React 19 + TypeScript
- **样式**: Tailwind CSS 4
- **UI 库**: @coss/ui (基于 Base UI，copy-paste 模式)
- **图标**: Lucide React (300+ 图标)
- **动画**: Framer Motion (Spring 配置)
- **编辑器**: Monaco Editor (本地 Worker)
- **终端**: xterm.js + node-pty

### 3.2 布局架构

```
App.tsx (根组件)
├── [Tree 模式] 仓库 + Worktree 合并侧边栏
│   └── 单个可调整大小的侧边栏
└── [Columns 模式] 仓库和 Worktree 独立侧边栏
    ├── 左侧：仓库列表
    ├── 中间：Worktree 列表
    └── 右侧：主内容

MainContent.tsx (核心调度器)
├── AgentPanel ← AI 对话
├── FilePanel ← 文件编辑
├── TerminalPanel ← Shell/Agent 终端
├── SourceControlPanel ← Git 管理
└── TodoPanel ← 任务列表
```

### 3.3 交互设计

**关键交互模式**:
- ✅ **键盘中心** - 98% 操作可用快捷键完成
- ✅ **拖拽** - 文件标签、Worktree 排序
- ✅ **流式动画** - 平滑过渡和微交互
- ✅ **响应式** - 调整大小的侧边栏和面板
- ✅ **上下文菜单** - 右键操作快速菜单
- ✅ **命令调色板** - Cmd+Shift+P 快速访问

### 3.4 主题与外观

| 特性 | 说明 |
|------|------|
| **深色模式** | 默认深色主题，护眼设计 |
| **Ghostty 主题** | 支持 438+ Ghostty 终端主题 |
| **实时同步** | App 主题与编辑器主题联动 |
| **自定义背景** | 支持图片/视频/文件夹作背景 |
| **字体配置** | 编辑器/终端字体独立配置 |

---

## 4. 多实例管理能力

### 4.1 最小化多实例单位：Worktree

```
Repository
└── Worktree 1 (feat/auth)
    ├── 物理目录: /repo/.git/worktrees/auth/
    ├── 编辑器状态 ← 独立保存
    ├── Terminal Session 1 ← 独立 PTY
    ├── Terminal Session 2 (Agent) ← 独立 Agent CLI 进程
    │   └── Agent Context (会话)
    └── UI Tab State

    Worktree 2 (bugfix/search)
    └── ...完全隔离...
```

### 4.2 多层级管理

**Repository 级别**:
- 多个 Git 仓库支持
- 仓库分组功能
- 仓库隐藏/显示
- 快速搜索

**Worktree 级别**:
- 独立分支管理
- 快速创建/删除
- 拖拽排序
- 可视化分支状态

**Agent Session 级别**:
- 每个 Worktree 独立会话
- 会话历史保存（Claude）
- 快速切换 Agent
- Session 持久化

### 4.3 会话持久化策略

| Agent | 会话持久化 | 说明 |
|-------|-----------|------|
| **Claude** | ✅ 完全支持 | `~/.claude/sessions/` 存储，可恢复对话 |
| **Codex** | ❌ 不支持 | 每次独立运行 |
| **Gemini** | ❌ 不支持 | 每次独立运行 |
| **Cursor** | ❌ 不支持 | 每次独立运行 |

---

## 5. 代码架构与技术栈

### 5.1 项目结构

```
EnsoAI/
├── src/
│   ├── main/              # Electron 主进程
│   │   ├── index.ts       # 应用入口
│   │   ├── ipc/           # IPC Handlers (17 模块)
│   │   │   ├── agent.ts
│   │   │   ├── git.ts
│   │   │   ├── terminal.ts
│   │   │   ├── files.ts
│   │   │   └── ...
│   │   └── services/      # 业务服务
│   │       ├── claude/    # Claude IDE Bridge (MCP 集成)
│   │       ├── git/       # simple-git 封装
│   │       ├── terminal/  # node-pty 管理
│   │       ├── ai/        # AI Service 统一入口
│   │       ├── cli/       # CLI 检测与安装
│   │       └── files/     # 文件监控
│   │
│   ├── renderer/          # React 前端 (1141+ 组件)
│   │   ├── App.tsx        # 根组件 (App 核心逻辑)
│   │   ├── components/    # UI 组件 (按功能域分)
│   │   │   ├── ui/        # @coss/ui 基础组件 (52 文件)
│   │   │   ├── layout/    # 布局组件
│   │   │   ├── chat/      # Agent 对话
│   │   │   ├── files/     # 编辑器 + 文件树
│   │   │   ├── terminal/  # 终端组件
│   │   │   ├── source-control/ # Git UI
│   │   │   └── ...
│   │   ├── stores/        # Zustand (14+ 文件)
│   │   │   ├── settings.ts    # 全局设置 (37KB，最复杂)
│   │   │   ├── editor.ts
│   │   │   ├── terminal.ts
│   │   │   └── ...
│   │   └── hooks/         # React Hooks (14+ 文件)
│   │       ├── useXterm.ts    # xterm.js 集成 (26KB)
│   │       ├── useFileTree.ts # 文件树逻辑 (13KB)
│   │       └── ...
│   │
│   ├── preload/           # Electron 预加载脚本
│   │   └── index.ts       # ContextBridge API
│   │
│   └── shared/            # 跨进程共享
│       └── types/         # TypeScript 类型定义
│           ├── ipc.ts     # IPC 通道定义 (关键)
│           ├── cli.ts     # CLI 类型
│           └── ...
│
├── resources/             # 静态资源
│   ├── Ghostty themes/    # 438+ 主题
│   └── ...
├── docs/                  # 文档
│   ├── architecture.md    # 架构文档
│   ├── design-system.md   # 设计规范
│   └── plans/             # 实现计划
└── build/                 # Electron Builder 配置
```

### 5.2 核心服务

#### **Git Service**
```typescript
git/
├── runtime.ts      # 底层命令执行
├── WorktreeManager # Worktree 管理
├── BranchManager   # 分支管理
└── CommitManager   # 提交管理
```
- 基于 `simple-git` 封装
- `authorizedWorkdirs` 白名单安全机制
- AI 增强：
  - `GIT_GENERATE_COMMIT_MSG` - Claude 生成提交信息
  - `GIT_CODE_REVIEW_START` - 流式代码审查

#### **PTY Manager**
```typescript
terminal/
├── PtyManager.ts          # 虚拟终端管理
├── AgentTerminalManager   # Agent 命令行管理
└── ShellTerminalManager   # Shell 命令行管理
```
- 统一管理所有虚拟终端实例
- 数据流：Renderer ↔ IPC ↔ node-pty
- 优雅关闭：等待 PTY 完全终止

#### **File Watcher**
```typescript
files/
├── FileWatcher.ts  # 文件变更监控
├── EncodingDetector # 编码检测
└── GitIgnoreManager # .gitignore 支持
```
- 多路目录监控
- 编码检测：`jschardet` + `iconv-lite`
- Git 忽略集成

#### **Claude IDE Bridge** ⭐
```typescript
claude/
├── ClaudeIdeBridge.ts     # WebSocket IDE 桥接
├── ClaudeProviderManager  # API 配置 (~/.claude/settings.json)
├── McpManager             # MCP 服务器 (~/.claude.json)
├── PromptsManager         # CLAUDE.md 管理
└── ClaudeHookManager      # 钩子注入 (~/.claude/hooks/)
```
- WebSocket 连接：IDE 感知
- Hook 系统：
  - `Stop` - Agent 完成通知
  - `PermissionRequest` - 权限请求
  - `PreToolUse` - 工具使用前
  - `UserPromptSubmit` - 用户输入提交
- Status Line 显示 Agent 实时状态

#### **AI Service**
```typescript
ai/
├── providers.ts      # Provider 抽象层（支持 Claude/Codex/Gemini/Cursor）
├── commit-message.ts # Commit Message 生成
├── code-review.ts    # Code Review 流式处理
├── branch-name.ts    # 分支名生成
└── todo-polish.ts    # Todo 内容润色
```

### 5.3 前端状态管理

**Zustand Stores** (14+ 文件)

```typescript
stores/
├── settings.ts              # 全局设置 (37KB)
│   ├── Claude Code 集成设置
│   ├── AI 功能配置
│   ├── 编辑器偏好
│   ├── 终端设置
│   └── UI 主题
├── editor.ts                # 编辑器状态
├── terminal.ts              # 终端会话
├── sourceControl.ts         # Git 状态
├── codeReviewContinue.ts    # 代码审查
├── agent.ts                 # Agent 管理
└── ...
```

**异步数据管理**:
- React Query 用于：
  - Worktree 列表
  - Git 分支列表
  - 文件树变更
  - CLI 检测状态

### 5.4 IPC 通信架构

**17 个 IPC Handler 模块**

```typescript
ipc/
├── index.ts              # 统一注册 + 资源清理
├── agent.ts              # Agent 控制
├── git.ts                # Git 操作
├── terminal.ts           # Terminal/PTY 管理
├── files.ts              # 文件 I/O
├── window.ts             # 窗口控制
├── settings.ts           # 设置读写
├── claudeConfig.ts       # Claude 配置
├── worktree.ts           # Worktree 管理
├── repository.ts         # Repository 管理
└── ...
```

**通信模式**:
```
请求-响应: ipcRenderer.invoke() ↔ ipcMain.handle()
主动推送: webContents.send() ← ipcRenderer.on()
```

### 5.5 类型系统

**核心类型文件** (`src/shared/types/`)

| 文件 | 用途 |
|------|------|
| `ipc.ts` | IPC 通道定义 (最重要，所有通信通道) |
| `cli.ts` | CLI 工具类型 |
| `claude.ts` | Claude 相关类型 |
| `index.ts` | 全局导出 |

### 5.6 技术栈总体

| 层次 | 主要技术 | 说明 |
|------|---------|------|
| **框架层** | Electron 39+ | 桌面应用框架 |
| **前端** | React 19, TypeScript 5.9 | 组件框架 + 类型安全 |
| **样式** | Tailwind CSS 4 | 原子化 CSS |
| **UI 组件** | @coss/ui (52 文件) | 设计系统 + Copy-paste |
| **编辑器** | Monaco Editor | 代码编辑 |
| **终端** | xterm.js + node-pty | Terminal 模拟 |
| **状态管理** | Zustand + React Query | 轻量级 + 异步 |
| **动画** | Framer Motion | Spring 物理引擎 |
| **图标** | Lucide React | 矢量图标库 |
| **Git** | simple-git | Git 命令包装 |
| **构建** | electron-vite | Vite + Electron |
| **打包** | electron-builder | 跨平台构建 |
| **国际化** | 自实现 i18n | 中/English 支持 |

---

## 6. 特色功能与创新点

### 6.1 🌟 Git Worktree + AI Agent 深度融合

**业界首创**：在 Electron 中实现真正的并行开发体验

```
Traditional IDE:          EnsoAI:
Main Branch              Main Branch + Agent 1
  └─ Agent Session         Feature-A + Agent 2 (isolated)
  
切换分支需要：              切换只需：
1. 停止当前 Agent           Cmd+3 → 瞬间切换
2. 清理工作目录             Agent 2 的上下文自动恢复
3. Checkout 新分支
4. 重新启动 Agent
```

### 6.2 🔄 多 Agent 无缝切换

- 支持 7 个官方 Agent + 无限自定义 Agent
- 同一项目不同分支可运行不同 Agent
- A/B 测试不同 AI 模型的能力

### 6.3 🏗️ IDE 桥接设计

```
One-Click Workflow:
EnsoAI (Orchestration)
  ├─ Worktree 管理 ✓
  ├─ AI 交互 ✓
  └─ Open In VS Code 一键深潜 ✓
     └─ VS Code (Deep Development)
        └─ 编辑、调试、测试
```

### 6.4 🎯 Claude IDE Bridge（MCP 集成）

**基于 MCP 协议的深度 IDE 集成**

```
Claude Code CLI          EnsoAI
     ↕ WebSocket (MCP)   ↕
  ┌──────────────────────┐
  │ Selection Changed    │ → 代码选中同步
  │ At Mentioned         │ → @提及输入
  │ Stop Hook            │ → 完成通知
  │ Permission Request   │ → 权限请求
  └──────────────────────┘
```

### 6.5 ✅ 高级代码审查

- 自动生成和可定制的审查提示词
- 支持 4 个 AI 模型的多角度审查
- 继续对话能力（与当前 Session 关联）
- 详细的变更分析和建议

### 6.6 🧩 三栏合并编辑器

- 行对齐的冲突可视化
- 一键采纳或智能合并
- 实时 Diff 预览

### 6.7 💾 会话持久化（Claude Only）

```
关闭        重新打开
   ↓          ↓
Session 1  Session 1
对话历史 → [恢复] → 继续对话
```

### 6.8 🎨 主题生态

- 438+ Ghostty 主题
- App 与编辑器/终端主题实时同步
- 自定义背景（图片/视频/文件夹）

---

## 7. 开发工作流示例

```
1️⃣ 打开 Workspace
   └─ 选择或添加 Git 仓库

2️⃣ 创建/切换 Worktree
   └─ 为新功能创建 worktree（自动关联新分支）

3️⃣ 启动 AI Agent
   └─ 在 Agent 面板与 Claude/Codex 对话
   └─ AI 直接在当前 worktree 目录下工作

4️⃣ 编辑 & 测试
   └─ 使用内置编辑器快速修改
   └─ 使用终端运行测试/构建

5️⃣ 代码审查 (可选)
   └─ AI 自动审查变更
   └─ 修复建议集成

6️⃣ 提交 & 合并
   └─ 自动生成 Commit Message
   └─ 通过终端 git commit/push
   └─ 或通过 "Open In" 跳转 IDE 进行最终审查
```

---

## 8. 适用场景

| 场景 | 适用性 | 说明 |
|------|--------|------|
| **多任务并行开发** | ✅ 完美 | Feature A + 同时修复 Bug B，互不干扰 |
| **AI 辅助 Code Review** | ✅ 完美 | 在新 Worktree 审查代码，主分支开发不受影响 |
| **实验性开发** | ✅ 完美 | 创建临时 worktree 让 AI 自由实验，不满意直接删除 |
| **对比调试** | ✅ 完美 | 多个 Worktree 并排对比不同实现 |
| **大型 Monorepo** | ⚠️ 建议配合 IDE | EnsoAI + VS Code 协作最佳 |
| **深度开发** | ⚠️ 不是首选 | 适合用 IDE，EnsoAI 作为编排层 |
| **中小项目** | ✅ 理想 | 轻量级，开箱即用 |

---

## 9. 与当前工作室项目对比

### 工作室项目 (ai-code-cli-plugin for JetBrains)

| 维度 | 工作室项目 | EnsoAI |
|------|----------|--------|
| **平台** | JetBrains IDE 集成 | 独立 Electron 应用 |
| **核心** | IDE 插件（受限） | 完整应用（自由度高） |
| **Worktree 管理** | ❌ 无 | ✅ 完全支持 |
| **多 Agent** | ⚠️ 支持多个 | ✅ 深度集成 |
| **编辑器** | ✅ 使用 IDE 编辑器 | ✅ 内置 Monaco |
| **终端** | ❌ 有限 | ✅ 完整 xterm |
| **持久化会话** | ❌ 无 | ✅ Claude 支持 |
| **三栏合并** | ❌ 无 | ✅ 专业编辑器 |
| **开发体验** | IDE 开发者 | 轻量级 + IDE 配合 |

### 互补性

```
工作室项目 (IDE inside)
     ↓
深度开发，充分利用 IDE 功能

EnsoAI (Orchestration layer)
     ↓
Worktree 管理、AI 交互
     ↓
需要深潜时：Open In IDE
```

---

## 10. 技术亮点总结

| 亮点 | 等级 | 说明 |
|------|-----|------|
| **Worktree 隔离模型** | ⭐⭐⭐⭐⭐ | 行业罕见，真正的并行开发 |
| **Claude IDE Bridge** | ⭐⭐⭐⭐⭐ | MCP 深度集成，IDE 感知 |
| **多 Agent 支持** | ⭐⭐⭐⭐ | 初步支持，未来可扩展 |
| **Workspaces 架构** | ⭐⭐⭐⭐ | 模块化，代码组织清晰 |
| **会话持久化（Claude）** | ⭐⭐⭐⭐ | 唯一支持，体验好 |
| **UI/UX 设计** | ⭐⭐⭐⭐ | 现代设计系统，交互顺滑 |
| **三栏合并编辑器** | ⭐⭐⭐⭐ | 冲突解决体验优秀 |
| **跨平台构建** | ⭐⭐⭐ | Electron Builder，覆盖广 |

---

## 总结

**EnsoAI** 是一个**高度创新的 AI 编程助手应用**，其核心创新在于：

1. **Git Worktree + AI Agent 的完美结合** - 实现真正的并行开发
2. **多 Agent 支持** - 在同一项目中灵活切换不同 AI 模型
3. **IDE 桥接设计** - 轻量级应用 + 全功能 IDE 的完美组合
4. **专业级开发体验** - 从代码审查到冲突解决的全链路支持

**适用人群**：
- ✅ 需要并行开发多个功能分支的开发者
- ✅ 想体验最新 AI 辅助编程的开发者
- ✅ 喜欢轻量级工具的开发者
- ✅ 中小型项目开发团队

**技术可学习价值**：
- 🎯 Electron + React 深度集成
- 🎯 Claude IDE Bridge (MCP) 实现
- 🎯 PTY 管理和终端集成
- 🎯 Git 工作流自动化
- 🎯 Zustand 状态管理最佳实践
- 🎯 Tailwind + @coss/ui 设计系统
