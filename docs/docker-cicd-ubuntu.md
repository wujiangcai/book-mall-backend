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

- `DEPLOY_HOST`：Ubuntu 公网 IP 或域名
- `DEPLOY_PORT`：SSH 端口，默认 `22`
- `DEPLOY_USER`：服务器登录用户
- `DEPLOY_SSH_KEY`：对应私钥内容
- `DEPLOY_HOST_KEY`：推荐配置，服务器 SSH host key 对应的 `known_hosts` 条目
- `TCR_USERNAME`：腾讯云账号 ID
- `TCR_PASSWORD`：TCR 个人版初始化密码
- `TCR_NAMESPACE`：TCR 命名空间，例如 `wujiangcai`

`DEPLOY_HOST_KEY` 不是强制项。

- 如果已配置，workflow 会直接把该条目写入 `~/.ssh/known_hosts`
- 如果未配置，workflow 会按 `DEPLOY_PORT` 执行 `ssh-keyscan` 自动写入

推荐在本地先生成并核对后，再保存到 GitHub Secret，例如：

```bash
ssh-keyscan -p <SSH端口> -H <服务器IP或域名>
```

## 4. 首次服务器准备

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

## 5. 服务器登录腾讯云 TCR

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

## 6. 外部配置文件

后端会从 `/app/config` 读取配置，服务器上对应目录是：

```text
/opt/book-mall/config
```

你需要在这里准备：

- `/opt/book-mall/config/alipay.yml`
- `/opt/book-mall/config/cos.yml`

## 7. 首次手工发布

第一次建议先在服务器手工验证一次：

```bash
cd /opt/book-mall
chmod +x scripts/deploy-prod.sh
APP_DIR=/opt/book-mall ./scripts/deploy-prod.sh
docker ps
docker logs -f book-mall-backend
```

## 8. 之后的自动发布

后续流程就是：

```text
本地修改 -> git add -> git commit -> git push origin main
```

只要 Actions 成功，服务器容器就会自动更新。

## 9. 回滚

如果新版本有问题，可以把 `docker-compose.prod.yml` 中的镜像 tag 从 `latest` 改成某个历史 SHA，再执行：

```bash
cd /opt/book-mall
docker compose --env-file .env.prod -f docker-compose.prod.yml pull
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d
```
