package com.mediscan.backend.controller;

import com.mediscan.backend.dto.common.ApiResponse;
import com.mediscan.backend.dto.common.PageResponse;
import com.mediscan.backend.dto.medicine.MedicineRequest;
import com.mediscan.backend.dto.medicine.MedicineResponse;
import com.mediscan.backend.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CRUD and search endpoints for the medicine catalog. Read endpoints require
 * authentication (like the rest of the API); write endpoints are additionally
 * intended for admin/curation use, though role-based restriction beyond
 * "authenticated" can be layered on later without changing this controller.
 */
@Tag(name = "Medicine", description = "Medicine catalog")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @Operation(summary = "List medicines", description = "Returns a paginated list of catalog medicines.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MedicineResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<MedicineResponse> response = medicineService.getAllMedicines(page, size);
        return ResponseEntity.ok(ApiResponse.of("Medicines retrieved successfully", response));
    }

    @Operation(summary = "Get medicine by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponse>> getById(@PathVariable String id) {
        MedicineResponse response = medicineService.getMedicineById(id);
        return ResponseEntity.ok(ApiResponse.of("Medicine retrieved successfully", response));
    }

    @Operation(summary = "Search medicines by name", description = "Case-insensitive partial match search, e.g. ?name=paracetamol")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<MedicineResponse>>> search(
            @Parameter(description = "Medicine name to search for") @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<MedicineResponse> response = medicineService.searchByName(name, page, size);
        return ResponseEntity.ok(ApiResponse.of("Search results retrieved successfully", response));
    }

    @Operation(summary = "Create a medicine catalog entry")
    @PostMapping
    public ResponseEntity<ApiResponse<MedicineResponse>> create(@Valid @RequestBody MedicineRequest request) {
        MedicineResponse response = medicineService.createMedicine(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Medicine created successfully", response));
    }

    @Operation(summary = "Update a medicine catalog entry")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponse>> update(
            @PathVariable String id, @Valid @RequestBody MedicineRequest request) {

        MedicineResponse response = medicineService.updateMedicine(id, request);
        return ResponseEntity.ok(ApiResponse.of("Medicine updated successfully", response));
    }

    @Operation(summary = "Delete a medicine catalog entry")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(ApiResponse.of("Medicine deleted successfully"));
    }
}
