package top.wjc.bookmallbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureVersionColumn("book");
        ensureVersionColumn("order");
    }

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
}
