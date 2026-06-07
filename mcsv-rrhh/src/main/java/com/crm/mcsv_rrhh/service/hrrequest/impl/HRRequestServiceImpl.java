package com.crm.mcsv_rrhh.service.hrrequest.impl;

import com.crm.common.service.StorageService;
import com.crm.common.util.CsvUtil;
import com.crm.mcsv_rrhh.client.UserClient;
import com.crm.common.dto.FileMetadataResponse;
import com.crm.mcsv_rrhh.dto.hrrequest.HRRequestDetailResponse;
import com.crm.mcsv_rrhh.dto.hrrequest.HRRequestResponse;
import com.crm.mcsv_rrhh.dto.contract.UpdateContractRequest;
import com.crm.mcsv_rrhh.dto.employee.UpdateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.leave.UpdateEmployeeLeaveRequest;
import com.crm.mcsv_rrhh.dto.transfer.UpdateTransferRequest;
import com.crm.mcsv_rrhh.dto.contractannex.UpdateContractAnnexRequest;
import com.crm.mcsv_rrhh.dto.overtime.OvertimeUpdateRequest;
import com.crm.mcsv_rrhh.entity.contractannex.ContractAnnex;
import com.crm.mcsv_rrhh.entity.employee.EmployeeStatus;
import com.crm.mcsv_rrhh.entity.leave.EmployeeLeave;
import com.crm.mcsv_rrhh.entity.overtime.Overtime;
import com.crm.mcsv_rrhh.entity.transfer.Transfer;
import com.crm.mcsv_rrhh.repository.contractannex.ContractAnnexRepository;
import com.crm.mcsv_rrhh.repository.leave.EmployeeLeaveRepository;
import com.crm.mcsv_rrhh.repository.overtime.OvertimeRepository;
import com.crm.mcsv_rrhh.repository.transfer.TransferRepository;
import com.crm.mcsv_rrhh.client.dto.UserDTO;
import com.crm.mcsv_rrhh.dto.hrrequest.RejectHRRequestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.crm.mcsv_rrhh.entity.contract.Contract;
import com.crm.mcsv_rrhh.entity.contract.ContractStatus;
import com.crm.mcsv_rrhh.enums.contract.ContractStatusName;
import com.crm.mcsv_rrhh.entity.employee.Employee;
import com.crm.mcsv_rrhh.entity.hrrequest.HRRequest;
import com.crm.mcsv_rrhh.entity.hrrequest.HRRequestType;
import com.crm.mcsv_rrhh.entity.settlement.Settlement;
import com.crm.mcsv_rrhh.enums.hrrequest.HRRequestTypeName;
import com.crm.mcsv_rrhh.enums.hrrequest.RequestStatus;
import com.crm.mcsv_rrhh.repository.settlement.SettlementRepository;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.contract.ContractRepository;
import com.crm.mcsv_rrhh.repository.contract.ContractStatusRepository;
import com.crm.mcsv_rrhh.repository.employee.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.employee.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.hrrequest.HRRequestRepository;
import com.crm.mcsv_rrhh.repository.hrrequest.HRRequestSpecification;
import com.crm.mcsv_rrhh.repository.hrrequest.HRRequestTypeRepository;
import com.crm.mcsv_rrhh.service.attendance.AttendanceLeaveSyncService;
import com.crm.mcsv_rrhh.service.attendance.AttendanceOvertimeSyncService;
import com.crm.mcsv_rrhh.mapper.contract.ContractMapper;
import com.crm.mcsv_rrhh.mapper.contractannex.ContractAnnexMapper;
import com.crm.mcsv_rrhh.mapper.employee.EmployeeMapper;
import com.crm.mcsv_rrhh.mapper.hrrequest.HRRequestMapper;
import com.crm.mcsv_rrhh.mapper.settlement.SettlementMapper;
import com.crm.mcsv_rrhh.mapper.transfer.TransferMapper;
import com.crm.mcsv_rrhh.service.hrrequest.HRRequestService;
import com.crm.mcsv_rrhh.service.projectassignment.ProjectAssignmentSyncService;
import com.crm.mcsv_rrhh.util.leave.LeaveCalculator;
import com.crm.mcsv_rrhh.util.leave.LeaveValidator;
import com.crm.mcsv_rrhh.util.overtime.OvertimeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final TransferRepository transferRepository;
    private final ContractAnnexRepository contractAnnexRepository;
    private final EmployeeLeaveRepository employeeLeaveRepository;
    private final OvertimeRepository overtimeRepository;
    private final UserClient userClient;
    private final StorageService storageService;
    private final LeaveValidator leaveValidator;
    private final OvertimeValidator overtimeValidator;
    private final ObjectMapper objectMapper;
    private final ProjectAssignmentSyncService projectAssignmentSyncService;
    private final AttendanceLeaveSyncService attendanceLeaveSyncService;
    private final AttendanceOvertimeSyncService attendanceOvertimeSyncService;
    private final HRRequestMapper hrRequestMapper;
    private final ContractMapper contractMapper;
    private final SettlementMapper settlementMapper;
    private final TransferMapper transferMapper;
    private final ContractAnnexMapper contractAnnexMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public HRRequest createForEmployee(Long employeeId, String requestTypeName, String action, String proposedData) {
        return createRequest(requestTypeName, employeeId, action, proposedData, null);
    }

    @Override
    @Transactional
    public HRRequest createForContract(Long contractId, Long employeeId, String action, String proposedData) {
        return createRequest(HRRequestTypeName.CONTRACT.getDisplayName(), employeeId, action, proposedData,
                b -> b.contractId(contractId));
    }

    @Override
    public HRRequest createForSettlement(Long settlementId, Long employeeId, String action, String proposedData) {
        return createRequest(HRRequestTypeName.SETTLEMENT.getDisplayName(), employeeId, action, proposedData,
                b -> b.settlementId(settlementId));
    }

    @Override
    public HRRequest createForTransfer(Long transferId, Long employeeId, String action, String proposedData) {
        return createRequest(HRRequestTypeName.TRANSFER.getDisplayName(), employeeId, action, proposedData,
                b -> b.transferId(transferId));
    }

    @Override
    public HRRequest createForAnnex(Long annexId, Long employeeId, String action, String proposedData) {
        return createRequest(HRRequestTypeName.ANNEX.getDisplayName(), employeeId, action, proposedData,
                b -> b.annexId(annexId));
    }

    @Override
    public HRRequest createForLeave(Long leaveId, Long employeeId, String action, String proposedData) {
        return createRequest(HRRequestTypeName.LEAVE.getDisplayName(), employeeId, action, proposedData,
                b -> b.leaveId(leaveId));
    }

    @Override
    public HRRequest createForOvertime(Long overtimeId, Long employeeId, String action, String proposedData) {
        return createRequest(HRRequestTypeName.OVERTIME.getDisplayName(), employeeId, action, proposedData,
                b -> b.overtimeId(overtimeId));
    }

    private HRRequest createRequest(String requestTypeName, Long employeeId, String action, String proposedData,
                                    Consumer<HRRequest.HRRequestBuilder> link) {
        HRRequestType type = hrRequestTypeRepository.findByName(requestTypeName)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de solicitud no encontrado: " + requestTypeName));

        HRRequest request = hrRequestMapper.toEntity(
                type, resolveInitialStatusId(type), employeeId, action, proposedData, link);

        return hrRequestRepository.save(request);
    }

    private Long resolveInitialStatusId(HRRequestType type) {
        String initialStatusName = Boolean.TRUE.equals(type.getRequireApproval())
                ? RequestStatus.PENDING_REVIEW.getDisplayName()
                : RequestStatus.PENDING_APPROVAL.getDisplayName();
        return employeeStatusRepository.findByName(initialStatusName)
                .map(EmployeeStatus::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: " + initialStatusName));
    }

    @Override
    public Page<HRRequestResponse> list(Long idModule, Long statusId,
                                         LocalDate createdFrom, LocalDate createdTo,
                                         LocalDate approvalFrom, LocalDate approvalTo,
                                         Pageable pageable, String sortBy, String sortDir) {
        Pageable effectivePageable = HRRequestSpecification.isEmployeeSortField(sortBy)
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
                : pageable;

        Page<HRRequest> page = hrRequestRepository.findAll(
                HRRequestSpecification.withFilters(idModule, statusId, createdFrom, createdTo, approvalFrom, approvalTo, sortBy, sortDir),
                effectivePageable);

        if (page.isEmpty()) return page.map(this::toResponse);

        Set<Long> typeIds     = page.map(HRRequest::getRequestTypeId).toSet();
        Set<Long> statusIds   = page.map(HRRequest::getStatusId).toSet();
        Set<Long> employeeIds = page.map(HRRequest::getIdModule).toSet();

        Set<Long> approverIds = page.stream()
                .flatMap(hr -> Stream.of(hr.getApproverId(), hr.getHhrrApproverId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> typeNames = hrRequestTypeRepository.findAllById(typeIds)
                .stream().collect(Collectors.toMap(HRRequestType::getId, HRRequestType::getName));
        Map<Long, String> statusNames = employeeStatusRepository.findAllById(statusIds)
                .stream().collect(Collectors.toMap(EmployeeStatus::getId, EmployeeStatus::getName));
        Map<Long, Employee> employees = employeeRepository.findAllById(employeeIds)
                .stream().collect(Collectors.toMap(Employee::getId, e -> e));

        Map<Long, String> approverNames = approverIds.isEmpty() ? Map.of() :
                userClient.getUsersByIds(new ArrayList<>(approverIds))
                        .stream().collect(Collectors.toMap(
                                UserDTO::getId,
                                u -> u.getFirstName() + " " + u.getLastName()));

        return page.map(hr -> toResponseBatch(hr, typeNames, statusNames, employees, approverNames));
    }

    @Override
    public HRRequestDetailResponse getById(Long id) {
        HRRequest hr = findOrThrow(id);

        HRRequestType type = hrRequestTypeRepository.findById(hr.getRequestTypeId()).orElse(null);
        EmployeeStatus status = employeeStatusRepository.findById(hr.getStatusId()).orElse(null);
        Employee employee = employeeRepository.findById(hr.getIdModule()).orElse(null);
        String approverName = hr.getApproverId() != null ? fetchFullName(hr.getApproverId()) : null;
        String hhrrApproverName = hr.getHhrrApproverId() != null ? fetchFullName(hr.getHhrrApproverId()) : null;

        return hrRequestMapper.toDetailResponse(hr, type, status, employee, approverName, hhrrApproverName);
    }

    @Override
    @Transactional
    public HRRequestResponse approve(Long id, Long approverId) {
        HRRequest hr = findOrThrow(id);
        String currentStatus = resolveStatusName(hr.getStatusId());

        if (RequestStatus.PENDING_REVIEW.getDisplayName().equals(currentStatus)) {
            hr.setApproverId(approverId);
            hr.setApprovalDate(LocalDateTime.now());
            hr.setStatusId(resolveStatusId(RequestStatus.PENDING_APPROVAL.getDisplayName()));
            hrRequestRepository.save(hr);

            return toResponse(hr);

        } else if (RequestStatus.PENDING_APPROVAL.getDisplayName().equals(currentStatus)) {
            Long approvedStatusId = resolveStatusId(RequestStatus.APPROVED.getDisplayName());

            String requestTypeName = hrRequestTypeRepository.findById(hr.getRequestTypeId())
                    .map(HRRequestType::getName).orElse(null);

            if (HRRequestTypeName.CONTRACT.getDisplayName().equals(requestTypeName)) {
                Contract contract = contractRepository.findById(hr.getContractId())
                        .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + hr.getContractId()));

                Integer previousCostCenter = contract.getCostCenter();
                boolean isUpdate = "UPDATE".equals(hr.getAction()) && hr.getProposedData() != null;

                if (isUpdate) {
                    try {
                        UpdateContractRequest proposed = objectMapper.readValue(hr.getProposedData(), UpdateContractRequest.class);
                        contractMapper.applyUpdate(contract, proposed);
                    } catch (Exception e) {
                        log.warn("No se pudo deserializar proposedData para HRRequest id {}: {}", hr.getId(), e.getMessage());
                    }
                    // Retag archivos pendientes a CONTRACT
                    retagPendingFiles(hr.getId(), contract.getId());
                } else {
                    contract.setStatusId(approvedStatusId);
                    contractStatusRepository.findByName(ContractStatusName.ACTIVE.getDisplayName())
                            .ifPresent(s -> contract.setContractStatusId(s.getId()));
                    Employee employee = employeeRepository.findById(contract.getEmployeeId()).orElse(null);
                    if (employee != null) {
                        employee.setHasContract(true);
                        employeeRepository.save(employee);
                    }
                }
                contractRepository.save(contract);

                if (isUpdate) {
                    if (!Objects.equals(previousCostCenter, contract.getCostCenter())) {
                        projectAssignmentSyncService.syncCostCenterChange(contract, previousCostCenter, LocalDate.now());
                    }
                } else {
                    projectAssignmentSyncService.openInitialAssignment(contract);
                }

            } else if (HRRequestTypeName.SETTLEMENT.getDisplayName().equals(requestTypeName)) {
                Settlement settlement = settlementRepository.findById(hr.getSettlementId())
                        .orElseThrow(() -> new ResourceNotFoundException("Finiquito no encontrado: " + hr.getSettlementId()));

                if ("UPDATE".equals(hr.getAction()) && hr.getProposedData() != null) {
                    try {
                        com.crm.mcsv_rrhh.dto.settlement.UpdateSettlementRequest proposed =
                                objectMapper.readValue(hr.getProposedData(), com.crm.mcsv_rrhh.dto.settlement.UpdateSettlementRequest.class);
                        settlementMapper.applyUpdate(settlement, proposed);
                    } catch (Exception e) {
                        log.warn("No se pudo deserializar proposedData para HRRequest id {}: {}", hr.getId(), e.getMessage());
                    }
                } else {
                    Contract contract = contractRepository.findById(settlement.getContractId())
                            .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + settlement.getContractId()));
                    contractStatusRepository.findByName(ContractStatusName.TERMINATED.getDisplayName())
                            .ifPresent(s -> contract.setContractStatusId(s.getId()));
                    contractRepository.save(contract);
                }
                settlementRepository.save(settlement);

            } else if (HRRequestTypeName.TRANSFER.getDisplayName().equals(requestTypeName)) {
                Transfer transfer = transferRepository.findById(hr.getTransferId())
                        .orElseThrow(() -> new ResourceNotFoundException("Traspaso no encontrado: " + hr.getTransferId()));

                if ("UPDATE".equals(hr.getAction()) && hr.getProposedData() != null) {
                    try {
                        UpdateTransferRequest proposed = objectMapper.readValue(hr.getProposedData(), UpdateTransferRequest.class);
                        transferMapper.applyUpdate(transfer, proposed);
                    } catch (Exception e) {
                        log.warn("No se pudo deserializar proposedData para HRRequest id {}: {}", hr.getId(), e.getMessage());
                    }
                    retagPendingTransferFiles(hr.getId(), transfer.getId());
                } else {
                    Employee employee = employeeRepository.findById(transfer.getEmployeeId())
                            .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id: " + transfer.getEmployeeId()));
                    Long activeContractStatusId = contractStatusRepository.findByName(ContractStatusName.ACTIVE.getDisplayName())
                            .map(ContractStatus::getId)
                            .orElseThrow(() -> new IllegalStateException("Estado de contrato 'Activo' no encontrado"));
                    Contract activeContract = contractRepository.findFirstByEmployeeIdAndContractStatusId(employee.getId(), activeContractStatusId)
                            .orElseThrow(() -> new IllegalStateException("El empleado no tiene un contrato activo para aplicar el traspaso"));
                    Integer previousCostCenter = activeContract.getCostCenter();
                    activeContract.setCostCenter(transfer.getToCostCenter());
                    contractRepository.save(activeContract);
                    projectAssignmentSyncService.syncCostCenterChange(
                            activeContract,
                            previousCostCenter,
                            transfer.getEffectiveDate() != null ? transfer.getEffectiveDate() : LocalDate.now());
                }
                transferRepository.save(transfer);

            } else if (HRRequestTypeName.ANNEX.getDisplayName().equals(requestTypeName)) {
                ContractAnnex annex = contractAnnexRepository.findById(hr.getAnnexId())
                        .orElseThrow(() -> new ResourceNotFoundException("Anexo no encontrado: " + hr.getAnnexId()));

                if ("UPDATE".equals(hr.getAction()) && hr.getProposedData() != null) {
                    try {
                        UpdateContractAnnexRequest proposed = objectMapper.readValue(hr.getProposedData(), UpdateContractAnnexRequest.class);
                        contractAnnexMapper.applyUpdate(annex, proposed);
                    } catch (Exception e) {
                        log.warn("No se pudo deserializar proposedData para HRRequest id {}: {}", hr.getId(), e.getMessage());
                    }
                    retagPendingAnnexFiles(hr.getId(), annex.getId());
                }
                contractAnnexRepository.save(annex);

            } else if (HRRequestTypeName.LEAVE.getDisplayName().equals(requestTypeName)) {
                EmployeeLeave leave = employeeLeaveRepository.findById(hr.getLeaveId())
                        .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado: " + hr.getLeaveId()));

                if ("UPDATE".equals(hr.getAction()) && hr.getProposedData() != null) {
                    UpdateEmployeeLeaveRequest proposed;
                    try {
                        proposed = objectMapper.readValue(hr.getProposedData(), UpdateEmployeeLeaveRequest.class);
                    } catch (Exception e) {
                        log.warn("No se pudo deserializar proposedData para HRRequest id {}: {}", hr.getId(), e.getMessage());
                        throw new RuntimeException("Error deserializando la actualización del permiso", e);
                    }
                    EmployeeLeave candidate = mergeLeaveCandidate(leave, proposed);
                    leaveValidator.validate(candidate, null, leave.getId(), hr.getId());
                    applyLeaveChanges(leave, candidate);
                    retagPendingLeaveFiles(hr.getId(), leave.getId());
                } else {
                    leaveValidator.validate(leave, null, leave.getId(), null);
                }

                if (!overtimeRepository.findApprovedByEmployeeIdAndDateBetween(
                        leave.getEmployeeId(), leave.getStartDate(), leave.getEndDate()).isEmpty()) {
                    throw new IllegalStateException(
                            "No se puede aprobar el permiso: el empleado tiene horas extras aprobadas en el rango");
                }

                employeeLeaveRepository.save(leave);
                attendanceLeaveSyncService.revertGeneratedForLeave(leave.getId());
                attendanceLeaveSyncService.generateForApprovedLeave(leave);

            } else if (HRRequestTypeName.OVERTIME.getDisplayName().equals(requestTypeName)) {
                Overtime overtime = overtimeRepository.findById(hr.getOvertimeId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Hora extra no encontrada: " + hr.getOvertimeId()));

                if ("UPDATE".equals(hr.getAction()) && hr.getProposedData() != null) {
                    OvertimeUpdateRequest proposed;
                    try {
                        proposed = objectMapper.readValue(hr.getProposedData(), OvertimeUpdateRequest.class);
                    } catch (Exception e) {
                        log.warn("No se pudo deserializar proposedData para HRRequest id {}: {}", hr.getId(), e.getMessage());
                        throw new RuntimeException("Error deserializando la actualización de la hora extra", e);
                    }
                    Overtime candidate = mergeOvertimeCandidate(overtime, proposed);
                    OvertimeValidator.Result result = overtimeValidator.validate(candidate, overtime.getId());
                    overtime.setOvertimeTypeId(candidate.getOvertimeTypeId());
                    overtime.setStartTime(candidate.getStartTime());
                    overtime.setEndTime(candidate.getEndTime());
                    overtime.setHours(result.hours());
                    overtime.setReason(candidate.getReason());
                } else {
                    overtimeValidator.validate(overtime, overtime.getId());
                }
                overtimeRepository.save(overtime);
                attendanceOvertimeSyncService.recalculateAttendanceOvertime(overtime.getAttendanceId());

            } else {
                Employee employee = employeeRepository.findById(hr.getIdModule())
                        .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id: " + hr.getIdModule()));

                if ("UPDATE".equals(hr.getAction()) && hr.getProposedData() != null) {
                    try {
                        UpdateEmployeeRequest proposed = objectMapper.readValue(hr.getProposedData(), UpdateEmployeeRequest.class);
                        employeeMapper.applyUpdate(employee, proposed);
                    } catch (Exception e) {
                        log.warn("No se pudo deserializar proposedData para HRRequest id {}: {}", hr.getId(), e.getMessage());
                    }
                } else {
                    employee.setStatusId(approvedStatusId);
                }
                employeeRepository.save(employee);
            }

            hr.setHhrrApproverId(approverId);
            hr.setHhrrApprovalDate(LocalDateTime.now());
            hr.setStatusId(approvedStatusId);
            hrRequestRepository.save(hr);

            return toResponse(hr);

        } else {
            throw new IllegalStateException("La solicitud no está en un estado aprobable");
        }
    }

    @Override
    @Transactional
    public HRRequestResponse reject(Long id, RejectHRRequestRequest req) {
        HRRequest hr = findOrThrow(id);
        String currentStatus = resolveStatusName(hr.getStatusId());
        if (RequestStatus.APPROVED.getDisplayName().equals(currentStatus)) {
            throw new IllegalStateException("La solicitud ya está aprobada y no puede ser rechazada");
        }
        if (RequestStatus.REJECTED.getDisplayName().equals(currentStatus)) {
            throw new IllegalStateException("La solicitud ya está rechazada");
        }

        Long rejectedStatusId = resolveStatusId(RequestStatus.REJECTED.getDisplayName());

        hr.setRejectionDetail(req.getRejectionDetail());
        hr.setStatusId(rejectedStatusId);
        hrRequestRepository.save(hr);

        String requestTypeName = hrRequestTypeRepository.findById(hr.getRequestTypeId())
                .map(HRRequestType::getName).orElse(null);

        if ("UPDATE".equals(hr.getAction()) && HRRequestTypeName.CONTRACT.getDisplayName().equals(requestTypeName)) {
            deletePendingFiles(hr.getId(), hr.getContractId());
        }
        if ("UPDATE".equals(hr.getAction()) && HRRequestTypeName.TRANSFER.getDisplayName().equals(requestTypeName)) {
            deletePendingTransferFiles(hr.getId(), hr.getTransferId());
        }
        if ("UPDATE".equals(hr.getAction()) && HRRequestTypeName.ANNEX.getDisplayName().equals(requestTypeName)) {
            deletePendingAnnexFiles(hr.getId(), hr.getAnnexId());
        }
        if ("UPDATE".equals(hr.getAction()) && HRRequestTypeName.LEAVE.getDisplayName().equals(requestTypeName)) {
            deletePendingLeaveFiles(hr.getId(), hr.getLeaveId());
        }
        if (HRRequestTypeName.LEAVE.getDisplayName().equals(requestTypeName) && hr.getLeaveId() != null) {
            attendanceLeaveSyncService.revertGeneratedForLeave(hr.getLeaveId());
        }

        if ("CREATE".equals(hr.getAction())) {
            if (HRRequestTypeName.CONTRACT.getDisplayName().equals(requestTypeName)) {
                Contract contract = contractRepository.findById(hr.getContractId())
                        .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + hr.getContractId()));
                contract.setStatusId(rejectedStatusId);
                contractRepository.save(contract);
            } else if (HRRequestTypeName.SETTLEMENT.getDisplayName().equals(requestTypeName)) {
                if (hr.getSettlementId() != null)
                    settlementRepository.deleteById(hr.getSettlementId());
            } else if (HRRequestTypeName.TRANSFER.getDisplayName().equals(requestTypeName)) {
                if (hr.getTransferId() != null)
                    transferRepository.deleteById(hr.getTransferId());
            } else if (HRRequestTypeName.ANNEX.getDisplayName().equals(requestTypeName)) {
                if (hr.getAnnexId() != null)
                    contractAnnexRepository.deleteById(hr.getAnnexId());
            } else if (HRRequestTypeName.LEAVE.getDisplayName().equals(requestTypeName)) {
                if (hr.getLeaveId() != null) {
                    deleteLeaveFiles(hr.getLeaveId());
                    employeeLeaveRepository.deleteById(hr.getLeaveId());
                }
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
                .findAllByNameIn(RequestStatus.ACTIVE_DISPLAY_NAMES)
                .stream().collect(Collectors.toMap(EmployeeStatus::getName, EmployeeStatus::getId));

        List<Long> pendingIds = List.of(
                statusIdsByName.get(RequestStatus.PENDING_REVIEW.getDisplayName()),
                statusIdsByName.get(RequestStatus.PENDING_APPROVAL.getDisplayName())
        );
        Long approvedId = statusIdsByName.get(RequestStatus.APPROVED.getDisplayName());

        long total   = idModule != null ? hrRequestRepository.countByIdModule(idModule)                              : hrRequestRepository.count();
        long pending = idModule != null ? hrRequestRepository.countByIdModuleAndStatusIdIn(idModule, pendingIds)     : hrRequestRepository.countByStatusIdIn(pendingIds);
        long active  = idModule != null ? hrRequestRepository.countByIdModuleAndStatusId(idModule, approvedId)       : hrRequestRepository.countByStatusId(approvedId);
        return Map.of("total", total, "active", active, "pending", pending);
    }

    @Override
    public byte[] exportCsv() {
        String header = "ID,RUT,Nombre,Apellido Paterno,Tipo Solicitud,Estado,Aprobador,Fecha Aprobación,Aprobador RRHH,Fecha Aprobación RRHH,Detalle Rechazo,Fecha Creación";

        return CsvUtil.build(header, hrRequestRepository.findAll(), hr -> {
            String typeName = hrRequestTypeRepository.findById(hr.getRequestTypeId()).map(HRRequestType::getName).orElse("");
            String statusName = resolveStatusName(hr.getStatusId());
            String approverName = hr.getApproverId() != null ? fetchFullName(hr.getApproverId()) : "";
            String hhrrApproverName = hr.getHhrrApproverId() != null ? fetchFullName(hr.getHhrrApproverId()) : "";
            Employee emp = employeeRepository.findById(hr.getIdModule()).orElse(null);

            return String.join(",",
                    String.valueOf(hr.getId()),
                    CsvUtil.escape(emp != null ? emp.getIdentification() : ""),
                    CsvUtil.escape(emp != null ? emp.getFirstName() : ""),
                    CsvUtil.escape(emp != null ? emp.getPaternalLastName() : ""),
                    CsvUtil.escape(typeName),
                    CsvUtil.escape(statusName),
                    CsvUtil.escape(approverName),
                    CsvUtil.formatDate(hr.getApprovalDate()),
                    CsvUtil.escape(hhrrApproverName),
                    CsvUtil.formatDate(hr.getHhrrApprovalDate()),
                    CsvUtil.escape(hr.getRejectionDetail()),
                    CsvUtil.formatDate(hr.getCreatedAt()));
        });
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
                .map(EmployeeStatus::getName)
                .orElse(null);
    }

    private Long resolveStatusId(String name) {
        return employeeStatusRepository.findByName(name)
                .map(EmployeeStatus::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: " + name));
    }

    private HRRequestResponse toResponse(HRRequest hr) {
        String typeName = hrRequestTypeRepository.findById(hr.getRequestTypeId())
                .map(HRRequestType::getName)
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

    private void retagPendingAnnexFiles(Long hrRequestId, Long annexId) {
        try {
            var response = storageService.listByEntity("ANNEX_PENDING", hrRequestId);
            if (response != null) {
                for (FileMetadataResponse file : response) {
                    storageService.retag(file.getId(), "ANNEX", annexId);
                }
            }
        } catch (Exception e) {
            log.warn("Error retagging pending annex files for hrRequest {}: {}", hrRequestId, e.getMessage());
        }
    }

    private void deletePendingAnnexFiles(Long hrRequestId, Long annexId) {
        try {
            var response = storageService.listByEntity("ANNEX_PENDING", hrRequestId);
            if (response != null) {
                ContractAnnex annex = contractAnnexRepository.findById(annexId).orElse(null);
                Long uploadedBy = annex != null ? annex.getEmployeeId() : null;
                if (uploadedBy != null) {
                    for (FileMetadataResponse file : response) {
                        storageService.delete(file.getId(), uploadedBy);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error deleting pending annex files for hrRequest {}: {}", hrRequestId, e.getMessage());
        }
    }

    private EmployeeLeave mergeLeaveCandidate(EmployeeLeave current, UpdateEmployeeLeaveRequest proposed) {
        var startDate = proposed.getStartDate() != null ? proposed.getStartDate() : current.getStartDate();
        var endDate = proposed.getEndDate() != null ? proposed.getEndDate() : current.getEndDate();
        var halfDay = proposed.getHalfDay() != null ? proposed.getHalfDay() : current.getHalfDay();

        return EmployeeLeave.builder()
                .id(current.getId())
                .employeeId(current.getEmployeeId())
                .contractId(current.getContractId())
                .leaveTypeId(proposed.getLeaveTypeId() != null ? proposed.getLeaveTypeId() : current.getLeaveTypeId())
                .startDate(startDate)
                .endDate(endDate)
                .halfDay(Boolean.TRUE.equals(halfDay))
                .totalDays(LeaveCalculator.computeTotalDays(startDate, endDate, halfDay))
                .reason(proposed.getReason() != null ? proposed.getReason() : current.getReason())
                .createdAt(current.getCreatedAt())
                .updatedAt(current.getUpdatedAt())
                .build();
    }

    private Overtime mergeOvertimeCandidate(Overtime current, OvertimeUpdateRequest proposed) {
        return Overtime.builder()
                .id(current.getId())
                .employeeId(current.getEmployeeId())
                .contractId(current.getContractId())
                .costCenter(current.getCostCenter())
                .overtimeTypeId(proposed.getOvertimeTypeId() != null
                        ? proposed.getOvertimeTypeId() : current.getOvertimeTypeId())
                .attendanceId(current.getAttendanceId())
                .date(current.getDate())
                .startTime(proposed.getStartTime() != null
                        ? proposed.getStartTime() : current.getStartTime())
                .endTime(proposed.getEndTime() != null
                        ? proposed.getEndTime() : current.getEndTime())
                .hours(current.getHours())
                .reason(proposed.getReason() != null ? proposed.getReason() : current.getReason())
                .createdAt(current.getCreatedAt())
                .updatedAt(current.getUpdatedAt())
                .build();
    }

    private void applyLeaveChanges(EmployeeLeave target, EmployeeLeave source) {
        target.setLeaveTypeId(source.getLeaveTypeId());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setHalfDay(source.getHalfDay());
        target.setTotalDays(source.getTotalDays());
        target.setReason(source.getReason());
    }

    private void retagPendingLeaveFiles(Long hrRequestId, Long leaveId) {
        try {
            var response = storageService.listByEntity("LEAVE_PENDING", hrRequestId);
            if (response != null) {
                for (FileMetadataResponse file : response) {
                    storageService.retag(file.getId(), "LEAVE", leaveId);
                }
            }
        } catch (Exception e) {
            log.warn("Error retagging pending leave files for hrRequest {}: {}", hrRequestId, e.getMessage());
        }
    }

    private void deletePendingLeaveFiles(Long hrRequestId, Long leaveId) {
        try {
            var response = storageService.listByEntity("LEAVE_PENDING", hrRequestId);
            if (response != null) {
                EmployeeLeave leave = employeeLeaveRepository.findById(leaveId).orElse(null);
                Long uploadedBy = leave != null ? leave.getEmployeeId() : null;
                if (uploadedBy != null) {
                    for (FileMetadataResponse file : response) {
                        storageService.delete(file.getId(), uploadedBy);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error deleting pending leave files for hrRequest {}: {}", hrRequestId, e.getMessage());
        }
    }

    private void deleteLeaveFiles(Long leaveId) {
        try {
            var response = storageService.listByEntity("LEAVE", leaveId);
            if (response != null) {
                EmployeeLeave leave = employeeLeaveRepository.findById(leaveId).orElse(null);
                Long uploadedBy = leave != null ? leave.getEmployeeId() : null;
                if (uploadedBy != null) {
                    for (FileMetadataResponse file : response) {
                        storageService.delete(file.getId(), uploadedBy);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error deleting leave files for leave {}: {}", leaveId, e.getMessage());
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
