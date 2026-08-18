package com.diksha.dto;

public class SubjectProgressResponse {

    private String subjectName;
    private double plannedHours;
    private double studiedHours;
    private double completionPercentage;
    private int totalSchedules;
    private int completedSchedules;

    public SubjectProgressResponse(
            String subjectName,
            double plannedHours,
            double studiedHours,
            double completionPercentage,
            int totalSchedules,
            int completedSchedules) {

        this.subjectName = subjectName;
        this.plannedHours = plannedHours;
        this.studiedHours = studiedHours;
        this.completionPercentage = completionPercentage;
        this.totalSchedules = totalSchedules;
        this.completedSchedules = completedSchedules;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public double getPlannedHours() {
        return plannedHours;
    }

    public double getStudiedHours() {
        return studiedHours;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public int getTotalSchedules() {
        return totalSchedules;
    }

    public int getCompletedSchedules() {
        return completedSchedules;
    }
}
