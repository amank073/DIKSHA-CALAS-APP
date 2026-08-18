package com.diksha.service;

import com.diksha.dto.DailyScheduleResponse;
import com.diksha.dto.ManualScheduleOverrideRequest;
import com.diksha.dto.StudyPlanRequest;
import com.diksha.dto.StudyPlanResponse;

import java.time.LocalDate;
import java.util.List;

public interface StudyPlanService {

    StudyPlanResponse getStudentActivePlan(
        Long studentId,
        String teacherEmail
);

List<?> getStudentScheduleForDate(
        Long studentId,
        LocalDate date,
        String teacherEmail
);

    StudyPlanResponse generatePlan(
            StudyPlanRequest request,
            String email
    );

    StudyPlanResponse generatePlanForStudent(
            StudyPlanRequest request,
            Long studentId,
            String actorEmail
    );

    StudyPlanResponse generateSystemPlanForStudent(
            StudyPlanRequest request,
            com.diksha.entity.User student
    );

    StudyPlanResponse getActivePlan(
            String email
    );

    List<StudyPlanResponse> getMyPlans(
            String email
    );

    List<?> getScheduleForDate(
            String email,
            LocalDate date
    );

    DailyScheduleResponse overrideSchedule(
            Long scheduleId,
            ManualScheduleOverrideRequest request,
            String actorEmail
    );
}
