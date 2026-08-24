package com.diksha.repository;

import com.diksha.entity.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentProgressRepository
        extends JpaRepository<StudentProgress, Long> {

    Optional<StudentProgress> findByUserIdAndContentId(
            Long userId,
            Long contentId
    );

    List<StudentProgress> findByUserId(Long userId);


    @Modifying
    @Query("DELETE FROM StudentProgress s WHERE s.user.id = :userId")
    void deleteByUserId(Long userId);
}