package com.diksha.service.impl;

import com.diksha.dto.CreateTeacherRequest;
import com.diksha.dto.StudentProfileResponse;
import com.diksha.dto.TeacherResponse;
import com.diksha.entity.Role;
import com.diksha.entity.StudentProfile;
import com.diksha.entity.TeacherProfile;
import com.diksha.entity.User;
import com.diksha.enums.RoleType;
import com.diksha.repository.RoleRepository;
import com.diksha.repository.StudentProfileRepository;
import com.diksha.repository.TeacherProfileRepository;
import com.diksha.repository.UserRepository;
import com.diksha.repository.TestRepository;
import com.diksha.entity.Test;
import com.diksha.service.TeacherManagementService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

@Service
public class TeacherManagementServiceImpl
        implements TeacherManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.diksha.repository.MessageRepository messageRepository;
    private final StudentProfileRepository profileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final TestRepository testRepository;

    public TeacherManagementServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            StudentProfileRepository profileRepository,
            TeacherProfileRepository teacherProfileRepository,
            TestRepository testRepository,
            com.diksha.repository.MessageRepository messageRepository) {

        this.userRepository = userRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageRepository = messageRepository;
        this.profileRepository = profileRepository;
        this.testRepository = testRepository;
    }

    // =========================================================
    // CREATE TEACHER
    // =========================================================

    @Override
    @Transactional
    public TeacherResponse createTeacher(
            CreateTeacherRequest request,
            String email) {

        requireAdmin(email);

        if (request == null) {
            throw new RuntimeException(
                    "Teacher request is required");
        }

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {

            throw new RuntimeException(
                    "Teacher email is required");
        }

        if (request.getPassword() == null ||
                request.getPassword().isBlank()) {

            throw new RuntimeException(
                    "Teacher password is required");
        }

        String teacherEmail =
                request.getEmail().trim();

        if (userRepository.existsByEmail(teacherEmail)) {
            throw new RuntimeException(
                    "Email already exists");
        }

        Role teacherRole =
                roleRepository
                        .findByName(RoleType.TEACHER)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "TEACHER role not found"));

        User teacher = new User();

        teacher.setFirstName(
                request.getFirstName());

        teacher.setLastName(
                request.getLastName());

        teacher.setEmail(
                teacherEmail);

        teacher.setPassword(
                passwordEncoder.encode(
                        request.getPassword()));

        teacher.setPhone(
                request.getPhone());

        teacher.setRole(
                teacherRole);

        teacher.setEnabled(true);

        User savedTeacher = userRepository.save(teacher);

        TeacherProfile profile = new TeacherProfile();
        profile.setUser(savedTeacher);
        profile.setSubjectSpecialization(request.getSubjectSpecialization());
        teacherProfileRepository.save(profile);

        return map(savedTeacher);
    }

    // =========================================================
    // GET ALL TEACHERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TeacherResponse> getTeachers(
            String email) {

        requireAdmin(email);

        return userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getRole() != null &&
                                user.getRole().getName()
                                        == RoleType.TEACHER)
                .map(this::map)
                .toList();
    }

    // =========================================================
    // GET SINGLE TEACHER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse getTeacher(
            Long teacherId,
            String email) {

        requireAdmin(email);

        User teacher =
                getTeacherById(teacherId);

        return map(teacher);
    }

    // =========================================================
    // UPDATE TEACHER
    // =========================================================

    @Override
    @Transactional
    public TeacherResponse updateTeacher(
            Long teacherId,
            CreateTeacherRequest request,
            String email) {

        requireAdmin(email);

        if (request == null) {
            throw new RuntimeException(
                    "Teacher update request is required");
        }

        User teacher =
                getTeacherById(teacherId);

        if (request.getFirstName() != null &&
                !request.getFirstName().isBlank()) {

            teacher.setFirstName(
                    request.getFirstName().trim());
        }

        if (request.getLastName() != null &&
                !request.getLastName().isBlank()) {

            teacher.setLastName(
                    request.getLastName().trim());
        }

        if (request.getPhone() != null &&
                !request.getPhone().isBlank()) {

            teacher.setPhone(
                    request.getPhone().trim());
        }

        if (request.getEmail() != null &&
                !request.getEmail().isBlank() &&
                !request.getEmail()
                        .equalsIgnoreCase(
                                teacher.getEmail())) {

            String newEmail =
                    request.getEmail().trim();

            if (userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException(
                        "Email already exists");
            }

            teacher.setEmail(newEmail);
        }

        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {

            teacher.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()));
        }

        return map(
                userRepository.save(teacher)
        );
    }

    // =========================================================
    // SET TEACHER STATUS
    // =========================================================

    @Override
    @Transactional
    public TeacherResponse setTeacherStatus(
            Long teacherId,
            boolean enabled,
            String email) {

        requireAdmin(email);

        User teacher =
                getTeacherById(teacherId);

        boolean wasEnabled = teacher.getEnabled();
        teacher.setEnabled(enabled);

        User savedTeacher = userRepository.save(teacher);

        // If deactivated, reassign students
        if (wasEnabled && !enabled) {
            reassignStudents(teacher);
        }

        return map(savedTeacher);
    }

    // Helper class for load balancing
    private static class TeacherLoad implements Comparable<TeacherLoad> {
        User teacher;
        int count;

        public TeacherLoad(User teacher, int count) {
            this.teacher = teacher;
            this.count = count;
        }

        @Override
        public int compareTo(TeacherLoad o) {
            if (this.count != o.count) {
                return Integer.compare(this.count, o.count);
            }
            return Long.compare(this.teacher.getId(), o.teacher.getId());
        }
    }

    private void reassignStudents(User deactivatedTeacher) {
        TeacherProfile deactivatedProfile = teacherProfileRepository.findByUserId(deactivatedTeacher.getId()).orElse(null);
        if (deactivatedProfile == null || deactivatedProfile.getSubjectSpecialization() == null) {
            return;
        }

        String subject = deactivatedProfile.getSubjectSpecialization();

        // Find all active teachers of the same subject
        List<User> activeSameSubjectTeachers = userRepository.findAll().stream()
                .filter(u -> u.getEnabled() && !u.getId().equals(deactivatedTeacher.getId()) && u.getRole() != null && u.getRole().getName() == RoleType.TEACHER)
                .filter(u -> {
                    TeacherProfile tp = teacherProfileRepository.findByUserId(u.getId()).orElse(null);
                    return tp != null && subject.equalsIgnoreCase(tp.getSubjectSpecialization());
                })
                .collect(Collectors.toList());

        PriorityQueue<TeacherLoad> queue = new PriorityQueue<>();
        for (User t : activeSameSubjectTeachers) {
            int count = profileRepository.findByAnyTeacherId(t.getId()).size();
            queue.add(new TeacherLoad(t, count));
        }

        // Find all students currently assigned to this deactivated teacher
        List<StudentProfile> studentsToReassign = profileRepository.findAll().stream()
                .filter(p -> (p.getPhysicsTeacher() != null && p.getPhysicsTeacher().getId().equals(deactivatedTeacher.getId())) ||
                             (p.getChemistryTeacher() != null && p.getChemistryTeacher().getId().equals(deactivatedTeacher.getId())) ||
                             (p.getMathsTeacher() != null && p.getMathsTeacher().getId().equals(deactivatedTeacher.getId())) ||
                             (p.getBiologyTeacher() != null && p.getBiologyTeacher().getId().equals(deactivatedTeacher.getId())))
                .collect(Collectors.toList());

        for (StudentProfile student : studentsToReassign) {
            User nextTeacher = null;
            if (!queue.isEmpty()) {
                TeacherLoad load = queue.poll();
                nextTeacher = load.teacher;
                load.count++;
                queue.add(load);
            }

            // Unassign or reassign
            if (student.getPhysicsTeacher() != null && student.getPhysicsTeacher().getId().equals(deactivatedTeacher.getId())) {
                student.setPhysicsTeacher(nextTeacher);
            }
            if (student.getChemistryTeacher() != null && student.getChemistryTeacher().getId().equals(deactivatedTeacher.getId())) {
                student.setChemistryTeacher(nextTeacher);
            }
            if (student.getMathsTeacher() != null && student.getMathsTeacher().getId().equals(deactivatedTeacher.getId())) {
                student.setMathsTeacher(nextTeacher);
            }
            if (student.getBiologyTeacher() != null && student.getBiologyTeacher().getId().equals(deactivatedTeacher.getId())) {
                student.setBiologyTeacher(nextTeacher);
            }
            profileRepository.save(student);
        }
    }

    // =========================================================
    // DELETE TEACHER
    // =========================================================

    @Override
    @Transactional
    public void deleteTeacher(
            Long teacherId,
            String email) {

        requireAdmin(email);

        User teacher = getTeacherById(teacherId);

        // 1. Unassign the teacher from any student profiles
        profileRepository.findAll().stream()
                .filter(p -> (p.getPhysicsTeacher() != null && p.getPhysicsTeacher().getId().equals(teacherId)) ||
                             (p.getChemistryTeacher() != null && p.getChemistryTeacher().getId().equals(teacherId)) ||
                             (p.getMathsTeacher() != null && p.getMathsTeacher().getId().equals(teacherId)) ||
                             (p.getBiologyTeacher() != null && p.getBiologyTeacher().getId().equals(teacherId)))
                .forEach(profile -> {
                    if (profile.getPhysicsTeacher() != null && profile.getPhysicsTeacher().getId().equals(teacherId)) profile.setPhysicsTeacher(null);
                    if (profile.getChemistryTeacher() != null && profile.getChemistryTeacher().getId().equals(teacherId)) profile.setChemistryTeacher(null);
                    if (profile.getMathsTeacher() != null && profile.getMathsTeacher().getId().equals(teacherId)) profile.setMathsTeacher(null);
                    if (profile.getBiologyTeacher() != null && profile.getBiologyTeacher().getId().equals(teacherId)) profile.setBiologyTeacher(null);
                    profileRepository.save(profile);
                });
                
        // 2. Reassign all tests created by this teacher to the Admin who is deleting them
        User adminUser = getUser(email);
        List<Test> teacherTests = testRepository.findAll()
                .stream()
                .filter(t -> t.getCreatedByTeacher() != null && t.getCreatedByTeacher().getId().equals(teacherId))
                .toList();

        String teacherName = (teacher.getFirstName() + " " + teacher.getLastName()).trim();
        for (Test test : teacherTests) {
            test.setCreatedByTeacher(adminUser);
            String oldDesc = test.getDescription() != null ? test.getDescription() : "";
            test.setDescription(oldDesc + "\n(Originally created by deleted teacher: " + teacherName + ")");
            testRepository.save(test);
        }

        // 3. Delete TeacherProfile
        teacherProfileRepository.findByUserId(teacherId).ifPresent(teacherProfileRepository::delete);

        // Delete messages to prevent constraint violation
        messageRepository.deleteByUserId(teacherId);

        // 4. Delete the User
        userRepository.delete(teacher);
    }

    // =========================================================
    // GET ASSIGNED STUDENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<StudentProfileResponse> getAssignedStudents(
            String email) {

        User teacher = getUser(email);

        if (teacher.getRole() == null ||
                teacher.getRole().getName()
                        != RoleType.TEACHER) {

            throw new RuntimeException(
                    "Only TEACHER can view assigned students");
        }

        return profileRepository
                .findAll()
                .stream()
                .filter(profile ->
                        (profile.getPhysicsTeacher() != null && profile.getPhysicsTeacher().getId().equals(teacher.getId())) ||
                        (profile.getChemistryTeacher() != null && profile.getChemistryTeacher().getId().equals(teacher.getId())) ||
                        (profile.getMathsTeacher() != null && profile.getMathsTeacher().getId().equals(teacher.getId())) ||
                        (profile.getBiologyTeacher() != null && profile.getBiologyTeacher().getId().equals(teacher.getId()))
                )
                .map(this::mapStudent)
                .toList();
    }

    // =========================================================
    // MAP ASSIGNED STUDENT
    // =========================================================

    private StudentProfileResponse mapStudent(
            StudentProfile profile) {

        User student = profile.getUser();

        String studentName =
                ((student.getFirstName() != null
                        ? student.getFirstName()
                        : "") + " " +
                        (student.getLastName() != null
                                ? student.getLastName()
                                : ""))
                        .trim();

        StringBuilder teacherNames = new StringBuilder();
        if (profile.getPhysicsTeacher() != null) teacherNames.append("Physics: ").append(profile.getPhysicsTeacher().getFirstName()).append(" ").append(profile.getPhysicsTeacher().getLastName()).append(", ");
        if (profile.getChemistryTeacher() != null) teacherNames.append("Chemistry: ").append(profile.getChemistryTeacher().getFirstName()).append(" ").append(profile.getChemistryTeacher().getLastName()).append(", ");
        if (profile.getMathsTeacher() != null) teacherNames.append("Maths: ").append(profile.getMathsTeacher().getFirstName()).append(" ").append(profile.getMathsTeacher().getLastName()).append(", ");
        if (profile.getBiologyTeacher() != null) teacherNames.append("Biology: ").append(profile.getBiologyTeacher().getFirstName()).append(" ").append(profile.getBiologyTeacher().getLastName()).append(", ");

        String finalTeachers = teacherNames.toString();
        if (finalTeachers.endsWith(", ")) finalTeachers = finalTeachers.substring(0, finalTeachers.length() - 2);

        return new StudentProfileResponse(
                profile.getId(),
                student.getId(),
                studentName,
                student.getEmail(),
                student.getPhone(),
                null,
                finalTeachers,
                profile.getCurrentClass(),
                profile.getTargetExam()
        );
    }

    // =========================================================
    // ADMIN CHECK
    // =========================================================

    private void requireAdmin(String email) {

        User admin = getUser(email);

        if (admin.getRole() == null ||
                admin.getRole().getName()
                        != RoleType.ADMIN) {

            throw new RuntimeException(
                    "Only ADMIN can perform this action");
        }
    }

    // =========================================================
    // GET USER
    // =========================================================

    private User getUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));
    }

    // =========================================================
    // GET TEACHER BY ID
    // =========================================================

    private User getTeacherById(
            Long teacherId) {

        if (teacherId == null) {
            throw new RuntimeException(
                    "Teacher ID is required");
        }

        User teacher =
                userRepository
                        .findById(teacherId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Teacher not found"));

        if (teacher.getRole() == null ||
                teacher.getRole().getName()
                        != RoleType.TEACHER) {

            throw new RuntimeException(
                    "User is not a teacher");
        }

        return teacher;
    }

    // =========================================================
    // MAP TEACHER
    // =========================================================

    private TeacherResponse map(
            User user) {

        String specialization = teacherProfileRepository
                .findByUserId(user.getId())
                .map(TeacherProfile::getSubjectSpecialization)
                .orElse(null);

        return new TeacherResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getEnabled(),
                specialization
        );
    }
}