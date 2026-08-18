package com.diksha.controller;

import com.diksha.dto.TopicRequest;
import com.diksha.dto.TopicResponse;
import com.diksha.service.TopicService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    // =========================================================
    // CREATE TOPIC
    // Teacher/Admin only
    // =========================================================

    @PostMapping("/subjects/{subjectId}/topics")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public TopicResponse createTopic(
            @PathVariable Long subjectId,
            @RequestBody TopicRequest request) {

        return topicService.createTopic(
                subjectId,
                request
        );
    }

    // =========================================================
    // GET TOPICS
    // Any authenticated user
    // =========================================================

    @GetMapping("/subjects/{subjectId}/topics")
    public List<TopicResponse> getTopicsBySubject(
            @PathVariable Long subjectId) {

        return topicService.getTopicsBySubject(subjectId);
    }

    // =========================================================
    // GET TOPIC
    // Any authenticated user
    // =========================================================

    @GetMapping("/topics/{id}")
    public TopicResponse getTopicById(
            @PathVariable Long id) {

        return topicService.getTopicById(id);
    }

    // =========================================================
    // UPDATE TOPIC
    // Teacher/Admin only
    // =========================================================

    @PutMapping("/topics/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public TopicResponse updateTopic(
            @PathVariable Long id,
            @RequestBody TopicRequest request) {

        return topicService.updateTopic(
                id,
                request
        );
    }

    // =========================================================
    // DELETE TOPIC
    // Teacher/Admin only
    // =========================================================

    @DeleteMapping("/topics/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTopic(
            @PathVariable Long id) {

        topicService.deleteTopic(id);
    }
}