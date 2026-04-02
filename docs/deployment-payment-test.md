# 服务器部署与支付宝联调说明

> 用于将 `book-mall-backend` 部署到公网服务器，并验证支付宝支付、异步通知、幂等与后台订单状态限制。

## 1. 适用范围

本文档适用于当前后端项目：
- Spring Boot 4
- Java 17
- MySQL
- 支付宝沙箱环境
- 当前订单主流程：`UNPAID -> PENDING_SHIP -> SHIPPED`

本轮支付修复已包含：
- 支付发起阶段不再扣库存
- 支付宝异步通知成功后再扣库存
- 异步通知事务化处理
- 支付结果原子更新
- 回调幂等处理
- 回调增加 `app_id`、`seller_id` 校验
- 后台禁止任意非法订单状态跳转

---

## 2. 部署前准备

### 2.1 服务器基础环境

需要准备：
- Linux 服务器一台（推荐 Ubuntu/CentOS）
- Java 17
- MySQL 8+
- 可公网访问的后端地址
- 可选：Nginx 反向代理

### 2.2 项目关键端口

默认后端端口：
- `8003`

如果服务器开启防火墙，需要放行：
- `8003`
- 或 Nginx 使用的 `80/443`

---

## 3. 配置文件说明

项目会读取：
- `src/main/resources/application.properties`
- 外部文件：`C:/Users/caiwujiang/Desktop/alipay.yml`
- 外部文件：`C:/Users/caiwujiang/Desktop/cos.yml`

部署到服务器时，必须把这些配置改成服务器可用形式。

### 3.1 建议做法

保留仓库内 `application.properties` 的通用配置，把敏感配置放到服务器外部文件，例如：
- `/opt/book-mall/config/alipay.yml`
- `/opt/book-mall/config/cos.yml`

然后修改 `spring.config.import` 指向服务器路径。

例如：

```properties
spring.config.import=optional:file:/opt/book-mall/config/alipay.yml,optional:file:/opt/book-mall/config/cos.yml
```

### 3.2 支付宝必须配置项

至少保证以下配置存在：

```yaml
alipay:
  app-id: 你的支付宝应用ID
  private-key: 你的应用私钥
  public-key: 支付宝公钥
  seller-id: 你的商户sellerId
```

说明：
- `app-id`：回调时会校验
- `seller-id`：本轮修复后建议必须配置，否则不会启用 seller 校验
- `private-key`：发起支付用
- `public-key`：验签用

### 3.3 服务器环境下必须修改的应用配置

部署到公网服务器后，至少要修改：

```properties
server.port=8003
alipay.notify-url=https://你的后端域名/api/front/pay/alipay/notify
alipay.return-url=https://你的后端域名/api/front/pay/alipay/return
alipay.frontend-base-url=https://你的前端域名
```

注意：
- `notify-url` 不能再使用 `localhost`
- `return-url` 也应改成公网地址
- 如果前端未部署，可暂时指向后端可访问地址对应的页面域名

---

## 4. 打包与启动

### 4.1 本地打包

在项目根目录执行：

```bash
mvn test
mvn -DskipTests package
```

生成产物：
- `target/book-mall-backend-0.0.1-SNAPSHOT.jar`

### 4.2 上传到服务器

建议目录结构：

```text
/opt/book-mall/
  app/
    book-mall-backend-0.0.1-SNAPSHOT.jar
  config/
    alipay.yml
    cos.yml
  logs/
```

### 4.3 启动命令

```bash
java -jar /opt/book-mall/app/book-mall-backend-0.0.1-SNAPSHOT.jar
```

后台启动可用：

```bash
nohup java -jar /opt/book-mall/app/book-mall-backend-0.0.1-SNAPSHOT.jar > /opt/book-mall/logs/backend.log 2>&1 &
```

### 4.4 验证启动成功

访问：

```text
https://你的后端域名/swagger-ui.html
https://你的后端域名/api-docs
```

若可访问，说明服务已正常启动。

---

## 5. Nginx 反向代理示例（可选）

如果使用 Nginx，可参考：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:8003;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

如果支付宝要求使用 HTTPS，建议服务器启用证书后通过 `443` 暴露。

---

## 6. 联调前检查清单

部署完成后，先确认：

- [ ] 后端公网地址可访问
- [ ] `swagger-ui.html` 可打开
- [ ] `api-docs` 可访问
- [ ] MySQL 连接正常
- [ ] 支付宝配置已正确加载
- [ ] `alipay.notify-url` 是公网地址
- [ ] `alipay.return-url` 是公网地址
- [ ] `alipay.seller-id` 已配置
- [ ] 数据库中存在可支付订单
- [ ] 前台用户 token、后台管理员 token 可用

---

## 7. 支付联调步骤

### 7.1 创建/准备一笔未支付订单

目标条件：
- `status = 0 (UNPAID)`
- `pay_time = null`
- `trade_no = null`

建议记录：
- `orderId`
- `orderNo`
- `totalAmount`
- 对应商品 `bookId`
- 当前库存值

### 7.2 发起支付

调用：

```http
POST /api/front/orders/{id}/pay
Authorization: <前台token>
```

预期：
- 返回支付宝支付表单 HTML
- 订单状态仍为 `UNPAID`
- `payTime` 仍为空
- `tradeNo` 仍为空
- 库存不减少

### 7.3 在支付宝沙箱完成支付

在返回的表单页面完成付款。

### 7.4 验证异步通知成功

支付完成后，检查：

- 后端日志中是否收到 `/api/front/pay/alipay/notify`
- 订单状态是否变为 `PENDING_SHIP`
- `pay_time` 是否写入
- `trade_no` 是否写入
- 商品库存是否减少一次

---

## 8. 支付回调测试重点

### 8.1 成功回调预期

订单支付成功后：
- `status: 0 -> 2`
- `pay_time` 写入
- `trade_no` 写入
- 库存仅扣减一次

### 8.2 重复通知幂等

对同一笔交易再次发送相同 notify：
- 应返回成功
- 不应重复扣库存
- 不应把状态改坏

### 8.3 非法回调拒绝

下列场景应拒绝：
- 签名不合法
- `total_amount` 不匹配
- `app_id` 不匹配
- `seller_id` 不匹配
- `out_trade_no` 不存在
- 当前订单状态不是 `UNPAID`

---

## 9. 后台订单状态测试

后台接口：

```http
GET /api/admin/orders
GET /api/admin/orders/{id}
PUT /api/admin/orders/{id}
PUT /api/admin/orders/{id}/cancel
PUT /api/admin/orders/{id}/ship
```

### 9.1 必测规则

#### 应禁止
- `UNPAID -> SHIPPED`
- `UNPAID -> COMPLETED`
- `COMPLETED -> UNPAID`
- 已取消订单再次取消
- 非待发货订单直接发货

#### 应允许
- 后台取消未支付订单
- `PENDING_SHIP -> SHIPPED`

---

## 10. 服务器联调时建议保留的日志

建议临时关注：
- 支付发起日志
- 支付宝异步通知原始参数
- 订单状态变更日志
- 库存扣减结果

若要快速排查 notify，可临时在：
- `src/main/java/top/wjc/bookmallbackend/controller/front/AlipayController.java`

的 `notify()` 中打印：

```java
params.forEach((k, v) -> System.out.println(k + "=" + v));
```

联调完成后再移除。

---

## 11. 本地已验证结果（当前代码）

当前代码已完成并验证：
- `mvn test` 通过
- 前台支付接口可正常返回支付宝支付表单
- 支付发起后订单仍为 `UNPAID`
- 后台禁止未支付订单直接发货
- 后台禁止未支付订单直接改已完成
- 后台允许取消未支付订单
- 未签名 notify 请求返回 `fail`

---

## 12. 联调完成后建议回归项

- [ ] 发起支付不扣库存
- [ ] 成功 notify 后库存扣减一次
- [ ] 重复 notify 不重复扣库存
- [ ] `pay_time` 和 `trade_no` 正常写入
- [ ] `app_id` 错误时拒绝
- [ ] `seller_id` 错误时拒绝
- [ ] 金额错误时拒绝
- [ ] 后台非法状态变更继续被拦截
- [ ] 已支付待发货订单可正常发货
