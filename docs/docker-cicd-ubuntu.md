# Ubuntu Docker 自动发布说明

本文档用于把 `book-mall-backend` 部署到 Ubuntu 服务器，并在每次 push 到 `main` 后自动构建镜像、推送到腾讯云 TCR、重启服务器容器。

## 1. 方案结构

- 代码仓库：GitHub
- CI/CD：GitHub Actions
- 镜像仓库：腾讯云 TCR 个人版
- 服务器：Ubuntu + Docker + Docker Compose
- 数据库：已有 MySQL

默认发布逻辑：

1. 本地提交代码并 push 到 `main`
2. GitHub Actions 执行 `./mvnw test`
3. 构建 Docker 镜像并推送到 `ccr.ccs.tencentyun.com/<namespace>/book-mall-backend:latest`
4. Actions 通过 SSH 登录 Ubuntu 服务器并同步部署文件
5. 服务器执行 `docker compose pull && docker compose up -d`

当前部署模板针对这台 Ubuntu 服务器上的本机 MySQL 做了适配：

- MySQL 监听在 `127.0.0.1:3306`
- 后端容器使用 `network_mode: host`
- 因此容器内访问 `localhost:3306` 实际就是宿主机 MySQL

这意味着当前模板适用于 Linux 服务器，不适用于 Docker Desktop 的默认网络模型。

## 2. 版本策略

不建议每次提交都去修改 `pom.xml` 版本号。

当前工作流会同时推送两个 tag：

- `latest`
- 当前提交的 git SHA

这比自动改 `pom.xml` 更稳，因为：

- 每次发布都可追溯到具体 commit
- 不会制造无意义的版本提交
- 回滚时可以直接切换到历史镜像 tag

如果你后面确实需要语义化版本，例如 `1.0.3`，建议用 Git tag 驱动发布，而不是每次 push 自动改项目版本。

## 3. 需要你在 GitHub 仓库中配置的 Secrets

进入仓库 `Settings -> Secrets and variables -> Actions`，添加：

- `DEPLOY_HOST`：Ubuntu 公网 IP 或域名，必须能被 GitHub Hosted Runner 访问
- `DEPLOY_PORT`：SSH 端口，默认 `22`，不是应用端口 `8003`
- `DEPLOY_USER`：服务器登录用户
- `DEPLOY_SSH_KEY`：对应私钥内容
- `DEPLOY_HOST_KEY`：服务器 SSH host key 的 `known_hosts` 格式整行内容，强烈推荐配置
- `TCR_USERNAME`：腾讯云账号 ID
- `TCR_PASSWORD`：TCR 个人版初始化密码
- `TCR_NAMESPACE`：TCR 命名空间，例如 `wujiangcai`

## 4. 推荐配置 SSH host key 固定值

为避免工作流强依赖在线 `ssh-keyscan`，建议在可信机器上先拿到服务器 host key，并保存到 `DEPLOY_HOST_KEY`。

例如：

```bash
ssh-keyscan -p <SSH端口> <服务器公网IP或域名>
```

如果你的 SSH 不是默认 22 端口，`known_hosts` 一般会带端口格式，例如：

```text
[example.com]:2222 ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAI...
```

建议你先在本地确认：

1. `ssh -p <SSH端口> <用户>@<主机>` 能正常登录
2. `ssh-keyscan` 拿到的 host key 与服务器实际 host key 一致
3. 再把整行内容保存到 GitHub Secret `DEPLOY_HOST_KEY`

如果服务器重装、迁移或重置了 SSH host key，需要同步更新这个 Secret。

## 5. 首次服务器准备

在 Ubuntu 服务器执行：

```bash
sudo mkdir -p /opt/book-mall/config /opt/book-mall/scripts
sudo chown -R $USER:$USER /opt/book-mall
```

把以下文件上传到服务器：

- `deploy/docker-compose.prod.yml` -> `/opt/book-mall/docker-compose.prod.yml`
- `deploy/.env.prod.example` -> `/opt/book-mall/.env.prod`
- `scripts/deploy-prod.sh` -> `/opt/book-mall/scripts/deploy-prod.sh`

再修改 `/opt/book-mall/.env.prod`，填入真实环境变量，尤其是：

- `IMAGE_NAME=ccr.ccs.tencentyun.com/<你的 TCR 命名空间>/book-mall-backend:latest`

## 6. 服务器登录腾讯云 TCR

根据腾讯云 TCR 个人版快速入门，个人版通过 Docker CLI 登录的命令格式为：

```bash
docker login ccr.ccs.tencentyun.com --username=<腾讯云账号ID>
```

其中用户名是腾讯云账号 ID，密码是你在 TCR 控制台初始化的固定密码。

服务器执行：

```bash
docker login ccr.ccs.tencentyun.com -u <腾讯云账号ID>
```

参考：
- 腾讯云 TCR 个人版快速入门：https://cloud.tencent.com/document/product/1141/63910

## 7. 外部配置文件

后端会从 `/app/config` 读取配置，服务器上对应目录是：

```text
/opt/book-mall/config
```

你需要在这里准备：

- `/opt/book-mall/config/alipay.yml`
- `/opt/book-mall/config/cos.yml`

## 8. 首次手工发布

第一次建议先在服务器手工验证一次：

```bash
cd /opt/book-mall
chmod +x scripts/deploy-prod.sh
APP_DIR=/opt/book-mall ./scripts/deploy-prod.sh
docker ps
docker logs -f book-mall-backend
```

## 9. 之后的自动发布

后续流程就是：

```text
本地修改 -> git add -> git commit -> git push origin main
```

只要 Actions 成功，服务器容器就会自动更新。

## 10. 常见问题排查

### 1. 日志提示 `TCP connection to <host>:<port> failed from runner`
优先检查：

- `DEPLOY_HOST` 是否真的是公网 IP / 公网域名
- `DEPLOY_PORT` 是否真的是 SSH 端口
- 云服务器安全组是否放通对应端口
- 服务器本机防火墙（如 `ufw`）是否放通对应端口
- `sshd` 是否监听了该端口，并且不是只监听 `127.0.0.1`

### 2. TCP 可达但 `ssh-keyscan` 失败
这通常表示：

- 该端口不是标准 SSH 服务
- 中间网络设备拦截了 SSH host key 返回
- SSH 服务异常

此时优先配置 `DEPLOY_HOST_KEY`，让工作流直接使用固定 host key，而不是依赖在线 `ssh-keyscan`。

### 3. SSH 登录验证失败
优先检查：

- `DEPLOY_USER` 是否正确
- `DEPLOY_SSH_KEY` 是否与服务端 `authorized_keys` 匹配
- 私钥内容是否完整，是否被错误裁剪

## 11. 回滚

如果新版本有问题，可以把 `docker-compose.prod.yml` 中的镜像 tag 从 `latest` 改成某个历史 SHA，再执行：

```bash
cd /opt/book-mall
docker compose --env-file .env.prod -f docker-compose.prod.yml pull
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d
```
