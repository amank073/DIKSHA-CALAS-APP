package com.diksha.service.impl;

import com.diksha.dto.AssignTeacherRequest;
import com.diksha.dto.StudentDashboardProgressResponse;
import com.diksha.dto.StudentProfileResponse;
import com.diksha.dto.StudentUpdateRequest;
import com.diksha.entity.StudentProfile;
import com.diksha.entity.User;
import com.diksha.entity.DeletedUser;
import com.diksha.repository.DeletedUserRepository;
import com.diksha.enums.RoleType;
import com.diksha.repository.*;
import com.diksha.service.StudentManagementService;
import com.diksha.service.StudentProgressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentManagementServiceImpl
        implements StudentManagementService {

    private final StudentProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentProgressService progressService;

    private final DailyProgressRepository dailyProgressRepository;
    private final DailyScheduleRepository dailyScheduleRepository;

    private final MilestoneRepository milestoneRepository;
    private final DeletedUserRepository deletedUserRepository;
    private final MessageRepository messageRepository;
    private final StudentProgressRepository studentProgressRepository;
    private final StudyPlanRepository studyPlanRepository;

    public StudentManagementServiceImpl(
            StudentProfileRepository profileRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            StudentProgressService progressService,
            DailyProgressRepository dailyProgressRepository,
            DailyScheduleRepository dailyScheduleRepository,

            MilestoneRepository milestoneRepository,
            StudentProgressRepository studentProgressRepository,
            StudyPlanRepository studyPlanRepository,
            DeletedUserRepository deletedUserRepository,
            MessageRepository messageRepository) {

        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.progressService = progressService;

        this.dailyProgressRepository = dailyProgressRepository;
        this.dailyScheduleRepository = dailyScheduleRepository;
        this.milestoneRepository = milestoneRepository;
        this.studentProgressRepository = studentProgressRepository;
        this.studyPlanRepository = studyPlanRepository;
        this.deletedUserRepository = deletedUserRepository;
        this.messageRepository = messageRepository;
    }

    // =========================================================
    // GET ALL STUDENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<StudentProfileResponse> getStudents(String email) {

        User currentUser = getUser(email);

        RoleType role = currentUser.getRole().getName();

        if (role == RoleType.ADMIN) {

            return profileRepository.findAll()
                    .stream()
                    .map(this::map)
                    .toList();
        }

        if (role == RoleType.TEACHER) {

            return profileRepository
                    .findByAnyTeacherId(currentUser.getId())
                    .stream()
                    .map(this::map)
                    .toList();
        }

        if (role == RoleType.STUDENT) {

            return profileRepository
                    .findByUserId(currentUser.getId())
                    .map(this::map)
                    .map(List::of)
                    .orElse(List.of());
        }

        throw new RuntimeException("Access denied");
    }

    // =========================================================
    // GET SINGLE STUDENT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getStudent(
            Long studentId,
            String email) {

        User currentUser = getUser(email);

        User student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        checkStudentAccess(currentUser, student);

        StudentProfile profile =
                profileRepository.findByUserId(studentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student profile not found"));

        return map(profile);
    }

    // =========================================================
    // ASSIGN TEACHER
    // =========================================================

    @Override
    @Transactional
    public StudentProfileResponse assignTeacher(
            Long studentId,
            AssignTeacherRequest request,
            String email) {

        User admin = getUser(email);

        if (admin.getRole().getName() != RoleType.ADMIN) {
            throw new RuntimeException(
                    "Only ADMIN can assign teacher");
        }

        if (request == null || request.getTeacherId() == null) {
            throw new RuntimeException(
                    "teacherId is required");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student not found"));

        if (student.getRole().getName() != RoleType.STUDENT) {
            throw new RuntimeException(
                    "Selected user is not a student");
        }

        User teacher = userRepository
                .findById(request.getTeacherId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Teacher not found"));

        if (teacher.getRole().getName() != RoleType.TEACHER) {
            throw new RuntimeException(
                    "Selected user is not a teacher");
        }

        StudentProfile profile =
                profileRepository.findByUserId(studentId)
                        .orElseGet(() -> {

                            StudentProfile p =
                                    new StudentProfile();

                            p.setUser(student);

                            return p;
                        });

        if ("Physics".equalsIgnoreCase(request.getSubject())) {
            profile.setPhysicsTeacher(teacher);
        } else if ("Chemistry".equalsIgnoreCase(request.getSubject())) {
            profile.setChemistryTeacher(teacher);
        } else if ("Mathematics".equalsIgnoreCase(request.getSubject())) {
            profile.setMathsTeacher(teacher);
        } else if ("Biology".equalsIgnoreCase(request.getSubject())) {
            profile.setBiologyTeacher(teacher);
        } else {
            throw new RuntimeException("Invalid subject specified.");
        }

        return map(profileRepository.save(profile));
    }

    // =========================================================
    // UPDATE STUDENT
    // =========================================================

    @Override
    @Transactional
    public StudentProfileResponse updateStudent(
            Long studentId,
            StudentUpdateRequest request,
            String email) {

        User currentUser = getUser(email);

        if (request == null) {
            throw new RuntimeException(
                    "Update request is required");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student not found"));

        if (student.getRole().getName() != RoleType.STUDENT) {
            throw new RuntimeException(
                    "Selected user is not a student");
        }

        RoleType role = currentUser.getRole().getName();

        // Only ADMIN or the same STUDENT can update
        if (role == RoleType.STUDENT) {

            if (!currentUser.getId().equals(studentId)) {
                throw new RuntimeException(
                        "Access denied");
            }

        } else if (role != RoleType.ADMIN) {

            throw new RuntimeException(
                    "Access denied");
        }

        if (request.getFirstName() != null &&
                !request.getFirstName().isBlank()) {

            student.setFirstName(
                    request.getFirstName().trim());
        }

        if (request.getLastName() != null &&
                !request.getLastName().isBlank()) {

            student.setLastName(
                    request.getLastName().trim());
        }

        if (request.getPhone() != null &&
                !request.getPhone().isBlank()) {

            student.setPhone(
                    request.getPhone().trim());
        }

        userRepository.save(student);

        StudentProfile profile =
                profileRepository.findByUserId(studentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student profile not found"));

        return map(profile);
    }

    // =========================================================
    // DELETE STUDENT
    // =========================================================

    @Override
    @Transactional
    public void deleteStudent(
            Long studentId,
            String email) {

        User admin = getUser(email);

        if (admin.getRole().getName() != RoleType.ADMIN) {
            throw new RuntimeException(
                    "Only ADMIN can delete students");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student not found"));

        if (student.getRole().getName() != RoleType.STUDENT) {
            throw new RuntimeException(
                    "Selected user is not a student");
        }

        // -----------------------------------------------------
        // 1. Delete Daily Progress
        // -----------------------------------------------------

        dailyProgressRepository.deleteByStudentId(studentId);

        // -----------------------------------------------------
        // 2. Delete Student Progress
        // -----------------------------------------------------

        studentProgressRepository.deleteByUserId(studentId);


        // -----------------------------------------------------
        // 4. Delete Milestones
        // -----------------------------------------------------

        milestoneRepository.deleteByStudentId(studentId);

        // -----------------------------------------------------
        // 5. Delete Study Plans and their Daily Schedules
        // -----------------------------------------------------

        dailyScheduleRepository.deleteByStudentId(studentId);
        studyPlanRepository.deleteByStudentId(studentId);
        messageRepository.deleteByUserId(studentId);

        // -----------------------------------------------------
        // 6. Delete Student Profile
        // -----------------------------------------------------

        StudentProfile profile =
                profileRepository.findByUserId(studentId)
                        .orElse(null);

        if (profile != null) {
            profileRepository.delete(profile);
        }

        // -----------------------------------------------------
        // 7. Finally delete User
        // -----------------------------------------------------

        String studentEmail = student.getEmail();
        userRepository.delete(student);

        if (!deletedUserRepository.existsByEmail(studentEmail)) {
            deletedUserRepository.save(new com.diksha.entity.DeletedUser(studentEmail));
        }
    }

    // =========================================================
    // STUDENT PROGRESS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public StudentDashboardProgressResponse getStudentProgress(
            Long studentId,
            String email) {

        User currentUser = getUser(email);

        User student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student not found"));

        checkStudentAccess(currentUser, student);

        return progressService.getDashboardProgress(
                student.getEmail());
    }

    // =========================================================
    // CHECK STUDENT ACCESS
    // =========================================================

    private void checkStudentAccess(
            User currentUser,
            User student) {

        RoleType role =
                currentUser.getRole().getName();

        // ADMIN can access all students
        if (role == RoleType.ADMIN) {
            return;
        }

        // STUDENT can access only own profile
        if (role == RoleType.STUDENT) {

            if (!currentUser.getId()
                    .equals(student.getId())) {

                throw new RuntimeException(
                        "Access denied");
            }

            return;
        }

        // TEACHER can access assigned students only
        if (role == RoleType.TEACHER) {

            StudentProfile profile =
                    profileRepository
                            .findByUserId(student.getId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Student profile not found"));

            boolean isAssigned = false;
            if (profile.getPhysicsTeacher() != null && profile.getPhysicsTeacher().getId().equals(currentUser.getId())) isAssigned = true;
            if (profile.getChemistryTeacher() != null && profile.getChemistryTeacher().getId().equals(currentUser.getId())) isAssigned = true;
            if (profile.getMathsTeacher() != null && profile.getMathsTeacher().getId().equals(currentUser.getId())) isAssigned = true;
            if (profile.getBiologyTeacher() != null && profile.getBiologyTeacher().getId().equals(currentUser.getId())) isAssigned = true;

            if (!isAssigned) {
                throw new RuntimeException(
                        "Access denied");
            }

            return;
        }

        throw new RuntimeException(
                "Access denied");
    }

    // =========================================================
    // GET USER
    // =========================================================

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));
    }

    // =========================================================
    // MAP RESPONSE
    // =========================================================

    private StudentProfileResponse map(
            StudentProfile profile) {

        User student = profile.getUser();

        String studentName =
                student.getFirstName() + " " +
                        student.getLastName();

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
                null, // Deprecated teacherId
                finalTeachers,
                profile.getCurrentClass(),
                profile.getTargetExam()
        );
    }
}