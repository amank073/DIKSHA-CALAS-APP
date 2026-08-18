package com.diksha.dto;

import com.diksha.enums.ExamType;
import com.diksha.enums.PlanVariant;

import java.time.LocalDate;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

public class StudyPlanRequest {

    @NotNull(message = "examType is required")
    private ExamType examType;
    @NotNull(message = "variant is required")
    private PlanVariant variant;
    @NotNull(message = "startDate is required")
    private LocalDate startDate;
    @NotNull(message = "endDate is required")
    private LocalDate endDate;
    @DecimalMin(value = "0.5", message = "dailyStudyHours must be at least 0.5")
    @DecimalMax(value = "16.0", message = "dailyStudyHours cannot exceed 16")
    private double dailyStudyHours;

    public StudyPlanRequest() {
    }

    @AssertTrue(message = "endDate must be on or after startDate")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public PlanVariant getVariant() {
        return variant;
    }

    public void setVariant(PlanVariant variant) {
        this.variant = variant;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getDailyStudyHours() {
        return dailyStudyHours;
    }

    public void setDailyStudyHours(double dailyStudyHours) {
        this.dailyStudyHours = dailyStudyHours;
    }
}