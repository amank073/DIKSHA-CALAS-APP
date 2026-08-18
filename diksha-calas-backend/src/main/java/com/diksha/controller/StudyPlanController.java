package com.diksha.controller;

import jakarta.validation.Valid;

import com.diksha.dto.DailyScheduleResponse;
import com.diksha.dto.ManualScheduleOverrideRequest;
import com.diksha.dto.StudyPlanRequest;
import com.diksha.dto.StudyPlanResponse;
import com.diksha.service.StudyPlanService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/student/study-plans")
@CrossOrigin("*")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    public StudyPlanController(
            StudyPlanService studyPlanService) {

        this.studyPlanService = studyPlanService;
    }

    // =========================================================
    // TEACHER - STUDENT STUDY PLAN
    // =========================================================

    @GetMapping("/teacher/students/{studentId}/active")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public StudyPlanResponse getStudentActivePlan(
            @PathVariable Long studentId,
            Authentication authentication) {

        return studyPlanService.getStudentActivePlan(
                studentId,
                authentication.getName()
        );
    }

    @GetMapping("/teacher/students/{studentId}/schedule")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public List<?> getStudentScheduleForDate(
            @PathVariable Long studentId,
            @RequestParam LocalDate date,
            Authentication authentication) {

        return studyPlanService.getStudentScheduleForDate(
                studentId,
                date,
                authentication.getName()
        );
    }

    // =========================================================
    // STUDENT STUDY PLAN
    // =========================================================

    @PostMapping("/generate")
    public StudyPlanResponse generatePlan(
            @Valid @RequestBody StudyPlanRequest request,
            Authentication authentication) {

        return studyPlanService.generatePlan(
                request,
                authentication.getName()
        );
    }

    @PostMapping("/teacher/students/{studentId}/generate")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public StudyPlanResponse generateStudentPlan(
            @PathVariable Long studentId,
            @Valid @RequestBody StudyPlanRequest request,
            Authentication authentication) {

        return studyPlanService.generatePlanForStudent(
                request,
                studentId,
                authentication.getName()
        );
    }

    @GetMapping("/active")
    public StudyPlanResponse getActivePlan(
            Authentication authentication) {

        return studyPlanService.getActivePlan(
                authentication.getName()
        );
    }

    @GetMapping
    public List<StudyPlanResponse> getMyPlans(
            Authentication authentication) {

        return studyPlanService.getMyPlans(
                authentication.getName()
        );
    }

    @GetMapping("/schedule")
    public List<?> getScheduleForDate(
            @RequestParam LocalDate date,
            Authentication authentication) {

        return studyPlanService.getScheduleForDate(
                authentication.getName(),
                date
        );
    }

    // =========================================================
    // MANUAL SCHEDULE OVERRIDE
    // Teacher/Admin only
    // =========================================================

    @PutMapping("/admin/override/schedule/{scheduleId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public DailyScheduleResponse overrideSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ManualScheduleOverrideRequest request,
            Authentication authentication) {

        return studyPlanService.overrideSchedule(
                scheduleId,
                request,
                authentication.getName()
        );
    }
}