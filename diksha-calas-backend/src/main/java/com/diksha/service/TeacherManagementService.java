package com.diksha.service;

import com.diksha.dto.CreateTeacherRequest;
import com.diksha.dto.StudentProfileResponse;
import com.diksha.dto.TeacherResponse;

import java.util.List;

public interface TeacherManagementService {


    List<StudentProfileResponse> getAssignedStudents(String email);
    TeacherResponse createTeacher(
            CreateTeacherRequest request,
            String email);

    List<TeacherResponse> getTeachers(
            String email);

    TeacherResponse getTeacher(
            Long teacherId,
            String email);

    TeacherResponse updateTeacher(
            Long teacherId,
            CreateTeacherRequest request,
            String email);

    TeacherResponse setTeacherStatus(
            Long teacherId,
            boolean enabled,
            String email
    );

    void deleteTeacher(
            Long teacherId,
            String email
    );
}