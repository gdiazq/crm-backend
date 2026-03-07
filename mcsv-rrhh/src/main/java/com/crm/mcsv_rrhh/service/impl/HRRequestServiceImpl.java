package com.crm.mcsv_rrhh.service.impl;

import com.crm.mcsv_rrhh.client.UserClient;
import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.mcsv_rrhh.dto.HRRequestDetailResponse;
import com.crm.mcsv_rrhh.dto.HRRequestResponse;
import com.crm.mcsv_rrhh.dto.UpdateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.UserDTO;
import com.crm.mcsv_rrhh.dto.RejectHRRequestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.entity.HRRequestType;
import com.crm.mcsv_rrhh.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.HRRequestRepository;
import com.crm.mcsv_rrhh.repository.HRRequestSpecification;
import com.crm.mcsv_rrhh.repository.HRRequestTypeRepository;
import com.crm.mcsv_rrhh.service.HRRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HRRequestServiceImpl implements HRRequestService {

    private final HRRequestRepository hrRequestRepository;
    private final HRRequestTypeRepository hrRequestTypeRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final EmployeeRepository employeeRepository;
    private final UserClient userClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public HRRequest createForEmployee(Long employeeId, String requestTypeName, String action, String proposedData) {
        HRRequestType type = hrRequestTypeRepository.findByName(requestTypeName)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de solicitud no encontrado: " + requestTypeName));

        String initialStatusName = Boolean.TRUE.equals(type.getRequireApproval())
                ? "Pendiente de revisión"
                : "Pendiente de aprobación";

        Long statusId = employeeStatusRepository.findByName(initialStatusName)
                .map(s -> s.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: " + initialStatusName));

        HRRequest request = HRRequest.builder()
                .requestTypeId(type.getId())
                .statusId(statusId)
                .requireApproval(type.getRequireApproval())
                .idModule(employeeId)
                .action(action)
                .proposedData(proposedData)
                .build();

        return hrRequestRepository.save(request);
    }

    @Override
    @Transactional
    public HRRequest createForContract(Long contractId, Long employeeId, String action, String proposedData) {
        HRRequestType type = hrRequestTypeRepository.findByName("Contrato")
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de solicitud no encontrado: Contrato"));

        String initialStatusName = Boolean.TRUE.equals(type.getRequireApproval())
                ? "Pendiente de revisión"
                : "Pendiente de aprobación";

        Long statusId = employeeStatusRepository.findByName(initialStatusName)
                .map(s -> s.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: " + initialStatusName));

        HRRequest request = HRRequest.builder()
                .requestTypeId(type.getId())
                .statusId(statusId)
                .requireApproval(type.getRequireApproval())
                .idModule(employeeId)
                .contractId(contractId)
                .action(action)
                .proposedData(proposedData)
                .build();

        return hrRequestRepository.save(request);
    }

    @Override
    public Page<HRRequestResponse> list(Long idModule, Long statusId,
                                         LocalDate createdFrom, LocalDate createdTo,
                                         LocalDate approvalFrom, LocalDate approvalTo,
                                         Pageable pageable) {
        return hrRequestRepository.findAll(
                HRRequestSpecification.withFilters(idModule, statusId, createdFrom, createdTo, approvalFrom, approvalTo),
                pageable
        ).map(this::toResponse);
    }

    @Override
    public HRRequestDetailResponse getById(Long id) {
        HRRequest hr = findOrThrow(id);

        CatalogItem requestType = hrRequestTypeRepository.findById(hr.getRequestTypeId())
                .map(t -> new CatalogItem(t.getId(), t.getName())).orElse(null);
        CatalogItem status = employeeStatusRepository.findById(hr.getStatusId())
                .map(s -> new CatalogItem(s.getId(), s.getName())).orElse(null);

        HRRequestDetailResponse.HRRequestDetailResponseBuilder builder = HRRequestDetailResponse.builder()
                .id(hr.getId())
                .idModule(hr.getIdModule())
                .requestType(requestType)
                .status(status)
                .requireApproval(hr.getRequireApproval())
                .action(hr.getAction())
                .approvalDate(hr.getApprovalDate())
                .hhrrApprovalDate(hr.getHhrrApprovalDate())
                .rejectionDetail(hr.getRejectionDetail())
                .createdAt(hr.getCreatedAt())
                .updatedAt(hr.getUpdatedAt());

        employeeRepository.findById(hr.getIdModule()).ifPresent(e -> builder
                .identification(e.getIdentification())
                .firstName(e.getFirstName())
                .paternalLastName(e.getPaternalLastName())
                .maternalLastName(e.getMaternalLastName()));

        if (hr.getApproverId() != null) {
            builder.approver(new CatalogItem(hr.getApproverId(), fetchFullName(hr.getApproverId())));
        }
        if (hr.getHhrrApproverId() != null) {
            builder.hhrrApprover(new CatalogItem(hr.getHhrrApproverId(), fetchFullName(hr.getHhrrApproverId())));
        }

        return builder.build();
    }

    @Override
    @Transactional
    public HRRequestResponse approve(Long id, Long approverId) {
        HRRequest hr = findOrThrow(id);
        String currentStatus = resolveStatusName(hr.getStatusId());

        if ("Pendiente de revisión".equals(currentStatus)) {
            hr.setApproverId(approverId);
            hr.setApprovalDate(LocalDateTime.now());
            hr.setStatusId(resolveStatusId("Pendiente de aprobación"));
            hrRequestRepository.save(hr);
            return toResponse(hr);

        } else if ("Pendiente de aprobación".equals(currentStatus)) {
            hr.setHhrrApproverId(approverId);
            hr.setHhrrApprovalDate(LocalDateTime.now());
            Long approvedStatusId = resolveStatusId("Aprobado");
            hr.setStatusId(approvedStatusId);
            hrRequestRepository.save(hr);

            Employee employee = employeeRepository.findById(hr.getIdModule())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id: " + hr.getIdModule()));

            if ("UPDATE".equals(hr.getAction()) && hr.getProposedData() != null) {
                try {
                    UpdateEmployeeRequest proposed = objectMapper.readValue(hr.getProposedData(), UpdateEmployeeRequest.class);
                    employee.setIdentification(proposed.getIdentification());
                    employee.setIdentificationTypeId(proposed.getIdentificationTypeId());
                    employee.setFirstName(proposed.getFirstName());
                    employee.setPaternalLastName(proposed.getPaternalLastName());
                    employee.setMaternalLastName(proposed.getMaternalLastName());
                    employee.setBirthDate(proposed.getBirthDate());
                    employee.setGenderId(proposed.getGenderId());
                    employee.setMaritalStatusId(proposed.getMaritalStatusId());
                    employee.setEducationLevelId(proposed.getEducationLevelId());
                    employee.setDriverLicenseId(proposed.getDriverLicenseId());
                    employee.setProfessionId(proposed.getProfessionId());
                    employee.setPersonalEmail(proposed.getPersonalEmail());
                    employee.setCorporateEmail(proposed.getCorporateEmail());
                    employee.setPhone(proposed.getPhone());
                    employee.setPhone2(proposed.getPhone2());
                    employee.setEmergencyContactName(proposed.getEmergencyContactName());
                    employee.setEmergencyContactRelationshipId(proposed.getEmergencyContactRelationshipId());
                    employee.setEmergencyContactPhone(proposed.getEmergencyContactPhone());
                    employee.setEmergencyContactPhone2(proposed.getEmergencyContactPhone2());
                    employee.setStreetName(proposed.getStreetName());
                    employee.setStreetNumber(proposed.getStreetNumber());
                    employee.setPostalCode(proposed.getPostalCode());
                    employee.setDepartment(proposed.getDepartment());
                    employee.setVillage(proposed.getVillage());
                    employee.setBlock(proposed.getBlock());
                    employee.setRegionId(proposed.getRegionId());
                    employee.setCityId(proposed.getCityId());
                    employee.setCommuneId(proposed.getCommuneId());
                    employee.setExpatId(proposed.getExpatId());
                    employee.setNationalityId(proposed.getNationalityId());
                    employee.setFamilyAllowanceTierId(proposed.getFamilyAllowanceTierId());
                    employee.setRetirementStatusId(proposed.getRetirementStatusId());
                    employee.setIsapreFun(proposed.getIsapreFun());
                    employee.setPensionStatusId(proposed.getPensionStatusId());
                    employee.setAfpId(proposed.getAfpId());
                    employee.setHealthInsuranceId(proposed.getHealthInsuranceId());
                    employee.setHealthInsuranceTariffId(proposed.getHealthInsuranceTariffId());
                    employee.setHealthInsuranceUF(proposed.getHealthInsuranceUF());
                    employee.setHealthInsurancePesos(proposed.getHealthInsurancePesos());
                    employee.setPaymentMethodId(proposed.getPaymentMethodId());
                    employee.setBankId(proposed.getBankId());
                    employee.setBankAccount(proposed.getBankAccount());
                    employee.setClothingSize(proposed.getClothingSize());
                    employee.setShoeSize(proposed.getShoeSize());
                    employee.setPantSize(proposed.getPantSize());
                    employee.setActive(proposed.getActive());
                    employee.setRehireEligible(proposed.getRehireEligible());
                } catch (Exception e) {
                    log.warn("No se pudo deserializar proposedData para HRRequest id {}: {}", hr.getId(), e.getMessage());
                }
            } else {
                employee.setStatusId(approvedStatusId);
            }

            employeeRepository.save(employee);
            return toResponse(hr);

        } else {
            throw new IllegalStateException("La solicitud no está en un estado aprobable");
        }
    }

    @Override
    @Transactional
    public HRRequestResponse reject(Long id, RejectHRRequestRequest req) {
        HRRequest hr = findOrThrow(id);
        Long rejectedStatusId = resolveStatusId("Rechazado");

        hr.setRejectionDetail(req.getRejectionDetail());
        hr.setStatusId(rejectedStatusId);
        hrRequestRepository.save(hr);

        if ("CREATE".equals(hr.getAction())) {
            Employee employee = employeeRepository.findById(hr.getIdModule())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id: " + hr.getIdModule()));
            employee.setStatusId(rejectedStatusId);
            employeeRepository.save(employee);
        }

        return toResponse(hr);
    }

    @Override
    public Map<String, Long> getStats(Long idModule) {
        List<Long> pendingIds = List.of(
                resolveStatusId("Pendiente de revisión"),
                resolveStatusId("Pendiente de aprobación")
        );
        Long approvedId = resolveStatusId("Aprobado");

        long total   = idModule != null ? hrRequestRepository.countByIdModule(idModule)                              : hrRequestRepository.count();
        long pending = idModule != null ? hrRequestRepository.countByIdModuleAndStatusIdIn(idModule, pendingIds)     : hrRequestRepository.countByStatusIdIn(pendingIds);
        long active  = idModule != null ? hrRequestRepository.countByIdModuleAndStatusId(idModule, approvedId)       : hrRequestRepository.countByStatusId(approvedId);
        return Map.of("total", total, "active", active, "pending", pending);
    }

    @Override
    public byte[] exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,RUT,Nombre,Apellido Paterno,Tipo Solicitud,Estado,Aprobador,Fecha Aprobación,Aprobador RRHH,Fecha Aprobación RRHH,Detalle Rechazo,Fecha Creación\n");

        hrRequestRepository.findAll().forEach(hr -> {
            String typeName = hrRequestTypeRepository.findById(hr.getRequestTypeId()).map(t -> t.getName()).orElse("");
            String statusName = resolveStatusName(hr.getStatusId());
            String approverName = hr.getApproverId() != null ? fetchFullName(hr.getApproverId()) : "";
            String hhrrApproverName = hr.getHhrrApproverId() != null ? fetchFullName(hr.getHhrrApproverId()) : "";
            String identification = "", firstName = "", paternalLastName = "";
            var empOpt = employeeRepository.findById(hr.getIdModule());
            if (empOpt.isPresent()) {
                identification = empOpt.get().getIdentification();
                firstName = empOpt.get().getFirstName();
                paternalLastName = empOpt.get().getPaternalLastName();
            }
            csv.append(hr.getId()).append(",")
               .append(escape(identification)).append(",")
               .append(escape(firstName)).append(",")
               .append(escape(paternalLastName)).append(",")
               .append(escape(typeName)).append(",")
               .append(escape(statusName)).append(",")
               .append(escape(approverName)).append(",")
               .append(formatDate(hr.getApprovalDate())).append(",")
               .append(escape(hhrrApproverName)).append(",")
               .append(formatDate(hr.getHhrrApprovalDate())).append(",")
               .append(escape(hr.getRejectionDetail())).append(",")
               .append(formatDate(hr.getCreatedAt())).append("\n");
        });

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String formatDate(java.time.LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private HRRequest findOrThrow(Long id) {
        return hrRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con id: " + id));
    }

    private String resolveStatusName(Long statusId) {
        if (statusId == null) return null;
        return employeeStatusRepository.findById(statusId)
                .map(s -> s.getName())
                .orElse(null);
    }

    private Long resolveStatusId(String name) {
        return employeeStatusRepository.findByName(name)
                .map(s -> s.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: " + name));
    }

    private HRRequestResponse toResponse(HRRequest hr) {
        String typeName = hrRequestTypeRepository.findById(hr.getRequestTypeId())
                .map(t -> t.getName())
                .orElse(null);
        String statusName = resolveStatusName(hr.getStatusId());

        HRRequestResponse.HRRequestResponseBuilder builder = HRRequestResponse.builder()
                .id(hr.getId())
                .idModule(hr.getIdModule())
                .requestTypeId(hr.getRequestTypeId())
                .requestTypeName(typeName)
                .statusId(hr.getStatusId())
                .statusName(statusName)
                .action(hr.getAction())
                .approverId(hr.getApproverId())
                .approvalDate(hr.getApprovalDate())
                .hhrrApproverId(hr.getHhrrApproverId())
                .hhrrApprovalDate(hr.getHhrrApprovalDate())
                .rejectionDetail(hr.getRejectionDetail())
                .createdAt(hr.getCreatedAt())
                .updatedAt(hr.getUpdatedAt());

        employeeRepository.findById(hr.getIdModule()).ifPresent(e -> builder
                .identification(e.getIdentification())
                .firstName(e.getFirstName())
                .paternalLastName(e.getPaternalLastName())
                .maternalLastName(e.getMaternalLastName()));

        if (hr.getApproverId() != null) {
            builder.approverFullName(fetchFullName(hr.getApproverId()));
        }
        if (hr.getHhrrApproverId() != null) {
            builder.hhrrApproverFullName(fetchFullName(hr.getHhrrApproverId()));
        }

        return builder.build();
    }

    private String fetchFullName(Long userId) {
        try {
            UserDTO user = userClient.getUserById(userId);
            if (user != null) return user.getFirstName() + " " + user.getLastName();
        } catch (Exception e) {
            log.warn("No se pudo obtener el usuario con id {}: {}", userId, e.getMessage());
        }
        return null;
    }
}
