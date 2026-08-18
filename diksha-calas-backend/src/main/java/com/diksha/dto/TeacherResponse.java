package com.diksha.dto;

public class TeacherResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Boolean enabled;
    private String subjectSpecialization;

    public TeacherResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            Boolean enabled,
            String subjectSpecialization) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.enabled = enabled;
        this.subjectSpecialization = subjectSpecialization;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public String getSubjectSpecialization() {
        return subjectSpecialization;
    }
}
