# Keycloak Configuration Guide

## Overview

This document describes the required Keycloak realm and client configuration for the auth-service to work with username-based authentication.

## Quick Setup (Recommended)

Use the one-click initialization script to configure everything automatically:

```bash
./nacos/init-keycloak.sh                    # default: http://127.0.0.1:8080
./nacos/init-keycloak.sh http://host:port   # custom address
```

The script is **idempotent** — safe to run multiple times. It detects existing resources and updates or skips as needed.

### What the Script Does (Step by Step)

| Step | Action | API Called |
|------|--------|-----------|
| 1 | Wait for Keycloak to be ready (polls `/realms/master`, up to 60s) | `GET /realms/{realm}` |
| 2 | Get admin access token via built-in `admin` user + `admin-cli` | `POST /realms/{realm}/protocol/openid-connect/token` |
| 3 | Create or update `auth-service` client (confidential, ROPC + service accounts enabled, secret=`auth-service-secret`) | `POST` or `PUT /admin/realms/{realm}/clients` |
| 4 | Verify client secret matches, reset if mismatched | `GET /admin/realms/{realm}/clients/{id}/client-secret` |
| 5 | Get service account user ID for `auth-service` | `GET /admin/realms/{realm}/clients/{id}/service-account-user` |
| 6 | Find admin client: `master-realm` (KC 26+) or `realm-management` (older) | `GET /admin/realms/{realm}/clients?clientId=...` |
| 7 | Assign admin roles to service account: `manage-users`, `view-users`, `manage-clients`, `view-clients` | `POST /admin/realms/{realm}/users/{id}/role-mappings/clients/{id}` |
| 8 | Assign `ROLE_ADMIN` to built-in `admin` user if role exists (otherwise created by `RoleInitializer` on auth-service startup) | `POST /admin/realms/{realm}/users/{id}/role-mappings/realm` |
| 9 | Verify: test `client_credentials` grant, test admin API access, test ROPC login | Multiple token + API calls |

## Manual Configuration

If you prefer to configure Keycloak manually via the Admin Console:

### Realm Settings

Navigate to **Realm Settings** → **Login** tab:

| Setting | Value | Description |
|---------|-------|-------------|
| Login with email | **Enabled** | Allows users to log in with email as well as username |
| Email as username | **Disabled** | Prevents Keycloak from forcing email=username |
| User registration | **Enabled** (if self-registration needed) | Allows public user registration |
| Verify email | **Disabled** (or configure SMTP) | Email verification requirement |

**Important:** The ROPC (Resource Owner Password Credentials) grant sends the `username` form parameter to Keycloak. By default, Keycloak resolves this against both the `username` field AND the `email` field. This means:

- If a user has username "john" and email "john@example.com", logging in with either "john" or "john@example.com" works
- No additional realm configuration is needed for basic username login via ROPC

### Client Settings

#### Create `auth-service` Client

1. Go to **Clients** → **Create client**
2. Client type: **OpenID Connect**
3. Client ID: `auth-service`
4. Click **Next**

#### Settings Tab

| Setting | Value |
|---------|-------|
| Client authentication | **On** (confidential) |
| Valid redirect URIs | `http://localhost:*` (for development) |
| Web origins | `http://localhost:*` |
| Direct access grants | **Enabled** (required for ROPC) |
| Service accounts | **Enabled** (required for admin operations) |

#### Credentials Tab

The client secret must be set to `auth-service-secret` to match the Nacos configuration:

```yaml
keycloak:
  client-secret: auth-service-secret
```

### Admin API Access

The auth-service needs admin access to Keycloak for user/role management. The `auth-service` client's service account must be assigned roles from the `master-realm` client (Keycloak 26+) or `realm-management` client (older versions).

**Required roles:**

| Role | Purpose |
|------|---------|
| `manage-users` | Create, update, delete users |
| `view-users` | List and search users |
| `manage-clients` | Manage client configurations |
| `view-clients` | View client configurations |

**Configuration in Nacos (`auth-service.yml`):**

```yaml
keycloak:
  admin-client-id: auth-service        # same client uses both ROPC and admin API
  admin-client-secret: auth-service-secret
```

The auth-service uses a two-step fallback for admin token acquisition:
1. First tries `client_credentials` grant (requires service accounts enabled + admin roles)
2. Falls back to `password` grant using `admin-username` / `admin-password`

## Configuration Reference

### Nacos Config (`auth-service.yml`)

| Property | Default Value | Description |
|----------|---------------|-------------|
| `keycloak.server-url` | `http://47.113.227.247:18080` | Keycloak server URL |
| `keycloak.realm` | `master` | Realm name |
| `keycloak.client-id` | `auth-service` | Client ID for ROPC login |
| `keycloak.client-secret` | `auth-service-secret` | Client secret |
| `keycloak.admin-client-id` | `auth-service` | Client ID for admin API (client_credentials) |
| `keycloak.admin-client-secret` | `auth-service-secret` | Client secret for admin API |
| `keycloak.admin-username` | `admin` | Fallback admin username (password grant) |
| `keycloak.admin-password` | `admin` | Fallback admin password |

### User Attributes

When a user registers via auth-service, the following fields are stored in Keycloak:

| Field | Keycloak Attribute | Notes |
|-------|-------------------|-------|
| username | `username` | Primary identifier, unique in realm |
| email | `email` | Optional |
| phone | `attributes.phone` | Optional, stored in attributes map |
| password | `credentials` | Stored as hashed credential |

### Roles

| Role | Description | Created By |
|------|-------------|------------|
| `ROLE_USER` | Normal user | `RoleInitializer` on auth-service startup |
| `ROLE_ADMIN` | Administrator | `RoleInitializer` on auth-service startup |

Roles are auto-created by `RoleInitializer` (a `CommandLineRunner` in auth-service) on startup. No manual role creation is needed.

## ROPC Grant Flow

The auth-service uses the ROPC (Resource Owner Password Credentials) grant for login:

```
POST /realms/{realm}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
client_id=auth-service
client_secret=auth-service-secret
username=<username-or-email>
password=<password>
```

**Response:**
```json
{
  "access_token": "eyJ...",
  "token_type": "Bearer",
  "expires_in": 300,
  "refresh_token": "eyJ..."
}
```

## Testing

### Direct Keycloak Tests

```bash
# ROPC login test
curl -X POST 'http://127.0.0.1:8080/realms/master/protocol/openid-connect/token' \
  -d 'grant_type=password&client_id=auth-service&client_secret=auth-service-secret&username=admin&password=admin'

# Client credentials test
curl -X POST 'http://127.0.0.1:8080/realms/master/protocol/openid-connect/token' \
  -d 'grant_type=client_credentials&client_id=auth-service&client_secret=auth-service-secret'
```

### Via Gateway

```bash
# Register a new user
curl -X POST http://localhost:28888/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "123456"}'

# Login with username
curl -X POST http://localhost:28888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "123456"}'

# Login with email (if email provided during registration)
curl -X POST http://localhost:28888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test@example.com", "password": "123456"}'
```

## Troubleshooting

### "Invalid user credentials" Error

1. Verify the user exists in Keycloak (check Keycloak Admin Console → Users)
2. Verify the username/email is correct
3. Check Keycloak logs for detailed error messages

### ROPC Grant Not Working

1. Ensure **Direct access grants** is enabled on the client
2. Ensure the client exists in the correct realm
3. Check client credentials (ID and secret) match the configuration

### Service Account Admin API Returns 403

1. Ensure **Service accounts** is enabled on the `auth-service` client
2. Ensure the service account has been assigned roles from `master-realm` (KC 26+) or `realm-management` (older versions)
3. Required roles: `manage-users`, `view-users`, `manage-clients`, `view-clients`

### Username Not Unique

Keycloak enforces username uniqueness within a realm by default. If you see a "User already exists" error, the username is already taken.

## References

- [Keycloak Admin API Documentation](https://www.keycloak.org/docs-api/latest/rest-api/)
- [Keycloak ROPC Grant](https://openid.net/specs/openid-connect-core-1_0.html#ResourceOwnerPasswordCredentialsGrant)
- Init script: `nacos/init-keycloak.sh`
