CREATE TABLE IF NOT EXISTS `agent_events` (
  `event_id`      BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT        NOT NULL,
  `event_type`    VARCHAR(60)   NOT NULL,
  `event_payload` JSON,
  `source`        VARCHAR(40)   NOT NULL DEFAULT 'SYSTEM',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`event_id`),
  KEY `idx_agent_events_user_created` (`user_id`, `created_at`),
  KEY `idx_agent_events_user_type_created` (`user_id`, `event_type`, `created_at`),
  CONSTRAINT `fk_agent_events_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
