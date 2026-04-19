package com.phishguard.controller;

import com.phishguard.config.AdminGuard;
import com.phishguard.model.Training;
import com.phishguard.repository.TrainingAttachmentRepository;
import com.phishguard.repository.TrainingRepository;
import com.phishguard.service.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TrainingAttachmentRepository attachmentRepository;

    @Autowired
    private AdminGuard adminGuard;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String UPLOAD_DIR = "uploads/trainings/";

    @GetMapping
    public List<Training> getAllTrainings() {
        return trainingRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createTraining(@RequestBody Training training) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        Training saved = trainingRepository.save(training);
        notificationService.notifyAllUsers(
            "New Training Module Available!",
            "New Training: " + saved.getTitle(),
            "A new training module <b>" + saved.getTitle() +
            "</b> has been added. Login to PhishGuard to start learning!"
        );
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadTraining(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        try {
            Training training = new Training();
            training.setTitle(title);
            training.setDescription(description);
            if (content != null) training.setContent(content);

            if (file != null && !file.isEmpty()) {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                training.setFileName(fileName);
                training.setFileUrl(baseUrl + "/api/trainings/files/" + fileName);
            }

            Training saved = trainingRepository.save(training);

            notificationService.notifyAllUsers(
                "New Training Module Available!",
                "New Training: " + saved.getTitle(),
                "A new training module <b>" + saved.getTitle() +
                "</b> has been added. Login to PhishGuard to start learning!"
            );

            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", "File upload failed: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(@PathVariable Long id,
                                            @RequestBody Map<String, Object> body) {
        return trainingRepository.findById(id).map(training -> {
            Object progressVal = body.get("progress");
            if (progressVal != null) {
                int progress = Math.min(100, Math.max(0, ((Number) progressVal).intValue()));
                training.setProgress(progress);
                trainingRepository.save(training);
            }
            return ResponseEntity.ok(training);
        }).orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTraining(@PathVariable Long id) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        if (trainingRepository.existsById(id)) {
            // Delete attachments first to avoid FK constraint violation
            attachmentRepository.deleteAllByTrainingId(id);
            trainingRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Training deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }

    // Public endpoint — no auth needed — browser can open directly
    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}