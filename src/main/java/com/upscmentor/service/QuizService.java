package com.upscmentor.service;

import com.upscmentor.model.dto.QuizRequest;
import com.upscmentor.model.entity.QuizResult;
import com.upscmentor.model.entity.User;
import com.upscmentor.prompts.SubjectPrompts;
import com.upscmentor.repository.QuizResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);

    private final AiModelRouterService aiModelRouterService;
    private final QuizResultRepository quizResultRepository;
    private final UserService userService;

    public QuizService(AiModelRouterService aiModelRouterService,
                       QuizResultRepository quizResultRepository,
                       UserService userService) {
        this.aiModelRouterService = aiModelRouterService;
        this.quizResultRepository = quizResultRepository;
        this.userService = userService;
    }

    /**
     * Generate quiz questions using AI
     */
    public String generateQuiz(QuizRequest request) {
        try {
            User user = userService.getUserById(request.getUserId());

            String subjectName;
            if (request.isOptionalSubject()) {
                subjectName = user.getOptionalSubject().getDisplayName();
            } else {
                subjectName = request.getSubject().getDisplayName();
            }

            String topic = request.getSpecificTopic() != null
                    ? request.getSpecificTopic()
                    : "General " + subjectName;

            String difficulty = request.getDifficulty() != null
                    ? request.getDifficulty().getDisplayName()
                    : "Intermediate";

            int numberOfQuestions = request.getNumberOfQuestions() > 0
                    ? request.getNumberOfQuestions()
                    : 5;

            String prompt = SubjectPrompts.quizGenerationPrompt(
                    subjectName, topic, numberOfQuestions, difficulty);

            logger.info("Generating {} quiz questions for subject: {}, topic: {}",
                    numberOfQuestions, subjectName, topic);

            String quizResponse = aiModelRouterService.generate(user, prompt);

            logger.info("Quiz generated successfully for user: {}", user.getUsername());

            return quizResponse;

        } catch (Exception e) {
            logger.error("Error generating quiz: {}", e.getMessage(), e);
            return "{\"error\": \"Failed to generate quiz. Please try again.\"}";
        }
    }

    /**
     * Save quiz result after user completes a quiz
     */
    public QuizResult saveQuizResult(Long userId, QuizRequest request,
                                     int correctAnswers, int totalQuestions) {
        QuizResult result = new QuizResult();
        result.setUserId(userId);
        result.setSubject(request.getSubject());
        result.setTotalQuestions(totalQuestions);
        result.setCorrectAnswers(correctAnswers);
        result.setScorePercentage((double) correctAnswers / totalQuestions * 100);

        // Generate AI feedback based on performance
        String feedback = generateQuizFeedback(request, correctAnswers, totalQuestions);
        result.setFeedback(feedback);

        return quizResultRepository.save(result);
    }

    /**
     * Generate personalized feedback based on quiz performance
     */
    private String generateQuizFeedback(QuizRequest request,
                                        int correctAnswers, int totalQuestions) {
        User user = userService.getUserById(request.getUserId());
        double percentage = (double) correctAnswers / totalQuestions * 100;

        String prompt = """
                A UPSC aspirant just completed a Prelims-style MCQ quiz on %s.

                Results:
                - Correct: %d out of %d
                - Score: %.1f%%
                - Difficulty: %s
                - Topic: %s

                UPSC Context:
                - In actual Prelims, negative marking of 1/3rd applies (0.83 marks deducted per wrong answer)
                - A score above 60%% in a quiz typically indicates Prelims-ready preparation
                - A score below 40%% indicates need for foundational revision

                Provide feedback in this structure:
                1. 📊 Performance Assessment — compare score to UPSC Prelims cut-off trends
                2. 🔍 Weak Areas Identified — what topics/concepts need revision
                3. 📚 Recommended Action — specific next steps (NCERT revision, standard book chapter, PYQ practice)
                4. 🎯 Score-Specific Guidance:
                   - If below 40%%: Focus on NCERT foundation + concept building
                   - If 40-60%%: Standard book revision + targeted PYQ practice
                   - If 60-80%%: Test series integration + current affairs linkage
                   - If above 80%%: Maintain consistency + full-length mock tests

                Keep it under 200 words. Be constructive but honest about gaps.
                """.formatted(
                request.getSubject().getDisplayName(),
                correctAnswers, totalQuestions, percentage,
                request.getDifficulty().getDisplayName(),
                request.getSpecificTopic()
        );

        try {
            return aiModelRouterService.generate(user, prompt);
        } catch (Exception e) {
            logger.error("Error generating feedback: {}", e.getMessage());
            return "Quiz completed! Score: " + correctAnswers + "/" + totalQuestions;
        }
    }

    /**
     * Get user's quiz history
     */
    public List<QuizResult> getUserQuizHistory(Long userId) {
        return quizResultRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get average score for a user
     */
    public Double getAverageScore(Long userId) {
        return quizResultRepository.findAverageScoreByUserId(userId);
    }

    /**
     * Get average score for user in specific subject
     */
    public Double getSubjectAverageScore(Long userId, com.upscmentor.model.enums.Subject subject) {
        return quizResultRepository.findAverageScoreByUserIdAndSubject(userId, subject);
    }

    /**
     * Get performance summary across all subjects
     */
    public Map<String, Object> getPerformanceSummary(Long userId) {
        List<QuizResult> results = getUserQuizHistory(userId);
        Double avgScore = getAverageScore(userId);

        int totalQuizzes = results.size();
        int totalQuestions = results.stream()
                .mapToInt(QuizResult::getTotalQuestions).sum();
        int totalCorrect = results.stream()
                .mapToInt(QuizResult::getCorrectAnswers).sum();

        return Map.of(
                "totalQuizzes", totalQuizzes,
                "totalQuestions", totalQuestions,
                "totalCorrect", totalCorrect,
                "averageScore", avgScore != null ? avgScore : 0.0,
                "recentResults", results.stream().limit(5).toList()
        );
    }
}
