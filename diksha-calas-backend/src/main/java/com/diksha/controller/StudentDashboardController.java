package com.diksha.controller;

import com.diksha.dto.StudentDashboardResponse;
import com.diksha.service.StudentDashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/dashboard")
@CrossOrigin("*")
public class StudentDashboardController {

    private final StudentDashboardService dashboardService;

    public StudentDashboardController(
            StudentDashboardService dashboardService) {

        this.dashboardService = dashboardService;
    }

    @GetMapping
    public StudentDashboardResponse getDashboard(
            Authentication authentication) {

        return dashboardService.getDashboard(
                authentication.getName()
        );
    }
}

