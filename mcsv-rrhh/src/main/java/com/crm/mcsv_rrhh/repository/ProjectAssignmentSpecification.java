package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.ProjectAssignment;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ProjectAssignmentSpecification {

    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    private ProjectAssignmentSpecification() {
    }

    public static Specification<ProjectAssignment> withFilters(String search,
                                                               Long employeeId,
                                                               Integer costCenter,
                                                               Boolean active,
                                                               LocalDate dateFrom,
                                                               LocalDate dateTo,
                                                               LocalDate createdFrom,
                                                               LocalDate createdTo,
                                                               LocalDate updatedFrom,
                                                               LocalDate updatedTo,
                                                               String sortBy,
                                                               String sortDir) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<ProjectAssignment, Employee> empJoin = null;
            boolean needEmployeeJoin = (search != null && !search.isBlank())
                    || (sortBy != null && EMPLOYEE_SORT_FIELDS.contains(sortBy));

            if (needEmployeeJoin) {
                empJoin = root.join("employee", JoinType.LEFT);
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("roleOnProject")), pattern),
                        cb.like(cb.lower(empJoin.get("identification")), pattern),
                        cb.like(cb.lower(empJoin.get("firstName")), pattern),
                        cb.like(cb.lower(empJoin.get("paternalLastName")), pattern),
                        cb.like(cb.lower(empJoin.get("maternalLastName")), pattern)
                ));
            }

            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employeeId"), employeeId));
            }

            if (costCenter != null) {
                predicates.add(cb.equal(root.get("costCenter"), costCenter));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (dateFrom != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("endDate")),
                        cb.greaterThanOrEqualTo(root.get("endDate"), dateFrom)
                ));
            }

            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), dateTo));
            }

            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom.atStartOfDay()));
            }

            if (createdTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), LocalDateTime.of(createdTo, LocalTime.MAX)));
            }

            if (updatedFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom.atStartOfDay()));
            }

            if (updatedTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), LocalDateTime.of(updatedTo, LocalTime.MAX)));
            }

            if (empJoin != null && sortBy != null && EMPLOYEE_SORT_FIELDS.contains(sortBy)
                    && query != null && query.getResultType() != Long.class) {
                query.orderBy("asc".equalsIgnoreCase(sortDir)
                        ? cb.asc(empJoin.get(sortBy))
                        : cb.desc(empJoin.get(sortBy)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
