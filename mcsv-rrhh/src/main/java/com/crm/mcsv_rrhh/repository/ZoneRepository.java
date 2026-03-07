package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Optional<Zone> findByName(String name);
}
