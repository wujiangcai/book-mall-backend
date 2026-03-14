# 后台 API 文档

> 面向后台管理端接口清单。统一返回 `Result<T>`。

## 1) 后台认证（无需登录）

- **POST /api/admin/auth/login**
  - DTO: `LoginRequest`
  - 响应: `AuthResponse`

## 2) 后台图书

- **GET /api/admin/books**
  - Query: `page` `pageSize` `categoryId` `keyword`
  - 响应: `PageResult<BookAdminListItemVO>`（`vo/BookAdminListItemVO.java`）
- **POST /api/admin/books**
  - DTO: `BookCreateRequest`（`dto/BookCreateRequest.java`）
- **PUT /api/admin/books/{id}**
  - DTO: `BookUpdateRequest`（`dto/BookUpdateRequest.java`）
- **DELETE /api/admin/books/{id}**
  - 软删除：`status = -1`
- **PUT /api/admin/books/{id}/status**
  - DTO: `BookStatusRequest`（`dto/BookStatusRequest.java`）

## 3) 后台分类

- **GET /api/admin/categories**
  - 响应: `CategoryAdminVO[]`（`vo/CategoryAdminVO.java`）
- **POST /api/admin/categories**
  - DTO: `CategoryCreateRequest`（`dto/CategoryCreateRequest.java`）
- **PUT /api/admin/categories/{id}**
  - DTO: `CategoryUpdateRequest`（`dto/CategoryUpdateRequest.java`）
- **DELETE /api/admin/categories/{id}**
- **PUT /api/admin/categories/{id}/status**
  - DTO: `CategoryStatusRequest`（`dto/CategoryStatusRequest.java`）

## 4) 后台订单

- **GET /api/admin/orders**
  - Query: `page` `pageSize` `status` `orderNo` `userId`
  - 响应: `PageResult<AdminOrderListItemVO>`（`vo/AdminOrderListItemVO.java`）
- **GET /api/admin/orders/{id}**
  - 响应: `AdminOrderDetailVO`（`vo/AdminOrderDetailVO.java`）
- **PUT /api/admin/orders/{id}/ship**
  - 规则：仅 `status=2` 可发货

## 5) 后台用户

- **GET /api/admin/users**
  - Query: `page` `pageSize` `keyword`
  - 响应: `PageResult<AdminUserListItemVO>`（`vo/AdminUserListItemVO.java`）
- **GET /api/admin/users/{id}**
  - 响应: `AdminUserDetailVO`（`vo/AdminUserDetailVO.java`）
- **PUT /api/admin/users/{id}/status**
  - DTO: `UserStatusRequest`（`dto/UserStatusRequest.java`）

## 6) 后台轮播图

- **GET /api/admin/banners**
  - 响应: `BannerVO[]`
- **POST /api/admin/banners**
  - DTO: `BannerCreateRequest`（`dto/BannerCreateRequest.java`）
- **PUT /api/admin/banners/{id}**
  - DTO: `BannerUpdateRequest`（`dto/BannerUpdateRequest.java`）
- **DELETE /api/admin/banners/{id}**
- **PUT /api/admin/banners/{id}/sort**
  - DTO: `BannerSortRequest`（`dto/BannerSortRequest.java`）
