package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.PagedResponse;
import com.crm.common.exception.DuplicateResourceException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.common.util.DateRangeUtil;
import com.crm.mcsv_rrhh.dto.TerminationQuizQuestionRequest;
import com.crm.mcsv_rrhh.dto.TerminationQuizQuestionResponse;
import com.crm.mcsv_rrhh.dto.UpdateTerminationQuizQuestionRequest;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.QuizQuestionGroup;
import com.crm.mcsv_rrhh.entity.TerminationQuizQuestion;
import com.crm.mcsv_rrhh.repository.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.QuizQuestionGroupRepository;
import com.crm.mcsv_rrhh.repository.TerminationQuizQuestionRepository;
import com.crm.mcsv_rrhh.repository.TerminationQuizQuestionSpecification;
import com.crm.mcsv_rrhh.service.TerminationQuizQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TerminationQuizQuestionServiceImpl implements TerminationQuizQuestionService {

    private final TerminationQuizQuestionRepository repository;
    private final QuizQuestionGroupRepository questionGroupRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TerminationQuizQuestionResponse> list(String search, Boolean active, Long questionGroupId,
                                                                Long employeeId,
                                                                LocalDate createdFrom, LocalDate createdTo,
                                                                LocalDate updatedFrom, LocalDate updatedTo,
                                                                Pageable pageable) {
        LocalDateTime cFrom = DateRangeUtil.startOf(createdFrom);
        LocalDateTime cTo   = DateRangeUtil.endOf(createdTo);
        LocalDateTime uFrom = DateRangeUtil.startOf(updatedFrom);
        LocalDateTime uTo   = DateRangeUtil.endOf(updatedTo);

        Specification<TerminationQuizQuestion> spec =
                TerminationQuizQuestionSpecification.withFilters(search, active, questionGroupId, employeeId, cFrom, cTo, uFrom, uTo);

        Page<TerminationQuizQuestion> page = repository.findAll(spec, pageable);
        long totalActive = repository.count(
                TerminationQuizQuestionSpecification.withFilters(null, true, null, null, null, null, null, null));

        return PagedResponse.of(page.map(this::toResponse), page.getTotalElements(), totalActive);
    }

    @Override
    @Transactional(readOnly = true)
    public TerminationQuizQuestionResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public TerminationQuizQuestionResponse create(TerminationQuizQuestionRequest request) {
        if (repository.existsByQuestion(request.getQuestion()))
            throw new DuplicateResourceException("Ya existe una pregunta con ese texto");

        QuizQuestionGroup group = resolveGroup(request.getQuestionGroup());

        TerminationQuizQuestion entity = TerminationQuizQuestion.builder()
                .employeeId(request.getEmployeeId())
                .question(request.getQuestion())
                .questionGroup(group)
                .required(request.getRequired() != null ? request.getRequired() : true)
                .active(true)
                .build();

        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public TerminationQuizQuestionResponse update(UpdateTerminationQuizQuestionRequest request) {
        TerminationQuizQuestion entity = findOrThrow(request.getId());

        if (request.getQuestion() != null &&
                !request.getQuestion().equals(entity.getQuestion()) &&
                repository.existsByQuestionAndIdNot(request.getQuestion(), request.getId()))
            throw new DuplicateResourceException("Ya existe una pregunta con ese texto");

        if (request.getEmployeeId() != null)    entity.setEmployeeId(request.getEmployeeId());
        if (request.getQuestion() != null)      entity.setQuestion(request.getQuestion());
        if (request.getQuestionGroup() != null)
            entity.setQuestionGroup(resolveGroup(request.getQuestionGroup()));
        if (request.getRequired() != null)      entity.setRequired(request.getRequired());

        return toResponse(repository.save(entity));
    }

    @Override
    public void updateStatus(Long id, Boolean active) {
        TerminationQuizQuestion entity = findOrThrow(id);
        entity.setActive(active);
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TerminationQuizQuestionResponse> getActiveQuestions() {
        return repository.findByActiveTrueOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private String resolveEmployeeName(Long employeeId) {
        if (employeeId == null) return null;
        return employeeRepository.findById(employeeId)
                .map(e -> e.getFirstName() + " " + e.getPaternalLastName())
                .orElse(null);
    }

    private QuizQuestionGroup resolveGroup(String name) {
        if (name == null || name.isBlank()) return null;
        return questionGroupRepository.findByName(name.trim())
                .orElseGet(() -> questionGroupRepository.save(
                        QuizQuestionGroup.builder().name(name.trim()).active(true).build()));
    }

    private TerminationQuizQuestion findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada con id: " + id));
    }

    private TerminationQuizQuestionResponse toResponse(TerminationQuizQuestion e) {
        return TerminationQuizQuestionResponse.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .employeeName(resolveEmployeeName(e.getEmployeeId()))
                .question(e.getQuestion())
                .questionGroupId(e.getQuestionGroup() != null ? e.getQuestionGroup().getId() : null)
                .questionGroupName(e.getQuestionGroup() != null ? e.getQuestionGroup().getName() : null)
                .required(e.getRequired())
                .active(e.getActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
