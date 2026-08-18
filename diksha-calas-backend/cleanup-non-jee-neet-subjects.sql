-- =========================================================
-- cleanup-non-jee-neet-subjects.sql
--
-- This platform is JEE/NEET prep only. The SSP algorithm
-- (StudyPlanServiceImpl.collectSubjects) only ever schedules
-- Physics, Chemistry, Mathematics, and Biology — anything else
-- was probably created while testing and should be removed.
--
-- SAFE BY DESIGN: this DEACTIVATES extra subjects (active = 0)
-- instead of deleting them, so it never breaks foreign keys from
-- existing topics/resources/tests/daily_schedules rows that may
-- already reference them. The algorithm already filters on
-- `active = 1`, so deactivated subjects simply stop being used —
-- past students' historical schedules are untouched.
--
-- Run with:
--   mysql -u root -p diksha_calas < cleanup-non-jee-neet-subjects.sql
-- =========================================================

-- 1. Preview what will be deactivated (run this first to check):
SELECT id, subject_name, course_id, active
FROM subjects
WHERE LOWER(TRIM(subject_name)) NOT IN ('physics', 'chemistry', 'mathematics', 'biology');

-- 2. Deactivate them:
UPDATE subjects
SET active = 0
WHERE LOWER(TRIM(subject_name)) NOT IN ('physics', 'chemistry', 'mathematics', 'biology');

-- 3. Confirm only the 4 canonical subjects remain active:
SELECT id, subject_name, course_id, active
FROM subjects
WHERE active = 1;

-- Optional — once you're confident nothing references the deactivated
-- rows and want to fully remove them instead of just deactivating,
-- you can hard-delete AFTER first clearing dependents in this order
-- (uncomment and run manually, one statement at a time):
--
-- DELETE ds FROM daily_schedules ds
--   JOIN topics t ON ds.topic_id = t.id
--   JOIN subjects s ON t.subject_id = s.id
--   WHERE s.active = 0;
-- DELETE r FROM resources r
--   JOIN topics t ON r.topic_id = t.id
--   JOIN subjects s ON t.subject_id = s.id
--   WHERE s.active = 0;
-- DELETE tst FROM tests tst
--   JOIN topics t ON tst.topic_id = t.id
--   JOIN subjects s ON t.subject_id = s.id
--   WHERE s.active = 0;
-- DELETE t FROM topics t
--   JOIN subjects s ON t.subject_id = s.id
--   WHERE s.active = 0;
-- DELETE FROM subjects WHERE active = 0;
