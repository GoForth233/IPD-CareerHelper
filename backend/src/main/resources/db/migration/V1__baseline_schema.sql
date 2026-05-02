-- =====================================================================
-- V1 Baseline Schema — CareerLoop full schema as of Sprint 4 (2026-04)
--
-- Flyway will mark this as already applied on an existing database
-- (baseline-on-migrate = true). On a fresh server it creates all tables.
-- All tables use utf8mb4 / unicode_ci for full emoji + CJK support.
-- =====================================================================

-- ─────────────────────────────────────────────
-- Core Auth
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `roles` (
  `role_id`     BIGINT       NOT NULL AUTO_INCREMENT,
  `role_code`   VARCHAR(50)  NOT NULL,
  `role_name`   VARCHAR(100) NOT NULL,
  `description` VARCHAR(255),
  `is_system`   TINYINT(1)   NOT NULL DEFAULT 1,
  `created_at`  DATETIME,
  `updated_at`  DATETIME,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `users` (
  `user_id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `nickname`         VARCHAR(64),
  `avatar_url`       VARCHAR(255),
  `school`           VARCHAR(100),
  `major`            VARCHAR(100),
  `graduation_year`  INT,
  `points`           INT         NOT NULL DEFAULT 0,
  `is_vip`           TINYINT(1)  NOT NULL DEFAULT 0,
  `status`           INT         NOT NULL DEFAULT 1,
  `org_id`           BIGINT,
  `profile_snapshot` JSON,
  `created_at`       DATETIME    NOT NULL,
  `updated_at`       DATETIME,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_auths` (
  `auth_id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT       NOT NULL,
  `identity_type`   VARCHAR(32)  NOT NULL,
  `identifier`      VARCHAR(100) NOT NULL,
  `credential`      TEXT,
  `last_login_time` DATETIME,
  PRIMARY KEY (`auth_id`),
  UNIQUE KEY `uk_identity_identifier` (`identity_type`, `identifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_roles` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT   NOT NULL,
  `role_id`    BIGINT   NOT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- Check-in streak tracking
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `check_ins` (
  `id`        BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`   BIGINT      NOT NULL,
  `check_day` DATE        NOT NULL,
  `action`    VARCHAR(32) NOT NULL,
  `created_at` DATETIME   NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_day_action` (`user_id`, `check_day`, `action`),
  KEY `idx_check_ins_user_day` (`user_id`, `check_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- Interviews
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `interviews` (
  `interview_id`     BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`          BIGINT      NOT NULL,
  `resume_id`        BIGINT,
  `position_name`    VARCHAR(50),
  `difficulty`       VARCHAR(20) NOT NULL DEFAULT 'Normal',
  `status`           VARCHAR(20) NOT NULL DEFAULT 'ONGOING',
  `mode`             VARCHAR(16) NOT NULL DEFAULT 'TEXT',
  `final_score`      INT,
  `report_mongo_id`  VARCHAR(50),
  `report_json`      LONGTEXT,
  `started_at`       DATETIME    NOT NULL,
  `ended_at`         DATETIME,
  `duration_seconds` INT,
  PRIMARY KEY (`interview_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `interview_messages` (
  `msg_id`       BIGINT      NOT NULL AUTO_INCREMENT,
  `interview_id` BIGINT      NOT NULL,
  `role`         VARCHAR(20) NOT NULL,
  `content`      TEXT,
  `created_at`   DATETIME    NOT NULL,
  PRIMARY KEY (`msg_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `interview_questions` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT,
  `position`         VARCHAR(80)   NOT NULL,
  `difficulty`       VARCHAR(16)   NOT NULL,
  `content`          VARCHAR(2000) NOT NULL,
  `summary`          VARCHAR(200),
  `contributor_hash` VARCHAR(64),
  `likes`            INT           NOT NULL DEFAULT 0,
  `draw_count`       INT           NOT NULL DEFAULT 0,
  `status`           VARCHAR(16)   NOT NULL DEFAULT 'APPROVED',
  `created_at`       DATETIME      NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_iq_pos_diff` (`position`, `difficulty`),
  KEY `idx_iq_likes` (`likes`),
  KEY `idx_iq_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- Assessments
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `assessment_scales` (
  `scale_id`       BIGINT       NOT NULL AUTO_INCREMENT,
  `title`          VARCHAR(100) NOT NULL,
  `description`    TEXT,
  `question_count` INT          NOT NULL DEFAULT 0,
  `status`         INT          NOT NULL DEFAULT 1,
  `version`        VARCHAR(20)  NOT NULL DEFAULT 'v1',
  `is_active`      TINYINT(1)   NOT NULL DEFAULT 1,
  `created_at`     DATETIME     NOT NULL,
  PRIMARY KEY (`scale_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `assessment_questions` (
  `question_id`    BIGINT      NOT NULL AUTO_INCREMENT,
  `scale_id`       BIGINT      NOT NULL,
  `question_text`  TEXT        NOT NULL,
  `question_type`  VARCHAR(20) NOT NULL DEFAULT 'SINGLE',
  `dimension_code` VARCHAR(50),
  `sort_order`     INT         NOT NULL DEFAULT 0,
  `is_active`      TINYINT(1)  NOT NULL DEFAULT 1,
  `created_at`     DATETIME    NOT NULL,
  `updated_at`     DATETIME,
  PRIMARY KEY (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `assessment_options` (
  `option_id`      BIGINT        NOT NULL AUTO_INCREMENT,
  `question_id`    BIGINT        NOT NULL,
  `option_label`   VARCHAR(20),
  `option_text`    VARCHAR(255)  NOT NULL,
  `score_value`    DECIMAL(8,2)  NOT NULL DEFAULT 0,
  `dimension_code` VARCHAR(50),
  `sort_order`     INT           NOT NULL DEFAULT 0,
  PRIMARY KEY (`option_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `assessment_records` (
  `record_id`      BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`        BIGINT      NOT NULL,
  `scale_id`       BIGINT      NOT NULL,
  `status`         VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
  `result_summary` VARCHAR(100),
  `result_json`    JSON,
  `ai_insight`     LONGTEXT,
  `created_at`     DATETIME    NOT NULL,
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `assessment_answers` (
  `answer_id`      BIGINT       NOT NULL AUTO_INCREMENT,
  `record_id`      BIGINT       NOT NULL,
  `question_id`    BIGINT       NOT NULL,
  `option_id`      BIGINT,
  `answer_text`    TEXT,
  `score_snapshot` DECIMAL(8,2) NOT NULL DEFAULT 0,
  `created_at`     DATETIME     NOT NULL,
  PRIMARY KEY (`answer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- Notifications
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `notifications` (
  `notification_id` BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT       NOT NULL,
  `type`            VARCHAR(50)  NOT NULL,
  `title`           VARCHAR(120) NOT NULL,
  `content`         TEXT,
  `link`            VARCHAR(255),
  `read_flag`       TINYINT(1)   NOT NULL DEFAULT 0,
  `created_at`      DATETIME     NOT NULL,
  PRIMARY KEY (`notification_id`),
  KEY `idx_notif_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- Homepage content
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `home_articles` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `title`        VARCHAR(255) NOT NULL,
  `summary`      VARCHAR(500),
  `image_url`    VARCHAR(512),
  `source_url`   VARCHAR(512),
  `category`     VARCHAR(32),
  `published_at` DATETIME,
  `created_at`   DATETIME     NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_home_articles_published` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `home_consultations` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `title`        VARCHAR(255) NOT NULL,
  `body_md`      TEXT,
  `author`       VARCHAR(100),
  `image_url`    VARCHAR(512),
  `source_url`   VARCHAR(512),
  `published_at` DATETIME,
  `created_at`   DATETIME     NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_home_consult_published` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `home_videos` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `bvid`         VARCHAR(32)  NOT NULL,
  `title`        VARCHAR(255) NOT NULL,
  `cover_url`    VARCHAR(512),
  `up_name`      VARCHAR(100),
  `up_mid`       BIGINT,
  `duration_sec` INT,
  `view_count`   BIGINT,
  `keyword`      VARCHAR(64),
  `sort_score`   BIGINT       NOT NULL DEFAULT 0,
  `fetched_at`   DATETIME     NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_home_videos_bvid` (`bvid`),
  KEY `idx_home_videos_keyword` (`keyword`),
  KEY `idx_home_videos_fetched_at` (`fetched_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- Career paths & skill map
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `career_paths` (
  `path_id`     INT         NOT NULL AUTO_INCREMENT,
  `code`        VARCHAR(50),
  `name`        VARCHAR(50),
  `description` TEXT,
  PRIMARY KEY (`path_id`),
  UNIQUE KEY `uk_career_path_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `career_nodes` (
  `node_id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `path_id`         INT          NOT NULL,
  `name`            VARCHAR(100),
  `description`     TEXT,
  `icon_url`        VARCHAR(255),
  `level`           INT,
  `sort_order`      INT          NOT NULL DEFAULT 0,
  `estimated_hours` INT          NOT NULL DEFAULT 10,
  `parent_id`       BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_career_progress` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT      NOT NULL,
  `node_id`    BIGINT      NOT NULL,
  `status`     VARCHAR(20) NOT NULL DEFAULT 'LOCKED',
  `updated_at` DATETIME,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- Resumes
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `resumes` (
  `resume_id`       BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT      NOT NULL,
  `title`           VARCHAR(50),
  `target_job`      VARCHAR(50),
  `file_url`        VARCHAR(500),
  `version`         VARCHAR(20) NOT NULL DEFAULT 'v1.0',
  `status`          VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',
  `parsed_content`  JSON,
  `diagnosis_score` INT         NOT NULL DEFAULT 0,
  `created_at`      DATETIME    NOT NULL,
  `updated_at`      DATETIME,
  PRIMARY KEY (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- Organizations (B-side cohort management)
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `organizations` (
  `org_id`        BIGINT       NOT NULL AUTO_INCREMENT,
  `code`          VARCHAR(50)  NOT NULL UNIQUE,
  `name`          VARCHAR(120) NOT NULL,
  `description`   VARCHAR(500),
  `contact_name`  VARCHAR(80),
  `contact_email` VARCHAR(120),
  `active`        TINYINT(1)   NOT NULL DEFAULT 1,
  `created_at`    DATETIME     NOT NULL,
  `updated_at`    DATETIME,
  PRIMARY KEY (`org_id`),
  KEY `idx_org_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- AI Assistant chat sessions
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `assistant_sessions` (
  `session_id` BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT      NOT NULL,
  `title`      VARCHAR(100),
  `model_name` VARCHAR(50),
  `created_at` DATETIME    NOT NULL,
  `updated_at` DATETIME,
  PRIMARY KEY (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `assistant_messages` (
  `msg_id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `session_id`        BIGINT      NOT NULL,
  `role`              VARCHAR(20) NOT NULL,
  `content`           TEXT        NOT NULL,
  `prompt_tokens`     INT         NOT NULL DEFAULT 0,
  `completion_tokens` INT         NOT NULL DEFAULT 0,
  `total_tokens`      INT         NOT NULL DEFAULT 0,
  `cost_micros`       BIGINT      NOT NULL DEFAULT 0,
  `created_at`        DATETIME    NOT NULL,
  PRIMARY KEY (`msg_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- Seed data: system roles
-- ─────────────────────────────────────────────

INSERT IGNORE INTO `roles` (`role_code`, `role_name`, `description`, `is_system`) VALUES
  ('STUDENT', 'Student', 'Default role for student-side users', 1),
  ('ADMIN',   'Admin',   'Back-office administrator',           1);
