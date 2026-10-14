# AutoMind

面向智网意图场景的跨域分布式多智能体调度系统（Intelligence Plane for Intent-Driven Networks）。

本项目将自然语言网络意图经由多智能体协同编排（Supervisor / Planner / Executor / Monitor），通过 MCP（Model Context Protocol）标准化工具调用与 A2A（Agent-to-Agent）跨域通信，形成可审计、可验证、可追踪的网络策略执行闭环（Plan-Execute-Verify）。

---

## 目录结构

```text
AutoMind/
├── AutoMind/                  # 后端项目 (Java / Spring Boot / Spring AI)
│   ├── AutoMind-common/       # 公共工具类、通用枚举与基础模型
│   ├── AutoMind-service/      # 核心业务服务、多 Agent 编排、MCP Server、RESTful API
│   ├── AutoMind-simulator/    # 网络拓扑与遥测数据仿真器
│   ├── deliverables/          # 架构图、数据模型设计、项目答辩文档
│   ├── docs/                  # 开发搭建指南、规范与数据初始化 SQL 脚本
│   └── pom.xml                # Maven 根配置
│
├── AutoMind-Vue/              # 前端控制台 (Vue 3 / Vite / TypeScript)
│   ├── public/                # 静态资源
│   ├── src/                   # 前端源码 (视图组件、拓扑可视化、状态管理)
│   ├── package.json           # 前端依赖配置
│   └── vite.config.js         # Vite 构建配置
│
├── .gitignore                 # 根 Git 忽略文件
└── README.md                  # 项目主文档
```

---

## 技术栈

### 后端 (AutoMind)
- **核心框架**：Java 17, Spring Boot 3.4.3
- **AI 编排**：Spring AI 1.1.0, Spring AI Alibaba (DashScope / Qwen-plus)
- **协议与协同**：MCP (Model Context Protocol), A2A (Agent-to-Agent)
- **数据持久化**：MySQL 8.0, MyBatis-Plus 3.5.7, Redis
- **高可用与熔断**：Resilience4j

### 前端 (AutoMind-Vue)
- **核心框架**：Vue 3, Vite, TypeScript
- **状态与路由**：Pinia, Vue Router
- **网络通信**：Axios (支持 Trace-Id 追踪与 SSE 实时事件流)

---

## 快速上手

### 1. 环境准备
- **JDK**：17+
- **Maven**：3.8+
- **Node.js**：18+ 及 npm / pnpm
- **MySQL**：8.0+
- **Redis**：6.0+

### 2. 后端服务启动
1. 导入数据库：
   执行 `AutoMind/docs/网络资源信息表结构与演示数据.sql` 初始化数据库表与模拟拓扑数据。
2. 配置应用参数：
   参考 `AutoMind/AutoMind-service/src/main/resources/application-dev.yaml.example`，在对应目录下创建 `application-dev.yaml`，填入 MySQL、Redis 及 DashScope API Key。
3. 编译并运行：
   ```bash
   cd AutoMind
   mvn clean install -DskipTests
   mvn spring-boot:run -pl AutoMind-service
   ```
   后端服务默认运行在 `http://localhost:9090`，Swagger 文档地址：`http://localhost:9090/swagger-ui.html`。

### 3. 前端界面启动
```bash
cd AutoMind-Vue
npm install
npm run dev
```
前端默认运行在 `http://localhost:5173`。

---

## 核心功能

- **多智能体意图解析与编排**：Supervisor 负责意图校验与分发，Planner 负责路由与资源规划，Executor 负责 MCP 工具下发，Monitor 负责执行验证与 SLA 闭环。
- **MCP 标准化能力暴露**：提供 `get_topo`、`set_qos`、`block_flow` 等标准化网络控制工具。
- **跨域 A2A 通信协议**：实现跨网络域 Agent 的握手、协商与能力路由。
- **实时链路遥测与自愈**：通过 SSE 实时推流链路状态，支持 SLA 违约告警与故障自动编排自愈。
