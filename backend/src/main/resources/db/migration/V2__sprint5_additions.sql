-- =====================================================================
-- V2 Sprint 5 Additions (2026-05)
--
-- Adds new columns to existing tables and creates new tables required
-- by Sprint 5 features: F10, F11, F12, F16, F18, F19, F25, F26, F28a,
-- F28c. Uses ADD COLUMN IF NOT EXISTS for safety against manual patches.
-- =====================================================================

-- ─────────────────────────────────────────────
-- Alter existing tables
-- ─────────────────────────────────────────────

-- F25: account soft-delete  |  F16: user ban support
ALTER TABLE `users`
  ADD COLUMN `deleted_at`    DATETIME     NULL COMMENT 'soft-delete timestamp; non-null = scheduled for deletion (F25)',
  ADD COLUMN `banned_reason` VARCHAR(255) NULL COMMENT 'reason for ban when status indicates BANNED (F16)';

-- F28a: question bank three-tier architecture (OFFICIAL / USER / AI_GENERATED)
ALTER TABLE `interview_questions`
  ADD COLUMN `source`        VARCHAR(20) NOT NULL DEFAULT 'USER'      COMMENT 'OFFICIAL | USER | AI_GENERATED',
  ADD COLUMN `review_status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT 'PUBLISHED | PENDING_REVIEW | REJECTED',
  ADD COLUMN `answer`        TEXT        NULL     COMMENT 'reference answer in Markdown (OFFICIAL / AI_GENERATED only)';

-- F15: multi-persona chat history separation
ALTER TABLE `assistant_sessions`
  ADD COLUMN `persona` VARCHAR(20) NOT NULL DEFAULT 'MENTOR' COMMENT 'MENTOR | CHALLENGER';

-- ─────────────────────────────────────────────
-- F11: AI long-term session memory (rolling summaries)
-- One row per (user, persona); updated in-place on each summarization.
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `conversation_summaries` (
  `id`              BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT      NOT NULL,
  `persona`         VARCHAR(20) NOT NULL DEFAULT 'MENTOR' COMMENT 'MENTOR | CHALLENGER',
  `summary_text`    TEXT        NOT NULL,
  `turn_count`      INT         NOT NULL DEFAULT 0,
  `model_used`      VARCHAR(50),
  `tokens_consumed` INT         NOT NULL DEFAULT 0,
  `created_at`      DATETIME    NOT NULL,
  `updated_at`      DATETIME,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_summary_user_persona` (`user_id`, `persona`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- F12: AI user fact memory
-- Extracted structured facts about the user; AI reads these on every turn.
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `user_facts` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `category`    VARCHAR(50)  NOT NULL COMMENT 'PERSONALITY | CAREER_GOAL | SKILL | PREFERENCE | EXPERIENCE',
  `fact_key`    VARCHAR(100) NOT NULL,
  `fact_value`  TEXT         NOT NULL,
  `confidence`  DECIMAL(3,2) NOT NULL DEFAULT 1.00,
  `source`      VARCHAR(50)  NOT NULL DEFAULT 'AI_EXTRACTED' COMMENT 'AI_EXTRACTED | USER_INPUT | ASSESSMENT | RESUME',
  `created_at`  DATETIME     NOT NULL,
  `updated_at`  DATETIME,
  PRIMARY KEY (`id`),
  KEY `idx_user_facts_user` (`user_id`),
  UNIQUE KEY `uk_user_fact_key` (`user_id`, `fact_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- F10: WeChat subscribe message quota tracking
-- Decremented when user grants permission; read before sending.
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `wx_subscribe_quota` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL,
  `template_id` VARCHAR(64) NOT NULL,
  `remaining`   INT         NOT NULL DEFAULT 0,
  `updated_at`  DATETIME,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wx_quota_user_tpl` (`user_id`, `template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- F28c: AI-generated personalised career plan
-- One row per user (UNIQUE); regenerated in-place on demand.
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `user_career_plans` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`           BIGINT       NOT NULL,
  `target_role`       VARCHAR(200),
  `start_state_json`  JSON,
  `milestones_json`   JSON,
  `weekly_focus_json` JSON,
  `model_used`        VARCHAR(50),
  `tokens_consumed`   INT          NOT NULL DEFAULT 0,
  `generated_at`      DATETIME     NOT NULL,
  `last_updated_at`   DATETIME,
  `version`           INT          NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_career_plan_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- F26: user feedback / complaint channel
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `user_feedback` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT       NULL     COMMENT 'nullable — unauthenticated users may also submit',
  `category`        VARCHAR(30)  NOT NULL COMMENT 'FUNCTION_BUG | SUGGESTION | CONTENT_REPORT | OTHER',
  `content`         TEXT         NOT NULL,
  `contact`         VARCHAR(120) NULL,
  `attachment_urls` JSON         NULL,
  `status`          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | PROCESSING | REPLIED | CLOSED',
  `created_at`      DATETIME     NOT NULL,
  `replied_at`      DATETIME     NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- F18: lightweight usage event tracking (analytics)
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `usage_events` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT      NULL,
  `event_type` VARCHAR(60) NOT NULL,
  `payload`    JSON        NULL,
  `created_at` DATETIME    NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_usage_events_user_type` (`user_id`, `event_type`),
  KEY `idx_usage_events_created`   (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- F19: admin audit log
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `admin_audit_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `admin_id`    BIGINT       NOT NULL,
  `action`      VARCHAR(60)  NOT NULL,
  `target_type` VARCHAR(40)  NOT NULL,
  `target_id`   VARCHAR(40)  NULL,
  `before_json` JSON         NULL,
  `after_json`  JSON         NULL,
  `ip`          VARCHAR(45)  NULL,
  `ua`          VARCHAR(255) NULL,
  `created_at`  DATETIME     NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_audit_admin_created` (`admin_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- F25: minimal account deletion audit log
-- Stores only the fact of deletion + time + hashed IP. No PII.
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `account_deletion_log` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT      NOT NULL,
  `ip_hash`    VARCHAR(64) NULL COMMENT 'SHA-256 of remote IP, not raw IP',
  `created_at` DATETIME    NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
