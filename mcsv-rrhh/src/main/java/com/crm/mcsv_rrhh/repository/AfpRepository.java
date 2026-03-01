package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Afp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AfpRepository extends JpaRepository<Afp, Long> {
    Optional<Afp> findByName(String name);
}
