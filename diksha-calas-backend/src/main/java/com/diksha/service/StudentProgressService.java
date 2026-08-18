package com.diksha.service;

import com.diksha.dto.*;

import java.util.List;

public interface StudentProgressService {

    StudentProgressResponse completeContent(
            Long contentId,
            String email
    );

    List<StudentProgressResponse> getMyProgress(
            String email
    );

    List<DailyProgressResponse> getMyDailyProgress(
            String email
    );

    StudyPlanProgressSummaryResponse getStudyPlanProgressSummary(
            String email
    );

    List<SubjectProgressResponse> getSubjectProgress(
            String email
    );

    StudentDashboardProgressResponse getDashboardProgress(
            String email
    );

    DailyProgressResponse saveDailyProgress(
            DailyProgressRequest request,
            String email
    );
}