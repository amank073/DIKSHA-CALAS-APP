package com.diksha.controller;

import com.diksha.dto.DailyScheduleResponse;
import com.diksha.dto.ManualScheduleOverrideRequest;
import com.diksha.service.StudyPlanService;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminScheduleController {

    private final StudyPlanService studyPlanService;

    public AdminScheduleController(
            StudyPlanService studyPlanService) {

        this.studyPlanService = studyPlanService;
    }

    @PutMapping("/override/schedule/{scheduleId}")
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