package com.crm.mcsv_rrhh.service.impl;

import com.crm.mcsv_rrhh.client.StorageClient;
import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.mcsv_rrhh.dto.ContractDetailResponse;
import com.crm.mcsv_rrhh.dto.ContractResponse;
import com.crm.mcsv_rrhh.dto.CreateContractRequest;
import com.crm.mcsv_rrhh.dto.FileMetadataResponse;
import com.crm.mcsv_rrhh.dto.UpdateContractRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.crm.mcsv_rrhh.entity.Contract;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.*;
import com.crm.mcsv_rrhh.repository.ContractSpecification;
import com.crm.mcsv_rrhh.service.ContractService;
import com.crm.mcsv_rrhh.service.HRRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {

    private static final String ENTITY_TYPE = "CONTRACT";
    private static final int MAX_DOCUMENTS = 5;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf", "image/jpeg", "image/png");

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final HRRequestRepository hrRequestRepository;
    private final HRRequestService hrRequestService;
    private final ObjectMapper objectMapper;
    private final StorageClient storageClient;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final ContractStatusRepository contractStatusRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final SafetyGroupRepository safetyGroupRepository;
    private final CompanyRepository companyRepository;
    private final ZoneRepository zoneRepository;
    private final JobTitleRepository jobTitleRepository;
    private final SiteRepository siteRepository;
    private final LaborUnionRepository laborUnionRepository;
    private final MealTypeRepository mealTypeRepository;
    private final TransportTypeRepository transportTypeRepository;

    @Override
    @Transactional
    public ContractDetailResponse createContract(CreateContractRequest request, List<MultipartFile> files) {
        Long pendingStatusId = employeeStatusRepository.findByName("Pendiente de revisión")
                .map(s -> s.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: Pendiente de revisión"));

        Long suspendedContractStatusId = contractStatusRepository.findByName("Suspendido")
                .map(s -> s.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado de contrato no encontrado: Suspendido"));

        Long contractTypeId = request.getEndDate() == null
                ? contractTypeRepository.findByName("Indefinido")
                        .map(t -> t.getId())
                        .orElse(request.getContractTypeId())
                : request.getContractTypeId();

        Contract contract = Contract.builder()
                .employeeId(request.getEmployeeId())
                .name(request.getName())
                .contractNumber(request.getContractNumber())
                .contractTypeId(contractTypeId)
                .contractStatusId(suspendedContractStatusId)
                .safetyGroupId(request.getSafetyGroupId())
                .contractDetail(request.getContractDetail())
                .baseSalary(request.getBaseSalary())
                .agreedSalary(request.getAgreedSalary())
                .companyId(request.getCompanyId())
                .zoneId(request.getZoneId())
                .jobTitleId(request.getJobTitleId())
                .siteId(request.getSiteId())
                .laborUnionId(request.getLaborUnionId())
                .weeklyWorkHours(request.getWeeklyWorkHours())
                .workDays(request.getWorkDays())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .mealTypeId(request.getMealTypeId())
                .transportTypeId(request.getTransportTypeId())
                .statusId(pendingStatusId)
                .active(true)
                .build();

        Contract saved = contractRepository.save(contract);
        HRRequest req = hrRequestService.createForContract(saved.getId(), saved.getEmployeeId(), "CREATE", null);

        List<FileMetadataResponse> documents = uploadFiles(saved.getId(), saved.getEmployeeId(), files);
        return toDetailResponse(saved, req.getId(), documents);
    }

    // ─── Detalle ──────────────────────────────────────────────────────────────

    @Override
    public ContractDetailResponse getById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));
        Long requestId = hrRequestRepository.findTopByContractIdOrderByCreatedAtDesc(id)
                .map(r -> r.getId()).orElse(null);
        List<FileMetadataResponse> documents = fetchDocuments(id);
        return toDetailResponse(contract, requestId, documents);
    }

    // ─── Editar ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ContractDetailResponse updateContract(Long id, UpdateContractRequest req, List<MultipartFile> files) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));

        String proposedData;
        try {
            proposedData = objectMapper.writeValueAsString(req);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando proposedData", e);
        }

        HRRequest hrReq = hrRequestService.createForContract(id, contract.getEmployeeId(), "UPDATE", proposedData);

        Long requestId = hrRequestRepository.findTopByContractIdOrderByCreatedAtDesc(id)
                .map(r -> r.getId()).orElse(hrReq.getId());

        List<FileMetadataResponse> newUploads = uploadFiles(id, contract.getEmployeeId(), files);
        List<FileMetadataResponse> existing = fetchDocuments(id);
        List<FileMetadataResponse> documents = new ArrayList<>(existing);
        documents.addAll(newUploads);
        return toDetailResponse(contract, requestId, documents);
    }

    // ─── Listar ───────────────────────────────────────────────────────────────

    @Override
    public Page<ContractResponse> list(Long employeeId, Long statusId,
                                       LocalDate createdFrom, LocalDate createdTo,
                                       Pageable pageable) {
        Specification<Contract> spec = ContractSpecification.withFilters(employeeId, statusId, createdFrom, createdTo);
        return contractRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    public Map<String, Long> getStats(Long employeeId) {
        Long pendingStatusId = contractStatusRepository.findByName("Pendiente de revisión")
                .map(s -> s.getId()).orElse(-1L);

        long total   = employeeId != null ? contractRepository.countByEmployeeId(employeeId)            : contractRepository.count();
        long active  = employeeId != null ? contractRepository.countByEmployeeIdAndActiveTrue(employeeId) : contractRepository.countByActiveTrue();
        long pending = employeeId != null ? contractRepository.countByEmployeeIdAndStatusId(employeeId, pendingStatusId) : contractRepository.countByStatusId(pendingStatusId);

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("active", active);
        stats.put("pending", pending);
        return stats;
    }

    // ─── Documentos ───────────────────────────────────────────────────────────

    @Override
    public void deleteDocument(Long contractId, Long fileId, Long userId) {
        if (!contractRepository.existsById(contractId)) {
            throw new ResourceNotFoundException("Contrato no encontrado: " + contractId);
        }
        storageClient.delete(fileId, userId);
    }

    private List<FileMetadataResponse> uploadFiles(Long contractId, Long uploadedBy, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        List<FileMetadataResponse> existing = fetchDocuments(contractId);
        if (existing.size() + files.size() > MAX_DOCUMENTS) {
            throw new IllegalArgumentException(
                    "El contrato ya tiene " + existing.size() + " documento(s). " +
                    "Máximo permitido: " + MAX_DOCUMENTS + ".");
        }

        List<FileMetadataResponse> uploaded = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException(
                        "El archivo '" + file.getOriginalFilename() + "' supera el límite de 10MB.");
            }
            if (!ALLOWED_TYPES.contains(file.getContentType())) {
                throw new IllegalArgumentException(
                        "El archivo '" + file.getOriginalFilename() + "' no es un formato permitido. Use PDF, JPG o PNG.");
            }
            ResponseEntity<FileMetadataResponse> response =
                    storageClient.upload(file, uploadedBy, ENTITY_TYPE, contractId, false);
            if (response.getBody() != null) {
                uploaded.add(response.getBody());
            }
        }
        return uploaded;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ContractResponse toResponse(Contract c) {
        String employeeName = employeeRepository.findById(c.getEmployeeId())
                .map(e -> e.getFirstName() + " " + e.getPaternalLastName())
                .orElse(null);

        return ContractResponse.builder()
                .id(c.getId())
                .employeeId(c.getEmployeeId())
                .employeeName(employeeName)
                .name(c.getName())
                .contractNumber(c.getContractNumber())
                .contractType(resolveName(c.getContractTypeId(), contractTypeRepository))
                .contractStatus(resolveName(c.getContractStatusId(), contractStatusRepository))
                .company(resolveName(c.getCompanyId(), companyRepository))
                .jobTitle(resolveName(c.getJobTitleId(), jobTitleRepository))
                .baseSalary(c.getBaseSalary())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .active(c.getActive())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private List<FileMetadataResponse> fetchDocuments(Long contractId) {
        try {
            ResponseEntity<List<FileMetadataResponse>> response =
                    storageClient.listByEntity(ENTITY_TYPE, contractId);
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("No se pudieron obtener documentos del contrato {}: {}", contractId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private ContractDetailResponse toDetailResponse(Contract c, Long requestId, List<FileMetadataResponse> documents) {
        return ContractDetailResponse.builder()
                .id(c.getId())
                .employeeId(c.getEmployeeId())
                .name(c.getName())
                .contractNumber(c.getContractNumber())
                .contractType(resolve(c.getContractTypeId(), contractTypeRepository))
                .contractStatus(resolve(c.getContractStatusId(), contractStatusRepository))
                .safetyGroup(resolve(c.getSafetyGroupId(), safetyGroupRepository))
                .contractDetail(c.getContractDetail())
                .baseSalary(c.getBaseSalary())
                .agreedSalary(c.getAgreedSalary())
                .company(resolve(c.getCompanyId(), companyRepository))
                .zone(resolve(c.getZoneId(), zoneRepository))
                .jobTitle(resolve(c.getJobTitleId(), jobTitleRepository))
                .site(resolve(c.getSiteId(), siteRepository))
                .laborUnion(resolve(c.getLaborUnionId(), laborUnionRepository))
                .weeklyWorkHours(c.getWeeklyWorkHours())
                .workDays(c.getWorkDays())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .mealType(resolve(c.getMealTypeId(), mealTypeRepository))
                .transportType(resolve(c.getTransportTypeId(), transportTypeRepository))
                .status(resolve(c.getStatusId(), employeeStatusRepository))
                .active(c.getActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .requestId(requestId)
                .documents(documents)
                .build();
    }

    private <T> String resolveName(Long id, JpaRepository<T, Long> repo) {
        if (id == null) return null;
        return repo.findById(id).map(e -> {
            try { return (String) e.getClass().getMethod("getName").invoke(e); }
            catch (Exception ex) { return null; }
        }).orElse(null);
    }

    private <T> CatalogItem resolve(Long id, JpaRepository<T, Long> repo) {
        if (id == null) return null;
        return repo.findById(id)
                .map(e -> {
                    try {
                        var getName = e.getClass().getMethod("getName");
                        return new CatalogItem(id, (String) getName.invoke(e));
                    } catch (Exception ex) {
                        return new CatalogItem(id, null);
                    }
                })
                .orElse(null);
    }
}
