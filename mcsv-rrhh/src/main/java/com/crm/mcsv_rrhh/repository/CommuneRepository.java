package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Commune;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommuneRepository extends JpaRepository<Commune, Long> {
    Optional<Commune> findByNameAndRegionId(String name, Long regionId);
    List<Commune> findByRegionId(Long regionId);
}
