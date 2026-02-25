package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Employee;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {

    private EmployeeSpecification() {}

    public static Specification<Employee> withFilters(String search, Boolean active, Long statusId, Long companyId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("paternalLastName")), pattern),
                        cb.like(cb.lower(root.get("maternalLastName")), pattern),
                        cb.like(cb.lower(root.get("identification")), pattern),
                        cb.like(cb.lower(root.get("corporateEmail")), pattern)
                ));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (statusId != null) {
                predicates.add(cb.equal(root.get("statusId"), statusId));
            }

            if (companyId != null) {
                predicates.add(cb.equal(root.get("companyId"), companyId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
