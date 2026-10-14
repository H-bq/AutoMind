# 全局代码规范

## 命名约定

| 元素 | 规范 | 示例 |
|---|---|---|
| 类名 | PascalCase | `IntentParserService` |
| 方法 / 字段 / 变量 | camelCase | `parseIntent` |
| 常量 | ALL_CAPS_WITH_UNDERSCORES | `MAX_RETRY_COUNT` |
| 包名 | 全小写，按领域分层 | `com.hs.automindservice.agent.planner` |
| 数据库表 / 字段 | snake_case | `intent_request`, `created_at` |

## 统一响应

所有 Controller 方法返回类型统一使用 common 模块的 `R<T>`，不得直接返回裸对象或自定义包装类。

## 异常体系

继承 common 模块的 `CommonException`，按场景选用子类：

| 异常类 | 适用场景 |
|---|---|
| `BizIllegalException` | 业务规则校验失败 |
| `UnauthorizedException` | 鉴权失败（401） |
| `ForbiddenException` | 权限不足（403） |
| `DbException` | 数据库操作异常 |

全局由 `CommonExceptionAdvice` 拦截处理，禁止在 Controller 层 try-catch 后吞掉异常。

## 代码生成

使用 Lombok，禁止手写 getter/setter/toString/equals：
- `@Data`：POJO / DTO
- `@Builder`：需要建造者模式的对象
- `@Slf4j`：日志注入
- `@RequiredArgsConstructor`：Service 层依赖注入（替代 `@Autowired`）

## 日期与序列化

- 日期格式统一：`yyyy-MM-dd HH:mm:ss`
- 时区统一：GMT+8
- 以上均通过 Jackson 全局配置，禁止在字段上单独覆盖（除非有明确的业务差异需求）

## AI 输出规范

大模型自然语言解析结果必须携带置信度字段，低置信度结果不得直接进入执行链路。

## 数据库查询规范

查询方式按复杂度二元选择，不允许混用：

| 场景 | 方式 |
|---|---|
| 单表 / 简单条件 | MyBatis-Plus Lambda 链式查询 |
| 多表关联 / 嵌套 / ResultMap | XML 中编写 SQL |

Lambda 查询正确示例：
```java
// 单条查询
lambdaQuery().eq(User::getId, id).one();

// 列表查询
lambdaQuery().eq(User::getStatus, 1).orderByDesc(User::getCreateTime).list();

// 更新
lambdaUpdate().eq(User::getId, id).set(User::getStatus, 0).update();
```

XML 查询适用场景：多表 JOIN、子查询、聚合统计、自定义 ResultMap 字段映射。

### 严禁 N+1 查询

禁止在任何循环（for / forEach / stream.map）中执行数据库查询，违者视为阻断性问题。

```java
// ❌ 禁止：N+1，每次循环都查库
for (User user : userList) {
    List<Document> docs = documentService.getByUserId(user.getId());
}

// ✅ 正确：批量查询后内存聚合
List<Long> userIds = userList.stream().map(User::getId).toList();
List<Document> allDocs = documentService.lambdaQuery()
    .in(Document::getUserId, userIds).list();
Map<Long, List<Document>> docMap = allDocs.stream()
    .collect(Collectors.groupingBy(Document::getUserId));
```

涉及关联数据时，优先用 XML JOIN 一次查出，而非多次查询拼装。

## 文档规范

新增项目指引保持纯文本、精简描述，只说明做什么，不展开代码实现细节。
