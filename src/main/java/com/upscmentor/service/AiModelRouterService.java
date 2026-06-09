package com.upscmentor.service;

import com.upscmentor.config.AiConfig.OllamaModelFactory;
import com.upscmentor.model.entity.User;
import com.upscmentor.rag.HybridRetrievalEngine;
import com.upscmentor.rag.HybridRetrievalEngine.RetrievalResult;
import com.upscmentor.service.KnowledgeBaseService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiModelRouterService {

    private static final Logger logger = LoggerFactory.getLogger(AiModelRouterService.class);

    private final ChatLanguageModel localModel;
    private final OllamaModelFactory ollamaModelFactory;
    private final HybridRetrievalEngine retrievalEngine;
    private final KnowledgeBaseService knowledgeBaseService;

    @Value("${ai.online.default-model:gpt-4o-mini}")
    private String defaultOnlineModel;

    @Value("${ai.online.openai.base-url:https://api.openai.com/v1}")
    private String defaultBaseUrl;

    @Value("${ai.online.temperature:0.7}")
    private double onlineTemperature;

    @Value("${ai.online.timeout:180}")
    private int onlineTimeout;

    public AiModelRouterService(ChatLanguageModel localModel,
                                 OllamaModelFactory ollamaModelFactory,
                                 HybridRetrievalEngine retrievalEngine,
                                 KnowledgeBaseService knowledgeBaseService) {
        this.localModel = localModel;
        this.ollamaModelFactory = ollamaModelFactory;
        this.retrievalEngine = retrievalEngine;
        this.knowledgeBaseService = knowledgeBaseService;
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

    /**
     * Generate response with RAG context.
     */
    public RagResponse generateWithRag(User user, String prompt, String query) {
        if (!knowledgeBaseService.hasKnowledgeBase(user.getId())) {
            return new RagResponse(generate(user, prompt), List.of());
        }

        List<RetrievalResult> chunks = retrievalEngine.retrieve(query, user.getId());

        if (chunks.isEmpty()) {
            return new RagResponse(generate(user, prompt), List.of());
        }

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("\n\n<context_documents>\n");
        List<String> sources = new ArrayList<>();

        for (RetrievalResult chunk : chunks) {
            String pageRef = chunk.page != null && !chunk.page.equals("1") ? " (page " + chunk.page + ")" : "";
            contextBuilder.append("  <document source=\"").append(chunk.source).append(pageRef).append("\">\n");
            contextBuilder.append("    ").append(chunk.content.replace("\n", "\n    ")).append("\n");
            contextBuilder.append("  </document>\n");

            String sourceLabel = chunk.source + (pageRef.isEmpty() ? "" : pageRef);
            if (!sources.contains(sourceLabel)) {
                sources.add(sourceLabel);
            }
        }

        contextBuilder.append("</context_documents>\n\n");
        contextBuilder.append("Instructions: Use the context documents above to provide accurate, specific answers. ");
        contextBuilder.append("If the context contains relevant information, cite the source and page number. ");
        contextBuilder.append("If the context does not contain relevant information, answer from your general knowledge.\n\n");

        String ragPrompt = contextBuilder.toString() + prompt;

        logger.info("RAG-augmented prompt for user '{}' with {} sources", user.getUsername(), sources.size());

        String response = generate(user, ragPrompt);
        return new RagResponse(response, sources);
    }

    public static class RagResponse {
        private final String text;
        private final List<String> sources;

        public RagResponse(String text, List<String> sources) {
            this.text = text;
            this.sources = sources;
        }

        public String getText() { return text; }
        public List<String> getSources() { return sources; }
        public boolean hasSources() { return !sources.isEmpty(); }
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
}
