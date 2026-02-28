package com.crm.mcsv_rrhh.util;

import com.crm.mcsv_rrhh.entity.DriverLicense;
import com.crm.mcsv_rrhh.entity.EducationLevel;
import com.crm.mcsv_rrhh.entity.Gender;
import com.crm.mcsv_rrhh.entity.IdentificationType;
import com.crm.mcsv_rrhh.entity.MaritalStatus;
import com.crm.mcsv_rrhh.repository.DriverLicenseRepository;
import com.crm.mcsv_rrhh.repository.EducationLevelRepository;
import com.crm.mcsv_rrhh.repository.GenderRepository;
import com.crm.mcsv_rrhh.repository.IdentificationTypeRepository;
import com.crm.mcsv_rrhh.repository.MaritalStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final IdentificationTypeRepository identificationTypeRepository;
    private final GenderRepository genderRepository;
    private final MaritalStatusRepository maritalStatusRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final DriverLicenseRepository driverLicenseRepository;

    @Override
    public void run(String... args) {
        initializeIdentificationTypes();
        initializeGenders();
        initializeMaritalStatuses();
        initializeEducationLevels();
        initializeDriverLicenses();
    }

    private void initializeIdentificationTypes() {
        createIdentificationTypeIfNotExists("RUT/RUN");
        createIdentificationTypeIfNotExists("DNI");
        createIdentificationTypeIfNotExists("CPF");
        createIdentificationTypeIfNotExists("SSN");
        createIdentificationTypeIfNotExists("Passport");
        log.info("Identification types initialized.");
    }

    private void createIdentificationTypeIfNotExists(String name) {
        if (identificationTypeRepository.findByName(name).isEmpty()) {
            identificationTypeRepository.save(IdentificationType.builder().name(name).status(true).build());
        }
    }

    private void initializeGenders() {
        createGenderIfNotExists("Masculino");
        createGenderIfNotExists("Femenino");
        createGenderIfNotExists("No binario");
        createGenderIfNotExists("Otro");
        createGenderIfNotExists("Prefiero no decirlo");
        log.info("Genders initialized.");
    }

    private void createGenderIfNotExists(String name) {
        if (genderRepository.findByName(name).isEmpty()) {
            genderRepository.save(Gender.builder().name(name).build());
        }
    }

    private void initializeMaritalStatuses() {
        createMaritalStatusIfNotExists("Soltero/a");
        createMaritalStatusIfNotExists("Casado/a");
        createMaritalStatusIfNotExists("Divorciado/a");
        createMaritalStatusIfNotExists("Viudo/a");
        createMaritalStatusIfNotExists("Conviviente civil");
        createMaritalStatusIfNotExists("Separado/a");
        log.info("Marital statuses initialized.");
    }

    private void createMaritalStatusIfNotExists(String name) {
        if (maritalStatusRepository.findByName(name).isEmpty()) {
            maritalStatusRepository.save(MaritalStatus.builder().name(name).build());
        }
    }

    private void initializeEducationLevels() {
        createEducationLevelIfNotExists("Educación básica");
        createEducationLevelIfNotExists("Educación media");
        createEducationLevelIfNotExists("Técnico nivel medio");
        createEducationLevelIfNotExists("Técnico nivel superior");
        createEducationLevelIfNotExists("Universitaria");
        createEducationLevelIfNotExists("Postgrado");
        log.info("Education levels initialized.");
    }

    private void createEducationLevelIfNotExists(String name) {
        if (educationLevelRepository.findByName(name).isEmpty()) {
            educationLevelRepository.save(EducationLevel.builder().name(name).build());
        }
    }

    private void initializeDriverLicenses() {
        createDriverLicenseIfNotExists("No posee");
        createDriverLicenseIfNotExists("Clase A1");
        createDriverLicenseIfNotExists("Clase A2");
        createDriverLicenseIfNotExists("Clase A3");
        createDriverLicenseIfNotExists("Clase B");
        createDriverLicenseIfNotExists("Clase C");
        createDriverLicenseIfNotExists("Clase D");
        createDriverLicenseIfNotExists("Clase E");
        createDriverLicenseIfNotExists("Clase F");
        log.info("Driver licenses initialized.");
    }

    private void createDriverLicenseIfNotExists(String name) {
        if (driverLicenseRepository.findByName(name).isEmpty()) {
            driverLicenseRepository.save(DriverLicense.builder().name(name).build());
        }
    }
}
