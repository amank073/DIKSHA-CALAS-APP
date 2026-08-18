package com.diksha.dto;

import java.time.LocalDate;

public class MilestoneResponse {

    private Long id;
    private Long studentId;
    private int monthNumber;
    private String title;
    private double scoreObtained;
    private double maxScore;
    private LocalDate assessmentDate;

    public MilestoneResponse(
            Long id,
            Long studentId,
            int monthNumber,
            String title,
            double scoreObtained,
            double maxScore,
            LocalDate assessmentDate) {

        this.id = id;
        this.studentId = studentId;
        this.monthNumber = monthNumber;
        this.title = title;
        this.scoreObtained = scoreObtained;
        this.maxScore = maxScore;
        this.assessmentDate = assessmentDate;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public int getMonthNumber() {
        return monthNumber;
    }

    public String getTitle() {
        return title;
    }

    public double getScoreObtained() {
        return scoreObtained;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }
}