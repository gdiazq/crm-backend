package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Employee;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {

    private EmployeeSpecification() {}

    public static Specification<Employee> withFilters(String search, Boolean active, Long excludeStatusId, Long statusId,
                                                      LocalDate createdFrom, LocalDate createdTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("paternalLastName")), pattern),
                        cb.like(cb.lower(root.get("maternalLastName")), pattern),
                        cb.like(cb.lower(root.get("corporateEmail")), pattern)
                ));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (statusId != null) {
                predicates.add(cb.equal(root.get("statusId"), statusId));
            } else if (excludeStatusId != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("statusId")),
                        cb.notEqual(root.get("statusId"), excludeStatusId)
                ));
            }

            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom.atStartOfDay()));
            }

            if (createdTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo.atTime(23, 59, 59)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
