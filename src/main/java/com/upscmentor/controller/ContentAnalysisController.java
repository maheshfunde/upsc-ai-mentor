package com.upscmentor.controller;

import com.upscmentor.model.dto.CurrentAffairsRequest;
import com.upscmentor.model.dto.PyqAnalysisRequest;
import com.upscmentor.service.ContentAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/content")
@CrossOrigin(origins = "*")
public class ContentAnalysisController {

    private final ContentAnalysisService contentAnalysisService;

    public ContentAnalysisController(ContentAnalysisService contentAnalysisService) {
        this.contentAnalysisService = contentAnalysisService;
    }

    @PostMapping("/current-affairs")
    public ResponseEntity<Map<String, Object>> analyzeCurrentAffairs(
            @Valid @RequestBody CurrentAffairsRequest request) {

        String result = contentAnalysisService.analyzeCurrentAffairs(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "analysis", result
        ));
    }

    @PostMapping("/pyq-analysis")
    public ResponseEntity<Map<String, Object>> analyzePyq(
            @Valid @RequestBody PyqAnalysisRequest request) {

        String result = contentAnalysisService.analyzePyq(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "analysis", result
        ));
    }
}
