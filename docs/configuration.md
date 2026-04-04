# 配置与部署说明

本文档说明 `book-mall-backend` 的配置组织方式，以及本地开发和服务器上线时应如何准备配置。

## 1. 配置目标

当前项目采用以下原则：

- 仓库内只保留可公开的通用配置和模板
- 密钥、私钥、Secret 等敏感信息不直接提交到 Git
- 本地开发和生产环境通过 Spring Profile 区分
- 真实敏感配置统一放在仓库外部或被忽略的 `config/` 目录

## 2. 配置文件结构

项目内配置：

- `src/main/resources/application.properties`
  - 公共配置
  - 默认激活 `local` profile
  - 默认从 `APP_CONFIG_DIR` 指向的目录加载外部密钥文件
- `src/main/resources/application-local.properties`
  - 本地开发默认配置
  - 包含本地 CORS 和本地回调地址
- `src/main/resources/application-prod.properties`
  - 生产环境默认配置
  - 域名、回调地址等通过环境变量覆盖

仓库内模板：

- `config/alipay.example.yml`
- `config/cos.example.yml`

真实配置文件，不提交 Git：

- `config/alipay.yml`
- `config/cos.yml`

忽略规则见：

- `.gitignore`

## 3. 运行时加载规则

默认配置如下：

```properties
spring.profiles.active=${SPRING_PROFILES_ACTIVE:local}
spring.config.import=optional:file:${APP_CONFIG_DIR:./config}/alipay.yml,optional:file:${APP_CONFIG_DIR:./config}/cos.yml
```

含义：

- 如果未设置 `SPRING_PROFILES_ACTIVE`，默认使用 `local`
- 如果未设置 `APP_CONFIG_DIR`，默认从当前运行目录下的 `./config` 读取外部配置
- `alipay.yml` 和 `cos.yml` 不存在时不会导致应用启动失败，但对应功能会不可用

## 4. 本地开发配置

### 4.1 默认行为

本地直接启动时，默认使用：

- Profile: `local`
- 外部配置目录：`./config`

也就是在项目根目录下启动时，程序会尝试读取：

- `config/alipay.yml`
- `config/cos.yml`

### 4.2 创建本地配置文件

在项目根目录执行：

```powershell
Copy-Item config/alipay.example.yml config/alipay.yml
Copy-Item config/cos.example.yml config/cos.yml
```

然后编辑这两个文件。

### 4.3 本地 `alipay.yml` 示例

```yaml
alipay:
  app-id: 你的支付宝应用ID
  private-key: 你的应用私钥
  public-key: 支付宝公钥
  seller-id: 你的商户sellerId
```

说明：

- `app-id`、`private-key`、`public-key`、`seller-id` 都属于敏感配置
- 不要把真实值写回 `application.properties`

### 4.4 本地 `cos.yml` 示例

```yaml
cos:
  secret-id: 你的腾讯云SecretId
  secret-key: 你的腾讯云SecretKey
  region: ap-guangzhou
  bucket: 你的bucket名称
  base-url: https://你的bucket.cos.ap-guangzhou.myqcloud.com
  sign-duration-seconds: 604800
```

说明：

- 上传轮播图、图书封面时需要 COS 配置完整
- 至少要保证以下字段有效：
  - `secret-id`
  - `secret-key`
  - `region`
  - `bucket`
  - `base-url`

### 4.5 本地启动方式

直接运行：

```powershell
.\mvnw.cmd spring-boot:run
```

或先打包再运行：

```powershell
.\mvnw.cmd test
java -jar .\target\book-mall-backend-0.0.1-SNAPSHOT.jar
```

### 4.6 本地常用环境变量

如果你不想把部分配置写进 `config/*.yml`，也可以用环境变量覆盖：

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
$env:APP_CONFIG_DIR="C:\Users\yourname\Desktop\book-mall-config"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-password"
$env:JWT_SECRET="your-jwt-secret"
```

## 5. 生产环境配置

### 5.1 推荐目录结构

以 Linux 服务器为例：

```text
/opt/book-mall/
  app/
    book-mall-backend-0.0.1-SNAPSHOT.jar
  config/
    alipay.yml
    cos.yml
  logs/
```

### 5.2 推荐环境变量

生产环境建议至少设置：

```bash
export SPRING_PROFILES_ACTIVE=prod
export APP_CONFIG_DIR=/opt/book-mall/config
export DB_USERNAME=bookmall
export DB_PASSWORD=your-db-password
export JWT_SECRET=your-jwt-secret
export CORS_ALLOWED_ORIGINS=https://your-frontend-domain
export ALIPAY_NOTIFY_URL=https://your-backend-domain/api/front/pay/alipay/notify
export ALIPAY_RETURN_URL=https://your-backend-domain/api/front/pay/alipay/return
export ALIPAY_FRONTEND_BASE_URL=https://your-frontend-domain
```

说明：

- 生产环境不要继续使用 `local`
- 生产域名、回调地址、CORS 来源应使用公网域名
- `notify-url` 和 `return-url` 不能使用 `localhost`

### 5.3 生产外部文件

服务器上的 `/opt/book-mall/config/alipay.yml` 与 `/opt/book-mall/config/cos.yml` 结构可直接参考：

- `config/alipay.example.yml`
- `config/cos.example.yml`

### 5.4 生产启动命令

```bash
java -jar /opt/book-mall/app/book-mall-backend-0.0.1-SNAPSHOT.jar
```

后台启动可用：

```bash
nohup java -jar /opt/book-mall/app/book-mall-backend-0.0.1-SNAPSHOT.jar > /opt/book-mall/logs/backend.log 2>&1 &
```

## 6. 关键配置项说明

### 6.1 数据库

公共配置中已保留：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/book_mall?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```

建议：

- 本地可以通过环境变量设置数据库账号密码
- 生产环境必须通过环境变量或外部配置注入

### 6.2 JWT

```properties
jwt.secret=${JWT_SECRET:your-secret-key-change-in-production}
```

要求：

- 开发环境可以临时使用自定义值
- 生产环境必须设置强随机密钥

### 6.3 CORS

- 本地默认来自 `application-local.properties`
- 生产默认来自 `application-prod.properties`

生产环境建议显式设置：

```bash
export CORS_ALLOWED_ORIGINS=https://your-frontend-domain
```

### 6.4 COS

如果后台上传轮播图或图书封面时报 `COS未配置`，优先检查：

1. `cos.yml` 是否被正确加载
2. `secret-id`、`secret-key`、`region` 是否为空
3. `bucket`、`base-url` 是否为空
4. 应用是否在修改配置后重启

## 7. 提交到 Git 前的检查

提交前建议确认：

- `config/alipay.yml` 没有被提交
- `config/cos.yml` 没有被提交
- 仓库中只保留 `.example.yml` 模板
- 没有把真实密码、私钥、Secret 写进 `application*.properties`

可用以下命令检查：

```powershell
git status
git diff
```

## 8. 常见问题

### 8.1 为什么要把真实配置放到 `config/`？

因为 `config/*.yml` 已被 `.gitignore` 忽略，适合保存本地或服务器的真实敏感配置。

### 8.2 为什么还保留 `.example.yml`？

为了让新环境快速知道需要哪些字段，以及字段结构是什么。

### 8.3 什么时候用环境变量，什么时候用外部文件？

建议：

- 密钥、私钥、COS 密钥：优先外部文件
- 部署地址、Profile、数据库密码：环境变量或外部文件都可以

### 8.4 生产和本地配置如何区分？

通过 `SPRING_PROFILES_ACTIVE`：

- `local` 用于本地开发
- `prod` 用于生产部署

## 9. 相关文件

- `src/main/resources/application.properties`
- `src/main/resources/application-local.properties`
- `src/main/resources/application-prod.properties`
- `config/README.md`
- `config/alipay.example.yml`
- `config/cos.example.yml`
- `docs/deployment-payment-test.md`
