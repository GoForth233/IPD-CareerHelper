-- V3: User consent records (F2 compliance)
-- ─────────────────────────────────────────────
-- Stores server-side proof of each user's agreement to a specific
-- agreement version. One row per (user_id, agreement_version) — a new
-- version produces a new row rather than overwriting the old one so we
-- retain the full audit trail.

CREATE TABLE IF NOT EXISTS `user_consents` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`           BIGINT       NOT NULL          COMMENT 'FK to users.id (nullable for pre-auth consent)',
  `agreement_version` VARCHAR(20)  NOT NULL          COMMENT 'e.g. "1.0", "2.0"',
  `agreed_at`         DATETIME     NOT NULL           COMMENT 'Server-side timestamp of consent',
  `client_ip`         VARCHAR(64)  DEFAULT NULL       COMMENT 'Client IP passed by frontend (best-effort)',
  `platform`          VARCHAR(50)  DEFAULT NULL       COMMENT 'miniprogram | h5 | app',
  `user_agent`        VARCHAR(512) DEFAULT NULL       COMMENT 'WeChat version string if available',
  PRIMARY KEY (`id`),
  KEY `idx_user_consents_user` (`user_id`),
  UNIQUE KEY `uk_user_consent_version` (`user_id`, `agreement_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='F2: Server-side record of user agreement to privacy policy and terms of service';
