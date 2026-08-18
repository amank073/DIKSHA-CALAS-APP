-- =========================================================
-- cleanup-non-jee-neet-courses.sql
--
-- This platform is JEE/NEET exam prep only. Anything else (like a
-- "Java Full Stack Development" course created while testing) shouldn't
-- be selectable by students. This deactivates it safely (doesn't
-- delete — so nothing breaks for students already enrolled in it).
--
-- Run with:
--   mysql -u root -p diksha_calas < cleanup-non-jee-neet-courses.sql
-- =========================================================

-- 1. Preview what will be deactivated:
SELECT id, course_name, active
FROM courses
WHERE LOWER(TRIM(course_name)) NOT IN ('jee', 'neet');

-- 2. Deactivate:
UPDATE courses
SET active = 0
WHERE LOWER(TRIM(course_name)) NOT IN ('jee', 'neet');

-- 3. Confirm only JEE/NEET remain active:
SELECT id, course_name, active
FROM courses
WHERE active = 1;
