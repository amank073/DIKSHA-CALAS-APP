package com.diksha.repository;

import com.diksha.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneRepository
        extends JpaRepository<Milestone, Long> {

    List<Milestone> findByStudentIdOrderByMonthNumberAsc(
            Long studentId
    );

    void deleteByStudentId(Long studentId);
}