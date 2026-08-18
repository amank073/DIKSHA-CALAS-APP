package com.diksha.dto;

import java.time.LocalDateTime;

public class StudentProgressResponse {

    private Long id;
    private Long contentId;
    private String contentTitle;
    private boolean completed;
    private LocalDateTime completedAt;

    public StudentProgressResponse(
            Long id,
            Long contentId,
            String contentTitle,
            boolean completed,
            LocalDateTime completedAt) {

        this.id = id;
        this.contentId = contentId;
        this.contentTitle = contentTitle;
        this.completed = completed;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getContentId() {
        return contentId;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
