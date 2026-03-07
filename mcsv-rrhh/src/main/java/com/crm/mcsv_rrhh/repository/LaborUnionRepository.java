package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.LaborUnion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LaborUnionRepository extends JpaRepository<LaborUnion, Long> {
    Optional<LaborUnion> findByName(String name);
}
