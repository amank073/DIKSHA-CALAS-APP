package com.diksha.dto;

public class StudentDashboardResponse {

    private String studentName;

    public StudentDashboardResponse(
            String studentName) {

        this.studentName = studentName;
    }

    public String getStudentName() {
        return studentName;
    }
}
