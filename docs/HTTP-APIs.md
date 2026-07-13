# HTTP 接口总览

## 模块概览

| 模块 | 服务名 | 基础路径 | 端口 | 职责 | Swagger UI | OpenAPI JSON |
|------|--------|----------|------|------|------------|--------------|
| auth-service | 认证服务 | `/api/auth` | 28081 | Identity Service（认证 + Keycloak 用户/角色管理） | [Swagger UI](http://127.0.0.1:28081/swagger-ui.html) | [JSON](http://127.0.0.1:28081/v3/api-docs) |
| admin-service | 管理服务 | `/api/admin` | 28085 | BFF for Admin UI（聚合 auth-service + user-service） | [Swagger UI](http://127.0.0.1:28085/swagger-ui.html) | [JSON](http://127.0.0.1:28085/v3/api-docs) |
| user-service | 用户服务 | `/api/users` | 28082 | Business Data Service（仅内部接口，不暴露给前端） | [Swagger UI](http://127.0.0.1:28082/swagger-ui.html) | [JSON](http://127.0.0.1:28082/v3/api-docs) |
| device-service | 设备服务 | `/api/devices` | 28084 | 设备管理 | [Swagger UI](http://127.0.0.1:28084/swagger-ui.html) | [JSON](http://127.0.0.1:28084/v3/api-docs) |
| gateway-service | 网关服务 | - | 28888 | 统一入口 + JWT 验证 | - | - |

---

## 1. Auth Service (认证服务 / Identity Service)

**基础路径:** `/api/auth`
**职责:** 认证（登录/注册/登出/Token管理）+ Keycloak 用户管理 + 角色管理

### 认证接口

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| POST | `/login` | 用户登录 | 用户名+密码登录，返回 JWT Token |
| POST | `/register` | 用户注册 | 创建 Keycloak 用户 + 同步到 user-service，返回 JWT Token |
| POST | `/refresh` | 刷新 Token | 使用 refreshToken 获取新 accessToken |
| POST | `/logout` | 用户登出 | 撤销用户 Keycloak 会话 |

### 管理员 - Keycloak 用户管理

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/admin/users` | 列出 Keycloak 用户 | 分页查询（first/max 参数） |
| GET | `/admin/users/{id}` | 获取 Keycloak 用户 | 按 Keycloak UUID 查询 |
| PUT | `/admin/users/{id}` | 更新 Keycloak 用户 | 更新 username/email |
| DELETE | `/admin/users/{id}` | 删除 Keycloak 用户 | 从 Keycloak 删除用户 |
| POST | `/admin/users/{id}/reset-password` | 重置密码 | 管理员重置用户密码 |

### 管理员 - 角色管理

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/admin/roles` | 列出所有角色 | 获取 realm 角色列表 |
| POST | `/admin/roles` | 创建角色 | 创建新的 realm 角色 |
| DELETE | `/admin/roles/{name}` | 删除角色 | 删除 realm 角色 |
| GET | `/admin/users/{id}/roles` | 获取用户角色 | 获取用户已分配的角色 |
| POST | `/admin/users/{id}/roles` | 分配角色 | 为用户分配角色 |
| DELETE | `/admin/users/{id}/roles` | 移除角色 | 移除用户的角色 |

### 健康检查

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/health` | 健康检查 |

---

## 2. Admin Service (管理服务 / BFF)

**基础路径:** `/api/admin`
**职责:** 管理后台的统一入口，聚合 auth-service 和 user-service

**前端只需调用此服务即可完成所有管理操作。**

### 用户管理（聚合两个服务）

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/users` | 用户列表 | 从 user-service 获取本地用户数据 |
| GET | `/users/{id}` | 用户详情 | 从 user-service 获取本地用户数据 |
| POST | `/users` | 创建用户 | 调用 auth-service 注册（自动同步到 user-service） |
| PUT | `/users/{id}` | 更新用户 | 同时更新 auth-service (Keycloak) 和 user-service (MySQL) |
| DELETE | `/users/{id}` | 删除用户 | 同时删除 auth-service (Keycloak) 和 user-service (MySQL) |

### 密码管理

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| POST | `/users/{id}/reset-password` | 重置密码 | 通过 auth-service 重置 Keycloak 密码 |

### 角色管理（代理到 auth-service）

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/roles` | 角色列表 | 获取所有 realm 角色 |
| POST | `/roles` | 创建角色 | 创建新的 realm 角色 |
| DELETE | `/roles/{name}` | 删除角色 | 删除 realm 角色 |
| GET | `/users/{id}/roles` | 用户角色 | 获取用户已分配的角色 |
| POST | `/users/{id}/roles` | 分配角色 | 为用户分配角色 |
| DELETE | `/users/{id}/roles` | 移除角色 | 移除用户的角色 |

### 健康检查

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/health` | 健康检查 |

---

## 3. User Service (用户服务 / 内部接口)

**基础路径:** `/api/users`
**职责:** 业务用户数据管理（仅内部服务间调用，不暴露给前端）

> **注意:** 此服务的接口仅供 auth-service 和 admin-service 内部调用，前端不应直接访问。

### 内部同步接口

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| POST | `/internal` | 同步用户 | auth-service 注册时调用，创建本地用户记录 |
| GET | `/internal/keycloak/{keycloakId}` | 按 Keycloak ID 查询 | 内部查询接口 |
| DELETE | `/internal/keycloak/{keycloakId}` | 按 Keycloak ID 删除 | 内部删除接口 |

### 内部管理接口（供 admin-service 调用）

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/internal` | 用户列表 | 分页查询本地用户 |
| GET | `/internal/{id}` | 用户详情 | 按本地 ID 查询 |
| PUT | `/internal/{id}` | 更新用户 | 更新本地用户数据 |
| DELETE | `/internal/{id}` | 删除用户 | 删除本地用户记录 |

### 健康检查

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/health` | 健康检查 |

---

## 4. Device Service (设备服务)

**基础路径:** `/api/devices`

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| POST | `/` | 创建设备 | 创建新设备 |
| GET | `/{id}` | 获取设备详情 | 根据 ID 查询单个设备 |
| GET | `/code/{deviceCode}` | 根据编码查询设备 | 使用设备编码查询 |
| GET | `/` | 获取设备列表 | 支持 status/type 筛选，分页查询 |
| PUT | `/{id}` | 更新设备 | 更新指定设备信息 |
| DELETE | `/{id}` | 删除设备 | 根据 ID 删除设备 |
| DELETE | `/code/{deviceCode}` | 根据编码删除设备 | 使用设备编码删除 |
| PATCH | `/{id}/status` | 更新设备状态 | 修改设备状态 |
| POST | `/{deviceCode}/heartbeat` | 设备心跳 | 设备上报心跳 |
| GET | `/health` | 健康检查 | 服务状态检查 |

---

## 5. Gateway Service (网关服务)

**端口:** 28888

网关服务无 Controller，采用 Spring Cloud Gateway 路由配置：

- 统一入口，路由转发到各微服务
- 与 Keycloak 集成实现认证授权
- JWT Claim 转发（X-User-Id, X-User-Email, X-User-Roles, X-Username）
- 路由规则见 `RouteConfig.java`

### 路由规则

| 路径前缀 | 目标服务 | 说明 |
|----------|----------|------|
| `/api/auth/**` | auth-service | 认证服务 |
| `/api/users/**` | user-service | 用户服务（内部接口） |
| `/api/admin/**` | admin-service | 管理服务（BFF） |
| `/api/devices/**` | device-service | 设备服务 |

---

## 架构图

```
管理后台前端 / 用户前端
    │
    ▼
Gateway (28888)
    │ JWT验证 + 角色路由
    │
    ├─── /api/auth/** ────→ auth-service (28081)
    │                          │
    │                          ├── Keycloak (认证/用户/角色)
    │                          │
    │                          └──→ user-service (内部同步)
    │
    ├─── /api/admin/** ───→ admin-service (28085) [BFF]
    │                          │
    │                          ├──→ auth-service (Feign)
    │                          └──→ user-service (Feign)
    │
    ├─── /api/users/** ───→ user-service (28082) [内部接口]
    │
    └─── /api/devices/** ─→ device-service (28084)
```

---

## 接口统计

| 模块 | 接口数量 | 说明 |
|------|----------|------|
| auth-service | 16 | 认证 + Keycloak 用户管理 + 角色管理 |
| admin-service | 13 | BFF 聚合层 |
| user-service | 8 | 内部接口（不暴露给前端） |
| device-service | 10 | 设备管理 |
| **总计** | **47** | |

---

## OpenAPI 文档 (Apifox 可直接导入)

### 访问地址

| 服务 | Swagger UI | OpenAPI JSON |
|------|------------|--------------|
| auth-service | http://127.0.0.1:28081/swagger-ui.html | http://127.0.0.1:28081/v3/api-docs |
| user-service | http://127.0.0.1:28082/swagger-ui.html | http://127.0.0.1:28082/v3/api-docs |
| device-service | http://127.0.0.1:28084/swagger-ui.html | http://127.0.0.1:28084/v3/api-docs |
| admin-service | http://127.0.0.1:28085/swagger-ui.html | http://127.0.0.1:28085/v3/api-docs |

### 导出到 Apifox

**方式一：使用导出脚本**

```bash
# 启动服务后执行
./docs/export-openapi.sh
```

导出文件位于 `docs/openapi/` 目录下。

**方式二：手动导入**

1. 启动对应服务
2. 浏览器访问 `http://127.0.0.1:{port}/v3/api-docs`，复制 JSON 内容
3. 打开 Apifox -> 项目设置 -> 导入数据
4. 选择 OpenAPI/Swagger 格式，粘贴或上传 JSON 文件

### 代码即文档

OpenAPI 文档从代码自动生成，保持同步。如需增强文档质量：

```java
@Operation(summary = "用户登录", description = "使用邮箱和密码登录，返回JWT Token")
@ApiResponse(responseCode = "200", description = "登录成功")
@PostMapping("/login")
public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
    // ...
}
```
