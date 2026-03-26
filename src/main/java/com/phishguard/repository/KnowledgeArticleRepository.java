package com.phishguard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.phishguard.model.KnowledgeArticle;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
}