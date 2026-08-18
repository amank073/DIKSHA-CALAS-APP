package com.diksha.service.impl;

import com.diksha.dto.StudentDashboardResponse;
import com.diksha.entity.User;
import com.diksha.repository.UserRepository;
import com.diksha.service.StudentDashboardService;
import com.diksha.service.StudentProgressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentDashboardServiceImpl
        implements StudentDashboardService {

    private final UserRepository userRepository;
    private final StudentProgressService progressService;

    public StudentDashboardServiceImpl(
            UserRepository userRepository,
            StudentProgressService progressService) {

        this.userRepository = userRepository;
        this.progressService = progressService;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDashboardResponse getDashboard(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return new StudentDashboardResponse(
                user.getEmail()
        );
    }
}
