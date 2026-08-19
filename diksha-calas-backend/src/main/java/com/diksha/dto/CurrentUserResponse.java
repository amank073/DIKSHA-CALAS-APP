package com.diksha.dto;

import com.diksha.enums.RoleType;

public class CurrentUserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private RoleType role;
    private String currentClass;
    private String subjectSpecialization;
    private String targetExam;
    private Boolean enabled;


    public CurrentUserResponse() {
    }

    public CurrentUserResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            RoleType role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public CurrentUserResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            RoleType role,
            String currentClass) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.currentClass = currentClass;

    }

    public CurrentUserResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            RoleType role,
            String currentClass,
            String subjectSpecialization,
            String targetExam,
            Boolean enabled) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.currentClass = currentClass;
        this.subjectSpecialization = subjectSpecialization;
        this.targetExam = targetExam;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public RoleType getRole() {
        return role;
    }

    public String getCurrentClass() {
        return currentClass;
    }

    public String getSubjectSpecialization() {
        return subjectSpecialization;
    }

    public String getTargetExam() {
        return targetExam;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
