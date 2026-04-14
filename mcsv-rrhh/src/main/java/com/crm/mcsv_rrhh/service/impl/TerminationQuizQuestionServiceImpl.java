package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.TerminationQuizOptionResponse;
import com.crm.mcsv_rrhh.dto.TerminationQuizQuestionRequest;
import com.crm.mcsv_rrhh.dto.TerminationQuizQuestionResponse;
import com.crm.mcsv_rrhh.dto.UpdateTerminationQuizQuestionRequest;
import com.crm.mcsv_rrhh.entity.TerminationQuizOption;
import com.crm.mcsv_rrhh.entity.TerminationQuizQuestion;
import com.crm.common.exception.DuplicateResourceException;
import com.crm.common.exception.ResourceNotFoundException;
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

import com.crm.common.util.DateRangeUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TerminationQuizQuestionServiceImpl implements TerminationQuizQuestionService {

    private final TerminationQuizQuestionRepository repository;

    // ─── List ─────────────────────────────────────────────────────────────────

    @Override
    public PagedResponse<TerminationQuizQuestionResponse> list(String search, Boolean active, String questionGroup,
                                                                Long employeeId,
                                                                LocalDate createdFrom, LocalDate createdTo,
                                                                LocalDate updatedFrom, LocalDate updatedTo,
                                                                Pageable pageable) {
        LocalDateTime cFrom = DateRangeUtil.startOf(createdFrom);
        LocalDateTime cTo   = DateRangeUtil.endOf(createdTo);
        LocalDateTime uFrom = DateRangeUtil.startOf(updatedFrom);
        LocalDateTime uTo   = DateRangeUtil.endOf(updatedTo);

        Specification<TerminationQuizQuestion> spec =
                TerminationQuizQuestionSpecification.withFilters(search, active, questionGroup, employeeId, cFrom, cTo, uFrom, uTo);

        Page<TerminationQuizQuestion> page = repository.findAll(spec, pageable);
        long totalActive = repository.count(
                TerminationQuizQuestionSpecification.withFilters(null, true, null, null, null, null, null, null));

        return PagedResponse.of(page.map(this::toResponse), page.getTotalElements(), totalActive);
    }

    // ─── GetById ──────────────────────────────────────────────────────────────

    @Override
    public TerminationQuizQuestionResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TerminationQuizQuestionResponse create(TerminationQuizQuestionRequest request) {
        if (repository.existsByQuestion(request.getQuestion()))
            throw new DuplicateResourceException("Ya existe una pregunta con ese texto");

        TerminationQuizQuestion entity = TerminationQuizQuestion.builder()
                .employeeId(request.getEmployeeId())
                .question(request.getQuestion())
                .questionGroup(request.getQuestionGroup())
                .required(request.getRequired() != null ? request.getRequired() : true)
                .displayOrder(request.getDisplayOrder())
                .active(true)
                .build();

        buildOptions(request.getOptions(), entity);

        return toResponse(repository.save(entity));
    }

    // ─── Update ───────────────────────────────────────────────────────────────

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
        if (request.getQuestionGroup() != null) entity.setQuestionGroup(request.getQuestionGroup());
        if (request.getRequired() != null)      entity.setRequired(request.getRequired());
        if (request.getDisplayOrder() != null)  entity.setDisplayOrder(request.getDisplayOrder());

        if (request.getOptions() != null) {
            entity.getOptions().clear();
            buildOptions(request.getOptions(), entity);
        }

        return toResponse(repository.save(entity));
    }

    // ─── UpdateStatus ─────────────────────────────────────────────────────────

    @Override
    public void updateStatus(Long id, Boolean active) {
        TerminationQuizQuestion entity = findOrThrow(id);
        entity.setActive(active);
        repository.save(entity);
    }

    // ─── GetActiveQuestions ───────────────────────────────────────────────────

    @Override
    public List<TerminationQuizQuestionResponse> getActiveQuestions() {
        return repository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void buildOptions(List<com.crm.mcsv_rrhh.dto.TerminationQuizOptionRequest> optionRequests,
                               TerminationQuizQuestion entity) {
        if (optionRequests == null) return;
        for (int i = 0; i < optionRequests.size(); i++) {
            var req = optionRequests.get(i);
            entity.getOptions().add(TerminationQuizOption.builder()
                    .question(entity)
                    .label(req.getLabel())
                    .displayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : i + 1)
                    .build());
        }
    }

    private TerminationQuizQuestion findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada con id: " + id));
    }

    private TerminationQuizQuestionResponse toResponse(TerminationQuizQuestion e) {
        return TerminationQuizQuestionResponse.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .question(e.getQuestion())
                .questionGroup(e.getQuestionGroup())
                .required(e.getRequired())
                .displayOrder(e.getDisplayOrder())
                .active(e.getActive())
                .options(e.getOptions().stream()
                        .map(o -> TerminationQuizOptionResponse.builder()
                                .id(o.getId())
                                .label(o.getLabel())
                                .displayOrder(o.getDisplayOrder())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
