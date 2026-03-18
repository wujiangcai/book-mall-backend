-- =============================================
-- 图书商城管理系统 - 数据库建表脚本
-- 数据库：book_mall
-- 字符集：UTF-8
-- 引擎：InnoDB
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS book_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE book_mall;

-- =============================================
-- 1. 用户表 (user)
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名，4-20字符',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `phone` VARCHAR(11) DEFAULT NULL COMMENT '手机号，11位数字',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  UNIQUE KEY `idx_phone` (`phone`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 图书分类表 (category)
-- =============================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- =============================================
-- 3. 图书表 (book)
-- =============================================
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图书ID',
  `book_name` VARCHAR(200) NOT NULL COMMENT '书名',
  `author` VARCHAR(100) DEFAULT NULL COMMENT '作者',
  `publisher` VARCHAR(100) DEFAULT NULL COMMENT '出版社',
  `isbn` VARCHAR(20) DEFAULT NULL COMMENT 'ISBN编号',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格，必须大于0',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量，不能为负数',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
  `description` TEXT DEFAULT NULL COMMENT '图书简介',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_isbn` (`isbn`),  -- 普通索引，允许重复
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_book_name` (`book_name`(100)),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_book_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';

-- =============================================
-- 4. 收货地址表 (address)
-- =============================================
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `phone` VARCHAR(11) NOT NULL COMMENT '收货人手机号',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区县',
  `detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认：0-否，1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`is_default`),
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- =============================================
-- 5. 购物车表 (cart)
-- =============================================
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `quantity` INT NOT NULL COMMENT '商品数量，必须大于0',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_book` (`user_id`, `book_id`),
  KEY `idx_book_id` (`book_id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- =============================================
-- 6. 订单表 (order)
-- =============================================
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号，格式：时间戳+用户ID+随机数',
  `trade_no` VARCHAR(64) DEFAULT NULL COMMENT '支付宝交易号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-待发货，3-已发货，4-已完成，5-已取消',
  `address_id` BIGINT NOT NULL COMMENT '收货地址ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_address_id` (`address_id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_order_address` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =============================================
-- 7. 订单详情表 (order_item)
-- =============================================
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单详情ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `book_name` VARCHAR(200) NOT NULL COMMENT '图书名称（冗余存储）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '购买时的价格',
  `quantity` INT NOT NULL COMMENT '购买数量',
  `total_price` DECIMAL(10,2) NOT NULL COMMENT '小计金额 = price × quantity',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_book_id` (`book_id`),
  CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_order_item_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单详情表';

-- =============================================
-- 8. 轮播图表 (banner)
-- =============================================
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `link_url` VARCHAR(500) DEFAULT NULL COMMENT '跳转链接',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入管理员账户
-- 用户名：admin
-- 密码：admin123（明文，BCrypt加密后存储）
-- 注意：以下哈希值为示例，生产环境部署前必须重新生成
-- 生成方法：运行项目根目录下的 PasswordGenerator.java
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `email`, `role`, `status`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKLU.yvC', '系统管理员', '13800138000', 'admin@bookmall.com', 1, 1);

-- 插入测试用户
-- 用户名：testuser
-- 密码：user123（明文，BCrypt加密后存储）
-- 注意：仅用于开发测试，生产环境应删除此账户
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `email`, `role`, `status`) 
VALUES ('testuser', '$2a$10$5K8p7VX3qhJjR8L9mN2pOeX8F9G1H2J3K4L5M6N7O8P9Q0R1S2T3U', '测试用户', '13900139000', 'test@example.com', 0, 1);

-- 插入图书分类
INSERT INTO `category` (`category_name`, `parent_id`, `sort_order`) VALUES
('计算机', 0, 1),
('文学', 0, 2),
('经济管理', 0, 3),
('科学技术', 0, 4),
('生活', 0, 5);

INSERT INTO `category` (`category_name`, `parent_id`, `sort_order`) VALUES
('编程语言', 1, 1),
('数据库', 1, 2),
('算法与数据结构', 1, 3),
('小说', 2, 1),
('散文', 2, 2);

-- 插入示例图书
INSERT INTO `book` (`book_name`, `author`, `publisher`, `isbn`, `category_id`, `price`, `stock`, `description`, `status`) VALUES
('Java编程思想', 'Bruce Eckel', '机械工业出版社', '9787111213826', 6, 108.00, 100, '本书详细介绍了Java编程的核心思想和技术。', 1),
('深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '9787111544937', 1, 139.00, 80, '从程序员的视角深入理解计算机系统。', 1),
('算法导论', 'Thomas H. Cormen', '机械工业出版社', '9787111407010', 8, 128.00, 60, '算法领域的经典著作。', 1),
('MySQL必知必会', 'Ben Forta', '人民邮电出版社', '9787115385604', 7, 49.00, 120, 'MySQL入门经典教程。', 1),
('活着', '余华', '作家出版社', '9787506365437', 9, 20.00, 200, '讲述了一个人和他命运之间的友情。', 1);

-- 插入轮播图
INSERT INTO `banner` (`image_url`, `link_url`, `sort_order`, `status`) VALUES
('https://example.com/banner1.jpg', 'https://example.com/promotion1', 1, 1),
('https://example.com/banner2.jpg', 'https://example.com/promotion2', 2, 1),
('https://example.com/banner3.jpg', 'https://example.com/promotion3', 3, 1);

-- =============================================
-- 脚本执行完成
-- =============================================
