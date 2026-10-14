# 开发环境搭建

## 环境依赖

| 依赖 | 版本要求 | 说明 |
|---|---|---|
| JDK | 17（强制） | 编译目标 Java 17，不兼容低版本 |
| Maven | 3.8+ | 多模块构建 |
| MySQL | 8.0 | dev 地址：47.117.156.128:3307，库名 c4 |
| Redis | 6+ | dev 地址：47.117.156.128:6379 |
| DashScope API Key | — | 配置在 application-dev.yaml 的 aliyun.openai.api-key |

## 启动步骤

1. 确认 `AutoMind-service/src/main/resources/application-dev.yaml` 中数据库、Redis、API Key 配置正确。
2. 在项目根目录执行全量构建：
   ```bash
   mvn clean install -DskipTests
   ```
3. 启动主服务（二选一）：
   ```bash
   # 方式一：Maven 命令
   mvn spring-boot:run -pl AutoMind-service

   # 方式二：IDE 直接运行主类
   com.hs.AutoMindServiceApplication
   ```
4. 验证启动：
   - 服务端口：`http://localhost:9090`
   - Swagger UI：`http://localhost:9090/swagger-ui.html`

## 常用命令速查

```bash
# 全量构建（跳过测试）
mvn clean install -DskipTests

# 仅编译检查
mvn compile

# 运行主服务
mvn spring-boot:run -pl AutoMind-service

# 打包可运行 JAR
mvn package -DskipTests -pl AutoMind-service

# 运行打包产物
java -jar AutoMind-service/target/AutoMind-service.jar --spring.profiles.active=dev
```
