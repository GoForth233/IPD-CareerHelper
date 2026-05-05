CREATE TABLE IF NOT EXISTS `agent_tasks` (
  `task_id`      BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT       NOT NULL,
  `task_key`     VARCHAR(120) NOT NULL,
  `title`        VARCHAR(160) NOT NULL,
  `description`  VARCHAR(500),
  `task_type`    VARCHAR(40)  NOT NULL,
  `priority`     VARCHAR(20)  NOT NULL,
  `status`       VARCHAR(20)  NOT NULL DEFAULT 'TODO',
  `target`       VARCHAR(200),
  `source`       VARCHAR(40)  NOT NULL DEFAULT 'DAILY_AGENT',
  `due_date`     DATE         NOT NULL,
  `completed_at` DATETIME,
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_id`),
  UNIQUE KEY `uk_agent_task_user_day_key` (`user_id`, `due_date`, `task_key`),
  KEY `idx_agent_tasks_user_due` (`user_id`, `due_date`),
  KEY `idx_agent_tasks_user_status` (`user_id`, `status`),
  CONSTRAINT `fk_agent_tasks_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
