package com.phishguard.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phishguard.config.AdminGuard;
import com.phishguard.model.KnowledgeArticle;
import com.phishguard.repository.KnowledgeArticleRepository;
import com.phishguard.service.NotificationService;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeArticleController {

    @Autowired private KnowledgeArticleRepository knowledgeArticleRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private AdminGuard adminGuard;

    // GET /api/knowledge — public
    @GetMapping
    public List<KnowledgeArticle> getAllArticles() {
        return knowledgeArticleRepository.findAll();
    }

    // GET /api/knowledge/{id} — public
    @GetMapping("/{id}")
    public KnowledgeArticle getArticleById(@PathVariable Long id) {
        return knowledgeArticleRepository.findById(id).orElse(null);
    }

    // POST /api/knowledge — ADMIN ONLY
    @PostMapping
    public ResponseEntity<?> createArticle(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody KnowledgeArticle article) {

        if (!adminGuard.isAdmin(userIdHeader)) return adminGuard.forbidden();

        if (article.getPublishedAt() == null) {
            article.setPublishedAt(java.time.LocalDateTime.now());
        }
        KnowledgeArticle saved = knowledgeArticleRepository.save(article);
        try {
            notificationService.createGlobal(
                "New Article Published",
                "New article published: " + saved.getTitle(),
                "article",
                "/dashboard/knowledge/" + saved.getId()
            );
        } catch (Exception e) {
            System.err.println("Notification creation failed (non-fatal): " + e.getMessage());
        }
        return ResponseEntity.ok(saved);
    }

    // DELETE /api/knowledge/{id} — ADMIN ONLY
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long id) {

        if (!adminGuard.isAdmin(userIdHeader)) return adminGuard.forbidden();

        if (!knowledgeArticleRepository.existsById(id)) return ResponseEntity.notFound().build();
        knowledgeArticleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/knowledge/{id}/file — ADMIN ONLY
    @PatchMapping("/{id}/file")
    public ResponseEntity<?> attachFile(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        if (!adminGuard.isAdmin(userIdHeader)) return adminGuard.forbidden();

        return knowledgeArticleRepository.findById(id).map(a -> {
            a.setFileUrl(body.get("fileUrl"));
            return ResponseEntity.ok(knowledgeArticleRepository.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }
}
