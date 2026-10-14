# 测试策略

## 框架

Spring Boot Test（JUnit 5），依赖已在 `AutoMind-service/pom.xml` 的 test scope 中引入，无需额外添加。

## 测试文件组织

测试文件与源文件镜像放置：

```
AutoMind-service/
└── src/
    ├── main/java/com/hs/automindservice/
    │   └── service/IntentParserService.java
    └── test/java/com/hs/automindservice/
        └── service/IntentParserServiceTest.java   ← 对应测试
```

## 分层测试原则

**单元测试**（优先级最高）
- 覆盖 Service 层核心逻辑和工具类。
- 使用 Mockito mock 外部依赖（数据库、Redis、大模型调用）。
- 每个公共方法至少覆盖正常路径和一条异常路径。

**集成测试**
- 覆盖 Agent 编排流程、MCP 工具调用链路。
- 使用 `@SpringBootTest` + Testcontainers（或 H2）隔离外部依赖。

**AI 输出测试**
- 不对大模型内容做精确断言（模型输出不确定）。
- 对结构化输出的 schema 合法性（字段存在、类型正确、置信度字段非空）做断言。

## 当前状态

测试覆盖率近乎为零（见 `docs/TECH_DEBT.md` #1）。

新功能开发规范：**新增 Service 方法必须同步补充对应单元测试**，否则 PR 不予合入。
