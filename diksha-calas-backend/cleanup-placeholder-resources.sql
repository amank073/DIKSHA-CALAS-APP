-- =========================================================
-- cleanup-placeholder-resources.sql
--
-- StudyPlanServiceImpl.generatePlanForStudentInternal ALWAYS prefers an
-- existing curated `Resource` row over ContentRecommender's YouTube
-- lookup (a teacher's hand-picked video should beat an auto-search).
-- This means any placeholder/test Resource rows created earlier
-- (e.g. url = "example.com/...") will keep showing up in every newly
-- generated plan too, silently bypassing the YouTube API entirely.
--
-- Run with:
--   mysql -u root -p diksha_calas < cleanup-placeholder-resources.sql
-- =========================================================

-- 1. Preview what will be deactivated (run this first to check):
SELECT id, title, resource_url, topic_id, active
FROM resources
WHERE resource_url LIKE '%example.com%';

-- 2. Deactivate them (safe — does not delete, just stops the algorithm
--    from picking them; ContentRecommender/YouTube will be used instead
--    the next time a plan is generated for that topic):
UPDATE resources
SET active = 0
WHERE resource_url LIKE '%example.com%';

-- 3. Confirm:
SELECT id, title, resource_url, topic_id, active
FROM resources
WHERE resource_url LIKE '%example.com%';
