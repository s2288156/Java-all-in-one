import base64
import json

import requests

from conftest import GATEWAY, TEST_PASSWORD, _state


class TestE2ERBAC:

    def test_01_register_user(self):
        username = _state["test_username"]
        resp = requests.post(
            f"{GATEWAY}/api/auth/register",
            json={"username": username, "password": TEST_PASSWORD},
        )
        assert resp.status_code == 200, f"Register failed: {resp.status_code} {resp.text}"
        data = resp.json()
        assert "token" in data, f"No token in response: {data}"
        _state["user_token"] = data["token"]
        print(f"\n  [PASS] 注册成功，username={username}")

    def test_02_new_user_cannot_access_admin(self):
        resp = requests.get(
            f"{GATEWAY}/api/admin/roles",
            headers={"Authorization": f"Bearer {_state['user_token']}"},
        )
        assert resp.status_code in (401, 403), f"Expected 401/403, got {resp.status_code}"
        print(f"\n  [PASS] 返回 {resp.status_code}，权限正确拒绝")

    def test_03_admin_login(self):
        assert "admin_token" in _state, "admin_token fixture did not run"
        print(f"\n  [PASS] admin 登录成功 (fixture)")

    def test_04_admin_lists_users(self):
        resp = requests.get(
            f"{GATEWAY}/api/admin/users",
            params={"page": 0, "size": 100},
            headers={"Authorization": f"Bearer {_state['admin_token']}"},
        )
        assert resp.status_code == 200, f"List users failed: {resp.status_code} {resp.text}"
        users = resp.json()["data"]["content"]
        target = next(
            (u for u in users if u["username"] == _state["test_username"]),
            None,
        )
        assert target is not None, (
            f"User {_state['test_username']} not found. "
            f"Available: {[u['username'] for u in users]}"
        )
        _state["user_id"] = target["id"]
        print(f"\n  [PASS] 找到 {_state['test_username']}，本地 ID={_state['user_id']}")

    def test_05_admin_assigns_role(self):
        resp = requests.post(
            f"{GATEWAY}/api/admin/users/{_state['user_id']}/roles",
            json={"roleNames": ["ROLE_ADMIN"]},
            headers={"Authorization": f"Bearer {_state['admin_token']}"},
        )
        assert resp.status_code == 200, f"Assign role failed: {resp.status_code} {resp.text}"
        print(f"\n  [PASS] ROLE_ADMIN 分配成功")

    def test_06_user_relogin_with_new_role(self):
        resp = requests.post(
            f"{GATEWAY}/api/auth/login",
            json={"username": _state["test_username"], "password": TEST_PASSWORD},
        )
        assert resp.status_code == 200, f"Re-login failed: {resp.status_code} {resp.text}"
        token = resp.json()["token"]
        payload = token.split(".")[1]
        payload += "=" * (4 - len(payload) % 4)
        claims = json.loads(base64.b64decode(payload))
        roles = claims.get("realm_access", {}).get("roles", [])
        assert "ROLE_ADMIN" in roles, f"ROLE_ADMIN not in token: {roles}"
        _state["new_admin_token"] = token
        print(f"\n  [PASS] 重新登录成功，token 含 ROLE_ADMIN")

    def test_07_user_can_access_admin(self):
        resp = requests.get(
            f"{GATEWAY}/api/admin/roles",
            headers={"Authorization": f"Bearer {_state['new_admin_token']}"},
        )
        assert resp.status_code == 200, f"Access admin failed: {resp.status_code} {resp.text}"
        print(f"\n  [PASS] admin 接口访问成功，权限生效")

    def test_08_admin_deletes_user(self):
        resp = requests.delete(
            f"{GATEWAY}/api/admin/users/{_state['user_id']}",
            headers={"Authorization": f"Bearer {_state['admin_token']}"},
        )
        assert resp.status_code == 200, f"Delete user failed: {resp.status_code} {resp.text}"
        print(f"\n  [PASS] 用户 {_state['test_username']} 删除成功")

    def test_09_deleted_user_cannot_login(self):
        resp = requests.post(
            f"{GATEWAY}/api/auth/login",
            json={"username": _state["test_username"], "password": TEST_PASSWORD},
        )
        assert resp.status_code in (400, 401, 500), (
            f"Expected login failure, got {resp.status_code}"
        )
        print(f"\n  [PASS] 用户已删除，登录失败（HTTP {resp.status_code}）")
