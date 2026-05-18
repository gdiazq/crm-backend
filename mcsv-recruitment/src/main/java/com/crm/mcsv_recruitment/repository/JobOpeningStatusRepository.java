package com.crm.mcsv_recruitment.repository;

import com.crm.mcsv_recruitment.entity.JobOpeningStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobOpeningStatusRepository extends JpaRepository<JobOpeningStatus, Long> {
    Optional<JobOpeningStatus> findByName(String name);
}
