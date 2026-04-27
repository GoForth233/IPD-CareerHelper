-- ==========================================
-- Career Platform 增量补强脚本（极速兼容版：无外键）
-- 目的：先一把执行成功，支撑联调
-- 说明：
-- 1) 不 DROP 旧表，尽量保留数据
-- 2) 使用信息架构判断，幂等加列/索引
-- 3) 暂不创建 FOREIGN KEY（避免 3780 类型不兼容）
-- ==========================================

SET NAMES utf8mb4;
USE `career_db`;

DROP PROCEDURE IF EXISTS `sp_add_column_if_missing`;
DELIMITER $$
CREATE PROCEDURE `sp_add_column_if_missing`(
  IN p_table VARCHAR(64),
  IN p_column VARCHAR(64),
  IN p_definition TEXT
)
BEGIN
  DECLARE v_cnt INT DEFAULT 0;
  SELECT COUNT(*) INTO v_cnt
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = p_table
    AND COLUMN_NAME = p_column;

  IF v_cnt = 0 THEN
    SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS `sp_add_index_if_missing`;
DELIMITER $$
CREATE PROCEDURE `sp_add_index_if_missing`(
  IN p_table VARCHAR(64),
  IN p_index VARCHAR(64),
  IN p_index_ddl TEXT
)
BEGIN
  DECLARE v_cnt INT DEFAULT 0;
  SELECT COUNT(*) INTO v_cnt
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = p_table
    AND INDEX_NAME = p_index;

  IF v_cnt = 0 THEN
    SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD INDEX `', p_index, '` ', p_index_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$
DELIMITER ;

-- A. 低风险字段补强
CALL sp_add_column_if_missing('assessment_scales', 'version', "VARCHAR(20) NOT NULL DEFAULT 'v1' COMMENT '量表版本'");
CALL sp_add_column_if_missing('assessment_scales', 'is_active', "TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否当前生效版本'");

CALL sp_add_column_if_missing('career_paths', 'version', "VARCHAR(20) NOT NULL DEFAULT 'v1' COMMENT '路线版本'");
CALL sp_add_column_if_missing('career_paths', 'is_active', "TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否生效'");

CALL sp_add_column_if_missing('assistant_sessions', 'model_name', "VARCHAR(50) DEFAULT NULL COMMENT '会话默认模型'");

CALL sp_add_column_if_missing('assistant_messages', 'prompt_tokens', "INT NOT NULL DEFAULT 0 COMMENT '输入token'");
CALL sp_add_column_if_missing('assistant_messages', 'completion_tokens', "INT NOT NULL DEFAULT 0 COMMENT '输出token'");
CALL sp_add_column_if_missing('assistant_messages', 'total_tokens', "INT NOT NULL DEFAULT 0 COMMENT '总token'");
CALL sp_add_column_if_missing('assistant_messages', 'cost_micros', "BIGINT NOT NULL DEFAULT 0 COMMENT '成本(百万分之一单位)'");

CALL sp_add_column_if_missing('user_career_progress', 'progress_percent', "TINYINT NOT NULL DEFAULT 0 COMMENT '进度0-100'");
CALL sp_add_column_if_missing('user_career_progress', 'last_cleared_at', "DATETIME DEFAULT NULL COMMENT '最近完成时间'");

-- B. 测评题库闭环
CREATE TABLE IF NOT EXISTS `assessment_questions` (
  `question_id` BIGINT NOT NULL AUTO_INCREMENT,
  `scale_id` BIGINT NOT NULL,
  `question_text` TEXT NOT NULL COMMENT '题干',
  `question_type` ENUM('SINGLE', 'MULTIPLE', 'TEXT', 'SCORE') NOT NULL DEFAULT 'SINGLE',
  `dimension_code` VARCHAR(50) DEFAULT NULL COMMENT '能力维度编码',
  `sort_order` INT NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`),
  KEY `idx_scale_question_order` (`scale_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评题目';

CREATE TABLE IF NOT EXISTS `assessment_options` (
  `option_id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL,
  `option_label` VARCHAR(20) DEFAULT NULL COMMENT '选项标签A/B/C',
  `option_text` VARCHAR(255) NOT NULL COMMENT '选项内容',
  `score_value` DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '该选项分值',
  `dimension_code` VARCHAR(50) DEFAULT NULL COMMENT '维度编码',
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`option_id`),
  KEY `idx_question_option_order` (`question_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评选项';

CREATE TABLE IF NOT EXISTS `assessment_answers` (
  `answer_id` BIGINT NOT NULL AUTO_INCREMENT,
  `record_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `option_id` BIGINT DEFAULT NULL,
  `answer_text` TEXT DEFAULT NULL COMMENT '文本题作答',
  `score_snapshot` DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '答题得分快照',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`answer_id`),
  UNIQUE KEY `uk_record_question` (`record_id`, `question_id`),
  KEY `idx_answer_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评作答明细';

-- C. RBAC
CREATE TABLE IF NOT EXISTS `roles` (
  `role_id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_code` VARCHAR(50) NOT NULL,
  `role_name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `is_system` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色定义';

CREATE TABLE IF NOT EXISTS `permissions` (
  `permission_id` BIGINT NOT NULL AUTO_INCREMENT,
  `permission_code` VARCHAR(100) NOT NULL COMMENT '如 interview:read',
  `permission_name` VARCHAR(100) NOT NULL,
  `resource` VARCHAR(50) DEFAULT NULL,
  `action` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`permission_id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点';

CREATE TABLE IF NOT EXISTS `role_permissions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_rp_role` (`role_id`),
  KEY `idx_rp_permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限映射';

CREATE TABLE IF NOT EXISTS `user_roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_ur_role_id` (`role_id`),
  KEY `idx_ur_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色映射';

-- D. 面试题库 + 评分
CREATE TABLE IF NOT EXISTS `interview_dimensions` (
  `dimension_id` BIGINT NOT NULL AUTO_INCREMENT,
  `dimension_code` VARCHAR(50) NOT NULL,
  `dimension_name` VARCHAR(100) NOT NULL,
  `weight` DECIMAL(5,2) NOT NULL DEFAULT 1.00,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`dimension_id`),
  UNIQUE KEY `uk_dimension_code` (`dimension_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试能力维度';

CREATE TABLE IF NOT EXISTS `interview_questions` (
  `question_id` BIGINT NOT NULL AUTO_INCREMENT,
  `position_name` VARCHAR(50) NOT NULL,
  `difficulty` ENUM('EASY', 'NORMAL', 'HARD') NOT NULL DEFAULT 'NORMAL',
  `dimension_id` BIGINT DEFAULT NULL,
  `question_text` TEXT NOT NULL,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`),
  KEY `idx_position_diff` (`position_name`, `difficulty`),
  KEY `idx_iq_dimension` (`dimension_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试题库';

CREATE TABLE IF NOT EXISTS `interview_answer_scores` (
  `score_id` BIGINT NOT NULL AUTO_INCREMENT,
  `interview_id` BIGINT NOT NULL,
  `question_id` BIGINT DEFAULT NULL,
  `dimension_id` BIGINT DEFAULT NULL,
  `answer_text` MEDIUMTEXT DEFAULT NULL,
  `score` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  `feedback` TEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`score_id`),
  KEY `idx_interview_dimension` (`interview_id`, `dimension_id`),
  KEY `idx_ias_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试逐题/逐维度评分';

-- E. 积分流水
CREATE TABLE IF NOT EXISTS `point_transactions` (
  `txn_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `change_amount` INT NOT NULL COMMENT '+/-积分变更',
  `balance_after` INT NOT NULL COMMENT '变更后余额',
  `biz_type` VARCHAR(50) NOT NULL COMMENT '如 ASSESSMENT_COMPLETE',
  `biz_id` VARCHAR(64) DEFAULT NULL COMMENT '业务ID',
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`txn_id`),
  KEY `idx_point_user_time` (`user_id`, `created_at`),
  KEY `idx_point_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水';

-- F. 操作审计日志
CREATE TABLE IF NOT EXISTS `operation_logs` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT,
  `actor_user_id` BIGINT DEFAULT NULL COMMENT '操作者用户ID',
  `actor_role` VARCHAR(50) DEFAULT NULL COMMENT '操作者角色快照',
  `action` VARCHAR(100) NOT NULL COMMENT '动作，如 USER_FREEZE',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '目标对象类型',
  `target_id` VARCHAR(64) DEFAULT NULL COMMENT '目标对象ID',
  `request_id` VARCHAR(64) DEFAULT NULL,
  `ip_address` VARCHAR(45) DEFAULT NULL,
  `metadata` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `idx_actor_time` (`actor_user_id`, `created_at`),
  KEY `idx_action_time` (`action`, `created_at`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志';

-- G. 额外索引补充
CALL sp_add_index_if_missing('interviews', 'idx_interview_user_status', '(`user_id`, `status`, `started_at`)');

-- H. 清理临时过程
DROP PROCEDURE IF EXISTS `sp_add_column_if_missing`;
DROP PROCEDURE IF EXISTS `sp_add_index_if_missing`;

-- END
