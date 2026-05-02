-- =====================================================================
-- V6: F28c — AI-generated personalised career plans
--
-- One row per user (unique on user_id).
-- milestones_json  : JSON array of CareerMilestone objects
-- weekly_focus_json: JSON array of strings (upcoming 4-week focus items)
-- start_state_json : JSON snapshot of user's profile when plan was generated
-- =====================================================================

CREATE TABLE IF NOT EXISTS `user_career_plans` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`           BIGINT        NOT NULL,
  `target_role`       VARCHAR(200),
  `start_state_json`  JSON,
  `milestones_json`   JSON,
  `weekly_focus_json` JSON,
  `model_used`        VARCHAR(50),
  `tokens_consumed`   INT           NOT NULL DEFAULT 0,
  `generated_at`      DATETIME,
  `last_updated_at`   DATETIME,
  `version`           INT           NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_career_plan_user` (`user_id`),
  CONSTRAINT `fk_career_plan_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='F28c AI-generated personalised career plan (one per user)';
