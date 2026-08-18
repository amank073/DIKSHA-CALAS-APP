package com.diksha.service.impl;

import com.diksha.dto.TestRequest;
import com.diksha.dto.TestResponse;
import com.diksha.entity.Test;
import com.diksha.entity.Topic;
import com.diksha.entity.User;
import com.diksha.repository.TestRepository;
import com.diksha.repository.TopicRepository;
import com.diksha.repository.UserRepository;
import com.diksha.service.TestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TestServiceImpl
        implements TestService {

    private final TestRepository testRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public TestServiceImpl(
            TestRepository testRepository,
            TopicRepository topicRepository,
            UserRepository userRepository) {

        this.testRepository = testRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public TestResponse create(TestRequest request, String teacherEmail) {

        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Teacher not found"));

        // Topic is optional — a Subject Wise / mixed-subject test isn't tied to one topic.
        Topic topic = null;
        if (request.getTopicId() != null) {
            topic = topicRepository
                    .findById(request.getTopicId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Topic not found"));
        } else if (!request.isMixedSubject()) {
            throw new RuntimeException(
                    "topicId is required for a Topic Wise Test");
        }

        Test test = new Test();

        test.setMixedSubject(request.isMixedSubject());
        test.setTitle(request.getTitle());
        test.setDescription(request.getDescription());
        test.setTestType(request.getTestType());
        test.setDurationMinutes(
                request.getDurationMinutes());
        test.setTotalMarks(
                request.getTotalMarks());
        test.setLink(request.getLink());
        test.setTopic(topic);
        test.setCreatedByTeacher(teacher);

        Test saved = testRepository.save(test);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestResponse> getByTopic(
            Long topicId) {

        return testRepository
                .findByTopicIdAndActive(topicId, true)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TestResponse getById(Long testId) {

        Test test = testRepository
                .findById(testId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Test not found"));

        return mapToResponse(test);
    }

    @Override
    @Transactional
    public void deactivate(Long testId) {

        Test test = testRepository
                .findById(testId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Test not found"));

        test.setActive(false);

        testRepository.save(test);
    }

    private TestResponse mapToResponse(Test test) {

        User teacher = test.getCreatedByTeacher();

        return new TestResponse(
                test.getId(),
                test.getTitle(),
                test.getDescription(),
                test.getTestType(),
                test.getDurationMinutes(),
                test.getTotalMarks(),
                test.getLink(),
                test.getTopic() == null
                        ? null
                        : test.getTopic().getId(),
                test.isMixedSubject(),
                teacher == null ? null : teacher.getId(),
                teacher == null ? null : teacher.getFirstName() + " " + teacher.getLastName(),
                test.isActive()
        );
    }
}
