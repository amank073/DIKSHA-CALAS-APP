package com.diksha.dto;

public class AssignTeacherRequest {

    private Long teacherId;
    private String subject;

    public AssignTeacherRequest() {
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}