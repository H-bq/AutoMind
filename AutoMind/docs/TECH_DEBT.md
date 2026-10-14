# 已知问题 / 技术债

> 格式：`#编号 [状态] 标题` → 说明 + 处理建议

---

## #1 [待处理] 无测试覆盖

当前 `src/test` 下无任何测试文件，新功能迭代存在回归风险。

**处理建议**：新功能开发同步补充单元测试；存量核心逻辑（Intent 解析、Agent 编排）优先补测。

---

## #2 [待处理] 无 CI/CD 流水线

所有构建和部署为手动操作，缺乏自动化保障。

**处理建议**：接入 GitHub Actions，覆盖构建、测试、打包三个阶段，详见 `docs/DEPLOYMENT.md`。

---

## #3 [待处理] API Key 明文存储

`application-dev.yaml` 中 DashScope API Key 为明文，存在泄露风险。

**处理建议**：改用环境变量（`DASHSCOPE_API_KEY`）注入，本地开发通过 `.env` 文件加载（加入 `.gitignore`）；生产环境使用 Vault 或云厂商密钥管理服务。

---

## #4 [已规避 / 需持续关注] HTTP Client 依赖冲突

Spring AI Alibaba 与 Apache HttpClient5 存在版本冲突，目前通过 `<exclusion>` 手动排除。

**风险点**：升级 `spring-ai-alibaba` 或 `httpclient5` 版本时需重新验证，冲突可能复现。

---

## #5 [待处理] skills/ 和 docs/ 目录文档缺失

`AGENT.md` 技能索引和信息地图中引用的文档均为规划中，尚未实际建立内容。

**处理建议**：开发对应功能模块前先补充对应 skill 文档，避免 AI Agent 按空文档执行。

---

## #6 [待处理] staging/prod 环境配置缺失

目前只有 dev 配置，无法支持多环境部署验证。

**处理建议**：建立 `application-staging.yaml` 和 `application-prod.yaml`，敏感项全部外部注入。

---

## #7 [已修复] pom.xml mainClass 配置错误

~~父 POM 的 `maven-assembly-plugin` 中 `mainClass` 填写为 `com.hmall`（遗留占位符）；`AutoMind-service/pom.xml` 的 `spring-boot-maven-plugin` 中填写为 `com.hs.pilot.Application`。~~

已更正为 `com.hs.AutoMindServiceApplication`。
