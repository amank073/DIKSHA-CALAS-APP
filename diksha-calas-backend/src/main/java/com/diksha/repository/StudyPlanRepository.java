package com.diksha.repository;

import com.diksha.entity.StudyPlan;
import com.diksha.enums.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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

    void deleteByStudentId(Long studentId);
}