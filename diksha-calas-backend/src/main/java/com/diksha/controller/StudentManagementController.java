package com.diksha.controller;

import com.diksha.dto.AssignTeacherRequest;
import com.diksha.dto.StudentDashboardProgressResponse;
import com.diksha.dto.StudentProfileResponse;
import com.diksha.dto.StudentUpdateRequest;
import com.diksha.service.StudentManagementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class StudentManagementController {

    private final StudentManagementService studentService;

    public StudentManagementController(
            StudentManagementService studentService) {

        this.studentService = studentService;
    }

    // =========================================================
    // ADMIN - GET ALL STUDENTS
    // =========================================================

    @GetMapping("/admin/students")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<StudentProfileResponse> getStudents(
            Authentication authentication) {

        return studentService.getStudents(
                authentication.getName());
    }

    // =========================================================
    // GET SINGLE STUDENT
    // =========================================================

    @GetMapping("/students/{studentId}")
    public StudentProfileResponse getStudent(
            @PathVariable Long studentId,
            Authentication authentication) {

        return studentService.getStudent(
                studentId,
                authentication.getName());
    }

    // =========================================================
    // TEACHER - GET STUDENT PROGRESS
    // =========================================================

    @GetMapping("/teacher/students/{studentId}/progress")
    @PreAuthorize("hasRole('TEACHER')")
    public StudentDashboardProgressResponse getStudentProgress(
            @PathVariable Long studentId,
            Authentication authentication) {

        return studentService.getStudentProgress(
                studentId,
                authentication.getName());
    }

    // =========================================================
    // ADMIN - ASSIGN TEACHER TO STUDENT
    // =========================================================

    @PutMapping("/admin/students/{studentId}/teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public StudentProfileResponse assignTeacher(
            @PathVariable Long studentId,
            @RequestBody AssignTeacherRequest request,
            Authentication authentication) {

        return studentService.assignTeacher(
                studentId,
                request,
                authentication.getName());
    }

    // =========================================================
    // UPDATE STUDENT
    // =========================================================

    @PutMapping("/admin/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public StudentProfileResponse updateStudent(
            @PathVariable Long studentId,
            @RequestBody StudentUpdateRequest request,
            Authentication authentication) {

        return studentService.updateStudent(
                studentId,
                request,
                authentication.getName());
    }

    // =========================================================
    // ADMIN - DELETE STUDENT
    // =========================================================

    @DeleteMapping("/admin/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteStudent(
            @PathVariable Long studentId,
            Authentication authentication) {

        studentService.deleteStudent(
                studentId,
                authentication.getName());
    }
}