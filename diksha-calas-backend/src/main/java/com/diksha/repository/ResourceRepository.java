package com.diksha.repository;

import com.diksha.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository
        extends JpaRepository<Resource, Long> {

    List<Resource> findByTopicIdAndActive(
            Long topicId,
            boolean active
    );

    boolean existsByTitleAndTopicId(
            String title,
            Long topicId
    );
}

