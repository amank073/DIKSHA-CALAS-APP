package com.diksha.dto;

public class TestRequest {

    private String title;
    private String description;
    private String testType;
    private int durationMinutes;
    private int totalMarks;
    private String link;
    /** Optional — null for a Subject Wise / mixed-subject test. */
    private Long topicId;
    private boolean mixedSubject;

    public TestRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public boolean isMixedSubject() {
        return mixedSubject;
    }

    public void setMixedSubject(boolean mixedSubject) {
        this.mixedSubject = mixedSubject;
    }
}
