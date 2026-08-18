package com.diksha.dto;

import java.time.LocalDate;

public class DailyScheduleResponse {

    private Long id;
    private LocalDate scheduledDate;
    private String subjectName;
    private Long topicId;
    private String topicName;
    private Long resourceId;
    private String resourceTitle;
    private Long testId;
    private String testTitle;
    private double plannedHours;
    private Integer weekNumber;
    private String testType;
    private String videoTitle;
    private String videoUrl;
    private String practiceTitle;
    private String practiceLink;
    private Integer practiceQuestionCount;
    private String notes;
    private boolean manualOverride;

    public DailyScheduleResponse(
            Long id,
            LocalDate scheduledDate,
            String subjectName,
            Long topicId,
            String topicName,
            Long resourceId,
            String resourceTitle,
            Long testId,
            String testTitle,
            double plannedHours,
            Integer weekNumber,
            String testType,
            String videoTitle,
            String videoUrl,
            String practiceTitle,
            String practiceLink,
            Integer practiceQuestionCount,
            String notes,
            boolean manualOverride) {

        this.id = id;
        this.scheduledDate = scheduledDate;
        this.subjectName = subjectName;
        this.topicId = topicId;
        this.topicName = topicName;
        this.resourceId = resourceId;
        this.resourceTitle = resourceTitle;
        this.testId = testId;
        this.testTitle = testTitle;
        this.plannedHours = plannedHours;
        this.weekNumber = weekNumber;
        this.testType = testType;
        this.videoTitle = videoTitle;
        this.videoUrl = videoUrl;
        this.practiceTitle = practiceTitle;
        this.practiceLink = practiceLink;
        this.practiceQuestionCount = practiceQuestionCount;
        this.notes = notes;
        this.manualOverride = manualOverride;
    }

    public Long getId() { return id; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public String getSubjectName() { return subjectName; }
    public Long getTopicId() { return topicId; }
    public String getTopicName() { return topicName; }
    public Long getResourceId() { return resourceId; }
    public String getResourceTitle() { return resourceTitle; }
    public Long getTestId() { return testId; }
    public String getTestTitle() { return testTitle; }
    public double getPlannedHours() { return plannedHours; }
    public Integer getWeekNumber() { return weekNumber; }
    public String getTestType() { return testType; }
    public String getVideoTitle() { return videoTitle; }
    public String getVideoUrl() { return videoUrl; }
    public String getPracticeTitle() { return practiceTitle; }
    public String getPracticeLink() { return practiceLink; }
    public Integer getPracticeQuestionCount() { return practiceQuestionCount; }
    public String getNotes() { return notes; }
    public boolean isManualOverride() { return manualOverride; }
}
