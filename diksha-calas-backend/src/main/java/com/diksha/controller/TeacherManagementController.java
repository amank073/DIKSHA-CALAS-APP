package com.diksha.controller;

import com.diksha.dto.CreateTeacherRequest;
import com.diksha.dto.StudentProfileResponse;
import com.diksha.dto.TeacherResponse;
import com.diksha.service.TeacherManagementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/teachers")
@CrossOrigin("*")
public class TeacherManagementController {

    private final TeacherManagementService teacherService;

    public TeacherManagementController(
            TeacherManagementService teacherService) {

        this.teacherService = teacherService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public TeacherResponse createTeacher(
            @RequestBody CreateTeacherRequest request,
            Authentication authentication) {

        return teacherService.createTeacher(
                request,
                authentication.getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<TeacherResponse> getTeachers(
            Authentication authentication) {

        return teacherService.getTeachers(
                authentication.getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{teacherId}")
    public void deleteTeacher(
            @PathVariable Long teacherId,
            Authentication authentication) {

        teacherService.deleteTeacher(
                teacherId,
                authentication.getName()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{teacherId}")
    public TeacherResponse getTeacher(
            @PathVariable Long teacherId,
            Authentication authentication) {

        return teacherService.getTeacher(
                teacherId,
                authentication.getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{teacherId}")
    public TeacherResponse updateTeacher(
            @PathVariable Long teacherId,
            @RequestBody CreateTeacherRequest request,
            Authentication authentication) {

        return teacherService.updateTeacher(
                teacherId,
                request,
                authentication.getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{teacherId}/status")
    public TeacherResponse setTeacherStatus(
            @PathVariable Long teacherId,
            @RequestParam boolean enabled,
            Authentication authentication) {

        return teacherService.setTeacherStatus(
                teacherId,
                enabled,
                authentication.getName());
    }

    @GetMapping("/assigned-students")
    @PreAuthorize("hasRole('TEACHER')")
    public List<StudentProfileResponse> getAssignedStudents(
            Authentication authentication) {

        return teacherService.getAssignedStudents(
                authentication.getName());
    }
}