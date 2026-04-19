package com.phishguard.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.phishguard.config.AdminGuard;
import com.phishguard.model.KnowledgeArticle;
import com.phishguard.repository.KnowledgeArticleRepository;
import com.phishguard.service.NotificationService;

import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeArticleController {

    @Autowired private KnowledgeArticleRepository knowledgeArticleRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private AdminGuard adminGuard;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String UPLOAD_DIR = "uploads/knowledge/";

    @GetMapping
    public List<KnowledgeArticle> getAllArticles() {
        return knowledgeArticleRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeArticle> getArticleById(@PathVariable Long id) {
        return knowledgeArticleRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createArticle(@RequestBody KnowledgeArticle article) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        if (article.getPublishedAt() == null) {
            article.setPublishedAt(java.time.LocalDateTime.now());
        }
        KnowledgeArticle saved = knowledgeArticleRepository.save(article);
        try {
            notificationService.notifyAllUsers(
                "New Knowledge Article!",
                "New Article: " + saved.getTitle(),
                "A new knowledge article <b>" + saved.getTitle() + "</b> has been published. Login to read it!"
            );
        } catch (Exception e) {
            System.err.println("Notification failed (non-fatal): " + e.getMessage());
        }
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadArticle(
            @RequestParam("title") String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        if (!adminGuard.isAdmin()) return adminGuard.forbidden();

        try {
            KnowledgeArticle article = new KnowledgeArticle();
            article.setTitle(title);
            if (content != null) article.setContent(content);
            article.setPublishedAt(java.time.LocalDateTime.now());

            if (file != null && !file.isEmpty()) {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), uploadPath.resolve(fileName),
                        StandardCopyOption.REPLACE_EXISTING);
                article.setFileUrl(baseUrl + "/api/knowledge/files/" + fileName);
            }

            KnowledgeArticle saved = knowledgeArticleRepository.save(article);
            notificationService.notifyAllUsers(
                "New Knowledge Article!",
                "New Article: " + saved.getTitle(),
                "A new knowledge article <b>" + saved.getTitle() + "</b> has been published!"
            );
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
            org.springframework.core.io.Resource resource =
                    new org.springframework.core.io.FileSystemResource(filePath);
            if (!resource.exists()) return ResponseEntity.notFound().build();
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .header("Content-Disposition", "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        if (!knowledgeArticleRepository.existsById(id)) return ResponseEntity.notFound().build();
        knowledgeArticleRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Article deleted"));
    }
}
