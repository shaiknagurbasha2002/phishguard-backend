package com.phishguard.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.phishguard.model.TrainingAttachment;

public interface TrainingAttachmentRepository extends JpaRepository<TrainingAttachment, Long> {
    List<TrainingAttachment> findByTrainingIdOrderByUploadedAtDesc(Long trainingId);
}
