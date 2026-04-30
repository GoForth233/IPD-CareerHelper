-- =====================================================================
-- Migration: switch resume.file_url + user.avatar_url from full HTTPS URLs
--            to bare OSS object keys (e.g. "resumes/uuid.pdf").
--
-- Context:
--   - The legacy `career-resume-store` bucket is decommissioned and the
--     project is moving to a new bucket (`careerloop-prod`). Old object
--     URLs would 404 anyway.
--   - Going forward FileService returns the bare key from upload and
--     issues short-lived presigned URLs on demand; the row only needs
--     to know the key so it survives bucket / endpoint changes.
--
-- This migration is destructive on PURPOSE: PO has confirmed the prior
-- bucket has no data of value. Run it once after bumping the backend
-- to the URL→key release.
--
-- Idempotency: re-running the script is safe; the trims are no-ops on
-- already-stripped rows.
-- =====================================================================

USE `career_db`;

-- ---- Strip the scheme + host from any legacy URLs sitting in resumes ----
UPDATE `resumes`
SET `file_url` = SUBSTRING(`file_url`, LOCATE('/', `file_url`, 9) + 1)
WHERE `file_url` LIKE 'https://%';

-- ---- Same for legacy avatar URLs on the users table ----
UPDATE `users`
SET `avatar_url` = SUBSTRING(`avatar_url`, LOCATE('/', `avatar_url`, 9) + 1)
WHERE `avatar_url` LIKE 'https://%';

-- ---- Optionally drop everything pointing at the dead bucket. PO has
--      confirmed the legacy bucket data is worthless; uncomment when
--      you're ready to wipe stale uploads from the UI:
--
--   TRUNCATE `resumes`;
--   UPDATE `users` SET `avatar_url` = NULL;
-- =====================================================================
