-- =====================================================
-- Add cross-tool user portrait column.
-- Holds a JSON-serialised UserProfileSnapshot:
--   { "version":1, "updatedAt":"...", "assessment":{...},
--     "resume":{...}, "interview":{...}, "preferences":{...} }
-- Free-form JSON so we don't need a migration every time we
-- want to remember something new about the user.
-- Safe on dev because Hibernate's ddl-auto:update would add it
-- anyway, but explicit DDL is required for prod where we run
-- ddl-auto:none.
-- =====================================================
ALTER TABLE users
    ADD COLUMN profile_snapshot JSON NULL
    COMMENT 'Cross-tool user portrait, see UserProfileSnapshot DTO';
