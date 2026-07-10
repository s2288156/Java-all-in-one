CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `keycloak_id` VARCHAR(36) DEFAULT NULL COMMENT 'Keycloak用户ID',
    `email` VARCHAR(255) NOT NULL COMMENT '邮箱地址',
    `username` VARCHAR(255) NOT NULL COMMENT '用户名',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_keycloak_id` (`keycloak_id`),
    UNIQUE KEY `uk_email` (`email`),
    INDEX `idx_keycloak_id` (`keycloak_id`),
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
