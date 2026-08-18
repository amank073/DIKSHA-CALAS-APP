package com.diksha.service;

import com.diksha.dto.MilestoneRequest;
import com.diksha.dto.MilestoneResponse;

import java.util.List;

public interface MilestoneService {

    MilestoneResponse create(
            MilestoneRequest request,
            String email
    );

    List<MilestoneResponse> getMyMilestones(
            String email
    );

    MilestoneResponse getById(
            Long milestoneId,
            String email
    );

    MilestoneResponse update(
            Long milestoneId,
            MilestoneRequest request,
            String email
    );

    void delete(
            Long milestoneId,
            String email
    );
}