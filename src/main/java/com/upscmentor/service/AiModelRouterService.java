package com.upscmentor.service;

import com.upscmentor.config.AiConfig.OllamaModelFactory;
import com.upscmentor.model.entity.User;
import dev.langchain4j.model.chat.ChatLanguageModel;
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
    private final OllamaModelFactory ollamaModelFactory;

    @Value("${ai.online.default-model:gpt-4o-mini}")
    private String defaultOnlineModel;

    @Value("${ai.online.openai.base-url:https://api.openai.com/v1}")
    private String defaultBaseUrl;

    @Value("${ai.online.temperature:0.7}")
    private double onlineTemperature;

    @Value("${ai.online.timeout:180}")
    private int onlineTimeout;

    public AiModelRouterService(ChatLanguageModel localModel, OllamaModelFactory ollamaModelFactory) {
        this.localModel = localModel;
        this.ollamaModelFactory = ollamaModelFactory;
    }

    public String generate(User user, String prompt) {
        return generate(user, prompt, null);
    }

    public String generate(User user, String prompt, String requestedLocalModelName) {
        String localModelOverride = normalizeModelName(requestedLocalModelName);
        if (localModelOverride != null) {
            logger.info("Using requested local Ollama model '{}' for user '{}'",
                    localModelOverride, user.getUsername());
            return ollamaModelFactory.create(localModelOverride).generate(prompt);
        }

        if (hasOnlineConfig(user)) {
            String apiKey = user.getOpenAiApiKey().trim();
            String baseUrl = resolveBaseUrl(user);
            String modelName = resolveModelName(user);

            try {
                ChatLanguageModel onlineModel = OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .baseUrl(baseUrl)
                        .modelName(modelName)
                        .temperature(onlineTemperature)
                        .timeout(Duration.ofSeconds(onlineTimeout))
                        .build();

                logger.info("Using online provider at base URL '{}' with model '{}' for user '{}'",
                        baseUrl, modelName, user.getUsername());
                return onlineModel.generate(prompt);
            } catch (Exception e) {
                logger.warn("Online model failed for user '{}'. Falling back to local model. Error: {}",
                        user.getUsername(), e.getMessage());
            }
        }

        String localModelName = resolveLocalModelName(user);
        if (localModelName == null) {
            logger.info("Using local Ollama model '{}' for user '{}'",
                    ollamaModelFactory.getDefaultModelName(), user.getUsername());
            return localModel.generate(prompt);
        }

        logger.info("Using local Ollama model '{}' for user '{}'", localModelName, user.getUsername());
        return ollamaModelFactory.create(localModelName).generate(prompt);
    }

    private boolean hasOnlineConfig(User user) {
        return user != null
                && user.getOpenAiApiKey() != null
                && !user.getOpenAiApiKey().isBlank();
    }

    private String resolveBaseUrl(User user) {
        if (user.getOnlineBaseUrl() != null && !user.getOnlineBaseUrl().isBlank()) {
            return user.getOnlineBaseUrl().trim();
        }
        return defaultBaseUrl;
    }

    private String resolveModelName(User user) {
        String userModel = user.getOnlineModelName();
        if (userModel != null && !userModel.isBlank()) {
            String model = userModel.trim();
            if (isSafetyOrGuardModel(model)) {
                logger.warn("Configured model '{}' looks like a safety/guard model. Using '{}' instead.",
                        model, defaultOnlineModel);
                return defaultOnlineModel;
            }
            logger.debug("Using user-configured model '{}'", model);
            return model;
        }
        logger.info("No model name configured for user '{}', falling back to default '{}'",
                user.getUsername(), defaultOnlineModel);
        return defaultOnlineModel;
    }

    private boolean isSafetyOrGuardModel(String model) {
        String m = model.toLowerCase();
        return m.contains("guard") || m.contains("moderation") || m.contains("safety");
    }

    private String resolveLocalModelName(User user) {
        if (user == null) {
            return null;
        }
        return normalizeModelName(user.getLocalModelName());
    }

    private String normalizeModelName(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return null;
        }
        return modelName.trim();
    }
}
