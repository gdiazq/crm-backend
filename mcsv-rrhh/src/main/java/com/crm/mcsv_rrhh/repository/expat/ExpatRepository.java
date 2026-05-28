package com.crm.mcsv_rrhh.repository.expat;

import com.crm.mcsv_rrhh.entity.expat.Expat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExpatRepository extends JpaRepository<Expat, Long> {
    Optional<Expat> findByName(String name);
}
