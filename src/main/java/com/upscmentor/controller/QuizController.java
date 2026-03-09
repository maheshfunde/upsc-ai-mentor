package com.upscmentor.controller;

import com.upscmentor.model.dto.QuizRequest;
import com.upscmentor.model.entity.QuizResult;
import com.upscmentor.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * POST /api/quiz/generate - Generate quiz questions
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateQuiz(@RequestBody QuizRequest request) {
        String quizData = quizService.generateQuiz(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "quiz", quizData
        ));
    }

    /**
     * POST /api/quiz/submit - Submit quiz results
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitQuiz(
            @RequestParam Long userId,
            @RequestBody QuizRequest request,
            @RequestParam int correctAnswers,
            @RequestParam int totalQuestions) {

        QuizResult result = quizService.saveQuizResult(
                userId, request, correctAnswers, totalQuestions);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "result", result,
                "message", "Quiz submitted successfully!"
        ));
    }

    /**
     * GET /api/quiz/history/{userId} - Get quiz history
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<QuizResult>> getHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getUserQuizHistory(userId));
    }

    /**
     * GET /api/quiz/performance/{userId} - Get performance summary
     */
    @GetMapping("/performance/{userId}")
    public ResponseEntity<Map<String, Object>> getPerformance(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getPerformanceSummary(userId));
    }
}