package com.diksha.dto;

import java.time.LocalDate;

public class ManualScheduleOverrideRequest {

    private LocalDate scheduledDate;
    private String subjectName;
    private Long topicId;
    private Long resourceId;
    private Long testId;
    private Double plannedHours;
    private Integer weekNumber;
    private String testType;

    private String videoTitle;
    private String videoUrl;

    private String practiceTitle;
    private String practiceLink;
    private Integer practiceQuestionCount;

    private String notes;

    public ManualScheduleOverrideRequest() {
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public Double getPlannedHours() {
        return plannedHours;
    }

    public void setPlannedHours(Double plannedHours) {
        this.plannedHours = plannedHours;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getPracticeTitle() {
        return practiceTitle;
    }

    public void setPracticeTitle(String practiceTitle) {
        this.practiceTitle = practiceTitle;
    }

    public String getPracticeLink() {
        return practiceLink;
    }

    public void setPracticeLink(String practiceLink) {
        this.practiceLink = practiceLink;
    }

    public Integer getPracticeQuestionCount() {
        return practiceQuestionCount;
    }

    public void setPracticeQuestionCount(
            Integer practiceQuestionCount) {

        this.practiceQuestionCount =
                practiceQuestionCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}