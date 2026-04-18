package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.entity.Transfer;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransferSpecification {

    private TransferSpecification() {}

    public static Specification<Transfer> withFilters(String search, Long statusId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.join("employee").get("firstName")), pattern),
                        cb.like(cb.lower(root.join("employee").get("paternalLastName")), pattern),
                        cb.like(cb.lower(root.join("employee").get("identification")), pattern)
                ));
            }

            if (statusId != null) {
                Subquery<LocalDateTime> maxDate = query.subquery(LocalDateTime.class);
                Root<HRRequest> hrForMax = maxDate.from(HRRequest.class);
                maxDate.select(cb.greatest(hrForMax.<LocalDateTime>get("createdAt")))
                       .where(cb.equal(hrForMax.get("transferId"), root.get("id")));

                Subquery<Long> hrExists = query.subquery(Long.class);
                Root<HRRequest> hrRoot = hrExists.from(HRRequest.class);
                hrExists.select(hrRoot.get("id"))
                        .where(
                            cb.equal(hrRoot.get("transferId"), root.get("id")),
                            cb.equal(hrRoot.get("statusId"), statusId),
                            cb.equal(hrRoot.get("createdAt"), maxDate)
                        );
                predicates.add(cb.exists(hrExists));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
