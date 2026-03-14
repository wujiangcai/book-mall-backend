# 前端开发执行计划（参考 implementation-plan.md）

> 依据 `docs/frontend-guideline.md` 与接口文档，采用单项目形态（前后台共存）。

## Phase 0：范围核对与契约锁定
**目标**：前后端需求与接口契约一致。
- 对照 `docs/毕设需求.md` 与 `docs/frontend-guideline.md` 的前台/后台/非功能清单。
- 对照 `docs/frontend-api.md` 与 `docs/admin-api.md` 的接口清单与字段。
- 明确统一响应格式与错误码（`docs/requirements.md`）。

**验证**：
- 前台/后台功能范围清晰；接口清单无缺漏。

---

## Phase 1：项目初始化与基础设施
**目标**：搭建单项目结构与基础能力。
- 初始化 Vite + Vue3 + TypeScript 项目结构（`frontend/`）。
- 建立目录规范：`views/front`、`views/admin`、`api/front`、`api/admin`、`router`、`store`、`utils`、`components`、`styles`。
- 创建占位页面并接入路由入口（前台/后台可编译）。
- 添加基础样式入口（`src/styles/index.css`）。
- 安装依赖并启动 dev server（默认端口 5174）。

**验证**：
- 前端工程可运行；前台/后台占位页面可访问。

---

## Phase 2：路由与鉴权机制
**目标**：实现前台/后台路由分区与权限控制。
- 建立前台路由骨架（`/`、`/books`、`/book/:id`、`/cart`、`/orders`、`/order/:id`、`/login`、`/register`、`/user`、`/address`）。
- 建立后台路由骨架（`/admin/login`、`/admin/dashboard`、`/admin/books`、`/admin/categories`、`/admin/orders`、`/admin/users`、`/admin/banners`）。
- 预留路由守卫入口（待后续接入 token/401/403 逻辑）。

**验证**：
- 前台/后台路由均可访问占位页面。


---

## Phase 3：API 封装与错误处理
**目标**：统一请求封装、错误处理与提示。
- 完成 Axios 实例与拦截器骨架（`src/api/request.ts`）。
- 统一 `code=200` 判定与错误提示（骨架）。
- 预留 401 处理入口（清 token/跳转登录）。
- 创建前台/后台 API 模块占位文件（`src/api/front/*`、`src/api/admin/*`）。

**验证**：
- 前端工程可编译；API 模块可被后续实现扩展。

---

## Phase 4：前台功能开发（商城）
**目标**：完成用户侧核心流程。
- 用户认证：注册/登录/用户中心。
- 图书浏览：列表/详情/分类/搜索。
- 购物车：增删改查。
- 地址管理：新增/编辑/删除/设默认。
- 订单流程：创建、支付、取消、列表/详情。

**验证**：
- 注册→登录→加购→下单→支付→订单列表流程贯通。

---

## Phase 5：后台功能开发（管理端）
**目标**：完成运营管理能力。
- 图书管理：列表/新增/编辑/上下架。
- 分类管理：列表/新增/编辑/启用/禁用。
- 订单管理：列表/详情/发货。
- 用户管理：列表/详情/状态更新。
- 轮播图管理：增删改查/排序。

**验证**：
- 后台关键管理流程可用；发货状态正确。

---

## Phase 6：联调与体验优化
**目标**：前后端联调与体验收敛。
- 统一错误提示与空状态展示。
- 关键页面的分页/筛选一致性。
- 完成前台/后台 UI 体验打磨。

**验证**：
- 前台与后台全流程稳定。

---

## Phase 7：验收与交付
**目标**：形成完整验收清单与可交付版本。
- 输出前台/后台功能验收清单。
- 进行演示流程走查与问题修复。

**验证**：
- 所有核心功能可演示，状态流转正确。

---

## 参考文档
- `docs/frontend-guideline.md`
- `docs/frontend-api.md`
- `docs/admin-api.md`
- `docs/requirements.md`
- `docs/毕设需求.md`
