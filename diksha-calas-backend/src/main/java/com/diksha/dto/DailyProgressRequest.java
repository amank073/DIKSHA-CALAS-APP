package com.diksha.dto;

public class DailyProgressRequest {

    private Long scheduleId;
    private double studiedHours;
    private String status;
    private String remarks;

    public DailyProgressRequest() {
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public double getStudiedHours() {
        return studiedHours;
    }

    public void setStudiedHours(double studiedHours) {
        this.studiedHours = studiedHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}