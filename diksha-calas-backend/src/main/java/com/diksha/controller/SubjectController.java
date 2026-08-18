package com.diksha.controller;

import com.diksha.dto.SubjectRequest;
import com.diksha.dto.SubjectResponse;
import com.diksha.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    // =========================================================
    // CREATE SUBJECT
    // Teacher/Admin only
    // =========================================================

    @PostMapping("/subjects")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectResponse createSubject(
            @RequestBody SubjectRequest request) {

        return subjectService.createSubject(
                request
        );
    }

    // =========================================================
    // GET SUBJECTS
    // Any authenticated user
    // =========================================================

    @GetMapping("/subjects")
    public List<SubjectResponse> getAllSubjects() {

        return subjectService.getAllSubjects();
    }

    // =========================================================
    // GET SUBJECT
    // Any authenticated user
    // =========================================================

    @GetMapping("/subjects/{id}")
    public SubjectResponse getSubjectById(
            @PathVariable Long id) {

        return subjectService.getSubjectById(id);
    }

    // =========================================================
    // UPDATE SUBJECT
    // Teacher/Admin only
    // =========================================================

    @PutMapping("/subjects/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public SubjectResponse updateSubject(
            @PathVariable Long id,
            @RequestBody SubjectRequest request) {

        return subjectService.updateSubject(
                id,
                request
        );
    }

    // =========================================================
    // DELETE SUBJECT
    // Teacher/Admin only
    // =========================================================

    @DeleteMapping("/subjects/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubject(
            @PathVariable Long id) {

        subjectService.deleteSubject(id);
    }
}