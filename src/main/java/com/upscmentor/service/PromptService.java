package com.upscmentor.service;

import com.upscmentor.model.entity.User;
import com.upscmentor.model.enums.Subject;
import com.upscmentor.prompts.SystemPrompts;
import org.springframework.stereotype.Service;

@Service
public class PromptService {

    private static final String LANGUAGE_INSTRUCTIONS_EN = "";

    private static final String LANGUAGE_INSTRUCTIONS_MR = """

            <language_instruction>
            Respond entirely in MARATHI (मराठी) language. Use Devanagari script.
            Keep technical UPSC terms (like Article, Amendment, Fundamental Rights) in English
            with Marathi explanations in parentheses. This helps students prepare for the exam
            while understanding concepts in their native language.
            </language_instruction>
            """;

    private static final String LANGUAGE_INSTRUCTIONS_BILINGUAL = """

            <language_instruction>
            Respond in BILINGUAL format — provide each section first in English,
            then provide a Marathi (मराठी) translation/explanation immediately below it.
            Use Devanagari script for Marathi text. Keep technical UPSC terms in English
            with Marathi explanations. Format each section as:

            ## English Section Title
            [English content]

            🇮🇳 मराठी स्पष्टीकरण
            [Marathi translation/explanation]
            </language_instruction>
            """;

    /**
     * Build a complete prompt with subject-specific system prompt + user context + message
     */
    public String buildSubjectPrompt(User user, Subject subject,
                                     String conversationSummary,
                                     String conversationHistory, String userMessage,
                                     String responseLanguage) {

        String systemPrompt = SystemPrompts.getPromptForSubject(subject.name());

        StringBuilder prompt = new StringBuilder();
        prompt.append("<system>").append(systemPrompt).append("</system>\n\n");

        prompt.append("<student_profile>\n");
        prompt.append("- Name: ").append(user.getName()).append("\n");
        prompt.append("- Preparation Level: ").append(user.getDifficultyLevel().getDisplayName()).append("\n");
        prompt.append("- Optional Subject: ").append(
                user.getOptionalSubject() != null
                    ? user.getOptionalSubject().getDisplayName()
                    : "Not selected").append("\n");
        prompt.append("- Target Year: ").append(user.getTargetYear()).append("\n");
        if (user.getWeakSubjects() != null) {
            prompt.append("- Weak Areas: ").append(user.getWeakSubjects()).append("\n");
        }
        prompt.append("</student_profile>\n\n");

        if (conversationSummary != null && !conversationSummary.isEmpty()) {
            prompt.append("<conversation_summary>\n").append(conversationSummary).append("\n</conversation_summary>\n\n");
        }

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            prompt.append("<recent_conversation>\n").append(conversationHistory).append("\n</recent_conversation>\n\n");
        }

        prompt.append("<response_format>\n")
              .append(SystemPrompts.STRUCTURED_OUTPUT_FORMAT)
              .append("</response_format>\n\n");

        prompt.append(getLanguageInstruction(responseLanguage));

        prompt.append("<student>").append(userMessage).append("</student>\n<mentor>:");

        return prompt.toString();
    }

    /**
     * Build prompt for optional subject
     */
    public String buildOptionalSubjectPrompt(User user, String conversationSummary,
                                             String conversationHistory, String userMessage,
                                             String responseLanguage) {

        if (user.getOptionalSubject() == null) {
            throw new IllegalStateException("User has no optional subject configured");
        }

        String systemPrompt = SystemPrompts.getOptionalSubjectPrompt(
                user.getOptionalSubject().getDisplayName());

        StringBuilder prompt = new StringBuilder();
        prompt.append("<system>").append(systemPrompt).append("</system>\n\n");

        prompt.append("<student_profile>\n");
        prompt.append("- Name: ").append(user.getName()).append("\n");
        prompt.append("- Optional Subject: ").append(
                user.getOptionalSubject() != null
                    ? user.getOptionalSubject().getDisplayName()
                    : "Not selected").append("\n");
        prompt.append("- Preparation Level: ").append(user.getDifficultyLevel().getDisplayName()).append("\n");
        prompt.append("</student_profile>\n\n");

        if (conversationSummary != null && !conversationSummary.isEmpty()) {
            prompt.append("<conversation_summary>\n").append(conversationSummary).append("\n</conversation_summary>\n\n");
        }

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            prompt.append("<recent_conversation>\n").append(conversationHistory).append("\n</recent_conversation>\n\n");
        }

        prompt.append("<response_format>\n")
              .append(SystemPrompts.STRUCTURED_OUTPUT_FORMAT)
              .append("</response_format>\n\n");

        prompt.append(getLanguageInstruction(responseLanguage));

        prompt.append("<student>").append(userMessage).append("</student>\n<mentor>:");

        return prompt.toString();
    }

    private String getLanguageInstruction(String language) {
        if (language == null) return LANGUAGE_INSTRUCTIONS_EN;
        return switch (language.toLowerCase()) {
            case "mr" -> LANGUAGE_INSTRUCTIONS_MR;
            case "bilingual" -> LANGUAGE_INSTRUCTIONS_BILINGUAL;
            default -> LANGUAGE_INSTRUCTIONS_EN;
        };
    }
}
