package com.crm.mcsv_rrhh.repository.hrrequest;

import com.crm.mcsv_rrhh.entity.hrrequest.HRRequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HRRequestTypeRepository extends JpaRepository<HRRequestType, Long> {

    Optional<HRRequestType> findByName(String name);

}
