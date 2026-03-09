package com.upscmentor.controller;

import com.upscmentor.model.dto.AnswerEvaluationRequest;
import com.upscmentor.model.dto.AnswerEvaluationResponse;
import com.upscmentor.service.AnswerEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/answer")
@CrossOrigin(origins = "*")
public class AnswerPracticeController {

    private final AnswerEvaluationService answerService;

    public AnswerPracticeController(AnswerEvaluationService answerService) {
        this.answerService = answerService;
    }

    /**
     * POST /api/answer/evaluate - Evaluate a written answer
     */
    @PostMapping("/evaluate")
    public ResponseEntity<AnswerEvaluationResponse> evaluateAnswer(
            @RequestBody AnswerEvaluationRequest request) {
        AnswerEvaluationResponse response = answerService.evaluateAnswer(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/answer/generate-question - Generate practice question
     */
    @GetMapping("/generate-question")
    public ResponseEntity<Map<String, String>> generateQuestion(
            @RequestParam Long userId,
            @RequestParam String subject,
            @RequestParam(defaultValue = "false") boolean isOptional) {

        String question = answerService.generatePracticeQuestion(userId, subject, isOptional);
        return ResponseEntity.ok(Map.of(
                "success", "true",
                "question", question
        ));
    }
}