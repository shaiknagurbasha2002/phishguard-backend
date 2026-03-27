package com.phishguard.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private int progress;

    // Legacy single-file field — kept for backward compatibility
    private String fileUrl;

    @OneToMany(mappedBy = "training", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("uploadedAt DESC")
    private List<TrainingAttachment> attachments = new ArrayList<>();

    public Training() {}

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public List<TrainingAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<TrainingAttachment> attachments) { this.attachments = attachments; }
}
