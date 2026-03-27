package com.phishguard.controller;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.phishguard.config.AdminGuard;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Autowired
    private AdminGuard adminGuard;

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "application/pdf",
        "text/plain",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    // POST /api/files/upload — ADMIN ONLY
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestParam("file") MultipartFile file) {

        if (!adminGuard.isAdmin(userIdHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("error",
                "File type not allowed. Supported: PDF, TXT, DOCX, PPTX, XLSX"));
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = baseUrl + "/uploads/" + fileName;
            return ResponseEntity.ok(Map.of(
                "fileUrl",   fileUrl,
                "fileName",  originalName != null ? originalName : fileName,
                "fileSize",  String.valueOf(file.getSize())
            ));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to store file: " + e.getMessage()));
        }
    }
}
