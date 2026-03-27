package com.phishguard.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Deletes the physical file from disk given its full URL.
     * URL format: http://host/uploads/uuid.ext → deletes uploads/uuid.ext
     * Non-fatal: logs a warning if the file is missing or cannot be deleted.
     */
    public void deleteByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            // Extract just the filename from the URL (last path segment)
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                System.out.println("Deleted physical file: " + filePath);
            } else {
                System.out.println("Physical file not found (already removed?): " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Could not delete physical file (non-fatal): " + e.getMessage());
        }
    }
}
