
# Nacos 客户端迁移计划

## 一、需求分析

用户要求将项目中的 Spring Cloud Alibaba Nacos 依赖替换为直接使用 Nacos Client 原生依赖：

| 修改项 | 当前状态 | 目标状态 |
|--------|----------|----------|
| Nacos 依赖方式 | spring-cloud-starter-alibaba-nacos-* | nacos-client |
| Nacos 版本 | 2023.0.1.0 | 3.2.0 |

## 二、修改内容

### 1. 父 pom.xml 修改

**移除**以下依赖管理：
- `spring-cloud-starter-alibaba-nacos-discovery`
- `spring-cloud-starter-alibaba-nacos-config`

**添加**以下依赖管理：
```xml
<dependency>
    <groupId>com.alibaba.nacos</groupId>
    <artifactId>nacos-client</artifactId>
    <version>3.2.0</version>
</dependency>
```

### 2. 子模块 pom.xml 修改

需要修改以下子模块：
- gateway-service/pom.xml
- auth-service/pom.xml
- user-service/pom.xml
- admin-service/pom.xml

**移除**：
- spring-cloud-starter-alibaba-nacos-discovery
- spring-cloud-starter-alibaba-nacos-config

**添加**：
```xml
<dependency>
    <groupId>com.alibaba.nacos</groupId>
    <artifactId>nacos-client</artifactId>
</dependency>
```

### 3. 配置文件修改

由于不再使用 Spring Cloud Alibaba 自动配置，需要手动配置 Nacos 客户端：
- 更新 bootstrap.yml 或 application.yml
- 添加 Nacos 服务发现和配置的手动配置类

## 三、风险评估

| 风险 | 描述 | 缓解措施 |
|------|------|----------|
| 配置方式变更 | Spring Cloud 自动配置不再生效 | 需要手动编写配置类 |
| API 兼容性 | Nacos Client 3.x API 可能与 2.x 不同 | 参考官方文档进行适配 |
| 服务注册发现 | 需要手动实现服务注册发现逻辑 | 使用 Nacos Client 原生 API |

## 四、执行步骤

1. 修改父 pom.xml，更新依赖管理
2. 修改各子模块 pom.xml，替换依赖
3. 创建 Nacos 配置类
4. 更新配置文件
5. 编译验证

---

**注意**: 此变更将移除 Spring Cloud Alibaba 的自动配置能力，需要手动实现 Nacos 客户端的配置和服务发现功能。
