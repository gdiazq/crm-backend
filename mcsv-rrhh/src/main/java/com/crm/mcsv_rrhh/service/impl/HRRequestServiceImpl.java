package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.storage.service.StorageService;
import com.crm.mcsv_rrhh.client.UserClient;
import com.crm.common.dto.FileMetadataResponse;
import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.mcsv_rrhh.dto.HRRequestDetailResponse;
import com.crm.mcsv_rrhh.dto.HRRequestResponse;
import com.crm.mcsv_rrhh.dto.UpdateContractRequest;
import com.crm.mcsv_rrhh.dto.UpdateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.UpdateTransferRequest;
import com.crm.mcsv_rrhh.entity.Transfer;
import com.crm.mcsv_rrhh.repository.TransferRepository;
import com.crm.mcsv_rrhh.dto.UserDTO;
import com.crm.mcsv_rrhh.dto.RejectHRRequestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.crm.mcsv_rrhh.entity.Contract;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.entity.HRRequestType;
import com.crm.mcsv_rrhh.entity.Settlement;
import com.crm.mcsv_rrhh.repository.SettlementRepository;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.ContractRepository;
import com.crm.mcsv_rrhh.repository.ContractStatusRepository;
import com.crm.mcsv_rrhh.repository.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.HRRequestRepository;
import com.crm.mcsv_rrhh.repository.HRRequestSpecification;
import com.crm.mcsv_rrhh.repository.HRRequestTypeRepository;
import com.crm.mcsv_rrhh.repository.LegalTerminationCauseRepository;
import com.crm.mcsv_rrhh.repository.NoReHiredCauseRepository;
import com.crm.mcsv_rrhh.repository.QualityOfWorkRepository;
import com.crm.mcsv_rrhh.repository.SafetyComplianceRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HRRequestServiceImpl implements HRRequestService {

    private final HRRequestRepository hrRequestRepository;
    private final HRRequestTypeRepository hrRequestTypeRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final ContractStatusRepository contractStatusRepository;
    private final SettlementRepository settlementRepository;
    private final LegalTerminationCauseRepository legalTerminationCauseRepository;
    private final QualityOfWorkRepository qualityOfWorkRepository;
    private final SafetyComplianceRepository safetyComplianceRepository;
    private final NoReHiredCauseRepository noReHiredCauseRepository;
    private final TransferRepository transferRepository;
    private final UserClient userClient;
    private final StorageService storageService;
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
    public HRRequest createForSettlement(Long settlementId, Long employeeId, String action, String proposedData) {
        HRRequestType type = hrRequestTypeRepository.findByName("Finiquito")
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de solicitud no encontrado: Finiquito"));

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
                .settlementId(settlementId)
                .action(action)
                .proposedData(proposedData)
                .build();

        return hrRequestRepository.save(request);
    }

    @Override
    public HRRequest createForTransfer(Long transferId, Long employeeId, String action, String proposedData) {
        HRRequestType type = hrRequestTypeRepository.findByName("Traspaso")
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de solicitud no encontrado: Traspaso"));

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
                .transferId(transferId)
                .action(action)
                .proposedData(proposedData)
                .build();

        return hrRequestRepository.save(request);
    }

    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    @Override
    public Page<HRRequestResponse> list(Long idModule, Long statusId,
                                         LocalDate createdFrom, LocalDate createdTo,
                                         LocalDate approvalFrom, LocalDate approvalTo,
                                         Pageable pageable, String sortBy, String sortDir) {
        Pageable effectivePageable = (sortBy != null && EMPLOYEE_SORT_FIELDS.contains(sortBy))
                ? org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
                : pageable;

        Page<HRRequest> page = hrRequestRepository.findAll(
                HRRequestSpecification.withFilters(idModule, statusId, createdFrom, createdTo, approvalFrom, approvalTo, sortBy, sortDir),
                effectivePageable);

        if (page.isEmpty()) return page.map(hr -> toResponse(hr));

        // Batch-load catálogos, empleados y aprobadores en vez de N queries + N HTTP calls
        Set<Long> typeIds     = page.map(HRRequest::getRequestTypeId).toSet();
        Set<Long> statusIds   = page.map(HRRequest::getStatusId).toSet();
        Set<Long> employeeIds = page.map(HRRequest::getIdModule).toSet();

        Set<Long> approverIds = page.stream()
                .flatMap(hr -> {
                    java.util.stream.Stream.Builder<Long> b = java.util.stream.Stream.builder();
                    if (hr.getApproverId() != null) b.add(hr.getApproverId());
                    if (hr.getHhrrApproverId() != null) b.add(hr.getHhrrApproverId());
                    return b.build();
                }).collect(Collectors.toSet());

        Map<Long, String> typeNames = hrRequestTypeRepository.findAllById(typeIds)
                .stream().collect(Collectors.toMap(t -> t.getId(), t -> t.getName()));
        Map<Long, String> statusNames = employeeStatusRepository.findAllById(statusIds)
                .stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getName()));
        Map<Long, Employee> employees = employeeRepository.findAllById(employeeIds)
                .stream().collect(Collectors.toMap(e -> e.getId(), e -> e));

        Map<Long, String> approverNames = approverIds.isEmpty() ? Map.of() :
                userClient.getUsersByIds(new java.util.ArrayList<>(approverIds))
                        .stream().collect(Collectors.toMap(
                                u -> u.getId(),
                                u -> u.getFirstName() + " " + u.getLastName()));

        return page.map(hr -> toResponseBatch(hr, typeNames, statusNames, employees, approverNames));
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

            String reqTypeName = hrRequestTypeRepository.findById(hr.getRequestTypeId())
                    .map(HRRequestType::getName).orElse(null);

            return toResponse(hr);

        } else if ("Pendiente de aprobación".equals(currentStatus)) {
            hr.setHhrrApproverId(approverId);
            hr.setHhrrApprovalDate(LocalDateTime.now());
            Long approvedStatusId = resolveStatusId("Aprobado");
            hr.setStatusId(approvedStatusId);
            hrRequestRepository.save(hr);

            String requestTypeName = hrRequestTypeRepository.findById(hr.getRequestTypeId())
                    .map(HRRequestType::getName).orElse(null);

            if ("Contrato".equals(requestTypeName)) {
                Contract contract = contractRepository.findById(hr.getContractId())
                        .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + hr.getContractId()));

                if ("UPDATE".equals(hr.getAction()) && hr.getProposedData() != null) {
                    try {
                        UpdateContractRequest proposed = objectMapper.readValue(hr.getProposedData(), UpdateContractRequest.class);
                        contract.setName(proposed.getName());
                        contract.setContractNumber(proposed.getContractNumber());
                        contract.setContractTypeId(proposed.getContractTypeId());
                        contract.setSafetyGroupId(proposed.getSafetyGroupId());
                        contract.setContractDetail(proposed.getContractDetail());
                        contract.setBaseSalary(proposed.getBaseSalary());
                        contract.setAgreedSalary(proposed.getAgreedSalary());
                        contract.setCompanyId(proposed.getCompanyId());
                        contract.setZoneId(proposed.getZoneId());
                        contract.setJobTitleId(proposed.getJobTitleId());
                        contract.setSiteId(proposed.getSiteId());
                        contract.setLaborUnionId(proposed.getLaborUnionId());
                        contract.setWeeklyWorkHours(proposed.getWeeklyWorkHours());
                        contract.setWorkDays(proposed.getWorkDays());
                        contract.setStartDate(proposed.getStartDate());
                        contract.setEndDate(proposed.getEndDate());
                        contract.setMealTypeId(proposed.getMealTypeId());
                        contract.setTransportTypeId(proposed.getTransportTypeId());
                    } catch (Exception e) {
                        log.warn("No se pudo deserializar proposedData para HRRequest id {}: {}", hr.getId(), e.getMessage());
                    }
                    // Retag archivos pendientes a CONTRACT
                    retagPendingFiles(hr.getId(), contract.getId());
                } else {
                    contract.setStatusId(approvedStatusId);
                    contractStatusRepository.findByName("Activo")
                            .ifPresent(s -> contract.setContractStatusId(s.getId()));
                    Employee employee = employeeRepository.findById(contract.getEmployeeId()).orElse(null);
                    if (employee != null) {
                        employee.setHasContract(true);
                        employeeRepository.save(employee);
                    }
                }
                contractRepository.save(contract);

            } else if ("Finiquito".equals(requestTypeName)) {
                Settlement settlement = settlementRepository.findById(hr.getSettlementId())
                        .orElseThrow(() -> new ResourceNotFoundException("Finiquito no encontrado: " + hr.getSettlementId()));

                if ("UPDATE".equals(hr.getAction()) && hr.getProposedData() != null) {
                    try {
                        com.crm.mcsv_rrhh.dto.UpdateSettlementRequest proposed =
                                objectMapper.readValue(hr.getProposedData(), com.crm.mcsv_rrhh.dto.UpdateSettlementRequest.class);
                        if (proposed.getEndDate() != null)
                            settlement.setEndDate(proposed.getEndDate());
                        if (proposed.getLegalTerminationCauseId() != null)
                            settlement.setLegalTerminationCause(
                                    legalTerminationCauseRepository.findById(proposed.getLegalTerminationCauseId()).orElse(null));
                        if (proposed.getQualityOfWorkId() != null)
                            settlement.setQualityOfWork(
                                    qualityOfWorkRepository.findById(proposed.getQualityOfWorkId()).orElse(null));
                        if (proposed.getSafetyComplianceId() != null)
                            settlement.setSafetyCompliance(
                                    safetyComplianceRepository.findById(proposed.getSafetyComplianceId()).orElse(null));
                        if (proposed.getRehireEligible() != null) {
                            settlement.setRehireEligible(proposed.getRehireEligible());
                            if (!proposed.getRehireEligible() && proposed.getNoReHiredCauseId() != null)
                                settlement.setNoReHiredCause(
                                        noReHiredCauseRepository.findById(proposed.getNoReHiredCauseId()).orElse(null));
                            else if (proposed.getRehireEligible())
                                settlement.setNoReHiredCause(null);
                        }
                        if (proposed.getObservations() != null)
                            settlement.setObservations(proposed.getObservations());
                    } catch (Exception e) {
                        log.warn("No se pudo deserializar proposedData para HRRequest id {}: {}", hr.getId(), e.getMessage());
                    }
                } else {
                    Contract contract = contractRepository.findById(settlement.getContractId())
                            .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + settlement.getContractId()));
                    contractStatusRepository.findByName("Terminado")
                            .ifPresent(s -> contract.setContractStatusId(s.getId()));
                    contractRepository.save(contract);
                }
                settlementRepository.save(settlement);

            } else if ("Traspaso".equals(requestTypeName)) {
                Transfer transfer = transferRepository.findById(hr.getTransferId())
                        .orElseThrow(() -> new ResourceNotFoundException("Traspaso no encontrado: " + hr.getTransferId()));

                if ("UPDATE".equals(hr.getAction()) && hr.getProposedData() != null) {
                    try {
                        UpdateTransferRequest proposed = objectMapper.readValue(hr.getProposedData(), UpdateTransferRequest.class);
                        if (proposed.getToCostCenter() != null) transfer.setToCostCenter(proposed.getToCostCenter());
                        if (proposed.getEffectiveDate() != null) transfer.setEffectiveDate(proposed.getEffectiveDate());
                        if (proposed.getReason() != null) transfer.setReason(proposed.getReason());
                    } catch (Exception e) {
                        log.warn("No se pudo deserializar proposedData para HRRequest id {}: {}", hr.getId(), e.getMessage());
                    }
                    retagPendingTransferFiles(hr.getId(), transfer.getId());
                } else {
                    Employee employee = employeeRepository.findById(transfer.getEmployeeId())
                            .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id: " + transfer.getEmployeeId()));
                    employee.setCostCenter(transfer.getToCostCenter());
                    employeeRepository.save(employee);
                }
                transferRepository.save(transfer);

            } else {
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
                        employee.setCostCenter(proposed.getCostCenter());
                    } catch (Exception e) {
                        log.warn("No se pudo deserializar proposedData para HRRequest id {}: {}", hr.getId(), e.getMessage());
                    }
                } else {
                    employee.setStatusId(approvedStatusId);
                }
                employeeRepository.save(employee);
            }

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

        String requestTypeName = hrRequestTypeRepository.findById(hr.getRequestTypeId())
                .map(HRRequestType::getName).orElse(null);

        if ("UPDATE".equals(hr.getAction()) && "Contrato".equals(requestTypeName)) {
            deletePendingFiles(hr.getId(), hr.getContractId());
        }
        if ("UPDATE".equals(hr.getAction()) && "Traspaso".equals(requestTypeName)) {
            deletePendingTransferFiles(hr.getId(), hr.getTransferId());
        }

        if ("CREATE".equals(hr.getAction())) {
            if ("Contrato".equals(requestTypeName)) {
                Contract contract = contractRepository.findById(hr.getContractId())
                        .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + hr.getContractId()));
                contract.setStatusId(rejectedStatusId);
                contractRepository.save(contract);
            } else if ("Finiquito".equals(requestTypeName)) {
                if (hr.getSettlementId() != null)
                    settlementRepository.deleteById(hr.getSettlementId());
            } else if ("Traspaso".equals(requestTypeName)) {
                if (hr.getTransferId() != null)
                    transferRepository.deleteById(hr.getTransferId());
            } else {
                Employee employee = employeeRepository.findById(hr.getIdModule())
                        .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id: " + hr.getIdModule()));
                employee.setStatusId(rejectedStatusId);
                employeeRepository.save(employee);
            }
        }
        // action="UPDATE" → no tocar la entidad

        return toResponse(hr);
    }

    @Override
    public Map<String, Long> getStats(Long idModule) {
        Map<String, Long> statusIdsByName = employeeStatusRepository
                .findAllByNameIn(List.of("Pendiente de revisión", "Pendiente de aprobación", "Aprobado"))
                .stream().collect(Collectors.toMap(s -> s.getName(), s -> s.getId()));

        List<Long> pendingIds = List.of(
                statusIdsByName.get("Pendiente de revisión"),
                statusIdsByName.get("Pendiente de aprobación")
        );
        Long approvedId = statusIdsByName.get("Aprobado");

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

    private HRRequestResponse toResponseBatch(HRRequest hr,
                                               Map<Long, String> typeNames,
                                               Map<Long, String> statusNames,
                                               Map<Long, Employee> employees,
                                               Map<Long, String> approverNames) {
        HRRequestResponse.HRRequestResponseBuilder builder = HRRequestResponse.builder()
                .id(hr.getId())
                .idModule(hr.getIdModule())
                .requestTypeId(hr.getRequestTypeId())
                .requestTypeName(typeNames.get(hr.getRequestTypeId()))
                .statusId(hr.getStatusId())
                .statusName(statusNames.get(hr.getStatusId()))
                .action(hr.getAction())
                .approverId(hr.getApproverId())
                .approverFullName(hr.getApproverId() != null ? approverNames.get(hr.getApproverId()) : null)
                .approvalDate(hr.getApprovalDate())
                .hhrrApproverId(hr.getHhrrApproverId())
                .hhrrApproverFullName(hr.getHhrrApproverId() != null ? approverNames.get(hr.getHhrrApproverId()) : null)
                .hhrrApprovalDate(hr.getHhrrApprovalDate())
                .rejectionDetail(hr.getRejectionDetail())
                .createdAt(hr.getCreatedAt())
                .updatedAt(hr.getUpdatedAt());

        Employee emp = employees.get(hr.getIdModule());
        if (emp != null) {
            builder.identification(emp.getIdentification())
                   .firstName(emp.getFirstName())
                   .paternalLastName(emp.getPaternalLastName())
                   .maternalLastName(emp.getMaternalLastName());
        }

        return builder.build();
    }

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

    private void retagPendingFiles(Long hrRequestId, Long contractId) {
        try {
            var response = storageService.listByEntity("CONTRACT_PENDING", hrRequestId);
            if (response != null) {
                for (FileMetadataResponse file : response) {
                    storageService.retag(file.getId(), "CONTRACT", contractId);
                }
            }
        } catch (Exception e) {
            log.warn("Error retagging pending files for hrRequest {}: {}", hrRequestId, e.getMessage());
        }
    }

    private void deletePendingFiles(Long hrRequestId, Long contractId) {
        try {
            var response = storageService.listByEntity("CONTRACT_PENDING", hrRequestId);
            if (response != null) {
                Contract contract = contractRepository.findById(contractId).orElse(null);
                Long uploadedBy = contract != null ? contract.getEmployeeId() : null;
                if (uploadedBy != null) {
                    for (FileMetadataResponse file : response) {
                        storageService.delete(file.getId(), uploadedBy);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error deleting pending files for hrRequest {}: {}", hrRequestId, e.getMessage());
        }
    }

    private void retagPendingTransferFiles(Long hrRequestId, Long transferId) {
        try {
            var response = storageService.listByEntity("TRANSFER_PENDING", hrRequestId);
            if (response != null) {
                for (FileMetadataResponse file : response) {
                    storageService.retag(file.getId(), "TRANSFER", transferId);
                }
            }
        } catch (Exception e) {
            log.warn("Error retagging pending transfer files for hrRequest {}: {}", hrRequestId, e.getMessage());
        }
    }

    private void deletePendingTransferFiles(Long hrRequestId, Long transferId) {
        try {
            var response = storageService.listByEntity("TRANSFER_PENDING", hrRequestId);
            if (response != null) {
                Transfer transfer = transferRepository.findById(transferId).orElse(null);
                Long uploadedBy = transfer != null ? transfer.getEmployeeId() : null;
                if (uploadedBy != null) {
                    for (FileMetadataResponse file : response) {
                        storageService.delete(file.getId(), uploadedBy);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error deleting pending transfer files for hrRequest {}: {}", hrRequestId, e.getMessage());
        }
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
