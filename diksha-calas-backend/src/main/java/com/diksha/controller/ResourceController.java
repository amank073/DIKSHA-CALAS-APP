package com.diksha.controller;

import com.diksha.dto.ResourceRequest;
import com.diksha.dto.ResourceResponse;
import com.diksha.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin("*")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(
            ResourceService resourceService) {

        this.resourceService = resourceService;
    }

    // =========================================================
    // CREATE RESOURCE
    // Teacher/Admin only
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse create(
            @RequestBody ResourceRequest request) {

        return resourceService.create(request);
    }

    // =========================================================
    // GET RESOURCES BY TOPIC
    // Any authenticated user
    // =========================================================

    @GetMapping("/topic/{topicId}")
    public List<ResourceResponse> getByTopic(
            @PathVariable Long topicId) {

        return resourceService.getByTopic(topicId);
    }

    // =========================================================
    // GET RESOURCE
    // Any authenticated user
    // =========================================================

    @GetMapping("/{resourceId}")
    public ResourceResponse getById(
            @PathVariable Long resourceId) {

        return resourceService.getById(resourceId);
    }

    // =========================================================
    // DEACTIVATE RESOURCE
    // Teacher/Admin only
    // =========================================================

    @DeleteMapping("/{resourceId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            @PathVariable Long resourceId) {

        resourceService.deactivate(resourceId);
    }
}