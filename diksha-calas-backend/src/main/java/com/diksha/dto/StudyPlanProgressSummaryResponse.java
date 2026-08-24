package com.diksha.dto;

public class StudyPlanProgressSummaryResponse {

    private Long planId;
    private int totalSchedules;
    private double totalPlannedHours;
    private double studiedHours;
    private int completedSchedules;
    private int incompleteSchedules;
    private double completionPercentage;

    public StudyPlanProgressSummaryResponse(
            Long planId,
            int totalSchedules,
            double totalPlannedHours,
            double studiedHours,
            int completedSchedules,
            int incompleteSchedules,
            double completionPercentage) {

        this.planId = planId;
        this.totalSchedules = totalSchedules;
        this.totalPlannedHours = totalPlannedHours;
        this.studiedHours = studiedHours;
        this.completedSchedules = completedSchedules;
        this.incompleteSchedules = incompleteSchedules;
        this.completionPercentage = completionPercentage;
    }

    public Long getPlanId() {
        return planId;
    }

    public int getTotalSchedules() {
        return totalSchedules;
    }

    public double getTotalPlannedHours() {
        return totalPlannedHours;
    }

    public double getStudiedHours() {
        return studiedHours;
    }

    public int getCompletedSchedules() {
        return completedSchedules;
    }

    public int getPartiallyCompletedSchedules() {
        return incompleteSchedules;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }
}