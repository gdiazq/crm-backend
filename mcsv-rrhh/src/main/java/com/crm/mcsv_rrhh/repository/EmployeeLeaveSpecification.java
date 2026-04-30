package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.EmployeeLeave;
import com.crm.mcsv_rrhh.entity.HRRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EmployeeLeaveSpecification {

    private EmployeeLeaveSpecification() {}

    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    public static Specification<EmployeeLeave> withFilters(String search, Long statusId,
                                                           Long leaveTypeId, Long employeeId, Long contractId,
                                                           LocalDate startFrom, LocalDate startTo,
                                                           LocalDate createdFrom, LocalDate createdTo,
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
                        cb.like(cb.lower(empJoin.get("identification")), pattern)
                ));
            }

            if (statusId != null) {
                Subquery<LocalDateTime> maxDate = query.subquery(LocalDateTime.class);
                Root<HRRequest> hrForMax = maxDate.from(HRRequest.class);
                maxDate.select(cb.greatest(hrForMax.<LocalDateTime>get("createdAt")))
                        .where(cb.equal(hrForMax.get("leaveId"), root.get("id")));

                Subquery<Long> hrExists = query.subquery(Long.class);
                Root<HRRequest> hrRoot = hrExists.from(HRRequest.class);
                hrExists.select(hrRoot.get("id"))
                        .where(
                                cb.equal(hrRoot.get("leaveId"), root.get("id")),
                                cb.equal(hrRoot.get("statusId"), statusId),
                                cb.equal(hrRoot.get("createdAt"), maxDate)
                        );
                predicates.add(cb.exists(hrExists));
            }

            if (leaveTypeId != null) {
                predicates.add(cb.equal(root.get("leaveTypeId"), leaveTypeId));
            }
            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employeeId"), employeeId));
            }
            if (contractId != null) {
                predicates.add(cb.equal(root.get("contractId"), contractId));
            }
            if (startFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startFrom));
            }
            if (startTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), startTo));
            }
            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom.atStartOfDay()));
            }
            if (createdTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo.atTime(23, 59, 59)));
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

    public static Specification<EmployeeLeave> overlapsForEmployee(Long employeeId, LocalDate startDate, LocalDate endDate,
                                                                   Long excludeLeaveId, Collection<Long> activeStatusIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("employeeId"), employeeId));
            predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), endDate));
            predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), startDate));

            if (excludeLeaveId != null) {
                predicates.add(cb.notEqual(root.get("id"), excludeLeaveId));
            }

            if (activeStatusIds == null || activeStatusIds.isEmpty()) {
                predicates.add(cb.disjunction());
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            Subquery<LocalDateTime> maxDate = query.subquery(LocalDateTime.class);
            Root<HRRequest> hrForMax = maxDate.from(HRRequest.class);
            maxDate.select(cb.greatest(hrForMax.<LocalDateTime>get("createdAt")))
                    .where(cb.equal(hrForMax.get("leaveId"), root.get("id")));

            Subquery<Long> hrExists = query.subquery(Long.class);
            Root<HRRequest> hrRoot = hrExists.from(HRRequest.class);
            hrExists.select(hrRoot.get("id"))
                    .where(
                            cb.equal(hrRoot.get("leaveId"), root.get("id")),
                            hrRoot.get("statusId").in(activeStatusIds),
                            cb.equal(hrRoot.get("createdAt"), maxDate)
                    );
            predicates.add(cb.exists(hrExists));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
