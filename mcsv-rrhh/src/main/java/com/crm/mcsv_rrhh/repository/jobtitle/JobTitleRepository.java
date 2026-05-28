package com.crm.mcsv_rrhh.repository.jobtitle;

import com.crm.mcsv_rrhh.entity.jobtitle.JobTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JobTitleRepository extends JpaRepository<JobTitle, Long> {
    Optional<JobTitle> findByName(String name);
}
