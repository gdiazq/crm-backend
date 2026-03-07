package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> findByName(String name);
}
