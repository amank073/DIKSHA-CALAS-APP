package com.diksha.repository;

import com.diksha.entity.DailyProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DailyProgressRepository
        extends JpaRepository<DailyProgress, Long> {

    List<DailyProgress> findByStudentIdOrderByLoggedAtDesc(
            Long studentId
    );

    Optional<DailyProgress> findByStudentIdAndDailyScheduleId(
            Long studentId,
            Long dailyScheduleId
    );

    @Modifying
    @Query("DELETE FROM DailyProgress d WHERE d.student.id = :studentId")
    void deleteByStudentId(Long studentId);
}