# 升级 Spring Boot、Spring Cloud 和 Nacos 版本 - 实施计划

## [ ] Task 1: 更新父 pom.xml 版本配置
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 更新 Spring Boot 版本到 4.0.6（已为最新）
  - 更新 Spring Cloud 版本到 2025.1.1
  - 添加 Spring Cloud Alibaba 版本 2025.1.0.0
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-1.1: 检查父 pom.xml 中 spring-cloud.version 为 2025.1.1
  - `programmatic` TR-1.2: 检查父 pom.xml 中 spring-cloud-alibaba.version 为 2025.1.0.0

## [ ] Task 2: 更新父 pom.xml 依赖管理
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 添加 Spring Cloud Alibaba 依赖管理
  - 移除原生 Nacos Client 依赖（由 Spring Cloud Alibaba 管理）
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-2.1: 检查父 pom.xml 包含 spring-cloud-alibaba-dependencies
  - `programmatic` TR-2.2: 检查父 pom.xml 不包含独立的 nacos-client 依赖

## [ ] Task 3: 更新 user-service pom.xml
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 添加 spring-cloud-starter-nacos-discovery 依赖
  - 添加 spring-cloud-starter-nacos-config 依赖
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-3.1: 检查 user-service/pom.xml 包含 nacos-discovery 依赖
  - `programmatic` TR-3.2: 检查 user-service/pom.xml 包含 nacos-config 依赖

## [ ] Task 4: 更新 auth-service pom.xml
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 添加 spring-cloud-starter-nacos-discovery 依赖
  - 添加 spring-cloud-starter-nacos-config 依赖
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-4.1: 检查 auth-service/pom.xml 包含 nacos-discovery 依赖
  - `programmatic` TR-4.2: 检查 auth-service/pom.xml 包含 nacos-config 依赖

## [ ] Task 5: 更新 gateway-service pom.xml
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 添加 spring-cloud-starter-nacos-discovery 依赖
  - 添加 spring-cloud-starter-nacos-config 依赖
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-5.1: 检查 gateway-service/pom.xml 包含 nacos-discovery 依赖
  - `programmatic` TR-5.2: 检查 gateway-service/pom.xml 包含 nacos-config 依赖

## [ ] Task 6: 更新 admin-service pom.xml
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 添加 spring-cloud-starter-nacos-discovery 依赖
  - 添加 spring-cloud-starter-nacos-config 依赖
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-6.1: 检查 admin-service/pom.xml 包含 nacos-discovery 依赖
  - `programmatic` TR-6.2: 检查 admin-service/pom.xml 包含 nacos-config 依赖

## [ ] Task 7: 验证 user-service 启动
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 启动 user-service
  - 检查是否有启动错误
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-7.1: 启动命令执行后返回码为 0（成功）
  - `human-judgment` TR-7.2: 检查启动日志中无 ERROR 级别日志

## [ ] Task 8: 验证服务注册到 Nacos
- **Priority**: P0
- **Depends On**: Task 7
- **Description**: 
  - 检查 Nacos 控制台确认服务注册状态
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `human-judgment` TR-8.1: Nacos 控制台服务列表中显示 user-service
  - `human-judgment` TR-8.2: 服务健康状态为 UP
