package com.diksha.service;

import com.diksha.dto.ResourceRequest;
import com.diksha.dto.ResourceResponse;

import java.util.List;

public interface ResourceService {

    ResourceResponse create(ResourceRequest request);

    List<ResourceResponse> getByTopic(Long topicId);

    ResourceResponse getById(Long resourceId);

    void deactivate(Long resourceId);
}
