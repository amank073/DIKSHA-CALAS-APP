package com.diksha.service.impl;

import com.diksha.dto.MilestoneRequest;
import com.diksha.dto.MilestoneResponse;
import com.diksha.entity.Milestone;
import com.diksha.entity.User;
import com.diksha.repository.MilestoneRepository;
import com.diksha.repository.UserRepository;
import com.diksha.service.MilestoneService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final UserRepository userRepository;

    public MilestoneServiceImpl(
            MilestoneRepository milestoneRepository,
            UserRepository userRepository) {

        this.milestoneRepository = milestoneRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public MilestoneResponse create(
            MilestoneRequest request,
            String email) {

        // Logged-in user (Admin / Teacher)
        getUser(email);

        validate(request);

        User student =
                userRepository
                        .findById(request.getStudentId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found"
                                ));

        // Milestone must belong to a STUDENT
        if (student.getRole() == null ||
                !"STUDENT".equals(
                        student.getRole()
                                .getName()
                                .name()
                )) {

            throw new RuntimeException(
                    "Milestone can only be assigned to a student"
            );
        }

        Milestone milestone = new Milestone();

        milestone.setStudent(student);

        milestone.setMonthNumber(
                request.getMonthNumber()
        );

        milestone.setTitle(
                request.getTitle()
        );

        milestone.setScoreObtained(
                request.getScoreObtained()
        );

        milestone.setMaxScore(
                request.getMaxScore()
        );

        milestone.setAssessmentDate(
                request.getAssessmentDate()
        );

        Milestone saved =
                milestoneRepository.save(milestone);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneResponse> getMyMilestones(
            String email) {

        User user = getUser(email);

        return milestoneRepository
                .findByStudentIdOrderByMonthNumberAsc(
                        user.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneResponse getById(
            Long milestoneId,
            String email) {

        User user = getUser(email);

        Milestone milestone =
                milestoneRepository
                        .findById(milestoneId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Milestone not found"
                                ));

        if (!milestone.getStudent()
                .getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to access this milestone"
            );
        }

        return mapToResponse(milestone);
    }

    @Override
    @Transactional
    public MilestoneResponse update(
            Long milestoneId,
            MilestoneRequest request,
            String email) {

        // Ensure logged-in user exists
        getUser(email);

        validate(request);

        Milestone milestone =
                milestoneRepository
                        .findById(milestoneId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Milestone not found"
                                ));

        milestone.setMonthNumber(
                request.getMonthNumber()
        );

        milestone.setTitle(
                request.getTitle()
        );

        milestone.setScoreObtained(
                request.getScoreObtained()
        );

        milestone.setMaxScore(
                request.getMaxScore()
        );

        milestone.setAssessmentDate(
                request.getAssessmentDate()
        );

        return mapToResponse(
                milestoneRepository.save(milestone)
        );
    }

    @Override
    @Transactional
    public void delete(
            Long milestoneId,
            String email) {

        // Ensure logged-in user exists
        getUser(email);

        Milestone milestone =
                milestoneRepository
                        .findById(milestoneId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Milestone not found"
                                ));

        milestoneRepository.delete(milestone);
    }

    private User getUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));
    }

    private void validate(
            MilestoneRequest request) {

        if (request == null) {
            throw new RuntimeException(
                    "Milestone request is required"
            );
        }
        if (request.getStudentId() == null) {
            throw new RuntimeException(
                    "studentId is required"
            );
        }

        if (request.getMonthNumber() <= 0) {
            throw new RuntimeException(
                    "monthNumber must be greater than 0"
            );
        }

        if (request.getTitle() == null ||
                request.getTitle().isBlank()) {

            throw new RuntimeException(
                    "title is required"
            );
        }

        if (request.getScoreObtained() < 0) {
            throw new RuntimeException(
                    "scoreObtained cannot be negative"
            );
        }

        if (request.getMaxScore() <= 0) {
            throw new RuntimeException(
                    "maxScore must be greater than 0"
            );
        }

        if (request.getScoreObtained()
                > request.getMaxScore()) {

            throw new RuntimeException(
                    "scoreObtained cannot exceed maxScore"
            );
        }

        if (request.getAssessmentDate() == null) {
            throw new RuntimeException(
                    "assessmentDate is required"
            );
        }
    }

    private MilestoneResponse mapToResponse(
            Milestone milestone) {

        return new MilestoneResponse(
                milestone.getId(),
                milestone.getStudent().getId(),
                milestone.getMonthNumber(),
                milestone.getTitle(),
                milestone.getScoreObtained(),
                milestone.getMaxScore(),
                milestone.getAssessmentDate()
        );
    }
}