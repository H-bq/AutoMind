# 部署与 CI/CD

## 打包

使用 `spring-boot-maven-plugin` 生成 fat JAR：

```bash
mvn package -DskipTests -pl AutoMind-service
# 产物：AutoMind-service/target/AutoMind-service.jar
```

## 环境说明

| 环境 | Profile | 配置文件 | 状态 |
|---|---|---|---|
| dev | `dev` | `application-dev.yaml` | 已配置，指向云上测试实例 |
| staging | `staging` | `application-staging.yaml` | 待建立 |
| prod | `prod` | `application-prod.yaml` | 待建立 |

## 启动命令

```bash
# dev 环境
java -jar AutoMind-service.jar --spring.profiles.active=dev

# staging 环境（配置就绪后）
java -jar AutoMind-service.jar --spring.profiles.active=staging
```

## JVM 推荐参数

```bash
java -Xms512m -Xmx1024m \
     -XX:+UseG1GC \
     -jar AutoMind-service.jar \
     --spring.profiles.active=dev
```

## CI/CD

当前无自动化流水线（见 `docs/TECH_DEBT.md` #2）。

规划中的流水线阶段（GitHub Actions 或 Jenkins）：

1. **build**：`mvn clean install -DskipTests`
2. **test**：`mvn test`
3. **package**：`mvn package -DskipTests`
4. **deploy**：推送 JAR 或 Docker 镜像至目标环境

## 注意事项

- `application-dev.yaml` 含明文 API Key，**禁止提交至公开仓库**，生产环境改用环境变量或 Vault 注入。
- staging/prod 环境配置建立时，敏感配置项必须通过外部注入，不得硬编码。
