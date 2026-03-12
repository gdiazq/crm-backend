package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.ContractDetailResponse;
import com.crm.mcsv_rrhh.dto.ContractResponse;
import com.crm.mcsv_rrhh.dto.CreateContractRequest;
import com.crm.mcsv_rrhh.dto.UpdateContractRequest;
import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/contract")
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "Gestión de contratos laborales")
public class ContractController {

    private final ContractService contractService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "identification", "firstName",
            "name", "companyId", "contractTypeId", "contractStatusId", "statusId", "startDate", "endDate", "createdAt"
    );

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear contrato para un empleado (con documentos opcionales, máx. 5, 10MB c/u)")
    public ResponseEntity<ContractDetailResponse> create(
            @RequestPart("data") @Valid CreateContractRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(contractService.createContract(request, files));
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Editar contrato (genera solicitud de aprobación, con documentos opcionales)")
    public ResponseEntity<ContractDetailResponse> update(
            @RequestPart("data") @Valid UpdateContractRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(contractService.updateContract(request.getId(), request, files));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "Obtener detalle de contrato por ID")
    public ResponseEntity<ContractDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getById(id));
    }

    @DeleteMapping("/{id}/documents/{fileId}")
    @Operation(summary = "Eliminar documento adjunto del contrato")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @PathVariable Long fileId,
            @RequestParam("userId") Long userId) {
        contractService.deleteDocument(id, fileId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/paged")
    @Operation(summary = "Listar contratos (paginado)")
    public ResponseEntity<PagedResponse<ContractResponse>> paged(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContractResponse> result = contractService.list(employeeId, statusId, createdFrom, createdTo, pageable, safeSortBy, sortDir);
        Map<String, Long> stats = contractService.getStats(employeeId);

        return ResponseEntity.ok(PagedResponse.of(result, stats.get("total"), stats.get("active"), stats.get("pending")));
    }
}
