package com.diksha.service;

import com.diksha.dto.StudentDashboardResponse;

public interface StudentDashboardService {

    StudentDashboardResponse getDashboard(String email);
}

