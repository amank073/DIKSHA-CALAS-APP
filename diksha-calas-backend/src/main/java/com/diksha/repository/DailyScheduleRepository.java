package com.diksha.repository;

import com.diksha.entity.DailySchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyScheduleRepository
        extends JpaRepository<DailySchedule, Long> {

    List<DailySchedule> findByStudyPlanIdOrderByScheduledDateAsc(
            Long studyPlanId
    );

    List<DailySchedule> findByStudyPlanIdAndScheduledDate(
            Long studyPlanId,
            LocalDate scheduledDate
    );

    void deleteByStudyPlanId(Long studyPlanId);
}