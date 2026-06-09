package com.upscmentor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.upscmentor.model.entity.KnowledgeBaseDocument;
import com.upscmentor.rag.ChromaDbClient;
import com.upscmentor.rag.DocumentProcessor;
import com.upscmentor.rag.EmbeddingService;
import com.upscmentor.rag.HybridRetrievalEngine;
import com.upscmentor.rag.HybridRetrievalEngine.RetrievalResult;
import com.upscmentor.rag.RagConfig;
import com.upscmentor.repository.KnowledgeBaseDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class KnowledgeBaseService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseService.class);

    private final KnowledgeBaseDocumentRepository documentRepository;
    private final DocumentProcessor documentProcessor;
    private final EmbeddingService embeddingService;
    private final ChromaDbClient chromaDbClient;
    private final HybridRetrievalEngine retrievalEngine;
    private final RagConfig ragConfig;

    public KnowledgeBaseService(KnowledgeBaseDocumentRepository documentRepository,
                                 DocumentProcessor documentProcessor,
                                 EmbeddingService embeddingService,
                                 ChromaDbClient chromaDbClient,
                                 HybridRetrievalEngine retrievalEngine,
                                 RagConfig ragConfig) {
        this.documentRepository = documentRepository;
        this.documentProcessor = documentProcessor;
        this.embeddingService = embeddingService;
        this.chromaDbClient = chromaDbClient;
        this.retrievalEngine = retrievalEngine;
        this.ragConfig = ragConfig;
    }

    @Transactional
    public KnowledgeBaseDocument uploadFile(Long userId, MultipartFile file) throws Exception {
        validateFile(file);

        if (documentRepository.existsByUserIdAndFilename(userId, file.getOriginalFilename())) {
            throw new IllegalArgumentException("File already uploaded: " + file.getOriginalFilename());
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String fileType = resolveFileType(extension);

        KnowledgeBaseDocument doc = new KnowledgeBaseDocument();
        doc.setUserId(userId);
        doc.setFilename(file.getOriginalFilename());
        doc.setFileType(fileType);
        doc.setStatus("UPLOADING");
        doc.setFileSizeKb(file.getSize() / 1024);
        doc = documentRepository.save(doc);

        String dirPath = ragConfig.getStoragePath() + "/" + userId + "/" + doc.getId();
        Path dir = Paths.get(dirPath);
        Files.createDirectories(dir);

        String filePath = dirPath + "/" + file.getOriginalFilename();
        file.transferTo(new File(filePath));
        doc.setFilePath(filePath);
        doc.setStatus("PROCESSING");
        documentRepository.save(doc);

        processDocumentAsync(doc.getId());

        return doc;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ragConfig.isTypeAllowed(extension)) {
            throw new IllegalArgumentException("File type not allowed: " + extension + ". Allowed: " + ragConfig.getAllowedTypes());
        }

        long maxSizeBytes = (long) ragConfig.getMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File too large: " + (file.getSize() / 1024 / 1024) + "MB. Max: " + ragConfig.getMaxSizeMb() + "MB");
        }
    }

    private String getFileExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(dot + 1) : "";
    }

    private String resolveFileType(String extension) {
        return switch (extension.toLowerCase()) {
            case "pdf" -> "PDF";
            case "txt", "md" -> "TEXT";
            case "png", "jpg", "jpeg" -> "IMAGE";
            default -> throw new IllegalArgumentException("Unknown extension: " + extension);
        };
    }

    @Async
    public void processDocumentAsync(Long documentId) {
        KnowledgeBaseDocument doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) return;

        try {
            doc.setStatus("PROCESSING");
            documentRepository.save(doc);

            File file = new File(doc.getFilePath());
            String text = documentProcessor.extractText(file, doc.getFileType());

            if (text.isBlank() || text.length() < 10) {
                doc.setStatus("FAILED");
                doc.setErrorMessage("Could not extract meaningful text from file");
                documentRepository.save(doc);
                return;
            }

            List<DocumentProcessor.Chunk> chunks = documentProcessor.chunkText(text, doc.getFilename());

            if (chunks.isEmpty()) {
                doc.setStatus("FAILED");
                doc.setErrorMessage("No chunks created from document");
                documentRepository.save(doc);
                return;
            }

            List<String> chunkTexts = chunks.stream().map(c -> c.content).toList();
            List<float[]> embeddings = embeddingService.embedAll(chunkTexts);

            List<String> ids = new ArrayList<>();
            List<ObjectNode> metadatas = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();

            for (int i = 0; i < chunks.size(); i++) {
                DocumentProcessor.Chunk chunk = chunks.get(i);
                ids.add(doc.getId() + "_chunk_" + i);

                ObjectNode meta = mapper.createObjectNode();
                meta.put("user_id", String.valueOf(doc.getUserId()));
                meta.put("document_id", String.valueOf(doc.getId()));
                meta.put("chunk_index", String.valueOf(i));
                meta.put("source", chunk.source);
                meta.put("page", chunk.page);
                meta.put("file_type", doc.getFileType());
                metadatas.add(meta);
            }

            boolean stored = chromaDbClient.addEmbeddings(ids, embeddings, chunkTexts, metadatas);

            if (!stored) {
                doc.setStatus("FAILED");
                doc.setErrorMessage("Failed to store embeddings in ChromaDB");
                documentRepository.save(doc);
                return;
            }

            doc.setStatus("READY");
            doc.setChunkCount(chunks.size());
            documentRepository.save(doc);

            logger.info("Document processed successfully: {} ({} chunks)", doc.getFilename(), chunks.size());

        } catch (Exception e) {
            logger.error("Failed to process document {}: {}", documentId, e.getMessage(), e);
            doc.setStatus("FAILED");
            doc.setErrorMessage(e.getMessage());
            documentRepository.save(doc);
        }
    }

    public List<KnowledgeBaseDocument> listDocuments(Long userId) {
        return documentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public boolean deleteDocument(Long userId, Long documentId) {
        KnowledgeBaseDocument doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null || !doc.getUserId().equals(userId)) {
            return false;
        }

        chromaDbClient.deleteByDocumentId(documentId);

        try {
            Path dir = Paths.get(doc.getFilePath()).getParent();
            if (dir != null && Files.exists(dir)) {
                Files.deleteIfExists(Paths.get(doc.getFilePath()));
                Files.deleteIfExists(dir);
            }
        } catch (Exception e) {
            logger.warn("Could not delete file from disk: {}", e.getMessage());
        }

        documentRepository.delete(doc);
        return true;
    }

    public String getPreview(Long userId, Long documentId) {
        KnowledgeBaseDocument doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null || !doc.getUserId().equals(userId)) {
            return null;
        }
        try {
            File file = new File(doc.getFilePath());
            String text = documentProcessor.extractText(file, doc.getFileType());
            return text.length() > 500 ? text.substring(0, 500) + "..." : text;
        } catch (Exception e) {
            return "Could not preview: " + e.getMessage();
        }
    }

    public Map<String, Object> getStats(Long userId) {
        long readyDocs = documentRepository.countReadyDocuments(userId);
        long totalChunks = documentRepository.totalChunksForUser(userId);
        List<KnowledgeBaseDocument> docs = documentRepository.findByUserIdAndStatus(userId, "READY");
        long totalSizeKb = docs.stream().mapToLong(d -> d.getFileSizeKb() != null ? d.getFileSizeKb() : 0).sum();

        return Map.of(
                "documentCount", readyDocs,
                "totalChunks", totalChunks,
                "storageUsedKb", totalSizeKb
        );
    }

    public boolean hasKnowledgeBase(Long userId) {
        return documentRepository.countReadyDocuments(userId) > 0;
    }
}
