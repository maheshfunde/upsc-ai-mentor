package com.upscmentor.repository;

import com.upscmentor.model.entity.KnowledgeBaseDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeBaseDocumentRepository extends JpaRepository<KnowledgeBaseDocument, Long> {

    List<KnowledgeBaseDocument> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<KnowledgeBaseDocument> findByUserIdAndStatus(Long userId, String status);

    boolean existsByUserIdAndFilename(Long userId, String filename);

    @Query("SELECT COUNT(d) FROM KnowledgeBaseDocument d WHERE d.userId = :userId AND d.status = 'READY'")
    long countReadyDocuments(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(d.chunkCount), 0) FROM KnowledgeBaseDocument d WHERE d.userId = :userId AND d.status = 'READY'")
    long totalChunksForUser(@Param("userId") Long userId);
}
