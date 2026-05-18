package com.crm.mcsv_recruitment.repository;

import com.crm.mcsv_recruitment.entity.JobOpening;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobOpeningSpecification {

    private JobOpeningSpecification() {}

    public static Specification<JobOpening> withFilters(String search,
                                                       Long statusId,
                                                       Integer costCenter,
                                                       Long supervisorUserId,
                                                       LocalDate closeDateFrom,
                                                       LocalDate closeDateTo,
                                                       LocalDateTime createdFrom,
                                                       LocalDateTime createdTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if (statusId != null) {
                predicates.add(cb.equal(root.get("statusId"), statusId));
            }
            if (costCenter != null) {
                predicates.add(cb.equal(root.get("costCenter"), costCenter));
            }
            if (supervisorUserId != null) {
                predicates.add(cb.equal(root.get("supervisorUserId"), supervisorUserId));
            }
            if (closeDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("closeDate"), closeDateFrom));
            }
            if (closeDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("closeDate"), closeDateTo));
            }
            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }
            if (createdTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
