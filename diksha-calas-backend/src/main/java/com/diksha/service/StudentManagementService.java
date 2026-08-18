package com.diksha.service;

import com.diksha.dto.StudentDashboardProgressResponse;
import com.diksha.dto.AssignTeacherRequest;
import com.diksha.dto.StudentProfileResponse;
import com.diksha.dto.StudentUpdateRequest;

import java.util.List;

public interface StudentManagementService {

    List<StudentProfileResponse> getStudents(String email);

    StudentProfileResponse getStudent(Long studentId, String email);
       
    StudentDashboardProgressResponse getStudentProgress(
        Long studentId,
        String email
);

    StudentProfileResponse assignTeacher(
            Long studentId,
            AssignTeacherRequest request,
            String email);

    StudentProfileResponse updateStudent(
            Long studentId,
            StudentUpdateRequest request,
            String email);

    void deleteStudent(Long studentId, String email);
}


