package com.crm.mcsv_rrhh.repository.employee;

import com.crm.mcsv_rrhh.entity.employee.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EmployeeStatusRepository extends JpaRepository<EmployeeStatus, Long> {
    Optional<EmployeeStatus> findByName(String name);
    List<EmployeeStatus> findAllByNameIn(Collection<String> names);
}
