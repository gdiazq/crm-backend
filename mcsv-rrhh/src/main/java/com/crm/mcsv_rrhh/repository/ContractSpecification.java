package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Contract;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ContractSpecification {

    private ContractSpecification() {}

    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    public static Specification<Contract> withFilters(String search,
                                                       Long employeeId, Long statusId,
                                                       Long contractStatusId, Long contractTypeId,
                                                       LocalDate createdFrom, LocalDate createdTo,
                                                       LocalDate startDateFrom, LocalDate startDateTo,
                                                       LocalDate endDateFrom, LocalDate endDateTo,
                                                       LocalDate updatedFrom, LocalDate updatedTo,
                                                       String sortBy, String sortDir) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            boolean needsEmpJoin = (search != null && !search.isBlank())
                    || (sortBy != null && EMPLOYEE_SORT_FIELDS.contains(sortBy)
                        && !Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType()));

            Join<Object, Object> empJoin = needsEmpJoin ? root.join("employee", JoinType.LEFT) : null;

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(empJoin.get("firstName")), pattern),
                        cb.like(cb.lower(empJoin.get("paternalLastName")), pattern),
                        cb.like(cb.lower(empJoin.get("maternalLastName")), pattern),
                        cb.like(cb.lower(empJoin.get("identification")), pattern)
                ));
            }

            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employeeId"), employeeId));
            }
            if (statusId != null) {
                predicates.add(cb.equal(root.get("statusId"), statusId));
            }
            if (contractStatusId != null) {
                predicates.add(cb.equal(root.get("contractStatusId"), contractStatusId));
            }
            if (contractTypeId != null) {
                predicates.add(cb.equal(root.get("contractTypeId"), contractTypeId));
            }
            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom.atStartOfDay()));
            }
            if (createdTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo.atTime(23, 59, 59)));
            }
            if (startDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
            }
            if (startDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), startDateTo));
            }
            if (endDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), endDateFrom));
            }
            if (endDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), endDateTo));
            }
            if (updatedFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom.atStartOfDay()));
            }
            if (updatedTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo.atTime(23, 59, 59)));
            }

            if (empJoin != null && sortBy != null && EMPLOYEE_SORT_FIELDS.contains(sortBy)
                    && !Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                query.orderBy(sortDir != null && sortDir.equalsIgnoreCase("asc")
                        ? cb.asc(empJoin.get(sortBy))
                        : cb.desc(empJoin.get(sortBy)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
