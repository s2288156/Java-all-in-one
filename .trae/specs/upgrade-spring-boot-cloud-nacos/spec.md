# 升级 Spring Boot、Spring Cloud 和 Nacos 版本 - 产品需求文档

## Overview
- **Summary**: 将项目升级到最新版本的 Spring Boot 4.x、Spring Cloud 2025.x 和 Spring Cloud Alibaba 2025.x，以获得更好的性能、安全性和新特性支持。
- **Purpose**: 解决当前版本过旧的问题，提升系统稳定性和可维护性，使用最新技术栈。
- **Target Users**: 开发团队、运维团队

## Goals
- 将 Spring Boot 升级到 4.0.x 最新版本
- 将 Spring Cloud 升级到 2025.x 最新版本
- 将 Spring Cloud Alibaba 升级到 2025.x 最新版本
- 确保服务能够正常注册到 Nacos
- 确保所有服务能够正常启动和运行

## Non-Goals (Out of Scope)
- 不修改业务逻辑代码
- 不添加新功能
- 不修改数据库结构

## Background & Context
- 当前项目使用 Spring Boot 4.0.6、Spring Cloud 2023.0.3 和原生 Nacos Client 3.2.0
- 需要使用 Spring Cloud Alibaba 2025.x 来实现服务注册与发现
- Spring Cloud Alibaba 2025.x 已支持 Spring Boot 4.x 和 Java 21+

## Functional Requirements
- **FR-1**: 升级父 pom.xml 中的版本依赖管理
- **FR-2**: 更新所有服务的 pom.xml 中的 Nacos 相关依赖
- **FR-3**: 确保配置文件正确配置，支持新的配置方式
- **FR-4**: 确保服务能够正常注册到 Nacos

## Non-Functional Requirements
- **NFR-1**: 升级后所有服务启动时间不超过 30 秒
- **NFR-2**: 服务注册到 Nacos 的延迟不超过 10 秒
- **NFR-3**: 保持与现有业务逻辑的兼容性

## Constraints
- **Technical**: 需要使用 Java 21，确保 JDK 版本兼容
- **Dependencies**: 需要与现有数据库、Nacos Server 保持兼容

## Assumptions
- Nacos Server 已安装并运行在 192.168.75.128:8848
- MySQL 数据库已运行并配置正确
- 网络连接正常

## Acceptance Criteria

### AC-1: 版本升级完成
- **Given**: 当前项目使用旧版本依赖
- **When**: 升级所有版本依赖后
- **Then**: 父 pom.xml 和所有子模块 pom.xml 应包含正确的新版本号
- **Verification**: `programmatic`

### AC-2: 服务能够启动
- **Given**: 所有版本依赖已升级
- **When**: 启动 user-service
- **Then**: 服务应成功启动且无错误日志
- **Verification**: `programmatic`

### AC-3: 服务注册到 Nacos
- **Given**: user-service 已启动
- **When**: 检查 Nacos 控制台
- **Then**: user-service 应显示在 Nacos 服务列表中
- **Verification**: `human-judgment`

## Open Questions
- [ ] 需要确认 Nacos Server 版本是否与客户端版本兼容
