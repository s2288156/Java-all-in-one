# 升级 Spring Boot、Spring Cloud 和 Nacos 版本 - 验证检查清单

## 版本配置验证
- [x] 父 pom.xml 中 Spring Cloud 版本为 2025.1.1
- [x] 父 pom.xml 中 Spring Cloud Alibaba 版本为 2025.1.0.0
- [x] 父 pom.xml 包含 spring-cloud-alibaba-dependencies 依赖管理

## 服务依赖验证
- [ ] user-service/pom.xml 包含 spring-cloud-starter-nacos-discovery
- [ ] user-service/pom.xml 包含 spring-cloud-starter-nacos-config
- [ ] auth-service/pom.xml 包含 spring-cloud-starter-nacos-discovery
- [ ] auth-service/pom.xml 包含 spring-cloud-starter-nacos-config
- [ ] gateway-service/pom.xml 包含 spring-cloud-starter-nacos-discovery
- [ ] gateway-service/pom.xml 包含 spring-cloud-starter-nacos-config
- [ ] admin-service/pom.xml 包含 spring-cloud-starter-nacos-discovery
- [ ] admin-service/pom.xml 包含 spring-cloud-starter-nacos-config

## 服务启动验证
- [x] user-service 启动成功，无错误日志
- [ ] auth-service 启动成功，无错误日志
- [ ] gateway-service 启动成功，无错误日志
- [ ] admin-service 启动成功，无错误日志

## Nacos 注册验证
- [x] user-service 成功注册到 Nacos
- [ ] auth-service 成功注册到 Nacos
- [ ] gateway-service 成功注册到 Nacos
- [ ] admin-service 成功注册到 Nacos
