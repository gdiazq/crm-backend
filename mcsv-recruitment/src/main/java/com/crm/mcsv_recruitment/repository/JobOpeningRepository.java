package com.crm.mcsv_recruitment.repository;

import com.crm.mcsv_recruitment.entity.JobOpening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobOpeningRepository
        extends JpaRepository<JobOpening, Long>, JpaSpecificationExecutor<JobOpening> {

    long countByStatusId(Long statusId);
}
