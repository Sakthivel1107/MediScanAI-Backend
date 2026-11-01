package com.mediscan.backend.service;

import com.mediscan.backend.service.impl.OcrServiceImpl;

import java.io.File;

public interface OcrService {

    /**
     * Runs OCR text extraction over the given image file.
     */
    OcrServiceImpl.OcrResult extractText(File imageFile);
}
