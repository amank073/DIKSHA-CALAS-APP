package com.diksha.service.impl;

import com.diksha.dto.ResourceRequest;
import com.diksha.dto.ResourceResponse;
import com.diksha.entity.Resource;
import com.diksha.entity.Topic;
import com.diksha.repository.ResourceRepository;
import com.diksha.repository.TopicRepository;
import com.diksha.service.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceServiceImpl
        implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final TopicRepository topicRepository;

    public ResourceServiceImpl(
            ResourceRepository resourceRepository,
            TopicRepository topicRepository) {

        this.resourceRepository = resourceRepository;
        this.topicRepository = topicRepository;
    }

    @Override
    @Transactional
    public ResourceResponse create(
            ResourceRequest request) {

        Topic topic = topicRepository
                .findById(request.getTopicId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Topic not found"));

        if (resourceRepository.existsByTitleAndTopicId(
                request.getTitle(),
                request.getTopicId())) {

            throw new RuntimeException(
                    "Resource already exists for this topic");
        }

        Resource resource = new Resource();

        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setResourceType(request.getResourceType());
        resource.setResourceUrl(request.getResourceUrl());
        resource.setTopic(topic);

        Resource saved =
                resourceRepository.save(resource);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getByTopic(
            Long topicId) {

        return resourceRepository
                .findByTopicIdAndActive(topicId, true)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getById(
            Long resourceId) {

        Resource resource =
                resourceRepository.findById(resourceId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Resource not found"));

        return mapToResponse(resource);
    }

    @Override
    @Transactional
    public void deactivate(Long resourceId) {

        Resource resource =
                resourceRepository.findById(resourceId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Resource not found"));

        resource.setActive(false);

        resourceRepository.save(resource);
    }

    private ResourceResponse mapToResponse(
            Resource resource) {

        return new ResourceResponse(
                resource.getId(),
                resource.getTitle(),
                resource.getDescription(),
                resource.getResourceType(),
                resource.getResourceUrl(),
                resource.getTopic().getId(),
                resource.isActive()
        );
    }
}

