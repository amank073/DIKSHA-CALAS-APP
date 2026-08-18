package com.diksha.controller;

import com.diksha.dto.MilestoneRequest;
import com.diksha.dto.MilestoneResponse;
import com.diksha.service.MilestoneService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/milestones")
@CrossOrigin("*")
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(
            MilestoneService milestoneService) {

        this.milestoneService = milestoneService;
    }

    @GetMapping
    public List<MilestoneResponse> getMyMilestones(
            Authentication authentication) {

        return milestoneService.getMyMilestones(
                authentication.getName()
        );
    }

    @GetMapping("/{milestoneId}")
    public MilestoneResponse getById(
            @PathVariable Long milestoneId,
            Authentication authentication) {

        return milestoneService.getById(
                milestoneId,
                authentication.getName()
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MilestoneResponse create(
            @RequestBody MilestoneRequest request,
            Authentication authentication) {

        return milestoneService.create(
                request,
                authentication.getName()
        );
    }

    @PutMapping("/{milestoneId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public MilestoneResponse update(
            @PathVariable Long milestoneId,
            @RequestBody MilestoneRequest request,
            Authentication authentication) {

        return milestoneService.update(
                milestoneId,
                request,
                authentication.getName()
        );
    }

    @DeleteMapping("/{milestoneId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long milestoneId,
            Authentication authentication) {

        milestoneService.delete(
                milestoneId,
                authentication.getName()
        );
    }
}