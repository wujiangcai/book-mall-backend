package top.wjc.bookmallbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
/**
 * 启动时执行的轻量兼容性迁移。
 *
 * <p>项目后期引入了乐观锁字段 version。为了兼容旧数据库结构，
 * 启动时会检查 book 与 order 表是否已经包含 version 列；若没有，则自动补齐。
 */
public class SchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureVersionColumn("book");
        ensureVersionColumn("order");
        ensureUserBehaviorTable();
    }

    /**
     * 检查指定数据表是否存在 version 字段，不存在则自动执行 ALTER TABLE。
     */
    private void ensureVersionColumn(String tableName) {
        String databaseName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (databaseName == null || databaseName.isBlank()) {
            log.warn("Skip schema migration because current database name is empty");
            return;
        }

        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = ?
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = 'version'
                """, Integer.class, databaseName, tableName);

        if (count != null && count > 0) {
            return;
        }

        log.warn("Detected missing `version` column on table `{}`, applying compatibility migration", tableName);
        jdbcTemplate.execute("""
                ALTER TABLE `%s`
                    ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER `id`
                """.formatted(tableName));
        log.info("Added `version` column to table `{}`", tableName);
    }

    /**
     * 检查并创建用户行为表，用于支撑混合推荐算法的浏览、加购、购买信号。
     */
    private void ensureUserBehaviorTable() {
        String databaseName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (databaseName == null || databaseName.isBlank()) {
            log.warn("Skip user_behavior migration because current database name is empty");
            return;
        }

        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = ?
                  AND TABLE_NAME = 'user_behavior'
                """, Integer.class, databaseName);
        if (count != null && count > 0) {
            return;
        }

        log.warn("Detected missing `user_behavior` table, applying recommendation migration");
        jdbcTemplate.execute("""
                CREATE TABLE `user_behavior` (
                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '行为ID',
                  `user_id` BIGINT NOT NULL COMMENT '用户ID',
                  `book_id` BIGINT NOT NULL COMMENT '图书ID',
                  `behavior_type` VARCHAR(20) NOT NULL COMMENT '行为类型：view/cart/purchase',
                  `score` INT NOT NULL DEFAULT 1 COMMENT '行为权重分',
                  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                  PRIMARY KEY (`id`),
                  KEY `idx_user_book` (`user_id`, `book_id`),
                  KEY `idx_book_type` (`book_id`, `behavior_type`),
                  KEY `idx_create_time` (`create_time`),
                  CONSTRAINT `fk_behavior_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                  CONSTRAINT `fk_behavior_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为表';
                """);
        log.info("Created `user_behavior` table");
    }
}
