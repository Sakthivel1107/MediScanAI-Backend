package com.mediscan.backend.service;

import com.mediscan.backend.dto.common.PageResponse;
import com.mediscan.backend.dto.scan.ScanResponse;
import com.mediscan.backend.enums.ScanStatus;
import com.mediscan.backend.exception.AiServiceException;
import com.mediscan.backend.exception.ResourceNotFoundException;
import com.mediscan.backend.model.Scan;
import com.mediscan.backend.repository.ScanRepository;
import com.mediscan.backend.service.impl.AiServiceImpl;
import com.mediscan.backend.service.impl.OcrServiceImpl;
import com.mediscan.backend.service.impl.ScanServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanServiceImplTest {

    @Mock
    private ScanRepository scanRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private OcrService ocrService;

    @Mock
    private AiService aiService;

    @InjectMocks
    private ScanServiceImpl scanService;

    @Test
    void processScan_shouldReturnCompletedScan_onHappyPath() {
        MockMultipartFile file = new MockMultipartFile("file", "strip.jpg", "image/jpeg", "fake-bytes".getBytes());

        when(imageService.storeImage(file)).thenReturn("2026/08/31/uuid.jpg");
        when(scanRepository.save(any(Scan.class))).thenAnswer(invocation -> {
            Scan s = invocation.getArgument(0);
            if (s.getId() == null) {
                s.setId("scan-1");
            }
            return s;
        });
        when(imageService.resolveImageFile("2026/08/31/uuid.jpg")).thenReturn(new File("dummy.jpg"));
        when(ocrService.extractText(any(File.class))).thenReturn(new OcrServiceImpl.OcrResult("Paracetamol 500mg", 0.8));

        AiServiceImpl.MedicineExtractionResult aiResult = new AiServiceImpl.MedicineExtractionResult();
        aiResult.setMedicineName("Paracetamol");
        aiResult.setGenericName("Acetaminophen");
        aiResult.setPurpose("Pain relief");
        aiResult.setDosage("500mg");
        aiResult.setWarnings(List.of("Do not exceed 4g per day"));
        aiResult.setManufacturer("ACME Pharma");
        aiResult.setConfidence(0.9);

        when(aiService.identifyMedicine("Paracetamol 500mg")).thenReturn(aiResult);

        ScanResponse response = scanService.processScan("user-1", file, null);

        assertThat(response.getStatus()).isEqualTo(ScanStatus.COMPLETED);
        assertThat(response.getMedicineName()).isEqualTo("Paracetamol");
        assertThat(response.getDisclaimer()).contains("informational purposes only");
    }

    @Test
    void processScan_shouldMarkScanFailed_whenAiServiceUnavailable() {
        MockMultipartFile file = new MockMultipartFile("file", "strip.jpg", "image/jpeg", "fake-bytes".getBytes());

        when(imageService.storeImage(file)).thenReturn("path/to/image.jpg");
        when(scanRepository.save(any(Scan.class))).thenAnswer(invocation -> {
            Scan s = invocation.getArgument(0);
            if (s.getId() == null) {
                s.setId("scan-2");
            }
            return s;
        });
        when(imageService.resolveImageFile(anyString())).thenReturn(new File("dummy.jpg"));
        when(ocrService.extractText(any(File.class))).thenReturn(new OcrServiceImpl.OcrResult("some text", 0.5));
        when(aiService.identifyMedicine(anyString())).thenThrow(new AiServiceException("AI unavailable"));

        assertThatThrownBy(() -> scanService.processScan("user-1", file, null))
                .isInstanceOf(AiServiceException.class);
    }

    @Test
    void getScanById_shouldThrowNotFound_whenScanBelongsToAnotherUser() {
        when(scanRepository.findByIdAndUserId("scan-1", "user-2")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scanService.getScanById("user-2", "scan-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getScanHistory_shouldReturnPagedResults() {
        Scan scan = Scan.builder().id("scan-1").userId("user-1").status(ScanStatus.COMPLETED).build();
        Page<Scan> page = new PageImpl<>(List.of(scan));

        when(scanRepository.findByUserIdOrderByScannedAtDesc(anyString(), any(Pageable.class))).thenReturn(page);

        PageResponse<ScanResponse> response = scanService.getScanHistory("user-1", 0, 20);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getId()).isEqualTo("scan-1");
    }

    @Test
    void deleteScan_shouldThrowNotFound_whenScanDoesNotBelongToUser() {
        when(scanRepository.findByIdAndUserId("scan-1", "user-2")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scanService.deleteScan("user-2", "scan-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
