# E2E RBAC 权限测试

## 前置条件

1. 基础设施已启动（MySQL, Nacos, Keycloak）
2. 所有微服务已启动（user-service → auth-service → admin-service → gateway-service）
3. Keycloak 中存在 admin 用户（username=admin, password=admin）且已分配 ROLE_ADMIN

## 安装依赖

```bash
# 方式一：使用系统 Python（需要 pip）
pip install -r test/requirements.txt

# 方式二：使用 venv 虚拟环境（推荐）
python3 -m venv test/.venv
source test/.venv/bin/activate
pip install -r test/requirements.txt
```

## 运行测试

```bash
# 标准运行（显示每个测试的 PASS/FAIL）
python -m pytest test/test_e2e_rbac.py -v

# 显示 print 输出（推荐，可以看到详细步骤）
python -m pytest test/test_e2e_rbac.py -v -s

# 使用 venv 运行
source test/.venv/bin/activate
pytest test_e2e_rbac.py -v -s
```

## 测试流程

| # | 测试 | 验证点 |
|---|------|--------|
| 1 | 注册新用户 | 返回 200 + token |
| 2 | 新用户访问 admin 接口 | 403 Forbidden |
| 3 | admin/admin 登录 | 返回 200 + token |
| 4 | admin 查询用户列表 | 找到新用户的本地 ID |
| 5 | admin 分配 ROLE_ADMIN | 返回 200 |
| 6 | 新用户重新登录 | JWT 中包含 ROLE_ADMIN |
| 7 | 新用户访问 admin 接口 | 200 OK |
| 8 | admin 删除用户 | 返回 200 |
| 9 | 验证删除后登录 | 401 Unauthorized |

## 注意事项

- 测试依赖执行顺序（test_01 ~ test_09），不要单独运行某个测试
- 每次运行会自动创建和清理测试用户，不会影响已有数据
- `admin/admin` 账号需要预先在 Keycloak 中存在并分配 ROLE_ADMIN
- 测试通过网关（localhost:28888）调用所有接口
- 如果网络不通 Keycloak，测试会失败，确保 Keycloak 可访问

## 文件说明

- `test_e2e_rbac.py` — 9 个测试用例
- `requirements.txt` — Python 依赖
- `README.md` — 本文档
