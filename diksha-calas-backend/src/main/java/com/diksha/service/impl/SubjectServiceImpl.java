package com.diksha.service.impl;

import com.diksha.dto.SubjectRequest;
import com.diksha.dto.SubjectResponse;
import com.diksha.entity.Subject;
import com.diksha.repository.SubjectRepository;
import com.diksha.service.SubjectService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class SubjectServiceImpl implements SubjectService {

    /**
     * This platform is JEE/NEET prep only — these are the only subjects
     * the SSP algorithm (see com.diksha.service.impl.StudyPlanServiceImpl
     * .collectSubjects) knows how to schedule, so creating anything else
     * would just silently never appear in a generated plan.
     */
    private static final Set<String> ALLOWED_SUBJECT_NAMES = Set.of(
            "physics", "chemistry", "mathematics", "biology"
    );

    private final SubjectRepository subjectRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    private void validateSubjectName(String subjectName) {
        if (subjectName == null || !ALLOWED_SUBJECT_NAMES.contains(subjectName.trim().toLowerCase())) {
            throw new RuntimeException(
                    "This platform only supports Physics, Chemistry, Mathematics, and Biology as subjects"
            );
        }
    }

    @Override
    public SubjectResponse createSubject(SubjectRequest request) {

        validateSubjectName(request.getSubjectName());

        if (subjectRepository.existsBySubjectName(request.getSubjectName())) {
            throw new RuntimeException("Subject already exists");
        }

        Subject subject = new Subject();

        subject.setSubjectName(request.getSubjectName());
        subject.setDescription(request.getDescription());

        Subject savedSubject = subjectRepository.save(subject);

        return mapToResponse(savedSubject);
    }

    @Override
    public List<SubjectResponse> getAllSubjects() {

        return subjectRepository.findAll()
                .stream()
                .filter(Subject::isActive)
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SubjectResponse getSubjectById(Long id) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        return mapToResponse(subject);
    }

    @Override
    public SubjectResponse updateSubject(Long id,
                                         SubjectRequest request) {

        validateSubjectName(request.getSubjectName());

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        subject.setSubjectName(request.getSubjectName());
        subject.setDescription(request.getDescription());

        Subject updatedSubject = subjectRepository.save(subject);

        return mapToResponse(updatedSubject);
    }

    @Override
    public void deleteSubject(Long id) {

        if (!subjectRepository.existsById(id)) {
            throw new RuntimeException("Subject not found");
        }

        subjectRepository.deleteById(id);
    }

    private SubjectResponse mapToResponse(Subject subject) {

        return new SubjectResponse(
                subject.getId(),
                subject.getSubjectName(),
                subject.getDescription(),
                subject.isActive()
        );
    }
}