package com.phishguard.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phishguard.model.KnowledgeArticle;
import com.phishguard.repository.KnowledgeArticleRepository;
import com.phishguard.service.NotificationService;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeArticleController {

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<KnowledgeArticle> getAllArticles() {
        return knowledgeArticleRepository.findAll();
    }

    @GetMapping("/{id}")
    public KnowledgeArticle getArticleById(@PathVariable Long id) {
        return knowledgeArticleRepository.findById(id).orElse(null);
    }

    @PostMapping
    public KnowledgeArticle createArticle(@RequestBody KnowledgeArticle article) {
        if (article.getPublishedAt() == null) {
            article.setPublishedAt(java.time.LocalDateTime.now());
        }
        KnowledgeArticle saved = knowledgeArticleRepository.save(article);

        // Fire a global notification so all users see it in their bell
        try {
            String msg = "New article published: " + saved.getTitle();
            notificationService.createGlobal(msg, "article", "/dashboard/knowledge/" + saved.getId());
        } catch (Exception e) {
            System.err.println("Notification creation failed (non-fatal): " + e.getMessage());
        }

        return saved;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        if (!knowledgeArticleRepository.existsById(id)) return ResponseEntity.notFound().build();
        knowledgeArticleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/knowledge/{id}/file — attach a file URL to an article
    @PatchMapping("/{id}/file")
    public ResponseEntity<?> attachFile(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        return knowledgeArticleRepository.findById(id).map(a -> {
            a.setFileUrl(body.get("fileUrl"));
            return ResponseEntity.ok(knowledgeArticleRepository.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }
}
