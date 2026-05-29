package com.crm.mcsv_rrhh.repository.zone;

import com.crm.mcsv_rrhh.entity.zone.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Optional<Zone> findByName(String name);
}
