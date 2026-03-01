package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeStatusRepository extends JpaRepository<EmployeeStatus, Long> {
    Optional<EmployeeStatus> findByName(String name);
}
