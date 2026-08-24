package com.diksha.repository;

import com.diksha.entity.StudyPlan;
import com.diksha.enums.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudyPlanRepository
        extends JpaRepository<StudyPlan, Long> {

    Optional<StudyPlan> findFirstByStudentIdAndStatusOrderByCreatedAtDesc(
            Long studentId,
            PlanStatus status
    );

    List<StudyPlan> findByStudentIdOrderByCreatedAtDesc(
            Long studentId
    );

    @Modifying
    @Query("DELETE FROM StudyPlan s WHERE s.student.id = :studentId")
    void deleteByStudentId(@org.springframework.data.repository.query.Param("studentId") Long studentId);
}