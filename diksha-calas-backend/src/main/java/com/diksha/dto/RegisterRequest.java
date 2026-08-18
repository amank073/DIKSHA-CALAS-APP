package com.diksha.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.diksha.enums.ExamType;
import com.diksha.enums.RoleType;
import com.diksha.enums.StudentClass;

public class RegisterRequest {

    @NotBlank(message = "firstName is required")
    @Size(max = 80, message = "firstName is too long")
    private String firstName;
    @Size(max = 80, message = "lastName is too long")
    private String lastName;
    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;
    @NotBlank(message = "password is required")
    @Size(min = 8, max = 100, message = "password must be 8-100 characters")
    private String password;
    private String phone;
    private RoleType role;

    // ---- Student-specific — public registration always creates a
    // STUDENT (see AuthServiceImpl.register()). currentClass and
    // examType are both REQUIRED: examType decides which course
    // (JEE/NEET) the student is auto-enrolled into so they can generate
    // their own plan immediately after registering. Teacher assignment
    // is intentionally NOT part of registration anymore — only an Admin
    // assigns a teacher, from Admin -> Students. ----
    private StudentClass currentClass;

    private ExamType examType;

    public RegisterRequest() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public StudentClass getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(StudentClass currentClass) {
        this.currentClass = currentClass;
    }



    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }
}
