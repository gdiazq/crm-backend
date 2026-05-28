package com.crm.mcsv_rrhh.repository.company;

import com.crm.mcsv_rrhh.entity.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);
}
