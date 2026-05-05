package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.AttendanceStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class AttendanceStatusSpecification {

    private AttendanceStatusSpecification() {}

    public static Specification<AttendanceStatus> withFilters(String search,
                                                              Boolean active,
                                                              LocalDate createdFrom,
                                                              LocalDate createdTo,
                                                              LocalDate updatedFrom,
                                                              LocalDate updatedTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("code")), pattern)
                ));
            }
            if (active != null) predicates.add(cb.equal(root.get("active"), active));
            if (createdFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom.atStartOfDay()));
            if (createdTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo.atTime(23, 59, 59)));
            if (updatedFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom.atStartOfDay()));
            if (updatedTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo.atTime(23, 59, 59)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
