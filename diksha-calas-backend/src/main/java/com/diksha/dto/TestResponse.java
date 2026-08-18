package com.diksha.dto;

public class TestResponse {

    private Long id;
    private String title;
    private String description;
    private String testType;
    private int durationMinutes;
    private int totalMarks;
    private String link;
    private Long topicId;
    private boolean mixedSubject;
    private Long createdByTeacherId;
    private String createdByTeacherName;
    private boolean active;

    public TestResponse(
            Long id,
            String title,
            String description,
            String testType,
            int durationMinutes,
            int totalMarks,
            String link,
            Long topicId,
            boolean mixedSubject,
            Long createdByTeacherId,
            String createdByTeacherName,
            boolean active) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.testType = testType;
        this.durationMinutes = durationMinutes;
        this.totalMarks = totalMarks;
        this.link = link;
        this.topicId = topicId;
        this.mixedSubject = mixedSubject;
        this.createdByTeacherId = createdByTeacherId;
        this.createdByTeacherName = createdByTeacherName;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTestType() {
        return testType;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public String getLink() {
        return link;
    }

    public Long getTopicId() {
        return topicId;
    }

    public boolean isMixedSubject() {
        return mixedSubject;
    }

    public Long getCreatedByTeacherId() {
        return createdByTeacherId;
    }

    public String getCreatedByTeacherName() {
        return createdByTeacherName;
    }

    public boolean isActive() {
        return active;
    }
}
