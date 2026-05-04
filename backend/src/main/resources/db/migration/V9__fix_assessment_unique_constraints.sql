-- V9: Add unique constraints to assessment tables to prevent duplicate seeding.
--
-- Root cause: assessment_questions and assessment_options had no unique
-- constraints, so repeated Flyway executions of V5 (during repair/rebuild)
-- created 2× duplicate questions and 3× duplicate options. This migration
-- adds the missing constraints so INSERT IGNORE works correctly in the future.
--
-- Safe to run after the manual cleanup that deleted all scales 2-8 data
-- and removed V5 from flyway_schema_history (V5 will re-run before this).

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. Unique question position per scale
--    Prevents the same sort_order from being inserted twice for a scale.
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE `assessment_questions`
  ADD UNIQUE KEY `uk_scale_sort_order` (`scale_id`, `sort_order`);

-- ─────────────────────────────────────────────────────────────────────────────
-- 2. Unique option label per question
--    Prevents A/B/C/D options from being duplicated for the same question.
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE `assessment_options`
  ADD UNIQUE KEY `uk_question_option_label` (`question_id`, `option_label`);

-- ─────────────────────────────────────────────────────────────────────────────
-- 3. Clean up any leftover stale scale entries with no questions
--    (e.g. the old manual-seed MBTI shell at scale_id=7, empty RIASEC at 8)
--    Uses a safe subquery pattern compatible with MySQL's DELETE restriction.
-- ─────────────────────────────────────────────────────────────────────────────
DELETE FROM `assessment_scales`
WHERE `scale_id` NOT IN (
  SELECT DISTINCT `scale_id` FROM `assessment_questions`
)
AND `scale_id` > 1;
