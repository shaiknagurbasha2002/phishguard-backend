package com.phishguard.dto;

import java.time.LocalDateTime;
import com.phishguard.model.TrainingAttachment;

/**
 * Safe DTO for TrainingAttachment — no entity references, no Hibernate proxies.
 * Prevents the circular JSON serialization loop:
 *   TrainingAttachment.getTraining() → Training.getAttachments() → infinite
 */
public class AttachmentResponse {

    public Long id;
    public String fileName;
    public String fileUrl;
    public Long fileSize;
    public LocalDateTime uploadedAt;

    public static AttachmentResponse from(TrainingAttachment a) {
        AttachmentResponse r = new AttachmentResponse();
        r.id          = a.getId();
        r.fileName    = a.getFileName();
        r.fileUrl     = a.getFileUrl();
        r.fileSize    = a.getFileSize();
        r.uploadedAt  = a.getUploadedAt();
        return r;
    }
}
