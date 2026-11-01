package com.mediscan.backend.service;

import com.mediscan.backend.service.impl.AiServiceImpl;

public interface AiService {

    /**
     * Sends OCR-extracted text to the AI/LLM and returns structured medicine information.
     * Never invents data: fields the model cannot confidently determine come back
     * as null/"Unknown" rather than hallucinated values.
     */
    AiServiceImpl.MedicineExtractionResult identifyMedicine(String extractedText);
}
