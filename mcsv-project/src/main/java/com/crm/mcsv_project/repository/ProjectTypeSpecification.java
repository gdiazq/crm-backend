package com.crm.mcsv_project.repository;

import com.crm.mcsv_project.entity.ProjectType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectTypeSpecification {

    private ProjectTypeSpecification() {}

    public static Specification<ProjectType> withFilters(String search, Boolean active,
                                                          LocalDateTime createdFrom, LocalDateTime createdTo,
                                                          LocalDateTime updatedFrom, LocalDateTime updatedTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }
            if (createdTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }
            if (updatedFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom));
            }
            if (updatedTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
