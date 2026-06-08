package com.upscmentor.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Value("${ai.ollama.base-url}")
    private String baseUrl;

    @Value("${ai.ollama.model-name}")
    private String modelName;

    @Value("${ai.ollama.temperature}")
    private double temperature;

    @Value("${ai.ollama.timeout}")
    private int timeout;

    @Bean
    public OllamaModelFactory ollamaModelFactory() {
        return new OllamaModelFactory(baseUrl, modelName, temperature, timeout);
    }

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return ollamaModelFactory().create(null);
    }

    public static class OllamaModelFactory {
        private final String baseUrl;
        private final String defaultModelName;
        private final double temperature;
        private final int timeout;

        public OllamaModelFactory(String baseUrl, String defaultModelName, double temperature, int timeout) {
            this.baseUrl = baseUrl;
            this.defaultModelName = defaultModelName;
            this.temperature = temperature;
            this.timeout = timeout;
        }

        public ChatLanguageModel create(String requestedModelName) {
            String resolvedModelName = requestedModelName != null && !requestedModelName.isBlank()
                    ? requestedModelName.trim()
                    : defaultModelName;

            return OllamaChatModel.builder()
                    .baseUrl(baseUrl)
                    .modelName(resolvedModelName)
                    .temperature(temperature)
                    .timeout(Duration.ofSeconds(timeout))
                    .build();
        }

        public String getDefaultModelName() {
            return defaultModelName;
        }
    }
}
