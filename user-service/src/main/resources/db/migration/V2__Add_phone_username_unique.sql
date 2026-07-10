-- Add phone column
ALTER TABLE `user` ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号' AFTER `email`;

-- Make email nullable (was NOT NULL)
ALTER TABLE `user` MODIFY COLUMN `email` VARCHAR(255) DEFAULT NULL COMMENT '邮箱地址';

-- Add unique constraint on username
ALTER TABLE `user` ADD UNIQUE KEY `uk_username` (`username`);
ALTER TABLE `user` ADD INDEX `idx_username` (`username`);
