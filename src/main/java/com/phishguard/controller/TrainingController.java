package com.phishguard.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.phishguard.config.AdminGuard;
import com.phishguard.dto.AttachmentResponse;
import com.phishguard.dto.TrainingResponse;
import com.phishguard.model.Training;
import com.phishguard.model.TrainingAttachment;
import com.phishguard.repository.TrainingRepository;
import com.phishguard.repository.TrainingAttachmentRepository;
import com.phishguard.service.FileStorageService;
import com.phishguard.service.NotificationService;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    @Autowired private TrainingRepository trainingRepository;
    @Autowired private TrainingAttachmentRepository attachmentRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private AdminGuard adminGuard;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/training — list all modules with their attachments (public)
    // Returns List<TrainingResponse> — safe DTOs, no Hibernate proxies
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    @Transactional(readOnly = true)
    public List<TrainingResponse> getAllTraining() {
        return trainingRepository.findAll()
                .stream()
                .map(TrainingResponse::from)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/training — add new module (ADMIN ONLY)
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping
    @Transactional
    public ResponseEntity<TrainingResponse> addTraining(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody Training training) {

        if (!adminGuard.isAdmin(userIdHeader)) return adminGuard.forbidden();

        Training saved = trainingRepository.save(training);
        try {
            notificationService.createGlobal(
                "New Training Module",
                "New training module added: " + saved.getTitle(),
                "training",
                "/dashboard/training"
            );
        } catch (Exception e) {
            System.err.println("Notification creation failed (non-fatal): " + e.getMessage());
        }
        return ResponseEntity.ok(TrainingResponse.from(saved));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/training/{id}/attachments — add one attachment (ADMIN ONLY)
    // Returns the updated TrainingResponse so the frontend refreshes its list
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/{id}/attachments")
    @Transactional
    public ResponseEntity<TrainingResponse> addAttachment(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        if (!adminGuard.isAdmin(userIdHeader)) return adminGuard.forbidden();

        Optional<Training> trainingOpt = trainingRepository.findById(id);
        if (trainingOpt.isEmpty()) return ResponseEntity.notFound().build();

        TrainingAttachment att = new TrainingAttachment();
        att.setTraining(trainingOpt.get());
        att.setFileName(body.getOrDefault("fileName", "file"));
        att.setFileUrl(body.get("fileUrl"));
        String sizeStr = body.get("fileSize");
        if (sizeStr != null && !sizeStr.isEmpty()) {
            try { att.setFileSize(Long.parseLong(sizeStr)); }
            catch (NumberFormatException ignored) {}
        }
        att.setUploadedAt(LocalDateTime.now());
        attachmentRepository.save(att);

        // Re-fetch so the returned Training includes the newly added attachment
        Training updated = trainingRepository.findById(id).orElse(trainingOpt.get());
        return ResponseEntity.ok(TrainingResponse.from(updated));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/training/{id}/attachments — list attachments for one module (public)
    // Returns List<AttachmentResponse> — no Training back-reference
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}/attachments")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AttachmentResponse>> getAttachments(@PathVariable Long id) {
        if (!trainingRepository.existsById(id)) return ResponseEntity.notFound().build();
        List<AttachmentResponse> list = attachmentRepository
                .findByTrainingIdOrderByUploadedAtDesc(id)
                .stream()
                .map(AttachmentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/training/{id}/attachments/{attachmentId} (ADMIN ONLY)
    // Checks ownership via single DB query — never loads the Training entity.
    // DB record removed first, then physical file deleted from disk.
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}/attachments/{attachmentId}")
    @Transactional
    public ResponseEntity<Void> deleteAttachment(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long id,
            @PathVariable Long attachmentId) {

        if (!adminGuard.isAdmin(userIdHeader)) return adminGuard.forbidden();

        Optional<TrainingAttachment> opt =
                attachmentRepository.findByIdAndTraining_Id(attachmentId, id);
        if (opt.isEmpty()) return ResponseEntity.notFound().<Void>build();

        String fileUrl = opt.get().getFileUrl();

        // Use JPQL bulk-delete by ID — avoids Spring Data's findById+remove cycle which
        // triggers Hibernate orphanRemoval cascade on the parent Training, causing a DB hang.
        attachmentRepository.deleteByAttachmentId(attachmentId); // DB first
        fileStorageService.deleteByUrl(fileUrl);                  // disk after

        return ResponseEntity.noContent().<Void>build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/training/{id} — delete module + all attachments (ADMIN ONLY)
    // Uses JPQL bulk-delete for attachments — no Hibernate cascade confusion.
    // Physical files deleted AFTER DB operations complete.
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteTraining(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long id) {

        if (!adminGuard.isAdmin(userIdHeader)) return adminGuard.forbidden();

        if (!trainingRepository.existsById(id)) return ResponseEntity.notFound().<Void>build();

        // Step 1: Collect file URLs before any DB changes
        List<String> fileUrls = attachmentRepository
                .findByTrainingIdOrderByUploadedAtDesc(id)
                .stream()
                .map(TrainingAttachment::getFileUrl)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toList());

        // Step 2: Bulk-delete attachment rows (JPQL, clears 1st-level cache)
        attachmentRepository.deleteAllByTrainingId(id);

        // Step 3: Delete training row (children already gone — no cascade confusion)
        trainingRepository.deleteById(id);

        // Step 4: Delete physical files after DB success
        fileUrls.forEach(fileStorageService::deleteByUrl);

        return ResponseEntity.noContent().<Void>build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/training/{id}/file — legacy single-file (ADMIN ONLY)
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/file")
    @Transactional
    public ResponseEntity<TrainingResponse> attachFile(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        if (!adminGuard.isAdmin(userIdHeader)) return adminGuard.forbidden();

        Optional<Training> opt = trainingRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        opt.get().setFileUrl(body.get("fileUrl"));
        Training saved = trainingRepository.save(opt.get());
        return ResponseEntity.ok(TrainingResponse.from(saved));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/training/{id}/progress — update progress (any user)
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}/progress")
    @Transactional
    public ResponseEntity<TrainingResponse> updateProgress(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {

        Optional<Training> optional = trainingRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Training training = optional.get();
        int progress = Math.min(100, Math.max(0, body.getOrDefault("progress", 0)));
        training.setProgress(progress);
        trainingRepository.save(training);
        return ResponseEntity.ok(TrainingResponse.from(training));
    }
}
