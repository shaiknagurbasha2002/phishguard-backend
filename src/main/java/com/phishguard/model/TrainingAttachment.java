package com.phishguard.model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "training_attachment")
public class TrainingAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id", nullable = false)
    @JsonIgnore
    private Training training;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 1024)
    private String fileUrl;

    private Long fileSize;  // bytes

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    public TrainingAttachment() {}

    public Long getId() { return id; }

    public Training getTraining() { return training; }
    public void setTraining(Training training) { this.training = training; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
