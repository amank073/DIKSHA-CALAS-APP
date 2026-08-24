package com.diksha.repository;

import com.diksha.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MilestoneRepository
        extends JpaRepository<Milestone, Long> {

    List<Milestone> findByStudentIdOrderByMonthNumberAsc(
            Long studentId
    );

    @Modifying
    @Query("DELETE FROM Milestone m WHERE m.student.id = :studentId")
    void deleteByStudentId(Long studentId);
}