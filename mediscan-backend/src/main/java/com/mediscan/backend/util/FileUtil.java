package com.mediscan.backend.util;

import com.mediscan.backend.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Helpers for safely handling uploaded image files.
 *
 * <p>Never trusts the original filename supplied by the client and never allows
 * path traversal; a fresh random filename is always generated.
 */
public final class FileUtil {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private FileUtil() {
    }

    /**
     * Validates that the uploaded file is a non-empty image of an allowed type.
     *
     * @throws BadRequestException if the file is missing, empty, or not an allowed image type
     */
    public static void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No image file was provided");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException(
                    "Invalid image file. Allowed types are: " + String.join(", ", ALLOWED_CONTENT_TYPES));
        }
    }

    /**
     * Generates a safe, collision-resistant filename for storage, preserving only
     * a whitelisted file extension derived from the content type (never the
     * client-supplied filename).
     */
    public static String generateSafeFilename(MultipartFile file) {
        String extension = extensionFromContentType(file.getContentType());
        return UUID.randomUUID() + "." + extension;
    }

    private static String extensionFromContentType(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    public static List<String> allowedExtensions() {
        return List.copyOf(ALLOWED_EXTENSIONS);
    }
}
