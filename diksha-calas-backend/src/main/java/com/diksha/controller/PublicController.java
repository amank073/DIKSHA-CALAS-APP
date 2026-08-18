package com.diksha.controller;

import com.diksha.dto.PublicTeacherResponse;
import com.diksha.entity.TeacherProfile;
import com.diksha.entity.User;
import com.diksha.enums.RoleType;
import com.diksha.repository.TeacherProfileRepository;
import com.diksha.repository.UserRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Mirrors the reference Python backend's GET /api/users/teachers being
 * public — the student registration page needs a teacher dropdown before
 * the student is logged in, so this can't sit behind the ADMIN-only
 * /api/admin/teachers endpoint.
 */
@RestController
@RequestMapping("/api/public")
@CrossOrigin("*")
public class PublicController {

    private final UserRepository userRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    public PublicController(UserRepository userRepository,
                            TeacherProfileRepository teacherProfileRepository) {
        this.userRepository = userRepository;
        this.teacherProfileRepository = teacherProfileRepository;
    }

    @GetMapping("/teachers")
    public List<PublicTeacherResponse> getPublicTeachers() {

        return userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getRole() != null
                                && user.getRole().getName() == RoleType.TEACHER
                                && Boolean.TRUE.equals(user.getEnabled()))
                .map(user -> new PublicTeacherResponse(
                        user.getId(),
                        user.getFirstName() + " " + user.getLastName(),
                        teacherProfileRepository.findByUserId(user.getId())
                                .map(TeacherProfile::getSubjectSpecialization)
                                .orElse(null)
                ))
                .toList();
    }
}
