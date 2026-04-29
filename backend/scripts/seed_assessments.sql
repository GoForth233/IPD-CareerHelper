-- =====================================================================
-- Sprint 4 -- Assessment seed: MBTI 16-question personality quiz
-- =====================================================================
-- Idempotent: re-running this script will reset the MBTI bank cleanly.
-- Run with:
--   mysql -u <user> -p career_db < backend/scripts/seed_assessments.sql
-- =====================================================================

-- Drop any prior MBTI bank so the script can be re-run safely
DELETE ao FROM assessment_options ao
JOIN assessment_questions aq ON ao.question_id = aq.question_id
JOIN assessment_scales s     ON aq.scale_id = s.scale_id
WHERE s.title = 'MBTI Personality Test';

DELETE aq FROM assessment_questions aq
JOIN assessment_scales s ON aq.scale_id = s.scale_id
WHERE s.title = 'MBTI Personality Test';

DELETE FROM assessment_scales WHERE title = 'MBTI Personality Test';

-- 1. Scale
INSERT INTO assessment_scales (title, description, question_count, status, version, is_active)
VALUES ('MBTI Personality Test',
        'Discover your personality type across four dimensions: Extraversion/Introversion, Sensing/Intuition, Thinking/Feeling, Judging/Perceiving.',
        16, 1, 'v1', 1);
SET @scale_id := LAST_INSERT_ID();

-- =====================================================================
-- 2. Questions + 2 options each (A maps to first letter, B to second)
-- =====================================================================

-- ---- E / I ----
INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'At social gatherings, you usually:', 'SINGLE', 'EI', 1, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Actively meet new people and feed off the group''s energy', 1, 'E', 0),
 (@q, 'B', 'Stick with a few close friends; large crowds drain you',     1, 'I', 1);

INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'You feel most recharged by:', 'SINGLE', 'EI', 2, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Spending time around other people',  1, 'E', 0),
 (@q, 'B', 'Having quiet time alone to recover', 1, 'I', 1);

INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'When tackling a problem you prefer to:', 'SINGLE', 'EI', 3, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Brainstorm out loud with others',     1, 'E', 0),
 (@q, 'B', 'Think it through quietly on your own', 1, 'I', 1);

INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'In conversation you tend to:', 'SINGLE', 'EI', 4, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Speak first and refine your idea as you go', 1, 'E', 0),
 (@q, 'B', 'Think it through fully before you speak',    1, 'I', 1);

-- ---- S / N ----
INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'You prefer information that is:', 'SINGLE', 'SN', 5, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Concrete, factual, drawn from real experience', 1, 'S', 0),
 (@q, 'B', 'Abstract, theoretical, exploring possibilities', 1, 'N', 1);

INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'When learning something new, you like to:', 'SINGLE', 'SN', 6, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Follow step-by-step instructions',           1, 'S', 0),
 (@q, 'B', 'Get the big picture first, fill details later', 1, 'N', 1);

INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'You trust more:', 'SINGLE', 'SN', 7, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Direct experience and proven methods', 1, 'S', 0),
 (@q, 'B', 'Hunches, patterns and inspiration',    1, 'N', 1);

INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'You enjoy:', 'SINGLE', 'SN', 8, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Realistic stories grounded in real life', 1, 'S', 0),
 (@q, 'B', 'Imaginative or speculative stories',      1, 'N', 1);

-- ---- T / F ----
INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'When deciding, you give more weight to:', 'SINGLE', 'TF', 9, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Logic, consistency and objective facts',   1, 'T', 0),
 (@q, 'B', 'How people will be affected emotionally',  1, 'F', 1);

INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'You would rather be seen as:', 'SINGLE', 'TF', 10, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Competent and rational', 1, 'T', 0),
 (@q, 'B', 'Caring and compassionate', 1, 'F', 1);

INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'In conflicts you focus on:', 'SINGLE', 'TF', 11, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Finding the underlying truth',     1, 'T', 0),
 (@q, 'B', 'Preserving harmony in the group', 1, 'F', 1);

INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'You are more convinced by:', 'SINGLE', 'TF', 12, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Hard data and analytical reasoning', 1, 'T', 0),
 (@q, 'B', 'Personal values and stories',        1, 'F', 1);

-- ---- J / P ----
INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'You prefer your day to be:', 'SINGLE', 'JP', 13, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Planned and structured',  1, 'J', 0),
 (@q, 'B', 'Spontaneous and flexible', 1, 'P', 1);

INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'With deadlines, you usually:', 'SINGLE', 'JP', 14, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Finish well in advance',          1, 'J', 0),
 (@q, 'B', 'Work best closer to the wire',    1, 'P', 1);

INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'Your workspace is usually:', 'SINGLE', 'JP', 15, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Tidy and organised',          1, 'J', 0),
 (@q, 'B', 'Creatively cluttered',        1, 'P', 1);

INSERT INTO assessment_questions (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES (@scale_id, 'You tend to:', 'SINGLE', 'JP', 16, 1);
SET @q := LAST_INSERT_ID();
INSERT INTO assessment_options (question_id, option_label, option_text, score_value, dimension_code, sort_order) VALUES
 (@q, 'A', 'Make decisions quickly and move on', 1, 'J', 0),
 (@q, 'B', 'Keep options open as long as possible', 1, 'P', 1);

-- =====================================================================
-- Done. Check with:
--   SELECT s.title, COUNT(*) AS q FROM assessment_scales s
--     JOIN assessment_questions aq ON aq.scale_id = s.scale_id
--     WHERE s.title = 'MBTI Personality Test' GROUP BY s.title;
-- =====================================================================
