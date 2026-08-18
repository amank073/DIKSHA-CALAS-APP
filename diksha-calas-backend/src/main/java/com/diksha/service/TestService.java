package com.diksha.service;

import com.diksha.dto.TestRequest;
import com.diksha.dto.TestResponse;

import java.util.List;

public interface TestService {

    TestResponse create(TestRequest request, String teacherEmail);

    List<TestResponse> getByTopic(Long topicId);

    TestResponse getById(Long testId);

    void deactivate(Long testId);
}
