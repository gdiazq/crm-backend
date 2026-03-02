package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.HRRequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HRRequestTypeRepository extends JpaRepository<HRRequestType, Long> {
    Optional<HRRequestType> findByName(String name);
}
