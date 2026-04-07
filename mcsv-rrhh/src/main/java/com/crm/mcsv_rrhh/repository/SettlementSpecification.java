package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.entity.Settlement;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SettlementSpecification {

    private SettlementSpecification() {}

    public static Specification<Settlement> withFilters(String search,
                                                         Long statusId,
                                                         Long legalTerminationCauseId,
                                                         Long qualityOfWorkId,
                                                         Long safetyComplianceId,
                                                         Long noReHiredCauseId,
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

            if (statusId != null) {
                // Subquery: max createdAt de hr_requests para este finiquito
                Subquery<LocalDateTime> maxDate = query.subquery(LocalDateTime.class);
                Root<HRRequest> hrForMax = maxDate.from(HRRequest.class);
                maxDate.select(cb.greatest(hrForMax.<LocalDateTime>get("createdAt")))
                       .where(cb.equal(hrForMax.get("settlementId"), root.get("id")));

                // Existe un hr_request con ese max date y el statusId pedido
                Subquery<Long> hrExists = query.subquery(Long.class);
                Root<HRRequest> hrRoot = hrExists.from(HRRequest.class);
                hrExists.select(hrRoot.get("id"))
                        .where(
                            cb.equal(hrRoot.get("settlementId"), root.get("id")),
                            cb.equal(hrRoot.get("statusId"), statusId),
                            cb.equal(hrRoot.get("createdAt"), maxDate)
                        );
                predicates.add(cb.exists(hrExists));
            }

            if (legalTerminationCauseId != null)
                predicates.add(cb.equal(root.get("legalTerminationCause").get("id"), legalTerminationCauseId));
            if (qualityOfWorkId != null)
                predicates.add(cb.equal(root.get("qualityOfWork").get("id"), qualityOfWorkId));
            if (safetyComplianceId != null)
                predicates.add(cb.equal(root.get("safetyCompliance").get("id"), safetyComplianceId));
            if (noReHiredCauseId != null)
                predicates.add(cb.equal(root.get("noReHiredCause").get("id"), noReHiredCauseId));
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
