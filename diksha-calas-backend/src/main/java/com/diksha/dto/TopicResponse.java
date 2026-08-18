package com.diksha.dto;

public class TopicResponse {

    private Long id;
    private String topicName;
    private String description;
    private Long subjectId;
    private String syllabusClass;
    private double tisScore;
    private Long parentTopicId;
    private boolean active;

    public TopicResponse() {
    }

    public TopicResponse(
            Long id,
            String topicName,
            String description,
            Long subjectId,
            String syllabusClass,
            double tisScore,
            Long parentTopicId,
            boolean active) {

        this.id = id;
        this.topicName = topicName;
        this.description = description;
        this.subjectId = subjectId;
        this.syllabusClass = syllabusClass;
        this.tisScore = tisScore;
        this.parentTopicId = parentTopicId;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getDescription() {
        return description;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public String getSyllabusClass() {
        return syllabusClass;
    }

    public double getTisScore() {
        return tisScore;
    }

    public Long getParentTopicId() {
        return parentTopicId;
    }

    public boolean isActive() {
        return active;
    }
}