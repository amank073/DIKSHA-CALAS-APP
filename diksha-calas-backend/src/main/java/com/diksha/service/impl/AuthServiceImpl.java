package com.diksha.service.impl;

import com.diksha.security.JwtService;
import com.diksha.dto.AuthResponse;
import com.diksha.dto.CurrentUserResponse;
import com.diksha.dto.LoginRequest;
import com.diksha.dto.RegisterRequest;
import com.diksha.entity.Role;
import com.diksha.entity.StudentProfile;
import com.diksha.entity.TeacherProfile;
import com.diksha.entity.User;
import com.diksha.enums.RoleType;
import com.diksha.repository.RoleRepository;
import com.diksha.repository.StudentProfileRepository;
import com.diksha.repository.UserRepository;
import com.diksha.repository.DeletedUserRepository;
import com.diksha.service.AuthService;
import com.diksha.service.StudyPlanService;
import com.diksha.dto.StudyPlanRequest;
import com.diksha.enums.PlanVariant;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final DeletedUserRepository deletedUserRepository;
    private final RoleRepository roleRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudyPlanService studyPlanService;
    private final com.diksha.repository.StudyPlanRepository studyPlanRepository;
    private final com.diksha.repository.TeacherProfileRepository teacherProfileRepository;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           StudentProfileRepository studentProfileRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           StudyPlanService studyPlanService,
                           com.diksha.repository.StudyPlanRepository studyPlanRepository,
                           com.diksha.repository.TeacherProfileRepository teacherProfileRepository,
                           DeletedUserRepository deletedUserRepository) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.studyPlanService = studyPlanService;
        this.studyPlanRepository = studyPlanRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.deletedUserRepository = deletedUserRepository;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(
                    "Email already exists",
                    null
            );
        }

        // Current Class and Exam Type (JEE/NEET) are both mandatory.
        if (request.getCurrentClass() == null) {
            throw new RuntimeException("Current class is required");
        }
        if (request.getExamType() == null) {
            throw new RuntimeException("Please select JEE or NEET");
        }

        // Public registration is restricted to STUDENT.
        // Teacher/Admin accounts must be created through their
        // protected management endpoints.
        Role role = roleRepository.findByName(RoleType.STUDENT)
                .orElseThrow(() ->
                        new RuntimeException("STUDENT role not found"));

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(role);

        User savedUser = userRepository.save(user);

        // Registration flow: User -> StudentProfile (class/cohort)
        // Teacher is intentionally left unassigned here — only an Admin assigns a
        // teacher, from Admin -> Students -> Assign Teacher.
        StudentProfile profile = new StudentProfile();
        profile.setUser(savedUser);
        profile.setCurrentClass(request.getCurrentClass());

        profile.setTargetExam(request.getExamType() != null ? request.getExamType().name() : null);

        studentProfileRepository.save(profile);

        // Generate automatic study plan for the new student
        StudyPlanRequest planRequest = new StudyPlanRequest();
        planRequest.setExamType(request.getExamType());
        
        if (request.getCurrentClass() == com.diksha.enums.StudentClass.CLASS_11) {
            planRequest.setVariant(PlanVariant.MONTH_24);
            planRequest.setEndDate(LocalDate.now().plusDays(730));
        } else {
            planRequest.setVariant(PlanVariant.MONTH_12);
            planRequest.setEndDate(LocalDate.now().plusDays(365));
        }
        
        planRequest.setStartDate(LocalDate.now());
        planRequest.setDailyStudyHours(4.0);
        
        CompletableFuture.runAsync(() -> {
            try {
                studyPlanService.generateSystemPlanForStudent(planRequest, savedUser);
            } catch (Exception e) {
                System.err.println("Failed to generate async study plan: " + e.getMessage());
            }
        });

        return new AuthResponse(
                "Registration Successful",
                null
        );
    }


    @Override
    public CurrentUserResponse me(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String currentClass = null;
        String subjectSpecialization = null;
        String targetExam = null;
        
        if (user.getRole() != null) {
            if (RoleType.STUDENT.equals(user.getRole().getName())) {
                StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
                if (profile != null) {
                    currentClass = profile.getCurrentClass() != null ? profile.getCurrentClass().name() : null;
                    targetExam = profile.getTargetExam();
                }
            } else if (RoleType.TEACHER.equals(user.getRole().getName())) {
                TeacherProfile profile = teacherProfileRepository.findByUserId(user.getId()).orElse(null);
                if (profile != null) {
                    subjectSpecialization = profile.getSubjectSpecialization();
                }
            }
        }

        return new CurrentUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole() == null ? null : user.getRole().getName(),
                currentClass,
                subjectSpecialization,
                targetExam,
                user.getEnabled()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            if (deletedUserRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("contact to support team");
            } else {
                throw new RuntimeException("signup first");
            }
        }

if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("invalid password");
        }

        // Auto-generate study plan for STUDENT if missing
        if (user.getRole() != null && com.diksha.enums.RoleType.STUDENT.equals(user.getRole().getName())) {
            boolean hasPlan = studyPlanRepository.findByStudentIdOrderByCreatedAtDesc(user.getId()).stream()
                    .anyMatch(p -> p.getStatus() == com.diksha.enums.PlanStatus.ACTIVE);
            
            if (!hasPlan) {
                StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
                if (profile != null && profile.getTargetExam() != null && profile.getCurrentClass() != null) {
                    StudyPlanRequest planRequest = new StudyPlanRequest();
                    planRequest.setExamType(com.diksha.enums.ExamType.valueOf(profile.getTargetExam().toUpperCase()));
                    
                    if (profile.getCurrentClass() == com.diksha.enums.StudentClass.CLASS_11) {
                        planRequest.setVariant(com.diksha.enums.PlanVariant.MONTH_24);
                        planRequest.setEndDate(java.time.LocalDate.now().plusDays(730));
                    } else {
                        planRequest.setVariant(com.diksha.enums.PlanVariant.MONTH_12);
                        planRequest.setEndDate(java.time.LocalDate.now().plusDays(365));
                    }
                    
                    planRequest.setStartDate(java.time.LocalDate.now());
                    planRequest.setDailyStudyHours(4.0);
                    
                    java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                            studyPlanService.generateSystemPlanForStudent(planRequest, user);
                        } catch (Exception e) {
                            System.err.println("Failed to generate async study plan on login: " + e.getMessage());
                        }
                    });
                }
            }
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                "Login Successful",
                token
        );
    }
}
