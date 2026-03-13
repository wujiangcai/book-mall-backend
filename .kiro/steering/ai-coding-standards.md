---
inclusion: always
---

# AI Coding 规范文档 - 图书商城后端项目

## 项目信息
- 项目名称：基于 Spring Boot 的图书商城管理系统
- 项目代码：book-mall-backend
- 技术栈：
  - 后端：Spring Boot 4.0.3 + Java 17 + MyBatis + MySQL + Maven + Lombok
  - 前端：Vue.js（前后端分离架构）
  - API 风格：RESTful API
- 项目类型：毕业设计项目
- 开发模式：AI 辅助开发
- 架构模式：前后端分离，后端提供 RESTful API 服务

---

## 一、核心原则

### 1.1 最小化幻觉原则
- **禁止臆测**：不确定的配置、API、依赖版本必须先查询官方文档或验证
- **禁止假设**：不假设数据库表结构、字段名、已有代码逻辑
- **先读后写**：修改任何文件前必须先完整读取该文件内容
- **验证优先**：每次生成代码后必须检查语法错误和编译问题

### 1.2 渐进式开发原则
- 每次只实现一个完整的小功能模块
- 先实现核心功能，再添加辅助功能
- 避免一次性生成大量未经验证的代码
- 每个功能完成后必须确认可运行再继续

### 1.3 可追溯原则
- 每次代码变更必须说明原因和目的
- 重要决策必须记录在注释或文档中
- 保持代码变更的原子性和独立性

---

## 二、代码生成规范

### 2.1 Java 代码规范

#### 包结构规范（严格遵循）
```
top.wjc.bookmallbackend
├── controller    # 控制器层 - RESTful API 接口
│   ├── admin     # 后台管理接口（图书管理、订单管理、用户管理、运营配置）
│   └── front     # 前台用户接口（商品浏览、购物车、订单、用户中心）
├── service       # 业务逻辑层 - 核心业务实现
│   ├── impl      # 服务实现类
│   └── ...       # 服务接口
├── mapper        # 数据访问层 - MyBatis Mapper 接口
├── entity        # 实体类 - 对应数据库表
│   ├── Book      # 图书实体
│   ├── User      # 用户实体
│   ├── Order     # 订单实体
│   ├── OrderItem # 订单详情实体
│   ├── Cart      # 购物车实体
│   ├── Address   # 收货地址实体
│   └── Category  # 图书分类实体
├── dto           # 数据传输对象 - 接收前端请求参数
├── vo            # 视图对象 - 返回给前端的数据
├── config        # 配置类（跨域、拦截器、MyBatis 配置等）
├── exception     # 异常处理（全局异常处理器、自定义异常）
├── interceptor   # 拦截器（登录验证、权限控制）
├── util          # 工具类（加密、日期、分页等）
├── constant      # 常量类（订单状态、用户角色等）
└── common        # 通用类（统一响应格式、分页对象等）
```

#### 命名规范
- 类名：大驼峰命名法（PascalCase），如 `UserController`
- 方法名：小驼峰命名法（camelCase），如 `getUserById`
- 常量：全大写下划线分隔，如 `MAX_PAGE_SIZE`
- 包名：全小写，如 `controller`

#### 注解使用规范
- Controller 层必须使用 `@RestController` 和 `@RequestMapping`
- Service 层必须使用 `@Service` 注解
- 使用 Lombok 注解简化代码：`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- 参数校验使用 `@Valid` 和 `@Validated`

#### 代码质量要求
- 每个类必须有清晰的职责，遵循单一职责原则
- 方法长度不超过 50 行，超过需拆分
- 避免深层嵌套，最多 3 层
- 必须处理所有可能的异常情况
- 禁止使用魔法数字，必须定义为常量

### 2.2 数据库相关规范

#### 核心数据表设计（必须严格遵循）
根据开题报告要求，系统必须包含以下核心表：

1. **图书表（book）**
   - 字段：id, book_name, author, publisher, isbn, price, stock, category_id, cover_image, description, status, create_time, update_time
   - 状态：上架(1)、下架(0)
   - **注意：** isbn 字段使用普通索引，不强制唯一。允许不同版本或空值；添加图书时若 ISBN 重复应提示管理员确认版本差异。

2. **用户表（user）**
   - 字段：id, username, password, nickname, phone, email, role, status, create_time, update_time
   - 角色：普通用户(0)、管理员(1)

3. **订单表（order）**
   - 字段：id, order_no, user_id, total_amount, status, address_id, create_time, update_time, pay_time, ship_time
   - 状态：待支付(0)、已支付(1)、待发货(2)、已发货(3)、已完成(4)、已取消(5)

4. **订单详情表（order_item）**
   - 字段：id, order_id, book_id, book_name, price, quantity, total_price

5. **购物车表（cart）**
   - 字段：id, user_id, book_id, quantity, create_time, update_time

6. **收货地址表（address）**
   - 字段：id, user_id, receiver_name, phone, province, city, district, detail_address, is_default

7. **图书分类表（category）**
   - 字段：id, category_name, parent_id, sort_order, status

8. **轮播图表（banner）**（运营配置）
   - 字段：id, image_url, link_url, sort_order, status

#### 实体类规范
- 使用 MyBatis 注解或 XML 映射
- 主键统一使用 `@TableId` 注解（MyBatis-Plus）或在 XML 中配置
- 字段必须与数据库列名一致，使用下划线命名（数据库）对应驼峰命名（Java）
- 时间字段统一使用 `LocalDateTime` 类型
- 必须使用 Lombok 的 `@Data` 注解
- 禁止在实体类中写业务逻辑

#### MyBatis Mapper 规范
- Mapper 接口必须使用 `@Mapper` 注解
- SQL 语句优先使用 XML 配置文件（复杂查询）
- 简单 CRUD 可使用注解方式（`@Select`, `@Insert`, `@Update`, `@Delete`）
- 禁止使用 `SELECT *`，必须明确指定字段
- 复杂查询必须添加注释说明
- 必须使用 `#{}` 参数化查询防止 SQL 注入，禁止使用 `${}`（除非动态表名/字段名）
- 分页查询必须限制最大页面大小（建议 100 条/页）

#### 数据库连接配置
```properties
# MyBatis 配置
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=top.wjc.bookmallbackend.entity
mybatis.configuration.map-underscore-to-camel-case=true
mybatis.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl
```

### 2.3 API 接口规范

#### RESTful 设计原则
- GET：查询操作，幂等
- POST：创建操作
- PUT：完整更新操作
- PATCH：部分更新操作
- DELETE：删除操作，幂等

#### API 路径规范（前后端分离架构）

**前台用户接口（/api/front）**
```
# 图书浏览与检索
GET  /api/front/books                    # 分页查询图书列表（支持分类、搜索）
GET  /api/front/books/{id}               # 查询图书详情
GET  /api/front/categories               # 获取图书分类树

# 用户注册与登录
POST /api/front/auth/register            # 用户注册
POST /api/front/auth/login               # 用户登录
POST /api/front/auth/logout              # 用户登出

# 用户中心
GET  /api/front/user/info                # 获取用户信息
PUT  /api/front/user/info                # 更新用户信息
GET  /api/front/user/addresses           # 获取收货地址列表
POST /api/front/user/addresses           # 添加收货地址
PUT  /api/front/user/addresses/{id}      # 更新收货地址
DELETE /api/front/user/addresses/{id}    # 删除收货地址

# 购物车
GET  /api/front/cart                     # 获取购物车列表
POST /api/front/cart                     # 添加商品到购物车
PUT  /api/front/cart/{id}                # 更新购物车商品数量
DELETE /api/front/cart/{id}              # 删除购物车商品

# 订单流程
POST /api/front/orders                   # 创建订单
GET  /api/front/orders                   # 查询我的订单列表
GET  /api/front/orders/{id}              # 查询订单详情
POST /api/front/orders/{id}/pay          # 模拟支付订单
POST /api/front/orders/{id}/cancel       # 取消订单

# 首页运营
GET  /api/front/banners                  # 获取轮播图列表
```

**后台管理接口（/api/admin）**
```
# 管理员登录
POST /api/admin/auth/login               # 管理员登录

# 图书管理
GET  /api/admin/books                    # 分页查询图书列表
GET  /api/admin/books/{id}               # 查询图书详情
POST /api/admin/books                    # 添加图书（上架）
PUT  /api/admin/books/{id}               # 更新图书信息
DELETE /api/admin/books/{id}             # 删除图书
PUT  /api/admin/books/{id}/status        # 上架/下架图书

# 分类管理
GET  /api/admin/categories               # 获取分类列表
POST /api/admin/categories               # 添加分类
PUT  /api/admin/categories/{id}          # 更新分类
DELETE /api/admin/categories/{id}        # 删除分类

# 订单管理
GET  /api/admin/orders                   # 分页查询订单列表
GET  /api/admin/orders/{id}              # 查询订单详情
PUT  /api/admin/orders/{id}/ship         # 订单发货

# 用户管理
GET  /api/admin/users                    # 分页查询用户列表
GET  /api/admin/users/{id}               # 查询用户详情
PUT  /api/admin/users/{id}/status        # 启用/禁用用户

# 运营配置
GET  /api/admin/banners                  # 获取轮播图列表
POST /api/admin/banners                  # 添加轮播图
PUT  /api/admin/banners/{id}             # 更新轮播图
DELETE /api/admin/banners/{id}           # 删除轮播图
PUT  /api/admin/banners/{id}/sort        # 调整轮播图排序
```

#### 统一响应格式
```java
{
    "code": 200,           // 业务状态码
    "message": "success",  // 提示信息
    "data": {},           // 响应数据
    "timestamp": 1234567890
}
```

---

## 三、AI 交互规范

### 3.1 需求描述规范
在请求 AI 生成代码时，必须提供：
- **明确的功能描述**：要实现什么功能
- **输入输出定义**：接收什么参数，返回什么数据
- **业务规则**：有哪些业务逻辑和校验规则
- **依赖关系**：依赖哪些已有的类或服务
- **异常处理**：需要处理哪些异常情况

### 3.2 代码审查检查点
AI 生成代码后，必须检查：
1. ✅ 包名和类名是否符合项目结构
2. ✅ 导入的依赖是否在 pom.xml 中存在
3. ✅ 使用的注解是否正确
4. ✅ 方法签名是否合理
5. ✅ 异常处理是否完整
6. ✅ 是否有语法错误
7. ✅ 是否符合 Java 17 语法特性
8. ✅ Lombok 注解使用是否正确

### 3.3 迭代开发流程
```
1. 明确单个功能需求
2. 让 AI 生成代码
3. 审查代码质量
4. 使用 getDiagnostics 检查错误
5. 修复问题
6. 确认功能可用
7. 提交代码
8. 继续下一个功能
```

---

## 四、依赖管理规范

### 4.1 添加依赖流程
1. 明确需要什么功能
2. 搜索官方推荐的依赖
3. 确认版本兼容性（Spring Boot 4.0.3 + Java 17）
4. 在 pom.xml 中添加依赖
5. 刷新 Maven 项目
6. 验证依赖可用

### 4.2 必需依赖清单
根据开题报告的技术选型，必须添加以下依赖：

```xml
<!-- MyBatis 持久层框架 -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>3.0.3</version>
</dependency>

<!-- MySQL 数据库驱动 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- 参数校验 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- JWT 令牌（用户认证） -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>

<!-- 密码加密 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- 可选：MyBatis-Plus（简化开发） -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.5</version>
</dependency>

<!-- 可选：Swagger API 文档 -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### 4.3 依赖版本兼容性
- Spring Boot 4.0.3 要求 Java 17+
- MyBatis Spring Boot Starter 3.x 兼容 Spring Boot 3.x/4.x
- MySQL Connector 8.x 兼容 MySQL 5.7+/8.x
- 禁止添加未经验证的第三方依赖

---

## 五、配置文件规范

### 5.1 application.properties 规范
```properties
# 应用配置
spring.application.name=book-mall-backend
server.port=8080

# 数据库配置（必须明确指定）
spring.datasource.url=jdbc:mysql://localhost:3306/book_mall?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:password}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MyBatis 配置
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=top.wjc.bookmallbackend.entity
mybatis.configuration.map-underscore-to-camel-case=true
mybatis.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

# 文件上传配置（图书封面、轮播图）
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# JWT 配置
jwt.secret=${JWT_SECRET:your-secret-key-change-in-production}
jwt.expiration=86400000

# 跨域配置（前后端分离必需）
cors.allowed-origins=http://localhost:3000,http://localhost:8081

# 日志配置
logging.level.top.wjc.bookmallbackend=DEBUG
logging.level.org.springframework.web=INFO
logging.level.top.wjc.bookmallbackend.mapper=DEBUG

# Swagger 配置（可选）
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### 5.2 敏感信息处理
- 数据库密码使用环境变量
- API 密钥不能硬编码
- 生产环境配置单独管理

---

## 六、异常处理规范

### 6.1 统一异常处理
必须创建全局异常处理器：
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 处理业务异常
    // 处理参数校验异常
    // 处理系统异常
}
```

### 6.2 自定义异常（必须创建）
```java
// 业务异常基类
public class BusinessException extends RuntimeException {
    private Integer code;
    private String message;
}

// 具体业务异常
public class InsufficientStockException extends BusinessException {}  // 库存不足
public class OrderNotFoundException extends BusinessException {}      // 订单不存在
public class UnauthorizedException extends BusinessException {}       // 未授权
public class BookOffShelfException extends BusinessException {}       // 图书已下架
public class InvalidOrderStatusException extends BusinessException {} // 订单状态异常
```

### 6.3 异常响应码规范
```
200  - 成功
400  - 请求参数错误
401  - 未登录或 Token 失效
403  - 无权限访问
404  - 资源不存在
500  - 服务器内部错误
1001 - 库存不足
1002 - 订单状态异常
1003 - 图书已下架
```

---

## 七、测试规范

### 7.1 单元测试要求
- 关键业务逻辑必须编写单元测试
- 测试覆盖率目标：核心业务 > 80%
- 使用 JUnit 5 + Mockito
- 测试方法命名：`should_ExpectedBehavior_When_Condition`

### 7.2 集成测试要求
- API 接口必须进行集成测试
- 使用 `@SpringBootTest` 和 `MockMvc`
- 测试正常流程和异常流程

---

## 八、AI 使用禁止事项

### 8.1 严格禁止
❌ 让 AI 一次性生成整个项目框架
❌ 不验证直接使用 AI 生成的 SQL 语句
❌ 复制粘贴 AI 代码而不理解其逻辑
❌ 让 AI 生成未在 pom.xml 中声明的依赖代码
❌ 使用 AI 生成的硬编码密码和密钥
❌ 不检查就提交 AI 生成的代码

### 8.2 谨慎使用
⚠️ AI 生成的复杂算法（需要人工验证）
⚠️ AI 建议的架构设计（需要评估合理性）
⚠️ AI 生成的数据库索引（需要性能测试）
⚠️ AI 推荐的第三方库（需要调研可靠性）

---

## 九、代码提交规范

### 9.1 提交信息格式
```
<type>(<scope>): <subject>

<body>

<footer>
```

类型（type）：
- feat: 新功能
- fix: 修复 bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 重构
- test: 测试相关
- chore: 构建或辅助工具变动

### 9.2 提交前检查
- [ ] 代码可以编译通过
- [ ] 没有明显的语法错误
- [ ] 关键功能已测试
- [ ] 没有敏感信息
- [ ] 符合代码规范

---

## 十、文档规范

### 10.1 代码注释
- 类注释：说明类的职责和用途
- 方法注释：说明参数、返回值、异常
- 复杂逻辑：必须添加行内注释
- 使用中文注释，清晰易懂

### 10.2 API 文档
- 使用 Swagger/OpenAPI 生成 API 文档
- 每个接口必须有描述
- 参数和响应必须有示例

---

## 十一、性能和安全规范

### 11.1 性能要求

#### 数据库查询优化
- 避免 N+1 查询问题（使用 JOIN 或批量查询）
- 订单列表查询必须分页，默认每页 10 条，最大 100 条
- 图书列表查询必须分页，默认每页 20 条

#### 分页参数规范

| 模块 | 默认每页条数 | 最大每页条数 | 说明 |
|------|------------|------------|------|
| 图书列表 | 20 | 100 | 前台和后台统一 |
| 订单列表 | 10 | 100 | 前台和后台统一 |
| 用户列表 | 10 | 100 | 仅后台 |
| 分类列表 | 不分页 | - | 数据量小 |
| 购物车 | 不分页 | - | 用户级数据 |
| 地址列表 | 不分页 | - | 用户级数据 |
| 轮播图 | 不分页 | 5 | 固定返回前5条 |
- 为常用查询字段添加索引：
  - book 表：category_id, status
  - order 表：user_id, order_no, status
  - cart 表：user_id, book_id
- 避免在循环中执行数据库操作

#### 缓存策略（可选）
- 图书分类列表可以缓存（变动频率低）
- 首页轮播图可以缓存
- 轮播图查询需限制返回前 5 条（后台可维护多张，但前台只显示前5张，使用 LIMIT 5）
- 热门图书列表可以缓存

#### 事务控制
- 创建订单操作必须使用事务（`@Transactional`）
- 事务范围尽可能小，避免长事务
- 支付扣减库存失败时必须回滚支付事务

#### 响应时间要求
- 列表查询响应时间 < 500ms
- 详情查询响应时间 < 200ms
- 创建订单响应时间 < 1s

### 11.2 安全要求（严格执行）

#### 认证与授权
- 前台用户接口必须验证 JWT Token
- 后台管理接口必须验证管理员权限
- Token 过期时间设置为 24 小时
- 密码使用 BCrypt 加密存储，禁止明文存储
- 登录失败次数限制（可选：5 次后锁定账户）

#### 输入验证
- 所有用户输入必须进行参数校验（使用 `@Valid` 和 `@Validated`）
- 价格、数量等数值必须校验范围
- 邮箱、手机号必须校验格式
- 文件上传必须校验文件类型和大小

#### SQL 注入防护
- 必须使用 MyBatis 的 `#{}` 参数化查询
- 禁止使用字符串拼接 SQL
- 动态表名/字段名必须使用白名单验证

#### XSS 防护
- 用户输入的文本内容需要进行 HTML 转义
- 富文本编辑器内容需要过滤危险标签

#### 敏感信息保护
- 密码字段不能在响应中返回
- 日志中不能打印敏感信息（密码、Token）
- 数据库密码使用环境变量配置

---

## 十二、AI Coding 最佳实践

### 12.1 有效的提问方式

✅ 好的提问示例 1：
"请为后台图书管理模块创建 AdminBookController，实现以下接口：
1. 分页查询图书列表（支持按书名、作者模糊搜索，按分类筛选）
2. 根据 ID 查询图书详情
3. 添加新图书（需要校验：书名不能为空，价格必须大于0，库存不能为负数）
4. 更新图书信息
5. 上架/下架图书
请使用统一响应格式，添加参数校验，并需要管理员权限验证。"

✅ 好的提问示例 2：
"请创建订单相关的实体类和 Mapper：
1. Order 实体类：包含订单编号、用户ID、总金额、订单状态、收货地址ID、创建时间等字段
2. OrderItem 实体类：包含订单ID、图书ID、图书名称、价格、数量、小计金额
3. OrderMapper 接口：包含插入订单、根据订单号查询、根据用户ID分页查询、更新订单状态等方法
请使用 MyBatis 注解方式，实体类使用 Lombok。"

✅ 好的提问示例 3：
"请实现前台用户下单的完整流程：
1. 接收购物车商品ID列表和收货地址ID
2. 校验库存是否充足
3. 生成订单编号（时间戳+用户ID+随机数）
4. 计算订单总金额
5. 创建订单和订单详情记录
6. 清空购物车
7. 支付时扣减库存
8. 返回订单信息
需要事务控制，库存不足时回滚，并抛出业务异常。"

❌ 不好的提问：
- "帮我写一个图书管理的代码"
- "创建一个订单功能"
- "实现用户登录"

### 12.2 代码审查清单
每次 AI 生成代码后，使用此清单检查：
```
[ ] 包名是否正确：top.wjc.bookmallbackend.xxx
[ ] 类名是否符合命名规范
[ ] 注解使用是否正确
[ ] 依赖是否已在 pom.xml 中声明
[ ] 是否有语法错误（使用 getDiagnostics 检查）
[ ] 异常处理是否完整
[ ] 是否有硬编码的配置
[ ] 注释是否清晰
[ ] 是否符合 RESTful 规范
[ ] 响应格式是否统一
```

### 12.3 模块开发优先级（按开题报告要求）

根据开题报告的研究内容，建议按以下顺序开发：

**第一阶段：基础架构搭建**
1. 配置数据库连接和 MyBatis
2. 创建统一响应格式类（Result）
3. 创建全局异常处理器
4. 配置跨域支持（CORS）
5. 集成 Swagger API 文档

**第二阶段：用户认证模块**
1. 创建用户实体类和 Mapper
2. 实现用户注册功能（密码加密）
3. 实现用户登录功能（JWT Token）
4. 实现登录拦截器
5. 实现用户信息查询和修改

**第三阶段：图书管理模块**
1. 创建图书分类实体和管理接口
2. 创建图书实体类和 Mapper
3. 实现后台图书管理（增删改查、上下架）
4. 实现前台图书浏览（分页、搜索、分类筛选）
5. 实现图书详情查询

**第四阶段：购物车模块**
1. 创建购物车实体类和 Mapper
2. 实现添加商品到购物车
3. 实现购物车列表查询
4. 实现购物车商品数量修改
5. 实现购物车商品删除

**第五阶段：订单流程模块**
1. 创建收货地址实体和管理接口
2. 创建订单和订单详情实体
3. 实现创建订单功能（库存校验、事务控制）
4. 实现模拟支付功能
5. 实现订单列表和详情查询
6. 实现订单状态跟踪

**第六阶段：后台管理模块**
1. 实现用户管理（查看、禁用/启用）
2. 实现订单管理（查看、发货）
3. 实现轮播图管理（增删改查、排序）

**第七阶段：系统优化**
1. 添加参数校验
2. 完善异常处理
3. 性能优化（SQL 优化、索引）
4. 安全加固（权限控制、输入验证）

### 12.4 渐进式开发示例
第一步：创建实体类和数据库表
第二步：创建 Mapper 接口
第三步：创建 Service 层
第四步：创建 Controller 层
第五步：添加参数校验
第六步：添加异常处理
第七步：编写单元测试
第八步：集成测试验证

---

## 十三、项目特定规范

### 13.1 图书商城核心业务规则（严格遵循）

#### 图书管理业务规则
- 图书价格必须大于 0
- 库存数量不能为负数
- 图书状态：上架(1)、下架(0)
- 下架的图书不能在前台展示和购买
- 图书必须关联分类，分类不能为空
- 图书封面图片必须是有效的图片格式（jpg, png, webp）
- ISBN 不强制唯一，允许同一本书的不同版本使用不同 ISBN
- 添加图书时，如果 ISBN 已存在，应提示管理员确认是否为不同版本

#### 用户注册与认证规则
- 用户名必须唯一，长度 4-20 字符
- 密码必须加密存储（使用 BCrypt）
- 手机号必须符合中国手机号格式（11 位数字）
- 用户角色：普通用户(0)、管理员(1)
- 前台接口需要用户登录验证（JWT Token）
- 后台接口需要管理员权限验证

#### 购物车业务规则
- 同一用户同一商品只能有一条购物车记录
- 添加已存在商品时，数量累加
- 购物车商品数量必须 > 0 且 <= 库存数量
- 下架商品不能加入购物车

#### 订单流程业务规则
- 订单编号生成规则：时间戳 + 用户ID + 随机数（确保唯一）
- 订单金额 = Σ(商品价格 × 数量)
- 创建订单时必须检查库存，库存不足不能下单
- 库存扣减策略：支付时扣减（支付前不扣减，支付时再次校验库存并扣减）
- 订单状态流转：
  - 正常流程：待支付(0) → 已支付(1) → 待发货(2) → 已发货(3) → 已完成(4)
  - 取消流程：待支付(0) → 已取消(5)
  - 状态说明：
    - 待支付：订单创建后等待用户支付
    - 已支付：用户完成支付（瞬时状态，立即自动变更为"待发货"）
    - 待发货：支付成功后自动进入此状态，等待管理员发货
    - 已发货：管理员确认发货后变更
    - 已完成：用户确认收货或系统自动完成
    - 已取消：用户主动取消或超时未支付自动取消
  - 状态变更实现：
    - 支付成功后，在同一事务中完成两次状态更新：已支付(1) → 待发货(2)
    - 这样确保状态流转的原子性和完整性
- 只有待支付状态的订单可以支付
- 只有待发货状态的订单可以发货
- 已取消、已完成的订单不能修改状态
- 订单必须关联收货地址

#### 收货地址业务规则
- 每个用户可以有多个收货地址
- 必须有一个默认地址
- 设置新默认地址时，取消其他地址的默认状态
- 收货人姓名、手机号、详细地址为必填项

#### 后台管理业务规则
- 管理员可以查看所有用户信息
- 管理员可以查看所有订单并进行发货操作
- 管理员可以管理图书的上架、下架、编辑
- 管理员可以配置首页轮播图
- 轮播图按 sort_order 排序显示
- 轮播图最多显示 5 张
- 图书删除采用软删除（status = -1），仅在确认未被订单引用时允许物理删除

### 13.2 数据字典（开发前必须遵循）

#### 用户角色枚举
```java
public enum UserRole {
    USER(0, "普通用户"),
    ADMIN(1, "管理员");
}
```

#### 订单状态枚举
```java
public enum OrderStatus {
    UNPAID(0, "待支付"),
    PAID(1, "已支付"),
    PENDING_SHIP(2, "待发货"),
    SHIPPED(3, "已发货"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");
}
```

#### 图书状态枚举
```java
public enum BookStatus {
    OFF_SHELF(0, "下架"),
    ON_SHELF(1, "上架");
}
```

#### 通用状态枚举
```java
public enum CommonStatus {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");
}
```

---

## 十四、问题排查规范

### 14.1 编译错误排查
1. 使用 `getDiagnostics` 工具检查错误
2. 检查导入的类是否存在
3. 检查依赖是否已添加到 pom.xml
4. 检查 Java 版本兼容性
5. 清理并重新构建项目

### 14.2 运行时错误排查
1. 查看完整的错误堆栈
2. 检查配置文件是否正确
3. 检查数据库连接是否正常
4. 检查端口是否被占用
5. 查看日志文件

---

## 十五、版本控制规范

### 15.1 分支管理
- main: 主分支，稳定版本
- develop: 开发分支
- feature/xxx: 功能分支
- bugfix/xxx: 修复分支

### 15.2 代码合并
- 功能开发在 feature 分支
- 开发完成后合并到 develop
- 测试通过后合并到 main

---

## 使用说明

本规范文件已配置为 `inclusion: always`，这意味着在所有 AI 交互中都会自动加载此规范。

在开发过程中：
1. 每次请求 AI 生成代码时，AI 会自动遵循此规范
2. 如果 AI 的输出不符合规范，请明确指出并要求修正
3. 定期回顾和更新此规范文件
4. 遇到新的问题和经验，及时补充到规范中

---

**最后提醒**：AI 是辅助工具，不是替代品。理解每一行代码的含义，对项目质量负责，才能完成高质量的毕业设计。

---

## 附录：开题报告核心要求对照表

根据开题报告的主要研究内容，以下是各模块的实现要点：

### 1. 系统总体设计与架构搭建 ✓
- [x] 采用前后端分离架构
- [x] 后端使用 Spring Boot + MyBatis
- [x] 设计 RESTful API 接口
- [x] 分层架构：Controller → Service → Mapper
- [x] 统一响应格式和异常处理

### 2. 图书商品信息的管理与展示 ✓
- [x] 后台：图书上架、下架、编辑、分类管理
- [x] 前台：多维度浏览（分类、搜索）
- [x] 图书详情展示
- [x] 前后台数据同步

### 3. 前台用户注册与中心管理 ✓
- [x] 用户注册、登录
- [x] JWT Token 认证
- [x] 个人信息维护
- [x] 收货地址管理
- [x] 密码加密存储

### 4. 购物流程与订单跟踪 ✓
- [x] 购物车管理
- [x] 订单生成
- [x] 模拟支付
- [x] 订单状态跟踪（待发货、已发货）
- [x] 订单详情查看

### 5. 后台综合管理功能 ✓
- [x] 用户管理（查看、权限管理）
- [x] 订单管理（查看、发货处理）
- [x] 运营配置（轮播图、促销活动）

### 6. 数据库设计与性能优化 ✓
- [x] 8 张核心数据表设计
- [x] 表间关系明确
- [x] 索引优化
- [x] 分页查询
- [x] 事务控制

### 7. 系统非功能性保障 ✓
- [x] 友好的 API 接口设计
- [x] 参数验证
- [x] 权限控制
- [x] 异常处理
- [x] 安全性保障

---

## 开发检查清单

在每个功能模块开发完成后，使用此清单进行自检：

**代码质量**
- [ ] 代码符合命名规范
- [ ] 没有语法错误（使用 getDiagnostics 检查）
- [ ] 注释清晰完整
- [ ] 没有硬编码的配置
- [ ] 使用了 Lombok 简化代码

**功能完整性**
- [ ] 实现了所有必需的接口
- [ ] 参数校验完整
- [ ] 异常处理完善
- [ ] 业务逻辑正确

**数据库操作**
- [ ] SQL 语句使用参数化查询
- [ ] 没有使用 SELECT *
- [ ] 事务控制正确
- [ ] 分页查询有限制

**安全性**
- [ ] 敏感接口有权限验证
- [ ] 密码已加密
- [ ] 输入已验证
- [ ] 没有 SQL 注入风险

**API 规范**
- [ ] 符合 RESTful 规范
- [ ] 使用统一响应格式
- [ ] 路径命名规范
- [ ] HTTP 方法使用正确

**测试验证**
- [ ] 使用 Swagger 测试接口
- [ ] 正常流程测试通过
- [ ] 异常流程测试通过
- [ ] 边界条件测试通过

---

祝你的毕业设计顺利完成！🎓

如有任何问题，请随时向 AI 提问，记住：
1. 描述清楚需求
2. 提供必要的上下文
3. 一次只做一个功能
4. 验证后再继续
