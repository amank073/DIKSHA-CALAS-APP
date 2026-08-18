package com.diksha.dto;

import com.diksha.enums.PlanStatus;
import com.diksha.enums.PlanVariant;

import java.time.LocalDate;
import java.util.List;

public class StudyPlanResponse {

    private Long id;
    private PlanVariant variant;
    private PlanStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private double dailyStudyHours;

    /**
     * JSON-serialized list of macro phases (see
     * com.diksha.service.engine.Contracts.MacroPhaseSpec), e.g.
     * [{"key":"foundation","name":"Foundation (Class 11)","startDate":"...", ...}, ...]
     * Was previously not exposed on this DTO at all — see MacroPlanEngine.
     */
    private String phaseBreakdown;

    private List<DailyScheduleResponse> schedules;

    public StudyPlanResponse(
            Long id,
            PlanVariant variant,
            PlanStatus status,
            LocalDate startDate,
            LocalDate endDate,
            double dailyStudyHours,
            String phaseBreakdown,
            List<DailyScheduleResponse> schedules) {

        this.id = id;
        this.variant = variant;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyStudyHours = dailyStudyHours;
        this.phaseBreakdown = phaseBreakdown;
        this.schedules = schedules;
    }

    public Long getId() {
        return id;
    }

    public PlanVariant getVariant() {
        return variant;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public double getDailyStudyHours() {
        return dailyStudyHours;
    }

    public String getPhaseBreakdown() {
        return phaseBreakdown;
    }

    public List<DailyScheduleResponse> getSchedules() {
        return schedules;
    }
}
