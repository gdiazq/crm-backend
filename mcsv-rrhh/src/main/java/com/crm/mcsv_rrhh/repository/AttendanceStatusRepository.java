package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceStatusRepository extends JpaRepository<AttendanceStatus, Long> {
    Optional<AttendanceStatus> findByCode(String code);
    boolean existsByName(String name);
    boolean existsByCode(String code);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByCodeAndIdNot(String code, Long id);
}
