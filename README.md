"# book-mall-backend

## 前端接口与集成指南（概览）

> 面向前端开发的接口说明（基于当前代码与 docs 对齐整理）。

### 1) 概览

- **Base URL**: `http://localhost:8003`
- **Swagger**:
  - OpenAPI JSON: `http://localhost:8003/api-docs`
  - UI: `http://localhost:8003/swagger-ui.html`
- **统一响应格式**: `top/wjc/bookmallbackend/common/Result.java:10`

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1710000000000
}
```

- **分页响应**: `top/wjc/bookmallbackend/common/PageResult.java:9`

```json
{
  "total": 100,
  "list": [],
  "page": 1,
  "size": 20
}
```

### 2) 认证与权限

- **JWT**: 通过 `Authorization` Header 传递（见 `docs/design.md`）。
- **前台拦截范围**: `/api/front/**`，以下路径**无需登录**（`config/WebMvcConfig.java:23`）
  - `/api/front/auth/**`
  - `/api/front/books/**`
  - `/api/front/categories`
  - `/api/front/banners`
- **后台拦截范围**: `/api/admin/**`，以下路径**无需登录**
  - `/api/admin/auth/**`

### 3) CORS

- 配置来源：`src/main/java/top/wjc/bookmallbackend/config/SecurityConfig.java:20`
- 允许来源：`cors.allowed-origins`（`src/main/resources/application.properties:25`）
  - 默认：`http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000,http://localhost:8081`
- 允许方法：GET/POST/PUT/DELETE/PATCH
- 允许 Header：`Content-Type`, `Authorization`
- 允许携带凭证：`true`

### 4) 错误码与统一异常

- 统一异常：`exception/GlobalExceptionHandler.java:16`
- HTTP 状态码：400/401/403/404/500
- 业务错误码（需求文档）见 `docs/requirements.md`：
  - `1001`：库存不足
  - `1002`：订单状态异常

### 5) 枚举与状态值

- **用户角色**（`constant/UserRole.java`）
  - 0: USER（普通用户）
  - 1: ADMIN（管理员）
- **通用启用状态**（`constant/CommonStatus.java`）
  - 0: DISABLED
  - 1: ENABLED
- **图书状态**（`constant/BookStatus.java`）
  - 0: OFF_SHELF（下架）
  - 1: ON_SHELF（上架）
  - 软删除：`status = -1`（见 `docs/phase3-alignment.md`）
- **订单状态**（`constant/OrderStatus.java`）
  - 0: UNPAID（待支付）
  - 1: PAID（已支付，瞬时）
  - 2: PENDING_SHIP（待发货）
  - 3: SHIPPED（已发货）
  - 4: COMPLETED（已完成）
  - 5: CANCELLED（已取消）
- **轮播图状态**（数据库/需求文档）
  - 0: 禁用
  - 1: 启用

### 6) 接口清单文档

- 前台接口：`docs/frontend-api.md`
- 后台接口：`docs/admin-api.md`
- 服务器部署与支付联调：`docs/deployment-payment-test.md`

### 7) docs 目录索引

- `docs/毕设需求.md`：项目背景与前后端需求范围
- `docs/design.md`：总体设计与接口设计说明
- `docs/requirements.md`：需求与错误码约定
- `docs/database-guide.md`：数据库结构/字段/枚举
- `docs/phase3-alignment.md`：图书/分类对齐
- `docs/phase4-alignment.md`：购物车对齐
- `docs/phase5-alignment.md`：地址对齐
- `docs/phase6-alignment.md`：订单前台流程对齐
- `docs/phase7-alignment.md`：后台订单/用户/轮播图对齐
- `docs/phase8-alignment.md`：非功能质量对齐
- `docs/frontend-api.md`：前台接口清单
- `docs/admin-api.md`：后台接口清单
- `docs/production-deployment.md`：生产环境上线部署说明
- `docs/frontend-guideline.md`：前端开发规范（前台+后台）
- `docs/frontend-implementation-plan.md`：前端开发执行计划

---

## 7) 关键流程（前端集成）

1. **注册/登录** → 获取 `token`
2. **浏览图书** → 选择加入购物车
3. **维护地址** → 选择/设置默认地址
4. **创建订单**（用 `cartIds`）→ 返回订单明细
5. **支付订单** → 订单状态进入待发货
6. **后台发货** → 订单状态变为已发货

---

## 8) 常见说明

- 前台图书/分类等公共接口无需登录。
- 需要登录的前台接口必须在 Header 中携带 `Authorization`。
- 软删除（`status = -1`）在查询端已过滤，详情/列表不会返回被删除数据。
" 
