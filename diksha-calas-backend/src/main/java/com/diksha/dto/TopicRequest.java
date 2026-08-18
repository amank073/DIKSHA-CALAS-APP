package com.diksha.dto;

public class TopicRequest {

    private String topicName;
    private String description;
    private String syllabusClass;
    private double tisScore;
    private Long parentTopicId;

    public TopicRequest() {
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSyllabusClass() {
        return syllabusClass;
    }

    public void setSyllabusClass(String syllabusClass) {
        this.syllabusClass = syllabusClass;
    }

    public double getTisScore() {
        return tisScore;
    }

    public void setTisScore(double tisScore) {
        this.tisScore = tisScore;
    }

    public Long getParentTopicId() {
        return parentTopicId;
    }

    public void setParentTopicId(Long parentTopicId) {
        this.parentTopicId = parentTopicId;
    }
}