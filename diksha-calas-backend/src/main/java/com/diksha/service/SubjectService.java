package com.diksha.service;

import com.diksha.dto.SubjectRequest;
import com.diksha.dto.SubjectResponse;

import java.util.List;

public interface SubjectService {

    SubjectResponse createSubject(SubjectRequest request);

    List<SubjectResponse> getAllSubjects();


    SubjectResponse getSubjectById(Long id);

    SubjectResponse updateSubject(Long id, SubjectRequest request);

    void deleteSubject(Long id);
}