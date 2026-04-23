DELIMITER $$

DROP PROCEDURE IF EXISTS add_version_column_if_missing $$
CREATE PROCEDURE add_version_column_if_missing(IN target_table VARCHAR(64))
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = target_table
          AND COLUMN_NAME = 'version'
    ) THEN
        SET @sql = CONCAT(
            'ALTER TABLE `', target_table,
            '` ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号'' AFTER `id`'
        );
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

CALL add_version_column_if_missing('book') $$
CALL add_version_column_if_missing('order') $$

DROP PROCEDURE add_version_column_if_missing $$

DELIMITER ;
