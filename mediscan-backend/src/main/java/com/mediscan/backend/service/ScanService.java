package com.mediscan.backend.service;

import com.mediscan.backend.dto.common.PageResponse;
import com.mediscan.backend.dto.scan.ScanResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ScanService {

    ScanResponse processScan(String userId, MultipartFile file, String note);

    PageResponse<ScanResponse> getScanHistory(String userId, int page, int size);

    ScanResponse getScanById(String userId, String scanId);

    void deleteScan(String userId, String scanId);
}
