package com.crm.mcsv_rrhh.service.emergencycontactrelationship.impl;

import com.crm.mcsv_rrhh.dto.emergencycontactrelationship.EmergencyContactRelationshipResponse;
import com.crm.mcsv_rrhh.entity.emergencycontactrelationship.EmergencyContactRelationship;
import com.crm.mcsv_rrhh.repository.emergencycontactrelationship.EmergencyContactRelationshipRepository;
import com.crm.mcsv_rrhh.service.emergencycontactrelationship.EmergencyContactRelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyContactRelationshipServiceImpl implements EmergencyContactRelationshipService {

    private final EmergencyContactRelationshipRepository repository;

    @Override
    public List<EmergencyContactRelationshipResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> EmergencyContactRelationshipResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
