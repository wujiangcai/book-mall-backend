# 乐观锁版本号改造数据库说明

本文档对应本次把项目从“条件更新 + 影响行数”改为“`version` 字段传统乐观锁”的改造。

## 1. 改了什么

本次数据库层只改两张表：

- `book`
- ``order``

新增字段：

- `version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'`

业务语义：

- 新增数据时，`version` 初始值为 `0`
- 每次成功更新时，`version = version + 1`
- 更新 SQL 会带上 `WHERE version = ?`

## 2. 需要执行的 SQL

仓库里已经提供了升级脚本：

- [upgrade_optimistic_lock.sql](/C:/Users/caiwujiang/Desktop/book-mall-backend/src/main/resources/sql/upgrade_optimistic_lock.sql)

内容如下：

```sql
ALTER TABLE `book`
    ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER `id`;

ALTER TABLE `order`
    ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER `id`;
```

如果你是新建库，直接使用更新后的：

- [schema.sql](/C:/Users/caiwujiang/Desktop/book-mall-backend/src/main/resources/sql/schema.sql)

如果你是升级已有远端环境，只需要执行 `upgrade_optimistic_lock.sql`。

## 3. 推荐上线顺序

推荐顺序：

1. 备份数据库
2. 停应用，或至少让实例不再接收新流量
3. 执行升级 SQL
4. 校验字段已加成功
5. 部署新版本应用
6. 做支付回调和订单流转的冒烟验证

原因：

- 新版本代码查询和更新时已经显式依赖 `version` 字段
- 如果先发新代码、后改库，新代码启动后访问数据库会失败
- 先改库、后发代码是安全的，因为旧代码不会使用 `version` 字段

## 4. 上线前检查

先确认目标库当前还没有这两个字段：

```sql
SELECT TABLE_NAME, COLUMN_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'book_mall'
  AND TABLE_NAME IN ('book', 'order')
  AND COLUMN_NAME = 'version';
```

如果结果为空，说明还没加过。

## 5. 执行步骤示例

### 方式一：在 MySQL 客户端手动执行

```sql
USE book_mall;

ALTER TABLE `book`
    ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER `id`;

ALTER TABLE `order`
    ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER `id`;
```

### 方式二：执行脚本文件

```bash
mysql -u <user> -p book_mall < src/main/resources/sql/upgrade_optimistic_lock.sql
```

## 6. 执行后校验

校验字段：

```sql
SHOW COLUMNS FROM `book` LIKE 'version';
SHOW COLUMNS FROM `order` LIKE 'version';
```

校验默认值：

```sql
SELECT COLUMN_NAME, COLUMN_DEFAULT, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'book_mall'
  AND TABLE_NAME IN ('book', 'order')
  AND COLUMN_NAME = 'version';
```

预期结果：

- `COLUMN_DEFAULT = 0`
- `IS_NULLABLE = NO`

## 7. 上线后的功能验证

建议至少验证这几条：

1. 新建订单正常
2. 发起支付正常
3. 支付回调后订单从 `UNPAID` 变为 `PENDING_SHIP`
4. 图书库存正常扣减
5. 同一支付回调重复通知时，不会重复扣库存
6. 管理端修改订单、发货、取消不报错

## 8. 回滚建议

最稳妥的回滚方式是只回滚应用，不立刻回滚数据库。

原因：

- 旧版本代码可以兼容表里多出一个 `version` 字段
- 新字段是新增列，不会破坏原有数据
- 这样回滚成本最低，风险也最小

如果确实需要回滚数据库，再在应用回退完成后执行：

```sql
ALTER TABLE `book` DROP COLUMN `version`;
ALTER TABLE `order` DROP COLUMN `version`;
```

不建议在新版本应用仍运行时直接删字段。

## 9. 额外说明

这次改造后，库存扣减和订单支付状态更新已经改成基于 `version` 的传统乐观锁。

核心行为变为：

- 先读当前记录版本号
- 更新时带 `WHERE version = ?`
- 更新成功后自动 `version + 1`
- 遇到并发冲突时，支付链路会做有限重试
- 普通后台修改遇到冲突时，接口返回 `409` 和提示：`数据已变更，请刷新后重试`
