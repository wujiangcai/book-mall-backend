# Phase 7 Alignment

## Context

Phase 7 will deliver admin management for orders, users, and banners, plus front banner list. Requirements specify admin order list/detail/ship, admin user list/detail/status update, admin banner CRUD + sort, and front banner list (top 5 enabled). Filters for admin order list: status, orderNo, userId. Ship action only when status=2 (PENDING_SHIP). User confirmed Phase 7 scope: orders + users + banner, with front banners included.

## Recommended Approach

### 1) Add/extend entities, DTOs, VOs

- Banner entity + VO/DTOs (none exist currently):
  - entity/Banner: id, imageUrl, linkUrl, sortOrder, status, createTime, updateTime.
  - DTOs: BannerCreateRequest, BannerUpdateRequest, BannerSortRequest.
  - VO: BannerVO (id, imageUrl, linkUrl, sortOrder, status).
- Admin order VOs (admin list/detail) for admin APIs:
  - AdminOrderListItemVO: orderId, orderNo, userId, totalAmount, status, createTime.
  - AdminOrderDetailVO: order info + user info + address snapshot + items.
- Admin user VOs + status DTO:
  - AdminUserListItemVO (id, username, nickname, phone, email, role, status, createTime).
  - AdminUserDetailVO (same + updateTime).
  - UserStatusRequest with status (0/1).

### 2) Extend mappers + XML

- OrderMapper + XML:
  - Admin list query with filters (status/orderNo/userId), pagination + count.
  - Select admin detail (order by id) and update status + shipTime.
- OrderItemMapper: reuse existing selectByOrderId for detail.
- UserMapper + XML:
  - Admin list query with keyword (username/phone), pagination + count.
  - Admin detail by id.
  - Update user status.
- BannerMapper + XML (new):
  - insert/update/delete/select list (admin).
  - update sort order.
  - front list: enabled only, order by sort_order asc, limit 5.

### 3) Implement services

- OrderService:
  - add admin methods: listAdmin, detailAdmin, ship (status must be 2).
- UserService:
  - add admin methods: listAdmin, detailAdmin, updateStatus.
- BannerService (new):
  - listAdmin, create, update, delete, updateSort.
  - listFront returns enabled top 5.

### 4) Controllers

- AdminOrderController:
  - GET /api/admin/orders (status/orderNo/userId/page/pageSize)
  - GET /api/admin/orders/{id}
  - PUT /api/admin/orders/{id}/ship
- AdminUserController:
  - GET /api/admin/users (page/pageSize/keyword)
  - GET /api/admin/users/{id}
  - PUT /api/admin/users/{id}/status
- AdminBannerController:
  - GET /api/admin/banners
  - POST /api/admin/banners
  - PUT /api/admin/banners/{id}
  - DELETE /api/admin/banners/{id}
  - PUT /api/admin/banners/{id}/sort
- FrontBannerController:
  - GET /api/front/banners (enabled top 5, sort_order asc)

### 5) Validation & rules

- Order ship: only status=2 → status=3, set shipTime; else InvalidOrderStatusException.
- User status: only 0/1 allowed.
- Banner create/update: validate imageUrl not blank and extension jpg/png/webp; default sortOrder if null.
- Front banners: status=1 and limit 5.

## Files to Modify/Add

### Entities/DTO/VO

- src/main/java/top/wjc/bookmallbackend/entity/Banner.java
- src/main/java/top/wjc/bookmallbackend/dto/BannerCreateRequest.java
- src/main/java/top/wjc/bookmallbackend/dto/BannerUpdateRequest.java
- src/main/java/top/wjc/bookmallbackend/dto/BannerSortRequest.java
- src/main/java/top/wjc/bookmallbackend/dto/UserStatusRequest.java
- src/main/java/top/wjc/bookmallbackend/vo/BannerVO.java
- src/main/java/top/wjc/bookmallbackend/vo/AdminOrderListItemVO.java
- src/main/java/top/wjc/bookmallbackend/vo/AdminOrderDetailVO.java
- src/main/java/top/wjc/bookmallbackend/vo/AdminUserListItemVO.java
- src/main/java/top/wjc/bookmallbackend/vo/AdminUserDetailVO.java

### Mappers/XML

- src/main/java/top/wjc/bookmallbackend/mapper/OrderMapper.java
- src/main/resources/mapper/OrderMapper.xml
- src/main/java/top/wjc/bookmallbackend/mapper/UserMapper.java
- src/main/resources/mapper/UserMapper.xml
- src/main/java/top/wjc/bookmallbackend/mapper/BannerMapper.java
- src/main/resources/mapper/BannerMapper.xml

### Services

- src/main/java/top/wjc/bookmallbackend/service/OrderService.java
- src/main/java/top/wjc/bookmallbackend/service/impl/OrderServiceImpl.java
- src/main/java/top/wjc/bookmallbackend/service/UserService.java
- src/main/java/top/wjc/bookmallbackend/service/impl/UserServiceImpl.java
- src/main/java/top/wjc/bookmallbackend/service/BannerService.java
- src/main/java/top/wjc/bookmallbackend/service/impl/BannerServiceImpl.java

### Controllers

- src/main/java/top/wjc/bookmallbackend/controller/admin/AdminOrderController.java
- src/main/java/top/wjc/bookmallbackend/controller/admin/AdminUserController.java
- src/main/java/top/wjc/bookmallbackend/controller/admin/AdminBannerController.java
- src/main/java/top/wjc/bookmallbackend/controller/front/FrontBannerController.java

## Verification

- Admin orders:
  - list with filters (status/orderNo/userId), pagination works.
  - detail returns order + user + items + address.
  - ship only when status=2; sets status=3 and shipTime.
- Admin users:
  - list with keyword; detail by id; update status 0/1; disabled users cannot access front APIs via interceptor.
- Admin banners:
  - CRUD works; sort updates; list order by sort_order asc.
- Front banners:
  - list returns enabled top 5, ordered by sort_order asc.
