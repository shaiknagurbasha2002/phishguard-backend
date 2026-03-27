package com.phishguard.dto;

import java.util.List;
import java.util.stream.Collectors;
import com.phishguard.model.Training;

/**
 * Safe DTO for Training — converts entity + EAGER attachments into plain data.
 * No Hibernate proxies, no bidirectional references — Jackson serializes cleanly.
 */
public class TrainingResponse {

    public Long id;
    public String title;
    public String description;
    public int progress;
    public String fileUrl;
    public List<AttachmentResponse> attachments;

    public static TrainingResponse from(Training t) {
        TrainingResponse r = new TrainingResponse();
        r.id          = t.getId();
        r.title       = t.getTitle();
        r.description = t.getDescription();
        r.progress    = t.getProgress();
        r.fileUrl     = t.getFileUrl();
        r.attachments = t.getAttachments().stream()
                         .map(AttachmentResponse::from)
                         .collect(Collectors.toList());
        return r;
    }
}
