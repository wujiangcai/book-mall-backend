# 实施计划（小步可验证）

## Phase 0：基线核对与范围锁定
**目标**：确保需求、API 设计、数据库结构一致。
- 对照 `docs/requirements.md` 与 `docs/design.md` 的接口列表、状态流转、校验规则。
- 对照 `src/main/resources/sql/schema.sql` 的字段、索引与状态枚举。
- 明确本期开发范围（后端全部模块按规范顺序）。

**验证**：
- 核对订单状态流转与字段（`order.status/pay_time/ship_time`）。
- 核对索引要求（book/order/cart/user）。

## Phase 0.5：数据库初始化与种子数据
**目标**：先完成数据库建表与必要的基础数据，确保后续开发可直接联调。
- 使用 `src/main/resources/sql/schema.sql` 初始化数据库结构。
- 按需求准备最小可用种子数据（如管理员账号、基础分类、示例图书、轮播图）。

**验证**：
- 核查表结构与索引已创建。
- 登录/查询接口能读取到种子数据。

**完成记录（2026-03-14）**：
- 已删除并重建 `book_mall` 数据库。
- 以 `utf8mb4` 字符集成功导入 `schema.sql`。
- 表结构已创建：user/category/book/cart/address/order/order_item/banner。
- 修正种子用户昵称乱码：admin=系统管理员，testuser=测试用户。

## Phase 1：基础架构与通用能力
**目标**：全局响应、异常、跨域、JWT 与拦截器基础。
- `common`：统一响应 `Result`/`PageResult`
- `exception`：全局异常处理 + 业务异常体系
- `config`：CORS 配置
- `util`/`interceptor`：JWT 工具 + 登录/权限拦截器

**验证**：
- 响应格式包含 code/message/data/timestamp
- 业务异常/校验异常响应符合规范
- CORS 允许配置域名和请求头
- JWT 生成/解析与过期校验正常

**完成记录（2026-03-14）**：
- 新增通用响应：`common/Result`、`common/PageResult`。
- 新增异常体系：`BusinessException`、库存不足/订单状态异常/下架图书/401/403/404 等。
- 新增全局异常处理：`exception/GlobalExceptionHandler`。
- 新增 JWT 工具：`util/JwtUtil`。
- 新增拦截器：`interceptor/UserAuthInterceptor`、`interceptor/AdminAuthInterceptor`。
- 新增配置：`config/CorsConfig`、`config/WebMvcConfig`、`config/SecurityConfig`。
- 更新配置：`application.properties`（DB/JWT/CORS/日志/Swagger）。
- 更新依赖：`pom.xml`（MyBatis/JWT/Validation/Security/MySQL）。

## Phase 2：用户认证与个人信息
**目标**：用户注册/登录、管理员登录、个人信息维护。
- 实体/Mapper：`User`
- Service：注册（BCrypt + 唯一性 + 手机号校验）、登录（JWT）
- Controller：
  - 前台：`/api/front/auth/register`、`/api/front/auth/login`
  - 后台：`/api/admin/auth/login`
  - 用户中心：`/api/front/user/info`（GET/PUT）

**验证**：
- 注册唯一性与手机号校验
- 登录返回 JWT，禁用用户不可登录
- 返回数据不含 password

**完成记录（2026-03-14）**：
- 新增实体：`entity/User`。
- 新增 DTO：`RegisterRequest`、`LoginRequest`、`UpdateUserRequest`。
- 新增 VO：`AuthResponse`、`UserInfoResponse`。
- 新增 Mapper：`mapper/UserMapper`（按字段查询/插入/更新）。
- 新增 Service：`service/UserService` + `service/impl/UserServiceImpl`。
- 新增 Controller：`FrontAuthController`、`AdminAuthController`、`UserController`。
- 已更新 admin/testuser 密码哈希并完成登录验证。

## Phase 3：分类与图书管理（前台+后台）
**目标**：分类树、图书 CRUD、上下架、前台检索。
- 实体/Mapper：`Category`、`Book`
- 后台：分类管理、图书管理（含启用/禁用分类接口）
- 前台：图书列表（分页+搜索+分类）、图书详情
- 软删除：status = -1，API 层统一过滤

**对齐记录**：
- 详细对齐清单见 `docs/phase3-alignment.md`

**完成记录（2026-03-14）**：
- 新增实体：`entity/Category`、`entity/Book`。
- 新增 DTO：`BookCreateRequest`、`BookUpdateRequest`、`BookStatusRequest`、`CategoryCreateRequest`、`CategoryUpdateRequest`、`CategoryStatusRequest`。
- 新增 VO：`BookListItemVO`、`BookDetailVO`、`BookAdminListItemVO`、`CategoryTreeVO`、`CategoryAdminVO`。
- 新增 Mapper：`BookMapper`、`CategoryMapper` 与对应 XML（包含前后台列表、详情、统计与软删除过滤）。
- 新增 Service：`BookService` / `CategoryService` 及实现类（分页、校验、树构建、软删除、状态更新）。
- 新增 Controller：`FrontBookController`、`FrontCategoryController`、`AdminBookController`、`AdminCategoryController`。
- 状态策略：`status = -1` 软删除，API 层统一过滤；分类启用/禁用接口已落地。
- ISBN 重复校验：新增时提示确认；更新时仅在变更 ISBN 时校验。
- 已完成接口联调验证：前台列表/详情/分类树、后台 CRUD/状态更新。

**验证**：
- 图书/分类校验（价格>0、库存≥0、分类有效）
- 前台列表仅返回上架图书
- 分页限制符合规范（默认20/最大100）

## Phase 4：购物车
**目标**：购物车增删改查，库存与上下架校验。
- 实体/Mapper：`Cart`
- Service：添加（合并数量）、更新数量、删除、列表
- Controller：`/api/front/cart` 相关接口

**对齐记录**：
- 详细对齐清单见 `docs/phase4-alignment.md`

**完成记录（2026-03-14）**：
- 新增实体：`entity/Cart`。
- 新增 DTO：`CartAddRequest`、`CartUpdateRequest`。
- 新增 VO：`CartItemVO`（含 totalPrice）。
- 新增 Mapper：`CartMapper` 与对应 XML（购物车列表关联 book 返回价格/库存/封面）。
- 新增 Service：`CartService` / `CartServiceImpl`（合并数量、库存校验、上架校验、归属校验）。
- 新增 Controller：`FrontCartController`（GET/POST/PUT/DELETE）。
- 规则落地：使用 cartId 作为更新/删除路径参数；已存在图书时累加数量。
- 已完成接口联调验证：添加/合并、更新数量、列表 totalPrice、删除。

**验证**：
- 下架或库存不足不可添加
- 数量>0且<=库存
- 同一用户同一商品唯一

## Phase 5：收货地址
**目标**：多地址、默认地址、删除限制。
- 实体/Mapper：`Address`
- Controller：`/api/front/user/addresses` 全套接口

**验证**：
- 首次添加自动设默认
- 切换默认会清除其他默认
- 待发货订单引用地址不可删除

## Phase 6：订单全流程
**目标**：创建订单、支付、取消、列表与详情。
- 实体/Mapper：`Order`、`OrderItem`
- Service：创建（事务 + 库存校验 + 清空购物车）、支付（再校验 + 扣库存 + 状态流转）、取消
- Controller：`/api/front/orders` 相关接口

**验证**：
- 订单创建失败时回滚
- 支付状态流转：待支付→已支付→待发货（同事务）
- 用户仅可查询自身订单

## Phase 7：后台管理（用户/订单/轮播图）
**目标**：后台运营与管理能力收尾。
- 用户管理：列表、启用/禁用
- 订单管理：列表、详情、发货
- 轮播图：增删改查、排序，前台取前5张

**验证**：
- 管理员权限校验
- 轮播图前台返回数量≤5且排序正确

## Phase 8：验证与优化
**目标**：非功能性要求（安全、性能、日志）落实。
- 参数校验（@Valid/@Validated）
- SQL 规范（无 SELECT *、参数化）
- 日志脱敏与异常日志
- 分页上限与索引校验

**验证**：
- 校验失败返回 400
- 日志不含密码/Token
- 关键查询命中索引

## 验证方式（阶段性）
- 每阶段完成后：优先使用 Swagger / 手动调用（如 Postman）进行接口验收
- 单元测试：关键业务逻辑（订单创建/支付/库存）
- 集成测试：完整下单流程（注册→登录→加购→下单→支付→发货）

## 计划执行顺序
1) Phase 1 基础架构
2) Phase 2 用户认证
3) Phase 3 图书与分类
4) Phase 4 购物车
5) Phase 5 地址
6) Phase 6 订单
7) Phase 7 后台管理
8) Phase 8 安全与性能
