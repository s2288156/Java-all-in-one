# Keycloak 认证授权集成方案（第一期）

## 一、需求分析

根据业务需求，第一期仅需实现 **用户名+密码** 认证方式，使用 OAuth2 的 **Resource Owner Password Credentials** 授权模式。

## 二、项目现状

### 2.1 现有模块结构
| 模块 | 职责 | 当前状态 |
|------|------|---------|
| gateway-service | API 网关 | 简单 AuthFilter |
| auth-service | 认证服务 | 自研 JJWT |
| user-service | 用户服务 | 无认证 |
| admin-service | 管理服务 | 无认证 |

## 三、集成方案

### 3.1 架构设计
```
客户端 → Gateway(Token验证) → auth-service(调用Keycloak获取Token) → Keycloak Server
                              ↓
                         后端业务服务
```

### 3.2 集成位置

| 模块 | 集成内容 | 说明 |
|------|---------|------|
| **auth-service** | 调用 Keycloak Token 端点获取 Token | 核心集成点 |
| **gateway-service** | 验证 JWT Token | 统一入口验证 |

### 3.3 认证流程
1. 客户端调用 `/api/auth/login`，传入 username + password
2. auth-service 调用 Keycloak 的 Token 端点获取 JWT
3. auth-service 返回 Token 给客户端
4. 客户端携带 Token 访问其他接口
5. Gateway 验证 Token 有效性

## 四、具体实现步骤

### 4.1 修改 auth-service

**4.1.1 添加依赖 (pom.xml)**
```xml
<!-- Spring WebClient -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Spring Security Crypto -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

**4.1.2 配置文件 (application.yml)**
```yaml
keycloak:
  server-url: http://keycloak-host:8080
  realm: your-realm
  client-id: your-client-id
  client-secret: your-client-secret
  token-uri: ${keycloak.server-url}/realms/${keycloak.realm}/protocol/openid-connect/token
```

**4.1.3 创建 Keycloak 配置类**
- `KeycloakProperties.java` - 配置属性类
- `KeycloakClient.java` - Keycloak API 客户端

**4.1.4 修改 AuthController**
- `/api/auth/login` - 调用 Keycloak 获取 Token
- `/api/auth/refresh` - 刷新 Token

**4.1.5 清理旧代码**
- 删除 `JwtUtil.java`、`JwtConfig.java`

### 4.2 修改 gateway-service

**4.2.1 添加依赖 (pom.xml)**
```xml
<!-- Spring Security OAuth2 Resource Server -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

**4.2.2 配置文件 (application.yml)**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${keycloak.server-url}/realms/${keycloak.realm}
          jwk-set-uri: ${keycloak.server-url}/realms/${keycloak.realm}/protocol/openid-connect/certs

keycloak:
  server-url: http://keycloak-host:8080
  realm: your-realm
```

**4.2.3 创建 SecurityConfig.java**
- 配置 JWT 验证
- 设置白名单（登录接口等）

**4.2.4 替换 AuthFilter**
- 删除旧的 `AuthFilter.java`

## 五、Keycloak Server 配置

### 5.1 Realm 设置
1. 创建 Realm：`your-realm`
2. 创建 Client：`your-client-id`
3. 启用 `Direct Access Grants Enabled`（密码模式）

### 5.2 用户创建
- 在 Realm 中创建测试用户
- 设置用户名和密码

## 六、API 接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/auth/login` | POST | 用户名密码登录 |
| `/api/auth/refresh` | POST | 刷新 Token |
| `/api/auth/logout` | POST | 登出 |

## 七、测试验证

| 测试场景 | 步骤 |
|---------|------|
| 登录获取 Token | POST /api/auth/login {username, password} |
| 访问受保护接口 | 携带 Authorization: Bearer {token} |
| 无效 Token | 使用过期/伪造 Token 访问 |

## 八、注意事项

1. Keycloak 服务需提前部署并配置完成
2. Client 需启用密码授权模式
3. 建议使用环境变量配置敏感信息