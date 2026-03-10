package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.HRRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HRRequestSpecification {

    private HRRequestSpecification() {}

    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    public static Specification<HRRequest> withFilters(Long idModule, Long statusId,
                                                        LocalDate createdFrom, LocalDate createdTo,
                                                        LocalDate approvalFrom, LocalDate approvalTo,
                                                        String sortBy, String sortDir) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (idModule != null) {
                predicates.add(cb.equal(root.get("idModule"), idModule));
            }

            if (statusId != null) {
                predicates.add(cb.equal(root.get("statusId"), statusId));
            }

            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom.atStartOfDay()));
            }

            if (createdTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo.atTime(23, 59, 59)));
            }

            if (approvalFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("approvalDate"), approvalFrom.atStartOfDay()));
            }

            if (approvalTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("approvalDate"), approvalTo.atTime(23, 59, 59)));
            }

            // Sort por campo de empleado via JOIN
            if (sortBy != null && EMPLOYEE_SORT_FIELDS.contains(sortBy)
                    && !Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                Join<Object, Object> empJoin = root.join("employee", JoinType.LEFT);
                query.orderBy(sortDir != null && sortDir.equalsIgnoreCase("asc")
                        ? cb.asc(empJoin.get(sortBy))
                        : cb.desc(empJoin.get(sortBy)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
