-- =====================================================================
-- V7: F8 — Add source / pinned / hidden columns to home_articles
--
-- source : origin of the article — MANUAL | RSS_JUEJIN | RSS_36KR
-- pinned : admin-pinned articles float to the top of the home feed
-- hidden : admin-hidden articles are excluded from the home feed
-- =====================================================================

ALTER TABLE `home_articles`
    ADD COLUMN `source` VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN `pinned` TINYINT(1)  NOT NULL DEFAULT 0,
    ADD COLUMN `hidden` TINYINT(1)  NOT NULL DEFAULT 0;

-- Deduplicate index on source_url so the RSS job can't insert the same
-- article twice (ON DUPLICATE KEY UPDATE used in the job).
ALTER TABLE `home_articles`
    ADD UNIQUE KEY `uk_home_articles_source_url` (`source_url`(400));
