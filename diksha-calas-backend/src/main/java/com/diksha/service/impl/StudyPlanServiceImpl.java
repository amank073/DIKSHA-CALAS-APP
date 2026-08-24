package com.diksha.service.impl;

import com.diksha.dto.DailyScheduleResponse;
import com.diksha.dto.ManualScheduleOverrideRequest;
import com.diksha.dto.StudyPlanRequest;
import com.diksha.dto.StudyPlanResponse;
import com.diksha.entity.DailySchedule;

import com.diksha.entity.Resource;
import com.diksha.entity.Subject;
import com.diksha.entity.StudyPlan;
import com.diksha.entity.Test;
import com.diksha.entity.Topic;
import com.diksha.entity.User;
import com.diksha.enums.ExamType;
import com.diksha.enums.PlanStatus;
import com.diksha.enums.RoleType;
import com.diksha.repository.*;
import com.diksha.service.MessageService;
import com.diksha.service.StudyPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import com.diksha.entity.StudentProfile;
import com.diksha.entity.TeacherProfile;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.diksha.service.engine.Contracts.*;

@Service
public class StudyPlanServiceImpl implements StudyPlanService {


    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final DailyScheduleRepository dailyScheduleRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final ResourceRepository resourceRepository;
    private final TestRepository testRepository;
    private final com.diksha.service.engine.PlanGeneratorService planGeneratorService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final MessageService messageService;


    private User getAccessibleStudent(
            Long studentId,
            String actorEmail) {

        if (studentId == null) {
            throw new RuntimeException("studentId is required");
        }

        User actor = getUser(actorEmail);

        User student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        RoleType role = actor.getRole() == null
                ? null
                : actor.getRole().getName();

        if (role == RoleType.ADMIN) {
            return student;
        }

        if (role == RoleType.STUDENT) {
            if (!actor.getId().equals(student.getId())) {
                throw new RuntimeException("Access denied");
            }
            return student;
        }

        if (role == RoleType.TEACHER) {
            StudentProfile profile =
                    studentProfileRepository
                            .findByUserId(student.getId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Student profile not found"));

            boolean isAssigned = false;
            if (profile.getPhysicsTeacher() != null && profile.getPhysicsTeacher().getId().equals(actor.getId())) isAssigned = true;
            if (profile.getChemistryTeacher() != null && profile.getChemistryTeacher().getId().equals(actor.getId())) isAssigned = true;
            if (profile.getMathsTeacher() != null && profile.getMathsTeacher().getId().equals(actor.getId())) isAssigned = true;
            if (profile.getBiologyTeacher() != null && profile.getBiologyTeacher().getId().equals(actor.getId())) isAssigned = true;

            if (!isAssigned) {
                throw new RuntimeException("Access denied");
            }

            return student;
        }

        throw new RuntimeException("Access denied");
    }

    public StudyPlanServiceImpl(
            StudyPlanRepository studyPlanRepository,
            DailyScheduleRepository dailyScheduleRepository,
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            ResourceRepository resourceRepository,
            TestRepository testRepository,
            StudentProfileRepository studentProfileRepository,
            TeacherProfileRepository teacherProfileRepository,
            com.diksha.service.engine.PlanGeneratorService planGeneratorService,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper,
            MessageService messageService) {

        this.studyPlanRepository = studyPlanRepository;
        this.dailyScheduleRepository = dailyScheduleRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.resourceRepository = resourceRepository;
        this.testRepository = testRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.planGeneratorService = planGeneratorService;
        this.objectMapper = objectMapper;
        this.messageService = messageService;
    }

    // =========================================================
    // GENERATE PLAN
    // =========================================================

    @Override
    @Transactional
    public StudyPlanResponse generatePlan(
            StudyPlanRequest request,
            String email) {

        User student = getUser(email);

        if (student.getRole() == null
                || student.getRole().getName() != RoleType.STUDENT) {
            throw new RuntimeException(
                    "Only STUDENT can generate a personal study plan"
            );
        }

        StudentProfile profile = studentProfileRepository.findByUserId(student.getId()).orElse(null);
        if (profile != null && profile.getTargetExam() != null) {
            if (request.getExamType() != null && !profile.getTargetExam().equalsIgnoreCase(request.getExamType().name())) {
                throw new RuntimeException("You can only generate a plan for your registered target exam: " + profile.getTargetExam());
            }
        }

        return generatePlanForStudentInternal(
                request,
                student
        );
    }

    @Override
    @Transactional
    public StudyPlanResponse generatePlanForStudent(
            StudyPlanRequest request,
            Long studentId,
            String actorEmail) {

        User student = getAccessibleStudent(
                studentId,
                actorEmail
        );

        StudyPlanResponse response = generatePlanForStudentInternal(
                request,
                student
        );

        User actor = getUser(actorEmail);
        try {
            messageService.sendMessage(actor, student.getId(), "Your entire study plan has been regenerated.", false);
        } catch (Exception e) {
            // Ignore messaging errors
        }

        return response;
    }

    @Override
    @Transactional
    public StudyPlanResponse generateSystemPlanForStudent(
            StudyPlanRequest request,
            User student) {

        return generatePlanForStudentInternal(
                request,
                student
        );
    }

    private StudyPlanResponse generatePlanForStudentInternal(
            StudyPlanRequest request,
            User student) {

        validateRequest(request);



        /*
         * Archive previous active plan.
         */
        studyPlanRepository
                .findFirstByStudentIdAndStatusOrderByCreatedAtDesc(
                        student.getId(),
                        PlanStatus.ACTIVE
                )
                .ifPresent(oldPlan -> {
                    oldPlan.setStatus(PlanStatus.ARCHIVED);
                    studyPlanRepository.save(oldPlan);
                });

        /*
         * -----------------------------------------------------------
         * Gather everything PlanGenerator needs as INPUT:
         * exam-relevant subjects (TIS + Prerequisites live on their
         * Topics), and the pool of tests available to schedule, scoped
         * to the student's ASSIGNED TEACHER only (Student -> Assigned
         * Teacher -> Teacher's Tests -> Scheduler).
         * -----------------------------------------------------------
         */
        List<Subject> examSubjects =
                collectSubjects(request.getExamType());

        if (examSubjects.isEmpty()) {
            throw new RuntimeException(
                    "No active subjects found for selected exam type");
        }

        StudentProfile studentProfile =
                studentProfileRepository.findByUserId(student.getId())
                        .orElseThrow(() -> new RuntimeException("Student profile not found"));

        User assignedTeacher = null;
        if (studentProfile.getPhysicsTeacher() != null) assignedTeacher = studentProfile.getPhysicsTeacher();
        else if (studentProfile.getChemistryTeacher() != null) assignedTeacher = studentProfile.getChemistryTeacher();
        else if (studentProfile.getMathsTeacher() != null) assignedTeacher = studentProfile.getMathsTeacher();
        else if (studentProfile.getBiologyTeacher() != null) assignedTeacher = studentProfile.getBiologyTeacher();


        List<TestCandidate> testPool =
                (assignedTeacher != null
                        ? testRepository.findByActiveAndCreatedByTeacherId(true, assignedTeacher.getId())
                        : List.<Test>of())
                        .stream()
                        .map(t -> new TestCandidate(
                                t.getId(),
                                t.getTitle(),
                                t.getTopic() != null ? t.getTopic().getId() : null,
                                t.isMixedSubject()
                        ))
                        .toList();

        /*
         * -----------------------------------------------------------
         * PlanGenerator coordinates Macro -> Micro (-> TopicSequencer)
         * -> Practice -> Assessment and hands back the Final Study Plan.
         * This class's only remaining job from here is turning that
         * result into persisted entities (DB concerns stay out of
         * PlanGeneratorService — see its class doc).
         * -----------------------------------------------------------
         */
        com.diksha.service.engine.PlanGeneratorService.GeneratedPlan generatedPlan =
                planGeneratorService.generate(
                        new com.diksha.service.engine.PlanGeneratorService.PlanGeneratorInput(
                                student.getId(),
                                request.getExamType(),
                                request.getVariant(),
                                request.getStartDate(),
                                request.getEndDate(),
                                request.getDailyStudyHours(),
                                examSubjects,
                                testPool
                        )
                );

        StudyPlan plan = new StudyPlan();
        plan.setStudent(student);
        plan.setVariant(request.getVariant());
        plan.setStatus(PlanStatus.ACTIVE);
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setDailyStudyHours(request.getDailyStudyHours());
        plan.setPhaseBreakdown(serializePhases(generatedPlan.phases()));

        StudyPlan savedPlan = studyPlanRepository.save(plan);

        for (com.diksha.service.engine.PlanGeneratorService.DraftStudySession session : generatedPlan.studySessions()) {

            DailySchedule schedule = new DailySchedule();
            schedule.setStudyPlan(savedPlan);
            schedule.setScheduledDate(session.date());
            schedule.setWeekNumber(session.weekNumber());
            schedule.setSubjectName(session.subjectName());
            schedule.setTopic(session.topic());
            schedule.setResource(session.resource());
            schedule.setTest(null);
            schedule.setPlannedHours(session.plannedHours());
            schedule.setManualOverride(false);
            schedule.setTestType("study");

            String vTitle = session.videoTitle();
            if (vTitle != null && vTitle.length() > 250) {
                vTitle = vTitle.substring(0, 250) + "...";
            }
            schedule.setVideoTitle(vTitle);

            String vUrl = session.videoUrl();
            if (vUrl != null && vUrl.length() > 490) {
                vUrl = vUrl.substring(0, 490);
            }
            schedule.setVideoUrl(vUrl);

            schedule.setPracticeTitle(session.practiceTitle());
            schedule.setPracticeLink(session.practiceLink());
            schedule.setPracticeQuestionCount(session.practiceQuestionCount());
            schedule.setNotes(session.notes());

            dailyScheduleRepository.save(schedule);
        }

        for (com.diksha.service.engine.PlanGeneratorService.DraftAssessmentSession session : generatedPlan.assessmentSessions()) {

            DailySchedule schedule = new DailySchedule();
            schedule.setStudyPlan(savedPlan);
            schedule.setScheduledDate(session.date());
            schedule.setWeekNumber(session.weekNumber());
            schedule.setTest(session.testEntity());
            schedule.setResource(null);
            schedule.setManualOverride(false);
            schedule.setTestType(session.testType());
            schedule.setPlannedHours(session.plannedHours());
            schedule.setTopic(session.topic());
            schedule.setSubjectName(session.subjectName());
            schedule.setNotes(session.notes());

            dailyScheduleRepository.save(schedule);
        }

        return mapToResponse(savedPlan);
    }


    // =========================================================
    // SERIALIZE PHASES (macro plan -> StudyPlan.phaseBreakdown JSON)
    // =========================================================

    private String serializePhases(List<MacroPhaseSpec> phases) {
        try {
            return objectMapper.writeValueAsString(phases);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize phase breakdown", e);
        }
    }

    // =========================================================
    // COLLECT SUBJECTS (exam-type filtered, deduplicated)
    // =========================================================

    private List<Subject> collectSubjects(
            ExamType examType) {

        Map<Long, Subject> uniqueSubjects = new LinkedHashMap<>();
        List<Subject> subjects = subjectRepository.findAll();

        for (Subject subject : subjects) {
            if (!subject.isActive()) continue;

            String subjectName = subject.getSubjectName();
            if (subjectName == null) continue;

            String normalizedName = subjectName.trim().toLowerCase();

            if (examType == ExamType.JEE) {
                if (!normalizedName.contains("physics")
                        && !normalizedName.contains("chemistry")
                        && !normalizedName.contains("mathematics")) {
                    continue;
                }
                if (normalizedName.contains("neet")) {
                    continue;
                }
            } else if (examType == ExamType.NEET) {
                if (!normalizedName.contains("physics")
                        && !normalizedName.contains("chemistry")
                        && !normalizedName.contains("biology")) {
                    continue;
                }
                if (normalizedName.contains("jee")) {
                    continue;
                }
            } else {
                continue;
            }

            uniqueSubjects.putIfAbsent(subject.getId(), subject);
        }

        return new ArrayList<>(uniqueSubjects.values());
    }





    // =========================================================
    // MY PLANS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<StudyPlanResponse> getMyPlans(
            String email) {

        User user = getUser(email);

        return studyPlanRepository
                .findByStudentIdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // ACTIVE PLAN
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public StudyPlanResponse getStudentActivePlan(
            Long studentId,
            String teacherEmail) {

        User student = getAccessibleStudent(
                studentId,
                teacherEmail
        );

        return getActivePlan(
                student.getEmail()
        );
    }


    @Override
    @Transactional(readOnly = true)
    public StudyPlanResponse getActivePlan(
            String email) {

        User user = getUser(email);

        StudyPlan plan =
                studyPlanRepository
                        .findFirstByStudentIdAndStatusOrderByCreatedAtDesc(
                                user.getId(),
                                PlanStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No active study plan found"
                                ));

        return mapToResponse(plan);

    }

    // =========================================================
    // SCHEDULE FOR DATE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<DailyScheduleResponse> getScheduleForDate(
            String email,
            LocalDate date) {

        User user = getUser(email);

        StudyPlan plan =
                studyPlanRepository
                        .findFirstByStudentIdAndStatusOrderByCreatedAtDesc(
                                user.getId(),
                                PlanStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No active study plan found"
                                ));

        return dailyScheduleRepository
                .findByStudyPlanIdAndScheduledDate(
                        plan.getId(),
                        date
                )
                .stream()
                .map(this::mapSchedule)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyScheduleResponse> getStudentScheduleForDate(
            Long studentId,
            LocalDate date,
            String teacherEmail) {

        if (studentId == null) {
            throw new RuntimeException(
                    "studentId is required"
            );
        }

        if (date == null) {
            throw new RuntimeException(
                    "date is required"
            );
        }

        // Verify that this student belongs to this teacher
        User student =
                getAccessibleStudent(
                        studentId,
                        teacherEmail
                );

        StudyPlan plan =
                studyPlanRepository
                        .findFirstByStudentIdAndStatusOrderByCreatedAtDesc(
                                student.getId(),
                                PlanStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No active study plan found"
                                ));

        return dailyScheduleRepository
                .findByStudyPlanIdAndScheduledDate(
                        plan.getId(),
                        date
                )
                .stream()
                .map(this::mapSchedule)
                .toList();
    }

    @Override
    @Transactional
    public DailyScheduleResponse overrideSchedule(
            Long scheduleId,
            ManualScheduleOverrideRequest request,
            String actorEmail) {

        if (scheduleId == null) {
            throw new RuntimeException(
                    "scheduleId is required"
            );
        }

        if (request == null) {
            throw new RuntimeException(
                    "Override request is required"
            );
        }

        if (request.getPlannedHours() != null
                && request.getPlannedHours() < 0) {
            throw new RuntimeException(
                    "plannedHours cannot be negative"
            );
        }

        DailySchedule schedule =
                dailyScheduleRepository
                        .findById(scheduleId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Daily schedule not found"
                                ));

        if (actorEmail == null || actorEmail.isBlank()) {
            throw new RuntimeException("Authenticated user is required");
        }

        if (schedule.getStudyPlan() == null
                || schedule.getStudyPlan().getStudent() == null) {
            throw new RuntimeException(
                    "Schedule is not linked to a student"
            );
        }

        getAccessibleStudent(
                schedule.getStudyPlan().getStudent().getId(),
                actorEmail
        );

        // ---------------------------------------------------------
        // Only update fields which are actually supplied.
        // Existing schedule data must not be erased.
        // ---------------------------------------------------------

        if (request.getScheduledDate() != null) {
            schedule.setScheduledDate(
                    request.getScheduledDate()
            );
        }

        if (request.getSubjectName() != null) {
            schedule.setSubjectName(
                    request.getSubjectName()
            );
        }

        if (request.getTopicId() != null) {

            Topic topic =
                    topicRepository
                            .findById(request.getTopicId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Topic not found"
                                    ));

            schedule.setTopic(topic);
        }

        if (request.getResourceId() != null) {

            Resource resource =
                    resourceRepository
                            .findById(request.getResourceId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Resource not found"
                                    ));

            schedule.setResource(resource);
        }

        if (request.getTestId() != null) {

            Test test =
                    testRepository
                            .findById(request.getTestId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Test not found"
                                    ));

            schedule.setTest(test);
        }

        if (request.getPlannedHours() != null) {
            schedule.setPlannedHours(
                    request.getPlannedHours()
            );
        }

        if (request.getWeekNumber() != null) {
            schedule.setWeekNumber(
                    request.getWeekNumber()
            );
        }

        if (request.getTestType() != null) {
            schedule.setTestType(
                    request.getTestType()
            );
        }

        if (request.getVideoTitle() != null) {
            schedule.setVideoTitle(
                    request.getVideoTitle()
            );
        }

        if (request.getVideoUrl() != null) {
            schedule.setVideoUrl(
                    request.getVideoUrl()
            );
        }

        if (request.getPracticeTitle() != null) {
            schedule.setPracticeTitle(
                    request.getPracticeTitle()
            );
        }

        if (request.getPracticeLink() != null) {
            schedule.setPracticeLink(
                    request.getPracticeLink()
            );
        }

        if (request.getPracticeQuestionCount() != null) {
            schedule.setPracticeQuestionCount(
                    request.getPracticeQuestionCount()
            );
        }

        if (request.getNotes() != null) {
            schedule.setNotes(
                    request.getNotes()
            );
        }

        schedule.setManualOverride(true);

        DailySchedule updatedSchedule = dailyScheduleRepository.save(schedule);
        
        String subjectName = updatedSchedule.getSubjectName();
        if (subjectName == null && updatedSchedule.getTopic() != null && updatedSchedule.getTopic().getSubject() != null) {
            subjectName = updatedSchedule.getTopic().getSubject().getSubjectName();
        }
        if (subjectName == null) {
            subjectName = "Subject";
        }
        
        // Remove (JEE) or (NEET) from subject name if present
        subjectName = subjectName.replaceAll(" \\(JEE\\)", "").replaceAll(" \\(NEET\\)", "").trim();

        User actor = getUser(actorEmail);
        try {
            messageService.sendMessage(actor, schedule.getStudyPlan().getStudent().getId(), "Your " + subjectName + " study plan has been changed.", false);
        } catch (Exception e) {
            // Ignore messaging errors
        }

        return mapSchedule(updatedSchedule);
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateRequest(
            StudyPlanRequest request) {

        if (request == null) {

            throw new RuntimeException(
                    "Study plan request is required"
            );
        }

        if (request.getExamType() == null) {

            throw new RuntimeException(
                    "Exam type is required"
            );
        }

        if (request.getVariant() == null) {

            throw new RuntimeException(
                    "Plan variant is required"
            );
        }

        if (request.getStartDate() == null
                || request.getEndDate() == null) {

            throw new RuntimeException(
                    "Start date and end date are required"
            );
        }

        if (request.getEndDate()
                .isBefore(
                        request.getStartDate()
                )) {

            throw new RuntimeException(
                    "End date cannot be before start date"
            );
        }

        if (request.getDailyStudyHours() <= 0) {

            throw new RuntimeException(
                    "Daily study hours must be greater than 0"
            );
        }
    }

    // =========================================================
    // USER
    // =========================================================

    private User getUser(
            String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));
    }

    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    private StudyPlanResponse mapToResponse(
            StudyPlan plan) {

        List<DailyScheduleResponse> schedules =
                dailyScheduleRepository
                        .findByStudyPlanIdOrderByScheduledDateAsc(
                                plan.getId()
                        )
                        .stream()
                        .map(this::mapSchedule)
                        .toList();

        return new StudyPlanResponse(
                plan.getId(),
                plan.getVariant(),
                plan.getStatus(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getDailyStudyHours(),
                plan.getPhaseBreakdown(),
                schedules
        );
    }

    private DailyScheduleResponse mapSchedule(
            DailySchedule schedule) {

        Resource resource =
                schedule.getResource();

        Test test =
                schedule.getTest();

        return new DailyScheduleResponse(
                schedule.getId(),
                schedule.getScheduledDate(),
                schedule.getSubjectName(),

                schedule.getTopic() == null
                        ? null
                        : schedule.getTopic().getId(),

                schedule.getTopic() == null
                        ? null
                        : schedule.getTopic().getTopicName(),

                resource == null
                        ? null
                        : resource.getId(),

                resource == null
                        ? null
                        : resource.getTitle(),

                test == null
                        ? null
                        : test.getId(),

                test == null
                        ? null
                        : test.getTitle(),

                schedule.getPlannedHours(),

                schedule.getWeekNumber(),

                schedule.getTestType(),

                schedule.getVideoTitle(),

                schedule.getVideoUrl(),

                schedule.getPracticeTitle(),

                schedule.getPracticeLink(),

                schedule.getPracticeQuestionCount(),

                schedule.getNotes(),

                schedule.isManualOverride()
        );
    }

    // =========================================================
    // ROUND
    // =========================================================

    private double round(
            double value) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
}
