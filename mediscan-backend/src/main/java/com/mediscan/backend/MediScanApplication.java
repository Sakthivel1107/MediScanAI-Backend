package com.mediscan.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the MediScan AI backend.
 *
 * <p>MediScan AI is an information-assistance application: it extracts and presents
 * medicine details from strip/package images. It does not provide medical diagnoses
 * or prescriptions. See {@code ScanResponse.disclaimer} for the disclaimer surfaced
 * on every scan result.
 */
@SpringBootApplication
public class MediScanApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediScanApplication.class, args);
    }
}
