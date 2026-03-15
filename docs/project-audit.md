# 项目功能与接口覆盖审计

> 生成时间：2026-03-15
> 环境：前端 http://localhost:5173 ，后端 http://localhost:8003
> 鉴权方式：请求头 `Authorization: <token>`（见 `frontend/src/api/request.ts`）

## 1. 概览
- 技术栈：Spring Boot + MyBatis（后端），Vue + Pinia + Arco Design（前端）
- 权限控制：`/api/front/**` 与 `/api/admin/**` 由拦截器控制（`src/main/java/top/wjc/bookmallbackend/config/WebMvcConfig.java`）
- 公开接口：
  - 前台：`/api/front/auth/**`、`/api/front/books/**`、`/api/front/categories`、`/api/front/banners`
  - 后台：`/api/admin/auth/**`

## 2. 前端功能完成度（按页面/模块）
### 前台（front）
- 首页：轮播、分类、推荐图书（数据驱动）
  - `frontend/src/views/front/Home.vue`
- 图书列表/搜索：关键词/分类筛选、分页（数据驱动）
  - `frontend/src/views/front/BookList.vue`
- 图书详情：详情展示、加入购物车（数据驱动）
  - `frontend/src/views/front/BookDetail.vue`
- 购物车：列表、勾选、数量、删除、结算下单（数据驱动）
  - `frontend/src/views/front/Cart.vue`
- 订单列表/详情：订单展示、支付/取消（数据驱动）；物流/售后为占位
  - `frontend/src/views/front/OrderList.vue`
  - `frontend/src/views/front/OrderDetail.vue`
- 登录/注册：登录注册完整（社交登录按钮为 UI 占位）
  - `frontend/src/views/front/Login.vue`
  - `frontend/src/views/front/Register.vue`
- 用户中心：资料展示与编辑、地址入口、订单入口（数据驱动）
  - `frontend/src/views/front/UserCenter.vue`
- 地址管理：列表/新增/编辑/删除/设默认（数据驱动）
  - `frontend/src/views/front/AddressManage.vue`

### 后台（admin）
- 管理端登录（数据驱动）
  - `frontend/src/views/admin/AdminLogin.vue`
- 仪表盘：统计卡片为静态展示（无 API）
  - `frontend/src/views/admin/Dashboard.vue`
- 图书管理：增删改查、状态切换、搜索（数据驱动）
  - `frontend/src/views/admin/BookManage.vue`
- 分类管理：增删改查、状态切换（数据驱动）
  - `frontend/src/views/admin/CategoryManage.vue`
- 订单管理：列表/详情/发货（数据驱动）；退款/物流面板为占位
  - `frontend/src/views/admin/OrderManage.vue`
- 用户管理：列表/状态切换（数据驱动）；角色面板为占位
  - `frontend/src/views/admin/UserManage.vue`
- 轮播管理：增删改查、排序（数据驱动）；优惠券/公告为占位
  - `frontend/src/views/admin/BannerManage.vue`
- 设置/日志：静态占位
  - `frontend/src/views/admin/Settings.vue`
  - `frontend/src/views/admin/Logs.vue`

## 3. 后端接口清单（按模块/权限）
> 见 `src/main/java/top/wjc/bookmallbackend/controller/**`

### 前台（/api/front）
- 认证：
  - POST `/api/front/auth/register`
  - POST `/api/front/auth/login`
- 用户：
  - GET `/api/front/user/info`
  - PUT `/api/front/user/info`
- 地址：
  - GET `/api/front/user/addresses`
  - POST `/api/front/user/addresses`
  - PUT `/api/front/user/addresses/{id}`
  - DELETE `/api/front/user/addresses/{id}`
  - PUT `/api/front/user/addresses/{id}/default`
- 图书：
  - GET `/api/front/books`
  - GET `/api/front/books/{id}`
- 分类：
  - GET `/api/front/categories`
- 购物车：
  - GET `/api/front/cart`
  - POST `/api/front/cart`
  - PUT `/api/front/cart/{id}`
  - DELETE `/api/front/cart/{id}`
- 订单：
  - POST `/api/front/orders`
  - POST `/api/front/orders/{id}/pay`
  - POST `/api/front/orders/{id}/cancel`
  - GET `/api/front/orders`
  - GET `/api/front/orders/{id}`
- 轮播：
  - GET `/api/front/banners`

### 后台（/api/admin）
- 认证：
  - POST `/api/admin/auth/login`
- 图书：
  - GET `/api/admin/books`
  - POST `/api/admin/books`
  - PUT `/api/admin/books/{id}`
  - DELETE `/api/admin/books/{id}`
  - PUT `/api/admin/books/{id}/status`
- 分类：
  - GET `/api/admin/categories`
  - POST `/api/admin/categories`
  - PUT `/api/admin/categories/{id}`
  - DELETE `/api/admin/categories/{id}`
  - PUT `/api/admin/categories/{id}/status`
- 订单：
  - GET `/api/admin/orders`
  - GET `/api/admin/orders/{id}`
  - PUT `/api/admin/orders/{id}/ship`
- 用户：
  - GET `/api/admin/users`
  - GET `/api/admin/users/{id}`
  - PUT `/api/admin/users/{id}/status`
- 轮播：
  - GET `/api/admin/banners`
  - POST `/api/admin/banners`
  - PUT `/api/admin/banners/{id}`
  - DELETE `/api/admin/banners/{id}`
  - PUT `/api/admin/banners/{id}/sort`

## 4. 接口使用对齐矩阵
### 4.1 后端已实现且前端已使用（核心功能已覆盖）
- 前台：auth、user、address、book、category、cart、order、banner 全部已被页面调用
- 后台：auth、book、category、order、user（list/status）、banner 均已被页面调用

### 4.2 后端已实现但前端未使用（潜在遗漏）
- **GET `/api/admin/users/{id}`**
  - 前端 API 模块存在，但无页面调用（`frontend/src/api/admin/user.ts`）

### 4.3 前端调用但后端缺失
- 未发现（基于 API 模块与控制器映射）

## 5. 运行验证结果（抽样）
### 登录
- 前台登录：`POST /api/front/auth/login` → 200（token 获取成功）
- 后台登录：`POST /api/admin/auth/login` → 200（token 获取成功）

### 前台公开接口
- `GET /api/front/categories` → 200
- `GET /api/front/books` → 200
- `GET /api/front/banners` → 200

### 前台需登录接口
- `GET /api/front/cart` → 200
- `GET /api/front/orders` → 200
- `GET /api/front/user/addresses` → 200

### 后台需登录接口
- `GET /api/admin/books` → 200
- `GET /api/admin/categories` → 200
- `GET /api/admin/users` → 200
- `GET /api/admin/orders` → 200
- `GET /api/admin/banners` → 200

## 6. 待完善项与修复建议（按优先级）
### P1（功能缺口/体验影响明显）
- 后台/前台存在多个“占位面板/按钮”，需决定是否落地：
  - 前台：订单物流、售后入口（`OrderList.vue`, `OrderDetail.vue`）
  - 后台：退款、物流面板（`OrderManage.vue`）
  - 后台：用户角色面板（`UserManage.vue`）
  - 后台：优惠券/公告（`BannerManage.vue`）
  - 后台：仪表盘统计、设置、日志（`Dashboard.vue`, `Settings.vue`, `Logs.vue`）

### P2（接口/页面覆盖完善）
- 后台用户详情接口 `/api/admin/users/{id}` 未被任何页面使用，可补充详情入口或移除冗余 API。

### P3（配置一致性）
- `application.properties` 中 CORS `allowed-origins` 仍包含旧端口（3000/8081），已由 `WebMvcConfig` 覆盖为 5173，建议统一以免误导后续开发。

## 6.1 占位功能可落地性评估（基于现有后端接口）
> 结论：仅“发货动作”可部分落地，其余需新增后端接口支持。

### 可落地（现有接口足够或基本满足）
- **后台：订单发货动作**（`OrderManage.vue`）
  - 现有接口：PUT `/api/admin/orders/{id}/ship`
  - 说明：可支持“发货”按钮/动作，但缺少物流明细查询/维护。

### 不可落地（后端接口缺失）
- **前台：订单物流入口**（`OrderList.vue`, `OrderDetail.vue`）
  - 缺失接口：物流信息查询/轨迹接口
- **前台：售后入口**（`OrderList.vue`, `OrderDetail.vue`）
  - 缺失接口：售后申请/售后记录接口
- **后台：退款面板**（`OrderManage.vue`）
  - 缺失接口：退款申请/退款处理接口
- **后台：物流面板**（`OrderManage.vue`）
  - 缺失接口：物流单号维护/物流轨迹查询接口
- **后台：用户角色面板**（`UserManage.vue`）
  - 缺失接口：角色查询/角色变更接口
- **后台：优惠券/公告**（`BannerManage.vue`）
  - 缺失接口：coupon/notice 相关 CRUD 接口
- **后台：仪表盘统计**（`Dashboard.vue`）
  - 缺失接口：订单/用户/销售额统计接口
- **后台：设置/日志**（`Settings.vue`, `Logs.vue`）
  - 缺失接口：系统配置读取/更新、日志查询接口

## 7. 附录
### 前端 API 模块路径
- `frontend/src/api/front/*.ts`
- `frontend/src/api/admin/*.ts`

### 前端路由与页面
- `frontend/src/router/front.ts`
- `frontend/src/router/admin.ts`
- `frontend/src/views/front/**`
- `frontend/src/views/admin/**`

## 8. 占位入口清理记录
- 清理时间：2026-03-15
- 说明：仅移除入口（菜单/路由），页面文件保留。
- 已移除路由（`frontend/src/router/admin.ts`）：
  - `/admin/dashboard`
  - `/admin/books/stock`
  - `/admin/orders/refund`
  - `/admin/orders/logistics`
  - `/admin/users/roles`
  - `/admin/banners/coupon`
  - `/admin/banners/notice`
  - `/admin/settings`
  - `/admin/logs`
- 已移除菜单入口（`frontend/src/views/admin/AdminLayout.vue`）：
  - 仪表盘
  - 库存管理
  - 退款处理
  - 物流跟踪
  - 权限管理
  - 优惠券
  - 公告管理
  - 系统设置（基础配置/操作日志）
