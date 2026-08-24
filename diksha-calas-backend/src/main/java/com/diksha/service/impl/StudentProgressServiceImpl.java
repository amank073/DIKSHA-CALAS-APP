package com.diksha.service.impl;

import com.diksha.dto.*;
import com.diksha.entity.Content;
import com.diksha.entity.DailyProgress;
import com.diksha.entity.DailySchedule;
import com.diksha.entity.StudentProgress;
import com.diksha.entity.StudyPlan;
import com.diksha.entity.User;
import com.diksha.enums.CompletionStatus;
import com.diksha.enums.PlanStatus;
import com.diksha.repository.ContentRepository;
import com.diksha.repository.DailyProgressRepository;
import com.diksha.repository.DailyScheduleRepository;
import com.diksha.repository.StudentProgressRepository;
import com.diksha.repository.StudyPlanRepository;
import com.diksha.repository.UserRepository;
import com.diksha.service.StudentProgressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentProgressServiceImpl
        implements StudentProgressService {

    private final DailyProgressRepository dailyProgressRepository;
    private final DailyScheduleRepository dailyScheduleRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final StudentProgressRepository progressRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    public StudentProgressServiceImpl(
            StudentProgressRepository progressRepository,
            ContentRepository contentRepository,
            UserRepository userRepository,
            DailyProgressRepository dailyProgressRepository,
            DailyScheduleRepository dailyScheduleRepository,
            StudyPlanRepository studyPlanRepository) {

        this.dailyProgressRepository = dailyProgressRepository;
        this.dailyScheduleRepository = dailyScheduleRepository;
        this.studyPlanRepository = studyPlanRepository;
        this.progressRepository = progressRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CONTENT PROGRESS
    // =========================================================

    @Override
    @Transactional
    public StudentProgressResponse completeContent(
            Long contentId,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() ->
                        new RuntimeException("Content not found"));

        StudentProgress progress =
                progressRepository
                        .findByUserIdAndContentId(
                                user.getId(),
                                contentId
                        )
                        .orElseGet(() -> {

                            StudentProgress newProgress =
                                    new StudentProgress();

                            newProgress.setUser(user);
                            newProgress.setContent(content);

                            return newProgress;
                        });

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        StudentProgress saved =
                progressRepository.save(progress);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentProgressResponse> getMyProgress(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return progressRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // DAILY PROGRESS
    // =========================================================

    @Override
    @Transactional
    public DailyProgressResponse saveDailyProgress(
            DailyProgressRequest request,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (request == null) {
            throw new RuntimeException(
                    "Daily progress request is required"
            );
        }

        if (request.getScheduleId() == null) {
            throw new RuntimeException(
                    "scheduleId is required"
            );
        }

        if (request.getStudiedHours() < 0) {
            throw new RuntimeException(
                    "studiedHours cannot be negative"
            );
        }

        DailySchedule schedule =
                dailyScheduleRepository.findById(
                                request.getScheduleId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Daily schedule not found"
                                ));

        // -----------------------------------------------------
        // SECURITY: Make sure this schedule belongs to
        // the currently logged-in student's study plan.
        // -----------------------------------------------------

        if (schedule.getStudyPlan() == null
                || schedule.getStudyPlan().getStudent() == null
                || !schedule.getStudyPlan()
                .getStudent()
                .getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to update this schedule"
            );
        }

        DailyProgress progress =
                dailyProgressRepository
                        .findByStudentIdAndDailyScheduleId(
                                user.getId(),
                                schedule.getId()
                        )
                        .orElseGet(() -> {

                            DailyProgress newProgress =
                                    new DailyProgress();

                            newProgress.setStudent(user);
                            newProgress.setDailySchedule(schedule);

                            return newProgress;
                        });

        progress.setHoursStudied(
                progress.getHoursStudied() + request.getStudiedHours()
        );

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {

            try {

                progress.setCompletionStatus(
                        CompletionStatus.valueOf(
                                request.getStatus()
                                        .trim()
                                        .toUpperCase()
                        )
                );

            } catch (IllegalArgumentException ex) {

                throw new RuntimeException(
                        "Invalid status. Use PENDING, " +
                                "INCOMPLETE or COMPLETED"
                );
            }
        }

        progress.setRemarks(
                request.getRemarks()
        );

        DailyProgress saved =
                dailyProgressRepository.save(progress);

        return mapDailyProgress(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyProgressResponse> getMyDailyProgress(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return dailyProgressRepository
                .findByStudentIdOrderByLoggedAtDesc(
                        user.getId()
                )
                .stream()
                .map(this::mapDailyProgress)
                .toList();
    }

    // =========================================================
    // STUDY PLAN SUMMARY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public StudyPlanProgressSummaryResponse
    getStudyPlanProgressSummary(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

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

        List<DailySchedule> schedules =
                dailyScheduleRepository
                        .findByStudyPlanIdOrderByScheduledDateAsc(
                                plan.getId()
                        );

        double totalPlannedHours =
                schedules.stream()
                        .mapToDouble(
                                DailySchedule::getPlannedHours
                        )
                        .sum();

        /*
         * Get all progress records for this student and keep
         * only progress belonging to schedules of this plan.
         */
        List<DailyProgress> progressList =
                dailyProgressRepository
                        .findByStudentIdOrderByLoggedAtDesc(
                                user.getId()
                        );

        Set<Long> scheduleIds =
                schedules.stream()
                        .map(DailySchedule::getId)
                        .collect(Collectors.toSet());

        Map<Long, DailyProgress> progressBySchedule =
                progressList.stream()
                        .filter(progress ->
                                progress.getDailySchedule() != null
                        )
                        .filter(progress ->
                                scheduleIds.contains(
                                        progress.getDailySchedule()
                                                .getId()
                                )
                        )
                        .collect(
                                Collectors.toMap(
                                        progress ->
                                                progress.getDailySchedule()
                                                        .getId(),
                                        progress -> progress,
                                        (first, second) -> first
                                )
                        );

        double studiedHours =
                progressBySchedule.values()
                        .stream()
                        .mapToDouble(
                                DailyProgress::getHoursStudied
                        )
                        .sum();

        int completedSchedules =
                (int) progressBySchedule.values()
                        .stream()
                        .filter(progress ->
                                progress.getCompletionStatus()
                                        == CompletionStatus.COMPLETED
                        )
                        .count();

        int incompleteSchedules =
                (int) progressBySchedule.values()
                        .stream()
                        .filter(progress ->
                                progress.getCompletionStatus()
                                        == CompletionStatus
                                        .INCOMPLETE
                        )
                        .count();

        double completionPercentage =
                totalPlannedHours <= 0
                        ? 0.0
                        : (studiedHours * 100.0)
                        / totalPlannedHours;

        return new StudyPlanProgressSummaryResponse(
                plan.getId(),
                schedules.size(),
                totalPlannedHours,
                studiedHours,
                completedSchedules,
                incompleteSchedules,
                completionPercentage
        );
    }

    // =========================================================
    // SUBJECT-WISE PROGRESS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<SubjectProgressResponse> getSubjectProgress(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

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

        List<DailySchedule> schedules =
                dailyScheduleRepository
                        .findByStudyPlanIdOrderByScheduledDateAsc(
                                plan.getId()
                        );

        /*
         * Get only schedule IDs belonging to active plan.
         */
        Set<Long> scheduleIds =
                schedules.stream()
                        .map(DailySchedule::getId)
                        .collect(Collectors.toCollection(
                                HashSet::new
                        ));

        /*
         * Map student's progress by schedule ID.
         */
        Map<Long, DailyProgress> progressBySchedule =
                dailyProgressRepository
                        .findByStudentIdOrderByLoggedAtDesc(
                                user.getId()
                        )
                        .stream()
                        .filter(progress ->
                                progress.getDailySchedule() != null
                        )
                        .filter(progress ->
                                scheduleIds.contains(
                                        progress.getDailySchedule()
                                                .getId()
                                )
                        )
                        .collect(
                                Collectors.toMap(
                                        progress ->
                                                progress.getDailySchedule()
                                                        .getId(),
                                        progress -> progress,
                                        (first, second) -> first
                                )
                        );

        /*
         * Group schedules by subject.
         */
        Map<String, List<DailySchedule>> schedulesBySubject =
                schedules.stream()
                        .filter(schedule ->
                                schedule.getSubjectName() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        schedule ->
                                                schedule.getSubjectName()
                                                        .trim()
                                )
                        );

        return schedulesBySubject.entrySet()
                .stream()
                .map(entry -> {

                    String subjectName = entry.getKey();

                    List<DailySchedule> subjectSchedules =
                            entry.getValue();

                    double plannedHours =
                            subjectSchedules.stream()
                                    .mapToDouble(
                                            DailySchedule::getPlannedHours
                                    )
                                    .sum();

                    double studiedHours =
                            subjectSchedules.stream()
                                    .map(schedule ->
                                            progressBySchedule.get(
                                                    schedule.getId()
                                            )
                                    )
                                    .filter(progress ->
                                            progress != null
                                    )
                                    .mapToDouble(
                                            DailyProgress::getHoursStudied
                                    )
                                    .sum();

                    int completedSchedules =
                            (int) subjectSchedules.stream()
                                    .map(schedule ->
                                            progressBySchedule.get(
                                                    schedule.getId()
                                            )
                                    )
                                    .filter(progress ->
                                            progress != null
                                    )
                                    .filter(progress ->
                                            progress.getCompletionStatus()
                                                    == CompletionStatus
                                                    .COMPLETED
                                    )
                                    .count();

                    double completionPercentage =
                            plannedHours <= 0
                                    ? 0.0
                                    : (studiedHours * 100.0)
                                    / plannedHours;

                    return new SubjectProgressResponse(
                            subjectName,
                            plannedHours,
                            studiedHours,
                            completionPercentage,
                            subjectSchedules.size(),
                            completedSchedules
                    );
                })
                .sorted(
                        Comparator.comparing(
                                SubjectProgressResponse
                                        ::getSubjectName
                        )
                )
                .toList();
    }

    // =========================================================
    // COURSE PROGRESS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public StudentDashboardProgressResponse getDashboardProgress(
            String email) {

        StudyPlanProgressSummaryResponse summary =
                getStudyPlanProgressSummary(email);

        List<SubjectProgressResponse> subjects =
                getSubjectProgress(email);

        List<DailyProgressResponse> recentProgress =
                getMyDailyProgress(email);

        return new StudentDashboardProgressResponse(
                summary,
                subjects,
                recentProgress
        );
    }



    // =========================================================
    // MAPPERS
    // =========================================================

    private StudentProgressResponse mapToResponse(
            StudentProgress progress) {

        return new StudentProgressResponse(
                progress.getId(),
                progress.getContent().getId(),
                progress.getContent().getTitle(),
                progress.isCompleted(),
                progress.getCompletedAt()
        );
    }

    private DailyProgressResponse mapDailyProgress(
            DailyProgress progress) {

        DailySchedule schedule =
                progress.getDailySchedule();

        return new DailyProgressResponse(
                progress.getId(),
                schedule.getId(),
                schedule.getTopic() != null
                        ? schedule.getTopic().getId()
                        : null,
                schedule.getTopic() != null
                        ? schedule.getTopic().getTopicName()
                        : null,
                schedule.getPlannedHours(),
                progress.getHoursStudied(),
                progress.getCompletionStatus()
                        .name(),
                progress.getRemarks(),
                progress.getLoggedAt()
        );
    }
}