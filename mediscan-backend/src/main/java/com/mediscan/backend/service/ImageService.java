package com.mediscan.backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface ImageService {

    /**
     * Validates and persists an uploaded image to the configured upload directory,
     * returning the relative path under which it was stored.
     */
    String storeImage(MultipartFile file);

    /**
     * Resolves a previously stored relative path to an absolute {@link File} handle.
     */
    File resolveImageFile(String relativePath);
}
