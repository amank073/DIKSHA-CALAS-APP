package com.diksha.service.impl;

import com.diksha.dto.TopicRequest;
import com.diksha.dto.TopicResponse;
import com.diksha.entity.Subject;
import com.diksha.entity.Topic;
import com.diksha.repository.SubjectRepository;
import com.diksha.repository.TopicRepository;
import com.diksha.service.TopicService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;

    public TopicServiceImpl(
            TopicRepository topicRepository,
            SubjectRepository subjectRepository) {

        this.topicRepository = topicRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public TopicResponse createTopic(
            Long subjectId,
            TopicRequest request) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        if (topicRepository.existsByTopicNameAndSubjectId(
                request.getTopicName(),
                subjectId)) {

            throw new RuntimeException(
                    "Topic already exists in this subject");
        }

        Topic topic = new Topic();

        topic.setTopicName(request.getTopicName());
        topic.setDescription(request.getDescription());
        topic.setSubject(subject);

        if (request.getSyllabusClass() != null
                && !request.getSyllabusClass().isBlank()) {

            topic.setSyllabusClass(
                    request.getSyllabusClass()
            );
        }

        topic.setTisScore(request.getTisScore());

        if (request.getParentTopicId() != null) {

            Topic parentTopic =
                    topicRepository.findById(
                            request.getParentTopicId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Parent topic not found"));

            if (!parentTopic.getSubject()
                    .getId()
                    .equals(subjectId)) {

                throw new RuntimeException(
                        "Parent topic must belong to the same subject");
            }

            topic.setParentTopic(parentTopic);
        }

        Topic savedTopic =
                topicRepository.save(topic);

        return mapToResponse(savedTopic);
    }

    @Override
    public List<TopicResponse> getTopicsBySubject(
            Long subjectId) {

        if (!subjectRepository.existsById(subjectId)) {
            throw new RuntimeException(
                    "Subject not found");
        }

        return topicRepository
                .findBySubjectId(subjectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public TopicResponse getTopicById(Long id) {

        Topic topic = topicRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Topic not found"));

        return mapToResponse(topic);
    }

    @Override
    public TopicResponse updateTopic(
            Long id,
            TopicRequest request) {

        Topic topic = topicRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Topic not found"));

        topic.setTopicName(
                request.getTopicName()
        );

        topic.setDescription(
                request.getDescription()
        );

        if (request.getSyllabusClass() != null
                && !request.getSyllabusClass().isBlank()) {

            topic.setSyllabusClass(
                    request.getSyllabusClass()
            );
        }

        topic.setTisScore(
                request.getTisScore()
        );

        if (request.getParentTopicId() != null) {

            Topic parentTopic =
                    topicRepository.findById(
                            request.getParentTopicId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Parent topic not found"));

            if (!parentTopic.getSubject()
                    .getId()
                    .equals(topic.getSubject().getId())) {

                throw new RuntimeException(
                        "Parent topic must belong to the same subject");
            }

            if (parentTopic.getId().equals(topic.getId())) {

                throw new RuntimeException(
                        "Topic cannot be its own parent");
            }

            topic.setParentTopic(parentTopic);

        } else {

            topic.setParentTopic(null);
        }

        Topic updatedTopic =
                topicRepository.save(topic);

        return mapToResponse(updatedTopic);
    }

    @Override
    public void deleteTopic(Long id) {

        if (!topicRepository.existsById(id)) {
            throw new RuntimeException(
                    "Topic not found");
        }

        topicRepository.deleteById(id);
    }

    private TopicResponse mapToResponse(
            Topic topic) {

        Long parentTopicId = null;

        if (topic.getParentTopic() != null) {
            parentTopicId =
                    topic.getParentTopic().getId();
        }

        return new TopicResponse(
                topic.getId(),
                topic.getTopicName(),
                topic.getDescription(),
                topic.getSubject().getId(),
                topic.getSyllabusClass(),
                topic.getTisScore(),
                parentTopicId,
                topic.isActive()
        );
    }
}