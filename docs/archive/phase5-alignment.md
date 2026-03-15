# Phase 5 对齐清单（收货地址）

## 1) API / DTO / VO 设计对齐

### 前台接口（需要登录）
- **GET /api/front/user/addresses**（地址列表）
  - VO: `AddressVO`
- **POST /api/front/user/addresses**（新增地址）
  - DTO: `AddressCreateRequest`
  - 规则：首次添加自动设默认；可传 isDefault=1 主动设默认
- **PUT /api/front/user/addresses/{id}**（更新地址）
  - DTO: `AddressUpdateRequest`（不用于切换默认地址）
- **DELETE /api/front/user/addresses/{id}**（删除地址）
  - 规则：仅当未被“待发货”订单引用时允许删除
- **PUT /api/front/user/addresses/{id}/default**（设为默认）
  - 规则：专用接口切换默认地址

### DTO 清单
- `AddressCreateRequest`（receiverName, phone, province, city, district, detailAddress, isDefault）
- `AddressUpdateRequest`（同上，但不用于切换默认）

### VO 清单
- `AddressVO`（id, receiverName, phone, province, city, district, detailAddress, isDefault）

## 2) Controller / Service / Mapper 对齐

### Controller
- `FrontAddressController`
  - GET /api/front/user/addresses
  - POST /api/front/user/addresses
  - PUT /api/front/user/addresses/{id}
  - DELETE /api/front/user/addresses/{id}
  - PUT /api/front/user/addresses/{id}/default

### Service
- `AddressService`: list, create, update, delete, setDefault

### Mapper
- `AddressMapper`: selectByUserId, selectById, insert, update, deleteById, clearDefaultByUserId, countDefaultByUserId
- `OrderMapper`（最小查询）：countPendingShipmentByAddressId

## 3) 实体字段对齐（schema.sql）

### Address
- id, userId, receiverName, phone, province, city, district, detailAddress, isDefault, createTime, updateTime
- 约束：同一用户可多个地址；默认地址 is_default=1

## 4) MyBatis XML 字段映射模板摘要

### AddressMapper.xml
- BaseResultMap / Base_Column_List
- selectByUserId（按 user_id 查询）
- selectById（用于校验归属）
- insert / update / deleteById
- clearDefaultByUserId（将该用户所有地址 is_default=0）
- countDefaultByUserId（是否存在默认地址）

### OrderMapper.xml（最小查询）
- countPendingShipmentByAddressId：status=2 的订单数量

## 5) 业务规则对齐
- 首次新增地址自动设默认
- 默认地址仅通过专用接口切换
- 删除地址仅在“待发货”订单未引用时允许
