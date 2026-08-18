package com.diksha.dto;

public class ContentResponse {

    private Long id;
    private String title;
    private String description;
    private String contentType;
    private String contentUrl;
    private Long topicId;
    private boolean active;

    public ContentResponse() {
    }

    public ContentResponse(
            Long id,
            String title,
            String description,
            String contentType,
            String contentUrl,
            Long topicId,
            boolean active) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.contentType = contentType;
        this.contentUrl = contentUrl;
        this.topicId = topicId;
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

    public String getContentType() {
        return contentType;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public Long getTopicId() {
        return topicId;
    }

    public boolean isActive() {
        return active;
    }
}