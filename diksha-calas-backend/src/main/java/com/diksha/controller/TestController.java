package com.diksha.controller;

import com.diksha.dto.TestRequest;
import com.diksha.dto.TestResponse;
import com.diksha.service.TestService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin("*")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

        @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestResponse create(
            @RequestBody TestRequest request,
            Authentication authentication) {

        return testService.create(request, authentication.getName());
    }

    @GetMapping("/topic/{topicId}")
    public List<TestResponse> getByTopic(
            @PathVariable Long topicId) {

        return testService.getByTopic(topicId);
    }

    @GetMapping("/{testId}")
    public TestResponse getById(
            @PathVariable Long testId) {

        return testService.getById(testId);
    }

        @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @DeleteMapping("/{testId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            @PathVariable Long testId) {

        testService.deactivate(testId);
    }
}
