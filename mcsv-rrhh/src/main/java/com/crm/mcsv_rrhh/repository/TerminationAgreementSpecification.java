package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.TerminationAgreement;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TerminationAgreementSpecification {

    private TerminationAgreementSpecification() {}

    public static Specification<TerminationAgreement> withFilters(String search,
                                                                   String status,
                                                                   Long employeeId,
                                                                   Long legalTerminationCauseId,
                                                                   Boolean rehireEligible,
                                                                   LocalDate endDateFrom,
                                                                   LocalDate endDateTo,
                                                                   LocalDateTime createdFrom,
                                                                   LocalDateTime createdTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.join("employee").get("firstName")), pattern),
                        cb.like(cb.lower(root.join("employee").get("paternalLastName")), pattern),
                        cb.like(cb.lower(root.join("employee").get("identification")), pattern),
                        cb.like(cb.lower(root.get("observations")), pattern)
                ));
            }

            if (status != null && !status.isBlank())
                predicates.add(cb.equal(root.get("status"), status));

            if (employeeId != null)
                predicates.add(cb.equal(root.get("employeeId"), employeeId));

            if (legalTerminationCauseId != null)
                predicates.add(cb.equal(root.get("legalTerminationCause").get("id"), legalTerminationCauseId));

            if (rehireEligible != null)
                predicates.add(cb.equal(root.get("rehireEligible"), rehireEligible));

            if (endDateFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), endDateFrom));

            if (endDateTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), endDateTo));

            if (createdFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));

            if (createdTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
