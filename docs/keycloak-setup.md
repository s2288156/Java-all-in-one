# Keycloak Configuration Guide

## Overview

This document describes the required Keycloak realm and client configuration for the auth-service to work with username-based authentication.

## Realm Settings

### Login Tab

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
| Client authentication | **Off** (public client) or **On** (confidential) |
| Valid redirect URIs | `http://localhost:*` (for development) |
| Web origins | `http://localhost:*` |
| Direct access grants | **Enabled** (required for ROPC) |
| Service accounts | **Enabled** (required for admin operations) |

#### Credentials Tab (if confidential client)

Copy the **Client Secret** and configure it in `auth-service`:
```yaml
keycloak:
  client-secret: <your-client-secret>
```

### Admin Client

The auth-service needs admin access to Keycloak for user/role management. Two options:

#### Option 1: Client Credentials (Recommended)

1. Create a client `auth-service-admin` with **Service accounts enabled**
2. Assign roles: `realm-management` → `realm-admin` (or specific roles like `manage-users`, `view-users`)
3. Configure in `auth-service`:
```yaml
keycloak:
  admin-client-id: auth-service-admin
  admin-client-secret: <admin-client-secret>
```

#### Option 2: Password Grant (Fallback)

Use the built-in `admin` user or a custom admin user:
```yaml
keycloak:
  admin-client-id: admin-cli
  admin-username: admin
  admin-password: <admin-password>
```

## User Attributes

When a user registers via auth-service, the following fields are stored in Keycloak:

| Field | Keycloak Attribute | Notes |
|-------|-------------------|-------|
| username | `username` | Primary identifier, unique in realm |
| email | `email` | Optional |
| phone | `phoneNumber` | Optional, stored in UserRepresentation |
| password | `credentials` | Stored as hashed credential |

## ROPC Grant Flow

The auth-service uses the ROPC (Resource Owner Password Credentials) grant for login:

```
POST /realms/{realm}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
client_id=auth-service
client_secret=<secret>
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

### Test Username Login

```bash
# Register a new user
curl -X POST http://localhost:28888/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "123456"}'

# Login with username
curl -X POST http://localhost:28888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "123456"}'
```

### Test Email Login (if email provided during registration)

```bash
# Register with email
curl -X POST http://localhost:28888/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser2", "password": "123456", "email": "test@example.com"}'

# Login with email (Keycloak resolves against email field)
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

### Username Not Unique

Keycloak enforces username uniqueness within a realm by default. If you see a "User already exists" error, the username is already taken.

## References

- [Keycloak Admin API Documentation](https://www.keycloak.org/docs-api/latest/rest-api/)
- [Keycloak ROPC Grant](https://openid.net/specs/openid-connect-core-1_0.html#ResourceOwnerPasswordCredentialsGrant)
