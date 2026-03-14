# 设计文档 - 图书商城管理系统

## 1. 系统架构设计

### 1.1 总体架构

本系统采用前后端分离的架构模式，前端和后端通过 RESTful API 进行通信。

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue.js)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  用户前台页面  │  │  管理后台页面  │  │   路由管理    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                  │              │
│         └──────────────────┴──────────────────┘              │
│                           │                                  │
│                    HTTP/HTTPS (RESTful API)                  │
│                           │                                  │
└───────────────────────────┼──────────────────────────────────┘
                            │
┌───────────────────────────┼──────────────────────────────────┐
│                    后端层 (Spring Boot)                       │
│  ┌────────────────────────┴────────────────────────┐         │
│  │           Controller 层 (控制器)                 │         │
│  │  ┌──────────────┐        ┌──────────────┐      │         │
│  │  │  前台控制器   │        │  后台控制器   │      │         │
│  │  └──────────────┘        └──────────────┘      │         │
│  └──────────────┬──────────────────┬───────────────┘         │
│                 │                  │                          │
│  ┌──────────────┴──────────────────┴───────────────┐         │
│  │           Service 层 (业务逻辑)                  │         │
│  │  用户服务 | 图书服务 | 订单服务 | 购物车服务     │         │
│  └──────────────┬──────────────────┬───────────────┘         │
│                 │                  │                          │
│  ┌──────────────┴──────────────────┴───────────────┐         │
│  │           Mapper 层 (数据访问)                   │         │
│  │  UserMapper | BookMapper | OrderMapper ...      │         │
│  └──────────────┬──────────────────┬───────────────┘         │
└─────────────────┼──────────────────┼───────────────────────────┘
                  │                  │
┌─────────────────┴──────────────────┴───────────────────────────┐
│                   数据层 (MySQL)                                │
│  user | book | order | cart | category | address | banner      │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 三层架构职责划分

**Controller 层（控制器层）**
- 接收 HTTP 请求，解析请求参数
- 调用 Service 层处理业务逻辑
- 封装响应数据，返回统一格式的 JSON
- 处理参数校验和异常
- 职责：请求路由、参数验证、响应封装

**Service 层（业务逻辑层）**
- 实现核心业务逻辑
- 处理事务控制
- 调用 Mapper 层进行数据操作
- 处理业务异常
- 职责：业务规则、事务管理、数据组装

**Mapper 层（数据访问层）**
- 执行数据库 CRUD 操作
- 编写 SQL 语句（XML 或注解）
- 映射查询结果到实体对象
- 职责：数据持久化、SQL 执行

### 1.3 前后端通信机制

**通信协议：** HTTP/HTTPS
**数据格式：** JSON
**API 风格：** RESTful
**认证方式：** JWT Token（存储在 HTTP Header 的 Authorization 字段）

**请求流程：**
1. 前端发起 HTTP 请求，携带 JWT Token（需要认证的接口）
2. 后端拦截器验证 Token 有效性和权限
3. Controller 接收请求，调用 Service 处理业务
4. Service 调用 Mapper 操作数据库
5. 数据层返回结果 → Service 处理 → Controller 封装 → 返回 JSON 响应
6. 前端接收响应，更新页面

---

## 2. 技术栈选型

### 2.1 后端技术栈

#### Spring Boot 4.0.3
**选择理由：**
- 约定大于配置，简化开发流程
- 内嵌 Tomcat，独立运行部署
- 丰富的 Starter 依赖，快速集成功能
- 成熟稳定，社区活跃，文档完善
- 适合快速构建企业级应用

**适用场景：** 核心框架，提供 Web、安全、数据访问等基础能力

#### MyBatis 3.0.3
**选择理由：**
- 半自动化 ORM，灵活控制 SQL
- 支持复杂查询和多表关联
- XML 和注解两种配置方式
- 性能优秀，适合复杂业务场景
- 相比 JPA 更适合需要 SQL 优化的场景

**适用场景：** 持久层框架，处理数据库操作

#### MySQL 8.x
**选择理由：**
- 成熟稳定的关系型数据库
- 完善的 ACID 特性，保证事务一致性
- 支持复杂查询和索引优化
- 开源免费，社区活跃
- 适合存储结构化数据（图书、订单、用户）

**适用场景：** 数据存储，保证数据完整性和一致性

#### Spring Security + JWT
**选择理由：**
- Spring Security 提供完善的安全框架
- JWT 无状态认证，适合前后端分离
- Token 存储用户信息和权限，减少数据库查询
- 支持跨域和分布式部署

**适用场景：** 用户认证、权限控制

#### Lombok
**选择理由：**
- 通过注解自动生成 getter/setter/构造器
- 减少样板代码，提高开发效率
- @Data、@Builder 等注解简化实体类编写

**适用场景：** 简化实体类、DTO、VO 的编写

#### Validation (Hibernate Validator)
**选择理由：**
- JSR-303 标准实现
- 声明式参数校验，代码简洁
- 支持自定义校验规则
- 与 Spring Boot 无缝集成

**适用场景：** 请求参数校验

#### Swagger/OpenAPI 3.0
**选择理由：**
- 自动生成 API 文档
- 提供在线测试界面
- 前后端协作更高效
- 支持导出 API 规范

**适用场景：** API 文档生成和测试

### 2.2 前端技术栈

#### Vue.js 3.x
**选择理由：**
- 渐进式框架，易于上手
- 响应式数据绑定，开发效率高
- 组件化开发，代码复用性强
- 虚拟 DOM，性能优秀
- 生态丰富，社区活跃

**适用场景：** 前端核心框架

#### Vue Router
**选择理由：**
- Vue 官方路由管理器
- 支持嵌套路由和动态路由
- 路由守卫实现权限控制
- 与 Vue 深度集成

**适用场景：** 页面路由管理

#### Axios
**选择理由：**
- 基于 Promise 的 HTTP 客户端
- 支持请求/响应拦截器
- 自动转换 JSON 数据
- 支持取消请求和超时设置

**适用场景：** 前后端 HTTP 通信

#### Element Plus
**选择理由：**
- Vue 3 的 UI 组件库
- 组件丰富，开箱即用
- 设计规范，界面美观
- 响应式布局，适配多端

**适用场景：** UI 组件库

---

## 3. 数据库设计

### 3.1 数据库 ER 图

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│    user     │         │  category   │         │    book     │
├─────────────┤         ├─────────────┤         ├─────────────┤
│ id (PK)     │         │ id (PK)     │    ┌───→│ id (PK)     │
│ username    │         │ category_   │    │    │ book_name   │
│ password    │         │   name      │    │    │ author      │
│ nickname    │         │ parent_id   │    │    │ publisher   │
│ phone       │         │ sort_order  │    │    │ isbn        │
│ email       │         │ status      │    │    │ category_id │←┐
│ role        │         │ create_time │    │    │ price       │ │
│ status      │         │ update_time │    │    │ stock       │ │
│ create_time │         └─────────────┘    │    │ cover_image │ │
│ update_time │                            │    │ description │ │
└──────┬──────┘                            │    │ status      │ │
       │                                   │    │ create_time │ │
       │ 1                                 │    │ update_time │ │
       │                                   │    └──────┬──────┘ │
       │ N                                 │           │        │
       ↓                                   │           │ 1      │
┌─────────────┐                            │           │        │
│   address   │                            │           │ N      │
├─────────────┤                            │           ↓        │
│ id (PK)     │                            │    ┌─────────────┐ │
│ user_id (FK)│                            │    │    cart     │ │
│ receiver_   │                            │    ├─────────────┤ │
│   name      │                            │    │ id (PK)     │ │
│ phone       │                            │    │ user_id (FK)│ │
│ province    │                            │    │ book_id (FK)│─┘
│ city        │                            │    │ quantity    │
│ district    │                            │    │ create_time │
│ detail_     │                            │    │ update_time │
│   address   │                            │    └─────────────┘
│ is_default  │                            │
│ create_time │                            │
│ update_time │                            │
└──────┬──────┘                            │
       │                                   │
       │ 1                                 │
       │                                   │
       │ N                                 │
       ↓                                   │
┌─────────────┐         ┌─────────────┐   │
│    order    │    1    │ order_item  │   │
├─────────────┤────────→├─────────────┤   │
│ id (PK)     │    N    │ id (PK)     │   │
│ order_no    │         │ order_id(FK)│   │
│ user_id (FK)│         │ book_id (FK)│───┘
│ total_amount│         │ book_name   │
│ status      │         │ price       │
│ address_id  │         │ quantity    │
│   (FK)      │         │ total_price │
│ create_time │         └─────────────┘
│ update_time │
│ pay_time    │         ┌─────────────┐
│ ship_time   │         │   banner    │
└─────────────┘         ├─────────────┤
                        │ id (PK)     │
                        │ image_url   │
                        │ link_url    │
                        │ sort_order  │
                        │ status      │
                        │ create_time │
                        │ update_time │
                        └─────────────┘
```

### 3.2 数据表详细设计

#### 表 1: user（用户表）

**表说明：** 存储系统用户信息，包括普通用户和管理员

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 用户 ID |
| username | VARCHAR(50) | UNIQUE NOT NULL | 用户名，4-20 字符 |
| password | VARCHAR(100) | NOT NULL | 密码（BCrypt 加密） |
| nickname | VARCHAR(50) | NULL | 昵称 |
| phone | VARCHAR(11) | UNIQUE | 手机号，11 位数字 |
| email | VARCHAR(100) | NULL | 邮箱 |
| role | TINYINT | DEFAULT 0 | 角色：0-普通用户，1-管理员 |
| status | TINYINT | DEFAULT 1 | 状态：0-禁用，1-启用 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE INDEX idx_username (username)
- UNIQUE INDEX idx_phone (phone)

**关系：**
- 一个用户可以有多个收货地址（1:N）
- 一个用户可以有多个购物车记录（1:N）
- 一个用户可以有多个订单（1:N）

#### 表 2: category（图书分类表）

**表说明：** 存储图书分类信息，支持二级分类

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 分类 ID |
| category_name | VARCHAR(50) | NOT NULL | 分类名称 |
| parent_id | BIGINT | DEFAULT 0 | 父分类 ID，0 表示顶级分类 |
| sort_order | INT | DEFAULT 0 | 排序值，越小越靠前 |
| status | TINYINT | DEFAULT 1 | 状态：0-禁用，1-启用 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_parent_id (parent_id)

**关系：**
- 一个分类可以有多个子分类（自关联 1:N）
- 一个分类可以有多本图书（1:N）

#### 表 3: book（图书表）

**表说明：** 存储图书商品信息

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 图书 ID |
| book_name | VARCHAR(200) | NOT NULL | 书名 |
| author | VARCHAR(100) | NULL | 作者 |
| publisher | VARCHAR(100) | NULL | 出版社 |
| isbn | VARCHAR(20) | NULL | ISBN 编号 |
| category_id | BIGINT | NOT NULL | 分类 ID，外键关联 category.id |
| price | DECIMAL(10,2) | NOT NULL | 价格，必须 > 0 |
| stock | INT | DEFAULT 0 | 库存数量，不能为负数 |
| cover_image | VARCHAR(500) | NULL | 封面图片 URL |
| description | TEXT | NULL | 图书简介 |
| status | TINYINT | DEFAULT 1 | 状态：0-下架，1-上架 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_category_id (category_id)
- INDEX idx_status (status)
- INDEX idx_book_name (book_name)
- INDEX idx_isbn (isbn)  # 普通索引，允许重复（不同版本可相同）

**关系：**
- 多本图书属于一个分类（N:1）
- 一本图书可以在多个购物车中（1:N）
- 一本图书可以在多个订单详情中（1:N）

#### 表 4: address（收货地址表）

**表说明：** 存储用户收货地址信息

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 地址 ID |
| user_id | BIGINT | NOT NULL | 用户 ID，外键关联 user.id |
| receiver_name | VARCHAR(50) | NOT NULL | 收货人姓名 |
| phone | VARCHAR(11) | NOT NULL | 收货人手机号 |
| province | VARCHAR(50) | NULL | 省份 |
| city | VARCHAR(50) | NULL | 城市 |
| district | VARCHAR(50) | NULL | 区县 |
| detail_address | VARCHAR(200) | NOT NULL | 详细地址 |
| is_default | TINYINT | DEFAULT 0 | 是否默认：0-否，1-是 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_user_id (user_id)

**关系：**
- 多个地址属于一个用户（N:1）
- 一个地址可以被多个订单使用（1:N）

#### 表 5: cart（购物车表）

**表说明：** 存储用户购物车信息

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 购物车 ID |
| user_id | BIGINT | NOT NULL | 用户 ID，外键关联 user.id |
| book_id | BIGINT | NOT NULL | 图书 ID，外键关联 book.id |
| quantity | INT | NOT NULL | 商品数量，必须 > 0 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE INDEX idx_user_book (user_id, book_id)

**关系：**
- 多个购物车记录属于一个用户（N:1）
- 多个购物车记录关联一本图书（N:1）

**业务规则：** 同一用户同一商品只能有一条购物车记录（通过唯一索引保证）

#### 表 6: order（订单表）

**表说明：** 存储订单主信息

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 订单 ID |
| order_no | VARCHAR(50) | UNIQUE NOT NULL | 订单编号 |
| user_id | BIGINT | NOT NULL | 用户 ID，外键关联 user.id |
| total_amount | DECIMAL(10,2) | NOT NULL | 订单总金额 |
| status | TINYINT | DEFAULT 0 | 订单状态：0-待支付，1-已支付，2-待发货，3-已发货，4-已完成，5-已取消 |
| address_id | BIGINT | NOT NULL | 收货地址 ID，外键关联 address.id |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| pay_time | DATETIME | NULL | 支付时间 |
| ship_time | DATETIME | NULL | 发货时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE INDEX idx_order_no (order_no)
- INDEX idx_user_id (user_id)
- INDEX idx_status (status)

**关系：**
- 多个订单属于一个用户（N:1）
- 多个订单使用一个地址（N:1）
- 一个订单有多个订单详情（1:N）

**订单状态流转：**
- 待支付(0) → 已支付(1) → 待发货(2) → 已发货(3) → 已完成(4)
- 待支付(0) → 已取消(5)

#### 表 7: order_item（订单详情表）

**表说明：** 存储订单商品明细

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 订单详情 ID |
| order_id | BIGINT | NOT NULL | 订单 ID，外键关联 order.id |
| book_id | BIGINT | NOT NULL | 图书 ID，外键关联 book.id |
| book_name | VARCHAR(200) | NOT NULL | 图书名称（冗余存储） |
| price | DECIMAL(10,2) | NOT NULL | 购买时的价格 |
| quantity | INT | NOT NULL | 购买数量 |
| total_price | DECIMAL(10,2) | NOT NULL | 小计金额 = price × quantity |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_order_id (order_id)

**关系：**
- 多个订单详情属于一个订单（N:1）
- 多个订单详情关联一本图书（N:1）

**设计说明：** book_name 冗余存储，防止图书信息修改后影响历史订单

#### 表 8: banner（轮播图表）

**表说明：** 存储首页轮播图信息

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 轮播图 ID |
| image_url | VARCHAR(500) | NOT NULL | 图片 URL |
| link_url | VARCHAR(500) | NULL | 跳转链接 |
| sort_order | INT | DEFAULT 0 | 排序值，越小越靠前 |
| status | TINYINT | DEFAULT 1 | 状态：0-禁用，1-启用 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_sort_order (sort_order)

**业务规则：** 前台最多显示 5 张轮播图

### 3.3 表间关系总结

**一对多关系：**
- user → address（一个用户有多个地址）
- user → cart（一个用户有多个购物车记录）
- user → order（一个用户有多个订单）
- category → book（一个分类有多本图书）
- category → category（一个分类有多个子分类，自关联）
- book → cart（一本图书在多个购物车中）
- book → order_item（一本图书在多个订单详情中）
- order → order_item（一个订单有多个订单详情）
- address → order（一个地址被多个订单使用）

---

## 4. RESTful API 接口设计

### 4.1 统一响应格式

**成功响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1678886400000
}
```

**错误响应：**
```json
{
  "code": 400,
  "message": "参数校验失败",
  "data": null,
  "timestamp": 1678886400000
}
```

**状态码规范：**
- 200：成功
- 400：请求参数错误
- 401：未登录或 Token 失效
- 403：无权限访问
- 404：资源不存在
- 500：服务器内部错误
- 1001：库存不足
- 1002：订单状态异常
- 1003：图书已下架

### 4.2 认证模块接口

#### 4.2.1 前台用户注册
- **路径：** POST /api/front/auth/register
- **认证：** 无需认证
- **请求体：**
```json
{
  "username": "string (4-20字符)",
  "password": "string (6-20字符)",
  "phone": "string (11位数字)",
  "email": "string (可选)"
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "testuser"
  }
}
```

#### 4.2.2 前台用户登录
- **路径：** POST /api/front/auth/login
- **认证：** 无需认证
- **请求体：**
```json
{
  "username": "string",
  "password": "string"
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "role": 0
    }
  }
}
```

#### 4.2.3 管理员登录
- **路径：** POST /api/admin/auth/login
- **认证：** 无需认证
- **请求体：** 同前台登录
- **响应：** 同前台登录（role 为 1）

### 4.3 用户模块接口

#### 4.3.1 获取用户信息
- **路径：** GET /api/front/user/info
- **认证：** 需要 Token
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com"
  }
}
```

#### 4.3.2 更新用户信息
- **路径：** PUT /api/front/user/info
- **认证：** 需要 Token
- **请求体：**
```json
{
  "nickname": "string",
  "phone": "string (11位)",
  "email": "string"
}
```

### 4.4 收货地址模块接口

#### 4.4.1 获取地址列表
- **路径：** GET /api/front/user/addresses
- **认证：** 需要 Token
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "receiverName": "张三",
      "phone": "13800138000",
      "province": "江苏省",
      "city": "南京市",
      "district": "栖霞区",
      "detailAddress": "仙林大道138号",
      "isDefault": 1
    }
  ]
}
```

#### 4.4.2 添加收货地址
- **路径：** POST /api/front/user/addresses
- **认证：** 需要 Token
- **请求体：**
```json
{
  "receiverName": "string",
  "phone": "string (11位)",
  "province": "string",
  "city": "string",
  "district": "string",
  "detailAddress": "string",
  "isDefault": 0
}
```

#### 4.4.3 更新收货地址
- **路径：** PUT /api/front/user/addresses/{id}
- **认证：** 需要 Token
- **路径参数：** id（地址 ID）
- **请求体：** 同添加地址

#### 4.4.4 删除收货地址
- **路径：** DELETE /api/front/user/addresses/{id}
- **认证：** 需要 Token
- **路径参数：** id（地址 ID）

### 4.5 图书模块接口（前台）

#### 4.5.1 分页查询图书列表
- **路径：** GET /api/front/books
- **认证：** 无需认证
- **查询参数：**
  - page: int（页码，默认 1）
  - pageSize: int（每页数量，默认 20，最大 100）
  - categoryId: long（分类 ID，可选）
  - keyword: string（搜索关键词，可选）
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "list": [
      {
        "id": 1,
        "bookName": "Java 编程思想",
        "author": "Bruce Eckel",
        "publisher": "机械工业出版社",
        "price": 108.00,
        "coverImage": "http://...",
        "categoryName": "计算机"
      }
    ]
  }
}
```

#### 4.5.2 查询图书详情
- **路径：** GET /api/front/books/{id}
- **认证：** 无需认证
- **路径参数：** id（图书 ID）
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "bookName": "Java 编程思想",
    "author": "Bruce Eckel",
    "publisher": "机械工业出版社",
    "isbn": "9787111213826",
    "price": 108.00,
    "stock": 50,
    "coverImage": "http://...",
    "description": "本书详细介绍了...",
    "categoryId": 1,
    "categoryName": "计算机",
    "status": 1
  }
}
```

#### 4.5.3 获取图书分类树
- **路径：** GET /api/front/categories
- **认证：** 无需认证
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "categoryName": "计算机",
      "children": [
        {
          "id": 2,
          "categoryName": "编程语言"
        }
      ]
    }
  ]
}
```

### 4.6 图书模块接口（后台）

#### 4.6.1 分页查询图书列表（后台）
- **路径：** GET /api/admin/books
- **认证：** 需要管理员 Token
- **查询参数：** 同前台，但返回所有状态的图书，`pageSize` 默认为 20，最大 100
- **说明：** 后台列表与前台使用一致的分页规则，方便管理端开发和调试

#### 4.6.2 添加图书
- **路径：** POST /api/admin/books
- **认证：** 需要管理员 Token
- **请求体：**
```json
{
  "bookName": "string",
  "author": "string",
  "publisher": "string",
  "isbn": "string",
  "categoryId": 1,
  "price": 108.00,
  "stock": 100,
  "coverImage": "string",
  "description": "string",
  "status": 1
}
```

#### 4.6.3 更新图书信息
- **路径：** PUT /api/admin/books/{id}
- **认证：** 需要管理员 Token
- **路径参数：** id（图书 ID）
- **请求体：** 同添加图书

#### 4.6.4 删除图书
- **路径：** DELETE /api/admin/books/{id}
- **认证：** 需要管理员 Token
- **路径参数：** id（图书 ID）
- **说明：** 默认执行软删除（status = -1）；仅在确认未被订单引用时允许物理删除

#### 4.6.5 上架/下架图书
- **路径：** PUT /api/admin/books/{id}/status
- **认证：** 需要管理员 Token
- **路径参数：** id（图书 ID）
- **请求体：**
```json
{
  "status": 1
}
```

### 4.7 分类模块接口（后台）

#### 4.7.1 获取分类列表
- **路径：** GET /api/admin/categories
- **认证：** 需要管理员 Token

#### 4.7.2 添加分类
- **路径：** POST /api/admin/categories
- **认证：** 需要管理员 Token
- **请求体：**
```json
{
  "categoryName": "string",
  "parentId": 0,
  "sortOrder": 0
}
```

#### 4.7.3 更新分类
- **路径：** PUT /api/admin/categories/{id}
- **认证：** 需要管理员 Token

#### 4.7.4 删除分类
- **路径：** DELETE /api/admin/categories/{id}
- **认证：** 需要管理员 Token

#### 4.7.5 启用/禁用分类
- **路径：** PUT /api/admin/categories/{id}/status
- **认证：** 需要管理员 Token
- **路径参数：** id（分类 ID）
- **请求体：**
```json
{
  "status": 1
}
```

### 4.8 购物车模块接口

#### 4.8.1 获取购物车列表
- **路径：** GET /api/front/cart
- **认证：** 需要 Token
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "bookId": 1,
      "bookName": "Java 编程思想",
      "price": 108.00,
      "quantity": 2,
      "stock": 50,
      "coverImage": "http://...",
      "totalPrice": 216.00
    }
  ]
}
```

#### 4.8.2 添加商品到购物车
- **路径：** POST /api/front/cart
- **认证：** 需要 Token
- **请求体：**
```json
{
  "bookId": 1,
  "quantity": 1
}
```

#### 4.8.3 更新购物车商品数量
- **路径：** PUT /api/front/cart/{id}
- **认证：** 需要 Token
- **路径参数：** id（购物车 ID）
- **请求体：**
```json
{
  "quantity": 3
}
```

#### 4.8.4 删除购物车商品
- **路径：** DELETE /api/front/cart/{id}
- **认证：** 需要 Token
- **路径参数：** id（购物车 ID）

### 4.9 订单模块接口（前台）

#### 4.9.1 创建订单
- **路径：** POST /api/front/orders
- **认证：** 需要 Token
- **请求体：**
```json
{
  "cartIds": [1, 2, 3],
  "addressId": 1
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "orderId": 1,
    "orderNo": "20240306123456789001",
    "totalAmount": 216.00
  }
}
```

#### 4.9.2 查询订单列表
- **路径：** GET /api/front/orders
- **认证：** 需要 Token
- **查询参数：**
  - page: int（页码，默认 1）
  - pageSize: int（每页数量，默认 10）
  - status: int（订单状态，可选）
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "list": [
      {
        "id": 1,
        "orderNo": "20240306123456789001",
        "totalAmount": 216.00,
        "status": 1,
        "createTime": "2024-03-06 12:34:56"
      }
    ]
  }
}
```

#### 4.9.3 查询订单详情
- **路径：** GET /api/front/orders/{id}
- **认证：** 需要 Token
- **路径参数：** id（订单 ID）
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "20240306123456789001",
    "totalAmount": 216.00,
    "status": 1,
    "createTime": "2024-03-06 12:34:56",
    "payTime": "2024-03-06 12:35:00",
    "address": {
      "receiverName": "张三",
      "phone": "13800138000",
      "fullAddress": "江苏省南京市栖霞区仙林大道138号"
    },
    "items": [
      {
        "bookId": 1,
        "bookName": "Java 编程思想",
        "price": 108.00,
        "quantity": 2,
        "totalPrice": 216.00
      }
    ]
  }
}
```

#### 4.9.4 支付订单
- **路径：** POST /api/front/orders/{id}/pay
- **认证：** 需要 Token
- **路径参数：** id（订单 ID）
- **说明：** 模拟支付，直接更新订单状态

#### 4.9.5 取消订单
- **路径：** POST /api/front/orders/{id}/cancel
- **认证：** 需要 Token
- **路径参数：** id（订单 ID）

### 4.10 订单模块接口（后台）

#### 4.10.1 查询订单列表（后台）
- **路径：** GET /api/admin/orders
- **认证：** 需要管理员 Token
- **查询参数：**
  - page, pageSize, status（同前台，pageSize 默认 10，最大 100）
  - orderNo: string（订单号，可选）
  - userId: long（用户 ID，可选）

#### 4.10.2 查询订单详情（后台）
- **路径：** GET /api/admin/orders/{id}
- **认证：** 需要管理员 Token

#### 4.10.3 订单发货
- **路径：** PUT /api/admin/orders/{id}/ship
- **认证：** 需要管理员 Token
- **路径参数：** id（订单 ID）

### 4.11 用户管理模块接口（后台）

#### 4.11.1 分页查询用户列表
- **路径：** GET /api/admin/users
- **认证：** 需要管理员 Token
- **查询参数：**
  - page, pageSize
  - keyword: string（用户名或手机号，可选）

#### 4.11.2 查询用户详情
- **路径：** GET /api/admin/users/{id}
- **认证：** 需要管理员 Token

#### 4.11.3 启用/禁用用户
- **路径：** PUT /api/admin/users/{id}/status
- **认证：** 需要管理员 Token
- **请求体：**
```json
{
  "status": 1
}
```

### 4.12 轮播图模块接口

#### 4.12.1 获取轮播图列表（前台）
- **路径：** GET /api/front/banners
- **认证：** 无需认证
- **说明：** 只返回启用状态的前 5 张轮播图，按 `sort_order` 升序排序；查询时使用 `LIMIT 5` 限制返回数量
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "imageUrl": "http://...",
      "linkUrl": "http://..."
    }
  ]
}
```

#### 4.12.2 获取轮播图列表（后台）
- **路径：** GET /api/admin/banners
- **认证：** 需要管理员 Token

#### 4.12.3 添加轮播图
- **路径：** POST /api/admin/banners
- **认证：** 需要管理员 Token
- **请求体：**
```json
{
  "imageUrl": "string",
  "linkUrl": "string",
  "sortOrder": 0
}
```

#### 4.12.4 更新轮播图
- **路径：** PUT /api/admin/banners/{id}
- **认证：** 需要管理员 Token

#### 4.12.5 删除轮播图
- **路径：** DELETE /api/admin/banners/{id}
- **认证：** 需要管理员 Token

#### 4.12.6 调整轮播图排序
- **路径：** PUT /api/admin/banners/{id}/sort
- **认证：** 需要管理员 Token
- **请求体：**
```json
{
  "sortOrder": 1
}
```

---

## 5. 项目目录结构

### 5.1 后端项目结构

```
book-mall-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── top/wjc/bookmallbackend/
│   │   │       ├── BookMallBackendApplication.java
│   │   │       │
│   │   │       ├── controller/              # 控制器层
│   │   │       │   ├── admin/               # 后台管理接口
│   │   │       │   │   ├── AdminAuthController.java
│   │   │       │   │   ├── AdminBookController.java
│   │   │       │   │   ├── AdminCategoryController.java
│   │   │       │   │   ├── AdminOrderController.java
│   │   │       │   │   ├── AdminUserController.java
│   │   │       │   │   └── AdminBannerController.java
│   │   │       │   │
│   │   │       │   └── front/               # 前台用户接口
│   │   │       │       ├── FrontAuthController.java
│   │   │       │       ├── FrontUserController.java
│   │   │       │       ├── FrontBookController.java
│   │   │       │       ├── FrontCartController.java
│   │   │       │       ├── FrontOrderController.java
│   │   │       │       └── FrontBannerController.java
│   │   │       │
│   │   │       ├── service/                 # 业务逻辑层
│   │   │       │   ├── UserService.java
│   │   │       │   ├── BookService.java
│   │   │       │   ├── CategoryService.java
│   │   │       │   ├── CartService.java
│   │   │       │   ├── OrderService.java
│   │   │       │   ├── AddressService.java
│   │   │       │   ├── BannerService.java
│   │   │       │   │
│   │   │       │   └── impl/                # 服务实现类
│   │   │       │       ├── UserServiceImpl.java
│   │   │       │       ├── BookServiceImpl.java
│   │   │       │       ├── CategoryServiceImpl.java
│   │   │       │       ├── CartServiceImpl.java
│   │   │       │       ├── OrderServiceImpl.java
│   │   │       │       ├── AddressServiceImpl.java
│   │   │       │       └── BannerServiceImpl.java
│   │   │       │
│   │   │       ├── mapper/                  # 数据访问层
│   │   │       │   ├── UserMapper.java
│   │   │       │   ├── BookMapper.java
│   │   │       │   ├── CategoryMapper.java
│   │   │       │   ├── CartMapper.java
│   │   │       │   ├── OrderMapper.java
│   │   │       │   ├── OrderItemMapper.java
│   │   │       │   ├── AddressMapper.java
│   │   │       │   └── BannerMapper.java
│   │   │       │
│   │   │       ├── entity/                  # 实体类
│   │   │       │   ├── User.java
│   │   │       │   ├── Book.java
│   │   │       │   ├── Category.java
│   │   │       │   ├── Cart.java
│   │   │       │   ├── Order.java
│   │   │       │   ├── OrderItem.java
│   │   │       │   ├── Address.java
│   │   │       │   └── Banner.java
│   │   │       │
│   │   │       ├── dto/                     # 数据传输对象
│   │   │       │   ├── LoginDTO.java
│   │   │       │   ├── RegisterDTO.java
│   │   │       │   ├── BookDTO.java
│   │   │       │   ├── CartDTO.java
│   │   │       │   ├── OrderDTO.java
│   │   │       │   └── AddressDTO.java
│   │   │       │
│   │   │       ├── vo/                      # 视图对象
│   │   │       │   ├── UserVO.java
│   │   │       │   ├── BookVO.java
│   │   │       │   ├── CartVO.java
│   │   │       │   ├── OrderVO.java
│   │   │       │   └── OrderDetailVO.java
│   │   │       │
│   │   │       ├── config/                  # 配置类
│   │   │       │   ├── WebConfig.java       # Web 配置（跨域）
│   │   │       │   ├── SecurityConfig.java  # 安全配置
│   │   │       │   ├── MyBatisConfig.java   # MyBatis 配置
│   │   │       │   └── SwaggerConfig.java   # Swagger 配置
│   │   │       │
│   │   │       ├── interceptor/             # 拦截器
│   │   │       │   ├── JwtInterceptor.java  # JWT 认证拦截器
│   │   │       │   └── AdminInterceptor.java # 管理员权限拦截器
│   │   │       │
│   │   │       ├── exception/               # 异常处理
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── BusinessException.java
│   │   │       │   ├── InsufficientStockException.java
│   │   │       │   ├── OrderNotFoundException.java
│   │   │       │   ├── UnauthorizedException.java
│   │   │       │   └── InvalidOrderStatusException.java
│   │   │       │
│   │   │       ├── util/                    # 工具类
│   │   │       │   ├── JwtUtil.java         # JWT 工具类
│   │   │       │   ├── PasswordUtil.java    # 密码加密工具
│   │   │       │   └── OrderNoUtil.java     # 订单号生成工具
│   │   │       │
│   │   │       ├── constant/                # 常量类
│   │   │       │   ├── UserRole.java        # 用户角色枚举
│   │   │       │   ├── OrderStatus.java     # 订单状态枚举
│   │   │       │   ├── BookStatus.java      # 图书状态枚举
│   │   │       │   └── CommonStatus.java    # 通用状态枚举
│   │   │       │
│   │   │       └── common/                  # 通用类
│   │   │           ├── Result.java          # 统一响应格式
│   │   │           └── PageResult.java      # 分页响应格式
│   │   │
│   │   └── resources/
│   │       ├── application.properties       # 应用配置
│   │       ├── mapper/                      # MyBatis XML 映射文件
│   │       │   ├── UserMapper.xml
│   │       │   ├── BookMapper.xml
│   │       │   ├── OrderMapper.xml
│   │       │   └── ...
│   │       └── sql/                         # SQL 脚本
│   │           └── schema.sql               # 数据库建表脚本
│   │
│   └── test/                                # 测试代码
│       └── java/
│           └── top/wjc/bookmallbackend/
│               └── ...
│
├── pom.xml                                  # Maven 配置
└── README.md                                # 项目说明
```

### 5.2 前端项目结构（参考）

```
book-mall-frontend/
├── src/
│   ├── main.js                    # 入口文件
│   ├── App.vue                    # 根组件
│   │
│   ├── views/                     # 页面组件
│   │   ├── front/                 # 前台页面
│   │   │   ├── Home.vue           # 首页
│   │   │   ├── BookList.vue       # 图书列表
│   │   │   ├── BookDetail.vue     # 图书详情
│   │   │   ├── Cart.vue           # 购物车
│   │   │   ├── Order.vue          # 订单列表
│   │   │   ├── OrderDetail.vue    # 订单详情
│   │   │   ├── Login.vue          # 登录
│   │   │   ├── Register.vue       # 注册
│   │   │   └── UserCenter.vue     # 用户中心
│   │   │
│   │   └── admin/                 # 后台页面
│   │       ├── Dashboard.vue      # 仪表盘
│   │       ├── BookManage.vue     # 图书管理
│   │       ├── OrderManage.vue    # 订单管理
│   │       ├── UserManage.vue     # 用户管理
│   │       └── BannerManage.vue   # 轮播图管理
│   │
│   ├── components/                # 公共组件
│   │   ├── Header.vue
│   │   ├── Footer.vue
│   │   └── ...
│   │
│   ├── router/                    # 路由配置
│   │   └── index.js
│   │
│   ├── api/                       # API 接口封装
│   │   ├── auth.js
│   │   ├── book.js
│   │   ├── cart.js
│   │   ├── order.js
│   │   └── ...
│   │
│   ├── utils/                     # 工具函数
│   │   ├── request.js             # Axios 封装
│   │   └── auth.js                # Token 管理
│   │
│   └── assets/                    # 静态资源
│       ├── images/
│       └── styles/
│
├── package.json
└── vite.config.js
```

---

## 6. 核心业务流程设计

### 6.1 用户注册登录流程

```
用户注册流程：
1. 用户填写注册信息（用户名、密码、手机号）
2. 前端校验：用户名长度、密码强度、手机号格式
3. 发送 POST /api/front/auth/register 请求
4. 后端校验：
   - 用户名唯一性
   - 手机号唯一性
   - 参数格式有效性
5. 密码使用 BCrypt 加密
6. 插入用户记录到 user 表
7. 返回注册成功响应

用户登录流程：
1. 用户输入用户名和密码
2. 发送 POST /api/front/auth/login 请求
3. 后端验证：
   - 查询用户是否存在
   - 验证密码是否正确（BCrypt.matches）
   - 检查用户状态是否启用
4. 生成 JWT Token（包含用户 ID、用户名、角色）
5. 返回 Token 和用户信息
6. 前端存储 Token（localStorage）
7. 后续请求在 Header 中携带 Token
```

### 6.2 图书浏览检索流程

```
图书列表查询流程：
1. 用户访问图书列表页面
2. 发送 GET /api/front/books 请求
   - 参数：page, pageSize, categoryId, keyword
3. 后端处理：
   - 构建查询条件（仅查询上架图书）
   - 按分类筛选（如果有 categoryId）
   - 按关键词模糊搜索（书名、作者）
   - 分页查询（LIMIT offset, pageSize）
4. 关联查询分类名称
5. 返回分页结果（total, list）
6. 前端渲染图书列表

图书详情查询流程：
1. 用户点击图书卡片
2. 发送 GET /api/front/books/{id} 请求
3. 后端查询图书详细信息
4. 关联查询分类信息
5. 返回完整图书数据
6. 前端渲染详情页
```

### 6.3 购物车管理流程

```
添加商品到购物车：
1. 用户点击"加入购物车"按钮
2. 发送 POST /api/front/cart 请求
   - 参数：bookId, quantity
3. 后端处理：
   - 验证用户登录状态（JWT Token）
   - 验证图书是否上架
   - 验证库存是否充足
   - 查询购物车是否已有该商品
   - 如果存在：更新数量（累加）
   - 如果不存在：插入新记录
4. 返回成功响应
5. 前端更新购物车图标数量

查询购物车列表：
1. 用户访问购物车页面
2. 发送 GET /api/front/cart 请求
3. 后端处理：
   - 根据用户 ID 查询购物车记录
   - 关联查询图书信息（书名、价格、库存、封面）
   - 计算每项小计（price × quantity）
4. 返回购物车列表
5. 前端渲染购物车，计算总金额
```

### 6.4 订单创建流程（核心事务）

```
订单创建流程（事务控制）：
1. 用户选择购物车商品，点击"结算"
2. 选择收货地址
3. 发送 POST /api/front/orders 请求
   - 参数：cartIds, addressId

4. 后端处理（@Transactional）：
   
   Step 1: 参数校验
   - 验证 cartIds 不为空
   - 验证 addressId 有效且属于当前用户
   
   Step 2: 查询购物车商品
   - 根据 cartIds 查询购物车记录
   - 关联查询图书信息
   
   Step 3: 库存校验
   - 遍历每个商品
   - 检查图书是否上架
   - 检查库存是否充足（stock >= quantity）
   - 如果任一商品库存不足，抛出 InsufficientStockException
   
   Step 4: 生成订单编号
   - 格式：时间戳(14位) + 用户ID(4位) + 随机数(4位)
   - 示例：20240306123456789001
   
   Step 5: 计算订单总金额
   - totalAmount = Σ(book.price × cart.quantity)
   
   Step 6: 创建订单主记录
   - 插入 order 表
   - 字段：order_no, user_id, total_amount, status(0), address_id
   
   Step 7: 创建订单详情记录
   - 遍历购物车商品
   - 插入 order_item 表
   - 字段：order_id, book_id, book_name, price, quantity, total_price
   
   Step 8: 清空购物车
   - 删除已下单的购物车记录

   Step 9: 提交事务
   - 如果任何步骤失败，回滚所有操作
```

### 6.5 订单支付流程

```
订单支付流程（模拟支付）：
1. 用户在订单详情页点击"支付"
2. 发送 POST /api/front/orders/{id}/pay 请求
3. 后端处理（@Transactional）：
   - 查询订单信息
   - 验证订单归属权限（order.userId == 当前用户ID）
   - 验证订单状态为待支付（status == 0）
   - 再次校验库存是否充足
   - 使用乐观锁扣减库存：UPDATE book SET stock = stock - ? WHERE id = ? AND stock >= ?
   - 库存不足则返回错误，订单保持待支付
   - 更新订单状态为已支付（status = 1）并记录支付时间
   - 立即更新订单状态为待发货（status = 2）
4. 返回支付成功响应
5. 前端更新订单状态显示

**说明**：库存扣减在支付时执行，支付成功后在同一事务中完成两次状态更新（已支付→待发货）。
```

### 6.6 订单发货流程

```
订单发货流程（后台管理）：
1. 管理员在订单管理页面点击"发货"
2. 发送 PUT /api/admin/orders/{id}/ship 请求
3. 后端处理：
   - 验证管理员权限
   - 查询订单信息
   - 验证订单状态为待发货（status == 2）
   - 如果状态不对，抛出 InvalidOrderStatusException
   - 更新订单状态为已发货（status = 3）
   - 记录发货时间（ship_time = NOW()）
4. 返回发货成功响应
5. 前端更新订单状态
6. 用户在前台可以看到订单状态变更

**说明**：管理员只能对"待发货"状态的订单执行发货操作，订单支付成功后会自动变为"待发货"状态。
```

---

## 7. 安全设计

### 7.1 JWT Token 认证机制

**Token 生成：**
```java
// 用户登录成功后生成 Token
String token = JWT.create()
    .withClaim("userId", user.getId())
    .withClaim("username", user.getUsername())
    .withClaim("role", user.getRole())
    .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24小时
    .sign(Algorithm.HMAC256(SECRET_KEY));
```

**Token 验证：**
```java
// JwtInterceptor 拦截器
public boolean preHandle(HttpServletRequest request, ...) {
    String token = request.getHeader("Authorization");
    if (token == null || !token.startsWith("Bearer ")) {
        throw new UnauthorizedException("未登录");
    }
    
    try {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(SECRET_KEY))
            .build()
            .verify(token.substring(7));
        
        Long userId = jwt.getClaim("userId").asLong();
        Integer role = jwt.getClaim("role").asInt();
        
        // 将用户信息存入 ThreadLocal 或 Request Attribute
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);
        
        return true;
    } catch (JWTVerificationException e) {
        throw new UnauthorizedException("Token 无效或已过期");
    }
}
```

**Token 存储（前端）：**
```javascript
// 登录成功后存储 Token
localStorage.setItem('token', response.data.token);

// Axios 请求拦截器自动添加 Token
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
```

### 7.2 密码加密方案

**注册时加密：**
```java
// 使用 BCrypt 加密密码
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
user.setPassword(hashedPassword);
userMapper.insert(user);
```

**登录时验证：**
```java
// 查询用户
User user = userMapper.selectByUsername(username);
if (user == null) {
    throw new BusinessException("用户不存在");
}

// 验证密码
if (!BCrypt.checkpw(plainPassword, user.getPassword())) {
    throw new BusinessException("密码错误");
}
```

**安全特性：**
- BCrypt 自动加盐，每次加密结果不同
- 单向加密，无法解密
- 计算成本高，防止暴力破解

### 7.3 权限控制方案

**拦截器配置：**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private JwtInterceptor jwtInterceptor;
    
    @Autowired
    private AdminInterceptor adminInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 前台用户接口需要登录
        registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/api/front/user/**")
            .addPathPatterns("/api/front/cart/**")
            .addPathPatterns("/api/front/orders/**")
            .excludePathPatterns("/api/front/auth/**");
        
        // 后台管理接口需要管理员权限
        registry.addInterceptor(adminInterceptor)
            .addPathPatterns("/api/admin/**")
            .excludePathPatterns("/api/admin/auth/login");
    }
}
```

**管理员权限拦截器：**
```java
public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            throw new UnauthorizedException("无权限访问");
        }
        return true;
    }
}
```

### 7.4 跨域配置

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:8081")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

### 7.5 SQL 注入防护

**使用 MyBatis 参数化查询：**
```xml
<!-- 正确：使用 #{} 参数化查询 -->
<select id="selectByUsername" resultType="User">
    SELECT * FROM user WHERE username = #{username}
</select>

<!-- 错误：使用 ${} 字符串拼接（存在 SQL 注入风险） -->
<select id="selectByUsername" resultType="User">
    SELECT * FROM user WHERE username = '${username}'
</select>
```

**动态表名/字段名处理：**
```java
// 使用白名单验证
private static final Set<String> ALLOWED_SORT_FIELDS = 
    Set.of("create_time", "price", "stock");

public List<Book> selectBooks(String sortField) {
    if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
        throw new BusinessException("非法的排序字段");
    }
    // 使用 ${} 时必须先验证
    return bookMapper.selectBooks(sortField);
}
```

### 7.6 XSS 防护

**输入过滤：**
```java
// 对用户输入的文本内容进行 HTML 转义
public String escapeHtml(String input) {
    if (input == null) return null;
    return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
}
```

**响应头设置：**
```java
// 设置 Content-Type
response.setContentType("application/json;charset=UTF-8");

// 设置 X-Content-Type-Options
response.setHeader("X-Content-Type-Options", "nosniff");
```

---

## 8. 性能优化设计

### 8.1 数据库索引设计

**索引创建 SQL：**
```sql
-- user 表索引
CREATE UNIQUE INDEX idx_username ON user(username);
CREATE UNIQUE INDEX idx_phone ON user(phone);

-- book 表索引
CREATE INDEX idx_category_id ON book(category_id);
CREATE INDEX idx_status ON book(status);
CREATE INDEX idx_book_name ON book(book_name);

-- cart 表索引
CREATE UNIQUE INDEX idx_user_book ON cart(user_id, book_id);

-- order 表索引
CREATE UNIQUE INDEX idx_order_no ON `order`(order_no);
CREATE INDEX idx_user_id ON `order`(user_id);
CREATE INDEX idx_status ON `order`(status);

-- order_item 表索引
CREATE INDEX idx_order_id ON order_item(order_id);

-- address 表索引
CREATE INDEX idx_user_id ON address(user_id);

-- banner 表索引
CREATE INDEX idx_sort_order ON banner(sort_order);
```

**索引使用原则：**
- 主键自动创建聚簇索引
- 外键字段创建索引（提升关联查询性能）
- WHERE 条件常用字段创建索引
- 唯一约束字段创建唯一索引
- 避免过多索引（影响写入性能）

### 8.2 分页查询方案

**MyBatis 分页查询：**
```xml
<select id="selectBooksByPage" resultType="Book">
    SELECT b.*, c.category_name
    FROM book b
    LEFT JOIN category c ON b.category_id = c.id
    WHERE b.status = 1
    <if test="categoryId != null">
        AND b.category_id = #{categoryId}
    </if>
    <if test="keyword != null and keyword != ''">
        AND (b.book_name LIKE CONCAT('%', #{keyword}, '%')
             OR b.author LIKE CONCAT('%', #{keyword}, '%'))
    </if>
    ORDER BY b.create_time DESC
    LIMIT #{offset}, #{pageSize}
</select>

<select id="countBooks" resultType="long">
    SELECT COUNT(*)
    FROM book b
    WHERE b.status = 1
    <if test="categoryId != null">
        AND b.category_id = #{categoryId}
    </if>
    <if test="keyword != null and keyword != ''">
        AND (b.book_name LIKE CONCAT('%', #{keyword}, '%')
             OR b.author LIKE CONCAT('%', #{keyword}, '%'))
    </if>
</select>
```

**分页参数限制：**
```java
public PageResult<Book> getBooksByPage(int page, int pageSize) {
    // 限制页码和每页数量
    if (page < 1) page = 1;
    if (pageSize < 1) pageSize = 20;
    if (pageSize > 100) pageSize = 100;

    int offset = (page - 1) * pageSize;

    List<Book> list = bookMapper.selectBooksByPage(offset, pageSize, ...);
    long total = bookMapper.countBooks(...);

    return new PageResult<>(total, list);
}

public PageResult<Order> getOrdersByPage(int page, int pageSize) {
    // 限制页码和每页数量
    if (page < 1) page = 1;
    if (pageSize < 1) pageSize = 10;
    if (pageSize > 100) pageSize = 100;

    int offset = (page - 1) * pageSize;

    List<Order> list = orderMapper.selectOrdersByPage(offset, pageSize, ...);
    long total = orderMapper.countOrders(...);

    return new PageResult<>(total, list);
}
```

### 8.3 缓存策略（可选）

**适合缓存的数据：**
- 图书分类列表（变动频率低）
- 首页轮播图（变动频率低）
- 热门图书列表（定时更新）

**缓存实现方案：**
```java
// 使用 Spring Cache + Redis
@Cacheable(value = "categories", key = "'all'")
public List<Category> getAllCategories() {
    return categoryMapper.selectAll();
}

@CacheEvict(value = "categories", allEntries = true)
public void updateCategory(Category category) {
    categoryMapper.update(category);
}
```

### 8.4 事务控制策略

**事务注解使用：**
```java
@Service
public class OrderServiceImpl implements OrderService {
    
    // 订单创建需要事务控制
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(OrderDTO orderDTO) {
        // 1. 校验库存
        // 2. 创建订单
        // 3. 创建订单详情
        // 4. 清空购物车
        // 任何步骤失败都会回滚
    }
    
    // 订单取消需要事务控制
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        // 1. 更新订单状态
        // 2. 若订单已支付，则恢复库存
    }
    
    // 查询操作不需要事务
    public Order getOrderById(Long orderId) {
        return orderMapper.selectById(orderId);
    }
}
```

**事务隔离级别：**
- 使用默认隔离级别（READ_COMMITTED）
- 避免脏读、不可重复读
- 关键操作使用乐观锁或悲观锁

**乐观锁示例（库存扣减）：**
```xml
<update id="decreaseStock">
    UPDATE book
    SET stock = stock - #{quantity}
    WHERE id = #{bookId}
      AND stock >= #{quantity}
</update>
```

```java
int rows = bookMapper.decreaseStock(bookId, quantity);
if (rows == 0) {
    throw new InsufficientStockException("库存不足");
}
```

### 8.5 性能监控指标

**关键性能指标：**
- 列表查询响应时间 < 500ms
- 详情查询响应时间 < 200ms
- 订单创建响应时间 < 1s
- 数据库连接池使用率 < 80%
- 接口成功率 > 99%

**性能优化建议：**
1. 避免 SELECT *，只查询需要的字段
2. 避免 N+1 查询，使用 JOIN 或批量查询
3. 合理使用索引，定期分析慢查询
4. 大数据量操作使用分页或批处理
5. 静态资源使用 CDN 加速
6. 数据库读写分离（可选）
7. 使用缓存减少数据库压力（可选）

---

## 9. 部署架构（可选）

### 9.1 开发环境
- 前端：本地开发服务器（Vite Dev Server，端口 3000）
- 后端：本地 Spring Boot 应用（端口 8080）
- 数据库：本地 MySQL（端口 3306）

### 9.2 生产环境（建议）
```
┌─────────────┐
│   用户浏览器  │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│   Nginx     │ (反向代理 + 静态资源)
└──────┬──────┘
       │
       ├─────→ 前端静态文件 (Vue.js 打包后)
       │
       └─────→ 后端 API (Spring Boot)
                    │
                    ↓
              ┌─────────────┐
              │   MySQL     │
              └─────────────┘
```

---

## 10. 总结

本设计文档详细描述了图书商城管理系统的技术架构、数据库设计、API 接口、项目结构、业务流程、安全设计和性能优化方案。

**核心技术特点：**
1. 前后端分离架构，职责清晰
2. RESTful API 设计，接口规范统一
3. JWT Token 认证，无状态安全
4. MyBatis 持久层，灵活高效
5. 事务控制严格，数据一致性强
6. 索引优化完善，查询性能优秀
7. 异常处理统一，错误信息清晰

**开发建议：**
1. 按模块渐进式开发，先实现核心功能
2. 每个功能完成后进行单元测试
3. 使用 Swagger 进行接口测试
4. 定期进行代码审查和重构
5. 关注性能指标，及时优化

本设计文档将作为后续开发的技术指导，确保系统实现符合需求规范和技术标准。
