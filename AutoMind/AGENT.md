# AutoMind — 项目全局指引

> AI Agent 入口：你是 AutoMind 首席 AI 开发者。每次会话开始时主动读取此文档以恢复全局架构认知，所有开发判断以本文档和信息地图中列出的参考文档为准，不引入未被文档支持的业务假设。

---

## 0. Agent 读取规则（强制）

1. **主入口文件**：本项目主指引文件为 `AGENT.md`。若运行环境优先识别 `AGENTS.md`，则 `AGENTS.md` 仅作为兼容入口，必须继续读取本文件。
2. **按需披露**：会话开始只需要先读取 `AGENT.md`。当任务涉及环境、规范、测试、部署、技术债、MCP、功能范围或业务背景时，再按“文档索引”读取对应参考文档。
3. **PowerShell 编码**：本项目 Markdown 文档均按 UTF-8 保存。通过 PowerShell 读取中文文档时，必须显式使用 `-Encoding UTF8`，避免中文乱码导致上下文失真。
4. **推荐读取命令**：

```powershell
Get-Content -Path AGENT.md -Encoding UTF8
Get-Content -Path docs\SETUP.md -Encoding UTF8
Get-Content -Path docs\功能点清单.md -Encoding UTF8
```

5. **中文文件名策略**：中文文档名允许保留，不强制改为英文。若后续发现某个工具链无法稳定处理中文路径，再为该文档增加英文别名或迁移路径，并同步更新本文档索引。

---

## 1. 项目概述

**做什么**：面向智网意图场景的跨域分布式多智能体调度系统。将 C 端用户的自然语言网络需求，经由 Intelligence Plane 解析、编排并下发，形成可审计、可验证、可追踪的网络策略执行闭环（PEV：Plan-Execute-Verify）。

**核心角色**：Supervisor Agent、Planner Agent、Executor Agent、Monitor Agent，通过 MCP（Model Context Protocol）标准化工具调用与 A2A（Agent-to-Agent）跨域语义通信协同工作。

**技术栈**：Java 17 / Spring Boot 3.4.3 / Spring AI 1.1.0 + Spring AI Alibaba 1.1.0.0-RC2（DashScope / Qwen-plus）/ MyBatis-Plus 3.5.7 / MySQL 8.0（库名 c4）/ Redis / 阿里云 OSS / 火山引擎 Ark（豆包）/ Resilience4j

**架构风格**：Maven 多模块单体（AutoMind-common + AutoMind-service），预留微服务拆分空间。

**当前阶段**：不接入真实物理网络设备或生产流量，只面向软件模拟与演示验证。

---

## 2. 项目结构概览

```
AutoMind/
├── pom.xml                        # 父 POM，统一依赖版本管理
├── AGENT.md                       # 本文件：全局指引 + AI Agent 上下文
├── AGENTS.md                      # 兼容入口：指向 AGENT.md
├── CONTRIBUTING.md                # Git 工作流与提交规范
├── AutoMind-common/               # 公共模块（jar）
│   └── src/main/java/common/
│       ├── advice/                # 全局异常处理（CommonExceptionAdvice）
│       ├── config/                # MyBatis、Jackson 全局配置
│       ├── domain/                # 基础数据模型（R、PageDTO、PageQuery、File）
│       ├── exception/             # 异常体系（CommonException 及子类）
│       └── utils/                 # 工具类（BeanUtils、CollUtils、UserContext、AliyunOSS、PDF 等）
├── AutoMind-service/              # 主服务模块，端口 9090
│   └── src/main/
│       ├── java/com/hs/automindservice/
│       │   └── AutoMindServiceApplication.java  # 主入口
│       └── resources/
│           ├── application.yaml           # 主配置
│           └── application-dev.yaml       # Dev 环境覆盖（含敏感 Key，勿提交公开仓库）
├── docs/                          # 项目文档
│   ├── SETUP.md                   # 开发环境搭建与常用命令
│   ├── STANDARDS.md               # 全局代码规范
│   ├── TESTING.md                 # 测试策略
│   ├── DEPLOYMENT.md              # 部署与 CI/CD
│   ├── TECH_DEBT.md               # 已知问题与技术债
│   ├── mcp_registry.md            # MCP Server 工具登记（待建立）
│   ├── 功能点清单.md               # 功能范围快速确认（待建立）
│   └── 开发者文档.md               # 系统分层、模块职责、数据模型（待建立）
└── skills/                        # AI Agent SOP 技能文档（待建立）
    ├── spring_ai_orchestration.md
    ├── java_python_bridge.md
    ├── a2a_protocol_impl.md
    ├── mcp_integration.md
    └── automated_startup_recovery.md
```

**关键入口**：`AutoMind-service/src/main/java/com/hs/AutoMindServiceApplication.java`

---

## 3. 文档索引

| 文档 | 路径 | 用途 |
|---|---|---|
| 开发环境 | `docs/SETUP.md` | 环境依赖、启动步骤、常用命令 |
| 代码规范 | `docs/STANDARDS.md` | 命名、异常、数据库查询、AI 输出规范 |
| Git 工作流 | `CONTRIBUTING.md` | 分支策略、提交规范、PR 流程 |
| 测试策略 | `docs/TESTING.md` | 框架选择、分层原则、测试组织 |
| 部署说明 | `docs/DEPLOYMENT.md` | 打包、环境配置、CI/CD 规划 |
| 技术债 | `docs/TECH_DEBT.md` | 已知问题、风险项、修复状态 |
| MCP 工具登记 | `docs/mcp_registry.md` | MCP Server 能力、权限边界（待建立） |
| 功能点清单 | `docs/功能点清单.md` | 功能范围快速确认（待建立） |
| 项目概述文档 | `C:/Users/Lenovo/Downloads/项目文档.md` | 业务目标、用户角色、成功指标 |
| 项目开发者文档 | `C:/Users/Lenovo/Downloads/开发者文档.md` | 系统分层、数据模型、API 范围 |

---

## 4. 执行哲学（AI Agent 专项）

1. **即时上下文加载**：每次会话开始时主动读取此 AGENT.md，需要细节时再按文档索引按需加载对应文档。
2. **需求来源对齐**：所有开发判断必须回到文档索引中列出的参考文档，不引入未被文档支持的业务假设。
3. **原子化任务执行**：将用户提出的自然语言需求拆解为可验证的原子功能单元。
4. **闭环优先**：涉及意图解析、Agent 调度、MCP 调用、遥测监控、自愈恢复的任务，必须保留执行记录与验证结果。
5. **边界先行**：任何高风险策略变更、权限相关操作、跨域通信和自愈重试，必须优先确认安全边界与降级路径。

---

## 5. 技能索引（AI Agent 专项）

执行特定任务前，先确认与任务相关后再深读对应文档：

| 技能文档 | 覆盖范围 |
|---|---|
| `skills/spring_ai_orchestration.md` | Spring AI 意图解析、多智能体编排、结构化输出、工具调用 |
| `skills/java_python_bridge.md` | Java-Python 跨语言调用边界、数据契约、容错要求 |
| `skills/a2a_protocol_impl.md` | 跨域 Agent 语义消息、链路状态交换、降级自治、审计 |
| `skills/mcp_integration.md` | MCP 工具调用、能力注册、错误码、认证要求 |
| `skills/automated_startup_recovery.md` | 服务启动观测、失败归因、有限重试、人工接管 |

---

## 6. 开发约束（Hard Constraints）

- **技术版本**：Java 17、Spring Boot 3.4.3、Spring AI 1.1.0+ 不得降级。
- **前端基座**：C 端 Service Portal 与 B 端 Command Center 面向 React 18 + TypeScript + 桌面/平板场景。
- **数据边界**：意图请求、审计日志、Agent 任务、MCP 调用、SLA 记录和遥测数据必须保留可追踪关系。
- **安全边界**：所有用户请求、MCP 调用、A2A 通信和数据查询必须携带租户与角色语义。
- **审计边界**：意图提交、审批决策、策略变更、MCP 调用、自愈触发和 SLA 校验必须留存审计轨迹。
- **自愈边界**：自愈流程必须有最大重试次数（Resilience4j 当前配置 3 次）、失败告警和人工接管出口。
- **大模型边界**：自然语言解析必须输出置信度，低置信度结果不得直接进入执行链路。

---

## 7. OpenAPI 注解规范（强制）

每个 Controller 接口方法必须写完整以下注解，SpringDoc 会自动生成契约：

### Controller 方法
@Operation(summary = "接口简介", description = "详细说明（可选）")
@ApiResponse(responseCode = "200", description = "成功",
content = @Content(schema = @Schema(implementation = XxxVO.class)))

### 接口参数
- @RequestParam → 加 @Parameter(description = "字段说明", required = true/false)
- @PathVariable  → 加 @Parameter(description = "字段说明")
- @RequestBody   → 对应的 DTO 类每个字段加 @Schema(description = "字段说明", example = "示例值")

### DTO / VO 实体类
@Schema(description = "整个对象的说明")
public class XxxDTO {
@Schema(description = "用户ID", example = "10001")
private Long userId;
}

(目的：前端访问 http://localhost:9090/v3/api-docs 即可获取完整 OpenAPI 3.0 契约)
