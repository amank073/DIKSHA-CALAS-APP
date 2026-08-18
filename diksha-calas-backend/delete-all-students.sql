-- =========================================================
-- delete-all-students.sql
--
-- Deletes ALL student accounts and everything tied to them, in FK-safe
-- order (children before parents). Teachers, Admins, Courses, Subjects,
-- Topics, Resources, and Tests are all left untouched.
--
-- Run with:
--   mysql -u root -p diksha_calas < delete-all-students.sql
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Daily progress logs (references daily_schedules + students)
DELETE FROM daily_progress
WHERE student_id IN (
    SELECT u.id FROM (SELECT id FROM users) u
    JOIN student_profiles sp ON sp.user_id = u.id
);

-- 2. Content-completion progress
DELETE FROM student_progress
WHERE user_id IN (
    SELECT u.id FROM (SELECT id FROM users) u
    JOIN student_profiles sp ON sp.user_id = u.id
);

-- 3. Milestones
DELETE FROM milestones
WHERE student_id IN (
    SELECT u.id FROM (SELECT id FROM users) u
    JOIN student_profiles sp ON sp.user_id = u.id
);

-- 4. Daily schedules (children of study_plans)
DELETE ds FROM daily_schedules ds
JOIN study_plans sp2 ON ds.study_plan_id = sp2.id
JOIN student_profiles sp ON sp.user_id = sp2.student_id;

-- 5. Study plans
DELETE sp2 FROM study_plans sp2
JOIN student_profiles sp ON sp.user_id = sp2.student_id;

-- 6. Enrollments
DELETE e FROM enrollments e
JOIN student_profiles sp ON sp.user_id = e.user_id;

-- 7. Student profiles
DELETE FROM student_profiles;

-- 8. Finally, the student User rows themselves
--    (role lookup: adjust the role name below if yours differs)
DELETE u FROM users u
JOIN roles r ON u.role_id = r.id
WHERE r.name = 'STUDENT';

SET FOREIGN_KEY_CHECKS = 1;

-- Confirm — should return 0 rows:
SELECT COUNT(*) AS remaining_students
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE r.name = 'STUDENT';
