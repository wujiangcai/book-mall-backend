# 需求文档 - 图书商城管理系统

## 引言

本文档描述了基于 Spring Boot 的图书商城管理系统的功能需求和非功能需求。系统采用前后端分离架构，为用户提供在线图书浏览、购买服务，为管理员提供图书、订单、用户的综合管理功能。

## 术语表

- **System**: 图书商城管理系统
- **User**: 注册并登录的前台用户
- **Admin**: 后台管理员
- **Book**: 图书商品
- **Cart**: 购物车
- **Order**: 订单
- **Category**: 图书分类
- **Address**: 收货地址
- **Token**: JWT 身份认证令牌
- **Inventory**: 图书库存
- **Payment**: 支付操作（模拟）
- **Banner**: 首页轮播图

## 需求列表

### 需求 1: 用户注册与认证

**用户故事:** 作为一个新用户，我希望能够注册账号并安全登录，以便使用图书商城的购物功能。

#### 验收标准

1. WHEN 用户提交注册信息，THE System SHALL 验证用户名唯一性（长度 4-20 字符）
2. WHEN 用户提交注册信息，THE System SHALL 验证手机号格式（11 位数字）
3. WHEN 用户提交注册信息，THE System SHALL 使用 BCrypt 算法加密存储密码
4. WHEN 用户提交有效的登录凭证，THE System SHALL 生成 JWT Token（有效期 24 小时）
5. WHEN 用户提交无效的登录凭证，THE System SHALL 返回认证失败错误
6. WHEN 用户访问需要认证的接口，THE System SHALL 验证 Token 有效性
7. IF Token 已过期或无效，THEN THE System SHALL 返回 401 未授权错误

### 需求 2: 用户个人信息管理

**用户故事:** 作为已登录用户，我希望能够查看和修改个人信息，以便保持账户信息的准确性。

#### 验收标准

1. WHEN 用户请求个人信息，THE System SHALL 返回用户昵称、手机号、邮箱等信息（不包含密码）
2. WHEN 用户更新个人信息，THE System SHALL 验证邮箱格式的有效性
3. WHEN 用户更新个人信息，THE System SHALL 验证手机号格式的有效性
4. THE System SHALL 记录用户信息的更新时间

### 需求 3: 收货地址管理

**用户故事:** 作为已登录用户，我希望能够管理多个收货地址，以便在下单时选择合适的配送地址。

#### 验收标准

1. WHEN 用户添加收货地址，THE System SHALL 验证收货人姓名、手机号、详细地址为必填项
2. WHEN 用户添加第一个地址，THE System SHALL 自动设置为默认地址
3. WHEN 用户设置新的默认地址，THE System SHALL 取消其他地址的默认状态
4. WHEN 用户查询地址列表，THE System SHALL 返回该用户的所有收货地址
5. WHEN 用户删除地址，THE System SHALL 验证该地址未被待发货订单使用
6. THE System SHALL 允许每个用户管理多个收货地址

### 需求 4: 图书分类管理

**用户故事:** 作为管理员，我希望能够管理图书分类，以便组织图书商品的展示结构。

#### 验收标准

1. WHEN 管理员创建分类，THE System SHALL 验证分类名称唯一性
2. WHEN 管理员创建分类，THE System SHALL 允许指定父分类 ID（支持二级分类）
3. WHEN 管理员更新分类，THE System SHALL 验证分类名称不与其他分类重复
4. WHEN 管理员删除分类，THE System SHALL 验证该分类下没有图书商品
5. THE System SHALL 按排序字段和创建时间排序分类列表

### 需求 5: 图书信息管理

**用户故事:** 作为管理员，我希望能够管理图书信息，以便控制商城的商品内容。

#### 验收标准

1. WHEN 管理员添加图书，THE System SHALL 验证书名不能为空
2. WHEN 管理员添加图书，THE System SHALL 验证价格必须大于 0
3. WHEN 管理员添加图书，THE System SHALL 验证库存数量不能为负数
4. WHEN 管理员添加图书，THE System SHALL 验证图书必须关联有效的分类
5. WHEN 管理员添加图书，THE System SHALL 验证封面图片格式（jpg、png、webp）
6. WHEN 管理员添加图书，THE System SHALL 将 ISBN 作为普通索引处理，允许重复；如果输入的 ISBN 已存在，THEN THE System SHALL 提示管理员确认是否为不同版本（例如平装/精装）。
7. WHEN 管理员更新图书信息，THE System SHALL 记录更新时间
8. WHEN 管理员上架图书，THE System SHALL 设置图书状态为上架（1）
9. WHEN 管理员下架图书，THE System SHALL 设置图书状态为下架（0）
10. WHEN 管理员删除图书，THE System SHALL 默认使用软删除方式标记图书状态（status = -1）
11. IF 图书已被订单引用，THEN THE System SHALL 禁止物理删除
12. THE System SHALL 支持按书名、作者、出版社、分类查询图书

### 需求 6: 前台图书浏览与检索

**用户故事:** 作为前台用户，我希望能够浏览和搜索图书，以便找到想要购买的商品。

#### 验收标准

1. WHEN 用户请求图书列表，THE System SHALL 仅返回上架状态的图书
2. WHEN 用户请求图书列表，THE System SHALL 支持按分类筛选
3. WHEN 用户请求图书列表，THE System SHALL 支持按书名、作者模糊搜索
4. WHEN 用户请求图书列表，THE System SHALL 分页返回结果（默认每页 20 条，最大 100 条）
5. WHEN 用户请求图书详情，THE System SHALL 返回完整的图书信息（书名、作者、出版社、价格、库存、封面、简介等）
6. IF 图书已下架，THEN THE System SHALL 在详情页显示下架状态
7. THE System SHALL 在列表查询中返回图书的分类名称

### 需求 7: 购物车管理

**用户故事:** 作为已登录用户，我希望能够管理购物车，以便临时保存想要购买的图书。

#### 验收标准

1. WHEN 用户添加图书到购物车，THE System SHALL 验证图书处于上架状态
2. WHEN 用户添加图书到购物车，THE System SHALL 验证库存数量充足
3. WHEN 用户添加已存在的图书，THE System SHALL 累加商品数量
4. WHEN 用户更新购物车商品数量，THE System SHALL 验证数量大于 0 且不超过库存
5. WHEN 用户查询购物车，THE System SHALL 返回商品的当前价格和库存信息
6. WHEN 用户删除购物车商品，THE System SHALL 移除该商品记录
7. THE System SHALL 确保同一用户同一商品只有一条购物车记录

### 需求 8: 订单创建与支付

**用户故事:** 作为已登录用户，我希望能够创建订单并完成支付，以便购买图书商品。

#### 验收标准

1. WHEN 用户创建订单，THE System SHALL 验证购物车商品列表不为空
2. WHEN 用户创建订单，THE System SHALL 验证收货地址有效性
3. WHEN 用户创建订单，THE System SHALL 验证所有商品库存充足
4. IF 任一商品库存不足，THEN THE System SHALL 返回库存不足错误并回滚事务
5. WHEN 用户创建订单，THE System SHALL 生成唯一订单编号（时间戳 + 用户 ID + 随机数）
6. WHEN 用户创建订单，THE System SHALL 计算订单总金额（Σ 商品价格 × 数量）
7. WHEN 用户创建订单，THE System SHALL 创建订单主记录和订单详情记录
8. WHEN 用户创建订单，THE System SHALL 清空已下单的购物车商品
9. WHEN 用户创建订单，THE System SHALL 设置订单状态为待支付（0）
10. WHEN 用户支付订单，THE System SHALL 验证订单状态为待支付
11. WHEN 用户支付订单，THE System SHALL 再次校验库存充足性
12. WHEN 用户支付订单，THE System SHALL 扣减图书库存
13. IF 支付时库存不足，THEN THE System SHALL 返回库存不足错误，订单保持待支付状态
14. WHEN 用户支付订单，THE System SHALL 更新订单状态为已支付（1）并记录支付时间
15. IF 订单状态不是待支付，THEN THE System SHALL 返回订单状态异常错误
16. THE System SHALL 使用事务确保订单创建的原子性
17. WHEN 用户支付订单成功，THE System SHALL 自动更新订单状态为待发货（2）
18. THE System SHALL 在支付成功后立即执行状态变更，无需人工干预

### 需求 9: 订单查询与跟踪

**用户故事:** 作为已登录用户，我希望能够查看订单历史和跟踪订单状态，以便了解购买记录和配送进度。

#### 验收标准

1. WHEN 用户查询订单列表，THE System SHALL 仅返回该用户的订单
2. WHEN 用户查询订单列表，THE System SHALL 分页返回结果（默认每页 10 条，最大 100 条）
3. WHEN 用户查询订单列表，THE System SHALL 支持按订单状态筛选
4. WHEN 用户查询订单详情，THE System SHALL 返回订单信息、商品明细、收货地址
5. WHEN 用户查询订单详情，THE System SHALL 验证订单归属权限
6. THE System SHALL 按创建时间倒序排列订单列表

### 需求 10: 订单取消

**用户故事:** 作为已登录用户，我希望能够取消未支付的订单，以便放弃不需要的购买。

#### 验收标准

1. WHEN 用户取消订单，THE System SHALL 验证订单状态为待支付
2. IF 订单状态不是待支付，THEN THE System SHALL 返回订单状态异常错误
3. WHEN 用户取消订单，THE System SHALL 更新订单状态为已取消（5）
4. WHEN 用户取消订单且订单已支付，THE System SHALL 恢复图书库存数量
5. THE System SHALL 使用事务确保取消操作的原子性

### 需求 11: 后台订单管理

**用户故事:** 作为管理员，我希望能够查看和处理订单，以便进行发货操作和售后服务。

#### 验收标准

1. WHEN 管理员查询订单列表，THE System SHALL 返回所有用户的订单
2. WHEN 管理员查询订单列表，THE System SHALL 支持按订单号、用户 ID、订单状态筛选
3. WHEN 管理员查询订单列表，THE System SHALL 分页返回结果（默认每页 10 条，最大 100 条）
4. WHEN 管理员查询订单详情，THE System SHALL 返回完整的订单信息和用户信息
5. WHEN 管理员标记订单发货，THE System SHALL 验证订单状态为待发货（2）
6. WHEN 管理员标记订单发货，THE System SHALL 更新订单状态为已发货（3）并记录发货时间
7. IF 订单状态不是待发货，THEN THE System SHALL 返回订单状态异常错误

**说明**：订单支付成功后，系统应自动将订单状态从"已支付(1)"变更为"待发货(2)"，管理员只需处理"待发货"状态的订单。

### 需求 12: 后台用户管理

**用户故事:** 作为管理员，我希望能够查看和管理用户账户，以便进行用户权限控制。

#### 验收标准

1. WHEN 管理员查询用户列表，THE System SHALL 分页返回所有用户信息（不包含密码）
2. WHEN 管理员查询用户列表，THE System SHALL 支持按用户名、手机号模糊搜索
3. WHEN 管理员查询用户详情，THE System SHALL 返回用户基本信息和注册时间
4. WHEN 管理员禁用用户，THE System SHALL 更新用户状态为禁用（0）
5. WHEN 管理员启用用户，THE System SHALL 更新用户状态为启用（1）
6. WHILE 用户状态为禁用，THE System SHALL 拒绝该用户的登录请求

### 需求 13: 轮播图管理

**用户故事:** 作为管理员，我希望能够管理首页轮播图，以便进行运营推广活动。

#### 验收标准

1. WHEN 管理员添加轮播图，THE System SHALL 验证图片 URL 不能为空
2. WHEN 管理员添加轮播图，THE System SHALL 验证图片格式（jpg、png、webp）
3. WHEN 管理员添加轮播图，THE System SHALL 设置默认排序值
4. WHEN 管理员更新轮播图排序，THE System SHALL 更新排序字段值
5. WHEN 管理员删除轮播图，THE System SHALL 移除该轮播图记录
6. WHEN 前台用户请求轮播图，THE System SHALL 按排序字段升序返回启用状态的轮播图，并限制返回前 5 张
7. THE System SHALL 限制轮播图最多显示 5 张

### 需求 14: 管理员认证与权限控制

**用户故事:** 作为管理员，我希望能够安全登录后台系统，以便访问管理功能。

#### 验收标准

1. WHEN 管理员提交登录凭证，THE System SHALL 验证用户角色为管理员（1）
2. IF 用户角色不是管理员，THEN THE System SHALL 返回权限不足错误
3. WHEN 管理员登录成功，THE System SHALL 生成 JWT Token（有效期 24 小时）
4. WHEN 管理员访问后台接口，THE System SHALL 验证 Token 中的角色为管理员
5. IF Token 中角色不是管理员，THEN THE System SHALL 返回 403 禁止访问错误

### 需求 15: 数据库查询性能优化

**用户故事:** 作为系统运维人员，我希望系统具有良好的查询性能，以便为用户提供流畅的使用体验。

#### 验收标准

1. THE System SHALL 为 book 表的 category_id 和 status 字段创建索引
2. THE System SHALL 为 order 表的 user_id、order_no 和 status 字段创建索引
3. THE System SHALL 为 cart 表的 user_id 和 book_id 字段创建联合索引
4. THE System SHALL 为 user 表的 username 和 phone 字段创建唯一索引
5. WHEN 执行列表查询，THE System SHALL 使用分页限制返回结果数量
6. WHEN 执行关联查询，THE System SHALL 避免 N+1 查询问题
7. THE System SHALL 确保列表查询响应时间小于 500ms
8. THE System SHALL 确保详情查询响应时间小于 200ms

### 需求 16: 输入验证与安全防护

**用户故事:** 作为系统安全负责人，我希望系统能够防范常见的安全攻击，以便保护用户数据安全。

#### 验收标准

1. WHEN 用户提交任何输入，THE System SHALL 使用参数化查询防止 SQL 注入
2. WHEN 用户提交价格或数量，THE System SHALL 验证数值范围的合理性
3. WHEN 用户提交邮箱，THE System SHALL 验证邮箱格式的有效性
4. WHEN 用户提交手机号，THE System SHALL 验证手机号格式（11 位数字）
5. WHEN 用户上传文件，THE System SHALL 验证文件类型和大小（最大 10MB）
6. THE System SHALL 在响应中排除密码字段
7. THE System SHALL 在日志中排除敏感信息（密码、Token）
8. THE System SHALL 对用户输入的文本内容进行 HTML 转义防止 XSS 攻击

### 需求 17: 统一异常处理

**用户故事:** 作为前端开发人员，我希望后端 API 返回统一的错误格式，以便进行统一的错误处理。

#### 验收标准

1. WHEN 发生业务异常，THE System SHALL 返回统一的错误响应格式（code、message、timestamp）
2. WHEN 发生参数校验失败，THE System SHALL 返回 400 状态码和详细的校验错误信息
3. WHEN 发生认证失败，THE System SHALL 返回 401 状态码和未授权错误信息
4. WHEN 发生权限不足，THE System SHALL 返回 403 状态码和禁止访问错误信息
5. WHEN 发生资源不存在，THE System SHALL 返回 404 状态码和资源不存在错误信息
6. WHEN 发生系统异常，THE System SHALL 返回 500 状态码并记录详细错误日志
7. WHEN 发生库存不足，THE System SHALL 返回业务错误码 1001 和库存不足提示
8. WHEN 发生订单状态异常，THE System SHALL 返回业务错误码 1002 和状态异常提示

### 需求 18: API 响应格式标准化

**用户故事:** 作为前端开发人员，我希望所有 API 返回统一的响应格式，以便简化前端数据处理逻辑。

#### 验收标准

1. THE System SHALL 使用统一的响应格式包含 code、message、data、timestamp 字段
2. WHEN 操作成功，THE System SHALL 返回 code 为 200 和 message 为 "success"
3. WHEN 操作成功且有返回数据，THE System SHALL 在 data 字段中包含响应数据
4. WHEN 操作成功且无返回数据，THE System SHALL 在 data 字段中返回 null
5. THE System SHALL 在每个响应中包含时间戳字段

### 需求 19: 跨域资源共享配置

**用户故事:** 作为前端开发人员，我希望能够从前端应用访问后端 API，以便实现前后端分离架构。

#### 验收标准

1. THE System SHALL 配置 CORS 允许指定的前端域名访问
2. THE System SHALL 允许 GET、POST、PUT、DELETE、PATCH 等 HTTP 方法
3. THE System SHALL 允许 Content-Type、Authorization 等请求头
4. THE System SHALL 允许携带认证凭证（Cookies、Authorization Header）
5. THE System SHALL 设置预检请求的缓存时间为 3600 秒

### 需求 20: 系统日志记录

**用户故事:** 作为系统运维人员，我希望系统能够记录关键操作日志，以便进行问题排查和审计。

#### 验收标准

1. WHEN 用户登录成功或失败，THE System SHALL 记录登录日志（用户名、IP 地址、时间）
2. WHEN 管理员执行敏感操作，THE System SHALL 记录操作日志（操作类型、操作对象、时间）
3. WHEN 发生系统异常，THE System SHALL 记录详细的错误堆栈信息
4. WHEN 执行数据库操作，THE System SHALL 在开发环境记录 SQL 语句
5. THE System SHALL 设置不同模块的日志级别（DEBUG、INFO、WARN、ERROR）
6. THE System SHALL 确保日志中不包含敏感信息（密码、Token、身份证号）
