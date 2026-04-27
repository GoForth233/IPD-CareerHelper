USE `career_db`;

ALTER TABLE `user_auths`
  MODIFY COLUMN `identity_type` VARCHAR(32) NOT NULL COMMENT '登录类型: EMAIL_PASSWORD, WECHAT, legacy PASSWORD';

CREATE UNIQUE INDEX `uk_identity_identifier`
ON `user_auths` (`identity_type`, `identifier`);

INSERT INTO `roles` (`role_code`, `role_name`, `description`, `is_system`)
SELECT 'STUDENT', 'Student', 'Default role for student-side users', 1
WHERE NOT EXISTS (
  SELECT 1 FROM `roles` WHERE `role_code` = 'STUDENT'
);

INSERT INTO `user_roles` (`user_id`, `role_id`)
SELECT u.`user_id`, r.`role_id`
FROM `users` u
JOIN `roles` r ON r.`role_code` = 'STUDENT'
LEFT JOIN `user_roles` ur ON ur.`user_id` = u.`user_id` AND ur.`role_id` = r.`role_id`
WHERE ur.`id` IS NULL;
