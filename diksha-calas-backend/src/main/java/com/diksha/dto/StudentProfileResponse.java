package com.diksha.dto;

import com.diksha.enums.StudentClass;

public class StudentProfileResponse {

    private Long profileId;
    private Long studentId;
    private String studentName;
    private String email;
    private String phone;
    private Long teacherId;
    private String teacherName;
    private StudentClass currentClass;

    private String targetExam;

    public StudentProfileResponse(
            Long profileId,
            Long studentId,
            String studentName,
            String email,
            String phone,
            Long teacherId,
            String teacherName,
            StudentClass currentClass,

            String targetExam) {

        this.profileId = profileId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.phone = phone;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.currentClass = currentClass;

        this.targetExam = targetExam;
    }

    public Long getProfileId() {
        return profileId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public StudentClass getCurrentClass() {
        return currentClass;
    }



    public String getTargetExam() {
        return targetExam;
    }
}
