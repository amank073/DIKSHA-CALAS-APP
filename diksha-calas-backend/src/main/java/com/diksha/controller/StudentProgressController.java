package com.diksha.controller;

import com.diksha.dto.*;
import com.diksha.service.StudentProgressService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/progress")
@CrossOrigin("*")
public class StudentProgressController {

    private final StudentProgressService progressService;

    public StudentProgressController(
            StudentProgressService progressService) {

        this.progressService = progressService;
    }

    @PostMapping("/content/{contentId}/complete")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentProgressResponse completeContent(
            @PathVariable Long contentId,
            Authentication authentication) {

        return progressService.completeContent(
                contentId,
                authentication.getName()
        );
    }

    @GetMapping("/study-plan/summary")
    public StudyPlanProgressSummaryResponse getStudyPlanProgressSummary(
            Authentication authentication) {

        return progressService.getStudyPlanProgressSummary(
                authentication.getName()
        );
    }

    @GetMapping("/subjects")
    public List<SubjectProgressResponse> getSubjectProgress(
            Authentication authentication) {

        return progressService.getSubjectProgress(
                authentication.getName()
        );
    }



    @PostMapping("/study-plan")
    @ResponseStatus(HttpStatus.CREATED)
    public DailyProgressResponse saveDailyProgress(
            @RequestBody DailyProgressRequest request,
            Authentication authentication) {

        return progressService.saveDailyProgress(
                request,
                authentication.getName()
        );
    }
    
    @GetMapping
public List<StudentProgressResponse> getMyProgress(
        Authentication authentication) {

    return progressService.getMyProgress(
            authentication.getName()
    );
}

    @GetMapping("/study-plan")
    public List<DailyProgressResponse> getMyDailyProgress(
            Authentication authentication) {

        return progressService.getMyDailyProgress(
                authentication.getName()
        );
    }

    @GetMapping("/dashboard")
    public StudentDashboardProgressResponse getDashboardProgress(
            Authentication authentication) {

        return progressService.getDashboardProgress(
                authentication.getName()
        );
    }
}
