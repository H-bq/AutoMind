# Git 工作流

## 分支策略

| 分支 | 用途 | 规则 |
|---|---|---|
| `main` | 生产就绪代码 | 只接受 PR 合入，禁止直接 push |
| `develop` | 集成分支 | 功能开发完成后合入此分支 |
| `feature/<描述>` | 功能开发 | 从 develop 切出，完成后向 develop 发起 PR |
| `fix/<描述>` | 缺陷修复 | 从 develop 切出 |
| `hotfix/<描述>` | 紧急修复 | 从 main 切出，修复后同时合入 main 和 develop |

## 提交信息规范

采用 [Conventional Commits](https://www.conventionalcommits.org/) 格式：

```
<type>(<scope>): <简短描述>

[可选正文]
```

type 取值：

| type | 含义 |
|---|---|
| `feat` | 新功能 |
| `fix` | 缺陷修复 |
| `refactor` | 重构（不改变功能） |
| `docs` | 文档变更 |
| `test` | 测试相关 |
| `chore` | 构建 / 依赖 / 配置 |
| `perf` | 性能优化 |

scope 取值（模块名）：`agent` | `intent` | `mcp` | `a2a` | `common` | `config` | `docs`

示例：
```
feat(agent): 添加 Planner Agent 意图拆解逻辑
fix(mcp): 修复工具调用超时未重试问题
docs(setup): 更新开发环境依赖说明
```

## PR 流程

1. 发起 PR 前确保本地 `mvn compile` 无报错。
2. PR 标题遵循提交信息规范（type + scope + 描述）。
3. PR 描述中说明：改动范围、影响模块、测试验证情况。
4. 至少一名成员 Review 通过后方可合入。
