package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.JobTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JobTitleRepository extends JpaRepository<JobTitle, Long> {
    Optional<JobTitle> findByName(String name);
}
