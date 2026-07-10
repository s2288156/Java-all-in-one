# AGENTS.md

## Project Overview

Spring Boot 4.0.7 + Spring Cloud 2025.1.2 + Spring Cloud Alibaba 2025.1.0.0 microservices project. Java 21, Maven multi-module.

## Service Ports

| Service | Port | Type |
|---------|------|------|
| gateway-service | 28888 | WebFlux (Spring Cloud Gateway) |
| auth-service | 28081 | WebMVC |
| user-service | 28082 | WebMVC + JPA |
| device-service | 28084 | WebMVC + JPA |
| admin-service | 28085 | WebMVC (proxy layer) |
| common-lib | - | Library (not runnable) |
| demo-module | - | Standalone (MQTT/Modbus demos) |

## Build & Run

```bash
# Compile entire project
mvn compile

# Run a single service
cd user-service && mvn spring-boot:run

# Update child module versions
mvn -N versions:update-child-modules
```

No test suite exists yet. `mvn test` will pass vacuously.

## Infrastructure Prerequisites

Services won't start without MySQL, Nacos, and Keycloak running. See `.mimocode/instructions/infra.md` for connection details (credentials are stored locally, not in git).

## Startup Order

1. Infrastructure (MySQL, Nacos, Keycloak)
2. `user-service` then `device-service` (base services)
3. `auth-service` then `admin-service` (depend on above)
4. `gateway-service` (depends on all)

## Critical Conventions

### Auth Architecture (Keycloak)

- **auth-service** is the **single gateway** for all Keycloak operations (token, user management, role management)
- Token endpoints: `POST /api/auth/login`, `POST /api/auth/logout`, `POST /api/auth/register`, `POST /api/auth/refresh`
- User endpoints: `/api/auth/users/*` (admin CRUD via Keycloak Admin API)
- Role endpoints: `/api/auth/roles/*` (admin CRUD via Keycloak Admin API)
- Uses Keycloak ROPC grant for login and token refresh
- auth-service must include `admin-client-id` and `admin-client-secret` in config (via `application.yml` or Nacos)

### Gateway JWT Claim Forwarding

Gateway (`JwtClaimForwardFilter`) extracts JWT claims and forwards as HTTP headers: `X-User-Id`, `X-User-Email`, `X-User-Roles` (comma-separated), `X-Username`. Use `UserContext` (from `common-lib`) to parse these in downstream services.

### User-Service Keycloak Integration

user-service is a **supplementary business data store only** — Keycloak is the primary user store.
- `keycloak_id` column links local users to Keycloak identities
- `InternalUserController` provides endpoints for auth-service to sync users

### Nacos Config Import

Use `spring.config.import: nacos:<service>.yml`. Do NOT use `optional:nacos:` prefix — it breaks config resolution.

### Gateway Routes

Routes are defined programmatically in `RouteConfig.java` via `RouteLocator`, not in YAML. YAML routes get overwritten during Nacos config merge.

### Database Migrations

Flyway manages schema. Migration files live at `<service>/src/main/resources/db/migration/V*__<name>.sql`. Use `baseline-on-migrate: true`.

### Database Conventions

- Column names: `created_time` / `updated_time` (not `created_at` / `updated_at`) across ALL tables
- Follow Alibaba manual: `idx_` prefix for indexes, `uk_` for unique constraints
- Use `utf8mb4` charset for all tables

### Service Communication

- `admin-service` → `user-service` via OpenFeign (`UserServiceClient`)
- `gateway-service` → all services via Nacos discovery + `lb://` URIs, validates JWT via Keycloak JWKS
- `auth-service` → Keycloak via `RestTemplate` for token operations

### common-lib

Shared library providing `ApiResponse`, `PageResponse`, `GlobalExceptionHandler`, `BusinessException`, `UserContext`. Services depend on it via Maven coordinates `org.all:common-lib`.

### demo-module

Standalone module for MQTT and Modbus TCP demos. Not part of the Spring Cloud ecosystem. Has its own `mainClass` config.

## Development Standards (Alibaba Java Manual)

All code must follow [Alibaba Java Development Manual](https://github.com/alibaba/p3c). Key rules for this project:

### Naming

- Class names: UpperCamelCase (`UserService`, `DeviceController`)
- Method names: lowerCamelCase (`getUserById`, `createDevice`)
- Constants: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)
- Package names: lowercase (`org.all.user.service`)
- Boolean fields: prefixed with `is`/`has`/`can` (`isActive`, `hasPermission`)

### Code Style

- One class per file; inner classes only when truly private
- No wildcard imports (`import java.util.*` is forbidden)
- Override annotations required (`@Override`)
- `@Deprecated` annotation + Javadoc required for deprecated methods
- Methods should not exceed 80 lines; classes should not exceed 500 lines

### Exception Handling

- Catch specific exceptions, never `catch (Exception e)`
- Do not swallow exceptions silently; log or rethrow
- Use `BusinessException` (from `common-lib`) for business errors
- Return `ApiResponse` with proper error codes, never expose stack traces

### Logging

- Use SLF4J (`private static final Logger log = LoggerFactory.getLogger(Xxx.class)`)
- Use `log.error("msg", e)` with exception as last param (never `log.error(e.getMessage())`)
- Do not print to `System.out` / `System.err`
- Log level: `ERROR` for system failures, `WARN` for recoverable issues, `INFO` for business events, `DEBUG` for diagnostics

### Database Design

- Table/column names: lowercase + underscores (`user_profile`, `created_time`)
- Primary key: `id BIGINT AUTO_INCREMENT`
- Required columns: `created_time DATETIME NOT NULL`, `updated_time DATETIME NOT NULL`
- Indexes: prefix `idx_` for single column, `uk_` for unique
- `VARCHAR` length must be explicitly declared; do not use `TEXT` for small fields
- Do NOT use reserved keywords as column names (`order`, `status`, `key`)
- Use `utf8mb4` charset for all tables
- Foreign keys: use application-level joins, NOT database-level foreign keys

## Prohibited Actions

- Do NOT run `git commit` unless the user's message literally contains "commit" or "提交"
- Do NOT run `git push` or `git add .` or `git add -A` without explicit user request
- Do NOT run `git add` on files the user did not specifically mention
- Do NOT modify `pom.xml` dependency versions without user approval
- Do NOT run `mvn deploy` or any publish-to-remote commands
- Do NOT delete or rewrite git history

## API Documentation

Swagger UI available at `http://127.0.0.1:{port}/swagger-ui.html` for each service. OpenAPI JSON at `/v3/api-docs`.

Full HTTP API reference: `docs/HTTP-APIs.md`
