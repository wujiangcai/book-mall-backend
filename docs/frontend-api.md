# 前台 API 文档

> 面向前端页面（用户侧）接口清单。统一返回 `Result<T>`。

## 1) 前台认证（无需登录）

- **POST /api/front/auth/register**
  - DTO: `RegisterRequest`（`dto/RegisterRequest.java`）
  - 响应: `AuthResponse`（`vo/AuthResponse.java`）
- **POST /api/front/auth/login**
  - DTO: `LoginRequest`（`dto/LoginRequest.java`）
  - 响应: `AuthResponse`

## 2) 前台用户（需要登录）

- **GET /api/front/user/info**
  - 响应: `UserInfoResponse`（`vo/UserInfoResponse.java`）
- **PUT /api/front/user/info**
  - DTO: `UpdateUserRequest`（`dto/UpdateUserRequest.java`）
  - 响应: `null`

## 3) 前台分类（无需登录）

- **GET /api/front/categories**
  - 响应: `CategoryTreeVO[]`（`vo/CategoryTreeVO.java`）

## 4) 前台图书（无需登录）

- **GET /api/front/books**
  - Query: `page` `pageSize` `categoryId` `keyword`
  - 响应: `PageResult<BookListItemVO>`（`vo/BookListItemVO.java`）
- **GET /api/front/books/{id}**
  - 响应: `BookDetailVO`（`vo/BookDetailVO.java`）

## 5) 前台购物车（需要登录）

- **GET /api/front/cart**
  - 响应: `CartItemVO[]`（`vo/CartItemVO.java`）
- **POST /api/front/cart**
  - DTO: `CartAddRequest`（`dto/CartAddRequest.java`）
- **PUT /api/front/cart/{id}**
  - DTO: `CartUpdateRequest`（`dto/CartUpdateRequest.java`）
- **DELETE /api/front/cart/{id}**

## 6) 前台地址（需要登录）

- **GET /api/front/user/addresses**
  - 响应: `AddressVO[]`（`vo/AddressVO.java`）
- **POST /api/front/user/addresses**
  - DTO: `AddressCreateRequest`（`dto/AddressCreateRequest.java`）
- **PUT /api/front/user/addresses/{id}**
  - DTO: `AddressUpdateRequest`（`dto/AddressUpdateRequest.java`）
- **DELETE /api/front/user/addresses/{id}**
- **PUT /api/front/user/addresses/{id}/default**

## 7) 前台订单（需要登录）

- **POST /api/front/orders**
  - DTO: `OrderCreateRequest`（`dto/OrderCreateRequest.java`）
  - 响应: `OrderCreateVO`（`vo/OrderCreateVO.java`）
- **POST /api/front/orders/{id}/pay**
- **POST /api/front/orders/{id}/cancel**
- **GET /api/front/orders**
  - Query: `page` `pageSize`
  - 响应: `PageResult<OrderListItemVO>`（`vo/OrderListItemVO.java`）
- **GET /api/front/orders/{id}**
  - 响应: `OrderDetailVO`（`vo/OrderDetailVO.java`）

## 8) 前台轮播图（无需登录）

- **GET /api/front/banners**
  - 响应: `BannerVO[]`（`vo/BannerVO.java`）
  - 规则：仅启用状态，最多 5 条，按 `sort_order` 升序

## 9) 前台推荐（获取推荐可匿名，行为上报需登录）

- **GET /api/front/recommendations**
  - Query: `sceneBookId` `limit`
  - 说明：
    - 该接口允许匿名访问。
    - 已登录用户会自动叠加个性化信号。
    - `sceneBookId` 用于图书详情页“AI 看了又看”场景。
  - 响应: `RecommendationBookVO[]`（`vo/RecommendationBookVO.java`）
  - 返回字段补充：
    - `score`：混合推荐融合分
    - `strategy`：命中的推荐策略，如“协同过滤 + AI智能推荐”
    - `reason`：推荐理由，用于前端解释推荐结果
- **POST /api/front/recommendations/behaviors**
  - DTO: `RecommendationBehaviorRequest`（`dto/RecommendationBehaviorRequest.java`）
  - 说明：
    - 该接口要求登录。
    - `behaviorType` 仅支持 `view`、`cart`、`purchase`。
    - 用于实时采集用户行为，支撑推荐算法持续更新。
