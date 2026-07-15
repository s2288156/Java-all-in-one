import time

import pytest
import requests


GATEWAY = "http://localhost:28888"
TEST_PASSWORD = "test123456"

# shared mutable state across test functions
_state = {}


@pytest.fixture(scope="session", autouse=True)
def _admin_token():
    resp = requests.post(
        f"{GATEWAY}/api/auth/login",
        json={"username": "admin", "password": "admin"},
    )
    assert resp.status_code == 200, f"Admin login failed: {resp.text}"
    _state["admin_token"] = resp.json()["token"]


@pytest.fixture(scope="session", autouse=True)
def _test_username():
    _state["test_username"] = f"testuser_{int(time.time())}"
