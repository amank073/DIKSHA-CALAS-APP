package com.diksha.repository;

import com.diksha.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentProfileRepository
        extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);

    @Query("SELECT s FROM StudentProfile s WHERE s.physicsTeacher.id = :teacherId OR s.chemistryTeacher.id = :teacherId OR s.mathsTeacher.id = :teacherId OR s.biologyTeacher.id = :teacherId")
    List<StudentProfile> findByAnyTeacherId(@Param("teacherId") Long teacherId);

    boolean existsByUserId(Long userId);
}