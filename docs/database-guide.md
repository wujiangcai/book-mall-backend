# 数据库开发文档 - 图书商城管理系统

## 文档信息

- **项目名称**：图书商城管理系统
- **数据库名称**：book_mall
- **数据库版本**：MySQL 8.0+
- **字符集**：utf8mb4
- **存储引擎**：InnoDB
- **文档版本**：v1.0
- **最后更新**：2024-03-06

---

## 目录

1. [数据库概述](#1-数据库概述)
2. [环境要求](#2-环境要求)
3. [快速开始](#3-快速开始)
4. [数据表详细说明](#4-数据表详细说明)
5. [索引设计说明](#5-索引设计说明)
6. [外键约束说明](#6-外键约束说明)
7. [初始化数据说明](#7-初始化数据说明)
8. [常用查询示例](#8-常用查询示例)
9. [数据库维护](#9-数据库维护)
10. [常见问题](#10-常见问题)
11. [变更记录](#11-变更记录)

---

## 1. 数据库概述

### 1.1 数据库架构

图书商城管理系统数据库包含 8 张核心业务表，采用关系型数据库设计，支持完整的电商业务流程。

**数据表清单：**

| 序号 | 表名 | 中文名称 | 说明 |
|------|------|---------|------|
| 1 | user | 用户表 | 存储用户账户信息（普通用户和管理员） |
| 2 | category | 图书分类表 | 存储图书分类信息，支持二级分类 |
| 3 | book | 图书表 | 存储图书商品信息 |
| 4 | address | 收货地址表 | 存储用户收货地址信息 |
| 5 | cart | 购物车表 | 存储用户购物车数据 |
| 6 | order | 订单表 | 存储订单主信息 |
| 7 | order_item | 订单详情表 | 存储订单商品明细 |
| 8 | banner | 轮播图表 | 存储首页轮播图信息 |

### 1.2 表间关系

```
user (1) ──────→ (N) address      # 一个用户有多个收货地址
user (1) ──────→ (N) cart         # 一个用户有多个购物车记录
user (1) ──────→ (N) order        # 一个用户有多个订单

category (1) ──→ (N) book         # 一个分类有多本图书
category (1) ──→ (N) category     # 一个分类有多个子分类（自关联）

book (1) ──────→ (N) cart         # 一本图书在多个购物车中
book (1) ──────→ (N) order_item   # 一本图书在多个订单详情中

order (1) ─────→ (N) order_item   # 一个订单有多个订单详情
address (1) ───→ (N) order        # 一个地址被多个订单使用
```

### 1.3 技术特性

- **事务支持**：InnoDB 引擎，支持 ACID 事务
- **外键约束**：保证数据完整性和一致性
- **索引优化**：为常用查询字段添加索引
- **字符集**：utf8mb4，支持 emoji 等特殊字符
- **自动时间戳**：create_time 和 update_time 自动维护

---

## 2. 环境要求

### 2.1 软件要求

- **MySQL 版本**：8.0 或更高版本
- **推荐配置**：
  - 内存：至少 2GB
  - 磁盘空间：至少 10GB
  - 操作系统：Windows/Linux/macOS

### 2.2 字符集配置

确保 MySQL 配置文件（my.cnf 或 my.ini）包含以下设置：

```ini
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

[client]
default-character-set=utf8mb4
```

### 2.3 权限要求

执行建表脚本的用户需要以下权限：

```sql
GRANT CREATE, ALTER, DROP, INDEX, REFERENCES ON book_mall.* TO 'your_user'@'localhost';
```

---

## 3. 快速开始

### 3.1 创建数据库

**方法 1：使用命令行**

```bash
# 登录 MySQL
mysql -u root -p

# 执行建表脚本
mysql> source /path/to/src/main/resources/sql/schema.sql;

# 或者直接执行
mysql -u root -p < src/main/resources/sql/schema.sql
```

**方法 2：使用数据库管理工具**

1. 打开 Navicat / DBeaver / MySQL Workbench
2. 连接到 MySQL 服务器
3. 打开 SQL 文件：`src/main/resources/sql/schema.sql`
4. 执行脚本

### 3.2 验证安装

```sql
-- 切换到数据库
USE book_mall;

-- 查看所有表
SHOW TABLES;

-- 查看表结构
DESC user;
DESC book;
DESC `order`;

-- 查看初始化数据
SELECT * FROM user;
SELECT * FROM category;
SELECT * FROM book;
```

### 3.3 预期结果

执行成功后应该看到：
- 8 张数据表创建成功
- 1 个管理员账户
- 1 个测试用户账户
- 10 个图书分类（5个一级 + 5个二级）
- 5 本示例图书
- 3 张轮播图

---

## 4. 数据表详细说明

### 4.1 user（用户表）

**表说明**：存储系统用户信息，包括普通用户和管理员。

**字段说明**：

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 用户ID，主键 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | - | 用户名，4-20字符，唯一 |
| password | VARCHAR(100) | NOT NULL | - | 密码，BCrypt加密存储 |
| nickname | VARCHAR(50) | NULL | NULL | 昵称 |
| phone | VARCHAR(11) | UNIQUE | NULL | 手机号，11位数字，唯一 |
| email | VARCHAR(100) | NULL | NULL | 邮箱地址 |
| role | TINYINT | NOT NULL | 0 | 角色：0-普通用户，1-管理员 |
| status | TINYINT | NOT NULL | 1 | 状态：0-禁用，1-启用 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 更新时间，自动更新 |

**索引**：
- PRIMARY KEY (id)
- UNIQUE INDEX idx_username (username)
- UNIQUE INDEX idx_phone (phone)
- INDEX idx_role (role)
- INDEX idx_status (status)

**业务规则**：
- 用户名必须唯一，长度 4-20 字符
- 手机号必须唯一，格式为 11 位数字
- 密码使用 BCrypt 加密，不可逆
- 禁用状态的用户不能登录

**示例数据**：
```sql
-- 管理员账户
username: admin
password: admin123 (加密后存储)
role: 1

-- 测试用户
username: testuser
password: user123 (加密后存储)
role: 0
```

---

### 4.2 category（图书分类表）

**表说明**：存储图书分类信息，支持二级分类结构。

**字段说明**：

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 分类ID，主键 |
| category_name | VARCHAR(50) | NOT NULL | - | 分类名称 |
| parent_id | BIGINT | NOT NULL | 0 | 父分类ID，0表示顶级分类 |
| sort_order | INT | NOT NULL | 0 | 排序值，越小越靠前 |
| status | TINYINT | NOT NULL | 1 | 状态：0-禁用，1-启用 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |

**索引**：
- PRIMARY KEY (id)
- INDEX idx_parent_id (parent_id)
- INDEX idx_sort_order (sort_order)
- INDEX idx_status (status)

**业务规则**：
- parent_id = 0 表示一级分类
- parent_id > 0 表示二级分类，值为父分类的 id
- 删除分类前需确保该分类下没有图书
- 按 sort_order 升序排序

**分类结构示例**：
```
计算机 (id=1, parent_id=0)
  ├─ 编程语言 (id=6, parent_id=1)
  ├─ 数据库 (id=7, parent_id=1)
  └─ 算法与数据结构 (id=8, parent_id=1)

文学 (id=2, parent_id=0)
  ├─ 小说 (id=9, parent_id=2)
  └─ 散文 (id=10, parent_id=2)
```

---

### 4.3 book（图书表）

**表说明**：存储图书商品信息，系统核心业务表。

**字段说明**：

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 图书ID，主键 |
| book_name | VARCHAR(200) | NOT NULL | - | 书名 |
| author | VARCHAR(100) | NULL | NULL | 作者 |
| publisher | VARCHAR(100) | NULL | NULL | 出版社 |
| isbn | VARCHAR(20) | NULL | NULL | ISBN编号 |

**说明**：ISBN 字段为普通索引，不强制唯一。考虑到同一本书可能存在多个版本，或某些图书没有ISBN，在应用层添加图书时若出现重复应提示管理员确认版本差异。
| category_id | BIGINT | NOT NULL, FK | - | 分类ID，外键关联category.id |
| price | DECIMAL(10,2) | NOT NULL | - | 价格，必须大于0 |
| stock | INT | NOT NULL | 0 | 库存数量，不能为负数 |
| cover_image | VARCHAR(500) | NULL | NULL | 封面图片URL |
| description | TEXT | NULL | NULL | 图书简介 |
| status | TINYINT | NOT NULL | 1 | 状态：0-下架，1-上架 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |

**索引**：
- PRIMARY KEY (id)
- INDEX idx_category_id (category_id)
- INDEX idx_status (status)
- INDEX idx_book_name (book_name(100))
- INDEX idx_isbn (isbn)   -- 普通索引，允许重复，适用于不同版本/空值
- INDEX idx_create_time (create_time)

**外键**：
- FOREIGN KEY (category_id) REFERENCES category(id)
  - ON DELETE RESTRICT：分类被引用时不能删除
  - ON UPDATE CASCADE：分类ID更新时级联更新

**业务规则**：
- 价格必须大于 0
- 库存不能为负数
- 下架状态（status=0）的图书不能在前台展示和购买
- 图书必须关联有效的分类
- 封面图片格式：jpg, png, webp

---

### 4.4 address（收货地址表）

**表说明**：存储用户收货地址信息，用于订单配送。

**字段说明**：

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 地址ID，主键 |
| user_id | BIGINT | NOT NULL, FK | - | 用户ID，外键关联user.id |
| receiver_name | VARCHAR(50) | NOT NULL | - | 收货人姓名 |
| phone | VARCHAR(11) | NOT NULL | - | 收货人手机号 |
| province | VARCHAR(50) | NULL | NULL | 省份 |
| city | VARCHAR(50) | NULL | NULL | 城市 |
| district | VARCHAR(50) | NULL | NULL | 区县 |
| detail_address | VARCHAR(200) | NOT NULL | - | 详细地址 |
| is_default | TINYINT | NOT NULL | 0 | 是否默认：0-否，1-是 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |

**索引**：
- PRIMARY KEY (id)
- INDEX idx_user_id (user_id)
- INDEX idx_is_default (is_default)

**外键**：
- FOREIGN KEY (user_id) REFERENCES user(id)
  - ON DELETE CASCADE：用户删除时级联删除地址
  - ON UPDATE CASCADE：用户ID更新时级联更新

**业务规则**：
- 每个用户可以有多个收货地址
- 每个用户必须有一个默认地址
- 设置新默认地址时，需取消其他地址的默认状态
- 收货人姓名、手机号、详细地址为必填项

---

### 4.5 cart（购物车表）

**表说明**：存储用户购物车信息，临时保存待购买商品。

**字段说明**：

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 购物车ID，主键 |
| user_id | BIGINT | NOT NULL, FK | - | 用户ID，外键关联user.id |
| book_id | BIGINT | NOT NULL, FK | - | 图书ID，外键关联book.id |
| quantity | INT | NOT NULL | - | 商品数量，必须大于0 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |

**索引**：
- PRIMARY KEY (id)
- UNIQUE INDEX idx_user_book (user_id, book_id)
- INDEX idx_book_id (book_id)

**外键**：
- FOREIGN KEY (user_id) REFERENCES user(id)
  - ON DELETE CASCADE：用户删除时级联删除购物车
- FOREIGN KEY (book_id) REFERENCES book(id)
  - ON DELETE CASCADE：图书删除时级联删除购物车记录

**业务规则**：
- 同一用户同一商品只能有一条购物车记录（通过唯一索引保证）
- 添加已存在商品时，数量累加
- 商品数量必须 > 0 且 <= 库存数量
- 下架商品不能加入购物车

---

### 4.6 order（订单表）

**表说明**：存储订单主信息，记录用户购买行为。

**字段说明**：

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 订单ID，主键 |
| order_no | VARCHAR(50) | NOT NULL, UNIQUE | - | 订单编号，格式：时间戳+用户ID+随机数 |
| user_id | BIGINT | NOT NULL, FK | - | 用户ID，外键关联user.id |
| total_amount | DECIMAL(10,2) | NOT NULL | - | 订单总金额 |
| status | TINYINT | NOT NULL | 0 | 订单状态：0-待支付，1-已支付，3-已发货，4-已完成，5-已取消 |
| address_id | BIGINT | NOT NULL, FK | - | 收货地址ID，外键关联address.id |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |
| pay_time | DATETIME | NULL | NULL | 支付时间 |
| ship_time | DATETIME | NULL | NULL | 发货时间 |

**订单状态枚举**：

| 状态值 | 状态名称 | 说明 |
|--------|---------|------|
| 0 | 待支付 | 订单已创建，等待用户支付 |
| 1 | 已支付 | 用户已支付，系统即将自动变更为待发货（瞬时状态） |
| 2 | 待发货 | 支付成功后自动进入，等待管理员发货 |
| 3 | 已发货 | 管理员已发货，等待用户收货 |
| 4 | 已完成 | 订单完成（用户确认收货或自动完成） |
| 5 | 已取消 | 订单已取消（用户主动取消或超时未支付） |

**订单状态流转**：
```
正常流程：
待支付(0) → 已支付(1) → 待发货(2) → 已发货(3) → 已完成(4)

取消流程：
待支付(0) → 已取消(5)
```

**状态变更触发时机**：
- 待支付 → 已支付：用户完成支付操作
- 已支付 → 待发货：支付成功后系统自动变更（在同一事务中立即执行）
- 待发货 → 已发货：管理员执行发货操作
- 已发货 → 已完成：用户确认收货或发货后7-15天自动完成
- 待支付 → 已取消：用户主动取消或创建后24小时未支付自动取消

**重要说明**：
- "已支付(1)"是一个瞬时状态，支付成功后立即自动变更为"待发货(2)"
- 这两次状态更新在同一事务中完成，确保原子性
- 管理员只需处理"待发货"状态的订单，无需关注"已支付"状态

**索引**：
- PRIMARY KEY (id)
- UNIQUE INDEX idx_order_no (order_no)
- INDEX idx_user_id (user_id)
- INDEX idx_status (status)
- INDEX idx_create_time (create_time)
- INDEX idx_address_id (address_id)

**外键**：
- FOREIGN KEY (user_id) REFERENCES user(id)
  - ON DELETE RESTRICT：用户有订单时不能删除
- FOREIGN KEY (address_id) REFERENCES address(id)
  - ON DELETE RESTRICT：地址被订单使用时不能删除

**业务规则**：
- 订单编号格式：时间戳(14位) + 用户ID(4位) + 随机数(4位)
- 订单总金额 = Σ(商品价格 × 数量)
- 只有待支付状态的订单可以支付
- 只有待发货状态的订单可以发货
- 已取消、已完成的订单不能修改状态

---

### 4.7 order_item（订单详情表）

**表说明**：存储订单商品明细，记录每个订单包含的商品信息。

**字段说明**：

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 订单详情ID，主键 |
| order_id | BIGINT | NOT NULL, FK | - | 订单ID，外键关联order.id |
| book_id | BIGINT | NOT NULL, FK | - | 图书ID，外键关联book.id |
| book_name | VARCHAR(200) | NOT NULL | - | 图书名称（冗余存储） |
| price | DECIMAL(10,2) | NOT NULL | - | 购买时的价格 |
| quantity | INT | NOT NULL | - | 购买数量 |
| total_price | DECIMAL(10,2) | NOT NULL | - | 小计金额 = price × quantity |

**索引**：
- PRIMARY KEY (id)
- INDEX idx_order_id (order_id)
- INDEX idx_book_id (book_id)

**外键**：
- FOREIGN KEY (order_id) REFERENCES order(id)
  - ON DELETE CASCADE：订单删除时级联删除订单详情
- FOREIGN KEY (book_id) REFERENCES book(id)
  - ON DELETE RESTRICT：图书被订单引用时不能删除

**业务规则**：
- book_name 冗余存储，防止图书信息修改后影响历史订单
- price 记录购买时的价格，不受后续价格变动影响
- total_price = price × quantity

**设计说明**：
- 为什么冗余存储 book_name？
  - 保证历史订单数据的准确性
  - 即使图书被删除或修改，订单详情仍然完整

---

### 4.8 banner（轮播图表）

**表说明**：存储首页轮播图信息，用于运营推广。

**字段说明**：

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 轮播图ID，主键 |
| image_url | VARCHAR(500) | NOT NULL | - | 图片URL |
| link_url | VARCHAR(500) | NULL | NULL | 跳转链接 |
| sort_order | INT | NOT NULL | 0 | 排序值，越小越靠前 |
| status | TINYINT | NOT NULL | 1 | 状态：0-禁用，1-启用 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |

**索引**：
- PRIMARY KEY (id)
- INDEX idx_sort_order (sort_order)
- INDEX idx_status (status)

**业务规则**：
- 前台最多显示 5 张轮播图
- 按 sort_order 升序排序
- 只显示启用状态（status=1）的轮播图
- 图片格式：jpg, png, webp

---

## 5. 索引设计说明

### 5.1 索引类型

**主键索引（PRIMARY KEY）**
- 所有表都有自增主键 `id`
- 自动创建聚簇索引
- 查询性能最优

**唯一索引（UNIQUE INDEX）**
- user.username：保证用户名唯一
- user.phone：保证手机号唯一
- order.order_no：保证订单号唯一
- cart(user_id, book_id)：保证同一用户同一商品只有一条记录

**普通索引（INDEX）**
- 外键字段：提升关联查询性能
- 状态字段：提升筛选查询性能
- 排序字段：提升排序查询性能
- 时间字段：提升时间范围查询性能

### 5.2 索引使用场景

| 索引 | 使用场景 |
|------|---------|
| idx_username | 用户登录、用户名唯一性校验 |
| idx_phone | 手机号查询、手机号唯一性校验 |
| idx_category_id | 按分类查询图书 |
| idx_isbn | 按 ISBN 查询图书，允许重复（普通索引） |
| idx_status | 查询上架/下架图书、启用/禁用用户 |
| idx_user_book | 查询购物车、防止重复添加 |
| idx_order_no | 按订单号查询订单 |
| idx_user_id | 查询用户的订单/地址/购物车 |
| idx_create_time | 按时间范围查询订单 |

### 5.3 索引优化建议

**适合创建索引的场景**：
- WHERE 条件常用字段
- JOIN 关联字段
- ORDER BY 排序字段
- 唯一约束字段

**不适合创建索引的场景**：
- 数据量很小的表（< 1000 行）
- 频繁更新的字段
- 区分度很低的字段（如性别）
- TEXT、BLOB 等大字段

**索引维护**：
```sql
-- 查看索引使用情况
SHOW INDEX FROM book;

-- 分析表
ANALYZE TABLE book;

-- 优化表
OPTIMIZE TABLE book;
```

---

## 6. 外键约束说明

### 6.1 外键策略

**ON DELETE CASCADE（级联删除）**
- 适用场景：主表删除时，从表数据也应该删除
- 使用示例：
  - user → address：用户删除时，删除其所有地址
  - user → cart：用户删除时，删除其购物车
  - order → order_item：订单删除时，删除订单详情

**ON DELETE RESTRICT（限制删除）**
- 适用场景：主表被引用时，不允许删除
- 使用示例：
  - category → book：分类被图书引用时，不能删除分类
  - user → order：用户有订单时，不能删除用户
  - book → order_item：图书被订单引用时，不能删除图书

**ON UPDATE CASCADE（级联更新）**
- 适用场景：主表主键更新时，从表外键自动更新
- 所有外键都使用此策略

### 6.2 外键关系图

```
user (id)
  ├─ CASCADE  → address (user_id)
  ├─ CASCADE  → cart (user_id)
  └─ RESTRICT → order (user_id)

category (id)
  └─ RESTRICT → book (category_id)

book (id)
  ├─ CASCADE  → cart (book_id)
  └─ RESTRICT → order_item (book_id)

order (id)
  └─ CASCADE  → order_item (order_id)

address (id)
  └─ RESTRICT → order (address_id)
```

### 6.3 外键使用注意事项

1. **删除数据前检查外键**
```sql
-- 删除用户前，检查是否有订单
SELECT COUNT(*) FROM `order` WHERE user_id = 1;

-- 删除分类前，检查是否有图书
SELECT COUNT(*) FROM book WHERE category_id = 1;
```

2. **禁用外键检查（谨慎使用）**
```sql
-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 执行操作...

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;
```

3. **外键性能影响**
- 外键会增加写操作的开销
- 但能保证数据完整性
- 建议在生产环境保留外键约束

---

## 7. 初始化数据说明

### 7.1 管理员账户

```sql
username: admin
password: admin123
role: 1 (管理员)
status: 1 (启用)
```

**重要提示**：
- 生产环境部署前必须修改管理员密码
- 密码需要使用 BCrypt 加密后存储
- 建议密码长度至少 8 位，包含大小写字母、数字、特殊字符

### 7.2 测试用户

```sql
username: testuser
password: user123
role: 0 (普通用户)
status: 1 (启用)
```

**用途**：
- 用于开发和测试
- 生产环境可以删除

### 7.3 图书分类

**一级分类**：
- 计算机
- 文学
- 经济管理
- 科学技术
- 生活

**二级分类**：
- 计算机 → 编程语言、数据库、算法与数据结构
- 文学 → 小说、散文

### 7.4 示例图书

| 书名 | 作者 | 分类 | 价格 | 库存 |
|------|------|------|------|------|
| Java编程思想 | Bruce Eckel | 编程语言 | 108.00 | 100 |
| 深入理解计算机系统 | Randal E. Bryant | 计算机 | 139.00 | 80 |
| 算法导论 | Thomas H. Cormen | 算法与数据结构 | 128.00 | 60 |
| MySQL必知必会 | Ben Forta | 数据库 | 49.00 | 120 |
| 活着 | 余华 | 小说 | 20.00 | 200 |

### 7.5 轮播图

- 3 张示例轮播图
- 图片 URL 为占位符，需要替换为实际图片地址

---

## 8. 常用查询示例

### 8.1 用户相关查询

```sql
-- 查询所有用户（不包含密码）
SELECT id, username, nickname, phone, email, role, status, create_time
FROM user;

-- 查询管理员列表
SELECT * FROM user WHERE role = 1;

-- 查询启用状态的用户
SELECT * FROM user WHERE status = 1;

-- 按用户名模糊搜索
SELECT * FROM user WHERE username LIKE '%test%';
```

### 8.2 图书相关查询

```sql
-- 查询上架图书列表（关联分类）
SELECT b.*, c.category_name
FROM book b
LEFT JOIN category c ON b.category_id = c.id
WHERE b.status = 1
ORDER BY b.create_time DESC;

-- 按分类查询图书
SELECT * FROM book WHERE category_id = 1 AND status = 1;

-- 按关键词搜索图书
SELECT * FROM book
WHERE (book_name LIKE '%Java%' OR author LIKE '%Java%')
  AND status = 1;

-- 查询库存不足的图书（库存 < 10）
SELECT * FROM book WHERE stock < 10;

-- 查询价格区间的图书
SELECT * FROM book WHERE price BETWEEN 50 AND 100 AND status = 1;
```

### 8.3 分类相关查询

```sql
-- 查询所有一级分类
SELECT * FROM category WHERE parent_id = 0 ORDER BY sort_order;

-- 查询某个分类的子分类
SELECT * FROM category WHERE parent_id = 1 ORDER BY sort_order;

-- 查询分类树（一级和二级）
SELECT 
    c1.id AS parent_id,
    c1.category_name AS parent_name,
    c2.id AS child_id,
    c2.category_name AS child_name
FROM category c1
LEFT JOIN category c2 ON c1.id = c2.parent_id
WHERE c1.parent_id = 0
ORDER BY c1.sort_order, c2.sort_order;
```

### 8.4 购物车相关查询

```sql
-- 查询用户购物车（关联图书信息）
SELECT 
    c.id,
    c.book_id,
    b.book_name,
    b.price,
    c.quantity,
    b.stock,
    b.cover_image,
    (b.price * c.quantity) AS total_price
FROM cart c
INNER JOIN book b ON c.book_id = b.id
WHERE c.user_id = 1;

-- 查询购物车总金额
SELECT SUM(b.price * c.quantity) AS cart_total
FROM cart c
INNER JOIN book b ON c.book_id = b.id
WHERE c.user_id = 1;
```

### 8.5 订单相关查询

```sql
-- 查询用户订单列表
SELECT * FROM `order`
WHERE user_id = 1
ORDER BY create_time DESC;

-- 查询订单详情（包含商品明细和地址）
SELECT 
    o.*,
    a.receiver_name,
    a.phone,
    CONCAT(a.province, a.city, a.district, a.detail_address) AS full_address
FROM `order` o
INNER JOIN address a ON o.address_id = a.id
WHERE o.id = 1;

-- 查询订单商品明细
SELECT * FROM order_item WHERE order_id = 1;

-- 查询待发货订单
SELECT * FROM `order` WHERE status = 2 ORDER BY pay_time;

-- 查询某时间段的订单
SELECT * FROM `order`
WHERE create_time BETWEEN '2024-03-01' AND '2024-03-31';

-- 统计订单金额
SELECT 
    COUNT(*) AS order_count,
    SUM(total_amount) AS total_sales
FROM `order`
WHERE status IN (2, 3, 4);
```

### 8.6 统计分析查询

```sql
-- 统计各分类图书数量
SELECT 
    c.category_name,
    COUNT(b.id) AS book_count
FROM category c
LEFT JOIN book b ON c.id = b.category_id
GROUP BY c.id, c.category_name;

-- 统计用户订单数和消费金额
SELECT 
    u.username,
    COUNT(o.id) AS order_count,
    IFNULL(SUM(o.total_amount), 0) AS total_spent
FROM user u
LEFT JOIN `order` o ON u.id = o.user_id
WHERE u.role = 0
GROUP BY u.id, u.username;

-- 统计图书销量排行
SELECT 
    b.book_name,
    SUM(oi.quantity) AS total_sold
FROM book b
INNER JOIN order_item oi ON b.id = oi.book_id
INNER JOIN `order` o ON oi.order_id = o.id
WHERE o.status IN (2, 3, 4)
GROUP BY b.id, b.book_name
ORDER BY total_sold DESC
LIMIT 10;
```

---

## 9. 数据库维护

### 9.1 备份策略

**完整备份（推荐每天执行）**
```bash
# 备份整个数据库
mysqldump -u root -p book_mall > book_mall_backup_$(date +%Y%m%d).sql

# 备份并压缩
mysqldump -u root -p book_mall | gzip > book_mall_backup_$(date +%Y%m%d).sql.gz
```

**增量备份（使用二进制日志）**
```sql
-- 启用二进制日志（my.cnf）
[mysqld]
log-bin=mysql-bin
expire_logs_days=7

-- 查看二进制日志
SHOW BINARY LOGS;

-- 刷新日志
FLUSH LOGS;
```

### 9.2 恢复数据

```bash
# 从备份文件恢复
mysql -u root -p book_mall < book_mall_backup_20240306.sql

# 从压缩备份恢复
gunzip < book_mall_backup_20240306.sql.gz | mysql -u root -p book_mall
```

### 9.3 性能优化

**慢查询日志**
```sql
-- 启用慢查询日志（my.cnf）
[mysqld]
slow_query_log=1
slow_query_log_file=/var/log/mysql/slow.log
long_query_time=2

-- 查看慢查询
SELECT * FROM mysql.slow_log;
```

**查询优化**
```sql
-- 分析查询执行计划
EXPLAIN SELECT * FROM book WHERE category_id = 1;

-- 查看索引使用情况
SHOW INDEX FROM book;

-- 分析表
ANALYZE TABLE book;
```

### 9.4 定期维护任务

**每周任务**：
```sql
-- 优化表（整理碎片）
OPTIMIZE TABLE user, book, `order`, order_item;

-- 检查表
CHECK TABLE user, book, `order`;

-- 修复表（如果需要）
REPAIR TABLE book;
```

**每月任务**：
- 检查磁盘空间
- 审查慢查询日志
- 更新统计信息
- 检查备份完整性

---

## 10. 常见问题

### 10.1 外键约束错误

**问题**：Cannot delete or update a parent row: a foreign key constraint fails

**原因**：尝试删除被外键引用的记录

**解决方案**：
```sql
-- 方案1：先删除子表记录
DELETE FROM order_item WHERE order_id = 1;
DELETE FROM `order` WHERE id = 1;

-- 方案2：临时禁用外键检查（谨慎使用）
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM `order` WHERE id = 1;
SET FOREIGN_KEY_CHECKS = 1;
```

### 10.2 字符集问题

**问题**：插入中文数据出现乱码

**解决方案**：
```sql
-- 检查数据库字符集
SHOW CREATE DATABASE book_mall;

-- 修改数据库字符集
ALTER DATABASE book_mall CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修改表字符集
ALTER TABLE book CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 10.3 自增ID重置

**问题**：需要重置自增ID

**解决方案**：
```sql
-- 重置自增ID（谨慎使用，会影响现有数据）
ALTER TABLE book AUTO_INCREMENT = 1;

-- 查看当前自增值
SELECT AUTO_INCREMENT FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'book_mall' AND TABLE_NAME = 'book';
```

### 10.4 索引失效

**常见原因**：
- 使用函数或表达式：`WHERE YEAR(create_time) = 2024`
- 使用 OR 连接不同字段
- 使用 LIKE 以通配符开头：`WHERE book_name LIKE '%Java'`
- 类型不匹配：`WHERE id = '1'`（id 是 BIGINT）

**解决方案**：
```sql
-- 错误：索引失效
SELECT * FROM book WHERE YEAR(create_time) = 2024;

-- 正确：使用索引
SELECT * FROM book WHERE create_time >= '2024-01-01' AND create_time < '2025-01-01';
```

### 10.5 死锁问题

**问题**：Deadlock found when trying to get lock

**原因**：多个事务相互等待对方释放锁

**解决方案**：
```sql
-- 查看死锁信息
SHOW ENGINE INNODB STATUS;

-- 预防死锁：
-- 1. 按相同顺序访问表和行
-- 2. 缩短事务时间
-- 3. 使用较低的隔离级别（如 READ COMMITTED）
-- 4. 为表添加合适的索引
```

---

## 11. 变更记录

### v1.0 (2024-03-06)
- 初始版本
- 创建 8 张核心业务表
- 添加主键、外键、索引约束
- 添加初始化数据
- 完成数据库设计文档

### 未来计划
- [ ] 添加用户操作日志表
- [ ] 添加商品评价表
- [ ] 添加优惠券表
- [ ] 添加支付记录表
- [ ] 实现读写分离
- [ ] 添加缓存层（Redis）

---

## 附录

### A. 数据库配置参考

**application.properties**
```properties
# 数据库连接配置
spring.datasource.url=jdbc:mysql://localhost:3306/book_mall?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MyBatis 配置
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=top.wjc.bookmallbackend.entity
mybatis.configuration.map-underscore-to-camel-case=true
mybatis.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl
```

### B. 相关文档

- [需求文档](../.kiro/specs/book-mall-system/requirements.md)
- [设计文档](../.kiro/specs/book-mall-system/design.md)
- [AI Coding 规范](../.kiro/steering/ai-coding-standards.md)

### C. 联系方式

如有问题或建议，请联系项目负责人。

---

**文档结束**
