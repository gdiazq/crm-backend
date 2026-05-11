package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.OvertimeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OvertimeTypeRepository extends JpaRepository<OvertimeType, Long> {
    Optional<OvertimeType> findByName(String name);
    List<OvertimeType> findByActiveTrue();
}
