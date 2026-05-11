package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.entity.Overtime;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OvertimeSpecification {

    private OvertimeSpecification() {}

    public static Specification<Overtime> withFilters(String search,
                                                      Long employeeId, Integer costCenter, Long statusId,
                                                      LocalDate dateFrom, LocalDate dateTo,
                                                      Long overtimeTypeId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                Join<Overtime, Employee> empJoin = root.join("employee", JoinType.LEFT);
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
            if (costCenter != null) {
                predicates.add(cb.equal(root.get("costCenter"), costCenter));
            }
            if (overtimeTypeId != null) {
                predicates.add(cb.equal(root.get("overtimeTypeId"), overtimeTypeId));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), dateTo));
            }

            if (statusId != null) {
                Subquery<LocalDateTime> maxDate = query.subquery(LocalDateTime.class);
                Root<HRRequest> hrForMax = maxDate.from(HRRequest.class);
                maxDate.select(cb.greatest(hrForMax.<LocalDateTime>get("createdAt")))
                        .where(cb.equal(hrForMax.get("overtimeId"), root.get("id")));

                Subquery<Long> hrExists = query.subquery(Long.class);
                Root<HRRequest> hrRoot = hrExists.from(HRRequest.class);
                hrExists.select(hrRoot.get("id"))
                        .where(
                                cb.equal(hrRoot.get("overtimeId"), root.get("id")),
                                cb.equal(hrRoot.get("statusId"), statusId),
                                cb.equal(hrRoot.get("createdAt"), maxDate)
                        );
                predicates.add(cb.exists(hrExists));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
