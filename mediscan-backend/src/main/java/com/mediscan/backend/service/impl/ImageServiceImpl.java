package com.mediscan.backend.service.impl;

import com.mediscan.backend.exception.BadRequestException;
import com.mediscan.backend.service.ImageService;
import com.mediscan.backend.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    private final Path uploadRoot;

    public ImageServiceImpl(
            @Value("${file.upload-dir}") String uploadDir) {

        uploadRoot = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        System.out.println("========================================");
        System.out.println("IMAGE UPLOAD ROOT:");
        System.out.println(uploadRoot);
        System.out.println("========================================");

        try {
            Files.createDirectories(uploadRoot);

            System.out.println("UPLOAD DIRECTORY CREATED/EXISTS:");
            System.out.println(Files.exists(uploadRoot));

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not initialize upload directory: "
                            + uploadRoot,
                    e
            );
        }
    }

    @Override
    public String storeImage(MultipartFile file) {

        System.out.println("========== STORE IMAGE ==========");

        FileUtil.validateImage(file);

        System.out.println("Filename: " + file.getOriginalFilename());
        System.out.println("Size: " + file.getSize());
        System.out.println("Type: " + file.getContentType());

        String datedSubdir = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        Path targetDir = uploadRoot
                .resolve(datedSubdir)
                .normalize();

        try {

            Files.createDirectories(targetDir);

            String safeFilename =
                    FileUtil.generateSafeFilename(file);

            Path targetFile = targetDir
                    .resolve(safeFilename)
                    .normalize();

            System.out.println("TARGET DIRECTORY:");
            System.out.println(targetDir);

            System.out.println("TARGET FILE:");
            System.out.println(targetFile);

            file.transferTo(targetFile);

            System.out.println("========== IMAGE SAVED ==========");
            System.out.println("Exists: " + Files.exists(targetFile));
            System.out.println("Absolute path: " +
                    targetFile.toAbsolutePath());

            if (!Files.exists(targetFile)) {
                throw new BadRequestException(
                        "Image was not created: " + targetFile
                );
            }

            String relativePath = uploadRoot
                    .relativize(targetFile)
                    .toString()
                    .replace(File.separatorChar, '/');

            log.info(
                    "Stored uploaded image at {}",
                    relativePath
            );

            return relativePath;

        } catch (IOException e) {

            log.error(
                    "Failed to store uploaded image",
                    e
            );

            throw new BadRequestException(
                    "Failed to store uploaded image"
            );
        }
    }

    @Override
    public File resolveImageFile(String relativePath) {

        Path resolved = uploadRoot
                .resolve(relativePath)
                .normalize();

        if (!resolved.startsWith(uploadRoot)) {
            throw new BadRequestException(
                    "Invalid image path"
            );
        }

        return resolved.toFile();
    }
}