package com.upscmentor.service;

import com.upscmentor.model.entity.User;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AiModelRouterService {

    private static final Logger logger = LoggerFactory.getLogger(AiModelRouterService.class);

    private final ChatLanguageModel localModel;

    @Value("${ai.online.default-model:gpt-4o-mini}")
    private String defaultOnlineModel;

    @Value("${ai.online.openai.base-url:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    @Value("${ai.online.groq.base-url:https://api.groq.com/openai/v1}")
    private String groqBaseUrl;

    @Value("${ai.online.groq.default-model:llama-3.3-70b-versatile}")
    private String defaultGroqModel;

    @Value("${ai.online.temperature:0.7}")
    private double onlineTemperature;

    @Value("${ai.online.timeout:180}")
    private int onlineTimeout;

    public AiModelRouterService(ChatLanguageModel localModel) {
        this.localModel = localModel;
    }

    public String generate(User user, String prompt) {
        return generate(user, prompt, null);
    }

    public String generate(User user, String prompt, String requestedLocalModelName) {
        String localModelOverride = normalizeModelName(requestedLocalModelName);
        if (localModelOverride != null) {
            logger.info("Using requested local Ollama model '{}' for user '{}'",
                    localModelOverride, user.getUsername());
            return OllamaChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName(localModelOverride)
                    .timeout(Duration.ofSeconds(180))
                    .build()
                    .generate(prompt);
        }

        if (hasOnlineConfig(user)) {
            String apiKey = user.getOpenAiApiKey().trim();
            boolean isGroq = apiKey.startsWith("gsk_");
            String modelName = resolveModelName(user, isGroq);
            String baseUrl = isGroq ? groqBaseUrl : openAiBaseUrl;

            try {
                ChatLanguageModel onlineModel = OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .baseUrl(baseUrl)
                        .modelName(modelName)
                        .temperature(onlineTemperature)
                        .timeout(Duration.ofSeconds(onlineTimeout))
                        .build();

                logger.info("Using online provider '{}' model '{}' for user '{}'",
                        isGroq ? "GROQ" : "OPENAI", modelName, user.getUsername());
                return onlineModel.generate(prompt);
            } catch (Exception e) {
                logger.warn("Online model failed for user '{}'. Falling back to local model. Error: {}",
                        user.getUsername(), e.getMessage());
            }
        }

        logger.info("Using local Ollama model '{}' for user '{}'", "llama3:8b", user.getUsername());
        return localModel.generate(prompt);
    }

    private String normalizeModelName(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return null;
        }
        return modelName.trim();
    }

    private boolean hasOnlineConfig(User user) {
        return user != null
                && user.getOpenAiApiKey() != null
                && !user.getOpenAiApiKey().isBlank();
    }

    private String resolveModelName(User user, boolean isGroq) {
        String userModel = user.getOnlineModelName();
        if (userModel != null && !userModel.isBlank()) {
            String model = userModel.trim();
            if (isSafetyOrGuardModel(model)) {
                String fallback = isGroq ? defaultGroqModel : defaultOnlineModel;
                logger.warn("Configured model '{}' looks like a safety/guard model. Using '{}' instead.",
                        model, fallback);
                return fallback;
            }
            return model;
        }
        return isGroq ? defaultGroqModel : defaultOnlineModel;
    }

    private boolean isSafetyOrGuardModel(String model) {
        String m = model.toLowerCase();
        return m.contains("guard") || m.contains("moderation") || m.contains("safety");
    }
}
