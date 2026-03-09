package com.upscmentor.service;

import com.upscmentor.model.entity.User;
import com.upscmentor.model.enums.Subject;
import com.upscmentor.prompts.SystemPrompts;
import org.springframework.stereotype.Service;

@Service
public class PromptService {

    private static final String STRUCTURED_RESPONSE_INSTRUCTIONS = """
            Response Instructions (STRICT):
            - Respond in clear markdown with the following section headings in order:
              1. ## Summary
              2. ## Key Points
              3. ## Examples
              4. ## Exam Relevance
              5. ## Quick Revision
            - Under "Key Points", use concise bullet points.
            - Under "Exam Relevance", include:
              - Prelims Focus
              - Mains Focus
            - Under "Quick Revision", add 3-5 one-line takeaway bullets.
            - Keep response practical, UPSC-oriented, and avoid very long paragraphs.
            - If user asks a very short direct fact question, still keep headings but concise.
            """;

    /**
     * Build a complete prompt with subject-specific system prompt + user context + message
     */
    public String buildSubjectPrompt(User user, Subject subject,
                                     String conversationHistory, String userMessage) {

        String systemPrompt = SystemPrompts.getPromptForSubject(subject.name());

        StringBuilder prompt = new StringBuilder();
        prompt.append("System: ").append(systemPrompt).append("\n\n");

        // Add user context
        prompt.append("Student Profile:\n");
        prompt.append("- Name: ").append(user.getName()).append("\n");
        prompt.append("- Preparation Level: ").append(user.getDifficultyLevel().getDisplayName()).append("\n");
        prompt.append("- Optional Subject: ").append(user.getOptionalSubject().getDisplayName()).append("\n");
        prompt.append("- Target Year: ").append(user.getTargetYear()).append("\n");

        if (user.getWeakSubjects() != null) {
            prompt.append("- Weak Areas: ").append(user.getWeakSubjects()).append("\n");
        }
        prompt.append("\n");

        // Add conversation history
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            prompt.append("Previous conversation:\n").append(conversationHistory).append("\n\n");
        }

        prompt.append(STRUCTURED_RESPONSE_INSTRUCTIONS).append("\n\n");
        prompt.append("Student: ").append(userMessage).append("\nMentor:");

        return prompt.toString();
    }

    /**
     * Build prompt for optional subject
     */
    public String buildOptionalSubjectPrompt(User user, String conversationHistory,
                                             String userMessage) {

        String systemPrompt = SystemPrompts.getOptionalSubjectPrompt(
                user.getOptionalSubject().getDisplayName());

        StringBuilder prompt = new StringBuilder();
        prompt.append("System: ").append(systemPrompt).append("\n\n");

        prompt.append("Student Profile:\n");
        prompt.append("- Name: ").append(user.getName()).append("\n");
        prompt.append("- Optional Subject: ").append(user.getOptionalSubject().getDisplayName()).append("\n");
        prompt.append("- Preparation Level: ").append(user.getDifficultyLevel().getDisplayName()).append("\n\n");

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            prompt.append("Previous conversation:\n").append(conversationHistory).append("\n\n");
        }

        prompt.append(STRUCTURED_RESPONSE_INSTRUCTIONS).append("\n\n");
        prompt.append("Student: ").append(userMessage).append("\nMentor:");

        return prompt.toString();
    }
}
