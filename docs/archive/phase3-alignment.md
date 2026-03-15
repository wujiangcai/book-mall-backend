# Phase 3 对齐清单（分类 / 图书）

## 1) API / DTO / VO 设计对齐

### 前台接口（无需登录）
- **GET /api/front/books**（分页列表，仅上架且 status != -1）
  - Query: page(默认1), pageSize(默认20,最大100), categoryId(可选), keyword(可选)
  - VO: `BookListItemVO`（不含 description）
- **GET /api/front/books/{id}**（详情，status != -1）
  - VO: `BookDetailVO`
- **GET /api/front/categories**（分类树，仅启用且 status != -1）
  - VO: `CategoryTreeVO`

### 后台接口（管理员）
- **GET /api/admin/books**（列表，status != -1）
  - VO: `BookAdminListItemVO`
- **POST /api/admin/books**（新增）
  - DTO: `BookCreateRequest`
- **PUT /api/admin/books/{id}**（更新）
  - DTO: `BookUpdateRequest`
- **DELETE /api/admin/books/{id}**（软删除：status = -1）
- **PUT /api/admin/books/{id}/status**（上下架）
  - DTO: `BookStatusRequest`
- **GET /api/admin/categories**（列表，status != -1）
  - VO: `CategoryAdminVO`
- **POST /api/admin/categories**（新增）
  - DTO: `CategoryCreateRequest`
- **PUT /api/admin/categories/{id}**（更新）
  - DTO: `CategoryUpdateRequest`
- **DELETE /api/admin/categories/{id}**（删除，需校验无图书）
- **PUT /api/admin/categories/{id}/status**（启用/禁用）
  - DTO: `CategoryStatusRequest`

### DTO 清单
- `BookCreateRequest` / `BookUpdateRequest` / `BookStatusRequest`
- `CategoryCreateRequest` / `CategoryUpdateRequest` / `CategoryStatusRequest`

### VO 清单
- `BookListItemVO`（前台列表）
- `BookDetailVO`（前台详情）
- `BookAdminListItemVO`（后台列表）
- `CategoryTreeVO`（前台分类树）
- `CategoryAdminVO`（后台分类列表）

## 2) Controller / Service / Mapper 对齐

### Controller
- `FrontBookController`
  - GET /api/front/books
  - GET /api/front/books/{id}
- `FrontCategoryController`
  - GET /api/front/categories
- `AdminBookController`
  - GET /api/admin/books
  - POST /api/admin/books
  - PUT /api/admin/books/{id}
  - DELETE /api/admin/books/{id}（soft delete）
  - PUT /api/admin/books/{id}/status
- `AdminCategoryController`
  - GET /api/admin/categories
  - POST /api/admin/categories
  - PUT /api/admin/categories/{id}
  - DELETE /api/admin/categories/{id}
  - PUT /api/admin/categories/{id}/status

### Service
- `BookService`: listFront, getFrontDetail, listAdmin, create, update, delete(soft), updateStatus
- `CategoryService`: getFrontTree, listAdmin, create, update, delete, updateStatus

### Mapper
- `BookMapper`: selectFrontList, selectFrontDetail, selectAdminList, selectById, insert, update, updateStatus, softDelete
- `CategoryMapper`: selectAllEnabled, selectAdminList, selectById, countByName, insert, update, updateStatus, softDelete

## 3) 实体字段对齐（schema.sql）

### Category
- id, categoryName, parentId, sortOrder, status, createTime, updateTime
- status: 1 启用 / 0 禁用 / -1 软删除（API 层过滤 -1）
- 排序：sort_order asc, create_time asc

### Book
- id, bookName, author, publisher, isbn, categoryId, price, stock, coverImage, description, status, createTime, updateTime
- status: 1 上架 / 0 下架 / -1 软删除（API 层过滤 -1）
- isbn 非唯一索引

## 4) MyBatis XML 字段映射模板摘要

### BookMapper.xml
- BaseResultMap / Base_Column_List
- selectFrontList（join category，status=1 且 != -1）
- selectFrontDetail（status != -1）
- selectAdminList（status != -1）
- selectById / insert / update / updateStatus / softDelete(status=-1)

### CategoryMapper.xml
- BaseResultMap / Base_Column_List
- selectAllEnabled（status=1 且 != -1）
- selectAdminList（status != -1）
- selectById / countByName / insert / update / updateStatus / softDelete(status=-1)
