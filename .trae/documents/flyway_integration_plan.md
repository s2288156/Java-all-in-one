# Flyway 数据库初始化集成方案

## 1. 需求分析

当前项目使用 Spring JPA 的 `ddl-auto: update` 进行数据库表管理，存在以下问题：
- 数据库Schema变更缺乏版本控制
- 无法追踪数据库变更历史
- 团队协作时容易出现Schema不一致

集成 Flyway 后可实现：
- 数据库Schema版本化管理
- 自动化数据库迁移
- 支持回滚和审计

## 2. 实施计划

### 2.1 修改文件清单

| 文件路径 | 修改类型 | 说明 |
|---------|---------|------|
| `user-service/pom.xml` | 添加依赖 | 引入 Flyway Core 依赖 |
| `user-service/src/main/resources/bootstrap.yml` | 修改配置 | 配置 Flyway，禁用 JPA ddl-auto |
| `user-service/src/main/resources/db/migration/V1__Create_users_table.sql` | 新建文件 | 创建初始用户表迁移脚本 |

### 2.2 实施步骤

#### 步骤1：添加 Flyway 依赖

在 `user-service/pom.xml` 中添加 Flyway 依赖：

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

#### 步骤2：创建数据库迁移脚本

创建目录 `src/main/resources/db/migration/`，并创建初始迁移脚本 `V1__Create_users_table.sql`：

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 步骤3：配置 Flyway

修改 `bootstrap.yml`：
- 设置 `spring.jpa.hibernate.ddl-auto=none`（禁用自动DDL）
- 添加 Flyway 配置

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

## 3. 依赖与版本

| 依赖 | GroupId | ArtifactId | 版本 |
|------|---------|-----------|------|
| Flyway Core | org.flywaydb | flyway-core | 由 Spring Boot 父POM管理 |

## 4. 风险与注意事项

### 4.1 风险评估

| 风险 | 等级 | 描述 | 缓解措施 |
|------|------|------|----------|
| 现有数据丢失 | 高 | 首次运行 Flyway 可能影响现有数据 | 执行前备份数据库 |
| Schema 冲突 | 中 | 迁移脚本与现有表结构冲突 | 确认数据库为空或已清理 |

### 4.2 注意事项

1. **首次执行**：确保数据库为空或已清理，避免表冲突
2. **脚本命名规范**：严格遵循 Flyway 命名规则 `V{版本}__{描述}.sql`
3. **备份数据**：执行迁移前建议备份数据库
4. **禁用 ddl-auto**：必须设置为 `none`，避免与 Flyway 冲突

## 5. 验证方式

启动 `user-service` 后，检查：
1. 数据库自动创建 `flyway_schema_history` 表
2. `users` 表按迁移脚本创建
3. 查看日志确认迁移成功：`Successfully applied 1 migration`

## 6. 后续扩展

- 添加更多迁移脚本时，按版本递增命名（如 `V2__Add_column.sql`）
- 可配置 Flyway 回滚策略
- 集成到 CI/CD 流程自动执行迁移