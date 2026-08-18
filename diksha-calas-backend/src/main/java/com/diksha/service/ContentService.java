package com.diksha.service;

import com.diksha.dto.ContentRequest;
import com.diksha.dto.ContentResponse;

import java.util.List;

public interface ContentService {

    ContentResponse createContent(Long topicId, ContentRequest request);

    List<ContentResponse> getContentsByTopic(Long topicId);

    ContentResponse getContentById(Long id);

    ContentResponse updateContent(Long id, ContentRequest request);

    void deleteContent(Long id);
}