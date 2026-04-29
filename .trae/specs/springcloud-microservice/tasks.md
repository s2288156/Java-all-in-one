# Spring Boot + Spring Cloud 微服务架构 - 实现计划

## [x] Task 1: 更新父pom.xml配置Spring Cloud依赖
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 添加Spring Cloud依赖管理
  - 添加Nacos相关依赖
  - 添加Spring Cloud Gateway依赖
- **Acceptance Criteria Addressed**: AC-1, AC-2
- **Test Requirements**:
  - `programmatic` TR-1.1: Maven依赖解析成功，无编译错误
- **Notes**: 使用Spring Cloud 2023.0.x版本，与Spring Boot 3.5.0兼容

## [ ] Task 2: 创建Nacos配置中心配置
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 在Nacos中创建命名空间
  - 创建各服务的配置文件（application.yml）
  - 配置数据库连接信息等
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `human-judgment` TR-2.1: Nacos控制台可查看配置文件
  - `human-judgment` TR-2.2: 配置格式正确，包含必要的数据库连接信息

## [x] Task 3: 创建网关服务(gateway-service)
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 创建Spring Boot模块gateway-service
  - 配置Nacos服务发现
  - 配置路由规则
  - 添加认证过滤器
- **Acceptance Criteria Addressed**: AC-1, AC-3
- **Test Requirements**:
  - `programmatic` TR-3.1: 服务启动成功并注册到Nacos
  - `programmatic` TR-3.2: 网关路由配置正确，请求能正确转发
- **Notes**: 使用Spring Cloud Gateway

## [x] Task 4: 创建认证服务(auth-service)
- **Priority**: P0
- **Depends On**: Task 1, Task 2
- **Description**: 
  - 创建Spring Boot模块auth-service
  - 配置Nacos服务发现和配置
  - 实现JWT令牌生成接口
  - 实现JWT令牌验证逻辑
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-5
- **Test Requirements**:
  - `programmatic` TR-4.1: 服务启动成功并注册到Nacos
  - `programmatic` TR-4.2: POST /api/auth/login 返回JWT令牌
  - `programmatic` TR-4.3: JWT令牌验证接口正常工作
- **Notes**: 使用JJWT库实现JWT

## [x] Task 5: 创建用户服务(user-service)
- **Priority**: P0
- **Depends On**: Task 1, Task 2
- **Description**: 
  - 创建Spring Boot模块user-service
  - 配置Nacos服务发现和配置
  - 创建用户实体和Repository
  - 实现用户CRUD REST API
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-4
- **Test Requirements**:
  - `programmatic` TR-5.1: 服务启动成功并注册到Nacos
  - `programmatic` TR-5.2: POST /api/users 创建用户成功
  - `programmatic` TR-5.3: GET /api/users/{id} 获取用户成功
  - `programmatic` TR-5.4: PUT /api/users/{id} 更新用户成功
  - `programmatic` TR-5.5: DELETE /api/users/{id} 删除用户成功
- **Notes**: 使用Spring Data JPA

## [x] Task 6: 创建后台管理服务(admin-service)
- **Priority**: P1
- **Depends On**: Task 1, Task 2
- **Description**: 
  - 创建Spring Boot模块admin-service
  - 配置Nacos服务发现和配置
  - 实现管理员相关API
  - 集成Feign调用用户服务
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-6
- **Test Requirements**:
  - `programmatic` TR-6.1: 服务启动成功并注册到Nacos
  - `programmatic` TR-6.2: 后台管理API正常响应
  - `programmatic` TR-6.3: Feign调用用户服务成功
- **Notes**: 使用OpenFeign进行服务间调用

## [x] Task 7: 集成测试验证
- **Priority**: P1
- **Depends On**: Task 3, Task 4, Task 5, Task 6
- **Description**: 
  - 启动所有服务进行集成测试
  - 验证服务间调用正常
  - 验证网关路由和认证过滤
- **Acceptance Criteria Addressed**: 所有AC
- **Test Requirements**:
  - `programmatic` TR-7.1: 所有服务成功注册到Nacos
  - `programmatic` TR-7.2: 通过网关访问各服务API正常
  - `programmatic` TR-7.3: 认证流程完整验证通过