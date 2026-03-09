package com.upscmentor.service;

import com.upscmentor.model.dto.AnswerEvaluationRequest;
import com.upscmentor.model.dto.AnswerEvaluationResponse;
import com.upscmentor.model.entity.User;
import com.upscmentor.prompts.SubjectPrompts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class AnswerEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(AnswerEvaluationService.class);

    private final AiModelRouterService aiModelRouterService;
    private final UserService userService;

    public AnswerEvaluationService(AiModelRouterService aiModelRouterService, UserService userService) {
        this.aiModelRouterService = aiModelRouterService;
        this.userService = userService;
    }

    /**
     * Evaluate a student's answer using AI
     */
    public AnswerEvaluationResponse evaluateAnswer(AnswerEvaluationRequest request) {
        try {
            User user = userService.getUserById(request.getUserId());

            String subjectName;
            if (request.isOptionalSubject()) {
                subjectName = user.getOptionalSubject().getDisplayName();
            } else {
                subjectName = request.getSubject() != null
                        ? request.getSubject().getDisplayName()
                        : "General Studies";
            }

            int wordLimit = request.getWordLimit() > 0 ? request.getWordLimit() : 250;

            String prompt = SubjectPrompts.answerEvaluationPrompt(
                    request.getQuestion(),
                    request.getUserAnswer(),
                    subjectName,
                    wordLimit
            );

            logger.info("Evaluating answer for user: {}, subject: {}",
                    user.getUsername(), subjectName);

            String aiResponse = aiModelRouterService.generate(user, prompt);

            // Parse the AI response
            return parseEvaluationResponse(aiResponse);

        } catch (Exception e) {
            logger.error("Error evaluating answer: {}", e.getMessage(), e);
            AnswerEvaluationResponse errorResponse = new AnswerEvaluationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError("Failed to evaluate answer: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Parse the AI evaluation response
     * Handles both JSON and plain text responses
     */
    private AnswerEvaluationResponse parseEvaluationResponse(String aiResponse) {
        AnswerEvaluationResponse response = new AnswerEvaluationResponse();

        try {
            // Try to extract JSON from the response
            String jsonStr = extractJson(aiResponse);

            if (jsonStr != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(jsonStr);

                response.setOverallScore(getIntValue(jsonNode, "overallScore", 5));
                response.setContentScore(getIntValue(jsonNode, "contentScore", 5));
                response.setStructureScore(getIntValue(jsonNode, "structureScore", 5));
                response.setAnalyticalScore(getIntValue(jsonNode, "analyticalScore", 5));
                response.setStrengths(getTextValue(jsonNode, "strengths", "Not available"));
                response.setWeaknesses(getTextValue(jsonNode, "weaknesses", "Not available"));
                response.setSuggestions(getTextValue(jsonNode, "suggestions", "Not available"));
                response.setModelAnswer(getTextValue(jsonNode, "modelAnswer", "Not available"));
                response.setDimensionsMissed(getTextValue(jsonNode, "dimensionsMissed", "None identified"));
            } else {
                // Fallback: Use the raw text response
                response.setOverallScore(5);
                response.setContentScore(5);
                response.setStructureScore(5);
                response.setAnalyticalScore(5);
                response.setSuggestions(aiResponse);
                response.setModelAnswer("AI evaluation provided in text format.");
            }

            response.setSuccess(true);

        } catch (Exception e) {
            logger.warn("Failed to parse JSON evaluation, using raw response: {}", e.getMessage());
            response.setOverallScore(5);
            response.setSuggestions(aiResponse);
            response.setSuccess(true);
        }

        return response;
    }

    /**
     * Extract JSON from AI response (might be wrapped in markdown code blocks)
     */
    private String extractJson(String text) {
        // Try to find JSON between ```json and ```
        int jsonStart = text.indexOf("```json");
        int jsonEnd = text.lastIndexOf("```");

        if (jsonStart != -1 && jsonEnd > jsonStart) {
            return text.substring(jsonStart + 7, jsonEnd).trim();
        }

        // Try to find JSON between { and }
        int braceStart = text.indexOf("{");
        int braceEnd = text.lastIndexOf("}");

        if (braceStart != -1 && braceEnd > braceStart) {
            return text.substring(braceStart, braceEnd + 1).trim();
        }

        return null;
    }

    private int getIntValue(JsonNode node, String field, int defaultValue) {
        return node.has(field) ? node.get(field).asInt(defaultValue) : defaultValue;
    }

    private String getTextValue(JsonNode node, String field, String defaultValue) {
        return node.has(field) ? node.get(field).asText(defaultValue) : defaultValue;
    }

    /**
     * Generate a practice question for a given subject
     */
    public String generatePracticeQuestion(Long userId, String subject, boolean isOptional) {
        try {
            User user = userService.getUserById(userId);

            String subjectName = isOptional
                    ? user.getOptionalSubject().getDisplayName()
                    : subject;

            String prompt = """
                    Generate ONE UPSC Mains-style practice question for:
                    Subject: %s
                    Student Level: %s
                    
                    Format:
                    - Question should be analytical/opinion-based (not factual)
                    - Mention the word limit (150 words or 250 words)
                    - Mention the marks (10 or 15)
                    - Add a hint about what dimensions to cover
                    
                    Example format:
                    **Question:** [Question text] (250 words, 15 marks)
                    
                    **Hint:** Cover these dimensions - [list dimensions]
                    **Key terms to use:** [list important terms]
                    """.formatted(subjectName, user.getDifficultyLevel().getDisplayName());

            return aiModelRouterService.generate(user, prompt);

        } catch (Exception e) {
            logger.error("Error generating practice question: {}", e.getMessage());
            return "Error generating question. Please try again.";
        }
    }
}
