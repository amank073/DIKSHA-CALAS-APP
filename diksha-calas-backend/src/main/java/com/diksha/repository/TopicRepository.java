package com.diksha.repository;

import com.diksha.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    boolean existsByTopicNameAndSubjectId(String topicName, Long subjectId);

    List<Topic> findBySubjectId(Long subjectId);
}