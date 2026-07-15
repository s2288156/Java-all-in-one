#!/bin/bash
# Initialize Keycloak for the project: create client, assign admin roles, create admin user
# Usage: ./nacos/init-keycloak.sh [keycloak-addr]
#
# Compatible with Keycloak 26+ (Quarkus distribution)
# - Uses 'master-realm' client (replaces 'realm-management' in KC 26+)
# - Sets client secret via PUT on full client representation

set -euo pipefail

KC="${1:-http://127.0.0.1:8080}"
REALM="master"
CLIENT_ID="auth-service"
CLIENT_SECRET="auth-service-secret"
ADMIN_USER="admin"
ADMIN_PASS="admin"

info()  { echo -e "\033[32m[OK]\033[0m $*"; }
warn()  { echo -e "\033[33m[WARN]\033[0m $*"; }
fail()  { echo -e "\033[31m[FAIL]\033[0m $*"; exit 1; }

# ── 1. Wait for Keycloak ──────────────────────────────────────────────
echo "Waiting for Keycloak at $KC ..."
for i in $(seq 1 30); do
  if curl -sf "$KC/realms/$REALM" >/dev/null 2>&1; then
    info "Keycloak is ready"
    break
  fi
  [ "$i" -eq 30 ] && fail "Keycloak not reachable after 30 attempts"
  sleep 2
done

# ── 2. Get admin token ───────────────────────────────────────────────
get_admin_token() {
  curl -sf -X POST "$KC/realms/$REALM/protocol/openid-connect/token" \
    -d "grant_type=password&client_id=admin-cli&username=$ADMIN_USER&password=$ADMIN_PASS" \
    | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])"
}

TOKEN=$(get_admin_token)
info "Got admin token"

api() {
  local method="$1" path="$2" data="${3:-}"
  if [ -n "$data" ]; then
    curl -sf -X "$method" "$KC$path" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "$data"
  else
    curl -sf -X "$method" "$KC$path" \
      -H "Authorization: Bearer $TOKEN"
  fi
}

# ── 3. Create auth-service client ────────────────────────────────────
EXISTING=$(curl -sf "$KC/admin/realms/$REALM/clients?clientId=$CLIENT_ID" \
  -H "Authorization: Bearer $TOKEN")

if echo "$EXISTING" | python3 -c "import sys,json; exit(0 if json.load(sys.stdin) else 1)" 2>/dev/null; then
  CLIENT_UUID=$(echo "$EXISTING" | python3 -c "import sys,json; print(json.load(sys.stdin)[0]['id'])")
  warn "Client '$CLIENT_ID' already exists (id=$CLIENT_UUID), updating..."
else
  CLIENT_UUID=""
fi

CLIENT_JSON=$(python3 -c "
import json
print(json.dumps({
    'clientId': '$CLIENT_ID',
    'enabled': True,
    'protocol': 'openid-connect',
    'publicClient': False,
    'secret': '$CLIENT_SECRET',
    'directAccessGrantsEnabled': True,
    'serviceAccountsEnabled': True,
    'redirectUris': ['http://localhost:*'],
    'webOrigins': ['http://localhost:*'],
    'standardFlowEnabled': True,
    'implicitFlowEnabled': False
}))
")

if [ -n "$CLIENT_UUID" ]; then
  # Update existing client via PUT (this also sets the secret correctly)
  api PUT "/admin/realms/$REALM/clients/$CLIENT_UUID" "$CLIENT_JSON" >/dev/null
  info "Updated client '$CLIENT_ID'"
else
  # Create new client
  api POST "/admin/realms/$REALM/clients" "$CLIENT_JSON" >/dev/null
  CLIENT_UUID=$(curl -sf "$KC/admin/realms/$REALM/clients?clientId=$CLIENT_ID" \
    -H "Authorization: Bearer $TOKEN" \
    | python3 -c "import sys,json; print(json.load(sys.stdin)[0]['id'])")
  info "Created client '$CLIENT_ID' (id=$CLIENT_UUID)"
fi

# ── 4. Verify client secret ──────────────────────────────────────────
SECRET_VALUE=$(curl -sf "$KC/admin/realms/$REALM/clients/$CLIENT_UUID/client-secret" \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['value'])")

if [ "$SECRET_VALUE" = "$CLIENT_SECRET" ]; then
  info "Client secret verified: '$CLIENT_SECRET'"
else
  warn "Client secret mismatch, resetting..."
  # Reset via regenerate then update
  api POST "/admin/realms/$REALM/clients/$CLIENT_UUID/client-secret" >/dev/null 2>&1
  # Use PUT to set the correct secret
  SECRET_JSON=$(python3 -c "import json; print(json.dumps({'secret': '$CLIENT_SECRET'}))")
  api PUT "/admin/realms/$REALM/clients/$CLIENT_UUID" "$SECRET_JSON" >/dev/null
  info "Client secret reset to '$CLIENT_SECRET'"
fi

# ── 5. Get service account user ──────────────────────────────────────
SA_USER=$(api GET "/admin/realms/$REALM/clients/$CLIENT_UUID/service-account-user")
SA_USER_ID=$(echo "$SA_USER" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
info "Service account user id=$SA_USER_ID"

# ── 6. Find master-realm client (KC 26+ equivalent of realm-management) ──
# In Keycloak 26+, built-in admin client is 'master-realm' not 'realm-management'
MASTER_REALM_ID=$(curl -sf "$KC/admin/realms/$REALM/clients?clientId=master-realm" \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d[0]['id'] if d else '')")

if [ -z "$MASTER_REALM_ID" ]; then
  # Fallback: try realm-management (for older Keycloak versions)
  MASTER_REALM_ID=$(curl -sf "$KC/admin/realms/$REALM/clients?clientId=realm-management" \
    -H "Authorization: Bearer $TOKEN" \
    | python3 -c "import sys,json; d=json.load(sys.stdin); print(d[0]['id'] if d else '')")
  [ -z "$MASTER_REALM_ID" ] && fail "Neither 'master-realm' nor 'realm-management' client found"
  info "Using legacy 'realm-management' client (id=$MASTER_REALM_ID)"
else
  info "Using 'master-realm' client (id=$MASTER_REALM_ID)"
fi

# ── 7. Assign admin roles to service account ─────────────────────────
# Get all available roles from the admin client
AVAILABLE_ROLES=$(api GET "/admin/realms/$REALM/clients/$MASTER_REALM_ID/roles")

# Target roles for full admin access
# manage-users, view-users: user management
# manage-clients, view-clients: client and role management (KC 26+)
TARGET_ROLES='["manage-users","view-users","manage-clients","view-clients"]'

ROLES_TO_ASSIGN=$(python3 -c "
import json, sys
available = json.loads('''$AVAILABLE_ROLES''')
targets = json.loads('$TARGET_ROLES')
result = [r for r in available if r['name'] in targets]
print(json.dumps(result))
")

if [ "$ROLES_TO_ASSIGN" = "[]" ]; then
  warn "No matching admin roles found to assign"
else
  api POST "/admin/realms/$REALM/users/$SA_USER_ID/role-mappings/clients/$MASTER_REALM_ID" \
    "$ROLES_TO_ASSIGN" >/dev/null 2>&1
  ASSIGNED=$(echo "$ROLES_TO_ASSIGN" | python3 -c "import sys,json; print(', '.join(r['name'] for r in json.load(sys.stdin)))")
  info "Assigned roles to service account: $ASSIGNED"
fi

# ── 8. Assign ROLE_ADMIN to built-in admin user ──────────────────────
# The 'admin' user is the Keycloak built-in admin (created by KEYCLOAK_ADMIN env var)
APP_ADMIN_ID=$(curl -sf "$KC/admin/realms/$REALM/users?username=$ADMIN_USER" \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d[0]['id'] if d else '')" 2>/dev/null || echo "")

if [ -z "$APP_ADMIN_ID" ]; then
  warn "Built-in '$ADMIN_USER' user not found, skipping ROLE_ADMIN assignment"
else
  ROLE_ADMIN=$(api GET "/admin/realms/$REALM/roles/ROLE_ADMIN" 2>/dev/null || true)
  if [ -n "$ROLE_ADMIN" ] && echo "$ROLE_ADMIN" | python3 -c "import sys,json; json.load(sys.stdin)" 2>/dev/null; then
    api POST "/admin/realms/$REALM/users/$APP_ADMIN_ID/role-mappings/realm" \
      "[$ROLE_ADMIN]" >/dev/null 2>&1 || true
    info "Assigned ROLE_ADMIN to user '$ADMIN_USER'"
  else
    warn "ROLE_ADMIN not found yet (will be created by auth-service RoleInitializer on startup)"
  fi
fi

# ── 9. Verify: test client_credentials ───────────────────────────────
echo ""
echo "Verifying setup..."
SA_TOKEN=$(curl -sf -X POST "$KC/realms/$REALM/protocol/openid-connect/token" \
  -d "grant_type=client_credentials&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET" 2>/dev/null \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])" 2>/dev/null || echo "")

if [ -n "$SA_TOKEN" ]; then
  info "client_credentials grant works"
  ADMIN_API_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "$KC/admin/realms/$REALM/users?max=1" \
    -H "Authorization: Bearer $SA_TOKEN" 2>/dev/null || echo "000")
  if [ "$ADMIN_API_CODE" = "200" ]; then
    info "Service account can access admin API"
  else
    warn "Service account admin API returned HTTP $ADMIN_API_CODE"
  fi
else
  warn "client_credentials grant failed"
fi

ROPC_OK=$(curl -sf -X POST "$KC/realms/$REALM/protocol/openid-connect/token" \
  -d "grant_type=password&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&username=$ADMIN_USER&password=$ADMIN_PASS" 2>/dev/null \
  | python3 -c "import sys,json; d=json.load(sys.stdin); exit(0 if 'access_token' in d else 1)" 2>/dev/null && echo "yes" || echo "no")
if [ "$ROPC_OK" = "yes" ]; then
  info "ROPC login works"
else
  warn "ROPC login failed (admin user may not exist yet)"
fi

# ── 10. Summary ──────────────────────────────────────────────────────
echo ""
echo "========================================="
echo "  Keycloak Initialization Complete"
echo "========================================="
echo ""
echo "  Realm:            $REALM"
echo "  Client ID:        $CLIENT_ID"
echo "  Client Secret:    $CLIENT_SECRET"
echo "  Admin User:       $ADMIN_USER"
echo "  Admin Password:   $ADMIN_PASS"
echo ""
echo "  Keycloak Admin Console:"
echo "    $KC"
echo ""
echo "  Verify with:"
echo "    # ROPC login test"
echo "    curl -X POST '$KC/realms/$REALM/protocol/openid-connect/token' \\"
echo "      -d 'grant_type=password&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&username=$ADMIN_USER&password=$ADMIN_PASS'"
echo ""
echo "    # Client credentials test"
echo "    curl -X POST '$KC/realms/$REALM/protocol/openid-connect/token' \\"
echo "      -d 'grant_type=client_credentials&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET'"
echo "========================================="
