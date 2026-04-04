# 生产环境上线部署说明

本文档用于将 `book-mall-backend` 部署到生产环境，并明确说明当前代码在支付宝、外部配置、前后端地址上的实际行为。

## 1. 当前代码的关键事实

上线前先确认以下几点：

- Spring Profile 使用的是 `prod`，不是 `product`
- 后端默认端口是 `8003`
- 后端默认通过 `APP_CONFIG_DIR` 加载外部配置文件：
  - `alipay.yml`
  - `cos.yml`
- 代码里的支付宝网关默认值仍然是沙箱地址：

```properties
alipay.gateway-url=https://openapi-sandbox.dl.alipaydev.com/gateway.do
```

这意味着：

- 本地联调可以继续走沙箱
- 正式上线时必须在外部 `alipay.yml` 中覆盖成正式网关
- 如果不覆盖，生产环境也会继续请求支付宝沙箱

正式网关应配置为：

```yaml
alipay:
  gateway-url: https://openapi.alipay.com/gateway.do
```

## 2. 服务器环境要求

建议准备：

- Linux 服务器一台
- Java 17
- MySQL 8+
- Nginx
- 可公网访问的前端地址和后端地址

默认涉及端口：

- 后端：`8003`
- 前端静态站点：`80` 或 `443`

如果直接用 IP `101.35.245.13` 部署，至少要保证：

- `101.35.245.13:8003` 可被前端访问
- 前端页面能通过 `http://101.35.245.13` 或你的正式域名打开

## 3. 推荐目录结构

```text
/opt/book-mall/
  app/
    book-mall-backend-0.0.1-SNAPSHOT.jar
  config/
    alipay.yml
    cos.yml
  logs/
  frontend/
    dist/
```

说明：

- `app/` 放后端 jar
- `config/` 放外部敏感配置
- `logs/` 放运行日志
- `frontend/dist/` 放前端构建产物

## 4. 后端生产配置

### 4.1 必填环境变量

生产环境建议至少配置：

```bash
export SPRING_PROFILES_ACTIVE=prod
export APP_CONFIG_DIR=/opt/book-mall/config
export DB_USERNAME=bookmall
export DB_PASSWORD=your-db-password
export JWT_SECRET=replace-with-a-strong-random-secret
export CORS_ALLOWED_ORIGINS=http://101.35.245.13
export ALIPAY_NOTIFY_URL=http://101.35.245.13:8003/api/front/pay/alipay/notify
export ALIPAY_RETURN_URL=http://101.35.245.13:8003/api/front/pay/alipay/return
export ALIPAY_FRONTEND_BASE_URL=http://101.35.245.13
```

如果你已经有正式域名，应替换为域名版本：

```bash
export CORS_ALLOWED_ORIGINS=https://your-frontend-domain
export ALIPAY_NOTIFY_URL=https://your-backend-domain/api/front/pay/alipay/notify
export ALIPAY_RETURN_URL=https://your-backend-domain/api/front/pay/alipay/return
export ALIPAY_FRONTEND_BASE_URL=https://your-frontend-domain
```

注意：

- `CORS_ALLOWED_ORIGINS` 必须写前端页面真实访问源
- `ALIPAY_FRONTEND_BASE_URL` 必须和前端实际访问地址一致
- 支付完成后，后端会按 `ALIPAY_FRONTEND_BASE_URL` 重定向回前端订单页
- `notify-url` 和 `return-url` 不能写 `localhost`

### 4.2 `alipay.yml` 正确写法

`/opt/book-mall/config/alipay.yml` 建议这样写：

```yaml
alipay:
  gateway-url: https://openapi.alipay.com/gateway.do
  app-id: your-app-id
  private-key: |
    your-private-key
  public-key: |
    your-alipay-public-key
  seller-id: your-seller-id
```

注意：

- 顶层只能是一层 `alipay:`
- 不要写成双层结构，例如 `alipay.alipay.app-id`
- `gateway-url` 在正式环境必须显式写正式网关

如果你只是联调沙箱，可以临时写成：

```yaml
alipay:
  gateway-url: https://openapi-sandbox.dl.alipaydev.com/gateway.do
  app-id: your-sandbox-app-id
  private-key: |
    your-private-key
  public-key: |
    your-alipay-public-key
  seller-id: your-sandbox-seller-id
```

### 4.3 `cos.yml` 示例

`/opt/book-mall/config/cos.yml`：

```yaml
cos:
  secret-id: your-secret-id
  secret-key: your-secret-key
  region: ap-nanjing
  bucket: your-bucket
  base-url: https://your-bucket.cos.ap-nanjing.myqcloud.com
  sign-duration-seconds: 604800
```

## 5. 后端打包与启动

### 5.1 本地打包

在项目根目录执行：

```bash
./mvnw -DskipTests package
```

Windows 下可用：

```powershell
.\mvnw.cmd -DskipTests package
```

产物默认在：

```text
target/book-mall-backend-0.0.1-SNAPSHOT.jar
```

### 5.2 上传到服务器

将以下内容放到服务器：

- `target/book-mall-backend-0.0.1-SNAPSHOT.jar` -> `/opt/book-mall/app/`
- `/opt/book-mall/config/alipay.yml`
- `/opt/book-mall/config/cos.yml`

### 5.3 启动方式

直接启动：

```bash
java -jar /opt/book-mall/app/book-mall-backend-0.0.1-SNAPSHOT.jar
```

后台运行：

```bash
nohup java -jar /opt/book-mall/app/book-mall-backend-0.0.1-SNAPSHOT.jar > /opt/book-mall/logs/backend.log 2>&1 &
```

## 6. systemd 方式启动

生产环境更建议使用 `systemd`。

创建 `/etc/systemd/system/book-mall-backend.service`：

```ini
[Unit]
Description=book mall backend
After=network.target

[Service]
Type=simple
WorkingDirectory=/opt/book-mall/app
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=APP_CONFIG_DIR=/opt/book-mall/config
Environment=DB_USERNAME=bookmall
Environment=DB_PASSWORD=your-db-password
Environment=JWT_SECRET=replace-with-a-strong-random-secret
Environment=CORS_ALLOWED_ORIGINS=http://101.35.245.13
Environment=ALIPAY_NOTIFY_URL=http://101.35.245.13:8003/api/front/pay/alipay/notify
Environment=ALIPAY_RETURN_URL=http://101.35.245.13:8003/api/front/pay/alipay/return
Environment=ALIPAY_FRONTEND_BASE_URL=http://101.35.245.13
ExecStart=/usr/bin/java -jar /opt/book-mall/app/book-mall-backend-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5
StandardOutput=append:/opt/book-mall/logs/backend.log
StandardError=append:/opt/book-mall/logs/backend.log

[Install]
WantedBy=multi-user.target
```

执行：

```bash
sudo systemctl daemon-reload
sudo systemctl enable book-mall-backend
sudo systemctl start book-mall-backend
sudo systemctl status book-mall-backend
```

## 7. 前端生产部署

当前前端通过 `VITE_API_BASE` 指向后端地址。

如果你直接用 IP 部署，生产环境变量可写：

```dotenv
VITE_API_BASE=http://101.35.245.13:8003
```

前端构建：

```bash
cd frontend
npm install
npm run build
```

构建完成后将 `frontend/dist` 部署到 Nginx 静态目录，例如：

```text
/opt/book-mall/frontend/dist
```

## 8. Nginx 配置示例

### 8.1 前端站点

```nginx
server {
    listen 80;
    server_name 101.35.245.13;

    root /opt/book-mall/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 8.2 后端反向代理

如果你想把后端也走 Nginx：

```nginx
server {
    listen 80;
    server_name api.your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:8003;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

如果使用 HTTPS，支付宝正式回调也应使用 HTTPS 地址。

## 9. 上线检查清单

上线前至少检查：

- `SPRING_PROFILES_ACTIVE=prod`
- `/opt/book-mall/config/alipay.yml` 已存在
- `/opt/book-mall/config/cos.yml` 已存在
- `alipay.yml` 使用单层 `alipay:`
- 正式环境已将 `alipay.gateway-url` 改为 `https://openapi.alipay.com/gateway.do`
- `ALIPAY_NOTIFY_URL` 是公网可访问地址
- `ALIPAY_RETURN_URL` 是公网可访问地址
- `ALIPAY_FRONTEND_BASE_URL` 与前端实际访问地址一致
- `CORS_ALLOWED_ORIGINS` 与前端真实 origin 一致
- MySQL 可连接
- `8003` 端口或反向代理端口已放行

## 10. 启动后验证

后端启动后可检查：

```text
http://101.35.245.13:8003/swagger-ui.html
http://101.35.245.13:8003/api-docs
```

如果使用域名或 Nginx，则替换为对应正式地址。

还应验证：

- 前端登录正常
- 后端接口无跨域报错
- 图片上传正常
- 支付下单能拉起支付宝
- 支付成功后异步通知能回到后端
- 支付成功后页面能正确跳回前端订单页

## 11. 常见错误

### 11.1 支付还是走了沙箱

原因通常是：

- 没有在外部 `alipay.yml` 覆盖 `gateway-url`
- 覆盖文件没有被 `APP_CONFIG_DIR` 正确加载

### 11.2 支付成功后页面没跳回前端

通常检查：

- `ALIPAY_FRONTEND_BASE_URL` 是否正确
- 前端路由地址是否可访问

### 11.3 前端请求被跨域拦截

通常检查：

- `CORS_ALLOWED_ORIGINS` 是否和前端真实 origin 完全一致
- 是否错误地把前端地址从 `http://101.35.245.13` 写成了 `http://101.35.245.13:8003`

### 11.4 支付宝回调失败

通常检查：

- `ALIPAY_NOTIFY_URL` 是否公网可访问
- 是否使用了 `localhost`
- 正式环境是否已启用 HTTPS
- `app-id`、`seller-id`、公钥是否匹配
