package com.diksha.repository;

import com.diksha.entity.DailySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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

    @Modifying
    @Query("DELETE FROM DailySchedule d WHERE d.studyPlan.id = :studyPlanId")
    void deleteByStudyPlanId(Long studyPlanId);
    @Modifying
    @Query("DELETE FROM DailySchedule d WHERE d.studyPlan.student.id = :studentId")
    void deleteByStudentId(@org.springframework.data.repository.query.Param("studentId") Long studentId);
}
