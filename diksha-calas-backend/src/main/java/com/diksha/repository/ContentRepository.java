package com.diksha.repository;

import com.diksha.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository
        extends JpaRepository<Content, Long> {

    List<Content> findByTopicId(Long topicId);

    boolean existsByTitleAndTopicId(
            String title,
            Long topicId
    );

}