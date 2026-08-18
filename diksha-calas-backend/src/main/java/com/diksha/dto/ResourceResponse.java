package com.diksha.dto;

public class ResourceResponse {

    private Long id;
    private String title;
    private String description;
    private String resourceType;
    private String resourceUrl;
    private Long topicId;
    private boolean active;

    public ResourceResponse(
            Long id,
            String title,
            String description,
            String resourceType,
            String resourceUrl,
            Long topicId,
            boolean active) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.resourceType = resourceType;
        this.resourceUrl = resourceUrl;
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

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public Long getTopicId() {
        return topicId;
    }

    public boolean isActive() {
        return active;
    }
}
