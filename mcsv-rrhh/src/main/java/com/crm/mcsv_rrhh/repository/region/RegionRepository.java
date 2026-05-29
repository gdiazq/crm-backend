package com.crm.mcsv_rrhh.repository.region;

import com.crm.mcsv_rrhh.entity.region.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByName(String name);
}
