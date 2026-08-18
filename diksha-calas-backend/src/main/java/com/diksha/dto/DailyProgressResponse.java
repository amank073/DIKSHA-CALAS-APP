package com.diksha.dto;

import java.time.LocalDateTime;

public class DailyProgressResponse {

    private Long id;
    private Long scheduleId;
    private Long topicId;
    private String topicName;
    private double plannedHours;
    private double studiedHours;
    private String status;
    private String remarks;
    private LocalDateTime loggedAt;

    public DailyProgressResponse(
            Long id,
            Long scheduleId,
            Long topicId,
            String topicName,
            double plannedHours,
            double studiedHours,
            String status,
            String remarks,
            LocalDateTime loggedAt) {

        this.id = id;
        this.scheduleId = scheduleId;
        this.topicId = topicId;
        this.topicName = topicName;
        this.plannedHours = plannedHours;
        this.studiedHours = studiedHours;
        this.status = status;
        this.remarks = remarks;
        this.loggedAt = loggedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public double getPlannedHours() {
        return plannedHours;
    }

    public double getStudiedHours() {
        return studiedHours;
    }

    public String getStatus() {
        return status;
    }

    public String getRemarks() {
        return remarks;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }
}