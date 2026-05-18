package com.crm.mcsv_recruitment.util;

import com.crm.mcsv_recruitment.entity.JobOpeningStatus;
import com.crm.mcsv_recruitment.enums.JobOpeningStatusName;
import com.crm.mcsv_recruitment.repository.JobOpeningStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final JobOpeningStatusRepository jobOpeningStatusRepository;

    @Override
    public void run(String... args) {
        initializeJobOpeningStatuses();
    }

    private void initializeJobOpeningStatuses() {
        if (jobOpeningStatusRepository.count() > 0) return;
        for (JobOpeningStatusName s : JobOpeningStatusName.values()) {
            if (jobOpeningStatusRepository.findByName(s.getDisplayName()).isEmpty()) {
                jobOpeningStatusRepository.save(
                        JobOpeningStatus.builder().name(s.getDisplayName()).active(true).build()
                );
            }
        }
        log.info("Job opening statuses initialized.");
    }
}
