# Phase 6 对齐清单（订单全流程）

## 1) API / DTO / VO 设计对齐

### 前台接口（需要登录）
- **POST /api/front/orders**（从购物车选中项创建订单）
  - DTO: `OrderCreateRequest`
  - body: addressId, cartIds
- **POST /api/front/orders/{id}/pay**（支付订单）
- **POST /api/front/orders/{id}/cancel**（取消订单）
- **GET /api/front/orders**（订单列表，分页）
- **GET /api/front/orders/{id}**（订单详情）

### DTO 清单
- `OrderCreateRequest`（addressId, cartIds）
- `OrderPayRequest`/`OrderCancelRequest`：不需要（使用 path param）

### VO 清单
- `OrderListItemVO`（orderId, orderNo, totalAmount, status, createTime, itemSummary）
- `OrderDetailVO`（订单信息 + OrderItemVO 列表）
- `OrderItemVO`（bookId, bookName, price, quantity, totalPrice）

## 2) Controller / Service / Mapper 对齐

### Controller
- `FrontOrderController`
  - POST /api/front/orders
  - POST /api/front/orders/{id}/pay
  - POST /api/front/orders/{id}/cancel
  - GET /api/front/orders
  - GET /api/front/orders/{id}

### Service
- `OrderService`: create, pay, cancel, list, detail

### Mapper
- `OrderMapper`: insert, selectById, selectByUserId, listByUserId（分页）, updateStatus, updatePayTime, updateShipTime, selectByOrderNo（如需）
- `OrderItemMapper`: batchInsert, selectByOrderId
- `CartMapper`: selectByIdsAndUserId, deleteByIdsAndUserId
- `BookMapper` / `AddressMapper`: 用于校验与归属检查

## 3) 实体字段对齐（schema.sql）

### Order
- id, orderNo, userId, totalAmount, status, addressId, createTime, updateTime, payTime, shipTime

### OrderItem
- id, orderId, bookId, bookName, price, quantity, totalPrice

## 4) MyBatis XML 字段映射模板摘要

### OrderMapper.xml
- BaseResultMap / Base_Column_List
- insert
- selectById
- selectByUserId
- listByUserId（分页）
- updateStatus
- updatePayTime
- updateShipTime
- selectByOrderNo（如需）

### OrderItemMapper.xml
- BaseResultMap / Base_Column_List
- batchInsert
- selectByOrderId

### CartMapper.xml
- selectByIdsAndUserId（按 cartIds + userId 拉取选中项）
- deleteByIdsAndUserId（创建订单后删除选中项）

## 5) 业务规则对齐
- 订单状态流转：UNPAID → PENDING_SHIP（支付后）；取消仅允许 UNPAID
- 创建订单与支付需做库存校验；仅在支付时扣减库存
- create / pay 全流程使用事务，保证订单、明细、库存、购物车一致性
- 订单仅允许本人查看、支付、取消

## 6) 文件清单
- src/main/java/top/wjc/bookmallbackend/entity/Order.java
- src/main/java/top/wjc/bookmallbackend/entity/OrderItem.java
- src/main/java/top/wjc/bookmallbackend/dto/OrderCreateRequest.java
- src/main/java/top/wjc/bookmallbackend/vo/OrderListItemVO.java
- src/main/java/top/wjc/bookmallbackend/vo/OrderDetailVO.java
- src/main/java/top/wjc/bookmallbackend/vo/OrderItemVO.java
- src/main/java/top/wjc/bookmallbackend/mapper/OrderMapper.java
- src/main/resources/mapper/OrderMapper.xml
- src/main/java/top/wjc/bookmallbackend/mapper/OrderItemMapper.java
- src/main/resources/mapper/OrderItemMapper.xml
- src/main/java/top/wjc/bookmallbackend/mapper/CartMapper.java
- src/main/resources/mapper/CartMapper.xml
- src/main/java/top/wjc/bookmallbackend/service/OrderService.java
- src/main/java/top/wjc/bookmallbackend/service/impl/OrderServiceImpl.java
- src/main/java/top/wjc/bookmallbackend/controller/front/FrontOrderController.java

## 7) 验证清单
- 创建订单：选中 cartIds 有效，订单状态=UNPAID，选中购物车项被删除
- 支付订单：库存复检，库存扣减，状态变为 PENDING_SHIP，payTime 写入
- 取消订单：仅 UNPAID 允许，状态变为 CANCELLED
- 列表/详情：仅返回本人订单，分页可用
