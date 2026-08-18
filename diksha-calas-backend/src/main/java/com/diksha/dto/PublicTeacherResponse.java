package com.diksha.dto;

/** Minimal, public-safe teacher info for the registration page's teacher dropdown — no email/phone exposed. */
public class PublicTeacherResponse {

    private Long id;
    private String fullName;
    private String subjectSpecialization;

    public PublicTeacherResponse(Long id, String fullName, String subjectSpecialization) {
        this.id = id;
        this.fullName = fullName;
        this.subjectSpecialization = subjectSpecialization;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSubjectSpecialization() {
        return subjectSpecialization;
    }
}
