package com.diksha.dto;

import java.util.List;

public class StudentDashboardProgressResponse {

    private StudyPlanProgressSummaryResponse summary;
    private List<SubjectProgressResponse> subjects;
    private List<DailyProgressResponse> recentProgress;

    public StudentDashboardProgressResponse(
            StudyPlanProgressSummaryResponse summary,
            List<SubjectProgressResponse> subjects,
            List<DailyProgressResponse> recentProgress) {

        this.summary = summary;
        this.subjects = subjects;
        this.recentProgress = recentProgress;
    }

    public StudyPlanProgressSummaryResponse getSummary() {
        return summary;
    }

    public List<SubjectProgressResponse> getSubjects() {
        return subjects;
    }

    public List<DailyProgressResponse> getRecentProgress() {
        return recentProgress;
    }
}