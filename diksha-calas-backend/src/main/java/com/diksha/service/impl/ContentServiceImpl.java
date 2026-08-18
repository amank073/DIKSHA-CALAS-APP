package com.diksha.service.impl;

import com.diksha.dto.ContentRequest;
import com.diksha.dto.ContentResponse;
import com.diksha.entity.Content;
import com.diksha.entity.Topic;
import com.diksha.repository.ContentRepository;
import com.diksha.repository.TopicRepository;
import com.diksha.service.ContentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final TopicRepository topicRepository;

    public ContentServiceImpl(
            ContentRepository contentRepository,
            TopicRepository topicRepository) {

        this.contentRepository = contentRepository;
        this.topicRepository = topicRepository;
    }

    @Override
    public ContentResponse createContent(
            Long topicId,
            ContentRequest request) {

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() ->
                        new RuntimeException("Topic not found"));

        if (contentRepository.existsByTitleAndTopicId(
                request.getTitle(), topicId)) {

            throw new RuntimeException(
                    "Content already exists in this topic");
        }

        Content content = new Content();

        content.setTitle(request.getTitle());
        content.setDescription(request.getDescription());
        content.setContentType(request.getContentType());
        content.setContentUrl(request.getContentUrl());
        content.setTopic(topic);

        Content savedContent = contentRepository.save(content);

        return mapToResponse(savedContent);
    }

    @Override
    public List<ContentResponse> getContentsByTopic(Long topicId) {

        if (!topicRepository.existsById(topicId)) {
            throw new RuntimeException("Topic not found");
        }

        return contentRepository.findByTopicId(topicId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ContentResponse getContentById(Long id) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Content not found"));

        return mapToResponse(content);
    }

    @Override
    public ContentResponse updateContent(
            Long id,
            ContentRequest request) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Content not found"));

        content.setTitle(request.getTitle());
        content.setDescription(request.getDescription());
        content.setContentType(request.getContentType());
        content.setContentUrl(request.getContentUrl());

        Content updatedContent = contentRepository.save(content);

        return mapToResponse(updatedContent);
    }

    @Override
    public void deleteContent(Long id) {

        if (!contentRepository.existsById(id)) {
            throw new RuntimeException("Content not found");
        }

        contentRepository.deleteById(id);
    }

    private ContentResponse mapToResponse(Content content) {

        return new ContentResponse(
                content.getId(),
                content.getTitle(),
                content.getDescription(),
                content.getContentType(),
                content.getContentUrl(),
                content.getTopic().getId(),
                content.isActive()
        );
    }
}