# Phase 4 对齐清单（购物车）

## 1) API / DTO / VO 设计对齐

### 前台接口（需要登录）
- **GET /api/front/cart**（购物车列表）
  - VO: `CartItemVO`（包含 totalPrice）
- **POST /api/front/cart**（添加商品，已存在则累加数量）
  - DTO: `CartAddRequest`
- **PUT /api/front/cart/{id}**（更新数量，路径参数为 cartId）
  - DTO: `CartUpdateRequest`
- **DELETE /api/front/cart/{id}**（删除，路径参数为 cartId）

### DTO 清单
- `CartAddRequest`（bookId, quantity）
- `CartUpdateRequest`（quantity）

### VO 清单
- `CartItemVO`（id, bookId, bookName, price, quantity, stock, coverImage, totalPrice）

## 2) Controller / Service / Mapper 对齐

### Controller
- `FrontCartController`
  - GET /api/front/cart
  - POST /api/front/cart
  - PUT /api/front/cart/{id}
  - DELETE /api/front/cart/{id}

### Service
- `CartService`: list, add(merge quantity), updateQuantity, delete

### Mapper
- `CartMapper`: selectByUserId, selectById, selectByUserIdAndBookId, insert, updateQuantity, deleteById

## 3) 实体字段对齐（schema.sql）

### Cart
- id, userId, bookId, quantity, createTime, updateTime
- 约束：同一用户同一商品唯一（idx_user_book）

## 4) MyBatis XML 字段映射模板摘要

### CartMapper.xml
- BaseResultMap / Base_Column_List
- listByUserId：join book 获取 bookName/price/stock/coverImage
- selectById：用于校验归属
- selectByUserIdAndBookId：用于合并数量
- insert / updateQuantity / deleteById

## 5) 业务规则对齐
- 添加：仅允许上架且未软删除图书；库存充足；已存在则累加数量
- 更新数量：quantity > 0 且 <= 库存
- 列表返回 totalPrice = price × quantity
