-- Phase E1: Unified career profile aggregated from snapshot / facts / plan / tasks.
-- Additive only; existing tables and APIs are untouched. Safe to roll back by
-- reverting the backend image — the new table simply stays unused.
CREATE TABLE IF NOT EXISTS `agent_user_profiles` (
  `profile_id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`                BIGINT       NOT NULL,
  `personalization_level`  VARCHAR(20)  NOT NULL DEFAULT 'LOW'
      COMMENT 'LOW | MEDIUM | HIGH — derived from completeness_score',
  `completeness_score`     INT          NOT NULL DEFAULT 0
      COMMENT '0-100, how much the agent knows about this user',
  `current_stage`          VARCHAR(50)
      COMMENT 'mirrors CareerAgentTodayDto.stage for fast lookup',
  `target_role`            VARCHAR(200),
  `target_role_source`     VARCHAR(50)
      COMMENT 'PREFERENCES | RESUME | INTERVIEW | USER_INPUT | INFERRED',
  `target_role_confidence` DECIMAL(3,2) NOT NULL DEFAULT 0.00,
  `primary_risk_code`      VARCHAR(50),
  `readiness_json`         JSON
      COMMENT 'resume / interview / overall readiness numbers',
  `skill_profile_json`     JSON
      COMMENT 'flat list of {name, level, source} entries; promoted to a real table later if needed',
  `behavior_profile_json`  JSON
      COMMENT 'streakDays, weeklyDays, completionRate7d, dismissRate7d, preferredDifficulty',
  `missing_signals_json`   JSON
      COMMENT 'list of fields the agent still needs from the user',
  `evidence_json`          JSON
      COMMENT 'where each fact came from, for explainability',
  `generated_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`profile_id`),
  UNIQUE KEY `uk_agent_user_profile_user` (`user_id`),
  KEY `idx_agent_user_profile_level` (`personalization_level`),
  KEY `idx_agent_user_profile_stage` (`current_stage`),
  CONSTRAINT `fk_agent_user_profile_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
