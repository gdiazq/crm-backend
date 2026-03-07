package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransportTypeRepository extends JpaRepository<TransportType, Long> {
    Optional<TransportType> findByName(String name);
}
