package com.phishguard.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phishguard.model.Training;
import com.phishguard.model.TrainingAttachment;
import com.phishguard.repository.TrainingRepository;
import com.phishguard.repository.TrainingAttachmentRepository;
import com.phishguard.service.NotificationService;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingAttachmentRepository attachmentRepository;

    @Autowired
    private NotificationService notificationService;

    // GET /api/training — list all modules (includes attachments via @OneToMany EAGER)
    @GetMapping
    public List<Training> getAllTraining() {
        return trainingRepository.findAll();
    }

    // POST /api/training — add new module
    @PostMapping
    public Training addTraining(@RequestBody Training training) {
        Training saved = trainingRepository.save(training);
        try {
            String msg = "New training module added: " + saved.getTitle();
            notificationService.createGlobal(msg, "training", "/dashboard/training");
        } catch (Exception e) {
            System.err.println("Notification creation failed (non-fatal): " + e.getMessage());
        }
        return saved;
    }

    // POST /api/training/{id}/attachments — add one attachment record
    @PostMapping("/{id}/attachments")
    public ResponseEntity<?> addAttachment(@PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        return trainingRepository.findById(id).map(training -> {
            TrainingAttachment att = new TrainingAttachment();
            att.setTraining(training);
            att.setFileName(body.getOrDefault("fileName", "file"));
            att.setFileUrl(body.get("fileUrl"));
            String sizeStr = body.get("fileSize");
            if (sizeStr != null && !sizeStr.isEmpty()) {
                try { att.setFileSize(Long.parseLong(sizeStr)); }
                catch (NumberFormatException ignored) {}
            }
            att.setUploadedAt(LocalDateTime.now());
            attachmentRepository.save(att);
            // Return the full updated Training (EAGER fetch includes new attachment)
            return ResponseEntity.ok(trainingRepository.findById(id).orElse(training));
        }).orElse(ResponseEntity.notFound().build());
    }

    // GET /api/training/{id}/attachments — list attachments for a module
    @GetMapping("/{id}/attachments")
    public ResponseEntity<?> getAttachments(@PathVariable Long id) {
        if (!trainingRepository.existsById(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(attachmentRepository.findByTrainingIdOrderByUploadedAtDesc(id));
    }

    // DELETE /api/training/{id}/attachments/{attachmentId} — remove one attachment
    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id,
                                                  @PathVariable Long attachmentId) {
        return attachmentRepository.findById(attachmentId).map(att -> {
            if (!att.getTraining().getId().equals(id)) {
                return ResponseEntity.notFound().<Void>build();
            }
            attachmentRepository.delete(att);
            return ResponseEntity.<Void>noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // PATCH /api/training/{id}/file — legacy single-file endpoint (kept for compat)
    @PatchMapping("/{id}/file")
    public ResponseEntity<?> attachFile(@PathVariable Long id,
                                        @RequestBody Map<String, String> body) {
        return trainingRepository.findById(id).map(t -> {
            t.setFileUrl(body.get("fileUrl"));
            return ResponseEntity.ok(trainingRepository.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/training/{id}/progress — update progress (0-100)
    @PutMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(@PathVariable Long id,
                                            @RequestBody Map<String, Integer> body) {
        Optional<Training> optional = trainingRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Training training = optional.get();
        int progress = body.getOrDefault("progress", 0);
        progress = Math.min(100, Math.max(0, progress));
        training.setProgress(progress);
        trainingRepository.save(training);
        return ResponseEntity.ok(training);
    }

    // DELETE /api/training/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable Long id) {
        if (!trainingRepository.existsById(id)) return ResponseEntity.notFound().build();
        trainingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
