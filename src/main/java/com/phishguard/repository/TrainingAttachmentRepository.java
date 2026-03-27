package com.phishguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.phishguard.model.TrainingAttachment;

public interface TrainingAttachmentRepository extends JpaRepository<TrainingAttachment, Long> {

    // Used by getAttachments endpoint and deleteTraining to collect fileUrls
    List<TrainingAttachment> findByTrainingIdOrderByUploadedAtDesc(Long trainingId);

    // Used by deleteAttachment to check ownership without loading the Training entity
    Optional<TrainingAttachment> findByIdAndTraining_Id(Long id, Long trainingId);

    // Bulk delete all attachments for a module via JPQL — avoids Hibernate cascade confusion
    // clearAutomatically=true flushes the 1st-level cache so subsequent queries see fresh state
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM TrainingAttachment a WHERE a.training.id = :trainingId")
    void deleteAllByTrainingId(@Param("trainingId") Long trainingId);

    // Single attachment JPQL delete — bypasses Spring Data's findById+remove cycle
    // which triggers Hibernate orphanRemoval cascade and causes a DB hang
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM TrainingAttachment a WHERE a.id = :id")
    void deleteByAttachmentId(@Param("id") Long id);
}
