package com.diksha.controller;

import com.diksha.dto.ContentRequest;
import com.diksha.dto.ContentResponse;
import com.diksha.service.ContentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

        @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping("/topics/{topicId}/contents")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentResponse createContent(
            @PathVariable Long topicId,
            @RequestBody ContentRequest request) {

        return contentService.createContent(topicId, request);
    }

    @GetMapping("/topics/{topicId}/contents")
    public List<ContentResponse> getContentsByTopic(
            @PathVariable Long topicId) {

        return contentService.getContentsByTopic(topicId);
    }

    @GetMapping("/contents/{id}")
    public ContentResponse getContentById(
            @PathVariable Long id) {

        return contentService.getContentById(id);
    }

        @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PutMapping("/contents/{id}")
    public ContentResponse updateContent(
            @PathVariable Long id,
            @RequestBody ContentRequest request) {

        return contentService.updateContent(id, request);
    }

        @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @DeleteMapping("/contents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContent(@PathVariable Long id) {

        contentService.deleteContent(id);
    }
}