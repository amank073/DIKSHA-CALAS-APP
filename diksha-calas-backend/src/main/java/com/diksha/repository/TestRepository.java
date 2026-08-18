package com.diksha.repository;

import com.diksha.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRepository
        extends JpaRepository<Test, Long> {

    List<Test> findByTopicIdAndActive(
            Long topicId,
            boolean active
    );

    List<Test> findByActive(
            boolean active
    );

    List<Test> findByActiveAndMixedSubject(
            boolean active,
            boolean mixedSubject
    );

    /** Used by AssessmentScheduler (via StudyPlanServiceImpl) to only
     *  schedule tests authored by the student's own assigned teacher. */
    List<Test> findByActiveAndCreatedByTeacherId(
            boolean active,
            Long createdByTeacherId
    );
}