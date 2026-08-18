package com.diksha.dto;

public class SubjectResponse {

    private Long id;
    private String subjectName;
    private String description;
    private boolean active;

    public SubjectResponse() {
    }

    public SubjectResponse(Long id,
                           String subjectName,
                           String description,
                           boolean active) {
        this.id = id;
        this.subjectName = subjectName;
        this.description = description;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}