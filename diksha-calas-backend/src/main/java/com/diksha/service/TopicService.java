package com.diksha.service;

import com.diksha.dto.TopicRequest;
import com.diksha.dto.TopicResponse;

import java.util.List;

public interface TopicService {

    TopicResponse createTopic(Long subjectId, TopicRequest request);

    List<TopicResponse> getTopicsBySubject(Long subjectId);

    TopicResponse getTopicById(Long id);

    TopicResponse updateTopic(Long id, TopicRequest request);

    void deleteTopic(Long id);
}