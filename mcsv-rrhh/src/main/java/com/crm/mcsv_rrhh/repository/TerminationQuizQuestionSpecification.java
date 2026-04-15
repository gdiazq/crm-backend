package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.TerminationQuizQuestion;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TerminationQuizQuestionSpecification {

    private TerminationQuizQuestionSpecification() {}

    public static Specification<TerminationQuizQuestion> withFilters(String search, Boolean active,
                                                                      Long questionGroupId, Long employeeId,
                                                                      LocalDateTime createdFrom, LocalDateTime createdTo,
                                                                      LocalDateTime updatedFrom, LocalDateTime updatedTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("question")), pattern));
            }

            if (active != null)        predicates.add(cb.equal(root.get("active"), active));
            if (questionGroupId != null)
                                       predicates.add(cb.equal(root.get("questionGroup").get("id"), questionGroupId));
            if (employeeId != null)   predicates.add(cb.equal(root.get("employeeId"), employeeId));
            if (createdFrom != null)  predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            if (createdTo != null)    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            if (updatedFrom != null)  predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom));
            if (updatedTo != null)    predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
