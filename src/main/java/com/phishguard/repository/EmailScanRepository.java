package com.phishguard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.phishguard.model.EmailScan;

public interface EmailScanRepository extends JpaRepository<EmailScan, Long> {
    List<EmailScan> findByUserId(Long userId);
}