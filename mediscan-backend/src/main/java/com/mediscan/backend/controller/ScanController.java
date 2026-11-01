package com.mediscan.backend.controller;

import com.mediscan.backend.dto.common.ApiResponse;
import com.mediscan.backend.dto.common.PageResponse;
import com.mediscan.backend.dto.scan.ScanResponse;
import com.mediscan.backend.security.CustomUserDetails;
import com.mediscan.backend.service.ScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles the medicine-strip scan pipeline (upload -> OCR -> AI) plus scan history.
 * All endpoints require authentication; users may only access their own scans.
 */
@Tag(name = "Scan", description = "Medicine strip scanning and scan history")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/scans")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;

    @Operation(
            summary = "Scan a medicine strip image",
            description = "Uploads a medicine strip image (multipart/form-data, field name 'file'), "
                    + "runs OCR and AI identification, and returns structured medicine information.",
            requestBody = @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ScanResponse>> createScan(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "note", required = false) String note) {

        ScanResponse response = scanService.processScan(principal.getId(), file, note);
        System.out.println(response);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Scan completed successfully", response));
    }

    @Operation(summary = "Get scan history", description = "Returns the authenticated user's scan history, paginated.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ScanResponse>>> getScanHistory(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        PageResponse<ScanResponse> response = scanService.getScanHistory(principal.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.of("Scan history retrieved successfully", response));
    }

    @Operation(summary = "Get a scan by id", description = "Returns a single scan belonging to the authenticated user.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScanResponse>> getScan(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String id) {

        ScanResponse response = scanService.getScanById(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.of("Scan retrieved successfully", response));
    }

    @Operation(summary = "Delete a scan", description = "Deletes a scan belonging to the authenticated user.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScan(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String id) {

        scanService.deleteScan(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.of("Scan deleted successfully"));
    }
}
