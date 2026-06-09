package com.upscmentor.controller;

import com.upscmentor.model.dto.KnowledgeBaseResponse;
import com.upscmentor.model.entity.KnowledgeBaseDocument;
import com.upscmentor.service.KnowledgeBaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@CrossOrigin(origins = "*")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping("/upload")
    public ResponseEntity<KnowledgeBaseResponse> uploadFile(
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file) {

        try {
            KnowledgeBaseDocument doc = knowledgeBaseService.uploadFile(userId, file);
            Map<String, Object> docData = Map.of(
                    "id", doc.getId(),
                    "filename", doc.getFilename(),
                    "fileType", doc.getFileType(),
                    "status", doc.getStatus(),
                    "fileSizeKb", doc.getFileSizeKb()
            );
            return ResponseEntity.ok(KnowledgeBaseResponse.ok("File uploaded. Processing in background.", docData));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(KnowledgeBaseResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(KnowledgeBaseResponse.error("Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/documents")
    public ResponseEntity<KnowledgeBaseResponse> listDocuments(@RequestParam Long userId) {
        try {
            List<KnowledgeBaseDocument> docs = knowledgeBaseService.listDocuments(userId);
            return ResponseEntity.ok(KnowledgeBaseResponse.ok("Documents retrieved", docs));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(KnowledgeBaseResponse.error("Failed to list documents: " + e.getMessage()));
        }
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<KnowledgeBaseResponse> deleteDocument(@PathVariable Long id,
                                                                 @RequestParam Long userId) {
        try {
            boolean deleted = knowledgeBaseService.deleteDocument(userId, id);
            if (deleted) {
                return ResponseEntity.ok(KnowledgeBaseResponse.ok("Document deleted", null));
            } else {
                return ResponseEntity.badRequest().body(KnowledgeBaseResponse.error("Document not found or access denied"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(KnowledgeBaseResponse.error("Delete failed: " + e.getMessage()));
        }
    }

    @GetMapping("/documents/{id}/preview")
    public ResponseEntity<KnowledgeBaseResponse> previewDocument(@PathVariable Long id,
                                                                  @RequestParam Long userId) {
        try {
            String preview = knowledgeBaseService.getPreview(userId, id);
            if (preview == null) {
                return ResponseEntity.badRequest().body(KnowledgeBaseResponse.error("Document not found"));
            }
            return ResponseEntity.ok(KnowledgeBaseResponse.ok("Preview retrieved", Map.of("text", preview)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(KnowledgeBaseResponse.error("Preview failed: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<KnowledgeBaseResponse> getStats(@RequestParam Long userId) {
        try {
            Map<String, Object> stats = knowledgeBaseService.getStats(userId);
            return ResponseEntity.ok(KnowledgeBaseResponse.ok("Stats retrieved", stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(KnowledgeBaseResponse.error("Stats failed: " + e.getMessage()));
        }
    }
}
