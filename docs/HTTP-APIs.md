# HTTP 接口总览

## 模块概览

| 模块 | 服务名 | 基础路径 | 端口 | Swagger UI | OpenAPI JSON |
|------|--------|----------|------|------------|--------------|
| auth-service | 认证服务 | `/api/auth` | 28081 | [Swagger UI](http://127.0.0.1:28081/swagger-ui.html) | [JSON](http://127.0.0.1:28081/v3/api-docs) |
| admin-service | 管理服务 | `/api/admin` | 28085 | [Swagger UI](http://127.0.0.1:28085/swagger-ui.html) | [JSON](http://127.0.0.1:28085/v3/api-docs) |
| user-service | 用户服务 | `/api/users` | 28082 | [Swagger UI](http://127.0.0.1:28082/swagger-ui.html) | [JSON](http://127.0.0.1:28082/v3/api-docs) |
| device-service | 设备服务 | `/api/devices` | 28084 | [Swagger UI](http://127.0.0.1:28084/swagger-ui.html) | [JSON](http://127.0.0.1:28084/v3/api-docs) |
| gateway-service | 网关服务 | - | 28888 | - | - |

---

## 1. Auth Service (认证服务)

**基础路径:** `/api/auth`

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| POST | `/login` | 用户登录 | 邮箱+密码登录，返回JWT Token |
| POST | `/refresh` | 刷新Token | 使用refreshToken获取新accessToken |
| GET | `/health` | 健康检查 | 服务状态检查 |

---

## 2. Admin Service (管理服务)

**基础路径:** `/api/admin`

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/users` | 获取用户列表 | 分页查询，支持page/size参数 |
| GET | `/users/{id}` | 获取用户详情 | 根据ID查询单个用户 |
| POST | `/users` | 创建用户 | 创建新用户 |
| PUT | `/users/{id}` | 更新用户 | 更新指定用户信息 |
| DELETE | `/users/{id}` | 删除用户 | 删除指定用户 |
| GET | `/health` | 健康检查 | 服务状态检查 |

**备注:** 通过Feign调用user-service实现，用于管理后台

---

## 3. User Service (用户服务)

**基础路径:** `/api/users`

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| POST | `/` | 创建用户 | 创建新用户 |
| GET | `/{id}` | 获取用户详情 | 根据ID查询单个用户 |
| GET | `/` | 获取用户列表 | 分页查询，支持page/size参数 |
| PUT | `/{id}` | 更新用户 | 更新指定用户信息 |
| DELETE | `/{id}` | 删除用户 | 删除指定用户 |
| GET | `/health` | 健康检查 | 服务状态检查 |

---

## 4. Device Service (设备服务)

**基础路径:** `/api/devices`

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| POST | `/` | 创建设备 | 创建新设备 |
| GET | `/{id}` | 获取设备详情 | 根据ID查询单个设备 |
| GET | `/code/{deviceCode}` | 根据编码查询设备 | 使用设备编码查询 |
| GET | `/` | 获取设备列表 | 支持status/type筛选，分页查询 |
| PUT | `/{id}` | 更新设备 | 更新指定设备信息 |
| DELETE | `/{id}` | 删除设备 | 根据ID删除设备 |
| DELETE | `/code/{deviceCode}` | 根据编码删除设备 | 使用设备编码删除 |
| PATCH | `/{id}/status` | 更新设备状态 | 修改设备状态 |
| POST | `/{deviceCode}/heartbeat` | 设备心跳 | 设备上报心跳 |
| GET | `/health` | 健康检查 | 服务状态检查 |

---

## 5. Gateway Service (网关服务)

**端口:** 28888

网关服务无Controller，采用Spring Cloud Gateway路由配置：

- 统一入口，路由转发到各微服务
- 与Keycloak集成实现认证授权
- 路由规则见 `RouteConfig.java`

---

## 接口统计

| 模块 | 接口数量 |
|------|----------|
| auth-service | 3 |
| admin-service | 6 |
| user-service | 6 |
| device-service | 10 |
| **总计** | **25** |

---

## OpenAPI 文档 (Apifox 可直接导入)

### 访问地址

服务启动后，可通过以下地址访问 OpenAPI 文档：

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
