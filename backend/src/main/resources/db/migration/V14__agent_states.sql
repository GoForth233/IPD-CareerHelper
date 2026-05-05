CREATE TABLE IF NOT EXISTS `agent_states` (
  `user_id`                     BIGINT        NOT NULL,
  `current_stage`               VARCHAR(50),
  `primary_goal`                VARCHAR(200),
  `primary_risk_code`           VARCHAR(50),
  `task_completion_rate_7d`     DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
  `task_dismiss_rate_7d`        DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
  `preferred_task_difficulty`   VARCHAR(20)   NOT NULL DEFAULT 'MEDIUM',
  `last_active_at`              DATETIME,
  `last_plan_adjusted_at`       DATETIME,
  `last_weekly_review_at`       DATETIME,
  `state_json`                  JSON,
  `updated_at`                  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_agent_states_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
