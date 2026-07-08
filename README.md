# Java-all-in-one

Spring Boot 4.0.7 + Spring Cloud 2025.1.2 + Spring Cloud Alibaba 2025.1.0.0 微服务项目

## 项目结构

| 模块 | 端口 | 说明 |
|------|------|------|
| gateway-service | 28888 | API网关，OAuth2 Resource Server |
| auth-service | 28081 | 认证服务，WebFlux应用 |
| user-service | 28082 | 用户管理服务 |
| device-service | 28084 | 设备管理服务 |
| admin-service | 28085 | 管理服务（代理层，调用user-service） |
| common-model | - | 公共模型（ApiResponse/PageResponse） |
| common-exception | - | 公共异常处理 |
| common-core | - | 公共核心（BaseEntity） |

## 前置条件

启动服务前，确保以下基础设施已运行：

1. **MySQL** - `127.0.0.1:3306` (用户名: root, 密码: root)
2. **Nacos** - `127.0.0.1:8848` (用户名: nacos, 密码: 1qaz!QAZ)
3. **Keycloak** - `http://ip:18080/` (用户名: admin, 密码: admin)

## 启动顺序

### 第1步：启动基础设施
```bash
# 确保MySQL、Nacos、Keycloak已启动
# Keycloak客户端配置：clientId=auth-service, secret=auth-service-secret
```

### 第2步：启动基础服务
```bash
# user-service（其他服务依赖）
cd user-service && mvn spring-boot:run

# device-service（独立服务）
cd device-service && mvn spring-boot:run
```

### 第3步：启动认证和管理服务
```bash
# auth-service（依赖Keycloak）
cd auth-service && mvn spring-boot:run

# admin-service（依赖user-service）
cd admin-service && mvn spring-boot:run
```

### 第4步：启动网关
```bash
# gateway-service（依赖所有服务）
cd gateway-service && mvn spring-boot:run
```

## 注意事项

### 端口配置
所有服务端口在原设计基础上+20000，避免与其他Docker容器冲突：
- gateway: 28888
- auth: 28081
- user: 28082
- device: 28084
- admin: 28085

### 配置导入
使用 `spring.config.import: nacos:<service>.yml` 导入Nacos配置，**不要**使用 `optional:nacos:` 前缀。

### 网关路由
使用编程式 `RouteLocator`（RouteConfig.java），因为YAML路由在Nacos配置合并时会被覆盖。

### 数据库
- user-service 使用 `user_db`
- device-service 使用 `device_db`
- 使用Flyway管理数据库迁移

### 服务依赖
- admin-service 通过 OpenFeign 调用 user-service
- gateway-service 通过 JWKS 验证Keycloak JWT
- auth-service 通过 WebClient 调用Keycloak获取token

## 常用命令

```shell
# 更新子模块版本
mvn -N versions:update-child-modules

# 编译整个项目
mvn compile

# 运行测试
mvn test
```
